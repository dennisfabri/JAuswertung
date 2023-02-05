package de.df.jauswertung.gui.plugins;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PENALTY;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_POINTS_CHANGED;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.MessagePanel;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.ergebnis.DataType;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JIntegerField.Validator;
import de.df.jutils.gui.JTimeField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.PanelInfo;
import de.df.jutils.plugin.UpdateEvent;
import net.miginfocom.swing.MigLayout;

public class PTimeInputPlugin extends ANullPlugin {

    public class DirectTimeInputAdapter implements TimeInputAdapter {
        private final int index;

        public DirectTimeInputAdapter(int x) {
            this.index = x;
        }

        @Override
        public ASchwimmer getSchwimmer() {
            return swimmers[index];
        }

        @Override
        public JIntegerField getInputField() {
            return inputs[index];
        }

        @Override
        public JTimeField getTimeField() {
            return times[index];
        }

        @Override
        public void moveUp() {
            previousRow(index);

        }

        @Override
        public void moveDown() {
            nextRow(index);

        }

        @Override
        public void updateTime() {
            changeTime(index);
            // updateRow(index, false);

        }

        @Override
        public boolean checkHighPoints() {
            if (swimmers[index] == null) {
                return true;
            }
            if (!SchwimmerUtils.checkTimeAndNotify(controller.getWindow(), swimmers[index],
                    disciplineNumber)) {
                inputs[index].requestFocus();
                return false;
            }
            return true;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public int getDiscipline() {
            return disciplineNumber;
        }

        @Override
        public void zeigeZieleinlauf() {
            // Nothing to do

        }

        @Override
        public boolean setStrafen(List<Strafe> penalties) {
            if (swimmers[index].getStrafen(disciplineNumber).isEmpty() && penalties.isEmpty()) {
                return false;
            }
            swimmers[index].setStrafen(index, penalties);
            return true;
        }

        @Override
        public void updatePenalty() {
            updateRow(index, false);
        }

        @Override
        public void addStrafe(Strafe penalty) {
            swimmers[index].addStrafe(index, penalty);

        }

        @Override
        public void runMeanTimeEditor() {
            editor.runMeanTimeEditor(getSchwimmer(), disciplineNumber);

        }

        @Override
        public boolean isByTimes() {
            return true;
        }

        @Override
        public void runPenaltyPoints() {
            editor.runPenaltyPoints(core.getWettkampf(), swimmers[index], disciplineNumber);
        }

        @Override
        public void runPenaltyCode() {
            editor.runPenaltyCode(core.getWettkampf(), swimmers[index], disciplineNumber,
                    core.getWettkampf().getStrafen());
        }

        @SuppressWarnings("rawtypes")
        @Override
        public AWettkampf getCompetition() {
            return core.getWettkampf();
        }

    }

    private static final String TITLE_INPUT = I18n.get("TimeInput");

    private IPluginManager controller = null;
    private CorePlugin core = null;
    private FEditorPlugin editor = null;

    private JGlassPanel<JPanel> main = null;
    private JPanel inputPanel = null;
    private JScrollPane scroller = null;

    private JComboBox<Integer> discipline;
    private JComboBox<Integer> amount;
    private JButton more;
    private MessagePanel messages = null;
    private FocusListener flx = null;

    private JLabel startnumber;
    private JLabel name;
    private JLabel gliederung;
    private JLabel agegroup;
    private JLabel disciplineLabel;
    private JLabel input;
    private JLabel time;
    private JLabel penalty;

    private JIntegerField[] startnumbers = new JIntegerField[0];
    private JLabel[] names = new JLabel[0];
    private JLabel[] gliederungen = new JLabel[0];
    private JLabel[] agegroups = new JLabel[0];
    private JIntegerField[] inputs = new JIntegerField[0];
    private KeyListener[] kl = new KeyListener[0];
    private DocumentListener[] dl = new DocumentListener[0];
    private FocusListener[] fl = new FocusListener[0];
    private JTimeField[] times = new JTimeField[0];
    private JLabel[] penaltiestext = new JLabel[0];
    private JLabel[] disciplines = new JLabel[0];
    private JButton[] penalties = new JButton[0];

    private ASchwimmer[] swimmers = new ASchwimmer[0];

    private boolean[] statusParticipate = new boolean[0];
    private boolean[] statusSN = new boolean[0];
    private boolean[] statusDN = new boolean[0];
    private boolean[] statusTime = new boolean[0];
    private ActionListener disciplineListener = null;
    private MoreInputListener moreInputListener = null;

    private int disciplineNumber = 0;
    private boolean byTimes = true;

    public PTimeInputPlugin() {
        super();
    }

    void updateInputPanel() {
        int length = 0;
        if (amount.getSelectedIndex() > -1) {
            length += (Integer) amount.getSelectedItem();
        }
        boolean byTimesNew = FormelManager.getInstance().get(core.getWettkampf().getRegelwerk().getFormelID())
                .getDataType().equals(DataType.TIME);
        if (length == names.length && byTimesNew == byTimes) {
            return;
        }

        byTimes = byTimesNew;

        FormLayout layout;
        if (byTimes) {
            layout = new FormLayout(
                    "4dlu,fill:default,4dlu,fill:default:grow,"
                            + "4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,"
                            + "4dlu,fill:default,4dlu,center:default,4dlu,fill:default,4dlu",
                    FormLayoutUtils.createLayoutString(1 + length));
            layout.setColumnGroups(new int[][] { { 12, 14, 18 } });
        } else {
            layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,"
                    + "4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,"
                    + "0dlu,0dlu,4dlu,center:default,4dlu,fill:default,4dlu",
                    FormLayoutUtils.createLayoutString(1 + length));
            layout.setColumnGroups(new int[][] { { 12, 18 } });
        }
        inputPanel.removeAll();
        inputPanel.setLayout(layout);

        inputPanel.add(startnumber, CC.xy(2, 2, "center,center"));
        inputPanel.add(name, CC.xy(4, 2, "center,center"));
        inputPanel.add(gliederung, CC.xy(6, 2, "center,center"));
        inputPanel.add(agegroup, CC.xy(8, 2, "center,center"));
        inputPanel.add(disciplineLabel, CC.xy(10, 2, "center,center"));
        inputPanel.add(input, CC.xy(12, 2, "center,center"));
        if (byTimes) {
            inputPanel.add(time, CC.xy(14, 2, "center,center"));
        }
        inputPanel.add(penalty, CC.xy(16, 2, "center,center"));

        JIntegerField[] startnumbersNew = new JIntegerField[length];
        JLabel[] namesNew = new JLabel[length];
        JLabel[] gliederungenNew = new JLabel[length];
        JLabel[] agegroupsNew = new JLabel[length];
        JLabel[] disciplinesNew = new JLabel[length];
        JIntegerField[] inputsNew = new JIntegerField[length];
        KeyListener[] klNew = new KeyListener[length];
        DocumentListener[] dlNew = new DocumentListener[length];
        FocusListener[] flNew = new FocusListener[length];
        JTimeField[] timesNew = new JTimeField[length];
        JLabel[] penaltiesTextNew = new JLabel[length];
        JButton[] penaltiesNew = new JButton[length];
        ASchwimmer[] swimmersNew = new ASchwimmer[length];
        boolean[] statusSNNew = new boolean[length];
        boolean[] statusDNNew = new boolean[length];
        boolean[] statusTimeNew = new boolean[length];
        boolean[] statusParticipateNew = new boolean[length];

        for (int x = 0; x < length; x++) {
            if (names.length > x) {
                startnumbersNew[x] = startnumbers[x];
                namesNew[x] = names[x];
                gliederungenNew[x] = gliederungen[x];
                agegroupsNew[x] = agegroups[x];
                disciplinesNew[x] = disciplines[x];
                inputsNew[x] = inputs[x];
                klNew[x] = kl[x];
                dlNew[x] = dl[x];
                flNew[x] = fl[x];
                timesNew[x] = times[x];
                swimmersNew[x] = swimmers[x];
                statusSNNew[x] = statusSN[x];
                statusDNNew[x] = statusDN[x];
                statusParticipateNew[x] = statusParticipate[x];
                statusTimeNew[x] = statusTime[x];
                penaltiesNew[x] = penalties[x];
                penaltiesTextNew[x] = penaltiestext[x];

                if (byTimes) {
                    inputsNew[x].setValidator((Validator) value -> {
                        value = value / 100;
                        if ((value % 100) >= 60) {
                            return false;
                        }
                        value = value / 100;
                        return value < 1000;
                    });
                    inputsNew[x].setToolTipText(I18n.getToolTip("TimeInputField"));
                } else {
                    inputsNew[x].setValidator((JIntegerField.Validator) null);
                    inputsNew[x].setToolTipText(I18n.getToolTip("RankInputField"));
                }
            } else {
                StartnumberListener sl = new StartnumberListener(x);
                klNew[x] = new TimeListener(x);
                dlNew[x] = new SessionedDocumentListener(
                        new TimeInputListener(new DirectTimeInputAdapter(x), this, controller));
                flNew[x] = new HighPointsListener(x);

                startnumbersNew[x] = new JIntegerField();
                startnumbersNew[x].setToolTipText(I18n.getToolTip("StartnumberInput"));
                startnumbersNew[x].addKeyListener(moreInputListener);
                startnumbersNew[x].addKeyListener(sl);
                startnumbersNew[x].getDocument().addDocumentListener(sl);
                startnumbersNew[x].setAutoSelectAll(true);
                startnumbersNew[x].setHorizontalAlignment(SwingConstants.RIGHT);
                namesNew[x] = new JLabel();
                gliederungenNew[x] = new JLabel();
                agegroupsNew[x] = new JLabel();
                disciplinesNew[x] = new JLabel();
                inputsNew[x] = new JIntegerField(JIntegerField.EMPTY_FIELD, JTimeField.MAX_TIME, false, true);
                if (byTimes) {
                    inputsNew[x].setValidator((Validator) value -> {
                        value = value / 100;
                        if ((value % 100) >= 60) {
                            return false;
                        }
                        value = value / 100;
                        return value < 1000;
                    });
                    inputsNew[x].setToolTipText(I18n.getToolTip("TimeInputField"));
                }
                inputsNew[x].setAutoSelectAll(true);
                inputsNew[x].setHorizontalAlignment(SwingConstants.RIGHT);
                inputsNew[x].addKeyListener(moreInputListener);
                inputsNew[x].getDocument().addDocumentListener(dlNew[x]);
                inputsNew[x].addKeyListener(klNew[x]);
                inputsNew[x].addFocusListener(flNew[x]);
                timesNew[x] = new JTimeField(inputsNew[x]);
                penaltiesTextNew[x] = new JLabel();
                penaltiesNew[x] = new JButton(I18n.get("Penalty"));
                penaltiesNew[x].setToolTipText(I18n.getToolTip("PenaltyButton"));
                penaltiesNew[x].addActionListener(new PenaltyListener(x));
                penaltiesNew[x].addKeyListener(new PenaltyKeyListener(x));
                swimmersNew[x] = null;
                statusSNNew[x] = true;
                statusDNNew[x] = true;
                statusParticipateNew[x] = true;
                statusTimeNew[x] = true;

                inputsNew[x].addFocusListener(flx);
                startnumbersNew[x].addFocusListener(flx);
            }
            inputPanel.add(startnumbersNew[x], CC.xy(2, 4 + (2 * x)));
            inputPanel.add(namesNew[x], CC.xy(4, 4 + (2 * x)));
            inputPanel.add(gliederungenNew[x], CC.xy(6, 4 + (2 * x)));
            inputPanel.add(agegroupsNew[x], CC.xy(8, 4 + (2 * x)));
            inputPanel.add(disciplinesNew[x], CC.xy(10, 4 + (2 * x)));
            inputPanel.add(inputsNew[x], CC.xy(12, 4 + (2 * x)));
            if (byTimes) {
                inputPanel.add(timesNew[x], CC.xy(14, 4 + (2 * x)));
            }
            inputPanel.add(penaltiesTextNew[x], CC.xy(16, 4 + (2 * x)));
            inputPanel.add(penaltiesNew[x], CC.xy(18, 4 + (2 * x)));
        }

        int old = names.length;

        startnumbers = startnumbersNew;
        names = namesNew;
        gliederungen = gliederungenNew;
        agegroups = agegroupsNew;
        disciplines = disciplinesNew;
        inputs = inputsNew;
        kl = klNew;
        dl = dlNew;
        fl = flNew;
        times = timesNew;
        penaltiestext = penaltiesTextNew;
        penalties = penaltiesNew;
        swimmers = swimmersNew;
        statusTime = statusTimeNew;
        statusSN = statusSNNew;
        statusDN = statusDNNew;
        statusParticipate = statusParticipateNew;

        for (int x = old; x < names.length; x++) {
            updateRow(x);
        }
    }

    private void initVariables() {
        disciplineListener = new DisciplineListener();
        moreInputListener = new MoreInputListener();

        startnumber = new JLabel(I18n.get("Startnumber"));
        name = new JLabel(I18n.get("Name"));
        gliederung = new JLabel(I18n.get("Organisation"));
        agegroup = new JLabel(I18n.get("AgeGroup"));
        disciplineLabel = new JLabel(I18n.get("Discipline"));
        input = new JLabel(TITLE_INPUT);
        time = new JLabel(I18n.get("Time"));
        penalty = new JLabel(I18n.get("Penalty"));

        discipline = new JComboBox<>();
        discipline.setToolTipText(I18n.getToolTip("SelectNumberOfDiscipline"));
        amount = new JComboBox<>(new Integer[] { 5, 10, 15, 20, 25, 30, 40, 50 });
        amount.addActionListener(new AmountActionListener());
        amount.setToolTipText(I18n.getToolTip("NumberOfInputLines"));
        more = new JButton(I18n.get("More"));
        more.setToolTipText(I18n.getToolTip("PrepareForNewInput"));
        more.addActionListener(e -> {
            prepareMoreInput();
        });

        flx = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                int miny = 0;
                int height = 0;
                if (startnumbers.length > 1) {
                    miny = startnumbers[0].getY();
                    height = startnumbers[1].getY() - miny;
                }

                Rectangle r = new Rectangle();
                scroller.computeVisibleRect(r);

                JScrollBar bar = scroller.getVerticalScrollBar();
                int min = bar.getValue();
                int max = min + r.height;

                Component c = e.getComponent();
                if (c.getY() - height + 1 < min) {
                    int pos = c.getY() - height + 1;
                    if (pos <= miny) {
                        pos = 0;
                    }
                    bar.setValue(pos);
                } else if (c.getY() + 2 * c.getHeight() > max) {
                    int diff = c.getY() + 2 * c.getHeight() - max;
                    bar.setValue(min + diff);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Nothing to do
            }
        };
    }

    void prepareMoreInput() {
        if (more.isEnabled()) {
            for (int x = 0; x < names.length; x++) {
                startnumbers[x].setText("");
            }
        }
        startnumbers[0].requestFocus();
    }

    private final class HighPointsListener extends FocusAdapter {

        private int index = 0;

        private String data = "";

        public HighPointsListener(int x) {
            index = x;
        }

        @Override
        public synchronized void focusLost(FocusEvent e) {
            if (swimmers[index] == null) {
                return;
            }
            if (inputs[index].getText().equals(data)) {
                return;
            }
            data = inputs[index].getText();
            if (!SchwimmerUtils.checkTimeAndNotify(controller.getWindow(), swimmers[index], disciplineNumber)) {
                inputs[index].requestFocus();
            }
        }
    }

    class ScrollableJPanel extends JPanel implements Scrollable {

        private static final long serialVersionUID = 680058034328664232L;

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 0;
            // return getScrollableUnitIncrement(visibleRect, orientation,
            // direction);
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            if (orientation == SwingConstants.HORIZONTAL) {
                return 0;
            }
            if ((names == null) || (names.length <= 1)) {
                return 0;
            }

            Component[] cs = getComponents();
            Component c = null;
            for (Component c1 : cs) {
                if (c1.hasFocus()) {
                    c = c1;
                    break;
                }
            }

            JLabel a = names[0];
            JLabel b = names[1];
            int diff = b.getY() - a.getY();
            // int space = diff - a.getHeight();

            if (c == null) {
                // No Component has focus, so we use default size;
                return diff;
            }
            if (direction > 0) {
                if (!visibleRect.contains(c.getX(), c.getY()) || !visibleRect.contains(c.getX(), c.getY() + 2 * diff)) {
                    return diff;
                }
            } else {
                if (!visibleRect.contains(c.getX(), c.getY() - diff)
                        || !visibleRect.contains(c.getX(), c.getY() + diff)) {
                    return diff;
                }
            }
            return 0;
        }

    }

    void initPanel() {
        initVariables();
        inputPanel = new ScrollableJPanel();

        scroller = new JScrollPane(inputPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getVerticalScrollBar().setUnitIncrement(30);
        scroller.getHorizontalScrollBar().setUnitIncrement(30);
        scroller.setBorder(null);

        JPanel in = new JPanel();
        in.setBorder(BorderUtils.createLabeledBorder(TITLE_INPUT));
        // in.setLayout(new BorderLayout(0, 0));
        // in.add(scroller);
        in.setLayout(new FormLayout("0dlu,fill:default:grow,0dlu", "0dlu,fill:default,0dlu"));
        in.add(scroller, CC.xy(2, 2));

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default:grow,4dlu");
        layout.setColumnGroups(new int[][] { { 2, 4 } });
        JPanel innerPanel = new JPanel(layout);
        innerPanel.add(createTopLeftPanel(), CC.xy(2, 2));
        innerPanel.add(createTopRightPanel(), CC.xy(4, 2));
        innerPanel.add(in, CC.xyw(2, 4, 3));

        JPanel panel = new JPanel(new MigLayout("", "[fill, grow, ::1200lp]", "[fill]"));
        panel.add(innerPanel);

        JPanel infopanel = new JPanel();
        infopanel.setName("Info");
        infopanel.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information")));
        infopanel.add(new JLabel(I18n.get("TimeInputInfo")));

        main = new JGlassPanel<>(panel);
        main.setName(TITLE_INPUT);
        main.setEnabled(false);

        JPanel glass = main.getGlassPanel();
        layout = new FormLayout("4dlu:grow,fill:default,4dlu:grow", "4dlu:grow,fill:default,4dlu:grow");
        layout.setRowGroups(new int[][] { { 1, 3 } });
        layout.setColumnGroups(new int[][] { { 1, 3 } });
        glass.setLayout(layout);
        glass.add(infopanel, CC.xy(2, 2));
    }

    private JPanel createTopLeftPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4 } });
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Selection")));

        top.add(new JLabel(I18n.get("Discipline")), CC.xy(2, 2));
        top.add(new JLabel(I18n.get("Amount")), CC.xy(2, 4));

        top.add(discipline, CC.xy(4, 2));
        top.add(amount, CC.xy(4, 4));
        top.add(more, CC.xy(4, 6, "right,center"));

        return top;
    }

    private JPanel createTopRightPanel() {
        messages = new MessagePanel(I18n.get("ErrorsOccured"), I18n.get("Ok"),
                new String[] { I18n.get("TimesIncorrect"), I18n.get("StartnumbersIncorrect"),
                        I18n.get("SwimmerHasLessDisciplines"), I18n.get("DoesNotParticipate") });
        messages.setBorder(BorderUtils.createLabeledBorder(I18n.get("Status")));
        return messages;
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return new PanelInfo[] { new PanelInfo(TITLE_INPUT, IconManager.getBigIcon("timeinput"), true, false, 340) {

            @Override
            public JPanel getPanelI() {
                if (main == null) {
                    initPanel();
                    updateInputPanel();
                    dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED);
                }
                return main;
            }
        } };
    }

    @Override
    public void setController(IPluginManager controller, String uid) {
        this.controller = controller;
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", uid);
        editor = (FEditorPlugin) controller.getFeature("de.df.jauswertung.editor", uid);
    }

    @Override
    public synchronized void dataUpdated(UpdateEvent due) {
        if (main == null) {
            return;
        }
        if (due.isSource(this)) {
            return;
        }
        boolean byTimesNew = FormelManager.getInstance().get(core.getWettkampf().getRegelwerk().getFormelID())
                .getDataType().equals(DataType.TIME);
        if (byTimesNew != byTimes) {
            updateAmount();
        }
        updateGUI();
    }

    void updateGUI() {
        if (core.getWettkampf().getSchwimmeranzahl() > 0 && !core.getWettkampf().isHeatBased()) {
            main.setEnabled(true);

            Regelwerk aks = core.getWettkampf().getRegelwerk();
            int number = aks.getMaxDisciplineCount();
            if (number != discipline.getItemCount()) {
                discipline.removeActionListener(disciplineListener);
                discipline.removeAllItems();
                for (int x = 1; x <= number; x++) {
                    discipline.addItem(x);
                }
                if ((disciplineNumber >= 0) && (disciplineNumber < number)) {
                    discipline.setSelectedIndex(disciplineNumber);
                    discipline.addActionListener(disciplineListener);
                } else {
                    disciplineNumber = 0;
                    discipline.addActionListener(disciplineListener);
                    discipline.setSelectedIndex(disciplineNumber);
                }
            }
            for (int x = 0; x < names.length; x++) {
                updateRow(x);
                updateTime(x);
            }
        } else {
            main.setEnabled(false);
        }
    }

    synchronized void updateRow(int index) {
        updateRow(index, true);
    }

    synchronized void updateRow(int index, boolean updateInput) {
        names[index].setForeground(penaltiestext[index].getForeground());

        statusDN[index] = true;
        statusSN[index] = true;
        statusTime[index] = true;
        statusParticipate[index] = true;

        // Test for valid number
        if (!startnumbers[index].isValidInt()) {
            names[index].setText(I18n.get("NoNumberFormat"));
            setInput(index, "", updateInput);
            gliederungen[index].setText("");
            agegroups[index].setText("");
            disciplines[index].setText("");
            inputs[index].setEnabled(false);
            statusSN[index] = false;
            swimmers[index] = null;
            updateStatus();
            return;
        }

        ASchwimmer s = null;
        if (core != null) {
            s = SearchUtils.getSchwimmer(core.getWettkampf(), startnumbers[index].getInt());
        }
        swimmers[index] = null;
        if (s != null) {
            // Search for duplicates
            for (ASchwimmer swimmer : swimmers) {
                if (swimmer == s) {
                    names[index].setText(I18n.get("AlreadyShown"));
                    names[index].setForeground(Color.RED);
                    setInput(index, "", updateInput);
                    penaltiestext[index].setText("");
                    gliederungen[index].setText("");
                    agegroups[index].setText("");
                    disciplines[index].setText("");
                    inputs[index].setEnabled(false);
                    penalties[index].setEnabled(false);
                    statusSN[index] = false;
                    updateStatus();
                    return;
                }
            }

            // Setup UI
            swimmers[index] = s;
            names[index].setText(s.getName());
            gliederungen[index].setText(s.getGliederung());
            agegroups[index].setText(I18n.getAgeGroupAsString(s));
            if (s.getAK().getDiszAnzahl() > discipline.getSelectedIndex()) {
                disciplines[index]
                        .setText(s.getAK().getDisziplin(discipline.getSelectedIndex(), s.isMaennlich()).getName());
                // Swimmer has this discipline
                if (s.isDisciplineChosen(discipline.getSelectedIndex())) {
                    int z = s.getZeit(discipline.getSelectedIndex());
                    if (z > 0) {
                        setTime(index, z, updateInput);
                    } else {
                        setInput(index, "", updateInput);
                    }
                    penaltiestext[index].setText(PenaltyUtils
                            .getPenaltyShortText(s.getAkkumulierteStrafe(discipline.getSelectedIndex()), s.getAK()));
                    inputs[index].setEnabled(true);
                    penalties[index].setEnabled(true);
                } else {
                    // Swimmer has not chosen this discipline
                    setInput(index, "", updateInput);
                    penaltiestext[index].setText("");
                    inputs[index].setEnabled(false);
                    penalties[index].setEnabled(false);
                    statusParticipate[index] = false;
                }
            } else {
                // Swimmer has less disciplines
                setInput(index, "", updateInput);
                disciplines[index].setText("");
                penaltiestext[index].setText("");
                inputs[index].setEnabled(false);
                penalties[index].setEnabled(false);
                statusDN[index] = false;
            }
            statusSN[index] = true;
        } else {
            if (startnumbers[index].getText().length() > 0) {
                // startnumber not found
                names[index].setText(I18n.get("NotFound"));
                statusSN[index] = false;
            } else {
                // no startnumber entered
                names[index].setText("");
                statusSN[index] = true;
            }
            setInput(index, "", updateInput);
            penaltiestext[index].setText("");
            gliederungen[index].setText("");
            agegroups[index].setText("");
            disciplines[index].setText("");
            inputs[index].setEnabled(false);
            penalties[index].setEnabled(false);
        }

        updateStatus();
    }

    synchronized void updateTime(int index) {
        if (byTimes) {
            statusTime[index] = (times[index].isValidValue());
        } else {
            statusTime[index] = (inputs[index].isValidInt());
        }
        updateStatus();
    }

    private void updateStatus() {
        int status = 0;
        for (int x = 0; x < names.length; x++) {
            if (!statusParticipate[x]) {
                status = status | 8;
            }
            if (!statusDN[x]) {
                status = status | 4;
            }
            if (!statusSN[x]) {
                status = status | 2;
            }
            if (!statusTime[x]) {
                status = status | 1;
            }
        }
        if (status % 2 >= 1) {
            messages.showMessage(0);
        } else {
            messages.hideMessage(0);
        }
        if (status % 4 >= 2) {
            messages.showMessage(1);
        } else {
            messages.hideMessage(1);
        }
        if (status % 8 >= 4) {
            messages.showMessage(2);
        } else {
            messages.hideMessage(2);
        }
        if (status % 16 >= 8) {
            messages.showMessage(3);
        } else {
            messages.hideMessage(3);
        }
        more.setEnabled(status == 0);
    }

    void updatePanel() {
        for (int x = 0; x < names.length; x++) {
            updateRow(x);
            updateTime(x);
        }
    }

    void updateAmount() {
        updateInputPanel();
        // panel.updateUI();
    }

    private int currentRow() {
        for (int x = 0; x < inputs.length; x++) {
            if (inputs[x].hasFocus() || startnumbers[x].hasFocus() || penalties[x].hasFocus()) {
                return x;
            }
        }
        return 0;
    }

    void nextRow() {
        nextRow(currentRow());
    }

    void nextRow(int index) {
        index++;
        if (index < inputs.length) {
            startnumbers[index].requestFocus();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    void previousRow(int index) {
        index--;
        if (index >= 0) {
            startnumbers[index].requestFocus();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    void changeTime(int index) {
        updateTime(index);
        if (statusTime[index]) {
            ASchwimmer s = swimmers[index];
            if (s != null) {
                if (s.getAK().getDiszAnzahl() > discipline.getSelectedIndex()) {
                    if (byTimes) {
                        s.setZeit(disciplineNumber, times[index].getTimeAsInt());
                    } else {
                        s.setZeit(disciplineNumber, inputs[index].getInt());
                    }
                    updateRow(index, false);
                    controller.sendDataUpdateEvent("ChangeTime", UpdateEventConstants.REASON_POINTS_CHANGED,
                            swimmers[index], disciplineNumber,
                            PTimeInputPlugin.this);
                }
            }
        }
    }

    private synchronized void setInput(int x, String value, boolean update) {
        if (!update) {
            return;
        }
        if (!value.equals(inputs[x].getText())) {
            inputs[x].getDocument().removeDocumentListener(dl[x]);
            inputs[x].setText(value);
            inputs[x].getDocument().addDocumentListener(dl[x]);
        }
    }

    private synchronized void setTime(int x, int value, boolean update) {
        if (!update) {
            return;
        }
        if ((!inputs[x].isValidInt()) || (Math.abs(value - inputs[x].getDouble()) > 0.005)) {
            inputs[x].getDocument().removeDocumentListener(dl[x]);
            if (byTimes) {
                times[x].setTimeAsInt(value);
            } else {
                inputs[x].setInt(value);
            }
            inputs[x].getDocument().addDocumentListener(dl[x]);
        }
    }

    final class MoreInputListener extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            if (e.isConsumed()) {
                return;
            }
            if (e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown() && !e.isShiftDown()) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    prepareMoreInput();
                    e.consume();
                    return;
                }
            }
            if (!e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown() && !e.isShiftDown()) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    nextRow();
                    e.consume();
                    return;
                }
            }
        }
    }

    final class AmountActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateAmount();
        }
    }

    final class DisciplineListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (discipline.getSelectedIndex() != disciplineNumber) {
                disciplineNumber = discipline.getSelectedIndex();
                updatePanel();
            }
        }
    }

    final class TimeListener extends KeyAdapter {

        final int index;

        public TimeListener(int index) {
            this.index = index;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.isConsumed()) {
                return;
            }
            switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (index > 0) {
                    inputs[index - 1].requestFocus();
                }
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                if (index + 1 < inputs.length) {
                    inputs[index + 1].requestFocus();
                }
                e.consume();
                break;
            case KeyEvent.VK_LEFT:
                if (e.isControlDown()) {
                    startnumbers[index].requestFocus();
                    e.consume();
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (e.isControlDown()) {
                    penalties[index].requestFocus();
                    e.consume();
                }
                break;
            default:
                break;
            }
        }
    }

    private final class StartnumberListener implements DocumentListener, KeyListener {

        final int index;

        public StartnumberListener(int index) {
            this.index = index;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateRow(index);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            insertUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            insertUpdate(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // keyTyped(e);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // keyTyped(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.isConsumed()) {
                return;
            }
            switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (index > 0) {
                    startnumbers[index - 1].requestFocus();
                }
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                if (index + 1 < startnumbers.length) {
                    startnumbers[index + 1].requestFocus();
                }
                e.consume();
                break;
            case KeyEvent.VK_RIGHT:
                if (e.isControlDown()) {
                    inputs[index].requestFocus();
                    e.consume();
                }
                break;
            default:
                break;
            }
        }
    }

    private final class PenaltyKeyListener extends KeyAdapter {

        final int index;

        public PenaltyKeyListener(int index) {
            this.index = index;
        }

        private void setNoPenalty() {
            swimmers[index].setStrafen(disciplineNumber, new LinkedList<>());
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateRow(index, false);
                    controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY,
                            swimmers[index], disciplineNumber,
                            PTimeInputPlugin.this);
                }
            });
        }

        @Override
        public void keyReleased(KeyEvent evt) {
            char key = evt.getKeyChar();
            if (key == '+') {
                nextRow(index);
            } else if (key == '-') {
                previousRow(index);
            } else if (key == '#') {
                setNoPenalty();
            }
        }
    }

    private class PenaltyListener implements ActionListener {

        private int index = 0;

        public PenaltyListener(int x) {
            index = x;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            editor.runPenaltyEditor(core.getWettkampf(), swimmers[index], disciplineNumber);
        }
    }
}