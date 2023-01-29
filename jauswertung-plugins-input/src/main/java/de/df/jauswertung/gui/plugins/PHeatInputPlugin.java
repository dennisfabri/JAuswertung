/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LAUF_LIST_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PENALTY;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_POINTS_CHANGED;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Eingabe;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.TimelimitsContainer;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWLaufliste;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.plugins.zielrichterentscheid.MZielrichterentscheidPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.JZieleinlaufDialog;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jauswertung.util.ergebnis.DataType;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jauswertung.util.vergleicher.SchwimmerStartnummernVergleicher;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JIcon;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JIntegerField.Validator;
import de.df.jutils.gui.JTimeField;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.CenterLayout;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.PanelInfo;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.util.StringTools;
import net.miginfocom.swing.MigLayout;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public class PHeatInputPlugin extends ANullPlugin {

    private static final String INPUT = I18n.get("HeatResultInput");

    int bahnen = -1;

    JGlassPanel<JPanel> main = null;

    private JPanel heatPanel = null;
    JButton next = null;
    JButton previous = null;
    JComboBox<String> heat = null;
    JLabel discipline = null;
    JLabel agegroup = null;

    private IHeatInputStrategy strategy = null;

    private JLabel[] names = null;
    private JLabel[] organisations = null;
    private JLabel[] agegroups = null;
    private JLabel[] startnumbers = null;
    private JIcon[] icons = null;
    JIntegerField[] inputs = null;
    JTimeField[] times = null;
    private JLabel[] penaltiestext = null;
    private JButton[] penalties = null;
    private DocumentListener[] dl = null;
    HighPointsListener[] fl = null;

    private JButton zieleinlauf = null;
    private JButton zielrichterentscheid = null;

    private NextHeatListener nextHeatListener = null;

    ASchwimmer[] swimmers = null;
    @SuppressWarnings("rawtypes")
    AWettkampf wk = null;

    IPluginManager controller = null;
    CorePlugin core = null;
    FEditorPlugin editor = null;
    MZielrichterentscheidPlugin zeplugin = null;

    public PHeatInputPlugin() {
        // Nothing to do
    }

    void createPanel() {
        nextHeatListener = new NextHeatListener();

        main = new JGlassPanel<>(initPanel());
        main.setName(INPUT);

        JPanel info = new JPanel();
        info.setName("Info");
        info.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information")));
        info.add(new JLabel(I18n.get("HeatInputInfo")));

        JPanel glass = main.getGlassPanel();
        glass.setLayout(new CenterLayout());
        glass.add(info);

        // Hauptpanel sperren
        main.setEnabled(false);
    }

    private JPanel initPanel() {
        next = new JTransparentButton(IconManager.getSmallIcon("next"));
        next.setToolTipText(I18n.getToolTip("GotoNextHeat"));
        next.addActionListener(arg0 -> {
            int index = heat.getSelectedIndex();
            if ((index < 0) && (heat.getItemCount() > 0)) {
                heat.setSelectedIndex(0);
                return;
            }
            index++;
            if (heat.getItemCount() > index) {
                heat.setSelectedIndex(index);
            }
        });
        previous = new JTransparentButton(IconManager.getSmallIcon("previous"));
        previous.setToolTipText(I18n.getToolTip("GotoPreviousHeat"));
        previous.addActionListener(arg0 -> {
            int index = heat.getSelectedIndex();
            if ((index < 0) && (heat.getItemCount() > 0)) {
                heat.setSelectedIndex(0);
                return;
            }
            index--;
            if (0 <= index) {
                heat.setSelectedIndex(index);
            }
        });
        heat = new JComboBox<>();
        heat.setToolTipText(I18n.getToolTip("SelectHeat"));
        heat.addActionListener(new HeatActionListener());
        heat.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
                keyPressed(arg0);
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                keyPressed(arg0);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    for (int x = 0; x < inputs.length; x++) {
                        if (inputs[x].isEnabled()) {
                            inputs[x].requestFocus();
                            break;
                        }
                    }
                }
            }
        });
        agegroup = new JLabel();
        discipline = new JLabel();

        zieleinlauf = new JButton(I18n.get("CheckZieleinlauf"));
        zieleinlauf.addActionListener(e -> {
            zeigeZieleinlauf();
        });

        zielrichterentscheid = new JButton(I18n.get("Zielrichterentscheid"));
        zielrichterentscheid.addActionListener(e -> {
            zeplugin.showZielrichterentscheid();
        });

        String disciplinePart = "4dlu,left:default:grow,4dlu,";
        String agegroupPart = "center:default:grow,4dlu,";
        String heatPart = "right:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu";

        FormLayout layout = new FormLayout(disciplinePart + agegroupPart + heatPart,
                FormLayoutUtils.createLayoutString(2));

        layout.setColumnGroup(2, 4, 6);

        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Overview")));
        top.add(agegroup, CC.xy(2, 2));
        top.add(discipline, CC.xy(4, 2));

        top.add(new JLabel(I18n.get("Heat")), CC.xy(6, 2));
        top.add(previous, CC.xy(8, 2));
        top.add(heat, CC.xy(10, 2));
        top.add(next, CC.xy(12, 2));

        top.add(getButtons(), CC.xyw(2, 4, 11, "fill,fill"));

        heatPanel = new JPanel();
        heatPanel.setBorder(BorderUtils.createLabeledBorder(I18n.get("Heat")));

        layout = new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default,4dlu");

        JPanel innerPanel = new JPanel(layout);
        innerPanel.add(top, CC.xy(2, 2));
        innerPanel.add(heatPanel, CC.xy(2, 4));

        JPanel panel = new JPanel(new MigLayout("", "[fill, grow, ::1200lp]", "[fill]"));
        panel.add(innerPanel);

        return panel;
    }

    private JPanel getButtons() {
        FormLayout layout = new FormLayout("0dlu:grow,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
        JPanel p = new JPanel(layout);
        p.add(zielrichterentscheid, CC.xy(2, 2));
        p.add(zieleinlauf, CC.xy(4, 2));
        return p;
    }

    @Override
    public void setController(IPluginManager c, String uid) {
        controller = c;
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", uid);
        editor = (FEditorPlugin) controller.getFeature("de.df.jauswertung.editor", uid);
        zeplugin = (MZielrichterentscheidPlugin) controller.getPlugin("de.df.jauswertung.zielrichterentscheid", uid);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void dataUpdated(UpdateEvent due) {
        if (main == null) {
            return;
        }

        if (due.isSource(this)) {
            return;
        }

        wk = core.getWettkampf();
        if (wk.isHeatBased()) {
            OWLaufliste<?> laufliste = wk.getLauflisteOW();
            if (laufliste.isEmpty()) {
                main.setEnabled(false);
                return;
            }
        } else {
            Laufliste laufliste = wk.getLaufliste();
            if ((laufliste.getLaufliste() == null) || (laufliste.getLaufliste().isEmpty())) {
                main.setEnabled(false);
            }
        }
        updatePanel(due);
    }

    private boolean byTimes = true;

    void updatePanel(UpdateEvent due) {
        if (wk.isHeatBased()) {
            strategy = new HeatInputStrategyByHeat<ASchwimmer>();
        } else {
            strategy = new HeatInputStrategyByTime<ASchwimmer>();
        }
        strategy.init();

        // Updating Panel
        EDTUtils.setEnabled(main, !strategy.isEmpty());
        if (strategy.isEmpty()) {
            return;
        }

        boolean byTimesNew = FormelManager.getInstance().get(wk.getRegelwerk().getFormelID()).getDataType()
                .equals(DataType.TIME);

        int b = strategy.getBahnen();

        if (bahnen != b || byTimesNew != byTimes) {
            bahnen = b;
            byTimes = byTimesNew;

            // Updating datastructures
            swimmers = new ASchwimmer[bahnen];

            // Updating heatPanel
            heatPanel.removeAll();
            FormLayout layout;
            if (byTimes) {
                layout = new FormLayout(
                        "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,"
                                + "4dlu,fill:default,4dlu,center:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                        FormLayoutUtils.createLayoutString(bahnen + 1));
                layout.setColumnGroups(new int[][] { { 14, 16, 18, 20 } });
            } else {
                layout = new FormLayout(
                        "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,"
                                + "0dlu,0dlu,4dlu,center:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                        FormLayoutUtils.createLayoutString(bahnen + 1));
                layout.setColumnGroups(new int[][] { { 14, 18, 20 } });
            }
            FormLayoutUtils.setRowGroups(layout, bahnen + 1);
            heatPanel.setLayout(layout);

            heatPanel.add(new JLabel(strategy.getSortingLabel()), CC.xy(2, 2, "center,center"));
            heatPanel.add(new JLabel(I18n.get("Name")), CC.xy(4, 2, "center,center"));
            heatPanel.add(new JLabel(I18n.get("Organisation")), CC.xy(6, 2, "center,center"));
            heatPanel.add(new JLabel(I18n.get("AgeGroup")), CC.xy(8, 2, "center,center"));
            heatPanel.add(new JLabel(I18n.get("StartnumberShort")), CC.xy(12, 2, "center,center"));
            heatPanel.add(new JLabel(I18n.get("Input")), CC.xy(14, 2, "center,center"));
            if (byTimes) {
                heatPanel.add(new JLabel(I18n.get("Time")), CC.xy(16, 2, "center,center"));
            }
            heatPanel.add(new JLabel(I18n.get("Penalty")), CC.xy(18, 2, "center,center"));
            heatPanel.add(new JLabel(strategy.getSortingLabel()), CC.xy(22, 2, "center,center"));

            names = new JLabel[bahnen];
            organisations = new JLabel[bahnen];
            agegroups = new JLabel[bahnen];
            startnumbers = new JLabel[bahnen];
            inputs = new JIntegerField[bahnen];
            if (byTimes) {
                times = new JTimeField[bahnen];
            }
            dl = new DocumentListener[bahnen];
            fl = new HighPointsListener[bahnen];
            swimmers = new ASchwimmer[bahnen];
            penalties = new JButton[bahnen];
            penaltiestext = new JLabel[bahnen];
            icons = new JIcon[bahnen];

            for (int x = 0; x < bahnen; x++) {
                names[x] = new JLabel();
                organisations[x] = new JLabel();
                agegroups[x] = new JLabel();
                startnumbers[x] = new JLabel();
                dl[x] = new TimeListener(x);
                fl[x] = new HighPointsListener(x);
                if (byTimes) {
                    inputs[x] = new JIntegerField(JIntegerField.EMPTY_FIELD, JTimeField.MAX_TIME, false, true);
                    inputs[x].setToolTipText(I18n.getToolTip("TimeInputField"));
                    inputs[x].setValidator((Validator)value -> {
                        value = value / 100;
                        if ((value % 100) >= 60) {
                            return false;
                        }
                        value = value / 100;
                        return value < 1000;
                    });
                } else {
                    inputs[x] = new JIntegerField(JIntegerField.EMPTY_FIELD, Integer.MAX_VALUE, false, true);
                    inputs[x].setToolTipText(I18n.getToolTip("RankInputField"));
                }
                inputs[x].addKeyListener(nextHeatListener);
                inputs[x].getDocument().addDocumentListener(dl[x]);
                inputs[x].addFocusListener(fl[x]);
                inputs[x].setAutoSelectAll(true);
                inputs[x].setHorizontalAlignment(SwingConstants.RIGHT);
                if (byTimes) {
                    times[x] = new JTimeField(inputs[x]);
                }
                penaltiestext[x] = new JLabel();
                penalties[x] = new JButton(I18n.get("Penalty"));
                penalties[x].setToolTipText(I18n.getToolTip("PenaltyButton"));
                penalties[x].addKeyListener(new PenaltyKeyListener(x));
                penalties[x].addActionListener(new PenaltyListener(x));

                icons[x] = new JIcon(IconManager.getSmallIcon("warn").getImage());
                icons[x].setVisible(false);

                heatPanel.add(new JLabel("" + (x + 1)), CC.xy(2, 4 + 2 * x, "center,center"));
                heatPanel.add(names[x], CC.xy(4, 4 + 2 * x));
                heatPanel.add(organisations[x], CC.xy(6, 4 + 2 * x));
                heatPanel.add(agegroups[x], CC.xy(8, 4 + 2 * x, "center,center"));
                heatPanel.add(icons[x], CC.xy(10, 4 + 2 * x, "center,center"));
                heatPanel.add(startnumbers[x], CC.xy(12, 4 + 2 * x, "center,center"));
                heatPanel.add(inputs[x], CC.xy(14, 4 + 2 * x));
                if (byTimes) {
                    heatPanel.add(times[x], CC.xy(16, 4 + 2 * x));
                }
                heatPanel.add(penaltiestext[x], CC.xy(18, 4 + 2 * x));
                heatPanel.add(penalties[x], CC.xy(20, 4 + 2 * x));
                heatPanel.add(new JLabel("" + (x + 1)), CC.xy(22, 4 + 2 * x, "center,center"));
            }
        }

        if (due != null && (due.getChangeReason() & REASON_LAUF_LIST_CHANGED) > 0) {
            strategy.heatlistChanged();
        }
        updateInputFields();
    }

    boolean update = false;

    synchronized void updateInputFields() {
        int index = heat.getSelectedIndex();
        if (index < 0) {
            index = 0;
        }

        if (update) {
            return;
        }
        try {
            if (update) {
                return;
            }
            update = true;
            strategy.updateInputFields(index);
        } finally {
            update = false;
        }
    }

    void updatePenalty(int x) {
        int index = heat.getSelectedIndex();
        if (index < 0) {
            index = 0;
        }
        strategy.updatePenalty(index, x);
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return new PanelInfo[] { new PanelInfo(I18n.get(INPUT), IconManager.getBigIcon("heatinput"), 350) {
            @Override
            public JPanel getPanelI() {
                if (main == null) {
                    createPanel();
                    dataUpdated(new UpdateEvent("Init", UpdateEventConstants.REASON_EVERYTHING_CHANGED, null));
                }
                return main;
            }
        } };
    }

    private int currentLane() {
        for (int x = 0; x < inputs.length; x++) {
            if (inputs[x].hasFocus()) {
                return x;
            }
            if (penalties[x].hasFocus()) {
                return x;
            }
        }
        return 0;
    }

    boolean nextLane() {
        return nextLane(false);
    }

    boolean nextLane(boolean switchheat) {
        return nextLane(currentLane(), switchheat);
    }

    boolean previousLane(boolean switchheat) {
        return previousLane(currentLane(), switchheat);
    }

    boolean nextLane(int index) {
        return nextLane(index, false);
    }

    boolean previousLane(int index) {
        return previousLane(index, false);
    }

    boolean nextLane(int index, boolean switchheat) {
        int i = index;

        index++;
        while ((index < inputs.length) && (!inputs[index].isEnabled())) {
            index++;
        }
        if (index < inputs.length) {
            inputs[index].requestFocus();
            return true;
        }

        if (switchheat) {
            if (fl[i].checkTime()) {
                nextHeat();
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
        return false;
    }

    boolean previousLane(int index, boolean switchheat) {
        int i = index;

        index--;
        while ((index >= 0) && (!inputs[index].isEnabled())) {
            index--;
        }
        if (index >= 0) {
            inputs[index].requestFocus();
            return true;
        }

        if (switchheat) {
            if (fl[i].checkTime()) {
                previousHeat(false);
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
        return false;
    }

    private synchronized void setInput(int x, String value) {
        if (!value.equals(inputs[x].getText())) {
            inputs[x].getDocument().removeDocumentListener(dl[x]);
            inputs[x].setText(value);
            inputs[x].getDocument().addDocumentListener(dl[x]);
        }
    }

    private synchronized void setTime(int x, int value) {
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

    private class PenaltyListener implements ActionListener {

        private int index = 0;

        public PenaltyListener(int x) {
            if ((x < 0) || (x >= bahnen)) {
                throw new IllegalArgumentException("Index to large! Should be 0 <=" + x + "<" + bahnen + ".");
            }
            index = x;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            strategy.runPenaltyEditor(heat.getSelectedIndex(), index);
        }
    }

    private boolean checkHighPoints(int index) {
        if (swimmers[index] == null) {
            return true;
        }
        if (inputs[index].getText().equals("")) {
            return true;
        }
        if (!SchwimmerUtils.checkTimeAndNotify(controller.getWindow(), swimmers[index],
                strategy.getDiscipline(index))) {
            inputs[index].requestFocus();
            return false;
        }
        return true;
    }

    private final class HighPointsListener extends FocusAdapter {

        private int index = 0;

        private String data = "";

        public HighPointsListener(int x) {
            index = x;
        }

        @Override
        public synchronized void focusLost(FocusEvent e) {
            checkTime();
        }

        public boolean checkTime() {
            String data2 = data;
            if (index >= inputs.length) {
                return true;
            }
            data = inputs[index].getText();
            if (data2.equals(data)) {
                return true;
            }
            return checkHighPoints(index);
        }
    }

    private class TimeListener extends KeyAdapter implements DocumentListener {

        int index = 0;

        public TimeListener(int x) {
            if ((x < 0) || (x >= bahnen)) {
                throw new IllegalArgumentException("Index to large! Should be 0 <=" + x + "<" + bahnen + ".");
            }
            index = x;
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
                } else {
                    if (fl[index].checkTime()) {
                        previousHeat(false);
                    }
                }
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                if (index + 1 < inputs.length) {
                    inputs[index + 1].requestFocus();
                } else {
                    if (fl[index].checkTime()) {
                        nextHeat();
                    }
                }
                e.consume();
                break;
            default:
                break;
            }
        }

        @Override
        public void insertUpdate(DocumentEvent arg0) {
            if (swimmers[index] == null) {
                return;
            }
            if (inputs[index].isValidInt() && (!byTimes || times[index].isValidValue())) {
                changeTime();
                updatePenalty(index);
                return;
            }
            String zeit = inputs[index].getText();
            if (zeit.indexOf("p") > -1) {
                setPenaltyPoints(zeit);
                return;
            }
            if (zeit.indexOf("c") > -1) {
                setPenaltyCode(zeit);
                return;
            }
            if ((zeit.indexOf(",") > -1) || (zeit.indexOf("z") > -1)) {
                showZieleinlauf(zeit);
                return;
            }
            if (zeit.indexOf("#") > -1) {
                setNoPenalty(zeit);
                return;
            }
            if ((zeit.indexOf("m") > -1) || (zeit.indexOf("+") > -1)) {
                setMeanTime(zeit);
                return;
            }
            if (zeit.indexOf("d") > -1) {
                setDisqualifikation(zeit);
                return;
            }
            if (zeit.indexOf("n") > -1) {
                setNA(zeit);
                return;
            }
            if (zeit.indexOf("w") > -1) {
                setWithdraw(zeit);
                return;
            }
            if (zeit.indexOf("f") > -1) {
                setDidNotFinish(zeit);
                return;
            }
        }

        private void setPenaltyPoints(String zeit) {
            if ((zeit.length() == 1) || StringTools.isInteger(StringTools.removeAll(zeit, 'p'))) {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'p');
                    inputs[index].setText(x);
                    if (checkHighPoints(index)) {
                        strategy.runPenaltyPoints(heat.getSelectedIndex(), index);
                    }
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'p');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        private void setPenaltyCode(String zeit) {
            if ((zeit.length() == 1) || StringTools.isInteger(StringTools.removeAll(zeit, 'c'))) {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'c');
                    inputs[index].setText(x);
                    if (checkHighPoints(index)) {
                        strategy.runPenaltyCode(heat.getSelectedIndex(), index);
                    }
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'c');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        private void setMeanTime(String zeit) {
            if ((zeit.length() == 1)
                    || StringTools.isInteger(StringTools.removeAll(StringTools.removeAll(zeit, 'm'), '+'))) {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(StringTools.removeAll(inputs[index].getText(), 'm'), '+');
                    inputs[index].setText(x);
                    if (checkHighPoints(index)) {
                        strategy.runMeanTimeEditor(index, t -> {
                            if (false == t) {
                                return;
                            }
                            boolean b = nextLane(index, false);
                            if (!b) {
                                zeigeZieleinlauf();
                            }
                        });
                    }
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'm');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        private void showZieleinlauf(String zeit) {
            if ((zeit.length() == 1)
                    || StringTools.isInteger(StringTools.removeAll(StringTools.removeAll(zeit, ','), 'z'))) {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(StringTools.removeAll(inputs[index].getText(), ','), 'z');
                    inputs[index].setText(x);
                    if (checkHighPoints(index)) {
                        zeigeZieleinlauf();
                    }
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(StringTools.removeAll(inputs[index].getText(), ','), 'z');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        /**
         * @param zeit
         */
        private void setNoPenalty(String zeit) {
            if ((zeit.length() == 1) || StringTools.isInteger(StringTools.removeAll(zeit, '#'))) {
                strategy.setStrafen(heat.getSelectedIndex(), index, new LinkedList<>());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String x = StringTools.removeAll(inputs[index].getText(), '#');
                        inputs[index].setText(x);
                        updatePenalty(index);
                        controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY,
                                swimmers[index], strategy.getDiscipline(index), PHeatInputPlugin.this);
                    }
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), '#');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        /**
         * @param zeit
         */
        private void setDisqualifikation(String zeit) {
            if ((zeit.length() == 1) || StringTools.isInteger(StringTools.removeAll(zeit, 'd'))) {
                strategy.addStrafe(heat.getSelectedIndex(), index, Strafe.DISQUALIFIKATION);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String x = StringTools.removeAll(inputs[index].getText(), 'd');
                        inputs[index].setText(x);
                        updatePenalty(index);
                        controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY,
                                swimmers[index], strategy.getDiscipline(index), PHeatInputPlugin.this);
                    }
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'd');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        private void setWithdraw(String zeit) {
            if ((zeit.length() == 1) || StringTools.isInteger(StringTools.removeAll(zeit, 'w'))) {
                strategy.addStrafe(heat.getSelectedIndex(), index, wk.getStrafen().getWithdraw());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String x = StringTools.removeAll(inputs[index].getText(), 'w');
                        inputs[index].setText(x);
                        updatePenalty(index);
                        controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY,
                                swimmers[index], strategy.getDiscipline(index), PHeatInputPlugin.this);
                    }
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'w');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        private void setDidNotFinish(String zeit) {
            if ((zeit.length() == 1) || StringTools.isInteger(StringTools.removeAll(zeit, 'f'))) {
                strategy.addStrafe(heat.getSelectedIndex(), index, wk.getStrafen().getDidNotFinish());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String x = StringTools.removeAll(inputs[index].getText(), 'f');
                        inputs[index].setText(x);
                        updatePenalty(index);
                        controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY,
                                swimmers[index], strategy.getDiscipline(index), PHeatInputPlugin.this);
                    }
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'f');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        private void setNA(String zeit) {
            if ((zeit.equals("n"))) {
                strategy.addStrafe(heat.getSelectedIndex(), index, wk.getStrafen().getNichtAngetreten());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String x = StringTools.removeAll(inputs[index].getText(), 'n');
                        inputs[index].setText(x);
                        updatePenalty(index);
                        controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY,
                                swimmers[index], strategy.getDiscipline(index), PHeatInputPlugin.this);
                    }
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'n');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        /**
         * 
         */
        private void changeTime() {
            synchronized (controller) {
                if (update) {
                    return;
                }
                strategy.changeTime(heat.getSelectedIndex(), index);
            }
        }

        @Override
        public void removeUpdate(DocumentEvent arg0) {
            insertUpdate(arg0);
        }

        @Override
        public void changedUpdate(DocumentEvent arg0) {
            insertUpdate(arg0);
        }
    }

    class HeatActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            try {
                int index = heat.getSelectedIndex();
                strategy.heatChanged(index);
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                // Nothing to do
            }
        }
    }

    int getFocusIndex() {
        for (int x = 0; x < inputs.length; x++) {
            if (inputs[x].hasFocus()) {
                return x;
            }
        }
        return 0;
    }

    class NextHeatListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown() && !e.isShiftDown()) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int index = getFocusIndex();
                    if (fl[index].checkTime()) {
                        nextHeat();
                    }
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    int index = getFocusIndex();
                    if (fl[index].checkTime()) {
                        previousHeat(true);
                    }
                    return;
                }
            }
            if (!e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown() && !e.isShiftDown()) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    boolean b = nextLane();
                    if (!b) {
                        zeigeZieleinlauf();
                    }
                    return;
                }
            }
            if (e.isConsumed()) {
                return;
            }
            switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                previousLane(true);
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                nextLane(true);
                e.consume();
                break;
            default:
                break;
            }

            // don't override the time in the field
            int index = getFocusIndex();
            if (inputs[index] == null)
                return;
            String keychars = "pc#mdn+-*/";
            if (!keychars.contains(Character.toString(e.getKeyChar())))
                return;
            if (inputs[index].getSelectedText() == null)
                return;
            if (inputs[index].getSelectedText().equals(inputs[index].getText())) {
                inputs[index].moveCaretPosition(0);
            }
        }
    }

    final class PenaltyKeyListener extends KeyAdapter {

        final int index;

        public PenaltyKeyListener(int index) {
            this.index = index;
        }

        /**
         * @param zeit
         */
        private void setNoPenalty() {
            strategy.setStrafen(heat.getSelectedIndex(), index, new LinkedList<>());
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY,
                            swimmers[index], strategy.getDiscipline(index), PHeatInputPlugin.this);
                }
            });
        }

        @Override
        public void keyReleased(KeyEvent evt) {
            char key = evt.getKeyChar();
            if (key == '*') {
                if (fl[index].checkTime()) {
                    nextHeat();
                }
                return;
            }
            if (key == '/') {
                if (fl[index].checkTime()) {
                    previousHeat(true);
                }
                return;
            }
            if (key == '#') {
                setNoPenalty();
                return;
            }
        }
    }

    boolean nextHeat() {
        int index = heat.getSelectedIndex() + 1;
        if (index < heat.getItemCount()) {
            heat.setSelectedIndex(index);
            for (JIntegerField input : inputs) {
                if (input.isEnabled()) {
                    input.requestFocus();
                    break;
                }
            }
            return true;
        }

        Toolkit.getDefaultToolkit().beep();
        return false;
    }

    boolean previousHeat(boolean top) {
        int index = heat.getSelectedIndex() - 1;
        if (index >= 0) {
            heat.setSelectedIndex(index);
            if (top) {
                for (JIntegerField input : inputs) {
                    if (input.isEnabled()) {
                        input.requestFocus();
                        break;
                    }
                }
            } else {
                for (int x = inputs.length - 1; x >= 0; x--) {
                    if (inputs[x].isEnabled()) {
                        inputs[x].requestFocus();
                        break;
                    }
                }
            }
            return true;
        }

        Toolkit.getDefaultToolkit().beep();
        return false;
    }

    void zeigeZieleinlauf() {
        strategy.zeigeZieleinlauf();
    }

    interface IHeatInputStrategy {
        public int getBahnen();

        public void runMeanTimeEditor(int index, ISimpleCallback<Boolean> iSimpleCallback);

        public String getSortingLabel();

        public void zeigeZieleinlauf();

        public void addStrafe(int index, int lane, Strafe disqualifikation);

        public void setStrafen(int index, int lane, LinkedList<Strafe> linkedList);

        public void runPenaltyCode(int index, int lane);

        public void runPenaltyPoints(int index, int lane);

        public int getDiscipline(int index);

        public void runPenaltyEditor(int index, int lane);

        public void changeTime(int index, int lane);

        public void updatePenalty(int index, int lane);

        public void heatChanged(int index);

        public void updateInputFields(int index);

        public boolean isEmpty();

        public void heatlistChanged();

        public void init();

        public boolean isTimeLimitBroken(int index, int lane);
    }

    class HeatInputStrategyByTime<T extends ASchwimmer> implements IHeatInputStrategy {

        int[] disciplines = new int[0];

        @Override
        public void init() {
        }

        @Override
        public int getBahnen() {
            return wk.getIntegerProperty(HEATS_LANES);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void heatlistChanged() {
            wk = core.getWettkampf();

            disciplines = Arrays.copyOf(disciplines, getBahnen());

            Laufliste<T> ll = wk.getLaufliste();
            int index = heat.getSelectedIndex();
            heat.removeAllItems();
            String[] items = new String[ll == null || ll.isEmpty() ? 0 : ll.getLaufliste().size()];
            if (items.length > 0) {
                @SuppressWarnings("null")
                ListIterator<Lauf<T>> li = ll.getLaufliste().listIterator();
                for (int x = 0; li.hasNext(); x++) {
                    items[x] = li.next().getName();
                }
            }
            heat.setModel(new DefaultComboBoxModel<>(items));
            if (index != -1) {
                heat.setSelectedIndex(index);
            } else {
                heat.setSelectedIndex(heat.getItemCount() > 0 ? 0 : -1);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean isEmpty() {
            wk = core.getWettkampf();
            Laufliste<T> ll = wk.getLaufliste();
            return ll.isEmpty();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void updateInputFields(int index) {
            if (index < 0) {
                return;
            }

            if (disciplines.length != getBahnen()) {
                disciplines = Arrays.copyOf(disciplines, getBahnen());
            }

            Lauf<T> current = (Lauf<T>) wk.getLaufliste().getLaufliste().get(index);
            updateZieleinlauf(current);
            for (int x = 0; x < current.getBahnen(); x++) {
                swimmers[x] = current.getSchwimmer(x);
                disciplines[x] = current.getDisznummer(x);
                if (swimmers[x] == null) {
                    inputs[x].setEnabled(false);
                    penalties[x].setEnabled(false);
                    setInput(x, "");
                    names[x].setText("");
                    organisations[x].setText("");
                    agegroups[x].setText("");
                    startnumbers[x].setText("");
                    penaltiestext[x].setText("");
                    icons[x].setVisible(false);
                } else {
                    inputs[x].setEnabled(true);
                    penalties[x].setEnabled(true);

                    ASchwimmer s = swimmers[x];

                    if (s.getZeit(disciplines[x]) == 0) {
                        setInput(x, "");
                    } else {
                        setTime(x, s.getZeit(disciplines[x]));
                    }
                    names[x].setText(s.getName());
                    String q = s.getQualifikationsebene();
                    if (q.length() > 0) {
                        q = " (" + q + ")";
                    }
                    organisations[x].setText(s.getGliederung() + q);
                    agegroups[x].setText(s.getAK().toString() + " " + I18n.geschlechtToString(s));
                    startnumbers[x].setText(StartnumberFormatManager.format(s));
                    penaltiestext[x].setText(
                            PenaltyUtils.getPenaltyShortText(s.getAkkumulierteStrafe(disciplines[x]), s.getAK()));
                    icons[x].setVisible(strategy.isTimeLimitBroken(index, x));
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void heatChanged(int index) {
            if (index >= 0) {
                Lauf<T> lauf = (Lauf<T>) wk.getLaufliste().getLaufliste().get(index);
                discipline.setText(lauf.getDisziplin());
                agegroup.setText(lauf.getAltersklasse(false));
            }
            previous.setEnabled(index > 0);
            next.setEnabled(index + 1 < heat.getItemCount());
            updateInputFields(index);
        }

        @SuppressWarnings("rawtypes")
        private void updateZieleinlauf(Lauf l) {
            zieleinlauf.setEnabled(l.alleZeitenEingegeben());
            zielrichterentscheid.setEnabled(zeplugin.isZielrichterentscheidEnabled());
        }

        @Override
        @SuppressWarnings("unchecked")
        public void updatePenalty(int index, int lane) {
            Lauf<T> current = (Lauf<T>) wk.getLaufliste().getLaufliste().get(index);
            swimmers[lane] = current.getSchwimmer(lane);
            disciplines[lane] = current.getDisznummer(lane);
            if (swimmers[lane] == null) {
                penaltiestext[lane].setText("");
            } else {
                ASchwimmer s = swimmers[lane];
                penaltiestext[lane].setText(
                        PenaltyUtils.getPenaltyShortText(s.getAkkumulierteStrafe(disciplines[lane]), s.getAK()));
            }
            updateZieleinlauf(current);
        }

        @Override
        public void changeTime(int index, int lane) {
            if (byTimes) {
                swimmers[lane].setZeit(disciplines[lane], times[lane].getTimeAsInt());
                icons[lane].setVisible(isTimeLimitBroken(index, lane));
            } else {
                swimmers[lane].setZeit(disciplines[lane], inputs[lane].getInt());
            }
            controller.sendDataUpdateEvent("ChangeTime", UpdateEventConstants.REASON_POINTS_CHANGED, swimmers[lane],
                    disciplines[lane], PHeatInputPlugin.this);
        }

        @Override
        public boolean isTimeLimitBroken(int index, int lane) {
            ASchwimmer s = swimmers[lane];
            if (s == null) {
                return false;
            }

            int disciplineIndex = getDiscipline(lane);
            String disziplin = s.getAK().getDisziplin(disciplineIndex, s.isMaennlich()).getName();
            int time = s.getZeit(disciplineIndex);
            Strafe penalty = s.getAkkumulierteStrafe(disciplineIndex);

            if (time <= 0) {
                return false;
            }

            TimelimitsContainer tlc = s.getWettkampf().getTimelimits();
            return tlc.isBrokenBy(time, penalty, disziplin, s,
                    wk.getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION), 0);
        }

        @Override
        public int getDiscipline(int index) {
            return disciplines[index];
        }

        @Override
        @SuppressWarnings("unchecked")
        public void runPenaltyEditor(int index, int lane) {
            editor.runPenaltyEditor(wk, swimmers[lane], disciplines[lane]);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void runPenaltyPoints(int index, int lane) {
            editor.runPenaltyPoints(wk, swimmers[lane], disciplines[lane]);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void runPenaltyCode(int index, int lane) {
            editor.runPenaltyCode(wk, swimmers[lane], disciplines[lane], wk.getStrafen());
        }

        @Override
        public void setStrafen(int index, int lane, LinkedList<Strafe> strafen) {
            swimmers[lane].setStrafen(disciplines[lane], strafen);
        }

        @Override
        public void addStrafe(int index, int lane, Strafe strafe) {
            swimmers[lane].addStrafe(disciplines[lane], strafe);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void zeigeZieleinlauf() {
            Lauf lauf = (Lauf) wk.getLaufliste().getLaufliste().get(heat.getSelectedIndex());
            if (!lauf.alleZeitenEingegeben()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            new JZieleinlaufDialog(controller.getWindow(), lauf, (ISimpleCallback<Boolean>)result -> {
                if (result) {
                    nextHeat();
                }
            }).setVisible(true);
        }

        @Override
        public String getSortingLabel() {
            return I18n.get("Lane");
        }

        @Override
        public void runMeanTimeEditor(int index, ISimpleCallback<Boolean> iSimpleCallback) {
            editor.runMeanTimeEditor(swimmers[index], getDiscipline(index), iSimpleCallback);
        }
    }

    class HeatInputStrategyByHeat<T extends ASchwimmer> implements IHeatInputStrategy {

        private class HeatInfo {

            public final String Item1;
            public final OWDisziplin<T> Item2;
            public final OWLauf<T> Item3;

            public HeatInfo(String a, OWDisziplin<T> b, OWLauf<T> c) {
                Item1 = a;
                Item2 = b;
                Item3 = c;
            }
        }

        private LinkedList<HeatInfo> owlaeufe = null;

        @Override
        public void init() {
            heatlistChanged();
        }

        @Override
        public int getBahnen() {
            int index = heat.getSelectedIndex();
            if (index < 0) {
                return 0;
            }
            if (index >= owlaeufe.size()) {
                return 0;
            }
            HeatInfo info = owlaeufe.get(index);
            OWDisziplin<T> d = info.Item2;
            if (d == null) {
                return 0;
            }
            return d.getBahnen();
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void heatlistChanged() {
            wk = core.getWettkampf();
            owlaeufe = new LinkedList<>();
            OWLaufliste ll = wk.getLauflisteOW();

            OWDisziplin<T>[] dx = ll.getDisziplinen();
            Arrays.sort(dx, new Comparator<OWDisziplin<T>>() {

                @Override
                public int compare(OWDisziplin<T> o1, OWDisziplin<T> o2) {
                    int roundId1 = wk.getRegelwerk().getRundenId(o1);
                    int roundId2 = wk.getRegelwerk().getRundenId(o2);
                    int diff = roundId1 - roundId2;
                    if (diff != 0) {
                        return diff;
                    }
                    diff = o1.akNummer - o2.akNummer;
                    if (diff != 0) {
                        return diff;
                    }
                    diff = (o1.maennlich ? 1 : 0) - (o2.maennlich ? 1 : 0);
                    if (diff != 0) {
                        return diff;
                    }
                    diff = o1.disziplin - o2.disziplin;
                    if (diff != 0) {
                        return diff;
                    }
                    diff = o1.round - o2.round;
                    if (diff != 0) {
                        return diff;
                    }
                    return 0;
                }
            });

            for (OWDisziplin d : dx) {
                for (OWLauf l : (LinkedList<OWLauf>) d.getLaeufe()) {
                    int rid = wk.getRegelwerk().getRundenId(d);
                    String heattext = String.format("%03d-%02d%s", rid, l.getLaufnummer(),
                            StringTools.characterString(l.getLaufbuchstabe()));
                    String name = String.format("%s - %s", heattext,
                            I18n.getDisciplineFullName(wk, l.getDisciplineId()));
                    owlaeufe.add(new HeatInfo(name, d, l));
                }
            }

            int index = heat.getSelectedIndex();
            heat.removeAllItems();
            Object[] items = owlaeufe.stream().map(l -> l.Item1).toArray();
            heat.setModel(new DefaultComboBoxModel(items));
            if (index != -1) {
                heat.setSelectedIndex(index);
            } else {
                heat.setSelectedIndex(0);
            }
        }

        @Override
        public boolean isEmpty() {
            return core.getWettkampf().getLauflisteOW().isEmpty();
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void updateInputFields(int index) {
            if (index < 0) {
                return;
            }
            OWLauf current = owlaeufe.get(index).Item3;
            OWDisziplin d = owlaeufe.get(index).Item2;

            ASchwimmer[] values;

            if (wk.isOpenWater()) {
                LinkedList<T> schwimmer = current.getAllSchwimmer();
                Collections.sort(schwimmer, new SchwimmerStartnummernVergleicher<>());

                values = schwimmer.toArray(new ASchwimmer[schwimmer.size()]);
            } else {
                values = new ASchwimmer[current.getBahnen()];
                for (int x = 0; x < values.length; x++) {
                    values[x] = current.getSchwimmer(x);
                }
            }

            updateZieleinlauf(current);
            for (int x = 0; x < current.getBahnen(); x++) {
                swimmers[x] = x < values.length ? values[x] : null;
                if (swimmers[x] == null) {
                    inputs[x].setEnabled(false);
                    penalties[x].setEnabled(false);
                    setInput(x, "");
                    names[x].setText("");
                    organisations[x].setText("");
                    agegroups[x].setText("");
                    startnumbers[x].setText("");
                    penaltiestext[x].setText("");
                } else {
                    inputs[x].setEnabled(true);
                    penalties[x].setEnabled(true);

                    ASchwimmer s = swimmers[x];

                    if (s.getZeit(d.Id) == 0) {
                        setInput(x, "");
                    } else {
                        setTime(x, s.getZeit(d.Id));
                    }
                    names[x].setText(s.getName());
                    String q = s.getQualifikationsebene();
                    if (q.length() > 0) {
                        q = " (" + q + ")";
                    }
                    organisations[x].setText(s.getGliederung() + q);
                    agegroups[x].setText(I18n.getAgeGroupAsString(s));
                    startnumbers[x].setText(StartnumberFormatManager.format(s));
                    penaltiestext[x]
                            .setText(PenaltyUtils.getPenaltyShortText(s.getAkkumulierteStrafe(d.Id), s.getAK()));
                    icons[x].setVisible(isTimeLimitBroken(index, x));
                }
            }
        }

        @Override
        public void heatChanged(int index) {
            if (index >= 0) {
                OWDisziplin<T> d = owlaeufe.get(index).Item2;

                Altersklasse ak = wk.getRegelwerk().getAk(d.akNummer);
                String disziplin = ak.getDisziplin(d.disziplin, d.maennlich).getName();

                discipline.setText(disziplin);
                agegroup.setText(I18n.getAgeGroupAsString(wk.getRegelwerk(), ak, d.maennlich));
            }
            previous.setEnabled(index > 0);
            next.setEnabled(index + 1 < heat.getItemCount());
            updatePanel(null);
            updateInputFields(index);
        }

        @SuppressWarnings("rawtypes")
        private void updateZieleinlauf(OWLauf l) {
            zieleinlauf.setEnabled(l.alleZeitenEingegeben());
            zielrichterentscheid.setEnabled(false);
        }

        @Override
        public void updatePenalty(int index, int lane) {
            HeatInfo info = owlaeufe.get(index);
            OWLauf<T> current = info.Item3;
            OWDisziplin<T> d = info.Item2;

            if (swimmers[lane] == null) {
                penaltiestext[lane].setText("");
            } else {
                ASchwimmer s = swimmers[lane];
                penaltiestext[lane].setText(PenaltyUtils.getPenaltyShortText(s.getAkkumulierteStrafe(d.Id), s.getAK()));
            }
            updateZieleinlauf(current);
        }

        @Override
        public void changeTime(int index, int lane) {
            OWDisziplin<T> d = owlaeufe.get(index).Item2;

            if (byTimes) {
                swimmers[lane].setZeit(d.Id, times[lane].getTimeAsInt());
                icons[lane].setVisible(isTimeLimitBroken(index, lane));
            } else {
                swimmers[lane].setZeit(d.Id, inputs[lane].getInt());
            }
            controller.sendDataUpdateEvent("ChangeTime", UpdateEventConstants.REASON_POINTS_CHANGED, swimmers[lane],
                    d.disziplin, PHeatInputPlugin.this);
        }

        @Override
        public boolean isTimeLimitBroken(int index, int lane) {
            HeatInfo info = owlaeufe.get(index);
            OWDisziplin<T> d = info.Item2;

            ASchwimmer s = swimmers[lane];
            if (s == null) {
                return false;
            }

            String disziplin = s.getAK().getDisziplin(d.disziplin, s.isMaennlich()).getName();
            int time = s.getZeit(d.Id);
            Strafe penalty = s.getAkkumulierteStrafe(d.Id);

            if (time <= 0) {
                return false;
            }

            TimelimitsContainer tlc = s.getWettkampf().getTimelimits();
            return tlc.isBrokenBy(time, penalty, disziplin, s,
                    wk.getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION), d.round);
        }

        @Override
        public void addStrafe(int index, int lane, Strafe strafe) {
            HeatInfo info = owlaeufe.get(index);
            OWDisziplin<T> d = info.Item2;

            ASchwimmer t = swimmers[lane];
            Eingabe e = t.getEingabe(d.Id, true);
            e.addStrafe(strafe);
        }

        @Override
        public void setStrafen(int index, int lane, LinkedList<Strafe> strafen) {
            HeatInfo info = owlaeufe.get(index);
            OWDisziplin<T> d = info.Item2;

            ASchwimmer t = swimmers[lane];
            Eingabe e = t.getEingabe(d.Id, true);
            e.setStrafen(strafen);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void runPenaltyEditor(int index, int lane) {
            HeatInfo info = owlaeufe.get(index);
            OWDisziplin<T> d = info.Item2;
            editor.runPenaltyEditor(wk, swimmers[lane], d.Id);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void runPenaltyPoints(int index, int lane) {
            HeatInfo info = owlaeufe.get(index);
            OWDisziplin<T> d = info.Item2;
            editor.runPenaltyPoints(wk, swimmers[lane], d.Id);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void runPenaltyCode(int index, int lane) {
            HeatInfo info = owlaeufe.get(index);
            OWDisziplin<T> d = info.Item2;
            editor.runPenaltyCode(wk, swimmers[lane], d.Id, wk.getStrafen());
        }

        @Override
        public int getDiscipline(int index) {
            HeatInfo info = owlaeufe.get(heat.getSelectedIndex());
            OWDisziplin<T> d = info.Item2;
            return d.disziplin;
        }

        @Override
        public void zeigeZieleinlauf() {
            HeatInfo info = owlaeufe.get(heat.getSelectedIndex());
            OWLauf<T> lauf = info.Item3;
            if (!lauf.alleZeitenEingegeben()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            new JZieleinlaufDialog<T>(controller.getWindow(), lauf, result -> {
                if (result) {
                    nextHeat();
                }
            }).setVisible(true);
        }

        @Override
        public String getSortingLabel() {
            if (wk.isOpenWater()) {
                return I18n.get("NumberShort");
            }
            return I18n.get("Lane");
        }

        @Override
        public void runMeanTimeEditor(int index, ISimpleCallback<Boolean> iSimpleCallback) {
            // editor.runMeanTimeEditor(swimmers[index], strategy.getDiscipline(index),
            // iSimpleCallback);
        }
    }

}