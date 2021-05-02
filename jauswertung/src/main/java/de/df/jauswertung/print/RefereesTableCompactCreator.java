/*
 * Created on 01.06.2005
 */
package de.df.jauswertung.print;

import java.awt.print.Printable;
import java.util.LinkedList;

import javax.swing.JTable;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.kampfrichter.KampfrichterEinheit;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jutils.print.TextTablePrintable;

public final class RefereesTableCompactCreator {

    private RefereesTableCompactCreator() {
        // Hide
    }

    private static <T extends ASchwimmer> JTable createTable(KampfrichterVerwaltung kt) {
        try {
            LinkedList<String> names = new LinkedList<>();
            LinkedList<String> data = new LinkedList<>();
            LinkedList<String> comment = new LinkedList<>();

            String last = " ";

            for (int x = 0; x < kt.getEinheitenCount(); x++) {
                KampfrichterEinheit ke = kt.getEinheit(x);
                if (!ke.isEmpty()) {
                    names.addLast(ke.getName() + "   ");
                    data.addLast(" ");
                    comment.addLast(" ");
                    String[][] contents = ke.getInhalt();
                    for (int y = 0; y < contents.length; y++) {
                        if (!last.equals(contents[y][0])) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("  ");
                            sb.append(contents[y][0]);
                            if ((contents[y][5].length() > 0) && !contents[y][5].equals("-")) {
                                sb.append("  (Stufe ");
                                sb.append(contents[y][5]);
                                sb.append(")");
                            }
                            sb.append(":");
                            names.addLast(sb.toString());

                            last = contents[y][0];
                        } else {
                            names.addLast(" ");
                        }

                        StringBuilder content = new StringBuilder();
                        content.append(contents[y][1]);
                        if (contents[y][2].length() > 0) {
                            content.append(" (");
                            content.append(contents[y][2]);
                            content.append(")");
                        }
                        if ((contents[y][4].length() > 0) && (!contents[y][4].equals("-"))) {
                            content.append(", Stufe ");
                            content.append(contents[y][4]);
                        }
                        data.addLast(content.toString());

                        if (contents[y][3].length() > 0) {
                            comment.addLast(contents[y][3]);
                        } else {
                            comment.addLast(" ");
                        }
                    }
                }
            }

            return TextTablePrintable.createTable(names.toArray(new String[names.size()]),
                    data.toArray(new String[data.size()]), comment.toArray(new String[comment.size()]));
        } catch (RuntimeException re) {
            re.printStackTrace();
            return null;
        }
    }

    public static <T extends ASchwimmer> Printable getPrintable(KampfrichterVerwaltung kt) {
        return new TextTablePrintable(RefereesTableCompactCreator.createTable(kt));
    }
}