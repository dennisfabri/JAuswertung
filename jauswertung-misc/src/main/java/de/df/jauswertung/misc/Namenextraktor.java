package de.df.jauswertung.misc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.TextFileUtils;

public class Namenextraktor {

    private static Set<String> vornamenM = null;
    private static Set<String> vornamenW = null;
    private static Set<String> nachnamen = null;

    private static List<String> vornamenMNeu = new ArrayList<>();
    private static List<String> vornamenWNeu = new ArrayList<>();
    private static List<String> nachnamenNeu = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        vornamenM = readNamen("VornamenM.txt");
        vornamenW = readNamen("VornamenW.txt");
        nachnamen = readNamen("Nachnamen.txt");

        System.out.println("Vornamen m: " + vornamenM.size());
        System.out.println("Vornamen w: " + vornamenW.size());
        System.out.println("Nachnamen: " + nachnamen.size());

        File[] files = new File("data/")
                .listFiles(pathname -> pathname.isFile() && pathname.getAbsolutePath().endsWith(".wk"));
        for (File file : files) {
            try {
                System.out.println("Lade " + file.getAbsolutePath());
                AWettkampf<?> wk = InputManager.ladeWettkampf(file.getAbsolutePath());
                if (wk != null) {
                    process(wk);
                }
            } catch (Exception ex) {
                System.out.println("  Problem bei der Verarbeiterung der Datei");
            }
        }

        String[] data = vornamenMNeu.toArray(new String[vornamenMNeu.size()]);
        Arrays.sort(data);
        writeFile("vornamenM1.txt", data);

        data = vornamenWNeu.toArray(new String[vornamenWNeu.size()]);
        Arrays.sort(data);
        writeFile("vornamenW1.txt", data);

        data = nachnamenNeu.toArray(new String[nachnamenNeu.size()]);
        Arrays.sort(data);
        writeFile("nachnamen1.txt", data);
    }

    private static void process(AWettkampf<?> wk) {
        if (wk instanceof EinzelWettkampf) {
            EinzelWettkampf ewk = (EinzelWettkampf) wk;
            for (Teilnehmer t : ewk.getSchwimmer()) {
                String vorname = t.getVorname();
                String nachname = t.getNachname();
                if (!vorname.contains(" ") && !vorname.contains("?")) {
                    if (t.isMaennlich()) {
                        if (!vornamenM.contains(vorname) && !vornamenMNeu.contains(vorname)
                                && !nachnamen.contains(vorname)) {
                            vornamenMNeu.add(vorname);
                        }
                    } else {
                        if (!vornamenW.contains(vorname) && !vornamenWNeu.contains(vorname)
                                && !nachnamen.contains(vorname)) {
                            vornamenWNeu.add(vorname);
                        }
                    }
                }
                if (!nachname.contains("?")) {
                    if (!nachnamen.contains(nachname) && !nachnamenNeu.contains(nachname)
                            && !vornamenM.contains(nachname)
                            && !vornamenW.contains(nachname)) {
                        nachnamenNeu.add(nachname);
                    }
                }
            }
        }
    }

    private static Set<String> readNamen(String name) {
        try {
            return new HashSet<>(
                    Files.readAllLines(Path.of("jauswertung-files", "src", "main", "resources", "names", name),
                            Charset.forName("Cp1252")));
        } catch (IOException ex) {
            ex.printStackTrace();
            return new HashSet<>();
        }
    }

    private static boolean writeFile(String date, String[] backup) {
        return TextFileUtils.StringArrayToFile("jauswertung-files/src/main/resources/names", date, backup);
    }
}
