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
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JZWStatusPanel;
import de.df.jauswertung.gui.util.MessagePanel;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.ZWUtils;
import de.df.jutils.gui.JDoubleField;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.plaf.GradientTaskPaneGroupUI;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Mueller @date 27.03.2010
 */
class JSimpleInputPanel extends JPanel {

    private static final String INPUT             = I18n.get("ZWInput");

    final IPluginManager        controller;
    private final CorePlugin    core;
    final PZWInputPlugin        parent;

    private JPanel              inputPanel        = null;
    private JZWStatusPanel      overview          = null;

    private JComboBox<Integer>  amount            = null;
    private JButton             more;
    private MessagePanel        messages;

    private JLabel              startnumber;
    private JLabel              name;
    private JLabel              gliederung;
    private JLabel              agegroup;
    private JLabel              input;

    private TimeListener[]      dl                = new TimeListener[0];
    JWarningTextField[]         startnumbers      = new JWarningTextField[0];
    JLabel[]                    names             = new JLabel[0];
    private JLabel[]            gliederungen      = new JLabel[0];
    private JLabel[]            agegroups         = new JLabel[0];
    JDoubleField[]              inputs            = new JDoubleField[0];
    ASchwimmer[]                swimmers          = new ASchwimmer[0];
    int[]                       indizes           = new int[0];

    private boolean[]           statusSN          = new boolean[0];
    boolean[]                   statusTime        = new boolean[0];
    boolean[]                   statusZW          = new boolean[0];

    private static long         OVERVIEW_REASONS  = UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_LOAD_WK
            | UpdateEventConstants.REASON_NEW_LOAD_WK | UpdateEventConstants.REASON_NEW_TN | UpdateEventConstants.REASON_NEW_WK
            | UpdateEventConstants.REASON_PENALTY | UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_SWIMMER_CHANGED
            | UpdateEventConstants.REASON_SWIMMER_DELETED;

    int                         disciplineNumber  = 0;

    private MoreInputListener   moreInputListener = null;

    public JSimpleInputPanel(PZWInputPlugin parent, IPluginManager controller, CorePlugin core) {
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
            length += (Integer) amount.getSelectedItem();
        }
        if (length == names.length) {
            return;
        }
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow," + "4dlu,fill:default:grow,4dlu,fill:default," + "4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(1 + length));
        inputPanel.removeAll();
        inputPanel.setLayout(layout);

        inputPanel.add(startnumber, CC.xy(2, 2, "center,center"));
        inputPanel.add(name, CC.xy(4, 2, "center,center"));
        inputPanel.add(gliederung, CC.xy(6, 2, "center,center"));
        inputPanel.add(agegroup, CC.xy(8, 2, "center,center"));
        inputPanel.add(input, CC.xy(10, 2, "center,center"));

        JWarningTextField[] startnumbersNew = new JWarningTextField[length];
        JLabel[] namesNew = new JLabel[length];
        JLabel[] gliederungenNew = new JLabel[length];
        JLabel[] agegroupsNew = new JLabel[length];
        JDoubleField[] inputsNew = new JDoubleField[length];
        TimeListener[] dlNew = new TimeListener[length];
        ASchwimmer[] swimmersNew = new ASchwimmer[length];
        int[] indizesNew = new int[length];
        boolean[] statusSNNew = new boolean[length];
        boolean[] statusTimeNew = new boolean[length];
        boolean[] statusZWNew = new boolean[length];

        Strafe s1 = core.getWettkampf().getStrafen().getNichtAngetreten();
        Strafe s2 = core.getWettkampf().getStrafen().getDisqualifiziert();
        String[] specialStrings = null;
        LinkedList<String> strings = new LinkedList<String>();
        strings.add(I18n.get("DidNotStartShort"));
        strings.add("n");
        strings.add(I18n.get("DisqualificationShort"));
        strings.add("d");
        if (s1.getShortname().length() != 0) {
            strings.add(s1.getShortname());
        }
        if (s2.getShortname().length() != 0) {
            strings.add(s2.getShortname());
        }
        specialStrings = strings.toArray(new String[strings.size()]);

        for (int x = 0; x < length; x++) {
            if (names.length > x) {
                startnumbersNew[x] = startnumbers[x];
                namesNew[x] = names[x];
                gliederungenNew[x] = gliederungen[x];
                agegroupsNew[x] = agegroups[x];
                inputsNew[x] = inputs[x];
                swimmersNew[x] = swimmers[x];
                indizesNew[x] = indizes[x];
                statusSNNew[x] = statusSN[x];
                statusTimeNew[x] = statusTime[x];
                statusZWNew[x] = statusZW[x];
                dlNew[x] = dl[x];
            } else {
                startnumbersNew[x] = new JWarningTextField(false, true);
                startnumbersNew[x].setValidator(new JWarningTextField.Validator() {
                    @Override
                    public boolean validate(String value) {
                        if ((value == null) || (value.trim().length() == 0)) {
                            return true;
                        }
                        value = value.trim();
                        if (StringTools.isInteger(value)) {
                            return true;
                        }
                        AWettkampf wk = core.getWettkampf();
                        if ((ZWUtils.getZWIndex(wk, value) >= 0) && (ZWUtils.getZWStartnummer(core.getWettkampf(), value) >= 0)) {
                            return true;
                        }
                        return false;
                    }
                });
                startnumbersNew[x].setToolTipText(I18n.getToolTip("StartnumberInput"));
                startnumbersNew[x].addKeyListener(moreInputListener);
                StartnumberListener sl = new StartnumberListener(x);
                startnumbersNew[x].addKeyListener(sl);
                startnumbersNew[x].getDocument().addDocumentListener(sl);
                startnumbersNew[x].setAutoSelectAll(true);
                startnumbersNew[x].setHorizontalAlignment(SwingConstants.RIGHT);
                namesNew[x] = new JLabel();
                gliederungenNew[x] = new JLabel();
                agegroupsNew[x] = new JLabel();
                dlNew[x] = new TimeListener(x);
                inputsNew[x] = new JDoubleField(false, true);
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
            inputPanel.add(startnumbersNew[x], CC.xy(2, 4 + (2 * x)));
            inputPanel.add(namesNew[x], CC.xy(4, 4 + (2 * x)));
            inputPanel.add(gliederungenNew[x], CC.xy(6, 4 + (2 * x)));
            inputPanel.add(agegroupsNew[x], CC.xy(8, 4 + (2 * x)));
            inputPanel.add(inputsNew[x], CC.xy(10, 4 + (2 * x)));
        }

        int old = names.length;

        startnumbers = startnumbersNew;
        names = namesNew;
        gliederungen = gliederungenNew;
        agegroups = agegroupsNew;
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

        startnumber = new JLabel(I18n.get("Startnumber"));
        name = new JLabel(I18n.get("Name"));
        gliederung = new JLabel(I18n.get("Organisation"));
        agegroup = new JLabel(I18n.get("AgeGroup"));
        input = new JLabel(I18n.get("ZWPoints"));

        amount = new JComboBox<Integer>(new Integer[] { 5, 10, 15, 20, 25, 30, 40, 50 });
        amount.setToolTipText(I18n.getToolTip("NumberOfInputLines"));
        amount.addActionListener(new AmountActionListener());
        more = new JButton(I18n.get("More"));
        more.setToolTipText(I18n.getToolTip("PrepareForNewInput"));
        more.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prepareMoreInput();
            }
        });
    }

    void initPanel() {
        initVariables();
        inputPanel = new ScrollableJPanel();

        JScrollPane scroller = new JScrollPane(inputPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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
        setLayout(layout);
        add(createPropertiesPanel(), CC.xy(2, 2));
        add(createMessagePanel(), CC.xy(4, 2));
        add(over, CC.xyw(2, 4, 3));
        add(in, CC.xyw(2, 6, 3));
    }

    private JPanel createPropertiesPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default,4dlu:grow");
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Selection")));

        top.add(new JLabel(I18n.get("Amount")), CC.xy(2, 2));

        top.add(amount, CC.xy(4, 2));
        top.add(more, CC.xy(4, 4, "right,center"));

        return top;
    }

    private JPanel createMessagePanel() {
        messages = new MessagePanel(I18n.get("ErrorsOccured"), I18n.get("Ok"), new String[] { I18n.get("StartnumbersIncorrect"), I18n.get("ZWIncorrect") });
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

    void updateGUI() {
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

    public void updateRow(int index) {
        updateRow(index, true);
    }

    synchronized void updateRow(int index, boolean updateInput) {
        statusZW[index] = true;
        if (!startnumbers[index].isValidString()) {
            swimmers[index] = null;
            indizes[index] = -1;
            names[index].setText(I18n.get("NoNumberFormat"));
            setInput(index, "", updateInput);
            gliederungen[index].setText("");
            agegroups[index].setText("");
            inputs[index].setEnabled(false);
            statusSN[index] = false;
            updateStatus();
            return;
        }
        ASchwimmer s = null;
        int i = -1;
        if (core != null) {
            AWettkampf wk = core.getWettkampf();

            int sn = ZWUtils.getZWStartnummer(wk, startnumbers[index].getText());
            i = ZWUtils.getZWIndex(wk, startnumbers[index].getText());
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
                    setInput(index, "", updateInput);
                    gliederungen[index].setText("");
                    agegroups[index].setText("");
                    inputs[index].setEnabled(false);
                    statusSN[index] = false;
                    updateStatus();
                    return;
                }
            }
            swimmers[index] = s;
            indizes[index] = i;
            StringBuffer extname = new StringBuffer();
            extname.append(s.getName());
            if (s.getMaximaleHLW() > 1) {
                boolean ok = false;
                if (s instanceof Mannschaft) {
                    Mannschaft m = (Mannschaft) s;
                    String member = m.getMitgliedsname(i);
                    if (member.length() > 0) {
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
            names[index].setText(extname.toString());
            gliederungen[index].setText(s.getGliederung());
            agegroups[index].setText(I18n.getAgeGroupAsString(s));
            if (s.getAK().hasHLW()) {
                switch (s.getHLWState(i)) {
                case ENTERED:
                    setInput(index, s.getHLWPunkte(i), updateInput);
                    break;
                case NICHT_ANGETRETEN: {
                    Strafe str = core.getWettkampf().getStrafen().getNichtAngetreten();
                    String text = I18n.get("DidNotStartShort");
                    if (str.getShortname().length() > 0) {
                        text = str.getShortname();
                    }
                    setInput(index, text, updateInput);
                    break;
                }
                case NOT_ENTERED:
                    setInput(index, "", updateInput);
                    break;
                case DISQALIFIKATION: {
                    Strafe str = core.getWettkampf().getStrafen().getNichtAngetreten();
                    String text = I18n.get("DisqualificationShort");
                    if (str.getShortname().length() > 0) {
                        text = str.getShortname();
                    }
                    setInput(index, text, updateInput);
                    break;
                }
                }
                inputs[index].setEnabled(true);
                statusZW[index] = true;
            } else {
                setInput(index, "", updateInput);
                inputs[index].setEnabled(false);
                statusZW[index] = false;
            }
            statusSN[index] = true;
        } else {
            if (startnumbers[index].getText().length() > 0) {
                names[index].setText(I18n.get("NotFound"));
                statusSN[index] = false;
            } else {
                names[index].setText("");
                statusSN[index] = true;
            }
            setInput(index, "", updateInput);
            gliederungen[index].setText("");
            agegroups[index].setText("");
            inputs[index].setEnabled(false);
            statusZW[index] = true;
        }

        updateStatus();
    }

    synchronized void updateZW(int index) {
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

    void updatePanel() {
        for (int x = 0; x < names.length; x++) {
            updateRow(x);
            updateZW(x);
        }
    }

    void updateAmount() {
        updateInputPanel();
    }

    void prepareMoreInput() {
        if (more.isEnabled()) {
            for (int x = 0; x < names.length; x++) {
                startnumbers[x].setText("");
            }
        }
        startnumbers[0].requestFocus();
    }

    private int currentRow() {
        for (int x = 0; x < inputs.length; x++) {
            if (inputs[x].hasFocus()) {
                return x;
            }
            if (startnumbers[x].hasFocus()) {
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

    synchronized void setInput(int x, String value, boolean update) {
        if (!update) {
            return;
        }
        if (!value.equals(inputs[x].getText())) {
            inputs[x].getDocument().removeDocumentListener(dl[x]);
            inputs[x].setText(value);
            inputs[x].getDocument().addDocumentListener(dl[x]);
        }
    }

    synchronized void setInput(int x, double value, boolean update) {
        if (!update) {
            return;
        }
        if ((value < 0.005) || (!inputs[x].isValidDouble()) || (Math.abs(value - inputs[x].getDouble()) > 0.005)) {
            if (value > 0.005) {
                inputs[x].getDocument().removeDocumentListener(dl[x]);
                inputs[x].setDouble(value);
                inputs[x].getDocument().addDocumentListener(dl[x]);
            } else {
                setInput(x, "0", update);
            }
        }
    }

    void prepare() {
        if (startnumbers.length > 0) {
            startnumbers[0].requestFocus();
        }
    }

    void quickEdit(int index, String value) {
        if (inputs[index].isEnabled()) {
            boolean doit = true;
            if (swimmers[index].hasHLWSet(indizes[index])) {
                if (!inputs[index].getText().equals(value)) {
                    HLWStates state = swimmers[index].getHLWState(indizes[index]);
                    String before = "";
                    switch (state) {
                    case ENTERED:
                        before = I18n.get("PointsPenalty", "" + Math.round(swimmers[index].getHLWPunkte(indizes[index])));
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
                    doit = DialogUtils.ask(controller.getWindow(), I18n.get("Question.InputAlreadyPresent", before, value),
                            I18n.get("Question.InputAlreadyPresent.Note", before, value));
                }
            }
            if (doit) {
                inputs[index].setText(value);
                if (inputs.length > index + 1) {
                    startnumbers[index + 1].requestFocus();
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

    final class TimeListener implements DocumentListener, KeyListener {

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
                    startnumbers[index].requestFocus();
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
            if (zeit.indexOf("+") > -1) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String s = StringTools.removeAll(inputs[index].getText(), '+');
                        setInput(index, s, true);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                nextRow(index);
                            }
                        });
                    }
                });
                return;
            }
            if (zeit.indexOf("-") > -1) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String s = StringTools.removeAll(inputs[index].getText(), '-');
                        setInput(index, s, true);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                previousRow(index);
                            }
                        });
                    }
                });
                return;
            }
            updateZW(index);
            if (statusZW[index]) {
                if ((swimmers[index] != null)) {
                    if (inputs[index].getText().length() > 0) {
                        if (inputs[index].isValidDouble()) {
                            swimmers[index].setHLWPunkte(indizes[index], inputs[index].getDouble());
                        } else {
                            swimmers[index].setHLWState(indizes[index], ZWUtils.getHLWState(swimmers[index].getWettkampf(), zeit));
                        }
                    } else {
                        swimmers[index].setHLWState(indizes[index], HLWStates.NOT_ENTERED);
                    }
                }
                controller.sendDataUpdateEvent("ChangeZWPoints", UpdateEventConstants.REASON_POINTS_CHANGED, swimmers[index], -1, parent);
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
                    startnumbers[index - 1].requestFocus();
                }
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                if (index + 1 < inputs.length) {
                    startnumbers[index + 1].requestFocus();
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
            if (startnumbers[index].getText().indexOf("+") > -1) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String s = StringTools.removeAll(startnumbers[index].getText(), '+');
                        startnumbers[index].setText(s);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                nextRow(index);
                            }
                        });
                    }
                });
                return;
            }
            if (startnumbers[index].getText().indexOf("-") > -1) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String s = StringTools.removeAll(startnumbers[index].getText(), '-');
                        startnumbers[index].setText(s);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                previousRow(index);
                            }
                        });
                    }
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

    final class MoreInputListener extends KeyAdapter {
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

    class ScrollableJPanel extends JPanel implements Scrollable {

        private static final long serialVersionUID = 680058034328664232L;

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

                        if (!visibleRect.contains(c.getX(), c.getY()) || !visibleRect.contains(c.getX(), c.getY() + 2 * diff)) {
                            return diff;
                        }
                    } else {
                        JLabel a = names[0];
                        JLabel b = names[1];
                        int diff = b.getY() - a.getY();

                        if (!visibleRect.contains(c.getX(), c.getY() - diff) || !visibleRect.contains(c.getX(), c.getY() + diff)) {
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