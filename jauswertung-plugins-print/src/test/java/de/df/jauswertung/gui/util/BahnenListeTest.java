package de.df.jauswertung.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;

class BahnenListeTest {

    private static final int LANE = 2;
    private static final String TEAM = "DLRG Musterstadt";

    private MannschaftWettkampf wk;

    @BeforeEach
    void setUp() {
        Disziplin[] disziplinen = { new Disziplin("A", 1, 50, 1), new Disziplin("B", 1, 50, 1),
                new Disziplin("C", 1, 50, 1) };
        Regelwerk regelwerk = new Regelwerk(1, false, "");
        regelwerk.setAk(0, new Altersklasse("AK", new Disziplin[][] { disziplinen, disziplinen }, false));

        wk = new MannschaftWettkampf(regelwerk, new Strafen());
        wk.setProperty(PropertyConstants.RESULT_MULTILINE, true);
        wk.setProperty(PropertyConstants.HEATS_LANES, 2);
    }

    @Test
    void showsStartersOfTheDisciplineInTheLane() {
        Mannschaft team = teamWithMembers();
        heat().setSchwimmer(team, 1, LANE - 1);

        assertEquals("Gamma, Delta", name(0));
    }

    @Test
    void showsStartersOfTheDisciplineInTheLaneInHeatsBeyondTheNumberOfLanes() {
        Mannschaft team = teamWithMembers();
        heat();
        heat();
        heat().setSchwimmer(team, 2, LANE - 1);

        assertEquals("Alpha, Gamma", name(2));
    }

    @Test
    void showsTeamNameWhenNoMembersHaveBeenEntered() {
        Mannschaft team = wk.createMannschaft(TEAM, false, TEAM, 0, "");
        wk.addSchwimmer(team);
        heat().setSchwimmer(team, 1, LANE - 1);

        assertEquals(TEAM, name(0));
    }

    private Mannschaft teamWithMembers() {
        Mannschaft team = wk.createMannschaft(TEAM, false, TEAM, 0, "");
        String[] nachnamen = { "Alpha", "Beta", "Gamma", "Delta" };
        for (int x = 0; x < nachnamen.length; x++) {
            team.getMannschaftsmitglied(x).setVorname("Vorname" + x);
            team.getMannschaftsmitglied(x).setNachname(nachnamen[x]);
        }
        team.setStarter(0, new int[] { 1, 2 });
        team.setStarter(1, new int[] { 3, 4 });
        team.setStarter(2, new int[] { 1, 3 });
        wk.addSchwimmer(team);
        return team;
    }

    private Lauf<Mannschaft> heat() {
        return wk.getLaufliste().add(Integer.MAX_VALUE);
    }

    private String name(int heat) {
        return new BahnenListe<>(wk).toJTable(LANE).getModel().getValueAt(heat, 1).toString();
    }
}
