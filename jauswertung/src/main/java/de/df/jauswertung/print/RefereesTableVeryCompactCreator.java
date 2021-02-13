/*
 * Created on 01.06.2005
 */
package de.df.jauswertung.print;

import static de.df.jutils.util.StringTools.toHtml;

import java.awt.Component;
import java.awt.Font;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;

import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.kampfrichter.Kampfrichter;
import de.df.jauswertung.daten.kampfrichter.KampfrichterEinheit;
import de.df.jauswertung.daten.kampfrichter.KampfrichterPosition;
import de.df.jauswertung.daten.kampfrichter.KampfrichterStufe;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.print.ComponentListPrintable2;
import de.df.jutils.print.PrintManager;

public final class RefereesTableVeryCompactCreator {

    private RefereesTableVeryCompactCreator() {
        // Hide
    }

    private static <T extends ASchwimmer> String[] createTable(KampfrichterVerwaltung kt) {
        try {
            List<String> entries = new ArrayList<String>();
            for (int x = 0; x < kt.getEinheitenCount(); x++) {
                StringBuilder sb = new StringBuilder();
                KampfrichterEinheit ke = kt.getEinheit(x);
                if (!ke.isEmpty()) {
                    sb.append("<p><b>" + toHtml(ke.getName()) + "</b></p>");
                    String[] positionen = ke.getPositionen();
                    for (String position : positionen) {
                        KampfrichterPosition kp = ke.getPosition(position);
                        String stufe = KampfrichterEinheit.stufeToText(kp.getMinimaleStufe(), false);
                        String header = I18n.get("RefereePositionAndLevel", position, stufe, kp.getMinimaleStufe() == KampfrichterStufe.KEINE ? 0 : 1);
                        sb.append("<p>" + toHtml(header) + "</p>");
                        for (Kampfrichter kr : ke.getKampfrichter(position)) {
                            StringBuilder sx = new StringBuilder();
                            sx.append(kr.getName());

                            String krstufe = KampfrichterEinheit.stufeToText(kr.getStufe(), false);
                            if (kr.getGliederung().trim().length() > 0 && krstufe.length() > 0) {
                                sx.append(" (");
                                sx.append(kr.getGliederung().trim());
                                sx.append(" - ");
                                sx.append(krstufe);
                                sx.append(")");
                            } else if (kr.getGliederung().trim().length() > 0) {
                                sx.append(" (");
                                sx.append(kr.getGliederung().trim());
                                sx.append(")");
                            } else if (krstufe.length() > 0) {
                                sx.append(" (");
                                sx.append(krstufe);
                                sx.append(")");
                            }

                            if (kr.getBemerkung().trim().length() > 0) {
                                sx.append(": ");
                                sx.append(kr.getBemerkung().trim());
                            }

                            sb.append("<p style=\"margin-left: 10px;\">" + toHtml(sx.toString()) + "</p>");
                        }
                    }
                }
                entries.add(sb.toString());
            }

            return entries.toArray(new String[entries.size()]);
        } catch (RuntimeException re) {
            re.printStackTrace();
            return new String[] { "" };
        }
    }

    public static <T extends ASchwimmer> Printable getPrintable(KampfrichterVerwaltung kt) {
        String css = I18n.get("CSS.PrintNormal");
        if (PrintManager.getFont() != null) {
            Font f = PrintManager.getFont();
            css = I18n.get("CSS.PrintPlusFontNormal", f.getFontName(), f.getSize());
        }

        String[] entries = createTable(kt);
        List<Component> components = new ArrayList<Component>();
        for (String entry : entries) {
            XHTMLPanel panel = new XHTMLPanel();
            panel.setDocumentFromString(I18n.get("PrintRefereesHtml", css, entry), "", new XhtmlNamespaceHandler());
            panel.relayout();
            components.add(panel);
        }

        // return new XHTMLPrintable(panel);

        return new ComponentListPrintable2(false, components.toArray(new Component[components.size()]));
    }
}