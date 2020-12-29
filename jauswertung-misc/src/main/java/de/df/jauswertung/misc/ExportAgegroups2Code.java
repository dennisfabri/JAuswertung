package de.df.jauswertung.misc;

import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.util.AltersklassenUtils;

public class ExportAgegroups2Code {

    public static void main(String[] args) {
        WriteAG(false);
        WriteAG(true);
    }

    private static void WriteAG(boolean einzel) {
        Regelwerk rwe = AltersklassenUtils.getDefaultAKs(einzel);

        System.out.format("  Agegroup ag;\n");
        System.out.format("  Discipline d;\n");

        for (Altersklasse ak : rwe.getAks()) {
            System.out.format("  ag = new Agegroup() { Name = \"%s\", IsTeam = %s};\n", ak.getName(), einzel ? 0 : 1);
            System.out.format("  uow.Agegroups.Add(ag);\n");
            System.out.format("");
            for (Disziplin d : ak.getDisziplinen(true)) {
                System.out.format("  d = new Discipline() { Name = \"%s\", Agegroup = ag };\n", d.getName());
                System.out.format("  uow.Disciplines.Add(d);\n");
            }
        }

    }

}
