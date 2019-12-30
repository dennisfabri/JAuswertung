package de.df.jauswertung.gui.plugins.print;

import java.awt.event.*;
import java.awt.print.Printable;
import java.text.MessageFormat;

import javax.swing.*;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.*;
import de.df.jauswertung.print.*;
import de.df.jutils.plugin.*;
import de.df.jutils.print.*;

/**
 * @author Dennis Fabri
 * @date 13.07.2005
 */
public class RefereePrinter implements Printer {

    CorePlugin      core;
    IPluginManager  controller;

    private JPanel  panel;
    private JButton print;
    private JButton preview;

    public RefereePrinter(IPluginManager window, CorePlugin plugin) {
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
        return I18n.get("Referees");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        boolean b = (wk.getKampfrichterverwaltung() != null);
        print.setEnabled(b);
        preview.setEnabled(b);
    }

    @SuppressWarnings({})
    <T extends ASchwimmer> Printable getPrintable() {
        return PrintManager.getFinalPrintable(getPrintable(core.getWettkampf()), core.getLastChangedDate(), true,
                I18n.get("Referees"));
    }

    public static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk) {
        Printable p = null;
        KampfrichterVerwaltung kv = wk.getKampfrichterverwaltung();
        if (kv == null) {
            return EmptyPrintable.Instance;
        }
        int mode = wk.getIntegerProperty(PropertyConstants.PRINT_REFEREES_COMPACT, 1);
        switch (mode) {
        case 0:
            p = RefereesTableCreator.getPrintable(kv);
            break;
        default:
        case 1:
            p = RefereesTableCompactCreator.getPrintable(kv);
            break;
        case 2:
            p = RefereesTableVeryCompactCreator.getPrintable(kv);
            break;
        }
        p = new HeaderFooterPrintable(p, new MessageFormat(I18n.get("Referees")), null, PrintManager.getFont());
        return p;
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintManager.print(getPrintable(), I18n.get("Referees"), controller.getWindow());
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
            PrintManager.preview(controller.getWindow(), pc, I18n.get("Referees"), IconManager.getIconBundle(),
                    IconManager.getTitleImages());
        }
    }
}