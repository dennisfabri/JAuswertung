package de.df.jauswertung.gui.veranstaltung;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.Utils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.PanelInfo;

public class PAusgabePlugin extends ANullPlugin implements Ausgabekontext {

    private VeranstaltungCorePlugin core;
    private PSchriftartPlugin schriftart;
    private JVeranstaltungsausgabe panel;

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);
        core = (VeranstaltungCorePlugin) c.getFeature(VeranstaltungswertungLauncher.CORE, uid);
        schriftart = (PSchriftartPlugin) c.getFeature(VeranstaltungswertungLauncher.SCHRIFTART, uid);
        panel = new JVeranstaltungsausgabe(this);
    }

    @Override
    public Veranstaltung getVeranstaltung() {
        return Utils.copy(core.getVeranstaltung());
    }

    @Override
    public Font getSelectedFont() {
        return schriftart.getSelectedFont();
    }

    @Override
    public JFrame getWindow() {
        return getController().getWindow();
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return new PanelInfo[] {
                new PanelInfo(I18n.get("Output"), IconManager.getBigIcon("resulttable"), true, true, 400) {
                    @Override
                    protected JPanel getPanelI() {
                        return panel;
                    }
                } };
    }
}
