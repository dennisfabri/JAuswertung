package de.df.jauswertung.misc.meldetoolimport;

import java.util.ArrayList;

import de.df.jauswertung.io.TextFileUtils;

public class Zuordnung {

    private Zuordnungstyp Typ;
    private String[] Disziplinen;

    public Zuordnung(String file, Zuordnungstyp typ) {
        Typ = typ;

        ArrayList<String> data = new ArrayList<String>();
        String[] rows = TextFileUtils.fileToStringArray(file, null, false);
        for (String row : rows) {
            row = row.trim();
            if (row.length() > 0) {
                // String[] entries = row.split(" ");

                data.add(row.substring(1).trim());
            }
        }
        Disziplinen = data.toArray(new String[data.size()]);
    }

    public Zuordnungstyp GetTyp() {
        return Typ;
    }

    public String[] getDisziplinen() {
        return Disziplinen;
    }

    public String[] getDisziplinen(String suffix) {
        String[] d = new String[Disziplinen.length];
        for (int x = 0; x < Disziplinen.length; x++) {
            d[x] = Disziplinen[x] + suffix;
        }
        return d;
    }

    public boolean isSingle() {
        return Typ == Zuordnungstyp.OpenSingle || Typ == Zuordnungstyp.PoolSingle;
    }
}
