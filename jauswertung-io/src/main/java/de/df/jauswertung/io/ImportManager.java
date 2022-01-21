/*
 * Created on 08.01.2005
 */
package de.df.jauswertung.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.exception.NotEnabledException;
import de.df.jauswertung.exception.NotSupportedException;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.NullFeedback;
import de.df.jutils.util.Tupel;

/**
 * @author Dennis Fabri @date 08.01.2005
 */
public class ImportManager {

    private static ImportManager manager = new ImportManager();

    private HashMap<String, IImporter> importers;

    public ImportManager() {
        importers = new HashMap<>();
        put(new CsvImporter());
        put(new ExcelImporter());
        put(new EasyWKImporter());
    }

    public synchronized void put(IImporter e) {
        if (e == null) {
            return;
        }
        importers.put(e.getName(), e);
    }

    public static String indizesToNames(int[] indizes, String spacer) {
        StringBuilder sb = new StringBuilder();
        sb.append(spacer);
        for (int x = 0; x < indizes.length; x++) {
            if (x > 0) {
                sb.append(", ");
            }
            // sb.append(spacer);
            switch (indizes[x]) {
            case ImportConstants.ALTERSKLASSE:
                sb.append(I18n.get("AgeGroup"));
                break;
            case ImportConstants.GESCHLECHT:
                sb.append(I18n.get("Sex"));
                break;
            case ImportConstants.GLIEDERUNG:
                sb.append(I18n.get("Organisation"));
                break;
            case ImportConstants.JAHRGANG:
                sb.append(I18n.get("YearOfBirth"));
                break;
            case ImportConstants.NACHNAME:
                sb.append(I18n.get("FamilyName"));
                break;
            case ImportConstants.NAME:
                sb.append(I18n.get("Name"));
                break;
            case ImportConstants.VORNAME:
                sb.append(I18n.get("FirstName"));
                break;
            default:
                sb.append(I18n.get("UnknownField"));
                break;
            }
        }
        return sb.toString();
    }

    public static IImporter getImporter(String name) {
        return manager.importers.get(name);
    }

    public static ImportManager getInstance() {
        return manager;
    }

    public static <T extends ASchwimmer> boolean isEnabled(AWettkampf<T> wk, ImportExportTypes datatype) {
        if (wk == null) {
            return false;
        }
        if (!isSupported(datatype)) {
            return false;
        }
        switch (datatype) {
        case ZWLIST:
        case ZW_RESULTS:
            return (wk.hasHLW());
        case HEATLIST:
        case REGISTRATION:
        case RESULTS:
            return true;
        case REFEREES:
            return (wk.getKampfrichterverwaltung() != null);
        case TEAMMEMBERS:
            return (wk instanceof MannschaftWettkampf);
        case HEATTIMES:
            return wk.isHeatBased() && wk.hasLaufliste();
        default:
            return false;
        }
    }

    public static <T extends ASchwimmer> Object importData(IImporter importer, InputStream is,
            ImportExportTypes datatype, AWettkampf<T> wk, Feedback fb)
            throws NullPointerException, NotSupportedException, NotEnabledException, TableFormatException,
            TableEntryException, TableException, IOException {
        return importData(importer, is, datatype, wk, fb, null, "");
    }

    @SuppressWarnings({ "unchecked" })
    public static <T extends ASchwimmer> Object importData(IImporter importer, InputStream is,
            ImportExportTypes datatype, AWettkampf<T> wk, Feedback fb, Object data, String filename)
            throws NullPointerException, NotSupportedException, NotEnabledException, TableFormatException,
            TableEntryException, TableException, IOException {
        if (importer == null) {
            throw new NullPointerException();
        }
        if (!isSupported(datatype)) {
            throw new NotSupportedException();
        }
        if (!isEnabled(wk, datatype)) {
            throw new NotEnabledException();
        }
        if (!importer.isSupported(datatype)) {
            throw new NotSupportedException();
        }
        if (fb == null) {
            fb = new NullFeedback();
        }
        switch (datatype) {
        case HEATLIST:
            return importer.heats(is, wk, fb);
        case ZWLIST:
            return importer.zusatzwertung(is, wk, fb);
        case REGISTRATION:
            return importer.registration(is, wk, fb, (LinkedList<T>) data, filename);
        case RESULTS:
            return importer.results(is, wk, fb);
        case REFEREES:
            return importer.referees(is, wk, fb);
        case TEAMMEMBERS:
            return importer.teammembers(is, wk, fb);
        case ZW_RESULTS:
            return importer.zusatzwertungResults(is, wk, fb);
        case HEATTIMES:
            return importer.heattimes(is, wk, fb);
        default:
            return null;
        }
    }

    public static <T extends ASchwimmer> Object importData(ImportExportTypes datatype, String filename, String format,
            AWettkampf<T> wk, Feedback fb) throws IOException, NullPointerException, NotSupportedException,
            NotEnabledException, TableFormatException, TableEntryException, TableException {
        IImporter e = getImporter(format);
        if (e == null) {
            throw new NullPointerException();
        }
        Object result = null;
        InputStream out = null;
        try {
            out = new FileInputStream(filename);
            result = importData(e, out, datatype, wk, fb, null, filename);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return result;
    }

    public static <T extends ASchwimmer> Object importData(ImportExportTypes datatype, String[] filenames,
            String format, AWettkampf<T> wk, Feedback fb) throws IOException, NullPointerException,
            NotSupportedException, NotEnabledException, TableFormatException, TableEntryException, TableException {
        IImporter e = getImporter(format);
        if (e == null) {
            throw new NullPointerException();
        }
        Object result = null;
        for (String filename : filenames) {
            InputStream out = null;
            try {
                out = new FileInputStream(filename);
                result = importData(e, out, datatype, wk, fb, result, filename);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
        return result;
    }

    @SuppressWarnings({ "unchecked" })
    public static <T extends ASchwimmer> Object finishImport(ImportExportTypes datatype, AWettkampf<T> wk, Object data,
            Feedback fb) {
        if (!isEnabled(wk, datatype)) {
            return null;
        }
        switch (datatype) {
        case REGISTRATION: {
            LinkedList<T> schwimmer = (LinkedList<T>) data;
            boolean ok = true;
            ListIterator<T> li = schwimmer.listIterator();
            while (li.hasNext()) {
                boolean b = wk.addSchwimmer(li.next());
                ok = ok && b;
            }
            if (ok) {
                return true;
            }
            break;
        }
        case TEAMMEMBERS: {
            Hashtable<String, String[]> names = (Hashtable<String, String[]>) data;
            Enumeration<String> keys = names.keys();
            while (keys.hasMoreElements()) {
                String id = keys.nextElement();
                int key = Integer.parseInt(id.substring(0, id.length() - 1));
                char part = id.charAt(id.length() - 1);
                T s = SearchUtils.getSchwimmer(wk, key);
                if ((s != null) && (s instanceof Mannschaft)) {
                    Mannschaft m = (Mannschaft) s;
                    Mannschaftsmitglied mm = m.getMannschaftsmitglied(at(part));

                    String[] info = names.get(id);
                    mm.setNachname(info[0]);
                    mm.setVorname(info[1]);
                    mm.setJahrgang(Integer.parseInt(info[2]));
                    switch (info[3].charAt(0)) {
                    case 'm':
                        mm.setGeschlecht(Geschlecht.maennlich);
                        break;
                    case 'f':
                        mm.setGeschlecht(Geschlecht.weiblich);
                        break;
                    default:
                        mm.setGeschlecht(Geschlecht.unbekannt);
                        break;
                    }
                }
            }
            return true;
        }
        case ZW_RESULTS: {
            Hashtable<Tupel<Integer, Integer>, Double> names = (Hashtable<Tupel<Integer, Integer>, Double>) data;
            Enumeration<Tupel<Integer, Integer>> keys = names.keys();
            while (keys.hasMoreElements()) {
                Tupel<Integer, Integer> key = keys.nextElement();
                T s = SearchUtils.getSchwimmer(wk, key.getFirst());
                int index = key.getSecond();
                if (s != null) {
                    double value = names.get(key);
                    int v = (int) Math.round(value);
                    if (v < -1.1) {
                        s.setHLWState(index, HLWStates.NICHT_ANGETRETEN);
                    } else if (v < -0.1) {
                        s.setHLWState(index, HLWStates.NOT_ENTERED);
                    } else {
                        s.setHLWPunkte(index, Math.max(value, 0));
                    }
                }
            }
            return true;
        }
        case RESULTS:
        case ZWLIST:
        case HEATLIST:
        case HEATTIMES:
            return data;
        case REFEREES:
            wk.setKampfrichterverwaltung((KampfrichterVerwaltung) data);
            return data;
        default:
            break;
        }

        return null;
    }

    private static int at(char x) {
        switch (x) {
        case 'a':
            return 0;
        case 'b':
            return 1;
        case 'c':
            return 2;
        case 'd':
            return 3;
        case 'e':
            return 4;
        default:
            throw new IllegalStateException("Expected character between 'a' and 'e' but was '" + x + "'.");
        }
    }

    public static String[] getSupportedFormats() {
        Set<String> keys = manager.importers.keySet();
        String[] types = new String[manager.importers.size()];
        int x = 0;
        for (String key : keys) {
            types[x] = key;
            x++;
        }
        Arrays.sort(types);
        return types;
    }

    public static boolean isSupported(String format, ImportExportTypes type) {
        IImporter e = getImporter(format);
        if (e == null) {
            return false;
        }
        return e.isSupported(type);
    }

    public static boolean isMultifileImportAllowed(ImportExportTypes type) {
        switch (type) {
        case REGISTRATION:
            return true;
        default:
            return false;
        }
    }

    public static boolean isSupported(ImportExportTypes type) {
        Collection<IImporter> en = getInstance().importers.values();
        return en.stream().anyMatch(i -> i.isSupported(type));
    }
}