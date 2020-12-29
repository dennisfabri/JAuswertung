/*
 * Created on 13.11.2003
 */
package de.df.jauswertung.gui.akeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JIntegerField.Validator;
import de.df.jutils.gui.JTimeField;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.layout.FormLayoutUtils;

/**
 * @author Dennis Fabri
 */
class DisziplinenPanel extends JPanel {

    private final class MoveKeyListener implements KeyListener {

        private final int x;
        private final int y;

        public MoveKeyListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            moveFocus(e.getKeyCode(), x, y);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // Nothing to do
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // Nothing to do
        }
    }

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long         serialVersionUID = 4050199739180266296L;

    JLabel                            sex1label        = new JLabel(I18n.get("Sex1"));
    JLabel                            sex2label        = new JLabel(I18n.get("Sex2"));

    private LinkedList<JTextField>    disziplinen;

    private LinkedList<JIntegerField> lengths;
    private LinkedList<JIntegerField> laps;

    private LinkedList<JTimeField>    recwt;
    private LinkedList<JTimeField>    recmt;

    private LinkedList<JIntegerField> recw;
    private LinkedList<JIntegerField> recm;

    private LinkedList<int[]>         roundsm;
    private LinkedList<int[]>         roundIdsm;
    private LinkedList<int[]>         roundsw;
    private LinkedList<int[]>         roundIdsw;

    private int                       count;

    private JButton                   plusButton;

    private JButton                   minusButton;

    private AKEditorPanel             parent;

    public DisziplinenPanel(AKEditorPanel p, Altersklasse aks) {
        parent = p;
        disziplinen = new LinkedList<JTextField>();
        recw = new LinkedList<JIntegerField>();
        recm = new LinkedList<JIntegerField>();
        recwt = new LinkedList<JTimeField>();
        recmt = new LinkedList<JTimeField>();
        roundsw = new LinkedList<int[]>();
        roundIdsw = new LinkedList<int[]>();
        roundsm = new LinkedList<int[]>();
        roundIdsm = new LinkedList<int[]>();

        lengths = new LinkedList<JIntegerField>();
        laps = new LinkedList<JIntegerField>();

        count = 0;
        plusButton = new JTransparentButton();
        minusButton = new JTransparentButton();

        if (aks != null) {
            count = aks.getDiszAnzahl();
        }
        prepareComponents(aks);
        initLayout(count);
        initGUI(count);
    }

    private static String generateRowLayout(int number) {
        String s = "4dlu,fill:default,4dlu,fill:default,4dlu";
        for (int x = 0; x < number; x++) {
            s += ",fill:default,4dlu";
        }
        s += ",fill:default,4dlu";
        return s;
    }

    private void initLayout(int number) {
        FormLayout layout = new FormLayout(
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                generateRowLayout(number));
        layout.setColumnGroups(new int[][] { { 4, 8 }, { 6, 10 } });
        FormLayoutUtils.setRowGroups(layout, 6, number);
        setLayout(layout);
    }

    void addRow() {
        if (count == disziplinen.size()) {
            disziplinen.addLast(createTextField(0, disziplinen.size()));
            recw.addLast(createIntegerField(1, recw.size(), false));
            recm.addLast(createIntegerField(2, recm.size(), false));
            lengths.addLast(createIntegerField(3, lengths.size(), true));
            laps.addLast(createIntegerField(4, laps.size(), true));
            recwt.addLast(createTimeField(recw.getLast()));
            recmt.addLast(createTimeField(recm.getLast()));
            roundsw.addLast(new int[0]);
            roundIdsw.add(new int[1]);
            roundsm.addLast(new int[0]);
            roundIdsm.add(new int[1]);
        }
        count++;
        initLayout(count);
        initGUI(count);
        updateUI();
        plusButton.requestFocus();
        parent.setDisciplineCount(count);
    }

    void notifyChange() {
        parent.notifyChange();
    }

    private JTextField createTextField(int x, int y) {
        JTextField tf = this.createTextField();
        addListeners(tf, x, y);
        return tf;
    }

    private JTextField createTextField() {
        JWarningTextField tf = new JWarningTextField(true, true);
        tf.setAutoSelectAll(true);
        return tf;
    }

    private void addListeners(JTextField df, int x, int y) {
        df.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                notifyChange();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
        });
        df.addKeyListener(new MoveKeyListener(x, y));
    }

    private JIntegerField createIntegerField(int x, int y, boolean required) {
        JIntegerField jf = createIntegerField(required);
        addListeners(jf, x, y);
        return jf;
    }

    private JIntegerField createIntegerField(boolean required) {
        JIntegerField df = new JIntegerField(JIntegerField.NO_MAXVALUE, JTimeField.MAX_TIME, required, required);
        if (!required) {
            df.setValidator(new Validator() {
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
        } else {
            df.setValidator(new Validator() {
                @Override
                public boolean validate(int value) {
                    return value > 0;
                }
            });
        }
        df.setColumns(8);
        df.setHorizontalAlignment(SwingConstants.RIGHT);
        df.setAutoSelectAll(true);
        return df;
    }

    private JTimeField createTimeField(JIntegerField jtf) {
        return new JTimeField(jtf);
    }

    void removeRow() {
        count--;
        initLayout(count);
        initGUI(count);
        updateUI();
        minusButton.requestFocus();
        parent.setDisciplineCount(count);
    }

    private void prepareComponents(Altersklasse aks) {
        plusButton.setIcon(IconManager.getSmallIcon("more"));
        plusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addRow();
                notifyChange();
            }
        });
        minusButton.setIcon(IconManager.getSmallIcon("less"));
        minusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeRow();
                notifyChange();
            }
        });

        if (aks == null) {
            return;
        }

        count = aks.getDiszAnzahl();

        for (int y = 0; y < count; y++) {
            Disziplin w = aks.getDisziplin(y, false);
            Disziplin m = aks.getDisziplin(y, true);
            JTextField name = createTextField();
            name.setText(w.getName());
            addListeners(name, 0, y);
            disziplinen.addLast(name);

            JIntegerField df = createIntegerField(false);
            recw.addLast(df);
            roundsw.addLast(w.getRunden());
            roundIdsw.addLast(w.getRundenIds());
            JTimeField tf = createTimeField(df);
            tf.setTimeAsInt(w.getRec());
            recwt.addLast(tf);
            addListeners(df, 1, y);

            df = createIntegerField(false);
            recm.addLast(df);
            roundsm.addLast(m.getRunden());
            roundIdsm.addLast(m.getRundenIds());
            tf = createTimeField(df);
            tf.setTimeAsInt(m.getRec());
            addListeners(df, 2, y);
            recmt.addLast(tf);

            df = createIntegerField(true);
            df.setInt(w.getLaenge());
            addListeners(df, 3, y);
            lengths.addLast(df);

            df = createIntegerField(true);
            df.setInt(w.getLaps());
            addListeners(df, 4, y);
            laps.addLast(df);
        }
    }

    private void initGUI(int number) {
        removeAll();

        add(new JLabel(I18n.get("Name")), CC.xywh(2, 2, 1, 3, "center,center"));
        add(new JLabel(I18n.get("Rec-Value")), CC.xyw(4, 2, 7, "center,center"));
        add(sex1label, CC.xyw(4, 4, 3, "center,center"));
        add(sex2label, CC.xyw(8, 4, 3, "center,center"));

        add(new JLabel(I18n.get("Timekeeping")), CC.xyw(12, 2, 3, "center,center"));
        add(new JLabel(I18n.get("Length")), CC.xy(12, 4, "center,center"));
        add(new JLabel(I18n.get("Members")), CC.xy(14, 4, "center,center"));

        JTextField[] namen = disziplinen.toArray(new JTextField[disziplinen.size()]);
        JIntegerField[] dfw = recw.toArray(new JIntegerField[recw.size()]);
        JIntegerField[] dfm = recm.toArray(new JIntegerField[recm.size()]);
        JTimeField[] dfwt = recwt.toArray(new JTimeField[recw.size()]);
        JTimeField[] dfmt = recmt.toArray(new JTimeField[recm.size()]);

        JIntegerField[] laengen = lengths.toArray(new JIntegerField[lengths.size()]);
        JIntegerField[] runden = laps.toArray(new JIntegerField[laps.size()]);

        for (int x = 0; x < number; x++) {
            add(namen[x], CC.xy(2, 6 + 2 * x));
            add(dfw[x], CC.xy(4, 6 + 2 * x));
            add(dfwt[x], CC.xy(6, 6 + 2 * x));
            add(dfm[x], CC.xy(8, 6 + 2 * x));
            add(dfmt[x], CC.xy(10, 6 + 2 * x));
            add(laengen[x], CC.xy(12, 6 + 2 * x));
            add(runden[x], CC.xy(14, 6 + 2 * x));
        }

        int y = 6 + 2 * number;
        minusButton.setEnabled(number > 0);
        add(plusButton, CC.xy(12, y));
        add(minusButton, CC.xy(14, y));
    }

    public Disziplin[] getDisziplinen(boolean maennlich) {
        Disziplin[] diszs = new Disziplin[count];
        JTextField[] namen = disziplinen.toArray(new JTextField[disziplinen.size()]);
        JIntegerField[] laengen = lengths.toArray(new JIntegerField[lengths.size()]);
        JIntegerField[] runden = laps.toArray(new JIntegerField[laps.size()]);
        JTimeField[] recs;
        int[][] rounds;
        int[][] roundIds;
        if (maennlich) {
            recs = recmt.toArray(new JTimeField[recm.size()]);
            rounds = roundsm.toArray(new int[roundsm.size()][0]);
            roundIds = roundIdsm.toArray(new int[roundIdsm.size()][0]);
        } else {
            recs = recwt.toArray(new JTimeField[recw.size()]);
            rounds = roundsw.toArray(new int[roundsw.size()][0]);
            roundIds = roundIdsw.toArray(new int[roundIdsw.size()][0]);
        }
        for (int x = 0; x < count; x++) {
            // TODO: Erweitern der Disziplineneingabe
            diszs[x] = new Disziplin(namen[x].getText(), recs[x].getTimeAsInt(), laengen[x].getInt(), runden[x].getInt());
            diszs[x].setRunden(rounds[x], roundIds[x]);
        }
        return diszs;
    }

    void moveFocus(int code, int x, int y) {
        if (code == java.awt.event.KeyEvent.VK_DOWN) {
            if (y + 1 < count) {
                switch (x) {
                default:
                case 0:
                    disziplinen.get(y + 1).requestFocus();
                    return;
                case 1:
                    recw.get(y + 1).requestFocus();
                    return;
                case 2:
                    recm.get(y + 1).requestFocus();
                    return;
                case 3:
                    lengths.get(y + 1).requestFocus();
                    return;
                case 4:
                    laps.get(y + 1).requestFocus();
                    return;
                }
            }
        }
        if (code == java.awt.event.KeyEvent.VK_UP) {
            if (y > 0) {
                switch (x) {
                default:
                case 0:
                    disziplinen.get(y - 1).requestFocus();
                    break;
                case 1:
                    recw.get(y - 1).requestFocus();
                    break;
                case 2:
                    recm.get(y - 1).requestFocus();
                    break;
                case 3:
                    lengths.get(y - 1).requestFocus();
                    break;
                case 4:
                    laps.get(y - 1).requestFocus();
                    break;
                }
            }
        }
    }
}