package de.df.jauswertung.misc.meldetoolimport;

import java.io.*;
import java.util.*;

import javax.swing.table.DefaultTableModel;

import de.df.jauswertung.io.CsvUtils;
import de.df.jutils.util.StringTools;

class Meldungsimport {

    private ArrayList<Hashtable<String, String>> rows = new ArrayList<Hashtable<String, String>>();

    Meldungsimport(String file) {
        Object[][] data = CsvUtils.read(file);
        for (int y = 1; y < data.length; y++) {
            Hashtable<String, String> row = new Hashtable<String, String>();
            for (int x = 0; x < data[0].length; x++) {
                row.put(data[0][x].toString(), data[y][x].toString());
            }
            rows.add(row);
        }
    }

    int size() {
        return rows.size();
    }

    void erstelleMeldung(Zuordnung zos, Gliederungen gld, String file) {
        System.out.println("Creating registration");
        Object[][] data = null;
        Object[] header = null;

        if (zos.isSingle()) {
            // Single
            System.out.println("  Single mode");
            data = new Object[rows.size()][0];
            for (int x = 0; x < rows.size(); x++) {
                // System.out.println(" Row " + x);
                data[x] = berechneZeile(rows.get(x), zos, gld);
            }
            ArrayList<String> headers = new ArrayList<>();
            headers.add("S#");
            headers.add("Id");
            headers.add("Vorname");
            headers.add("Nachname");
            headers.add("Jahrgang");
            headers.add("Gliederung");
            headers.add("Altersklasse");
            headers.add("Geschlecht");
            Collections.addAll(headers, zos.getDisziplinen());
            header = headers.toArray(new String[headers.size()]);
        } else {
            // Team
            System.out.println("  Team mode");

            Hashtable<String, ArrayList<Hashtable<String, String>>> groupedRows = new Hashtable<String, ArrayList<Hashtable<String, String>>>();
            for (int x = 0; x < rows.size(); x++) {
                Hashtable<String, String> row = rows.get(x);
                String key = createKey(zos.GetTyp(), row.get("Mannschaftsname"), row.get("Geschlecht"));
                System.out.println("  " + key);
                ArrayList<Hashtable<String, String>> list;
                if (groupedRows.containsKey(key)) {
                    list = groupedRows.get(key);
                } else {
                    list = new ArrayList<Hashtable<String, String>>();
                    groupedRows.put(key, list);
                }
                list.add(row);
            }

            int maxMembers = 6;
            switch (zos.GetTyp()) {
            default:
                maxMembers = 6;
                break;
            case OpenMixed:
            case PoolMixed:
                maxMembers = 12;
                break;
            }

            data = new Object[groupedRows.size()][0];
            ArrayList<Hashtable<String, String>>[] values = groupedRows.values()
                    .toArray(new ArrayList[groupedRows.size()]);
            for (int x = 0; x < groupedRows.size(); x++) {
                data[x] = berechneZeile(values[x], zos, gld, maxMembers);
            }

            ArrayList<String> headers = new ArrayList<>();
            headers.add("S#");
            headers.add("Id");
            headers.add("Name");
            headers.add("Gliederung");
            headers.add("Altersklasse");
            headers.add("Geschlecht");
            for (int x = 0; x < maxMembers; x++) {
                headers.add("Vorname " + (x + 1));
                headers.add("Nachname " + (x + 1));
                headers.add("Jahrgang " + (x + 1));
                headers.add("Geschlecht " + (x + 1));
            }
            Collections.addAll(headers, zos.getDisziplinen());
            Collections.addAll(headers, zos.getDisziplinen(" - Reihenfolge"));
            header = headers.toArray(new String[headers.size()]);
        }
        if (header != null) {
            FileOutputStream fis;
            try {
                System.out.println("  Writing " + file);
                fis = new FileOutputStream(file);
                CsvUtils.write(new FileOutputStream(file), new DefaultTableModel(data, header));
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String createKey(Zuordnungstyp typ, String t1, String t2) {
        if (Zuordnungstyp.OpenMixed == typ || Zuordnungstyp.PoolMixed == typ) {
            return t1;
        }
        return t1 + "|" + t2;
    }

    private static String getGliederung(Mannschaft m) {
        String gldstr = m.get("Gliederung");
        if (gldstr == null || gldstr.trim().length() == 0) {
            gldstr = m.get("Mannschaftsname");
        }
        return gldstr;
    }

    private Object[] berechneZeile(ArrayList<Hashtable<String, String>> members, Zuordnung zos, Gliederungen gld,
            int maxMembers) {
        @SuppressWarnings("unchecked")
        Mannschaft m = new Mannschaft(members.toArray(new Hashtable[members.size()]));

        ArrayList<Object> row = new ArrayList<Object>();
        String gldstr = getGliederung(m);

        System.out.println(gldstr);

        String sex = m.get("Geschlecht").equals("1") ? "f" : "m";
        if (zos.GetTyp() == Zuordnungstyp.OpenMixed || zos.GetTyp() == Zuordnungstyp.PoolMixed) {
            sex = "x";
        }

        int id1 = gld.GetId(gldstr);
        String id2 = m.get("Geschlecht");
        row.add("" + id1 + (id2.equals("1") ? "13" : "14"));
        row.add("" + id1);
        row.add(gldstr);
        row.add(gldstr);
        row.add("Open");
        row.add(sex);
        for (int x = 0; x < maxMembers; x++) {
            MMitglied mm = m.getMitglied(x);
            if (mm == null) {
                Collections.addAll(row, "", "", "", "");
            } else {
                Collections.addAll(row, mm.toInfo());
            }
        }
        for (int x = 0; x < zos.getDisziplinen().length; x++) {
            String d = toAuswahl(m.get(GetKey(zos, x)));
            if (d == null || d.trim().length() == 0) {
                d = m.getReihenfolge(zos, x);
                if (d == null || d.trim().length() == 0) {
                    d = "";
                } else {
                    d = "x";
                }
            }
            row.add(d);
        }
        for (int x = 0; x < zos.getDisziplinen().length; x++) {
            row.add(m.getReihenfolge(zos, x));
        }
        return row.toArray();
    }

    private Object[] berechneZeile(Hashtable<String, String> entry, Zuordnung zos, Gliederungen gld) {
        ArrayList<Object> row = new ArrayList<Object>();
        String gldstr = entry.get("Gliederung");
        if (gldstr == null || gldstr.trim().length() == 0) {
            gldstr = entry.get("Mannschaftsname");
        }

        for (Map.Entry<String, String> kv : entry.entrySet()) {
            System.out.println(kv.getKey() + " => " + kv.getValue());
        }

        int id1 = gld.GetId(gldstr);
        int id2x = Integer.parseInt(entry.get("Starternummer")) + (entry.get("Geschlecht").equals("1") ? 0 : 6);
        String id2 = "" + id2x;
        row.add("" + id1 + (id2.length() > 1 ? "" : "0") + id2);
        row.add("" + id1 + "-" + id2);
        row.add(entry.get("Vorname"));
        row.add(entry.get("Nachname"));
        row.add(toJahrgang(entry.get("Geburtstag")));
        row.add(entry.get("Mannschaftsname"));
        row.add("Open");
        row.add(entry.get("Geschlecht").equals("1") ? "f" : "m");
        for (int x = 0; x < zos.getDisziplinen().length; x++) {
            row.add(toAuswahl(entry.get(GetKey(zos, x))));
        }
        return row.toArray();
    }

    private String toAuswahl(String d) {
        if ("ja".equals(d)) {
            d = "x";
        } else if ("nein".equals(d)) {
            d = "";
        } else if ("9:99,99".equals(d)) {
            d = "9:59,99";
        } else {
            try {
                int i = Integer.parseInt(d);
                int m = i / 10000;
                int s = (i / 100) % 100;
                int h = i % 100;
                int zeit = m * 60 * 100 + s * 100 + h;
                if (zeit > 100) {
                    d = StringTools.zeitString(zeit);
                } else {
                    d = "x";
                }
            } catch (Exception ex) {
                // Nothing to do
            }
        }
        return d;
    }

    private String GetKey(Zuordnung zu, int index) {
        String prefix = "";
        switch (zu.GetTyp()) {
        case OpenSingle:
            prefix = "OCEAN-Einzel-Start-";
            break;
        case OpenTeam:
            prefix = "OCEAN-Staffel-Startposition-";
            break;
        case OpenMixed:
            prefix = "OCEAN-Mixed-Startposition-";
            break;
        case PoolSingle:
            prefix = "Pool-Einzel-Meldezeit-";
            break;
        case PoolTeam:
            prefix = "Pool-Staffel-Meldezeit-";
            break;
        case PoolMixed:
            prefix = "Pool-Mixed-Meldezeit-";
            break;
        }
        return prefix + (index + 1);
    }

    public static String toJahrgang(String date) {
        String[] parts;
        if (date.contains("-")) {
            parts = date.split("-");
            return parts[0];
        }
        parts = date.split("\\.");
        return parts[parts.length - 1];
    }
}
