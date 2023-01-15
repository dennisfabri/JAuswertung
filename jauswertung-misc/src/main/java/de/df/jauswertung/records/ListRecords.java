package de.df.jauswertung.records;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.SearchUtils;

public class ListRecords {

    public static String[] FILES = { "dm1999", "dm2000", "dm2001", "dm2002", "dm2003", "dm2004", "dm2005", "dm2006",
            "dm2007", "dm2008", "dm2009", "dm2010",
            "dm2011", "dsm2011" };
    public static String[] LOCATIONS = { "Bad Nauheim", "Halle/Saale", "Itzehoe", "Uelzen", "Braunschweig", "Paderborn",
            "Wetzlar", "Wuppertal", "Duisburg",
            "Paderborn", "Itzehoe", "Heidenheim", "Bremen", "Geislingen" };
    public static Date[][] DATES = { { getDate(1999, 10, 9), getDate(1999, 10, 8) },
            { getDate(2000, 10, 28), getDate(2000, 10, 28) },
            { getDate(2001, 10, 27), getDate(2001, 10, 26) }, { getDate(2002, 10, 4), getDate(2002, 10, 5) },
            { getDate(2003, 10, 3), getDate(2003, 10, 4) },
            { getDate(2004, 10, 23), getDate(2004, 10, 22) }, { getDate(2005, 10, 22), getDate(2005, 10, 22) },
            { getDate(2006, 10, 7), getDate(2006, 10, 6) },
            { getDate(2007, 9, 23), getDate(2007, 9, 22) }, { getDate(2008, 10, 4), getDate(2008, 10, 3) },
            { getDate(2009, 10, 4), getDate(2009, 10, 3) },
            { getDate(2010, 10, 4), getDate(2010, 10, 3) }, { getDate(2011, 10, 22), getDate(2010, 10, 21) },
            { getDate(2011, 3, 11), getDate(2011, 3, 12) } };

    @SuppressWarnings("deprecation")
    private static Date getDate(int y, int m, int d) {
        return new Date(y - 1900, m - 1, d);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws FileNotFoundException {
        RecordsDB records = new RecordsDB();
        for (int x = 0; x < FILES.length; x++) {
            for (int y = 0; y < 2; y++) {
                listRecords(records, "src/test/resources/competitions/results/" + FILES[x] + "-"
                        + (y == 1 ? "einzel" : "mannschaft") + ".wk", DATES[x][y], LOCATIONS[x]);
            }
        }
        System.out.println("");
    }

    @SuppressWarnings("unchecked")
    private static void listRecords(RecordsDB records, String name, Date date, String location) {
        @SuppressWarnings("rawtypes")
        AWettkampf wk = InputManager.ladeWettkampf(name);
        if (wk == null) {
            System.out.println("Datei \"" + name + "\" wurde nicht gefunden.");
            return;
        }

        {
            // Remove swimmers with general penalties
            LinkedList<ASchwimmer> swimmers = wk.getSchwimmer();
            for (ASchwimmer s : swimmers) {
                Strafe strafe = s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF);
                boolean ok = !isStrafe(strafe);
                if (!ok) {
                    wk.removeSchwimmer(s);
                }
            }
        }

        if (date.before(getDate(2007, 1, 1))) {
            for (int x = 0; x < wk.getRegelwerk().size(); x++) {
                Altersklasse ak = wk.getRegelwerk().getAk(x);
                for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                    for (int z = 0; z < 2; z++) {
                        Disziplin d = ak.getDisziplin(y, z == 1);
                        if (d.getName().equalsIgnoreCase("4*50m kleine Rettungsstaffel")) {
                            d.setName("4*50m Rettungsstaffel");
                        } else if (d.getName().equalsIgnoreCase("4*50m Rettungsstaffel")) {
                            d.setName("4*50m Rettungsstaffel (alt)");
                        } else if (d.getName().equalsIgnoreCase("100m Kombinierte Rettungs�bung")) {
                            d.setName("100m Kombinierte Rettungs�bung (alt)");
                        } else if (d.getName().equalsIgnoreCase("100m Kombiniertes Retten")) {
                            d.setName("100m Kombinierte Rettungs�bung (alt)");
                        }
                    }
                }
            }
        }

        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            Altersklasse ak = wk.getRegelwerk().getAk(x);
            int ak_id = records.getAgegroupId(ak.getName());
            if (ak_id >= 0) {
                for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                    int disc_id = records.getDisciplineId(ak.getDisziplin(y, true).getName());
                    if (disc_id <= 0) {
                        System.out.println("Discipline not found: " + ak.getDisziplin(y, true).getName());
                    } else {
                        for (int i = 0; i < 2; i++) {
                            LinkedList<ASchwimmer> swimmers = SearchUtils.getSchwimmer(wk, ak, i == 1);
                            if (!swimmers.isEmpty()) {
                                ASchwimmer best = null;
                                long time = 0;
                                for (ASchwimmer s : swimmers) {
                                    if (!isStrafe(s.getAkkumulierteStrafe(y)) && s.isDisciplineChosen(y)) {
                                        if (best == null && s.getZeit(y) > 0) {
                                            best = s;
                                            time = s.getZeit(y);
                                        } else {
                                            if (time > s.getZeit(y)) {
                                                best = s;
                                                time = s.getZeit(y);
                                            }
                                        }
                                    }
                                }
                                if (best != null) {
                                    String first;
                                    String second;
                                    if (best instanceof Teilnehmer) {
                                        Teilnehmer t = (Teilnehmer) best;
                                        first = t.getVorname();
                                        second = t.getNachname();
                                    } else {
                                        first = null;
                                        second = best.getName();
                                    }
                                    int id = records.getNextRecordId();
                                    Record r = new Record(id, date, time,
                                            records.getDisciplineId(ak.getDisziplin(y, i == 1).getName()),
                                            records.getAgegroupId(ak.getName()), i == 1,
                                            records.getCompetitorId(first, second, i == 1));
                                    if (records.isRecord(r)) {
                                        // System.out.print("+");
                                        printRecord(id, best, y, date, location, records);
                                        records.addRecord(r);
                                    } else {
                                        // System.out.print("-");
                                        // printRecord(best, l, date,
                                        // location, records);
                                    }
                                } else {
                                    System.out.println("No result found for " + ak.getName() + " "
                                            + (i == 1 ? "male" : "female") + " in "
                                            + ak.getDisziplin(y, i == 1).getName());
                                }
                            }
                        }
                    }
                }
            } else {
                System.out.println("Could not find agegroup: " + ak.getName());
            }
        }
    }

    private static void printRecord(int recordId, ASchwimmer s, int discipline, Date date, String location,
            RecordsDB records) {
        int clubId;
        {
            int id = records.getClubId(s.getGliederung());
            if (id <= 0) {
                System.out.println("  insert into club values (\"" + 0 + "\", \"" + s.getGliederung()
                        + "\", \"1\"); -- Club for " + s.getName());
                Club c = new Club(10000, s.getGliederung());
                records.addClub(c);
            }
            clubId = id;
        }

        int compId;
        {
            String first = null;
            String second = null;
            if (s instanceof Teilnehmer) {
                Teilnehmer t = (Teilnehmer) s;
                first = t.getVorname();
                second = t.getNachname();
            } else {
                Mannschaft m = (Mannschaft) s;
                first = m.getMitgliedernamen(", ");
                second = m.getName();
            }
            compId = records.getCompetitorId(first, second, s.isMaennlich());
            if (compId <= 0) {
                char gender = s.isMaennlich() ? 'm' : 'f';
                boolean team = s instanceof Mannschaft;
                // INSERT INTO `competitor`(`ID_Comp`, `team`, `Familyname`,
                // `Surname`, `Birth`, `Gender`, `nation_id`, `federation_id`,
                // `club_id`) VALUES
                // ([value-1],[value-2],[value-3],[value-4],[value-5],[value-6],[value-7],[value-8],[value-9])

                compId = records.getNextCompetitorId();
                System.out.println(
                        "  INSERT INTO `competitor`(`ID_Comp`, `team`, `Familyname`, `Surname`, `Birth`, `Gender`, `nation_id`, `federation_id`, `club_id`) "
                                + "\n                   VALUES (" + compId + ", " + (team ? "'1'" : "'0'") + ",'"
                                + second + "','" + first + "',NULL,'" + gender
                                + "',12,1," + clubId + ");");
                Competitor c = new Competitor(compId, first, second, 1, s instanceof Mannschaft, s.isMaennlich());
                records.addCompetitor(c);
            }
        }
        // INSERT INTO `record`(`ID_Rec`, `recordtime`, `event_id`,
        // `recorddate`, `venue_id`, `ID_Comp`, `age_group`, `gender`,
        // `worldRecord`, `euroRecord`, `asiaRecord`, `africaRecord`,
        // `americanRecord`)
        // VALUES
        // ([value-1],[value-2],[value-3],[value-4],[value-5],[value-6],
        // [value-7],[value-8],[value-9],[value-10],[value-11],[value-12],[value-13])

        int eventId = records.getDisciplineId(s.getAK().getDisziplin(discipline, s.isMaennlich()).getName());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String rdate = df.format(date) + " 00:00:00";
        int venueId = records.getLocationId(location);
        if (venueId <= 0) {
            System.out.println(location);
        }
        long zeit = s.getZeit(discipline);
        if (zeit > 5999) {
            long mt = zeit / 6000;
            long st = zeit % 6000;
            zeit = st + mt * 10000;
        }
        int agegroupId = records.getAgegroupId(s.getAK().getName());

        String query = "INSERT INTO `record`(`ID_Rec`, `recordtime`, `event_id`, `recorddate`, `venue_id`, `ID_Comp`, `age_group`, `gender`, `worldRecord`, `euroRecord`, `asiaRecord`, `africaRecord`, `americanRecord`)";
        query += " VALUES\n      ";
        query += "(" + recordId + "," + zeit + "," + eventId + ",'" + rdate + "'," + venueId + "," + compId + ","
                + agegroupId + ",'"
                + (s.isMaennlich() ? 'm' : 'f') + "','1','0','0','0','0');";
        if (true)
            System.out.println(query);
        // System.out.println("Record found: " + s.getName() + " - " +
        // s.getAK().getDisziplin(discipline, s.isMaennlich())
        // + " on " + DF.format(date) + " in " + location + " with " +
        // s.getZeit(discipline));
    }

    private static boolean isStrafe(Strafe strafe) {
        boolean ok = true;
        switch (strafe.getArt()) {
        case AUSSCHLUSS:
            ok = false;
            break;
        case DISQUALIFIKATION:
            ok = false;
            break;
        case NICHT_ANGETRETEN:
            ok = false;
            break;
        case NICHTS:
            break;
        case STRAFPUNKTE:
            if (strafe.getStrafpunkte() > 0) {
                ok = false;
            }
            break;
        }
        return !ok;
    }

}
