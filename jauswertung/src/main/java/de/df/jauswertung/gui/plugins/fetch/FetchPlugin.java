package de.df.jauswertung.gui.plugins.fetch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JMenuItem;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.util.HttpUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;

public class FetchPlugin extends ANullPlugin {

    private CorePlugin core    = null;

    private MenuInfo[] menu    = null;
    private JMenuItem  item    = null;

    private String     lastURL = "";

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public MenuInfo[] getMenues() {
        if (menu == null) {
            item = new JMenuItem(I18n.get("FetchCompetition"));
            item.setToolTipText(I18n.getToolTip("FetchCompetition"));
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    showWindow();
                }
            });
            item.setAccelerator(I18n.getKeyStroke("FetchCompetition"));

            menu = new MenuInfo[1];
            menu[0] = new MenuInfo(I18n.get("Information"), 550, item, 2010);
        }
        return menu;
    }

    private void showWindow() {
        String text = DialogUtils.showTextDialog(getController().getWindow(), I18n.get("FetchCompetition"), I18n.get("Server"), lastURL);
        if (text == null) {
            return;
        }
        lastURL = text.trim();
        byte[] data = null;
        try {
            data = HttpUtils.download("http://" + lastURL + "/wettkampf.wk");
        } catch (IOException e) {
            System.err.println("FetchPlugin: Wettkampf konnte nicht geladen werden.");
            e.printStackTrace();
            DialogUtils.wichtigeMeldung(getController().getWindow(), I18n.get("Error.DownloadOfCompetitionFailed"));
            return;
        }
        AWettkampf<?> wk = InputManager.ladeWettkampf(data);
        if (wk == null) {
            System.err.println("FetchPlugin: Wettkampf konnte nicht geladen werden.");
            DialogUtils.wichtigeMeldung(getController().getWindow(), I18n.get("Error.DownloadOfCompetitionFailed"));
            return;
        }
        core.setWettkampf(wk, true);
    }

}
