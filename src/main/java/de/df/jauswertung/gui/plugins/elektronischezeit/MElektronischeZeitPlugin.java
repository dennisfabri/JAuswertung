package de.df.jauswertung.gui.plugins.elektronischezeit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.MOptionenPlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MElektronischeZeitPlugin extends ANullPlugin {

    private CorePlugin    core         = null;
    MOptionenPlugin       optionen     = null;
    private FEditorPlugin editor       = null;

    private MenuInfo[]    menu         = null;
    private JMenuItem     item         = null;

    private String        importedlast = "";

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        optionen = (MOptionenPlugin) plugincontroller.getPlugin("de.df.jauswertung.options", pluginuid);
        editor = (FEditorPlugin) plugincontroller.getFeature("de.df.jauswertung.editor", pluginuid);

        optionen.addOptionsPlugin(new ETimeOptionsPlugin(this));
    }

    @Override
    protected IPluginManager getController() {
        return super.getController();
    }

    FEditorPlugin getEditor() {
        return editor;
    }

    @Override
    public MenuInfo[] getMenues() {
        if (menu == null) {
            item = new JMenuItem(I18n.get("ElektronischeZeitnahme"));
            item.setToolTipText(I18n.getToolTip("ElektronischeZeitnahme"));
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    showWindow();
                }
            });
            item.setAccelerator(I18n.getKeyStroke("ElektronischeZeitnahme"));

            menu = new MenuInfo[1];
            menu[0] = new MenuInfo(I18n.get("Edit"), 300, item, 999);
            dataUpdated(null);
        }
        return menu;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (menu == null) {
            return;
        }
        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();
        boolean isEnabled = false;
        if (wk.hasSchwimmer()) {
            if (wk.isHeatBased()) {
                isEnabled = !wk.getLauflisteOW().isEmpty();
            } else if (wk.isDLRGBased()) {
                isEnabled = !wk.getLaufliste().isEmpty();
            }
        }

        item.setEnabled(isEnabled);
    }

    @SuppressWarnings({ "unchecked" })
    <T extends ASchwimmer> void showWindow() {
        JElektronischeZeit<T> jm = new JElektronischeZeit<T>(getController().getWindow(), (AWettkampf<T>) core.getWettkampf(), this);
        ModalFrameUtil.showAsModal(jm, getController().getWindow());
    }

    void setPort(int port) {
        Utils.getPreferences().putInt("TimeServerPort", port);
    }

    int getPort() {
        return Utils.getPreferences().getInt("TimeServerPort", 80);
    }

    void setLastImportedHeatID(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null!");
        }
        importedlast = id;
    }

    String getLastImportedHeatID() {
        return importedlast;
    }

    /**
     * @param jm
     */
    @SuppressWarnings("rawtypes")
    void notifyChange(JElektronischeZeit jm) {
        if (jm.hasChanged()) {
            getController().sendDataUpdateEvent("ElektronischeZeitnahme", UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_PENALTY,
                    this);
        }
    }
}