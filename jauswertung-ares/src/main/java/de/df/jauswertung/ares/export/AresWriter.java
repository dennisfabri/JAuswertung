package de.df.jauswertung.ares.export;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jutils.gui.util.DialogUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AresWriter {

    @SuppressWarnings("unchecked")
    public <T extends ASchwimmer> void write(String[] filenames, String dir) throws IOException {
        if (filenames == null || filenames.length == 0) {
            throw new IOException(I18n.get("NoFilesSelected"));
        }
        AWettkampf<T>[] wks = new AWettkampf[filenames.length];
        for (int x = 0; x < filenames.length; x++) {
            String filename = filenames[x];
            wks[x] = InputManager.ladeWettkampf(filename);
            if (wks[x] == null) {
                throw new IOException(I18n.get("CouldNotOpenFile", filename));
            }
        }
        write(wks, dir);
    }

    private <T extends ASchwimmer> void write(AWettkampf<T>[] wks, String dir) throws IOException {
        boolean hasFinals = FormelManager.isHeatBased(wks[0].getRegelwerk().getFormelID());
        for (int x = 1; x < wks.length; x++) {
            if (hasFinals != FormelManager.isHeatBased(wks[x].getRegelwerk().getFormelID())) {
                throw new IOException(I18n.get("Competition finals has mismatching formulas"));
            }
        }
        if (hasFinals) {
            AresWriterFinals.writeAres(wks, dir);
        } else {
            AresWriterDefault.writeAres(wks, dir);
            Files.createDirectories(Path.of(dir + "2"));
            AresWriterDefault2.writeAres(wks, dir + "2");
        }
    }

}
