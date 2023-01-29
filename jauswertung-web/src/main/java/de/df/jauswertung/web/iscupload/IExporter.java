package de.df.jauswertung.web.iscupload;

import java.io.IOException;

import de.df.jauswertung.daten.AWettkampf;

public interface IExporter {
    boolean canExport(AWettkampf<?> wk);

    String export(AWettkampf<?> wk) throws IOException;
}
