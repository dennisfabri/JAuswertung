/*
 * jTeilnehmerAendernLoeschen.java Created on 17. March 2001, 19:48
 */

package de.df.jauswertung.gui.plugins.editor;

/**
 * @author Dennis Fabri
 * @version
 */
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.tasks.JTaskPaneGroup;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.Qualifikation;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startunterlagen;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.teammembersinput.Length50Validator;
import de.df.jauswertung.gui.util.DisciplinesPanel;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.AltersklassenUtils;
import de.df.jutils.gui.JDoubleField;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;
import de.df.jutils.gui.plaf.GradientTaskPaneGroupUI;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.UIUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.util.StringTools;

class JSchwimmerEditieren<T extends ASchwimmer> extends JDialog {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long                        serialVersionUID    = 4049357534697435955L;

    private static final JWarningTextField.Validator validator50         = new Length50Validator();

    private JIntegerField                            jahrgang            = new JIntegerField(false, true);

    private JIntegerField                            startnummer         = new JIntegerField(true, true);

    private JWarningTextField                        name                = new JWarningTextField(true, true);
    private JWarningTextField                        vorname             = new JWarningTextField(true, true);
    private JWarningTextField                        qualifikationsebene = new JWarningTextField(false, false);
    private JComboBox<String>                        quali               = new JComboBox<String>(new String[] {
            I18n.get("Quali.Open"), I18n.get("Quali.NotQualified"), I18n.get("Quali.Qualified"),
            I18n.get("Quali.Direct"), I18n.get("Quali.Set"), I18n.get("Quali.Disabled"), I18n.get("Nachruecker") });
    private JDoubleField[]                           melde1              = new JDoubleField[] {
            new JDoubleField(), new JDoubleField() };
    private JCheckBox[]                              protokoll1          = new JCheckBox[] {
            new JCheckBox(I18n.get("RegistrationWithProtocol.Short", "A")),
            new JCheckBox(I18n.get("RegistrationWithProtocol.Short", "B")) };
    private JWarningTextField                        bemerkung           = new JWarningTextField(false, false);
    private JComboBox<String>                        geschlecht;

    private JPanel                                   members             = new JPanel();
    private JWarningTextField[]                      membersFirstname    = new JWarningTextField[0];
    private JWarningTextField[]                      membersSurname      = new JWarningTextField[0];
    @SuppressWarnings({ "unchecked", "cast" })
    private JComboBox<String>[]                      membersSex          = (JComboBox<String>[]) new JComboBox[0];
    private JIntegerField[]                          membersYearOfBirth  = new JIntegerField[0];

    private JWarningTextField                        gliederung          = new JWarningTextField(true, true);
    private DisciplinesPanel<T>                      disciplinePanel     = null;
    private JTaskPaneGroup                           disciplineContainer = null;
    private JCheckBox                                ausserkonkurrenz    = new JCheckBox(I18n.get("AusserKonkurrenz"));
    private JComboBox<String>                        ak                  = new JComboBox<String>();

    private JCheckBox                                startpass           = new JCheckBox(
            I18n.get("Startunterlagenkontrolle"));

    private AWettkampf<T>                            wk                  = null;
    private T                                        schwimmer           = null;

    private Window                                   parent              = null;
    private CorePlugin                               core                = null;

    private boolean                                  changed             = false;

    public JSchwimmerEditieren(T swimmer, AWettkampf<T> wettkampf, JFrame parent, boolean delete, CorePlugin core) {
        super(parent, true);
        this.parent = parent;
        init(swimmer, wettkampf, delete, core);
    }

    public JSchwimmerEditieren(T swimmer, AWettkampf<T> wettkampf, JDialog parent, boolean delete, CorePlugin core) {
        super(parent, true);
        this.parent = parent;
        init(swimmer, wettkampf, delete, core);
    }

    private void init(T swimmer, AWettkampf<T> wettkampf, boolean delete, CorePlugin coreplugin) {
        if (swimmer == null) {
            throw new NullPointerException();
        }
        if (wettkampf == null) {
            throw new NullPointerException();
        }

        this.core = coreplugin;
        schwimmer = swimmer;
        wk = wettkampf;

        Regelwerk rw = wk.getRegelwerk();

        enableAutoselect();

        geschlecht = new JComboBox<>(
                new String[] { I18n.geschlechtToString(rw, false), I18n.geschlechtToString(rw, true) });

        disciplinePanel = new DisciplinesPanel<>(wk);
        disciplineContainer = new JTaskPaneGroup();
        disciplineContainer.setUI(new GradientTaskPaneGroupUI());
        disciplineContainer.setTitle(I18n.get("DisciplineSelectionAndTimes"));
        disciplineContainer.add(disciplinePanel);

        startnummer.setInt(schwimmer.getStartnummer());
        if (swimmer instanceof Teilnehmer) {
            Teilnehmer t = (Teilnehmer) swimmer;
            name.setText(t.getNachname());
            vorname.setText(t.getVorname());
            jahrgang.setInt(t.getJahrgang());
        } else {

            Mannschaft m = (Mannschaft) schwimmer;
            name.setText(m.getName());
            membersFirstname = Arrays.copyOf(membersFirstname, swimmer.getMaxMembers());
            membersSurname = Arrays.copyOf(membersSurname, swimmer.getMaxMembers());
            membersSex = Arrays.copyOf(membersSex, swimmer.getMaxMembers());
            membersYearOfBirth = Arrays.copyOf(membersYearOfBirth, swimmer.getMaxMembers());

            Object[][] input = new Object[membersFirstname.length][4];
            for (int x = 0; x < membersFirstname.length; x++) {
                membersFirstname[x] = new JWarningTextField(false, true);
                membersFirstname[x].setValidator(validator50);
                membersFirstname[x].setAutoSelectAll(true);
                membersSurname[x] = new JWarningTextField(false, true);
                membersSurname[x].setValidator(validator50);
                membersSurname[x].setAutoSelectAll(true);
                membersSex[x] = new JComboBox<String>(new String[] { I18n.get("Sex2"), I18n.get("Sex1"), "-" });
                membersYearOfBirth[x] = new JIntegerField(3000, false, true);
                membersYearOfBirth[x].setAutoSelectAll(true);

                Mannschaftsmitglied mm = m.getMannschaftsmitglied(x);
                membersFirstname[x].setText(mm.getVorname());
                membersSurname[x].setText(mm.getNachname());
                switch (mm.getGeschlecht()) {
                case maennlich:
                    membersSex[x].setSelectedIndex(0);
                    break;
                case weiblich:
                    membersSex[x].setSelectedIndex(1);
                    break;
                case unbekannt:
                    membersSex[x].setSelectedIndex(2);
                    break;
                }
                membersYearOfBirth[x].setInt(mm.getJahrgang());

                input[x][0] = membersSurname[x];
                input[x][1] = membersFirstname[x];
                input[x][2] = membersSex[x];
                input[x][3] = membersYearOfBirth[x];
            }

            FormLayoutUtils.createTable(members, null, new String[] { "Nachname", "Vorname", "Geschlecht", "Jahrgang" },
                    input, new int[0], SwingConstants.NEXT, true);
            // members.setText(((Mannschaft) schwimmer).getMitglieder());
            members.setBorder(BorderUtils.createLabeledBorder("Mannschaftsmitglieder"));
        }
        for (int x = 0; x < melde1.length; x++) {
            if (schwimmer.getMeldepunkte(x) > 0.005) {
                melde1[x].setDouble(schwimmer.getMeldepunkte(x));
            }
            protokoll1[x].setSelected(schwimmer.hasMeldungMitProtokoll(x));
        }
        qualifikationsebene.setText(schwimmer.getQualifikationsebene());
        bemerkung.setText(schwimmer.getBemerkung());
        gliederung.setText(schwimmer.getGliederung());

        geschlecht.setSelectedIndex(schwimmer.isMaennlich() ? 1 : 0);
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            Regelwerk r = wk.getRegelwerk();
            ak.addItem(r.getAk(x).toString());
        }
        ak.setSelectedIndex(AltersklassenUtils.getAkNummer(schwimmer.getAK(), wk.getRegelwerk()));
        ak.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                updateDisciplinePanel();
            }
        });
        ausserkonkurrenz.setSelected(schwimmer.isAusserKonkurrenz());

        switch (schwimmer.getQualifikation()) {
        case OFFEN:
            quali.setSelectedIndex(0);
            break;
        case NICHT_QUALIFIZIERT:
            quali.setSelectedIndex(1);
            break;
        case QUALIFIZIERT:
            quali.setSelectedIndex(2);
            break;
        case DIREKT:
            quali.setSelectedIndex(3);
            break;
        case GESETZT:
            quali.setSelectedIndex(4);
            break;
        case GESPERRT:
            quali.setSelectedIndex(5);
            break;
        case NACHRUECKER:
            quali.setSelectedIndex(6);
            break;
        }

        startpass.setSelected(schwimmer.getStartunterlagen() == Startunterlagen.PRUEFEN);

        geschlecht.setSelectedIndex(schwimmer.isMaennlich() ? 1 : 0);

        initComponents(delete);

        disciplinePanel.showAk(schwimmer);

        addActions();
    }

    private void addActions() {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = 3257572818995525944L;

            @Override
            public void actionPerformed(ActionEvent e) {
                beenden();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        Action enterAction = new AbstractAction() {
            private static final long serialVersionUID = 3257572818995525944L;

            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "ENTER");
        getRootPane().getActionMap().put("ENTER", enterAction);
    }

    void beenden() {
        setVisible(false);
        dispose();
    }

    public boolean hasChanged() {
        return changed;
    }

    @Override
    public void setVisible(boolean b) {
        parent.setEnabled(!b);
        super.setVisible(b);
    }

    private void initComponents(boolean delete) {
        boolean einzel = schwimmer instanceof Teilnehmer;
        if (einzel) {
            setTitle(I18n.get("EditSwimmer"));
        } else {
            setTitle(I18n.get("EditTeam"));
        }
        // setResizable(false);
        addWindowListener(new WindowCloseListener());

        // disciplinePanel.setBorder(BorderUtils.createLabeledBorder(I18n
        // .get("DisciplineSelectionAndTimes")));

        SimpleFormBuilder sfb = new SimpleFormBuilder(true);

        sfb.add(I18n.get("Startnumber"), startnummer);
        if (einzel) {
            sfb.add(I18n.get("Surname"), name);
            sfb.add(I18n.get("FirstName"), vorname);
            sfb.add(I18n.get("YearOfBirth"), jahrgang);
        } else {
            sfb.add(I18n.get("Name"), name);
        }
        sfb.add(I18n.get("Comment"), bemerkung);
        sfb.add(I18n.get("Organisation"), gliederung);
        sfb.add(I18n.get("Qualifikationsebene"), qualifikationsebene);
        sfb.add(I18n.get("Qualification"), quali);
        sfb.add(I18n.get("AgeGroup"), ak);
        sfb.add(I18n.get("Sex"), geschlecht);
        sfb.add(I18n.get("Wertung"), ausserkonkurrenz);
        sfb.add(startpass);

        SimpleFormBuilder sfb2 = new SimpleFormBuilder(true, false);
        for (int x = 0; x < melde1.length; x++) {
            sfb2.add(I18n.get("AnouncedPointsNr", StringTools.ABC[x]), melde1[x]);
            sfb2.add(protokoll1[x], true);
        }
        sfb2.addSpanning(disciplineContainer);
        sfb2.addSpanning(members);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(I18n.get("General"), UIUtils.surroundWithScroller(sfb.getPanel(), false, true));
        tabs.addTab(I18n.get("Registration"), UIUtils.surroundWithScroller(sfb2.getPanel(), false, true));

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu");

        JPanel panel = new JPanel(layout);
        panel.setLayout(layout);

        panel.add(tabs, CC.xy(2, 2));
        panel.add(createButtonPanel(delete), CC.xy(2, 4));

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);

        Container px = getContentPane();
        px.add(scroll);

        pack();

        Dimension size = getSize();

        UIStateUtils.uistatemanage(parent, this, "JSchwimmerEditieren");

        setSize(Math.max(size.width, getWidth()), Math.max(size.height, getHeight()));
    }

    private JPanel createButtonPanel(boolean delete) {
        JButton aendern = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        aendern.setToolTipText(I18n.getToolTip("AcceptChanges"));
        aendern.addActionListener(new OkActionListener());

        JButton abbrechen = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        abbrechen.setToolTipText(I18n.getToolTip("CancelChanges"));
        abbrechen.addActionListener(new WindowCloseListener());

        FormLayout layout = new FormLayout("fill:default,4dlu:grow,fill:default,4dlu,fill:default", "fill:default");

        JPanel buttonPanel = new JPanel(layout);
        layout.setColumnGroups(new int[][] { { 1, 3, 5 } });

        if (delete) {
            JButton loeschen = new JButton(I18n.get("Delete"), IconManager.getSmallIcon("delete"));
            if (schwimmer instanceof Teilnehmer) {
                loeschen.setToolTipText(I18n.getToolTip("DeleteSwimmer"));
            } else {
                loeschen.setToolTipText(I18n.getToolTip("DeleteTeam"));
            }
            loeschen.addActionListener(new DeleteActionListener());

            buttonPanel.add(loeschen, CC.xy(1, 1));
        }
        buttonPanel.add(aendern, CC.xy(3, 1));
        buttonPanel.add(abbrechen, CC.xy(5, 1));
        return buttonPanel;
    }

    /**
     * 
     */
    void close() {
        beenden();
    }

    /**
     * 
     */
    void doOk() {
        if (!validateData()) {
            return;
        }
        int sn = 0;
        try {
            sn = Integer.parseInt(startnummer.getText());
        } catch (Exception ex) {
            return;
        }
        if (!wk.switchStartnummer(schwimmer, sn)) {
            DialogUtils.error(this, I18n.get("Error"), I18n.get("Error.StartnumberChangeFailed"),
                    I18n.get("Error.StartnumberChangeFailed.Note"));
            return;
        }
        if (schwimmer instanceof Teilnehmer) {
            Teilnehmer t = (Teilnehmer) schwimmer;
            t.setNachname(name.getText());
            t.setVorname(vorname.getText());
            t.setJahrgang(jahrgang.getInt());
        } else {
            Mannschaft m = ((Mannschaft) schwimmer);
            m.setName(name.getText());
            for (int x = 0; x < m.getMaxMembers(); x++) {
                Mannschaftsmitglied mm = m.getMannschaftsmitglied(x);
                mm.setVorname(membersFirstname[x].getText());
                mm.setNachname(membersSurname[x].getText());
                switch (membersSex[x].getSelectedIndex()) {
                case 0:
                    mm.setGeschlecht(Geschlecht.maennlich);
                    break;
                case 1:
                    mm.setGeschlecht(Geschlecht.weiblich);
                    break;
                default:
                    mm.setGeschlecht(Geschlecht.unbekannt);
                    break;
                }
                mm.setJahrgang(membersYearOfBirth[x].getInt());
            }
            // m.setMitglieder(members.getText());
        }
        schwimmer.setMaennlich(geschlecht.getSelectedIndex() == 1);
        for (int x = 0; x < melde1.length; x++) {
            schwimmer.setMeldepunkte(x, melde1[x].getDouble());
            schwimmer.setMeldungMitProtokoll(x, protokoll1[x].isSelected());
        }
        schwimmer.setBemerkung(bemerkung.getText());
        schwimmer.setGliederung(gliederung.getText());
        schwimmer.setQualifikationsebene(qualifikationsebene.getText());
        schwimmer.setAKNummer(ak.getSelectedIndex(), true);
        schwimmer.setQualifikation(getQualifikation());
        schwimmer.setAusserKonkurrenz(ausserkonkurrenz.isSelected());
        if (schwimmer.getAK().isDisciplineChoiceAllowed()) {
            schwimmer.setDisciplineChoice(disciplinePanel.getSelection());
        }

        Startunterlagen su = Startunterlagen.NICHT_PRUEFEN;
        if (startpass.isSelected()) {
            su = Startunterlagen.PRUEFEN;
        }
        schwimmer.setStartunterlagen(su);

        schwimmer.setMeldezeiten(disciplinePanel.getMeldezeiten());

        changed = true;

        beenden();
    }

    private boolean validateData() {
        try {
            Integer.parseInt(startnummer.getText());
        } catch (Exception ex) {
            return false;
        }
        if (schwimmer instanceof Teilnehmer) {
            int value = jahrgang.getInt();
            if (value < 0 || (value >= 100 && value < 1900)) {
                return false;
            }
        } else {
            Mannschaft m = ((Mannschaft) schwimmer);
            for (int x = 0; x < m.getMaxMembers(); x++) {
                int value = membersYearOfBirth[x].getInt();
                if (value < 0 || (value >= 100 && value < 1900)) {
                    return false;
                }
            }
        }

        return disciplinePanel.validateData();
    }

    private Qualifikation getQualifikation() {
        switch (quali.getSelectedIndex()) {
        default:
        case 0:
            return Qualifikation.OFFEN;
        case 1:
            return Qualifikation.NICHT_QUALIFIZIERT;
        case 2:
            return Qualifikation.QUALIFIZIERT;
        case 3:
            return Qualifikation.DIREKT;
        case 4:
            return Qualifikation.GESETZT;
        case 5:
            return Qualifikation.GESPERRT;
        case 6:
            return Qualifikation.NACHRUECKER;
        }
    }

    /**
     * 
     */
    void delete() {
        EDTUtils.setVisible(this, false);
        if (core.remove(schwimmer)) {
            changed = true;
            beenden();
        } else {
            EDTUtils.setVisible(this, true);
        }
    }

    private void enableAutoselect() {
        name.setAutoSelectAll(true);
        vorname.setAutoSelectAll(true);
        qualifikationsebene.setAutoSelectAll(true);
        // members.setAutoSelectAll(true);
        bemerkung.setAutoSelectAll(true);
        gliederung.setAutoSelectAll(true);
        startnummer.setAutoSelectAll(true);
        for (int x = 0; x < melde1.length; x++) {
            melde1[x].setAutoSelectAll(true);
        }
    }

    void updateDisciplinePanel() {
        disciplinePanel.showAk(Math.max(0, ak.getSelectedIndex()));
    }

    final class WindowCloseListener extends WindowAdapter implements ActionListener {

        @Override
        public void windowClosing(WindowEvent evt) {
            close();
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            windowClosing(null);
        }
    }

    final class DeleteActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            delete();
        }
    }

    final class OkActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            doOk();
        }
    }
}