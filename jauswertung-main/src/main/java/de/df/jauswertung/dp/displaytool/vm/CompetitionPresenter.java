package de.df.jauswertung.dp.displaytool.vm;

import java.util.Arrays;

import de.df.jauswertung.dp.displaytool.data.Competition;

public class CompetitionPresenter {

    private Competition competition = new Competition("");

    public PresentationRow[] getRows() {
        return Arrays.stream(competition.getCompetitors()).map(c -> new PresentationRow(c))
                .toArray(PresentationRow[]::new);
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
        this.competition.sort();
    }

    public Object[][] getTable() {
        return Arrays.stream(competition.getCompetitors()).map(c -> c.getRow()).toArray(Object[][]::new);
    }

    public String getName() {
        return competition.getName();
    }
}