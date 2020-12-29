package de.df.jauswertung.misc;

import static de.df.jauswertung.daten.PropertyConstants.*;

import java.text.NumberFormat;
import java.util.*;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.kampfrichter.*;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.io.*;
import de.df.jauswertung.util.AltersklassenUtils;
import de.df.jutils.util.RandomUtils;

/**
 * @author Dennis Mueller
 */
public final class ErzeugeWettkaempfe {

    private static Random rng = RandomUtils.getRandomNumberGenerator(RandomUtils.Generators.MersenneTwister);

    private ErzeugeWettkaempfe() {
        // Hide constructor
    }

    private static Strafe getStrafe(Strafen sn, boolean ausg) {
        int anzahl = 0;
        for (StrafenKapitel sk : sn.getKapitel()) {
            for (StrafenParagraph sp : sk.getParagraphen()) {
                for (Strafe s : sp.getStrafen()) {
                    if (ausg) {
                        if (s.getArt() == Strafarten.AUSSCHLUSS) {
                            anzahl++;
                        }
                    } else {
                        if (s.getArt() != Strafarten.AUSSCHLUSS) {
                            anzahl++;
                        }
                    }
                }
            }
        }
        int index = rng.nextInt(anzahl);
        anzahl = 0;
        for (StrafenKapitel sk : sn.getKapitel()) {
            for (StrafenParagraph sp : sk.getParagraphen()) {
                for (Strafe s : sp.getStrafen()) {
                    if (ausg) {
                        if (s.getArt() == Strafarten.AUSSCHLUSS) {
                            if (index == anzahl) {
                                return s;
                            }
                            anzahl++;
                        }
                    } else {
                        if (s.getArt() != Strafarten.AUSSCHLUSS) {
                            if (index == anzahl) {
                                return s;
                            }
                            anzahl++;
                        }
                    }
                }
            }
        }
        return Strafe.NICHTS;
    }

    private static <T extends ASchwimmer> int getJahrgang(AWettkampf<T> wk, int akx) {
        Altersklasse ak = wk.getRegelwerk().getAk(akx);
        int min = ak.getMinimumAlter();
        if (min < 10) {
            min = 10;
        }
        int range = ak.getMaximumAlter() - min;
        if (range <= 0) {
            range = 5;
        }
        return Calendar.getInstance().get(Calendar.YEAR) - min - rng.nextInt(range);
    }

    public static EinzelWettkampf einzel(Namengenerator n, String datei, int anzahl, int zeitenbis) {
        if (n == null) {
            n = new Namengenerator(null, anzahl);
        }

        long time = System.currentTimeMillis();

        EinzelWettkampf ewk = new EinzelWettkampf(AltersklassenUtils.getDefaultAKs(true),
                InputManager.ladeStrafen(null, true));
        // ewk.setLogo(IconManager.getLogoImage());
        int lanes = 6;
        if (anzahl > 20) {
            lanes = 8;
        }
        ewk.setProperty(PropertyConstants.HEATS_LANES, lanes);
        erzeugeKampfrichter(ewk, n);
        erzeugeTexte(ewk, n);

        for (int a = 0; a < 5; a++) {
            for (int g = 0; g < 2; g++) {
                for (int x = 0; x < anzahl; x++) {
                    Teilnehmer tn = ewk.createTeilnehmer(n.generateNachname(), n.generateVorname(g == 1),
                            getJahrgang(ewk, a), g == 1, n.generateGliederung(), a, "");
                    tn.setQualifikationsebene(n.generateLV(tn.getGliederung()));
                    ewk.addSchwimmer(tn);
                    if (tn.getAK().isDisciplineChoiceAllowed()) {
                        int d = tn.getAK().getMinimalChosenDisciplines()
                                + rng.nextInt(tn.getAK().getMaximalChosenDisciplines()
                                        - tn.getAK().getMinimalChosenDisciplines() + 1);
                        while (tn.getDisciplineChoiceCount() < d) {
                            int index = rng.nextInt(tn.getAK().getDiszAnzahl());
                            tn.setDisciplineChoice(index, true);
                        }
                    }
                    Altersklasse ak = tn.getAK();
                    int disz = ak.getDiszAnzahl();
                    if (zeitenbis >= 0) {
                        disz = Math.min(disz, zeitenbis);
                    }
                    double factor = 1.75 * rng.nextDouble();
                    for (int i = 0; i < disz; i++) {
                        if (tn.isDisciplineChosen(i)) {
                            double result = (0.98 + factor + 0.25 * rng.nextDouble())
                                    * ak.getDisziplin(i, g == 1).getRec();
                            tn.setZeit(i, (int) result);
                            tn.setMeldezeit(i, (int) Math.round(tn.getZeit(i) * (0.95 + rng.nextDouble() * 0.1)));
                            if (rng.nextDouble() < 0.1) {
                                tn.addStrafe(i, getStrafe(ewk.getStrafen(), false));
                            }
                        }
                    }
                    if (rng.nextDouble() < 0.01) {
                        tn.addStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF, getStrafe(ewk.getStrafen(), true));
                    }
                    if (ak.hasHLW()) {
                        for (int i = 0; i < tn.getMaximaleHLW(); i++) {
                            if (rng.nextDouble() < 0.01) {
                                tn.setHLWState(i, HLWStates.NICHT_ANGETRETEN);
                            } else {
                                tn.setHLWPunkte(i, getHLWPunkte());
                            }
                        }
                    }
                    tn.setMeldepunkte(0, (rng.nextDouble() / 2.0 + 0.5) * ak.getUsedDisciplines() * 1000);
                    tn.setMeldungMitProtokoll(0, rng.nextDouble() > 0.2);
                    tn.setMeldepunkte(1,
                            (rng.nextDouble() / 2.0 + 0.5) * ak.getUsedDisciplines() * 1000 + tn.getHLWPunkte());
                    tn.setMeldungMitProtokoll(1, rng.nextDouble() > 0.2);
                }
            }
        }
        ewk.getLaufliste().erzeugen();
        ewk.getHLWListe().erzeugen();
        if (datei != null) {
            System.out.println("  " + datei + " ("
                    + NumberFormat.getNumberInstance().format(0.001 * Math.round((System.currentTimeMillis() - time)))
                    + "s)");
            OutputManager.speichereWettkampf(datei, ewk);
        }
        return ewk;
    }

    private static <T extends ASchwimmer> void erzeugeTexte(AWettkampf<T> wk, Namengenerator ng) {
        wk.setProperty(NAME, "Bezirksmeisterschaften Musterhausen");
        wk.setProperty(LOCATION, "Musterhausen");
        wk.setProperty(ORGANIZER, "DLRG Musterhausen");
        wk.setProperty(AUSRICHTER, "DLRG Musterhausen Ost");
        wk.setProperty(DATE, "01.01." + Calendar.getInstance().get(Calendar.YEAR));
        wk.setProperty(BEGIN, "10:00 Uhr");
        wk.setProperty(END, "18:00 Uhr");
        wk.setProperty(OTHER_COMPETITION_INFO, "");
        wk.setProperty(NAME_OF_POOL, "Hallenbad Musterhausen");
        wk.setProperty(DEPTH_OF_POOL, "2m (Startseite) - 4m (Wendeseite)");
        wk.setProperty(LENGTH_OF_POOL, "25m");
        wk.setProperty(WATERTEMPERATURE, "26 Grad");
        wk.setProperty(POSITION_OF_MANAKIN,
                "Die Puppenaufnahme wurde bei der Disziplin 50m Retten (mit Flossen) am Beckenrand gehalten und bei der Disziplin 100m Retten mit Flossen erfolgt die Puppenaufnahme am Beckenboden in 2m Tiefe.");
        wk.setProperty(OTHER_LOCATION_INFO, "");
        wk.setProperty(INFOPAGE,
                "Auf dieser Seite k\u00F6nnen Sie beliebige Texte eintragen und so auch spezielle Informationen unterbringen, die sonst keinen Platz finden w\u00FCrden.");

    }

    private static <T extends ASchwimmer> void erzeugeKampfrichter(AWettkampf<T> wk, Namengenerator ng) {
        KampfrichterVerwaltung kv = InputManager.ladeKampfrichter("5. Deutsche Meisterschaften");

        for (int y = 0; y < kv.getEinheitenCount(); y++) {
            KampfrichterEinheit ke = kv.getEinheit(y);
            String[] positionen = ke.getPositionen();
            for (String pos : positionen) {
                int anzahl = ke.getKampfrichter(pos).length;
                if (pos.equals("Zeitnehmer")) {
                    anzahl = wk.getIntegerProperty(PropertyConstants.HEATS_LANES) * 2;
                }
                LinkedList<Kampfrichter> karis = new LinkedList<Kampfrichter>();
                for (int x = 0; x < anzahl; x++) {
                    int zahl = rng.nextInt(6);
                    KampfrichterStufe stufe = KampfrichterStufe.KEINE;
                    switch (zahl) {
                    default:
                        stufe = KampfrichterStufe.KEINE;
                        break;
                    case 1:
                        stufe = KampfrichterStufe.F1;
                        break;
                    case 2:
                        stufe = KampfrichterStufe.F1E2;
                        break;
                    case 3:
                        stufe = KampfrichterStufe.E1;
                        break;
                    case 4:
                        stufe = KampfrichterStufe.E12;
                        break;
                    case 5:
                        stufe = KampfrichterStufe.D12;
                        break;
                    }
                    Kampfrichter kr = new Kampfrichter(ng.generateVorname(true) + " " + ng.generateNachname(),
                            ng.generateGliederung(), "", stufe.mit(ke.getPosition(pos).getMinimaleStufe()));
                    karis.addLast(kr);
                }
                ke.setKampfrichter(pos, karis);
            }
        }

        wk.setKampfrichterverwaltung(kv);
    }

    public static MannschaftWettkampf mannschaft(Namengenerator n, String datei, int anzahl, int zeitenbis) {
        if (n == null) {
            n = new Namengenerator(null, anzahl);
        }

        long time = System.currentTimeMillis();

        MannschaftWettkampf mwk = new MannschaftWettkampf(AltersklassenUtils.getDefaultAKs(false),
                InputManager.ladeStrafen(null, false));
        // mwk.setLogo(IconManager.getLogoImage());

        int lanes = 6;
        if (anzahl > 20) {
            lanes = 8;
        }
        mwk.setProperty(PropertyConstants.HEATS_LANES, lanes);
        erzeugeKampfrichter(mwk, n);
        erzeugeTexte(mwk, n);

        for (int a = 0; a < 5; a++) {
            for (int g = 0; g < 2; g++) {
                for (int x = 0; x < anzahl; x++) {
                    String ge = n.generateGliederung();
                    Mannschaft m = mwk.createMannschaft(ge + " " + (x + 1), (g == 1), ge, a, "");
                    m.setQualifikationsebene(n.generateLV(m.getGliederung()));
                    mwk.addSchwimmer(m);
                    if (m.getAK().isDisciplineChoiceAllowed()) {
                        int d = m.getAK().getMaximalChosenDisciplines();
                        while (m.getDisciplineChoiceCount() < d) {
                            int index = rng.nextInt(m.getAK().getDiszAnzahl());
                            m.setDisciplineChoice(index, true);
                        }
                    }

                    // StringBuffer members = new StringBuffer();
                    for (int k = 0; k < m.getMinMembers(); k++) {
                        Mannschaftsmitglied mm = m.getMannschaftsmitglied(k);
                        mm.setVorname(n.generateVorname(m.isMaennlich()));
                        mm.setNachname(n.generateNachname());
                        mm.setGeschlecht(m.isMaennlich() ? Geschlecht.maennlich : Geschlecht.weiblich);
                        int alter = 0;
                        if (m.getAK().getName().equals("AK 12")) {
                            alter = 12;
                        } else if (m.getAK().getName().equals("AK 13/14")) {
                            alter = 13;
                        } else if (m.getAK().getName().equals("AK 15/16")) {
                            alter = 16;
                        } else if (m.getAK().getName().equals("AK 17/18")) {
                            alter = 17;
                        } else if (m.getAK().getName().equals("AK Offen")) {
                            alter = 20;
                        }
                        if (alter > 0) {
                            mm.setJahrgang(Calendar.getInstance().get(Calendar.YEAR) - alter);
                        }
                    }

                    Altersklasse ak = m.getAK();
                    int disz = ak.getDiszAnzahl();
                    if (zeitenbis >= 0) {
                        disz = Math.min(disz, zeitenbis);
                    }
                    double factor = 1.75 * rng.nextDouble();
                    for (int i = 0; i < disz; i++) {
                        if (m.isDisciplineChosen(i)) {
                            double result = (0.98 + factor + 0.25 * rng.nextDouble())
                                    * ak.getDisziplin(i, g == 1).getRec();
                            m.setZeit(i, (int) result);
                            m.setMeldezeit(i, (int) Math.round(m.getZeit(i) * (0.95 + rng.nextDouble() * 0.1)));
                            if (rng.nextDouble() < 0.1) {
                                m.addStrafe(i, getStrafe(mwk.getStrafen(), false));
                            }
                        }
                    }
                    if (rng.nextDouble() < 0.01) {
                        m.addStrafe(ASchwimmer.DISCIPLINE_NUMBER_SELF, getStrafe(mwk.getStrafen(), true));
                    }
                    if (ak.hasHLW()) {
                        for (int i = 0; i < m.getMaximaleHLW(); i++) {
                            if (rng.nextDouble() < 0.01) {
                                m.setHLWState(i, HLWStates.NICHT_ANGETRETEN);
                            } else {
                                m.setHLWPunkte(i, getHLWPunkte());
                            }
                        }
                    }
                    m.setMeldepunkte(0, (rng.nextDouble() / 2.0 + 0.5) * ak.getUsedDisciplines() * 1000);
                    m.setMeldungMitProtokoll(0, rng.nextDouble() > 0.2);
                    m.setMeldepunkte(1,
                            (rng.nextDouble() / 2.0 + 0.5) * ak.getUsedDisciplines() * 1000 + m.getHLWPunkte());
                    m.setMeldungMitProtokoll(1, rng.nextDouble() > 0.2);
                }
            }
        }
        mwk.setProperty(PropertyConstants.HEATS_SORTING_ORDER, Laufliste.REIHENFOLGE_MELDEPUNKTE);
        mwk.getLaufliste().erzeugen();
        mwk.getHLWListe().erzeugen();
        if (datei != null) {
            System.out.println("  " + datei + " ("
                    + NumberFormat.getNumberInstance().format(0.001 * Math.round((System.currentTimeMillis() - time)))
                    + "s)");
            OutputManager.speichereWettkampf(datei, mwk);
        }
        return mwk;
    }

    /**
     * @param r
     * @return
     */
    private static int getHLWPunkte() {
        double r = rng.nextDouble();
        if (r < 0.05) {
            return 0;
        }
        return 200;
    }

    public static void main(String[] args) {
        System.out.println("Erzeuge Wettk\u00E4mpfe:");
        String s = "../../test/resources/values";
        if ((args != null) && (args.length > 0)) {
            s = args[0];
        }
        einzel(new Namengenerator(s, 12), "../../../data/Einzel 1.wk", 12, -1);
        einzel(new Namengenerator(s, 32), "../../../data/Einzel 2.wk", 32, -1);
        einzel(new Namengenerator(s, 100), "../../../data/Einzel 3.wk", 64, -1);
        einzel(new Namengenerator(s, 1000), "../../../data/Einzel 4.wk", 128, -1);
        mannschaft(new Namengenerator(s, 12), "../../../data/Mannschaft 1.wk", 12, -1);
        mannschaft(new Namengenerator(s, 32), "../../../data/Mannschaft 2.wk", 32, -1);
        mannschaft(new Namengenerator(s, 100), "../../../data/Mannschaft 3.wk", 64, -1);
        mannschaft(new Namengenerator(s, 1000), "../../../data/Mannschaft 4.wk", 128, -1);
        einzel(new Namengenerator(s, 12), "../../../data/Einzel 1 (Zeiten unvollst\u00E4ndig).wk", 12, 2);
        einzel(new Namengenerator(s, 32), "../../../data/Einzel 2 (Zeiten unvollst\u00E4ndig).wk", 32, 2);
        einzel(new Namengenerator(s, 100), "../../../data/Einzel 3 (Zeiten unvollst\u00E4ndig).wk", 64, 2);
        mannschaft(new Namengenerator(s, 12), "../../../data/Mannschaft 1 (Zeiten unvollst\u00E4ndig).wk", 12, 2);
        mannschaft(new Namengenerator(s, 32), "../../../data/Mannschaft 2 (Zeiten unvollst\u00E4ndig).wk", 32, 2);
        mannschaft(new Namengenerator(s, 100), "../../../data/Mannschaft 3 (Zeiten unvollst\u00E4ndig).wk", 64, 2);
        System.out.println("Fertig");
    }
}