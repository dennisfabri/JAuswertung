package de.df.jauswertung.io.massexport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.io.InputManager;

public class MassExport {

    private MassExport() {
        // Hide Constructor
    }

    public static void export(String source, String destination) throws IOException {
        AWettkampf<?> wk = InputManager.ladeWettkampf(source);
        if (wk == null) {
            throw new FileNotFoundException("Could not find file: " + source);
        }
        CompetitionExporter.export(destination, wk, new String[] { "HTML" });
    }

    public static void main(String[] args) {
        int minutes = (int) Double.parseDouble(args[1]);
        long time = 1L * minutes * 60 * 1000;
        System.out.println("JAuswertung MassExport");
        System.out.println("  Input file:       " + args[0]);
        System.out.println("  Output directory: " + args[2]);
        System.out.println("  It will run about every " + minutes + " minutes.");

        String destination = args[2];
        if (!(destination.endsWith("/") || destination.endsWith(File.separator))) {
            destination += File.separator;
        }

        while (true) {
            long ctime = System.currentTimeMillis();
            try {
                export(args[0], destination);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int diff = (int) (System.currentTimeMillis() - ctime);
            try {
                if (time - diff > 0) {
                    Thread.sleep(time - diff);
                }
            } catch (InterruptedException e) {
                // Nothing to do
            }
        }
    }
}
