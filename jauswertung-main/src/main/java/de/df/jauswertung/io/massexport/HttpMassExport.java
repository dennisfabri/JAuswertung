package de.df.jauswertung.io.massexport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.HttpUtils;
import de.df.jutils.io.FileUtils;

public class HttpMassExport {

    private HttpMassExport() {
        // Hide Constructor
    }

    public static void export(String source, String destination) throws IOException {
        String file = "http://" + source + "/wettkampf.wk";
        byte[] data = HttpUtils.download(file);
        AWettkampf<?> wk = InputManager.ladeWettkampf(data);
        if (wk == null) {
            throw new FileNotFoundException("Could not find file: " + file);
        }
        String part2 = "single";
        if (wk instanceof MannschaftWettkampf) {
            part2 = "team";
        }
        String part1 = "pool";
        if (wk.isOpenWater()) {
            part1 = "ow";
        }
        CompetitionExporter.export(destination + part1 + "-" + part2 + File.separator, wk, new String[] { "HTML" });
        System.out.println();
    }

    public static void main(String[] args) {
        int minutes = (int) Double.parseDouble(args[1]);
        long time = 1L * minutes * 60 * 1000;
        System.out.println("JAuswertung HttpMassExport");
        System.out.println("  Source host:             " + args[0]);
        System.out.println("  Output directory:        " + args[2]);
        System.out.println("  Configuration directory: " + args[3]);
        System.out.println("  It will run about every " + minutes + " minutes.");

        String destination = args[2];
        if (!(destination.endsWith("/") || destination.endsWith(File.separator))) {
            destination += File.separator;
        }

        String configDir = args[3];
        if (!(configDir.endsWith("/") || configDir.endsWith(File.separator))) {
            configDir += File.separator;
        }

        while (true) {
            long ctime = System.currentTimeMillis();
            try {
                String[] command = FileUtils.readTextFile(configDir + "upload_command.txt");
                export(args[0], destination);
                System.out.print("Upload command: ");
                for (String s : command) {
                    System.out.print(" " + s);
                }
                System.out.println();
                Runtime.getRuntime().exec(command);
                // Runtime.getRuntime().exec(new String[] { "tools/winscp/winscp.com",
                // "/script=" + destination + "../uploadscript.txt" });
                System.out.println(" finished");
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
