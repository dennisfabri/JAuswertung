package de.df.jauswertung.gui.veranstaltung;

import javax.swing.JPanel;

import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.PanelInfo;
import de.df.jutils.plugin.UpdateEvent;

public class PEinstellungenPlugin extends ANullPlugin {

    private VeranstaltungCorePlugin core;
    private JVeranstaltungseinstellungen panel;

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);
        core = (VeranstaltungCorePlugin) c.getFeature(VeranstaltungswertungLauncher.CORE, uid);
        panel = new JVeranstaltungseinstellungen(this::changed);
    }

    private void changed() {
        panel.getProperties(core.getVeranstaltung());
        core.changed("Properties", UpdateEventConstants.REASON_PROPERTIES_CHANGED, this);
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return new PanelInfo[] {
                new PanelInfo(I18n.get("Properties"), IconManager.getBigIcon("eigenschaften"), true, true, 200) {
                    @Override
                    protected JPanel getPanelI() {
                        return panel;
                    }
                } };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (due.isReason(UpdateEventConstants.REASON_NEW_LOAD_WK)) {
            panel.setProperties(core.getVeranstaltung());
        }
    }
}
