package de.df.jauswertung.gui.plugins.timelimit;

import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.TimelimitsContainer;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class TimeLimitsPlugin extends ANullPlugin {

    private MenuInfo[] menues;
    private CorePlugin core;

    private JMenuItem edit;
    private JMenuItem check;

    private Consumer<TimelimitsContainer> callback = tlc -> saveResult(tlc);

    public TimeLimitsPlugin() {
        edit = new JMenuItem(I18n.get("Edit"));
        edit.addActionListener(arg0 -> {
            showEditor();
        });
        edit.setToolTipText(I18n.getToolTip("Edit"));

        check = new JMenuItem(I18n.get("Check"));
        check.addActionListener(arg0 -> {
            // showEditor(false);
        });
        check.setToolTipText(I18n.getToolTip("Check"));
        check.setEnabled(true);

        JMenu menu = new JMenu(I18n.get("Timelimits"));
        menu.setToolTipText(I18n.getToolTip("Timelimits"));
        menu.add(edit);
        menu.add(check);

        menues = new MenuInfo[1];
        menues[0] = new MenuInfo(I18n.get("Prepare"), 510, menu, 975);
    }

    @Override
    public void setController(IPluginManager controller, String pluginuid) {
        super.setController(controller, pluginuid);
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public MenuInfo[] getMenues() {
        return menues;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        boolean enabled = false;
        if (core != null) {
            AWettkampf<?> wk = core.getWettkampf();
            if (wk != null) {
                enabled = wk.getTimelimits().hasTimelimits();
            }
        }
        check.setEnabled(enabled);
    }

    private void showEditor() {
        AWettkampf<?> wk = core.getWettkampf();
        Sex female = new Sex(false, wk.getRegelwerk().getTranslation("Female", "Female"),
                wk.getRegelwerk().getTranslation("female", "female"),
                wk.getRegelwerk().getTranslation("femaleShort", "f"));
        Sex male = new Sex(true, wk.getRegelwerk().getTranslation("Male", "Male"),
                wk.getRegelwerk().getTranslation("male", "male"), wk.getRegelwerk().getTranslation("maleShort", "m"));

        JTimelimitsEditor editor = new JTimelimitsEditor(getController().getWindow(), wk.getTimelimits(), callback,
                female, male);
        ModalFrameUtil.showAsModal(editor, getController().getWindow());
    }

    void saveResult(TimelimitsContainer tlc) {
        if (tlc != null) {
            AWettkampf<?> wk = core.getWettkampf();
            wk.getTimelimits().update(tlc);
            getController().sendDataUpdateEvent(I18n.get("Timelimits"), UpdateEventConstants.REASON_TIMELIMITS_CHANGED,
                    this);
        }
    }
}