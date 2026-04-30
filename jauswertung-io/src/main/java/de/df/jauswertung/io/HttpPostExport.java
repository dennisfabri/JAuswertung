package de.df.jauswertung.io;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.io.util.HttpUtils;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jutils.util.NullFeedback;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public final class HttpPostExport {

    private HttpPostExport() {
        // Hide Constructor
    }

    private static NullFeedback nf = new NullFeedback();

    @SuppressWarnings({})
    public static void export(String source, String destination) throws IOException {
        String url = "http://" + source + "/wettkampf.wk";
        byte[] data = HttpUtils.download(url);
        AWettkampf<?> wk = InputManager.ladeWettkampf(data);
        if (wk == null) {
            throw new FileNotFoundException("Could not find url: " + url);
        }
        log.info("Exporting data from {} to {}", url, destination);

        wk = CompetitionUtils.createCompetitionWithCompleteDisciplines(wk);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ExportManager.export("XML", os, ImportExportTypes.RESULTS, wk, nf);

        String text = os.toString(StandardCharsets.UTF_8);
        HttpUtils.post(destination, text);

        log.info("Export finished");
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

        while (true) {
            long ctime = System.currentTimeMillis();
            try {
                export(source, destination);
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
