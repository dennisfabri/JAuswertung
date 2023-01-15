package de.df.jauswertung.misc.meldetoolimport;

import java.util.ArrayList;
import java.util.Hashtable;

public class Mannschaft {

    private final MMitglied[] mitglieder;

    public Mannschaft(Hashtable<String, String>[] members) {
        mitglieder = new MMitglied[members.length];
        for (int x = 0; x < members.length; x++) {
            mitglieder[x] = new MMitglied(members[x]);
        }
    }

    public String get(String key) {
        if ("Name".equals(key)) {
            key = "Gliederung";
        }
        if ("Maennlich".equals(key)) {
            key = "Geschlecht";
        }
        int x = 0;
        while (x < mitglieder.length) {
            String result = mitglieder[x].get(key);
            if (result != null && result.trim().length() > 0) {
                return result;
            }
            x++;
        }
        return "";
    }

    public MMitglied getMitglied(int x) {
        if (x >= mitglieder.length) {
            return null;
        }
        return mitglieder[x];
    }

    public String getReihenfolge(Zuordnung zos, int index) {
        String prefix = "";
        switch (zos.GetTyp()) {
        case PoolTeam:
            prefix = "Pool-Staffel-Startposition-";
            break;
        case OpenTeam:
            prefix = "OCEAN-Staffel-Startposition-";
            break;
        default:
            return "";
        }
        String key = prefix + (index + 1);

        int[] entries = new int[6];
        for (int x = 0; x < entries.length; x++) {
            entries[x] = -1;
        }
        for (int x = 0; x < mitglieder.length; x++) {
            MMitglied mm = mitglieder[x];
            String s = mm.get(key);
            try {
                int pos = Integer.parseInt(s);
                entries[pos - 1] = x + 1;
            } catch (Exception ex) {
                // Nothing to do
            }
        }
        ArrayList<String> data = new ArrayList<>();
        for (int x = 0; x < entries.length; x++) {
            if (entries[x] > 0) {
                data.add("" + entries[x]);
            }
        }
        return String.join(",", data);
    }
}
