package de.df.jauswertung.misc.meldetoolimport;

import java.util.Hashtable;

import de.df.jauswertung.io.CsvUtils;

public class Gliederungen {

    Hashtable<String, Integer> Map = new Hashtable<String, Integer>();

    public Gliederungen(String file) {
        Object[][] data = CsvUtils.read(file);
        for (Object[] row : data) {
            Map.put(row[0].toString().toLowerCase().trim(), Integer.parseInt(row[1].toString().trim()));
        }
    }

    public int GetId(String gld) {
        gld = gld.replace("DLRG ", "").replace("LV ", "").trim();
        return Map.get(gld.toLowerCase().trim());
    }
}
