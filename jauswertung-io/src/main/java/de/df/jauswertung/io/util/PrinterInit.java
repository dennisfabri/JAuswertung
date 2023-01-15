package de.df.jauswertung.io.util;

import java.awt.print.PageFormat;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.IOUtils;
import de.df.jutils.print.PageSetting;
import de.df.jutils.print.PageSetup;

public final class PrinterInit {

    private PrinterInit() {
    }

    public static void init() {
        initializeDefaults();
        loadFromSettings();
    }

    private static void loadFromSettings() {
        try {
            Object data = IOUtils.readFromPreferences("PrintSettings");
            if (data instanceof PageSetting[]) {
                PageSetting[] pageSettings = (PageSetting[]) data;
                PageSetup.setPageSettings(pageSettings);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void initializeDefaults() {
        PageSetup.setOrientation(I18n.get("Results"), PageFormat.LANDSCAPE);
        PageSetup.setOrientation(I18n.get("GroupEvaluation"), PageFormat.LANDSCAPE);
        PageSetup.setOrientation(I18n.get("Meldezeiten"), PageFormat.LANDSCAPE);
        PageSetup.setOrientation(I18n.get("Laufliste"), PageFormat.LANDSCAPE);
        PageSetup.setOrientation(I18n.get("ZWList"), PageFormat.LANDSCAPE);
        PageSetup.setOrientation(I18n.get("Disciplines"), PageFormat.LANDSCAPE);
        PageSetup.setOrientation(I18n.get("BrokenRecords"), PageFormat.LANDSCAPE);
    }
}
