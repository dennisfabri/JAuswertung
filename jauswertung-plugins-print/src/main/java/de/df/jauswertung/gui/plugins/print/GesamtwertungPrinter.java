/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class GesamtwertungPrinter implements Printer {

    CorePlugin core = null;
    IPluginManager controller = null;

    private JPanel panel = null;
    private JButton print = null;
    private JButton preview = null;
    private JLabel warning = null;
    private JLabel filter = null;
    private JComboBox type = null;

    public GesamtwertungPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {

        panel = new JPanel(new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default"
                        + ",4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu"));

        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        print.setEnabled(false);

        preview = new JButton(I18n.get("Preview"));
        preview.addActionListener(new PreviewActionListener());
        preview.setEnabled(false);

        warning = new JLabel(IconManager.getSmallIcon("warn"));
        warning.setToolTipText(I18n.get("InputNotComplete"));
        warning.setVisible(false);

        filter = new JLabel(IconManager.getSmallIcon("filter"));
        filter.setToolTipText(I18n.get("InputFiltered"));
        filter.setVisible(false);

        type = new JComboBox(new Object[] { I18n.get("Organization"), I18n.get("Qualifikationsebene") });
        type.setSelectedIndex(0);
        type.addActionListener(arg0 -> {
            dataUpdated(null, core.getWettkampf(), core.getFilteredWettkampf());
        });

        panel.add(filter, CC.xy(2, 2));
        panel.add(warning, CC.xy(4, 2));
        panel.add(new JLabel(I18n.get("GroupBy")), CC.xy(6, 2));
        panel.add(type, CC.xy(8, 2));
        panel.add(preview, CC.xy(10, 2));
        panel.add(print, CC.xy(12, 2));
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
        return I18n.get("GroupEvaluation");
    }

    @Override
    public <T extends ASchwimmer> void dataUpdated(UpdateEvent due, AWettkampf<T> wk, AWettkampf<T> filteredwk) {
        boolean result = false;
        if (filteredwk.getRegelwerk().hasGesamtwertung() && filteredwk.hasSchwimmer()) {
            result = true;
        }
        boolean resulttype = result;
        boolean resultfilter = result;
        if (result && (type.getSelectedIndex() > 0)) {
            filteredwk = Utils.copy(filteredwk);
            LinkedList<T> ll = filteredwk.getSchwimmer();
            for (T s : ll) {
                if (s.getQualifikationsebene().length() == 0) {
                    filteredwk.removeSchwimmer(s);
                }
            }
            resulttype = false;
            if (filteredwk.getRegelwerk().hasGesamtwertung() && filteredwk.hasSchwimmer()) {
                resulttype = true;
            }
            if (!resulttype) {
                type.setSelectedIndex(0);
            }
        }
        type.setEnabled(result);
        warning.setVisible(resultfilter && !filteredwk.isCompetitionComplete());
        filter.setVisible(resultfilter && wk.isFilterActive());
        print.setEnabled(result);
        preview.setEnabled(result);
    }

    Printable getPrintable() {
        AWettkampf<ASchwimmer> wk = Utils.copy(core.getFilteredWettkampf());
        if (type.getSelectedIndex() > 0) {
            LinkedList<ASchwimmer> ll = wk.getSchwimmer();
            for (ASchwimmer s : ll) {
                if (s.getQualifikationsebene().length() == 0) {
                    wk.removeSchwimmer(s);
                } else {
                    s.setGliederung(s.getQualifikationsebene());
                }
            }
        }
        return PrintManager.getFinalPrintable(PrintUtils.getGesamtwertungPrintable(wk), core.getLastChangedDate(), true,
                I18n.get("GroupEvaluation"));
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintExecutor.print(getPrintable(), I18n.get("GroupEvaluation"), true, controller.getWindow());
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            PrintableCreator pc = GesamtwertungPrinter.this::getPrintable;
            PrintExecutor.preview(controller.getWindow(), pc, I18n.get("GroupEvaluation"), IconManager.getIconBundle(),
                    IconManager.getTitleImages());
        }
    }
}