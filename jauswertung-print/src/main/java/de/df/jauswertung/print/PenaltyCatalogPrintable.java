/**
 * 
 */
package de.df.jauswertung.print;

import java.awt.*;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.daten.regelwerk.StrafenKapitel;
import de.df.jauswertung.daten.regelwerk.StrafenParagraph;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.ComponentListPrintable2;

public class PenaltyCatalogPrintable<T extends ASchwimmer> extends ComponentListPrintable2 {

    public PenaltyCatalogPrintable(AWettkampf<T> wk) {
        super(0, false, createPenalties(wk).toArray(new Component[0]));
    }

    private static <T extends ASchwimmer> LinkedList<Component> createPenalties(AWettkampf<T> wk) {

        Strafen strafen = wk.getStrafen();
        ListIterator<StrafenKapitel> chapters = strafen.getKapitel().listIterator();

        LinkedList<Component> text = new LinkedList<>();

        while (chapters.hasNext()) {
            StrafenKapitel chapter = chapters.next();
            if (!chapter.getName().equals("Sonstiges")) {
                JLabel c = createLabel(chapter.getName(), 1);
                text.addLast(c);

                for (StrafenParagraph paragraph : chapter.getParagraphen()) {
                    JLabel p = createLabel(paragraph.getName(), 2);
                    text.addLast(p);

                    for (Strafe s : paragraph.getStrafen()) {
                        JPanel px = new JPanel(new FormLayout("0dlu,25dlu,2dlu,fill:default:grow",
                                                              "0dlu,fill:default,0dlu,fill:default,2dlu"));

                        px.add(createLabel(s.getShortname().isEmpty() ? "" : s.getShortname(), 3),
                               CC.xywh(2, 2, 1, 3, "fill,center"));

                        JTextArea tp = new JTextArea();
                        tp.setOpaque(false);
                        tp.setText(s.getName());
                        tp.setWrapStyleWord(true);
                        tp.setLineWrap(true);
                        tp.setBorder(null);
                        tp.setFont(PrintManager.getFont());
                        tp.setForeground(Color.BLACK);

                        px.add(tp, CC.xy(4, 2));
                        px.add(createLabel(PenaltyUtils.getPenaltyValue(s, null), 3), CC.xy(4, 4));

                        text.addLast(px);
                    }
                }
            }
        }
        return text;
    }

    private static JLabel createLabel(String text, int size) {
        if (text == null) {
            text = "";
        }
        switch (size) {
        case 1:
            text = "<html><body><h2>" + text + "</h2></body></html>";
            break;
        case 2:
            text = "<html><body><h3>" + text + "</h3></body></html>";
            break;
        default:
            break;
        }
        JLabel l = new JLabel(text);
        l.setFont(PrintManager.getFont());
        l.setForeground(Color.BLACK);
        return l;
    }

}
