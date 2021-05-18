package de.df.jauswertung.util;

import java.awt.print.PageFormat;
import java.util.HashMap;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.IOUtils;
import de.df.jutils.print.PageSetup;

public class PrinterInit {
    private PrinterInit() {        
    }

    public static void init() {
        initPrintSetup();
    }
    
    @SuppressWarnings({ "unchecked" })
    private static void initPrintSetup() {
        Object o = IOUtils.readFromPreferences("PrintSettings");
        HashMap<String, PageFormat> prasTable = (HashMap<String, PageFormat>) o;
        if (prasTable == null) {
            prasTable = new HashMap<>();
            prasTable.put(I18n.get("Results"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("GroupEvaluation"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("Meldezeiten"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("Laufliste"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("ZWList"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("Disciplines"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
            prasTable.put(I18n.get("BrokenRecords"), PageSetup.createPageFormat(PageFormat.LANDSCAPE));
        }
        PageSetup.setPRASTable(prasTable);
    }    
}
