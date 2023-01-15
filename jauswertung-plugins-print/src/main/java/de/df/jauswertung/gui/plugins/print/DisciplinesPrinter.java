package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.PrintUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.JTablePrintable;

/**
 * @author Dennis Fabri
 * @date 13.07.2005
 */
public class DisciplinesPrinter implements Printer {

    CorePlugin core;
    IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JButton preview;

    public DisciplinesPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu", "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        panel.add(preview, CC.xy(2, 2));
        panel.add(print, CC.xy(4, 2));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.print.Printer#getPanels()
     */
    @Override
    public JPanel getPanel() {
        return panel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.print.Printer#getNames()
     */
    @Override
    public String getName() {
        return I18n.get("Disciplines");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        // Nothing to do
    }

    <T extends ASchwimmer> Printable getPrintable() {
        return PrintManager.getFinalPrintable(getPrintable(core.getWettkampf().getRegelwerk()),
                core.getLastChangedDate(), I18n.get("Disciplines"),
                I18n.get("Disciplines"));
    }

    public static <T extends ASchwimmer> Printable getPrintable(Regelwerk aks) {
        JTable table = PrintUtils.getDisciplinesTable(aks);
        return PrintManager.getPrintable(table, "", JTablePrintable.OPT_ALL, true, true);
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintExecutor.print(getPrintable(), I18n.get("Disciplines"), controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintableCreator pc = new PrintableCreator() {
                @Override
                public Printable create() {
                    return getPrintable();
                }
            };
            PrintExecutor.preview(controller.getWindow(), pc, I18n.get("Disciplines"), IconManager.getIconBundle(),
                    IconManager.getTitleImages());
        }
    }
}