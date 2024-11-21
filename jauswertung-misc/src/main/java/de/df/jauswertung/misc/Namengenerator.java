/*
 * NameGenerator.java Created on 5. September 2002, 19:18
 */

package de.df.jauswertung.misc;

/**
 * @author dennis
 */
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import de.df.jauswertung.io.TextFileUtils;
import de.df.jauswertung.util.RandomUtils;

public class Namengenerator {

    private static Random zufall = RandomUtils.getRandomNumberGenerator();

    private static String[] vornamenM2 = { "Dennis", "Dirk", "Christian", "Holger", "David", "Stefan", "Carsten",
            "Sven", "Kai", "Matthias",
            "Christoph", "Daniel", "Markus", "Maik", "Martin", "Maximilian", "Marius", "Andreas", "Alexander", "Timo",
            "Andreas", "Thomas", "Jens", "Pascal",
            "Benjamin" };
    private static String[] vornamenW2 = { "Anke", "Stefanie", "Bianca", "Susanne", "Christina", "Marina", "Andrea",
            "Julia", "Mareike", "Sandra",
            "Christine", "Anna", "Marie", "Sophie", "Maria", "Janine", "Jasmin", "Astrid", "Sabrina", "Nadja", "Gina",
            "Angela", "Lisa", "Karin", "Alexandra",
            "Eva", "Melanie", "Carolin", "Regina", "Daniela" };
    private static String[] nachnamen2 = { "Fabri", "Meier", "Schulz", "Meyer", "Schmidt", "Spicker", "Schwert",
            "David", "Lange", "Himmel",
            "Ullrich", "Wahl", "Wagner", "Nitsche", "Fuchs", "Peters", "Kersting", "Langer", "Schneider", "Lindner" };
    private static String[] gliederungen2 = { "Gelsenkirchen", "Dortmund", "Schwerte", "Bochum", "M\u00FCnster",
            "M\u00FCnchen", "Hamburg",
            "Hannover", "Essen", "Lippstadt", "Recklinghausen", "Borken" };

    private static String[] lvs2 = { "BY", "NR", "SH", "WE" };

    private String dir = "required";

    private String[] vornamenM = null;
    private String[] vornamenW = null;
    private String[] nachnamen = null;
    private String[] gliederungen = null;
    private String[] lvs = null;

    private Hashtable<String, String> gld2lv = new Hashtable<>();

    private String[] readNamen(String date, String[] backup) {
        return TextFileUtils.fileToStringArray(dir, date, backup, false);
    }

    public String generateVorname(boolean maennlich) {
        if (!maennlich) {
            return vornamenW[zufall.nextInt(vornamenW.length)];
        }
        return vornamenM[zufall.nextInt(vornamenM.length)];
    }

    public String generateNachname() {
        return nachnamen[zufall.nextInt(nachnamen.length)];
    }

    public String generateLV(String gliederung) {
        if (!gld2lv.containsKey(gliederung)) {
            gld2lv.put(gliederung, lvs[zufall.nextInt(lvs.length)]);
        }
        return gld2lv.get(gliederung);
    }

    public String generateGliederung() {
        return gliederungen[zufall.nextInt(gliederungen.length)];
    }

    public Namengenerator(String directory, int anzahl) {
        dir = directory;
        if (dir == null) {
            dir = "";
        }
        vornamenM = readNamen("VornamenM.txt", vornamenM2);
        vornamenW = readNamen("VornamenW.txt", vornamenW2);
        nachnamen = readNamen("Nachnamen.txt", nachnamen2);
        gliederungen = readNamen("Gliederungen.txt", gliederungen2);
        lvs = readNamen("LVs.txt", lvs2);

        if (gliederungen.length > anzahl) {
            LinkedList<String> gl = new LinkedList<>();
            for (String aGliederungen : gliederungen) {
                gl.addLast(aGliederungen);
            }
            Collections.shuffle(gl);
            int size = gl.size();
            for (int x = 0; x < size - anzahl; x++) {
                gl.removeFirst();
            }
            gliederungen = gl.toArray(new String[gl.size()]);
        }
    }

    public String generateCompetitorId() {
        return "" + zufall.nextInt(100000) + 100000;
    }
}