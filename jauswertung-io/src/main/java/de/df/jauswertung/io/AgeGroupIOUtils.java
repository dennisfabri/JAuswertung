/*
 * Created on 10.04.2004
 */
package de.df.jauswertung.io;

import java.io.File;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.util.Utils;

/**
 * @author Dennis Fabri
 * @date 10.04.2004
 */
public final class AgeGroupIOUtils {

    private AgeGroupIOUtils() {
        // Hide constructor
    }

    public static synchronized Regelwerk ladeAKs(String name) {
        return (Regelwerk) InputManager.ladeObject(name);
    }

    public static synchronized boolean speichereAKs(String name, Regelwerk aks) {
        return OutputManager.speichereObject(name, aks);
        // return AgeGroupOutputUtils.speichereAKs(name, aks);
    }

    public static String getDefaultAKsName() {
        return "DLRG 2024";
    }

    public static Regelwerk getDefaultAKs(boolean einzel) {
        return AgeGroupIOUtils.ladeAKs(
                Utils.getUserDir() + "aks" + File.separator + getDefaultAKsName() + ".rw" + (einzel ? "e" : "m"));
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
}