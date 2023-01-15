/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.zwinput;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.tasks.JTaskPaneGroup;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JZWStatusPanel;
import de.df.jauswertung.print.util.BarcodeUtils;
import de.df.jauswertung.print.util.BarcodeUtils.ZWResult;
import de.df.jauswertung.print.util.BarcodeUtils.ZWResultType;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.graphics.ColorUtils;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JSignal;
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
 * @date 27.03.2010
 */
class JZWBarcodeInputPanel extends JPanel {

    private static final Color GREEN = new Color(100, 222, 75);
    private static final Color YELLOW = new Color(255, 210, 50);
    private static final Color RED = new Color(255, 60, 50);
    private static final Color ORANGE = ColorUtils.calculateColor(YELLOW, RED, 0.5);

    private static final String INPUT = I18n.get("ZWInput");

    private final IPluginManager controller;
    private final CorePlugin core;
    private final PZWInputPlugin parent;

    private JIntegerField input;
    private JButton enter;

    private JLabel startnumber;
    private JLabel name;
    private JLabel organisation;
    private JLabel agegroup;
    private JLabel points;

    private JSignal hlwOk;
    private JSignal hlwDNS;
    private JSignal hlwNok;
    private JSignal barcodeError;

    private JPanel inputPanel = null;
    private JZWStatusPanel overview = null;

    private static long OVERVIEW_REASONS = UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_LOAD_WK
            | UpdateEventConstants.REASON_NEW_LOAD_WK | UpdateEventConstants.REASON_NEW_TN
            | UpdateEventConstants.REASON_NEW_WK | UpdateEventConstants.REASON_PENALTY
            | UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_SWIMMER_CHANGED
            | UpdateEventConstants.REASON_SWIMMER_DELETED;

    JZWBarcodeInputPanel(PZWInputPlugin parent, IPluginManager controller, CorePlugin core) {
        this.controller = controller;
        this.core = core;
        this.parent = parent;
        initPanel();
    }

    private void clear(boolean cleartext) {
        clear(cleartext, true);
    }

    private void clear(boolean cleartext, boolean requestFocus) {
        if (cleartext) {
            input.setText("");
        } else {
            if (requestFocus) {
                input.selectAll();
            }
        }
        if (requestFocus) {
            input.requestFocus();
        }

        startnumber.setText(" ");
        name.setText(" ");
        organisation.setText(" ");
        agegroup.setText(" ");
        points.setText(" ");

        hlwDNS.setEnabled(false);
        hlwOk.setEnabled(false);
        hlwNok.setEnabled(false);
        barcodeError.setEnabled(false);
    }

    private void signalBarcodeError() {
        clear(false);
        barcodeError.setEnabled(true);
    }

    private void signalHlwOk() {
        clear(true);
        hlwOk.setEnabled(true);
    }

    private void signalHlwNok() {
        clear(true);
        hlwNok.setEnabled(true);
    }

    private void signalHlwDNS() {
        clear(true);
        hlwDNS.setEnabled(true);
    }

    private boolean enterValue() {
        if (input.getText().length() == 0) {
            input.requestFocus();
            return false;
        }
        boolean ok = BarcodeUtils.checkZWCode(input.getText());
        if (!ok) {
            Toolkit.getDefaultToolkit().beep();
            signalBarcodeError();
            return false;
        }
        ZWResult result = BarcodeUtils.fromZWCode(input.getText());
        if (result == null) {
            signalBarcodeError();
            return false;
        }
        ASchwimmer s = SearchUtils.getSchwimmer(core.getWettkampf(), result.sn);
        if (s == null) {
            signalBarcodeError();
            return false;
        }
        if (!s.getAK().hasHLW()) {
            signalBarcodeError();
            return false;
        }
        if (result.offset >= s.getMaximaleHLW()) {
            signalBarcodeError();
            return false;
        }

        boolean doit = true;
        // If hlw points are already set:
        if (s.hasHLWSet(result.offset)) {
            boolean ask = false;
            String before = null;
            // Check if points have been entered
            HLWStates state = s.getHLWState(result.offset);
            if ((state == HLWStates.ENTERED) || (state == HLWStates.NICHT_ANGETRETEN)) {
                ZWResultType type = ZWResultType.OK;
                if (state == HLWStates.NICHT_ANGETRETEN) {
                    type = ZWResultType.DNS;
                    before = I18n.get("DidNotStartShort");
                } else {
                    int punkte = (int) Math.round(s.getHLWPunkte(result.offset));
                    before = I18n.get("PointsPenalty", punkte);
                    if (punkte == 200) {
                        type = ZWResultType.OK;
                    } else {
                        type = ZWResultType.NOT_OK;
                    }
                }

                // Check if current result equals new result
                if (type != result.ok) {
                    ask = true;
                }
            } else {
                // Entered value is a penalty
                ask = true;
                before = I18n.get("DisqualificationShort");
            }
            if (ask) {
                String after = "-";
                switch (result.ok) {
                case DNS:
                    after = I18n.get("DidNotStartShort");
                    break;
                case NOT_OK:
                    after = I18n.get("PointsPenalty", "0");
                    break;
                case OK:
                    after = I18n.get("PointsPenalty", "200");
                    break;
                }
                doit = DialogUtils.ask(controller.getWindow(), I18n.get("Question.InputAlreadyPresent", before, after),
                        I18n.get("Question.InputAlreadyPresent.Note", before, after));
            }
        }

        if (doit) {
            switch (result.ok) {
            case DNS:
                s.setHLWPunkte(result.offset, 0);
                s.setHLWState(result.offset, HLWStates.NICHT_ANGETRETEN);
                signalHlwDNS();
                break;
            case NOT_OK:
                s.setHLWState(result.offset, HLWStates.ENTERED);
                s.setHLWPunkte(result.offset, 0);
                signalHlwNok();
                break;
            case OK:
                s.setHLWState(result.offset, HLWStates.ENTERED);
                s.setHLWPunkte(result.offset, 200);
                signalHlwOk();
                break;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(StartnumberFormatManager.format(s));
            if (s.getMaximaleHLW() > 1) {
                sb.append(" ");
                sb.append(StringTools.asText(result.offset));
            }
            startnumber.setText(sb.toString());
            sb.setLength(0);

            sb.append(s.getName());
            if (s instanceof Mannschaft) {
                Mannschaft m = (Mannschaft) s;
                String mitglied = m.getMitgliedsname(result.offset);
                if (mitglied.length() > 0) {
                    sb.append(" (");
                    sb.append(mitglied);
                    sb.append(")");
                }
            }
            name.setText(sb.toString());
            sb.setLength(0);

            sb.append(s.getGliederung());
            if (s.getQualifikationsebene().length() > 0) {
                sb.append(" (");
                sb.append(s.getQualifikationsebene());
                sb.append(")");
            }
            organisation.setText(sb.toString());
            sb.setLength(0);

            agegroup.setText(I18n.getAgeGroupAsString(s));

            NumberFormat nf = NumberFormat.getIntegerInstance();

            points.setText(nf.format(s.getHLWPunkte(result.offset)));

            controller.sendDataUpdateEvent("ChangeZWPoints", UpdateEventConstants.REASON_POINTS_CHANGED, s, -1, parent);
        } else {
            clear(true);
        }
        return true;
    }

    private void initPanel() {
        startnumber = new JLabel();
        name = new JLabel();
        organisation = new JLabel();
        agegroup = new JLabel();
        points = new JLabel();

        hlwDNS = new JSignal(I18n.get("DidNotStart"));
        hlwOk = new JSignal(I18n.get("FullPoints"));
        hlwNok = new JSignal(I18n.get("0Points"));
        barcodeError = new JSignal(I18n.get("BarcodeNotCorrect"));
        hlwDNS.setBasecolor(ORANGE);
        hlwOk.setBasecolor(GREEN);
        hlwNok.setBasecolor(RED);
        barcodeError.setBasecolor(YELLOW);

        inputPanel = new JPanel();
        input = new JIntegerField(false, true);
        input.addFocusListener(new ResettingFocusAdapter());
        input.setValidator(value -> {
            if (input.getText().length() == 0) {
                return true;
            }
            return BarcodeUtils.checkZWCode(value);
        });
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown() && !e.isShiftDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        enterValue();
                        return;
                    }
                }
                if (!e.isControlDown() && !e.isAltDown() && !e.isAltGraphDown() && !e.isShiftDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        enterValue();
                    }
                }
            }
        });

        enter = new JButton(I18n.get("Apply"));
        enter.addFocusListener(new ResettingFocusAdapter());
        enter.addActionListener(e -> {
            enterValue();
        });

        JScrollPane scroller = new JScrollPane(inputPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getVerticalScrollBar().setUnitIncrement(10);
        scroller.getHorizontalScrollBar().setUnitIncrement(10);
        scroller.setBorder(null);

        JPanel in = new JPanel();
        in.setBorder(BorderUtils.createLabeledBorder(INPUT));
        in.setLayout(new FormLayout("0dlu,fill:default:grow,0dlu", "0dlu,fill:default,0dlu"));
        in.add(scroller, CC.xy(2, 2));

        JTaskPaneGroup over = new JTaskPaneGroup();
        over.setUI(new GradientTaskPaneGroupUI());
        over.setTitle(I18n.get("Overview"));
        over.setBackground(in.getBackground());
        over.setOpaque(false);

        overview = new JZWStatusPanel();
        over.add(overview);

        JPanel panel = new JPanel(new FormLayout("4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu"));
        panel.add(over, CC.xy(2, 2));
        panel.add(createInputPanel(), CC.xy(2, 4));
        panel.add(createStatusPanel(), CC.xy(2, 6));

        setLayout(new MigLayout("", "[fill, grow, ::1200lp]", "[fill]"));
        add(panel);

        clear(true);
    }

    private JPanel createInputPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu");
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Input")));

        top.add(new JLabel(I18n.get("Input")), CC.xy(2, 2));
        top.add(input, CC.xy(4, 2));
        top.add(enter, CC.xy(4, 4, "right,fill"));

        return top;
    }

    private JPanel createStatusPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,40dlu,fill:default,4dlu,fill:default:grow,4dlu",
                FormLayoutUtils.createLayoutString(8));
        layout.setColumnGroups(new int[][] { { 2, 6 } });
        layout.setRowGroups(new int[][] { { 2, 4, 6, 8, 10, 12, 14, 16 } });
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("StatusOfLastInput")));

        top.add(hlwOk, CC.xywh(2, 2, 1, 3));
        top.add(hlwNok, CC.xywh(2, 6, 1, 3));
        top.add(hlwDNS, CC.xywh(2, 10, 1, 3));
        top.add(barcodeError, CC.xywh(2, 14, 1, 3));

        top.add(new JLabel(I18n.get("Startnumber")), CC.xy(4, 2));
        top.add(new JLabel(I18n.get("Name")), CC.xy(4, 4));
        top.add(new JLabel(I18n.get("Organisation")), CC.xy(4, 6));
        top.add(new JLabel(I18n.get("AgeGroup")), CC.xy(4, 8));
        top.add(new JLabel(I18n.get("Points")), CC.xy(4, 10));

        top.add(startnumber, CC.xy(6, 2));
        top.add(name, CC.xy(6, 4));
        top.add(organisation, CC.xy(6, 6));
        top.add(agegroup, CC.xy(6, 8));
        top.add(points, CC.xy(6, 10));

        return top;
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
        } else {
            setEnabled(false);
            clear(true);
        }
    }

    final class ResettingFocusAdapter extends FocusAdapter {
        @Override
        public void focusLost(FocusEvent e) {
            SwingUtilities.invokeLater(() -> {
                if (!(input.hasFocus())) {
                    clear(false, false);
                }
            });
        }
    }

    public boolean acceptsInput(String path) {
        return input.hasFocus();
    }
}