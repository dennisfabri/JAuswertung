/*
 * Created on 25.06.2007
 */
package de.df.jauswertung.misc;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.io.*;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.NullFeedback;

public class ErzeugeExports {

    @SuppressWarnings({ "unchecked" })
    public static void main(String[] args) {
        Feedback nf = new NullFeedback();
        @SuppressWarnings("rawtypes")
        AWettkampf wk = InputManager.ladeWettkampf("Einzel 1.wk");
        String[] namen = ExportManager.getSupportedFormats();
        for (String aNamen : namen) {
            for (int y = 0; y < ExportManager.NAMES.length; y++) {
                String name = "../../test/" + ExportManager.NAMES[y] + "." + ExportManager.getSuffixes(aNamen)[0];
                if (ExportManager.isSupported(aNamen, y)) {
                    try {
                        ExportManager.export(y, name, aNamen, wk, nf);
                    } catch (Exception e) {
                        // Nothing to do
                    }
                }
            }
        }
    }
}