/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.teammembersinput;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.tasks.JTaskPaneGroup;

import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JTeammembersStatusPanel;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jauswertung.util.vergleicher.SchwimmerAKVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerGeschlechtVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerNameVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerStartnummernVergleicher;
import de.df.jutils.data.CompoundComparator;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.jtable.ColumnFittingMouseAdapter;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.jtable.SimpleTableModel;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.plaf.GradientTaskPaneGroupUI;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Mueller
 * @date 27.03.2010
 */
class JTeammembersInputPanel extends JPanel {

    private static final String                      INPUT                = I18n.get("Teammembers");

    final IPluginManager                             controller;
    private final CorePlugin                         core;
    final PTeammembersInputPlugin                    parent;

    private String                                   selectedOrganisation = null;

    private JLabel                                   startnumber;
    private JLabel                                   name;
    private JLabel                                   organisation;
    private JLabel                                   agegroup;

    private Mannschaft[]                             teams                = new Mannschaft[0];
    private JTable                                   teamstable;
    private DefaultTableModel                        model;

    private JPanel                                   input;

    private Mannschaft                               selectedTeam;

    JPanel                                           panel                = null;
    private JPanel                                   inputPanel           = null;
    private JTeammembersStatusPanel                  overview             = null;

    private static final JWarningTextField.Validator validator            = new Length50Validator();

    private static long                              OVERVIEW_REASONS     = UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_LOAD_WK
            | UpdateEventConstants.REASON_NEW_LOAD_WK | UpdateEventConstants.REASON_NEW_TN | UpdateEventConstants.REASON_NEW_WK
            | UpdateEventConstants.REASON_PENALTY | UpdateEventConstants.REASON_TEAMASSIGNMENT_CHANGED | UpdateEventConstants.REASON_SWIMMER_CHANGED
            | UpdateEventConstants.REASON_SWIMMER_DELETED;

    public JTeammembersInputPanel(PTeammembersInputPlugin parent, IPluginManager controller, CorePlugin core) {
        this.controller = controller;
        this.core = core;
        this.parent = parent;
        initPanel();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void updateModel() {
        if (selectedOrganisation == null) {
            teams = new Mannschaft[0];
        } else {
            MannschaftWettkampf mwk = core.getMannschaftWettkampf();
            teams = (Mannschaft[]) SearchUtils
                    .getSchwimmer(mwk, new String[] { selectedOrganisation }, true).stream().sorted(new CompoundComparator(new SchwimmerAKVergleicher<>(),
                            new SchwimmerGeschlechtVergleicher<>(), new SchwimmerNameVergleicher<>(), new SchwimmerStartnummernVergleicher<>()))
                    .toArray(size -> new Mannschaft[size]);
        }

        Object[][] data = new Object[teams.length][4];
        for (int x = 0; x < teams.length; x++) {
            Mannschaft m = teams[x];
            data[x][0] = StartnumberFormatManager.format(m);
            data[x][1] = m.getName();
            data[x][2] = m.getAK().getName();
            data[x][3] = I18n.geschlechtToShortString(m);
        }

        model = new SimpleTableModel(data, new Object[] { I18n.get("StartnumberShort"), I18n.get("Name"), I18n.get("AgeGroup"), I18n.get("Sex") });
        teamstable.setModel(model);
    }

    private JComponent createTable() {
        model = new SimpleTableModel(new Object[0][0], new Object[] { I18n.get("StartnumberShort"), I18n.get("Name"), I18n.get("AgeGroup"), I18n.get("Sex") });
        teamstable = new JTable(model);
        teamstable.getTableHeader().setReorderingAllowed(false);
        teamstable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableUtils.setAlternatingTableCellRenderer(teamstable);
        ColumnFittingMouseAdapter.enable(teamstable);

        teamstable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int index = teamstable.getSelectedRow();
                if (index >= 0 && index < teams.length) {
                    setSelectedTeam(teams[index]);
                } else {
                    setSelectedTeam(null);
                }
            }

        });

        return new JScrollPane(teamstable);
    }

    private void setSelectedTeam(Mannschaft mannschaft) {
        if (selectedTeam == null && mannschaft == null) {
            return;
        }
        int id1 = -1;
        int id2 = -1;
        if (selectedTeam != null) {
            id1 = selectedTeam.getStartnummer();
        }
        if (mannschaft != null) {
            id2 = mannschaft.getStartnummer();
        }
        if (id1 != id2) {
            selectedTeam = mannschaft;
            updateSelection();
        }
    }

    void initPanel() {
        startnumber = new JLabel();
        name = new JLabel();
        organisation = new JLabel();
        agegroup = new JLabel();

        JComponent table = createTable();

        inputPanel = new JPanel();
        JScrollPane scroller = new JScrollPane(inputPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getVerticalScrollBar().setUnitIncrement(10);
        scroller.getHorizontalScrollBar().setUnitIncrement(10);
        scroller.setBorder(null);

        JPanel in = new JPanel();
        in.setBorder(BorderUtils.createLabeledBorder(INPUT));
        in.setLayout(new FormLayout("0dlu,fill:default:grow,0dlu", "0dlu,fill:default,0dlu"));
        in.add(scroller, CC.xy(2, 2));

        JTaskPaneGroup over = new JTaskPaneGroup();
        over.setUI(new GradientTaskPaneGroupUI());
        over.setTitle(I18n.get("Overview"));
        over.setBackground(in.getBackground());
        over.setOpaque(false);

        overview = new JTeammembersStatusPanel();
        over.add(overview);

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu");
        setLayout(layout);
        add(over, CC.xyw(2, 2, 3));
        add(table, CC.xywh(2, 4, 1, 3));
        add(createInfoPanel(), CC.xy(4, 4));
        add(createInputPanel(), CC.xy(4, 6));
    }

    private JWarningTextField[] firstname = new JWarningTextField[0];
    private JWarningTextField[] lastname  = new JWarningTextField[0];
    private JIntegerField[]     year      = new JIntegerField[0];
    private JComboBox<String>[] sex       = new JComboBox[0];

    private JPanel createInputPanel() {
        input = new JPanel();
        input.setBorder(BorderUtils.createLabeledBorder(I18n.get("Input")));

        return input;
    }

    private JPanel createInfoPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(4));
        layout.setRowGroups(new int[][] { { 2, 4, 6, 8 } });
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Team")));

        top.add(new JLabel(I18n.get("Startnumber")), CC.xy(2, 2));
        top.add(new JLabel(I18n.get("Name")), CC.xy(2, 4));
        top.add(new JLabel(I18n.get("Organisation")), CC.xy(2, 6));
        top.add(new JLabel(I18n.get("AgeGroup")), CC.xy(2, 8));

        top.add(startnumber, CC.xy(4, 2));
        top.add(name, CC.xy(4, 4));
        top.add(organisation, CC.xy(4, 6));
        top.add(agegroup, CC.xy(4, 8));

        return top;
    }

    public synchronized void dataUpdated(UpdateEvent due) {
        if ((due.getChangeReason() & OVERVIEW_REASONS) > 0 && core.getMannschaftWettkampf() != null) {
            overview.setData(core.getMannschaftWettkampf(), selectedOrganisation);
        }
        if (due.isSource(parent)) {
            return;
        }
        updateGUI();
    }

    void updateGUI() {
        if (core.getMannschaftWettkampf() != null && core.getMannschaftWettkampf().hasSchwimmer()) {
            setEnabled(true);
            updateModel();
        } else {
            setEnabled(false);
        }
    }

    public void updateSelection() {
        if (selectedTeam == null) {
            startnumber.setText("");
            name.setText("");
            organisation.setText("");
            agegroup.setText("");
        } else {
            startnumber.setText(StartnumberFormatManager.format(selectedTeam));
            name.setText(selectedTeam.getName());
            organisation.setText(selectedTeam.getGliederungMitQGliederung());
            agegroup.setText(I18n.getAgeGroupAsString(selectedTeam));
        }
        updateInput();
    }

    private boolean isUpdating = false;

    public void updateInput() {
        isUpdating = true;
        try {
            if (selectedTeam == null) {
                for (int x = 0; x < firstname.length; x++) {
                    firstname[x].setText("");
                    lastname[x].setText("");
                    year[x].setText("");
                    sex[x].setSelectedIndex(2);

                    firstname[x].setEnabled(false);
                    lastname[x].setEnabled(false);
                    year[x].setEnabled(false);
                    sex[x].setEnabled(false);
                }
            } else {
                int rowcount = selectedTeam.getAK().getMaxMembers();
                if (rowcount != firstname.length) {
                    input.removeAll();

                    FormLayout layout = new FormLayout(
                            "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu",
                            FormLayoutUtils.createLayoutString(rowcount + 1));
                    input.setLayout(layout);

                    JWarningTextField[] firstnameOld = firstname;
                    JWarningTextField[] lastnameOld = lastname;
                    JIntegerField[] yearOld = year;
                    JComboBox<String>[] sexOld = sex;

                    firstname = new JWarningTextField[rowcount];
                    lastname = new JWarningTextField[rowcount];
                    year = new JIntegerField[rowcount];
                    sex = new JComboBox[rowcount];

                    input.add(new JLabel(I18n.get("NumberShort")), CC.xy(2, 2));
                    input.add(new JLabel(I18n.get("Firstname")), CC.xy(4, 2));
                    input.add(new JLabel(I18n.get("FamilyName")), CC.xy(6, 2));
                    input.add(new JLabel(I18n.get("Sex")), CC.xy(8, 2));
                    input.add(new JLabel(I18n.get("YearOfBirth")), CC.xy(10, 2));

                    for (int x = 0; x < rowcount; x++) {
                        if (x < firstnameOld.length) {
                            firstname[x] = firstnameOld[x];
                            lastname[x] = lastnameOld[x];
                            year[x] = yearOld[x];
                            sex[x] = sexOld[x];
                        } else {
                            firstname[x] = new JWarningTextField(false, true);
                            firstname[x].setValidator(validator);
                            firstname[x].setAutoSelectAll(true);
                            lastname[x] = new JWarningTextField(false, true);
                            lastname[x].setValidator(validator);
                            lastname[x].setAutoSelectAll(true);
                            year[x] = new JIntegerField(3000, false, true);
                            year[x].setAutoSelectAll(true);
                            sex[x] = new JComboBox<String>(new String[] { I18n.get("sex1"), I18n.get("sex2"), "-" });

                            firstname[x].getDocument().addDocumentListener(new InputChangedListener(x));
                            lastname[x].getDocument().addDocumentListener(new InputChangedListener(x));
                            year[x].getDocument().addDocumentListener(new InputChangedListener(x));
                            sex[x].addItemListener(new InputChangedListener(x));
                        }

                        input.add(new JLabel("" + (x + 1)), CC.xy(2, 4 + 2 * x));
                        input.add(firstname[x], CC.xy(4, 4 + 2 * x));
                        input.add(lastname[x], CC.xy(6, 4 + 2 * x));
                        input.add(sex[x], CC.xy(8, 4 + 2 * x));
                        input.add(year[x], CC.xy(10, 4 + 2 * x));
                    }
                }

                for (int x = 0; x < rowcount; x++) {
                    Mannschaftsmitglied mm = selectedTeam.getMannschaftsmitglied(x);
                    firstname[x].setText(mm.getVorname());
                    lastname[x].setText(mm.getNachname());
                    if (mm.getJahrgang() <= 0) {
                        year[x].setText("");
                    } else {
                        year[x].setInt(mm.getJahrgang());
                    }
                    switch (mm.getGeschlecht()) {
                    case weiblich:
                        sex[x].setSelectedIndex(0);
                        break;
                    case maennlich:
                        sex[x].setSelectedIndex(1);
                        break;
                    case unbekannt:
                        sex[x].setSelectedIndex(2);
                        break;
                    }

                    firstname[x].setEnabled(true);
                    lastname[x].setEnabled(true);
                    year[x].setEnabled(true);
                    sex[x].setEnabled(true);
                }
            }
        } finally {
            isUpdating = false;
        }
    }

    public void setSelectedOrganisation(String selection) {
        selectedOrganisation = selection;
        overview.setData(core.getMannschaftWettkampf(), selectedOrganisation);
        updateModel();
    }

    public void rowChanged(int row) {
        if (selectedTeam == null) {
            return;
        }
        if (isUpdating) {
            return;
        }
        Mannschaftsmitglied mm = selectedTeam.getMannschaftsmitglied(row);
        mm.setVorname(firstname[row].getText());
        mm.setNachname(lastname[row].getText());
        try {
            mm.setJahrgang(year[row].getInt());
        } catch (IllegalArgumentException ex) {
        }
        switch (sex[row].getSelectedIndex()) {
        case 0:
            mm.setGeschlecht(Geschlecht.weiblich);
            break;
        case 1:
            mm.setGeschlecht(Geschlecht.maennlich);
            break;
        case 2:
        default:
            mm.setGeschlecht(Geschlecht.unbekannt);
            break;
        }
        controller.sendDataUpdateEvent("ChangeTeam", UpdateEventConstants.REASON_SWIMMER_CHANGED, selectedTeam, null, parent);
    }

    public class InputChangedListener implements DocumentListener, ItemListener {

        private final int row;

        public InputChangedListener(int row) {
            this.row = row;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            rowChanged(row);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            rowChanged(row);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            rowChanged(row);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            rowChanged(row);
        }
    }
}