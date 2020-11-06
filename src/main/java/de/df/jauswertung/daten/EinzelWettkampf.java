/*
 * EinzelWettkampf.java Created on 10. Februar 2001, 16:29
 */

package de.df.jauswertung.daten;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.util.AltersklassenUtils;

/**
 * Hauptklasse zur Verwaltung eines EinzelWettkampfes.
 * 
 * @author Dennis Mueller
 * @version 1.0
 */
public class EinzelWettkampf extends AWettkampf<Teilnehmer> {

    private static final long serialVersionUID = -6729452115452029599L;

    // Only for xstream
    @Deprecated
    public EinzelWettkampf() {
        this(new Regelwerk(), new Strafen());
    }

    /**
     * Creates new einzelWettkampf
     * 
     * @param aks
     *            Teilnehmende Altersklassen
     */
    public EinzelWettkampf(final Regelwerk aks, final Strafen s) {
        super(AltersklassenUtils.checkAKs(aks, true), s);
    }

    public Teilnehmer createTeilnehmer(String name, String vname, int tJahrgang, boolean geschlecht, String gliederung, int ak, String bemerkung) {
        return new Teilnehmer(this, name.trim(), vname.trim(), tJahrgang, geschlecht, gliederung.trim(), ak, bemerkung.trim());
    }

    @Override
    public boolean isEinzel() {
        return true;
    }
}