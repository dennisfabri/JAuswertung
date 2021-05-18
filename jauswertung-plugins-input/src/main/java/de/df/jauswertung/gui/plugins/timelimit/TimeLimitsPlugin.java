package de.df.jauswertung.gui.plugins.timelimit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.TimelimitsContainer;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class TimeLimitsPlugin extends ANullPlugin {

    private MenuInfo[]                           menues;
    private CorePlugin                           core;

    private JMenuItem                            edit;
    private JMenuItem                            check;

    private ISimpleCallback<TimelimitsContainer> callback = new ISimpleCallback<TimelimitsContainer>() {

                                                              @Override
                                                              public void callback(TimelimitsContainer tlc) {
                                                                  saveResult(tlc);
                                                              }
                                                          };

    public TimeLimitsPlugin() {
        edit = new JMenuItem(I18n.get("Edit"));
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                showEditor();
            }
        });
        edit.setToolTipText(I18n.getToolTip("Edit"));

        check = new JMenuItem(I18n.get("Check"));
        check.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // showEditor(false);
            }
        });
        check.setToolTipText(I18n.getToolTip("Check"));
        check.setEnabled(true);

        JMenu menu = new JMenu(I18n.get("Timelimits"));
        menu.setToolTipText(I18n.getToolTip("Timelimits"));
        menu.add(edit);
        menu.add(check);

        menues = new MenuInfo[1];
        menues[0] = new MenuInfo(I18n.get("Edit"), 500, menu, 975);
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
        JTimelimitsEditor editor = new JTimelimitsEditor(getController().getWindow(), wk.getTimelimits(), callback);
        ModalFrameUtil.showAsModal(editor, getController().getWindow());
    }

    void saveResult(TimelimitsContainer tlc) {
        if (tlc != null) {
            AWettkampf<?> wk = core.getWettkampf();
            wk.getTimelimits().update(tlc);
            getController().sendDataUpdateEvent(I18n.get("Timelimits"), UpdateEventConstants.REASON_TIMELIMITS_CHANGED, this);
        }
    }
}