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
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Eingabe;
import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.io.exception.NotEnabledException;
import de.df.jauswertung.io.exception.NotSupportedException;
import de.df.jauswertung.io.value.TeamWithStarters;
import de.df.jauswertung.io.value.ZWStartnummer;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.NullFeedback;

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
            return wk.hasSchwimmer() && wk.hasHLW();
        case REGISTRATION:
            return true;
        case REFEREES:
            return wk.getKampfrichterverwaltung() != null;
        case TEAMMEMBERS:
            return wk.hasSchwimmer() && wk instanceof MannschaftWettkampf;
        case STARTERS:
            return wk.hasSchwimmer() && wk instanceof MannschaftWettkampf && Utils.isInDevelopmentMode();
        case HEATTIMES:
            return wk.hasSchwimmer() && wk.isHeatBased() && wk.hasLaufliste();
        case RESULTS:
        case HEATLIST:
            return wk.hasSchwimmer();
        case REGISTRATION_UPDATE:
            return wk.hasSchwimmer() && Utils.isInDevelopmentMode();
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
        case REGISTRATION_UPDATE:
            return importer.registrationUpdate(is, wk, fb, (LinkedList<T>) data, filename);
        case STARTERS:
            return importer.starters(is, wk, fb);
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
        try (InputStream input = new FileInputStream(filename)) {
            return importData(e, input, datatype, wk, fb, null, filename);
        }
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
            try (InputStream out = new FileInputStream(filename)) {
                result = importData(e, out, datatype, wk, fb, result, filename);
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
        case REGISTRATION_UPDATE: {
            if (updateRegistration(wk, (LinkedList<T>) data)) {
                return true;
            }
            break;
        }
        case STARTERS: {
            if (updateStarters(wk, (List<TeamWithStarters>) data)) {
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
                if (s instanceof Mannschaft m) {
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
            Hashtable<ZWStartnummer, Double> names = (Hashtable<ZWStartnummer, Double>) data;
            Enumeration<ZWStartnummer> keys = names.keys();
            while (keys.hasMoreElements()) {
                ZWStartnummer key = keys.nextElement();
                T s = SearchUtils.getSchwimmer(wk, key.getStartnummer());
                int index = key.getIndex();
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

    private static <T extends ASchwimmer> boolean updateStarters(AWettkampf<T> wk, List<TeamWithStarters> data) {
        MannschaftWettkampf mwk = (MannschaftWettkampf) wk;
        for (TeamWithStarters starters : data) {
            int sn = starters.getStartnumber();
            Mannschaft m = SearchUtils.getSchwimmer(mwk, sn);
            int disz = findDisciplineIndex(m, starters.getDiscipline());
            if (disz < 0) {
                continue;
            }
            if (mwk.isHeatBased() && starters.getRound() > 0) {
                String id = OWDisziplin.getId(m.getAKNummer(), m.isMaennlich(), disz, starters.getRound());
                OWDisziplin<Mannschaft> d = mwk.getLauflisteOW().getDisziplin(id);
                if (d.contains(m)) {
                    Eingabe e = m.getEingabe(id, true);
                    if (e != null) {
                        e.setStarter(starters.getStarters());
                    }
                }
            } else {
                m.setStarter(disz, starters.getStarters());
            }
        }
        return true;
    }

    private static int findDisciplineIndex(Mannschaft m, String discipline) {
        return ImportUtils.getDisciplineIndex(m.getAK(), discipline);
    }

    private static <T extends ASchwimmer> boolean updateRegistration(AWettkampf<T> wk, LinkedList<T> schwimmer) {
        boolean ok = true;
        schwimmer.forEach(s -> {
            ASchwimmer as = SearchUtils.getSchwimmer(wk, s.getStartnummer());
            if (as != null) {
                if (as instanceof Teilnehmer target && s instanceof Teilnehmer source) {
                    target.setVorname(source.getVorname());
                    target.setNachname(source.getNachname());
                    target.setJahrgang(source.getJahrgang());
                }
                if (as instanceof Mannschaft target && s instanceof Mannschaft source) {
                    target.setName(source.getName());
                }
            }
        });
        return ok;
    }

    private static final char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    private static int at(char x) {
        for (int i = 0; i < alphabet.length; i++) {
            if (alphabet[i] == x) {
                return i;
            }
        }
        throw new IllegalStateException("Expected character between 'a' and 'z' but was '" + x + "'.");
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