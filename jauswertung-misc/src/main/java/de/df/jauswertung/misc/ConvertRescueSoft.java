package de.df.jauswertung.misc;

import java.io.*;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.df.jauswertung.io.*;
import de.df.jutils.io.FileUtils;
import de.df.jutils.util.StringTools;

public class ConvertRescueSoft {

    public static final int maxTeammembers = 6;

    public static final String[] PoolSingleEvents = new String[] { "200m Obstacle Swim", "100m Manikin Carry with Fins",
            "100m Manikin Tow with Fins",
            "100m Rescue Medley", "50m Manikin Carry", "200m Super Lifesaver" };

    public static final String[] PoolTeamEvents = new String[] { "4 x 25m Manikin Relay", "4 x 50m Obstacle Relay",
            "4 x 50m Medley Relay",
            "Line Throw" };

    public static final String[] OpenwaterSingleEvents = new String[] { "Surf Race", "Board Race", "Surf Ski Race",
            "Oceanman", "Beach Flags", "Beach Sprint" };

    public static final String[] OpenwaterTeamEvents = new String[] { "Rescue Tube Rescue", "Board Rescue",
            "Oceanmen Relay", "Beach Sprint Relay" };

    public static class ExcelData {

        public ExcelData(String[] titles, Object[][][] tables) {
            this.titles = titles;
            this.tables = tables;
        }

        public final String[] titles;
        public final Object[][][] tables;
    }

    public static class Swimmer {
        public String Firstname;
        public String Lastname;
        public String Organization;
        public int YearOfBirth;
        public String Startnumber;
        public String Sex;
        public String Agegroup;

        public String[] RegistrationTimesPoolSingle = new String[PoolSingleEvents.length];
        public String[] RegistrationTimesPoolTeam = new String[PoolTeamEvents.length];
        public int[] PositionPoolTeam = new int[PoolTeamEvents.length];

        public String[] RegistrationTimesOpenwaterSingle = new String[OpenwaterSingleEvents.length];
        public int[] PositionOpenwaterTeam = new int[OpenwaterTeamEvents.length];

        public int TeamId;
    }

    public static class Team {
        public String Name;
        public List<Swimmer> swimmers = new ArrayList<>();
    }

    private static ExcelData readFile(String filename) throws IOException {
        byte[] data;

        FileInputStream is = new FileInputStream(filename);
        try {
            data = FileUtils.readFile(is);
        } finally {
            is.close();
        }

        try {
            HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = ExcelReader.sheetsToTable(wb);
            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }
            return new ExcelData(titles, tables);
        } catch (OfficeXmlFileException e) {
            XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(data));
            Object[][][] tables = Excel2007Utils.sheetsToTable(wb);

            String[] titles = new String[tables.length];
            for (int x = 0; x < tables.length; x++) {
                titles[x] = wb.getSheetName(x);
            }
            return new ExcelData(titles, tables);
        }
    }

    private static int getIndex(Object[] headers, String text) {
        for (int x = 0; x < headers.length; x++) {
            if (text.equals(headers[x])) {
                return x;
            }
        }
        return -1;
    }

    private static String parseTime(Object value) {
        if (value == null || "".equals(value)) {
            return "";
        }
        try {
            double d = Double.parseDouble(value.toString()) * 24 * 60 * 60 * 100;
            int time = (int) Math.round(d);
            return StringTools.zeitString(time);
        } catch (Exception e) {
            return "";
        }
    }

    public static List<Swimmer> parseSheet(String title, Object[][] sheet) {
        List<Swimmer> members = new ArrayList<>();
        if (sheet.length == 0) {
            return members;
        }

        boolean first = true;

        Object[] headers = sheet[0];
        for (Object[] row : sheet) {
            if (first) {
                first = false;
                continue;
            }
            Swimmer s = new Swimmer();
            s.Agegroup = "AK Offen";
            s.Firstname = row[getIndex(headers, "Vorname")].toString();
            s.Lastname = row[getIndex(headers, "Nachname")].toString();
            s.TeamId = (int) Double.parseDouble(row[getIndex(headers, "TeamID")].toString());
            s.Organization = row[getIndex(headers, "Gliederung")].toString();
            s.Sex = row[getIndex(headers, "Geschlecht")].toString();
            s.Startnumber = row[getIndex(headers, "s#")].toString();
            s.YearOfBirth = (int) Double.parseDouble(row[getIndex(headers, "Jahrgang")].toString());
            for (int x = 0; x < PoolSingleEvents.length; x++) {
                s.RegistrationTimesPoolSingle[x] = parseTime(row[getIndex(headers, PoolSingleEvents[x])]);
            }
            for (int x = 0; x < PoolTeamEvents.length; x++) {
                s.RegistrationTimesPoolTeam[x] = parseTime(row[getIndex(headers, PoolTeamEvents[x])]);
                try {
                    s.PositionPoolTeam[x] = (int) Double
                            .parseDouble(row[getIndex(headers, "#" + PoolTeamEvents[x])].toString());
                } catch (NumberFormatException nfe) {
                    s.PositionPoolTeam[x] = -1;
                }
            }
            for (int x = 0; x < OpenwaterSingleEvents.length; x++) {
                s.RegistrationTimesOpenwaterSingle[x] = row[getIndex(headers, OpenwaterSingleEvents[x])].toString();
            }
            for (int x = 0; x < OpenwaterTeamEvents.length; x++) {
                try {
                    s.PositionOpenwaterTeam[x] = (int) Double
                            .parseDouble(row[getIndex(headers, OpenwaterTeamEvents[x])].toString());
                } catch (NumberFormatException nfe) {
                    s.PositionOpenwaterTeam[x] = -1;
                }
            }

            members.add(s);
        }
        return members;
    }

    public static void WriteFile(String filename, String[] data) {
        TextFileUtils.StringArrayToFile("Test", filename, data);
    }

    private static void Add(StringBuilder sb, String text) {
        if (sb.length() > 0) {
            sb.append(";");
        }
        sb.append("\"");
        sb.append(text);
        sb.append("\"");
    }

    private static void Add(StringBuilder sb, int[] values) {
        StringBuilder sx = new StringBuilder();
        sx.append(values[0]);
        for (int x = 1; x < values.length; x++) {
            sx.append(",");
            sx.append(values[x]);
        }
        Add(sb, sx.toString());
    }

    private static void Add(StringBuilder sb, int value) {
        if (sb.length() > 0) {
            sb.append(";");
        }
        sb.append(value);
    }

    public static void WritePoolSingle(List<Swimmer> swimmers) {
        List<String> rows = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        Add(sb, "Altersklasse");
        Add(sb, "Gliederung");
        Add(sb, "Geschlecht");
        Add(sb, "Vorname");
        Add(sb, "Nachname");
        Add(sb, "TeamId");
        Add(sb, "S#");
        Add(sb, "Jahrgang");
        for (String event : PoolSingleEvents) {
            Add(sb, event);
        }
        rows.add(sb.toString());

        for (Swimmer s : swimmers) {
            sb.setLength(0);
            Add(sb, s.Agegroup);
            Add(sb, s.Organization);
            Add(sb, s.Sex);
            Add(sb, s.Firstname);
            Add(sb, s.Lastname);
            Add(sb, s.TeamId);
            Add(sb, s.Startnumber);
            Add(sb, s.YearOfBirth);
            for (String time : s.RegistrationTimesPoolSingle) {
                Add(sb, time);
            }
            rows.add(sb.toString());
        }
        WriteFile("PoolSingle.csv", rows.toArray(new String[rows.size()]));
    }

    public static void WritePoolTeam(List<Team> teams) {
        List<String> rows = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        Add(sb, "Altersklasse");
        Add(sb, "Gliederung");
        Add(sb, "Geschlecht");
        Add(sb, "Name");
        Add(sb, "S#");
        for (String event : PoolTeamEvents) {
            Add(sb, event);
        }
        for (String event : PoolTeamEvents) {
            Add(sb, event + " - Reihenfolge");
        }
        for (int x = 0; x < maxTeammembers; x++) {
            int pos = x + 1;
            Add(sb, "Vorname " + pos);
            Add(sb, "Nachname " + pos);
            Add(sb, "Jahrgang " + pos);
            Add(sb, "Geschlecht " + pos);
        }
        rows.add(sb.toString());

        for (Team t : teams) {
            sb.setLength(0);

            Swimmer s = t.swimmers.get(0);

            Add(sb, s.Agegroup);
            Add(sb, s.Organization);
            Add(sb, s.Sex);
            Add(sb, s.Organization);
            Add(sb, "" + s.TeamId + "-" + (s.Sex.equals("m") ? "2" : "1"));
            for (int i = 0; i < s.RegistrationTimesPoolTeam.length; i++) {
                String value = "";
                for (Swimmer si : t.swimmers) {
                    if (si.RegistrationTimesPoolTeam[i].length() > 0) {
                        value = si.RegistrationTimesPoolTeam[i];
                        break;
                    }
                }
                if (value.length() > 0) {
                    Add(sb, value);
                } else {
                    Add(sb, "");
                }
            }
            for (int i = 0; i < s.PositionPoolTeam.length; i++) {
                int[] starter = new int[4];
                for (int x = 0; x < starter.length; x++) {
                    starter[x] = 0;
                }
                int pos = 1;
                for (Swimmer si : t.swimmers) {
                    if (si.PositionPoolTeam[i] > 0) {
                        starter[si.PositionPoolTeam[i] - 1] = pos;
                    }
                    pos++;
                }
                Add(sb, starter);
            }
            for (Swimmer sx : t.swimmers) {
                Add(sb, sx.Firstname);
                Add(sb, sx.Lastname);
                Add(sb, sx.YearOfBirth);
                Add(sb, sx.Sex);
            }
            for (int x = t.swimmers.size(); x < maxTeammembers; x++) {
                Add(sb, "");
                Add(sb, "");
                Add(sb, "");
                Add(sb, "");

            }
            rows.add(sb.toString());
        }
        WriteFile("PoolTeam.csv", rows.toArray(new String[rows.size()]));
    }

    public static void WriteOpenwaterSingle(List<Swimmer> swimmers) {
        List<String> rows = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        Add(sb, "Altersklasse");
        Add(sb, "Gliederung");
        Add(sb, "Geschlecht");
        Add(sb, "Vorname");
        Add(sb, "Nachname");
        Add(sb, "TeamId");
        Add(sb, "S#");
        Add(sb, "Jahrgang");
        for (String event : OpenwaterSingleEvents) {
            Add(sb, event);
        }
        rows.add(sb.toString());

        for (Swimmer s : swimmers) {
            sb.setLength(0);
            Add(sb, s.Agegroup);
            Add(sb, s.Organization);
            Add(sb, s.Sex);
            Add(sb, s.Firstname);
            Add(sb, s.Lastname);
            Add(sb, s.TeamId);
            Add(sb, s.Startnumber);
            Add(sb, s.YearOfBirth);
            for (String time : s.RegistrationTimesOpenwaterSingle) {
                Add(sb, time);
            }
            rows.add(sb.toString());
        }
        WriteFile("OpenwaterSingle.csv", rows.toArray(new String[rows.size()]));
    }

    public static void WriteOpenwaterTeam(List<Team> teams) {
        List<String> rows = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        Add(sb, "Altersklasse");
        Add(sb, "Gliederung");
        Add(sb, "Geschlecht");
        Add(sb, "Name");
        Add(sb, "S#");
        for (String event : OpenwaterTeamEvents) {
            Add(sb, event);
        }
        for (String event : OpenwaterTeamEvents) {
            Add(sb, event + " - Reihenfolge");
        }
        for (int x = 0; x < maxTeammembers; x++) {
            int pos = x + 1;
            Add(sb, "Vorname " + pos);
            Add(sb, "Nachname " + pos);
            Add(sb, "Jahrgang " + pos);
            Add(sb, "Geschlecht " + pos);
        }
        rows.add(sb.toString());

        for (Team t : teams) {
            sb.setLength(0);

            Swimmer s = t.swimmers.get(0);

            Add(sb, s.Agegroup);
            Add(sb, s.Organization);
            Add(sb, s.Sex);
            Add(sb, s.Organization);
            Add(sb, "" + s.TeamId + "-" + (s.Sex.equals("m") ? "2" : "1"));
            for (int i = 0; i < s.PositionOpenwaterTeam.length; i++) {
                int max = -1;
                for (Swimmer si : t.swimmers) {
                    max = Math.max(max, si.PositionOpenwaterTeam[i]);
                }
                if (max >= 0) {
                    Add(sb, max);
                } else {
                    Add(sb, "");
                }
            }
            for (int i = 0; i < s.PositionOpenwaterTeam.length; i++) {
                int[] starter = new int[4];
                for (int x = 0; x < starter.length; x++) {
                    starter[x] = 0;
                }
                int pos = 1;
                for (Swimmer si : t.swimmers) {
                    if (si.PositionOpenwaterTeam[i] > 0) {
                        starter[si.PositionOpenwaterTeam[i] - 1] = pos;
                    }
                    pos++;
                }
                Add(sb, starter);
            }
            for (Swimmer sx : t.swimmers) {
                Add(sb, sx.Firstname);
                Add(sb, sx.Lastname);
                Add(sb, sx.YearOfBirth);
                Add(sb, sx.Sex);
            }
            for (int x = t.swimmers.size(); x < maxTeammembers; x++) {
                Add(sb, "");
                Add(sb, "");
                Add(sb, "");
                Add(sb, "");

            }
            rows.add(sb.toString());
        }
        WriteFile("OpenwaterTeam.csv", rows.toArray(new String[rows.size()]));
    }

    private static List<Team> ToTeams(List<Swimmer> swimmers) {
        Hashtable<String, Team> teams = new Hashtable<>();

        for (Swimmer s : swimmers) {
            Team t = teams.get(s.Organization + s.Sex);
            if (t == null) {
                t = new Team();
                t.Name = s.Organization;
                teams.put(t.Name + s.Sex, t);
            }
            t.swimmers.add(s);
        }

        return new ArrayList<>(teams.values());
    }

    public static void main(String[] args) throws IOException {
        List<Swimmer> swimmers = new ArrayList<>();
        ExcelData excel = readFile("Test/Meldedaten.xlsx");
        for (int x = 0; x < excel.titles.length; x++) {
            swimmers.addAll(parseSheet(excel.titles[x], excel.tables[x]));
        }
        WritePoolSingle(swimmers);
        WriteOpenwaterSingle(swimmers);

        List<Team> teams = ToTeams(swimmers);
        WritePoolTeam(teams);
        WriteOpenwaterTeam(teams);
    }

}
