package de.df.jauswertung.gui.plugins.elektronischezeit;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
import de.df.jauswertung.gui.plugins.elektronischezeit.layer.HeatInfo;
import de.df.jauswertung.gui.plugins.elektronischezeit.layer.LaneInfo;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JTimeField;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.JIntegerField.Validator;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.util.StringTools;

class JHeatPanel<T extends ASchwimmer> extends JPanel {

    final JElektronischeZeit<T> parent;

    private JLabel discipline = new JLabel();
    private JLabel agegroup = new JLabel();

    private JButton previous = null;
    private JButton next = null;
    JComboBox<String> heat = null;

    private JTimePanel<T> time = null;

    JLabel[][] heatinfo = new JLabel[0][0];
    JTimeField[] times = new JTimeField[0];
    JIntegerField[] inputs = new JIntegerField[0];

    boolean updating = true;

    private final IETStrategy strategy;

    public JHeatPanel(JElektronischeZeit<T> parent) {
        this.parent = parent;

        strategy = parent.getStrategy();

        initUI();
    }

    private void initUI() {
        FormLayout layout = new FormLayout("0dlu,fill:default:grow,0dlu",
                "0dlu,fill:default,4dlu,fill:default:grow,0dlu");
        setLayout(layout);

        add(createUpper(), CC.xy(2, 2));
        add(createLower(), CC.xy(2, 4));

        updating = false;

        updateHeat();
    }

    void setTimePanel(JTimePanel<T> t) {
        if (t == null) {
            throw new IllegalArgumentException("Timepanel must not be null");
        }
        time = t;
    }

    private JPanel createHeatSelector() {
        previous = new JTransparentButton(IconManager.getSmallIcon("previous"));
        previous.addActionListener(e -> {
            previousHeat();
        });
        next = new JTransparentButton(IconManager.getSmallIcon("next"));
        next.addActionListener(e -> {
            nextHeat();
        });
        heat = new JComboBox<>(strategy.getHeatnames());
        heat.addActionListener(e -> {
            updateHeat();
        });

        FormLayout layout = new FormLayout("0dlu,fill:default,4dlu,fill:default,4dlu,fill:default,0dlu",
                "0dlu,fill:default,0dlu");
        JPanel p = new JPanel(layout);

        p.add(previous, CC.xy(2, 2));
        p.add(heat, CC.xy(4, 2));
        p.add(next, CC.xy(6, 2));

        return p;
    }

    private JPanel createUpper() {
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                FormLayoutUtils.createLayoutString(3));
        JPanel p = new JPanel(layout);
        p.setBorder(BorderUtils.createLabeledBorder(I18n.get("SelectHeat")));

        p.add(new JLabel(I18n.get("Heat")), CC.xy(2, 2, "left,fill"));
        p.add(createHeatSelector(), CC.xy(4, 2, "right,fill"));
        p.add(new JLabel(I18n.get("Discipline")), CC.xy(2, 4, "left,fill"));
        p.add(discipline, CC.xy(4, 4, "right,fill"));
        p.add(new JLabel(I18n.get("AgeGroup")), CC.xy(2, 6, "left,fill"));
        p.add(agegroup, CC.xy(4, 6, "right,fill"));

        return p;

    }

    private JPanel createLower() {
        String[] ids = new String[] { "Lane", "Name", "Organisation", "AgeGroup", "Input", "Time", "Penalty", "",
                "Lane" };
        String[] align = new String[] { "center", "left", "left", "center", "fill", "fill", "fill", "fill", "center" };
        int[] textcolumns = new int[] { 0, 1, 2, 3, 6, 8 };

        int lanes = strategy.getLanecount();
        FormLayout layout = new FormLayout(
                "4dlu,fill:default,4dlu,fill:default:grow,4dlu," + "fill:default:grow,4dlu,fill:default:grow,4dlu,"
                        + "fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,"
                        + "fill:default,4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(1 + lanes));
        JPanel p = new JPanel(layout);
        p.setBorder(BorderUtils.createLabeledBorder(I18n.get("Heat")));

        heatinfo = new JLabel[textcolumns.length][lanes];
        inputs = new JIntegerField[lanes];
        times = new JTimeField[lanes];

        for (int x = 0; x < ids.length; x++) {
            String text = (ids[x].length() > 0 ? I18n.get(ids[x]) : "");
            p.add(new JLabel(text), CC.xy(2 + 2 * x, 2, "center,fill"));
        }

        for (int x = 0; x < textcolumns.length; x++) {
            for (int y = 0; y < lanes; y++) {
                int col = textcolumns[x];
                heatinfo[x][y] = new JLabel();
                p.add(heatinfo[x][y], CC.xy(2 + 2 * col, 4 + 2 * y, align[col] + ",fill"));
            }
        }
        for (int y = 0; y < lanes; y++) {
            inputs[y] = new JIntegerField(JIntegerField.EMPTY_FIELD, JTimeField.MAX_TIME, false, true);
            inputs[y].setToolTipText(I18n.getToolTip("TimeInputField"));
            inputs[y].setValidator((Validator)value -> {
                value = value / 100;
                if ((value % 100) >= 60) {
                    return false;
                }
                value = value / 100;
                return value < 1000;
            });
            inputs[y].getDocument().addDocumentListener(new TimeListener(y));
            inputs[y].setAutoSelectAll(true);
            inputs[y].setHorizontalAlignment(SwingConstants.RIGHT);

            times[y] = new JTimeField(inputs[y]);
            p.add(inputs[y], CC.xy(10, 4 + 2 * y));
            p.add(times[y], CC.xy(12, 4 + 2 * y));

            heatinfo[0][y].setText("" + (y + 1));
            heatinfo[heatinfo.length - 1][y].setText("" + (y + 1));
        }

        return p;
    }

    void previousHeat() {
        heat.setSelectedIndex(heat.getSelectedIndex() - 1);
    }

    void nextHeat() {
        heat.setSelectedIndex(heat.getSelectedIndex() + 1);
    }

    void updateHeat() {
        if (updating) {
            return;
        }
        updating = true;
        try {
            int index = heat.getSelectedIndex();
            previous.setEnabled(index > 0);
            next.setEnabled(index + 1 < heat.getItemCount());

            HeatInfo heat = strategy.getHeat(index);

            agegroup.setText(heat.getAltersklasse());
            discipline.setText(heat.getDisziplin());

            for (int x = 0; x < heat.getLanecount(); x++) {
                // T t = lauf.getSchwimmer(x);
                LaneInfo lane = heat.getLanes()[x];
                inputs[x].setEnabled(lane != null);
                if (lane != null) {
                    heatinfo[1][x].setText(lane.getName());
                    heatinfo[2][x].setText(lane.getGliederung());
                    heatinfo[3][x].setText(lane.getAgegroup());
                    heatinfo[4][x].setText(lane.getPenalty());

                    if (lane.getTime() <= 0) {
                        // times[x].setTimeAsInt(0);
                        inputs[x].setText("");
                    } else {
                        times[x].setTimeAsInt(lane.getTime());
                    }
                } else {
                    for (int y = 1; y < heatinfo.length - 1; y++) {
                        heatinfo[y][x].setText("");
                    }
                }
            }
            if (time != null) {
                time.follow(heat.getEvent(), heat.getLaufnummer(), heat.getLaufbuchstabe());
            }
        } finally {
            updating = false;
        }
    }

    private class TimeListener extends KeyAdapter implements DocumentListener {

        int index = 0;

        public TimeListener(int x) {
            if ((x < 0) || (x >= inputs.length)) {
                throw new IllegalArgumentException("Index to large! Should be 0 <=" + x + "<" + inputs.length + ".");
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
                    // if (fl[index].checkTime()) {
                    // previousHeat(false);
                    // }
                }
                e.consume();
                break;
            case KeyEvent.VK_DOWN:
                if (index + 1 < times.length) {
                    inputs[index + 1].requestFocus();
                } else {
                    // if (fl[index].checkTime()) {
                    // nextHeat();
                    // }
                }
                e.consume();
                break;
            default:
                break;
            }
        }

        @Override
        public void insertUpdate(DocumentEvent arg0) {
            int heatnr = heat.getSelectedIndex();
            // @SuppressWarnings("rawtypes")
            // Lauf lauf = wk.getLaufliste().getLaufliste().get(heatnr);
            // ASchwimmer s = lauf.getSchwimmer(index);
            // if (s == null) {
            // return;
            // }
            if (inputs[index].isValidInt() && times[index].isValidValue()) {
                changeTime();
                // updateHeat();
                return;
            }
            String zeit = inputs[index].getText();
            if (zeit.indexOf("p") > -1) {
                setPenaltyPoints(heatnr, index, zeit);
                return;
            }
            if (zeit.indexOf("c") > -1) {
                setPenaltyCode(heatnr, index, zeit);
                return;
            }
            if ((zeit.indexOf(",") > -1) || (zeit.indexOf("z") > -1)) {
                showZieleinlauf(heatnr, index, zeit);
                return;
            }
            if (zeit.indexOf("#") > -1) {
                setNoPenalty(heatnr, index, zeit);
                return;
            }
            if (zeit.indexOf("m") > -1) {
                setMeanTime(heatnr, index, zeit);
                return;
            }
            if (zeit.indexOf("d") > -1) {
                setDisqualifikation(heatnr, index, zeit);
                return;
            }
            if (zeit.indexOf("n") > -1) {
                setNA(heatnr, index, zeit);
                return;
            }
        }

        private void setPenaltyPoints(int heatnr, int row, String zeit) {
            if ((zeit.length() == 1) || StringTools.isInteger(StringTools.removeAll(zeit, 'p'))) {
                // SwingUtilities.invokeLater(new Runnable() {
                // @Override
                // public void run() {
                // String x = StringTools.removeAll(
                // inputs[index].getText(), 'p');
                // int heatnr = heat.getSelectedIndex();
                // Lauf lauf = wk.getLaufliste().getLaufliste()
                // .get(heatnr);
                // ASchwimmer s = lauf.getSchwimmer(index);
                // inputs[index].setText(x);
                // dialog.getEditor().runPenaltyPoints(s,
                // lauf.getDisznummer(index));
                // }
                // });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'p');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        private void setPenaltyCode(int heatnr, int row, String zeit) {
            if ((zeit.length() == 1) || StringTools.isInteger(StringTools.removeAll(zeit, 'c'))) {
                // SwingUtilities.invokeLater(new Runnable() {
                // @Override
                // public void run() {
                // String x = StringTools.removeAll(
                // inputs[index].getText(), 'c');
                // int heatnr = heat.getSelectedIndex();
                // Lauf lauf = wk.getLaufliste().getLaufliste()
                // .get(heatnr);
                // ASchwimmer s = lauf.getSchwimmer(index);
                // inputs[index].setText(x);
                // dialog.getEditor().runPenaltyCode(s,
                // lauf.getDisznummer(index), wk.getStrafen());
                // }
                // });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'c');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        private void setMeanTime(int heatnr, int row, String zeit) {
            if ((zeit.length() == 1) || StringTools.isInteger(StringTools.removeAll(zeit, 'm'))) {
                // SwingUtilities.invokeLater(new Runnable() {
                // @Override
                // public void run() {
                // String x = StringTools.removeAll(
                // inputs[index].getText(), 'm');
                // inputs[index].setText(x);
                // int heatnr = heat.getSelectedIndex();
                // Lauf lauf = wk.getLaufliste().getLaufliste()
                // .get(heatnr);
                // ASchwimmer s = lauf.getSchwimmer(index);
                // dialog.getEditor().runMeanTimeEditor(s,
                // lauf.getDisznummer(index), null);
                // }
                // });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'm');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        private void showZieleinlauf(int heatnr, int row, String zeit) {
            if ((zeit.length() == 1)
                    || StringTools.isInteger(StringTools.removeAll(StringTools.removeAll(zeit, ','), 'z'))) {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(StringTools.removeAll(inputs[index].getText(), ','), 'z');
                    inputs[index].setText(x);
                    // zeigeZieleinlauf();
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
        private void setNoPenalty(int heatnr, int row, String zeit) {
            if ((zeit.length() == 1) || StringTools.isInteger(StringTools.removeAll(zeit, '#'))) {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), '#');
                    inputs[index].setText(x);
                    heatinfo[4][index].setText("");
                });
                strategy.setNoPenalty(heatnr, row);
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
        private void setDisqualifikation(int heatnr, int row, String zeit) {
            if ((zeit.length() == 1) || StringTools.isInteger(StringTools.removeAll(zeit, 'd'))) {
                strategy.setDisqualification(heatnr, row);
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'd');
                    inputs[index].setText(x);
                    updateHeat();
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'd');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        /**
         * @param zeit
         */
        private void setNA(int heatnr, int row, String zeit) {
            if ((zeit.equals("n"))) {
                strategy.setNA(heatnr, row);
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'n');
                    inputs[index].setText(x);
                    updateHeat();
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    String x = StringTools.removeAll(inputs[index].getText(), 'n');
                    inputs[index].setText(x);
                    Toolkit.getDefaultToolkit().beep();
                });
            }
        }

        private void changeTime() {
            synchronized (parent.getController()) {
                if (updating) {
                    return;
                }
                int heatnr = heat.getSelectedIndex();

                int timevalue = times[index].getTimeAsInt();

                if (strategy.getTime(heatnr, index) != timevalue) {
                    strategy.setTime(heatnr, index, timevalue);
                }
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

    public String getHeatname() {
        return strategy.getHeatname(heat.getSelectedIndex());
    }

    public boolean checkTimes(int[] result) {
        return strategy.checkTimes(heat.getSelectedIndex(), result);
    }

    public void setTimes(int[] result) {
        strategy.setTimes(heat.getSelectedIndex(), result);
        updateHeat();
        parent.setChanging();
        parent.sendDataUpdate();

    }
}