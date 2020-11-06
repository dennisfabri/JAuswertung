package de.df.jauswertung.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.HttpUtils;
import de.df.jutils.util.NullFeedback;

public class HttpPostExport {

    private HttpPostExport() {
        // Hide Constructor
    }

    private static NullFeedback nf = new NullFeedback();

    @SuppressWarnings({})
    public static void export(String source, String destination, DateFormat df) throws IOException {
        String file = "http://" + source + "/wettkampf.wk";
        byte[] data = HttpUtils.download(file);
        AWettkampf<?> wk = InputManager.ladeWettkampf(data);
        if (wk == null) {
            throw new FileNotFoundException("Could not find file: " + file);
        }
        System.out.print("Export timestamp " + df.format(new Date()) + ": ");

        wk = CompetitionUtils.createCompetitionWithCompleteDisciplines(wk);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ExportManager.export("XML", os, ExportManager.RESULTS, wk, nf);

        String text = new String(os.toByteArray(), "ISO-8859-1");
        text = text.replace("ISO-8859-1", "UTF-8");
        HttpUtils.post(destination, text);

        System.out.println("Finished on " + df.format(new Date()));
    }

    public static void main(String[] args) {
        String source = "localhost";
        String destination = "http://localhost:8000/test";
        int minutes = 1;

        if (args != null && args.length >= 3) {
            source = args[0];

            minutes = (int) Double.parseDouble(args[1]);

            destination = args[2];
            if (!(destination.endsWith("/") || destination.endsWith(File.separator))) {
                destination += File.separator;
            }
        }

        long time = 1L * minutes * 60 * 1000;

        System.out.println("JAuswertung HttpPostExport");
        System.out.println("  Source:      " + source);
        System.out.println("  destination: " + destination);
        System.out.println("  It will run about every " + minutes + " minutes.");

        DateFormat df = DateFormat.getTimeInstance();

        while (true) {
            long ctime = System.currentTimeMillis();
            try {
                export(source, destination, df);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int diff = (int) (System.currentTimeMillis() - ctime);
            try {
                Thread.sleep(time - diff);
            } catch (InterruptedException e) {
                // Nothing to do
            }
        }
    }
}
