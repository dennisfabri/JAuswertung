package de.df.jauswertung.gui.veranstaltung;

import java.awt.Font;

import javax.swing.JPanel;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.PanelInfo;

public class PSchriftartPlugin extends ANullPlugin {

    private final JVeranstaltungsschriftart panel = new JVeranstaltungsschriftart();

    public Font getSelectedFont() {
        return panel.getSelectedFont();
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return new PanelInfo[] { new PanelInfo(I18n.get("Font"), IconManager.getBigIcon("font"), true, true, 300) {
            @Override
            protected JPanel getPanelI() {
                return panel;
            }
        } };
    }
}
