package de.df.jauswertung.gui.plugins;

import java.util.List;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JTimeField;

interface TimeInputAdapter {
    ASchwimmer getSchwimmer();

    JIntegerField getInputField();

    JTimeField getTimeField();

    void moveUp();

    void moveDown();

    void updateTime();

    boolean checkHighPoints();

    int getIndex();

    int getDiscipline();

    void zeigeZieleinlauf();

    boolean setStrafen(List<Strafe> penalties);

    void updatePenalty();

    void addStrafe(Strafe penalty);

    void runMeanTimeEditor();

    boolean isByTimes();

    void runPenaltyPoints();

    void runPenaltyCode();

    @SuppressWarnings("rawtypes")
    AWettkampf getCompetition();
}