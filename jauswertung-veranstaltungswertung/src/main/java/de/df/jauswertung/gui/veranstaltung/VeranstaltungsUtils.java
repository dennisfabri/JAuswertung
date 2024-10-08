package de.df.jauswertung.gui.veranstaltung;

import java.util.*;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.daten.veranstaltung.CompetitionContainer;
import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.ergebnis.FormelDirectPoints;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;

import static java.util.Arrays.stream;

class VeranstaltungsUtils {

    public static AWettkampf<?>[] veranstaltung2Wettkampf(Veranstaltung vs, boolean gliederungen,
            boolean separateByGender) {
        AWettkampf<?>[] wks = getWettkaempfe(vs.getCompetitions());
        if (wks.length == 0) {
            return null;
        }

        List<AWettkampf<?>> result = new ArrayList<>();
        result.add(veranstaltung2Wettkampf(vs, gliederungen));
        if (separateByGender) {
            Set<String> genders = new HashSet<>();
            for (AWettkampf<?> wk : wks) {
                genders.add(wk.getRegelwerk().getTranslation("maleShort", I18n.get("maleShort")));
                genders.add(wk.getRegelwerk().getTranslation("femaleShort", I18n.get("femaleShort")));
            }

            for (String gender : genders) {
                result.add(veranstaltung2Wettkampf(vs, gliederungen, gender));
            }
        }
        return result.toArray(new AWettkampf[0]);
    }

    private static AWettkampf<?> veranstaltung2Wettkampf(Veranstaltung vs, boolean gliederungen, String gender) {
        AWettkampf<?>[] wks = getWettkaempfe(vs.getCompetitions());
        if (wks.length == 0) {
            return null;
        }

        for (AWettkampf<?> wk : wks) {
            filterCompetitorsByGender(gender, wk);
        }
        return veranstaltung2Wettkampf(stream(wks).filter(AWettkampf::hasSchwimmer).toArray(AWettkampf[]::new),
                gliederungen, vs);
    }

    private static <T extends ASchwimmer> void filterCompetitorsByGender(String gender, AWettkampf<T> wk) {
        boolean keepMale = wk.getRegelwerk().getTranslation("maleShort", I18n.get("maleShort")).equals(gender);
        boolean keepFemale = wk.getRegelwerk().getTranslation("femaleShort", I18n.get("femaleShort"))
                               .equals(gender);

        if (!keepMale) {
            wk.removeSchwimmerWhen(s -> s.isMaennlich());
        }
        if (!keepFemale) {
            wk.removeSchwimmerWhen(s -> !s.isMaennlich());
        }
    }

    @SuppressWarnings({ "rawtypes" })
    private static AWettkampf veranstaltung2Wettkampf(Veranstaltung vs, boolean gliederungen) {
        AWettkampf[] wks = getWettkaempfe(vs.getCompetitions());
        if (wks.length == 0) {
            return null;
        }

        return veranstaltung2Wettkampf(wks, gliederungen, vs);
    }

    private static MannschaftWettkampf veranstaltung2Wettkampf(AWettkampf<?>[] wks, boolean gliederungen,
            Veranstaltung vs) {
        for (AWettkampf<?> wk : wks) {
            if (wk != null) {
                wk.removeSchwimmerWhen(ASchwimmer::isAusserKonkurrenz);
            }
        }

        if (!gliederungen) {
            for (AWettkampf<?> wk : wks) {
                if (wk != null) {
                    wk.removeSchwimmerWhen(t -> t.getQualifikationsebene().isEmpty());
                    wk.getSchwimmer().forEach(s -> {
                        s.setGliederung(s.getQualifikationsebene());
                        s.setQualifikationsebene("");
                    });
                }
            }
        }

        // TODO: Variablere Methode entwickeln
        // Regelwerk aks = new Regelwerk(false, wks[0].getRegelwerk().getFormelID());
        Regelwerk aks = new Regelwerk(false, FormelDirectPoints.ID);
        int amount = 0;
        for (AWettkampf<?> wk3 : wks) {
            if (wk3 != null) {
                for (int y = 0; y < wk3.getRegelwerk().size(); y++) {
                    if (SearchUtils.hasSchwimmer(wk3, wk3.getRegelwerk().getAk(y))) {
                        amount++;
                    }
                }
            }
        }
        aks.setSize(amount);

        int offset = 0;
        for (AWettkampf<?> wk2 : wks) {
            if (wk2 != null) {
                Regelwerk a = wk2.getRegelwerk();
                for (int y = 0; y < a.size(); y++) {
                    if (SearchUtils.hasSchwimmer(wk2, wk2.getRegelwerk().getAk(y))) {
                        aks.setAk(offset, a.getAk(y));
                        aks.getAk(offset).setName(aks.getAk(offset).getName());

                        offset++;
                    }
                }
            }
        }

        Strafen str = InputManager.ladeStrafen(null, false);
        MannschaftWettkampf wk = new MannschaftWettkampf(aks, str);
        offset = 0;
        for (AWettkampf<?> wk1 : wks) {
            if (wk1 != null) {
                Regelwerk regelwerk = wk1.getRegelwerk();
                for (int y = 0; y < regelwerk.size(); y++) {
                    if (SearchUtils.hasSchwimmer(wk1, regelwerk.getAk(y))) {
                        Altersklasse ak1 = aks.getAk(offset);

                        for (int sex = 0; sex < 2; sex++) {
                            JResultTable rt = JResultTable.getResultTable(wk1, ak1, sex == 1, false, ak1.hasHLW(), 0);
                            for (int row = 0; row < rt.getRowCount(); row++) {
                                ASchwimmer s = rt.getResult(row).getSchwimmer();
                                SchwimmerResult<?> sr = rt.getResult(row);
                                Mannschaft m = wk.createMannschaft(s.getName(), s.isMaennlich(),
                                        s.getGliederungMitQGliederung(), offset, "");
                                m.setDisciplineChoice(s.getDisciplineChoice());
                                m.setStrafen(ASchwimmer.DISCIPLINE_NUMBER_SELF,
                                        s.getStrafen(ASchwimmer.DISCIPLINE_NUMBER_SELF));

                                for (int z = 0; z < m.getAK().getDiszAnzahl(); z++) {
                                    Strafe strafe = sr.getResults()[z].getStrafe();
                                    if (strafe.getArt() != Strafarten.DISQUALIFIKATION) {
                                        LinkedList<Strafe> strafen = new LinkedList<>();
                                        strafen.add(strafe);
                                        m.setStrafen(z, strafen);
                                    }

                                    int points = (int) Math.round(sr.getResults()[z].getPoints() * 100);
                                    if (strafe.getArt() == Strafarten.STRAFPUNKTE) {
                                        points += strafe.getStrafpunkte() * 100;
                                    }
                                    m.setZeit(z, points);
                                }
                                if (m.getAK().hasHLW()) {
                                    m.setHLWState(0, s.getHLWState());
                                    m.setHLWPunkte(0, s.getHLWPunkte());
                                }
                                wk.addSchwimmer(m);
                            }
                        }
                        offset++;
                    }
                }
            }
        }

        aks.setGesamtwertung(true);
        aks.setGesamtwertungHart(vs.isGesamtwertungHart());
        aks.setGesamtwertungSkalieren(vs.getGesamtwertungSkalieren());
        aks.setGesamtwertungsmodus(vs.getGesamtwertungsmodus());

        return wk;
    }

    @SuppressWarnings("rawtypes")
    public static AWettkampf[] getWettkaempfe(List<CompetitionContainer> lcc) {
        AWettkampf[] wks = new AWettkampf[lcc.size()];
        for (int x = 0; x < wks.length; x++) {
            CompetitionContainer cc = lcc.get(x);
            wks[x] = InputManager.ladeWettkampf(cc.getFilename());
        }
        return wks;
    }

}
