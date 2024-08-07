/*
 * Created on 21.01.2005
 */
package de.df.jauswertung.gui.plugins.http;

import java.awt.event.ItemEvent;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
import de.df.jauswertung.io.util.HttpUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Fabri
 * @date 21.01.2005
 */
public class HttpServerPlugin extends ANullPlugin {

    private static final Logger log = LoggerFactory.getLogger(HttpServerPlugin.class);

    private CorePlugin core;
    MOptionenPlugin optionen;

    private ButtonInfo[] quicks;
    private JToggleButton button;
    private JResultsSelectionButton filter;
    private HttpOptionsPlugin httpOptions;

    private int port = 80;

    private HttpServer httpServer;

    private DataProvider source;
    boolean state = false;

    public HttpServerPlugin() {
        super();

        port = Utils.getPreferences().getInt("HttpServerPort", 80);

        button = new JToggleButton(IconManager.getSmallIcon("webserver"));
        button.setToolTipText(I18n.get("StartStopHttpServer"));
        button.addActionListener(e -> buttonAction());

        filter = new JResultsSelectionButton(new IWettkampfProvider() {
            @Override
            public <T extends ASchwimmer> AWettkampf<T> getWettkampf() {
                return core.getWettkampf();
            }
        });
        filter.addItemListener(arg0 -> {
            if (arg0.getStateChange() == ItemEvent.DESELECTED) {
                source.setSelection(filter.getSelection(core.getWettkampf()));
            }
        });
        filter.setEnabled(false);

        quicks = new ButtonInfo[2];
        quicks[0] = new ButtonInfo(button, 1000);
        quicks[1] = new ButtonInfo(filter, 1001);
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
    }

    private DataProvider getDataProvider() {
        if (source == null) {
            source = new DataProvider(core);
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

            DPRequestHandler requestHandler = new DPRequestHandler(getDataProvider());

            final SocketConfig socketConfig = SocketConfig.custom()
                    .setSoTimeout(15, TimeUnit.SECONDS)
                    .setTcpNoDelay(true)
                    .build();

            ServerBootstrap httpServerBootstrap = ServerBootstrap.bootstrap()
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
                    .register("*", requestHandler)
                    .setCanonicalHostName(InetAddress.getLocalHost().getHostName());

            for (String ip : listInterfaces()) {
                httpServerBootstrap.registerVirtual(ip, "*", requestHandler);
            }

            httpServer = httpServerBootstrap.create();

            httpServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (httpServer != null) {
                        httpServer.close(CloseMode.GRACEFUL);
                    }
                }
            });

            httpServer.start();

            httpOptions.setEnabled(false);
            filter.setEnabled(source.getExportMode() == ExportMode.Filtered);
            return true;
        } catch (Exception e) {
            log.warn("Could not start HTTP-Server", e);
            DialogUtils.wichtigeMeldung(null, I18n.get("HttpServerNotStartet"));
            shutDown();
            httpServer = null;
            state = false;
            httpOptions.setEnabled(true);
            filter.setEnabled(false);
            return false;
        }
    }

    private static List<String> listInterfaces() throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        ArrayList<NetworkInterface> list = Collections.list(nets);
        return list.stream().map(HttpServerPlugin::displayInterfaceInformation)
                .flatMap(i -> i.stream())
                .distinct().toList();
    }

    private static List<String> displayInterfaceInformation(NetworkInterface netint) {
        List<String> addresses = new ArrayList<>();
        if (netint.getDisplayName().contains("Hyper-V")) {
            return addresses;
        }
        if (netint.getDisplayName().contains("Microsoft Wi-Fi Direct Virtual Adapter")) {
            return addresses;
        }
        if (netint.getDisplayName().contains("Loopback")) {
            return addresses;
        }
        if (netint.getDisplayName().contains("WAN Miniport")) {
            return addresses;
        }
        log.info(netint.getDisplayName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            addresses.add(inetAddress.getCanonicalHostName());
            addresses.add(inetAddress.getHostAddress());
            addresses.add(inetAddress.getHostName());
        }
        return addresses;
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