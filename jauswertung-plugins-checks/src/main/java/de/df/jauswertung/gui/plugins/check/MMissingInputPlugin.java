/*
 * Created on 18.02.2006
 */
package de.df.jauswertung.gui.plugins.check;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenuItem;

import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MMissingInputPlugin extends ANullPlugin {

    private JMenuItem[]        menu;
    private MenuInfo[]         minfo;

    private JButton            button;

    private CorePlugin         core;
    private FEditorPlugin      editor;

    private JMissingInputFrame frame = null;

    public MMissingInputPlugin() {
        button = new JButton(IconManager.getSmallIcon("check"));
        button.setToolTipText(I18n.getToolTip("CheckInput"));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                showWindow();
            }
        });

        menu = new JMenuItem[1];
        menu[0] = new JMenuItem(I18n.get("CheckInput"), IconManager.getSmallIcon("check"));
        menu[0].setToolTipText(I18n.getToolTip("CheckInput"));
        menu[0].setEnabled(false);
        menu[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                showWindow();
            }
        });
        menu[0].setAccelerator(I18n.getKeyStroke("checkdata"));

        minfo = new MenuInfo[1];
        minfo[0] = new MenuInfo(I18n.get("Prepare"), 510, menu, 2000);
    }

    void showWindow() {
        frame = new JMissingInputFrame(getController().getWindow(), core, editor, f -> notifiyChange());
        ModalFrameUtil.showAsModal(frame, getController().getWindow());
    }

    @Override
    public MenuInfo[] getMenues() {
        return minfo;
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        return new ButtonInfo[] { new ButtonInfo(button, 900) };
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        editor = (FEditorPlugin) plugincontroller.getFeature("de.df.jauswertung.editor", pluginuid);
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        boolean b = core.getWettkampf().hasSchwimmer();
        menu[0].setEnabled(b);
        button.setEnabled(b);
        if ((frame != null) && (frame.isVisible())) {
            frame.dataUpdate();
        }
    }

    /**
     * @param frame
     */
    void notifiyChange() {
        UpdateEvent ue = frame.getUpdateEvent(this);
        if (ue != null) {
            getController().sendDataUpdateEvent(ue);
        }
    }
}