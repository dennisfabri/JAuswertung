package de.df.jauswertung.misc.meldetoolimport;

import java.io.File;

public class Meldetool2JA {

    public static void main(String[] args) {
        String directory = "AddOn/Daten/JRP 2019";
        if (!CheckFiles(directory)) {
            return;
        }
        System.out.println("Files exist.");

        Zuordnung zos = new Zuordnung(directory + "/Zuordnung OCEAN Einzel.txt", Zuordnungstyp.OpenSingle);
        Zuordnung zot = new Zuordnung(directory + "/Zuordnung OCEAN Staffel.txt", Zuordnungstyp.OpenTeam);
        Zuordnung zom = new Zuordnung(directory + "/Zuordnung OCEAN Mixed.txt", Zuordnungstyp.OpenMixed);
        Zuordnung zps = new Zuordnung(directory + "/Zuordnung Pool Einzel.txt", Zuordnungstyp.PoolSingle);
        Zuordnung zpt = new Zuordnung(directory + "/Zuordnung Pool Staffel.txt", Zuordnungstyp.PoolTeam);
        Zuordnung zpm = new Zuordnung(directory + "/Zuordnung Pool Mixed.txt", Zuordnungstyp.PoolMixed);

        Gliederungen gld = new Gliederungen(directory + "/Gliederungen.csv");

        Meldungsimport mi = new Meldungsimport(directory + "/Meldung.csv");
        System.out.println("Meldung importiert: " + mi.size());
        mi.erstelleMeldung(zos, gld, directory + "/Meldung OCEAN Einzel.csv");
        mi.erstelleMeldung(zot, gld, directory + "/Meldung OCEAN Staffel.csv");
        mi.erstelleMeldung(zom, gld, directory + "/Meldung OCEAN Mixed.csv");
        mi.erstelleMeldung(zps, gld, directory + "/Meldung Pool Einzel.csv");
        mi.erstelleMeldung(zpt, gld, directory + "/Meldung Pool Staffel.csv");
        mi.erstelleMeldung(zpm, gld, directory + "/Meldung Pool Mixed.csv");
    }

    private static String[] filenames = new String[] { "Meldung.csv", "Gliederungen.csv", "Zuordnung Pool Einzel.txt", "Zuordnung Pool Staffel.txt",
            "Zuordnung Pool Mixed.txt", "Zuordnung OCEAN Einzel.txt", "Zuordnung OCEAN Staffel.txt", "Zuordnung OCEAN Mixed.txt" };

    private static boolean CheckFiles(String dir) {
        for (String filename : filenames) {
            if (!new File(dir + "/" + filename).exists()) {
                System.out.println("File is missing: " + filename);
                return false;
            }

        }
        return true;
    }
}
