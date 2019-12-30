package de.df.jauswertung.misc;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.io.*;

public class Namenextraktor {

    private static String[]           vornamenM    = null;
    private static String[]           vornamenW    = null;
    private static String[]           nachnamen    = null;

    private static LinkedList<String> vornamenMNeu = new LinkedList<String>();
    private static LinkedList<String> vornamenWNeu = new LinkedList<String>();
    private static LinkedList<String> nachnamenNeu = new LinkedList<String>();

    public static void main(String[] args) throws IOException {
        vornamenM = readNamen("VornamenM.txt", new String[0]);
        vornamenW = readNamen("VornamenW.txt", new String[0]);
        nachnamen = readNamen("Nachnamen.txt", new String[0]);

        System.out.println(vornamenM.length);

        File[] files = new File("data/").listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile()) {
                    if (pathname.getAbsolutePath().endsWith(".wk")) {
                        return true;
                    }
                }
                return false;
            }
        });
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
        writeFile("vornamenM.txt", data);

        data = vornamenWNeu.toArray(new String[vornamenWNeu.size()]);
        Arrays.sort(data);
        writeFile("vornamenW.txt", data);

        data = nachnamenNeu.toArray(new String[nachnamenNeu.size()]);
        Arrays.sort(data);
        writeFile("nachnamen.txt", data);
    }

    private static void process(AWettkampf<?> wk) {
        if (wk instanceof EinzelWettkampf) {
            EinzelWettkampf ewk = (EinzelWettkampf) wk;
            for (Teilnehmer t : ewk.getSchwimmer()) {
                String vorname = t.getVorname();
                String nachname = t.getNachname();
                if (t.isMaennlich()) {
                    if (!contains(vornamenM, vorname) && !vornamenMNeu.contains(vorname) && !contains(nachnamen, vorname)) {
                        vornamenMNeu.add(vorname);
                    }
                } else {
                    if (!contains(vornamenW, vorname) && !vornamenWNeu.contains(vorname) && !contains(nachnamen, vorname)) {
                        vornamenWNeu.add(vorname);
                    }
                }
                if (!contains(nachnamen, nachname) && !nachnamenNeu.contains(nachname) && !contains(vornamenM, nachname) && !contains(vornamenW, nachname)) {
                    nachnamenNeu.add(nachname);
                }
            }
        }
    }

    private static boolean contains(String[] data, String text) {
        for (String d : data) {
            if (d.equals(text)) {
                return true;
            }
        }
        return false;
    }

    private static String[] readNamen(String date, String[] backup) {
        return IOUtils.fileToStringArray("include/main/names", date, backup, false);
    }

    private static boolean writeFile(String date, String[] backup) {
        return IOUtils.StringArrayToFile(".", date, backup);
    }
}
