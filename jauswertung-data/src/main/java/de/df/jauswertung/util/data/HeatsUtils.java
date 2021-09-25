package de.df.jauswertung.util.data;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.util.SearchUtils;

public class HeatsUtils {
    
    public static <T extends ASchwimmer> void save(AWettkampf<T> wk, OWSelection t, AWettkampf<T> wkx) {
        Laufliste<T> heats = wkx.getLaufliste();
        OWDisziplin<T> disziplin = wk.getLauflisteOW().getDisziplin(t.akNummer, t.male, t.discipline, t.round);
        if (disziplin == null) {
            disziplin = wk.getLauflisteOW().addDisziplin(t.akNummer, t.male, t.discipline, t.round);
        }
        disziplin.laeufe.clear();
        for (Lauf<T> lauf : heats.getLaufliste()) {
            OWLauf<T> l = new OWLauf<>(wk, disziplin.Id, lauf);
            for (int x = 0; x < l.getBahnen(); x++) {
                T tx = l.getSchwimmer(x);
                if (tx != null) {
                    T ti = SearchUtils.getSchwimmer(wk, tx);
                    if (ti instanceof Mannschaft && disziplin.round > 0) {
                        Mannschaft m = (Mannschaft) ti;
                        int[] starter = m.getStarter(OWDisziplin.getId(disziplin.akNummer, disziplin.maennlich,
                                disziplin.disziplin, disziplin.round - 1));
                        if (starter == null && disziplin.round == 1) {
                            starter = m.getStarter(disziplin.disziplin);
                        }
                        m.setStarter(disziplin.Id, starter);
                    }
                    disziplin.Schwimmer.add(ti);
                }
            }
            disziplin.laeufe.add(l);
        }
        wk.setProperty(PropertyConstants.HEATS_SORTING_ORDER,
                wkx.getIntegerProperty(PropertyConstants.HEATS_SORTING_ORDER, Laufliste.REIHENFOLGE_REGELWERK));    
    }

}
