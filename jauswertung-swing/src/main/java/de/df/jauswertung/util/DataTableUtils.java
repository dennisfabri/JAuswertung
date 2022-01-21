/*
 * Export.java Created on 3. Oktober 2002, 12:52
 */

package de.df.jauswertung.util;

import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static de.df.jauswertung.daten.PropertyConstants.ZW_LANES;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.LEFT;
import static javax.swing.SwingConstants.RIGHT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.SwingConstants;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.kampfrichter.KampfrichterEinheit;
import de.df.jauswertung.daten.kampfrichter.KampfrichterStufe;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.laufliste.HLWLauf;
import de.df.jauswertung.daten.laufliste.HLWListe;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startunterlagen;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jauswertung.util.data.Heattime;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jauswertung.util.ergebnis.ResultCalculator;
import de.df.jauswertung.util.ergebnis.SchwimmerData;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jauswertung.util.valueobjects.ZWStartkarte;
import de.df.jauswertung.util.vergleicher.SchwimmerAKVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerGeschlechtVergleicher;
import de.df.jauswertung.util.vergleicher.SchwimmerNameVergleicher;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.ExtendedTableModel.TitleCell;
import de.df.jutils.gui.jtable.ExtendedTableModel.TitleRow;
import de.df.jutils.io.csv.CsvManager;
import de.df.jutils.io.csv.FixedDecimal;
import de.df.jutils.io.csv.Seconds;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.NullFeedback;
import de.df.jutils.util.StringTools;

/**
 * @author dennis
 */
public final class DataTableUtils {

    public enum RegistrationDetails {
        SHORT, WITH_TIMES, EVERYTHING, SHORT_WITH_TEAMMEMBERS
    }

    private DataTableUtils() {
        // Nothing to do
    }

    static final class DataContainer {
        private LinkedList<Integer>   aligns  = new LinkedList<Integer>();
        private LinkedList<String>    formats = new LinkedList<String>();
        private LinkedList<Object>    titles  = new LinkedList<Object>();
        private LinkedList<TitleCell> row1    = new LinkedList<TitleCell>();
        private LinkedList<TitleCell> row2    = new LinkedList<TitleCell>();

        public void add(String title, int align, String format, String r, boolean mode) {
            add(title, null, align, format, r, mode);
        }

        public void add(String title, Object info, int align, String format, String r, boolean mode) {
            addI(I18n.get(title, info), align, format, I18n.get(r), mode);
        }

        public void addNoTranslate(String title, int align, String format, String r, boolean mode) {
            addI(title, align, format, r, mode);
        }

        public void addI(String title, int align, String format, String r, boolean mode) {
            titles.addLast(title);
            aligns.addLast(align);
            formats.addLast(format);
            if (mode) {
                row1.addLast(new TitleCell(r, 1, 2));
            } else {
                row2.addLast(new TitleCell(r, 1, 1));
            }
        }

        public void addTitle(String t, int span) {
            addTitle(t, null, span);
        }

        public void addTitleI(String t, int span) {
            row1.addLast(new TitleCell(t, span, 1));
        }

        public void addTitle(String t, Object info, int span) {
            addTitleI(I18n.get(t, info), span);
        }

        public TitleRow[] getExtendedTitles() {
            return new TitleRow[] { new TitleRow(row1.toArray(new TitleCell[row1.size()])), new TitleRow(row2.toArray(new TitleCell[row2.size()])) };
        }

        public String[] getTitles() {
            return titles.toArray(new String[titles.size()]);
        }

        public String[] getFormats() {
            return formats.toArray(new String[formats.size()]);
        }

        public Integer[] getAlignments() {
            return aligns.toArray(new Integer[aligns.size()]);
        }
    }

    public static String detectMitglieder(String[] titles, Object[] data) {
        String[] dat = new String[data.length];
        for (int x = 0; x < data.length; x++) {
            if (data[x] != null) {
                dat[x] = data[x].toString();
            } else {
                dat[x] = "";
            }
        }
        return detectMitglieder(titles, dat);
    }

    public static int identify(String[] titles, String title) {
        for (int x = 0; x < titles.length; x++) {
            if (titles[x].equalsIgnoreCase(title)) {
                return x;
            }
        }
        return -1;
    }

    public static String detectMitglieder(String[] titles, String[] data) {
        LinkedList<Integer> index = new LinkedList<Integer>();
        boolean found = true;
        while (found) {
            found = false;
            String line = I18n.get("MemberNr", index.size() + 1);
            for (int x = 0; x < titles.length; x++) {
                if (titles[x].equalsIgnoreCase(line)) {
                    index.addLast(x);
                    found = true;
                    break;
                }
            }
        }
        if (index.size() == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        ListIterator<Integer> li = index.listIterator();
        boolean first = true;
        while (li.hasNext()) {
            String name = data[li.next()].trim().replace(';', ',');
            if (name.length() > 0) {
                if (first) {
                    first = false;
                } else {
                    result.append(";");
                }
                result.append(name);
            }
        }
        return result.toString();
    }

    public static <T extends ASchwimmer> ExtendedTableModel results(AWettkampf<T> wk, boolean removeUnranked, Feedback fb) {
        if (wk == null) {
            return null;
        }
        if (fb == null) {
            fb = new NullFeedback();
        }
        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;
        boolean einzel = (w instanceof EinzelWettkampf);

        int maxteammembers = getMaximumMembers(wk, wk.getSchwimmer());

        Regelwerk aks = wk.getRegelwerk();
        int diszAnzahl = 0;
        for (int x = 0; x < aks.size(); x++) {
            diszAnzahl = Math.max(diszAnzahl, aks.getAk(x).getDiszAnzahl());
        }

        DataContainer dc = new DataContainer();

        dc.add("Rank", RIGHT, "0", "Rank", true);
        dc.add("Name", LEFT, "", "Name", true);
        if (einzel) {
            dc.add("Surname", LEFT, "", "Surname", true);
            dc.add("FirstName", LEFT, "", "FirstName", true);
            dc.add("YearOfBirth", RIGHT, "00", "YearOfBirth", true);
        } else {
            for (int x = 0; x < maxteammembers; x++) {
                dc.add("MemberNr", (x + 1), LEFT, "", "Member", true);
            }
        }
        dc.add("Organisation", LEFT, "", "Organisation", true);
        dc.add("Qualifikationsebene", LEFT, "", "Qualifikationsebene", true);
        dc.add("AgeGroup", LEFT, "", "AgeGroup", true);
        dc.add("Sex", LEFT, "", "Sex", true);
        dc.add("Comment", LEFT, "", "Comment", true);
        dc.add("ReportedPoints", RIGHT, "0.00", "ReportedPoints", true);
        dc.add("Points", RIGHT, "0.00", "Points", true);
        dc.add("Difference", RIGHT, "0.00", "Difference", true);
        dc.add("Startnumber", RIGHT, "0", "Startnumber", true);
        dc.add("AusserKonkurrenz", CENTER, "", "AusserKonkurrenz", true);
        dc.add("Disciplines", LEFT, "", "Disciplines", true);
        for (int x = 1; x <= diszAnzahl; x++) {
            dc.addTitle("DisciplineNumber", x, 4);

            dc.add("RankNr", x, RIGHT, "0", "Rank", false);
            dc.add("TimeNr", x, RIGHT, "mm:ss.00", "Time", false);
            dc.add("PointsNr", x, RIGHT, "0.00", "Points", false);
            dc.add("PenaltyNr", x, RIGHT, "0", "Penalty", false);
        }
        dc.addNoTranslate(wk.getRegelwerk().getZusatzwertungShort(), RIGHT, "0", wk.getRegelwerk().getZusatzwertungShort(), true);
        if (wk.isHeatBased()) {
            dc.add("Qualified", RIGHT, "", "Qualified", true);
        }

        LinkedList<Object[]> result = new LinkedList<Object[]>();

        for (int x = 0; x < aks.size(); x++) {
            LinkedList<T> teilies = SearchUtils.getSchwimmer(wk, aks.getAk(x));
            if ((teilies != null) && (teilies.size() > 0)) {
                fb.showFeedback(aks.getAk(x).getName());
                if (aks.getAk(x).hasMehrkampfwertung()) {
                    altersklasse(result, wk, aks.getAk(x), false, diszAnzahl, maxteammembers, false, true, true, removeUnranked, 0);
                    altersklasse(result, wk, aks.getAk(x), true, diszAnzahl, maxteammembers, false, true, true, removeUnranked, 0);
                }

                if (wk.isHeatBased()) {
                    if (wk.isHeatBased()) {
                        for (OWDisziplin<T> y : wk.getLauflisteOW().getDisziplinen()) {
                            Altersklasse ak = wk.getRegelwerk().getAk(y.akNummer);
                            int qualified = 0;
                            int[] runden = ak.getDisziplin(y.disziplin, y.maennlich).getRunden();
                            boolean isFinal = runden.length <= y.round;
                            if (!isFinal) {
                                qualified = runden[y.round];
                            }

                            AWettkampf<T> wkl = ResultUtils.createCompetitionFor(wk,
                                    new OWSelection(wk.getRegelwerk().getAk(y.akNummer), y.akNummer, y.maennlich, y.disziplin, y.round, isFinal));

                            if (wkl.getRegelwerk().getAk(x).hasEinzelwertung()) {
                                AWettkampf<T> ew = ResultUtils.generateEinzelwertungswettkampf(wk, x, false);
                                // if (!isFinal) {
                                // ew.getRegelwerk().setFormelID(FormelILS.ID);
                                // }
                                if (ew != null) {
                                    for (int i = 0; i < wkl.getRegelwerk().size(); i++) {
                                        Altersklasse a = wkl.getRegelwerk().getAk(i);
                                        for (Disziplin[] dx : a.getDisziplinen()) {
                                            for (Disziplin d : dx) {
                                                d.setName(d.getName());
                                            }
                                        }
                                        if (a.getDiszAnzahl() == 0) {
                                            break;
                                        }
                                        // isFinal = y.round >= ak.getDisziplin(y.disziplin, y.maennlich).getRunden().length;
                                        String runde = " - " + I18n.getRound(y.round, isFinal);
                                        a.setName(aks.getAk(i).getName() + " - " + a.getDisziplin(0, true).getName() + runde);

                                        altersklasse(result, wkl, a, false, diszAnzahl, maxteammembers, false, true, true, removeUnranked, qualified);
                                        altersklasse(result, wkl, a, true, diszAnzahl, maxteammembers, false, true, true, removeUnranked, qualified);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (aks.getAk(x).hasEinzelwertung()) {
                        AWettkampf<T> ew = ResultUtils.generateEinzelwertungswettkampf(wk, x, false);
                        if (ew != null) {
                            for (int y = 0; y < ew.getRegelwerk().size(); y++) {
                                Altersklasse a = ew.getRegelwerk().getAk(y);
                                a.setName(aks.getAk(x).getName() + " - " + a.getDisziplin(0, true).getName());
                                altersklasse(result, ew, a, false, diszAnzahl, maxteammembers, false, true, true, removeUnranked, 0);
                                altersklasse(result, ew, a, true, diszAnzahl, maxteammembers, false, true, true, removeUnranked, 0);
                            }
                        }
                    }
                }
            }
        }

        wk = CompetitionUtils.generateWertungsgruppenwettkampf(wk);
        if (wk != null) {
            aks = wk.getRegelwerk();
            for (int x = 0; x < aks.size(); x++) {
                LinkedList<T> teilies = SearchUtils.getSchwimmer(wk, aks.getAk(x));
                if ((teilies != null) && (teilies.size() > 0)) {
                    fb.showFeedback(aks.getAk(x).getName());
                    if (aks.getAk(x).hasMehrkampfwertung()) {
                        altersklasse(result, wk, aks.getAk(x), false, diszAnzahl, maxteammembers, false, true, true, removeUnranked, 0);
                        altersklasse(result, wk, aks.getAk(x), true, diszAnzahl, maxteammembers, false, true, true, removeUnranked, 0);
                    }

                    if (aks.getAk(x).hasEinzelwertung()) {
                        AWettkampf<T> ew = ResultUtils.generateEinzelwertungswettkampf(wk, x, false);
                        if (ew != null) {
                            for (int y = 0; y < ew.getRegelwerk().size(); y++) {
                                Altersklasse a = ew.getRegelwerk().getAk(y);
                                a.setName(aks.getAk(x).getName() + " - " + a.getDisziplin(0, true).getName());
                                altersklasse(result, ew, a, false, diszAnzahl, maxteammembers, false, true, true, removeUnranked, 0);
                                altersklasse(result, ew, a, true, diszAnzahl, maxteammembers, false, true, true, removeUnranked, 0);
                            }
                        }
                    }
                }
            }
        }

        ExtendedTableModel etm = new ExtendedTableModel(result.toArray(new Object[0][0]), dc.getTitles());
        etm.setName(I18n.get("Results"));
        etm.setColumnAlignments(dc.getAlignments());
        etm.setColumnFormats(dc.getFormats());
        etm.setLandscape(true);
        etm.setExtendedTitles(dc.getExtendedTitles());
        return etm;
    }

    public static <T extends ASchwimmer> ExtendedTableModel results(AWettkampf<T> wk, Altersklasse ak, boolean male, boolean removeUnranked, int qualified) {
        if (wk == null) {
            return null;
        }
        if (!ak.hasMehrkampfwertung()) {
            return null;
        }
        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;
        boolean einzel = (w instanceof EinzelWettkampf);

        int diszAnzahl = ak.getDiszAnzahl();

        DataContainer dc = new DataContainer();

        boolean showpoints = diszAnzahl > 1;

        boolean fullSizeColumns = true;
        if (!showpoints) {
            int columncount = 5 + (einzel ? 3 : 0) + (ak.hasHLW() ? 1 : 0) + (qualified > 0 ? 1 : 0);

            fullSizeColumns = false;
            Disziplin d = ak.getDisziplin(0, male);
            String name = d.getName();
            dc.addTitleI(name, columncount);
        }

        dc.add("RankShort", RIGHT, "0", "RankShort", fullSizeColumns);
        if (einzel) {
            dc.add("Surname", LEFT, "", "Surname", fullSizeColumns);
            dc.add("FirstName", LEFT, "", "FirstName", fullSizeColumns);
            dc.add("YearOfBirthShort", RIGHT, "00", "YearOfBirthShort", fullSizeColumns);
        } else {
            dc.add("Name", LEFT, "", "Name", fullSizeColumns);
        }
        dc.add("Organisation", LEFT, "", "Organisation", fullSizeColumns);
        dc.add("QualifikationsebeneShort", LEFT, "", "QualifikationsebeneShort", fullSizeColumns);
        // dc.add("Comment", LEFT, "", "Comment", true);
        // dc.add("ReportedPoints", RIGHT, "0.00", "ReportedPoints", true);
        if (showpoints) {
            dc.add("Points", RIGHT, "0.00", "Points", fullSizeColumns);
            dc.add("Difference", RIGHT, "0.00", "Difference", fullSizeColumns);
        }
        // dc.add("Startnumber", RIGHT, "0", "Startnumber", true);
        // dc.add("AusserKonkurrenz", CENTER, "", "AusserKonkurrenz", true);
        // dc.add("Disciplines", LEFT, "", "Disciplines", true);
        for (int x = 1; x <= diszAnzahl; x++) {
            Disziplin d = ak.getDisziplin(x - 1, male);
            String name = d.getName();
            if (showpoints) {
                dc.addTitleI(name, 2 + (showpoints ? 2 : 0));
            }

            if (showpoints) {
                dc.add("RankNr", x, RIGHT, "0", "Rank", false);
            }
            dc.add("TimeNr", x, RIGHT, "mm:ss.00", "Time", false);
            if (showpoints) {
                dc.add("PointsNr", x, RIGHT, "0.00", "Points", false);
            }
            dc.add("PenaltyNr", x, RIGHT, "0", "Penalty", false);
        }
        if (ak.hasHLW()) {
            dc.add(wk.getRegelwerk().getZusatzwertungShort(), RIGHT, "0", wk.getRegelwerk().getZusatzwertungShort(), fullSizeColumns);
        }
        if (qualified > 0) {
            dc.add("Qualified", CENTER, "", "Qualified", fullSizeColumns);
        }

        LinkedList<Object[]> result = new LinkedList<Object[]>();

        altersklasse(result, wk, ak, male, diszAnzahl, 0, true, showpoints, true, removeUnranked, qualified);

        ExtendedTableModel etm = new ExtendedTableModel(result.toArray(new Object[0][0]), dc.getTitles());
        etm.setName(ak.getName() + " " + I18n.geschlechtToString(wk.getRegelwerk(), male));
        etm.setColumnAlignments(dc.getAlignments());
        etm.setColumnFormats(dc.getFormats());
        etm.setLandscape(ak.getDiszAnzahl() > 1);
        etm.setExtendedTitles(dc.getExtendedTitles());
        return etm;
    }

    public static <T extends ASchwimmer> ExtendedTableModel resultsEinzelwertung(AWettkampf<T> wk, Altersklasse ak, boolean male, int disz,
            boolean removeUnranked, int qualified) {
        if (wk == null) {
            return null;
        }
        if (ak == null) {
            return null;
        }
        if (!ak.hasEinzelwertung()) {
            return null;
        }

        int index = wk.getRegelwerk().indexOf(ak);
        AWettkampf<T> w = ResultUtils.generateEinzelwertungswettkampf(wk, index, removeUnranked);
        if (w == null) {
            return null;
        }
        if (!w.hasSchwimmer()) {
            return null;
        }
        Altersklasse a = w.getRegelwerk().getAk(disz);
        a.setName(a.getName() + " " + I18n.geschlechtToShortString(w.getRegelwerk(), male) + " - " + a.getDisziplin(0, male).getName());
        ExtendedTableModel etm = results(w, a, male, removeUnranked, qualified);
        if (etm == null) {
            return null;
        }
        if (etm.getRowCount() <= 0) {
            return null;
        }
        etm.setLandscape(false);
        etm.setName(a.getName());
        return etm;
    }

    private static synchronized <T extends ASchwimmer> void altersklasse(LinkedList<Object[]> result, AWettkampf<T> wk, Altersklasse ak, boolean maennlich,
            int maxdisz, int maxteammembers, boolean separate, boolean mustShowPoints, boolean zw, boolean removeUnranked, int qualified) {
        if (maxdisz < ak.getDiszAnzahl()) {
            maxdisz = ak.getDiszAnzahl();
        }

        SchwimmerResult<T>[] results = ResultCalculator.getResults(wk, ak, maennlich, null, zw);
        if (results.length == 0) {
            return;
        }

        double points = results[0].getPoints();
        for (SchwimmerResult<T> result1 : results) {
            if ((!removeUnranked) || (!result1.hasKeineWertung())) {
                Object[] row = platzToLine(result1, maxdisz, maxteammembers, points, separate, mustShowPoints, qualified);
                if (qualified > 0) {
                    boolean isQualified = result1.getPlace() <= qualified;
                    boolean hasWithdrawn = result1.getStrafe().isWithdraw();
                    if (isQualified && hasWithdrawn) {
                        qualified++;
                    }
                }
                result.addLast(row);
            }
        }
    }

    /**
     * @param platz
     * @param t
     * @param ak
     * @param maxdisz
     * @param einzel
     * @return
     */
    private static <T extends ASchwimmer> Object[] platzToLine(SchwimmerResult<T> line, int maxdisz, int maxteammembers, double points, boolean separate,
            boolean mustShowPoints, int qualified) {
        ASchwimmer t = line.getSchwimmer();
        LinkedList<Object> row = new LinkedList<Object>();
        if (line.hasKeineWertung()) {
            row.addLast("");
        } else {
            row.addLast(line.getPlace());
        }
        if ((!(t instanceof Teilnehmer)) || (!separate)) {
            row.addLast(t.getName());
        }

        boolean showpoints = mustShowPoints || (t.getAK().getDiszAnzahl() > 1);

        if (t instanceof Teilnehmer) {
            Teilnehmer teili = (Teilnehmer) t;
            row.addLast(teili.getNachname());
            row.addLast(teili.getVorname());
            row.addLast(I18n.yearToShortObject(teili.getJahrgang()));
        }
        if (t instanceof Mannschaft) {
            Mannschaft m = (Mannschaft) t;
            for (int x = 0; x < maxteammembers; x++) {
                row.addLast(m.getMitgliedsname(x));
            }
        }

        row.addLast(t.getGliederung());
        row.addLast(t.getQualifikationsebene());
        if (!separate) {
            row.addLast(t.getAK().toString());
            row.addLast(I18n.geschlechtToString(t));
            row.addLast(line.getSchwimmer().getBemerkung());
            row.addLast(t.getMeldepunkte(0));
        }
        if (showpoints) {
            if (line.hasKeineWertung()) {
                row.addLast("");
                row.addLast("");
            } else {
                row.addLast(line.getPoints());
                row.addLast(Math.abs(line.getPoints() - points));
            }
        }
        if (!separate) {
            row.addLast(StartnumberFormatManager.format(t));
            row.addLast(I18n.booleanToYesNo(t.isAusserKonkurrenz()));
            row.addLast(line.getSchwimmer().getDisciplineChoiceAsString(CsvManager.getSeparator()));
        }
        SchwimmerData<T>[] daten = line.getResults();
        for (int x = 0; x < daten.length; x++) {
            if (!line.getSchwimmer().isDisciplineChosen(x)) {
                row.addLast("");
                row.addLast("");
                if (showpoints) {
                    row.addLast("");
                    row.addLast("");
                }
            } else {
                if (showpoints) {
                    if (daten[x].getRank() <= 0) {
                        row.addLast("");
                    } else {
                        row.addLast(daten[x].getRank());
                    }
                }
                if (daten[x].getTime() <= 0) {
                    row.addLast("");
                } else {
                    row.addLast(new Seconds(daten[x].getTime()));
                }
                if (showpoints) {
                    row.addLast(new FixedDecimal(daten[x].getPoints(), 2));
                }
                row.addLast(PenaltyUtils.getPenaltyShortText(daten[x].getStrafe(), daten[x].getSchwimmer().getAK()));
            }
        }
        for (int x = daten.length; x < maxdisz; x++) {
            row.addLast("");
            row.addLast("");
            if (showpoints) {
                row.addLast("");
                row.addLast("");
            }
        }
        if ((!separate) || t.getAK().hasHLW()) {
            if (t.hasHLWSet()) {
                row.addLast(t.getHLWPunkte());
            } else {
                row.addLast("");
            }
        }
        if (qualified > 0) {
            int place = line.getPlace();
            boolean isQualified = place <= qualified && place > 0 && !line.hasKeineWertung() && !line.getSchwimmer().isAusserKonkurrenz() && daten.length != 1
                    && (line.getStrafe() == null || !line.getStrafe().isWithdraw());
            if (isQualified) {
                isQualified = daten[0].getRank() > 0 && daten[0].getTime() > 0 && daten[0].getPoints() > 0;
            }
            row.addLast(isQualified ? "Q" : " ");
        }
        return row.toArray();
    }

    public static <T extends ASchwimmer> ExtendedTableModel registration(AWettkampf<T> wk, Feedback fb) {
        return registration(wk, null, RegistrationDetails.EVERYTHING, null, true, fb);
    }

    private static Object[] remove(Object[] source, boolean[] columns) {
        if (columns == null) {
            return source;
        }
        LinkedList<Object> l = new LinkedList<Object>();
        for (int x = 0; x < Math.min(columns.length, source.length); x++) {
            if (columns[x]) {
                l.addLast(source[x]);
            }
        }
        for (int x = columns.length; x < source.length; x++) {
            l.addLast(source[x]);
        }
        return l.toArray();
    }

    private static <T extends Object> void remove(LinkedList<T> liste, boolean[] columns) {
        if (columns == null) {
            return;
        }
        for (int x = columns.length - 1; x >= 0; x--) {
            if (!columns[x]) {
                liste.remove(x);
            }
        }
    }

    public static KampfrichterStufe getLevel(String s) {
        s = s.replace(" ", "").replace(";", "").replace("&", "").replace(",", "").replace("/", "").toUpperCase();

        if (s.equals("") || s.equals("-")) {
            return KampfrichterStufe.KEINE;
        }

        KampfrichterStufe ks = KampfrichterStufe.KEINE;
        if (s.indexOf("D123") >= 0) {
            ks = ks.mit(KampfrichterStufe.D123);
            s = s.replace("D123", "");
        }
        if (s.indexOf("D12") >= 0) {
            ks = ks.mit(KampfrichterStufe.D12);
            s = s.replace("D12", "");
        }
        if (s.indexOf("D3") >= 0) {
            ks = ks.mit(KampfrichterStufe.F1D3);
            s = s.replace("D3", "");
        }

        if (s.indexOf("E123") >= 0) {
            ks = ks.mit(KampfrichterStufe.E123);
            s = s.replace("E123", "");
        }
        if (s.indexOf("E13") >= 0) {
            ks = ks.mit(KampfrichterStufe.E13);
            s = s.replace("E13", "");
        }
        if (s.indexOf("E12") >= 0) {
            ks = ks.mit(KampfrichterStufe.E12);
            s = s.replace("E12", "");
        }
        if (s.indexOf("E1") >= 0) {
            ks = ks.mit(KampfrichterStufe.E1);
            s = s.replace("E1", "");
        }
        if (s.indexOf("E3") >= 0) {
            ks = ks.mit(KampfrichterStufe.F1E3);
            s = s.replace("E3", "");
        }

        if (s.indexOf("F1") >= 0) {
            ks = ks.mit(KampfrichterStufe.F1);
            s = s.replace("F1", "");
        }
        if (s.indexOf("E2") >= 0) {
            ks = ks.mit(KampfrichterStufe.E2);
            s = s.replace("E2", "");
        }

        if (s.length() > 0) {
            return null;
        }
        return ks;
    }

    public static <T extends ASchwimmer> ExtendedTableModel teammembers(AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return null;
        }
        if (!(wk instanceof MannschaftWettkampf)) {
            return null;
        }
        if (fb == null) {
            fb = new NullFeedback();
        }

        MannschaftWettkampf w = (MannschaftWettkampf) wk;
        LinkedList<Mannschaft> schwimmer = w.getSchwimmer();

        int maxteamsize = wk.getRegelwerk().getAk(0).getMaxMembers();
        for (int x = 1; x < wk.getRegelwerk().size(); x++) {
            maxteamsize = Math.max(maxteamsize, wk.getRegelwerk().getAk(x).getMaxMembers());
        }

        LinkedList<Integer> aligns = new LinkedList<Integer>();
        LinkedList<String> formats = new LinkedList<String>();
        LinkedList<Object> titles = new LinkedList<Object>();

        LinkedList<Object[]> result = new LinkedList<Object[]>();

        titles.addLast(I18n.get("StartnumberShort"));
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("0");
        titles.addLast(I18n.get("Team"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Organisation"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Qualifikationsebene"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("AgeGroup"));
        aligns.addLast(SwingConstants.CENTER);
        formats.addLast("");

        titles.addLast(I18n.get("Surname"));
        aligns.addLast(SwingConstants.CENTER);
        formats.addLast("");
        titles.addLast(I18n.get("Firstname"));
        aligns.addLast(SwingConstants.CENTER);
        formats.addLast("");
        titles.addLast(I18n.get("YearOfBirth"));
        aligns.addLast(SwingConstants.CENTER);
        formats.addLast("");
        titles.addLast(I18n.get("Sex"));
        aligns.addLast(SwingConstants.CENTER);
        formats.addLast("");

        ListIterator<Mannschaft> li = schwimmer.listIterator();

        int length = schwimmer.size();
        int percent = 0;
        int counter = 0;
        while (li.hasNext()) {
            int per = (counter * 100) / length;
            if (per > percent + 5) {
                percent = per;
                double y = ((double) percent) / 110;
                fb.showFeedback(I18n.get("Percent", y));
            }
            counter++;

            Mannschaft t = li.next();

            for (int i = 0; i < maxteamsize; i++) {
                LinkedList<Object> row = new LinkedList<Object>();

                row.addLast(StartnumberFormatManager.format(t) + StringTools.asText(i));
                row.addLast(t.getName());
                row.addLast(t.getGliederung());
                row.addLast(t.getQualifikationsebene());
                row.addLast(I18n.getAgeGroupAsString(t));

                Mannschaftsmitglied m = t.getMannschaftsmitglied(i);

                row.addLast(m.getNachname());
                row.addLast(m.getVorname());
                row.addLast(m.getJahrgang() > 0 ? m.getJahrgang() : "");
                row.addLast(I18n.getSexShortString(m.getGeschlecht()));

                result.addLast(row.toArray());
            }
        }

        ExtendedTableModel tm = new ExtendedTableModel(result.toArray(new Object[0][0]), titles.toArray());
        tm.setColumnAlignments(aligns.toArray(new Integer[aligns.size()]));
        tm.setColumnFormats(formats.toArray(new String[formats.size()]));
        tm.setName(I18n.get("Teammembers"));
        tm.setLandscape(false);

        return tm;
    }

    public static <T extends ASchwimmer> ExtendedTableModel teammemberssingle(AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return null;
        }
        if (!(wk instanceof MannschaftWettkampf)) {
            return null;
        }
        if (fb == null) {
            fb = new NullFeedback();
        }

        MannschaftWettkampf w = (MannschaftWettkampf) wk;
        LinkedList<Mannschaft> schwimmer = w.getSchwimmer();

        int maxteamsize = 0;
        {
            for (int x = 1; x < wk.getRegelwerk().size(); x++) {
                maxteamsize = Math.max(maxteamsize, wk.getRegelwerk().getAk(x).getMaxMembers());
            }
        }

        LinkedList<Integer> aligns = new LinkedList<Integer>();
        LinkedList<String> formats = new LinkedList<String>();
        LinkedList<Object> titles = new LinkedList<Object>();

        LinkedList<Object[]> result = new LinkedList<Object[]>();

        titles.addLast(I18n.get("StartnumberShort"));
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("0");
        titles.addLast(I18n.get("Surname"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Firstname"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Sex"));
        aligns.addLast(SwingConstants.CENTER);
        formats.addLast("");
        titles.addLast(I18n.get("YearOfBirth"));
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("");
        titles.addLast(I18n.get("Team"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Organisation"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Qualifikationsebene"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("AgeGroup"));
        aligns.addLast(SwingConstants.CENTER);
        formats.addLast("");
        titles.addLast(I18n.get("Sex"));
        aligns.addLast(SwingConstants.CENTER);
        formats.addLast("");

        ListIterator<Mannschaft> li = schwimmer.listIterator();

        int length = schwimmer.size();
        int percent = 0;
        int counter = 0;

        LinkedList<Object> row = new LinkedList<Object>();

        while (li.hasNext()) {
            int per = (counter * 100) / length;
            if (per > percent + 5) {
                percent = per;
                double y = ((double) percent) / 110;
                fb.showFeedback(I18n.get("Percent", y));
            }
            counter++;

            Mannschaft t = li.next();

            int max = t.getMaxMembers();
            for (int x = 0; x < max; x++) {
                row.clear();

                row.addLast(StartnumberFormatManager.format(t) + StringTools.asText(x));

                Mannschaftsmitglied m = t.getMannschaftsmitglied(x);
                row.addLast(m.getNachname());
                row.addLast(m.getVorname());
                row.addLast(I18n.getSexShortString(m.getGeschlecht()));
                row.addLast(m.getJahrgang() > 0 ? m.getJahrgang() : "");

                row.addLast(t.getName());
                row.addLast(t.getGliederung());
                row.addLast(t.getQualifikationsebene());
                row.addLast(t.getAK().toString());
                row.addLast(I18n.geschlechtToString(t));

                result.addLast(row.toArray());
            }
        }

        ExtendedTableModel tm = new ExtendedTableModel(result.toArray(new Object[0][0]), titles.toArray());
        tm.setColumnAlignments(aligns.toArray(new Integer[aligns.size()]));
        tm.setColumnFormats(formats.toArray(new String[formats.size()]));
        tm.setName(I18n.get("TeammembersSingle"));
        tm.setLandscape(false);

        return tm;
    }

    public static <T extends ASchwimmer> ExtendedTableModel[] zusatzwertungResults(final AWettkampf<T> wk, Feedback fb, boolean extendedNames) {
        boolean[][] all = new boolean[2][wk.getRegelwerk().size()];
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            for (int y = 0; y < 2; y++) {
                all[y][x] = true;
            }
        }
        return zusatzwertungResults(wk, all, fb, extendedNames);
    }

    public static <T extends ASchwimmer> ExtendedTableModel[] zusatzwertungResults(final AWettkampf<T> wk, boolean[][] selection, Feedback fb,
            boolean extendedNames) {
        if (wk == null) {
            return null;
        }
        if (fb == null) {
            fb = new NullFeedback();
        }

        LinkedList<ExtendedTableModel> results = new LinkedList<ExtendedTableModel>();

        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            Altersklasse ak = wk.getRegelwerk().getAk(x);
            if (ak.hasHLW()) {
                for (int y = 0; y < 2; y++) {
                    if (selection[y][x]) {
                        LinkedList<T> swimmers = SearchUtils.getSchwimmer(wk, ak, y == 1);
                        if ((swimmers != null) && (!swimmers.isEmpty())) {
                            ExtendedTableModel etm = zusatzwertungResults(wk, swimmers, fb, false, extendedNames);
                            etm.setName(ak.getName() + " " + I18n.geschlechtToString(wk.getRegelwerk(), y == 1));
                            results.addLast(etm);
                        }
                    }
                }
            }
        }

        if (results.isEmpty()) {
            return null;
        }
        return results.toArray(new ExtendedTableModel[results.size()]);
    }

    public static <T extends ASchwimmer> ExtendedTableModel zusatzwertungResults(final AWettkampf<T> wk, LinkedList<T> swimmers, Feedback fb, boolean full,
            boolean extendedNames) {
        if (wk == null) {
            return null;
        }
        if (swimmers == null) {
            swimmers = wk.getSchwimmer();
        }
        if (fb == null) {
            fb = new NullFeedback();
        }
        if (extendedNames) {
            if (!(wk instanceof MannschaftWettkampf)) {
                extendedNames = false;
            }
        }

        Collections.sort(swimmers, new SchwimmerNameVergleicher<T>());
        Collections.sort(swimmers, new SchwimmerGeschlechtVergleicher<T>());
        Collections.sort(swimmers, new SchwimmerAKVergleicher<T>());

        LinkedList<Integer> aligns = new LinkedList<Integer>();
        LinkedList<String> formats = new LinkedList<String>();
        LinkedList<Object> titles = new LinkedList<Object>();

        LinkedList<Object[]> result = new LinkedList<Object[]>();

        titles.addLast(I18n.get("StartnumberShort"));
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("0");
        titles.addLast(I18n.get("Name"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        if (extendedNames) {
            titles.addLast(I18n.get("Member"));
            aligns.addLast(SwingConstants.LEFT);
            formats.addLast("");
        }
        titles.addLast(I18n.get("Organisation"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("QualifikationsebeneShort"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        if (full) {
            titles.addLast(I18n.get("AgeGroup"));
            aligns.addLast(SwingConstants.CENTER);
            formats.addLast("");
            titles.addLast(I18n.get("Sex"));
            aligns.addLast(SwingConstants.CENTER);
            formats.addLast("");
        }
        titles.addLast(wk.getRegelwerk().getZusatzwertungShort());
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("0.0");

        ListIterator<T> li = swimmers.listIterator();

        int length = swimmers.size();
        int percent = 0;
        int counter = 0;
        while (li.hasNext()) {
            int per = (counter * 100) / length;
            if (per > percent + 5) {
                percent = per;
                double y = ((double) percent) / 110;
                fb.showFeedback(I18n.get("Percent", y));
            }
            counter++;

            T t = li.next();

            if (t.getAK().hasHLW()) {
                for (int x = 0; x < t.getMaximaleHLW(); x++) {
                    LinkedList<Object> row = new LinkedList<Object>();

                    if (t.getMaximaleHLW() > 1) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(StartnumberFormatManager.format(t));
                        sb.append(StringTools.ABC[x]);
                        row.addLast(sb.toString());
                    } else {
                        row.addLast(StartnumberFormatManager.format(t));
                    }
                    row.addLast(t.getName());
                    if (extendedNames) {
                        String name = "";
                        if (t instanceof Mannschaft) {
                            name = ((Mannschaft) t).getMitgliedsname(x);
                        }
                        row.addLast(name);
                    }
                    row.addLast(t.getGliederung());
                    row.addLast(t.getQualifikationsebene());
                    if (full) {
                        row.addLast(t.getAK().toString());
                        row.addLast(I18n.geschlechtToString(t));
                    }
                    switch (t.getHLWState(x)) {
                    case ENTERED: {
                        double value = t.getHLWPunkte(x);
                        if (Math.round(value * 100) % 100 == 0) {
                            row.addLast((int) value);
                        } else {
                            row.addLast(value);
                        }
                        break;
                    }
                    case NICHT_ANGETRETEN:
                        if (wk.getRegelwerk().getFormelID().equals(FormelDLRG2007.ID) && (wk.getStrafen().getStrafe("V1") != null)) {
                            row.addLast("V1");
                        } else {
                            row.addLast("n.a.");
                        }
                        break;
                    case DISQALIFIKATION:
                        if (wk.getRegelwerk().getFormelID().equals(FormelDLRG2007.ID) && (wk.getStrafen().getStrafe("S1") != null)) {
                            row.addLast("S1");
                        } else {
                            row.addLast("disq.");
                        }
                        break;
                    case NOT_ENTERED:
                        row.addLast("-");
                        break;
                    }

                    result.addLast(row.toArray());
                }
            }
        }

        ExtendedTableModel tm = new ExtendedTableModel(result.toArray(new Object[0][0]), titles.toArray());
        tm.setColumnAlignments(aligns.toArray(new Integer[aligns.size()]));
        tm.setColumnFormats(formats.toArray(new String[formats.size()]));
        tm.setLandscape(false);

        return tm;
    }

    public static <T extends ASchwimmer> ExtendedTableModel heats(AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return null;
        }
        Laufliste<T> laufe = wk.getLaufliste();
        if (laufe == null) {
            return null;
        }

        int defaultalign = SwingConstants.CENTER;
        if (Utils.getPreferences().getBoolean("HHListLeftAlign", false)) {
            defaultalign = SwingConstants.LEFT;
        }

        DataContainer dc = new DataContainer();
        dc.add("Heat", RIGHT, "", "Heat", true);
        dc.add("AgeGroup", LEFT, "", "AgeGroup", true);
        dc.add("Discipline", LEFT, "", "Discipline", true);
        dc.addTitle("Lane", wk.getIntegerProperty(HEATS_LANES));
        for (int x = 1; x <= wk.getIntegerProperty(HEATS_LANES); x++) {
            dc.addI(I18n.get("LaneNumber", x), defaultalign, "", "" + x, false);
        }

        boolean mixedheats = laufe.hasMixedHeats();
        int groupsize = 2;
        if (mixedheats) {
            groupsize = 3;
        }

        ListIterator<Lauf<T>> li = laufe.getLaufliste().listIterator();

        LinkedList<Object[]> data = new LinkedList<Object[]>();

        int bahnen = wk.getIntegerProperty(HEATS_LANES);

        int length = laufe.getLaufliste().size();
        int percent = 0;
        int counter = 0;
        while (li.hasNext()) {
            int per = (counter * 100) / length;
            if (per > percent) {
                percent = per;
                double y = ((double) percent) / 100;
                fb.showFeedback(I18n.get("Percent", y));
            }
            counter++;

            Lauf<T> lauf = li.next();
            for (int x = 0; x < groupsize; x++) {
                data.addLast(laufToLine(lauf, bahnen, x, mixedheats));
            }
        }

        ExtendedTableModel tm = new ExtendedTableModel(data.toArray(new Object[0][0]), dc.getTitles());
        tm.setColumnAlignments(dc.getAlignments());
        tm.setColumnFormats(dc.getFormats());
        tm.setLandscape(false);
        tm.setExtendedTitles(dc.getExtendedTitles());
        return tm;
    }

    /**
     * @param laufe
     * @param lauf
     * @return
     */
    private static <T extends ASchwimmer> Object[] laufToLine(Lauf<T> lauf, int bahnen, int offset, boolean mixedheats) {
        LinkedList<Object> row = new LinkedList<Object>();
        if (offset == (mixedheats ? 1 : 0)) {
            row.addLast(lauf.getName());
        } else {
            row.addLast("");
        }
        if (lauf.isEmpty()) {
            for (int x = 0; x < bahnen + 2; x++) {
                row.addLast("");
            }
        } else {
            if (offset == (mixedheats ? 1 : 0)) {
                row.addLast(lauf.getStartgruppe());
                row.addLast(lauf.getDisziplin());
            } else {
                row.addLast("");
                row.addLast("");
            }
            for (int x = 0; x < bahnen; x++) {
                switch (offset) {
                case 0:
                    if (lauf.getSchwimmer(x) != null) {
                        row.addLast(lauf.getSchwimmer(x).getName());
                    } else {
                        row.addLast("");
                    }
                    break;
                case 1:
                    if (lauf.getSchwimmer(x) != null) {
                        String g = lauf.getSchwimmer(x).getGliederung();
                        String q = lauf.getSchwimmer(x).getQualifikationsebene().trim();
                        if (q.length() > 0) {
                            g += " (" + q + ")";
                        }
                        row.addLast(g);
                    } else {
                        row.addLast("");
                    }
                    break;
                case 2:
                    if (lauf.getSchwimmer(x) != null) {
                        row.addLast(I18n.getAgeGroupAsString(lauf.getSchwimmer(x)));
                    } else {
                        row.addLast("");
                    }
                    break;
                default:
                }
            }
        }
        return row.toArray();
    }

    public static <T extends ASchwimmer> ExtendedTableModel zusatzwertung(AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return null;
        }
        if (wk.getHLWListe().isEmpty()) {
            return null;
        }

        HLWListe<T> laufe = wk.getHLWListe();
        if (laufe == null) {
            return null;
        }

        int defaultalign = SwingConstants.CENTER;
        if (Utils.getPreferences().getBoolean("HHListLeftAlign", false)) {
            defaultalign = SwingConstants.LEFT;
        }

        DataContainer dc = new DataContainer();
        dc.add("TimeOfDay", RIGHT, "", "TimeOfDay", true);
        dc.add("AgeGroup", LEFT, "", "AgeGroup", true);
        dc.addTitle("Lanes", wk.getIntegerProperty(ZW_LANES));
        for (int x = 1; x <= wk.getIntegerProperty(ZW_LANES); x++) {
            dc.addI(I18n.get("LaneNumber", x), defaultalign, "", "" + x, false);
        }

        LinkedList<Object[]> result = new LinkedList<Object[]>();

        int puppen = wk.getIntegerProperty(ZW_LANES);

        Object[][] pause = new Object[3][2 + puppen];
        for (int x = 0; x < pause.length; x++) {
            for (int y = 0; y < 3; y++) {
                pause[y][x] = "";
            }
        }
        pause[1][1] = I18n.get("Pause");

        int length = laufe.getLauflistenCount();
        int counter = 0;
        ListIterator<LinkedList<HLWLauf<T>>> lli = laufe.getIterator();
        while (lli.hasNext()) {
            int percent = 0;
            int per = (counter * 100) / length;
            if (per > percent) {
                percent = per;
                double y = ((double) percent) / 100;
                fb.showFeedback(I18n.get("Percent", y));
            }
            LinkedList<HLWLauf<T>> heats = lli.next();
            ListIterator<HLWLauf<T>> li = heats.listIterator();

            while (li.hasNext()) {
                counter++;
                HLWLauf<T> lauf = li.next();
                for (int x = 0; x < 3; x++) {
                    result.addLast(zusatzwertungToLine(lauf, puppen, x));
                }
            }

            for (int x = 0; x < 3; x++) {
                result.addLast(pause[x]);
            }
        }

        for (int x = 0; x < 3; x++) {
            result.removeLast();
        }

        ExtendedTableModel tm = new ExtendedTableModel(result.toArray(new Object[0][0]), dc.getTitles());
        tm.setColumnAlignments(dc.getAlignments());
        tm.setColumnFormats(dc.getFormats());
        tm.setLandscape(false);
        tm.setExtendedTitles(dc.getExtendedTitles());

        return tm;
    }

    public static <T extends ASchwimmer> ExtendedTableModel referees(AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return null;
        }
        if (wk.getKampfrichterverwaltung() == null) {
            return null;
        }

        LinkedList<Object> titles = new LinkedList<Object>();
        LinkedList<Integer> aligns = new LinkedList<Integer>();
        LinkedList<String> formats = new LinkedList<String>();

        titles.addLast(I18n.get("Category"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Position"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Name"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Organization"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Comment"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Level"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("MinLevel"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");

        LinkedList<Object[]> result = new LinkedList<Object[]>();

        KampfrichterVerwaltung kv = wk.getKampfrichterverwaltung();

        int percent = 0;

        for (int y = 0; y < kv.getEinheitenCount(); y++) {
            int per = (y * 100) / kv.getEinheitenCount();
            if (per > percent) {
                percent = per;
                double z = ((double) percent) / 100;
                fb.showFeedback(I18n.get("Percent", z));
            }
            KampfrichterEinheit ke = kv.getEinheit(y);
            String[][] content = ke.getInhalt();
            for (String[] aContent : content) {
                result.addLast(refereeToLine(ke.getName(), aContent));
            }
        }

        ExtendedTableModel tm = new ExtendedTableModel(result.toArray(new Object[0][0]), titles.toArray());
        tm.setColumnAlignments(aligns.toArray(new Integer[aligns.size()]));
        tm.setColumnFormats(formats.toArray(new String[formats.size()]));
        return tm;
    }

    private static <T extends ASchwimmer> Object[] refereeToLine(String category, String[] content) {
        return new String[] { category, content[0], content[1], content[2], content[3], content[4], content[5] };
    }

    /**
     * @param lauf
     * @param puppen
     * @param offset
     * @return
     */
    private static <T extends ASchwimmer> Object[] zusatzwertungToLine(Lauf<T> lauf, int puppen, int offset) {
        LinkedList<Object> row = new LinkedList<Object>();
        if (offset == 1) {
            row.addLast(lauf.getName());
        } else {
            row.addLast("");
        }
        if (lauf.isEmpty()) {
            for (int x = 0; x < puppen + 1; x++) {
                row.addLast("");
            }
        } else {
            if (offset == 1) {
                row.addLast(lauf.getAltersklasse());
            } else {
                row.addLast("");
            }
            for (int x = 0; x < puppen; x++) {
                T schwimmer = lauf.getSchwimmer(x);
                switch (offset) {
                case 0:
                    if (schwimmer != null) {
                        row.addLast(schwimmer.getName() + " (" + StartnumberFormatManager.format(schwimmer) + ")");
                    } else {
                        row.addLast("");
                    }
                    break;
                case 1:
                    if (schwimmer != null) {
                        String g = schwimmer.getGliederung();
                        String q = schwimmer.getQualifikationsebene().trim();
                        if (q.length() > 0) {
                            g += " (" + q + ")";
                        }
                        row.addLast(g);
                    } else {
                        row.addLast("");
                    }
                    break;
                case 2:
                    if (schwimmer != null) {
                        row.addLast(I18n.getAgeGroupAsString(schwimmer));
                    } else {
                        row.addLast("");
                    }
                    break;
                default:
                    row.addLast("");
                    break;
                }
            }
        }
        return row.toArray();
    }

    public static <T extends ASchwimmer> ExtendedTableModel startkarten(AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return null;
        }
        Laufliste<T> laufe = wk.getLaufliste();
        if (laufe == null) {
            return null;
        }

        LinkedList<Object> titles = new LinkedList<Object>();
        LinkedList<Integer> aligns = new LinkedList<Integer>();
        LinkedList<String> formats = new LinkedList<String>();

        titles.addLast(I18n.get("Startnumber"));
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("0");
        titles.addLast(I18n.get("Name"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;
        if (w instanceof EinzelWettkampf) {
            titles.addLast(I18n.get("Surname"));
            aligns.addLast(SwingConstants.LEFT);
            formats.addLast("");
            titles.addLast(I18n.get("FirstName"));
            aligns.addLast(SwingConstants.LEFT);
            formats.addLast("");
            titles.addLast(I18n.get("YearOfBirth"));
            aligns.addLast(SwingConstants.RIGHT);
            formats.addLast("00");
        }
        titles.addLast(I18n.get("Organisation"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("QualifikationsebeneShort"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("AgeGroup"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Sex"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Comment"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Discipline"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Competition"));
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("");
        titles.addLast(I18n.get("Heat"));
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("");
        titles.addLast(I18n.get("Lane"));
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("");

        ListIterator<Lauf<T>> li = laufe.getLaufliste().listIterator();

        LinkedList<Object[]> result = new LinkedList<Object[]>();

        int length = laufe.getLaufliste().size();
        int percent = 0;
        int counter = 0;
        while (li.hasNext()) {
            int per = (counter * 100) / length;
            if (per > percent) {
                percent = per;
                double y = ((double) percent) / 100;
                fb.showFeedback(I18n.get("Percent", y));
            }
            counter++;

            Lauf<T> lauf = li.next();
            startkartenLaufToLines(result, lauf);
        }

        ExtendedTableModel tm = new ExtendedTableModel(result.toArray(new Object[0][0]), titles.toArray());
        tm.setColumnAlignments(aligns.toArray(new Integer[aligns.size()]));
        tm.setColumnFormats(formats.toArray(new String[formats.size()]));
        return tm;
    }

    /**
     * @param laufe
     * @param p
     * @param lauf
     */
    private static <T extends ASchwimmer> void startkartenLaufToLines(LinkedList<Object[]> result, Lauf<T> lauf) {
        if (!lauf.isEmpty()) {
            for (int x = 0; x < lauf.getBahnen(); x++) {
                T schwimmer = lauf.getSchwimmer(x);
                if (schwimmer != null) {
                    result.addLast(startkarteToLine(schwimmer, lauf, x));
                }
            }
        }
    }

    /**
     * @param schwimmer
     * @param lauf
     * @param x
     * @return
     */
    private static <T extends ASchwimmer> Object[] startkarteToLine(T schwimmer, Lauf<T> lauf, int x) {
        int disz = lauf.getDisznummer(x);

        LinkedList<Object> row = new LinkedList<Object>();
        row.addLast(StartnumberFormatManager.format(schwimmer));
        row.addLast(schwimmer.getName());
        if (schwimmer instanceof Teilnehmer) {
            Teilnehmer t = (Teilnehmer) schwimmer;
            row.addLast(t.getNachname());
            row.addLast(t.getVorname());
            row.addLast(I18n.yearToShortObject(t.getJahrgang()));
        }
        row.addLast(schwimmer.getGliederung());
        row.addLast(schwimmer.getQualifikationsebene());
        row.addLast(schwimmer.getAK().toString());
        row.addLast(I18n.geschlechtToString(schwimmer));
        row.addLast(schwimmer.getBemerkung());
        row.addLast(schwimmer.getAK().getDisziplin(disz, schwimmer.isMaennlich()).getName());
        row.addLast(disz + 1);
        row.addLast(lauf.getName());
        row.addLast(x + 1);
        return row.toArray();
    }

    public static <T extends ASchwimmer> ExtendedTableModel zusatzwertungStartkarten(AWettkampf<T> wk, boolean printZWnames, Feedback fb) {
        if (wk == null) {
            return null;
        }
        HLWListe<T> laufe = wk.getHLWListe();
        if (laufe == null) {
            return null;
        }

        LinkedList<Object> titles = new LinkedList<Object>();
        LinkedList<Integer> aligns = new LinkedList<Integer>();
        LinkedList<String> formats = new LinkedList<String>();

        titles.addLast(I18n.get("Startnumber"));
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("0");
        titles.addLast(I18n.get("Name"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Surname"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("FirstName"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("YearOfBirth"));
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("00");
        titles.addLast(I18n.get("Organisation"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("QualifikationsebeneShort"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("AgeGroup"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Sex"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("Comment"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("TimeOfDay"));
        aligns.addLast(SwingConstants.CENTER);
        formats.addLast("");
        titles.addLast(I18n.get("Lane"));
        aligns.addLast(SwingConstants.CENTER);
        formats.addLast("");

        LinkedList<Object[]> result = new LinkedList<Object[]>();

        LinkedList<ZWStartkarte<T>> sk = SchwimmerUtils.toZWStartkarten(wk.getHLWListe(), 1, true, 0, 0, true);
        Collections.sort(sk, new Comparator<ZWStartkarte<T>>() {
            @Override
            public int compare(ZWStartkarte<T> sk1, ZWStartkarte<T> sk2) {
                int skt2 = sk2.getTimecode();
                int skt1 = sk1.getTimecode();

                if (skt1 != skt2) {
                    return skt1 * 100 - skt2 * 100;
                }

                return sk1.getBahnindex() - sk2.getBahnindex();
            }
        });
        ListIterator<ZWStartkarte<T>> li = sk.listIterator();

        int length = sk.size();
        int percent = 0;
        int counter = 0;
        while (li.hasNext()) {
            int per = (counter * 100) / length;
            if (per > percent) {
                percent = per;
                double y = ((double) percent) / 100;
                fb.showFeedback(I18n.get("Percent", y));
            }
            counter++;

            ZWStartkarte<T> lauf = li.next();
            result.addLast(startkarteZWToLine(lauf, printZWnames));
        }

        ExtendedTableModel tm = new ExtendedTableModel(result.toArray(new Object[0][0]), titles.toArray());
        tm.setColumnAlignments(aligns.toArray(new Integer[aligns.size()]));
        tm.setColumnFormats(formats.toArray(new String[formats.size()]));
        return tm;
    }

    /**
     * @param schwimmer
     * @param lauf
     * @param x
     * @return
     */
    private static <T extends ASchwimmer> Object[] startkarteZWToLine(ZWStartkarte<T> sk, boolean printZWnames) {
        T schwimmer = sk.getSchwimmer();

        LinkedList<Object> row = new LinkedList<Object>();

        try {
            row.addLast(Integer.parseInt(sk.getStartnummer()));
        } catch (RuntimeException re) {
            row.addLast(sk.getStartnummer());
        }
        if (printZWnames) {
            row.addLast(sk.getExtendedName());
        } else {
            row.addLast(sk.getName());
        }
        row.addLast(sk.getNachname());
        row.addLast(sk.getVorname());
        row.addLast(I18n.yearToShortObject(sk.getJahrgang()));
        row.addLast(sk.getGliederung());
        row.addLast(sk.getQualifikationsebene());
        row.addLast(sk.getAK());
        row.addLast(I18n.geschlechtToString(schwimmer));
        row.addLast(schwimmer.getBemerkung());
        row.addLast(sk.getUhrzeit());
        if (sk.getBahnindex() >= 0) {
            row.addLast(sk.getBahnindex() + 1);
        } else {
            row.addLast(sk.getBahn());
        }
        return row.toArray();
    }

    public static <T extends ASchwimmer> ExtendedTableModel registration(AWettkampf<T> wk, LinkedList<T> schwimmer, RegistrationDetails detail,
            boolean[] columns, boolean members, Feedback fb) {
        return registration(wk, schwimmer, detail, columns, members, false, fb);
    }

    public static <T extends ASchwimmer> ExtendedTableModel registration(AWettkampf<T> wk, LinkedList<T> schwimmer, RegistrationDetails detail,
            boolean[] columns, boolean members, boolean compact, Feedback fb) {
        if (wk == null) {
            return null;
        }
        if (schwimmer == null) {
            schwimmer = wk.getSchwimmer();
        }
        if (fb == null) {
            fb = new NullFeedback();
        }

        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;
        boolean einzel = (w instanceof EinzelWettkampf);
        boolean isOpenwater = FormelManager.isOpenwater(w.getRegelwerk().getFormelID());

        int maxteamsize = getMaximumMembers(wk, schwimmer);
        int maxregs = 1; // getMaximumRegisteredPoints(wk, schwimmer);

        LinkedList<Integer> aligns = new LinkedList<Integer>();
        LinkedList<String> formats = new LinkedList<String>();
        LinkedList<Object> titles = new LinkedList<Object>();

        LinkedList<Object[]> result = new LinkedList<Object[]>();

        titles.addLast(I18n.get("StartnumberShort"));
        aligns.addLast(SwingConstants.RIGHT);
        formats.addLast("0");
        if (einzel) {
            if (!compact) {
                titles.addLast(I18n.get("Surname"));
                aligns.addLast(SwingConstants.LEFT);
                formats.addLast("");
                titles.addLast(I18n.get("FirstName"));
                aligns.addLast(SwingConstants.LEFT);
                formats.addLast("");
            } else {
                titles.addLast(I18n.get("Name"));
                aligns.addLast(SwingConstants.LEFT);
                formats.addLast("");
            }
            titles.addLast(I18n.get("YearOfBirthShort"));
            aligns.addLast(SwingConstants.RIGHT);
            formats.addLast("00");
        } else {
            titles.addLast(I18n.get("Name"));
            aligns.addLast(SwingConstants.LEFT);
            formats.addLast("");
            if (members) {
                for (int x = 0; x < maxteamsize; x++) {
                    titles.addLast(I18n.get("SurnameNr", x + 1));
                    aligns.addLast(SwingConstants.LEFT);
                    formats.addLast("");
                    titles.addLast(I18n.get("FirstnameNr", x + 1));
                    aligns.addLast(SwingConstants.LEFT);
                    formats.addLast("");
                    titles.addLast(I18n.get("YearOfBirthNr", x + 1));
                    aligns.addLast(SwingConstants.LEFT);
                    formats.addLast("");
                    titles.addLast(I18n.get("SexNr", x + 1));
                    aligns.addLast(SwingConstants.LEFT);
                    formats.addLast("");
                }
            }
        }
        titles.addLast(I18n.get("Organisation"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        titles.addLast(I18n.get("QualifikationsebeneShort"));
        aligns.addLast(SwingConstants.LEFT);
        formats.addLast("");
        if (compact) {
            titles.addLast(I18n.get("AgeGroup"));
            aligns.addLast(SwingConstants.CENTER);
            formats.addLast("");
        } else {
            titles.addLast(I18n.get("AgeGroup"));
            aligns.addLast(SwingConstants.CENTER);
            formats.addLast("");
            titles.addLast(I18n.get("Sex"));
            aligns.addLast(SwingConstants.CENTER);
            formats.addLast("");
        }
        if (detail != RegistrationDetails.WITH_TIMES) {
            if (maxregs <= 1) {
                titles.addLast(I18n.get("ReportedPoints"));
                aligns.addLast(SwingConstants.RIGHT);
                formats.addLast("0.00");
                titles.addLast(I18n.get("Protocol"));
                aligns.addLast(SwingConstants.LEFT);
                formats.addLast("");
            } else {
                for (int x = 0; x < maxregs; x++) {
                    titles.addLast(I18n.get("ReportedPointsNr", StringTools.ABC[x]));
                    aligns.addLast(SwingConstants.RIGHT);
                    formats.addLast("0.00");
                    titles.addLast(I18n.get("ProtocolNr", StringTools.ABC[x]));
                    aligns.addLast(SwingConstants.LEFT);
                    formats.addLast("");
                }
            }
            titles.addLast(I18n.get("AusserKonkurrenzShort"));
            aligns.addLast(SwingConstants.CENTER);
            formats.addLast("");
            titles.addLast(I18n.get("Comment"));
            aligns.addLast(SwingConstants.LEFT);
            formats.addLast("");
        }
        if ((detail == RegistrationDetails.EVERYTHING) || (detail == RegistrationDetails.SHORT || (detail == RegistrationDetails.SHORT_WITH_TEAMMEMBERS))) {
            titles.addLast(I18n.get("StartunterlagenkontrolleShort"));
            aligns.addLast(SwingConstants.LEFT);
            formats.addLast("");
            titles.addLast(I18n.get("QualificationShort"));
            aligns.addLast(SwingConstants.LEFT);
            formats.addLast("");
        }
        if (detail == RegistrationDetails.SHORT_WITH_TEAMMEMBERS && (!wk.isEinzel())) {
            titles.addLast(I18n.get("Teammembers"));
            aligns.addLast(SwingConstants.LEFT);
            formats.addLast("");
        }

        LinkedList<String> disciplines = new LinkedList<String>();
        Hashtable<String, Integer> indizes = new Hashtable<String, Integer>();
        if ((detail != RegistrationDetails.SHORT) && (detail != RegistrationDetails.SHORT_WITH_TEAMMEMBERS)) {
            int index = 0;
            Regelwerk aks = wk.getRegelwerk();
            for (int x = 0; x < aks.size(); x++) {
                Altersklasse ak = aks.getAk(x);
                if (SearchUtils.hasSchwimmer(schwimmer, ak)) {
                    for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                        String name = I18n.getDisziplinShort(ak.getDisziplin(y, true).getName());
                        if (indizes.get(name) == null) {
                            titles.addLast(name);
                            aligns.addLast(isOpenwater ? SwingConstants.CENTER : SwingConstants.RIGHT);
                            formats.addLast("");

                            indizes.put(name, index);
                            disciplines.addLast(name);
                            index++;
                        }
                    }
                }
            }
        }

        ListIterator<T> li = schwimmer.listIterator();

        int length = schwimmer.size();
        int percent = 0;
        int counter = 0;
        while (li.hasNext()) {
            int per = (counter * 100) / length;
            if (per > percent + 5) {
                percent = per;
                double y = ((double) percent) / 110;
                fb.showFeedback(I18n.get("Percent", y));
            }
            counter++;

            T t = li.next();

            LinkedList<Object> row = new LinkedList<Object>();

            row.addLast(StartnumberFormatManager.format(t));
            if (einzel) {
                Teilnehmer teili = (Teilnehmer) t;
                if (!compact) {
                    row.addLast(teili.getNachname());
                    row.addLast(teili.getVorname());
                } else {
                    row.addLast(teili.getName());
                }
                if (teili.getJahrgang() > 0) {
                    int zahl = teili.getJahrgang() % 100;
                    if (zahl >= 10) {
                        row.addLast("" + zahl);
                    } else {
                        row.addLast("0" + zahl);
                    }
                } else {
                    row.addLast("");
                }
            } else {
                row.addLast(t.getName());
                Mannschaft m = (Mannschaft) t;
                if (members) {
                    for (int x = 0; x < maxteamsize; x++) {
                        if (x < m.getMaxMembers()) {
                            Mannschaftsmitglied mm = m.getMannschaftsmitglied(x);
                            if (mm.isEmpty()) {
                                row.addLast("");
                                row.addLast("");
                                row.addLast("");
                                row.addLast("");
                            } else {
                                row.addLast(mm.getNachname());
                                row.addLast(mm.getVorname());
                                row.addLast(I18n.getYearOfBirth(mm.getJahrgang()));
                                row.addLast(I18n.getSexShortString(mm.getGeschlecht()));
                            }
                        } else {
                            row.addLast("");
                            row.addLast("");
                            row.addLast("");
                            row.addLast("");
                        }
                    }
                }
            }
            row.addLast(t.getGliederung());
            row.addLast(t.getQualifikationsebene());
            if (compact) {
                row.addLast(t.getAK().toString() + " " + I18n.geschlechtToShortString(t));
            } else {
                row.addLast(t.getAK().toString());
                row.addLast(I18n.geschlechtToString(t));
            }
            if (detail != RegistrationDetails.WITH_TIMES) {
                for (int x = 0; x < Math.min(maxregs, t.getMeldepunkteSize()); x++) {
                    row.addLast(new FixedDecimal(t.getMeldepunkte(x), 2));
                    row.addLast(I18n.booleanToYesNo(t.hasMeldungMitProtokoll(x)));
                }
                for (int x = t.getMeldepunkteSize(); x < maxregs; x++) {
                    row.addLast("");
                    row.addLast("");
                }
                row.addLast(I18n.booleanToYesNo(t.isAusserKonkurrenz()));
                row.addLast(t.getBemerkung());
            }
            if ((detail == RegistrationDetails.EVERYTHING) || (detail == RegistrationDetails.SHORT) || (detail == RegistrationDetails.SHORT_WITH_TEAMMEMBERS)) {
                Startunterlagen su = t.getStartunterlagen();
                String text = "";
                switch (su) {
                case PRUEFEN:
                    text = I18n.get("yes");
                    break;
                case NICHT_PRUEFEN:
                    text = I18n.get("no");
                    break;
                }
                row.addLast(text);
                row.addLast(t.getQualifikation().toString());
            }

            if (detail != RegistrationDetails.SHORT && (detail != RegistrationDetails.SHORT_WITH_TEAMMEMBERS)) {
                String[] times = new String[disciplines.size()];
                Altersklasse ak = t.getAK();
                for (int x = 0; x < ak.getDiszAnzahl(); x++) {
                    if (t.isDisciplineChosen(x)) {
                        int meldezeit = t.getMeldezeit(x);
                        times[indizes.get(I18n.getDisziplinShort(ak.getDisziplin(x, true).getName()))] = (isOpenwater || meldezeit <= 0) ? "+"
                                : StringTools.zeitString(meldezeit);
                    }
                }
                for (String time : times) {
                    if (time == null) {
                        row.addLast("");
                    } else {
                        row.addLast(time);
                    }
                }
            }
            if (detail == RegistrationDetails.SHORT_WITH_TEAMMEMBERS && (t instanceof Mannschaft)) {
                Mannschaft m = (Mannschaft) t;
                row.addLast(m.getMitgliedernamenShort(", "));
            }

            result.addLast(row.toArray());
        }

        if (columns != null) {
            ListIterator<Object[]> ii = result.listIterator();
            while (ii.hasNext()) {
                Object[] i = remove(ii.next(), columns);
                ii.remove();
                ii.add(i);
            }
        }

        remove(aligns, columns);
        remove(formats, columns);
        remove(titles, columns);

        ExtendedTableModel tm = new ExtendedTableModel(result.toArray(new Object[0][0]), titles.toArray());
        tm.setColumnAlignments(aligns.toArray(new Integer[aligns.size()]));
        tm.setColumnFormats(formats.toArray(new String[formats.size()]));

        return tm;
    }

    private static <T extends ASchwimmer> int getMaximumMembers(AWettkampf<T> wk, LinkedList<T> schwimmer) {
        if (!(wk instanceof MannschaftWettkampf)) {
            return 0;
        }

        int maxteamsize = wk.getRegelwerk().getAk(0).getMaxMembers();
        for (int x = 1; x < wk.getRegelwerk().size(); x++) {
            maxteamsize = Math.max(maxteamsize, wk.getRegelwerk().getAk(x).getMaxMembers());
        }

        return maxteamsize;
    }

    public static <T extends ASchwimmer> ExtendedTableModel getSchnellsteZeiten(AWettkampf<T> wk, boolean print) {
        if (wk == null) {
            return null;
        }
        {
            wk = Utils.copy(wk);
            LinkedList<T> sw = wk.getSchwimmer();
            for (T s : sw) {
                if (s.getAkkumulierteStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF).isStrafe()) {
                    wk.removeSchwimmer(s);
                }
            }
        }

        LinkedList<Object[]> mp = new LinkedList<Object[]>();
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            ak.setStrafeIstDisqualifikation(true);
            ak.setEinzelwertung(true);
            ak.setEinzelwertungHlw(false);
            for (int y = 0; y < 2; y++) {
                AWettkampf<T> w = ResultUtils.generateEinzelwertungswettkampf(wk, x, y == 1);
                if (w != null) {
                    {
                        LinkedList<T> sw = w.getSchwimmer();
                        for (T s : sw) {
                            if (s.getAkkumulierteStrafe(0).isStrafe()) {
                                w.removeSchwimmer(s);
                            }
                        }
                    }
                    for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                        JResultTable result = JResultTable.getResultTable(w, w.getRegelwerk().getAk(z), y == 1, print, true, 0);
                        if (result.getRowCount() > 0) {
                            ASchwimmer s = result.getResult(0).getSchwimmer();
                            int zeit = s.getZeit(0);
                            StringBuilder name = new StringBuilder(s.getName());
                            StringBuilder gld = new StringBuilder(s.getGliederung());
                            if (s.getQualifikationsebene().length() > 0) {
                                gld.append(" (");
                                gld.append(s.getQualifikationsebene());
                                gld.append(")");
                            }
                            int pos = 1;
                            while ((pos < result.getRowCount()) && (result.getResult(pos).getSchwimmer().getZeit(0) == zeit)) {
                                ASchwimmer schwimmer = result.getResult(pos).getSchwimmer();
                                name.append(", ");
                                name.append(schwimmer.getName());
                                gld.append(", ");
                                gld.append(schwimmer.getGliederung());
                                if (schwimmer.getQualifikationsebene().length() > 0) {
                                    gld.append(" (");
                                    gld.append(schwimmer.getQualifikationsebene());
                                    gld.append(")");
                                }
                                pos++;
                            }
                            mp.addLast(new Object[] { name.toString(), gld.toString(), ak.getName(), I18n.geschlechtToShortString(aks, y == 1),
                                    ak.getDisziplin(z, y == 1).getName(), StringTools.zeitString(s.getZeit(0)) });
                        } else {
                            mp.addLast(new Object[] { " ", " ", " ", " ", " ", " " });
                        }
                    }

                }
            }
        }
        if (mp.isEmpty()) {
            return null;
        }

        ExtendedTableModel etm = new ExtendedTableModel(mp.toArray(new Object[mp.size()][]),
                new Object[] { I18n.get("Name"), I18n.get("Organisation"), I18n.get("AgeGroup"), I18n.get("Sex"), I18n.get("Discipline"), I18n.get("Time") });
        etm.setColumnAlignments(
                new int[] { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.RIGHT });
        etm.setColumnFormats(new String[] { "", "", "", "", "", "m:ss.00" });
        etm.setName(I18n.get("SchnellsteZeiten"));
        return etm;
    }

    public static <T extends ASchwimmer> ExtendedTableModel easywkHeattimes(AWettkampf<T> wk, boolean print) {
        Heattime[] times;
        if (wk.isHeatBased()) {
            times = heattimesOW(wk);
        } else {
            times = heattimesPool(wk);
        }
        if (times == null || times.length == 0) {
            return null;
        }
        Arrays.sort(times, new Comparator<Heattime>() {
            @Override
            public int compare(Heattime ht1, Heattime ht2) {
                return (ht1.CompetitionId - ht2.CompetitionId) * 100000 + (500 + ht1.Heat - ht2.Heat) * 100 + (10 + ht1.Lane - ht2.Lane);
            }
        });

        Object[][] data = new Object[times.length][0];
        for (int x = 0; x < data.length; x++) {
            Heattime time = times[x];
            data[x] = new Object[] { time.CompetitionId, time.CompetitionType, time.CompetitionName, time.Count, time.Length, time.DisciplineChar,
                    time.Discipline, time.Lanecount, time.Abschnitt, time.Date, time.Heat, time.Lane, time.Bahnseite, time.Surname, time.Firstname,
                    time.YearOfBirth, time.Agegroup, time.Sex, time.IsRelay ? I18n.get("yes") : I18n.get("No"), time.DsvId, time.Organization,
                    time.Organization, time.OrganizationId, time.Lsv, time.Country, time.Kreisname, time.Timetype, time.Length,
                    StringTools.zeitString(time.Time), time.Status, time.CompetitionId, time.Startnumber, time.OrganizationId, time.KreisId };
        }

        ExtendedTableModel etm = new ExtendedTableModel(data,
                new Object[] { "WkNr", "WkTyp", "WkName", "Anzahl", "Länge", "Lage", "LageText", "AnzahlBahnen", "Abschnitt", "Datum", "Lauf", "Bahn",
                        "Bahnseite", "Name", "Vorname", "Jahrgang", "AK", "Geschlecht", "IstStaffel", "DsvId", "Verein", "Pseudoname", "Vereinsid", "Lsv",
                        "Fina", "Kreisname", "ZeitArt", "Meter", "Zeit", "Status", "InterneWkId", "InternePersId", "InterneVereinsId", "InterneKreisId" }
        // new Object[] { I18n.get("Name"), I18n.get("Organisation"), I18n.get("AgeGroup"), I18n.get("Sex"), I18n.get("Discipline"), I18n.get("Competition"),
        // I18n.get("Heat"), I18n.get("Lane"), I18n.get("Time"), I18n.get("Penalty") }
        );
        // etm.setColumnAlignments(new int[] { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT,
        // SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.RIGHT, SwingConstants.RIGHT, SwingConstants.LEFT });
        etm.setColumnFormats(new String[] { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                "m:ss.00", "", "", "", "", "" });
        etm.setName(I18n.get("Heattimes"));
        return etm;
    }

    public static <T extends ASchwimmer> ExtendedTableModel heattimes(AWettkampf<T> wk, boolean print) {
        Heattime[] times;
        if (wk.isHeatBased()) {
            times = heattimesOW(wk);
        } else {
            times = heattimesPool(wk);
        }
        if (times == null || times.length == 0) {
            return null;
        }
        Arrays.sort(times, new Comparator<Heattime>() {
            @Override
            public int compare(Heattime ht1, Heattime ht2) {
                return (ht1.CompetitionId - ht2.CompetitionId) * 100000 + (500 + ht1.Heat - ht2.Heat) * 100 + (10 + ht1.Lane - ht2.Lane);
            }
        });

        Object[][] data = new Object[times.length][0];
        for (int x = 0; x < data.length; x++) {
            Heattime time = times[x];
            data[x] = new Object[] { time.CompetitionId, time.CompetitionType, time.CompetitionName, time.Count, time.Length, time.DisciplineChar,
                    time.Discipline, time.Lanecount, time.Abschnitt, time.Date, time.Heat, time.Lane, time.Bahnseite, time.Surname, time.Firstname,
                    time.YearOfBirth, time.Agegroup, time.Sex, time.IsRelay ? I18n.get("yes") : I18n.get("No"), time.DsvId, time.Organization,
                    time.Organization, time.OrganizationId, time.Lsv, time.Country, time.Kreisname, time.Timetype, time.Length,
                    1.0 * time.Time / 24 / 60 / 60 / 100, time.Status, time.CompetitionId, time.Startnumber, time.OrganizationId, time.KreisId };
        }

        ExtendedTableModel etm = new ExtendedTableModel(data,
                new Object[] { "WkNr", "WkTyp", "WkName", "Anzahl", "Länge", "Lage", "LageText", "AnzahlBahnen", "Abschnitt", "Datum", "Lauf", "Bahn",
                        "Bahnseite", "Name", "Vorname", "Jahrgang", "AK", "Geschlecht", "IstStaffel", "DsvId", "Verein", "Pseudoname", "Vereinsid", "Lsv",
                        "Fina", "Kreisname", "ZeitArt", "Meter", "Zeit", "Status", "InterneWkId", "InternePersId", "InterneVereinsId", "InterneKreisId" }
        // new Object[] { I18n.get("Name"), I18n.get("Organisation"), I18n.get("AgeGroup"), I18n.get("Sex"), I18n.get("Discipline"), I18n.get("Competition"),
        // I18n.get("Heat"), I18n.get("Lane"), I18n.get("Time"), I18n.get("Penalty") }
        );
        // etm.setColumnAlignments(new int[] { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.LEFT,
        // SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.RIGHT, SwingConstants.RIGHT, SwingConstants.LEFT });
        etm.setColumnFormats(new String[] { "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                "m:ss.00", "", "", "", "", "" });
        etm.setName(I18n.get("Heattimes"));
        return etm;
    }

    private static <T extends ASchwimmer> Heattime[] heattimesPool(AWettkampf<T> wk) {
        return new Heattime[0];
    }

    private static <T extends ASchwimmer> Heattime[] heattimesOW(AWettkampf<T> wk) {
        ArrayList<Heattime> times = new ArrayList<>();

        for (OWDisziplin<T> dis : wk.getLauflisteOW().getDisziplinen()) {
            for (OWLauf<T> lauf : dis.getLaeufe()) {
                for (int x = 0; x < lauf.getBahnen(); x++) {
                    T t = lauf.getSchwimmer(x);
                    if (t != null) {
                        int competitionId = wk.getRegelwerk().getRundenId(dis);
                        int heat = lauf.getLaufnummer();
                        Strafe s = t.getAkkumulierteStrafe(dis.Id);
                        Disziplin disziplin = wk.getRegelwerk().getAk(dis.akNummer).getDisziplin(dis.disziplin, dis.maennlich);

                        int rounds = disziplin.getRunden().length + 1;
                        boolean isFinal = dis.round + 1 == rounds;

                        Teilnehmer tn = null;
                        if (t instanceof Teilnehmer) {
                            tn = (Teilnehmer) t;
                        }

                        Heattime ht = new Heattime();
                        ht.Startnumber = t.getStartnummer();
                        ht.Agegroup = wk.getRegelwerk().getAk(dis.akNummer).getName();
                        ht.CompetitionId = competitionId;
                        ht.Discipline = disziplin.getName();
                        ht.CompetitionName = ht.Discipline + " " + I18n.geschlechtToString(t);
                        ht.Heat = heat;
                        ht.Sex = I18n.getSexShortString(dis.maennlich);
                        ht.Lane = x + 1;
                        ht.Name = t.getName();
                        ht.Length = disziplin.getLaenge();
                        ht.Organization = t.getGliederungMitQGliederung();
                        ht.Time = t.getMeldezeit(dis.disziplin); // t.getZeit(dis.Id);
                        ht.Lanecount = lauf.getBahnen();
                        ht.IsRelay = tn == null;
                        ht.Penalty = (s == null || s.getArt() == Strafarten.NICHTS) ? "" : s.getShortname();
                        ht.CompetitionType = I18n.getRound(dis.round, isFinal);
                        if (tn != null) {
                            ht.YearOfBirth = tn.getJahrgang();
                            ht.Surname = tn.getNachname();
                            ht.Firstname = tn.getVorname();
                        }

                        times.add(ht);
                    }
                }
            }
        }

        return times.toArray(new Heattime[times.size()]);

    }
}