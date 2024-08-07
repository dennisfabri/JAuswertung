/*
 * Export.java Created on 2. Oktober 2002, 12:37
 */

package de.df.jauswertung.io;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.LinkedList;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.DataTableUtils;
import de.df.jauswertung.util.DataTableUtils.RegistrationDetails;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.*;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.io.csv.CsvDataWriter;
import de.df.jutils.io.csv.CsvManager;
import de.df.jutils.io.csv.CsvSimpleDoubleConverter;
import de.df.jutils.io.csv.CsvSimpleIntegerConverter;
import de.df.jutils.io.csv.CsvSimpleSecondsConverter;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.NullFeedback;

/**
 * Diese Klasse sorgt fuer den Export der Ergebnisse in Excel-Dateien
 *
 * @author dennis
 */
public abstract class ASpreadsheetExporter extends EmptyExporter {

    /**
     * Diese Methode organisiert den Export in Excel-Dateien
     *
     * @param name Name der Datei
     * @param wk   Zu exportierender Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    public final synchronized <T extends ASchwimmer> boolean results(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (fb == null) {
            fb = new NullFeedback();
        }
        try {
            LinkedList<ExtendedTableModel[]> tms = new LinkedList<>();
            LinkedList<Integer> repeatRows = new LinkedList<>();
            LinkedList<Integer> repeatCols = new LinkedList<>();
            LinkedList<String> titles = new LinkedList<>();

            Regelwerk aks = wk.getRegelwerk();

            int page = processCompetition(wk, fb, aks, 0, tms, repeatRows, repeatCols, titles);

            AWettkampf<T> wkx = CompetitionUtils.generateWertungsgruppenwettkampf(wk);
            if (wkx != null) {
                Regelwerk akx = wkx.getRegelwerk();
                page = processCompetition(wkx, fb, akx, page, tms, repeatRows, repeatCols, titles);
            }

            if (wk.isHeatBased()) {
                page = generateRounds(wk, fb, tms, repeatRows, repeatCols, titles, page);
            }

            page = generateEinzelwertung(wk, fb, tms, repeatRows, repeatCols, titles, aks, page);

            if (wkx != null) {
                Regelwerk akx = wkx.getRegelwerk();
                page = generateEinzelwertung(wkx, fb, tms, repeatRows, repeatCols, titles, akx, page);
            }

            // Seriendruck
            page++;
            fb.showFeedback(I18n.get("PageNr", page));
            ExtendedTableModel tm = DataTableUtils.results(wk, true, null);
            if (tm == null) {
                throw new IllegalStateException("pinting must not be null!");
            }

            // Convert everything to Strings so that wordprocessors
            // do not have to bother with formattings
            DecimalFormat df = new DecimalFormat();
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();

            CsvManager.setSeparator(dfs.getPatternSeparator());
            CsvDataWriter[] cdw = new CsvDataWriter[3];
            cdw[0] = new CsvSimpleIntegerConverter();
            cdw[1] = new CsvSimpleDoubleConverter(dfs.getDecimalSeparator(), 2);
            cdw[2] = new CsvSimpleSecondsConverter(dfs.getDecimalSeparator());

            for (int x = 0; x < tm.getColumnCount(); x++) {
                for (int y = 0; y < tm.getRowCount(); y++) {
                    Object o = tm.getValueAt(y, x);
                    for (CsvDataWriter aCdw : cdw) {
                        if (aCdw.canConvert(o)) {
                            o = aCdw.convert(o, null);
                            break;
                        }
                    }

                    tm.setValueAt(o, y, x);
                }
            }
            tm.setName(I18n.get("Seriendruck"));
            tm.setExtendedTitles(null);
            tms.addLast(new ExtendedTableModel[] { tm });
            repeatRows.addLast(1);
            repeatCols.addLast(wk instanceof MannschaftWettkampf ? 2 : 3);
            titles.addLast(tm.getName());

            page++;
            fb.showFeedback(I18n.get("PageNr", page));

            tm = DataTableUtils.results(wk, false, null);
            tm.setName(I18n.get("Data"));
            tm.setExtendedTitles(null);
            tms.addLast(new ExtendedTableModel[] { tm });
            repeatRows.addLast(1);
            repeatCols.addLast(wk instanceof MannschaftWettkampf ? 2 : 3);
            titles.addLast(tm.getName());

            if ((wk.getFilter().length > 1) && (wk.getCurrentFilterIndex() == 0)) {
                AWettkampf<T> wky = Utils.copy(wk);
                for (int i = 1; i < wk.getFilter().length; i++) {
                    wky.setCurrentFilterIndex(i);
                    AWettkampf<T> wkz = CompetitionUtils.getFilteredInstance(wky);

                    if (wkz.hasSchwimmer()) {
                        // Seriendruck
                        page++;
                        fb.showFeedback(I18n.get("PageNr", page));
                        tm = DataTableUtils.results(wkz, true, null);
                        if (tm == null) {
                            throw new IllegalStateException("pinting must not be null!");
                        }

                        // Convert everything to Strings so that wordprocessors
                        // do not have to bother with formattings

                        for (int x = 0; x < tm.getColumnCount(); x++) {
                            for (int y = 0; y < tm.getRowCount(); y++) {
                                Object o = tm.getValueAt(y, x);
                                for (CsvDataWriter aCdw : cdw) {
                                    if (aCdw.canConvert(o)) {
                                        o = aCdw.convert(o, null);
                                        break;
                                    }
                                }

                                tm.setValueAt(o, y, x);
                            }
                        }
                        tm.setName(I18n.get("Seriendruck") + " - " + wky.getCurrentFilter().getName());
                        tm.setExtendedTitles(null);
                        tms.addLast(new ExtendedTableModel[] { tm });
                        repeatRows.addLast(1);
                        repeatCols.addLast(wkz instanceof MannschaftWettkampf ? 2 : 3);
                        titles.addLast(tm.getName());

                        page++;
                        fb.showFeedback(I18n.get("PageNr", page));

                        tm = DataTableUtils.results(wkz, false, null);
                        tm.setName(I18n.get("Data") + " - " + wky.getCurrentFilter().getName());
                        tm.setExtendedTitles(null);
                        tms.addLast(new ExtendedTableModel[] { tm });
                        repeatRows.addLast(1);
                        repeatCols.addLast(wkz instanceof MannschaftWettkampf ? 2 : 3);
                        titles.addLast(tm.getName());
                    }
                }
            }

            write(name, tms.toArray(new ExtendedTableModel[tms.size()][0]), 1,
                    repeatRows.toArray(new Integer[0]),
                    repeatCols.toArray(new Integer[0]), titles.toArray(new String[0]),
                    wk.getStringProperty(PropertyConstants.NAME));

            return true;
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static <T extends ASchwimmer> int processCompetition(AWettkampf<T> wk, Feedback fb, Regelwerk aks, int page,
            LinkedList<ExtendedTableModel[]> tms, LinkedList<Integer> repeatRows, LinkedList<Integer> repeatCols,
            LinkedList<String> titles) {
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            if (ak.hasMehrkampfwertung()) {
                for (int y = 0; y < 2; y++) {
                    LinkedList<T> llak = SearchUtils.getSchwimmer(wk, ak, y == 1);
                    if (!llak.isEmpty()) {
                        page++;
                        fb.showFeedback(I18n.get("PageNr", page));
                        ExtendedTableModel tm = DataTableUtils.results(wk, ak, y == 1, false, 0);
                        if (tm != null) {
                            tms.addLast(new ExtendedTableModel[] { tm });
                            repeatRows.addLast(2);
                            repeatCols.addLast(wk instanceof MannschaftWettkampf ? 2 : 3);
                            titles.addLast(tm.getName());
                        }
                    }
                }
            }
        }
        return page;
    }

    private <T extends ASchwimmer> int generateEinzelwertung(AWettkampf<T> wk, Feedback fb,
            LinkedList<ExtendedTableModel[]> tms, LinkedList<Integer> repeatRows, LinkedList<Integer> repeatCols,
            LinkedList<String> titles, Regelwerk aks, int page) {
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            if (ak.hasEinzelwertung()) {
                for (int z = 0; z < 2; z++) {
                    LinkedList<ExtendedTableModel> etm = new LinkedList<>();
                    for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                        LinkedList<T> llak = SearchUtils.getSchwimmer(wk, ak, z == 1);
                        if ((llak != null) && (!llak.isEmpty())) {
                            page++;
                            fb.showFeedback(I18n.get("PageNr", page));
                            ExtendedTableModel tm = DataTableUtils.resultsEinzelwertung(wk, ak, z == 1, y, false, 0);
                            if (tm != null) {
                                etm.addLast(tm);
                            }
                        }
                    }
                    if (!etm.isEmpty()) {
                        tms.addLast(etm.toArray(new ExtendedTableModel[0]));
                        repeatRows.addLast(1);
                        repeatCols.addLast(3);
                        titles.addLast(
                                ak.getName() + " " + I18n.geschlechtToShortString(aks, z == 1) + " - Disziplinen");
                    }
                }
            }
        }
        return page;
    }

    @SuppressWarnings("unchecked")
    private <T extends ASchwimmer> int generateRounds(AWettkampf<T> wk, Feedback fb,
            LinkedList<ExtendedTableModel[]> tms, LinkedList<Integer> repeatRows, LinkedList<Integer> repeatCols,
            LinkedList<String> titles, int page) {
        Regelwerk aks = wk.getRegelwerk();
        for (int akNummer = 0; akNummer < aks.size(); akNummer++) {
            Altersklasse ak = aks.getAk(akNummer);

            for (int x = 0; x < 2; x++) {
                LinkedList<ExtendedTableModel> etm = new LinkedList<>();

                boolean male = x == 1;

                final int akn = akNummer;

                OWDisziplin<T>[] owds = wk.getLauflisteOW().getDisziplinen();
                owds = Arrays.stream(owds).filter(y -> y.akNummer == akn && y.maennlich == male)
                        .sorted((o1, o2) -> (o1.disziplin - o2.disziplin) + (o2.round - o1.round) * 100)
                        .toArray(OWDisziplin[]::new);
                for (OWDisziplin<T> owd : owds) {
                    OWSelection ows = new OWSelection(ak, owd.akNummer, owd.maennlich, owd.disziplin, owd.round);
                    ExtendedTableModel tm = generateRound(wk, ows, fb);
                    if (tm != null) {
                        etm.add(tm);
                    }
                }

                if (!etm.isEmpty()) {
                    page++;
                    fb.showFeedback(I18n.get("PageNr", page));

                    tms.addLast(etm.toArray(new ExtendedTableModel[0]));
                    repeatRows.addLast(1);
                    repeatCols.addLast(3);
                    titles.addLast(I18n.getAgeGroupAsStringShort(aks, ak, male) + " - " + I18n.get("Rounds"));
                }
            }
        }
        return page;
    }

    private <T extends ASchwimmer> ExtendedTableModel generateRound(AWettkampf<T> wk, OWSelection ows, Feedback fb) {
        wk = ResultUtils.createCompetitionFor(wk, ows);
        if (wk == null) {
            return null;
        }

        Altersklasse ak = wk.getRegelwerk().getAk(ows.akNummer);
        int discipline = 0;
        boolean maennlich = ows.male;
        int round = ows.round;
        int qualification = 0;
        Disziplin d = ak.getDisziplin(discipline, maennlich);
        int[] runden = d.getRunden();
        if (round < runden.length) {
            qualification = runden[round];
        }
        boolean isFinal = ows.isFinal;
        d.setName(d.getName() + " - " + I18n.getRound(round, isFinal));
        if (!isFinal || round == 0) {
            Regelwerk rw = wk.getRegelwerk();
            rw.setFormelID(switch (rw.getFormelID()) {
            case FormelILSOutdoorFinals.ID -> FormelILSOutdoor.ID;
            case FormelDLRG2007Finals.ID -> FormelDLRG2007.ID;
            default -> FormelILS.ID;
            });
        }

        LinkedList<T> llak = SearchUtils.getSchwimmer(wk, ak, maennlich);
        if ((llak != null) && (!llak.isEmpty())) {
            ak = wk.getRegelwerk().getAk(ows.akNummer);
            return DataTableUtils.resultsEinzelwertung(wk, ak, maennlich, discipline, false, qualification);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T extends ASchwimmer> boolean registration(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            LinkedList<T> schwimmer = wk.getSchwimmer();
            schwimmer.sort(CompetitionUtils.VERGLEICHER_STARTNUMMER);
            schwimmer.sort(CompetitionUtils.VERGLEICHER_NAME);
            schwimmer.sort(CompetitionUtils.VERGLEICHER_GLIEDERUNG);
            schwimmer.sort(CompetitionUtils.VERGLEICHER_MELDEPUNKTE);
            schwimmer.sort(CompetitionUtils.VERGLEICHER_ALTERSKLASSE);
            ExtendedTableModel tm = DataTableUtils.registration(wk, schwimmer, RegistrationDetails.EVERYTHING, null,
                    true, fb);
            if (tm == null) {
                return false;
            }
            tm.setName(I18n.get("Registration"));
            tm.setLandscape(false);
            write(name, tm, 1, 1, 1, wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void write(OutputStream name, ExtendedTableModel tm, int groupsize, int repeatrows,
            int repeatcols, String competition) throws IOException {
        write(name, new ExtendedTableModel[] { tm }, groupsize, repeatrows, repeatcols, competition);
    }

    protected abstract void write(OutputStream name, ExtendedTableModel[] tms, int groupsize, int repeatrows,
            int repeatcols, String competition) throws IOException;

    protected abstract void write(OutputStream name, ExtendedTableModel[][] tms, int groupsize, Integer[] repeatrows,
            Integer[] repeatcols, String[] titles, String competition) throws IOException;

    @Override
    public final <T extends ASchwimmer> boolean startkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {

            ExtendedTableModel tm = DataTableUtils.startkarten(wk, fb);
            if (tm == null) {
                return false;
            }
            tm.setName(I18n.get("Startkarten"));
            tm.setLandscape(false);
            write(name, tm, 1, 1, 1, wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public final boolean isSupported(ImportExportTypes type) {
        return switch (type) {
        case REGISTRATION, HEAT_LIST, STARTKARTEN, RESULTS, REFEREES, TEAM_MEMBERS, ZW_RESULTS, BEST_TIMES, HEATS_OVERVIEW -> true;
        default -> false;
        };
    }

    @Override
    public final <T extends ASchwimmer> boolean heats(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            ExtendedTableModel tm = DataTableUtils.heats(wk, fb);
            if (tm == null) {
                return false;
            }
            tm.setName(I18n.get("Laufliste"));
            tm.setLandscape(true);
            write(name, tm, wk.getLaufliste().hasMixedHeats() ? 3 : 2, 2, 3,
                    wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public final <T extends ASchwimmer> boolean heatsoverview(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            JTable t = PrintUtils.getLaufuebersichtTable(wk);
            if (t == null) {
                return false;
            }
            TableModel tm = t.getModel();
            ExtendedTableModel etm = new ExtendedTableModel(tm);
            etm.setName(I18n.get("Laufliste"));
            etm.setLandscape(true);
            write(name, etm, 1, 1, 1, wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exportiert die Kampfrichter eines Wettkampfes in eine CSV-Datei.
     *
     * @param name Name der Datei
     * @param wk   Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    public final synchronized <T extends ASchwimmer> boolean referees(OutputStream name, AWettkampf<T> wk,
            Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            ExtendedTableModel tm = DataTableUtils.referees(wk, fb);
            if (tm == null) {
                return false;
            }
            tm.setName(I18n.get("Referees"));
            tm.setLandscape(true);
            write(name, tm, 1, 1, 1, wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public final <T extends ASchwimmer> boolean teammembers(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            ExtendedTableModel[] tm = new ExtendedTableModel[1];
            tm[0] = DataTableUtils.teammembers(wk, fb);
            if (tm[0] == null) {
                return false;
            }
            write(name, tm, 1, 1, 1, wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public final <T extends ASchwimmer> boolean zusatzwertungResults(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            ExtendedTableModel[] tm = DataTableUtils.zusatzwertungResults(wk, fb, PrintUtils.printZWnames);
            if (tm == null) {
                return false;
            }
            write(name, tm, 1, 1, 1, wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public final <T extends ASchwimmer> boolean bestezeiten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            ExtendedTableModel etm = DataTableUtils.getSchnellsteZeiten(wk, false);
            if (etm == null) {
                return false;
            }
            write(name, etm, 1, 1, 1, wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public final <T extends ASchwimmer> boolean heattimes(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            ExtendedTableModel etm = DataTableUtils.heattimes(wk);
            if (etm == null) {
                return false;
            }
            write(name, etm, 1, 1, 1, wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}