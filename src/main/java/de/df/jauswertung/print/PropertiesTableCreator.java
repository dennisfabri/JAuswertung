/*
 * Created on 01.06.2005
 */
package de.df.jauswertung.print;

import static de.df.jauswertung.daten.PropertyConstants.AUSRICHTER;
import static de.df.jauswertung.daten.PropertyConstants.BEGIN;
import static de.df.jauswertung.daten.PropertyConstants.DATE;
import static de.df.jauswertung.daten.PropertyConstants.DEPTH_OF_POOL;
import static de.df.jauswertung.daten.PropertyConstants.END;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static de.df.jauswertung.daten.PropertyConstants.LENGTH_OF_POOL;
import static de.df.jauswertung.daten.PropertyConstants.LOCATION;
import static de.df.jauswertung.daten.PropertyConstants.NAME;
import static de.df.jauswertung.daten.PropertyConstants.NAME_OF_POOL;
import static de.df.jauswertung.daten.PropertyConstants.ORGANIZER;
import static de.df.jauswertung.daten.PropertyConstants.OTHER_COMPETITION_INFO;
import static de.df.jauswertung.daten.PropertyConstants.OTHER_LOCATION_INFO;
import static de.df.jauswertung.daten.PropertyConstants.POSITION_OF_MANAKIN;
import static de.df.jauswertung.daten.PropertyConstants.WATERTEMPERATURE;

import java.awt.print.Printable;
import java.util.LinkedList;

import javax.swing.JTable;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jutils.print.TextTablePrintable;

public final class PropertiesTableCreator {

    private PropertiesTableCreator() {
        // Hide
    }

    public static <T extends ASchwimmer> JTable createTable(AWettkampf<T> wk) {
        try {
            LinkedList<String> names = new LinkedList<String>();
            LinkedList<String> data = new LinkedList<String>();

            String filtername = wk.getCurrentFilter().getName();
            if (wk.getCurrentFilterIndex() == 0) {
                filtername = null;
            }

            names.addLast(I18n.get("Name") + ":   ");
            data.addLast(wk.getStringProperty(NAME) + (filtername != null ? " (" + filtername + ")" : ""));

            names.addLast(I18n.get("Location") + ":   ");
            data.addLast(wk.getStringProperty(LOCATION));

            names.addLast(I18n.get("Organizer") + ":   ");
            data.addLast(wk.getStringProperty(ORGANIZER));

            names.addLast(I18n.get("Ausrichter") + ":   ");
            data.addLast(wk.getStringProperty(AUSRICHTER));

            names.addLast(I18n.get("Date") + ":   ");
            data.addLast(wk.getStringProperty(DATE));

            names.addLast(I18n.get("Begin") + ":   ");
            data.addLast(wk.getStringProperty(BEGIN));

            names.addLast(I18n.get("End") + ":   ");
            data.addLast(wk.getStringProperty(END));

            String s = wk.getStringProperty(OTHER_COMPETITION_INFO);
            if ((s != null) && (s.trim().length() > 0)) {
                names.addLast(I18n.get("Other") + ":   ");
                data.addLast(s);
            }

            names.addLast(I18n.get("NameOfPool") + ":   ");
            data.addLast(wk.getStringProperty(NAME_OF_POOL));

            names.addLast(I18n.get("DepthOfPool") + ":   ");
            data.addLast(wk.getStringProperty(DEPTH_OF_POOL));

            names.addLast(I18n.get("Poollength") + ":   ");
            data.addLast(wk.getStringProperty(LENGTH_OF_POOL));

            names.addLast(I18n.get("NumberOfLanes") + ":   ");
            data.addLast("" + wk.getIntegerProperty(HEATS_LANES));

            names.addLast(I18n.get("Watertemperature") + ":   ");
            data.addLast(wk.getStringProperty(WATERTEMPERATURE));

            names.addLast(I18n.get("Puppenaufnahme") + ":   ");
            data.addLast(wk.getStringProperty(POSITION_OF_MANAKIN));

            s = wk.getStringProperty(OTHER_LOCATION_INFO);
            if ((s != null) && (s.trim().length() > 0)) {
                names.addLast(I18n.get("Other") + ":   ");
                data.addLast(s);
            }

            names.addLast(" ");
            data.addLast(" ");
            names.addLast(" ");
            data.addLast(" ");

            names.addLast(I18n.get("Rulebook") + ":");
            data.addLast(I18n.get("RulebookAndYear", FormelManager.getInstance().get(wk.getRegelwerk().getFormelID()).getName(),
                    wk.getRegelwerk().getBeschreibung()));

            names.addLast(I18n.get("Software") + ":");
            String date = Utils.getBuildDate();
            if (date != null) {
                date = date.trim();
                if (date.length() > 0) {
                    date = date + " - ";
                }
            } else {
                date = "";
            }
            data.addLast(I18n.getVersion() + " - " + date + I18n.get("ProgramURLInfo") + "");

            return TextTablePrintable.createTable(names.toArray(new String[names.size()]), data.toArray(new String[data.size()]));
        } catch (RuntimeException re) {
            re.printStackTrace();
            return null;
        }
    }

    public static <T extends ASchwimmer> Printable getPrintable(AWettkampf<T> wk) {
        return new TextTablePrintable(PropertiesTableCreator.createTable(wk));
    }
}