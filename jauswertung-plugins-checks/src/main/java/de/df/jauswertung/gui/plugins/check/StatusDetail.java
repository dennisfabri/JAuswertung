package de.df.jauswertung.gui.plugins.check;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.util.TimeStatus;

record StatusDetail(ASchwimmer schwimmer, TimeStatus status) {
}
