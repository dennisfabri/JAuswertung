/*
 * Created on 21.01.2005
 */
package de.df.jauswertung.gui.plugins.http;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JToggleButton;

import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.MOptionenPlugin;
import de.df.jauswertung.gui.plugins.core.IWettkampfProvider;
import de.df.jauswertung.gui.plugins.core.JResultsSelectionButton;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.HttpUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri
 * @date 21.01.2005
 */
public class HttpServerPlugin extends ANullPlugin {

    private CorePlugin              core;
    MOptionenPlugin                 optionen;

    private ButtonInfo[]            quicks;
    private JToggleButton           button;
    private JResultsSelectionButton filter;
    private JButton                 clearcache;
    private HttpOptionsPlugin       httpOptions;

    private int                     port  = 80;

    private HttpServer   httpServer;

    private DataProvider            source;
    // private HttpDataProviderCache cache = null;

    boolean                         state = false;

    public HttpServerPlugin() {
        super();

        port = Utils.getPreferences().getInt("HttpServerPort", 80);

        button = new JToggleButton(IconManager.getSmallIcon("webserver"));
        button.setToolTipText(I18n.get("StartStopHttpServer"));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonAction();
            }
        });

        clearcache = new JButton(IconManager.getSmallIcon("clearcache"));
        clearcache.setToolTipText(I18n.get("clearsCache"));
        clearcache.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearCache();
            }
        });
        clearcache.setEnabled(false);

        filter = new JResultsSelectionButton(new IWettkampfProvider() {
            @Override
            public <T extends ASchwimmer> AWettkampf<T> getWettkampf() {
                return core.getWettkampf();
            }
        });
        filter.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (arg0.getStateChange() == ItemEvent.DESELECTED) {
                    source.setSelection(filter.getSelection(core.getWettkampf()));
                    clearCache();
                }
            }
        });
        filter.setEnabled(false);

        quicks = new ButtonInfo[2];
        quicks[0] = new ButtonInfo(button, 1000);
        quicks[1] = new ButtonInfo(filter, 1001);
        // quicks[1] = new ButtonInfo(clearcache, 1002);
    }

    void clearCache() {
        // if (cache != null) {
        // cache.clearCache();
        // }
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        if (core == null) {
            throw new NullPointerException();
        }
        optionen = (MOptionenPlugin) plugincontroller.getPlugin("de.df.jauswertung.options", pluginuid);
        if (optionen == null) {
            throw new NullPointerException();
        }
        httpOptions = new HttpOptionsPlugin(this);
        optionen.addOptionsPlugin(httpOptions);
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        super.dataUpdated(due);

        // if (cache != null) {
        // cache.notifyDataUpdate();
        // }
    }

    private DataProvider getDataProvider() {
        if (source == null) {
            source = new DataProvider(core);
            // cache = new HttpDataProviderCache(source);
        }
        return source;
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        return quicks;
    }

    public String registerDataProvider(IDataProvider dp) {
        getDataProvider();
        return source.registerDataProvider(dp);
    }

    public void unregisterDataProvider(String key) {
        getDataProvider();
        source.unregisterDataProvider(key);
    }

    public boolean isEnabled() {
        return state;
    }

    boolean startUp() {
        try {
            state = true;
            
            final SocketConfig socketConfig = SocketConfig.custom()
                    .setSoTimeout(15, TimeUnit.SECONDS)
                    .setTcpNoDelay(true)
                    .build();
            
            httpServer = ServerBootstrap.bootstrap()
                    .setListenerPort(port)
                    .setSocketConfig(socketConfig)
                    .setExceptionListener(new ExceptionListener() {
                        
                        @Override
                        public void onError(final Exception ex) {
                            ex.printStackTrace();
                        }

                        @Override
                        public void onError(final HttpConnection conn, final Exception ex) {
                            if (ex instanceof SocketTimeoutException) {
                                System.err.println("Connection timed out");
                            } else if (ex instanceof ConnectionClosedException) {
                                System.err.println(ex.getMessage());
                            } else {
                                ex.printStackTrace();
                            }
                        }

                    })
                    .register("*", new DPRequestHandler(getDataProvider()))
                    .create();            
            
            httpServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    httpServer.close(CloseMode.GRACEFUL);
                }
            });
            
            httpServer.start();
            
            httpOptions.setEnabled(false);
            filter.setEnabled(source.getExportMode() == ExportMode.Filtered);
            clearcache.setEnabled(true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtils.wichtigeMeldung(null, I18n.get("HttpServerNotStartet"));
            shutDown();
            httpServer = null;
            state = false;
            httpOptions.setEnabled(true);
            filter.setEnabled(false);
            clearcache.setEnabled(false);
            return false;
        }
    }

    @Override
    public void shutDown() {
        // if (cache != null) {
        // cache.clearCache();
        // }
        if (httpServer != null) {
            httpServer.close(CloseMode.GRACEFUL);

            try {
                // Send an HTTP-Request to activate the server thread
                HttpUtils.download("http://localhost:" + port + "/empty.txt");
            } catch (Exception e) {
                // Nothing to do
            }

            try {
                httpServer.awaitTermination(TimeValue.ofMilliseconds(500));
            } catch (InterruptedException e) {
                // Nothing to do
            }

            httpServer = null;
            state = false;
        }
        httpOptions.setEnabled(true);
        filter.setEnabled(false);
        clearcache.setEnabled(false);
    }

    void setPort(int p) {
        port = p;
        Utils.getPreferences().putInt("HttpServerPort", p);
    }

    public int getPort() {
        return port;
    }

    boolean isRunning() {
        return state;
    }

    ExportMode getExportMode() {
        getDataProvider();
        return source.getExportMode();
    }

    void setExportMode(ExportMode e) {
        getDataProvider();
        source.setExportMode(e);
        filter.setVisible(e == ExportMode.Filtered);
        clearCache();
    }

    void buttonAction() {
        if (button.isSelected()) {
            boolean success = startUp();
            if (!success) {
                button.setSelected(false);
            }
        } else {
            shutDown();
        }
    }
}