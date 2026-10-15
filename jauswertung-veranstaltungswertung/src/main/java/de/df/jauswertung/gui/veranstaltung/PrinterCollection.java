/*
 * Created on 03.01.2005
 */
package de.df.jauswertung.gui.veranstaltung;

import java.util.LinkedList;

import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jutils.gui.JLabelSeparator;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.layout.SimpleTableLayout;

/**
 * @author Dennis Fabri
 * @date 03.01.2005
 */
class PrinterCollection implements Printer {

    private String name;
    private JPanel panel = new JPanel(new SimpleTableLayout(1, 5, 5));
    private LinkedList<Printer> printers = new LinkedList<>();

    public PrinterCollection(String name) {
        this.name = name;
    }

    public void add(Printer p) {
        printers.add(p);
    }

    public void doLayoutOneColumn() {
        panel.removeAll();
        int x = 0;
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu",
                FormLayoutUtils.createLayoutString(printers.size() * 2, 1));
        panel.setLayout(layout);

        for (Printer p : printers) {
            panel.add(new JLabelSeparator(p.getName()), CC.xy(2, x * 4 + 2));
            panel.add(p.getPanel(), CC.xyw(1, x * 4 + 4, 3));

            x++;
        }
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.print.Printer#getName()
     */
    @Override
    public String getName() {
        return name;
    }
}
