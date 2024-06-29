package de.df.jauswertung.gui.util;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.*;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.util.EDTUtils;

public final class TableZWUtils {

    private TableZWUtils() {
        // Hide
    }

    public static <T extends ASchwimmer> Object[] getHeatNames(AWettkampf<T> wk) {
        ListIterator<Lauf<T>> li = wk.getLaufliste().getLaufliste().listIterator();
        LinkedList<Object> result = new LinkedList<>();
        while (li.hasNext()) {
            result.addLast(new UniqueString(li.next().getName()));
        }
        return result.toArray(new Object[0]);
    }

    /**
     * UniqueString is a class to support JComboBoxes with equals entries.
     */
    private static final class UniqueString {

        private final String text;

        public UniqueString(String s) {
            text = s;
        }

        @Override
        public String toString() {
            return text;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}