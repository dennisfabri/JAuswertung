/*
 * Created on 01.03.2005
 */
package de.df.jauswertung.gui.plugins.editor;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JIntegerField.Validator;
import de.df.jutils.gui.JTimeField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPluginManager;

class JMeanTimeEditor extends JDialog {

    private final class TimeKeyListener extends KeyAdapter {

        private final int x;

        public TimeKeyListener(int index) {
            x = index;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.isConsumed()) {
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume();
                if (x == 0) {
                    integer[1].requestFocus();
                } else {
                    if ((integer[x].getText().length() == 0) || (x + 1 == integer.length)) {
                        doOk();
                    } else {
                        integer[x + 1].requestFocus();
                    }
                }
            }
        }
    }

    private final class TimeListener implements DocumentListener {

        private final int x;

        public TimeListener(int index) {
            x = index;
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
            boolean o = true;
            for (JIntegerField anInteger : integer) {
                o = o && ((anInteger.getText().length() == 0) || ((anInteger.isValidInt() && time[x].isValidValue())));
            }
            ok.setEnabled(o);
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 3256719580843227188L;

    ASchwimmer schwimmer = null;
    int disziplin = 0;
    JIntegerField[] integer = null;
    JTimeField[] time = null;
    JButton ok = null;
    IPluginManager controller = null;
    private ISimpleCallback<Boolean> cb = null;

    /**
     * This is the default constructor
     */
    public JMeanTimeEditor(IPluginManager controller, ASchwimmer s, int disz, ISimpleCallback<Boolean> cb) {
        super(controller.getWindow(), I18n.get("TimeInput"), true);
        if (!s.isDisciplineChosen(disz)) {
            throw new IllegalArgumentException();
        }
        this.controller = controller;
        this.cb = cb;

        schwimmer = s;
        disziplin = disz;

        initialize(controller.getWindow());
        addActions();
    }

    @Override
    public void setVisible(boolean visible) {
        controller.getWindow().setEnabled(!visible);
        super.setVisible(visible);
    }

    private void addActions() {
        WindowUtils.addEscapeAction(this, new Runnable() {
            @Override
            public void run() {
                doCancel();
            }
        });
    }

    void doCancel() {
        setVisible(false);
        if (cb != null) {
            cb.callback(false);
        }
    }

    void doOk() {
        int min = Integer.MAX_VALUE;
        int max = 0;
        int meantime = 0;
        int amount = 0;
        int[] times = new int[integer.length];
        for (int x = 0; x < integer.length; x++) {
            if ((integer[x].getText().length() > 0) && (!(integer[x].isValidInt() && time[x].isValidValue()))) {
                Toolkit.getDefaultToolkit().beep();
                integer[x].requestFocus();
                return;
            }
            int t = time[x].getTimeAsInt();
            if (t > 0) {
                times[amount] = t;
                meantime += t;
                amount++;
                if (t > max) {
                    max = t;
                }
                if (t < min) {
                    min = t;
                }
            }
        }

        if (amount == 0) {
            meantime = 0;
        } else {
            if (amount < 3) {
                meantime = meantime / amount;
            } else {
                Arrays.sort(times);
                meantime = times[amount / 2];
            }
            if (max - min > 30) {
                NumberFormat nf = NumberFormat.getNumberInstance();
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(2);
                double seconds = max - min;
                seconds = seconds / 100.0;

                if (!DialogUtils.askAndWarn(this, I18n.get("TimesFarAway", nf.format(seconds)),
                        I18n.get("TimesFarAway.Note", nf.format(seconds)))) {
                    return;
                }
            }
        }

        int zeit = schwimmer.getZeit(disziplin);
        if (zeit != meantime) {
            schwimmer.setZeit(disziplin, meantime);
            if (SchwimmerUtils.checkTimeAndNotify(this, schwimmer, disziplin)) {
                controller.sendDataUpdateEvent("ChangeTime", UpdateEventConstants.REASON_POINTS_CHANGED, schwimmer,
                        disziplin, null);
            } else {
                schwimmer.setZeit(disziplin, zeit);
                integer[0].requestFocus();
                return;
            }
        }

        setVisible(false);
        if (cb != null) {
            cb.callback(true);
        }
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize(JFrame parent) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderUtils.createSpaceBorder());
        setContentPane(panel);

        ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                doOk();
            }
        });

        JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                doCancel();
            }
        });

        integer = new JIntegerField[3];
        time = new JTimeField[integer.length];
        for (int x = 0; x < integer.length; x++) {
            integer[x] = new JIntegerField(JTimeField.MAX_TIME, true);
            integer[x].setValidator(new Validator() {
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
            integer[x].getDocument().addDocumentListener(new TimeListener(x));
            integer[x].addKeyListener(new TimeKeyListener(x));
            integer[x].setAutoSelectAll(true);

            time[x] = new JTimeField(integer[x]);
        }
        // integer[0].requestFocus();
        if (schwimmer.getZeit(disziplin) == 0) {
            integer[0].setInt(JIntegerField.EMPTY_FIELD);
        } else {
            time[0].setTimeAsInt(schwimmer.getZeit(disziplin));
            integer[1].requestFocus();
        }

        FormLayout buttonLayout = new FormLayout("fill:default:grow,fill:default,4dlu,fill:default", "fill:default");
        buttonLayout.setColumnGroups(new int[][] { { 2, 4 } });
        JPanel buttons = new JPanel(buttonLayout);
        buttons.add(ok, CC.xy(2, 1));
        buttons.add(cancel, CC.xy(4, 1));

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(2 + integer.length));
        // layout.setRowGroups(new int[][] { { 2, 4, 6 } });
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Input")));
        // top.add(new JLabel(I18n.get("Input")), CC.xy(2, 2));
        // top.add(new JLabel(I18n.get("Time")), CC.xy(2, 4));

        for (int x = 0; x < integer.length; x++) {
            top.add(new JLabel(I18n.get("TimeNr", (x + 1))), CC.xy(2, 2 + 2 * x));
            top.add(integer[x], CC.xy(4, 2 + 2 * x));
            top.add(time[x], CC.xy(6, 2 + 2 * x));
        }
        top.add(buttons, CC.xyw(2, 4 + 2 * (integer.length), 3));

        layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4, 6, 8, 10 } });
        JPanel bottom = new JPanel(layout);
        bottom.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information")));
        bottom.add(new JLabel(I18n.get("Name")), CC.xy(2, 2));
        bottom.add(new JLabel(I18n.get("Organisation")), CC.xy(2, 4));
        bottom.add(new JLabel(I18n.get("AgeGroup")), CC.xy(2, 6));
        bottom.add(new JLabel(I18n.get("DisciplineNr")), CC.xy(2, 8));
        bottom.add(new JLabel(I18n.get("DisciplineName")), CC.xy(2, 10));

        bottom.add(new JLabel(schwimmer.getName()), CC.xy(4, 2));
        bottom.add(new JLabel(schwimmer.getGliederung()), CC.xy(4, 4));
        bottom.add(new JLabel(schwimmer.getAK().toString() + " " + I18n.geschlechtToString(schwimmer)), CC.xy(4, 6));
        bottom.add(new JLabel("" + (disziplin + 1)), CC.xy(4, 8));
        bottom.add(new JLabel(schwimmer.getAK().getDisziplin(disziplin, true).getName()), CC.xy(4, 10));

        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);

        pack();
        setResizable(false);
        UIStateUtils.uistatemanage(parent, this, "JMeanTimeEditor");
        pack();

        if (schwimmer.getZeit(disziplin) > 0) {
            integer[1].requestFocus();
        }
    }
}