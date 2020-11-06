package de.df.jauswertung.gui.plugins.zulassung;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Qualifikation;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MZulassungPlugin extends ANullPlugin {

    private JMenu      menu   = new JMenu(I18n.get("Zulassung"));
    private JMenuItem  neu    = new JMenuItem(I18n.get("New"));
    private JMenuItem  delete = new JMenuItem(I18n.get("RemoveNonQualified"));
    private JMenuItem  reset  = new JMenuItem(I18n.get("ResetZulassung"));

    private CorePlugin core   = null;

    public MZulassungPlugin() {
        menu.setEnabled(false);
        menu.add(neu);
        menu.add(reset);
        menu.add(delete);

        neu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startWizard();
            }
        });
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });
    }

    void reset() {
        if (!DialogUtils.ask(getController().getWindow(), I18n.get("ResetZulassungFrage"), I18n.get("ResetZulassungFrage.Note"))) {
            return;
        }

        AWettkampf<?> wk = core.getWettkampf();
        LinkedList<? extends ASchwimmer> swimmers = wk.getSchwimmer();

        boolean changed = false;
        for (ASchwimmer t : swimmers) {
            if (!t.getQualifikation().isManual()) {
                t.setQualifikation(Qualifikation.OFFEN);
                changed = true;
            }
        }

        if (changed) {
            getController().sendDataUpdateEvent("Zulassung", UpdateEventConstants.REASON_SWIMMER_CHANGED, this);
        }
    }

    void delete() {
        JRemoveZulassung.start(getController().getWindow(), getController(), core);
    }

    void startWizard() {
        JZulassungWizard wiz = new JZulassungWizard(getController().getWindow(), core, getController());
        wiz.start(true);
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        menu.setEnabled(core.getWettkampf().hasSchwimmer());
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(I18n.get("Edit"), 500, menu, 93) };
    }
}