package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.ProtocolPrintable;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 13.07.2005
 */
public class ProtocolPrinter implements Printer {

    CorePlugin core;
    IPluginManager controller;

    private JPanel panel;
    private JButton print;
    private JLabel filter;
    private JButton preview;
    private JLabel warning;

    public ProtocolPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        FormLayout layout = new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu");
        panel = new JPanel(layout);

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());

        print.setEnabled(false);
        preview.setEnabled(false);

        warning = new JLabel(IconManager.getSmallIcon("warn"));
        warning.setToolTipText(I18n.get("InputNotComplete"));
        warning.setVisible(false);

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        panel.add(filter, CC.xy(2, 2));
        panel.add(warning, CC.xy(4, 2));
        panel.add(preview, CC.xy(6, 2));
        panel.add(print, CC.xy(8, 2));
    }

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
        return I18n.get("Protocol");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        boolean b = filteredwk.hasSchwimmer();
        print.setEnabled(b);
        preview.setEnabled(b);
        warning.setVisible(b && !filteredwk.isCompetitionComplete());
        filter.setVisible(b && wk.isFilterActive());
    }

    <T extends ASchwimmer> Printable getPrintable() {
        AWettkampf<T> wk = core.getFilteredWettkampf();
        return PrintManager.getFinalPrintable(new ProtocolPrintable<>(wk), wk.getLastChangedDate(),
                new MessageFormat(I18n.get("Protocol")),
                I18n.get("Protocol"));
    }

    final class PrintActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!ZielrichterentscheidPrinter.checkWettkampf(controller.getWindow(), core.getWettkampf())) {
                return;
            }

            PrintExecutor.print(getPrintable(), I18n.get("Protocol"), controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!ZielrichterentscheidPrinter.checkWettkampf(controller.getWindow(), core.getWettkampf())) {
                return;
            }

            PrintableCreator pc = this::getPrintable;
            PrintExecutor.preview(controller.getWindow(), pc, I18n.get("Protocol"), IconManager.getIconBundle(),
                    IconManager.getTitleImages());
        }
    }
}