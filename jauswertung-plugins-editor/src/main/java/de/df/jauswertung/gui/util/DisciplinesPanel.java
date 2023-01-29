/*
 * Created on 17.03.2006
 */
package de.df.jauswertung.gui.util;

import java.awt.CardLayout;
import java.util.LinkedList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JIntegerField.Validator;
import de.df.jutils.gui.JTimeField;
import de.df.jutils.gui.layout.FormLayoutUtils;

public class DisciplinesPanel<T extends ASchwimmer> extends JPanel {

    private final class SelectionListener implements ChangeListener {

        private int x;
        private int y;

        public SelectionListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            updateInputField(x, y);
            fireChangeEvent();
        }
    }

    private static final long serialVersionUID = 8650758383163688649L;

    private LinkedList<DataChangeListener> listeners;

    private JCheckBox[][] boxes;
    private CardLayout cards = new CardLayout();
    private int current = 0;
    private JIntegerField[][] input;
    private JTimeField[][] times;

    public DisciplinesPanel(AWettkampf<T> wk) {
        setLayout(cards);

        if (wk == null) {
            return;
        }

        listeners = new LinkedList<>();

        Regelwerk aks = wk.getRegelwerk();
        boxes = new JCheckBox[aks.size()][0];
        input = new JIntegerField[aks.size()][0];
        times = new JTimeField[aks.size()][0];

        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            int anzahl = ak.getDiszAnzahl();

            JPanel p = new JPanel(new FormLayout(
                    "4dlu,fill:default,4dlu,fill:default:grow," + "4dlu,fill:default,4dlu,fill:default,4dlu",
                    FormLayoutUtils.createLayoutString(ak.getDiszAnzahl())));

            boxes[x] = new JCheckBox[anzahl];
            input[x] = new JIntegerField[anzahl];
            times[x] = new JTimeField[anzahl];
            for (int y = 0; y < boxes[x].length; y++) {
                boxes[x][y] = new JCheckBox();
                input[x][y] = new JIntegerField(JIntegerField.EMPTY_FIELD, JTimeField.MAX_TIME);
                input[x][y].setValidator((Validator)value -> {
                    value = value / 100;
                    if ((value % 100) >= 60) {
                        return false;
                    }
                    value = value / 100;
                    return value < 1000;
                });
                times[x][y] = new JTimeField(input[x][y]);
                boxes[x][y].addChangeListener(new SelectionListener(x, y));
                input[x][y].setAutoSelectAll(true);
                input[x][y].setHorizontalAlignment(SwingConstants.RIGHT);
                input[x][y].getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        fireChangeEvent();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        fireChangeEvent();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        fireChangeEvent();
                    }
                });
                if (!ak.isDisciplineChoiceAllowed()) {
                    boxes[x][y].setSelected(true);
                    boxes[x][y].setEnabled(false);
                } else {
                    boxes[x][y].setSelected(false);
                    input[x][y].setEnabled(false);
                }
                p.add(boxes[x][y], CC.xy(2, y * 2 + 2));
                p.add(new JLabel(ak.getDisziplin(y, false).getName()), CC.xy(4, y * 2 + 2));
                p.add(input[x][y], CC.xy(6, y * 2 + 2));
                p.add(times[x][y], CC.xy(8, y * 2 + 2));
            }

            add(p, "" + x);
        }
        cards.show(this, "0");
    }

    public void showAk(T t) {
        int x = t.getAKNummer();
        for (int z = 0; z < boxes.length; z++) {
            for (int y = 0; y < boxes[z].length; y++) {
                if (y < t.getAK().getDiszAnzahl()) {
                    boxes[z][y].setSelected(t.isDisciplineChosen(y));
                    times[z][y].setTimeAsInt(t.getMeldezeit(y));
                }
            }
        }
        showAk(x);
    }

    public void showAk(int x) {
        if (current == x) {
            return;
        }
        for (int y = 0; y < boxes[current].length; y++) {
            if (y < boxes[x].length) {
                if (boxes[x][y].isEnabled()) {
                    boxes[x][y].setSelected(boxes[current][y].isSelected());
                }
                times[x][y].setTimeAsInt(times[current][y].getTimeAsInt());
            }
        }
        current = x;
        cards.show(this, "" + x);
    }

    public boolean[] getSelection() {
        boolean[] c = new boolean[boxes[current].length];
        for (int i = 0; i < c.length; i++) {
            c[i] = boxes[current][i].isSelected();
        }
        return c;
    }

    public int[] getMeldezeiten() {
        int[] c = new int[boxes[current].length];
        for (int i = 0; i < c.length; i++) {
            c[i] = times[current][i].getTimeAsInt();
        }
        return c;
    }

    public boolean isInputValid() {
        for (int i = 0; i < times[current].length; i++) {
            if (!times[current][i].isValidValue()) {
                return false;
            }
        }
        return true;
    }

    void updateInputField(int x, int y) {
        input[x][y].setEnabled(boxes[x][y].isSelected());
    }

    public void reset() {
        for (int x = 0; x < input.length; x++) {
            for (int y = 0; y < input[x].length; y++) {
                input[x][y].setText("");
                if (boxes[x][y].isEnabled()) {
                    boxes[x][y].setSelected(false);
                }
            }
        }
    }

    public static interface DataChangeListener {
        void dataChanged();
    }

    public void addChangeListener(DataChangeListener cl) {
        listeners.add(cl);
    }

    public void removeListener(DataChangeListener cl) {
        listeners.remove(cl);
    }

    void fireChangeEvent() {
        for (DataChangeListener cl : listeners) {
            try {
                cl.dataChanged();
            } catch (RuntimeException re) {
                // Nothing to do
            }
        }
    }

    public boolean validateData() {
        return true;
    }
}