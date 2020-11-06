/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins;

import java.awt.Window;
import java.net.URL;

import javax.help.CSH;
import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.WindowPresentation;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.MenuInfo;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public class MHelpPlugin extends ANullPlugin {

    private static final String QUESTION_STRING = I18n.get("?");
    private static final String INFO_STRING     = I18n.get("Help");

    JMenuItem                   info            = new JMenuItem(INFO_STRING);

    private static HelpBroker loadHelp() {
        HelpSet hs = null;
        String helpHS = "help.hs";
        ClassLoader cl = MHelpPlugin.class.getClassLoader();
        try {
            URL hsURL = HelpSet.findHelpSet(cl, helpHS);
            hs = new HelpSet(null, hsURL);
            // Create a HelpBroker object:
            DefaultHelpBroker hb = new DefaultHelpBroker();
            hb.setHelpSet(hs);
            hb.initPresentation();

            WindowPresentation pres = hb.getWindowPresentation();
            Window win = pres.getHelpWindow();

            if (win instanceof JFrame) {
                ((JFrame) win).setIconImages(IconManager.getTitleImages());
            }

            WindowUtils.center(win);
            UIStateUtils.uistatemanage(win, "HelpSet");

            return hb;
        } catch (Exception ee) {
            ee.printStackTrace();
            // HelpSet could not be loaded
            return null;
        }
    }

    public MHelpPlugin() {
        info.setEnabled(false);
        info.setToolTipText(I18n.getToolTip("Help"));
        try {
            info.addActionListener(new CSH.DisplayHelpFromSource(loadHelp()));
            info.setEnabled(true);
        } catch (RuntimeException re) {
            // Hide Menuitem
            re.printStackTrace();
            info.setVisible(false);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * de.df.jauswertung.gui.beta.plugin.AuswertungPlugIn#getSupportedMenues()
     */
    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(QUESTION_STRING, 1000, info, 900) };
    }
}