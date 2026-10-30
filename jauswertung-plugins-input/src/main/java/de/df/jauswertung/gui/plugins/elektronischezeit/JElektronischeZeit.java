package de.df.jauswertung.gui.plugins.elektronischezeit;

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
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.AresfileTimesSource;
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.HttpTimesSource;
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.ITimesSource;
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.SourcesConfig;
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.TimesSourceException;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.exception.NotEnabledException;
import de.df.jauswertung.util.BetaFeature;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPlugin;
import de.df.jutils.plugin.IPluginManager;
import org.lisasp.alphatiming.legacy.api.Heat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class JElektronischeZeit<T extends ASchwimmer> extends JFrame {

    private boolean changed = false;
    private Heat[] heats = new Heat[0];

    private final MElektronischeZeitPlugin electric;
    private ITimesSource source = null;

    private JButton close;
    private JButton update;

    private JHeatPanel<T> heat = null;
    private JTimePanel<T> time = null;

    private final JFrame parent;

    private IETStrategy strategy = null;

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
        } else {
            strategy = new ETDLRGStrategy(getController(), getPlugin(), this, w);
        }

        switch (SourcesConfig.getSource()) {
        case http:
            source = new HttpTimesSource();
            break;
        case aresfile:
            source = new AresfileTimesSource();
            break;
        default:
            throw new IllegalStateException("Unknown datasource");
        }

        createPanel();

        pack();
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // setExtendedState(Frame.MAXIMIZED_BOTH);
        WindowUtils.addEscapeAction(this, this::close);
        WindowUtils.addEnterAction(this, this::enterTimes);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        UIStateUtils.uistatemanage(parent, this, "JElektronischeZeit");
        update();
    }

    private void createPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu,150dlu,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        setLayout(layout);

        heat = new JHeatPanel<>(this);
        time = new JTimePanel<>(this, heat);
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
        update.addActionListener(e -> {
            update();
        });
        close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(e -> {
            close();
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

    private Heat[] loadHeats() throws TimesSourceException {
        String address = SourcesConfig.getAddress();
        if (address.equalsIgnoreCase("dummy") && BetaFeature.DUMMY_TIMES_SOURCE.isEnabled()) {
            return strategy.generateHeats();
        }
        return source.getHeats();
    }

    /** On failure the times shown so far are kept, so a failed update does not empty the window. */
    void update() {
        try {
            Heat[] heatsnew = loadHeats();
            for (int x = 0; x < heatsnew.length; x++) {
                heatsnew[x] = ElectronicHeats.inHundredths(heatsnew[x]);
            }
            heats = heatsnew;
        } catch (TimesSourceException e) {
            log.warn("Could not load the times", e);
            DialogUtils.wichtigeMeldung(this, describe(e));
        }
        time.updateTimes();
    }

    static String describe(TimesSourceException e) {
        String source = e.getSource();
        String port = String.valueOf(e.getPort());
        String text = switch (e.getReason()) {
            case INVALID_ADDRESS -> I18n.get("Error.Times.InvalidAddress", source);
            case UNKNOWN_HOST -> I18n.get("Error.Times.UnknownHost", source);
            case NOT_RUNNING -> I18n.get("Error.Times.NotRunning", source, port);
            case NO_ANSWER -> I18n.get("Error.Times.NoAnswer", source, port);
            case CONNECTION_FAILED -> I18n.get("Error.Times.ConnectionFailed", source, port);
            case WRONG_SERVER -> I18n.get("Error.Times.WrongServer", source, port, String.valueOf(e.getHttpStatus()));
            case UNREADABLE_DATA -> I18n.get("Error.Times.UnreadableData", source, port);
            case FILE_NOT_SET -> I18n.get("Error.Times.FileNotSet");
            case FILE_NOT_FOUND -> I18n.get("Error.Times.FileNotFound", source);
            case FILE_UNREADABLE -> I18n.get("Error.Times.FileUnreadable", source);
        };
        return text + "\n\n" + I18n.get("Error.Times.SeeLog");
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