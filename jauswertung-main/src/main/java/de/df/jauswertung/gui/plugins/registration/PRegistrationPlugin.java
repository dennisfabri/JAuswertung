/*
 * Created on 07.04.2004
 */
package de.df.jauswertung.gui.plugins.registration;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LOAD_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_WK;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.PanelInfo;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri
 * @date 07.04.2004
 */
public class PRegistrationPlugin extends ANullPlugin {

    private CorePlugin                                   core  = null;

    private PRegistrationInternalEinzelPlugin            peep  = null;
    private PRegistrationInternalMannschaftStaffelPlugin pemsp = null;

    private JPanel                                       panel = null;

    public PRegistrationPlugin() {
        panel = new JPanel(new BorderLayout());
        panel.setName(I18n.get("Registration"));
    }

    @Override
    public void setController(IPluginManager controller, String uid) {
        super.setController(controller, uid);
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", uid);
        peep = (PRegistrationInternalEinzelPlugin) controller.getFeature("de.df.jauswertung.singleregistration", uid);
        pemsp = (PRegistrationInternalMannschaftStaffelPlugin) controller.getFeature("de.df.jauswertung.teamteamregistration", uid);
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return new PanelInfo[] { new PanelInfo(panel, IconManager.getBigIcon("eingabe"), 100) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if ((due.getChangeReason() & (REASON_NEW_WK | REASON_LOAD_WK)) > 0) {
            EDTUtils.executeOnEDT(new Runnable() {
                @Override
                public void run() {
                    updateDisplay();
                }
            });
        }
    }

    void updateDisplay() {
        if (((AWettkampf<?>) core.getWettkampf()) instanceof EinzelWettkampf) {
            panel.removeAll();
            panel.add(peep.getPanel(), BorderLayout.CENTER);
        } else {
            panel.removeAll();
            panel.add(pemsp.getPanel(), BorderLayout.CENTER);
        }
        panel.updateUI();
    }
}