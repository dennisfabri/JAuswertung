package de.df.jauswertung.test.util;

import org.junit.Assert;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jutils.util.StringTools;

public final class TestUtils {

    private TestUtils() {
        // Hide
    }

    public static <T extends ASchwimmer> void checkRow(Object[][] data, SchwimmerResult<T>[] results, int x,
            int offset) {
        if (x >= results.length) {
            return;
        }
        T t = results[x].getSchwimmer();
        if (data[7][t.getStartnummer() - 1].toString().startsWith("Ausschlu")
                || data[7][t.getStartnummer() - 1].toString().startsWith("disq")
                || data[7][t.getStartnummer() - 1].toString().startsWith("ausg.")) {
            Assert.assertTrue(t.isAusgeschlossen());
        } else {
            if (data[7][t.getStartnummer() - 1].equals("")) {
                long result1 = 0;
                long result2 = Math.round(results[x].getPoints() * 100);
                Assert.assertEquals(t.getName() + " ", result1, result2 + offset);
            } else {
                double punkte = Double.parseDouble(data[7][t.getStartnummer() - 1].toString());
                long result1 = Math.round(punkte * 100);
                long result2 = Math.round(results[x].getPoints() * 100);
                StringBuilder sb = new StringBuilder(t.getName());
                sb.append(" (");
                for (int y = 0; y < results[x].getSchwimmer().getAK().getDiszAnzahl(); y++) {
                    if (y != 0) {
                        sb.append(" ");
                    }
                    sb.append(StringTools.zeitString(results[x].getSchwimmer().getZeit(y)));
                }
                sb.append(") ");
                Assert.assertEquals(sb.toString(), result1 - offset, result2);
            }
        }
    }
}