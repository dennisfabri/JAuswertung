package de.df.jauswertung.gui.plugins.heatsow.define;

import java.util.Arrays;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;

import static java.lang.String.format;

public class JOWHeatsDisziplinEditPanel extends JPanel {

    private final Regelwerk rwk;
    private final Disziplin disziplin;
    private final boolean male;

    private int[] runden;
    private int[] rundenIds;

    private JIntegerField[] input1 = new JIntegerField[0];
    private JIntegerField[] input2 = new JIntegerField[0];
    private final JButton add;
    private final JButton remove;

    public JOWHeatsDisziplinEditPanel(Regelwerk rwk, Disziplin d, boolean male) {
        setBorder(BorderUtils.createLabeledBorder(d.getName()));

        this.rwk = rwk;
        disziplin = d;
        this.male = male;

        runden = null;
        rundenIds = null;

        add = new JTransparentButton(IconManager.getSmallIcon("more"));
        remove = new JTransparentButton(IconManager.getSmallIcon("less"));

        add.addActionListener(e -> addRound());
        remove.addActionListener(e -> removeRound());

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

    public ValidationResult isInputValid() {
        ValidationResult result = ValidationResult.OK;
        for (int x = 0; x <= runden.length; x++) {
            result = result.merge(validate(x, x < runden.length ? runden[x] : 1, true));
            result = result.merge(validate(x, x < runden.length ? runden[x] : 1, false));
        }
        return result;
    }

    public void doSave() {
        if (isInputValid().isValid()) {
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

    private ValidationResult validateLeft(int row, int value) {
        if (row > 0) {
            if (row < input1.length) {
                if (input1[row - 1].getInt() < value) {
                    return new ValidationResult(getValidationPrefix() + "Runde " + (row + 1) + " muss kleiner als Runde " + row + " sein.");
                }
            }
            if (input2[row - 1].getInt() <= 0) {
                return new ValidationResult(getValidationPrefix() + "Id " + (row + 1) + " muss größer als 0 sein.");
            }
        }
        return value > 0 ? ValidationResult.OK : new ValidationResult(getValidationPrefix() + "Runde " + (row + 1) + " muss größer als 0 sein.");
    }

    private String getValidationPrefix() {
        return format("%s %s: ", disziplin.getName(), I18n.geschlechtToString(rwk, male));
    }

    private ValidationResult validateRight(int row, int value) {
        return value > 0 ? ValidationResult.OK : new ValidationResult(getValidationPrefix() + "Runde " + (row + 1) + " muss größer als 0 sein.");
    }

    private ValidationResult validate(int row, int value, boolean left) {
        return left ? validateLeft(row, value) : validateRight(row, value);
    }

    private final class InputValidator implements JIntegerField.Validator {

        private final int row;
        private final boolean left;

        public InputValidator(int x, boolean left) {
            row = x;
            this.left = left;
        }

        @Override
        public boolean validate(int value) {
            return JOWHeatsDisziplinEditPanel.this.validate(row, value, left).isValid();
        }
    }

    private final class InputChangedListener implements DocumentListener {

        private final int row;

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
