/*
 * Export.java Created on 2. Oktober 2002, 12:37
 */

package de.df.jauswertung.io;

import static de.df.jauswertung.io.ExportManager.BEST_TIMES;
import static de.df.jauswertung.io.ExportManager.HEATLIST;
import static de.df.jauswertung.io.ExportManager.HEATS_OVERVIEW;
import static de.df.jauswertung.io.ExportManager.HEATTIMES;
import static de.df.jauswertung.io.ExportManager.REFEREES;
import static de.df.jauswertung.io.ExportManager.REGISTRATION;
import static de.df.jauswertung.io.ExportManager.RESULTS;
import static de.df.jauswertung.io.ExportManager.STARTKARTEN;
import static de.df.jauswertung.io.ExportManager.TEAMMEMBERS;
import static de.df.jauswertung.io.ExportManager.ZWLIST;
import static de.df.jauswertung.io.ExportManager.ZW_RESULTS;
import static de.df.jauswertung.io.ExportManager.ZW_STARTKARTEN;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.FormelILS;
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
public abstract class ASpreadsheetExporter implements IExporter {

    /**
     * Diese Methode organisiert den Export in Excel-Dateien
     * 
     * @param name
     *            Name der Datei
     * @param WK
     *            Zu exportierender Wettkampf
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
            LinkedList<ExtendedTableModel[]> tms = new LinkedList<ExtendedTableModel[]>();
            LinkedList<Integer> repeatRows = new LinkedList<Integer>();
            LinkedList<Integer> repeatCols = new LinkedList<Integer>();
            LinkedList<String> titles = new LinkedList<String>();

            Regelwerk aks = wk.getRegelwerk();

            int page = 0;
            for (int x = 0; x < aks.size(); x++) {
                Altersklasse ak = aks.getAk(x);
                if (ak.hasMehrkampfwertung()) {
                    for (int y = 0; y < 2; y++) {
                        LinkedList<T> llak = SearchUtils.getSchwimmer(wk, ak, y == 1);
                        if (llak.size() > 0) {
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

            AWettkampf<T> wkx = CompetitionUtils.generateWertungsgruppenwettkampf(wk);
            if (wkx != null) {
                Regelwerk akx = wkx.getRegelwerk();
                for (int x = 0; x < akx.size(); x++) {
                    Altersklasse ak = akx.getAk(x);
                    if (ak.hasMehrkampfwertung()) {
                        for (int y = 0; y < 2; y++) {
                            LinkedList<T> llak = SearchUtils.getSchwimmer(wkx, ak, y == 1);
                            if (llak.size() > 0) {
                                page++;
                                fb.showFeedback(I18n.get("PageNr", page));
                                ExtendedTableModel tm = DataTableUtils.results(wkx, ak, y == 1, false, 0);
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

            write(name, tms.toArray(new ExtendedTableModel[tms.size()][0]), 1, repeatRows.toArray(new Integer[repeatRows.size()]),
                    repeatCols.toArray(new Integer[repeatCols.size()]), titles.toArray(new String[titles.size()]),
                    wk.getStringProperty(PropertyConstants.NAME));

            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private <T extends ASchwimmer> int generateEinzelwertung(AWettkampf<T> wk, Feedback fb, LinkedList<ExtendedTableModel[]> tms,
            LinkedList<Integer> repeatRows, LinkedList<Integer> repeatCols, LinkedList<String> titles, Regelwerk aks, int page) {
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            if (ak.hasEinzelwertung()) {
                for (int z = 0; z < 2; z++) {
                    LinkedList<ExtendedTableModel> etm = new LinkedList<ExtendedTableModel>();
                    for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                        LinkedList<T> llak = SearchUtils.getSchwimmer(wk, ak, z == 1);
                        if ((llak != null) && (llak.size() > 0)) {
                            page++;
                            fb.showFeedback(I18n.get("PageNr", page));
                            ExtendedTableModel tm = DataTableUtils.resultsEinzelwertung(wk, ak, z == 1, y, false, 0);
                            if (tm != null) {
                                etm.addLast(tm);
                            }
                        }
                    }
                    if (etm.size() > 0) {
                        tms.addLast(etm.toArray(new ExtendedTableModel[etm.size()]));
                        repeatRows.addLast(1);
                        repeatCols.addLast(3);
                        titles.addLast(ak.getName() + " " + I18n.geschlechtToShortString(aks, z == 1) + " - Disziplinen");
                    }
                }
            }
        }
        return page;
    }

    @SuppressWarnings("unchecked")
    private <T extends ASchwimmer> int generateRounds(AWettkampf<T> wk, Feedback fb, LinkedList<ExtendedTableModel[]> tms, LinkedList<Integer> repeatRows,
            LinkedList<Integer> repeatCols, LinkedList<String> titles, int page) {
        Regelwerk aks = wk.getRegelwerk();
        for (int akNummer = 0; akNummer < aks.size(); akNummer++) {
            Altersklasse ak = aks.getAk(akNummer);

            for (int x = 0; x < 2; x++) {
                LinkedList<ExtendedTableModel> etm = new LinkedList<ExtendedTableModel>();

                boolean male = x == 1;

                final int akn = akNummer;

                OWDisziplin<T>[] owds = wk.getLauflisteOW().getDisziplinen();
                owds = Arrays.asList(owds).stream().filter(y -> y.akNummer == akn && y.maennlich == male).sorted(new Comparator<OWDisziplin<T>>() {
                    @Override
                    public int compare(OWDisziplin<T> o1, OWDisziplin<T> o2) {
                        return (o1.disziplin - o2.disziplin) + (o2.round - o1.round) * 100;
                    }
                }).toArray(OWDisziplin[]::new);
                for (OWDisziplin<T> owd : owds) {
                    int rounds = ak.getDisziplin(owd.disziplin, owd.maennlich).getRunden().length + 1;
                    boolean isFinal = owd.round + 1 == rounds;
                    OWSelection ows = new OWSelection(ak, owd.akNummer, owd.maennlich, owd.disziplin, owd.round, isFinal);
                    ExtendedTableModel tm = generateRound(wk, ows, fb);
                    if (tm != null) {
                        etm.add(tm);
                    }
                }

                if (etm.size() > 0) {
                    page++;
                    fb.showFeedback(I18n.get("PageNr", page));

                    tms.addLast(etm.toArray(new ExtendedTableModel[etm.size()]));
                    repeatRows.addLast(1);
                    repeatCols.addLast(3);
                    titles.addLast(I18n.getAgeGroupAsStringShort(aks, ak, male) + " - " + I18n.get("Rounds"));
                }
            }
        }
        return page;
    }

    private <T extends ASchwimmer> ExtendedTableModel generateRound(AWettkampf<T> wk, OWSelection ows, Feedback fb) {
        wk = CompetitionUtils.createCompetitionFor(wk, ows);
        if (wk == null) {
            return null;
        }

        Altersklasse ak = wk.getRegelwerk().getAk(ows.akNummer);
        boolean maennlich = ows.male;
        int round = ows.round;
        int qualification = 0;
        Disziplin d = ak.getDisziplin(0, maennlich);
        int[] runden = d.getRunden();
        if (round < runden.length) {
            qualification = runden[round];
        }
        boolean isFinal = ows.isFinal;
        d.setName(d.getName() + " - " + I18n.getRound(round, isFinal));
        if (!isFinal) {
            wk.getRegelwerk().setFormelID(FormelILS.ID);
        }

        // result.updateResult(wk, ak, maennlich, null, ak.hasHLW(), qualification);

        LinkedList<T> llak = SearchUtils.getSchwimmer(wk, ak, maennlich);
        if ((llak != null) && (llak.size() > 0)) {
            ak = wk.getRegelwerk().getAk(ows.akNummer);
            ExtendedTableModel tm = DataTableUtils.results(wk, ak, maennlich, false, qualification);
            if (tm != null) {
                return tm;
            }
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
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_STARTNUMMER);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_NAME);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_GLIEDERUNG);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_MELDEPUNKTE);
            Collections.sort(schwimmer, CompetitionUtils.VERGLEICHER_ALTERSKLASSE);
            ExtendedTableModel tm = DataTableUtils.registration(wk, schwimmer, RegistrationDetails.EVERYTHING, null, true, fb);
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

    private <T extends ASchwimmer> void write(OutputStream name, ExtendedTableModel tm, int groupsize, int repeatrows, int repeatcols, String competition)
            throws IOException {
        write(name, new ExtendedTableModel[] { tm }, groupsize, repeatrows, repeatcols, competition);
    }

    protected abstract <T extends ASchwimmer> void write(OutputStream name, ExtendedTableModel[] tms, int groupsize, int repeatrows, int repeatcols,
            String competition) throws IOException;

    protected abstract <T extends ASchwimmer> void write(OutputStream name, ExtendedTableModel[][] tms, int groupsize, Integer[] repeatrows,
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
    public final <T extends ASchwimmer> boolean zusatzwertungStartkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            ExtendedTableModel tm = DataTableUtils.zusatzwertungStartkarten(wk, fb);
            if (tm == null) {
                return false;
            }
            tm.setName(I18n.get("ZWStartkarten"));
            tm.setLandscape(false);
            write(name, tm, 1, 1, 1, wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public final boolean isSupported(int type) {
        switch (type) {
        case REGISTRATION:
        case HEATLIST:
        case ZWLIST:
        case STARTKARTEN:
        case ZW_STARTKARTEN:
        case RESULTS:
        case REFEREES:
        case TEAMMEMBERS:
        case ZW_RESULTS:
        case BEST_TIMES:
        case HEATS_OVERVIEW:
            return true;
        case HEATTIMES:
        default:
            return false;
        }
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
            write(name, tm, wk.getLaufliste().hasMixedHeats() ? 3 : 2, 2, 3, wk.getStringProperty(PropertyConstants.NAME));
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

    @Override
    public final <T extends ASchwimmer> boolean zusatzwertung(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }
        try {
            ExtendedTableModel tm = DataTableUtils.zusatzwertung(wk, fb);
            if (tm == null) {
                return false;
            }
            tm.setName(I18n.get("ZWList"));
            tm.setLandscape(true);
            write(name, tm, 3, 2, 2, wk.getStringProperty(PropertyConstants.NAME));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exportiert die Kampfrichter eines Wettkampfes in eine CSV-Datei.
     * 
     * @param name
     *            Name der Datei
     * @param wk
     *            Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    public final synchronized <T extends ASchwimmer> boolean referees(OutputStream name, AWettkampf<T> wk, Feedback fb) {
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
    public final <T extends ASchwimmer> boolean protocol(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public final <T extends ASchwimmer> boolean penalties(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
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
            // tm[1] = DataTableUtils.teammemberssingle(wk, fb);
            // if (tm[1] == null) {
            // return false;
            // }
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
            ExtendedTableModel etm = DataTableUtils.heattimes(wk, false);
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