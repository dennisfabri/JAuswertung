package de.df.jauswertung.gui.plugins.elektronischezeit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.exception.NotEnabledException;
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.AresfileTimesSource;
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.HttpTimesSource;
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.ITimesSource;
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.SourcesConfig;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.dm.ares.data.Heat;

class JElektronischeZeit<T extends ASchwimmer> extends JFrame {

    private static final long              serialVersionUID = -2312160133856798878L;

    private boolean                        changed          = false;
    private Heat[]                         heats            = null;

    private final MElektronischeZeitPlugin electric;
    private ITimesSource                   source           = null;

    private JButton                        close;
    private JButton                        update;

    private JHeatPanel<T>                  heat             = null;
    private JTimePanel<T>                  time             = null;

    private final JFrame                   parent;

    private IETStrategy                    strategy         = null;

    public IETStrategy getStrategy() {
        return strategy;
    }

    public IPluginManager getController() {
        return electric.getController();
    }

    public IPlugin getPlugin() {
        return electric;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public JElektronischeZeit(JFrame p, AWettkampf<T> w, MElektronischeZeitPlugin electric) {
        super(I18n.get("ElektronischeZeitnahme"));
        setIconImages(IconManager.getTitleImages());

        parent = p;
        this.electric = electric;

        if (w.isHeatBased()) {
            strategy = new ETHeatStrategy(getController(), getPlugin(), this, w);
        } else if (w.isDLRGBased()) {
            strategy = new ETDLRGStrategy(getController(), getPlugin(), this, w);
        } else {
            throw new NotEnabledException("This option is not implemented.");
        }

        switch (SourcesConfig.getSource()) {
        case http:
            source = new HttpTimesSource();
            break;
        case aresfile:
            source = new AresfileTimesSource();
            break;
        default:
            throw new IllegalStateException("Unkown datasource");
        }

        createPanel();

        pack();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        WindowUtils.center(this, parent);
        // setExtendedState(Frame.MAXIMIZED_BOTH);
        WindowUtils.addEscapeAction(this, new Runnable() {
            @Override
            public void run() {
                close();
            }
        });
        WindowUtils.addEnterAction(this, new Runnable() {
            @Override
            public void run() {
                enterTimes();
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        UIStateUtils.uistatemanage(this, "JElektronischeZeit");
        update();
    }

    private void createPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu,150dlu,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        setLayout(layout);

        heat = new JHeatPanel<T>(this);
        time = new JTimePanel<T>(this, heat);
        heat.setTimePanel(time);
        time.setDirectMatching(strategy.isDirectMatching());

        add(heat, CC.xy(2, 2));
        add(time, CC.xy(4, 2));
        add(createButtons(), CC.xyw(2, 4, 3, "right,fill"));
    }

    private JPanel createButtons() {
        FormLayout layout = new FormLayout("0dlu,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
        layout.setColumnGroups(new int[][] { { 2, 4 } });
        JPanel p = new JPanel(layout);
        update = new JButton(I18n.get("Update"), IconManager.getSmallIcon("update"));
        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        p.add(update, CC.xy(2, 2));
        p.add(close, CC.xy(4, 2));

        return p;
    }

    void close() {
        setVisible(false);
        dispose();
    }

    @Override
    public void setVisible(boolean visible) {
        if (parent != null) {
            parent.setEnabled(!visible);
        }
        super.setVisible(visible);
    }
    
    private Heat[] loadHeats() {
        String address = SourcesConfig.getAddress();
        if (address.equalsIgnoreCase("dummy") && Utils.isInDevelopmentMode()) {
            return strategy.generateHeats();
        }
        return source.getHeats();
    }

    void update() {
        boolean failed = true;
        Heat[] heatsnew = loadHeats();
        if (heatsnew != null) {
            for (Heat h : heatsnew) {
                h.updateTimes(10);
            }
            failed = false;
        } else {
            heatsnew = new Heat[0];
        }
        heats = heatsnew;
        if (failed) {
            heats = new Heat[0];
            DialogUtils.wichtigeMeldung(this, I18n.get("Error.DownloadOfTimesFailed"));
        }
        time.updateTimes();
    }

    Heat[] getHeats() {
        return heats;
    }

    public void setChanging() {
        changed = true;
    }

    public void sendDataUpdate() {
        electric.notifyChange(this);
    }

    public boolean hasChanged() {
        return changed;
    }

    void enterTimes() {
        time.enterTimes();
    }
}