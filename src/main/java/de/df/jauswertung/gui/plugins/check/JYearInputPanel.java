/*
 * Created on 19.02.2006
 */
package de.df.jauswertung.gui.plugins.check;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.CenterLayout;
import de.df.jutils.gui.layout.SimpleTableBuilder;

public class JYearInputPanel extends JGlassPanel<JPanel> {

    private static final long  serialVersionUID = 8023494074221318513L;

    private CorePlugin         core;
    private FEditorPlugin      editor;
    private JMissingInputFrame parent;

    private Teilnehmer[]       swimmers;
    JComboBox<String>[]        agegroups;
    JIntegerField[]            years;
    private JButton[]          edit;

    private JPanel             panel;

    private boolean            changed          = false;

    public JYearInputPanel(JMissingInputFrame parent, CorePlugin core, FEditorPlugin editor) {
        super(new JPanel());
        panel = getComponent();
        this.core = core;
        this.editor = editor;
        this.parent = parent;
        init();
    }

    public boolean hasChanged() {
        return changed;
    }

    public void unsetChanged() {
        changed = false;
    }

    private void init() {
        JLabel info = new JLabel(I18n.get("NoMissingYearsInfo"));
        info.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information"), true));
        getGlassPanel().setLayout(new CenterLayout());
        getGlassPanel().add(info);
        swimmers = new Teilnehmer[0];
        years = new JIntegerField[0];
        edit = new JButton[0];

        setEnabled(false);
    }

    @SuppressWarnings("rawtypes")
    void updateData() {
        AWettkampf xwk = core.getWettkampf();
        if (!xwk.hasSchwimmer()) {
            swimmers = new Teilnehmer[0];
            return;
        }
        if (!(xwk instanceof EinzelWettkampf)) {
            swimmers = new Teilnehmer[0];
            return;
        }
        EinzelWettkampf wk = (EinzelWettkampf) xwk;
        LinkedList<Teilnehmer> noyear = new LinkedList<Teilnehmer>();

        LinkedList<Teilnehmer> swimmerlist = wk.getSchwimmer();
        ListIterator<Teilnehmer> li = swimmerlist.listIterator();
        while (li.hasNext()) {
            Teilnehmer s = li.next();
            if (s.getJahrgang() == 0) {
                noyear.addLast(s);
            } else {
                if (!s.fitsAgeGroup()) {
                    noyear.addLast(s);
                }
            }
        }
        updateSwimmers(noyear);
    }

    private void updateSwimmers(LinkedList<Teilnehmer> noyear) {
        swimmers = noyear.toArray(new Teilnehmer[noyear.size()]);

        sortSwimmers();
    }

    private void sortSwimmers() {
        Arrays.sort(swimmers, new Comparator<ASchwimmer>() {
            @Override
            public int compare(ASchwimmer o1, ASchwimmer o2) {
                if (o1.getAKNummer() != o2.getAKNummer()) {
                    return o1.getAKNummer() - o2.getAKNummer();
                }
                if (o1.isMaennlich() != o2.isMaennlich()) {
                    return (o1.isMaennlich() ? 1 : -1);
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    @SuppressWarnings("unchecked")
    void updateGUI() {
        setEnabled(swimmers.length > 0);

        panel.removeAll();
        SimpleTableBuilder dfb = new SimpleTableBuilder(panel, new boolean[] { false, true, true, true, true, false, true }, false);
        dfb.add(new JLabel(I18n.get("StartnumberShort")), "center,center");
        dfb.add(new JLabel(I18n.get("Name")), "center,center");
        dfb.add(new JLabel(I18n.get("Organisation")), "center,center");
        dfb.add(new JLabel(I18n.get("AgeGroup")), "center,center");
        dfb.add(new JLabel(I18n.get("YearOfBirth")), "center,center");
        dfb.add(new JLabel());
        dfb.add(new JLabel(I18n.get("Proposal")), "center,center");

        String[] groups = createAgeGroups(core.getWettkampf());

        agegroups = new JComboBox[swimmers.length];
        years = new JIntegerField[swimmers.length];
        edit = new JButton[swimmers.length];
        for (int x = 0; x < swimmers.length; x++) {
            Teilnehmer s = swimmers[x];

            String proposal = "";

            agegroups[x] = new JComboBox<String>(groups);
            agegroups[x].setSelectedIndex(s.getAKNummer());
            agegroups[x].addActionListener(new AgeGroupInputListener(x));
            years[x] = new JIntegerField(false, true);
            years[x].setHorizontalAlignment(SwingConstants.RIGHT);
            years[x].setAutoSelectAll(true);
            if (s.getJahrgang() > 0) {
                years[x].setInt(s.getJahrgang());
                int ak = s.getWettkampf().getRegelwerk().getAkNachAlter(Calendar.getInstance().get(Calendar.YEAR) - s.getJahrgang());
                if (ak >= 0) {
                    proposal = s.getWettkampf().getRegelwerk().getAk(ak).getName();
                }
            }
            years[x].getDocument().addDocumentListener(new YearInputListener(x));
            years[x].addKeyListener(new CursorListener(x));
            edit[x] = new JTransparentButton(IconManager.getSmallIcon("edit"));
            edit[x].addActionListener(new Editor(x));

            dfb.add(new JLabel(StartnumberFormatManager.format(s)));
            dfb.add(new JLabel(s.getName()));
            dfb.add(new JLabel(s.getGliederung()));
            dfb.add(agegroups[x]);
            dfb.add(years[x]);
            dfb.add(edit[x]);
            dfb.add(new JLabel(proposal));
        }

        dfb.getPanel();
    }

    @SuppressWarnings("rawtypes")
    private String[] createAgeGroups(AWettkampf wk) {
        Regelwerk aks = wk.getRegelwerk();
        String[] result = new String[aks.size()];
        for (int x = 0; x < result.length; x++) {
            result[x] = aks.getAk(x).getName();
        }
        return result;
    }

    void changeYearOfBirth(int index) {
        changed = true;
        swimmers[index].setJahrgang(years[index].getInt());
    }

    void check(ASchwimmer a) {
        if (!(a instanceof Teilnehmer)) {
            return;
        }

        Teilnehmer s = (Teilnehmer) a;

        sortSwimmers();

        if (s.getJahrgang() == 0) {
            LinkedList<Teilnehmer> temp = new LinkedList<Teilnehmer>();
            for (Teilnehmer swimmer : swimmers) {
                if (swimmer.equals(s)) {
                    return;
                }
                temp.addLast(swimmer);
            }
            temp.addLast(s);
            updateSwimmers(temp);
        }
    }

    void edit(int index) {
        editor.editSchwimmer(parent, swimmers[index], false);
        parent.check(swimmers[index]);
        parent.updateGUI();
    }

    void setAgeGroup(int index) {
        swimmers[index].setAKNummer(agegroups[index].getSelectedIndex(), true);
    }

    private class YearInputListener implements DocumentListener {

        private final int index;

        public YearInputListener(int x) {
            index = x;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changeYearOfBirth(index);
        }
    }

    private class Editor implements ActionListener {

        private final int index;

        public Editor(int x) {
            index = x;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            edit(index);
        }
    }

    private class AgeGroupInputListener implements ActionListener {

        private final int index;

        public AgeGroupInputListener(int x) {
            index = x;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setAgeGroup(index);
        }
    }

    private class CursorListener implements KeyListener {

        private final int index;

        public CursorListener(int x) {
            index = x;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if ((index > 0) && (e.getKeyCode() == KeyEvent.VK_UP)) {
                years[index - 1].requestFocus();
            }
            if ((index + 1 < years.length) && ((e.getKeyCode() == KeyEvent.VK_DOWN) || (e.getKeyCode() == KeyEvent.VK_ENTER))) {
                years[index + 1].requestFocus();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // keyPressed(e);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            keyPressed(e);
        }
    }
}