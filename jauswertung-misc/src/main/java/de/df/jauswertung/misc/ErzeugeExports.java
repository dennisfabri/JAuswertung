/*
 * Created on 25.06.2007
 */
package de.df.jauswertung.misc;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.io.ImportExportTypes;
import de.df.jauswertung.io.InputManager;
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
            for (ImportExportTypes type : ImportExportTypes.values()) {
                int y = type.getValue();
                String name = "../../test/" + ExportManager.NAMES[y] + "." + ExportManager.getSuffixes(aNamen)[0];
                if (ExportManager.isSupported(aNamen, type)) {
                    try {
                        ExportManager.export(type, name, aNamen, wk, nf);
                    } catch (Exception e) {
                        // Nothing to do
                    }
                }
            }
        }
    }
}