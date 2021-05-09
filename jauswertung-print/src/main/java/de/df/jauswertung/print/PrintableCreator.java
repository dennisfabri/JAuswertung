package de.df.jauswertung.print;

import static de.df.jauswertung.daten.PropertyConstants.INFOPAGE;

import java.awt.Color;
import java.awt.Component;
import java.awt.print.Printable;
import java.text.MessageFormat;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.eit.easyprint.TextPrintable;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.penalties.PenaltyUIUtils;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.ComponentListPrintable2;
import de.df.jutils.print.printables.ComponentPackingPrintable;
import de.df.jutils.print.printables.EmptyPrintable;
import de.df.jutils.print.printables.HeaderFooterPrintable;

public class PrintableCreator {

    public static <T extends ASchwimmer> Printable createInfoPagePrintable(AWettkampf<T> wk) {
        String s = wk.getStringProperty(INFOPAGE);
        if ((s == null) || (s.length() == 0)) {
            return EmptyPrintable.Instance;
        }
        return new TextPrintable(s, PrintManager.getFont());
    }

    public static <T extends ASchwimmer> Printable createZielrichterentscheidPrintable(AWettkampf<T> wk) {
        Printable p = null;
        ZielrichterentscheidPrintable<T> zep = new ZielrichterentscheidPrintable<>(CompetitionUtils.getFilteredInstance(wk));
        p = new HeaderFooterPrintable(zep, new MessageFormat(I18n.get("Zielrichterentscheide")), null, PrintManager.getFont());
        return p;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T extends ASchwimmer> Printable createPenaltiesPrintable(AWettkampf wk, boolean kurz) {
        JPanel[] panels = PenaltyUIUtils.getPenalties(wk, kurz);
        if (panels == null) {
            return EmptyPrintable.Instance;
        }
    
        Component[] cs = new Component[panels.length];
        for (int x = 0; x < panels.length; x++) {
            panels[x].setOpaque(false);
            panels[x].setBackground(Color.WHITE);
            panels[x].setBorder(new LineBorder(Color.BLACK, 1));
            cs[x] = panels[x];
        }
        Printable p = null;
        if (kurz) {
            p = new ComponentPackingPrintable(3, 3, false, cs);
        } else {
            p = new ComponentListPrintable2(3, false, cs);
        }
        
        return PrintManager.getHeaderPrintable(p, I18n.get("Penaltylist"));
    }

    public static <T extends ASchwimmer> Printable createBestzeitenPrintable(AWettkampf<T> wk) {
        JComponent[] tables = PrintUtils.getSchnellsteZeitenTables(wk);
        if (tables == null) {
            return null;
        }
        return new ComponentListPrintable2(false, tables);
    
    }

}
