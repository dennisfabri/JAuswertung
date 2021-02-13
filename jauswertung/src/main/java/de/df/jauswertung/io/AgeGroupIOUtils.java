/*
 * Created on 10.04.2004
 */
package de.df.jauswertung.io;

import de.df.jauswertung.daten.regelwerk.Regelwerk;

/**
 * @author Dennis Fabri
 * @date 10.04.2004
 */
public final class AgeGroupIOUtils {

    private AgeGroupIOUtils() {
        // Hide constructor
    }

    public static synchronized Regelwerk ladeAKs(String name) {
        return AgeGroupInputUtils.ladeAKs(name);
    }

    public static synchronized boolean speichereAKs(String name, Regelwerk aks) {
        return OutputManager.speichereObject(name, aks);
        // return AgeGroupOutputUtils.speichereAKs(name, aks);
    }
}