package de.df.jauswertung.daten.laufliste;

import static de.df.jauswertung.daten.PropertyConstants.*;
import static de.df.jauswertung.daten.laufliste.Reihenfolge.fromValue;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import org.dom4j.Element;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.event.PropertyChangeListener;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.RandomUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;

/**
 * Erzeugt und verwaltet die Laufliste eines Wettkampfes
 */
public class Laufliste<T extends ASchwimmer> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2268666080913175340L;

    private final AWettkampf<T> wk;
    private LinkedList<Lauf<T>> laufliste;

    private Einteilung[] verteilung;
    private BlockEinteilung[] blocks = null;

    private static final Random random = RandomUtils.getRandomNumberGenerator();
    private static long seed = random.nextLong();

    private int mode = Laufeinteilungsmodus.Auto.getValue();

    private boolean isfinal = true;

    public boolean isFinal() {
        return isfinal;
    }

    public void setIsFinal(boolean f) {
        isfinal = f;
    }

    public final class WettkampfChangeListener implements PropertyChangeListener {
        @Serial
        private static final long serialVersionUID = -2390423576945211763L;

        @Override
        public void propertyChanged(Object source, String property) {
            if (HEATS_LANES.equals(property)) {
                int b = wk.getIntegerProperty(HEATS_LANES);
                if (laufliste != null) {
                    for (Lauf<T> tLauf : laufliste) {
                        tLauf.setBahnen(b);
                    }
                }
            }
        }
    }

    private record Meldesorter(int index) implements Comparator<ASchwimmer> {
        @Override
        public int compare(ASchwimmer o1, ASchwimmer o2) {
            return (int) ((o2.getMeldepunkte(index) - o1.getMeldepunkte(index)) * 100);
        }
    }

    public static class Einteilung implements Serializable {

        @XStreamAsAttribute
        private final int startgroup;
        @XStreamAsAttribute
        private final boolean male;
        @XStreamAsAttribute
        private int discipline;

        public Einteilung() {
            this(0, false, 0);
        }

        public Einteilung(int startgroup, boolean male, int discipline) {
            this.startgroup = startgroup;
            this.male = male;
            this.discipline = discipline;
        }

        public int getStartgruppe() {
            return startgroup;
        }

        public boolean isMaennlich() {
            return male;
        }

        public int getDisziplin() {
            return discipline;
        }

        public void setDisziplin(int d) {
            this.discipline = d;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Einteilung cmp)) {
                return false;
            }
            if (startgroup != cmp.startgroup) {
                return false;
            }
            if (male != cmp.male) {
                return false;
            }
            return discipline == cmp.discipline;
        }

        @Override
        public String toString() {
            return String.format("Einteilung(%d, %sd, %d)", startgroup, male ? "male" : "female", discipline);
        }
    }

    public static class BlockEinteilung implements Serializable {
        private final int startgroup;
        private final boolean male;

        public BlockEinteilung(int startgroup, boolean male) {
            this.startgroup = startgroup;
            this.male = male;
        }

        public int getStartgruppe() {
            return startgroup;
        }

        public boolean isMaennlich() {
            return male;
        }
    }

    /**
     * Creates new Laufliste
     *
     * @param wettkampf Der Wettkampf zu dem die Laufliste gehört
     */
    public Laufliste(AWettkampf<T> wettkampf) {
        wk = wettkampf;
        laufliste = null;
        verteilung = null;

        PropertyChangeListener pcl = new WettkampfChangeListener();
        wk.addPropertyChangeListener(pcl);
    }

    public Einteilung[] getVerteilung() {
        return getVerteilung(verteilung);
    }

    public Einteilung[] getVerteilung(Einteilung[] aufteilung) {
        if (aufteilung == null) {
            return getStandardVerteilung();
        }
        Einteilung[] daten = getStandardVerteilung();

        LinkedList<Einteilung> result = new LinkedList<>();
        for (Einteilung anAufteilung : aufteilung) {
            result.addLast(anAufteilung);
        }
        // Nicht mehr belegte AKs entfernen
        ListIterator<Einteilung> li = result.listIterator();
        while (li.hasNext()) {
            Einteilung ein = li.next();
            boolean found = false;
            for (Einteilung aDaten : daten) {
                if (aDaten.equals(ein)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                li.remove();
            }
        }
        for (Einteilung aDaten : daten) {
            boolean found = false;
            for (Einteilung einteilung : aufteilung) {
                if (aDaten.equals(einteilung)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.addLast(aDaten);
            }
        }
        return result.toArray(new Einteilung[result.size()]);
    }

    public BlockEinteilung[] getStandardBlocks() {
        LinkedList<Integer> sgs = new LinkedList<>();

        Startgruppe[] startgruppen = wk.getRegelwerk().getEffektiveStartgruppen();
        int anzahl = startgruppen.length;

        for (int x = 0; x < anzahl; x++) {
            if (SearchUtils.hasSchwimmer(wk, startgruppen[x])) {
                sgs.addLast(x);
            }
        }
        BlockEinteilung[] result = new BlockEinteilung[2 * sgs.size()];
        ListIterator<Integer> li = sgs.listIterator();
        for (int x = 0; li.hasNext(); x++) {
            int sg = li.next();
            result[2 * x] = new BlockEinteilung(sg, false);
            result[2 * x + 1] = new BlockEinteilung(sg, true);
        }
        return result;
    }

    public BlockEinteilung[] getBlocks() {
        if (blocks == null) {
            return getStandardBlocks();
        }
        boolean extend = false;
        BlockEinteilung[] temp = getStandardBlocks();
        BlockEinteilung[] temp2 = Arrays.copyOf(temp, temp.length);
        for (int x = 0; x < temp.length; x++) {
            boolean found = false;
            for (BlockEinteilung block : blocks) {
                if (temp[x].equals(block)) {
                    found = true;
                    temp[x] = null;
                    break;
                }
            }
            if (!found) {
                // Es werden nicht alle besetzten Altersklassen verwendet
                extend = true;
            }
        }

        for (int y = 0; y < blocks.length; y++) {
            boolean found = false;
            for (int x = 0; x < temp2.length; x++) {
                if (blocks[y].equals(temp2[x])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                blocks[y] = null;
                extend = true;
            }
        }

        if (extend) {
            LinkedList<BlockEinteilung> liste = new LinkedList<>();
            for (BlockEinteilung block : blocks) {
                if (block != null) {
                    liste.addLast(block);
                }
            }
            for (BlockEinteilung aTemp : temp) {
                if (aTemp != null) {
                    liste.addLast(aTemp);
                }
            }
            BlockEinteilung[] result = new BlockEinteilung[liste.size()];
            ListIterator<BlockEinteilung> li = liste.listIterator();
            for (int x = 0; x < result.length; x++) {
                result[x] = li.next();
            }
            blocks = result;
        }
        return blocks;
    }

    public Einteilung[] getStandardVerteilung() {
        return getStandardVerteilung(null);
    }

    private static int getSGIndex(int[][] sgs, int index) {
        int ix = getSGIndex(sgs, index, false);
        if (ix >= 0) {
            return ix;
        }
        return getSGIndex(sgs, index, true);
    }

    private static int getSGIndex(int[][] sgs, int index, boolean male) {
        if (sgs == null) {
            return index;
        }
        if (sgs.length <= index) {
            return -1;
        }
        return sgs[index][male ? 1 : 0];
    }

    @SuppressWarnings("unchecked")
    public Einteilung[] getStandardVerteilung(int[][] sgs) {
        // Bestimme maximale Anzahl an Disziplinen in einer Altersklasse

        Startgruppe[] startgruppen = wk.getRegelwerk().getEffektiveStartgruppen();

        int maxdisz = 0;
        int mindisz = Integer.MAX_VALUE;
        int mindisz2 = Integer.MAX_VALUE;
        int anzahl = (sgs == null ? startgruppen.length : sgs.length);
        int[] diszArray = new int[anzahl];
        for (int x = 0; x < anzahl; x++) {
            int index = getSGIndex(sgs, x);
            if (index >= 0) {
                Startgruppe sg = startgruppen[index];
                LinkedList<Altersklasse> aks = wk.getRegelwerk().getAKsForStartgroup(sg);
                Altersklasse ak = aks.getFirst();
                diszArray[x] = ak.getDiszAnzahl();
                if (ak.getDiszAnzahl() > diszArray[x]) {
                    diszArray[x] = ak.getDiszAnzahl();
                }
                if (diszArray[x] > maxdisz) {
                    maxdisz = diszArray[x];
                }
                if (diszArray[x] < mindisz) {
                    mindisz = diszArray[x];
                }
                if ((diszArray[x] > 1) && (diszArray[x] < mindisz2)) {
                    mindisz2 = diszArray[x];
                }
            }
        }
        if (mindisz2 < Integer.MAX_VALUE) {
            mindisz = mindisz2;
        }

        LinkedList<Einteilung>[] disciplines = new LinkedList[maxdisz];
        for (int x = 0; x < maxdisz; x++) {
            disciplines[x] = new LinkedList<>();
        }

        boolean hasMinPlus1 = false;
        for (int x = 0; x < mindisz; x++) {
            for (int y = 0; y < anzahl; y++) {
                for (int z = 0; z < 2; z++) {
                    int index = getSGIndex(sgs, y, z == 1);
                    if (index >= 0) {
                        Startgruppe sg = startgruppen[index];
                        LinkedList<Altersklasse> aks = wk.getRegelwerk().getAKsForStartgroup(sg);
                        Altersklasse ak = aks.getFirst();
                        if ((ak.getDiszAnzahl() > x) && SearchUtils.hasSchwimmer(wk, sg, z == 1)) {
                            if (diszArray[y] <= mindisz) {
                                disciplines[x].addLast(new Einteilung(index, z == 1, x));
                            }
                            if (diszArray[y] == mindisz + 1) {
                                hasMinPlus1 = true;
                            }
                        }
                    }
                }
            }
        }
        if (hasMinPlus1) {
            for (int x = 0; x < mindisz + 1; x++) {
                LinkedList<Einteilung> temp = new LinkedList<>();
                for (int y = 0; y < anzahl; y++) {
                    for (int z = 0; z < 2; z++) {
                        int index = getSGIndex(sgs, y, z == 1);
                        if ((index >= 0) && SearchUtils.hasSchwimmer(wk,
                                startgruppen[index]) && (diszArray[y] == mindisz + 1)) {
                            temp.addLast(new Einteilung(index, z == 1, x));
                        }
                    }
                }
                disciplines[x].addAll(0, temp);
            }
        }

        for (int x = 0; x < maxdisz; x++) {
            LinkedList<Einteilung> temp = new LinkedList<>();
            for (int y = 0; y < anzahl; y++) {
                for (int z = 0; z < 2; z++) {
                    int index = getSGIndex(sgs, y, z == 1);
                    if ((index >= 0) && SearchUtils.hasSchwimmer(wk, startgruppen[index])) {
                        if ((diszArray[y] > mindisz + (hasMinPlus1 ? 1 : 0)) && (diszArray[y] > x)) {
                            temp.addLast(new Einteilung(index, z == 1, x));
                        }
                    }
                }
            }

            // Disziplinen abwechselnd in der Mitte und am Ende einfügen
            int size = disciplines[x / 2].size();
            if (x % 2 == 0) {
                disciplines[x / 2].addAll(size / 2, temp);
            } else {
                disciplines[x / 2].addAll(temp);
            }
        }

        LinkedList<Einteilung> daten = new LinkedList<>();

        for (int x = 0; x < maxdisz; x++) {
            daten.addAll(disciplines[x]);
        }

        return daten.toArray(Einteilung[]::new);
    }

    public synchronized void erzeugen() {
        mode = Laufeinteilungsmodus.Auto.getValue();
        erzeugen(null, true);
    }

    public boolean isStandardVerteilung() {
        if (verteilung == null) {
            return true;
        }
        return isStandardVerteilung(verteilung);
    }

    private boolean isStandardVerteilung(Einteilung[] aufteilung) {
        if (aufteilung == null) {
            return true;
        }
        Einteilung[] daten = getStandardVerteilung();
        if (aufteilung.length != daten.length) {
            return false;
        }
        for (int x = 0; x < daten.length; x++) {
            if (!aufteilung[x].equals(daten[x])) {
                return false;
            }
        }
        return true;
    }

    private static boolean[] getSelection(int lanes, Hashtable<String, boolean[]> selection, String discipline) {
        boolean[] s = null;
        if (selection != null) {
            s = selection.get(discipline);
        }
        if (s != null) {
            if (s.length == lanes) {
                return s;
            }
        }

        if (s == null) {
            s = new boolean[0];
        }
        boolean[] b = new boolean[lanes];
        for (int x = 0; x < b.length; x++) {
            if (x >= s.length) {
                b[x] = true;
            } else {
                b[x] = s[x];
            }
        }
        return b;

    }

    private static int getBahnen(boolean[] selection, int bahnen) {
        if (selection == null) {
            return bahnen;
        }
        int result = bahnen;
        for (int x = 0; x < Math.min(selection.length, bahnen); x++) {
            if (!selection[x]) {
                result--;
            }
        }
        return result;
    }

    private Einteilung[] blocksToEinteilung(BlockEinteilung[] sgs) {
        if (sgs == null) {
            return null;
        }
        LinkedList<Einteilung> result = new LinkedList<>();

        Startgruppe[] startgruppen = wk.getRegelwerk().getEffektiveStartgruppen();

        int[][] selected = new int[startgruppen.length][2];
        int pos = 0;
        while (pos < sgs.length) {
            for (int x = 0; x < selected.length; x++) {
                for (int y = 0; y < 2; y++) {
                    selected[x][y] = -1;
                }
            }
            int offset = 0;
            while ((pos < sgs.length) && (sgs[pos].getStartgruppe() >= 0)) {
                boolean male = sgs[pos].isMaennlich();
                selected[sgs[pos].getStartgruppe()][male ? 1 : 0] = offset;
                pos++;
                offset++;
            }
            pos++;
            Einteilung[] temp = getStandardVerteilung(offsetToIndizes(selected));
            if (temp != null) {
                for (Einteilung aTemp : temp) {
                    result.addLast(aTemp);
                }
            }
        }
        return result.toArray(new Einteilung[result.size()]);
    }

    private static int[][] offsetToIndizes(int[][] offset) {
        int max = -1;
        for (int[] anOffset : offset) {
            for (int y = 0; y < 2; y++) {
                if (max < anOffset[y]) {
                    max = anOffset[y];
                }
            }
        }
        max++;
        int[][] result = new int[max][2];
        if (max > 0) {
            for (int x = 0; x < max; x++) {
                for (int y = 0; y < 2; y++) {
                    result[x][y] = -1;
                }
                for (int i = 0; i < offset.length; i++) {
                    for (int j = 0; j < 2; j++) {
                        if (offset[i][j] == x) {
                            result[x][j] = i;
                        }
                    }
                }
            }
        }
        return result;
    }

    public synchronized boolean erzeugen(BlockEinteilung[] sgs) {
        mode = Laufeinteilungsmodus.Blocks.getValue();
        blocks = sgs;
        return erzeugen(blocksToEinteilung(sgs), false);
    }

    public synchronized boolean erzeugen(Einteilung[] aufteilung) {
        return erzeugen(aufteilung, true);
    }

    @SuppressWarnings("unchecked")
    private synchronized boolean erzeugen(Einteilung[] aufteilung, boolean update) {
        if (update) {
            mode = Laufeinteilungsmodus.Einteilung.getValue();
        }
        // Aufraeumen
        reset();

        // Soll eine leere Laufliste erzeugt werden?
        if (wk.getBooleanProperty(HEATS_EMPTY_LIST)) {
            add(0);
            return false;
        }

        seed = random.nextLong();

        if (aufteilung == null) {
            verteilung = null;
            aufteilung = getStandardVerteilung();
        } else {
            if (isStandardVerteilung(aufteilung)) {
                verteilung = null;
            } else {
                aufteilung = getVerteilung(aufteilung);
                verteilung = aufteilung;
            }
        }
        if (update) {
            if (isStandardVerteilung()) {
                mode = Laufeinteilungsmodus.Auto.getValue();
            }
        }

        boolean reordered = false;

        Startgruppe[] startgruppen = wk.getRegelwerk().getEffektiveStartgruppen();

        if (verteilung != null) {
            // pos enthält den aktuellen Index der Disziplin je AK und
            // Geschlecht
            int[][] pos = new int[startgruppen.length][2];
            // reihenfolge enthält die Reihenfolge der Disziplinen
            // je AK und Geschlecht.
            int[][][] reihenfolge = new int[startgruppen.length][0][0];
            for (int x = 0; x < reihenfolge.length; x++) {
                Altersklasse ak = wk.getRegelwerk().getAKsForStartgroup(startgruppen[x]).getFirst();
                reihenfolge[x] = new int[2][ak.getDiszAnzahl()];
                for (int z = 0; z < 2; z++) {
                    // Inizes auf 0 setzen
                    pos[x][z] = 0;
                    // Standardreihenfolge initialisieren
                    for (int y = 0; y < reihenfolge[x][z].length; y++) {
                        reihenfolge[x][z][y] = y;
                    }
                }
            }

            // Bestimmen der Reihenfolge der Disziplinen
            for (int x = 0; x < verteilung.length; x++) {
                int sg = aufteilung[x].getStartgruppe();
                boolean male = aufteilung[x].isMaennlich();
                int disz = aufteilung[x].getDisziplin();

                int index = pos[sg][male ? 1 : 0];

                if (disz != index) {
                    // aufteilung anpassen
                    aufteilung[x] = new Einteilung(sg, male, index);
                    // Reihenfolge zur Korrektur merken
                    reihenfolge[sg][male ? 1 : 0][index] = disz;
                }

                // Index erhoehen
                pos[sg][male ? 1 : 0]++;
            }

            // verteilung2 = aufteilung;

            // Daten merken
            int[][][] memory = new int[reihenfolge.length][2][0];
            for (int x = 0; x < reihenfolge.length; x++) {
                for (int y = 0; y < reihenfolge[x].length; y++) {
                    memory[x][y] = new int[reihenfolge[x][y].length];
                    System.arraycopy(reihenfolge[x][y], 0, memory[x][y], 0, reihenfolge[x][y].length);
                }
            }

            // r speichert die Reihenfolge der Disziplinen je AK für die
            // Umsortierung in wk.reorderDisciplines(r).
            int[][] r = new int[reihenfolge.length][0];
            for (int x = 0; x < reihenfolge.length; x++) {
                r[x] = new int[reihenfolge[x][0].length];
                for (int y = 0; y < reihenfolge[x][0].length; y++) {
                    if (reihenfolge[x][0][y] != reihenfolge[x][1][y]) {
                        // TODO: Vielleicht kann man hier noch etwas
                        // Geschickteres tun.
                        r[x][y] = reihenfolge[x][0][y];
                        reihenfolge[x][0][y] = y;
                    } else {
                        r[x][y] = reihenfolge[x][0][y];
                        reihenfolge[x][0][y] = y;
                    }
                }
                // Anpassung der maennlichen Disziplinenreihenfolge
                // an die weibliche Reihenfolge
                for (int z = 0; z < reihenfolge[x][0].length; z++) {
                    for (int i = 0; i < reihenfolge[x][1].length; i++) {
                        if (memory[x][1][z] == memory[x][0][i]) {
                            reihenfolge[x][1][z] = i;
                        }
                    }
                }
            }

            wk.reorderDisciplines(r);
            reordered = true;

            // Korrekturen der Reihenfolge, wenn unterschiedliche Reihenfolge je
            // Geschlecht.
            for (Einteilung t : aufteilung) {
                if (t.isMaennlich()) {
                    int sg = t.getStartgruppe();
                    int d = t.getDisziplin();

                    t.setDisziplin(reihenfolge[sg][1][d]);
                }
            }
        }

        int bahnen = wk.getIntegerProperty(HEATS_LANES);
        boolean respectQualifications = wk.getBooleanProperty(HEATS_RESPECT_QUALIFICATIONS)
                && !wk.HasOpenQualifications();

        Hashtable<String, boolean[]> laneSelection = (Hashtable<String, boolean[]>) wk
                .getProperty(PropertyConstants.HEATS_LANE_SELECTION);

        boolean dsmmode = startDSMMode();

        // Schwimmer nach Altersklassen trennen und anschließend sortieren,
        // um sie besser verarbeiten zu können
        LinkedList<T>[][] schwimmer = new LinkedList[startgruppen.length][2];
        for (int sg = 0; sg < schwimmer.length; sg++) {
            schwimmer[sg][0] = SearchUtils.getSchwimmer(wk, startgruppen[sg], false);
            schwimmer[sg][1] = SearchUtils.getSchwimmer(wk, startgruppen[sg], true);

            if (respectQualifications) {
                for (int y = 0; y < 2; y++) {
                    schwimmer[sg][y].removeIf(t -> !t.getQualifikation().isAccepted());
                }
            }

            vorsortieren(wk, schwimmer[sg][0], startgruppen[sg], dsmmode);
            vorsortieren(wk, schwimmer[sg][1], startgruppen[sg], dsmmode);
        }

        boolean rotieren = wk.getBooleanProperty(HEATS_ROTATE);
        boolean rotierenJeAK = wk.getIntegerProperty(PropertyConstants.HEATS_SORTING_ORDER) == Reihenfolge.Regelwerk
                .getValue();

        // Schwimmer auf die Läufe verteilen
        LinkedList<Lauf<T>>[][][] laeufe = new LinkedList[startgruppen.length][3][0];
        for (int sg = 0; sg < startgruppen.length; sg++) {
            Altersklasse akx = wk.getRegelwerk().getAKsForStartgroup(startgruppen[sg]).getFirst();
            laeufe[sg][0] = new LinkedList[akx.getDiszAnzahl()];
            laeufe[sg][1] = new LinkedList[akx.getDiszAnzahl()];
            laeufe[sg][2] = new LinkedList[akx.getDiszAnzahl()];
            for (int disz = 0; disz < akx.getDiszAnzahl(); disz++) {
                boolean r = rotieren;
                if (rotierenJeAK) {
                    r = startgruppen[sg].hasLaufrotation();
                }
                if (startgruppen[sg].getLaufsortierung() == Reihenfolge.Meldezeiten.getValue()) {
                    r = false;
                }

                nachsortieren(wk, schwimmer[sg][0], startgruppen[sg], disz, dsmmode);
                nachsortieren(wk, schwimmer[sg][1], startgruppen[sg], disz, dsmmode);
                LinkedList<Lauf<T>>[] result = startgruppeVerteilen(wk,
                        disz,
                        akx.getDiszAnzahl(),
                        startgruppen[sg],
                        schwimmer[sg],
                        bahnen,
                        r,
                        getSelection(bahnen,
                                laneSelection,
                                akx.getDisziplin(disz, true)
                                        .getName()),
                        isFinal());

                laeufe[sg][0][disz] = result[0];
                laeufe[sg][1][disz] = result[1];
                laeufe[sg][2][disz] = result[2];
            }
        }

        boolean mixedInFront = wk.getBooleanProperty(HEATS_MIXED_IN_FRONT);

        // Läufe zu einer Laufliste zusammenführen
        for (Einteilung anAufteilung : aufteilung) {
            int sg = anAufteilung.getStartgruppe();
            boolean male = anAufteilung.isMaennlich();
            int disz = anAufteilung.getDisziplin();

            LinkedList<Altersklasse> aks = wk.getRegelwerk().getAKsForStartgroup(startgruppen[sg]);
            Altersklasse ak = aks.getFirst();

            assert disz >= 0;
            assert disz < ak.getDiszAnzahl();

            if (mixedInFront) {
                if (laeufe[sg][2][disz] != null) {
                    laufliste.addAll(laeufe[sg][2][disz]);
                    laeufe[sg][2][disz] = null;
                }
            }
            laufliste.addAll(laeufe[sg][male ? 1 : 0][disz]);
            if (!mixedInFront) {
                if (laeufe[sg][2][disz] != null) {
                    laufliste.addAll(laeufe[sg][2][disz]);
                    laeufe[sg][2][disz] = null;
                }
            }
        }

        if (wk.getBooleanProperty(PropertyConstants.HEATS_JOIN_HEATS) && (laufliste.size() >= 2)) {
            ListIterator<Lauf<T>> li = laufliste.listIterator();
            Lauf<T> previous = li.next();
            while (li.hasNext()) {
                Lauf<T> current = li.next();
                if (previous.join(current)) {
                    li.remove();
                } else {
                    previous = current;
                }
            }
        }

        stopDSMMode(dsmmode);

        for (Lauf<T> tLauf : laufliste) {
            tLauf.resetUsable();
        }

        neueNummerierung();

        return reordered;
    }

    @SuppressWarnings("unchecked")
    private boolean startDSMMode() {
        if (!Utils.isInDevelopmentModeFor("DSM")) {
            return false;
        }
        Object o = wk.getProperty(PropertyConstants.DSM_MODE_DATA);
        if (!(o instanceof int[][][] movements)) {
            return false;
        }

        {
            int[][][] ints = (int[][][]) o;
            boolean allzero = true;
            for (int[][] anInt : ints) {
                for (int[] value : anInt) {
                    for (int i : value) {
                        if (i != 0) {
                            allzero = false;
                            break;
                        }
                    }
                }
            }
            if (allzero) {
                return false;
            }
        }

        for (T t : wk.getSchwimmer()) {
            t.setBemerkung("");
        }

        Startgruppe[] startgruppen = wk.getRegelwerk().getEffektiveStartgruppen();

        LinkedList<T>[][] schwimmer = new LinkedList[startgruppen.length][2];
        for (int sg = 0; sg < schwimmer.length; sg++) {
            schwimmer[sg][0] = SearchUtils.getSchwimmer(wk, startgruppen[sg], false);
            schwimmer[sg][1] = SearchUtils.getSchwimmer(wk, startgruppen[sg], true);
            vorsortieren(wk, schwimmer[sg][0], startgruppen[sg], true);
            vorsortieren(wk, schwimmer[sg][1], startgruppen[sg], true);
        }
        for (int x = 0; x < movements.length; x++) {
            if (x < startgruppen.length) {
                for (int y = 0; y < movements[x].length; y++) {
                    if ((y != x) && (y < startgruppen.length)) {
                        for (int z = 0; z < 2; z++) {
                            int amount = movements[x][y][z];
                            if (amount > schwimmer[y][z].size()) {
                                amount = schwimmer[y][z].size();
                            }
                            for (int i = 0; i < amount; i++) {
                                T t = schwimmer[y][z].removeLast();
                                schwimmer[x][z].addLast(t);

                                LinkedList<Altersklasse> aks = wk.getRegelwerk().getAKsForStartgroup(startgruppen[x]);
                                Altersklasse ak = aks.getFirst();

                                t.setBemerkung(ak.getName());
                                t.setAusserKonkurrenz(true);
                            }
                        }
                    }
                }
            }
        }
        wk.setProperty(PropertyConstants.HEATS_NOT_COMPETING_MIXED, true);

        CompetitionUtils.switchAGs(wk);
        return true;
    }

    private void stopDSMMode(boolean enabled) {
        if (enabled) {
            CompetitionUtils.switchAGs(wk);
            for (T t : wk.getSchwimmer()) {
                t.setAusserKonkurrenz(false);
                t.setBemerkung("");
            }
        }
    }

    private static <T extends ASchwimmer> void vorsortieren(AWettkampf<T> wk, LinkedList<T> schwimmer, Startgruppe sg,
            boolean dsmmode) {
        Reihenfolge sort = fromValue(wk.getIntegerProperty(HEATS_SORTING_ORDER));
        if (wk.getIntegerProperty(HEATS_SORTING_ORDER) == Reihenfolge.Regelwerk.getValue()) {
            sort = fromValue(sg.getLaufsortierung());
        }
        vorsortieren(sort, wk, schwimmer, dsmmode);
    }

    private static <T extends ASchwimmer> void vorsortieren(Reihenfolge sort, AWettkampf<T> wk, LinkedList<T> schwimmer,
            boolean dsmmode) {
        switch (sort) {
        case ZufallJeDisziplin, Meldezeiten, ILSPool, ILSPoolVorlauf, ILSOpenWater, ILSOpenWaterVorlauf:
            seed = random.nextLong();
            break;
        default:
            break;
        }
        Collections.shuffle(schwimmer, RandomUtils.getRandomNumberGenerator(seed));
        switch (sort) {
        case GleicheGliederungVerteilen:
            try {
                vorsortierenVerteilt(wk, schwimmer);
            } catch (RuntimeException re) {
                re.printStackTrace();
            }
            break;
        case GleicheGliederungGegeneinander:
            schwimmer.sort(Comparator.comparing(ASchwimmer::getGliederung));
            break;
        case Meldepunkte:
            Comparator<ASchwimmer> comparator2 = new Meldesorter(
                    wk.getIntegerProperty(PropertyConstants.HEATS_REGISTERED_POINTS_INDEX, 0));
            schwimmer.sort(comparator2);
            break;
        case Meldezeiten:
        case ILSPool:
        case ILSPoolVorlauf:
        case ILSOpenWater:
        case ILSOpenWaterVorlauf:
            // Vorsortierung zufällig. So dass bei fehlenden Zeiten der Zufall
            // entscheidet
        case ZufallJeDisziplin:
        case Zufall:
        default:
            break;
        }

        // Normale und Schwimmer "außer Konkurrenz" trennen?
        if (wk.getBooleanProperty(HEATS_NOT_COMPETING_MIXED) || dsmmode) {
            schwimmer.sort((o1, o2) -> {
                if (o1.isAusserKonkurrenz() == o2.isAusserKonkurrenz()) {
                    return 0;
                }
                return (o1.isAusserKonkurrenz() ? 1 : -1);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends ASchwimmer> void vorsortierenVerteilt(AWettkampf<T> wk, LinkedList<T> ll) {
        if ((ll == null) || (ll.isEmpty())) {
            return;
        }

        int size = ll.size();
        int bahnen = wk.getIntegerProperty(HEATS_LANES);

        int anzahlGliederungen = wk.getGliederungen().size();

        LinkedList<T>[] listen = new LinkedList[anzahlGliederungen];
        for (int x = 0; x < anzahlGliederungen; x++) {
            listen[x] = new LinkedList<>();
        }

        int zahl = 0;
        while (!ll.isEmpty()) {
            ListIterator<T> li = ll.listIterator();
            T temp = li.next();
            li.remove();
            listen[zahl].addLast(temp);
            String g = temp.getGliederung();
            while (li.hasNext()) {
                temp = li.next();
                if (g.equals(temp.getGliederung())) {
                    li.remove();
                    listen[zahl].addLast(temp);
                }
            }
            zahl++;
        }

        int amount = listen.length;
        while (listen[amount - 1].isEmpty()) {
            amount--;
        }

        // Cleanup empty organisations
        LinkedList<T>[] listentemp = new LinkedList[amount];
        System.arraycopy(listen, 0, listentemp, 0, amount);
        listen = listentemp;
        Arrays.sort(listen, new Comparator<LinkedList<T>>() {
            @Override
            public int compare(LinkedList<T> o1, LinkedList<T> o2) {
                return o1.size() - o2.size();
            }
        });

        int anzahl = size / bahnen;
        LinkedList<T> overflow = new LinkedList<>();
        while (size % bahnen != 0) {
            for (int x = listen.length - 1; x >= 0; x--) {
                if (!listen[x].isEmpty()) {
                    overflow.addLast(listen[x].getFirst());
                    listen[x].removeFirst();
                    size--;
                }
                if (size % bahnen == 0) {
                    break;
                }
            }
        }
        T[][] laeufe = (T[][]) new ASchwimmer[anzahl][bahnen];
        int pos = 0;
        int bahn = 0;
        for (LinkedList<T> aListen : listen) {
            for (T t : aListen) {
                laeufe[pos][bahn] = t;
                pos++;
                if (pos == anzahl) {
                    bahn++;
                    pos = 0;
                }
            }
        }

        ll.clear();

        ll.addAll(overflow);
        for (int y = 0; y < anzahl; y++) {
            for (int x = 0; x < bahnen; x++) {
                ll.addLast(laeufe[y][x]);
            }
        }
    }

    private static <T extends ASchwimmer> void nachsortieren(AWettkampf<T> wk, LinkedList<T> schwimmer, Startgruppe sg,
            int disziplin, boolean dsmmode) {
        Reihenfolge sort = fromValue(wk.getIntegerProperty(HEATS_SORTING_ORDER));
        if (sort == Reihenfolge.Regelwerk) {
            sort = fromValue(sg.getLaufsortierung());
        }
        switch (sort) {
        default:
            break;
        case Meldezeiten:
        case ILSPool:
        case ILSPoolVorlauf:
            Comparator<ASchwimmer> comparator = new MeldezeitenComparator(disziplin);
            schwimmer.sort(comparator);
            break;
        }

        // Normale und Schwimmer "außer Konkurrenz" trennen?
        if ((wk.getBooleanProperty(HEATS_NOT_COMPETING_MIXED)) || (dsmmode)) {
            schwimmer.sort((Comparator<ASchwimmer>) (o1, o2) -> {
                if (o1.isAusserKonkurrenz() == o2.isAusserKonkurrenz()) {
                    return 0;
                }
                return (o1.isAusserKonkurrenz() ? 1 : -1);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends ASchwimmer> LinkedList<Lauf<T>>[] startgruppeVerteilen(AWettkampf<T> wk, int disziplin,
            int disziplinAnzahl, Startgruppe sg, LinkedList<T>[] schwimmer, int bahnen, boolean rotieren,
            boolean[] laneSelection,
            boolean isFinal) {
        LinkedList<Lauf<T>>[] result = new LinkedList[] {
                new LinkedList<Lauf<T>>(), new LinkedList<Lauf<T>>(), new LinkedList<Lauf<T>>()
        };

        if (schwimmer[0].isEmpty() && schwimmer[1].isEmpty()) {
            return result;
        }
        T s = schwimmer[0].isEmpty() ? schwimmer[1].getFirst() : schwimmer[0].getFirst();

        // Bei Disziplinenwahl nicht beteiligte Schwimmer entfernen
        if (s.getAK().isDisciplineChoiceAllowed()) {
            LinkedList<T>[] schwimmer2 = new LinkedList[2];
            schwimmer2[0] = new LinkedList<>(schwimmer[0]);
            schwimmer2[1] = new LinkedList<>(schwimmer[1]);

            for (int x = 0; x < 2; x++) {
                schwimmer2[x].removeIf(t -> !t.isDisciplineChosen(disziplin));
            }
            schwimmer = schwimmer2;
        }

        Reihenfolge reihenfolge = fromValue(wk.getIntegerProperty(HEATS_SORTING_ORDER));
        if (reihenfolge == Reihenfolge.Regelwerk) {
            reihenfolge = fromValue(sg.getLaufsortierung());
        }
        if (!isFinal) {
            if (reihenfolge == Reihenfolge.ILSPool) {
                reihenfolge = Reihenfolge.ILSPoolVorlauf;
            }
            if (reihenfolge == Reihenfolge.ILSOpenWater) {
                reihenfolge = Reihenfolge.ILSOpenWaterVorlauf;
            }
        }

        Reihenfolge reihenfolgeFinal = switch (reihenfolge) {
        case Regelwerk -> Reihenfolge.Regelwerk;
        case Zufall -> Reihenfolge.Zufall;
        case GleicheGliederungGegeneinander -> Reihenfolge.GleicheGliederungGegeneinander;
        case GleicheGliederungVerteilen -> Reihenfolge.GleicheGliederungVerteilen;
        case Meldepunkte -> Reihenfolge.Meldepunkte;
        case Meldezeiten, ILSPool, ILSOpenWater -> Reihenfolge.Meldezeiten;
        case ILSPoolVorlauf -> Reihenfolge.ILSPoolVorlauf;
        case ILSOpenWaterVorlauf -> Reihenfolge.ILSOpenWaterVorlauf;
        case ZufallJeDisziplin -> Reihenfolge.ZufallJeDisziplin;
        };

        rotieren = rotieren && reihenfolge.isRotatable();

        boolean mixedHeats = wk.getBooleanProperty(HEATS_MIXED);

        // Dürfen Läufe gemischt werden?
        if (mixedHeats) {
            // Wenn ja: Überprüfung, ob sich das Zusammenlegen lohnt
            int b = getBahnen(laneSelection, bahnen);
            int s1 = schwimmer[0].size() % b;
            int s2 = schwimmer[1].size() % b;
            if ((s1 + s2 <= b) && (s1 + s2 > 0) && (s2 > 0) && (s1 > 0)) {
                // Wenn es sich lohnt: Aufteilen
                // 1. Gemischter Lauf mit den schlechtesten Schwimmern
                // 2. Weibliche Läufe
                // 3. Männliche Läufe
                LinkedList<T> l1 = new LinkedList<>(schwimmer[0]);
                LinkedList<T> l2 = new LinkedList<>(schwimmer[1]);
                LinkedList<T> rest1 = new LinkedList<>();
                LinkedList<T> rest2 = new LinkedList<>();
                for (int x = 0; x < s1; x++) {
                    rest1.addFirst(l1.removeLast());
                }
                for (int x = 0; x < s2; x++) {
                    rest2.addFirst(l2.removeLast());
                }

                // 1.
                // Restliche Teilnehmer erst einzeln einsortieren,
                // anschließend Läufe zusammenfassen
                // -> Bessere Verteilung auf die Bahnen
                startgruppeVerteilen(wk,
                        result[2],
                        disziplin,
                        disziplinAnzahl,
                        reihenfolgeFinal,
                        rest1,
                        bahnen,
                        rotieren,
                        laneSelection,
                        isFinal);
                startgruppeVerteilen(wk,
                        result[2],
                        disziplin,
                        disziplinAnzahl,
                        reihenfolgeFinal,
                        rest2,
                        bahnen,
                        rotieren,
                        laneSelection,
                        isFinal);
                ListIterator<Lauf<T>> li = result[2].listIterator(result[2].size());
                Lauf<T> lauf1 = li.previous();
                Lauf<T> lauf2 = li.previous();
                lauf1.join(lauf2);
                li.remove();

                // 2.
                startgruppeVerteilen(wk,
                        result[0],
                        disziplin,
                        disziplinAnzahl,
                        isFinal ? reihenfolgeFinal : reihenfolge,
                        l1,
                        bahnen,
                        rotieren,
                        laneSelection,
                        isFinal);

                // 3.
                startgruppeVerteilen(wk,
                        result[1],
                        disziplin,
                        disziplinAnzahl,
                        isFinal ? reihenfolgeFinal : reihenfolge,
                        l2,
                        bahnen,
                        rotieren,
                        laneSelection,
                        isFinal);
                return result;
            }
        }
        startgruppeVerteilen(wk,
                result[0],
                disziplin,
                disziplinAnzahl,
                isFinal ? reihenfolgeFinal : reihenfolge,
                schwimmer[0],
                bahnen,
                rotieren,
                laneSelection,
                isFinal);
        startgruppeVerteilen(wk,
                result[1],
                disziplin,
                disziplinAnzahl,
                isFinal ? reihenfolgeFinal : reihenfolge,
                schwimmer[1],
                bahnen,
                rotieren,
                laneSelection,
                isFinal);
        return result;
    }

    private static <T extends ASchwimmer> void startgruppeVerteilen(AWettkampf<T> wk, LinkedList<Lauf<T>> laufliste,
            int disziplin, int disziplinAnzahl, Reihenfolge reihenfolge, LinkedList<T> schwimmer, int vBahnen,
            boolean rotieren,
            boolean[] laneSelection, boolean isFinal) {
        if (schwimmer.isEmpty()) {
            return;
        }

        switch (reihenfolge) {
        case ILSPoolVorlauf: {
            startgruppeVerteilenILS(wk,
                    laufliste,
                    disziplin,
                    disziplinAnzahl,
                    schwimmer,
                    vBahnen,
                    rotieren,
                    laneSelection);
            return;
        }
        case ILSOpenWaterVorlauf: {
            startgruppeVerteilenILSOutdoor(wk,
                    laufliste,
                    disziplin,
                    disziplinAnzahl,
                    schwimmer,
                    vBahnen,
                    rotieren,
                    laneSelection);
            return;
        }
        }

        int bahnen1 = Math.min(schwimmer.size(), vBahnen);

        int rotationsWeite = 2;
        if (bahnen1 == 4) {
            rotationsWeite = 1;
        } else if ((disziplinAnzahl == 3) && (bahnen1 == 8)) {
            rotationsWeite = 3;
        }

        LinkedList<Lauf<T>> ll = new LinkedList<>();
        Lauf<T> lauf = new Lauf<>(vBahnen, 0, 0, laneSelection);
        ll.addFirst(lauf);
        for (T t : schwimmer) {
            if (lauf.isFull()) {
                if (rotieren) {
                    lauf.rotieren(disziplinAnzahl - disziplin - 1, rotationsWeite);
                }
                lauf = new Lauf<>(vBahnen, 0, 0, laneSelection);
                ll.addFirst(lauf);
            }
            lauf.addSchwimmer(t, disziplin);
        }
        if (rotieren) {
            lauf.rotieren(disziplinAnzahl - disziplin - 1, lauf.getAnzahl() == 4 ? 1 : rotationsWeite);
        }

        if (wk.getBooleanProperty(HEATS_AVOID_ALMOST_EMPTY)) {
            // Verbesserte Verteilung auf die letzten beiden Läufe
            boolean fastestHeatUntouched = wk.getBooleanProperty(HEATS_AAE_FASTEST_HEAT_UNTOUCHED, false);
            if ((lauf.getAnzahl() < lauf.getBenutzbareBahnenAnzahl() - 2)
                    && (ll.size() >= (fastestHeatUntouched ? 3 : 2))) {
                Lauf<T> l1 = ll.get(0);
                Lauf<T> l2 = ll.get(1);
                int amount = l1.getAnzahl() + l2.getAnzahl();

                l1.leeren();
                l2.leeren();

                ListIterator<T> li = schwimmer.listIterator(schwimmer.size() - amount);

                int rest = amount / 2;
                amount = amount - rest;
                for (int x = 0; x < amount; x++) {
                    l2.addSchwimmer(li.next(), disziplin);
                }
                for (int x = 0; x < rest; x++) {
                    l1.addSchwimmer(li.next(), disziplin);
                }
                if (rotieren) {
                    l1.rotieren(disziplinAnzahl - disziplin - 1, rotationsWeite);
                    l2.rotieren(disziplinAnzahl - disziplin - 1, rotationsWeite);
                }
            }
        }

        laufliste.addAll(ll);
    }

    private static <T extends ASchwimmer> void startgruppeVerteilenILS(AWettkampf<T> wk, LinkedList<Lauf<T>> laufliste,
            int disziplin, int disziplinAnzahl, LinkedList<T> schwimmer, int vbahnen, boolean rotieren,
            boolean[] laneSelection) {
        if (schwimmer.isEmpty()) {
            return;
        }

        int bahnen = getBahnen(laneSelection, vbahnen);

        int amount = schwimmer.size() / bahnen + (schwimmer.size() % bahnen > 0 ? 1 : 0);

        @SuppressWarnings("unchecked")
        Lauf<T>[] ll = new Lauf[amount];
        for (int x = 0; x < ll.length; x++) {
            ll[x] = new Lauf<>(vbahnen, x, 0, laneSelection);
        }

        // Einteilung in drei Gruppen:
        // Gruppe 1: Die schnellsten drei Läufe
        // Gruppe 2: Restliche Läufe
        // Gruppe 3: Letzter Lauf (Vermeidung von leerem Lauf)
        int size1;
        int size2;
        int size3;
        if (amount <= 3) {
            size1 = schwimmer.size();
            size2 = 0;
            size3 = 0;
        } else if (amount == 4) {
            int rest = schwimmer.size() % bahnen;
            if (rest == 0) {
                rest = bahnen;
            }
            if (rest < 3) {
                int rest2 = bahnen;
                while (rest < rest2 - 1 && rest < 3) {
                    rest++;
                    rest2--;
                }
            }
            size1 = schwimmer.size() - rest;
            size2 = 0;
            size3 = rest;
        } else {
            size1 = 3 * bahnen;
            int rest = schwimmer.size() % bahnen;
            if (rest > 0 && rest < 3) {
                int rest2 = bahnen;
                while (rest < rest2 - 1 && rest < 3) {
                    rest++;
                    rest2--;
                }
            }
            size2 = schwimmer.size() - size1 - rest;
            size3 = rest;
        }

        int firstheats = Math.min(3, amount);
        int secondheats = amount - firstheats - (size3 > 0 ? 1 : 0);
        int pos = 0;
        ListIterator<T> li = schwimmer.listIterator();
        // ersten drei läufe nach ILS gleichmäßig befüllen
        for (int x = 0; x < size1; x++) {
            ll[pos].addSchwimmer(li.next(), disziplin);
            pos = (pos + 1) % firstheats;
        }

        // restliche läufe nach sortierung auffüllen
        pos = firstheats;
        for (int x = 0; x < size2; x++) {
            ll[pos].addSchwimmer(li.next(), disziplin);
            if (ll[pos].isFull()) {
                pos++;
            }
        }
        pos = firstheats + secondheats;
        // letzten lauf füllen
        for (int x = 0; x < size3; x++) {
            ll[pos].addSchwimmer(li.next(), disziplin);
        }

        for (int x = 0; x < ll.length / 2; x++) {
            Lauf<T> l1 = ll[x];
            Lauf<T> l2 = ll[ll.length - 1 - x];

            ll[x] = l2;
            ll[ll.length - 1 - x] = l1;
        }

        laufliste.addAll(Arrays.asList(ll));
    }

    private static <T extends ASchwimmer> void startgruppeVerteilenILSOutdoor(AWettkampf<T> wk,
            LinkedList<Lauf<T>> laufliste, int disziplin, int disziplinAnzahl, LinkedList<T> schwimmer, int vbahnen,
            boolean rotieren, boolean[] laneSelection) {
        if (schwimmer.isEmpty()) {
            return;
        }

        int bahnen = getBahnen(laneSelection, vbahnen);

        int amount = schwimmer.size() / bahnen + (schwimmer.size() % bahnen > 0 ? 1 : 0);

        @SuppressWarnings("unchecked")
        Lauf<T>[] ll = new Lauf[amount];
        for (int x = 0; x < ll.length; x++) {
            ll[x] = new Lauf<>(vbahnen, x, 0, laneSelection);
            ll[x].setLeftToRight(true);
        }

        // Verteilung wie folgt:
        // - Gleiche Gliederung auf unterschiedliche Läufe verteilen
        // - Alle Läufe gleichmäßig besetzen
        // - Zufällige Reihenfolge in den Läufen
        // - Immer in Bahn 1 anfangen

        LinkedList<String> gliederung = wk.getGliederungenMitQGliederung();
        Collections.shuffle(gliederung);

        Hashtable<String, Integer> gld2Index = new Hashtable<>();
        int pos = 0;
        for (String gld : gliederung) {
            gld2Index.put(gld, pos);
            pos++;
        }

        schwimmer.sort((o1, o2) -> {
            if (o1.getGliederungMitQGliederung().equals(o2.getGliederungMitQGliederung())) {
                return 0;
            }
            int i1 = gld2Index.get(o1.getGliederungMitQGliederung());
            int i2 = gld2Index.get(o2.getGliederungMitQGliederung());
            return i1 - i2;
        });

        @SuppressWarnings("unchecked")
        LinkedList<T>[] llx = new LinkedList[amount];
        for (int x = 0; x < llx.length; x++) {
            llx[x] = new LinkedList<>();
        }

        pos = 0;
        for (T t : schwimmer) {
            llx[pos].add(t);
            pos++;
            if (pos == llx.length) {
                pos = 0;
            }
        }

        for (int x = 0; x < ll.length; x++) {
            Collections.shuffle(llx[x]);
            for (T t : llx[x]) {
                ll[x].addSchwimmer(t, disziplin);
            }
        }

        laufliste.addAll(Arrays.asList(ll));
    }

    public void resetAll() {
        laufliste = null;
        verteilung = null;
        blocks = null;
        mode = Laufeinteilungsmodus.Auto.getValue();
    }

    private void reset() {
        if (laufliste != null) {
            laufliste.clear();
        } else {
            laufliste = new LinkedList<>();
        }
    }

    public synchronized LinkedList<Lauf<T>> getLaufliste() {
        return laufliste;
    }

    public synchronized void remove(T s) {
        if ((s == null) || (laufliste == null) || (laufliste.isEmpty())) {
            return;
        }
        if (wk.getSchwimmeranzahl() == 0) {
            laufliste = null;
            return;
        }

        for (Lauf<T> l : laufliste) {
            l.removeSchwimmer(s);
        }
    }

    public void set(T s, int disz, int laufindex, int bahn) {
        Lauf<T> heat = laufliste.get(laufindex);
        heat.setSchwimmer(s, disz, bahn);
    }

    public boolean hasMixedHeats() {
        return laufliste.stream()
                .anyMatch(lauf -> lauf.isMoreThanOneAgeGroup() || !lauf.isOnlyOneSex() || lauf.isStartgroup());
    }

    public synchronized void check(T s) {
        if ((s == null) || (laufliste == null) || (laufliste.isEmpty())) {
            return;
        }
        if (wk.getSchwimmeranzahl() == 0) {
            clear();
            return;
        }

        int disz = s.getAK().getDiszAnzahl();
        for (Lauf<T> l : laufliste) {
            for (int x = 0; x < l.getBahnen(); x++) {
                T temp = l.getSchwimmer(x);
                if ((temp != null) && (s.equals(temp))) {
                    int d = l.getDisznummer(x);
                    if ((disz <= d) || (!temp.isDisciplineChosen(d))) {
                        l.removeSchwimmer(x);
                    }
                }
            }
        }
    }

    public synchronized void check() {
        check(true);
    }

    public synchronized void check(boolean hasOther) {
        if ((laufliste == null) || (laufliste.isEmpty())) {
            return;
        }
        if (wk.getSchwimmeranzahl() == 0) {
            clear();
            return;
        }
        if (wk.isHeatBased() && hasOther) {
            clear();
            return;
        }

        for (Lauf<T> l : laufliste) {
            for (int x = 0; x < l.getBahnen(); x++) {
                T temp = l.getSchwimmer(x);
                if (temp != null) {
                    int d = l.getDisznummer(x);
                    if ((temp.getAK().getDiszAnzahl() <= d) || (!temp.isDisciplineChosen(d))) {
                        l.removeSchwimmer(x);
                    }
                }
            }
        }
    }

    public synchronized Lauf<T> add(int index) {
        if (index < 0) {
            return null;
        }
        if (laufliste == null) {
            laufliste = new LinkedList<>();
        }
        if (laufliste.size() < index) {
            index = laufliste.size();
        }

        Lauf<T> aktuell = null;
        int ln = 1;
        int lb = 0;
        if (!laufliste.isEmpty()) {
            if (index > 0) {
                aktuell = laufliste.get(index - 1);
            } else {
                aktuell = laufliste.get(index);
            }
            ln = aktuell.getLaufnummer();
            lb = aktuell.getLaufbuchstabe();
        }
        Lauf<T> neu = wk.getLaufliste().getNextFree(ln, lb);
        laufliste.add(index, neu);
        return neu;
    }

    public synchronized boolean isLaneUsed(int x) {
        return laufliste.stream().anyMatch(l -> l.getSchwimmer(x) != null);
    }

    public synchronized Lauf<T> suche(T schwimmer, int disziplin) {
        if (laufliste == null) {
            return null;
        }

        return laufliste.stream().filter(result -> result.includes(schwimmer, disziplin)).findFirst().orElse(null);

    }

    public synchronized int sucheIndex(T schwimmer, int disziplin) {
        if (laufliste == null) {
            return -1;
        }

        int index = 0;
        for (Lauf<T> result : laufliste) {
            if (result.includes(schwimmer, disziplin)) {
                return index;
            }
            index++;
        }

        return -1;
    }

    public synchronized void remove(int index) {
        if ((laufliste == null) || (index < 0) || (laufliste.size() <= index)) {
            return;
        }

        Lauf<T> l = laufliste.get(index);
        for (int x = 0; x < l.getBahnen(); x++) {
            if (l.getSchwimmer(x) != null) {
                l.removeSchwimmer(x);
            }
        }

        laufliste.remove(index);
    }

    public synchronized void clear() {
        laufliste = null;
    }

    public synchronized boolean exists(Lauf<T> l) {
        if (laufliste == null) {
            return false;
        }
        if (laufliste.isEmpty()) {
            return false;
        }

        ListIterator<Lauf<T>> li = laufliste.listIterator();
        do {
            Lauf<T> lauf = li.next();
            if (lauf.equals(l)) {
                return true;
            }
        } while (li.hasNext());

        return false;
    }

    private synchronized Lauf<T> getNextFree(int laufnummer, int laufbuchstabe) {
        int b = wk.getIntegerProperty(HEATS_LANES);

        // Kleiner Pre-Test fuer schoenere Ergebnisse
        Lauf<T> neu = new Lauf<>(b, laufnummer, laufbuchstabe);
        if (!exists(neu)) {
            return neu;
        }
        neu = new Lauf<>(b, laufnummer + 1, 0);
        if (!exists(neu)) {
            return neu;
        }

        // Jetzt wird gruendlich getestet
        laufbuchstabe--;
        do {
            laufbuchstabe++;
            if (laufbuchstabe >= 26) {
                laufbuchstabe = 0;
                laufnummer++;
            }
            neu = new Lauf<>(b, laufnummer, laufbuchstabe);
        } while (exists(neu));

        return neu;
    }

    public synchronized void removeEmptyHeats() {
        if (laufliste == null) {
            return;
        }
        laufliste.removeIf(Lauf::isEmpty);
    }

    public synchronized boolean neueNummerierung() {
        return neueNummerierung(0, wk.getIntegerProperty(HEATS_FIRST_HEAT));
    }

    public synchronized boolean neueNummerierung(int index, int min) {
        if (laufliste == null) {
            return false;
        }
        if (laufliste.isEmpty()) {
            return false;
        }

        boolean changed = false;

        ListIterator<Lauf<T>> li = laufliste.listIterator();
        int nummer = min;
        while (li.hasNext()) {
            Lauf<T> l = li.next();
            if (index > 0) {
                if (l.getLaufnummer() >= min) {
                    return false;
                }
                index--;
                continue;
            }
            if ((nummer != l.getLaufnummer()) || l.getLaufbuchstabe() != 0) {
                l.setLaufnummer(nummer);
                l.setLaufbuchstabe(0);
                changed = true;
            }
            nummer++;
        }
        return changed;
    }

    public void reorderAgegroupsByStartgroups(int[][] reihenfolge) {
        if (laufliste == null) {
            return;
        }

        Startgruppe[] startgruppen = wk.getRegelwerk().getEffektiveStartgruppen();
        int[] sgindizes = new int[wk.getRegelwerk().size()];
        for (int x = 0; x < startgruppen.length; x++) {
            LinkedList<Altersklasse> aks = wk.getRegelwerk().getAKsForStartgroup(startgruppen[x]);
            for (int y = 0; y < wk.getRegelwerk().size(); y++) {
                if (aks.contains(wk.getRegelwerk().getAk(y))) {
                    sgindizes[y] = x;
                }
            }
        }

        for (Lauf<T> lauf : laufliste) {
            for (int x = 0; x < lauf.getBahnen(); x++) {
                if (lauf.getSchwimmer(x) != null) {
                    T t = lauf.getSchwimmer(x);
                    int disz = lauf.getDisznummer(x);
                    int index = sgindizes[t.getAKNummer()];
                    lauf.updateDisziplin(x, reihenfolge[index][disz]);
                }
            }
        }
    }

    private static final class MeldezeitenComparator implements Comparator<ASchwimmer>, Serializable {

        private final int disziplin;

        public MeldezeitenComparator(int disz) {
            disziplin = disz;
        }

        @Override
        public int compare(ASchwimmer o1, ASchwimmer o2) {
            long m1 = o1.getMeldezeit(disziplin);
            long m2 = o2.getMeldezeit(disziplin);
            if ((m1 == 0) && (m2 == 0)) {
                return 0;
            }
            if (m1 == 0) {
                return (int) m2;
            }
            if (m2 == 0) {
                return (int) -m1;
            }
            return (int) (m1 - m2);
        }
    }

    public boolean isEmpty() {
        return (laufliste == null) || laufliste.isEmpty();
    }

    public int getLastMode() {
        return mode;
    }

    public String[] getHeatNames() {
        if ((laufliste == null) || laufliste.isEmpty()) {
            return new String[0];
        }
        String[] result = new String[laufliste.size()];
        int x = 0;
        for (Lauf<T> l : laufliste) {
            result[x] = l.getName();
            x++;
        }
        return result;
    }

    public static void migrator3(Element node) {
        for (Element einteilung : node.element("verteilung2").elements()) {
            for (Element entry : einteilung.elements().toArray(Element[]::new)) {
                if (entry.getName().equals("first")) {
                    entry.setName("startgroup");
                } else if (entry.getName().equals("second")) {
                    entry.setName("male");
                } else if (entry.getName().equals("third")) {
                    entry.setName("discipline");
                }
            }
        }
    }
}