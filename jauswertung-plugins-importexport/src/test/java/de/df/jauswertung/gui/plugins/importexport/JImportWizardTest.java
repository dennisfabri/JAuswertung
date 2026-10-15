package de.df.jauswertung.gui.plugins.importexport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.io.TableEntryException;

class JImportWizardTest {

    @Test
    void entryErrorTitleNamesCellAndFile() {
        TableEntryException tee = new TableEntryException("x", "/tmp/meldungen/verein-a.xlsx", "Tabelle1", 6, 2);

        assertEquals("Fehler in \"Tabelle1 C7\" der Datei \"verein-a.xlsx\"", JImportWizard.getEntryErrorTitle(tee));
    }

    @Test
    void entryErrorTitleWithoutFileNamesOnlyTheCell() {
        TableEntryException tee = new TableEntryException("x", null, "Tabelle1", 6, 2);

        assertEquals("Fehler in \"Tabelle1 C7\"", JImportWizard.getEntryErrorTitle(tee));
    }
}
