package de.df.jauswertung.gui.plugins.zwinput;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.PanelInfo;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri
 * @since 05.04.2004
 */
public class PZWInputPlugin extends ANullPlugin {

    private static final String INPUT1 = I18n.get("ZWInput");

    private IPluginManager controller = null;
    private CorePlugin core = null;
    private JGlassPanel<JPanel> input1 = null;
    private JSimpleInputPanel simple = null;

    public PZWInputPlugin() {
        super();
    }

    void initPanel() {
        simple = new JSimpleInputPanel(this, controller, core);

        input1 = new JGlassPanel<>(simple);
        input1.setName(INPUT1);
        input1.setEnabled(false);


        addInfoToGlass(input1);
    }

    private static void addInfoToGlass(JGlassPanel<JPanel> input1) {
        JPanel infoPanel = new JPanel();
        infoPanel.setName("Info");
        infoPanel.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information")));
        infoPanel.add(new JLabel(I18n.get("ZWInputInfo")));

        JPanel glass = input1.getGlassPanel();
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu:grow", "4dlu:grow,fill:default,4dlu:grow");
        layout.setRowGroups(new int[][] { { 1, 3 } });
        layout.setColumnGroups(new int[][] { { 1, 3 } });
        glass.setLayout(layout);
        glass.add(infoPanel, CC.xy(2, 2));
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return new PanelInfo[] { new PanelInfo(INPUT1, IconManager.getBigIcon("zwinput"), true, false, 400) {

            @Override
            public JPanel getPanelI() {
                if (input1 == null) {
                    initPanel();
                    dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED);
                }
                return input1;
            }
        }};
    }

    @Override
    public void setController(IPluginManager controller, String uid) {
        super.setController(controller, uid);
        this.controller = controller;
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", uid);
    }

    @Override
    public synchronized void dataUpdated(UpdateEvent due) {
        if (input1 == null) {
            return;
        }
        simple.dataUpdated(due);
        updateGUI();

        IPluginManager c = getController();

        if (c != null) {
            c.setPanelEnabled(INPUT1, core.getWettkampf().hasHLW());
        }
    }

    void updateGUI() {
        boolean hasHLW = core.getWettkampf().hasHLW();
        input1.setEnabled(hasHLW);
    }
}