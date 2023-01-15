/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenuItem;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.MenuInfo;

public class MHelpPlugin extends ANullPlugin {

    private static final String QUESTION_STRING = I18n.get("?");
    private static final String INFO_STRING = I18n.get("Help");

    private static final String PATH_TO_DOC = "handbuch/index.html";

    JMenuItem info = new JMenuItem(INFO_STRING);

    public MHelpPlugin() {
        info.setEnabled(false);
        info.setToolTipText(I18n.getToolTip("Help"));
        try {
            info.addActionListener(e -> {
                File htmlFile = new File(PATH_TO_DOC);
                try {
                    Desktop.getDesktop().browse(htmlFile.toURI());
                } catch (IOException ex) {
                    DialogUtils.showException(getController().getWindow(), "Hilfe nicht verfügbar",
                            "Beim Anzeigen der Hilfe ist ein Fehler aufgetreten.",
                            "Sie können die Hilfe auch über das Startmenü aufrufen.", ex);
                    ex.printStackTrace();
                }
            });
            info.setEnabled(true);
        } catch (RuntimeException re) {
            re.printStackTrace();

            // Hide MenuItem
            info.setVisible(false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.beta.plugin.AuswertungPlugIn#getSupportedMenues()
     */
    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(QUESTION_STRING, 1000, info, 900) };
    }
}