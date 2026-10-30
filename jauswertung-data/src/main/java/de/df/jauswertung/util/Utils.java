/*
 * Created on 20.11.2005
 */
package de.df.jauswertung.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.prefs.Preferences;

import com.rits.cloning.Cloner;
import com.rits.cloning.CloningException;
import com.rits.cloning.IDeepCloner;
import com.rits.cloning.IFastCloner;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;


public final class Utils {

    private static final Cloner cloner;

    static {
        cloner = new RecordCloner();
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

    private final static JaroWinklerSimilarity metric = new JaroWinklerSimilarity();
    private final static double SimilarityBoundary = 0.9;

    public static boolean areSimilar(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        if (s1.contains(s2)) {
            return true;
        }
        if (s2.contains(s1)) {
            return true;
        }
        if (AreMarked(s1, s2)) {
            return false;
        }
        double result = metric.apply(s1.trim(), s2.trim());
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
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
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
        @Override
        public Object clone(final Object t, final IDeepCloner cloner, final Map<Object, Object> clones) {
            final Date m = (Date) t;
            return new Date(m.getTime());
        }
    }

    /**
     * Java does not allow setting the final fields of a record, so a record is rebuilt through its canonical
     * constructor from deep copies of its components. A cycle that runs through a record cannot be rebuilt that way,
     * because the record has to exist before its components are complete; it fails with a
     * {@link RecordCycleException} instead of recursing endlessly.
     */
    private static final class RecordCloner extends Cloner {

        private final ThreadLocal<Set<Object>> recordsInProgress = ThreadLocal
                .withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

        @Override
        @SuppressWarnings("unchecked")
        protected <T> T cloneInternal(T o, Map<Object, Object> clones) {
            if (o == null || !o.getClass().isRecord()) {
                return super.cloneInternal(o, clones);
            }
            if (clones != null && clones.containsKey(o)) {
                return (T) clones.get(o);
            }
            Set<Object> inProgress = recordsInProgress.get();
            if (!inProgress.add(o)) {
                throw new RecordCycleException(o.getClass());
            }
            try {
                T copy = cloneRecord(o, clones);
                if (clones != null) {
                    clones.put(o, copy);
                }
                return copy;
            } finally {
                inProgress.remove(o);
            }
        }

        @SuppressWarnings("unchecked")
        private <T> T cloneRecord(T o, Map<Object, Object> clones) {
            RecordComponent[] components = o.getClass().getRecordComponents();
            Object[] values = new Object[components.length];
            Class<?>[] types = new Class<?>[components.length];
            try {
                for (int x = 0; x < components.length; x++) {
                    Method accessor = components[x].getAccessor();
                    accessor.setAccessible(true);
                    values[x] = cloneInternal(accessor.invoke(o), clones);
                    types[x] = components[x].getType();
                }
                Constructor<?> constructor = o.getClass().getDeclaredConstructor(types);
                constructor.setAccessible(true);
                return (T) constructor.newInstance(values);
            } catch (ReflectiveOperationException e) {
                throw new CloningException("Cannot copy record " + o.getClass().getName(), e);
            }
        }
    }

    public static <T extends Object> T copy(T t) {
        if (t == null) {
            return null;
        }
        return cloner.deepClone(t);
    }
}
