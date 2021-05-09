/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import de.df.jauswertung.print.UnterschriftPrintable;
import de.df.jauswertung.util.DataTableUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.printables.EmptyPrintable;
import de.df.jutils.print.printables.JTablePrintable;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class ZWResultsPrinter implements Printer {

    CorePlugin        core          = null;
    IPluginManager    controller    = null;

    private JPanel    panel         = null;
    private JButton   print         = null;
    private JButton   preview       = null;
    private JLabel    warning       = null;
    private JLabel    filter        = null;
    private JCheckBox unterschrift  = null;

    JSelectionDialog  printDialog   = null;
    JSelectionDialog  previewDialog = null;

    @SuppressWarnings("rawtypes")
    private void initDialogs() {
        if (printDialog == null) {
            AWettkampf wk = core.getFilteredWettkampf();
            printDialog = new JSelectionDialog(controller.getWindow(), wk, new PrintCB(), I18n.get("Print"), JSelectionDialog.MODE_AK_SELECTION);
            previewDialog = new JSelectionDialog(controller.getWindow(), wk, new PreviewCB(), I18n.get("Preview"), JSelectionDialog.MODE_AK_SELECTION);
        }
    }

    public ZWResultsPrinter(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {

        panel = new JPanel(new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu",
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

        unterschrift = new JCheckBox(I18n.get("Unterschrift"));
        unterschrift.setSelected(Utils.getPreferences().getBoolean("ZWResultsWithSignature", false));
        unterschrift.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Utils.getPreferences().putBoolean("ZWResultsWithSignature", unterschrift.isSelected());
            }
        });
        unterschrift.setEnabled(false);

        panel.add(filter, CC.xy(2, 2));
        panel.add(warning, CC.xy(4, 2));
        panel.add(unterschrift, CC.xy(6, 2));
        panel.add(preview, CC.xy(8, 2));
        panel.add(print, CC.xy(10, 2));
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.print.Printer#getPanels()
     */

    @Override
    public JPanel getPanel() {
        return panel;
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.print.Printer#getNames()
     */
    @Override
    public String getName() {
        return I18n.get("ZWResults");
    }

    @Override
    public <T extends ASchwimmer> void dataUpdated(UpdateEvent due, AWettkampf<T> wk, AWettkampf<T> filteredwk) {
        boolean result = false;
        if (filteredwk != null) {
            if (filteredwk.hasSchwimmer()) {
                ListIterator<T> li = filteredwk.getSchwimmer().listIterator();
                while (li.hasNext()) {
                    if (li.next().getAK().hasHLW()) {
                        result = true;
                        break;
                    }
                }
            }
        }

        print.setEnabled(result);
        preview.setEnabled(result);
        unterschrift.setEnabled(result);
        checkWarning(filteredwk);
        filter.setVisible(result && wk.isFilterActive());
    }

    <T extends ASchwimmer> void checkWarning(AWettkampf<T> wk) {
        boolean check = false;
        if (!wk.isHlwComplete()) {
            check = true;
        }
        warning.setVisible(preview.isEnabled() && check);
    }

    /**
     * @param selected
     */
    Printable getPrintable(boolean[][] selected) {
        AWettkampf<?> wk = core.getFilteredWettkampf();

        ExtendedTableModel[] etm = DataTableUtils.zusatzwertungResults(wk, selected, null, PrintUtils.printZWnames);
        if (etm == null) {
            return EmptyPrintable.Instance;
        }
        Printable p = PrintManager.getPrintable(etm, JTablePrintable.OPT_ALL, true, true);
        if (unterschrift.isSelected()) {
            p = new UnterschriftPrintable(p);
        }
        return PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), I18n.get("ZWResults"), I18n.get("ZWResults"));
    }

    void printResults(boolean[][] selection) {
        PrintExecutor.print(getPrintable(selection), I18n.get("ZWResults"), true, controller.getWindow());
    }

    private class PPrintableCreator implements PrintableCreator {

        private boolean[][] selection;

        public PPrintableCreator(boolean[][] selection) {
            this.selection = selection;
        }

        @Override
        public Printable create() {
            return getPrintable(selection);
        }

    }

    void previewResults(boolean[][] selection) {
        PrintExecutor.preview(controller.getWindow(), new PPrintableCreator(selection), I18n.get("ZWResults"), IconManager.getIconBundle(), IconManager.getTitleImages());
    }

    void showPreviewSelectionDialog() {
        initDialogs();
        previewDialog.setCompetition(core.getFilteredWettkampf());
        previewDialog.setDiscipline(ASchwimmer.DISCIPLINE_NUMBER_ZW);
        previewDialog.setVisible(true);
    }

    void showPrintSelectionDialog() {
        initDialogs();
        printDialog.setCompetition(core.getFilteredWettkampf());
        printDialog.setDiscipline(ASchwimmer.DISCIPLINE_NUMBER_ZW);
        printDialog.setVisible(true);
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            showPrintSelectionDialog();
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            showPreviewSelectionDialog();
        }
    }

    final class PrintCB implements JSelectionDialog.PrintCallBack {

        @Override
        public void print(boolean[][] selected) {
            printResults(selected);
        }

    }

    final class PreviewCB implements JSelectionDialog.PrintCallBack {

        @Override
        public void print(boolean[][] selected) {
            previewResults(selected);
        }

    }
}