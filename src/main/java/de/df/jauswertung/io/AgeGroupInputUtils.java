/*
 * Created on 10.04.2004
 */
package de.df.jauswertung.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.util.ergebnis.*;

/**
 * @author Dennis Fabri
 * @date 10.04.2004
 */
final class AgeGroupInputUtils {

    private AgeGroupInputUtils() {
        // Hide constructor
    }

    public static synchronized Regelwerk ladeAKs(String name) {
        Regelwerk aks = ladeAKsLegacy(name);
        if (aks != null) {
            return aks;
        }
        return (Regelwerk) InputManager.ladeObject(name);
    }

    private static synchronized Regelwerk ladeAKsLegacy(String name) {
        Regelwerk aks = null;
        try {
            ZipFile zf = new ZipFile(name);
            ZipEntry entry = zf.getEntry("header");
            if (entry == null) {
                try {
                    zf.close();
                } catch (Exception ex) {
                    // Nothing to do
                }
                throw new IllegalStateException("Header expected");
            }
            InputStreamReader isr = new InputStreamReader(zf.getInputStream(entry));
            BufferedReader br = new BufferedReader(isr);
            boolean einzel = "true".equals(readLine(br));
            int count = Integer.parseInt(readLine(br));
            aks = new Regelwerk(count, einzel, FormelDLRG.ID);
            String line = readLine(br);
            if (line != null) {
                if (FormelManager.getInstance().get(line) != null) {
                    aks.setFormelID(line);
                } else {
                    aks.setFormelID(FormelDLRG.ID);
                }
            } else {
                aks.setFormelID(FormelDLRG.ID);
            }
            line = readLine(br);
            if (line != null) {
                aks.setBeschreibung(line.trim());
            }
            line = readLine(br);
            if (line != null) {
                aks.setGesamtwertung("true".equals(line));
                line = readLine(br);
                aks.setGesamtwertungHart("true".equals(line));
                line = readLine(br);
                aks.setGesamtwertungSkalieren("true".equals(line) ? Skalierungsmodus.ANZAHL_DISZIPLINEN : Skalierungsmodus.KEINER);
                line = readLine(br);
                if ("all".equals(line)) {
                    aks.setGesamtwertungsmodus(GroupEvaluationMode.All);
                } else {
                    if ("best".equals(line)) {
                        aks.setGesamtwertungsmodus(GroupEvaluationMode.Best);
                    } else {
                        aks.setGesamtwertungsmodus(GroupEvaluationMode.BestInDiscipline);
                    }
                }
            }
            br.close();
            for (int x = 0; x < count; x++) {
                isr = new InputStreamReader(zf.getInputStream(zf.getEntry("" + x)));
                br = new BufferedReader(isr);
                aks.setAk(x, ladeAK(br));
                br.close();
            }
            zf.close();
            return aks;
        } catch (RuntimeException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private static String readLine(BufferedReader br) throws IOException {
        String s = br.readLine();
        if (s == null) {
            return null;
        }
        return s.trim();
    }

    private static synchronized Altersklasse ladeAK(BufferedReader br) throws IOException {
        String name = readLine(br);
        boolean hlw = "true".equals(readLine(br));
        boolean kompakt = "true".equals(readLine(br));
        boolean gww = "true".equals(readLine(br));
        boolean gwm = "true".equals(readLine(br));
        int count = Integer.parseInt(readLine(br));
        Disziplin[][] disziplinen = new Disziplin[2][count];
        for (int x = 0; x < count; x++) {
            for (int y = 0; y < 2; y++) {
                String dname = readLine(br);
                int rec = Integer.parseInt(readLine(br));
                disziplinen[y][x] = new Disziplin(dname, rec, 50, 1);
            }
        }
        int min = count;
        int max = count;
        String line = readLine(br);
        boolean choice = "true".equals(line);
        min = Integer.parseInt(readLine(br));
        max = Integer.parseInt(readLine(br));
        boolean einzelwertung = "true".equals(readLine(br));
        boolean einzelwertunghlw = "true".equals(readLine(br));
        line = readLine(br);
        boolean strafeIstDisqualifikation = false;
        if (line != null) {
            strafeIstDisqualifikation = "true".equals(line);
        }

        Altersklasse ak = new Altersklasse(name, disziplinen, hlw);
        ak.setGesamtwertung(false, gww);
        ak.setGesamtwertung(true, gwm);
        ak.setKompaktToString(kompakt);
        ak.setDisciplineChoiceAllowed(choice);
        ak.setChosenDisciplines(min, min, max);
        ak.setEinzelwertung(einzelwertung);
        ak.setEinzelwertungHlw(einzelwertunghlw);
        ak.setStrafeIstDisqualifikation(strafeIstDisqualifikation);
        return ak;
    }
}