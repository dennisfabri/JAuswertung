package de.df.jauswertung.daten;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import de.df.jauswertung.daten.kampfrichter.Kampfrichter;
import de.df.jauswertung.daten.kampfrichter.KampfrichterStufe;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.GroupEvaluationMode;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Skalierungsmodus;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.daten.regelwerk.Startunterlagen;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.daten.regelwerk.StrafenKapitel;
import de.df.jauswertung.daten.regelwerk.StrafenParagraph;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;

/**
 * Competitions that contain every class of the model, with values that differ from the defaults and with objects
 * shared the way the application shares them (swimmers in heats, finish judge decisions and open water heats).
 * <p>
 * <b>Update this class whenever the model changes:</b> a new class or a new field in {@code de.df.jauswertung.daten}
 * must be reachable and filled here. {@code VollstaendigesModellKopieTest} fails for a model class that is missing.
 */
public final class VollstaendigesModell {

    private static final int BAHNEN = 4;

    private VollstaendigesModell() {
    }

    public static EinzelWettkampf einzel() {
        EinzelWettkampf wk = new EinzelWettkampf(regelwerk(true), strafen());
        gemeinsam(wk);
        Teilnehmer a = wk.createTeilnehmer("", "Albers", "Anna", 2001, false, "Gliederung A", 0, "Bemerkung");
        Teilnehmer b = wk.createTeilnehmer("", "Berg", "Bernd", 1999, true, "Gliederung B", 1, "");
        a.setCompetitorId("C-1");
        b.setCompetitorId("C-2");
        schwimmer(wk, a, 0);
        schwimmer(wk, b, 1);
        wettkampfdaten(wk);
        return wk;
    }

    public static MannschaftWettkampf mannschaft() {
        MannschaftWettkampf wk = new MannschaftWettkampf(regelwerk(false), strafen());
        gemeinsam(wk);
        Mannschaft a = wk.createMannschaft("Team A", false, "Gliederung A", 0, "Bemerkung");
        Mannschaft b = wk.createMannschaft("Team B", true, "Gliederung B", 1, "");
        mitglieder(a);
        mitglieder(b);
        a.setStarter(0, new int[] { 1, 2, 3, 4 });
        schwimmer(wk, a, 0);
        schwimmer(wk, b, 1);
        wk.createTeammembersRegistrationsId();
        wettkampfdaten(wk);
        return wk;
    }

    private static Regelwerk regelwerk(boolean einzel) {
        Regelwerk aks = new Regelwerk(2, einzel, FormelDLRG2007.ID);
        aks.setBeschreibung("Regelwerk");
        for (int x = 0; x < aks.size(); x++) {
            Disziplin[] disziplinen = { new Disziplin("50m A " + x, 3000 + x, 50, 1),
                    new Disziplin("100m B " + x, 9000 + x, 100, 2) };
            Altersklasse ak = new Altersklasse("AK " + x, new Disziplin[][] { disziplinen, disziplinen }, x == 1);
            ak.setMinimumAlter(10 + x);
            ak.setMaximumAlter(30 + x);
            ak.setStrafeIstDisqualifikation(true);
            ak.setLaufrotation(true);
            ak.setStartgruppe("Startgruppe");
            ak.setWertungsgruppe("Wertungsgruppe");
            if (!einzel) {
                ak.setMemberCounts(4, 5);
            }
            aks.setAk(x, ak);
        }
        aks.setStartgruppen(new Startgruppe[] { new Startgruppe("Startgruppe") });
        Wertungsgruppe wertungsgruppe = new Wertungsgruppe("Wertungsgruppe");
        wertungsgruppe.setProtokollMitMehrkampfwertung(true);
        aks.setWertungsgruppen(new Wertungsgruppe[] { wertungsgruppe });
        aks.setTranslation("Male", "Männlich");
        aks.setGesamtwertung(true);
        aks.setGesamtwertungsmodus(GroupEvaluationMode.Best);
        aks.setGesamtwertungSkalieren(Skalierungsmodus.ANZAHL_DISZIPLINEN);
        aks.setZusatzwertung("Herz-Lungen-Wiederbelebung");
        aks.setZusatzwertungKurz("HLW");
        aks.setZusatzwertungBasispunkte(300);
        aks.getAk(0).getInterneStartgruppe();
        return aks;
    }

    private static Strafen strafen() {
        Strafen strafen = new Strafen();
        StrafenKapitel kapitel = new StrafenKapitel(1);
        kapitel.setName("Kapitel");
        StrafenParagraph paragraph = new StrafenParagraph(2);
        paragraph.setName("Paragraph");
        paragraph.setPar("§ 2");
        paragraph.addStrafe(new Strafe("Frühstart", "F", Strafarten.STRAFPUNKTE, 100));
        kapitel.addParagraph(paragraph);
        strafen.addKapitel(kapitel);
        return strafen;
    }

    private static void gemeinsam(AWettkampf<?> wk) {
        wk.setProperty(PropertyConstants.HEATS_LANES, BAHNEN);
        wk.setProperty(PropertyConstants.SHORTNAME, "Kurzname");
        wk.setProperty(PropertyConstants.ART_DES_WETTKAMPFS, Wettkampfart.Landesmeisterschaften);
        wk.setProperty(PropertyConstants.DSM_MODE_DATA, new int[][][] { { { 1, 0 } } });
        Hashtable<String, boolean[]> bahnen = new Hashtable<>();
        bahnen.put("50m A 0", new boolean[] { false, true, true, true });
        wk.setProperty(PropertyConstants.HEATS_LANE_SELECTION, bahnen);
        wk.setLogo(new byte[] { 1, 2, 3 });

        KampfrichterVerwaltung kv = new KampfrichterVerwaltung();
        kv.addEinheit("Einheit");
        kv.getEinheit(0).addPosition("Schiedsrichter", KampfrichterStufe.E1, 1);
        kv.getEinheit(0).addKampfrichter("Schiedsrichter",
                new Kampfrichter("Kampfrichter", "Gliederung", "Bemerkung", KampfrichterStufe.E2));
        wk.setKampfrichterverwaltung(kv);

        wk.setFilter(new Filter[] { new Filter("Alle", null), new Filter("Gliederung", new String[] { "Gliederung A" }) });
        wk.setCurrentFilterIndex(1);

        Timelimits timelimits = new Timelimits();
        timelimits.setName("Pflichtzeiten");
        timelimits.setShortname("PZ");
        timelimits.setCheck(Timelimitchecktype.UPPER_LIMIT);
        timelimits.setLimits(new Timelimit[] { new Timelimit("50m A 0", 6000, 10, 30, "AK 0", true) });
        wk.getTimelimits().add(timelimits);

        wk.setZulassungslimits(new int[][] { { 1, 2 }, { 3, 4 } });
        wk.setDirektqualifizierte(new int[][] { { 5, 6 }, { 7, 8 } });
        wk.setNachruecker(new int[][] { { 9, 10 }, { 11, 12 } });
    }

    private static void mitglieder(Mannschaft m) {
        for (int x = 0; x < 4; x++) {
            Mannschaftsmitglied mitglied = m.getMannschaftsmitglied(x);
            mitglied.setVorname("Vorname " + x);
            mitglied.setNachname(m.getName());
            mitglied.setGeschlecht(x % 2 == 0 ? Geschlecht.weiblich : Geschlecht.maennlich);
            mitglied.setJahrgang(2000 + x);
            mitglied.setImportId("M-" + x);
        }
    }

    private static <T extends ASchwimmer> void schwimmer(AWettkampf<T> wk, T s, int nummer) {
        s.setStartnummer(nummer + 1);
        s.setImportId("I-" + nummer);
        s.setMeldePlatz(nummer + 1);
        s.setMeldepunkte(0, 123.5);
        s.setMeldezeit(0, 4500);
        s.setZeit(0, 4000 + nummer);
        s.setZeit(1, 9500);
        s.setStrafen(0, new LinkedList<>(List.of(wk.getStrafen().getDisqualifiziert())));
        s.setAllgemeineStrafen(new LinkedList<>(List.of(new Strafe("Allgemein", "A", Strafarten.STRAFPUNKTE, 50))));
        s.setHLWState(0, HLWStates.ENTERED);
        s.setHLWPunkte(0, 200);
        s.setAusserKonkurrenz(nummer == 1);
        s.setDopingkontrolle(nummer == 0);
        s.setQualifikation(Qualifikation.QUALIFIZIERT);
        s.setStartunterlagen(Startunterlagen.PRUEFEN);
        wk.addSchwimmer(s);
    }

    @SuppressWarnings("unchecked")
    private static <T extends ASchwimmer> void wettkampfdaten(AWettkampf<T> wk) {
        Laufliste<T> laufliste = wk.getLaufliste();
        Laufliste.BlockEinteilung[] blocks = laufliste.getStandardBlocks();
        laufliste.erzeugen(blocks.length > 1 ? new Laufliste.BlockEinteilung[] { blocks[1], blocks[0] } : blocks);
        Laufliste.Einteilung[] verteilung = laufliste.getStandardVerteilung();
        if (verteilung.length > 1) {
            laufliste.erzeugen(new Laufliste.Einteilung[] { verteilung[1], verteilung[0] });
        }
        Lauf<T> lauf = laufliste.getLaufliste().getFirst();
        lauf.setLeftToRight(true);

        T erster = wk.getSchwimmer().getFirst();
        T zweiter = wk.getSchwimmer().getLast();
        Zielrichterentscheid<T> ze = new Zielrichterentscheid<>();
        ze.setDisziplin(0);
        ze.addSchwimmer(erster);
        ze.addSchwimmer(zweiter);
        wk.setZielrichterentscheide(new LinkedList<>(List.of(ze)));

        OWDisziplin<T> disziplin = wk.getLauflisteOW().addDisziplin(0, erster.isMaennlich(), 0, 0);
        disziplin.addSchwimmer(erster);
        Lauf<T> owLauf = new Lauf<>(BAHNEN, 1, 0, null);
        owLauf.setSchwimmer(erster, 0, 0);
        disziplin.laeufe.add(new OWLauf<>(wk, disziplin.Id, owLauf));
        erster.setZeit(disziplin.Id, 3900);
        erster.setStarter(disziplin.Id, new int[] { 1 });
        erster.addStrafe(disziplin.Id, new Strafe("Runde", "R", Strafarten.DISQUALIFIKATION, 0));
    }
}
