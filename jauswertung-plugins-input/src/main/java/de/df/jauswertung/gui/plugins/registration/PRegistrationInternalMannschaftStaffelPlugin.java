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
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.DisciplinesPanel;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
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
public class PRegistrationInternalMannschaftStaffelPlugin extends AFeature {

    static final String MALE = I18n.get("male");
    static final String FEMALE = I18n.get("female");
    static final String ADD = I18n.get("add");
    static final String ADD_MULTI = I18n.get("AddMultiple");

    static final String WRONG_INPUT = I18n.get("WrongInput");

    CorePlugin core = null;

    JPanel panel;

    JIntegerField startnummer;
    JWarningTextField name;
    JDoubleField melde;
    JWarningTextField bemerkung;
    JComboBox<String> gliederung;
    JWarningTextField qualifikationsebene;
    JComboBox<String> altersklasse;
    JComboBox<String> geschlecht;
    JIntegerField amount;
    private JComboBox<String> ausserkonkurrenz;
    @SuppressWarnings("rawtypes")
    private DisciplinesPanel disciplines;
    private JTaskPaneGroup jt;
    JButton hinzu;
    private JButton hinzu2;

    private UpdateListener ul = new UpdateListener();

    /**
     * This method initializes
     */
    public PRegistrationInternalMannschaftStaffelPlugin() {
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
        melde = new JDoubleField(JDoubleField.EMPTY_FIELD, 10000);
        bemerkung = new JWarningTextField();
        gliederung = new JCompletingComboBox<>(true);
        qualifikationsebene = new JWarningTextField();
        altersklasse = new JComboBox<>();
        geschlecht = new JComboBox<>(new String[] { FEMALE, MALE });
        amount = new JIntegerField(JIntegerField.EMPTY_FIELD, 20);
        ausserkonkurrenz = new JComboBox<>(new String[] { I18n.get("Normal"), I18n.get("AusserKonkurrenz") });
        ausserkonkurrenz.setSelectedIndex(0);
        hinzu = new JButton(ADD, IconManager.getSmallIcon("add"));
        hinzu2 = new JButton(ADD_MULTI, IconManager.getSmallIcon("add"));

        startnummer.getDocument().addDocumentListener(ul);
        startnummer.addActionListener(ul);
        name.getDocument().addDocumentListener(ul);
        name.addActionListener(ul);
        melde.getDocument().addDocumentListener(ul);
        melde.addActionListener(ul);
        Component c = ComboBoxUtil.getEditorComponent(gliederung);
        if (c instanceof JTextField jtf) {
            jtf.getDocument().addDocumentListener(ul);
        } else {
            gliederung.addActionListener(ul);
            gliederung.addItemListener(ul);
        }

        startnummer.setAutoSelectAll(true);
        name.setAutoSelectAll(true);
        melde.setAutoSelectAll(true);
        bemerkung.setAutoSelectAll(true);
        qualifikationsebene.setAutoSelectAll(true);
        amount.setAutoSelectAll(true);

        startnummer.addKeyListener(new EnterListener(name));
        name.addKeyListener(new EnterListener(melde));
        melde.addKeyListener(new EnterListener(bemerkung));
        bemerkung.addKeyListener(new EnterListener(gliederung));
        gliederung.addKeyListener(new EnterListener(qualifikationsebene));
        qualifikationsebene.addKeyListener(new EnterListener(altersklasse));
        altersklasse.addKeyListener(new EnterListener(geschlecht));
        geschlecht.addKeyListener(new EnterListener(ausserkonkurrenz));
        ausserkonkurrenz.addKeyListener(new EnterListener(hinzu));

        amount.getDocument().addDocumentListener(ul);
        amount.addActionListener(ul);

        altersklasse.addItemListener(event -> updateDisciplines());

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu," + "fill:default:grow,4dlu",
                FormLayoutUtils.createLayoutString(15));
        layout.setRowGroups(new int[][] { { 2, 4, 6, 8, 10, 12, 14, 16, 18, 24 }, { 22, 26 } });
        panel = new JPanel(layout);
        panel.setName("staffel");

        panel.add(new JLabel(I18n.get("Startnumber")), CC.xy(2, 2));
        panel.add(new JLabel(I18n.get("Name")), CC.xy(2, 4));
        panel.add(new JLabel(I18n.get("ReportedPoints")), CC.xy(2, 6));
        panel.add(new JLabel(I18n.get("Comment")), CC.xy(2, 8));
        panel.add(new JLabel(I18n.get("Organisation")), CC.xy(2, 10));
        panel.add(new JLabel(I18n.get("Qualifikationsebene")), CC.xy(2, 12));
        panel.add(new JLabel(I18n.get("AgeGroup")), CC.xy(2, 14));
        panel.add(new JLabel(I18n.get("Sex")), CC.xy(2, 16));
        panel.add(new JLabel(I18n.get("Wertung")), CC.xy(2, 18));

        panel.add(startnummer, CC.xy(4, 2));
        panel.add(name, CC.xy(4, 4));
        panel.add(melde, CC.xy(4, 6));
        panel.add(bemerkung, CC.xy(4, 8));
        panel.add(gliederung, CC.xy(4, 10));
        panel.add(qualifikationsebene, CC.xy(4, 12));
        panel.add(altersklasse, CC.xy(4, 14));
        panel.add(geschlecht, CC.xy(4, 16));
        panel.add(ausserkonkurrenz, CC.xy(4, 18));

        panel.add(hinzu, CC.xywh(2, 22, 3, 1, "right,default"));

        panel.add(new JLabel(I18n.get("Amount")), CC.xy(2, 24));
        panel.add(amount, CC.xy(4, 24));

        panel.add(hinzu2, CC.xywh(2, 26, 3, 1, "right,default"));

        hinzu.addActionListener(new NewActionListener());
        hinzu2.addActionListener(new MultiNewActionListener());
        gliederung.setEditable(true);
        geschlecht.setSelectedIndex(0);

        startnummer.requestFocus();

        updateButtons();
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
            disciplines = new DisciplinesPanel(core.getWettkampf());
            jt = new JTaskPaneGroup();
            jt.setUI(new GradientTaskPaneGroupUI());
            jt.setTitle(I18n.get("DisciplineSelectionAndTimes"));
            jt.add(disciplines);
            panel.add(jt, CC.xyw(4, 20, 1, "fill,fill"));

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

    @SuppressWarnings("rawtypes")
    void viewNextSN() {
        AWettkampf wk = core.getMannschaftWettkampf();
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
        boolean result = startnummer.isValidInt() && (name.getText().length() > 0) && melde.isValidDouble();
        if (disciplines != null) {
            result = result && disciplines.isInputValid();
        }

        if (result) {
            Component c = ComboBoxUtil.getEditorComponent(gliederung);
            if (c instanceof JTextField jtf) {
                result = jtf.getText().length() > 0;
            } else {
                result = (gliederung.getSelectedItem() != null)
                        && (gliederung.getSelectedItem().toString().length() > 0);
            }
        }

        hinzu.setEnabled(result);
        hinzu2.setEnabled(result && amount.isValidInt() && (amount.getInt() > 0));
    }

    final class NewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            addNewMannschaft();
        }
    }

    final class MultiNewActionListener implements ActionListener {

        private void add() {
            addNewMannschaften();
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            synchronized (getController().getWindow()) {
                getController().getWindow().setEnabled(false);
            }
            add();
            synchronized (getController().getWindow()) {
                getController().getWindow().setEnabled(true);
            }
        }
    }

    public JPanel getPanel() {
        if (panel == null) {
            initialize();
            dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED);
        }
        return panel;
    }

    final class UpdateListener
            implements ActionListener, DocumentListener, ItemListener, DisciplinesPanel.DataChangeListener {

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

    void addNewMannschaft() {
        int sn = startnummer.getInt();
        String teamname = name.getText();
        String g = "";
        if (gliederung.getSelectedItem() != null) {
            g = gliederung.getSelectedItem().toString();
        }

        if (sn > 0 && SearchUtils.getSchwimmer(core.getMannschaftWettkampf(), sn) != null) {
            DialogUtils.inform(null, WRONG_INPUT, I18n.get("StartnummerAlreadyAssigned"),
                    I18n.get("StartnummerAlreadyAssigned.Note"));
            startnummer.requestFocus();
            return;
        }
        if (teamname.length() == 0) {
            DialogUtils.inform(null, WRONG_INPUT, I18n.get("NameMustNotBeEmpty"), I18n.get("NameMustNotBeEmpty.Note"));
            name.requestFocus();
            return;
        }
        if (g.length() == 0) {
            DialogUtils.inform(null, WRONG_INPUT, I18n.get("OrganisationMustNotBeEmpty"),
                    I18n.get("OrganisationMustNotBeEmpty.Note"));
            gliederung.requestFocus();
            return;
        }

        core.addMannschaft(name.getText(), gliederung.getSelectedItem().toString(), qualifikationsebene.getText(),
                geschlecht.getSelectedIndex() == 1,
                altersklasse.getSelectedIndex(), bemerkung.getText(), startnummer.getInt(), melde.getDouble(),
                disciplines.getSelection(),
                disciplines.getMeldezeiten(), ausserkonkurrenz.getSelectedIndex() == 1, null);

        name.setText("");
        bemerkung.setText("");
        startnummer.setText("");
        melde.setText("");
        amount.setText("");
        disciplines.reset();

        startnummer.setInt(core.getMannschaftWettkampf().viewNextStartnummer());
        startnummer.requestFocus();
    }

    void addNewMannschaften() {
        int sn = startnummer.getInt();
        String teamname = name.getText();
        String g = "";
        if (gliederung.getSelectedItem() != null) {
            g = gliederung.getSelectedItem().toString();
        }

        if (sn > 0 && SearchUtils.getSchwimmer(core.getMannschaftWettkampf(), sn) != null) {
            DialogUtils.inform(null, WRONG_INPUT, I18n.get("StartnummerAlreadyAssigned"),
                    I18n.get("StartnummerAlreadyAssigned.Note"));
            startnummer.requestFocus();
            return;
        }
        if (teamname.isEmpty()) {
            DialogUtils.inform(null, WRONG_INPUT, I18n.get("NameMustNotBeEmpty"), I18n.get("NameMustNotBeEmpty.Note"));
            name.requestFocus();
            return;
        }
        if (g.isEmpty()) {
            DialogUtils.inform(null, WRONG_INPUT, I18n.get("OrganisationMustNotBeEmpty"),
                    I18n.get("OrganisationMustNotBeEmpty.Note"));
            gliederung.requestFocus();
            return;
        }
        if (amount.getInt() <= 0) {
            DialogUtils.inform(null, WRONG_INPUT, I18n.get("AmountMustBeBiggerThanZero"),
                    I18n.get("AmountMustBeBiggerThanZero.Note"));
            amount.requestFocus();
            return;
        }

        boolean maennlich = geschlecht.getSelectedIndex() == 1;
        int ak = altersklasse.getSelectedIndex();
        String b = bemerkung.getText();
        double punkte = melde.getDouble();
        String quali = qualifikationsebene.getText();

        core.addMannschaften(teamname, amount.getInt(), g, quali, maennlich, ak, b, sn, punkte,
                disciplines.getSelection(), disciplines.getMeldezeiten(),
                ausserkonkurrenz.getSelectedIndex() == 1);

        name.setText("");
        bemerkung.setText("");
        startnummer.setInt(JIntegerField.EMPTY_FIELD);
        melde.setDouble(JDoubleField.EMPTY_FIELD);
        amount.setInt(JIntegerField.EMPTY_FIELD);
        disciplines.reset();

        startnummer.setInt(core.getMannschaftWettkampf().viewNextStartnummer());
        startnummer.requestFocus();
    }

    private void updateDisciplines() {
        disciplines.showAk(Math.max(0, altersklasse.getSelectedIndex()));
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
                        addNewMannschaft();
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