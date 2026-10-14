/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.teammembersinput;

import static de.df.jauswertung.daten.PropertyConstants.DATE;
import static de.df.jauswertung.daten.PropertyConstants.LOCATION;
import static de.df.jauswertung.daten.PropertyConstants.NAME;
import static de.df.jauswertung.daten.PropertyConstants.SHORTNAME;


import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.PrintQueueStatus;
import de.df.jauswertung.gui.plugins.WarningPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.IOUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
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

    PrintQueueStatus printcallback;

    private RegisteredTeamnamesPrinter2 teammembersprinter;

    public PPrintTeammembersPlugin() {
        printcallback = new PrintQueueStatus(() -> getController().getWindow());
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
}
