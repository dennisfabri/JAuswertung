package de.df.jauswertung.io;

import static de.df.jauswertung.io.ImportConstants.ALTERSKLASSE;
import static de.df.jauswertung.io.ImportConstants.AUSSER_KONKURRENZ;
import static de.df.jauswertung.io.ImportConstants.BEMERKUNG;
import static de.df.jauswertung.io.ImportConstants.CATEGORY;
import static de.df.jauswertung.io.ImportConstants.DISCIPLINES;
import static de.df.jauswertung.io.ImportConstants.DISCIPLINE;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT1;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT10;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT11;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT12;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT2;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT3;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT4;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT5;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT6;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT7;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT8;
import static de.df.jauswertung.io.ImportConstants.GESCHLECHT9;
import static de.df.jauswertung.io.ImportConstants.GLIEDERUNG;
import static de.df.jauswertung.io.ImportConstants.INDEX_COUNT;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG1;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG10;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG11;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG12;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG2;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG3;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG4;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG5;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG6;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG7;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG8;
import static de.df.jauswertung.io.ImportConstants.JAHRGANG9;
import static de.df.jauswertung.io.ImportConstants.MAX_INDEX;
import static de.df.jauswertung.io.ImportConstants.MELDEZEITEN;
import static de.df.jauswertung.io.ImportConstants.MEMBERS;
import static de.df.jauswertung.io.ImportConstants.NACHNAME;
import static de.df.jauswertung.io.ImportConstants.NACHNAME1;
import static de.df.jauswertung.io.ImportConstants.NACHNAME10;
import static de.df.jauswertung.io.ImportConstants.NACHNAME11;
import static de.df.jauswertung.io.ImportConstants.NACHNAME12;
import static de.df.jauswertung.io.ImportConstants.NACHNAME2;
import static de.df.jauswertung.io.ImportConstants.NACHNAME3;
import static de.df.jauswertung.io.ImportConstants.NACHNAME4;
import static de.df.jauswertung.io.ImportConstants.NACHNAME5;
import static de.df.jauswertung.io.ImportConstants.NACHNAME6;
import static de.df.jauswertung.io.ImportConstants.NACHNAME7;
import static de.df.jauswertung.io.ImportConstants.NACHNAME8;
import static de.df.jauswertung.io.ImportConstants.NACHNAME9;
import static de.df.jauswertung.io.ImportConstants.NAME;
import static de.df.jauswertung.io.ImportConstants.POSITION;
import static de.df.jauswertung.io.ImportConstants.PROTOCOL1;
import static de.df.jauswertung.io.ImportConstants.PROTOCOL2;
import static de.df.jauswertung.io.ImportConstants.PUNKTE1;
import static de.df.jauswertung.io.ImportConstants.PUNKTE2;
import static de.df.jauswertung.io.ImportConstants.QUALI;
import static de.df.jauswertung.io.ImportConstants.QUALI_LEVEL;
import static de.df.jauswertung.io.ImportConstants.STARTNUMMER;
import static de.df.jauswertung.io.ImportConstants.STARTPASS;
import static de.df.jauswertung.io.ImportConstants.STUFE;
import static de.df.jauswertung.io.ImportConstants.VORNAME;
import static de.df.jauswertung.io.ImportConstants.VORNAME1;
import static de.df.jauswertung.io.ImportConstants.VORNAME10;
import static de.df.jauswertung.io.ImportConstants.VORNAME11;
import static de.df.jauswertung.io.ImportConstants.VORNAME12;
import static de.df.jauswertung.io.ImportConstants.VORNAME2;
import static de.df.jauswertung.io.ImportConstants.VORNAME3;
import static de.df.jauswertung.io.ImportConstants.VORNAME4;
import static de.df.jauswertung.io.ImportConstants.VORNAME5;
import static de.df.jauswertung.io.ImportConstants.VORNAME6;
import static de.df.jauswertung.io.ImportConstants.VORNAME7;
import static de.df.jauswertung.io.ImportConstants.VORNAME8;
import static de.df.jauswertung.io.ImportConstants.VORNAME9;
import static de.df.jauswertung.io.ImportConstants.ZW;
import static de.df.jauswertung.io.ImportConstants.getRequiredIndizes;
import static de.df.jauswertung.io.ImportConstants.getRequiredIndizesForUpdate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Qualifikation;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.kampfrichter.Kampfrichter;
import de.df.jauswertung.daten.kampfrichter.KampfrichterEinheit;
import de.df.jauswertung.daten.kampfrichter.KampfrichterStufe;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Startunterlagen;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.util.ZWUtils;
import de.df.jauswertung.io.value.TeamWithStarters;
import de.df.jauswertung.io.value.ZWStartnummer;
import de.df.jauswertung.util.DataTableUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.i18n.EmptyResourceBundle;
import de.df.jutils.io.csv.CsvManager;
import de.df.jutils.resourcebundle.SafeResourceBundle;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.StringTools;

public class ImportUtils {

    private static Logger log = LoggerFactory.getLogger(ImportUtils.class);

    private static final ResourceBundle aknames = getAkNames();

    private static ResourceBundle getAkNames() {
        try {
            SafeResourceBundle srb = new SafeResourceBundle(ResourceBundle.getBundle("aks"));
            srb.setVerbose(false);
            return srb;
        } catch (RuntimeException re) {
            return new EmptyResourceBundle();
        }
    }

    private static final char[] alphabet1 = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    private static int position(char[] list, char element) {
        for (int x = 0; x < list.length; x++) {
            if (list[x] == element) {
                return x;
            }
        }
        return -1;
    }

    static <T extends ASchwimmer> Hashtable<String, String[]> tablesToTeammembers(AWettkampf<T> wk, Feedback fb,
            String[] sheets, Object[][][] tables, String file) throws TableEntryException, TableException {

        Hashtable<String, String[]> result = new Hashtable<String, String[]>();
        int[][] startsak = new int[wk.getRegelwerk().size()][2];

        int valid = 0;

        for (int y = 0; y < tables.length; y++) {
            int size = result.size();

            String sheet = sheets[y];

            fb.showFeedback(I18n.get("ImportingSheet", sheet));

            Object[][] data = tables[y];
            if ((data == null) || (data.length == 0)) {
                fb.showFeedback(I18n.get("Error.SheetEmpty"));
                continue;
            }
            int length = data[0].length;

            String[] titles = new String[length];
            for (int x = 0; x < length; x++) {
                titles[x] = data[0][x].toString();
            }
            int[] indizes = null;
            try {
                indizes = identifyIndizes(wk, titles, false, false, file, sheet);
                boolean first = true;
                StringBuilder text = new StringBuilder();
                if (indizes[STARTNUMMER] < 0) {
                    text.append("\t").append(I18n.get("Startnumber"));
                    first = false;
                }
                if (indizes[NACHNAME] < 0) {
                    text.append(first ? "\t" : ", ").append(I18n.get("Surname"));
                    first = false;
                }
                if (indizes[VORNAME] < 0) {
                    text.append(first ? "\t" : ", ").append(I18n.get("FirstName"));
                    first = false;
                }
                if (text.length() > 0) {
                    fb.showFeedback(I18n.get("Error.NotAllHeadersFound", text.toString()));
                    continue;
                }
            } catch (TableFormatException tfe) {
                fb.showFeedback(I18n.get("Error.NotAllHeadersFound", ImportUtils.indizesToNames(tfe.getData(), "\t")));
                continue;
            }
            for (int x = 1; x < data.length; x++) {
                String sntext = data[x][indizes[STARTNUMMER]].toString().trim().toLowerCase();
                if (sntext.length() < 2) {
                    throw new TableEntryException(
                            I18n.get("StartnumberFormatUnknown", sntext,
                                    StringTools.getCellName(sheet, x, indizes[STARTNUMMER])),
                            file, sheet, x, indizes[STARTNUMMER]);
                }
                char n = sntext.charAt(sntext.length() - 1);
                int pos = position(alphabet1, n);
                if (pos < 0) {
                    throw new TableEntryException(
                            I18n.get("StartnumberFormatUnknown", sntext,
                                    StringTools.getCellName(sheet, x, indizes[STARTNUMMER])),
                            file, sheet, x, indizes[STARTNUMMER]);
                }

                int sn;
                try {
                    sn = Integer.parseInt(sntext.substring(0, sntext.length() - 1));
                } catch (RuntimeException re) {
                    throw new TableEntryException(
                            I18n.get("StartnumberFormatUnknown", sntext,
                                    StringTools.getCellName(sheet, x, indizes[STARTNUMMER])),
                            file, sheet, x, indizes[STARTNUMMER]);
                }

                ASchwimmer s = SearchUtils.getSchwimmer(wk, sn);
                if (s == null) {
                    fb.showFeedback(I18n.get("StartnumberNotFound", sn,
                            StringTools.getCellName(sheet, x, indizes[STARTNUMMER])));
                    continue;
                }
                startsak[s.getAKNummer()][s.isMaennlich() ? 1 : 0]++;

                String[] mm = setMitglied(wk, data[x], indizes[VORNAME], indizes[NACHNAME], indizes[GESCHLECHT],
                        indizes[JAHRGANG], x, sheet, file);

                result.put(sntext, mm);
            }

            fb.showFeedback(I18n.get("ImportedNamesForTeams", result.size() - size));

            valid++;
        }

        if (valid == 0) {
            fb.showFeedback(I18n.get("Error.NoValidSheetFound"));
        } else {
            for (int x = 0; x < startsak.length; x++) {
                if (startsak[x][0] + startsak[x][1] > 0) {
                    fb.showFeedback(I18n.get("ImportedNamesForTeamsExtended", wk.getRegelwerk().getAk(x).getName(),
                            startsak[x][0], startsak[x][1]));
                }
            }
        }
        fb.showFeedback(I18n.get("ImportFinished"));

        if (result.size() == 0) {
            throw new TableException(I18n.get("Error.ResultEmpty"), file, null);
        }
        return result;
    }

    static <T extends ASchwimmer> List<TeamWithStarters> tablesToStarters(AWettkampf<T> wk, Feedback fb,
            String[] sheets, Object[][][] tables, String file) throws TableEntryException, TableException {

        List<TeamWithStarters> result = new ArrayList<>();

        int valid = 0;

        MannschaftWettkampf mwk = (MannschaftWettkampf) wk;

        for (int y = 0; y < tables.length; y++) {
            int size = result.size();

            String sheet = sheets[y];

            fb.showFeedback(I18n.get("ImportingSheet", sheet));

            Object[][] data = tables[y];
            if ((data == null) || (data.length == 0)) {
                fb.showFeedback(I18n.get("Error.SheetEmpty"));
                continue;
            }
            int length = data[0].length;

            String[] titles = new String[length];
            for (int x = 0; x < length; x++) {
                titles[x] = data[0][x].toString();
            }
            int[] indizes = null;
            try {
                indizes = identifyIndizes(wk, titles, false, false, file, sheet);
                boolean first = true;
                StringBuilder text = new StringBuilder();
                if (indizes[STARTNUMMER] < 0) {
                    text.append("\t").append(I18n.get("Startnumber"));
                    first = false;
                }
                if (indizes[DISCIPLINE] < 0) {
                    text.append(first ? "\t" : ", ").append(I18n.get("Discipline"));
                    first = false;
                }
                if (text.length() > 0) {
                    fb.showFeedback(I18n.get("Error.NotAllHeadersFound", text.toString()));
                    continue;
                }
            } catch (TableFormatException tfe) {
                fb.showFeedback(I18n.get("Error.NotAllHeadersFound", ImportUtils.indizesToNames(tfe.getData(), "\t")));
                continue;
            }
            for (int x = 1; x < data.length; x++) {
                int sn = getStartnummer(wk, data[x], indizes[STARTNUMMER], x, sheets[y], file);
                String discipline = data[x][indizes[DISCIPLINE]] == null ? "" : data[x][indizes[DISCIPLINE]].toString();
                if (discipline.isBlank()) {
                    continue;
                }

                Mannschaft s = SearchUtils.getSchwimmer(mwk, sn);
                if (s == null) {
                    fb.showFeedback(I18n.get("StartnumberNotFound", sn,
                            StringTools.getCellName(sheet, x, indizes[STARTNUMMER])));
                    continue;
                }

                int[] starters = getStarters(data[x], tables[y][0]);

                result.add(new TeamWithStarters(sn, discipline,0, starters));
            }

            fb.showFeedback(I18n.get("ImportedNamesForTeams", result.size() - size));

            valid++;
        }

        if (valid == 0) {
            fb.showFeedback(I18n.get("Error.NoValidSheetFound"));
        }
        fb.showFeedback(I18n.get("ImportFinished"));

        if (result.isEmpty()) {
            throw new TableException(I18n.get("Error.ResultEmpty"), file, null);
        }
        return result;
    }

    private static int[] getStarters(Object[] data, Object[] objects) {
        List<String> temp = new ArrayList<>();
        for (int x = 0; x < 100; x++) {

            String starterX = String.format("starter%d", x + 1);

            for (int i = 0; i < objects.length; i++) {
                if (objects[i] == null) {
                    continue;
                }
                String title = objects[i].toString().trim().toLowerCase();
                if (title.equals(starterX)) {
                    temp.add(data[i] == null ? "" : data[i].toString().trim().toLowerCase());
                }
            }
        }
        return temp.stream().mapToInt(index -> {
            try {
                return Integer.valueOf(index) + 1;
            } catch (NumberFormatException nfe) {
                return 0;
            }
        }).toArray();

    }

    private static <T extends ASchwimmer> int getStarts(LinkedList<T> result) {
        int amount = 0;
        for (T t : result) {
            amount += t.getDisciplineChoiceCount();
        }
        return amount;
    }

    @SuppressWarnings({ "unchecked" })
    private static <T extends ASchwimmer> T generateSchwimmer(Object[] data, int[] indizes, String[] titles,
            AWettkampf<T> wk, int row, String sheet, String file) throws TableEntryException {
        try {
            boolean empty = true;
            boolean[] required = getRequiredIndizes(wk instanceof MannschaftWettkampf);
            for (int x = 0; x < data.length; x++) {
                boolean used = false;
                for (int y = 0; y < indizes.length; y++) {
                    if ((indizes[y] == x) && required[y]) {
                        used = true;
                        break;
                    }
                }
                if (used) {
                    Object aData = data[x];
                    String temp = aData.toString().trim();
                    if (temp.length() > 0) {
                        empty = false;
                        break;
                    }
                }
            }
            if (empty) {
                return null;
            }

            Qualifikation quali = getQuali(data, indizes[QUALI], row, sheet, file);
            String qualilevel = "";
            if (indizes[QUALI_LEVEL] >= 0) {
                qualilevel = data[indizes[QUALI_LEVEL]].toString();
            }

            AWettkampf<T> w = wk;

            if (data.length - 1 < indizes[MAX_INDEX]) {
                Object[] newdata = new Object[indizes[MAX_INDEX] + 1];
                System.arraycopy(data, 0, newdata, 0, data.length);
                for (int x = data.length; x < newdata.length; x++) {
                    newdata[x] = "";
                }
                data = newdata;
            }
            if (w instanceof EinzelWettkampf) {
                String vorname = data[indizes[VORNAME]].toString();
                if (vorname.equals("")) {
                    throw new TableEntryException(
                            I18n.get("MissingEntry", I18n.get("FirstName"),
                                    StringTools.getCellName(sheet, row, indizes[VORNAME])),
                            file, sheet, row, indizes[VORNAME]);
                }
                String nachname = data[indizes[NACHNAME]].toString();
                if (nachname.equals("")) {
                    throw new TableEntryException(
                            I18n.get("MissingEntry", I18n.get("Surname"),
                                    StringTools.getCellName(sheet, row, indizes[NACHNAME])),
                            file, sheet, row, indizes[NACHNAME]);
                }
            } else {
                String name = data[indizes[NAME]].toString();
                if (name.equals("")) {
                    throw new TableEntryException(
                            I18n.get("MissingEntry", I18n.get("Name"),
                                    StringTools.getCellName(sheet, row, indizes[NAME])),
                            file, sheet, row, indizes[NAME]);
                }
            }

            boolean maennlich = getMaennlich(wk, data[indizes[GESCHLECHT]], indizes[GESCHLECHT], row, sheet, file);
            String gname = "";
            if (indizes[GLIEDERUNG] >= 0) {
                gname = data[indizes[GLIEDERUNG]].toString();
            }
            if (gname.equals("")) {
                if (w instanceof MannschaftWettkampf) {

                    gname = name2Gliederung(data[indizes[NAME]].toString());
                }
            }
            if (gname.equals("")) {
                throw new TableEntryException(
                        I18n.get("MissingEntry", I18n.get("Organisation"),
                                StringTools.getCellName(sheet, row, indizes[GLIEDERUNG])),
                        file, sheet, row, indizes[GLIEDERUNG]);
            }
            String gliederung = wk.getGliederung(gname);
            Object ako = data[indizes[ALTERSKLASSE]];
            int ak = wk.getRegelwerk().getIndex(ako.toString());
            if (ak < 0) {
                if (ako instanceof Number) {
                    Number akn = (Number) ako;
                    ak = wk.getRegelwerk().getIndex("" + akn.intValue());
                    if (ak < 0) {
                        int a = akn.intValue() - 1;
                        if ((a >= 0) && (a < wk.getRegelwerk().size())) {
                            ak = a;
                        }
                    }
                }
            }
            if (ak < 0) {
                if (ako.toString().trim().length() == 0) {
                    if ((wk instanceof EinzelWettkampf) && (indizes[JAHRGANG] >= 0)) {
                        int jahrgang = getJahrgang(data, indizes[JAHRGANG], row, sheet, file);
                        if (jahrgang > 0) {
                            int base = wk.getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION,
                                    Calendar.getInstance().get(Calendar.YEAR));
                            int alter = base - jahrgang;
                            ak = wk.getRegelwerk().getAkNachAlter(alter);
                        }
                    }
                } else {
                    String a = ako.toString().trim().toLowerCase().replace("/", "").replace("|", "").replace("-", "")
                            .replace(" ", "");
                    ak = wk.getRegelwerk().getIndex(aknames.getString(a));
                }
            }
            if (ak < 0 && wk.getRegelwerk().getAks().length == 1) {
                ak = 0;
            }
            if (ak < 0) {
                if (indizes[ALTERSKLASSE] < data.length - 1) {
                    throw new TableEntryException(
                            I18n.get("Error.AgeGroupNotFound", data[indizes[ALTERSKLASSE]].toString(),
                                    StringTools.getCellName(sheet, row, indizes[ALTERSKLASSE])),
                            file, sheet, row, indizes[ALTERSKLASSE]);
                }
                throw new TableEntryException(
                        I18n.get("Error.GeneralAgeGroupNotFound", data[indizes[ALTERSKLASSE]].toString(),
                                StringTools.getRowName(sheet, row, indizes[ALTERSKLASSE])),
                        file, sheet, row, indizes[ALTERSKLASSE]);
            }

            String bemerkung = getBemerkung(data, indizes[BEMERKUNG]);
            double punkte1 = getPunkte(data, indizes[PUNKTE1], row, sheet, file);
            double punkte2 = getPunkte(data, indizes[PUNKTE2], row, sheet, file);

            ASchwimmer s = null;
            if (w instanceof EinzelWettkampf) {
                int jahrgang = getJahrgang(data, indizes[JAHRGANG], row, sheet, file);
                String vorname = data[indizes[VORNAME]].toString();
                String nachname = data[indizes[NACHNAME]].toString();
                EinzelWettkampf ewk = (EinzelWettkampf) w;
                s = ewk.createTeilnehmer(nachname, vorname, jahrgang, maennlich, gliederung, ak, bemerkung);
            } else {
                String name = data[indizes[NAME]].toString();
                MannschaftWettkampf mwk = (MannschaftWettkampf) w;
                Mannschaft m = mwk.createMannschaft(name, maennlich, gliederung, ak, bemerkung);

                setMitglied(wk, m.getMannschaftsmitglied(0), data, indizes[VORNAME1], indizes[NACHNAME1],
                        indizes[GESCHLECHT1], indizes[JAHRGANG1], row, sheet, file);
                if (m.getAK().getMaxMembers() > 1) {
                    setMitglied(wk, m.getMannschaftsmitglied(1), data, indizes[VORNAME2], indizes[NACHNAME2],
                            indizes[GESCHLECHT2], indizes[JAHRGANG2], row, sheet, file);
                }
                if (m.getAK().getMaxMembers() > 2) {
                    setMitglied(wk, m.getMannschaftsmitglied(2), data, indizes[VORNAME3], indizes[NACHNAME3],
                            indizes[GESCHLECHT3], indizes[JAHRGANG3], row, sheet, file);
                }
                if (m.getAK().getMaxMembers() > 3) {
                    setMitglied(wk, m.getMannschaftsmitglied(3), data, indizes[VORNAME4], indizes[NACHNAME4],
                            indizes[GESCHLECHT4], indizes[JAHRGANG4], row, sheet, file);
                }
                if (m.getAK().getMaxMembers() > 4) {
                    setMitglied(wk, m.getMannschaftsmitglied(4), data, indizes[VORNAME5], indizes[NACHNAME5],
                            indizes[GESCHLECHT5], indizes[JAHRGANG5], row, sheet, file);
                }
                if (m.getAK().getMaxMembers() > 5) {
                    setMitglied(wk, m.getMannschaftsmitglied(5), data, indizes[VORNAME6], indizes[NACHNAME6],
                            indizes[GESCHLECHT6], indizes[JAHRGANG6], row, sheet, file);
                }
                if (m.getAK().getMaxMembers() > 6) {
                    setMitglied(wk, m.getMannschaftsmitglied(6), data, indizes[VORNAME7], indizes[NACHNAME7],
                            indizes[GESCHLECHT7], indizes[JAHRGANG7], row, sheet, file);
                }
                if (m.getAK().getMaxMembers() > 7) {
                    setMitglied(wk, m.getMannschaftsmitglied(7), data, indizes[VORNAME8], indizes[NACHNAME8],
                            indizes[GESCHLECHT8], indizes[JAHRGANG8], row, sheet, file);
                }
                if (m.getAK().getMaxMembers() > 8) {
                    setMitglied(wk, m.getMannschaftsmitglied(8), data, indizes[VORNAME9], indizes[NACHNAME9],
                            indizes[GESCHLECHT9], indizes[JAHRGANG9], row, sheet, file);
                }
                if (m.getAK().getMaxMembers() > 9) {
                    setMitglied(wk, m.getMannschaftsmitglied(9), data, indizes[VORNAME10], indizes[NACHNAME10],
                            indizes[GESCHLECHT10], indizes[JAHRGANG10], row, sheet, file);
                }
                if (m.getAK().getMaxMembers() > 10) {
                    setMitglied(wk, m.getMannschaftsmitglied(10), data, indizes[VORNAME11], indizes[NACHNAME11],
                            indizes[GESCHLECHT11], indizes[JAHRGANG11], row, sheet, file);
                }
                if (m.getAK().getMaxMembers() > 11) {
                    setMitglied(wk, m.getMannschaftsmitglied(11), data, indizes[VORNAME12], indizes[NACHNAME12],
                            indizes[GESCHLECHT12], indizes[JAHRGANG12], row, sheet, file);
                }

                s = m;
            }
            int[] meldezeiten = ImportUtils.getMeldezeiten(data, indizes[MELDEZEITEN], titles,
                    wk.getRegelwerk().getAk(ak), row, sheet, file);
            int[][] starter = ImportUtils.getStarter(data, titles, wk.getRegelwerk().getAk(ak), row, sheet, file);
            boolean ausserKonkurrenz = getBoolean(data, indizes[AUSSER_KONKURRENZ], row, sheet, file, false);
            try {
                if (s.getAK().isDisciplineChoiceAllowed()) {
                    s.setDisciplineChoice(ImportUtils.getDisciplineSelection(data, indizes[DISCIPLINES], titles,
                            s.getAK(), row, sheet, file));
                }
            } catch (RuntimeException re) {
                re.printStackTrace();
                if (indizes[DISCIPLINES] >= 0) {
                    throw new TableEntryException(
                            I18n.get("Error.Disciplines", s.getName(), data[indizes[DISCIPLINES]].toString()), file,
                            sheet, row, indizes[DISCIPLINES]);
                }
                // throw new TableEntryException(I18n.get("Error.Disciplines", s.getName(),
                // I18n.get("Error.NotFound")));
            }
            try {
                if (meldezeiten != null) {
                    s.setMeldezeiten(meldezeiten);
                }
            } catch (RuntimeException re) {
                re.printStackTrace();
                throw new TableEntryException(
                        I18n.get("Error.Meldezeiten", s.getName(), StringTools.getRowName(sheet, row)), file, sheet,
                        row, -1);
            }
            try {
                if (starter != null) {
                    s.setStarter(starter);
                }
            } catch (RuntimeException re) {
                re.printStackTrace();
                throw new TableEntryException(
                        I18n.get("Error.Starter", s.getName(), StringTools.getRowName(sheet, row)), file, sheet, row,
                        -1);
            }
            if (indizes[STARTPASS] >= 0) {
                s.setStartunterlagen(ImportUtils.checkStartpass(data[indizes[STARTPASS]].toString(), row,
                        indizes[STARTPASS], sheet, file));
            }
            s.setMeldungMitProtokoll(0, getBoolean(data, indizes[PROTOCOL1], row, sheet, file, false));
            s.setMeldungMitProtokoll(1, getBoolean(data, indizes[PROTOCOL2], row, sheet, file, false));
            s.setMeldepunkte(0, punkte1);
            s.setMeldepunkte(1, punkte2);
            s.setAusserKonkurrenz(ausserKonkurrenz);
            s.setStartnummer(getStartnummer(wk, data, indizes[STARTNUMMER], row, sheet, file));
            s.setQualifikationsebene(qualilevel);
            s.setQualifikation(quali);

            return (T) s;
        } catch (RuntimeException re) {
            re.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings({ "unchecked" })
    private static <T extends ASchwimmer> T generateSchwimmerUpdate(Object[] data, int[] indizes, String[] titles,
            AWettkampf<T> wk, int row, String sheet, String file) throws TableEntryException {
        try {
            boolean empty = true;
            boolean[] required = getRequiredIndizesForUpdate(wk instanceof MannschaftWettkampf);
            for (int x = 0; x < data.length; x++) {
                boolean used = false;
                for (int y = 0; y < indizes.length; y++) {
                    if ((indizes[y] == x) && required[y]) {
                        used = true;
                        break;
                    }
                }
                if (used) {
                    Object aData = data[x];
                    String temp = aData.toString().trim();
                    if (temp.length() > 0) {
                        empty = false;
                        break;
                    }
                }
            }
            if (empty) {
                log.info("Empty");
                return null;
            }

            AWettkampf<T> w = wk;

            int sn = getStartnummer(wk, data, indizes[STARTNUMMER], row, sheet, file);
            ASchwimmer as = SearchUtils.getSchwimmer(w, sn);
            if (as == null) {
                log.info("Not found {}", sn);
                return null;
            }
            T updated = (T) Utils.copy(as);

            if (w instanceof EinzelWettkampf) {
                String vorname = data[indizes[VORNAME]].toString();
                if (vorname.equals("")) {
                    throw new TableEntryException(
                            I18n.get("MissingEntry", I18n.get("FirstName"),
                                    StringTools.getCellName(sheet, row, indizes[VORNAME])),
                            file, sheet, row, indizes[VORNAME]);
                }
                String nachname = data[indizes[NACHNAME]].toString();
                if (nachname.equals("")) {
                    throw new TableEntryException(
                            I18n.get("MissingEntry", I18n.get("Surname"),
                                    StringTools.getCellName(sheet, row, indizes[NACHNAME])),
                            file, sheet, row, indizes[NACHNAME]);
                }
            } else {
                String name = data[indizes[NAME]].toString();
                if (name.equals("")) {
                    throw new TableEntryException(
                            I18n.get("MissingEntry", I18n.get("Name"),
                                    StringTools.getCellName(sheet, row, indizes[NAME])),
                            file, sheet, row, indizes[NAME]);
                }
            }

            if (updated instanceof Teilnehmer t) {
                int jahrgang = getJahrgang(data, indizes[JAHRGANG], row, sheet, file);
                String vorname = data[indizes[VORNAME]].toString();
                String nachname = data[indizes[NACHNAME]].toString();

                if (jahrgang == t.getJahrgang() && t.getVorname().equals(vorname) && t.getNachname().equals(nachname)) {
                    // log.info("Equals {}", t);
                    return null;
                }

                t.setJahrgang(jahrgang);
                t.setVorname(vorname);
                t.setNachname(nachname);

                log.info("Updating {}", t);
                return updated;
            }

            if (updated instanceof Mannschaft m) {
                String name = data[indizes[NAME]].toString();

                if (m.getName().equals(name)) {
                    // log.info("Equals {}", m);
                    return null;
                }

                m.setName(name);

                log.info("Updating {}", m);
                return updated;
            }
            log.info("Not Individual or Team");
            return null;
        } catch (RuntimeException re) {
            re.printStackTrace();
            return null;
        }
    }

    private static <T extends ASchwimmer> String[] setMitglied(AWettkampf<T> wk, Object[] data, int vorname,
            int nachname, int geschlecht, int jahrgang, int row, String sheet, String file) throws TableEntryException {
        String[] result = new String[4];
        result[0] = "";
        result[1] = "";
        result[2] = "0";
        result[3] = "-";
        if (nachname >= 0) {
            result[0] = data[nachname].toString();
        }
        if (vorname >= 0) {
            result[1] = data[vorname].toString();
        }
        if (jahrgang >= 0) {
            result[2] = "" + getJahrgang(data, jahrgang, row, sheet, file);
        }
        if (geschlecht >= 0) {
            String g = data[geschlecht].toString().trim();
            if (g.length() == 0 || g.equals("-")) {
                result[3] = "-";
            } else {
                try {
                    result[3] = getMaennlich(wk, g, geschlecht, row, sheet, file) ? "m" : "f";
                } catch (Exception x) {
                    result[3] = "-";
                }
            }
        }
        return result;
    }

    private static <T extends ASchwimmer> void setMitglied(AWettkampf<T> wk, Mannschaftsmitglied mm, Object[] data,
            int vorname, int nachname, int geschlecht, int jahrgang, int row, String sheet, String file)
            throws TableEntryException {
        if (vorname >= 0) {
            mm.setVorname(data[vorname].toString());
        }
        if (nachname >= 0) {
            mm.setNachname(data[nachname].toString());
        }
        if (jahrgang >= 0) {
            mm.setJahrgang(getJahrgang(data, jahrgang, row, sheet, file));
        }
        if (geschlecht >= 0) {
            String g = data[geschlecht].toString().trim();
            if (g.length() == 0 || g.equals("-")) {
                mm.setGeschlecht(Geschlecht.unbekannt);
            } else {
                try {
                    mm.setGeschlecht(getMaennlich(wk, g, geschlecht, row, sheet, file) ? Geschlecht.maennlich
                            : Geschlecht.weiblich);
                } catch (Exception x) {
                    mm.setGeschlecht(Geschlecht.unbekannt);
                }
            }
        }
    }

    private static String name2Gliederung(String name) {
        name = name.trim();
        String[] parts = name.split(" ");
        if (parts.length <= 1) {
            return name;
        }
        try {
            Integer.parseInt(parts[parts.length - 1]);
            return name.substring(0, name.lastIndexOf(" "));
        } catch (NumberFormatException nfe) {
            // Not an integer
            return name;
        }
    }

    private static int checkJahrgang(int jahr) {
        int thisyear = Calendar.getInstance().get(Calendar.YEAR) - 2000;
        if ((jahr >= 0) && (jahr <= thisyear)) {
            jahr += 2000;
        }
        if ((jahr > thisyear) && (jahr < 100)) {
            jahr += 1900;
        }
        if (jahr < 1900) {
            throw new IllegalArgumentException("Year out of range (has to be at leat 1900)");
        }
        return jahr;
    }

    private static int getJahrgang(Object[] data, int index, int row, String sheet, String file)
            throws TableEntryException {
        try {
            if (index < 0) {
                return 0;
            }
            if (data[index] == null) {
                return 0;
            }

            int value = 0;
            if (data[index] instanceof Number) {
                Number n = (Number) data[index];
                value = n.intValue();
            } else {
                String s = data[index].toString().trim();
                if (s.length() == 0) {
                    return 0;
                }
                if (s.equals("00")) {
                    return 2000;
                }
                value = Integer.parseInt(s);
            }
            return checkJahrgang(value);
        } catch (RuntimeException re) {
            re.printStackTrace();
            throw new TableEntryException(I18n.get("EntryErrorDescription", StringTools.getCellName(sheet, row, index),
                    data[index].toString()), file, sheet, row, index);
        }
    }

    private static Qualifikation getQuali(Object[] data, int index, int row, String sheet, String file)
            throws TableEntryException {
        if (index < 0) {
            return Qualifikation.OFFEN;
        }
        if (data[index] == null) {
            return Qualifikation.OFFEN;
        }
        String text = data[index].toString().trim();
        if (text.equalsIgnoreCase("")) {
            return Qualifikation.OFFEN;
        }
        if (text.equalsIgnoreCase(Qualifikation.DIREKT.toString())) {
            return Qualifikation.DIREKT;
        }
        if (text.equalsIgnoreCase(Qualifikation.GESETZT.toString())) {
            return Qualifikation.GESETZT;
        }
        if (text.equalsIgnoreCase(Qualifikation.GESPERRT.toString())) {
            return Qualifikation.GESPERRT;
        }
        if (text.equalsIgnoreCase(Qualifikation.NICHT_QUALIFIZIERT.toString())) {
            return Qualifikation.NICHT_QUALIFIZIERT;
        }
        if (text.equalsIgnoreCase(Qualifikation.OFFEN.toString())) {
            return Qualifikation.OFFEN;
        }
        if (text.equalsIgnoreCase(Qualifikation.QUALIFIZIERT.toString())) {
            return Qualifikation.QUALIFIZIERT;
        }
        if (text.equalsIgnoreCase(Qualifikation.NACHRUECKER.toString())) {
            return Qualifikation.NACHRUECKER;
        }
        if (text.equalsIgnoreCase("-")) {
            return Qualifikation.NICHT_QUALIFIZIERT;
        }
        if (text.equalsIgnoreCase("+")) {
            return Qualifikation.QUALIFIZIERT;
        }
        if (text.equalsIgnoreCase("ja")) {
            return Qualifikation.QUALIFIZIERT;
        }
        if (text.equalsIgnoreCase("nein")) {
            return Qualifikation.NICHT_QUALIFIZIERT;
        }
        if (text.equalsIgnoreCase("yes")) {
            return Qualifikation.QUALIFIZIERT;
        }
        if (text.equalsIgnoreCase("no")) {
            return Qualifikation.NICHT_QUALIFIZIERT;
        }
        if (text.equalsIgnoreCase("q")) {
            return Qualifikation.QUALIFIZIERT;
        }
        if (text.equalsIgnoreCase("d")) {
            return Qualifikation.DIREKT;
        }
        if (text.equalsIgnoreCase("g")) {
            return Qualifikation.GESETZT;
        }
        if (text.equalsIgnoreCase("n")) {
            return Qualifikation.NICHT_QUALIFIZIERT;
        }
        if (text.equalsIgnoreCase("nq")) {
            return Qualifikation.NICHT_QUALIFIZIERT;
        }
        if (text.equalsIgnoreCase("na")) {
            return Qualifikation.GESPERRT;
        }
        if (text.equalsIgnoreCase("dns")) {
            return Qualifikation.GESPERRT;
        }

        throw new TableEntryException(
                I18n.get("EntryErrorDescription", StringTools.getCellName(sheet, row, index), data[index].toString()),
                file, sheet, row, index);
    }

    private static final String[] YES_CONSTANTS = new String[] { "ja", "j", "true", "1", "+", "yes", "y", "x",
            I18n.get("yes") };
    private static final String[] NO_CONSTANTS = new String[] { "no", "n", "false", "0", "-", "no", I18n.get("no") };

    private static boolean getBoolean(Object[] data, int index, int row, String sheet, String file,
            boolean defaultvalue) throws TableEntryException {
        if (index < 0) {
            return defaultvalue;
        }
        try {
            if (data[index] instanceof Boolean) {
                return (Boolean) data[index];
            }
            String s = data[index].toString().trim().toLowerCase();
            if (s.length() == 0) {
                return defaultvalue;
            }

            for (String yes : YES_CONSTANTS) {
                if (s.equals(yes)) {
                    return true;
                }
            }
            for (String no : NO_CONSTANTS) {
                if (s.equals(no)) {
                    return false;
                }
            }
            throw new TableEntryException(
                    I18n.get("Error.NoBoolean", data[index].toString(), StringTools.getCellName(sheet, row, index)),
                    file, sheet, row, index);
        } catch (RuntimeException re) {
            throw new TableEntryException(
                    I18n.get("Error.NoBoolean", data[index].toString(), StringTools.getCellName(sheet, row, index)),
                    file, sheet, row, index);
        }
    }

    private static double getPunkte(Object[] data, int index, int row, String sheet, String file)
            throws TableEntryException {
        if (index < 0) {
            return 0.0;
        }
        if (data[index] instanceof Number) {
            Number n = (Number) data[index];
            return n.doubleValue();
        }
        String text = data[index].toString().trim().toLowerCase();
        if (text.length() == 0) {
            return 0.0;
        }
        // Compatibility with ChaosSoftware
        if (text.startsWith("ausschlu") || text.startsWith("ausschluss") || text.startsWith("disq")) {
            return 0;
        }
        try {
            text = text.replace(',', '.');
            NumberFormat nf = NumberFormat.getInstance();
            if (nf instanceof DecimalFormat) {
                DecimalFormat df = (DecimalFormat) nf;
                DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
                text = text.replace(dfs.getDecimalSeparator(), '.');
            }
            return Double.parseDouble(text);
        } catch (RuntimeException re) {
            throw new TableEntryException("Konnte Punkte in Zelle " + StringTools.getCellName(sheet, row, index)
                    + " nicht umwandeln: " + data[index].toString(), file, sheet, row, index);
        }
    }

    private static String getBemerkung(Object[] data, int index) {
        if (index < 0) {
            return "";
        }
        return data[index].toString();
    }

    private static <T extends ASchwimmer> int getStartnummer(AWettkampf<T> wk, Object[] data, int index, int row,
            String sheet, String file) throws TableEntryException {
        if (index < 0) {
            return 0;
        }
        if (data[index].toString().trim().length() == 0) {
            return 0;
        }
        if (data[index] instanceof Number) {
            Number n = (Number) data[index];
            return n.intValue();
        }
        try {
            return (int) Double.parseDouble(data[index].toString().trim());
        } catch (RuntimeException re) {
            // Nothing to do
        }
        try {
            return StartnumberFormatManager.convert(wk, data[index].toString().trim());
        } catch (RuntimeException re) {
            throw new TableEntryException(
                    I18n.get("WrongEntry", I18n.get("WrongValueForStartnumber", data[index].toString()),
                            StringTools.getCellName(sheet, row, index)),
                    file, sheet, row, index);
        }
    }

    private static <T extends ASchwimmer> ZWStartnummer getStartnummerHLW(AWettkampf<T> wk, Object[] data, int index,
            int row, String sheet, String file) throws TableEntryException {
        if (index < 0) {
            return null;
        }
        if (data[index].toString().trim().length() == 0) {
            return null;
        }
        if (data[index] instanceof Number n) {
            return new ZWStartnummer(n.intValue(), 0);
        }
        try {
            int sn = ZWUtils.getZWStartnummer(wk, data[index].toString());
            if (sn > 0) {
                int pos = ZWUtils.getZWIndex(wk, data[index].toString());
                return new ZWStartnummer(sn, pos);
            }
            return new ZWStartnummer(StartnumberFormatManager.convert(wk, data[index].toString().trim()), 0);
        } catch (RuntimeException re) {
            throw new TableEntryException(
                    I18n.get("WrongEntry", I18n.get("WrongValueForStartnumber", data[index].toString()),
                            StringTools.getCellName(sheet, row, index)),
                    file, sheet, row, index);
        }
    }

    private static class GenderToBooleanMapper {

        private final GenderIdentifier identifyAsFalse;
        private final GenderIdentifier identifyAsTrue;

        public GenderToBooleanMapper(GenderIdentifier identifyAsFalse, GenderIdentifier identifyAsTrue) {
            if (identifyAsFalse == null) {
                throw new NullPointerException("identifyAsFalse must not be null.");
            }
            if (identifyAsTrue == null) {
                throw new NullPointerException("identifyAsTrue must not be null.");
            }
            this.identifyAsFalse = identifyAsFalse;
            this.identifyAsTrue = identifyAsTrue;
        }

        boolean matches(Object data, int index, int row, String sheet, String file) throws TableEntryException {
            if (data instanceof Boolean) {
                return (Boolean) data;
            }
            if (identifyAsFalse.matches(data.toString())) {
                return false;
            }
            if (identifyAsTrue.matches(data.toString())) {
                return true;
            }
            throw new TableEntryException(
                    I18n.get("Error.SexExpectedButWas", data.toString(), StringTools.getCellName(sheet, row, index)),
                    file, sheet, row, index);
        }
    }

    private static class GenderIdentifier {

        private final Set<String> matchingValues;

        public GenderIdentifier(String... matchingValues) {
            this.matchingValues = Arrays.stream(matchingValues).filter(Objects::nonNull)
                    .filter(value -> !value.isBlank()).map(value -> value.trim().toLowerCase()).distinct()
                    .collect(Collectors.toSet());
        }

        boolean matches(String input) {
            return matchingValues.contains(input.toLowerCase());
        }
    }

    public static <T extends ASchwimmer> boolean getMaennlich(AWettkampf<T> wk, Object data, int index, int row,
            String sheet, String file)
            throws TableEntryException {
        GenderIdentifier female = new GenderIdentifier(
                "w", "weiblich", "weibl.",
                "f", "female",
                "0", "false",
                I18n.get("female"),
                wk != null ? wk.getRegelwerk().getTranslation("Female", I18n.get("Female")) : null,
                wk != null ? wk.getRegelwerk().getTranslation("female", I18n.get("female")) : null,
                wk != null ? wk.getRegelwerk().getTranslation("femaleShort", I18n.get("femaleShort")) : null);
        GenderIdentifier male = new GenderIdentifier(
                "m", "männlich", "männl.",
                "m", "male",
                "1", "true",
                I18n.get("male"),
                wk != null ? wk.getRegelwerk().getTranslation("Male", I18n.get("Male")) : null,
                wk != null ? wk.getRegelwerk().getTranslation("male", I18n.get("male")) : null,
                wk != null ? wk.getRegelwerk().getTranslation("maleShort", I18n.get("maleShort")) : null);
        return new GenderToBooleanMapper(female, male).matches(data, index, row, sheet, file);
    }

    private static <T extends ASchwimmer> int[] identifyIndizes(AWettkampf<T> wk, String[] titles, boolean einzel,
            boolean required, String file, String sheet) throws TableFormatException {
        LinkedList<Integer> indexlist = new LinkedList<>();

        int[] indizes = new int[INDEX_COUNT];
        for (int x = 0; x < indizes.length; x++) {
            indizes[x] = -1;
        }
        for (int x = 0; x < titles.length; x++) {
            String title = titles[x].toLowerCase().trim();
            ImportUtils.identifyIndex(wk, indizes, title, x);
        }
        if (indizes[GLIEDERUNG] == -1) {
            if (einzel) {
                indexlist.addLast(GLIEDERUNG);
            }
        }
        if (indizes[ALTERSKLASSE] == -1) {
            indexlist.addLast(ALTERSKLASSE);
        }
        if (indizes[GESCHLECHT] == -1) {
            indexlist.addLast(GESCHLECHT);
        }
        if (einzel) {
            if (indizes[VORNAME] == -1) {
                indexlist.addLast(VORNAME);
            }
            if (indizes[NACHNAME] == -1) {
                indexlist.addLast(NACHNAME);
            }
        } else {
            if (indizes[NAME] == -1) {
                indexlist.addLast(NAME);
            }
        }
        if (indexlist.size() > 0) {
            if (required) {
                int[] data = new int[indexlist.size()];
                ListIterator<Integer> li = indexlist.listIterator();
                for (int x = 0; li.hasNext(); x++) {
                    data[x] = li.next();
                }
                throw new TableFormatException(data, file, sheet);
            }
        }
        for (int x = 0; x < INDEX_COUNT - 1; x++) {
            if (indizes[x] > indizes[MAX_INDEX]) {
                indizes[MAX_INDEX] = indizes[x];
            }
        }
        return indizes;
    }

    static <T extends ASchwimmer> Hashtable<ZWStartnummer, Double> tablesToZWResult(AWettkampf<T> wk, Feedback fb,
            String[] sheets, Object[][][] tables, String file) throws TableException, TableEntryException {
        Hashtable<ZWStartnummer, Double> result = new Hashtable<ZWStartnummer, Double>();
        int[][] startsak = new int[wk.getRegelwerk().size()][2];
        int valid = 0;
        for (int y = 0; y < tables.length; y++) {
            int size = result.size();

            String sheet = sheets[y];

            fb.showFeedback(I18n.get("ImportingSheet", sheet));

            Object[][] data = tables[y];
            if ((data == null) || (data.length == 0)) {
                fb.showFeedback(I18n.get("Error.SheetEmpty"));
                continue;
            }
            int length = data[0].length;

            String[] titles = new String[length];
            for (int x = 0; x < length; x++) {
                titles[x] = data[0][x].toString();
            }
            int[] indizes = null;
            try {
                indizes = identifyIndizes(wk, titles, false, false, file, sheet);
                boolean first = true;
                StringBuilder text = new StringBuilder();
                if (indizes[STARTNUMMER] < 0) {
                    text.append("\t").append(I18n.get("Startnumber"));
                    first = false;
                }
                if (indizes[ZW] < 0) {
                    if (!first) {
                        text.append(", ");
                    } else {
                        first = false;
                    }
                    text.append("\t").append(wk.getRegelwerk().getZusatzwertungShort()).append(", ");
                }
                if (text.length() > 0) {
                    fb.showFeedback(I18n.get("Error.NotAllHeadersFound", text.toString()));
                    continue;
                }
            } catch (TableFormatException tfe) {
                fb.showFeedback(I18n.get("Error.NotAllHeadersFound", ImportUtils.indizesToNames(tfe.getData(), "\t")));
                continue;
            }
            for (int x = 1; x < data.length; x++) {
                ZWStartnummer sn = getStartnummerHLW(wk, data[x], indizes[STARTNUMMER], x, sheet, file);
                if (sn == null) {
                    fb.showFeedback(I18n.get("StartnumberFormatUnknown", data[x][indizes[STARTNUMMER]],
                            StringTools.getCellName(sheet, x, indizes[STARTNUMMER])));
                    throw new TableException(I18n.get("Error.ParseError", data[x][indizes[STARTNUMMER]],
                            StringTools.getCellName(sheet, x, indizes[STARTNUMMER])), file, sheet);
                }
                ASchwimmer s = SearchUtils.getSchwimmer(wk, sn.getStartnummer());
                if (s == null) {
                    fb.showFeedback(I18n.get("StartnumberNotFound", sn,
                            StringTools.getCellName(sheet, x, indizes[STARTNUMMER])));
                    continue;
                }

                Object o = data[x][indizes[ZW]];
                if (o instanceof Number) {
                    result.put(sn, ((Number) o).doubleValue());
                } else {
                    String zw = o.toString().toLowerCase().trim();
                    if (zw.length() > 0) {
                        double value = 0;
                        zw = zw.replace(" ", "");
                        if (zw.equals("v1") || zw.equals("n.a.") || zw.equals("n") || zw.equals("na")) {
                            value = -2;
                        } else if (zw.equals("-")) {
                            value = -1;
                        } else {
                            value = getPunkte(data[x], indizes[ZW], x, sheet, file);
                        }
                        if (result.get(sn) != null) {
                            fb.showFeedback(I18n.get("StartnumberAlreadyFound", data[x][indizes[STARTNUMMER]],
                                    StringTools.getCellName(sheet, x, indizes[STARTNUMMER])));
                            throw new TableException(I18n.get("Error.DuplicateEntry", data[x][indizes[STARTNUMMER]],
                                    StringTools.getCellName(sheet, x, indizes[STARTNUMMER])), file, sheet);
                        }
                        result.put(sn, value);
                    }
                }
                startsak[s.getAKNummer()][s.isMaennlich() ? 1 : 0]++;
            }

            fb.showFeedback(I18n.get("ImportedPoints", result.size() - size));

            valid++;
        }

        if (valid == 0) {
            fb.showFeedback(I18n.get("Error.NoValidSheetFound"));
        } else {
            for (int x = 0; x < startsak.length; x++) {
                if (startsak[x][0] + startsak[x][1] > 0) {
                    fb.showFeedback(I18n.get("ImportedPointsExtended", wk.getRegelwerk().getAk(x).getName(),
                            startsak[x][0], startsak[x][1]));
                }
            }
        }
        fb.showFeedback(I18n.get("ImportFinished"));

        if (result.size() == 0) {
            throw new TableException(I18n.get("Error.ResultEmpty"), file, null);
        }

        return result;
    }

    static <T extends ASchwimmer> LinkedList<T> tablesToRegistration(AWettkampf<T> wk, Feedback fb, String[] sheets,
            Object[][][] tables, String file) throws TableException, TableEntryException {
        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;
        boolean einzel = (w instanceof EinzelWettkampf);
        LinkedList<T> result = new LinkedList<>();
        int[][] startsak = new int[wk.getRegelwerk().size()][2];

        int valid = 0;

        for (int y = 0; y < tables.length; y++) {
            int size = result.size();
            int starts = getStarts(result);

            String sheet = sheets[y];

            fb.showFeedback(I18n.get("ImportingSheet", sheet));

            Object[][] data = tables[y];
            if ((data == null) || (data.length == 0)) {
                fb.showFeedback(I18n.get("Error.SheetEmpty"));
                continue;
            }
            int length = data[0].length;

            String[] titles = new String[length];
            for (int x = 0; x < length; x++) {
                titles[x] = data[0][x].toString();
            }
            int[] indizes = null;
            try {
                indizes = identifyIndizes(wk, titles, einzel, true, file, sheet);
            } catch (TableFormatException tfe) {
                fb.showFeedback(I18n.get("Error.NotAllHeadersFound", ImportUtils.indizesToNames(tfe.getData(), "")));
                continue;
            }
            for (int x = 1; x < data.length; x++) {
                T s = generateSchwimmer(data[x], indizes, titles, wk, x, sheet, file);
                if (s != null) {
                    startsak[s.getAKNummer()][(s.isMaennlich() ? 1 : 0)] += s.getDisciplineChoiceCount();
                    result.addLast(s);
                }
            }

            fb.showFeedback(I18n.get("ImportedEntries", result.size() - size, getStarts(result) - starts));

            valid++;
        }

        if (valid == 0) {
            fb.showFeedback(I18n.get("Error.NoValidSheetFound"));
        } else {
            fb.showFeedback(I18n.get("ResultsOfImport"));
            for (int x = 0; x < startsak.length; x++) {
                if (startsak[x][0] + startsak[x][1] > 0) {
                    fb.showFeedback(I18n.get("ImportedStarts", wk.getRegelwerk().getAk(x).getName(), startsak[x][0],
                            startsak[x][1]));
                }
            }
        }
        fb.showFeedback(I18n.get("ImportFinished"));

        // if (result.size() == 0) {
        // throw new TableException(I18n.get("Error.ResultEmpty"), file,
        // null);
        // }

        return result;
    }

    static <T extends ASchwimmer> LinkedList<T> tablesToRegistrationUpdate(AWettkampf<T> wk, Feedback fb,
            String[] sheets,
            Object[][][] tables, String file) throws TableException, TableEntryException {
        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;
        boolean einzel = (w instanceof EinzelWettkampf);
        LinkedList<T> result = new LinkedList<T>();
        int[][] startsak = new int[wk.getRegelwerk().size()][2];

        int valid = 0;

        for (int y = 0; y < tables.length; y++) {
            int size = result.size();
            int starts = getStarts(result);

            String sheet = sheets[y];

            fb.showFeedback(I18n.get("ImportingSheet", sheet));

            Object[][] data = tables[y];
            if ((data == null) || (data.length == 0)) {
                fb.showFeedback(I18n.get("Error.SheetEmpty"));
                continue;
            }
            int length = data[0].length;

            String[] titles = new String[length];
            for (int x = 0; x < length; x++) {
                titles[x] = data[0][x].toString();
            }
            int[] indizes = null;
            try {
                indizes = identifyIndizes(wk, titles, einzel, false, file, sheet);
            } catch (TableFormatException tfe) {
                fb.showFeedback(I18n.get("Error.NotAllHeadersFound", ImportUtils.indizesToNames(tfe.getData(), "")));
                continue;
            }
            for (int x = 1; x < data.length; x++) {
                T s = generateSchwimmerUpdate(data[x], indizes, titles, wk, x, sheet, file);
                if (s != null) {
                    startsak[s.getAKNummer()][(s.isMaennlich() ? 1 : 0)] += s.getDisciplineChoiceCount();
                    result.addLast(s);
                }
            }

            fb.showFeedback(I18n.get("ImportedEntries", result.size() - size, getStarts(result) - starts));

            valid++;
        }

        if (valid == 0) {
            fb.showFeedback(I18n.get("Error.NoValidSheetFound"));
        } else {
            fb.showFeedback(I18n.get("ResultsOfImport"));
            for (int x = 0; x < startsak.length; x++) {
                if (startsak[x][0] + startsak[x][1] > 0) {
                    fb.showFeedback(I18n.get("ImportedStarts", wk.getRegelwerk().getAk(x).getName(), startsak[x][0],
                            startsak[x][1]));
                }
            }
        }
        fb.showFeedback(I18n.get("ImportFinished"));

        // if (result.size() == 0) {
        // throw new TableException(I18n.get("Error.ResultEmpty"), file,
        // null);
        // }

        return result;
    }

    static <T extends ASchwimmer> KampfrichterVerwaltung tablesToReferees(AWettkampf<T> wk, Feedback fb,
            String[] sheets, Object[][][] tables, String file) throws TableEntryException {
        KampfrichterVerwaltung kv = Utils.copy(wk.getKampfrichterverwaltung());
        for (int x = 0; x < kv.getEinheitenCount(); x++) {
            KampfrichterEinheit ke = kv.getEinheit(x);
            String[] positionen = ke.getPositionen();
            for (String aPositionen : positionen) {
                ke.setKampfrichter(aPositionen, new LinkedList<Kampfrichter>());
            }
        }
        int referees = 0;
        int valid = 0;

        for (int y = 0; y < tables.length; y++) {
            int size = referees;

            String sheet = sheets[y];

            fb.showFeedback(I18n.get("ImportingSheet", sheet));

            Object[][] data = tables[y];
            if ((data == null) || (data.length == 0)) {
                fb.showFeedback(I18n.get("Error.SheetEmpty"));
                continue;
            }
            int length = data[0].length;

            String[] titles = new String[length];
            for (int x = 0; x < length; x++) {
                titles[x] = data[0][x].toString();
            }
            int[] indizes = null;
            try {
                indizes = identifyIndizes(wk, titles, false, false, file, sheet);

                LinkedList<String> missing = new LinkedList<String>();

                if (indizes[CATEGORY] < 0) {
                    missing.addLast(I18n.get("Category"));
                }
                if (indizes[POSITION] < 0) {
                    missing.addLast(I18n.get("Position"));
                }
                if (indizes[NAME] < 0) {
                    missing.addLast(I18n.get("Name"));
                }
                if (indizes[STUFE] < 0) {
                    missing.addLast(I18n.get("Level"));
                }
                if (!missing.isEmpty()) {
                    StringBuilder text = new StringBuilder();
                    text.append("\t");
                    text.append(missing.getFirst());
                    missing.removeFirst();
                    for (String s : missing) {
                        text.append(",\n\t");
                        text.append(s);
                    }
                    if (text.length() > 0) {
                        fb.showFeedback(I18n.get("Error.NotAllHeadersFound", text.toString()));
                        continue;
                    }
                }
            } catch (TableFormatException tfe) {
                fb.showFeedback(I18n.get("Error.NotAllHeadersFound", ImportUtils.indizesToNames(tfe.getData(), "\t")));
                continue;
            }

            for (int x = 1; x < data.length; x++) {
                String category = data[x][indizes[CATEGORY]].toString();
                String position = data[x][indizes[POSITION]].toString();
                String name = data[x][indizes[NAME]].toString();
                String gliederung = "";
                if (indizes[GLIEDERUNG] >= 0) {
                    gliederung = data[x][indizes[GLIEDERUNG]].toString();
                }
                String stufe = data[x][indizes[STUFE]].toString();
                String bemerkung = "";
                if (indizes[BEMERKUNG] >= 0) {
                    bemerkung = data[x][indizes[BEMERKUNG]].toString();
                }

                KampfrichterEinheit ke = kv.getEinheit(category);
                if (ke == null) {
                    throw new TableEntryException(I18n.get("CategoryNotFound", category), file, sheet, x,
                            indizes[CATEGORY]);
                }
                try {
                    KampfrichterStufe level = DataTableUtils.getLevel(stufe);
                    if (level == null) {
                        throw new TableEntryException(I18n.get("LevelUnknown", stufe), file, sheet, x, indizes[STUFE]);
                    }
                    Kampfrichter kari = new Kampfrichter(name, gliederung, bemerkung, level);
                    ke.addKampfrichter(position, kari);
                } catch (IllegalArgumentException iae) {
                    throw new TableEntryException(I18n.get("PositionNotFound", position), file, sheet, x,
                            indizes[POSITION]);
                }

                referees++;
            }

            fb.showFeedback(I18n.get("ImportedReferees", referees - size));

            valid++;
        }

        if (valid == 0) {
            fb.showFeedback(I18n.get("Error.NoValidSheetFound"));
        }
        fb.showFeedback(I18n.get("ImportFinished"));

        if (referees == 0) {
            return null;
        }

        return kv;
    }

    private static Startunterlagen checkStartpass(String text, int row, int column, String sheet, String file)
            throws TableEntryException {
        text = text.trim().toLowerCase();
        if (column < 0) {
            return Startunterlagen.NICHT_PRUEFEN;
        }
        if (text.equals("+") || text.equals("x") || text.equals(I18n.get("yes").toLowerCase())) {
            return Startunterlagen.PRUEFEN;
        }
        if ((text.length() == 0) || text.equals(I18n.get("no")) || (text.equals("-"))) {
            return Startunterlagen.NICHT_PRUEFEN;
        }
        throw new TableEntryException(I18n.get("UnknownValue", text, StringTools.getCellName(sheet, row, column)), file,
                sheet, row, column);
    }

    private static String[] getAlternativeNames(String s) {
        String[] t = new String[15];
        t[0] = s.trim().toLowerCase();
        if (t[0].startsWith("'") && t[0].length() > 1) {
            t[0] = t[0].substring(1);
        }

        t[1] = t[0].replace("  ", " ");
        t[2] = t[1].replace("25 m", "25m").replace("50 m", "50m").replace("100 m", "100m").replace("200 m", "200m")
                .replace("400 m", "400m").replace("800 m", "800m").replace("4 * ", "4*").replace("4 x ", "4*")
                .replace("4x", "4*");
        t[3] = t[2].replace("25m", "25 m").replace("50m", "50 m").replace("100m", "100 m").replace("200m", "200 m")
                .replace("400m", "400 m").replace("800m", "800 m").replace("4*", "4 * ").replace("4x", "4 * ")
                .replace("4 x ", "4 * ");
        for (int x = 0; x < 2; x++) {
            t[4 + x] = t[2 + x].replace(" k. staffel", " kombinierte staffel")
                    .replace(" k. retten", " kombinierte rettungs\u00fcbung").replace(" k.", " kombiniertes")
                    .replace(" m.f.", " mit flossen").replace(" m.fl.", " mit flossen").replace(" m.", " mit")
                    .replace(" fl.", " flossen").replace(" f.", " flossen").replace(" e. p.", " einer puppe")
                    .replace("super lifesaver", "super-lifesaver").replace("life-saver", "lifesaver")
                    .replace(" roa", " rückenlage ohne armtätigkeit")
                    .replace("rückenlage ohne arme", "rückenlage ohne armtätigkeit")
                    .replace("rückenschwimmen", "rückenlage ohne armtätigkeit").replace(" e.", " einer")
                    .replace("st.", "staffel").replace("hindernis-schwimmen", "hindernisschwimmen")
                    .replace("hindernis- schwimmen", "hindernisschwimmen")
                    .replace("kombinierte rettungs-\u00fcbung", "kombinierte rettungs\u00fcbung")
                    .replace("kombiniertes rettungs-schwimmen", "kombinierte rettungs\u00fcbung")
                    .replace("kombinierte rettungs- \u00fcbung", "kombinierte rettungs\u00fcbung");
            t[6 + x] = t[4 + x].replace("kombiniertes", "kombinierte");
            t[8 + x] = t[4 + x].replace("hindernis", "hindernisschwimmen").replace("flossen", "flossenschwimmen");
        }

        t[10] = (t[4].equals("lifesaver") ? "100m lifesaver" : t[2]);
        t[10] = (t[4].equals("life-saver") ? "100m lifesaver" : t[10]);
        t[10] = (t[4].equals("leinenwurf") ? "werfen einer leine" : t[10]);
        t[10] = (t[10].equals("super lifesaver") ? "200m super-lifesaver" : t[10]);
        t[10] = (t[10].equals("super-lifesaver") ? "200m super-lifesaver" : t[10]);
        t[10] = (t[10].equals("200m sls") ? "200m super-lifesaver" : t[10]);
        t[10] = (t[10].equals("sls") ? "200m super-lifesaver" : t[10]);
        t[10] = (t[10].equals("100m ls") ? "100m lifesaver" : t[10]);
        t[10] = (t[10].equals("ls") ? "100m lifesaver" : t[10]);
        t[10] = (t[10].equals("100m retten einer puppe mit flossen und gurtretter") ? "100m lifesaver" : t[10]);
        t[10] = (t[10].equals("100m retten mit flossen und gurtretter") ? "100m lifesaver" : t[10]);
        t[10] = (t[10].equals("100m schwimmen und retten mit flossen und gurtretter") ? "100m lifesaver" : t[10]);
        t[10] = (t[10].equals("50m retten einer puppe") ? "50m retten" : t[10]);
        t[10] = (t[10].equals("50m retten einer puppe mit flossen") ? "50m retten mit flossen" : t[10]);
        t[10] = (t[10].equals("100m retten einer puppe mit flossen") ? "100m retten mit flossen" : t[10]);
        t[10] = (t[10].equals("100m schwimmen und retten mit flossen") ? "100m retten mit flossen" : t[10]);
        t[10] = (t[10].equals("100m kombiniertes retten") ? "100m kombinierte rettungs\u00fcbung" : t[10]);
        t[10] = (t[10].equals("100m kombi") ? "100m kombinierte rettungs\u00fcbung" : t[10]);
        t[10] = (t[10].equals("50m freistil") ? "50m freistilschwimmen" : t[10]);
        t[10] = (t[10].equals("50m kombi") ? "50m kombiniertes schwimmen" : t[10]);

        t[11] = t[4] + "staffel";
        t[12] = t[5] + "staffel";

        t[13] = t[4] + "schwimmen";
        t[14] = t[5] + "schwimmen";

        return t;
    }

    private static boolean[] getDisciplineSelection(Object[] data, int index, String[] titles, Altersklasse ak, int row,
            String sheet, String file) throws TableEntryException {
        if (!ak.isDisciplineChoiceAllowed()) {
            return null;
        }
        int[] indizes = getDisciplineIndizes(titles, ak);
        String error = null;
        boolean ok = true;
        for (int x = 0; x < indizes.length; x++) {
            if (indizes[x] < 0) {
                if (error == null) {
                    error = ak.getDisziplin(x, true).getName();
                } else {
                    error += ", " + ak.getDisziplin(x, true).getName();
                }
                ok = false;
            }
        }

        if (ok) {
            boolean[] result = new boolean[indizes.length];
            for (int x = 0; x < indizes.length; x++) {
                Object o = data[indizes[x]];
                if (o instanceof Boolean) {
                    result[x] = (Boolean) o;
                } else {
                    String s = o.toString().trim();
                    result[x] = (s.length() > 0) && (!s.equals("-"));
                }
            }
            return result;
        }

        if (index < 0) {
            throw new TableEntryException(I18n.get("Error.MissingDisciplines", error), file, sheet, -1, -1);
        }
        boolean[] result = getDisciplineSelection(data[index].toString(), ak, file, sheet, row, index);
        if ((result == null) && (error != null)) {
            throw new TableEntryException(I18n.get("Error.MissingDisciplines", error), file, sheet, row, index);
        }
        return result;
    }

    public static int getDisciplineIndex(Altersklasse ak, String title) {
        return getDisciplineIndex(ak, title, "");
    }

    public static int getDisciplineIndex(Altersklasse ak, String title, String suffix) {
        if (suffix != null && suffix.length() > 0) {
            if (title.endsWith(suffix)) {
                title = title.substring(0, title.length() - suffix.length()).trim();
            } else {
                return -1;
            }
        }
        String[] t = getAlternativeNames(title);
        for (int y = 0; y < ak.getDiszAnzahl(); y++) {
            String d = ak.getDisziplin(y, true).getName();
            String d1 = d.toLowerCase();
            String d2 = I18n.getDisziplinShort(d).toLowerCase();

            for (String aT1 : t) {
                if (d1.equals(aT1) || d2.equals(aT1)) {
                    return y;
                }
            }

            String shorttext1 = d1.replace("25m", "").replace("50m", "").replace("100m", "").replace("200m", "")
                    .replace("400m", "").replace("800m", "").replace("4*25m", "").replace("4*50m", "").trim();
            String shorttext2 = d2.replace("25m", "").replace("50m", "").replace("100m", "").replace("200m", "")
                    .replace("400m", "").replace("800m", "").replace("4*25m", "").replace("4*50m", "").trim();

            for (String aT : t) {
                if (shorttext1.equals(aT) || shorttext2.equals(aT)) {
                    return y;
                }
            }
        }
        return -1;
    }

    private static int[] getDisciplineIndizes(String[] titles, Altersklasse ak) {
        return getDisciplineIndizes(titles, ak, "");
    }

    private static int[] getDisciplineIndizes(String[] titles, Altersklasse ak, String suffix) {
        int[] indizes = new int[ak.getDiszAnzahl()];
        for (int x = 0; x < indizes.length; x++) {
            indizes[x] = -1;
        }

        for (int x = 0; x < titles.length; x++) {
            int y = getDisciplineIndex(ak, titles[x], suffix);
            if (y >= 0) {
                indizes[y] = x;
            }
        }
        return indizes;
    }

    private static boolean[] getDisciplineSelection(String data, Altersklasse ak, String file, String sheet, int row,
            int col) throws TableEntryException {
        String separator = ";";
        if (data.indexOf(separator) < 0) {
            separator = ",";
        }
        if (data.indexOf(separator) < 0) {
            separator = "" + CsvManager.getSeparator();
        }
        if (data.indexOf(separator) < 0) {
            return null;
        }
        String[] s = StringTools.split(data, separator);
        boolean[] result = new boolean[ak.getDiszAnzahl()];
        for (int x = 0; x < s.length; x++) {
            s[x] = s[x].trim().toLowerCase();
            if (s[x].length() > 0) {
                boolean found = false;
                for (int counter = 0; counter < 3; counter++) {
                    for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                        if (ak.getDisziplin(y, false).getName().equalsIgnoreCase(s[x].trim())) {
                            result[y] = true;
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                    switch (counter) {
                    case 0:
                        // Second test
                        s[x] = s[x].replaceAll("  ", " ");
                        break;
                    case 1:
                        // Third test
                        s[x] = s[x].replaceAll("25 m", "25m");
                        s[x] = s[x].replaceAll("50 m", "50m");
                        s[x] = s[x].replaceAll("100 m", "100m");
                        s[x] = s[x].replaceAll("200 m", "200m");
                        break;
                    default:
                        break;
                    }
                }
                if (!found) {
                    String d = "";
                    for (int i = 0; i < ak.getDiszAnzahl(); i++) {
                        if (i != 0) {
                            d += ", ";
                        }
                        d += ak.getDisziplin(i, false);
                    }
                    throw new TableEntryException(I18n.get("Error.DisciplineNotFound", data, s[x], ak.getName(), d,
                            StringTools.getCellName(sheet, row, col)), file, sheet, row, col);
                }
            }
        }
        return result;
    }

    private static double getDouble(String s, String file, String sheet, int row, int col) throws TableEntryException {
        if ((s == null) || (s.length() == 0)) {
            return 0;
        }
        try {
            return Double.parseDouble(s);
        } catch (RuntimeException re) {
            try {
                s = s.replace(',', '.');
                return Double.parseDouble(s);
            } catch (RuntimeException re2) {
                throw new TableEntryException(I18n.get("Error.NotANumber", s, StringTools.getCellName(sheet, row, col)),
                        file, sheet, row, col);
            }
        }
    }

    private static double getLong(String s, String file, String sheet, int row, int col) throws TableEntryException {
        if ((s == null) || (s.length() == 0)) {
            return 0;
        }
        try {
            return (long) Double.parseDouble(s);
        } catch (RuntimeException re) {
            try {
                s = s.replace(',', '.');
                return (long) Double.parseDouble(s);
            } catch (RuntimeException re2) {
                throw new TableEntryException(I18n.get("Error.NotANumber", s, StringTools.getCellName(sheet, row, col)),
                        file, sheet, row, col);
            }
        }
    }

    private static int[] getMeldezeiten(Object[] data, int index, int row, String sheet, String file)
            throws TableEntryException {
        if (index < 0) {
            return null;
        }
        try {
            String cell = data[index].toString();
            char separator = ';';
            if (cell.indexOf(separator) < 0) {
                separator = ',';
            }
            if (cell.indexOf(separator) < 0) {
                separator = CsvManager.getSeparator();
            }
            if (cell.indexOf(separator) < 0) {
                return null;
            }
            String[] s = StringTools.split(cell, "" + separator);
            int[] zeiten = new int[s.length];
            for (int x = 0; x < s.length; x++) {
                if (s[x].length() > 0) {
                    zeiten[x] = getTime(s[x], file, sheet, row, index);
                } else {
                    zeiten[x] = 0;
                }
            }
            return zeiten;
        } catch (RuntimeException re) {
            re.printStackTrace();
            return null;
        }
    }

    public static int getZeit(Object[] data, int index, int row, String sheet, String file) throws TableEntryException {
        Object o = data[index];
        if (o instanceof String) {
            try {
                o = Double.valueOf((String) o);
            } catch (RuntimeException re) {
                // Nothing to do
            }
        }
        if (o instanceof Number) {
            double d = ((Number) o).doubleValue();
            if (d < 0.1) {
                if (d < 0) {
                    d = 0;
                } else {
                    d = d * 24 * 60 * 60 * 100;
                }
            } else {
                if (d < 100) {
                    d *= 100;
                }
            }
            while (d >= 60 * 60 * 100) {
                d -= 60 * 60 * 100;
            }
            return (int) Math.round(d);
        }
        String s = o.toString().toLowerCase().trim();
        if (s.equals("+") || s.equals("x") || s.equals("ja") || s.equals("yes") || (s.length() == 0)) {
            return 0;
        }
        if (s.equals("-")) {
            return 0;
        }
        return getTime(s, file, sheet, row, index);
    }

    private static int[] getMeldezeiten(Object[] data, int index, String[] titles, Altersklasse ak, int row,
            String sheet, String file) throws TableEntryException {
        int[] indizes = getDisciplineIndizes(titles, ak);
        String error = null;
        int errortype = 0;
        boolean ok = true;
        for (int x = 0; x < indizes.length; x++) {
            if (indizes[x] < 0) {
                error = ak.getDisziplin(x, true).getName();
                errortype = 1;
                ok = false;
                break;
            }
        }
        if (ok) {
            try {
                int[] result = new int[indizes.length];
                boolean atLeastOne = false;
                for (int x = 0; x < indizes.length; x++) {
                    result[x] = 0;
                    try {
                        result[x] = getZeit(data, indizes[x], row, sheet, file);
                        if (result[x] > ASchwimmer.TIME_EPSILON) {
                            atLeastOne = true;
                        }
                    } catch (RuntimeException e) {
                        error = ak.getDisziplin(x, true).getName();
                        errortype = 2;
                        e.printStackTrace();
                        throw new RuntimeException("Dummy");
                    }
                }
                // If no time is found, we should try it the other way.
                if (atLeastOne) {
                    return result;
                }
            } catch (RuntimeException re) {
                // Nothing to do
            }
        }

        // The other way ;-)
        int[] result = getMeldezeiten(data, index, row, sheet, file);
        if ((result == null) && (errortype > 0)) {
            if (errortype == 2) {
                throw new TableEntryException(I18n.get("Error.MissingMeldezeitenNotANumber", error,
                        StringTools.getCellName(sheet, row, index)), file, sheet, row, index);
            }
            // throw new
            // TableEntryException(I18n.get("Error.MissingMeldezeiten",
            // error), sheet, row, index);
            return null;
        }

        return result;
    }

    private static int[][] getStarter(Object[] data, String[] titles, Altersklasse ak, int row, String sheet,
            String file) throws TableEntryException {
        int[] indizes = getDisciplineIndizes(titles, ak, " - Reihenfolge");
        boolean ok = false;
        for (int x = 0; x < indizes.length; x++) {
            if (indizes[x] >= 0) {
                ok = true;
                break;
            }
        }
        if (ok) {
            try {
                int[][] result = new int[indizes.length][0];
                boolean atLeastOne = false;
                for (int x = 0; x < indizes.length; x++) {
                    int index = indizes[x];
                    if (index >= 0) {
                        Object o = data[index];
                        if (o != null) {
                            try {
                                String text = o.toString().trim();
                                if (text.length() > 0) {
                                    String[] parts = text.split(",");
                                    int[] values = new int[parts.length];
                                    for (int y = 0; y < parts.length; y++) {
                                        values[y] = Integer.parseInt(parts[y].trim());
                                    }
                                    result[x] = values;
                                    atLeastOne = true;
                                }
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                                throw new TableEntryException(I18n.get("Error.StarterWrongFormat", file,
                                        StringTools.getCellName(sheet, row, index)), file, sheet, row, index);
                            }
                        }
                    }
                }
                if (atLeastOne) {
                    return result;
                }
            } catch (RuntimeException re) {
                // Nothing to do
            }
        }

        return null;
    }

    public static int getTime(Object data) throws Exception {
        try {
            return getTime(data.toString(), "", "", 0, 0);
        } catch (Exception ex) {
            throw new Exception("Invalid input", ex);
        }
    }

    public static int getTime(String s, String file, String sheet, int row, int col) throws TableEntryException {
        try {
            if (s == null) {
                return 0;
            }
            s = s.trim();
            if (s.length() == 0) {
                return 0;
            }
            s = s.replace(";", ":").replace(",", ":");
            if (s.indexOf(':') > -1) {
                String s1 = s.substring(0, s.indexOf(':'));
                String s2 = s.substring(s.indexOf(':') + 1).replace(':', ',');
                return (int) Math
                        .round((getLong(s1, file, sheet, row, col) * 60 + getDouble(s2, file, sheet, row, col)) * 100);
            }
            return (int) Math.round(getDouble(s, file, sheet, row, col) * 100);
        } catch (TableEntryException re) {
            throw new TableEntryException(
                    I18n.get("EntryErrorDescription", StringTools.getCellName(sheet, row, col), s), file, sheet, row,
                    col);
        }
    }

    private static <T extends ASchwimmer> void identifyIndex(AWettkampf<T> wk, int[] indizes, String title, int x) {
        if (title.equalsIgnoreCase(I18n.get("Name").toLowerCase())) {
            indizes[NAME] = x;
            return;
        }
        if (title.equalsIgnoreCase(I18n.get("Surname").toLowerCase())) {
            indizes[NACHNAME] = x;
            return;
        }
        if (title.equalsIgnoreCase("Lastname")) {
            indizes[NACHNAME] = x;
            return;
        }
        if (title.equalsIgnoreCase(I18n.get("FirstName"))) {
            indizes[VORNAME] = x;
            return;
        }
        if (title.equalsIgnoreCase("FirstName")) {
            indizes[VORNAME] = x;
            return;
        }
        if (title.equals(I18n.get("Points").toLowerCase())) {
            indizes[PUNKTE1] = x;
            return;
        }
        if (title.equals(I18n.get("ReportedPoints").toLowerCase())) {
            indizes[PUNKTE1] = x;
            return;
        }
        if (title.equals("meldepunkte")) {
            indizes[PUNKTE1] = x;
            return;
        }
        if (title.equals(I18n.get("PointsNr", "A").toLowerCase())) {
            indizes[PUNKTE1] = x;
            return;
        }
        if (title.equals(I18n.get("ReportedPointsNr", "A").toLowerCase())) {
            indizes[PUNKTE1] = x;
            return;
        }
        if (title.equals(I18n.get("PointsNr", "B").toLowerCase())) {
            indizes[PUNKTE2] = x;
            return;
        }
        if (title.equals(I18n.get("PointsWithZW", wk.getRegelwerk().getZusatzwertung()).toLowerCase())) {
            indizes[PUNKTE1] = x;
            return;
        }
        if (title.equals(I18n.get("PointsWithZW", wk.getRegelwerk().getZusatzwertungShort()).toLowerCase())) {
            indizes[PUNKTE1] = x;
            return;
        }
        if (title.equals(I18n.get("ReportedPointsWithZW", wk.getRegelwerk().getZusatzwertung()).toLowerCase())) {
            indizes[PUNKTE1] = x;
            return;
        }
        if (title.equals(I18n.get("ReportedPointsWithZW", wk.getRegelwerk().getZusatzwertungShort()).toLowerCase())) {
            indizes[PUNKTE1] = x;
            return;
        }
        if (title.equals(I18n.get("PointsWithoutZW", wk.getRegelwerk().getZusatzwertung()).toLowerCase())) {
            indizes[PUNKTE2] = x;
            return;
        }
        if (title.equals(I18n.get("PointsWithoutZW", wk.getRegelwerk().getZusatzwertungShort()).toLowerCase())) {
            indizes[PUNKTE2] = x;
            return;
        }
        if (title.equals(I18n.get("ReportedPointsWithoutZW", wk.getRegelwerk().getZusatzwertung()).toLowerCase())) {
            indizes[PUNKTE2] = x;
            return;
        }
        if (title
                .equals(I18n.get("ReportedPointsWithoutZW", wk.getRegelwerk().getZusatzwertungShort()).toLowerCase())) {
            indizes[PUNKTE2] = x;
            return;
        }
        if (title.equals(I18n.get("ReportedPointsNr", "B").toLowerCase())) {
            indizes[PUNKTE2] = x;
            return;
        }
        // For compatibility reasons
        if (title.equals("HLW")) {
            indizes[ZW] = x;
            return;
        }
        if (title.equals(wk.getRegelwerk().getZusatzwertungShort().toLowerCase())) {
            indizes[ZW] = x;
            return;
        }
        if (title.equals(wk.getRegelwerk().getZusatzwertung().toLowerCase())) {
            indizes[ZW] = x;
            return;
        }
        if (title.equals(I18n.get("StartunterlagenkontrolleShort").toLowerCase())
                || title.equals(I18n.get("Startunterlagenkontrolle").toLowerCase())
                || title.equals(I18n.get("Startunterlagen").toLowerCase())) {
            indizes[STARTPASS] = x;
            return;
        }
        if (title.equals(I18n.get("Comment").toLowerCase())) {
            indizes[BEMERKUNG] = x;
            return;
        }
        if (title.equals(I18n.get("Organisation").toLowerCase())) {
            indizes[GLIEDERUNG] = x;
            return;
        }
        if (title.equals("organisation")) {
            indizes[GLIEDERUNG] = x;
            return;
        }
        if (title.equals("verein")) {
            indizes[GLIEDERUNG] = x;
            return;
        }
        if (title.equals(I18n.get("Club").toLowerCase())) {
            indizes[GLIEDERUNG] = x;
            return;
        }
        if (title.equals("gld")) {
            indizes[GLIEDERUNG] = x;
            return;
        }
        if (title.equals(I18n.get("Startberechtigung").toLowerCase())) {
            indizes[QUALI] = x;
            return;
        }
        if (title.equals(I18n.get("Startberechtigt").toLowerCase())) {
            indizes[QUALI] = x;
            return;
        }
        if (title.equals(I18n.get("Qualification").toLowerCase())) {
            indizes[QUALI] = x;
            return;
        }
        if (title.equals(I18n.get("Qualified").toLowerCase())) {
            indizes[QUALI] = x;
            return;
        }
        if (title.equals(I18n.get("QualifiedShort").toLowerCase())) {
            indizes[QUALI] = x;
            return;
        }
        if (title.equals(I18n.get("AgeGroupShort").toLowerCase())) {
            if (indizes[ALTERSKLASSE] < 0) {
                indizes[ALTERSKLASSE] = x;
                return;
            }
        }
        if (title.equals(I18n.get("AgeGroup").toLowerCase())) {
            indizes[ALTERSKLASSE] = x;
            return;
        }
        if (title.equals("alters-klasse")) {
            indizes[ALTERSKLASSE] = x;
            return;
        }
        if (title.equals(I18n.get("Members").toLowerCase())) {
            indizes[MEMBERS] = x;
            return;
        }
        if (title.equals("sheetname")) {
            if (indizes[ALTERSKLASSE] < 0) {
                indizes[ALTERSKLASSE] = x;
                return;
            }
        }
        if (title.equals(I18n.get("Sex").toLowerCase())) {
            indizes[GESCHLECHT] = x;
            return;
        }
        if (title.equals("gender".toLowerCase())) {
            indizes[GESCHLECHT] = x;
            return;
        }
        if (title.equals("m/w")) {
            indizes[GESCHLECHT] = x;
            return;
        }
        if (title.equals("(m/w)")) {
            indizes[GESCHLECHT] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirth").toLowerCase())) {
            indizes[JAHRGANG] = x;
            return;
        }
        if (title.equals("YearOfBirth".toLowerCase())) {
            indizes[JAHRGANG] = x;
            return;
        }
        if (title.equals("jg")) {
            indizes[JAHRGANG] = x;
            return;
        }
        if (title.equals("jahr-gang")) {
            indizes[JAHRGANG] = x;
            return;
        }
        if (title.equals("jahr- gang")) {
            indizes[JAHRGANG] = x;
            return;
        }
        if (title.equals("a.k.")) {
            indizes[AUSSER_KONKURRENZ] = x;
            return;
        }
        if (title.equals("a. k.")) {
            indizes[AUSSER_KONKURRENZ] = x;
            return;
        }
        if (title.equals(I18n.get("AusserKonkurrenz").toLowerCase())) {
            indizes[AUSSER_KONKURRENZ] = x;
            return;
        }
        if (title.equals(I18n.get("Startnumber").toLowerCase())) {
            indizes[STARTNUMMER] = x;
            return;
        }
        if (title.equalsIgnoreCase("Startnumber")) {
            indizes[STARTNUMMER] = x;
            return;
        }
        if (title.equals(I18n.get("StartnumberShort").toLowerCase())) {
            indizes[STARTNUMMER] = x;
            return;
        }
        if (title.equals("s#")) {
            indizes[STARTNUMMER] = x;
            return;
        }
        if (title.equals("#")) {
            indizes[STARTNUMMER] = x;
            return;
        }
        if (title.equals("sn")) {
            indizes[STARTNUMMER] = x;
            return;
        }
        if (title.equals(I18n.get("Disciplines").toLowerCase())) {
            indizes[DISCIPLINES] = x;
            return;
        }
        if (title.equals(I18n.get("Meldezeiten").toLowerCase())) {
            indizes[MELDEZEITEN] = x;
            return;
        }
        if (title.equals(I18n.get("Category").toLowerCase())) {
            indizes[CATEGORY] = x;
            return;
        }
        if (title.equals(I18n.get("Position").toLowerCase())) {
            indizes[POSITION] = x;
            return;
        }
        if (title.equals(I18n.get("Level").toLowerCase())) {
            indizes[STUFE] = x;
            return;
        }
        if (title.equals(I18n.get("Qualifikationsebene").toLowerCase())) {
            indizes[QUALI_LEVEL] = x;
            return;
        }
        if (title.equals(I18n.get("LandesverbandShort").toLowerCase())) {
            indizes[QUALI_LEVEL] = x;
            return;
        }
        if (title.equals(I18n.get("Landesverband").toLowerCase())) {
            indizes[QUALI_LEVEL] = x;
            return;
        }
        if (title.equals("gld2".toLowerCase())) {
            indizes[QUALI_LEVEL] = x;
            return;
        }
        if (title.equals("q-gld".toLowerCase())) {
            indizes[QUALI_LEVEL] = x;
            return;
        }
        if (title.equals("lv".toLowerCase())) {
            indizes[QUALI_LEVEL] = x;
            return;
        }
        if (title.equals((I18n.get("Organisation") + " 2").toLowerCase())) {
            indizes[QUALI_LEVEL] = x;
            return;
        }
        if (title.equals((I18n.get("Club") + " 2").toLowerCase())) {
            indizes[QUALI_LEVEL] = x;
            return;
        }
        if (title.equals(I18n.get("Qualification").toLowerCase())) {
            indizes[QUALI] = x;
            return;
        }
        if (title.equals("Quali".toLowerCase())) {
            indizes[QUALI] = x;
            return;
        }
        if (title.equals(I18n.get("Protocol").toLowerCase())) {
            indizes[PROTOCOL1] = x;
            return;
        }
        if (title.equals(I18n.get("ProtocolNr", "A").toLowerCase())) {
            indizes[PROTOCOL1] = x;
            return;
        }
        if (title.equals(I18n.get("ProtocolNr", "B").toLowerCase())) {
            indizes[PROTOCOL2] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "1").toLowerCase())) {
            indizes[NACHNAME1] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "1").toLowerCase())) {
            indizes[VORNAME1] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "1").toLowerCase())) {
            indizes[GESCHLECHT1] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "1").toLowerCase())) {
            indizes[JAHRGANG1] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "2").toLowerCase())) {
            indizes[NACHNAME2] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "2").toLowerCase())) {
            indizes[VORNAME2] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "2").toLowerCase())) {
            indizes[GESCHLECHT2] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "2").toLowerCase())) {
            indizes[JAHRGANG2] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "3").toLowerCase())) {
            indizes[NACHNAME3] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "3").toLowerCase())) {
            indizes[VORNAME3] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "3").toLowerCase())) {
            indizes[GESCHLECHT3] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "3").toLowerCase())) {
            indizes[JAHRGANG3] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "4").toLowerCase())) {
            indizes[NACHNAME4] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "4").toLowerCase())) {
            indizes[VORNAME4] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "4").toLowerCase())) {
            indizes[GESCHLECHT4] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "4").toLowerCase())) {
            indizes[JAHRGANG4] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "5").toLowerCase())) {
            indizes[NACHNAME5] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "5").toLowerCase())) {
            indizes[VORNAME5] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "5").toLowerCase())) {
            indizes[GESCHLECHT5] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "5").toLowerCase())) {
            indizes[JAHRGANG5] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "6").toLowerCase())) {
            indizes[NACHNAME6] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "6").toLowerCase())) {
            indizes[VORNAME6] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "6").toLowerCase())) {
            indizes[GESCHLECHT6] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "6").toLowerCase())) {
            indizes[JAHRGANG6] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "7").toLowerCase())) {
            indizes[NACHNAME7] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "7").toLowerCase())) {
            indizes[VORNAME7] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "7").toLowerCase())) {
            indizes[GESCHLECHT7] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "7").toLowerCase())) {
            indizes[JAHRGANG7] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "8").toLowerCase())) {
            indizes[NACHNAME8] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "8").toLowerCase())) {
            indizes[VORNAME8] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "8").toLowerCase())) {
            indizes[GESCHLECHT8] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "8").toLowerCase())) {
            indizes[JAHRGANG8] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "9").toLowerCase())) {
            indizes[NACHNAME9] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "9").toLowerCase())) {
            indizes[VORNAME9] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "9").toLowerCase())) {
            indizes[GESCHLECHT9] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "9").toLowerCase())) {
            indizes[JAHRGANG9] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "10").toLowerCase())) {
            indizes[NACHNAME10] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "10").toLowerCase())) {
            indizes[VORNAME10] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "10").toLowerCase())) {
            indizes[GESCHLECHT10] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "10").toLowerCase())) {
            indizes[JAHRGANG10] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "11").toLowerCase())) {
            indizes[NACHNAME11] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "11").toLowerCase())) {
            indizes[VORNAME11] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "11").toLowerCase())) {
            indizes[GESCHLECHT11] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "11").toLowerCase())) {
            indizes[JAHRGANG11] = x;
            return;
        }
        if (title.equals(I18n.get("SurnameNr", "12").toLowerCase())) {
            indizes[NACHNAME12] = x;
            return;
        }
        if (title.equals(I18n.get("FirstnameNr", "12").toLowerCase())) {
            indizes[VORNAME12] = x;
            return;
        }
        if (title.equals(I18n.get("SexNr", "12").toLowerCase())) {
            indizes[GESCHLECHT12] = x;
            return;
        }
        if (title.equals(I18n.get("YearOfBirthNr", "12").toLowerCase())) {
            indizes[JAHRGANG12] = x;
            return;
        }
        if (title.equals(I18n.get("Discipline").toLowerCase())) {
            indizes[DISCIPLINE] = x;
            return;
        }
        if (title.equals("discipline".toLowerCase())) {
            indizes[DISCIPLINE] = x;
            return;
        }
    }

    public static String indizesToNames(int[] indizes, String spacer) {
        StringBuilder sb = new StringBuilder();
        sb.append(spacer);
        for (int x = 0; x < indizes.length; x++) {
            if (x > 0) {
                sb.append(", ");
            }
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

}
