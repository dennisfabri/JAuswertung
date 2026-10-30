package de.df.jauswertung.util.valueobjects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;

class TeamCodeTest {

    /** Created before TeamCode existed, by {@code Team.getCode()}, for {@link #mannschaft()} without export id. */
    private static final String JA00 = "JA00785e33b43604426365031bdfd2e292d4221bc7bcbc441b431b230303431b65431ba7d4cce282ccd41c20a3282fc5c6c0c6d0d2d2d2a6c25cd9d82624b5b8c4c639a328b3d8c608a4ded846d9d8c5cfd92cd81d0018301849";

    /** Created before TeamCode existed, by {@code Team.getCode()}, for {@link #mannschaft()} with export id "DE-123". */
    private static final String JA01 = "JA01785e33b4360442636b17575d43236365031bdfd2e292d4221bc7bcbc441b431b230303431b65431ba7d4cce282ccd41c20a3282fc5c6c0c6d0d2d2d2a6c25cd9d82624b5b8c4c639a328b3d8c608a4ded846d9342cc43d20c40900a0f91a04";

    @Test
    void schreibtJA00OhneExportId() {
        assertEquals(JA00, TeamCode.to(new Team(mannschaft(), "")));
    }

    @Test
    void schreibtJA01MitExportId() {
        assertEquals(JA01, TeamCode.to(new Team(mannschaft(), "DE-123")));
    }

    @Test
    void liestJA00() throws IOException {
        Team team = TeamCode.from(JA00);

        assertTrue(team.isExportIdEmpty());
        assertKopfUndMitglieder(team);
    }

    @Test
    void liestJA01() throws IOException {
        Team team = TeamCode.from(JA01);

        assertEquals("DE-123", team.getExportId());
        assertKopfUndMitglieder(team);
    }

    @Test
    void lehntUnbekanntesFormatAb() {
        assertThrows(IOException.class, () -> TeamCode.from("JA02" + JA00.substring(4)));
    }

    @Test
    void lehntFalschePruefsummeAb() {
        String kaputt = JA00.substring(0, JA00.length() - 2) + "00";
        assertThrows(Exception.class, () -> TeamCode.from(kaputt));
    }

    private static void assertKopfUndMitglieder(Team team) {
        assertAll(
                () -> assertEquals(1, team.getId()),
                () -> assertEquals(1, team.getAk()),
                () -> assertTrue(team.isMale()),
                () -> assertEquals(3, team.getMemberCount()),
                () -> assertMitglied(team.getMember(0), "Anna", "Muster", Geschlecht.maennlich, 2001, ""),
                () -> assertMitglied(team.getMember(1), "Bernd", "Beispiel", Geschlecht.weiblich, 1999, "x7"),
                () -> assertMitglied(team.getMember(2), "Chris", "Test", Geschlecht.unbekannt, 2003, ""),
                () -> assertEquals(3, team.getMember(2).getIndex()));
    }

    private static void assertMitglied(Teammember member, String vorname, String nachname, Geschlecht geschlecht,
            int jahrgang, String importId) {
        assertAll(
                () -> assertFalse(member.isEmpty()),
                () -> assertEquals(vorname, member.getFirstname()),
                () -> assertEquals(nachname, member.getLastname()),
                () -> assertEquals(geschlecht, member.getGeschlecht()),
                () -> assertEquals(jahrgang, member.getJahrgang()),
                () -> assertEquals(importId, member.getImportId()));
    }

    /** Member 2 stays empty and is left out of the code; the last name of member 1 contains forbidden characters. */
    private static Mannschaft mannschaft() {
        Regelwerk regelwerk = new Regelwerk(2, false, FormelDLRG2007.ID);
        for (int x = 0; x < regelwerk.size(); x++) {
            Disziplin[] disziplinen = { new Disziplin("4x50m", 6000, 50, 4) };
            regelwerk.setAk(x, new Altersklasse("AK " + x, new Disziplin[][] { disziplinen, disziplinen }, false));
        }
        MannschaftWettkampf wk = new MannschaftWettkampf(regelwerk, new Strafen());
        Mannschaft m = wk.createMannschaft("Team", true, "Gliederung", 1, "");
        wk.addSchwimmer(m);
        mitglied(m, 0, "Anna", "Muster", Geschlecht.maennlich, 2001, "");
        mitglied(m, 1, "Bernd", "Bei<sp;iel", Geschlecht.weiblich, 1999, "x7");
        mitglied(m, 3, "Chris", "Test", Geschlecht.unbekannt, 2003, "");
        return m;
    }

    private static void mitglied(Mannschaft m, int index, String vorname, String nachname, Geschlecht geschlecht,
            int jahrgang, String importId) {
        Mannschaftsmitglied mitglied = m.getMannschaftsmitglied(index);
        mitglied.setVorname(vorname);
        mitglied.setNachname(nachname);
        mitglied.setGeschlecht(geschlecht);
        mitglied.setJahrgang(jahrgang);
        mitglied.setImportId(importId);
    }
}
