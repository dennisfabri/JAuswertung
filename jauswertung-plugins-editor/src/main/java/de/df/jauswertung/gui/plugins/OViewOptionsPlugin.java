/*
 * Created on 21.01.2005
 */
package de.df.jauswertung.gui.plugins;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.ListLayout;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.JPanelContainer;

/**
 * @author Dennis Fabri
 * @date 21.01.2005
 */
public class OViewOptionsPlugin extends ANullPlugin implements MOptionenPlugin.OptionsPlugin {

    private static final String COMPRESS_LISTS = "CompressLists";

    MOptionenPlugin optionen;

    private JPanel panel;
    private JCheckBox hlwleftalignCheckbox;
    private JCheckBox doubleMode;
    private JCheckBox compress = null;

    private boolean doublewindowmode = false;
    private boolean hlwleftalign = false;

    public OViewOptionsPlugin() {
        super();
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        CorePlugin core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        if (core == null) {
            throw new IllegalStateException();
        }
        optionen = (MOptionenPlugin) plugincontroller.getPlugin("de.df.jauswertung.options", pluginuid);
        if (optionen == null) {
            throw new IllegalStateException();
        }

        Preferences preferences = Utils.getPreferences();
        doublewindowmode = !preferences.getBoolean("WindowMode", JPanelContainer.DOUBLE);
        hlwleftalign = preferences.getBoolean("HHListLeftAlign", false);
        PrintUtils.setCompressLists(preferences.getBoolean(COMPRESS_LISTS, true));

        optionen.addOptionsPlugin(this);
    }

    @Override
    public JPanel[] getPanels() {
        if (panel == null) {
            panel = new JPanel(new ListLayout(5));
            panel.setBorder(BorderUtils.createSpaceBorder());
            panel.setName(I18n.get("View"));
            panel.setToolTipText(I18n.getToolTip("ViewOptions"));
            hlwleftalignCheckbox = new JCheckBox(I18n.get("HHListLeftAlign"));
            hlwleftalignCheckbox.setToolTipText(I18n.getToolTip("HHListLeftAlign"));
            hlwleftalignCheckbox.addActionListener(e -> {
                optionen.notifyChange();
            });
            doubleMode = new JCheckBox(I18n.get("DividedGUI"));
            doubleMode.setToolTipText(I18n.getToolTip("DividedGUI"));
            doubleMode.addActionListener(e -> {
                optionen.notifyChange();
            });

            compress = new JCheckBox(I18n.get("CompressLists"));
            compress.setSelected(PrintUtils.getCompressLists());
            compress.addActionListener(arg0 -> {
                optionen.notifyChange();
            });

            panel.add(doubleMode);
            panel.add(hlwleftalignCheckbox);
            panel.add(compress);

            cancel();
        }
        return new JPanel[] { panel };
    }

    @Override
    public void apply() {
        doublewindowmode = doubleMode.isSelected();
        hlwleftalign = hlwleftalignCheckbox.isSelected();
        PrintUtils.setCompressLists(compress.isSelected());

        Utils.getPreferences().putBoolean("WindowMode", !doublewindowmode);
        Utils.getPreferences().putBoolean("HHListLeftAlign", hlwleftalign);
        Utils.getPreferences().putBoolean(COMPRESS_LISTS, PrintUtils.getCompressLists());
    }

    @Override
    public void cancel() {
        if (doublewindowmode != doubleMode.isSelected()) {
            doubleMode.setSelected(doublewindowmode);
        }
        if (hlwleftalign != hlwleftalignCheckbox.isSelected()) {
            hlwleftalignCheckbox.setSelected(hlwleftalign);
        }
        if (PrintUtils.getCompressLists() != compress.isSelected()) {
            compress.setSelected(PrintUtils.getCompressLists());
        }
    }

    @Override
    public boolean isOk() {
        return true;
    }
}