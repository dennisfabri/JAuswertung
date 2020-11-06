/*
 * Created on 28.03.2004
 */
package de.df.jauswertung.gui.plugins.registration;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_AKS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_GLIEDERUNG_CHANGED;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.util.ComboBoxUtil;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.tasks.JTaskPaneGroup;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.DisciplinesPanel;
import de.df.jauswertung.gui.util.DisciplinesPanel.DataChangeListener;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.JDoubleField;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.autocomplete.JCompletingComboBox;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.plaf.GradientTaskPaneGroupUI;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri
 * @date 28.03.2004
 */
public class PRegistrationInternalEinzelPlugin extends AFeature {

    private static final String MALE        = I18n.get("male");
    private static final String FEMALE      = I18n.get("female");
    private static final String ADD         = I18n.get("add");
    private static final String WRONG_INPUT = I18n.get("WrongInput");

    private CorePlugin          core        = null;

    private JPanel              panel;
    private JIntegerField       startnummer;
    private JWarningTextField   name;
    private JWarningTextField   vorname;
    private JIntegerField       jahrgang;
    private JDoubleField        melde;
    private JWarningTextField   bemerkung;
    private JComboBox<String>   gliederung;
    private JWarningTextField   qualiebene;
    private JComboBox<String>   altersklasse;
    private JComboBox<String>   geschlecht;
    private JComboBox<String>   ausserkonkurrenz;
    @SuppressWarnings("rawtypes")
    private DisciplinesPanel    disciplines;
    private JTaskPaneGroup      jt;

    JButton                     hinzu;

    private UpdateListener      ul          = new UpdateListener();

    /**
     * This method initializes
     */
    public PRegistrationInternalEinzelPlugin() {
        super();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        startnummer = new JIntegerField();
        name = new JWarningTextField(true, false);
        vorname = new JWarningTextField(true, false);
        jahrgang = new JIntegerField();
        melde = new JDoubleField();
        bemerkung = new JWarningTextField();
        gliederung = new JCompletingComboBox<String>(true);
        qualiebene = new JWarningTextField();
        altersklasse = new JComboBox<String>();
        geschlecht = new JComboBox<String>(new String[] { FEMALE, MALE });
        ausserkonkurrenz = new JComboBox<String>(new String[] { I18n.get("Normal"), I18n.get("AusserKonkurrenz") });
        ausserkonkurrenz.setSelectedIndex(0);
        hinzu = new JButton(ADD, IconManager.getSmallIcon("add"));

        startnummer.getDocument().addDocumentListener(ul);
        startnummer.addActionListener(ul);
        name.getDocument().addDocumentListener(ul);
        name.addActionListener(ul);
        vorname.getDocument().addDocumentListener(ul);
        vorname.addActionListener(ul);
        melde.getDocument().addDocumentListener(ul);
        melde.addActionListener(ul);

        startnummer.setAutoSelectAll(true);
        name.setAutoSelectAll(true);
        vorname.setAutoSelectAll(true);
        jahrgang.setAutoSelectAll(true);
        melde.setAutoSelectAll(true);
        bemerkung.setAutoSelectAll(true);
        qualiebene.setAutoSelectAll(true);

        jahrgang.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateAgeGroup();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
        });

        altersklasse.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                updateDisciplines();
            }
        });

        Component c = ComboBoxUtil.getEditorComponent(gliederung);
        if (c instanceof JTextField) {
            JTextField jtf = (JTextField) c;
            jtf.getDocument().addDocumentListener(ul);
        } else {
            gliederung.addActionListener(ul);
            gliederung.addItemListener(ul);
        }

        startnummer.addKeyListener(new EnterListener(name));
        name.addKeyListener(new EnterListener(vorname));
        vorname.addKeyListener(new EnterListener(jahrgang));
        jahrgang.addKeyListener(new EnterListener(melde));
        melde.addKeyListener(new EnterListener(bemerkung));
        bemerkung.addKeyListener(new EnterListener(gliederung));
        gliederung.addKeyListener(new EnterListener(qualiebene));
        qualiebene.addKeyListener(new EnterListener(altersklasse));
        altersklasse.addKeyListener(new EnterListener(geschlecht));
        geschlecht.addKeyListener(new EnterListener(ausserkonkurrenz));
        ausserkonkurrenz.addKeyListener(new EnterListener(hinzu));

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu," + "fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(13));
        layout.setRowGroups(new int[][] { { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22 } });
        panel = new JPanel(layout);
        panel.setName("einzel");

        panel.add(new JLabel(I18n.get("Startnumber")), CC.xy(2, 2));
        panel.add(new JLabel(I18n.get("FamilyName")), CC.xy(2, 4));
        panel.add(new JLabel(I18n.get("FirstName")), CC.xy(2, 6));
        panel.add(new JLabel(I18n.get("YearOfBirth")), CC.xy(2, 8));
        panel.add(new JLabel(I18n.get("ReportedPoints")), CC.xy(2, 10));
        panel.add(new JLabel(I18n.get("Comment")), CC.xy(2, 12));
        panel.add(new JLabel(I18n.get("Organisation")), CC.xy(2, 14));
        panel.add(new JLabel(I18n.get("Qualifikationsebene")), CC.xy(2, 16));
        panel.add(new JLabel(I18n.get("AgeGroup")), CC.xy(2, 18));
        panel.add(new JLabel(I18n.get("Sex")), CC.xy(2, 20));
        panel.add(new JLabel(I18n.get("AusserKonkurrenz")), CC.xy(2, 22));

        panel.add(startnummer, CC.xy(4, 2));
        panel.add(name, CC.xy(4, 4));
        panel.add(vorname, CC.xy(4, 6));
        panel.add(jahrgang, CC.xy(4, 8));
        panel.add(melde, CC.xy(4, 10));
        panel.add(bemerkung, CC.xy(4, 12));
        panel.add(gliederung, CC.xy(4, 14));
        panel.add(qualiebene, CC.xy(4, 16));
        panel.add(altersklasse, CC.xy(4, 18));
        panel.add(geschlecht, CC.xy(4, 20));
        panel.add(ausserkonkurrenz, CC.xy(4, 22));

        panel.add(hinzu, CC.xywh(2, 26, 3, 1, "right,default"));

        hinzu.addActionListener(new NewActionListener());
        gliederung.setEditable(true);
        geschlecht.setSelectedIndex(0);
    }

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);
        if (c == null) {
            throw new NullPointerException();
        }
        core = (CorePlugin) c.getFeature("de.df.jauswertung.core", uid);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void dataUpdated(UpdateEvent due) {
        if (panel == null) {
            return;
        }
        if ((due.getChangeReason() & REASON_AKS_CHANGED) > 0) {
            if (disciplines != null) {
                panel.remove(jt);
            }
            disciplines = new DisciplinesPanel(core.getWettkampf(), true);
            jt = new JTaskPaneGroup();
            jt.setUI(new GradientTaskPaneGroupUI());
            jt.setTitle(I18n.get("DisciplineSelectionAndTimes"));
            jt.add(disciplines);
            panel.add(jt, CC.xyw(2, 24, 3, "fill,fill"));

            disciplines.addChangeListener(ul);

            int index = Math.max(altersklasse.getSelectedIndex(), 0);
            altersklasse.removeAllItems();
            Regelwerk aks = core.getWettkampf().getRegelwerk();
            for (int x = 0; x < aks.size(); x++) {
                altersklasse.addItem(aks.getAk(x).toString());
            }
            if (index < altersklasse.getItemCount()) {
                altersklasse.setSelectedIndex(index);
            } else {
                altersklasse.setSelectedIndex(0);
            }
        }
        if ((due.getChangeReason() & REASON_GLIEDERUNG_CHANGED) > 0) {
            String g = "";
            if (gliederung.getSelectedIndex() > -1) {
                g = gliederung.getSelectedItem().toString();
            }
            gliederung.removeAllItems();
            ListIterator li = core.getWettkampf().getGliederungen().listIterator();
            while (li.hasNext()) {
                gliederung.addItem(li.next().toString());
            }
            if (gliederung.getItemCount() > 0) {
                gliederung.setSelectedIndex(0);
                for (int x = 0; x < gliederung.getItemCount(); x++) {
                    if (gliederung.getItemAt(x).equals(g)) {
                        gliederung.setSelectedIndex(x);
                    }
                }
            }
        }

        viewNextSN();
        updateButtons();
    }

    /**
     * 
     */
    private void viewNextSN() {
        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();
        if (wk != null) {
            if (startnummer.getText().equals("")) {
                startnummer.setInt(wk.viewNextStartnummer());
            } else {
                int sn = startnummer.getInt();
                if (!wk.isStartnummerFree(sn)) {
                    startnummer.setInt(wk.viewNextStartnummer());
                }
            }
        }
    }

    void updateButtons() {
        boolean result = startnummer.isValidInt() && (name.getText().length() > 0) && (vorname.getText().length() > 0) && melde.isValidDouble();
        if (disciplines != null) {
            result = result && disciplines.isInputValid();
        }
        if (result) {
            Component c = ComboBoxUtil.getEditorComponent(gliederung);
            if (c instanceof JTextField) {
                JTextField jtf = (JTextField) c;
                result = jtf.getText().length() > 0;
            } else {
                result = (gliederung.getSelectedItem() != null) && (gliederung.getSelectedItem().toString().length() > 0);
            }
        }

        hinzu.setEnabled(result);
    }

    final class NewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            add();
        }
    }

    public JPanel getPanel() {
        if (panel == null) {
            initialize();
            dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED);
        }
        return panel;
    }

    /**
     * 
     */
    void add() {
        EinzelWettkampf ewk = core.getEinzelWettkampf();

        int sn = startnummer.getInt();
        String nachname = name.getText();
        String vname = vorname.getText();
        String g = "";
        if (gliederung.getSelectedItem() != null) {
            g = gliederung.getSelectedItem().toString();
        }
        int jahr = jahrgang.getInt();

        if (sn > 0) {
            if (SearchUtils.getSchwimmer(ewk, sn) != null) {
                DialogUtils.inform(null, WRONG_INPUT, I18n.get("StartnummerAlreadyAssigned"), I18n.get("StartnummerAlreadyAssigned.Note"));
                startnummer.requestFocus();
                return;
            }
        }
        if (nachname.length() == 0) {
            DialogUtils.inform(null, WRONG_INPUT, I18n.get("FamilyNameMustNotBeEmpty"), I18n.get("FamilyNameMustNotBeEmpty.Note"));
            name.requestFocus();
            return;
        }
        if (vname.length() == 0) {
            DialogUtils.inform(null, WRONG_INPUT, I18n.get("FirstNameMustNotBeEmpty"), I18n.get("FirstNameMustNotBeEmpty.Note"));
            vorname.requestFocus();
            return;
        }
        if (jahr > 0) {
            if (jahr > Calendar.getInstance().get(Calendar.YEAR)) {
                jahrgang.requestFocus();
                DialogUtils.inform(null, WRONG_INPUT, I18n.get("YearOfBirthTooHigh"), I18n.get("YearOfBirthTooHigh.Note"));
                return;
            }
        }
        if (g.length() == 0) {
            DialogUtils.inform(null, WRONG_INPUT, I18n.get("OrganisationMustNotBeEmpty"), I18n.get("OrganisationMustNotBeEmpty.Note"));
            gliederung.requestFocus();
            return;
        }

        core.addTeilnehmer(name.getText(), vorname.getText(), jahrgang.getInt(), gliederung.getSelectedItem().toString(), qualiebene.getText(),
                geschlecht.getSelectedIndex() == 1, altersklasse.getSelectedIndex(), bemerkung.getText(), startnummer.getInt(), melde.getDouble(),
                disciplines.getSelection(), disciplines.getMeldezeiten(), ausserkonkurrenz.getSelectedIndex() == 1);

        name.setText("");
        vorname.setText("");
        jahrgang.setInt(JIntegerField.EMPTY_FIELD);
        bemerkung.setText("");
        // qualiebene.setText("");
        startnummer.setInt(JIntegerField.EMPTY_FIELD);
        melde.setDouble(JDoubleField.EMPTY_FIELD);
        disciplines.reset();

        startnummer.setInt(core.getEinzelWettkampf().viewNextStartnummer());
        startnummer.requestFocus();
    }

    final class UpdateListener implements ActionListener, DocumentListener, ItemListener, DataChangeListener {

        @Override
        public void dataChanged() {
            updateButtons();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            updateButtons();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateButtons();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateButtons();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateButtons();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            updateButtons();
        }
    }

    void updateDisciplines() {
        disciplines.showAk(Math.max(0, altersklasse.getSelectedIndex()));
    }

    void updateAgeGroup() {
        if (jahrgang.getText().length() == 0) {
            return;
        }
        int jahr = SchwimmerUtils.ermittleJahrgang(jahrgang.getInt());
        int base = core.getWettkampf().getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION, Calendar.getInstance().get(Calendar.YEAR));
        int alter = base - jahr;
        Regelwerk aks = core.getWettkampf().getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            if (aks.getAk(x).fitsAge(alter)) {
                altersklasse.setSelectedIndex(x);
                break;
            }
        }
    }

    private class EnterListener extends KeyAdapter {

        private final JComponent next;

        public EnterListener(JComponent x) {
            next = x;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK) {
                    if (hinzu.isEnabled()) {
                        add();
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                } else if (e.getModifiersEx() == 0) {
                    next.requestFocus();
                }
            }
        }
    }
}