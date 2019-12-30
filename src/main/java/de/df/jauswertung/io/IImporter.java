package de.df.jauswertung.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedList;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.Tupel;

/**
 * Interface zum Import von Daten
 * 
 * @author Dennis Fabri
 * @date 17.06.2006
 */
public interface IImporter {
    /**
     * Importiert aus einer Datei die Meldedaten von Schwimmern.
     * 
     * @param <T>
     *            Teilnehmer oder Mannschaft
     * @param name
     *            Name der Datei
     * @param wk
     *            Aktueller Wettkampf
     * @return Liste der importierten Schwimmer
     * @throws TableFormatException
     *             Die TableFormatException wird geworfen, wenn noetige
     *             Informationen im Aufbau der Datei nicht vorgesehen sind.
     * @throws TableEntryException
     *             Die TableEntryException wird geworfen, wenn Eintraege ein
     *             falsches Format aufweisen.
     */
    <T extends ASchwimmer> LinkedList<T> registration(InputStream name, AWettkampf<T> wk, Feedback fb, LinkedList<T> data, String filename)
            throws TableFormatException, TableEntryException, TableException, IOException;

    <T extends ASchwimmer> Hashtable<String, String[]> teammembers(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException;

    <T extends ASchwimmer> Hashtable<Tupel<Integer, Integer>, Double> zusatzwertungResults(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException;

    <T extends ASchwimmer> AWettkampf<T> heats(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException;

    <T extends ASchwimmer> AWettkampf<T> zusatzwertung(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException;

    <T extends ASchwimmer> AWettkampf<T> results(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException;

    <T extends ASchwimmer> AWettkampf<T> heattimes(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException;

    <T extends ASchwimmer> KampfrichterVerwaltung referees(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException;

    boolean isSupported(int type);

    String getName();

    String[] getSuffixes();
}