package de.df.jauswertung.gui.plugins.switchagegroups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

public class BSwitchAgeGroupsPlugin extends ANullPlugin {

    private final ButtonInfo     bi;
    private final JButton        button;

    private CorePlugin           core    = null;

    private final static boolean ENABLED = Utils.isInDevelopmentModeFor("SwitchAgeGroups");

    public BSwitchAgeGroupsPlugin() {
        if (ENABLED) {
            button = new JButton(IconManager.getSmallIcon("switch"));
            button.setToolTipText(I18n.get("SwitchAgeGroups"));
            bi = new ButtonInfo(button, 9000);

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switchAGs();
                }
            });
        } else {
            button = null;
            bi = null;
        }
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (ENABLED) {
            button.setEnabled(core.getWettkampf().hasSchwimmer());
        }
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        if (!ENABLED) {
            return new ButtonInfo[] {};
        }
        return new ButtonInfo[] { bi };
    }

    @SuppressWarnings({})
    void switchAGs() {
        LinkedList<ASchwimmer> notchanged = CompetitionUtils.switchAGs(core.getWettkampf());
        if (core.getWettkampf().getSchwimmeranzahl() > (notchanged == null ? 0 : notchanged.size())) {
            getController().sendDataUpdateEvent("ChangePerson", UpdateEventConstants.REASON_SWIMMER_CHANGED, this);
            if (notchanged != null) {
                StringBuffer sb = new StringBuffer();

                ListIterator<ASchwimmer> li = notchanged.listIterator();
                while (li.hasNext()) {
                    ASchwimmer t = li.next();
                    sb.append("\n").append(t.getName()).append(" (").append(t.getBemerkung()).append(")");
                }

                DialogUtils.wichtigeMeldung(getController().getWindow(), I18n.get("Error.NotAllSwimmersCouldBeChanged", sb.toString()));
            }
        } else {
            DialogUtils.wichtigeMeldung(getController().getWindow(), I18n.get("Info.NoSwimmersHaveBeenChanged"));
        }
    }
}