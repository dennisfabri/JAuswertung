package de.df.jauswertung.misc;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import lombok.Value;

public class ExportStarters {
    public static void main(String[] args)
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
        generate("JRP Mannschaft Ocean.wk", "Mannschaft Ocean.csv");
        generate("JRP Mannschaft Ocean Mixed.wk", "Mannschaft Ocean Mixed.csv");
        generate("JRP Mannschaft Pool.wk", "Mannschaft Pool.csv");
        generate("JRP Mannschaft Pool Mixed.wk", "Mannschaft Pool Mixed.csv");
    }

    @Value
    public static class Team {
        @CsvBindByName(column = "S#")
        private String startnumber;
        @CsvBindByName(column = "Geschlecht")
        private String gender;
        @CsvBindByName(column = "Gliederung")
        private String organization;
        @CsvBindByName(column = "Disziplin")
        private String discipline;
        @CsvBindByName(column = "Id1")
        private String id1;
        @CsvBindByName(column = "Id2")
        private String id2;
        @CsvBindByName(column = "Id3")
        private String id3;
        @CsvBindByName(column = "Id4")
        private String id4;
    }

    private static void generate(String input, String output)
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
        MannschaftWettkampf wk = (MannschaftWettkampf) InputManager.ladeWettkampf("../../../../data/jrp/" + input);
        write(extractStartsFrom(wk), Path.of("..", "..", "..", "..", "data", "jrp", output));
    }

    private static List<Team> extractStartsFrom(MannschaftWettkampf wk) {
        List<Team> teams = new ArrayList<>();
        for (Mannschaft m : wk.getSchwimmer()) {
            for (int x = 0; x < m.getAK().getDiszAnzahl(); x++) {
                if (m.isDisciplineChosen(x)) {
                    Team team = new Team(StartnumberFormatManager.format(m), I18n.geschlechtToString(m), m.getName(),
                            m.getAK().getDisziplinenNamen()[x],
                            formatSN(m, x, 0), formatSN(m, x, 1), formatSN(m, x, 2), formatSN(m, x, 3));
                    teams.add(team);
                }
            }
        }
        return teams;
    }

    private static String formatSN(Mannschaft m, int disziplin, int index) {
        int[] indizes = m.getStarter(disziplin);
        if (indizes == null || indizes.length <= index) {
            return "";
        }
        if (indizes[index] <= 0) {
            return "";
        }
        int team = m.getStartnummer() / 10;
        int starter = indizes[index] + (m.isMaennlich() ? 6 : 0);
        return "" + team + "-" + (starter < 10 ? "0" : "") + starter;
    }

    private static void write(List<Team> teams, Path path)
            throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        try (Writer writer = new FileWriter(path.toString(), StandardCharsets.UTF_8)) {

            new StatefulBeanToCsvBuilder<Team>(writer)
                    .withQuotechar('\"')
                    .withSeparator(';').build().write(teams);
        }
    }
}
