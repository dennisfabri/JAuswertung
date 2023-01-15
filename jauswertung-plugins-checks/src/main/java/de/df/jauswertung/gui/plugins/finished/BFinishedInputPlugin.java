package de.df.jauswertung.gui.plugins.finished;

import javax.swing.JLabel;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

public class BFinishedInputPlugin extends ANullPlugin {

    private ButtonInfo[] buttons;
    private JStatusButton button;
    private CorePlugin core;

    private JLabel text = new JLabel();

    public BFinishedInputPlugin() {
        // Nothing to do
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);

        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void dataUpdated(UpdateEvent due) {
        AWettkampf wk = core.getWettkampf();
        button.setEnabled(wk.hasSchwimmer());
        int[] lastcomplete = wk.getLastComplete();
        if (lastcomplete[0] >= 0) {
            Altersklasse ak = wk.getRegelwerk().getAk(lastcomplete[0]);
            int male = lastcomplete[1];
            int discipline = lastcomplete[2];
            text.setText(
                    I18n.get("LastComplete", ak.getName(), I18n.geschlechtToShortString(wk.getRegelwerk(), male == 1),
                            ak.getDisziplin(discipline, male == 1).getName()));
        } else {
            text.setText("");
        }
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        if (buttons == null) {
            button = new JStatusButton(core);

            buttons = new ButtonInfo[2];
            buttons[0] = new ButtonInfo(button, 10000);
            buttons[1] = new ButtonInfo(text, 10001);
        }
        return buttons;
    }
}