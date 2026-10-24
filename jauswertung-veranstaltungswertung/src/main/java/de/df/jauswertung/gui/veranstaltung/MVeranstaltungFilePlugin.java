package de.df.jauswertung.gui.veranstaltung;

import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import de.df.jauswertung.gui.plugins.AFilePlugin;
import de.df.jauswertung.gui.util.I18n;

public class MVeranstaltungFilePlugin extends AFilePlugin<VeranstaltungCorePlugin> {

    public MVeranstaltungFilePlugin() {
        super(VeranstaltungswertungLauncher.CORE, new SimpleFileFilter(I18n.get("Veranstaltung"), "vs"), "Veranstaltung",
                "Veranstaltungswertung", (controller, core) -> core.neu());
    }
}
