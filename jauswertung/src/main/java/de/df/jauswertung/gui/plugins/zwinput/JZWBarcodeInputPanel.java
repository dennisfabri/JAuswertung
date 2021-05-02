/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.zwinput;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.tasks.JTaskPaneGroup;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.http.HttpServerPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JZWStatusPanel;
import de.df.jauswertung.util.BarcodeUtils;
import de.df.jauswertung.util.BarcodeUtils.ZWResult;
import de.df.jauswertung.util.BarcodeUtils.ZWResultType;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.graphics.ColorUtils;
import de.df.jutils.gui.JIcon;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JIntegerField.Validator;
import de.df.jutils.gui.JSignal;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.plaf.GradientTaskPaneGroupUI;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Mueller
 * @date 27.03.2010
 */
class JZWBarcodeInputPanel extends JPanel {

    // Light
    // private static final Color YELLOW = new Color(255, 230, 100);
    // private static final Color RED = new Color(255, 150, 100);
    // private static final Color GREEN = new Color(176, 255, 124);

    // Intensive
    private static final Color     GREEN            = new Color(100, 222, 75);
    private static final Color     YELLOW           = new Color(255, 210, 50);
    private static final Color     RED              = new Color(255, 60, 50);
    private static final Color     ORANGE           = ColorUtils.calculateColor(YELLOW, RED, 0.5);

    private static final String    INPUT            = I18n.get("ZWInput");

    final IPluginManager           controller;
    private final CorePlugin       core;
    private final HttpServerPlugin http;
    final PZWInputPlugin           parent;

    JIntegerField                  input;
    JButton                        enter;

    private JLabel                 startnumber;
    private JLabel                 name;
    private JLabel                 organisation;
    private JLabel                 agegroup;
    private JLabel                 points;

    private JSignal                hlwOk;
    private JSignal                hlwDNS;
    private JSignal                hlwNok;
    private JSignal                barcodeError;

    private JIcon                  appcode;
    private JLabel                 appcodeSpacer;

    JPanel                         panel            = null;
    private JPanel                 inputPanel       = null;
    private JZWStatusPanel         overview         = null;

    private static long            OVERVIEW_REASONS = UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_LOAD_WK
            | UpdateEventConstants.REASON_NEW_LOAD_WK | UpdateEventConstants.REASON_NEW_TN | UpdateEventConstants.REASON_NEW_WK
            | UpdateEventConstants.REASON_PENALTY | UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_SWIMMER_CHANGED
            | UpdateEventConstants.REASON_SWIMMER_DELETED;

    public JZWBarcodeInputPanel(PZWInputPlugin parent, IPluginManager controller, CorePlugin core, HttpServerPlugin http) {
        this.controller = controller;
        this.core = core;
        this.http = http;
        this.parent = parent;
        initPanel();
    }

    void prepare() {
        clear(true);
    }

    void clear(boolean cleartext) {
        clear(cleartext, true);
    }

    void clear(boolean cleartext, boolean requestFocus) {
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

    void signalBarcodeError() {
        clear(false);
        barcodeError.setEnabled(true);
    }

    void signalHlwOk() {
        clear(true);
        hlwOk.setEnabled(true);
    }

    void signalHlwNok() {
        clear(true);
        hlwNok.setEnabled(true);
    }

    void signalHlwDNS() {
        clear(true);
        hlwDNS.setEnabled(true);
    }

    boolean enterValue() {
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

    void initPanel() {
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

        appcode = new JIcon();
        appcode.setBorder(new LineBorder(UIManager.getColor("InternalFrame.activeBorderColor"), 1));
        appcode.setToolTipText(
                "<html><p>Mit dem AppCode können Smartphones mit JAuswertung verbunden werden und als Barcodescanner dienen.</p><br/><p>Zur Zeit suche ich noch nach Entwicklern, um die notwendigen Apps umzusetzen. Solltest Du jemanden kennen, der sich in diesem Bereich auskennt, frag ihn doch einfach mal. Ich habe mit Smartphones noch einiges mehr vor.</p></html>");
        appcode.setVisible(false);

        inputPanel = new JPanel();
        input = new JIntegerField(false, true);
        input.addFocusListener(new ResettingFocusAdapter());
        input.setValidator(new Validator() {
            @Override
            public boolean validate(int value) {
                if (input.getText().length() == 0) {
                    return true;
                }
                return BarcodeUtils.checkZWCode(value);
            }
        });
        input.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                appcode.setVisible(false);
                appcodeSpacer.setVisible(true);
            }

            @Override
            public void focusGained(FocusEvent e) {
                boolean ok = acceptsInput("");
                appcode.setVisible(ok);
                appcodeSpacer.setVisible(!ok);
            }
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
        enter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enterValue();
            }
        });

        JScrollPane scroller = new JScrollPane(inputPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu");
        setLayout(layout);
        add(over, CC.xyw(2, 2, 3));
        add(createInputPanel(), CC.xyw(2, 4, 3));
        add(createStatusPanel(), CC.xy(2, 6));
        add(createAppcodePanel(), CC.xy(4, 6));

        clear(true);
    }

    private JPanel createInputPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default,4dlu");
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Input")));

        top.add(new JLabel(I18n.get("Input")), CC.xy(2, 2));
        top.add(input, CC.xy(4, 2));
        top.add(enter, CC.xy(4, 4, "right,fill"));

        return top;
    }

    private JPanel createStatusPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,40dlu,fill:default,4dlu,fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(8));
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

    private JPanel createAppcodePanel() {
        FormLayout layout = new FormLayout("4dlu,center:default:grow,4dlu", "4dlu,center:default:grow,4dlu");
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Appcode")));

        appcodeSpacer = new JLabel();
        appcodeSpacer.setOpaque(true);
        appcodeSpacer.setBackground(Color.WHITE);
        appcodeSpacer.setPreferredSize(new Dimension(200, 200));
        appcodeSpacer.setMinimumSize(new Dimension(200, 200));
        appcodeSpacer.setMaximumSize(new Dimension(200, 200));
        // appcodeSpacer
        // .setText("<html><p>Mit dem Appcode können Sie dem 'JAuswertung Extender' (Android-App) die Eingabe von Barcodes über ein WLAN oder Kabelnetzwerk
        // erlauben.</p><p>&nbsp;</p><p>Um den Appcode zu aktivieren, muss der Fokus im Feld 'Eingabe' und der HTTP-Server aktiviert sein.</p></html>");
        appcodeSpacer.setText(
                "<html><p>Mit dem AppCode können Smartphones mit JAuswertung verbunden werden und als Barcodescanner dienen.</p><br/><p>Zur Zeit suche ich noch nach Entwicklern, um die notwendigen Apps umzusetzen. Solltest Du jemanden kennen, der sich in diesem Bereich auskennt, frag ihn doch einfach mal. Ich habe mit Smartphones noch einiges mehr vor.</p></html>");
        appcodeSpacer.setBorder(new CompoundBorder(new LineBorder(UIManager.getColor("InternalFrame.activeBorderColor"), 1), BorderUtils.createSpaceBorder()));

        top.add(appcodeSpacer, CC.xy(2, 2));
        top.add(appcode, CC.xy(2, 2));

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
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (!(input.hasFocus())) {
                        clear(false, false);
                    }
                }
            });
        }
    }

    public boolean input(String path) {
        if (acceptsInput(path)) {
            input.setText(path);
            return enterValue();
        }
        return false;
    }

    public boolean acceptsInput(String path) {
        return input.hasFocus() && http.isEnabled();
    }

    public void setAppcode(Image qrCode) {
        appcode.setImage(qrCode);
    }
}