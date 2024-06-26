package de.df.jauswertung.io;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.DataTableUtils;
import de.df.jauswertung.util.DataTableUtils.RegistrationDetails;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.util.Feedback;

/**
 * Export.java Created on 3. Oktober 2002, 12:52
 * 
 * @author dennis
 */
public class CsvExporter extends EmptyExporter {

    public static boolean excelmode = true;

    public CsvExporter() {
        // Nothing to do
    }

    @Override
    public String[] getSuffixes() {
        return new String[] { "csv" };
    }

    @Override
    public String getName() {
        return "CSV";
    }

    /**
     * Exportiert die Schwimmer eines Wettkampfes in eine CSV-Datei.
     * 
     * @param os OutputStream
     * @param wk Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    public synchronized <T extends ASchwimmer> boolean results(OutputStream os, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        try {
            TableModel tm = DataTableUtils.results(wk, false, null);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(os, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exportiert die Meldeliste eines Wettkampfes in eine CSV-Datei.
     * 
     * @param name Name der Datei
     * @param wk   Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    @SuppressWarnings({ "unchecked" })
    public synchronized <T extends ASchwimmer> boolean registration(OutputStream name, AWettkampf<T> wk, Feedback fb) {
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
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exportiert die Laufliste eines Wettkampfes in eine CSV-Datei.
     * 
     * @param name Name der Datei
     * @param wk   Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    public synchronized <T extends ASchwimmer> boolean heats(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }

        try {
            TableModel tm = DataTableUtils.heats(wk, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized <T extends ASchwimmer> boolean heatsoverview(OutputStream name, AWettkampf<T> wk, Feedback fb) {
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
            CsvUtils.write(name, tm);
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
    public synchronized <T extends ASchwimmer> boolean referees(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }

        try {
            TableModel tm = DataTableUtils.referees(wk, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized <T extends ASchwimmer> boolean zusatzwertung(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }

        try {
            TableModel tm = DataTableUtils.zusatzwertung(wk, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized <T extends ASchwimmer> boolean startkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }

        try {
            TableModel tm = DataTableUtils.startkarten(wk, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.io.Exporter#isSupported(int)
     */
    @Override
    public boolean isSupported(ImportExportTypes type) {
        return switch (type) {
        case STARTERS -> Utils.isInDevelopmentMode();
        case HEATLIST, ZWLIST, STARTKARTEN, REGISTRATION, RESULTS, ZW_STARTKARTEN, REFEREES, TEAMMEMBERS, BEST_TIMES, ZW_RESULTS, HEATS_OVERVIEW -> true;
        default -> false;
        };
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungStartkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        if (name == null) {
            return false;
        }

        try {
            TableModel tm = DataTableUtils.zusatzwertungStartkarten(wk, PrintUtils.printZWnames, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean teammembers(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        try {
            TableModel tm = DataTableUtils.teammembers(wk, fb);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungResults(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        try {
            TableModel tm = DataTableUtils.zusatzwertungResults(wk, null, fb, true, PrintUtils.printZWnames);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean bestezeiten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        try {
            TableModel tm = DataTableUtils.getSchnellsteZeiten(wk, false);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean heattimes(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        if (wk == null) {
            return false;
        }
        try {
            TableModel tm = DataTableUtils.easywkHeattimes(wk, false);
            if (tm == null) {
                return false;
            }
            CsvUtils.write(name, tm);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean starter(OutputStream os, AWettkampf<T> wk, Feedback fb) {
        if (!(wk instanceof MannschaftWettkampf)) {
            return false;
        }
        List<Object[]> data = new ArrayList<>();
        MannschaftWettkampf mwk = (MannschaftWettkampf) wk;
        for (Mannschaft s : mwk.getSchwimmer()) {
            for (int x = 0; x < s.getAK().getDiszAnzahl(); x++) {
                if (s.isDisciplineChosen(x)) {
                    List<String> row = new ArrayList<>();
                    row.add(StartnumberFormatManager.format(s));
                    row.add(s.getName());
                    row.add(I18n.geschlechtToString(s));
                    row.add(s.getAK().getDisziplin(x, s.isMaennlich()).getName());
                    row.add("0");
                    row.add(isStraightFinal(mwk, s.getAKNummer(), x, s.isMaennlich()) ? "true": "false");
                    int[] starters = s.getStarter(x);
                    for (int starter : starters) {
                        if (starter == 0) {
                            row.add("");
                            continue;
                        }
                        row.add("" + starter);
                    }
                    data.add(row.toArray());
                }
            }
        }
        CsvUtils.write(os,
                new DefaultTableModel(data.toArray(new Object[0][0]),
                        new String[] { "S#", I18n.get("Name"), I18n.get("Sex"), I18n.get("Discipline"), "round","final",
                                "Id1", "Id2", "Id3", "Id4", "Id5", "Id6" }));
        return true;
    }

    private boolean isStraightFinal(MannschaftWettkampf mwk, int akNummer, int disciplineIndex, boolean male) {
        if (!mwk.isHeatBased()) {
            return true;
        }
        return mwk.isFinal(new OWSelection(mwk.getRegelwerk().getAk(akNummer), akNummer, male, disciplineIndex, 0 ));
    }
}