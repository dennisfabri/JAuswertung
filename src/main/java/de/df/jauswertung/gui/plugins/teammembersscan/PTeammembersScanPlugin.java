/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.teammembersscan;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.http.HttpServerPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.PanelInfo;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Mueller
 * @date 08.06.2005
 */
public class PTeammembersScanPlugin extends ANullPlugin {

    private static final String  INPUT           = I18n.get("Members");

    IPluginManager               controller      = null;
    private CorePlugin           core            = null;
    private HttpServerPlugin     http            = null;

    JGlassPanel<JPanel>          input           = null;
    JTeammembersBarcodeScanPanel barcode         = null;

    TeammembersDataProvider      barcodeprovider = null;

    public PTeammembersScanPlugin() {
        super();
    }

    void initPanel() {
        barcode = new JTeammembersBarcodeScanPanel(this, controller, core, http);

        barcodeprovider = new TeammembersDataProvider(barcode, http);

        http.registerDataProvider(barcodeprovider);

        input = new JGlassPanel<JPanel>(barcode);
        input.setEnabled(false);

        addInfoToGlass(input);
    }

    private static void addInfoToGlass(JGlassPanel<JPanel> input1) {
        JPanel infopanel = new JPanel();
        infopanel.setName("Info");
        infopanel.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information")));
        infopanel.add(new JLabel(I18n.get("TeammembersInputInfo")));

        JPanel glass = input1.getGlassPanel();
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu:grow", "4dlu:grow,fill:default,4dlu:grow");
        layout.setRowGroups(new int[][] { { 1, 3 } });
        layout.setColumnGroups(new int[][] { { 1, 3 } });
        glass.setLayout(layout);
        glass.add(infopanel, CC.xy(2, 2));
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return new PanelInfo[] { new PanelInfo(INPUT, IconManager.getBigIcon("team"), true, false, 500) {
            @Override
            public JPanel getPanelI() {
                if (input == null) {
                    initPanel();
                    dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED);
                }
                return input;
            }
        } };
    }

    @Override
    public void setController(IPluginManager controller, String uid) {
        super.setController(controller, uid);
        this.controller = controller;
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", uid);
        http = (HttpServerPlugin) controller.getFeature("de.df.jauswertung.http", uid);
    }

    @Override
    public synchronized void dataUpdated(UpdateEvent due) {
        if (barcode == null) {
            return;
        }
        barcode.dataUpdated(due);
        updateGUI();
        if (controller != null) {
            @SuppressWarnings("rawtypes")
            AWettkampf wk = core.getWettkampf();
            boolean enabled = wk instanceof MannschaftWettkampf;
            controller.setPanelEnabled(INPUT, enabled);
        }
    }

    void updateGUI() {
        MannschaftWettkampf mwk = core.getMannschaftWettkampf();
        boolean enabled = mwk != null && mwk.hasSchwimmer();
        input.setEnabled(enabled);
    }
}