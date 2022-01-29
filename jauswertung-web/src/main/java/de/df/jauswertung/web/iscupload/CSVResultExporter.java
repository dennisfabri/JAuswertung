package de.df.jauswertung.web.iscupload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.io.ImportExportTypes;

public class CSVResultExporter implements IExporter {

    @Override
    public boolean canExport(AWettkampf<?> wk) {
        return ExportManager.isEnabled(wk, ImportExportTypes.RESULTS);
    }

    @Override
    public String export(AWettkampf<?> wk) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            boolean ok = ExportManager.export("CSV", bos, ImportExportTypes.RESULTS, wk, null);
            if (!ok) {
                return null;
            }

            return IOUtils.toString(bos.toByteArray(), "Cp1252");
        }
    }

}
