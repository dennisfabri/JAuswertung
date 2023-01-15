package de.df.jauswertung.gui.plugins.aselection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jauswertung.util.vergleicher.SchwimmerGeschlechtVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerMeldepunkteVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerNameVergleicher;
import de.df.jutils.gui.JLabelSeparator;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.util.StringTools;

class JSelectionPanel<T extends ASchwimmer> extends JPanel {

    private final class PenaltyListener implements ActionListener {

        private int x;

        public PenaltyListener(int index) {
            x = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            editPenalties(x);
        }
    }

    private boolean changed = false;

    private static final long serialVersionUID = 3735076099011819345L;

    private final AMSelectionPlugin root;
    private final JFrame parent;
    private final FEditorPlugin editor;

    private T[] swimmers = null;

    private JRadioButton[] yes = null;
    private JRadioButton[] no = null;
    private JButton[] penalty = null;
    private JLabel[] penaltytext = null;

    public JSelectionPanel(JFrame parent, AMSelectionPlugin root, FEditorPlugin editor, LinkedList<ASchwimmer> s) {
        this.parent = parent;
        this.editor = editor;
        this.root = root;

        initGUI(s);
    }

    @SuppressWarnings("unchecked")
    private void initGUI(LinkedList<ASchwimmer> liste) {
        if (liste == null) {
            return;
        }
        swimmers = (T[]) liste.toArray(new ASchwimmer[liste.size()]);
        Arrays.sort(swimmers, new SchwimmerNameVergleicher<ASchwimmer>());
        Arrays.sort(swimmers, new SchwimmerMeldepunkteVergleicher<ASchwimmer>());
        Arrays.sort(swimmers, new SchwimmerGeschlechtVergleicher<ASchwimmer>());

        yes = new JRadioButton[swimmers.length];
        no = new JRadioButton[swimmers.length];
        penaltytext = new JLabel[swimmers.length];
        penalty = new JButton[swimmers.length];

        FormLayout layout = new FormLayout(
                "4dlu,fill:default," + "4dlu,fill:default:grow,4dlu,fill:default:grow,"
                        + "4dlu,fill:default,4dlu,fill:default,"
                        + "4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(swimmers.length + 2 + 2));
        layout.setColumnGroups(new int[][] { { 10, 12, 14, 16 } });

        setLayout(layout);

        int row = 0;

        row = addTitle(row);

        if (swimmers.length == 0) {
            // No entries
            return;
        }

        boolean female = true;
        if (!swimmers[0].isMaennlich()) {
            row += 2;
            add(new JLabelSeparator(I18n.geschlechtToStringSubject(swimmers[0])), CC.xyw(2, row, 15, "fill,fill"));
        }

        ActionListener buttonAL = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setChanged();
            }
        };

        for (int x = 0; x < swimmers.length; x++) {
            if (female && (swimmers[x].isMaennlich())) {
                female = false;
                row += 2;
                add(new JLabelSeparator(I18n.get("Male")), CC.xyw(2, row, 13, "fill,fill"));
            }
            row += 2;

            yes[x] = new JRadioButton();
            no[x] = new JRadioButton();
            ButtonGroup bg = new ButtonGroup();
            bg.add(yes[x]);
            bg.add(no[x]);

            if (root.getSelection().getValue(swimmers[x])) {
                yes[x].setSelected(true);
            } else {
                no[x].setSelected(true);
            }
            yes[x].addActionListener(buttonAL);
            no[x].addActionListener(buttonAL);

            penaltytext[x] = new JLabel();
            penaltytext[x].setText(PenaltyUtils.getPenaltyShortText(
                    swimmers[x].getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF), swimmers[x].getAK()));
            penalty[x] = new JButton(I18n.get("Penalty"));
            penalty[x].addActionListener(new PenaltyListener(x));

            add(new JLabel(StartnumberFormatManager.format(swimmers[x])), CC.xy(2, row));
            add(new JLabel(swimmers[x].getName()), CC.xy(4, row));
            add(new JLabel(swimmers[x].getGliederung()), CC.xy(6, row));
            add(new JLabel(StringTools.punkteString(swimmers[x].getMeldepunkte(0))), CC.xy(8, row));
            add(yes[x], CC.xy(10, row, "center,center"));
            add(no[x], CC.xy(12, row, "center,center"));
            add(penaltytext[x], CC.xy(14, row));
            add(penalty[x], CC.xy(16, row));
        }
    }

    private int addTitle(int row) {
        row += 2;

        add(new JLabel(I18n.get("StartnumberShort")), CC.xywh(2, row, 1, 3, "center,center"));
        add(new JLabel(I18n.get("Name")), CC.xywh(4, row, 1, 3, "center,center"));
        add(new JLabel(I18n.get("Organisation")), CC.xywh(6, row, 1, 3, "center,center"));
        add(new JLabel(I18n.get("ReportedPointsShort")), CC.xywh(8, row, 1, 3, "center,center"));
        add(new JLabel(I18n.get("Check")), CC.xyw(10, row, 3, "center,center"));
        add(new JLabel(I18n.get("Penalty")), CC.xywh(14, row, 1, 3, "center,center"));
        row += 2;

        add(new JLabel(I18n.get("Yes")), CC.xy(10, row, "center,center"));
        add(new JLabel(I18n.get("No")), CC.xy(12, row, "center,center"));

        return row;
    }

    void doSave() {
        for (int x = 0; x < swimmers.length; x++) {
            boolean selected = yes[x].isSelected();
            root.getSelection().setValue(swimmers[x], selected);
        }
        changed = false;
    }

    void doUpdate() {
        for (int x = 0; x < swimmers.length; x++) {
            if (root.getSelection().getValue(swimmers[x])) {
                yes[x].setSelected(true);
            } else {
                no[x].setSelected(true);
            }
        }
        changed = false;
    }

    boolean hasChanged() {
        return changed;
    }

    void editPenalties(int x) {
        editor.runPenaltyEditor(parent, swimmers[x].getWettkampf(), swimmers[x]);
        penaltytext[x].setText(PenaltyUtils.getPenaltyShortText(
                swimmers[x].getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF), swimmers[x].getAK()));
    }

    void setChanged() {
        changed = true;
    }
}