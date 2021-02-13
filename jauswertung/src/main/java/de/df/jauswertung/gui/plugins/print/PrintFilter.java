package de.df.jauswertung.gui.plugins.print;

import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;

public class PrintFilter<T extends ASchwimmer> {

    private JPanel panel = null;

    public JComponent getPanel() {
        if (panel == null) {
            FormLayout layout = new FormLayout("0dlu,fill:default,0dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
            panel = new JPanel(layout);

            JButton dots = new JButton("...");
            Insets i = dots.getMargin();
            if (i != null) {
                i.left = 1;
                i.right = 0;
            } else {
                i = new Insets(1, 1, 1, 0);
            }
            dots.setMargin(i);

            panel.add(new JToggleButton("Filtern"), CC.xy(2, 2));
            panel.add(dots, CC.xy(4, 2));
        }
        return panel;
    }

    public AWettkampf<T> applyFilter(AWettkampf<T> wk) {
        return wk;
    }
}
