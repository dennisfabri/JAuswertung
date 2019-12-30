/*
 * Created on 05.06.2004
 */
package de.df.jauswertung.util.ergebnis;

import java.util.Hashtable;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.*;

/**
 * @author Dennis Fabri
 * @date 05.06.2004
 */
public interface Formel<T extends ASchwimmer> {

    String getID();

    String getFormel();

    String getDescription();

    String getName();

    DataType getDataType();

    void setPoints(AWettkampf<T> wk, SchwimmerData<T>[] swimmer, Disziplin d, Hashtable<String, Zielrichterentscheid<T>> zes);

    SchwimmerResult<T>[] toResults(SchwimmerResult<T>[] results, AWettkampf<T> wk, Altersklasse ak, Hashtable<String, Zielrichterentscheid<T>> zes, boolean zw);
}