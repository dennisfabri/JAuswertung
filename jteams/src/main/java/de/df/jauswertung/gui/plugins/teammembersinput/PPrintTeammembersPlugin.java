/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.teammembersinput;

import static de.df.jauswertung.daten.PropertyConstants.DATE;
import static de.df.jauswertung.daten.PropertyConstants.LOCATION;
import static de.df.jauswertung.daten.PropertyConstants.NAME;
import static de.df.jauswertung.daten.PropertyConstants.SHORTNAME;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.WarningPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.IOUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.JPFrame;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PageSetup;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintQueue;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
public class PPrintTeammembersPlugin extends ANullPlugin {

    private CorePlugin core = null;
    WarningPlugin warner;
    SelectOrganisationPlugin selectOrganisation;

    PrintCallback printcallback;

    private RegisteredTeamnamesPrinter2 teammembersprinter;

    public PPrintTeammembersPlugin() {
        printcallback = new PrintCallback();
        PrintQueue.getInstance().addPrintCallback(printcallback);
    }

    private void createTeammemberPrinters(IPluginManager controller, CorePlugin plugin, WarningPlugin warn) {
        teammembersprinter = new RegisteredTeamnamesPrinter2(controller, plugin);
        teammembersprinter.dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED, plugin.getWettkampf(),
                plugin.getFilteredWettkampf());
        if (selectOrganisation != null) {
            teammembersprinter.setOrganisation(selectOrganisation.getSelectedOrganisation());
        }
    }

    void select(String selected) {
        if (teammembersprinter == null) {
            return;
        }
        teammembersprinter.setOrganisation(selected);
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        return teammembersprinter.getQuickButtons();
    }

    @Override
    public void setController(IPluginManager controller, String pluginuid) {
        super.setController(controller, pluginuid);

        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", pluginuid);
        warner = (WarningPlugin) controller.getFeature("de.df.jauswertung.warning", pluginuid);
        selectOrganisation = (SelectOrganisationPlugin) controller.getFeature("de.df.jauswertung.selectorganisation",
                pluginuid);

        selectOrganisation.addSelectionListener(this::select);

        createTeammemberPrinters(getController(), core, warner);
        dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED);
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void dataUpdated(UpdateEvent due) {
        AWettkampf wk = core.getWettkampf();
        PrintManager.setName(wk.getStringProperty(NAME), wk.getStringProperty(SHORTNAME));
        PrintManager.setOrt(wk.getStringProperty(LOCATION));
        PrintManager.setDatum(wk.getStringProperty(DATE));

        try {
            teammembersprinter.dataUpdated(due, wk, wk);
        } catch (RuntimeException re) {
            re.printStackTrace();
            // Protect execution of normal code from uncatched exceptions
        }
    }

    @Override
    public MenuInfo[] getMenues() {
        return null;
    }

    @Override
    public void shutDown() {
        IOUtils.writeToPreferences("PrintSettings", PageSetup.getPageSettings());
        PrintQueue.getInstance().removePrintCallback(printcallback);
    }

    void showError(String title, String message, String note) {
        new ErrorScheduler(title, message, note).start();
    }

    private boolean showErrorI(String title, String message, String note) {
        JFrame parent = getController().getWindow();
        if (parent.isEnabled()) {
            DialogUtils.warn(parent, title, message, note);
            return true;
        }
        return false;
    }

    private final class PrintCallback implements PrintQueue.IPrintCallback {
        @Override
        @SuppressWarnings("synthetic-access")
        public void jobFinished(String job, int jobs) {
            SwingUtilities.invokeLater(new StatusTextChanger(getController().getWindow(), ""));
        }

        @Override
        public void jobAdded(String job, int jobs) {
            jobStarted(job, jobs);
        }

        @Override
        @SuppressWarnings("synthetic-access")
        public void jobStarted(String job, int jobs) {
            SwingUtilities.invokeLater(
                    new StatusTextChanger(getController().getWindow(), I18n.get("PrintingJobOfJobs", job, jobs)));
        }

        @Override
        @SuppressWarnings("synthetic-access")
        public void jobError(String job, int jobs, String title, String message, String note) {
            showError(title, message, note);
            SwingUtilities.invokeLater(new StatusTextChanger(getController().getWindow(), ""));
        }

        @Override
        @SuppressWarnings("synthetic-access")
        public void queueEmpty() {
            SwingUtilities.invokeLater(new StatusTextChanger(getController().getWindow(), null));
        }
    }

    private static class StatusTextChanger implements Runnable {

        private final JPFrame frame;
        private final String text;

        public StatusTextChanger(JPFrame f, String t) {
            frame = f;
            text = t;
        }

        @Override
        public void run() {
            frame.setStatusBarText(text);
        }
    }

    private class ErrorScheduler extends Thread {

        private final String title;
        private final String message;
        private final String note;

        public ErrorScheduler(String title, String message, String note) {
            this.title = title;
            this.message = message;
            this.note = note;
        }

        @Override
        public void run() {
            boolean ok = false;
            while (!ok) {
                ok = EDTUtils.executeOnEDTwithReturn(() -> showErrorI(title, message, note));
            }
        }
    }
}