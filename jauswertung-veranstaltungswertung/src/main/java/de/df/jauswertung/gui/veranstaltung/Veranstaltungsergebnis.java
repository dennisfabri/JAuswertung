package de.df.jauswertung.gui.veranstaltung;

import de.df.jauswertung.daten.MannschaftWettkampf;

/**
 * @param wettkampfnamen name of the competition each age group of {@code wettkampf} comes from, by age-group index
 */
record Veranstaltungsergebnis(MannschaftWettkampf wettkampf, String[] wettkampfnamen) {
}
