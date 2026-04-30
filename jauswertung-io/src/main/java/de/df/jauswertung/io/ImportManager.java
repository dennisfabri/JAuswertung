/*
 * Created on 08.01.2005
 */
package de.df.jauswertung.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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
import de.df.jauswertung.io.portal.PortalImporter;
import de.df.jauswertung.io.value.TeamWithStarters;
import de.df.jauswertung.io.value.ZWStartnummer;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.valueobjects.Teammember;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.NullFeedback;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Dennis Fabri @date 08.01.2005
 */
@Slf4j
public class ImportManager {

    private static ImportManager manager = new ImportManager();

    private HashMap<String, IImporter> importers;

    public ImportManager() {
        importers = new HashMap<>();
        put(new CsvImporter());
        put(new ExcelImporter());
        put(new EasyWKImporter());
        put(new PortalImporter());
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
            case ZW_RESULTS:
                return wk.hasSchwimmer() && wk.hasHLW();
            case REGISTRATION:
                return true;
            case REFEREES:
                return wk.getKampfrichterverwaltung() != null;
            case TEAM_MEMBERS, STARTERS:
                return wk.hasSchwimmer() && wk instanceof MannschaftWettkampf;
            case HEAT_TIMES:
                return wk.hasSchwimmer() && wk.isHeatBased() && wk.hasLaufliste();
            case RESULTS:
            case HEAT_LIST, REGISTRATION_UPDATE:
                return wk.hasSchwimmer();
            default:
                return false;
        }
    }

    public static <T extends ASchwimmer> Object importData(IImporter importer, InputStream is,
                                                           ImportExportTypes datatype, AWettkampf<T> wk, Feedback fb, String filename)
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
        return switch (datatype) {
            case HEAT_LIST -> importer.heats(is, wk, fb);
            case REGISTRATION -> importer.registration(is, wk, fb, filename);
            case RESULTS -> importer.results(is, wk, fb);
            case REFEREES -> importer.referees(is, wk, fb);
            case TEAM_MEMBERS -> importer.teammembers(is, wk, fb);
            case ZW_RESULTS -> importer.zusatzwertungResults(is, wk, fb);
            case HEAT_TIMES -> importer.heattimes(is, wk, fb);
            case REGISTRATION_UPDATE -> importer.registrationUpdate(is, wk, fb, filename);
            case STARTERS -> importer.starters(is, wk, fb);
            default -> null;
        };
    }

    public static <T extends ASchwimmer> Object importData(ImportExportTypes datatype, String[] filenames,
                                                           String format, AWettkampf<T> wk, Feedback fb) throws
                                                                                                         IOException,
                                                                                                         NullPointerException,
                                                                                                         NotSupportedException,
                                                                                                         NotEnabledException,
                                                                                                         TableFormatException,
                                                                                                         TableEntryException,
                                                                                                         TableException {
        IImporter e = getImporter(format);
        if (e == null) {
            throw new NullPointerException();
        }
        Object result = null;
        for (String filename : filenames) {
            try (InputStream out = new FileInputStream(filename)) {
                result = importData(e, out, datatype, wk, fb, filename);
            }
        }
        return result;
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends ASchwimmer> Object finishImport(ImportExportTypes datatype, AWettkampf<T> wk, Object data,
                                                             Feedback fb) {
        if (!isEnabled(wk, datatype)) {
            return null;
        }
        switch (datatype) {
            case REGISTRATION: {
                LinkedList<T> schwimmer = (LinkedList<T>) data;
                boolean ok = true;
                for (T t : schwimmer) {
                    boolean b = wk.addSchwimmer(t);
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
            case TEAM_MEMBERS: {
                Hashtable<String, Teammember> names = (Hashtable<String, Teammember>) data;
                names.entrySet().stream().collect(Collectors.groupingBy(e -> e.getKey().toLowerCase(Locale.ROOT).substring(0, e.getKey().length() - 1)))
                        .forEach((id, entries) -> {
                            int key = Integer.parseInt(id);
                            T s = SearchUtils.getSchwimmer(wk, key);
                            if (s instanceof Mannschaft m) {
                                m.clearMannschaftsmitglieder();
                                entries.forEach(entry -> {
                                    char part = entry.getKey().toLowerCase(Locale.ROOT).charAt(entry.getKey().length() - 1);
                                    int position = at(part);
                                    if (position >= m.getMaxMembers()) {
                                        log.info(
                                                "Es sind nur {} Mannschaftsmitglieder erlaubt. Es soll aber ein Mitglied an Position {} eingefügt werden ({} - Startnummer {}).",
                                                m.getMaxMembers(),
                                                position + 1,
                                                m.getName(),
                                                m.getStartnummer());
                                    } else {
                                        Teammember info = entry.getValue();
                                        Mannschaftsmitglied mm = m.getMannschaftsmitglied(position);
                                        mm.setNachname(info.getLastname());
                                        mm.setVorname(info.getFirstname());
                                        mm.setJahrgang(info.getJahrgang());
                                        mm.setGeschlecht(info.getGeschlecht());
                                    }
                                });
                            }
                        });
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
            case HEAT_LIST:
            case HEAT_TIMES:
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
                if (d != null && d.contains(m)) {
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
