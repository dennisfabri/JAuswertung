/*
 * Created on 24.02.2006
 */
package de.df.jauswertung.gui.plugins.kampfrichter;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;

class JKampfrichterTeamPanel extends JPanel {

    private static final long           serialVersionUID = -2152155460517268613L;

    boolean                             changed          = false;
    private KampfrichterVerwaltung      team;

    private JKampfrichterEinheitPanel[] panels           = new JKampfrichterEinheitPanel[0];
    private JTabbedPane                 tabs             = new JTabbedPane();

    JKampfrichterTeamPanel(KampfrichterVerwaltung kt) {
        team = kt;
        initGUI();
    }

    private void initGUI() {
        panels = new JKampfrichterEinheitPanel[team.getEinheitenCount()];
        for (int x = 0; x < team.getEinheitenCount(); x++) {
            panels[x] = new JKampfrichterEinheitPanel(team.getEinheit(x));
            JScrollPane scr = new JScrollPane(panels[x]);
            scr.setBorder(null);
            scr.getVerticalScrollBar().setUnitIncrement(10);
            tabs.addTab(team.getEinheit(x).getName(), scr);
        }

        FormLayout layout = new FormLayout("0dlu,fill:default,4dlu,fill:default:grow,0dlu", "0dlu,fill:default:grow,0dlu");

        setLayout(layout);
        add(tabs, CC.xyw(2, 2, 3, "fill,fill"));
    }

    void save() {
        for (JKampfrichterEinheitPanel panel : panels) {
            if (panel.hasChanged()) {
                panel.save();
            }
        }
        changed = false;
    }

    boolean hasChanged() {
        for (JKampfrichterEinheitPanel panel : panels) {
            if (panel.hasChanged()) {
                return true;
            }
        }
        return changed;
    }
}
