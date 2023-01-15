/*
 * Created on 20.11.2005
 */
package de.df.jauswertung.util;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.github.mpkorstanje.simmetrics.StringMetric;
import com.github.mpkorstanje.simmetrics.metrics.Jaro;
import com.rits.cloning.Cloner;
import com.rits.cloning.IDeepCloner;
import com.rits.cloning.IFastCloner;

import de.df.jutils.gui.util.UIPerformanceMode;

public final class Utils {

    private static final Cloner cloner;

    static {
        cloner = new Cloner();
        cloner.registerFastCloner(Hashtable.class, new FastClonerHashtable());
        cloner.registerFastCloner(Date.class, new FastClonerDate());

        cloner.setDumpClonedClasses(false);
    }

    private Utils() {
        // Hide
    }

    private final static String[][] NotSimilar = new String[][] {
            { "Gelsenkirchen-Buer", "Gelsenkirchen-Horst", "Gelsenkirchen-Mitte" }, { "Weimar", "Wismar" },
            { "Dettingen", "Ertingen", "Ettlingen" } };

    private static boolean initialized = false;
    private static ResourceBundle build = null;

    private final static StringMetric metric = new Jaro();
    private final static double SimilarityBoundary = 0.85;

    public static boolean areSimilar(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        if (s1.indexOf(s2) >= 0) {
            return true;
        }
        if (s2.indexOf(s1) >= 0) {
            return true;
        }
        if (AreMarked(s1, s2)) {
            return false;
        }
        double result = metric.compare(s1.trim(), s2.trim());
        return (result >= SimilarityBoundary);
    }

    private static boolean AreMarked(String s1, String s2) {
        for (String[] ns : NotSimilar) {
            boolean b1 = false;
            boolean b2 = false;
            for (String n : ns) {
                if (n.equalsIgnoreCase(s1)) {
                    b1 = true;
                } else if (n.equalsIgnoreCase(s2)) {
                    b2 = true;
                }
            }
            if (b1 && b2) {
                return true;
            }
        }
        return false;
    }

    private static void initialize() {
        if (initialized) {
            return;
        }
        synchronized (Utils.class) {
            initialized = true;
            try {
                build = ResourceBundle.getBundle("build");
            } catch (RuntimeException re) {
                // Nothin to do
            }
        }
    }

    public static String getBuildDate() {
        if (!initialized) {
            initialize();
        }
        if (build == null) {
            return DateFormat.getDateInstance(DateFormat.SHORT).format(new Date());
        }
        return build.getString("builddate");
    }

    public static void setDevelopmentMode(boolean enable) {
        try {
            if (enable) {
                Preferences dev = getPreferences().node("development");
                dev.put("dsm", "1");
                dev.put("pluginmanager", "1");
                dev.put("switchagegroups", "1");
            } else {
                if (getPreferences().nodeExists("development")) {
                    getPreferences().node("development").removeNode();
                }
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static boolean isInDevelopmentMode() {
        if (!initialized) {
            initialize();
        }
        try {
            return getPreferences().nodeExists("development");
        } catch (BackingStoreException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isInDevelopmentModeFor(String type) {
        if (!isInDevelopmentMode()) {
            return false;
        }

        Preferences dev = getPreferences().node("development");
        return dev.getLong(type.toLowerCase(), 0) == 1;
    }

    public static String getUserDir() {
        String dir = System.getProperty("user.dir");
        if (!dir.endsWith(File.separator)) {
            dir += File.separator;
        }
        return dir;
    }

    public static Preferences getPreferences() {
        return Preferences.userRoot().node("jauswertung");
    }

    private static class FastClonerHashtable implements IFastCloner {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
            final Hashtable<Object, Object> m = (Hashtable) t;
            final Hashtable<Object, Object> result = new Hashtable<>();
            for (final Map.Entry e : m.entrySet()) {
                result.put(cloner.deepClone(e.getKey(), clones), cloner.deepClone(e.getValue(), clones));
            }
            return result;
        }
    }

    private static class FastClonerDate implements IFastCloner {
        public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
            final Date m = (Date) t;
            return new Date(m.getTime());
        }
    }

    public static <T extends Object> T copy(T t) {
        if (t == null) {
            return null;
        }
        return cloner.deepClone(t);
    }

    public static UIPerformanceMode getUIPerformanceMode() {
        switch (getPreferences().getInt("UIPerformanceMode", UIPerformanceMode.Default.value)) {
        default:
            return UIPerformanceMode.Default;
        case 1:
            return UIPerformanceMode.Software;
        case 2:
            return UIPerformanceMode.OpenGL;
        }
    }

    public static void setUIPerformanceMode(UIPerformanceMode mode) {
        getPreferences().putInt("UIPerformanceMode", mode.value);
    }
}