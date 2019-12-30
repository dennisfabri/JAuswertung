/*
 * Created on 24.02.2005
 */
package de.df.jauswertung.gui.plugins.editor;

import java.awt.Window;
import java.util.LinkedList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.*;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

public class FEditorPlugin extends AFeature {

    private OGeneralOptionsPlugin penaltyPrint = null;
    private CorePlugin            core         = null;

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        penaltyPrint = (OGeneralOptionsPlugin) plugincontroller.getFeature("de.df.jauswertung.generaloptions", pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        // Nothing to do
    }

    public boolean toggleNames(ASchwimmer... s) {
        if (core.getEinzelWettkampf() != null) {
            if (s != null && s.length > 0) {
                for (ASchwimmer value : s) {
                    Teilnehmer t = (Teilnehmer) value;
                    String vorname = t.getNachname();
                    String nachname = t.getVorname();
                    t.setVorname(vorname);
                    t.setNachname(nachname);
                }
                getController().sendDataUpdateEvent("ChangePerson", UpdateEventConstants.REASON_SWIMMER_CHANGED, this);
            }
            return true;
        }
        return false;
    }

    public void runTimeEditor(ASchwimmer s, int disz) {
        JTimeEditor jte = new JTimeEditor(getController(), s, disz);
        SwingUtilities.invokeLater(new WindowLauncher(jte));
    }

    public void runMeanTimeEditor(ASchwimmer s, int disz) {
        runMeanTimeEditor(s, disz, null);
    }

    public void runMeanTimeEditor(ASchwimmer s, int disz, ISimpleCallback<Boolean> cb) {
        JMeanTimeEditor jte = new JMeanTimeEditor(getController(), s, disz, cb);
        SwingUtilities.invokeLater(new WindowLauncher(jte));
    }

    public void runZWEditor(ASchwimmer s) {
        JZWEditor jhe = new JZWEditor(getController(), s);
        SwingUtilities.invokeLater(new WindowLauncher(jhe));
    }

    public void editSchwimmer(ASchwimmer s, boolean delete) {
        editSchwimmer(getController().getWindow(), s, delete);
    }

    public void editSchwimmer(JFrame parent, ASchwimmer s, boolean delete) {
        if (s instanceof Teilnehmer) {
            edit(parent, (Teilnehmer) s, delete);
            return;
        }
        if (s instanceof Mannschaft) {
            edit(parent, (Mannschaft) s, delete);
        }

    }

    public void editSchwimmer(JDialog parent, ASchwimmer s, boolean delete) {
        if (s instanceof Teilnehmer) {
            edit(parent, (Teilnehmer) s, delete);
            return;
        }
        if (s instanceof Mannschaft) {
            edit(parent, (Mannschaft) s, delete);
        }

    }

    public void edit(Teilnehmer t, boolean delete) {
        edit(getController().getWindow(), t, delete);
    }

    public void edit(JFrame parent, Teilnehmer t, boolean delete) {
        JSchwimmerEditieren<Teilnehmer> jte = new JSchwimmerEditieren<Teilnehmer>(t, core.getEinzelWettkampf(), parent, delete, core);
        editAndUpdateSwimmer(t, jte);
    }

    public void edit(JDialog parent, Teilnehmer t, boolean delete) {
        JSchwimmerEditieren<Teilnehmer> jte = new JSchwimmerEditieren<Teilnehmer>(t, core.getEinzelWettkampf(), parent, delete, core);
        editAndUpdateSwimmer(t, jte);
    }

    public boolean editOrganization(JFrame parent, String gld) {
        return editAndUpdateOrganization(new JOrganizationEditor(getController(), core, gld, parent));
    }

    public void edit(Mannschaft t, boolean delete) {
        edit(getController().getWindow(), t, delete);
    }

    public void edit(JFrame parent, Mannschaft t, boolean delete) {
        JSchwimmerEditieren<Mannschaft> jmal = new JSchwimmerEditieren<Mannschaft>(t, core.getMannschaftWettkampf(), parent, delete, core);
        editAndUpdateTeam(t, jmal);
    }

    public void edit(JDialog parent, Mannschaft t, boolean delete) {
        JSchwimmerEditieren<Mannschaft> jmal = new JSchwimmerEditieren<Mannschaft>(t, core.getMannschaftWettkampf(), parent, delete, core);
        editAndUpdateTeam(t, jmal);
    }

    private void editAndUpdateTeam(Mannschaft t, JSchwimmerEditieren<Mannschaft> jmal) {
        jmal.setVisible(true);
        if (jmal.hasChanged()) {
            if (SearchUtils.getSchwimmer(core.getMannschaftWettkampf(), t) == null) {
                // getController().sendDataUpdateEvent("RemoveTeam",
                // UpdateEventConstants.REASON_SWIMMER_DELETED, t, null,
                // this);
            } else {
                getController().sendDataUpdateEvent("ChangeTeam", UpdateEventConstants.REASON_SWIMMER_CHANGED, t, null, this);
            }
        }
    }

    private void editAndUpdateSwimmer(Teilnehmer t, JSchwimmerEditieren<Teilnehmer> jmal) {
        jmal.setVisible(true);
        if (jmal.hasChanged()) {
            if (SearchUtils.getSchwimmer(core.getEinzelWettkampf(), t) == null) {
                // getController().sendDataUpdateEvent("RemoveSwimmer",
                // UpdateEventConstants.REASON_SWIMMER_DELETED, t, null,
                // this);
            } else {
                getController().sendDataUpdateEvent("ChangeSwimmer", UpdateEventConstants.REASON_SWIMMER_CHANGED, t, null, this);
            }
        }
    }

    private boolean editAndUpdateOrganization(JOrganizationEditor jmal) {
        jmal.setVisible(true);
        return jmal.hasChanged();
    }

    public <T extends ASchwimmer> void runPenaltyPoints(AWettkampf<T> wk, T s, int disz) {
        JPenaltyPointsAdditor<T> jte = new JPenaltyPointsAdditor<T>(getController(), wk, s, disz);
        SwingUtilities.invokeLater(new WindowLauncher(jte));
    }

    public <T extends ASchwimmer> void runPenaltyPoints(AWettkampf<T> wk, T s, String id) {
        JPenaltyPointsAdditor<T> jte = new JPenaltyPointsAdditor<T>(getController(), wk, s, id);
        SwingUtilities.invokeLater(new WindowLauncher(jte));
    }

    public <T extends ASchwimmer> void runPenaltyCode(AWettkampf<T> wk, T s, int disz, Strafen str) {
        JPenaltyAdditor<T> jte = new JPenaltyAdditor<T>(getController(), wk, s, disz, str);
        SwingUtilities.invokeLater(new WindowLauncher(jte));
    }

    public <T extends ASchwimmer> void runPenaltyCode(AWettkampf<T> wk, T s, String id, Strafen str) {
        JPenaltyAdditor<T> jte = new JPenaltyAdditor<T>(getController(), wk, s, id, str);
        SwingUtilities.invokeLater(new WindowLauncher(jte));
    }

    public <T extends ASchwimmer> void runPenaltyEditor(JFrame parent, AWettkampf<T> wk, T s) {
        runPenaltyEditor(parent, wk, s, ASchwimmer.DISCIPLINE_NUMBER_SELF);
    }

    public <T extends ASchwimmer> void runPenaltyEditor(AWettkampf<T> wk, T s, int disz) {
        runPenaltyEditor(getController().getWindow(), wk, s, disz);
    }

    public <T extends ASchwimmer> void runPenaltyEditor(AWettkampf<T> wk, T s, String id) {
        runPenaltyEditor(getController().getWindow(), wk, s, id);
    }

    public <T extends ASchwimmer> void runPenaltyEditor(JFrame parent, AWettkampf<T> wk, T s, int disz) {
        if ((s.getAK().getDiszAnzahl() < disz)) {
            disz = s.getAK().getDiszAnzahl();
        }
        JPenaltyEditor<T> editor = new JPenaltyEditor<T>(parent, getController(), wk, s, disz, penaltyPrint.getPrintEnabled());
        ModalFrameUtil.showAsModal(editor, parent);
    }

    public <T extends ASchwimmer> void runPenaltyEditor(JFrame parent, AWettkampf<T> wk, T s, String disz) {
        JPenaltyEditor<T> editor = new JPenaltyEditor<T>(parent, getController(), wk, s, disz, penaltyPrint.getPrintEnabled());
        ModalFrameUtil.showAsModal(editor, parent);
    }

    public <T extends ASchwimmer> void runPenaltyEditor(JDialog parent, AWettkampf<T> wk, T s, int disz) {
        if ((s.getAK().getDiszAnzahl() < disz)) {
            disz = s.getAK().getDiszAnzahl();
        }
        JPenaltyEditor<T> editor = new JPenaltyEditor<T>(parent, getController(), wk, s, disz, penaltyPrint.getPrintEnabled());
        ModalFrameUtil.showAsModal(editor, parent);
    }

    public <T extends ASchwimmer> void runPenaltyEditor(JDialog parent, AWettkampf<T> wk, T s, String disz) {
        JPenaltyEditor<T> editor = new JPenaltyEditor<T>(parent, getController(), wk, s, disz, penaltyPrint.getPrintEnabled());
        ModalFrameUtil.showAsModal(editor, parent);
    }

    private static class WindowLauncher implements Runnable {

        private Window window = null;

        public WindowLauncher(Window w) {
            window = w;
        }

        @Override
        public void run() {
            window.setVisible(true);
        }
    }

    public void renameOrganization(JFrame parent, AWettkampf<ASchwimmer> wk, ASchwimmer t) {
        String value = DialogUtils.showTextDialog(parent, I18n.get("RenameOrganization.Title"),
                I18n.get("RenameOrganization.Text", t.getGliederungMitQGliederung()), t.getGliederung(), 30);
        if (value != null) {
            LinkedList<ASchwimmer> list = SearchUtils.getSchwimmer(wk, new String[] { t.getGliederungMitQGliederung() }, true);
            for (ASchwimmer s : list) {
                s.setGliederung(value);
            }
            getController().sendDataUpdateEvent("ChangeTeam", UpdateEventConstants.REASON_SWIMMER_CHANGED, list, null, this);
        }
    }

    public void renameQualiOrganization(JFrame parent, AWettkampf<ASchwimmer> wk, ASchwimmer t) {
        String value = DialogUtils.showTextDialog(parent, I18n.get("RenameQualiOrganization.Title"),
                I18n.get("RenameQualiOrganization.Text", t.getQualifikationsebene()), t.getQualifikationsebene(), 30);
        if (value != null) {
            LinkedList<ASchwimmer> list = SearchUtils.getSchwimmerForQGld(wk, t.getQualifikationsebene());
            for (ASchwimmer s : list) {
                s.setQualifikationsebene(value);
            }
            getController().sendDataUpdateEvent("ChangeTeam", UpdateEventConstants.REASON_SWIMMER_CHANGED, list, null, this);
        }
    }
}