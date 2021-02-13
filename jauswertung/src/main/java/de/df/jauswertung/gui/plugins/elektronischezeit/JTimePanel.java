package de.df.jauswertung.gui.plugins.elektronischezeit;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.util.StringTools;
import de.dm.ares.data.Heat;

class JTimePanel<T extends ASchwimmer> extends JPanel {

    private HeatMatchingMode            directMatching = HeatMatchingMode.Heat2Competition;

    private final JHeatPanel<T>         heat;
    private final JElektronischeZeit<T> parent;
    private int                         lanesamount    = 0;

    private JPanel                      upper, lower;
    private JButton                     previous;
    private JButton                     next;
    private JComboBox<String>           current;
    private JButton                     enter;

    private JLabel[]                    lanes;
    private JComboBox<String>[]         times;
    private JComboBox<String>[]         penalty;

    public JTimePanel(JElektronischeZeit<T> parent, JHeatPanel<T> heat) {
        setBorder(BorderUtils.createLabeledBorder(I18n.get("ElektronischeZeitnahme")));
        this.parent = parent;
        this.heat = heat;

        initUI();
    }

    public void setDirectMatching(HeatMatchingMode d) {
        directMatching = d;
    }

    private void initUI() {
        createUpper();
        createLower();

        enter = new JButton(I18n.get("Enter"));
        enter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enterTimes();
            }
        });

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,12dlu,fill:default,4dlu,fill:default:grow,4dlu");

        setLayout(layout);

        add(upper, CC.xy(2, 2));
        add(lower, CC.xy(2, 4));
        add(enter, CC.xy(2, 6, "right,bottom"));
    }

    private void createLower() {
        lower = new JPanel();
        updateLower(8);
    }

    @SuppressWarnings("unchecked")
    private void updateLower(int newlanes) {
        if (newlanes == lanesamount) {
            return;
        }

        lanesamount = newlanes;

        lower.removeAll();

        FormLayout layout = new FormLayout("0dlu,fill:default,4dlu,right:default:grow,4dlu,fill:default:grow,0dlu",
                FormLayoutUtils.createLayoutString(lanesamount + 1, 4, 0));
        lower.setLayout(layout);

        lower.add(new JLabel(I18n.get("Lane")), CC.xy(2, 2));
        lower.add(new JLabel(I18n.get("Time")), CC.xy(4, 2, "center,fill"));
        lower.add(new JLabel(I18n.get("Penalty")), CC.xy(6, 2, "center,fill"));

        lanes = new JLabel[lanesamount];
        times = new JComboBox[lanesamount];
        penalty = new JComboBox[lanesamount];
        for (int x = 0; x < lanesamount; x++) {
            lanes[x] = new JLabel("" + (x + 1));
            times[x] = new JComboBox<String>();
            penalty[x] = new JComboBox<String>(new String[] { "-" });
            times[x].setEnabled(false);
            penalty[x].setEnabled(false);

            lower.add(lanes[x], CC.xy(2, 4 + 2 * x));
            lower.add(times[x], CC.xy(4, 4 + 2 * x, "fill,fill"));
            lower.add(penalty[x], CC.xy(6, 4 + 2 * x));
        }
    }

    private void createUpper() {
        previous = new JTransparentButton(IconManager.getSmallIcon("previous"));
        previous.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previousHeat();
            }
        });
        next = new JTransparentButton(IconManager.getSmallIcon("next"));
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextHeat();
            }
        });
        current = new JComboBox<String>();
        current.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateHeat();
            }
        });

        FormLayout layout = new FormLayout(
                "0dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,0dlu",
                "0dlu,fill:default,0dlu");
        upper = new JPanel(layout);

        upper.add(new JLabel(I18n.get("Heat")), CC.xy(2, 2));
        upper.add(previous, CC.xy(4, 2));
        upper.add(current, CC.xy(6, 2));
        upper.add(next, CC.xy(8, 2));

        previous.setEnabled(false);
        current.setEnabled(false);
        next.setEnabled(false);
    }

    void updateTimes() {
        int index = Math.max(0, current.getSelectedIndex());
        Heat[] heats = parent.getHeats();
        String[] entries = new String[heats.length];
        for (int x = 0; x < heats.length; x++) {
            entries[x] = "" + heats[x].getEvent() + "/" + heats[x].getHeat();
        }
        current.setModel(new DefaultComboBoxModel<String>(entries));
        if (index >= entries.length) {
            index = entries.length - 1;
        }
        if (index >= 0) {
            current.setSelectedIndex(index);
        }
        current.setEnabled(entries.length > 0);
        updateHeat();
    }

    void updateHeat() {
        int index = current.getSelectedIndex();
        previous.setEnabled(index > 0);
        next.setEnabled(index + 1 < current.getItemCount());

        Heat[] heats = parent.getHeats();

        if ((index < 0) || (index >= heats.length)) {
            // No heatdata found
            for (int x = 0; x < times.length; x++) {
                times[x].setModel(new DefaultComboBoxModel<String>(new String[] { "-" }));
                times[x].setSelectedIndex(0);
                times[x].setEnabled(false);

                penalty[x].setSelectedIndex(0);
            }
        } else {
            Heat h = heats[index];
            updateLower(h.getMaxLane() + 1);
            long[][] t = h.getTimes();
            for (int x = 0; x <= h.getMaxLane(); x++) {
                String[] tx = new String[t[x].length];
                for (int y = 0; y < tx.length; y++) {
                    tx[y] = StringTools.zeitString(t[x][y]);
                }
                if (tx.length == 0) {
                    tx = new String[] { "-" };
                    times[x].setEnabled(false);
                } else {
                    times[x].setEnabled(true);
                }
                times[x].setModel(new DefaultComboBoxModel<String>(tx));
                times[x].setSelectedIndex(tx.length - 1);
                times[x].setEnabled(tx.length > 1);

                penalty[x].setSelectedIndex(0);
            }
        }
    }

    void nextHeat() {
        int index = current.getSelectedIndex();
        if (index < current.getItemCount() - 1) {
            current.setSelectedIndex(index + 1);
        }
    }

    void previousHeat() {
        int index = current.getSelectedIndex();
        if (index > 0) {
            current.setSelectedIndex(index - 1);
        }
    }

    void enterTimes() {
        Heat[] heats = parent.getHeats();
        if ((heats == null) || (heats.length == 0)) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        int index = Math.max(0, current.getSelectedIndex());
        Heat h = heats[index];

        if (!h.fits(heat.getHeatname())) {
            boolean ok = DialogUtils.ask(parent, I18n.get("Question.HeatIDsDoNotMatch"),
                    I18n.get("Question.HeatIDsDoNotMatch.Note"));
            if (!ok) {
                return;
            }
        }
        if ((index == heats.length - 1) && (h.hasMissingTime(lanes.length))) {
            boolean ok = DialogUtils.ask(parent, I18n.get("Question.HeatMaybeIncomplete"),
                    I18n.get("Question.HeatMaybeIncomplete.Note"));
            if (!ok) {
                return;
            }
        }
        long[][] tx = h.getTimes();
        int[] result = new int[h.getMaxLane() + 1];
        for (int x = 0; x < result.length; x++) {
            if (tx[x].length > 0) {
                int y = Math.max(0, times[x].getSelectedIndex());
                result[x] = (int) tx[x][y];
            } else {
                result[x] = 0;
            }
        }
        if (!heat.checkTimes(result)) {
            boolean ok = DialogUtils.ask(parent, I18n.get("Question.HeatHasLowOrHighTimes"),
                    I18n.get("Question.HeatHasLowOrHighTimes.Note"));
            if (!ok) {
                return;
            }

        }

        heat.setTimes(result);

        parent.update();
        updateTimes();
    }

    private boolean timesExists(String t) {
        for (int x = 0; x < current.getModel().getSize(); x++) {
            if (t.equals(current.getModel().getElementAt(x))) {
                return true;
            }
        }
        return false;
    }

    void follow(int event, int lauf, int offset) {
        if (current.getModel().getSize() == 0) {
            return;
        }
        String s;
        switch (directMatching) {
        default:
        case Heat2Competition: {
            s = "" + lauf + "/" + (offset + 1);
            while (!timesExists(s)) {
                if (offset > 0) {
                    offset--;
                } else {
                    if (lauf > 1) {
                        lauf--;
                    } else {
                        s = current.getItemAt(0);
                        break;
                        // current.setSelectedIndex(0);
                        // return;
                    }
                }
                s = "" + lauf + "/" + (offset + 1);
            }
        }
            break;
        case HeatModulo1002Competition: {
            offset = (lauf / 100) - 1;
            lauf = lauf % 100;
            int laufx = lauf;

            s = "" + lauf + "/" + (offset + 1);
            while (!timesExists(s)) {
                if (lauf > 1) {
                    lauf--;
                } else {
                    if (offset > 1) {
                        offset--;
                        lauf = laufx;
                    } else {
                        s = current.getItemAt(0);
                        break;
                        // current.setSelectedIndex(0);
                        // return;
                    }
                }
                s = "" + lauf + "/" + (offset + 1);
            }
            break;
        }
        case Heat2Heat: {
            s = "" + event + "/" + lauf;
            while (!timesExists(s)) {
                if (lauf > 1) {
                    lauf--;
                } else {
                    if (event > 1) {
                        event--;
                        lauf = 100;
                    } else {
                        s = current.getItemAt(0);
                        break;
                        // current.setSelectedIndex(0);
                        // return;
                    }
                }
                s = "" + event + "/" + lauf;
            }

        }
        }
        current.setSelectedItem(s);
    }
}