package de.df.jauswertung.gui.veranstaltung;

import java.util.LinkedList;
import java.util.ListIterator;

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
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.ergebnis.FormelDirectPoints;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;

class Veranstaltungsutils {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static AWettkampf veranstaltung2Wettkampf(Veranstaltung vs, boolean gliederungen) {
        AWettkampf[] wks = getWettkaempfe(vs.getCompetitions());
        if (wks.length == 0) {
            return null;
        }

        if (!gliederungen) {
            for (AWettkampf wk : wks) {
                if (wk != null) {
                    LinkedList<ASchwimmer> swimmers = wk.getSchwimmer();
                    for (ListIterator<ASchwimmer> li = swimmers.listIterator(); li.hasNext();) {
                        ASchwimmer s = li.next();
                        if (s.getQualifikationsebene().length() == 0) {
                            li.remove();
                        } else {
                            s.setGliederung(s.getQualifikationsebene());
                            s.setQualifikationsebene("");
                        }
                    }
                }
            }
        }

        // TODO: Variablere Methode entwickeln
        // Regelwerk aks = new Regelwerk(false, wks[0].getRegelwerk().getFormelID());
        Regelwerk aks = new Regelwerk(false, FormelDirectPoints.ID);
        int amount = 0;
        for (AWettkampf wk3 : wks) {
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
        for (AWettkampf wk2 : wks) {
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
        for (AWettkampf wk1 : wks) {
            if (wk1 != null) {
                Regelwerk a = wk1.getRegelwerk();
                for (int y = 0; y < a.size(); y++) {
                    if (SearchUtils.hasSchwimmer(wk1, wk1.getRegelwerk().getAk(y))) {
                        Altersklasse ak1 = aks.getAk(offset);

                        for (int sex = 0; sex < 2; sex++) {
                            JResultTable rt = JResultTable.getResultTable(wk1, ak1, sex == 1, false, ak1.hasHLW(), 0);
                            // LinkedList<ASchwimmer> ll = SearchUtils.getSchwimmer(wk1, ak1);
                            // ListIterator<ASchwimmer> li = ll.listIterator();
                            for (int row = 0; row < rt.getRowCount(); row++) {
                                // ASchwimmer s = li.next();
                                ASchwimmer s = rt.getSchwimmer(row);
                                SchwimmerResult sr = rt.getResult(row);
                                Mannschaft m = wk.createMannschaft(s.getName(), s.isMaennlich(), s.getGliederungMitQGliederung(), offset, "");
                                m.setDisciplineChoice(s.getDisciplineChoice());
                                m.setStrafen(ASchwimmer.DISCIPLINE_NUMBER_SELF, s.getStrafen(ASchwimmer.DISCIPLINE_NUMBER_SELF));

                                for (int z = 0; z < m.getAK().getDiszAnzahl(); z++) {
                                    LinkedList<Strafe> strafen = new LinkedList<Strafe>();
                                    Strafe strafe = sr.getResults()[z].getStrafe();
                                    strafen.add(strafe);
                                    m.setStrafen(z, strafen);

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

        OutputManager.speichereWettkampf("../../vs.wk", wk);

        return wk;
    }

    @SuppressWarnings("rawtypes")
    public static AWettkampf[] getWettkaempfe(LinkedList<CompetitionContainer> lcc) {
        AWettkampf[] wks = new AWettkampf[lcc.size()];
        for (int x = 0; x < wks.length; x++) {
            CompetitionContainer cc = lcc.get(x);
            wks[x] = InputManager.ladeWettkampf(cc.getFilename());
        }
        return wks;
    }

}
