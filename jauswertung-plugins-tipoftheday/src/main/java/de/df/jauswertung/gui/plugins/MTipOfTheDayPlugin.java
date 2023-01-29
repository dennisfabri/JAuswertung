/*
 * Created on 18.04.2005
 */
package de.df.jauswertung.gui.plugins;

import java.io.File;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.l2fprod.common.totd.JTipOfTheDay;
import com.l2fprod.common.totd.JTipOfTheDay.ShowOnStartupChoice;
import com.l2fprod.common.totd.tips.DefaultTip;
import com.l2fprod.common.totd.tips.DefaultTipModel;

import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.Utils;
import de.df.jutils.i18n.I18nUtils;
import de.df.jutils.io.FileUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MTipOfTheDayPlugin extends ANullPlugin {

    private JTipOfTheDay tips = null;
    JDialog dialog;
    private JMenuItem item = new JMenuItem(I18n.get("Tips"));

    public MTipOfTheDayPlugin() {
        item.setToolTipText(I18n.getToolTip("TipOfTheDay"));
        item.addActionListener(arg0 -> {
            showTips();
        });
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(I18n.get("?"), 1000, item, 910) };
    }

    private synchronized JTipOfTheDay getTips() {
        if (tips == null) {
            String[] text = FileUtils.readTextFile(
                    I18nUtils.getLocalizedFile(Utils.getUserDir() + "tips" + File.separator + "tips.txt"), "Cp1252");

            DefaultTip[] tiptext = new DefaultTip[text.length];
            for (int x = 0; x < tiptext.length; x++) {
                tiptext[x] = new DefaultTip("", text[x]);
            }

            UIManager.put("TipOfTheDay.didYouKnowText", I18n.get("TipOfTheDay.didYouKnowText"));

            tips = new JTipOfTheDay(new DefaultTipModel(tiptext));
            tips.setCurrentTip((int) (Math.random() * tiptext.length));
        }

        return tips;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (due.isReason(UpdateEventConstants.REASON_STARTUP)) {
            showTipAtStartup();
        }
    }

    void showTips() {
        SwingUtilities.invokeLater(() -> {
            if (dialog == null) {
                dialog = createDialog();
            }
            dialog.setVisible(true);
        });
    }

    JDialog createDialog() {
        ShowOnStartupChoice choice = new ShowOnStartupChoice() {
            @Override
            public boolean isShowingOnStartup() {
                return Utils.getPreferences().node("l2fprod").getBoolean(JTipOfTheDay.PREFERENCE_KEY, true);
            }

            @Override
            public void setShowingOnStartup(boolean showOnStartup) {
                if (showOnStartup != isShowingOnStartup()) {
                    Utils.getPreferences().node("l2fprod").putBoolean(JTipOfTheDay.PREFERENCE_KEY, showOnStartup);
                }
            }
        };
        return getTips().getUI().createDialog(getController().getWindow(), choice);
    }

    void showTipAtStartup() {
        SwingUtilities.invokeLater(this::showTipAtStartupI);
    }

    void showTipAtStartupI() {
        getTips().showDialog(getController().getWindow(), Utils.getPreferences().node("l2fprod"));
    }
}
