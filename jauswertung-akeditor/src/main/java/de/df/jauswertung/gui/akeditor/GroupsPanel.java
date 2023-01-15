package de.df.jauswertung.gui.akeditor;

import java.awt.GridLayout;

import javax.swing.JPanel;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jutils.gui.border.BorderUtils;

class GroupsPanel extends JPanel {

    private ResultgroupsPanel resultgroups;
    private Startgroupspanel startgroups;

    public GroupsPanel(JAKsEditor parent) {
        resultgroups = new ResultgroupsPanel(parent);
        startgroups = new Startgroupspanel(parent);

        setBorder(BorderUtils.createSpaceBorder());
        setLayout(new GridLayout(2, 1, 5, 5));
        // setLayout(new GridLayout(1, 1, 0, 0));
        add(startgroups);
        add(resultgroups);
    }

    public void getSettings(Regelwerk aks) {
        resultgroups.getSettings(aks);
        startgroups.getSettings(aks);
    }

    public void setSettings(Regelwerk aks) {
        resultgroups.setSettings(aks);
        startgroups.setSettings(aks);
    }
}
