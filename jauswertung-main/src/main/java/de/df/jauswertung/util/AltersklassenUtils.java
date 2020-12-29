/*
 * AltersklassenTools.java Created on 3. Oktober 2002, 12:28
 */

package de.df.jauswertung.util;

/**
 * @author dennis
 */

import java.io.File;
import java.util.Hashtable;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.util.ergebnis.FormelDLRG;

public final class AltersklassenUtils {

    private AltersklassenUtils() {
        // Never used
    }

    public static int getAKNummer(final Altersklasse ak, final Regelwerk aks) {
        if (ak == null) {
            return -1;
        }
        if (aks == null) {
            return -1;
        }

        for (int x = 0; x < aks.size(); x++) {
            if (ak.equals(aks.getAk(x))) {
                return x;
            }
        }
        return -1;
    }

    public static int getMaxDiszCount(Regelwerk aks) {
        int count = 0;
        for (int x = 0; x < aks.size(); x++) {
            if (aks.getAk(x).getDiszAnzahl() > count) {
                count = aks.getAk(x).getDiszAnzahl();
            }
        }
        return count;
    }

    private static String[] DISCIPLINE_PREFIXES = new String[] { "1x", "2x", "3x", "4x", "1*", "2*", "3*", "4*", "25m", "50m", "100m", "200m" };

    public static <T extends ASchwimmer> int getUniqueDisciplinesCount(AWettkampf<T> wk) {
        Regelwerk aks = wk.getRegelwerk();
        Hashtable<String, String> result = new Hashtable<String, String>();
        for (int x = 0; x < aks.size(); x++) {
            if (SearchUtils.hasSchwimmer(wk, aks.getAk(x))) {
                Disziplin[] disziplinen = aks.getAk(x).getDisziplinen(true);
                for (Disziplin aDisziplinen : disziplinen) {
                    String name = aDisziplinen.getName();
                    name = name.toLowerCase().replace(" ", "");
                    for (String DISCIPLINE_PREFIXE : DISCIPLINE_PREFIXES) {
                        name = name.replace(DISCIPLINE_PREFIXE, "");
                    }
                    if (name.equals("kombiniertesschwimmen")) {
                        name = "retten";
                    } else if (name.equals("freistilschwimmen")) {
                        name = "hindernisschwimmen";
                    } else if (name.equals("flossenschwimmen")) {
                        name = "rettenmitflossen";
                    }
                    result.put(name, name);
                }
            }
        }
        return result.size() + (aks.hasHlw() ? 1 : 0);
    }

    public static String getDefaultAKsName() {
        return "DLRG 2021";
    }

    public static Regelwerk getDefaultAKs(boolean einzel) {
        return AgeGroupIOUtils.ladeAKs(Utils.getUserDir() + "aks" + File.separator + getDefaultAKsName() + ".rw" + (einzel ? "e" : "m"));
    }

    public static Regelwerk getAKs(String name) {
        Regelwerk aks = AgeGroupIOUtils.ladeAKs(Utils.getUserDir() + "aks" + File.separator + name);
        if (aks == null) {
            aks = AgeGroupIOUtils.ladeAKs(name);
        }
        return aks;
    }

    public static Regelwerk getAKs(String name, boolean einzel) {
        return getAKs(name + (einzel ? ".rwe" : ".rwm"));
    }

    public static int getAkNummer(final Altersklasse ak, final Regelwerk aks) {
        if (ak == null) {
            throw new NullPointerException();
        }
        if (aks == null) {
            throw new NullPointerException();
        }
        String akname = ak.toString();
        int ergebnis = 0;
        for (int x = 0; x < aks.size(); x++) {
            if (akname.equals(aks.getAk(x).toString())) {
                ergebnis = x;
            }
        }
        return ergebnis;
    }

    public static Regelwerk generateGesamtwertungAKs() {
        Regelwerk aks = new Regelwerk(1, false, FormelDLRG.ID);
        Altersklasse gesamtwertungAK = new Altersklasse("Gesamtwertung", new Disziplin[2][0], false, true);
        aks.setAk(0, gesamtwertungAK);
        return aks;
    }

    public static Regelwerk checkAKs(final Regelwerk aks, boolean einzel) {
        if (aks == null) {
            // throw new IllegalArgumentException("Agegroups must not be null!");
            return new Regelwerk(einzel, FormelDLRG.ID);
        }
        return aks;
    }
}