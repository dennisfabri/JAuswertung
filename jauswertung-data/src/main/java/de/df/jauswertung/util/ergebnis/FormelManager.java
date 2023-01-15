/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.util.Utils;

/**
 * @author Dennis Fabri
 * @date 05.06.2004
 */
public final class FormelManager {

    private static FormelManager instance = new FormelManager();

    private Hashtable<String, Formel<ASchwimmer>> formeln = new Hashtable<String, Formel<ASchwimmer>>();

    private LinkedList<Formel<ASchwimmer>> flist = new LinkedList<Formel<ASchwimmer>>();
    private LinkedList<Formel<ASchwimmer>> publicFlist = new LinkedList<Formel<ASchwimmer>>();

    /**
     * 
     */
    private FormelManager() {
        add(new FormelDLRG<ASchwimmer>(), false);
        add(new FormelDLRG2007<ASchwimmer>());
        add(new FormelDLRG2007Finals<ASchwimmer>());
        add(new FormelILS<ASchwimmer>());
        add(new FormelILSSERC<ASchwimmer>());
        add(new FormelILSOutdoor<ASchwimmer>());
        add(new FormelILSFinals<ASchwimmer>());
        add(new FormelILSOutdoorFinals<ASchwimmer>());
        add(new FormelDP<ASchwimmer>());
        add(new FormelDSV<ASchwimmer>(), false);
        add(new FormelFeuerwehr2005<ASchwimmer>(), false);
        add(new FormelMedaillen<ASchwimmer>(), false);
        add(new FormelLinear<ASchwimmer>());
        add(new FormelDirectPoints<ASchwimmer>(), false);
    }

    public static FormelManager getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T extends ASchwimmer> Formel<T> get(String type) {
        return (Formel<T>) formeln.get(type);
    }

    private void add(Formel<ASchwimmer> f) {
        add(f, true);
    }

    private void add(Formel<ASchwimmer> f, boolean isPublic) {
        if (f == null) {
            throw new NullPointerException();
        }
        formeln.put(f.getID(), f);
        flist.addLast(f);
        if (isPublic || Utils.isInDevelopmentMode()) {
            publicFlist.addLast(f);
            Collections.sort(publicFlist, new Comparator<Formel<ASchwimmer>>() {

                @Override
                public int compare(Formel<ASchwimmer> o1, Formel<ASchwimmer> o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    public static Formel<ASchwimmer>[] getFormeln() {
        return getInstance().publicFlist.toArray(new Formel[getInstance().publicFlist.size()]);
    }

    public static boolean isDLRGBased(String id) {
        Formel<ASchwimmer> f = getInstance().get(id);
        return (f instanceof FormelDLRG2007);
    }

    public static boolean isHeatBased(String id) {
        Formel<ASchwimmer> f = getInstance().get(id);
        return (f instanceof FormelILSFinals);
    }

    public static DataType getDataType(String id) {
        Formel<ASchwimmer> f = getInstance().get(id);
        return f.getDataType();
    }

    public static boolean isOpenwater(String id) {
        Formel<ASchwimmer> f = getInstance().get(id);
        return (f instanceof FormelILSOutdoorFinals) || (f instanceof FormelILSOutdoor);
    }
}