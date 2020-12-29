import de.df.jauswertung.gui.JAuswertungLauncher;

/**
 * Klasse zum einfacheren Starten von JAuswertung
 * 
 * @author Dennis Mueller
 */
public final class JAuswertung {

    private JAuswertung() {
        // Hide
    }

    /**
     * Ruft die eigentliche Startmethode auf.
     * 
     * @param args
     *            Kommandozeilenparameter
     */
    public static void main(final String[] args) {
        JAuswertungLauncher.main(args);
    }
}