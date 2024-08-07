/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.zwinput;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.tasks.JTaskPaneGroup;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JZWStatusPanel;
import de.df.jauswertung.gui.util.MessagePanel;
import de.df.jauswertung.io.util.ZWUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.JDoubleField;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.plaf.GradientTaskPaneGroupUI;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.util.StringTools;
import net.miginfocom.swing.MigLayout;

/**
 * @author Dennis Fabri
 * @since 27.03.2010
 */
class JSimpleInputPanel extends JPanel {

    private static final String INPUT = I18n.get("ZWInput");

    private final IPluginManager controller;
    private final CorePlugin core;
    private final PZWInputPlugin parent;

    private JPanel inputPanel = null;
    private JZWStatusPanel overview = null;

    private JComboBox<Integer> amount = null;
    private JButton more;
    private MessagePanel messages;

    private JLabel startNumber;
    private JLabel name;
    private JLabel gliederung;
    private JLabel agegroup;
    private JLabel input;

    private TimeListener[] dl = new TimeListener[0];
    private JWarningTextField[] startNumbers = new JWarningTextField[0];
    private JLabel[] names = new JLabel[0];
    private JLabel[] gliederungen = new JLabel[0];
    private JLabel[] ageGroups = new JLabel[0];
    private JDoubleField[] inputs = new JDoubleField[0];
    private ASchwimmer[] swimmers = new ASchwimmer[0];
    private int[] indizes = new int[0];

    private boolean[] statusSN = new boolean[0];
    private boolean[] statusTime = new boolean[0];
    private boolean[] statusZW = new boolean[0];

    private static final long OVERVIEW_REASONS = UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_LOAD_WK
            | UpdateEventConstants.REASON_NEW_LOAD_WK | UpdateEventConstants.REASON_NEW_TN
            | UpdateEventConstants.REASON_NEW_WK | UpdateEventConstants.REASON_PENALTY
            | UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_SWIMMER_CHANGED
            | UpdateEventConstants.REASON_SWIMMER_DELETED;

    private MoreInputListener moreInputListener = null;

    JSimpleInputPanel(PZWInputPlugin parent, IPluginManager controller, CorePlugin core) {
        this.controller = controller;
        this.core = core;
        this.parent = parent;
        initVariables();
        initPanel();
        updateInputPanel();
    }

    private void updateInputPanel() {
        int length = 0;
        if (amount.getSelectedIndex() > -1) {
            length = (Integer) amount.getSelectedItem();
        }
        if (length == names.length) {
            return;
        }
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,"
                + "4dlu,fill:default:grow,4dlu,fill:default," + "4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(1 + length));
        inputPanel.removeAll();
        inputPanel.setLayout(layout);

        inputPanel.add(startNumber, CC.xy(2, 2, "center,center"));
        inputPanel.add(name, CC.xy(4, 2, "center,center"));
        inputPanel.add(gliederung, CC.xy(6, 2, "center,center"));
        inputPanel.add(agegroup, CC.xy(8, 2, "center,center"));
        inputPanel.add(input, CC.xy(10, 2, "center,center"));

        JWarningTextField[] startNumbersNew = new JWarningTextField[length];
        JLabel[] namesNew = new JLabel[length];
        JLabel[] gliederungenNew = new JLabel[length];
        JLabel[] ageGroupsNew = new JLabel[length];
        JDoubleField[] inputsNew = new JDoubleField[length];
        TimeListener[] dlNew = new TimeListener[length];
        ASchwimmer[] swimmersNew = new ASchwimmer[length];
        int[] indizesNew = new int[length];
        boolean[] statusSNNew = new boolean[length];
        boolean[] statusTimeNew = new boolean[length];
        boolean[] statusZWNew = new boolean[length];

        Strafe s1 = core.getWettkampf().getStrafen().getNichtAngetreten();
        Strafe s2 = core.getWettkampf().getStrafen().getDisqualifiziert();
        String[] specialStrings;
        LinkedList<String> strings = new LinkedList<>();
        strings.add(I18n.get("DidNotStartShort"));
        strings.add("n");
        strings.add(I18n.get("DisqualificationShort"));
        strings.add("d");
        if (!s1.getShortname().isEmpty()) {
            strings.add(s1.getShortname());
        }
        if (!s2.getShortname().isEmpty()) {
            strings.add(s2.getShortname());
        }
        specialStrings = strings.toArray(new String[0]);

        for (int x = 0; x < length; x++) {
            if (names.length > x) {
                startNumbersNew[x] = startNumbers[x];
                namesNew[x] = names[x];
                gliederungenNew[x] = gliederungen[x];
                ageGroupsNew[x] = ageGroups[x];
                inputsNew[x] = inputs[x];
                swimmersNew[x] = swimmers[x];
                indizesNew[x] = indizes[x];
                statusSNNew[x] = statusSN[x];
                statusTimeNew[x] = statusTime[x];
                statusZWNew[x] = statusZW[x];
                dlNew[x] = dl[x];
            } else {
                startNumbersNew[x] = new JWarningTextField(false, true);
                startNumbersNew[x].setValidator(value -> {
                    if ((value == null) || (value.isBlank())) {
                        return true;
                    }
                    value = value.trim();
                    if (StringTools.isInteger(value)) {
                        return true;
                    }
                    return (ZWUtils.getZWIndex(core.getWettkampf(), value) >= 0)
                           && (ZWUtils.getZWStartnummer(core.getWettkampf(), value) >= 0);
                });
                startNumbersNew[x].setToolTipText(I18n.getToolTip("StartnumberInput"));
                startNumbersNew[x].addKeyListener(moreInputListener);
                StartnumberListener sl = new StartnumberListener(x);
                startNumbersNew[x].addKeyListener(sl);
                startNumbersNew[x].getDocument().addDocumentListener(sl);
                startNumbersNew[x].setAutoSelectAll(true);
                startNumbersNew[x].setHorizontalAlignment(SwingConstants.RIGHT);
                namesNew[x] = new JLabel();
                gliederungenNew[x] = new JLabel();
                ageGroupsNew[x] = new JLabel();
                dlNew[x] = new TimeListener(x);
                inputsNew[x] = new JDoubleField();
                inputsNew[x].setToolTipText(I18n.getToolTip("ZWPointsInput"));
                inputsNew[x].addKeyListener(moreInputListener);
                inputsNew[x].addKeyListener(dlNew[x]);
                inputsNew[x].setSpecialStrings(specialStrings);
                inputsNew[x].getDocument().addDocumentListener(dlNew[x]);
                inputsNew[x].setAutoSelectAll(true);
                inputsNew[x].setHorizontalAlignment(SwingConstants.RIGHT);
                swimmersNew[x] = null;
                indizesNew[x] = -1;
                statusSNNew[x] = true;
                statusTimeNew[x] = true;
                statusZWNew[x] = true;
            }
            inputPanel.add(startNumbersNew[x], CC.xy(2, 4 + (2 * x)));
            inputPanel.add(namesNew[x], CC.xy(4, 4 + (2 * x)));
            inputPanel.add(gliederungenNew[x], CC.xy(6, 4 + (2 * x)));
            inputPanel.add(ageGroupsNew[x], CC.xy(8, 4 + (2 * x)));
            inputPanel.add(inputsNew[x], CC.xy(10, 4 + (2 * x)));
        }

        int old = names.length;

        startNumbers = startNumbersNew;
        names = namesNew;
        gliederungen = gliederungenNew;
        ageGroups = ageGroupsNew;
        inputs = inputsNew;
        swimmers = swimmersNew;
        indizes = indizesNew;
        statusTime = statusTimeNew;
        statusZW = statusZWNew;
        statusSN = statusSNNew;
        dl = dlNew;

        for (int x = old; x < names.length; x++) {
            updateRow(x);
        }
    }

    private void initVariables() {
        moreInputListener = new MoreInputListener();

        startNumber = new JLabel(I18n.get("Startnumber"));
        name = new JLabel(I18n.get("Name"));
        gliederung = new JLabel(I18n.get("Organisation"));
        agegroup = new JLabel(I18n.get("AgeGroup"));
        input = new JLabel(I18n.get("ZWPoints"));

        amount = new JComboBox<>(new Integer[] { 5, 10, 15, 20, 25, 30, 40, 50 });
        amount.setToolTipText(I18n.getToolTip("NumberOfInputLines"));
        amount.addActionListener(new AmountActionListener());
        more = new JButton(I18n.get("More"));
        more.setToolTipText(I18n.getToolTip("PrepareForNewInput"));
        more.addActionListener(e -> {
            prepareMoreInput();
        });
    }

    private void initPanel() {
        initVariables();
        inputPanel = new ScrollableJPanel();

        JScrollPane scroller = new JScrollPane(inputPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getVerticalScrollBar().setUnitIncrement(10);
        scroller.getHorizontalScrollBar().setUnitIncrement(10);
        scroller.setBorder(null);

        JPanel in = new JPanel();
        in.setBorder(BorderUtils.createLabeledBorder(INPUT));
        // in.setLayout(new BorderLayout(0, 0));
        // in.add(scroller);
        in.setLayout(new FormLayout("0dlu,fill:default:grow,0dlu", "0dlu,fill:default,0dlu"));
        in.add(scroller, CC.xy(2, 2));

        JTaskPaneGroup over = new JTaskPaneGroup();
        over.setUI(new GradientTaskPaneGroupUI());
        over.setTitle(I18n.get("Overview"));
        // jt.setBorder(new ShadowLabeledBorder(names[x]));
        over.setBackground(in.getBackground());
        over.setOpaque(false);

        // over.setBorder(new ShadowBorder());
        overview = new JZWStatusPanel();
        over.add(overview);

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu");
        layout.setColumnGroups(new int[][] { { 2, 4 } });
        JPanel panel = new JPanel(layout);
        panel.add(createPropertiesPanel(), CC.xy(2, 2));
        panel.add(createMessagePanel(), CC.xy(4, 2));
        panel.add(over, CC.xyw(2, 4, 3));
        panel.add(in, CC.xyw(2, 6, 3));

        setLayout(new MigLayout("", "[fill, grow, ::1200lp]", "[fill]"));
        add(panel);
    }

    private JPanel createPropertiesPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu:grow");
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Selection")));

        top.add(new JLabel(I18n.get("Amount")), CC.xy(2, 2));

        top.add(amount, CC.xy(4, 2));
        top.add(more, CC.xy(4, 4, "right,center"));

        return top;
    }

    private JPanel createMessagePanel() {
        messages = new MessagePanel(I18n.get("ErrorsOccured"), I18n.get("Ok"),
                new String[] { I18n.get("StartnumbersIncorrect"), I18n.get("ZWIncorrect") });
        messages.setBorder(BorderUtils.createLabeledBorder(I18n.get("Status")));
        return messages;
    }

    public synchronized void dataUpdated(UpdateEvent due) {
        if ((due.getChangeReason() & OVERVIEW_REASONS) > 0) {
            overview.setData(core.getWettkampf());
        }
        if (due.isSource(parent)) {
            return;
        }
        updateGUI();
    }

    private void updateGUI() {
        if (core.getWettkampf().hasHLW()) {
            setEnabled(true);

            Regelwerk aks = core.getWettkampf().getRegelwerk();
            int number = 0;
            for (int x = 0; x < aks.size(); x++) {
                number = Math.max(number, aks.getAk(x).getDiszAnzahl());
            }
            for (int x = 0; x < names.length; x++) {
                updateRow(x);
                updateZW(x);
            }
        } else {
            setEnabled(false);
        }
    }

    private synchronized void updateRow(int index) {
        statusZW[index] = true;
        if (!startNumbers[index].isValidString()) {
            swimmers[index] = null;
            indizes[index] = -1;
            names[index].setText(I18n.get("NoNumberFormat"));
            setInput(index, "");
            gliederungen[index].setText("");
            ageGroups[index].setText("");
            inputs[index].setEnabled(false);
            statusSN[index] = false;
            updateStatus();
            return;
        }
        ASchwimmer s = null;
        int i = -1;
        if (core != null) {
            int sn = ZWUtils.getZWStartnummer(core.getWettkampf(), startNumbers[index].getText());
            i = ZWUtils.getZWIndex(core.getWettkampf(), startNumbers[index].getText());
            if ((sn >= 0) && (i >= 0)) {
                s = SearchUtils.getSchwimmer(core.getWettkampf(), sn);
            }
        }
        swimmers[index] = null;
        indizes[index] = -1;
        if ((s != null) && (i < s.getMaximaleHLW())) {
            for (int x = 0; x < swimmers.length; x++) {
                if ((swimmers[x] == s) && (indizes[x] == i)) {
                    names[index].setText(I18n.get("AlreadyShown"));
                    setInput(index, "");
                    gliederungen[index].setText("");
                    ageGroups[index].setText("");
                    inputs[index].setEnabled(false);
                    statusSN[index] = false;
                    updateStatus();
                    return;
                }
            }
            swimmers[index] = s;
            indizes[index] = i;
            names[index].setText(getExtendedName(s, i));
            gliederungen[index].setText(s.getGliederung());
            ageGroups[index].setText(I18n.getAgeGroupAsString(s));
            if (s.getAK().hasHLW()) {
                switch (s.getHLWState(i)) {
                case ENTERED:
                    setInput(index, s.getHLWPunkte(i));
                    break;
                case NICHT_ANGETRETEN: {
                    Strafe str = core.getWettkampf().getStrafen().getNichtAngetreten();
                    String text = I18n.get("DidNotStartShort");
                    if (!str.getShortname().isEmpty()) {
                        text = str.getShortname();
                    }
                    setInput(index, text);
                    break;
                }
                case NOT_ENTERED:
                    setInput(index, "");
                    break;
                case DISQALIFIKATION: {
                    Strafe str = core.getWettkampf().getStrafen().getNichtAngetreten();
                    String text = I18n.get("DisqualificationShort");
                    if (!str.getShortname().isEmpty()) {
                        text = str.getShortname();
                    }
                    setInput(index, text);
                    break;
                }
                }
                inputs[index].setEnabled(true);
                statusZW[index] = true;
            } else {
                setInput(index, "");
                inputs[index].setEnabled(false);
                statusZW[index] = false;
            }
            statusSN[index] = true;
        } else {
            if (!startNumbers[index].getText().isEmpty()) {
                names[index].setText(I18n.get("NotFound"));
                statusSN[index] = false;
            } else {
                names[index].setText("");
                statusSN[index] = true;
            }
            setInput(index, "");
            gliederungen[index].setText("");
            ageGroups[index].setText("");
            inputs[index].setEnabled(false);
            statusZW[index] = true;
        }

        updateStatus();
    }

    private static String getExtendedName(ASchwimmer s, int i) {
        StringBuilder extname = new StringBuilder();
        extname.append(s.getName());
        if (s.getMaximaleHLW() > 1) {
            boolean ok = false;
            if (s instanceof Mannschaft m) {
                String member = m.getMitgliedsname(i);
                if (!member.isEmpty()) {
                    extname.append(" (");
                    extname.append(StringTools.ABC[i]);
                    extname.append(": ");
                    extname.append(member);
                    extname.append(")");
                    ok = true;
                }
            }
            if (!ok) {
                extname.append(" (");
                extname.append(StringTools.ABC[i]);
                extname.append(")");
            }
        }
        return extname.toString();
    }

    private synchronized void updateZW(int index) {
        statusTime[index] = (inputs[index].isValidDouble() || inputs[index].isSpecialString());
        updateStatus();
    }

    private void updateStatus() {
        int status = 0;

        boolean validSN = true;
        boolean validZW = true;
        for (int x = 0; x < names.length; x++) {
            if (!statusSN[x]) {
                validSN = false;
                status++;
            }
            if (!statusZW[x]) {
                validZW = false;
                status++;
            }
        }
        if (!validSN) {
            messages.showMessage(0);
        } else {
            messages.hideMessage(0);
        }
        if (!validZW) {
            messages.showMessage(1);
        } else {
            messages.hideMessage(1);
        }
        more.setEnabled(status == 0);
    }

    private void updateAmount() {
        updateInputPanel();
    }

    private void prepareMoreInput() {
        if (more.isEnabled()) {
            for (int x = 0; x < names.length; x++) {
                startNumbers[x].setText("");
            }
        }
        startNumbers[0].requestFocus();
    }

    private int currentRow() {
        for (int x = 0; x < inputs.length; x++) {
            if (inputs[x].hasFocus()) {
                return x;
            }
            if (startNumbers[x].hasFocus()) {
                return x;
            }
        }
        return 0;
    }

    private void nextRow() {
        nextRow(currentRow());
    }

    private void nextRow(int index) {
        index++;
        if (index < inputs.length) {
            startNumbers[index].requestFocus();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void previousRow(int index) {
        index--;
        if (index >= 0) {
            startNumbers[index].requestFocus();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private synchronized void setInput(int x, String value) {
        if (!value.equals(inputs[x].getText())) {
            inputs[x].getDocument().removeDocumentListener(dl[x]);
            inputs[x].setText(value);
            inputs[x].getDocument().addDocumentListener(dl[x]);
        }
    }

    private synchronized void setInput(int x, double value) {
        if ((value < 0.005) || (!inputs[x].isValidDouble()) || (Math.abs(value - inputs[x].getDouble()) > 0.005)) {
            if (value > 0.005) {
                inputs[x].getDocument().removeDocumentListener(dl[x]);
                inputs[x].setDouble(value);
                inputs[x].getDocument().addDocumentListener(dl[x]);
            } else {
                setInput(x, "0");
            }
        }
    }

    private void quickEdit(int index, String value) {
        if (inputs[index].isEnabled()) {
            boolean doit = true;
            if (swimmers[index].hasHLWSet(indizes[index])) {
                if (!inputs[index].getText().equals(value)) {
                    HLWStates state = swimmers[index].getHLWState(indizes[index]);
                    String before = "";
                    switch (state) {
                    case ENTERED:
                        before = I18n.get("PointsPenalty",
                                "" + Math.round(swimmers[index].getHLWPunkte(indizes[index])));
                        break;
                    case DISQALIFIKATION:
                        before = I18n.get("DisqualificationShort");
                        break;
                    case NICHT_ANGETRETEN:
                        before = I18n.get("NotStartedShort");
                        break;
                    case NOT_ENTERED:
                        // Nothing to do
                        break;
                    }
                    doit = DialogUtils.ask(controller.getWindow(),
                            I18n.get("Question.InputAlreadyPresent", before, value),
                            I18n.get("Question.InputAlreadyPresent.Note", before, value));
                }
            }
            if (doit) {
                inputs[index].setText(value);
                if (inputs.length > index + 1) {
                    startNumbers[index + 1].requestFocus();
                }
            }
        }
    }

    private final class AmountActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateAmount();
        }
    }

    private final class TimeListener implements DocumentListener, KeyListener {

        final int index;

        public TimeListener(int index) {
            this.index = index;
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
                    startNumbers[index].requestFocus();
                    e.consume();
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (e.isControlDown()) {
                    e.consume();
                }
                break;
            case KeyEvent.VK_F4:
                quickEdit(index, "0");
                break;
            case KeyEvent.VK_F5:
                quickEdit(index, "200");
                break;
            case KeyEvent.VK_F3:
                quickEdit(index, "n");
                break;
            default:
                break;
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            String zeit = inputs[index].getText();
            if (zeit.contains("+")) {
                SwingUtilities.invokeLater(() -> {
                    String s = StringTools.removeAll(inputs[index].getText(), '+');
                    setInput(index, s);
                    SwingUtilities.invokeLater(() -> {
                        nextRow(index);
                    });
                });
                return;
            }
            if (zeit.contains("-")) {
                SwingUtilities.invokeLater(() -> {
                    String s = StringTools.removeAll(inputs[index].getText(), '-');
                    setInput(index, s);
                    SwingUtilities.invokeLater(() -> {
                        previousRow(index);
                    });
                });
                return;
            }
            updateZW(index);
            if (statusZW[index]) {
                if ((swimmers[index] != null)) {
                    if (!inputs[index].getText().isEmpty()) {
                        if (inputs[index].isValidDouble()) {
                            swimmers[index].setHLWPunkte(indizes[index], inputs[index].getDouble());
                        } else {
                            swimmers[index].setHLWState(indizes[index],
                                    ZWUtils.getHLWState(swimmers[index].getWettkampf(), zeit));
                        }
                    } else {
                        swimmers[index].setHLWState(indizes[index], HLWStates.NOT_ENTERED);
                    }
                }
                controller.sendDataUpdateEvent("ChangeZWPoints", UpdateEventConstants.REASON_POINTS_CHANGED,
                        swimmers[index], -1, parent);
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            insertUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            insertUpdate(e);
        }
    }

    private final class StartnumberListener implements DocumentListener, KeyListener {

        final int index;

        public StartnumberListener(int index) {
            this.index = index;
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
                    startNumbers[index - 1].requestFocus();
                }
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                if (index + 1 < inputs.length) {
                    startNumbers[index + 1].requestFocus();
                }
                e.consume();
                break;
            case KeyEvent.VK_LEFT:
                if (e.isControlDown()) {
                    e.consume();
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (e.isControlDown()) {
                    inputs[index].requestFocus();
                    e.consume();
                }
                break;
            case KeyEvent.VK_F4:
                quickEdit(index, "0");
                break;
            case KeyEvent.VK_F5:
                quickEdit(index, "200");
                break;
            case KeyEvent.VK_F3:
                quickEdit(index, "n");
                break;
            default:
                break;
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (startNumbers[index].getText().contains("+")) {
                SwingUtilities.invokeLater(() -> {
                    String s = StringTools.removeAll(startNumbers[index].getText(), '+');
                    startNumbers[index].setText(s);
                    SwingUtilities.invokeLater(() -> {
                        nextRow(index);
                    });
                });
                return;
            }
            if (startNumbers[index].getText().contains("-")) {
                SwingUtilities.invokeLater(() -> {
                    String s = StringTools.removeAll(startNumbers[index].getText(), '-');
                    startNumbers[index].setText(s);
                    SwingUtilities.invokeLater(() -> {
                        previousRow(index);
                    });
                });
                return;
            }
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
    }

    private final class MoreInputListener extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            if (e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown() && !e.isShiftDown()) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    prepareMoreInput();
                    return;
                }
            }
            if (!e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown() && !e.isShiftDown()) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    nextRow();
                    return;
                }
            }
        }
    }

    private class ScrollableJPanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return getScrollableUnitIncrement(visibleRect, orientation, direction);
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
            if ((names != null) && (names.length > 1)) {
                Component[] cs = getComponents();
                Component c = null;
                for (Component c1 : cs) {
                    if (c1.hasFocus()) {
                        c = c1;
                        break;
                    }
                }

                if (c != null) {
                    if (direction > 0) {
                        JLabel a = names[0];
                        JLabel b = names[1];
                        int diff = b.getY() - a.getY();

                        if (!visibleRect.contains(c.getX(), c.getY())
                                || !visibleRect.contains(c.getX(), c.getY() + 2 * diff)) {
                            return diff;
                        }
                    } else {
                        JLabel a = names[0];
                        JLabel b = names[1];
                        int diff = b.getY() - a.getY();

                        if (!visibleRect.contains(c.getX(), c.getY() - diff)
                                || !visibleRect.contains(c.getX(), c.getY() + diff)) {
                            return diff;
                        }
                    }
                } else {
                    JLabel a = names[0];
                    JLabel b = names[1];

                    return b.getY() - a.getY();
                }
            }
            return 0;
        }
    }
}