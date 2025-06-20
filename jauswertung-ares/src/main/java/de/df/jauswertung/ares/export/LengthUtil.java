package de.df.jauswertung.ares.export;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

class LengthUtil {

    private static final String CHARSET = "ISO-8859-1";

    private static final String[][] shortnames = new String[][]{
            new String[]{"Obstacle Swim", "OS"}, new String[]{"Manikin Carry", "MC"},
            new String[]{"Manikin Carry with Fins", "MCF"}, new String[]{"Rescue Medley", "RM"},
            new String[]{"Manikin Tow with Fins", "MTF"}, new String[]{"Super Lifesaver", "SL"},
            new String[]{"Line Throw", "LT"}, new String[]{"Super Lifesaver", "SLS"},
            new String[]{"Obstacle Relay", "OR"}, new String[]{"Manikin Relay", "MKR"},
            new String[]{"Medley Relay", "MER"}, new String[]{"Mixed Pool Lifesaver Relay", "MLR"},
            new String[]{"Pool Lifesaver Relay", "MLR"}, new String[]{"Mixed Lifesaver Relay", "MLR"},
            new String[]{"Lifesaver Relay", "MLR"},
            };

    static Discipline guessLength(String disziplin) {
        String original = disziplin;
        if (disziplin.endsWith(" - Preheat")) {
            disziplin = disziplin.substring(0, disziplin.length() - " - Preheat".length());
        }
        int amount = 0;
        int length = 0;
        if (disziplin.startsWith("4x50m") || disziplin.startsWith("4*50m")) {
            disziplin = disziplin.substring(6);
            amount = 4;
            length = 50;
        } else if (disziplin.startsWith("4 x 50m") || disziplin.startsWith("4 * 50m")) {
            disziplin = disziplin.substring(8);
            amount = 4;
            length = 50;
        } else if (disziplin.startsWith("4x25m") || disziplin.startsWith("4*25m")) {
            disziplin = disziplin.substring(6);
            amount = 4;
            length = 25;
        } else if (disziplin.startsWith("4 x 25m") || disziplin.startsWith("4 * 25m")) {
            disziplin = disziplin.substring(8);
            amount = 4;
            length = 25;
        } else if (disziplin.startsWith("25m") || disziplin.startsWith("25 m")) {
            disziplin = disziplin.substring(4);
            amount = 1;
            length = 25;
        } else if (disziplin.startsWith("50m") || disziplin.startsWith("50 m")) {
            disziplin = disziplin.substring(4);
            amount = 1;
            length = 50;
        } else if (disziplin.startsWith("100m") || disziplin.startsWith("100 m")) {
            disziplin = disziplin.substring(5);
            amount = 1;
            length = 100;
        } else if (disziplin.startsWith("200m") || disziplin.startsWith("200 m")) {
            disziplin = disziplin.substring(5);
            amount = 1;
            length = 200;
        } else if (disziplin.equalsIgnoreCase("Line Throw")) {
            amount = 1;
            length = 100;
        } else if (disziplin.equalsIgnoreCase("Pool Lifesaver Relay")) {
            amount = 4;
            length = 50;
        } else if (disziplin.equalsIgnoreCase("Obstacle Swim")) {
            amount = 1;
            length = 200;
        } else if (disziplin.equalsIgnoreCase("Manikin Tow with Fins")) {
            amount = 1;
            length = 100;
        } else if (disziplin.equalsIgnoreCase("Manikin Carry with Fins")) {
            amount = 1;
            length = 100;
        } else if (disziplin.equalsIgnoreCase("Super Lifesaver")) {
            amount = 1;
            length = 200;
        } else if (disziplin.equalsIgnoreCase("Rescue Medley")) {
            amount = 1;
            length = 100;
        } else if (disziplin.equalsIgnoreCase("Manikin Carry")) {
            amount = 1;
            length = 50;
        } else if (disziplin.equalsIgnoreCase("Manikin Relay")) {
            amount = 2;
            length = 50;
        } else if (disziplin.equalsIgnoreCase("Obstacle Relay")) {
            amount = 4;
            length = 50;
        } else if (disziplin.equalsIgnoreCase("Mixed Pool Lifesaver Relay")) {
            amount = 4;
            length = 50;
        } else if (disziplin.equalsIgnoreCase("Medley Relay")) {
            amount = 4;
            length = 50;
        } else {
            System.err.println(disziplin);
        }
        if (disziplin.equalsIgnoreCase("Manikin Tow with Fins")) {
            disziplin = "Lifesaver";
        } else if (disziplin.equalsIgnoreCase("Manikin Carry with Fins")) {
            disziplin = "Manikin Carry w Fins";
        }
        if (disziplin.length() > 15) {
            disziplin = disziplin.substring(0, 15);
        }

        System.out.println("Discipline: " + disziplin + " - " + amount + "x" + length);


        return new Discipline(original, disziplin, amount, length);
    }

    static void writeStyles(Hashtable<String, Integer> disziplinen, OutputStream os) throws UnsupportedEncodingException {
        // idStyle;Style;StyleAbrév
        // 0; "Freistil " ;"FR"
        // 1; "Hindernis " ;"HI"
        // 2; "Rückenlage o.A. " ;"RU"
        // 3; "Schleppen e Puppe" ;"SP"
        // 4; "Vermischt " ;"ME"

        Hashtable<String, String> shorts = new Hashtable<>();
        for (String[] sn : shortnames) {
            shorts.put(sn[0].toLowerCase(), sn[1]);
        }

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("idStyle;Style;StyleAbrév");
        Enumeration<String> dis = disziplinen.keys();

        Hashtable<Integer, String> reverse = new Hashtable<>();

        LinkedList<Integer> ids = new LinkedList<>();
        while (dis.hasMoreElements()) {
            String d = dis.nextElement();
            int id = disziplinen.get(d);
            System.out.println(id + " => " + d);
            ids.add(id);
            reverse.put(id, d);
        }
        Collections.sort(ids);

        for (Integer id : ids) {
            String d = shortenDiscipline(reverse.get(id)).replace(" - Preheat", "");
            String dlower = d.toLowerCase();
            String shortname = "";
            if (shorts.containsKey(dlower)) {
                shortname = shorts.get(dlower);
            }

            if (shortname.isEmpty()) {
                System.out.println("Kürzel nicht gefunden: "+ d);
            }

            d = d.replace("\"", "");
            ps.println(id + ";\"" + d + "\";\"" + shortname + "\"");
        }
    }

    private static String shortenDiscipline(String d) {
        if (d.startsWith("200m ")) {
            d = d.substring("200m ".length());
        }
        if (d.startsWith("100m ")) {
            d = d.substring("100m ".length());
        }
        if (d.startsWith("50m ")) {
            d = d.substring("50m ".length());
        }
        if (d.startsWith("200 m ")) {
            d = d.substring("200 m ".length());
        }
        if (d.startsWith("100 m ")) {
            d = d.substring("100 m ".length());
        }
        if (d.startsWith("50 m ")) {
            d = d.substring("50 m ".length());
        }
        if (d.startsWith("4x25m ")) {
            d = d.substring("4x25m ".length());
        }
        if (d.startsWith("4x50m ")) {
            d = d.substring("4x50m ".length());
        }
        if (d.startsWith("4 x 25 m ")) {
            d = d.substring("4 x 25 m ".length());
        }
        if (d.startsWith("4 x 50 m ")) {
            d = d.substring("4 x 50 m ".length());
        }
        if (d.startsWith("4 x 25m ")) {
            d = d.substring("4 x 25m ".length());
        }
        if (d.startsWith("4 x 50m ")) {
            d = d.substring("4 x 50m ".length());
        }
        return d;
    }
}
