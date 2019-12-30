import de.df.jauswertung.ares.gui.JAresWriter;
import de.df.jutils.gui.util.DesignInit;

public final class AresLauncher {

    private AresLauncher() {
        // Nothing to do
    }

    public static void main(String[] args) {
        DesignInit.init();
        new JAresWriter().setVisible(true);
    }
}