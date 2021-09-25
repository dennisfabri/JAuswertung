/*
 * InOut.java Created on 10. Februar 2001, 14:43
 */

package de.df.jauswertung.io;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.misc.BugReport;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.util.Utils;
import de.df.jutils.data.HashtableUtils;

/**
 * @author Dennis Fabri
 * @version
 */

public final class OutputManager {

    private OutputManager() {
        // Hide constructor
    }

    public static synchronized boolean speichereAKs(String name, Regelwerk aks) {
        return AgeGroupIOUtils.speichereAKs(name, aks);
    }

    @SuppressWarnings("rawtypes")
    public static boolean speichereWettkampf(String name, AWettkampf wk) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (wk == null) {
            throw new NullPointerException();
        }
        byte[] logo = (byte[]) wk.getProperty(PropertyConstants.LOGO);
        Hashtable<String, byte[]> data = null;
        if (logo != null) {
            wk = Utils.copy(wk);
            data = new Hashtable<String, byte[]>();
            data.put("logo.png", logo);
            wk.setProperty(PropertyConstants.LOGO, null);
        }
        return speichereObject(name, wk, data);
    }

    public static boolean speichereKampfrichter(String name, KampfrichterVerwaltung kv) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (kv == null) {
            throw new NullPointerException();
        }
        return speichereObject(name, kv, null);
    }

    public static synchronized boolean speichereBugReport(String name, BugReport br) {
        return speichereObject(name, br, null);
    }

    public static synchronized boolean speichereObject(String name, Object o, Hashtable<String, byte[]> extdata) {
        try {
            FileOutputStream ostream = new FileOutputStream(name);
            boolean value = speichereObject(ostream, o, extdata);
            ostream.close();
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            // Nothing to do
        }
        return false;
    }

    public static synchronized boolean speichereObject(OutputStream os, Object o) {
        return speichereObject(os, o, null);
    }

    public static synchronized boolean speichereObject(String name, Object o) {
        return speichereObject(name, o, null);
    }

    public static synchronized boolean speichereObject(OutputStream ostream, Object o, Hashtable<String, byte[]> extdata) {
        try {
            ZipOutputStream zos = new ZipOutputStream(ostream);

            // New XML-Save
            zos.putNextEntry(new ZipEntry("data.xml"));
            OutputStreamWriter osw = new OutputStreamWriter(zos, StandardCharsets.UTF_8);
            IOUtils.toXML(o, osw);

            if (extdata != null) {
                for (String key : HashtableUtils.getKeyIterable(extdata)) {
                    byte[] data = extdata.get(key);
                    zos.putNextEntry(new ZipEntry(key.toString()));
                    zos.write(data);
                }
            }

            zos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // Nothing to do
        }
        return false;
    }

    public static synchronized boolean serializeObject(OutputStream ostream, Serializable o) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(ostream);
            oos.writeObject(o);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // Nothing to do
        }
        return false;
    }

    public static synchronized byte[] serialize(Serializable o) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.toXML(o, bos);
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            // Nothing to do
        }
        return null;
    }
}