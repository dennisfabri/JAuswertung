package de.df.jauswertung.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

public class TextFileUtils {

    public static String[] fileToStringArray(String directory, String datei, String[] backup, boolean toLowerCase) {
        return TextFileUtils.fileToStringArray(directory + java.io.File.separator + datei, backup, toLowerCase);
    }

    public static String[] fileToStringArray(String datei, String[] backup, boolean toLowerCase) {
        java.util.Vector<String> v = new java.util.Vector<String>();
    
        FileInputStream fis = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(datei);
            br = new BufferedReader(new InputStreamReader(fis, "Cp1252"));
            String s = br.readLine();
            while (s != null) {
                s = s.trim();
                if (s.length() > 0) {
                    if (toLowerCase) {
                        s = s.toLowerCase();
                    }
                    v.add(s);
                }
                s = br.readLine();
            }
            fis.close();
            Object[] o = v.toArray();
            String[] strings = new String[o.length];
            for (int x = 0; x < o.length; x++) {
                strings[x] = (String) o[x];
            }
            return strings;
        } catch (Exception e) {
            e.printStackTrace();
            return backup;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception io) {
                // Nothing to do
            }
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception io) {
                // Nothing to do
            }
        }
    }

    public static boolean StringArrayToFile(String directory, String datei, String[] data) {
        FileOutputStream fis = null;
        PrintWriter br = null;
        try {
            fis = new FileOutputStream(directory + java.io.File.separator + datei);
            br = new PrintWriter(new OutputStreamWriter(fis, "Cp1252"));
            for (String d : data) {
                br.println(d);
            }
            br.close();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

}
