/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.teammembersscan;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.tasks.JTaskPaneGroup;

import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JTeammembersStatusPanel;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.TeamUtils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jauswertung.util.valueobjects.Team;
import de.df.jauswertung.util.valueobjects.Teammember;
import de.df.jutils.graphics.ColorUtils;
import de.df.jutils.gui.JDottingLabel;
import de.df.jutils.gui.JSignal;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.plaf.GradientTaskPaneGroupUI;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.util.StringTools;
import net.miginfocom.swing.MigLayout;

/**
 * @author Dennis Fabri
 * @date 27.03.2010
 */
class JTeammembersBarcodeScanPanel extends JPanel {
    private static final Color GREEN = new Color(100, 222, 75);
    private static final Color YELLOW = new Color(255, 210, 50);
    private static final Color RED = new Color(255, 60, 50);
    private static final Color ORANGE = ColorUtils.calculateColor(YELLOW, RED, 0.5);

    final IPluginManager controller;
    private final CorePlugin core;
    final PTeammembersScanPlugin parent;

    JWarningTextField input;
    JButton enter;

    JButton helpId;
    JButton listIds;
    JButton generateId;
    JLabel exportId;

    private JLabel startnumber;
    private JLabel name;
    private JLabel organisation;
    private JLabel agegroup;

    private JPanel details;

    private JLabel[] ids = new JLabel[0];
    private JDottingLabel[] firstnames = new JDottingLabel[0];
    private JDottingLabel[] lastnames = new JDottingLabel[0];
    private JLabel[] sexes = new JLabel[0];
    private JLabel[] yearofbirths = new JLabel[0];

    private JSignal statusOk;
    private JSignal statusPartial;
    private JSignal statusNok;
    private JSignal barcodeError;

    private JTeammembersStatusPanel overview = null;

    private static long OVERVIEW_REASONS = UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_LOAD_WK
            | UpdateEventConstants.REASON_NEW_LOAD_WK | UpdateEventConstants.REASON_NEW_TN
            | UpdateEventConstants.REASON_NEW_WK | UpdateEventConstants.REASON_PENALTY
            | UpdateEventConstants.REASON_TEAMASSIGNMENT_CHANGED | UpdateEventConstants.REASON_SWIMMER_CHANGED
            | UpdateEventConstants.REASON_SWIMMER_DELETED | UpdateEventConstants.REASON_TEAMMEMBERS_EXPORTID_CHANGED;

    public JTeammembersBarcodeScanPanel(PTeammembersScanPlugin parent, IPluginManager controller, CorePlugin core) {
        this.controller = controller;
        this.core = core;
        this.parent = parent;
        initPanel();
    }

    void prepare() {
        clear(true);
    }

    void clear(boolean cleartext) {
        clear(cleartext, true);
    }

    void clear(boolean cleartext, boolean requestFocus) {
        if (cleartext) {
            input.setText("");
        } else {
            if (requestFocus) {
                input.selectAll();
            }
        }
        if (requestFocus) {
            input.requestFocus();
        }

        startnumber.setText(" ");
        name.setText(" ");
        organisation.setText(" ");
        agegroup.setText(" ");

        ShowDetails(null);

        statusPartial.setEnabled(false);
        statusOk.setEnabled(false);
        statusNok.setEnabled(false);
        barcodeError.setEnabled(false);
    }

    void signalBarcodeError() {
        clear(false);
        barcodeError.setEnabled(true);
    }

    void signalZWOk() {
        clear(true);
        statusOk.setEnabled(true);
    }

    void signalZWNok() {
        clear(true);
        statusNok.setEnabled(true);
    }

    void signalZWDNS() {
        clear(true);
        statusPartial.setEnabled(true);
    }

    boolean enterValue() {
        if (input.getText().length() == 0) {
            input.requestFocus();
            return false;
        }
        Team team = null;
        boolean ok = false;
        try {
            team = Team.FromCode(input.getText().trim());
        } catch (IOException ioex) {
            // Nothing to do
        }
        ok = team != null;
        if (!ok) {
            Toolkit.getDefaultToolkit().beep();
            signalBarcodeError();
            return false;
        }

        MannschaftWettkampf wk = core.getMannschaftWettkampf();
        @SuppressWarnings("null")
        Mannschaft s = SearchUtils.getSchwimmer(wk, team.getId());
        if (s == null) {
            signalBarcodeError();
            return false;
        }
        boolean doit = true;

        if (!checkExportId(wk, team)) {
            return false;
        }

        // If zw points are already set:
        if (s.hasMannschaftsmitglieder()) {
            boolean ask = false;

            for (int x = 0; x < Math.min(s.getMaxMembers(), team.getMemberCount()); x++) {
                Mannschaftsmitglied mm = s.getMannschaftsmitglied(x);
                Teammember tm = team.getMember(x);
                if (!mm.getVorname().equals(tm.getFirstname())) {
                    ask = true;
                    break;
                }
                if (!mm.getNachname().equals(tm.getLastname())) {
                    ask = true;
                    break;
                }
                if (mm.getJahrgang() != tm.getJahrgang()) {
                    ask = true;
                    break;
                }
                if (!mm.getGeschlecht().equals(tm.getGeschlecht())) {
                    ask = true;
                    break;
                }
            }

            for (int x = team.getMemberCount(); x < s.getMaxMembers(); x++) {
                if (!s.getMannschaftsmitglied(x).isEmpty()) {
                    ask = true;
                    break;
                }
            }

            if (ask) {
                doit = DialogUtils.ask(controller.getWindow(), I18n.get("Question.TeammembersAlreadyPresent"),
                        I18n.get("Question.TeammembersAlreadyPresent.Note"));
            }
        }

        if (doit) {
            for (int x = 0; x < Math.min(s.getMaxMembers(), team.getMemberCount()); x++) {
                Mannschaftsmitglied mm = s.getMannschaftsmitglied(x);
                Teammember tm = team.getMember(x);
                mm.setVorname(tm.getFirstname());
                mm.setNachname(tm.getLastname());
                mm.setJahrgang(tm.getJahrgang());
                mm.setGeschlecht(tm.getGeschlecht());
            }

            for (int x = team.getMemberCount(); x < s.getMaxMembers(); x++) {
                s.getMannschaftsmitglied(x).clear();
            }

            if (s.isMannschaftsmitgliederComplete()) {
                signalZWOk();
            } else if (s.isMannschaftsmitgliederComplete()) {
                signalZWDNS();
            } else {
                signalZWNok();
            }

            StringBuilder sb = new StringBuilder();
            startnumber.setText(StartnumberFormatManager.format(s));
            sb.setLength(0);

            sb.append(s.getName());
            name.setText(sb.toString());
            sb.setLength(0);

            sb.append(s.getGliederung());
            if (s.getQualifikationsebene().length() > 0) {
                sb.append(" (");
                sb.append(s.getQualifikationsebene());
                sb.append(")");
            }
            organisation.setText(sb.toString());
            sb.setLength(0);

            agegroup.setText(I18n.getAgeGroupAsString(s));

            ShowDetails(s);

            controller.sendDataUpdateEvent("ChangeTeam", UpdateEventConstants.REASON_SWIMMER_CHANGED, s, -1, parent);
        } else {
            clear(true);
        }
        return true;
    }

    private enum IdState {
        ok, old, notExpected, expectedButMissing, notFound
    }

    private boolean checkExportId(MannschaftWettkampf wk, Team team) {
        String currentId = wk.getCurrentTeammembersRegistrationsId();
        String teamId = team.getExportId();

        IdState state = IdState.ok;

        if (StringUtils.isEmpty(currentId) != StringUtils.isEmpty(teamId)) {
            if (StringUtils.isEmpty(currentId)) {
                state = IdState.notExpected;
            } else {
                state = IdState.expectedButMissing;
            }
        } else if (currentId != null) {
            if (currentId.equals(teamId)) {
                state = IdState.ok;
            } else {
                state = IdState.notFound;
                for (String id : wk.getTeammembersRegistrationIds()) {
                    if (id.equals(teamId)) {
                        state = IdState.old;
                    }
                }
            }
        }

        String question = I18n.get("Question.WrongExportId");
        String note = "";
        switch (state) {
        case ok:
            break;
        case expectedButMissing:
            note = I18n.get("Question.WrongExportId.Note.ExportIdIsMissing");
            break;
        case notExpected:
            note = I18n.get("Question.WrongExportId.Note.ExportIdIsNotExpected", teamId);
            break;
        case notFound:
            note = I18n.get("Question.WrongExportId.Note.ExportIdDoesNotMatch", teamId);
            break;
        case old:
            note = I18n.get("Question.WrongExportId.Note.ExportIdIsOld", teamId);
            break;
        }

        if (state != IdState.ok) {
            return DialogUtils.ask(controller.getWindow(), question, note);
        }
        return true;

    }

    private void ShowDetails(Mannschaft s) {

        int amount = ids.length;
        if (s != null) {
            amount = s.getMaxMembers();
        }
        if (amount != ids.length) {
            JLabel[] newids = new JLabel[amount];
            JDottingLabel[] newfirstnames = new JDottingLabel[amount];
            JDottingLabel[] newlastnames = new JDottingLabel[amount];
            JLabel[] newsexes = new JLabel[amount];
            JLabel[] newyearofbirths = new JLabel[amount];

            for (int x = 0; x < ids.length; x++) {
                newids[x] = ids[x];
                newfirstnames[x] = firstnames[x];
                newlastnames[x] = lastnames[x];
                newsexes[x] = sexes[x];
                newyearofbirths[x] = yearofbirths[x];
            }
            for (int x = ids.length; x < amount; x++) {
                newids[x] = new JLabel("" + (x + 1));
                newfirstnames[x] = new JDottingLabel();
                newlastnames[x] = new JDottingLabel();
                newsexes[x] = new JLabel();
                newyearofbirths[x] = new JLabel();
            }

            ids = newids;
            firstnames = newfirstnames;
            lastnames = newlastnames;
            sexes = newsexes;
            yearofbirths = newyearofbirths;

            details.removeAll();
        }

        if (details.getComponentCount() == 0) {
            details.setLayout(new FormLayout(
                    "0dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,0dlu",
                    FormLayoutUtils.createLayoutString(amount + 1, 4, 0)));

            details.add(new JLabel(I18n.get("NumberShort")), CC.xy(2, 2));
            details.add(new JLabel(I18n.get("FamilyName")), CC.xy(4, 2));
            details.add(new JLabel(I18n.get("Firstname")), CC.xy(6, 2));
            details.add(new JLabel(I18n.get("Sex")), CC.xy(8, 2));
            details.add(new JLabel(I18n.get("YearOfBirth")), CC.xy(10, 2));

            for (int y = 0; y < amount; y++) {
                details.add(ids[y], CC.xy(2, 4 + 2 * y));
                details.add(lastnames[y], CC.xy(4, 4 + 2 * y));
                details.add(firstnames[y], CC.xy(6, 4 + 2 * y));
                details.add(sexes[y], CC.xy(8, 4 + 2 * y));
                details.add(yearofbirths[y], CC.xy(10, 4 + 2 * y));
            }
        }

        if (s != null) {
            for (int y = 0; y < amount; y++) {
                Mannschaftsmitglied mm = s.getMannschaftsmitglied(y);
                for (int x = 0; x < 5; x++) {
                    firstnames[y].setText(mm.getVorname());
                    lastnames[y].setText(mm.getNachname());
                    switch (mm.getGeschlecht()) {
                    case maennlich:
                        sexes[y].setText(I18n.get("sex2"));
                        break;
                    case weiblich:
                        sexes[y].setText(I18n.get("sex1"));
                        break;
                    case unbekannt:
                        sexes[y].setText("");
                        break;
                    }
                    if (mm.getJahrgang() <= 0) {
                        yearofbirths[y].setText("");
                    } else {
                        yearofbirths[y].setText("" + mm.getJahrgang());
                    }
                }
            }
        } else {
            for (int y = 0; y < amount; y++) {
                for (int x = 0; x < 5; x++) {
                    firstnames[y].setText("");
                    lastnames[y].setText("");
                    sexes[y].setText("");
                    yearofbirths[y].setText("");
                }
            }
        }
    }

    void initPanel() {

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu");
        JPanel panel = new JPanel(layout);
        panel.add(createOverviewPanel(), CC.xyw(2, 2, 3));
        panel.add(createInputPanel(), CC.xy(2, 4));
        panel.add(createIdPanel(), CC.xy(4, 4));
        panel.add(createStatusPanel(), CC.xyw(2, 6, 3));

        setLayout(new MigLayout("", "[fill, grow, ::1200lp]", "[fill]"));
        add(panel);

        clear(true);
    }

    private JPanel createOverviewPanel() {
        JTaskPaneGroup over = new JTaskPaneGroup();
        over.setUI(new GradientTaskPaneGroupUI());
        over.setTitle(I18n.get("Overview"));
        over.setOpaque(false);

        overview = new JTeammembersStatusPanel();
        over.add(overview);

        return over;
    }

    private JPanel createInputPanel() {
        input = new JWarningTextField(false, true);
        input.addFocusListener(new ResettingFocusAdapter());
        input.setValidator(value -> {
            if (input.getText().length() == 0) {
                return true;
            }
            return TeamUtils.checkBarcode(input.getText().trim());
        });
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown() && !e.isShiftDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        enterValue();
                        return;
                    }
                }
                if (!e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown() && !e.isShiftDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        enterValue();
                    }
                }
                if (TeamUtils.checkBarcode(input.getText())) {
                    enterValue();
                }
            }
        });

        enter = new JButton(I18n.get("Apply"));
        enter.addFocusListener(new ResettingFocusAdapter());
        enter.addActionListener(e -> {
            enterValue();
        });

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu");
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Input")));

        top.add(new JLabel(I18n.get("Input")), CC.xy(2, 2));
        top.add(input, CC.xy(4, 2));
        top.add(enter, CC.xy(4, 4, "right,fill"));

        return top;
    }

    private JPanel createIdPanel() {
        exportId = new JLabel();
        generateId = new JButton(I18n.get("Generate"));
        generateId.addActionListener(e -> {
            core.getMannschaftWettkampf().createTeammembersRegistrationsId();
            parent.sendDataUpdateEvent("ExportId", UpdateEventConstants.REASON_TEAMMEMBERS_EXPORTID_CHANGED);
        });
        helpId = new JButton(I18n.get("Help.Short"));
        helpId.addActionListener(e -> {
        });
        listIds = new JButton(I18n.get("ListIds"));
        listIds.addActionListener(e -> {
        });

        listIds.setEnabled(false);
        helpId.setEnabled(false);

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu");
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("ExportId")));

        top.add(new JLabel(I18n.get("Id")), CC.xy(2, 2));
        top.add(exportId, CC.xyw(4, 2, 3));
        top.add(helpId, CC.xy(2, 4, "left,fill"));
        top.add(listIds, CC.xy(4, 4, "right,fill"));
        top.add(generateId, CC.xy(6, 4, "fill,fill"));

        return top;
    }

    private JPanel createStatusPanel() {
        statusOk = new JSignal(I18n.get("Complete"));
        statusNok = new JSignal(I18n.get("Incomplete"));
        statusPartial = new JSignal(I18n.get("Names"));
        barcodeError = new JSignal(I18n.get("BarcodeNotCorrect"));
        statusOk.setBasecolor(GREEN);
        statusNok.setBasecolor(RED);
        statusPartial.setBasecolor(ORANGE);
        barcodeError.setBasecolor(YELLOW);

        startnumber = new JLabel();
        name = new JLabel();
        organisation = new JLabel();
        agegroup = new JLabel();
        details = new JPanel();

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,40dlu,fill:default,4dlu,fill:default:grow,4dlu",
                FormLayoutUtils.createLayoutString(8) + ",fill:default:grow,4dlu");
        layout.setColumnGroups(new int[][] { { 2, 6 } });
        layout.setRowGroups(new int[][] { { 2, 4, 6, 8, 10, 12, 14, 16 } });
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("StatusOfLastInput")));

        top.add(statusOk, CC.xywh(2, 2, 1, 3));
        top.add(statusNok, CC.xywh(2, 6, 1, 3));
        top.add(statusPartial, CC.xywh(2, 10, 1, 3));
        top.add(barcodeError, CC.xywh(2, 14, 1, 3));

        top.add(new JLabel(I18n.get("Startnumber")), CC.xy(4, 2));
        top.add(new JLabel(I18n.get("Name")), CC.xy(4, 4));
        top.add(new JLabel(I18n.get("Organisation")), CC.xy(4, 6));
        top.add(new JLabel(I18n.get("AgeGroup")), CC.xy(4, 8));

        top.add(startnumber, CC.xy(6, 2));
        top.add(name, CC.xy(6, 4));
        top.add(organisation, CC.xy(6, 6));
        top.add(agegroup, CC.xy(6, 8));

        top.add(details, CC.xywh(4, 12, 3, 7));

        ShowDetails(null);

        return top;
    }

    public synchronized void dataUpdated(UpdateEvent due) {
        if ((due.getChangeReason() & OVERVIEW_REASONS) > 0 && core.getMannschaftWettkampf() != null) {
            overview.setData(core.getMannschaftWettkampf(), null);
        }
        updateId();
        if (due.isSource(parent)) {
            return;
        }
        updateGUI();
    }

    void updateId() {
        if (exportId == null) {
            return;
        }
        String id = "";
        String tooltip = null;

        MannschaftWettkampf wk = core.getMannschaftWettkampf();
        if (wk != null) {
            id = wk.getCurrentTeammembersRegistrationsId();

            String[] tIds = wk.getTeammembersRegistrationIds();
            String[] exportIds = wk.getLast10TeammembersRegistrationIds();
            if (tIds != null && tIds.length > 0) {
                if (tIds.length > 11) {
                    tooltip = I18n.get("ExportIds.Tooltip3", id, StringTools.concat("<br/>- ", exportIds));
                } else if (exportIds.length > 0) {
                    tooltip = I18n.get("ExportIds.Tooltip2", id, StringTools.concat("<br/>- ", exportIds));
                } else {
                    tooltip = I18n.get("ExportIds.Tooltip1", id);
                }

            }
        }
        exportId.setText(id);
        exportId.setToolTipText(tooltip);
    }

    void updateGUI() {
        if (core.getMannschaftWettkampf() != null && core.getMannschaftWettkampf().hasSchwimmer()) {
            setEnabled(true);
        } else {
            setEnabled(false);
            clear(true);
        }
    }

    final class ResettingFocusAdapter extends FocusAdapter {
        @Override
        public void focusLost(FocusEvent e) {
            SwingUtilities.invokeLater(() -> {
                if (!(input.hasFocus())) {
                    clear(false, false);
                }
            });
        }
    }

    public boolean input(String path) {
        if (acceptsInput(path)) {
            input.setText(path);
            return enterValue();
        }
        return false;
    }

    public boolean acceptsInput(String path) {
        return input.hasFocus();
    }
}