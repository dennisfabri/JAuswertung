/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.teammembersinput;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.MannschaftWettkampf;
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
 * @date 08.06.2005
 */
public class PTeammembersInputPlugin extends ANullPlugin {

    private static final String INPUT = I18n.get("Input");

    IPluginManager controller = null;
    private CorePlugin core = null;
    private SelectOrganisationPlugin organisation = null;

    // JGlassPanel<JPanel> inputbarcode = null;
    JGlassPanel<JPanel> inputmanual = null;
    // JTeammembersBarcodeScanPanel barcode = null;
    JTeammembersInputPanel manual = null;

    public PTeammembersInputPlugin() {
        super();
    }

    void initPanel() {
        // barcode = new JTeammembersBarcodeScanPanel(this, controller, core);
        manual = new JTeammembersInputPanel(this, controller, core);

        // inputbarcode = new JGlassPanel<JPanel>(barcode);
        // inputbarcode.setEnabled(false);
        // addInfoToGlass(inputbarcode);

        inputmanual = new JGlassPanel<JPanel>(manual);
        inputmanual.setEnabled(false);

        addInfoToGlass(inputmanual);
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
        return new PanelInfo[] { new PanelInfo(INPUT, IconManager.getBigIcon("team"), true, false, 400) {
            @Override
            public JPanel getPanelI() {
                if (inputmanual == null) {
                    initPanel();
                    dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED);
                }
                return inputmanual;
            }
        } };
    }

    @Override
    public void setController(IPluginManager controller, String uid) {
        super.setController(controller, uid);
        this.controller = controller;
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", uid);
        organisation = (SelectOrganisationPlugin) controller.getFeature("de.df.jauswertung.selectorganisation", uid);

        organisation.addSelectionListener(new ISelectionListener() {
            @Override
            public void selected(String selection) {
                // barcode.setSelectedOrganisation(selection);
                manual.setSelectedOrganisation(selection);
            }
        });
    }

    @Override
    public synchronized void dataUpdated(UpdateEvent due) {
        // if (barcode != null) {
        // barcode.dataUpdated(due);
        // }
        if (manual != null) {
            manual.dataUpdated(due);
        }
        updateGUI();
    }

    void updateGUI() {
        MannschaftWettkampf mwk = core.getMannschaftWettkampf();
        boolean enabled = mwk != null && mwk.hasSchwimmer();
        // inputbarcode.setEnabled(enabled);
        inputmanual.setEnabled(enabled);
    }
}