package de.df.jauswertung.misc;

import java.util.HashSet;

import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.util.AltersklassenUtils;

public class ExportSQL {

    public static void main(String[] args) {
        WriteAG(false);
        WriteAG(true);
    }

    private static HashSet<String> disciplines = new HashSet<>();

    private static void WriteAG(boolean einzel) {
        Regelwerk rwe = AgeGroupIOUtils.getDefaultAKs(einzel);

        for (Altersklasse ak : rwe.getAks()) {
            System.out.format(
                    " INSERT INTO [dbo].[Agegroups] ([Name], [Description], [IsTeam]) VALUES ('%s', NULL, %s);\n",
                    ak.getName(), einzel ? 0 : 1);
            for (Disziplin d : ak.getDisziplinen(true)) {
                if (!disciplines.contains(d.getName())) {
                    System.out.format("    INSERT INTO [dbo].[Disciplines] ([Name], [Distance]) VALUES ('%s', %s);\n",
                            d.getName(), 50);
                    disciplines.add(d.getName());
                }
                System.out.format(
                        "    INSERT INTO [dbo].[AgegroupDisciplines] ([AgegroupId] ,[DisciplineId]) SELECT a.Id, d.Id FROM Agegroups a INNER JOIN Disciplines d ON a.Name = '%s' and a.IsTeam = %s AND d.Name = '%s';\n",
                        ak.getName(), einzel ? 0 : 1, d.getName());
            }
            System.out.println();
        }
    }
}