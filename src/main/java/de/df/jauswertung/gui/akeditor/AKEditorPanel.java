/*
 * Created on 13.11.2003
 */
package de.df.jauswertung.gui.akeditor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.JIntSpinner;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;

/**
 * @author Dennis Fabri
 */
class AKEditorPanel extends JPanel {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID      = 3762813770862309687L;

    private JWarningTextField name;
    private JCheckBox         zw;
    private JComboBox<String> gesamt;
    private JCheckBox         choiceAllowed;
    private JCheckBox         mehrkampf;
    private JCheckBox         einzelwertung;
    private JCheckBox         einzelwertungZw;
    private JCheckBox         strafeIstDisqualifikation;
    private JComboBox<String> laufsortierung;
    private JCheckBox         laufrotation;
    private JIntSpinner       mindisz;
    private JIntSpinner       useddisz;
    private JIntSpinner       maxdisz;

    private JComboBox<String> resultgroup;
    private JComboBox<String> startgroup;

    JIntSpinner               minMembers;
    JIntSpinner               maxMembers;

    JIntegerField             minAlter;
    JIntegerField             maxAlter;
    JIntegerField             minAlterInSumme;
    JIntegerField             maxAlterInSumme;

    private DisziplinenPanel  disziplinen           = null;
    private JPanel            disziplinenLabelPanel = new JPanel();
    private JPanel            allgemein             = new JPanel();
    AKsEditorPanel            parent                = null;
    private Altersklasse      ak                    = null;

    private boolean           einzel;
    private int               index;

    public AKEditorPanel(AKsEditorPanel parent, Altersklasse newAk, boolean einzel, int index, String[] start, String[] result) {
        this.parent = parent;
        this.ak = newAk;
        this.einzel = einzel;
        this.index = index;

        prepareGUI(start, result);
        initLayout();
        initGUI();
    }

    public int getIndex() {
        return index;
    }

    public String getAKName() {
        return name.getText();
    }

    public void notifyChange() {
        parent.notifyChange();
    }

    public boolean getGeaendert(Altersklasse altersklasse) {
        if (!altersklasse.getName().equals(name.getText())) {
            return true;
        }
        if (altersklasse.hasHLW() != zw.isSelected()) {
            return true;
        }
        int pos = gesamt.getSelectedIndex();
        boolean gw = (pos % 2 != 0);
        boolean gm = (pos > 1);
        if (altersklasse.getGesamtwertung(false) != gw) {
            return true;
        }
        Disziplin[] diszs = disziplinen.getDisziplinen(true);
        if (diszs.length != altersklasse.getDiszAnzahl()) {
            return true;
        }
        for (int x = 0; x < diszs.length; x++) {
            if (!diszs[x].equals(altersklasse.getDisziplin(x, false))) {
                return true;
            }
        }

        if (altersklasse.getGesamtwertung(true) != gm) {
            return true;
        }
        if (altersklasse.getLaufsortierung() != laufsortierung.getSelectedIndex()) {
            return true;
        }
        if (altersklasse.getLaufrotation() != laufrotation.isSelected()) {
            return true;
        }
        diszs = disziplinen.getDisziplinen(false);
        if (diszs.length != altersklasse.getDiszAnzahl()) {
            return true;
        }
        for (int x = 0; x < diszs.length; x++) {
            if (!diszs[x].equals(altersklasse.getDisziplin(x, true))) {
                return true;
            }
        }
        if (choiceAllowed.isSelected() != ak.isDisciplineChoiceAllowed()) {
            return true;
        }
        if (choiceAllowed.isSelected()) {
            if (mindisz.getInt() != ak.getMinimalChosenDisciplines()) {
                return true;
            }
            if (useddisz.getInt() != ak.getUsedDisciplines()) {
                return true;
            }
            if (maxdisz.getInt() != ak.getMaximalChosenDisciplines()) {
                return true;
            }
        }
        if (mehrkampf.isSelected() != ak.hasMehrkampfwertung()) {
            return true;
        }
        if (einzelwertung.isSelected() != ak.hasEinzelwertung()) {
            return true;
        }
        if (strafeIstDisqualifikation.isSelected() != ak.isStrafeIstDisqualifikation()) {
            return true;
        }
        if (einzelwertung.isSelected()) {
            if (einzelwertungZw.isSelected() != ak.isEinzelwertungHlw()) {
                return true;
            }
        }
        if (startgroup.getSelectedIndex() != 0) {
            if (ak.getStartgruppe() == null) {
                return true;
            }
            if (!ak.getStartgruppe().equals(startgroup.getSelectedItem())) {
                return true;
            }
        } else {
            if (ak.getStartgruppe() != null) {
                return true;
            }
        }
        if (resultgroup.getSelectedIndex() != 0) {
            if (ak.getWertungsgruppe() == null) {
                return true;
            }
            if (!ak.getWertungsgruppe().equals(resultgroup.getSelectedItem())) {
                return true;
            }
        } else {
            if (ak.getWertungsgruppe() != null) {
                return true;
            }
        }
        return false;
    }

    private void initLayout() {
        setLayout(new BorderLayout(5, 5));
        add(allgemein, BorderLayout.NORTH);
        add(disziplinenLabelPanel, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(disziplinen);
        scroll.setBorder(null);
        disziplinenLabelPanel.add(scroll);

        allgemein.setBorder(BorderUtils.createLabeledBorder(I18n.get("GeneralSettings")));
    }

    private void prepareGUI(String[] start, String[] result) {
        name = new JWarningTextField();
        zw = new JCheckBox();
        gesamt = new JComboBox<String>();
        choiceAllowed = new JCheckBox();
        mehrkampf = new JCheckBox();
        einzelwertung = new JCheckBox();
        einzelwertungZw = new JCheckBox();
        strafeIstDisqualifikation = new JCheckBox();
        resultgroup = new JComboBox<String>();
        startgroup = new JComboBox<String>();

        mehrkampf.setSelected(ak.hasMehrkampfwertung());
        einzelwertung.setSelected(ak.hasEinzelwertung());
        einzelwertungZw.setSelected(ak.isEinzelwertungHlw());
        einzelwertungZw.setEnabled(ak.hasEinzelwertung() && ak.hasHLW());

        strafeIstDisqualifikation.setSelected(ak.isStrafeIstDisqualifikation());

        choiceAllowed.setSelected(ak.isDisciplineChoiceAllowed());
        mindisz = new JIntSpinner(ak.getMinimalChosenDisciplines(), 1, ak.getDiszAnzahl(), 1);
        useddisz = new JIntSpinner(ak.getUsedDisciplines(), 1, ak.getDiszAnzahl(), 1);
        maxdisz = new JIntSpinner(ak.getMaximalChosenDisciplines(), 1, ak.getDiszAnzahl(), 1);
        mindisz.setEnabled(ak.isDisciplineChoiceAllowed());
        useddisz.setEnabled(ak.isDisciplineChoiceAllowed());
        maxdisz.setEnabled(ak.isDisciplineChoiceAllowed());

        minMembers = new JIntSpinner(ak.getMinMembers(), 1, 32, 1);
        maxMembers = new JIntSpinner(ak.getMaxMembers(), 1, 32, 1);

        choiceAllowed.setSelected(ak.isDisciplineChoiceAllowed());

        disziplinen = new DisziplinenPanel(this, ak);
        disziplinenLabelPanel.setBorder(BorderUtils.createLabeledBorder(I18n.get("Disciplines")));
        disziplinenLabelPanel.setLayout(new BorderLayout());

        gesamt.addItem(I18n.get("No"));
        // gesamt.addItem(I18n.geschlechtToStringSubject(false));
        // gesamt.addItem(I18n.geschlechtToStringSubject(true));
        gesamt.addItem(I18n.get("Sex1"));
        gesamt.addItem(I18n.get("Sex2"));
        gesamt.addItem(I18n.get("Both"));

        name.setAutoSelectAll(true);
        name.setText(ak.toString());
        zw.setSelected(ak.hasHLW());
        int pos = (ak.getGesamtwertung(true) ? 1 : 0) + (ak.getGesamtwertung(false) ? 2 : 0);
        gesamt.setSelectedIndex(pos);

        laufsortierung = new JComboBox<String>(
                new String[] { I18n.get("Randomly"), I18n.get("SameOrganisationSameHeat"), I18n.get("SameOrganisationDifferentHeats"),
                        I18n.get("SortByAnouncedPoints"), I18n.get("SortByAnouncedTimes"), I18n.get("RandomlyPerDiscipline"), I18n.get("SortByILS") });
        laufsortierung.setSelectedIndex(Math.max(0, ak.getLaufsortierung()));

        laufrotation = new JCheckBox();
        laufrotation.setSelected(ak.getLaufrotation());

        minAlter = new JIntegerField(100, false, false);
        maxAlter = new JIntegerField(100, false, true);
        if (ak.getMinimumAlter() > 0) {
            minAlter.setInt(ak.getMinimumAlter());
        }
        if (ak.getMaximumAlter() > 0) {
            maxAlter.setInt(ak.getMaximumAlter());
        }
        minAlterInSumme = new JIntegerField(1000, false, false);
        maxAlterInSumme = new JIntegerField(1000, false, true);
        if (ak.getMinimumAlterInSumme() > 0) {
            minAlterInSumme.setInt(ak.getMinimumAlterInSumme());
        }
        if (ak.getMaximumAlterInSumme() > 0) {
            maxAlterInSumme.setInt(ak.getMaximumAlterInSumme());
        }

        minAlter.setValidator(new JIntegerField.Validator() {
            @Override
            public boolean validate(int value) {
                return (maxAlter.getInt() <= 0) || (value <= 0) || maxAlter.getInt() >= value;
            }
        });
        maxAlter.setValidator(new JIntegerField.Validator() {
            @Override
            public boolean validate(int value) {
                if ((minAlter.getInt() <= 0) || (value <= 0)) {
                    return true;
                }
                return minAlter.getInt() <= value;
            }
        });
        minAlterInSumme.setValidator(new JIntegerField.Validator() {
            @Override
            public boolean validate(int value) {
                return (maxAlterInSumme.getInt() <= 0) || (value <= 0) || maxAlterInSumme.getInt() >= value;
            }
        });
        maxAlterInSumme.setValidator(new JIntegerField.Validator() {
            @Override
            public boolean validate(int value) {
                if ((minAlterInSumme.getInt() <= 0) || (value <= 0)) {
                    return true;
                }
                return minAlterInSumme.getInt() <= value;
            }
        });

        name.setToolTipText(I18n.getToolTip("NameOfAgeGroup"));
        zw.setToolTipText(I18n.getToolTip("TeilnahmeAnZW"));
        gesamt.setToolTipText(I18n.getToolTip("TeilnahmeAnGesamtwertung"));
        choiceAllowed.setToolTipText(I18n.getToolTip("DisziplinenauswahlErlauben"));
        mindisz.setToolTipText(I18n.getToolTip("MinimaleAnzahlDisziplinen"));
        useddisz.setToolTipText(I18n.getToolTip("BenutzteAnzahlDisziplinen"));
        maxdisz.setToolTipText(I18n.getToolTip("MaximaleAnzahlDisziplinen"));
        mehrkampf.setToolTipText(I18n.getToolTip("Mehrkampf"));
        einzelwertung.setToolTipText(I18n.getToolTip("WertungDerEinzeldisziplinen"));
        einzelwertungZw.setToolTipText(I18n.getToolTip("WertungDerEinzeldisziplinenErfordertZW"));
        strafeIstDisqualifikation.setToolTipText(I18n.getToolTip("StrafeIstDisqualifikation"));
        laufsortierung.setToolTipText(I18n.getToolTip("Laufsortierung"));
        laufrotation.setToolTipText(I18n.getToolTip("Laufrotation"));
        minAlter.setToolTipText(I18n.getToolTip("MinimalAgeInAgeGroup"));
        maxAlter.setToolTipText(I18n.getToolTip("MaximalAgeInAgeGroup"));
        minAlterInSumme.setToolTipText(I18n.getToolTip("MinimalAgeInAgeGroupSum"));
        maxAlterInSumme.setToolTipText(I18n.getToolTip("MaximalAgeInAgeGroupSum"));

        minMembers.setToolTipText(I18n.getToolTip("MinimalMembers"));
        maxMembers.setToolTipText(I18n.getToolTip("MaximalMembers"));

        updateStartgroups(start);
        updateResultgroups(result);

        if (ak.getStartgruppe() != null) {
            startgroup.setSelectedItem(ak.getStartgruppe());
        } else {
            startgroup.setSelectedIndex(0);
        }
        if (ak.getWertungsgruppe() != null) {
            resultgroup.setSelectedItem(ak.getWertungsgruppe());
        } else {
            resultgroup.setSelectedIndex(0);
        }

        updateLaufrotationUndSortierung();
    }

    private void initGUI() {
        choiceAllowed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                updateDiszChoice();
                notifyChange();
            }
        });
        mindisz.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                updateMinDiszChoice();
                notifyChange();
            }
        });
        useddisz.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                updateUsedDiszChoice();
                notifyChange();
            }
        });
        maxdisz.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                updateMaxDiszChoice();
                notifyChange();
            }
        });
        minMembers.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                if (maxMembers.getInt() < minMembers.getInt()) {
                    maxMembers.setInt(minMembers.getInt());
                }
                notifyChange();
            }
        });
        maxMembers.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                if (maxMembers.getInt() < minMembers.getInt()) {
                    minMembers.setInt(maxMembers.getInt());
                }
                notifyChange();
            }
        });

        zw.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateEinzelwertung();
                notifyChange();
            }
        });

        laufrotation.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                notifyChange();
            }
        });

        mehrkampf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // updateEinzelwertung();
                notifyChange();
            }
        });

        einzelwertung.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEinzelwertung();
                notifyChange();
            }
        });

        strafeIstDisqualifikation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notifyChange();
            }
        });

        einzelwertungZw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notifyChange();
            }
        });
        gesamt.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                notifyChange();
            }
        });

        name.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                parent.nameChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                parent.nameChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                parent.nameChanged();
            }
        });
        laufsortierung.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                updateLaufrotation();
                notifyChange();
            }
        });
        startgroup.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                updateLaufrotationUndSortierung();
                notifyChange();
            }
        });
        resultgroup.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                notifyChange();
            }
        });

        minAlter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkAge();
                notifyChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkAge();
                notifyChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkAge();
                notifyChange();
            }
        });

        maxAlter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkAge();
                notifyChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkAge();
                notifyChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkAge();
                notifyChange();
            }
        });

        minAlterInSumme.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkAgeInSum();
                notifyChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkAgeInSum();
                notifyChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkAgeInSum();
                notifyChange();
            }
        });

        maxAlterInSumme.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkAgeInSum();
                notifyChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkAgeInSum();
                notifyChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkAgeInSum();
                notifyChange();
            }
        });

        SimpleFormBuilder left = new SimpleFormBuilder(false);

        left.addSeparator(I18n.get("Wertung"));
        left.add(I18n.get("AdditionalPoints"), zw);
        left.add(I18n.get("PenaltyIsDisqualification"), strafeIstDisqualifikation);
        left.add(I18n.get("Mehrkampf"), mehrkampf);
        left.add(I18n.get("Einzelwertung"), einzelwertung);
        left.add(I18n.get("EinzelwertungZW"), einzelwertungZw);
        left.add(I18n.get("GroupEvaluation"), gesamt);
        left.add(I18n.get("Resultgroup"), resultgroup);

        left.addSeparator(I18n.get("AgeBounds"));
        left.add(I18n.get("MinimumAge"), minAlter);
        left.add(I18n.get("MaximumAge"), maxAlter);
        if (!einzel) {
            left.add(I18n.get("MinimumAgeSum"), minAlterInSumme);
            left.add(I18n.get("MaximumAgeSum"), maxAlterInSumme);
        }

        SimpleFormBuilder right = new SimpleFormBuilder(false);

        right.addSeparator(I18n.get("DisciplineChoice"));
        right.add(I18n.get("AllowChoiceOfDisciplines"), choiceAllowed);
        right.add(I18n.get("MinimalDisciplines"), mindisz);
        right.add(I18n.get("UsedDisciplines"), useddisz);
        right.add(I18n.get("MaximalDisciplines"), maxdisz);

        right.addSeparator(I18n.get("Heatarrangement"));
        right.add(I18n.get("Startgroup"), startgroup);
        right.add(I18n.get("SortingOfSwimmers"), laufsortierung);
        right.add(I18n.get("RotateLanes"), laufrotation);

        if (!einzel) {
            right.addSeparator(I18n.get("Teammembers"));
            right.add(I18n.get("MinimumAmount"), minMembers);
            right.add(I18n.get("MaximumAmount"), maxMembers);
        }

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,10dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setColumnGroups(new int[][] { { 2, 4 } });

        allgemein.setLayout(layout);

        SimpleFormBuilder sfb = new SimpleFormBuilder(false);
        sfb.add(I18n.get("Name"), name);

        allgemein.add(sfb.getPanel(), CC.xyw(2, 2, 3, "fill,fill"));
        allgemein.add(left.getPanel(), CC.xy(2, 4));
        allgemein.add(right.getPanel(), CC.xy(4, 4));
    }

    boolean checkAge() {
        if ((minAlter.getInt() > 0) && (maxAlter.getInt() > 0)) {
            if (minAlter.getInt() > maxAlter.getInt()) {
                return false;
            }
        }
        return true;
    }

    boolean checkAgeInSum() {
        if ((minAlterInSumme.getInt() > 0) && (maxAlterInSumme.getInt() > 0)) {
            if (minAlterInSumme.getInt() > maxAlterInSumme.getInt()) {
                return false;
            }
        }
        return true;
    }

    public Altersklasse getAK() {
        Altersklasse nak = new Altersklasse();
        nak.setName(name.getText());
        nak.setHLW(zw.isSelected());
        nak.setGesamtwertung(true, ((gesamt.getSelectedIndex() % 2)) > 0);
        nak.setGesamtwertung(false, (gesamt.getSelectedIndex() > 1));
        Disziplin[] disciplines = this.disziplinen.getDisziplinen(false);
        nak.setDiszAnzahl(disciplines.length);
        for (int x = 0; x < disciplines.length; x++) {
            nak.setDisziplin(disciplines[x], x, false);
        }
        disciplines = this.disziplinen.getDisziplinen(true);
        for (int x = 0; x < disciplines.length; x++) {
            nak.setDisziplin(disciplines[x], x, true);
        }
        nak.setDisciplineChoiceAllowed(choiceAllowed.isSelected());
        if (choiceAllowed.isSelected()) {
            nak.setChosenDisciplines(mindisz.getInt(), useddisz.getInt(), maxdisz.getInt());
        } else {
            nak.setChosenDisciplines(disciplines.length, disciplines.length, disciplines.length);
        }
        nak.setMehrkampfwertung(mehrkampf.isSelected());
        nak.setEinzelwertung(einzelwertung.isSelected());
        nak.setEinzelwertungHlw(einzelwertungZw.isSelected());
        nak.setStrafeIstDisqualifikation(strafeIstDisqualifikation.isSelected());
        nak.setLaufsortierung(laufsortierung.getSelectedIndex());
        nak.setLaufrotation(laufrotation.isSelected());

        nak.setMaximumAlter(maxAlter.getInt());
        nak.setMinimumAlter(minAlter.getInt());
        nak.setMaximumAlterInSumme(maxAlterInSumme.getInt());
        nak.setMinimumAlterInSumme(minAlterInSumme.getInt());

        nak.setMemberCounts(minMembers.getInt(), maxMembers.getInt());

        if (resultgroup.getSelectedIndex() > 0) {
            nak.setWertungsgruppe((String) resultgroup.getSelectedItem());
        } else {
            nak.setWertungsgruppe(null);
        }
        if (startgroup.getSelectedIndex() > 0) {
            nak.setStartgruppe((String) startgroup.getSelectedItem());
        } else {
            nak.setStartgruppe(null);
        }

        return nak;
    }

    void updateDiszChoice() {
        boolean b = choiceAllowed.isSelected();
        mindisz.setEnabled(b);
        useddisz.setEnabled(b);
        maxdisz.setEnabled(b);
    }

    void updateMinDiszChoice() {
        int i = mindisz.getInt();
        if (i > useddisz.getInt()) {
            useddisz.setInt(i);
        }
    }

    void updateUsedDiszChoice() {
        int i = useddisz.getInt();
        if (i > maxdisz.getInt()) {
            maxdisz.setInt(i);
        }
        if (i < mindisz.getInt()) {
            mindisz.setInt(i);
        }
    }

    void updateMaxDiszChoice() {
        int i = maxdisz.getInt();
        if (i < useddisz.getInt()) {
            useddisz.setInt(i);
        }
    }

    void setDisciplineCount(int count) {
        mindisz.setMaximum(count);
        useddisz.setMaximum(count);
        maxdisz.setMaximum(count);
    }

    void updateEinzelwertung() {
        einzelwertungZw.setEnabled(einzelwertung.isSelected() && zw.isSelected());
    }

    void updateLaufrotationUndSortierung() {
        boolean b = startgroup.getSelectedIndex() <= 0;
        if (b != laufsortierung.isEnabled()) {
            laufsortierung.setEnabled(b);
        }
        updateLaufrotation();
    }

    void updateLaufrotation() {
        boolean b = laufsortierung.getSelectedIndex() != Laufliste.REIHENFOLGE_MELDEZEITEN;
        b = b && startgroup.getSelectedIndex() <= 0;
        if (b != laufrotation.isEnabled()) {
            laufrotation.setEnabled(b);
        }
    }

    public void updateStartgroups(String[] startgroups) {
        String selected = (String) startgroup.getSelectedItem();
        String[] items = new String[startgroups.length + 1];
        items[0] = I18n.get("Item.NoStartgroup");
        for (int x = 0; x < startgroups.length; x++) {
            items[x + 1] = startgroups[x];
        }
        startgroup.setModel(new DefaultComboBoxModel<String>(items));
        try {
            startgroup.setSelectedItem(selected);
        } catch (Exception e) {
            startgroup.setSelectedIndex(0);
        }
        startgroup.setEnabled(items.length > 1);
    }

    public void updateResultgroups(String[] resultgroups) {
        String selected = (String) resultgroup.getSelectedItem();
        String[] items = new String[resultgroups.length + 1];
        items[0] = I18n.get("Item.NoResultgroup");
        for (int x = 0; x < resultgroups.length; x++) {
            items[x + 1] = resultgroups[x];
        }
        resultgroup.setModel(new DefaultComboBoxModel<String>(items));
        try {
            resultgroup.setSelectedItem(selected);
        } catch (Exception e) {
            resultgroup.setSelectedIndex(0);
        }
        resultgroup.setEnabled(items.length > 1);
    }
}