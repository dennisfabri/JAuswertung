/*
 * Created on 08.01.2005
 */
package de.df.jauswertung.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.misc.BugReport;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.util.Utils;

/**
 * @author Dennis Fabri
 * @date 08.01.2005
 */
public final class InputManager {

    private static Logger log = LoggerFactory.getLogger(InputManager.class);

    private InputManager() {
        // Hide constructor
    }

    public static Strafen ladeStrafen(String name, boolean einzel) {
        if (name == null) {
            name = AgeGroupIOUtils.getDefaultAKsName();
        }
        if (name.endsWith(".ake") || name.endsWith(".rwe") || name.endsWith(".akm") || name.endsWith(".rwm")) {
            name = name.substring(0, name.length() - 4);
        }

        String file = name;
        Strafen s = ladeStrafenIntern(file, einzel);

        if (s == null) {
            file = name.substring(name.lastIndexOf(File.separator) + 1);
            s = ladeStrafenIntern(file, einzel);
        }
        if (s == null) {
            file = name.substring(name.lastIndexOf("/") + 1);
            s = ladeStrafenIntern(file, einzel);
        }
        if (s == null) {
            if (name.lastIndexOf("'") > 0) {
                file = name.substring(0, name.lastIndexOf("'"));
                s = ladeStrafenIntern(file, einzel);
            }
        }
        if (s == null) {
            return ladeStrafenIntern("default", einzel);
        }
        return s;
    }

    private static Strafen ladeStrafenIntern(String absolute, boolean einzel) {
        Strafen s = (Strafen) ladeObject("penalties" + File.separator + absolute + (einzel ? "e" : "m") + ".def");
        if (s == null) {
            s = (Strafen) ladeObject("penalties" + File.separator + absolute + ".def");
        }
        if (s == null) {
            s = (Strafen) ladeObject(absolute + (einzel ? "e" : "m") + ".def");
        }
        if (s == null) {
            s = (Strafen) ladeObject(absolute + ".def");
        }
        return s;
    }

    public static synchronized Regelwerk ladeAKs(String name) {
        return AgeGroupIOUtils.ladeAKs(name);
    }

    @SuppressWarnings("rawtypes")
    public static AWettkampf ladeWettkampf(String name) {
        Hashtable<String, Object> data = ladeObjekte(name);
        if (data == null) {
            return null;
        }
        AWettkampf wk = (AWettkampf) data.get("data.xml");
        if (data.get("logo.png") instanceof byte[]) {
            wk.setProperty(PropertyConstants.LOGO, data.get("logo.png"));
        }
        return wk;
    }

    @SuppressWarnings("rawtypes")
    public static AWettkampf ladeWettkampf(byte[] daten) {
        Hashtable<String, Object> data = ladeObjekte(daten);
        if (data == null) {
            return null;
        }
        AWettkampf wk = (AWettkampf) data.get("data.xml");
        if (data.get("logo.png") instanceof byte[]) {
            wk.setProperty(PropertyConstants.LOGO, data.get("logo.png"));
        }
        return wk;
    }

    public static KampfrichterVerwaltung ladeKampfrichter(String name) {
        Object o = ladeObject(name);
        if (o == null) {
            o = ladeObject("referees/" + name);
        }
        if (o == null) {
            if (!name.toLowerCase().endsWith(".kr")) {
                o = ladeKampfrichter(name + ".kr");
            }
        }
        return (KampfrichterVerwaltung) o;
    }

    public static synchronized BugReport ladeBugReport(String name) {
        BugReport br = null;
        try {
            try (FileInputStream is = new FileInputStream(name); ObjectInputStream ois = new ObjectInputStream(is)) {
                return (BugReport) ois.readObject();
            }
        } catch (RuntimeException | IOException | ClassNotFoundException e) {
            return (BugReport) ladeObject(name);
        }
    }

    public static synchronized Object ladeObject(String name) {
        log.debug("Öffne Datei {}", name);
        try {
            // Try to read XML-Data from Zip-File
            FileInputStream zf = new FileInputStream(name);
            Object o = ladeObject(zf);
            zf.close();
            if (o != null) {
                return o;
            }
        } catch (IOException ef) {
            // ef.printStackTrace();
        } catch (RuntimeException ef) {
            // ef.printStackTrace();
        }
        try {
            // Try to read XML-Data from Zip-File
            FileInputStream zf = new FileInputStream(name);
            Object o = ladeUnzippedObject(zf);
            zf.close();
            if (o != null) {
                return o;
            }
        } catch (IOException ef) {
            // ef.printStackTrace();
        } catch (RuntimeException ef) {
            // ef.printStackTrace();
        }
        try {
            // Try to read XML-Data from Zip-File
            FileInputStream zf = new FileInputStream(Utils.getUserDir() + name);
            Object o = ladeObject(zf);
            zf.close();
            if (o != null) {
                return o;
            }
        } catch (RuntimeException ef1) {
            // ef1.printStackTrace();
            return null;
        } catch (IOException ef1) {
            // ef1.printStackTrace();
            return null;
        }
        return null;
    }

    public static synchronized Hashtable<String, Object> ladeObjekte(String name) {
        try {
            // Try to read XML-Data from Zip-File
            FileInputStream zf = new FileInputStream(name);
            Hashtable<String, Object> o = ladeObjekte(zf);
            zf.close();
            return o;
        } catch (IOException ef) {
            // Nothing to do
        } catch (RuntimeException ef) {
            // Nothing to do
        }
        try {
            // Try to read XML-Data from Zip-File
            FileInputStream zf = new FileInputStream(Utils.getUserDir() + name);
            Hashtable<String, Object> o = ladeObjekte(zf);
            zf.close();
            return o;
        } catch (FileNotFoundException fnf) {
            return null;
        } catch (Exception ef1) {
            // ef1.printStackTrace();
            return null;
        }
    }

    public static synchronized Hashtable<String, Object> ladeObjekte(byte[] data) {
        try {
            // Try to read XML-Data from Zip-File
            ByteArrayInputStream zf = new ByteArrayInputStream(data);
            Hashtable<String, Object> o = ladeObjekte(zf);
            zf.close();
            return o;
        } catch (Exception ef) {
            // ef.printStackTrace();
        }
        return null;
    }

    public static synchronized Object ladeObject(InputStream is) {
        try {
            // Try to read XML-Data from Zip-File
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze = zis.getNextEntry();
            if ((ze != null) && (!ze.getName().equals("data.xml"))) {
                return null;
            }
            return IOUtils.fromXML(zis);
        } catch (Exception ef) {
            return null;
        }
    }

    public static synchronized Object ladeUnzippedObject(InputStream is) {
        try {
            return IOUtils.fromXML(is);
        } catch (Exception ef) {
            return null;
        }
    }

    public static synchronized Hashtable<String, Object> ladeObjekte(InputStream is) {
        try {
            Hashtable<String, Object> result = new Hashtable<String, Object>();

            // Try to read XML-Data from Zip-File
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String name = ze.getName();
                if (!name.equals("data.xml")) {
                    ByteArrayOutputStream bis = new ByteArrayOutputStream();
                    int i = zis.read();
                    while (i >= 0) {
                        bis.write(i);
                        i = zis.read();
                    }
                    bis.flush();
                    byte[] data = bis.toByteArray();
                    result.put(name, data);
                } else {
                    Object o = IOUtils.fromXML(zis);

                    result.put("data.xml", o);
                }

                ze = zis.getNextEntry();
            }
            if (result.size() == 0) {
                return null;
            }
            return result;
        } catch (Exception ef) {
            ef.printStackTrace();
            return null;
        }
    }

    public static synchronized Object unserializeObject(InputStream is) {
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            Object o = ois.readObject();
            ois.close();
            return o;
        } catch (Exception ef) {
            // ef.printStackTrace();
            return null;
        }
    }

    public static synchronized Object unserialize(byte[] data) {
        try {
            return IOUtils.fromXML(new ByteArrayInputStream(data));
        } catch (IOException e) {
            return null;
        }
    }
}
