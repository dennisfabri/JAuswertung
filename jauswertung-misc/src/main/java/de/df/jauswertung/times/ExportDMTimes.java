package de.df.jauswertung.times;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.InputManager;

public class ExportDMTimes {

    public static void main(String[] args) {
        System.out.format("  Competition c;\n");
        System.out.format("  Entry e;\n");
        System.out.format("  Time t;\n");

        ExportTimes("required/misc/results/dm2007-mannschaft.wk", "DM2007", "22.09.2007");
        ExportTimes("required/misc/results/dm2007-einzel.wk", "DM2007", "23.09.2007");
    }

    private static <T extends ASchwimmer> void ExportTimes(String filename, String name, String datum) {
        AWettkampf<T> wk = InputManager.ladeWettkampf(filename);

        System.out.format("  c = new Competition() { Name = \"%s\" };\n", name);
        System.out.format("  uow.Competitions.Add(c);");
        for (T s : wk.getSchwimmer()) {
            if (s.getAllgemeineStrafen().isEmpty()) {
                String entryname = s.getName();
                if (s instanceof Mannschaft) {
                    entryname = String.format("%s (%s)", s.getName(), I18n.getAgeGroupAsStringShort(s));
                }
                System.out.format("  e = new Entry() { Name = \"%s\", Organization = \"%s\", Competition = c };\n", entryname, s.getGliederungMitQGliederung());
                System.out.format("  uow.Entries.Add(e);\n");
                for (int x = 0; x < s.getAK().getDiszAnzahl(); x++) {
                    if (s.isDisciplineChosen(x) && s.getAkkumulierteStrafe(x).getArt() != Strafarten.NICHT_ANGETRETEN) {
                        int time = s.getZeit(x);
                        String penalty = I18n.getPenaltyShort(s.getAkkumulierteStrafe(x));
                        System.out.format(
                                "  t = new Time() { DisciplineId = GetDisciplineId(uow, \"%s\", %s, \"%s\"), Value = %s, Penalty = \"%s\", Moment = GetDate(\"%s\") };\n",
                                s.getAK().getName(), s instanceof Mannschaft, s.getAK().getDisziplin(x, true).getName(), time, penalty, datum);
                        System.out.format("  if (t.DisciplineId > 0) { uow.Times.Add(t); }\n");
                    }
                }
                System.out.format("  uow.SaveChanges();\n");
            }
        }
    }

}
