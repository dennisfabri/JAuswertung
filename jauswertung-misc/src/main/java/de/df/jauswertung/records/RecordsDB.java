package de.df.jauswertung.records;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;

import de.df.jauswertung.io.CsvUtils;
import de.df.jutils.util.DateUtils;

public class RecordsDB {

    private static final String BASEDIR = "src/test/resources/competitions/Rekorde/";

    private LinkedList<Record> records = new LinkedList<>();
    private LinkedList<Agegroup> agegroups = new LinkedList<>();
    private LinkedList<Discipline> disciplines = new LinkedList<>();
    private LinkedList<Competitor> competitors = new LinkedList<>();
    private LinkedList<Location> locations = new LinkedList<>();
    private LinkedList<Club> clubs = new LinkedList<>();

    public RecordsDB() throws FileNotFoundException {
        Initialize();
    }

    private void Initialize() throws FileNotFoundException {
        initAgegroups();
        initDisciplines();
        initCompetitors();
        initClubs();
        initLocations();

        initRecords();

        printRecords();
    }

    private void printRecords() {
        if (records.isEmpty()) {
            return;
        }
        Collections.sort(records);
        Record current = records.getFirst();
        for (Record r : records) {
            if ((r.Male != current.Male) || (r.Agegroup != current.Agegroup) || (r.Discipline != current.Discipline)) {
                printRecord(current);
                current = r;
            }
        }
        printRecord(current);
    }

    private void printRecord(Record r) {
        Discipline d = getDiscipline(r.Discipline);
        Agegroup a = getAgegroup(r.Agegroup);
        int year = DateUtils.getYear(r.Date);
        String competitor = getCompetitor(r.Competitor);
        System.out.println((r.Agegroup < 6 ? "DM" : "DSM") + year + ";" + a.Name + ";" + (r.Male ? "m" : "w") + ";"
                + d.Name + ";" + formatTime(r.Time) + ";"
                + competitor + ";" + (d.Team ? "Mannschaft" : "Einzel"));
    }

    private String getCompetitor(int id) {
        for (Competitor d : competitors) {
            if (d.Id == id) {
                if (d.First != null && !d.First.contains(";") && !d.First.trim().isEmpty()) {
                    return d.Second + ", " + d.First;
                }
                return d.Second;
            }
        }
        return "<unbekannt>";
    }

    private String formatTime(long time) {
        long m = time / 6000;
        long s = (time % 6000) / 100;
        long ms = time % 100;
        return "" + m + ":" + (s < 10 ? "0" : "") + s + "," + (ms < 10 ? "0" : "") + ms;
    }

    private Agegroup getAgegroup(int id) {
        for (Agegroup d : agegroups) {
            if (d.Id == id) {
                return d;
            }
        }
        return null;
    }

    private Discipline getDiscipline(int id) {
        for (Discipline d : disciplines) {
            if (d.Id == id) {
                return d;
            }
        }
        return null;
    }

    private void initRecords() throws FileNotFoundException {
        Object[][] data = CsvUtils.read(new FileInputStream(BASEDIR + "record.csv"));
        // id, time, event_id, date, venue_id, ID_Comp, age_group, gender,
        // worldRecord, euroRecord, asiaRecord, africaRecord, americanRecord
        // 1,"10620","1","2004-01-01 00:00:00","1","1","1","f","1","0","0","0","0"
        for (Object[] line : data) {
            if (line[8].equals("1")) {
                int id = Integer.parseInt(line[0].toString());
                long time = Long.parseLong(line[1].toString());
                if (time > 9999) {
                    long minutes = time / 10000;
                    long seconds = time % 10000;
                    time = seconds + minutes * 6000;
                }
                Date date = getDate(line[3].toString());
                int discipline = Integer.parseInt(line[2].toString());
                int agegroup = Integer.parseInt(line[6].toString());
                boolean male = !line[7].equals("f");
                int competitor = Integer.parseInt(line[5].toString());
                Record r = new Record(id, date, time, discipline, agegroup, male, competitor);
                if (isRecord(r)) {
                    records.addLast(r);
                }
            }
        }
    }

    private void initAgegroups() throws FileNotFoundException {
        Object[][] data = CsvUtils.read(new FileInputStream(BASEDIR + "age.csv"));
        // 1,"AK 12"
        for (Object[] line : data) {
            int id = Integer.parseInt(line[0].toString());
            String agegroup = line[1].toString();
            Agegroup a = new Agegroup(id, agegroup);
            agegroups.addLast(a);
        }
    }

    private void initDisciplines() throws FileNotFoundException {
        Object[][] data = CsvUtils.read(new FileInputStream(BASEDIR + "event.csv"));
        // 1,"4*25m Hindernis", 1
        for (Object[] line : data) {
            int id = Integer.parseInt(line[0].toString());
            String event = line[1].toString();
            boolean team = line[2].equals("1");
            Discipline a = new Discipline(id, event, team);
            disciplines.addLast(a);
        }
    }

    private void initCompetitors() throws FileNotFoundException {
        Object[][] data = CsvUtils.read(new FileInputStream(BASEDIR + "competitor.csv"));
        // "1","1","Weimar",,"2011-06-01","f","12","1","1"
        for (Object[] line : data) {
            int id = Integer.parseInt(line[0].toString());
            String first = line[3].toString();
            String second = line[2].toString();
            boolean male = !line[5].equals("f");
            int clubId = Integer.parseInt(line[8].toString());
            boolean team = !line[1].equals("1");

            Competitor a = new Competitor(id, first, second, clubId, team, male);
            competitors.addLast(a);
        }
    }

    private void initLocations() throws FileNotFoundException {
        Object[][] data = CsvUtils.read(new FileInputStream(BASEDIR + "venue.csv"));
        // "101","Paderborn","12"
        for (Object[] line : data) {
            int id = Integer.parseInt(line[0].toString());
            String location = line[1].toString();

            Location a = new Location(id, location);
            locations.addLast(a);
        }
    }

    private void initClubs() throws FileNotFoundException {
        Object[][] data = CsvUtils.read(new FileInputStream(BASEDIR + "club.csv"));
        // "1","Weimar","1"
        for (Object[] line : data) {
            int id = Integer.parseInt(line[0].toString());
            String club = line[1].toString();

            Club a = new Club(id, club);
            clubs.addLast(a);
        }
    }

    private Date getDate(String text) {
        String[] parts = text.split(" ");
        String[] datepart = parts[0].split("-");
        String year = datepart[0];
        String month = datepart[1];
        String day = datepart[2];

        return DateUtils.get(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        // return new Date(Integer.parseInt(year) - 1900, Integer.parseInt(month) - 1,
        // Integer.parseInt(day));
    }

    public boolean isRecord(Record record) {
        for (Record r : records) {
            if ((r.Discipline == record.Discipline) && r.Male == record.Male && r.Agegroup == record.Agegroup
                    && ((r.Date.before(record.Date)) || (r.Date.equals(record.Date))) && (r.Time <= record.Time)) {
                return false;
            }
        }
        return true;
    }

    public void addRecord(Record record) {
        records.add(record);
    }

    private static String[][] AGEGROUP_MAPPING = new String[][] { { "ak 80+", "ak 80" }, { "AK 240+", "AK 240" } };

    public int getAgegroupId(String name) {
        for (Agegroup a : agegroups) {
            if (name.equalsIgnoreCase(a.Name)) {
                return a.Id;
            }
            for (String[] map : AGEGROUP_MAPPING) {
                if (name.equalsIgnoreCase(map[0]) && a.Name.equalsIgnoreCase(map[1])) {
                    return a.Id;
                }
            }
        }
        return -1;
    }

    public int getCompetitorId(String first, String second, boolean male) {
        for (Competitor a : competitors) {
            if (second.equalsIgnoreCase(a.Second) && male == a.Male) {
                if ((first == null) || first.equalsIgnoreCase(a.First)) {
                    return a.Id;
                }
            }
        }
        return -1;
    }

    public void addCompetitor(Competitor c) {
        competitors.addLast(c);
    }

    public void addClub(Club c) {
        clubs.addLast(c);
    }

    private static String[][] DISCPLINE_MAPPING = new String[][] {
            { "100m Kombiniertes Retten", "100m Kombinierte Rettungs�bung" },
            { "4*25m RLB", "4*25m R�ckenlage ohne Armt�tigkeit" },
            { "4*50m kleine Rettungsstaffel", "4*50m Rettungsstaffel" },
            { "200m Super Lifesaver", "200m Super-Lifesaver" },
            { "100m Retten mit Flossen und Gurtretter", "100m Lifesaver" },
            { "100m Retten einer Puppe mit Flossen und Gurtretter", "100m Lifesaver" },
            { "50m Retten einer Puppe", "50m Retten" },
            { "50m Retten einer Puppe mit Flossen", "50m Retten mit Flossen" },
            { "100m Retten einer Puppe mit Flossen", "100m Retten mit Flossen" } };

    public int getDisciplineId(String name) {
        for (Discipline a : disciplines) {
            if (name.equalsIgnoreCase(a.Name)) {
                return a.Id;
            }
            for (String[] map : DISCPLINE_MAPPING) {
                if (name.equalsIgnoreCase(map[0]) && a.Name.equalsIgnoreCase(map[1])) {
                    return a.Id;
                }
            }
        }
        return -1;
    }

    private static String[][] CLUB_MAPPING = new String[][] { { "SCW", "Schwerte" }, { "LUK", "Luckenwalde" },
            { "GT", "G�tersloh" }, { "LI", "Lichtenberg" },
            { "ENN", "Ennigerloh" }, { "RWD", "Rheda-Wiedenbr�ck" }, { "MD", "Magdeburg" }, { "EMS", "Emsdetten" },
            { "ANK", "Anklam" }, { "ALP", "Alpen" },
            { "HSK", "Hochsauerlandkreis" }, { "ND", "LV Niedersachsen" }, { "BA", "LV Baden" },
            { "WE", "LV Westfalen" }, { "HE", "LV Hessen" },
            { "NR", "LV Nordrhein" }, { "TH", "LV Th�ringen" }, { "SA", "LV Sachen-Anhalt" },
            { "BB", "LV Brandenburg" }, { "BY", "LV Bayern" },
            { "SH", "LV Schleswig-Holstein" }, { "SR", "LV Saar" }, { "MV", "LV Mecklemburg-Vorpommern" },
            { "W�", "LV W�rttemberg" }, { "BE", "LV Berlin" },
            { "SN", "LV Sachsen" }, { "BS", "LV Braunschweig" }, { "RP", "LV Rheinland-Pfalz" }, { "HBS", "HBS" },
            { "AAF", "AAF" }, { "RUE", "RUE" },
            { "HST", "HST" }, { "LER", "LER" }, { "OE", "OE" },

    };

    public int getClubId(String name) {
        for (Club a : clubs) {
            if (name.equalsIgnoreCase(a.Name)) {
                return a.Id;
            }
            for (String[] map : CLUB_MAPPING) {
                if (name.equalsIgnoreCase(map[0]) && a.Name.equalsIgnoreCase(map[1])) {
                    return a.Id;
                }
            }
        }
        return -1;
    }

    public int getLocationId(String name) {
        for (Location a : locations) {
            if (name.equalsIgnoreCase(a.Name)) {
                return a.Id;
            }
        }
        return -1;
    }

    public int getNextCompetitorId() {
        int id = 0;
        for (Competitor c : competitors) {
            id = Math.max(c.Id, id);
        }
        return id + 1;
    }

    public int getNextRecordId() {
        int id = 0;
        for (Record c : records) {
            id = Math.max(c.Id, id);
        }
        return id + 1;
    }
}
