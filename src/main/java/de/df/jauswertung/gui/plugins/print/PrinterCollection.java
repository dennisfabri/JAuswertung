/*
 * Created on 03.01.2005
 */
package de.df.jauswertung.gui.plugins.print;

import java.util.LinkedList;

import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jutils.gui.JLabelSeparator;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.layout.SimpleTableLayout;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri
 * @date 03.01.2005
 */
public class PrinterCollection implements Printer {

    private String              name;
    private JPanel              panel    = new JPanel(new SimpleTableLayout(1, 5, 5));
    private LinkedList<Printer> printers = new LinkedList<Printer>();

    public PrinterCollection(String name) {
        this.name = name;
    }

    public void add(Printer p) {
        printers.add(p);
    }

    public void doLayoutOneColumn() {
        panel.removeAll();
        int x = 0;
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(printers.size() * 2, 1));
        panel.setLayout(layout);

        for (Printer p : printers) {
            panel.add(new JLabelSeparator(p.getName()), CC.xy(2, x * 4 + 2));
            panel.add(p.getPanel(), CC.xyw(1, x * 4 + 4, 3));

            x++;
        }
    }

    public void doLayout() {
        panel.removeAll();
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu,6dlu,fill:default:grow,4dlu",
                FormLayoutUtils.createLayoutString(printers.size() + (printers.size() % 2), 1));
        layout.setColumnGroups(new int[][] { { 2, 5 } });
        panel.setLayout(layout);

        int max = printers.size() / 2 + (printers.size() % 2);

        int x = 0;
        int y = 0;
        for (Printer p : printers) {
            panel.add(new JLabelSeparator(p.getName()), CC.xy(2 + y * 3, x * 4 + 2));
            panel.add(p.getPanel(), CC.xyw(1 + y * 3, x * 4 + 4, 3));

            x++;
            if (x >= max) {
                x = 0;
                y++;
            }
        }
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.print.Printer#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        for (Printer printer : printers) {
            try {
                long time = System.currentTimeMillis();
                printer.dataUpdated(due, wk, filteredwk);
                time = System.currentTimeMillis() - time;
                if (time > 0) {
                    System.out.println("    " + getName() + "/" + printer.getName() + ": " + time);
                }
            } catch (Exception t) {
                t.printStackTrace();
            }
        }
    }
}
