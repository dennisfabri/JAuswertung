package de.df.jauswertung.gui.plugins.heatsow.define;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;

public class JOWHeatsDisziplinEditPanel extends JPanel {

    private Disziplin       disziplin;

    private int[]           runden;
    private int[]           rundenIds;

    private JIntegerField[] input1 = new JIntegerField[0];
    private JIntegerField[] input2 = new JIntegerField[0];
    private JButton         add;
    private JButton         remove;

    public JOWHeatsDisziplinEditPanel(Disziplin d, boolean male) {
        setBorder(BorderUtils.createLabeledBorder(d.getName()));

        disziplin = d;
        runden = null;
        rundenIds = null;

        add = new JTransparentButton(IconManager.getSmallIcon("more"));
        remove = new JTransparentButton(IconManager.getSmallIcon("less"));

        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addRound();
            }
        });
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeRound();
            }
        });

        rebuildUI();
    }

    public int[] getIds() {
        int[] ids = new int[input2.length];
        for (int x = 0; x < input2.length; x++) {
            ids[x] = input2[x].getInt();
        }
        return ids;
    }

    void addRound() {
        runden = Arrays.copyOf(runden, runden.length + 1);
        rundenIds = Arrays.copyOf(rundenIds, rundenIds.length + 1);
        rebuildUI();
    }

    void removeRound() {
        if (runden.length > 0) {
            runden = Arrays.copyOf(runden, runden.length - 1);
            rundenIds = Arrays.copyOf(rundenIds, rundenIds.length - 1);
            rebuildUI();
        }
    }

    private boolean isUpdating = false;

    void changed(int x) {
        if (isUpdating) {
            return;
        }
        if (x < input1.length) {
            runden[x] = input1[x].getInt();
        }
        rundenIds[x] = input2[x].getInt();
    }

    public boolean isInputValid() {
        for (int x = 0; x <= runden.length; x++) {
            if (!validate(x, x < runden.length ? runden[x] : 1, true) || !validate(x, x < runden.length ? runden[x] : 1, false)) {
                return false;
            }
        }
        return true;
    }

    public void doSave() {
        if (isInputValid()) {
            disziplin.setRunden(runden, rundenIds);
        }
    }

    private JIntegerField[] copy(JIntegerField[] input, int length, boolean left) {
        JIntegerField[] newInput = Arrays.copyOf(input, length);
        for (int x = input.length; x < length; x++) {
            newInput[x] = new JIntegerField(true);
            newInput[x].setValidator(new InputValidator(x, left));
            newInput[x].setAutoSelectAll(true);
            newInput[x].setHorizontalAlignment(SwingConstants.RIGHT);
            newInput[x].getDocument().addDocumentListener(new InputChangedListener(x));
        }
        return newInput;
    }

    private void rebuildUI() {
        if (runden == null || input1.length != runden.length) {
            if (runden == null) {
                runden = disziplin.getRunden();
                rundenIds = disziplin.getRundenIds();
            }
            removeAll();
            input1 = copy(input1, runden.length, true);
            input2 = copy(input2, rundenIds.length, false);
            setLayout(new FormLayout(
                    "4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu",
                    FormLayoutUtils.createLayoutString(runden.length + 2)));

            add(new JLabel("Runde"), CC.xy(2, 2, "center,center"));
            add(new JLabel("Typ"), CC.xy(4, 2, "center,center"));
            add(new JLabel("Qualiplätze"), CC.xy(6, 2, "center,center"));
            add(new JLabel("Id"), CC.xy(8, 2, "center,center"));

            for (int x = 0; x < runden.length; x++) {
                add(new JLabel("" + (x + 1)), CC.xy(2, 2 * x + 4, "center,center"));
                add(new JLabel("Quali"), CC.xy(4, 2 * x + 4, "center,center"));
                add(input1[x], CC.xy(6, 2 * x + 4));
                add(input2[x], CC.xy(8, 2 * x + 4));
            }
            add(new JLabel("" + (runden.length + 1)), CC.xy(2, 2 * runden.length + 4, "center,center"));
            add(new JLabel("Finale"), CC.xy(4, 2 * runden.length + 4, "center,center"));
            // add(input[x], CC.xy(6, 2 * runden.length + 2));
            add(input2[runden.length], CC.xy(8, 2 * runden.length + 4));
            add(add, CC.xy(10, 2 * runden.length + 4));
            add(remove, CC.xy(12, 2 * runden.length + 4));

            remove.setEnabled(runden.length > 0);
        }

        try {
            isUpdating = true;
            for (int x = 0; x < runden.length; x++) {
                input1[x].setInt(runden[x]);
            }
            for (int x = 0; x < rundenIds.length; x++) {
                input2[x].setInt(rundenIds[x]);
            }
        } finally {
            isUpdating = false;
        }
        updateUI();
    }

    private boolean validateLeft(int row, int value) {
        if (row > 0) {
            if (row < input1.length) {
                if (input1[row - 1].getInt() < value) {
                    return false;
                }
            }
            if (input2[row - 1].getInt() <= 0) {
                return false;
            }
        }
        return value > 0;
    }

    private boolean validateRight(int row, int value) {
        return value > 0;
    }

    private boolean validate(int row, int value, boolean left) {
        return left ? validateLeft(row, value) : validateRight(row, value);
    }

    private final class InputValidator implements JIntegerField.Validator {

        private int     row;
        private boolean left;

        public InputValidator(int x, boolean left) {
            row = x;
            this.left = left;
        }

        @Override
        public boolean validate(int value) {
            return JOWHeatsDisziplinEditPanel.this.validate(row, value, left);
        }
    }

    private final class InputChangedListener implements DocumentListener {

        private int row;

        public InputChangedListener(int x) {
            row = x;
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changed(row);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            changed(row);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changed(row);
        }
    }

    public void copyFrom(JOWHeatsDisziplinEditPanel source) {
        runden = Arrays.copyOf(source.runden, source.runden.length);
        rundenIds = Arrays.copyOf(source.rundenIds, source.rundenIds.length);
        for (int x = 0; x < rundenIds.length; x++) {
            if (rundenIds[x] > 0) {
                rundenIds[x]++;
            }
        }
        rebuildUI();
    }

    public int enumerate1(int offset, boolean isMale) {
        if (isMale && offset == 1) {
            offset = 2;
        }
        for (int x = 0; x < rundenIds.length; x++) {
            rundenIds[x] = offset;
            offset += 2;
        }
        rebuildUI();
        return offset;
    }

    public int enumerate2(int offset) {
        for (int x = 0; x < rundenIds.length; x++) {
            rundenIds[x] = offset;
            offset++;
        }
        rebuildUI();
        return offset;
    }
}
