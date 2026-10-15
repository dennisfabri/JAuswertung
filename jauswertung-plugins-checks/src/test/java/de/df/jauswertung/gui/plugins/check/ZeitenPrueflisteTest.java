package de.df.jauswertung.gui.plugins.check;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;
import de.df.jauswertung.util.ergebnis.FormelILSFinals;

class ZeitenPrueflisteTest {

    private static final int REC = 6000;
    private static final int NORMAL = 7000;
    private static final int SCHNELL = 5000;
    private static final int LANGSAM = 20000;

    private EinzelWettkampf wk;

    @BeforeEach
    void wettkampf() {
        wk = wettkampf(FormelDLRG2007.ID);
    }

    private static EinzelWettkampf wettkampf(String formel) {
        Disziplin[] disziplinen = { new Disziplin("A", REC, 50, 1), new Disziplin("B", REC, 50, 1) };
        Regelwerk regelwerk = new Regelwerk(1, true, formel);
        Altersklasse ak = new Altersklasse("AK", new Disziplin[][] { disziplinen, disziplinen }, false);
        ak.setDisciplineChoiceAllowed(true);
        regelwerk.setAk(0, ak);
        return new EinzelWettkampf(regelwerk, new Strafen());
    }

    private static Teilnehmer schwimmer(EinzelWettkampf wk, Zeitart art, String name, int zeitA, int zeitB) {
        Teilnehmer t = wk.createTeilnehmer("", name, "Vorname", 2000, false, "Gliederung", 0, "");
        t.setDisciplineChoice(0, true);
        t.setDisciplineChoice(1, true);
        art.setZeit(t, 0, zeitA);
        art.setZeit(t, 1, zeitB);
        wk.addSchwimmer(t);
        return t;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static ZeitenPruefliste gebaut(Zeitart art, AWettkampf wk) {
        ZeitenPruefliste liste = new ZeitenPruefliste(art);
        liste.baue(wk);
        return liste;
    }

    private static List<String> eintraege(ZeitenPruefliste liste, int disziplin) {
        return liste.getEintraege(disziplin).stream().map(e -> ((Teilnehmer) e.schwimmer()).getNachname() + ":" + e.status()).toList();
    }

    @ParameterizedTest
    @EnumSource(Zeitart.class)
    void listetFehlendeUndUnplausibleZeitenSortiertNachName(Zeitart art) {
        schwimmer(wk, art, "Delta", NORMAL, NORMAL);
        schwimmer(wk, art, "Charlie", LANGSAM, NORMAL);
        schwimmer(wk, art, "Alpha", 0, NORMAL);
        schwimmer(wk, art, "Bravo", SCHNELL, 0);

        ZeitenPruefliste liste = gebaut(art, wk);

        assertEquals(2, liste.getDisziplinen());
        assertEquals(List.of("Alpha:NONE", "Bravo:FAST", "Charlie:SLOW"), eintraege(liste, 0));
        assertEquals(List.of("Bravo:NONE"), eintraege(liste, 1));
    }

    @ParameterizedTest
    @EnumSource(Zeitart.class)
    void ohneAuffaelligeZeitenIstDieListeLeer(Zeitart art) {
        schwimmer(wk, art, "Alpha", NORMAL, NORMAL);

        assertEquals(0, gebaut(art, wk).getDisziplinen());
    }

    @ParameterizedTest
    @EnumSource(Zeitart.class)
    void nichtGewaehlteDisziplinWirdNichtGelistet(Zeitart art) {
        Teilnehmer t = schwimmer(wk, art, "Alpha", NORMAL, 0);
        t.setDisciplineChoice(1, false);

        assertEquals(0, gebaut(art, wk).getDisziplinen());
    }

    @ParameterizedTest
    @EnumSource(Zeitart.class)
    void nachBearbeitungUnplausibleZeitWirdAufgenommen(Zeitart art) {
        schwimmer(wk, art, "Alpha", 0, NORMAL);
        Teilnehmer t = schwimmer(wk, art, "Bravo", NORMAL, NORMAL);
        ZeitenPruefliste liste = gebaut(art, wk);

        art.setZeit(t, 0, SCHNELL);
        liste.pruefe(wk, t);

        assertEquals(List.of("Alpha:NONE", "Bravo:FAST"), eintraege(liste, 0));
    }

    @ParameterizedTest
    @EnumSource(Zeitart.class)
    void nachBearbeitungFehlendeZeitWirdAufgenommen(Zeitart art) {
        schwimmer(wk, art, "Alpha", 0, NORMAL);
        Teilnehmer t = schwimmer(wk, art, "Bravo", NORMAL, NORMAL);
        ZeitenPruefliste liste = gebaut(art, wk);

        art.setZeit(t, 0, 0);
        liste.pruefe(wk, t);

        assertEquals(List.of("Alpha:NONE", "Bravo:NONE"), eintraege(liste, 0));
    }

    @ParameterizedTest
    @EnumSource(Zeitart.class)
    void nachBearbeitungPlausibleZeitBleibtMitNeuemStatus(Zeitart art) {
        Teilnehmer t = schwimmer(wk, art, "Alpha", 0, NORMAL);
        ZeitenPruefliste liste = gebaut(art, wk);

        art.setZeit(t, 0, NORMAL);
        liste.pruefe(wk, t);

        assertEquals(List.of("Alpha:NORMAL"), eintraege(liste, 0));
    }

    @ParameterizedTest
    @EnumSource(Zeitart.class)
    void nachBearbeitungGeaenderterStatusWirdUebernommen(Zeitart art) {
        Teilnehmer t = schwimmer(wk, art, "Alpha", 0, NORMAL);
        ZeitenPruefliste liste = gebaut(art, wk);

        art.setZeit(t, 0, LANGSAM);
        liste.pruefe(wk, t);

        assertEquals(List.of("Alpha:SLOW"), eintraege(liste, 0));
    }

    @ParameterizedTest
    @EnumSource(Zeitart.class)
    void nachAbwahlDerDisziplinWirdEntfernt(Zeitart art) {
        schwimmer(wk, art, "Alpha", 0, NORMAL);
        Teilnehmer t = schwimmer(wk, art, "Bravo", 0, NORMAL);
        ZeitenPruefliste liste = gebaut(art, wk);

        t.setDisciplineChoice(0, false);
        liste.pruefe(wk, t);

        assertEquals(List.of("Alpha:NONE"), eintraege(liste, 0));
    }

    @ParameterizedTest
    @EnumSource(Zeitart.class)
    void zeitenBeiWettkaempfenMitLaeufenProRundeWerdenNichtGeprueft(Zeitart art) {
        EinzelWettkampf runden = wettkampf(FormelILSFinals.ID);
        schwimmer(runden, art, "Alpha", 0, NORMAL);

        ZeitenPruefliste liste = gebaut(art, runden);

        assertEquals(art == Zeitart.ZEIT, liste.getDisziplinen() == 0);
    }
}
