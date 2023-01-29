/*
 * Created on 21.01.2005
 */
package de.df.jauswertung.gui.plugins;

import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;

/**
 * @author Dennis Fabri
 * @date 21.01.2005
 */
public class OGeneralOptionsPlugin extends ANullPlugin implements MOptionenPlugin.OptionsPlugin {

    MOptionenPlugin optionen;
    private JPanel panel;
    private JCheckBox penaltyCheckbox;
    private JCheckBox singleCheckbox;
    private JCheckBox hightimesCheckbox;
    private JCheckBox lowtimesCheckbox;

    private boolean state = false;
    private boolean singlewk = false;
    private boolean hightimes = false;

    public OGeneralOptionsPlugin() {
        super();
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        CorePlugin core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        if (core == null) {
            throw new NullPointerException();
        }
        optionen = (MOptionenPlugin) plugincontroller.getPlugin("de.df.jauswertung.options", pluginuid);
        if (optionen == null) {
            throw new NullPointerException();
        }

        Preferences preferences = Utils.getPreferences();
        state = preferences.getBoolean("PrintPenalties", false);
        singlewk = preferences.getBoolean("SingleCompetition", false);
        hightimes = preferences.getBoolean("NotifyOnHighTimes", true);

        SchwimmerUtils.EnableHighPointWarning = preferences.getBoolean("NotifyOnLowTimes", true);
        SchwimmerUtils.EnableHighTimesWarning = hightimes;

        optionen.addOptionsPlugin(this);
    }

    @Override
    public JPanel[] getPanels() {
        if (panel == null) {
            panel = new JPanel(new FormLayout("4dlu,fill:default:grow:4dlu", FormLayoutUtils.createLayoutString(5)));
            // panel.setBorder(BorderUtils.createSpaceBorder());
            panel.setName(I18n.get("General"));
            panel.setToolTipText(I18n.getToolTip("GeneralOptions"));
            singleCheckbox = new JCheckBox(I18n.get("SingleCompetitionAtStartup"));
            singleCheckbox.setToolTipText(I18n.getToolTip("SingleCompetitionAtStartup"));
            singleCheckbox.addActionListener(e -> {
                optionen.notifyChange();
            });
            penaltyCheckbox = new JCheckBox(I18n.get("PrintPenalties"));
            penaltyCheckbox.setToolTipText(I18n.getToolTip("SetPrintPenalties"));
            penaltyCheckbox.addActionListener(e -> {
                optionen.notifyChange();
            });

            hightimesCheckbox = new JCheckBox(I18n.get("DisplayHighTimesWarning"));
            hightimesCheckbox.setToolTipText(I18n.getToolTip("DisplayHighTimesWarning"));
            hightimesCheckbox.addActionListener(e -> {
                optionen.notifyChange();
            });

            lowtimesCheckbox = new JCheckBox(I18n.get("DisplayLowTimesWarning"));
            lowtimesCheckbox.setToolTipText(I18n.getToolTip("DisplayLowTimesWarning"));
            lowtimesCheckbox.addActionListener(e -> {
                optionen.notifyChange();
            });

            panel.add(singleCheckbox, CC.xy(2, 2));
            panel.add(new JSeparator(), CC.xy(2, 4));
            panel.add(penaltyCheckbox, CC.xy(2, 6));
            panel.add(hightimesCheckbox, CC.xy(2, 8));
            panel.add(lowtimesCheckbox, CC.xy(2, 10));

            cancel();
        }
        return new JPanel[] { panel };
    }

    public boolean getPrintEnabled() {
        return state;
    }

    @Override
    public void apply() {
        state = penaltyCheckbox.isSelected();
        singlewk = singleCheckbox.isSelected();
        hightimes = hightimesCheckbox.isSelected();
        SchwimmerUtils.EnableHighPointWarning = lowtimesCheckbox.isSelected();

        Utils.getPreferences().putBoolean("PrintPenalties", state);
        Utils.getPreferences().putBoolean("SingleCompetition", singlewk);
        Utils.getPreferences().putBoolean("NotifyOnHighTimes", hightimes);
        Utils.getPreferences().putBoolean("NotifyOnLowTimes", SchwimmerUtils.EnableHighPointWarning);
    }

    @Override
    public void cancel() {
        if (state != penaltyCheckbox.isSelected()) {
            penaltyCheckbox.setSelected(state);
        }
        if (singlewk != singleCheckbox.isSelected()) {
            singleCheckbox.setSelected(singlewk);
        }
        if (hightimes != hightimesCheckbox.isSelected()) {
            hightimesCheckbox.setSelected(hightimes);
        }
        if (SchwimmerUtils.EnableHighPointWarning != lowtimesCheckbox.isSelected()) {
            lowtimesCheckbox.setSelected(SchwimmerUtils.EnableHighPointWarning);
        }
    }

    @Override
    public boolean isOk() {
        return true;
    }
}