package de.df.jauswertung.misc;

import de.df.jauswertung.gui.plugins.bugreport.BugreportPlugin;
import de.df.jauswertung.util.DefaultInit;
import de.df.jutils.gui.util.DesignInit;

public class BugReportCreator {
    public static void main(String[] args) {
        DefaultInit.init();
        DesignInit.init();

        BugreportPlugin bugreportPlugin = new BugreportPlugin();
        bugreportPlugin.show(Thread.currentThread(), new RuntimeException("Test exception for bugreport"));
    }
}
