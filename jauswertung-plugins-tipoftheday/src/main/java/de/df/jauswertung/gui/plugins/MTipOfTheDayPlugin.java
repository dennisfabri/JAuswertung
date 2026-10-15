/*
 * Created on 18.04.2005
 */
package de.df.jauswertung.gui.plugins;

import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.TipOfTheDay;
import de.df.jutils.gui.TipOfTheDayDialog;
import de.df.jutils.i18n.I18nUtils;
import de.df.jutils.io.FileUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MTipOfTheDayPlugin extends ANullPlugin {

    private static final String PREFERENCE_NODE = "l2fprod";
    private static final String PREFERENCE_KEY = "ShowTipOnStartup";

    private TipOfTheDay tips = null;
    private final JMenuItem item = new JMenuItem(I18n.get("Tips"));

    public MTipOfTheDayPlugin() {
        item.setToolTipText(I18n.getToolTip("TipOfTheDay"));
        item.addActionListener(arg0 -> showTips());
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(I18n.get("?"), 1000, item, 910) };
    }

    private synchronized TipOfTheDay getTips() {
        if (tips == null) {
            String[] text = FileUtils.readTextFile(
                    I18nUtils.getLocalizedFile(Utils.getUserDir() + "tips" + File.separator + "tips.txt"), "utf-8");
            tips = new TipOfTheDay(List.of(text));
            tips.setCurrentIndex((int) (Math.random() * text.length));
        }
        return tips;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (due.isReason(UpdateEventConstants.REASON_STARTUP)) {
            SwingUtilities.invokeLater(this::showTipAtStartup);
        }
    }

    private void showTips() {
        SwingUtilities.invokeLater(() -> createDialog().setVisible(true));
    }

    private void showTipAtStartup() {
        if (isShowingOnStartup()) {
            createDialog().setVisible(true);
        }
    }

    private JDialog createDialog() {
        TipOfTheDay.Texts texts = new TipOfTheDay.Texts(I18n.get("TipOfTheDay.dialogTitle"),
                I18n.get("TipOfTheDay.didYouKnowText"), I18n.get("TipOfTheDay.showOnStartupText"),
                I18n.get("TipOfTheDay.previousTipText"), I18n.get("TipOfTheDay.nextTipText"),
                I18n.get("TipOfTheDay.closeText"));
        return TipOfTheDayDialog.create(getController().getWindow(), getTips(), texts, IconManager.getBigIcon("tip"),
                new ShowOnStartupChoice());
    }

    private static boolean isShowingOnStartup() {
        return preferences().getBoolean(PREFERENCE_KEY, true);
    }

    private static Preferences preferences() {
        return Utils.getPreferences().node(PREFERENCE_NODE);
    }

    private static final class ShowOnStartupChoice implements TipOfTheDay.ShowOnStartupChoice {

        @Override
        public boolean isShowingOnStartup() {
            return MTipOfTheDayPlugin.isShowingOnStartup();
        }

        @Override
        public void setShowingOnStartup(boolean showOnStartup) {
            preferences().putBoolean(PREFERENCE_KEY, showOnStartup);
        }
    }
}
