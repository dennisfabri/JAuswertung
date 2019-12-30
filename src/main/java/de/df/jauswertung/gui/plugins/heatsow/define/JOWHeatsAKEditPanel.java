package de.df.jauswertung.gui.plugins.heatsow.define;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.layout.FormLayoutUtils;

public class JOWHeatsAKEditPanel extends JPanel {

    private JOWHeatsDisziplinEditPanel[] panels;

    public JOWHeatsAKEditPanel(Regelwerk rw, Altersklasse ak, boolean male) {
        panels = new JOWHeatsDisziplinEditPanel[ak.getDiszAnzahl()];
        setLayout(new FormLayout("4dlu,fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(panels.length + 1)));
        add(new JLabel(I18n.getAgeGroupAsString(rw, ak, male)), CC.xy(2, 2));
        for (int x = 0; x < panels.length; x++) {
            panels[x] = new JOWHeatsDisziplinEditPanel(ak.getDisziplin(x, male), male);
            add(panels[x], CC.xy(2, 2 * x + 4));
        }
    }

    public boolean isInputValid() {
        for (int x = 0; x < panels.length; x++) {
            if (!panels[x].isInputValid()) {
                return false;
            }
        }
        return true;
    }

    public void doSave() {
        if (isInputValid()) {
            for (int x = 0; x < panels.length; x++) {
                panels[x].doSave();
            }
        }
    }

    public Collection<Integer> GetIds() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (int x = 0; x < panels.length; x++) {
            for (int i : panels[x].getIds()) {
                ids.add(i);
            }
        }
        return ids;
    }

    public boolean copyFrom(JOWHeatsAKEditPanel from) {
        if (panels.length != from.panels.length) {
            return false;
        }
        for (int x = 0; x < panels.length; x++) {
            panels[x].copyFrom(from.panels[x]);
        }
        return true;
    }

    public int enumerate1(int offset, boolean isMale) {
        for (int x = 0; x < panels.length; x++) {
            offset = panels[x].enumerate1(offset, isMale);
        }
        return offset;
    }

    public int enumerate2(int offset) {
        for (int x = 0; x < panels.length; x++) {
            offset = panels[x].enumerate2(offset);
        }
        return offset;
    }
}
