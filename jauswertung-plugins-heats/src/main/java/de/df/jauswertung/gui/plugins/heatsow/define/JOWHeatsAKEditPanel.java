package de.df.jauswertung.gui.plugins.heatsow.define;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.layout.FormLayoutUtils;

public class JOWHeatsAKEditPanel extends JPanel {

    private final JOWHeatsDisziplinEditPanel[] panels;

    public JOWHeatsAKEditPanel(Regelwerk rw, Altersklasse ak, boolean male) {
        panels = new JOWHeatsDisziplinEditPanel[ak.getDiszAnzahl()];
        setLayout(new FormLayout("4dlu,fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(panels.length + 1)));
        add(new JLabel(I18n.getAgeGroupAsString(rw, ak, male)), CC.xy(2, 2));
        for (int x = 0; x < panels.length; x++) {
            panels[x] = new JOWHeatsDisziplinEditPanel(rw, ak.getDisziplin(x, male), male);
            add(panels[x], CC.xy(2, 2 * x + 4));
        }
    }

    public ValidationResult isInputValid() {
        ValidationResult result = ValidationResult.OK;
        for (JOWHeatsDisziplinEditPanel panel : panels) {
            result = result.merge(panel.isInputValid());
        }
        return result;
    }

    public void doSave() {
        if (isInputValid().isValid()) {
            for (JOWHeatsDisziplinEditPanel panel : panels) {
                panel.doSave();
            }
        }
    }

    public Collection<Integer> GetIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (JOWHeatsDisziplinEditPanel panel : panels) {
            for (int i : panel.getIds()) {
                ids.add(i);
            }
        }
        return ids;
    }

    public void copyFrom(JOWHeatsAKEditPanel from) {
        if (panels.length != from.panels.length) {
            return;
        }
        for (int x = 0; x < panels.length; x++) {
            panels[x].copyFrom(from.panels[x]);
        }
    }

    public int enumerate1(int offset, boolean isMale) {
        for (JOWHeatsDisziplinEditPanel panel : panels) {
            offset = panel.enumerate1(offset, isMale);
        }
        return offset;
    }

    public int enumerate2(int offset) {
        for (JOWHeatsDisziplinEditPanel panel : panels) {
            offset = panel.enumerate2(offset);
        }
        return offset;
    }
}
