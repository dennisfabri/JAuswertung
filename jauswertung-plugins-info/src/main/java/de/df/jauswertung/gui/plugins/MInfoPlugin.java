/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import de.df.jauswertung.gui.util.AboutDialogCreator;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.window.JAboutDialog;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.MenuInfo;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public class MInfoPlugin extends ANullPlugin {

    private static final String QUESTION_STRING = I18n.get("?");
    private static final String INFO_STRING = I18n.get("Info");

    private JMenuItem info;
    private JAboutDialog about;

    public MInfoPlugin() {
        info = new JMenuItem(INFO_STRING, IconManager.getSmallIcon("info"));
        info.setToolTipText(I18n.getToolTip("About"));
        info.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                displayInfo();
            }
        });
    }

    void displayInfo() {
        synchronized (this) {
            if (about == null) {
                about = AboutDialogCreator.create(getController().getWindow());
            }
        }
        about.setVisible(true);
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(QUESTION_STRING, 1000, info, 1000) };
    }
}