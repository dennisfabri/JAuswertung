package de.df.jauswertung.gui.plugins;

import java.util.EnumMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.BetaFeature;
import de.df.jauswertung.util.BetaFeature.Area;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.SimpleTableLayout;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

public class OBetaPlugin extends AFeature implements MOptionenPlugin.OptionsPlugin {

    private final EnumMap<BetaFeature, JCheckBox> checkBoxes = new EnumMap<>(BetaFeature.class);

    private JPanel optionsPanel;

    private MOptionenPlugin optionen;

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);

        optionen = (MOptionenPlugin) plugincontroller.getFeature("de.df.jauswertung.options", pluginuid);
        optionen.addOptionsPlugin(this);
    }

    @Override
    public JPanel[] getPanels() {
        if (optionsPanel == null) {
            initPanel();
        }
        return new JPanel[] { optionsPanel };
    }

    private void initPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu");
        optionsPanel = new JPanel(layout);
        optionsPanel.setName(I18n.get("Beta"));
        optionsPanel.setToolTipText(I18n.getToolTip("BetaOptions"));

        optionsPanel.add(createArea(Area.FEATURES, "BetaFeatures.Features"), CC.xy(2, 2));
        optionsPanel.add(createArea(Area.DIAGNOSTICS, "BetaFeatures.Diagnostics"), CC.xy(2, 4));
        optionsPanel.add(new JLabel(I18n.get("BetaFeatures.RestartRequired")), CC.xy(2, 6));
    }

    private JPanel createArea(Area area, String titleKey) {
        JPanel panel = new JPanel(new SimpleTableLayout(1, 5, 5));
        panel.setBorder(BorderUtils.createLabeledBorder(I18n.get(titleKey)));
        for (BetaFeature feature : BetaFeature.inArea(area)) {
            String key = "BetaFeature." + feature.getKey();
            JCheckBox checkBox = new JCheckBox(I18n.get(key));
            checkBox.setToolTipText(I18n.getToolTip(key));
            checkBox.setSelected(feature.isEnabled());
            checkBox.addActionListener(e -> optionen.notifyChange());
            checkBoxes.put(feature, checkBox);
            panel.add(checkBox);
        }
        return panel;
    }

    @Override
    public void apply() {
        checkBoxes.forEach((feature, checkBox) -> feature.setEnabled(checkBox.isSelected()));
    }

    @Override
    public void cancel() {
        checkBoxes.forEach((feature, checkBox) -> checkBox.setSelected(feature.isEnabled()));
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        // Nothing to do here
    }

    @Override
    public boolean isOk() {
        return true;
    }
}
