package de.df.jauswertung.misc;

import static de.df.jauswertung.daten.PropertyConstants.ALWAYS_SHOW_FULL_NAMES;
import static de.df.jauswertung.daten.PropertyConstants.ART_DES_WETTKAMPFS;
import static de.df.jauswertung.daten.PropertyConstants.DOPING_REGISTERED_POINTS_INDEX;
import static de.df.jauswertung.daten.PropertyConstants.DSM_MODE_DATA;
import static de.df.jauswertung.daten.PropertyConstants.ELEKTRONISCHE_ZEITNAHME;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_AAE_FASTEST_HEAT_UNTOUCHED;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_AVOID_ALMOST_EMPTY;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_EMPTY_LIST;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_FIRST_HEAT;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_JOIN_HEATS;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANE_SELECTION;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_MIXED;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_MIXED_IN_FRONT;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_NOT_COMPETING_MIXED;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_NUMBERING_SCHEME;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_REGISTERED_POINTS_INDEX;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_RESPECT_QUALIFICATIONS;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_ROTATE;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_SORTING_ORDER;
import static de.df.jauswertung.daten.PropertyConstants.HEAT_CHANGE_BLOCKED;
import static de.df.jauswertung.daten.PropertyConstants.ISC_RESULT_UPLOAD_COMPETITION_ID;
import static de.df.jauswertung.daten.PropertyConstants.ISC_RESULT_UPLOAD_EDVNUMBER;
import static de.df.jauswertung.daten.PropertyConstants.NAMENTLICHE_MELDUNG_STRIKT;
import static de.df.jauswertung.daten.PropertyConstants.PREFER_PROTOCOL;
import static de.df.jauswertung.daten.PropertyConstants.PRINT_REFEREES_COMPACT;
import static de.df.jauswertung.daten.PropertyConstants.RESULT_MULTILINE;
import static de.df.jauswertung.daten.PropertyConstants.SHORTNAME;
import static de.df.jauswertung.daten.PropertyConstants.STARTNUMBERFORMAT;
import static de.df.jauswertung.daten.PropertyConstants.SUK_REGISTERED_POINTS_INDEX;
import static de.df.jauswertung.daten.PropertyConstants.UPLOAD_ID;
import static de.df.jauswertung.daten.PropertyConstants.UPLOAD_INDEX;
import static de.df.jauswertung.daten.PropertyConstants.URKUNDE;
import static de.df.jauswertung.daten.PropertyConstants.URKUNDE_EINZELWERTUNG;
import static de.df.jauswertung.daten.PropertyConstants.YEAR_OF_COMPETITION;
import static de.df.jauswertung.daten.PropertyConstants.ZULASSUNG_REGISTERED_POINTS_INDEX;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.imageio.ImageIO;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Filter;
import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.Qualifikation;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.Timelimit;
import de.df.jauswertung.daten.Timelimitchecktype;
import de.df.jauswertung.daten.Timelimits;
import de.df.jauswertung.daten.Wettkampfart;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.kampfrichter.Kampfrichter;
import de.df.jauswertung.daten.kampfrichter.KampfrichterEinheit;
import de.df.jauswertung.daten.kampfrichter.KampfrichterStufe;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.laufliste.Reihenfolge;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.GroupEvaluationMode;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Skalierungsmodus;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.daten.regelwerk.Startunterlagen;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.gui.util.OWUtils;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.print.graphics.Dimension;
import de.df.jauswertung.print.graphics.Point;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.data.HeatsUtils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import lombok.extern.slf4j.Slf4j;

/**
 * Creates competitions that contain every data structure the UI can still produce, as fixtures for file-format
 * compatibility tests. XStream omits null fields, so every reference field is filled. Values differ from the defaults
 * a freshly created object would have, so a test can tell a loaded value from a reconstructed one. Competitions with
 * finals use the open water rulebooks and run through rounds the way the heats plugin does. Each year's files go to
 * their own directory and are never overwritten, so files of every year stay loadable as regression fixtures.
 */
@Slf4j
public final class ErzeugeKompatibilitaetsWettkaempfe {

    private static final int SCHWIMMER_JE_AK_UND_GESCHLECHT = 2;
    private static final int SCHWIMMER_JE_GESCHLECHT_FINALE = 10;
    private static final int BAHNEN = 8;
    private static final Strafarten[] STRAFARTEN_RUNDE = { Strafarten.STRAFPUNKTE, Strafarten.DISQUALIFIKATION,
            Strafarten.NICHT_ANGETRETEN, Strafarten.NICHTS };

    private ErzeugeKompatibilitaetsWettkaempfe() {
    }

    public static void main(String[] args) {
        int jahr = args.length > 0 ? Integer.parseInt(args[0]) : Calendar.getInstance().get(Calendar.YEAR);
        String ziel = args.length > 1 ? args[1] : "../../../../jauswertung-io/src/test/resources/compatibility/" + jahr;
        String namen = args.length > 2 ? args[2] : "../../../../jauswertung-io/src/test/resources/values";
        File verzeichnis = new File(ziel);
        File[] vorhanden = verzeichnis.listFiles((d, n) -> n.endsWith(".wk"));
        if (vorhanden != null && vorhanden.length > 0) {
            throw new IllegalStateException("Bereits erzeugt, Dateien werden nicht \u00fcberschrieben: "
                    + verzeichnis.getAbsolutePath());
        }
        verzeichnis.mkdirs();

        int anzahl = SCHWIMMER_JE_AK_UND_GESCHLECHT;
        speichern(vervollstaendigen(
                ErzeugeWettkaempfe.einzel(new Namengenerator(namen, anzahl * 10), null, anzahl, -1, jahr), jahr),
                ziel + "/Einzel.wk");
        speichern(vervollstaendigen(
                ErzeugeWettkaempfe.mannschaft(new Namengenerator(namen, anzahl * 10), null, anzahl, -1, jahr), jahr),
                ziel + "/Mannschaft.wk");

        Namengenerator ng = new Namengenerator(namen, SCHWIMMER_JE_GESCHLECHT_FINALE * 4);
        speichern(finale(new EinzelWettkampf(ozean(true), InputManager.ladeStrafen(null, true)), ng, jahr),
                ziel + "/Finale-Einzel.wk");
        speichern(finale(new MannschaftWettkampf(ozean(false), InputManager.ladeStrafen(null, false)), ng, jahr),
                ziel + "/Finale-Mannschaft.wk");
    }

    private static void speichern(AWettkampf<?> wk, String datei) {
        if (!OutputManager.speichereWettkampf(datei, wk)) {
            throw new IllegalStateException("Speichern fehlgeschlagen: " + datei);
        }
        log.info("Erzeugt: {}", datei);
    }

    private static <T extends ASchwimmer> AWettkampf<T> vervollstaendigen(AWettkampf<T> wk, int jahr) {
        regelwerk(wk.getRegelwerk());
        eigenschaften(wk, jahr);
        schwimmer(wk);
        kampfrichter(wk.getKampfrichterverwaltung());
        filter(wk);
        zeitlimits(wk);
        zulassung(wk);
        if (wk instanceof MannschaftWettkampf mwk) {
            mwk.createTeammembersRegistrationsId();
        }
        laufliste(wk);
        zielrichterentscheid(wk);
        eigenschaftenNachLaufliste(wk);
        return wk;
    }

    private static void regelwerk(Regelwerk aks) {
        Startgruppe jugend = new Startgruppe("Jugend");
        jugend.setLaufsortierung(Reihenfolge.Zufall.getValue());
        jugend.setLaufrotation(false);
        Startgruppe offen = new Startgruppe("Offen");
        offen.setLaufsortierung(Reihenfolge.GleicheGliederungVerteilen.getValue());
        offen.setLaufrotation(false);
        aks.setStartgruppen(new Startgruppe[] { jugend, offen });

        Wertungsgruppe junioren = new Wertungsgruppe("Junioren");
        junioren.setProtokollMitEinzelwertung(false);
        junioren.setProtokollMitMehrkampfwertung(true);
        junioren.setEinzelwertungHlw(true);
        junioren.setStrafeIstDisqualifikation(true);
        Wertungsgruppe senioren = new Wertungsgruppe("Senioren");
        senioren.setProtokollMitEinzelwertung(false);
        senioren.setProtokollMitMehrkampfwertung(true);
        aks.setWertungsgruppen(new Wertungsgruppe[] { junioren, senioren });

        // Laufliste.erzeugen with a custom split fails when a start group mixes age groups with different numbers
        // of disciplines.
        Reihenfolge[] reihenfolgen = Arrays.stream(Reihenfolge.values()).filter(r -> r != Reihenfolge.Regelwerk)
                .toArray(Reihenfolge[]::new);
        int jugendDisziplinen = aks.getAk(0).getDiszAnzahl();
        int offenDisziplinen = aks.getAk(aks.size() > 2 ? 2 : 0).getDiszAnzahl();
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            boolean jung = x < 2;
            if (jung && ak.getDiszAnzahl() == jugendDisziplinen) {
                ak.setStartgruppe(jugend.getName());
            } else if (!jung && ak.getDiszAnzahl() == offenDisziplinen) {
                ak.setStartgruppe(offen.getName());
            }
            ak.setWertungsgruppe(jung ? junioren.getName() : senioren.getName());
            ak.setLaufsortierung(reihenfolgen[x % reihenfolgen.length].getValue());
            ak.setLaufrotation(x % 2 == 0);
        }
        aks.getAk(1).setHLW(true);

        aks.setTranslation("Male", "Männlich");
        aks.setTranslation("male", "männlich");
        aks.setTranslation("maleShort", "m");
        aks.setTranslation("Female", "Weiblich");
        aks.setTranslation("female", "weiblich");
        aks.setTranslation("femaleShort", "w");

        aks.setGesamtwertung(true);
        aks.setGesamtwertungHart(true);
        aks.setGesamtwertungsmodus(GroupEvaluationMode.Best);
        aks.setGesamtwertungSkalieren(Skalierungsmodus.ANZAHL_DISZIPLINEN);
        aks.setZusatzwertung("Herz-Lungen-Wiederbelebung");
        aks.setZusatzwertungKurz("HLW");
        aks.setZusatzwertungBasispunkte(300);
    }

    private static <T extends ASchwimmer> void eigenschaften(AWettkampf<T> wk, int jahr) {
        wk.setProperty(HEATS_LANES, BAHNEN);
        wk.setProperty(SHORTNAME, "BM Musterhausen");
        wk.setProperty(ART_DES_WETTKAMPFS, Wettkampfart.Landesmeisterschaften);
        wk.setProperty(ELEKTRONISCHE_ZEITNAHME, true);
        wk.setProperty(YEAR_OF_COMPETITION, jahr + 1);
        wk.setProperty(NAMENTLICHE_MELDUNG_STRIKT, true);
        wk.setProperty(RESULT_MULTILINE, true);
        wk.setProperty(ALWAYS_SHOW_FULL_NAMES, true);
        wk.setProperty(STARTNUMBERFORMAT, Arrays.stream(StartnumberFormatManager.getFormats())
                .filter(f -> !f.equals("Default")).findFirst().orElseThrow());
        wk.setProperty(PRINT_REFEREES_COMPACT, 0);
        wk.setProperty(ISC_RESULT_UPLOAD_EDVNUMBER, "1234567");
        wk.setProperty(ISC_RESULT_UPLOAD_COMPETITION_ID, "42");
        wk.setProperty(UPLOAD_ID, "musterhausen-" + jahr);
        wk.setProperty(UPLOAD_INDEX, 1);
        wk.setProperty(HEATS_NUMBERING_SCHEME, HeatsNumberingScheme.Hundreds.getValue());

        wk.setProperty(HEATS_FIRST_HEAT, 2);
        wk.setProperty(HEATS_SORTING_ORDER, Reihenfolge.Meldezeiten.getValue());
        wk.setProperty(HEATS_AVOID_ALMOST_EMPTY, true);
        wk.setProperty(HEATS_AAE_FASTEST_HEAT_UNTOUCHED, true);
        wk.setProperty(HEATS_MIXED, false);
        wk.setProperty(HEATS_MIXED_IN_FRONT, true);
        wk.setProperty(HEATS_JOIN_HEATS, true);
        wk.setProperty(HEATS_ROTATE, true);
        wk.setProperty(HEATS_NOT_COMPETING_MIXED, false);
        wk.setProperty(HEATS_REGISTERED_POINTS_INDEX, 1);
        wk.setProperty(HEATS_LANE_SELECTION, bahnenauswahl(wk));
        int startgruppen = wk.getRegelwerk().getEffektiveStartgruppen().length;
        int[][][] dsm = new int[startgruppen][startgruppen][2];
        dsm[0][startgruppen - 1][0] = 1;
        wk.setProperty(DSM_MODE_DATA, dsm);

        wk.setProperty(PREFER_PROTOCOL, true);
        wk.setProperty(ZULASSUNG_REGISTERED_POINTS_INDEX, 1);
        wk.setProperty(DOPING_REGISTERED_POINTS_INDEX, 1);
        wk.setProperty(SUK_REGISTERED_POINTS_INDEX, 1);

        wk.setLogo(logo());
        wk.setProperty(URKUNDE, urkunde("Urkunde"));
        wk.setProperty(URKUNDE_EINZELWERTUNG, urkunde("Urkunde Einzelwertung"));
    }

    /**
     * Options that would change the generated heats are set afterwards.
     */
    private static <T extends ASchwimmer> void eigenschaftenNachLaufliste(AWettkampf<T> wk) {
        wk.setProperty(HEATS_EMPTY_LIST, true);
        wk.setProperty(HEATS_RESPECT_QUALIFICATIONS, true);
        wk.setProperty(HEAT_CHANGE_BLOCKED, true);
    }

    private static <T extends ASchwimmer> Hashtable<String, boolean[]> bahnenauswahl(AWettkampf<T> wk) {
        int bahnen = wk.getIntegerProperty(HEATS_LANES);
        Hashtable<String, boolean[]> auswahl = new Hashtable<>();
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < aks.getAk(x).getDiszAnzahl(); y++) {
                boolean[] frei = new boolean[bahnen];
                Arrays.fill(frei, true);
                frei[0] = auswahl.isEmpty();
                auswahl.put(aks.getAk(x).getDisziplin(y, true).getName(), frei);
            }
        }
        return auswahl;
    }

    private static byte[] logo() {
        BufferedImage bild = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        bild.setRGB(0, 0, 0xCC0000);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(bild, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Hashtable<String, Object>[] urkunde(String titel) {
        return new Hashtable[] { textfeld(titel, 100, 50, Font.BOLD, 1), textfeld("<name>", 100, 200, Font.ITALIC, 2) };
    }

    private static Hashtable<String, Object> textfeld(String text, int x, int y, int stil, int ausrichtung) {
        Hashtable<String, Object> feld = new Hashtable<>();
        feld.put("offset", new Point(x, y));
        feld.put("size", new Dimension(400, 40));
        feld.put("fontname", "DejaVu Sans");
        feld.put("fontstyle", stil);
        feld.put("fontsize", "18");
        feld.put("alignment", ausrichtung);
        feld.put("text", text);
        return feld;
    }

    private static <T extends ASchwimmer> void schwimmer(AWettkampf<T> wk) {
        Qualifikation[] qualis = Qualifikation.values();
        Startunterlagen[] startunterlagen = Startunterlagen.values();
        HLWStates[] hlwStates = HLWStates.values();
        Strafarten[] strafarten = Strafarten.values();
        int hlwIndex = 0;
        int strafenIndex = 0;

        List<T> alle = wk.getSchwimmer();
        for (int i = 0; i < alle.size(); i++) {
            T s = alle.get(i);
            s.setQualifikation(qualis[i % qualis.length]);
            s.setStartunterlagen(startunterlagen[i % startunterlagen.length]);
            s.setAusserKonkurrenz(i % 10 == 3);
            s.setDopingkontrolle(i % 10 == 4);
            s.setMeldePlatz(i + 1);
            s.setImportId("import-" + (i + 1));
            s.setBemerkung("Bemerkung " + (i + 1));

            if (s.getAK().hasHLW()) {
                HLWStates state = hlwStates[hlwIndex++ % hlwStates.length];
                if (state == HLWStates.ENTERED) {
                    s.setHLWPunkte(0, 180 - hlwIndex);
                } else {
                    s.setHLWState(0, state);
                }
            }

            if (strafenIndex < strafarten.length) {
                Strafarten art = strafarten[strafenIndex];
                Strafe strafe = new Strafe("Strafe " + art.name(), "S" + strafenIndex, art, 100);
                int disziplin = art == Strafarten.AUSSCHLUSS ? ASchwimmer.DISCIPLINE_NUMBER_SELF : ersteDisziplin(s);
                if (disziplin != Integer.MIN_VALUE) {
                    s.addStrafe(disziplin, strafe);
                    strafenIndex++;
                }
            }

            if (s instanceof Mannschaft m) {
                mannschaft(m, i);
            }
        }
    }

    private static int ersteDisziplin(ASchwimmer s) {
        for (int d = 0; d < s.getAK().getDiszAnzahl(); d++) {
            if (s.isDisciplineChosen(d)) {
                return d;
            }
        }
        return Integer.MIN_VALUE;
    }

    private static void mannschaft(Mannschaft m, int index) {
        Geschlecht[] geschlechter = Geschlecht.values();
        for (int k = 0; k < m.getMaxMembers(); k++) {
            Mannschaftsmitglied mm = m.getMannschaftsmitglied(k);
            if (mm.getVorname() != null && !mm.getVorname().isEmpty()) {
                mm.setImportId("import-" + (index + 1) + "-" + (k + 1));
                mm.setGeschlecht(geschlechter[(index + k) % geschlechter.length]);
            }
        }
        for (int d = 0; d < m.getAK().getDiszAnzahl(); d++) {
            if (m.isDisciplineChosen(d)) {
                m.setStarter(d, starter(m));
            }
        }
    }

    private static void kampfrichter(KampfrichterVerwaltung kv) {
        KampfrichterStufe[] stufen = KampfrichterStufe.values();
        int index = 0;
        for (int e = 0; e < kv.getEinheitenCount(); e++) {
            KampfrichterEinheit einheit = kv.getEinheit(e);
            for (String position : einheit.getPositionen()) {
                for (Kampfrichter kr : einheit.getKampfrichter(position)) {
                    kr.setStufe(stufen[index % stufen.length]);
                    kr.setBemerkung("Bemerkung " + (index + 1));
                    index++;
                }
            }
        }
    }

    private static <T extends ASchwimmer> void filter(AWettkampf<T> wk) {
        Set<String> gliederungen = new HashSet<>();
        for (T s : wk.getSchwimmer()) {
            gliederungen.add(s.getGliederung());
            if (gliederungen.size() == 2) {
                break;
            }
        }
        Filter[] filter = Arrays.copyOf(wk.getFilter(), wk.getFilter().length + 1);
        filter[filter.length - 1] = new Filter("Auswahl", gliederungen.toArray(new String[0]));
        wk.setFilter(filter);
        wk.setCurrentFilterIndex(filter.length - 1);
    }

    private static <T extends ASchwimmer> void zeitlimits(AWettkampf<T> wk) {
        Altersklasse ak = wk.getRegelwerk().getAk(0);
        String disziplin = ak.getDisziplin(0, true).getName();
        int rec = ak.getDisziplin(0, true).getRec();
        Timelimitchecktype[] checks = Timelimitchecktype.values();
        for (int x = 0; x < checks.length; x++) {
            Timelimits limits = new Timelimits();
            limits.setName("Pflichtzeit " + (x + 1));
            limits.setShortname("PZ" + (x + 1));
            limits.setCheck(checks[x]);
            limits.setLimits(new Timelimit[] { new Timelimit(disziplin, rec * (2 + x), 10, 99, ak.getName(), true),
                    new Timelimit(disziplin, rec * (2 + x), 10, 99, ak.getName(), false) });
            wk.getTimelimits().add(limits);
        }
    }

    private static <T extends ASchwimmer> void zulassung(AWettkampf<T> wk) {
        int[][] limits = new int[wk.getRegelwerk().size()][2];
        int[][] direkt = new int[limits.length][2];
        int[][] nachruecker = new int[limits.length][2];
        for (int x = 0; x < limits.length; x++) {
            Arrays.fill(limits[x], 3);
            Arrays.fill(direkt[x], 1);
            Arrays.fill(nachruecker[x], 2);
        }
        wk.setZulassungslimits(limits);
        wk.setDirektqualifizierte(direkt);
        wk.setNachruecker(nachruecker);
    }

    private static <T extends ASchwimmer> void laufliste(AWettkampf<T> wk) {
        Laufliste<T> laufliste = wk.getLaufliste();
        if (wk instanceof EinzelWettkampf) {
            laufliste.erzeugen(ersterBlockZuletzt(laufliste.getStandardVerteilung(),
                    e -> e.getStartgruppe() + "/" + e.isMaennlich()));
        } else {
            laufliste.erzeugen(ersterBlockZuletzt(laufliste.getStandardBlocks(),
                    b -> b.getStartgruppe() + "/" + b.isMaennlich()));
        }
    }

    /**
     * Keeps the discipline order within each start group; changing it makes Laufliste reorder the disciplines of the
     * rulebook.
     */
    private static <E> E[] ersterBlockZuletzt(E[] werte, Function<E, String> block) {
        String erster = block.apply(werte[0]);
        List<E> vorne = Arrays.stream(werte).filter(w -> !block.apply(w).equals(erster)).toList();
        List<E> hinten = Arrays.stream(werte).filter(w -> block.apply(w).equals(erster)).toList();
        E[] ergebnis = Arrays.copyOf(werte, werte.length);
        for (int x = 0; x < ergebnis.length; x++) {
            ergebnis[x] = x < vorne.size() ? vorne.get(x) : hinten.get(x - vorne.size());
        }
        return ergebnis;
    }

    private static <T extends ASchwimmer> void zielrichterentscheid(AWettkampf<T> wk) {
        for (Lauf<T> lauf : wk.getLaufliste().getLaufliste()) {
            for (int a = 0; a < lauf.getBahnen(); a++) {
                T erster = lauf.getSchwimmer(a);
                if (erster == null) {
                    continue;
                }
                for (int b = a + 1; b < lauf.getBahnen(); b++) {
                    T zweiter = lauf.getSchwimmer(b);
                    int disziplin = lauf.getDisznummer(a);
                    if (zweiter != null && lauf.getDisznummer(b) == disziplin && erster.getZeit(disziplin) > 0) {
                        zweiter.setZeit(disziplin, erster.getZeit(disziplin));
                        Zielrichterentscheid<T> ze = new Zielrichterentscheid<>();
                        ze.setDisziplin(disziplin);
                        ze.addSchwimmer(erster);
                        ze.addSchwimmer(zweiter);
                        LinkedList<Zielrichterentscheid<T>> entscheide = wk.getZielrichterentscheide();
                        entscheide.add(ze);
                        wk.setZielrichterentscheide(entscheide);
                        return;
                    }
                }
            }
        }
        throw new IllegalStateException("Kein Lauf für einen Zielrichterentscheid gefunden");
    }

    private static Regelwerk ozean(boolean einzel) {
        Regelwerk aks = AgeGroupIOUtils.getAKs("International - Ocean.rw" + (einzel ? "e" : "m"));
        Altersklasse ak = aks.getAk(0);
        for (boolean maennlich : new boolean[] { false, true }) {
            ak.getDisziplin(0, maennlich).setRunden(new int[] { 8 }, new int[] { 1, 2 });
            ak.getDisziplin(1, maennlich).setRunden(new int[] { 8, 4 }, new int[] { 1, 2, 3 });
        }
        return aks;
    }

    private static <T extends ASchwimmer> AWettkampf<T> finale(AWettkampf<T> wk, Namengenerator ng, int jahr) {
        wk.setProperty(HEATS_LANES, BAHNEN);
        for (boolean maennlich : new boolean[] { false, true }) {
            for (int x = 0; x < SCHWIMMER_JE_GESCHLECHT_FINALE; x++) {
                ASchwimmer s = switch (wk) {
                case EinzelWettkampf ewk -> {
                    Teilnehmer t = ewk.createTeilnehmer(ng.generateCompetitorId(), ng.generateNachname(),
                            ng.generateVorname(maennlich), jahr - 20 - x,
                            maennlich, ng.generateGliederung(), 0, "");
                    ewk.addSchwimmer(t);
                    yield t;
                }
                case MannschaftWettkampf mwk -> {
                    String gliederung = ng.generateGliederung();
                    Mannschaft m = mwk.createMannschaft(gliederung + " " + (x + 1), maennlich, gliederung, 0, "");
                    mwk.addSchwimmer(m);
                    for (int k = 0; k < m.getMinMembers(); k++) {
                        Mannschaftsmitglied mm = m.getMannschaftsmitglied(k);
                        mm.setVorname(ng.generateVorname(maennlich));
                        mm.setNachname(ng.generateNachname());
                        mm.setGeschlecht(maennlich ? Geschlecht.maennlich : Geschlecht.weiblich);
                    }
                    yield m;
                }
                default -> throw new IllegalArgumentException(wk.getClass().getName());
                };
                Altersklasse ak = s.getAK();
                int anzahl = ak.isDisciplineChoiceAllowed() ? ak.getMaximalChosenDisciplines() : ak.getDiszAnzahl();
                for (int d = 0; d < Math.min(anzahl, ak.getDiszAnzahl()); d++) {
                    s.setDisciplineChoice(d, true);
                    s.setMeldezeit(d, zeit(x));
                    if (s instanceof Mannschaft m) {
                        m.setStarter(d, starter(m));
                    }
                }
            }
        }

        OWSelection[] runden = OWUtils.getCreatableRounds(wk);
        while (runden.length > 0) {
            for (OWSelection runde : runden) {
                rundeDurchfuehren(wk, runde);
            }
            runden = OWUtils.getCreatableRounds(wk);
        }
        return wk;
    }

    private static <T extends ASchwimmer> void rundeDurchfuehren(AWettkampf<T> wk, OWSelection runde) {
        AWettkampf<T> rundenWk = ResultUtils.createCompetitionFor(wk, runde);
        if (rundenWk == null) {
            throw new IllegalStateException("Keine Teilnehmer für " + runde.getId());
        }
        rundenWk.getLaufliste().erzeugen();
        HeatsUtils.save(wk, runde, rundenWk);

        List<T> teilnehmer = new ArrayList<>();
        for (OWLauf<T> lauf : wk.getLauflisteOW().getDisziplin(runde).getLaeufe()) {
            for (int bahn = 0; bahn < lauf.getBahnen(); bahn++) {
                if (lauf.getSchwimmer(bahn) != null) {
                    teilnehmer.add(SearchUtils.getSchwimmer(wk, lauf.getSchwimmer(bahn)));
                }
            }
        }
        for (int platz = 0; platz < teilnehmer.size(); platz++) {
            T s = teilnehmer.get(platz);
            s.setZeit(runde.getId(), zeit(platz));
            if (s instanceof Mannschaft m) {
                m.setStarter(runde.getId(), starter(m));
            }
            if (platz >= teilnehmer.size() - 2) {
                Strafarten art = STRAFARTEN_RUNDE[(runde.discipline + runde.round + platz) % STRAFARTEN_RUNDE.length];
                s.addStrafe(runde.getId(), new Strafe("Strafe " + art.name(), "S", art, 50));
            }
        }
    }

    private static int zeit(int platz) {
        return 30000 + 100 * platz;
    }

    private static int[] starter(Mannschaft m) {
        int[] starter = new int[m.getMinMembers()];
        for (int k = 0; k < starter.length; k++) {
            starter[k] = k + 1;
        }
        return starter;
    }
}
