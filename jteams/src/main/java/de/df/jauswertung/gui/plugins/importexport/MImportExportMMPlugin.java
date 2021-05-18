package de.df.jauswertung.gui.plugins.importexport;

import de.df.jauswertung.gui.plugins.teammembersinput.SelectOrganisationPlugin;
import de.df.jutils.plugin.IPluginManager;

public class MImportExportMMPlugin extends MImportExportPlugin {

    private SelectOrganisationPlugin selectOrganisation = null;

    public MImportExportMMPlugin() {
        super(ImportExportMode.Teammembers);
    }

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);

        selectOrganisation = (SelectOrganisationPlugin) c.getPlugin("de.df.jauswertung.selectorganisation", uid);
    }

    @Override
    protected String getSelectedOrganisation() {
        if (selectOrganisation != null) {
            return selectOrganisation.getSelectedOrganisation();
        }
        return null;
    }
}