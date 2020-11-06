/*
 * Created on 19.02.2006
 */
package de.df.jauswertung.gui.plugins.check;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.shared.swing.renderer.DefaultCellRenderer;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Disziplin;
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
import de.df.jutils.util.StringTools;

public class JRegistrationTimesInputPanel extends JGlassPanel<JPanel> {

    private static final long     serialVersionUID = 1973322987330030590L;

    private CorePlugin            core;
    private FEditorPlugin         editor;
    private JMissingInputFrame    parent;

    ASchwimmer[][]                swimmers         = new ASchwimmer[0][0];
    StatusDetail[][]              stati            = new StatusDetail[0][0];

    private JGlassPanel<JPanel>[] panels;
    JIntegerField[][]             input;
    private JTimeField[][]        time;
    private JLabel[][]            recs;
    private JButton[][]           edit;
    private JLabel[][]            signal;
    private JLabel[][]            discipline;

    JList<String>                 disciplines;
    JPanel                        container;
    CardLayout                    layout;

    private JPanel                panel;

    private boolean               changed          = false;

    public JRegistrationTimesInputPanel(JMissingInputFrame parent, CorePlugin core, FEditorPlugin editor) {
        super(new JPanel());
        panel = getComponent();
        panel.setBorder(BorderUtils.createSpaceBorder());
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

    @SuppressWarnings("unchecked")
    private void init() {
        JLabel info = new JLabel(I18n.get("NoMissingTimesInfo"));
        info.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information"), true));
        getGlassPanel().setLayout(new CenterLayout());
        getGlassPanel().add(info);

        setEnabled(false);

        layout = new CardLayout();
        container = new JPanel(layout);

        disciplines = new JList<String>();
        disciplines.setMinimumSize(new Dimension(100, 10));
        disciplines.setPreferredSize(new Dimension(100, 10));
        disciplines.setCellRenderer(new DefaultCellRenderer() {
            @SuppressWarnings("rawtypes")
            @Override
            public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
                Component c = super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    jc.setBorder(BorderUtils.createSpaceBorder(2));
                }
                return c;
            }
        });
        disciplines.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                layout.show(container, "" + disciplines.getSelectedIndex());
            }
        });
        JScrollPane scr1 = new JScrollPane(disciplines);
        scr1.setBorder(BorderUtils.createLabeledBorder(I18n.get("Disciplines")));

        panel.setLayout(new BorderLayout(5, 5));
        panel.add(scr1, BorderLayout.WEST);
        panel.add(container, BorderLayout.CENTER);
    }

    void updateData() {
        AWettkampf<ASchwimmer> wk = core.getWettkampf();
        if (!wk.hasSchwimmer()) {
            swimmers = new ASchwimmer[0][0];
            return;
        }
        int max = wk.getRegelwerk().getMaxDisciplineCount();
        @SuppressWarnings("unchecked")
        LinkedList<ASchwimmer>[] notimes = new LinkedList[max];
        @SuppressWarnings("unchecked")
        LinkedList<StatusDetail>[] timestatus = new LinkedList[max];
        for (int x = 0; x < max; x++) {
            notimes[x] = new LinkedList<ASchwimmer>();
            timestatus[x] = new LinkedList<StatusDetail>();
        }

        LinkedList<ASchwimmer> swimmerlist = wk.getSchwimmer();
        ListIterator<ASchwimmer> li = swimmerlist.listIterator();
        while (li.hasNext()) {
            ASchwimmer s = li.next();
            for (int x = 0; x < s.getAK().getDiszAnzahl(); x++) {
                switch (SchwimmerUtils.getRegistrationTimeStatus(wk, s, x)) {
                case FAST:
                    notimes[x].addLast(s);
                    timestatus[x].addLast(new StatusDetail(s, TimeStatus.FAST));
                    break;
                case SLOW:
                    notimes[x].addLast(s);
                    timestatus[x].addLast(new StatusDetail(s, TimeStatus.SLOW));
                    break;
                case NORMAL:
                    break;
                case NONE:
                    notimes[x].addLast(s);
                    timestatus[x].addLast(new StatusDetail(s, TimeStatus.NONE));
                    break;
                }
            }
        }
        boolean hasSchwimmer = false;
        for (int x = 0; x < max; x++) {
            if (notimes[x].size() > 0) {
                hasSchwimmer = true;
                break;
            }
        }
        if (hasSchwimmer) {
            swimmers = new ASchwimmer[max][0];
            stati = new StatusDetail[max][0];
            for (int x = 0; x < max; x++) {
                updateSwimmers(notimes[x], timestatus[x], x);
            }
        } else {
            swimmers = new ASchwimmer[0][0];
            stati = new StatusDetail[0][0];
        }
    }

    static final class StatusDetail {

        private final ASchwimmer swimmer;

        private final TimeStatus status;

        public StatusDetail(ASchwimmer s, TimeStatus status) {
            swimmer = s;
            this.status = status;
        }

        public ASchwimmer getSchwimmer() {
            return swimmer;
        }

        public TimeStatus getStatus() {
            return status;
        }
    }

    private void updateSwimmers(LinkedList<ASchwimmer> notimes, LinkedList<StatusDetail> timestatus, int x) {
        swimmers[x] = notimes.toArray(new ASchwimmer[notimes.size()]);
        stati[x] = timestatus.toArray(new StatusDetail[timestatus.size()]);
        sortSwimmers(x);
    }

    private void sortSwimmers(int x) {
        Arrays.sort(swimmers[x], new Comparator<ASchwimmer>() {
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
        Arrays.sort(stati[x], new Comparator<StatusDetail>() {
            @Override
            public int compare(StatusDetail s1, StatusDetail s2) {
                ASchwimmer o1 = s1.getSchwimmer();
                ASchwimmer o2 = s2.getSchwimmer();
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

    @SuppressWarnings({ "unchecked" })
    void updateGUI() {
        setEnabled(swimmers.length > 0);
        container.removeAll();
        if (swimmers.length == 0) {
            return;
        }

        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();

        int dcount = wk.getRegelwerk().getMaxDisciplineCount();
        panels = new JGlassPanel[dcount];
        input = new JIntegerField[dcount][0];
        time = new JTimeField[dcount][0];
        recs = new JLabel[dcount][0];
        edit = new JButton[dcount][0];
        signal = new JLabel[dcount][0];
        discipline = new JLabel[dcount][0];

        LinkedList<String> discs = new LinkedList<String>();
        for (int x = 0; x < dcount; x++) {
            discs.addLast(I18n.get("DisciplineNumber", x + 1));
            panels[x] = new JGlassPanel<JPanel>(new JPanel());
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
            edit[x] = new JButton[swimmers[x].length];
            signal[x] = new JLabel[swimmers[x].length];
            discipline[x] = new JLabel[swimmers[x].length];

            JPanel p = panels[x].getComponent();
            FormLayout formlayout = new FormLayout("4dlu,right:default,4dlu,fill:default:grow," + "4dlu,fill:default:grow,4dlu,fill:default:grow,"
                    + "4dlu,fill:default:grow,4dlu,fill:default," + "4dlu,fill:default:grow,4dlu,fill:default:grow,"
                    + "4dlu,fill:default,4dlu,fill:default,4dlu", FormLayoutUtils.createLayoutString(swimmers[x].length + 1));
            p.setLayout(formlayout);
            p.add(new JLabel(I18n.get("StartnumberShort")), CC.xy(2, 2, "center,center"));
            p.add(new JLabel(I18n.get("Name")), CC.xy(4, 2, "center,center"));
            p.add(new JLabel(I18n.get("Organisation")), CC.xy(6, 2, "center,center"));
            p.add(new JLabel(I18n.get("AgeGroup")), CC.xy(8, 2, "center,center"));
            p.add(new JLabel(I18n.get("Discipline")), CC.xy(10, 2, "center,center"));
            p.add(new JLabel(I18n.get("TimeInput")), CC.xy(14, 2, "center,center"));
            p.add(new JLabel(I18n.get("Time")), CC.xy(16, 2, "center,center"));
            p.add(new JLabel(I18n.get("Rec-Value")), CC.xy(20, 2, "center,center"));
            for (int y = 0; y < swimmers[x].length; y++) {
                input[x][y] = new JIntegerField(JTimeField.MAX_TIME, false, true);
                input[x][y].setValidator(new Validator() {
                    @Override
                    public boolean validate(int value) {
                        value = value / 100;
                        if ((value % 100) >= 60) {
                            return false;
                        }
                        value = value / 100;
                        return value < 1000;
                    }
                });
                input[x][y].addKeyListener(new CursorListener(x, y));
                input[x][y].setHorizontalAlignment(SwingConstants.RIGHT);
                input[x][y].setAutoSelectAll(true);
                time[x][y] = new JTimeField(input[x][y]);
                recs[x][y] = new JLabel();
                edit[x][y] = new JTransparentButton(IconManager.getSmallIcon("edit"));
                signal[x][y] = new JLabel();
                discipline[x][y] = new JLabel();

                if (swimmers[x][y].getAK().getDiszAnzahl() > x) {
                    if (swimmers[x][y].getMeldezeit(x) > 0) {
                        time[x][y].setTimeAsInt(swimmers[x][y].getMeldezeit(x));
                    }
                    Disziplin disziplin = swimmers[x][y].getAK().getDisziplin(x, swimmers[x][y].isMaennlich());
                    recs[x][y].setText(StringTools.zeitString(disziplin.getRec()));
                    discipline[x][y].setText(disziplin.getName());
                    updateStatus(signal[x][y], stati[x][y].getStatus());
                    input[x][y].addFocusListener(new HighPointsListener(x, y));
                    input[x][y].getDocument().addDocumentListener(new InputListener(x, y));
                    edit[x][y].addActionListener(new Editor(x, y));
                } else {
                    time[x][y].setEnabled(false);
                    time[x][y].setEnabled(false);
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
                p.add(edit[x][y], CC.xy(18, y * 2 + 4));
                p.add(recs[x][y], CC.xy(20, y * 2 + 4, "right,fill"));
            }
        }

        int index = Math.max(0, disciplines.getSelectedIndex());
        disciplines.setListData(discs.toArray(new String[discs.size()]));
        if (index < discs.size()) {
            disciplines.setSelectedIndex(index);
        } else {
            disciplines.setSelectedIndex(0);
        }
    }

    static void updateStatus(JLabel label, TimeStatus ts) {
        switch (ts) {
        default:
        case NONE:
            return;
        case FAST:
            label.setIcon(IconManager.getSmallIcon("fast"));
            label.setToolTipText(I18n.getToolTip("TimeTooLow"));
            break;
        case SLOW:
            label.setIcon(IconManager.getSmallIcon("slow"));
            label.setToolTipText(I18n.getToolTip("TimeTooHigh"));
            break;
        }
    }

    void setTime(int x, int y) {
        changed = true;
        swimmers[x][y].setMeldezeit(x, time[x][y].getTimeAsInt());
    }

    void check(ASchwimmer s) {
        StatusDetail sd = null;
        for (int x = 0; x < swimmers.length; x++) {
            boolean remove = false;
            boolean add = false;
            if ((s.getAK().getDiszAnzahl() > x) && s.isDisciplineChosen(x)) {
                TimeStatus ts = SchwimmerUtils.getRegistrationTimeStatus(core.getWettkampf(), s, x);
                if (ts != TimeStatus.NORMAL) {
                    add = true;
                    sd = new StatusDetail(s, ts);
                }
            } else {
                remove = true;
            }

            sortSwimmers(x);

            if (add) {
                boolean found = false;
                LinkedList<ASchwimmer> temp1 = new LinkedList<ASchwimmer>();
                LinkedList<StatusDetail> temp2 = new LinkedList<StatusDetail>();
                for (int y = 0; y < swimmers[x].length; y++) {
                    if (swimmers[x][y].equals(s)) {
                        found = true;
                        break;
                    }
                    temp1.addLast(swimmers[x][y]);
                    temp2.addLast(stati[x][y]);
                }
                if (!found) {
                    temp1.addLast(s);
                    temp2.addLast(sd);
                    updateSwimmers(temp1, temp2, x);
                }
            } else if (remove) {
                boolean found = false;
                LinkedList<ASchwimmer> temp1 = new LinkedList<ASchwimmer>();
                LinkedList<StatusDetail> temp2 = new LinkedList<StatusDetail>();
                for (int y = 0; y < swimmers[x].length; y++) {
                    if (swimmers[x][y].equals(s)) {
                        found = true;
                    } else {
                        temp1.addLast(swimmers[x][y]);
                        temp2.addLast(stati[x][y]);
                    }
                }
                if (found) {
                    updateSwimmers(temp1, temp2, x);
                }
            }
        }
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

    private final class HighPointsListener extends FocusAdapter {

        private final int index;
        private final int disz;

        private String    data = "";

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
            if (!SchwimmerUtils.checkTimeAndNotify(SwingUtilities.getWindowAncestor(JRegistrationTimesInputPanel.this), swimmers[disz][index], disz)) {
                input[disz][index].requestFocus();
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

        public CursorListener(int d, int x) {
            index = x;
            this.disziplin = d;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if ((index > 0) && (e.getKeyCode() == KeyEvent.VK_UP)) {
                input[disziplin][index - 1].requestFocus();
            }
            if ((index + 1 < input[disziplin].length) && ((e.getKeyCode() == KeyEvent.VK_DOWN) || (e.getKeyCode() == KeyEvent.VK_ENTER))) {
                input[disziplin][index + 1].requestFocus();
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