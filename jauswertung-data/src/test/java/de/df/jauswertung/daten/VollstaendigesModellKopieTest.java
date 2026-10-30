package de.df.jauswertung.daten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.df.jauswertung.util.Utils;

/**
 * Undo/redo keeps its snapshots as {@link Utils#copy} of the whole competition. A copy must equal the original in every
 * field, transient ones included, must not share a mutable object with it, and must keep objects shared that the
 * original shares.
 * <p>
 * The test only covers what {@link VollstaendigesModell} builds; {@link #vollstaendigesModellEnthaeltJedeModellklasse()}
 * fails for a class of {@code de.df.jauswertung.daten} that is not in it.
 */
class VollstaendigesModellKopieTest {

    private static final String DATEN = "de.df.jauswertung.daten";

    private static final Map<String, String> NICHT_IM_WETTKAMPF = Map.of(
            DATEN + ".Mannschaftsmitgliedermeldung", "Hülle von .wkmm-Dateien um einen Wettkampf",
            DATEN + ".veranstaltung.CompetitionContainer", "Veranstaltungswertung (.vs)",
            DATEN + ".veranstaltung.Veranstaltung", "Veranstaltungswertung (.vs)",
            DATEN + ".misc.BugReport", "Fehlerberichte",
            DATEN + ".misc.ReportBuilder", "Fehlerberichte",
            DATEN + ".PropertyConstants", "nur Konstanten",
            DATEN + ".laufliste.Laufliste$Meldesorter", "Comparator, wird nicht gespeichert",
            DATEN + ".laufliste.Laufliste$MeldezeitenComparator", "Comparator, wird nicht gespeichert",
            DATEN + ".laufliste.OWSelection", "Auswahl einer Runde, wird nur übergeben");

    private static final Set<Class<?>> WERTE = Set.of(String.class, Integer.class, Long.class, Short.class, Byte.class,
            Double.class, Float.class, Boolean.class, Character.class);

    static Stream<Arguments> wettkaempfe() {
        return Stream.of(Arguments.of("Einzel", (Supplier<AWettkampf<?>>) VollstaendigesModell::einzel),
                Arguments.of("Mannschaft", (Supplier<AWettkampf<?>>) VollstaendigesModell::mannschaft));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("wettkaempfe")
    void kopieIstGleichUndUnabhaengig(String name, Supplier<AWettkampf<?>> wettkampf) {
        AWettkampf<?> wk = wettkampf.get();

        new Vergleich().pruefe(wk, Utils.copy(wk), name);
    }

    @Test
    void vollstaendigesModellEnthaeltJedeModellklasse() throws IOException, URISyntaxException {
        Set<Class<?>> erreicht = Collections.newSetFromMap(new IdentityHashMap<>());
        for (AWettkampf<?> wk : List.<AWettkampf<?>>of(VollstaendigesModell.einzel(), VollstaendigesModell.mannschaft())) {
            Vergleich vergleich = new Vergleich();
            vergleich.pruefe(wk, Utils.copy(wk), wk.getClass().getSimpleName());
            erreicht.addAll(vergleich.klassen);
        }

        Set<String> fehlend = new TreeSet<>();
        for (Class<?> c : modellklassen()) {
            if (!NICHT_IM_WETTKAMPF.containsKey(c.getName()) && !erreicht.contains(c)) {
                fehlend.add(c.getName());
            }
        }
        assertTrue(fehlend.isEmpty(), "VollstaendigesModell um diese Klassen erweitern: " + fehlend);

        for (String ausgenommen : NICHT_IM_WETTKAMPF.keySet()) {
            assertTrue(erreicht.stream().noneMatch(c -> c.getName().equals(ausgenommen)),
                    ausgenommen + " ist im Wettkampf erreichbar, Ausnahme entfernen");
        }
    }

    private static Set<Class<?>> modellklassen() throws IOException, URISyntaxException {
        Path wurzel = Path.of(AWettkampf.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Set<Class<?>> klassen = Collections.newSetFromMap(new IdentityHashMap<>());
        try (Stream<Path> dateien = Files.walk(wurzel.resolve(DATEN.replace('.', '/')))) {
            for (Path datei : dateien.filter(p -> p.toString().endsWith(".class")).toList()) {
                String name = wurzel.relativize(datei).toString().replace(".class", "").replace('/', '.')
                        .replace('\\', '.');
                if (name.matches(".*\\$\\d+.*")) {
                    continue;
                }
                Class<?> c = forName(name);
                if (!c.isEnum() && !c.isInterface() && !Modifier.isAbstract(c.getModifiers()) && !c.isSynthetic()) {
                    klassen.add(c);
                }
            }
        }
        for (String ausgenommen : NICHT_IM_WETTKAMPF.keySet()) {
            forName(ausgenommen);
        }
        return klassen;
    }

    private static Class<?> forName(String name) {
        try {
            return Class.forName(name, false, AWettkampf.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            return fail("Klasse nicht gefunden: " + name, e);
        }
    }

    private static final class Vergleich {

        private final Map<Object, Object> zuordnung = new IdentityHashMap<>();
        private final Set<Object> kopien = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Set<Class<?>> klassen = Collections.newSetFromMap(new IdentityHashMap<>());
        private final List<Runnable> mengen = new ArrayList<>();

        /**
         * Sets have no order to walk them side by side; their elements are checked once the rest of the graph has
         * been mapped.
         */
        void pruefe(Object original, Object kopie, String pfad) {
            vergleiche(original, kopie, pfad);
            mengen.forEach(Runnable::run);
            mengen.clear();
        }

        private void vergleiche(Object original, Object kopie, String pfad) {
            if (original == null || kopie == null) {
                assertSame(original, kopie, pfad);
                return;
            }
            assertEquals(original.getClass(), kopie.getClass(), pfad);
            if (WERTE.contains(original.getClass()) || original instanceof Enum<?>) {
                assertEquals(original, kopie, pfad);
                return;
            }
            Object bisher = zuordnung.get(original);
            if (bisher != null) {
                assertSame(bisher, kopie, pfad + ": im Original geteiltes Objekt ist in der Kopie nicht mehr geteilt");
                return;
            }
            assertNotSame(original, kopie, pfad + ": Kopie teilt ein veränderliches Objekt mit dem Original");
            assertTrue(kopien.add(kopie), pfad + ": zwei Objekte des Originals haben dieselbe Kopie");
            zuordnung.put(original, kopie);

            Class<?> c = original.getClass();
            if (c.isArray()) {
                vergleicheArray(original, kopie, pfad);
            } else if (original instanceof Set<?> menge) {
                mengen.add(() -> vergleicheMenge(menge, (Set<?>) kopie, pfad));
            } else if (original instanceof Map<?, ?> map) {
                vergleicheMap(map, (Map<?, ?>) kopie, pfad);
            } else if (original instanceof List<?> || original instanceof Collection<?> && !(original instanceof Set<?>)) {
                vergleicheFolge((Collection<?>) original, (Collection<?>) kopie, pfad);
            } else if (original instanceof Date) {
                assertEquals(original, kopie, pfad);
            } else if (c.getName().startsWith("de.df.")) {
                vergleicheFelder(c, original, kopie, pfad);
            } else {
                fail(pfad + ": Typ " + c.getName() + " wird von diesem Test nicht verglichen; Vergleich erweitern");
            }
        }

        private void vergleicheArray(Object original, Object kopie, String pfad) {
            int laenge = Array.getLength(original);
            assertEquals(laenge, Array.getLength(kopie), pfad + ".length");
            for (int x = 0; x < laenge; x++) {
                vergleiche(Array.get(original, x), Array.get(kopie, x), pfad + "[" + x + "]");
            }
        }

        private void vergleicheMap(Map<?, ?> original, Map<?, ?> kopie, String pfad) {
            assertEquals(original.size(), kopie.size(), pfad + ".size");
            for (Map.Entry<?, ?> eintrag : original.entrySet()) {
                Object schluessel = eintrag.getKey();
                assertTrue(WERTE.contains(schluessel.getClass()) || schluessel instanceof Enum<?>,
                        pfad + ": Schlüssel vom Typ " + schluessel.getClass().getName() + "; Vergleich erweitern");
                assertTrue(kopie.containsKey(schluessel), pfad + "[" + schluessel + "] fehlt");
                vergleiche(eintrag.getValue(), kopie.get(schluessel), pfad + "[" + schluessel + "]");
            }
        }

        private void vergleicheMenge(Set<?> original, Set<?> kopie, String pfad) {
            assertEquals(original.size(), kopie.size(), pfad + ".size");
            for (Object o : original) {
                if (WERTE.contains(o.getClass()) || o instanceof Enum<?>) {
                    assertTrue(kopie.contains(o), pfad + ": " + o + " fehlt");
                    continue;
                }
                Object k = zuordnung.get(o);
                assertTrue(k != null, pfad + ": Element nur über diese Menge erreichbar; Vergleich erweitern");
                assertTrue(kopie.stream().anyMatch(e -> e == k), pfad + ": Kopie eines Elements fehlt");
            }
        }

        private void vergleicheFolge(Collection<?> original, Collection<?> kopie, String pfad) {
            assertEquals(original.size(), kopie.size(), pfad + ".size");
            Iterator<?> k = kopie.iterator();
            int x = 0;
            for (Object o : original) {
                vergleiche(o, k.next(), pfad + "[" + x++ + "]");
            }
        }

        private void vergleicheFelder(Class<?> c, Object original, Object kopie, String pfad) {
            if (c.getName().startsWith(DATEN + ".")) {
                klassen.add(c);
            }
            for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) {
                for (Field feld : k.getDeclaredFields()) {
                    if (Modifier.isStatic(feld.getModifiers())) {
                        continue;
                    }
                    feld.setAccessible(true);
                    try {
                        vergleiche(feld.get(original), feld.get(kopie), pfad + "." + feld.getName());
                    } catch (IllegalAccessException e) {
                        fail(pfad + "." + feld.getName(), e);
                    }
                }
            }
        }
    }
}
