package de.df.jauswertung.gui.util;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextPane;

import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.window.JAboutDialog;

public final class AboutDialogCreator {

    private AboutDialogCreator() {
        // Hide constructor
    }

    public static JAboutDialog create(JFrame parent) {
        JTextPane general = new JTextPane();
        general.setEditable(false);
        general.setContentType("text/plain");
        general.setText(I18n.get("AboutGeneral", I18n.getVersion()));
        general.setName(I18n.get("AboutGeneralTitle"));

        JComponent[] c = null;
        if (Utils.isInDevelopmentMode()) {
            c = new JComponent[] { general,
                    JAboutDialog.getSystemInformation(
                            new String[] { I18n.get("Systeminformation"), I18n.get("Property"), I18n.get("Value") }),
                    JAboutDialog.getUIInformation(
                            new String[] { I18n.get("UIInformation"), I18n.get("Property"), I18n.get("Value") }) };
        } else {
            c = new JComponent[] { general };
        }

        JAboutDialog about = new JAboutDialog(parent, I18n.get("About"), IconManager.getLogo(), c);
        UIStateUtils.uistatemanage(parent, about, "JAuswertungAbout");
        return about;
    }

}
