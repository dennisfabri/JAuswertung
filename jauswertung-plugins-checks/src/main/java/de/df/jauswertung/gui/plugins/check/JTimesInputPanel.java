package de.df.jauswertung.gui.plugins.check;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.shared.swing.renderer.DefaultCellRenderer;
import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jauswertung.gui.util.TimeStatus;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JIntegerField.Validator;
import de.df.jutils.gui.JTimeField;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.CenterLayout;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.util.StringTools;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.LinkedList;

public class JTimesInputPanel extends JGlassPanel<JPanel> {

    private static final long serialVersionUID = 1973322987330030590L;

    private final CorePlugin core;
    private final FEditorPlugin editor;
    private final JMissingInputFrame parent;

    private final ZeitenPruefliste pruefliste;
    private final boolean mitStrafen;

    private ASchwimmer[][] swimmers = new ASchwimmer[0][0];
    private StatusDetail[][] stati = new StatusDetail[0][0];

    private JIntegerField[][] input;
    private JTimeField[][] time;
    private JButton[][] penalty;
    private JLabel[][] penaltytext;
    private JLabel[][] recs;
    private JButton[][] edit;
    private JLabel[][] signal;
    private JLabel[][] discipline;

    private JList<String> disciplines;
    private JPanel container;
    private CardLayout layout;

    private final JPanel panel;

    private boolean changed = false;

    JTimesInputPanel(JMissingInputFrame parent, CorePlugin core, FEditorPlugin editor, Zeitart art) {
        super(new JPanel());
        panel = getComponent();
        panel.setBorder(BorderUtils.createSpaceBorder());
        this.core = core;
        this.editor = editor;
        this.parent = parent;
        this.pruefliste = new ZeitenPruefliste(art);
        this.mitStrafen = art == Zeitart.ZEIT;

        init();
    }

    public boolean hasChanged() {
        return changed;
    }

    public void unsetChanged() {
        changed = false;
    }

    @SuppressWarnings("unchecked")
    private void init() {
        JLabel info = new JLabel(I18n.get("NoMissingTimesInfo"));
        info.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information"), true));
        getGlassPanel().setLayout(new CenterLayout());
        getGlassPanel().add(info);

        setEnabled(false);

        layout = new CardLayout();
        container = new JPanel(layout);

        disciplines = new JList<>();
        disciplines.setMinimumSize(new Dimension(100, 10));
        disciplines.setPreferredSize(new Dimension(100, 10));
        disciplines.setCellRenderer(new DefaultCellRenderer() {
            private static final long serialVersionUID = -4064957849985467566L;

            @SuppressWarnings("rawtypes")
            @Override
            public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3,
                                                          boolean arg4) {
                Component c = super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
                if (c instanceof JComponent jc) {
                    jc.setBorder(BorderUtils.createSpaceBorder(2));
                }
                return c;
            }
        });
        disciplines.addListSelectionListener(e -> {
            layout.show(container, "" + disciplines.getSelectedIndex());
        });
        JScrollPane scr1 = new JScrollPane(disciplines);
        scr1.setBorder(BorderUtils.createLabeledBorder(I18n.get("Disciplines")));

        panel.setLayout(new BorderLayout(5, 5));
        panel.add(scr1, BorderLayout.WEST);
        panel.add(container, BorderLayout.CENTER);
    }

    void updateData() {
        pruefliste.baue(core.getWettkampf());
        uebernehmeEintraege();
    }

    private void uebernehmeEintraege() {
        int anzahl = pruefliste.getDisziplinen();
        swimmers = new ASchwimmer[anzahl][];
        stati = new StatusDetail[anzahl][];
        for (int x = 0; x < anzahl; x++) {
            stati[x] = pruefliste.getEintraege(x).toArray(new StatusDetail[0]);
            swimmers[x] = Arrays.stream(stati[x]).map(StatusDetail::schwimmer).toArray(ASchwimmer[]::new);
        }
    }

    @SuppressWarnings({"unchecked"})
    void updateGUI() {
        setEnabled(swimmers.length > 0);
        container.removeAll();
        if (swimmers.length == 0) {
            return;
        }

        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();

        int dcount = wk.getRegelwerk().getMaxDisciplineCount();
        JGlassPanel<JPanel>[] panels = new JGlassPanel[dcount];
        input = new JIntegerField[dcount][0];
        time = new JTimeField[dcount][0];
        penalty = new JButton[dcount][0];
        recs = new JLabel[dcount][0];
        penaltytext = new JLabel[dcount][0];
        edit = new JButton[dcount][0];
        signal = new JLabel[dcount][0];
        discipline = new JLabel[dcount][0];

        LinkedList<String> discs = new LinkedList<>();
        for (int x = 0; x < dcount; x++) {
            discs.addLast(I18n.get("DisciplineNumber", x + 1));
            panels[x] = new JGlassPanel<>(new JPanel());
            JScrollPane scroller = new JScrollPane(panels[x]);
            scroller.setBorder(BorderUtils.createLabeledBorder(I18n.get("DisciplineNumber", x + 1)));
            scroller.getHorizontalScrollBar().setUnitIncrement(10);
            scroller.getVerticalScrollBar().setUnitIncrement(10);
            container.add(scroller, "" + x);
        }

        for (int x = 0; x < swimmers.length; x++) {
            input[x] = new JIntegerField[swimmers[x].length];
            time[x] = new JTimeField[swimmers[x].length];
            recs[x] = new JLabel[swimmers[x].length];
            penalty[x] = new JButton[swimmers[x].length];
            penaltytext[x] = new JLabel[swimmers[x].length];
            edit[x] = new JButton[swimmers[x].length];
            signal[x] = new JLabel[swimmers[x].length];
            discipline[x] = new JLabel[swimmers[x].length];

            JPanel p = panels[x].getComponent();
            FormLayout formlayout = new FormLayout(
                    "4dlu,right:default,4dlu,fill:default:grow," + "4dlu,fill:default:grow,4dlu,fill:default:grow,"
                            + "4dlu,fill:default:grow,4dlu,fill:default,"
                            + "4dlu,fill:default:grow,4dlu,fill:default:grow,"
                            + (mitStrafen ? "4dlu,fill:default:grow,4dlu,fill:default," : "")
                            + "4dlu,fill:default,4dlu,fill:default,4dlu",
                    FormLayoutUtils.createLayoutString(swimmers[x].length + 1));
            int editSpalte = mitStrafen ? 22 : 18;
            p.setLayout(formlayout);
            p.add(new JLabel(I18n.get("StartnumberShort")), CC.xy(2, 2, "center,center"));
            p.add(new JLabel(I18n.get("Name")), CC.xy(4, 2, "center,center"));
            p.add(new JLabel(I18n.get("Organisation")), CC.xy(6, 2, "center,center"));
            p.add(new JLabel(I18n.get("AgeGroup")), CC.xy(8, 2, "center,center"));
            p.add(new JLabel(I18n.get("Discipline")), CC.xy(10, 2, "center,center"));
            p.add(new JLabel(I18n.get("TimeInput")), CC.xy(14, 2, "center,center"));
            p.add(new JLabel(I18n.get("Time")), CC.xy(16, 2, "center,center"));
            if (mitStrafen) {
                p.add(new JLabel(I18n.get("Penalty")), CC.xy(18, 2, "center,center"));
            }
            p.add(new JLabel(I18n.get("Rec-Value")), CC.xy(editSpalte + 2, 2, "center,center"));
            for (int y = 0; y < swimmers[x].length; y++) {
                input[x][y] = new JIntegerField(JTimeField.MAX_TIME, false, true);
                input[x][y].setValidator((Validator) value -> {
                    value = value / 100;
                    if ((value % 100) >= 60) {
                        return false;
                    }
                    value = value / 100;
                    return value < 1000;
                });
                input[x][y].addKeyListener(new CursorListener(x, y));
                input[x][y].setHorizontalAlignment(SwingConstants.RIGHT);
                input[x][y].setAutoSelectAll(true);
                time[x][y] = new JTimeField(input[x][y]);
                recs[x][y] = new JLabel();
                penalty[x][y] = new JButton(I18n.get("Penalty"));
                penaltytext[x][y] = new JLabel();
                edit[x][y] = new JTransparentButton(IconManager.getSmallIcon("edit"));
                signal[x][y] = new JLabel();
                discipline[x][y] = new JLabel();

                if (swimmers[x][y].getAK().getDiszAnzahl() > x) {
                    int zeit = pruefliste.getArt().getZeit(swimmers[x][y], x);
                    if (zeit > 0) {
                        time[x][y].setTimeAsInt(zeit);
                    }
                    penaltytext[x][y].setText(PenaltyUtils.getPenaltyShortText(swimmers[x][y].getAkkumulierteStrafe(x),
                                                                               swimmers[x][y].getAK()));
                    Disziplin disziplin = swimmers[x][y].getAK().getDisziplin(x, swimmers[x][y].isMaennlich());
                    recs[x][y].setText(StringTools.zeitString(disziplin.getRec()));
                    discipline[x][y].setText(disziplin.getName());
                    updateStatus(signal[x][y], stati[x][y].status());
                    input[x][y].addFocusListener(new HighPointsListener(x, y));
                    input[x][y].getDocument().addDocumentListener(new InputListener(x, y));
                    penalty[x][y].addActionListener(new PenaltyListener(x, y));
                    edit[x][y].addActionListener(new Editor(x, y));
                } else {
                    time[x][y].setEnabled(false);
                    time[x][y].setEnabled(false);
                    penalty[x][y].setEnabled(false);
                    penaltytext[x][y].setEnabled(false);
                    edit[x][y].setEnabled(false);
                }

                p.add(new JLabel(StartnumberFormatManager.format(swimmers[x][y])), CC.xy(2, y * 2 + 4));
                p.add(new JLabel(swimmers[x][y].getName()), CC.xy(4, y * 2 + 4));
                p.add(new JLabel(swimmers[x][y].getGliederung()), CC.xy(6, y * 2 + 4));
                p.add(new JLabel(I18n.getAgeGroupAsString(swimmers[x][y])), CC.xy(8, y * 2 + 4));
                p.add(discipline[x][y], CC.xy(10, y * 2 + 4));
                p.add(signal[x][y], CC.xy(12, y * 2 + 4));
                p.add(input[x][y], CC.xy(14, y * 2 + 4));
                p.add(time[x][y], CC.xy(16, y * 2 + 4));
                if (mitStrafen) {
                    p.add(penaltytext[x][y], CC.xy(18, y * 2 + 4));
                    p.add(penalty[x][y], CC.xy(20, y * 2 + 4));
                }
                p.add(edit[x][y], CC.xy(editSpalte, y * 2 + 4));
                p.add(recs[x][y], CC.xy(editSpalte + 2, y * 2 + 4, "right,fill"));
            }
        }

        int index = Math.max(0, disciplines.getSelectedIndex());
        disciplines.setListData(discs.toArray(new String[0]));
        if (index < discs.size()) {
            disciplines.setSelectedIndex(index);
        } else {
            disciplines.setSelectedIndex(0);
        }
    }

    static void updateStatus(JLabel label, TimeStatus ts) {
        switch (ts) {
            case FAST:
                label.setIcon(IconManager.getSmallIcon("fast"));
                label.setToolTipText(I18n.getToolTip("TimeTooLow"));
                break;
            case SLOW:
                label.setIcon(IconManager.getSmallIcon("slow"));
                label.setToolTipText(I18n.getToolTip("TimeTooHigh"));
                break;
            default:
                break;
        }
    }

    void setPenalty(int x, int y) {
        editor.runPenaltyEditor(parent, core.getWettkampf(), swimmers[x][y], x);
    }

    public void updatePenalties() {
        if (!mitStrafen) {
            return;
        }
        changed = true;
        for (int x = 0; x < swimmers.length; x++) {
            for (int y = 0; y < swimmers[x].length; y++) {
                penaltytext[x][y].setText(PenaltyUtils.getPenaltyShortText(swimmers[x][y].getAkkumulierteStrafe(x),
                                                                           swimmers[x][y].getAK()));
            }
        }
    }

    void setTime(int x, int y) {
        changed = true;
        pruefliste.getArt().setZeit(swimmers[x][y], x, time[x][y].getTimeAsInt());
    }

    void check(ASchwimmer s) {
        pruefliste.pruefe(core.getWettkampf(), s);
        uebernehmeEintraege();
    }

    void edit(int x, int y) {
        editor.editSchwimmer(parent, swimmers[x][y], false);
        parent.check(swimmers[x][y]);
        parent.updateGUI();
    }

    private class InputListener implements DocumentListener {

        private final int x;
        private final int y;

        public InputListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void insertUpdate(DocumentEvent arg0) {
            changedUpdate(arg0);
        }

        @Override
        public void removeUpdate(DocumentEvent arg0) {
            changedUpdate(arg0);
        }

        @Override
        public void changedUpdate(DocumentEvent arg0) {
            setTime(x, y);
        }
    }

    private class PenaltyListener implements ActionListener {

        private final int x;
        private final int y;

        public PenaltyListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            setPenalty(x, y);
        }
    }

    private final class HighPointsListener extends FocusAdapter {

        private final int index;
        private final int disz;

        private String data = "";

        public HighPointsListener(int x, int y) {
            this.disz = x;
            this.index = y;
        }

        @Override
        public synchronized void focusLost(FocusEvent e) {
            if (swimmers[disz][index] == null) {
                return;
            }
            if (input[disz][index].getText().equals(data)) {
                return;
            }
            data = input[disz][index].getText();
            if (!SchwimmerUtils.checkTimeAndNotify(SwingUtilities.getWindowAncestor(JTimesInputPanel.this),
                                                   swimmers[disz][index], disz)) {
                EDTUtils.requestFocus(input[disz][index]);
            }
        }
    }

    private class Editor implements ActionListener {

        private final int x;
        private final int y;

        public Editor(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            edit(x, y);
        }
    }

    private class CursorListener implements KeyListener {

        private final int index;
        private final int disziplin;

        public CursorListener(int d, int index) {
            this.index = index;
            this.disziplin = d;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if ((index > 0) && (e.getKeyCode() == KeyEvent.VK_UP)) {
                EDTUtils.requestFocus(input[disziplin][index - 1]);
            }
            if ((index + 1 < input[disziplin].length)
                    && ((e.getKeyCode() == KeyEvent.VK_DOWN) || (e.getKeyCode() == KeyEvent.VK_ENTER))) {
                EDTUtils.requestFocus(input[disziplin][index + 1]);
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
