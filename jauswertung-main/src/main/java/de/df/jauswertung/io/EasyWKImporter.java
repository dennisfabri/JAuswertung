/*
 * Created on 12.02.2004
 */
package de.df.jauswertung.io;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.Tupel;

/**
 * @author Dennis Fabri
 * @date 12.02.2004
 */
public final class EasyWKImporter implements IImporter {

    private static final String[] SUFFIXES   = { ".csv" };

    private static final char[]   SEPARATORS = { ',', ',', ';' };

    static {
        NumberFormat nf = NumberFormat.getInstance();
        if (nf instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat) nf;
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
            SEPARATORS[0] = dfs.getPatternSeparator();
        }
    }

    public EasyWKImporter() {
        // Nothing to do
    }

    @Override
    public <T extends ASchwimmer> Hashtable<String, String[]> teammembers(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> Hashtable<Tupel<Integer, Integer>, Double> zusatzwertungResults(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> LinkedList<T> registration(InputStream name, AWettkampf<T> wk, Feedback fb, LinkedList<T> data2, String filename)
            throws TableFormatException, TableEntryException, TableException {
        return null;
    }

    static char identifySeparator(String data) {
        for (char SEPARATOR : SEPARATORS) {
            int index = data.indexOf(SEPARATOR);
            if (index >= 0) {
                return SEPARATOR;
            }
        }
        return SEPARATORS[0];
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> heats(InputStream name, AWettkampf<T> wk, Feedback fb) throws TableFormatException, TableEntryException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> zusatzwertung(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> results(InputStream name, AWettkampf<T> wk, Feedback fb) throws TableFormatException, TableEntryException {
        return null;
    }

    @Override
    public <T extends ASchwimmer> KampfrichterVerwaltung referees(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException {
        return null;
    }

    @Override
    public boolean isSupported(int type) {
        switch (type) {
        case ImportManager.HEATTIMES:
            return true;
        default:
            return false;
        }
    }

    @Override
    public String getName() {
        return "EasyWK";
    }

    @Override
    public String[] getSuffixes() {
        return SUFFIXES;
    }

    private static int IndexCompetitionNumber = 0;
    private static int IndexStartnumber       = 1;
    private static int IndexTime              = 2;
    private static int IndexStatus            = 3;

    private static int[] identifyIndizes(Object[] titles) {
        int[] indizes = new int[4];
        Arrays.fill(indizes, -1);

        // "WkNr", "InternePersId", "Endzeit"
        for (int x = 0; x < titles.length; x++) {
            if (titles[x] == null) {
                continue;
            }
            String title = titles[x].toString().trim().toLowerCase();
            if (title.equals("wknr")) {
                indizes[IndexCompetitionNumber] = x;
            } else if (title.equals("internepersid")) {
                indizes[IndexStartnumber] = x;
            } else if (title.equals("endzeit")) {
                indizes[IndexTime] = x;
            } else if (title.equals("status")) {
                indizes[IndexStatus] = x;
            }
        }
        return indizes;
    }

    @Override
    public <T extends ASchwimmer> AWettkampf<T> heattimes(InputStream name, AWettkampf<T> wk, Feedback fb)
            throws TableFormatException, TableEntryException, TableException, IOException {
        fb.showFeedback(I18n.get("LoadingFile"));
        Object[][] data = CsvUtils.read(name);
        if (data == null) {
            fb.showFeedback(I18n.get("FileNotFoundOrEmpty"));
            return null;
        }
        wk = Utils.copy(wk);
        int[] indizes = identifyIndizes(data[0]);
        for (int index : indizes) {
            if (index < 0) {
                return null;
            }
        }
        for (int row = 1; row < data.length; row++) {
            Object compnrText = data[row][indizes[IndexCompetitionNumber]];
            Object snText = data[row][indizes[IndexStartnumber]];
            Object timeText = data[row][indizes[IndexTime]];
            Object statusText = data[row][indizes[IndexStatus]];

            int competitionNumber = toInt(compnrText);
            int startnumber = toInt(snText);
            int time = fromTime(timeText);
            Strafarten status = fromStatus(statusText);

            if (status == null) {
                continue;
            }

            for (int x = 0; x < wk.getRegelwerk().size(); x++) {
                Altersklasse ak = wk.getRegelwerk().getAk(x);
                for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                    for (int z = 0; z < 2; z++) {
                        Disziplin d = ak.getDisziplin(y, z == 1);
                        int[] rundenIds = d.getRundenIds();
                        for (int r = 0; r < rundenIds.length; r++) {
                            if (rundenIds[r] == competitionNumber) {
                                String id = wk.getLauflisteOW().getDisziplin(x, z == 1, y, r).Id;
                                try {
                                    switch (status) {
                                    case NICHTS: {
                                        T t = SearchUtils.getSchwimmer(wk, startnumber);
                                        try {
                                            fb.showFeedback(t.getName() + " in Wettkampf " + compnrText + ": " + timeText + " (" + statusText + ")");
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        t.setZeit(id, time);
                                        break;
                                    }
                                    case NICHT_ANGETRETEN: {
                                        T t = SearchUtils.getSchwimmer(wk, startnumber);
                                        try {
                                            fb.showFeedback(t.getName() + " in Wettkampf " + compnrText + ": n.a.");
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        t.setZeit(id, 0);
                                        if (t.getStrafen(id).stream().allMatch(s -> !s.isDidNotStart())) {
                                            t.addStrafe(id, Strafe.NICHT_ANGETRETEN);
                                        }
                                        break;
                                    }
                                    default:
                                        break;
                                    }
                                } catch (Exception ex) {
                                    fb.showFeedback("Row not matched: " + row);
                                }
                            }
                        }
                    }
                }
            }
        }
        return wk;
    }

    private Strafarten fromStatus(Object statusText) {
        try {
            String text = statusText.toString();
            if (text.equalsIgnoreCase("ok")) {
                return Strafarten.NICHTS;
            } else if (text.equalsIgnoreCase("n.a")) {
                return Strafarten.NICHT_ANGETRETEN;
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private int fromTime(Object timeText) {
        try {
            return ImportUtils.getTime(timeText);
        } catch (Exception ex) {
            return 0;
        }
    }

    private int toInt(Object snText) {
        try {
            return Integer.parseInt(snText.toString().trim());
        } catch (Exception ex) {
            return 0;
        }
    }
}