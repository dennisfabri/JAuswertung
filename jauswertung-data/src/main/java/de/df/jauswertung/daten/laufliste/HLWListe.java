package de.df.jauswertung.daten.laufliste;

import static de.df.jauswertung.daten.PropertyConstants.ZW_DURATION;
import static de.df.jauswertung.daten.PropertyConstants.ZW_EMPTY_LIST;
import static de.df.jauswertung.daten.PropertyConstants.ZW_IGNORE_AK_SWIMMERS;
import static de.df.jauswertung.daten.PropertyConstants.ZW_LANES;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE_AGEGROUPS;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE_DURATION;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE_MODE;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE_RESTARTS;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE_START;
import static de.df.jauswertung.daten.PropertyConstants.ZW_RESPECT_QUALIFICATIONS;
import static de.df.jauswertung.daten.PropertyConstants.ZW_SORTING_ORDER;
import static de.df.jauswertung.daten.PropertyConstants.ZW_STARTTIME;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import org.dom4j.Element;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.event.PropertyChangeListener;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.util.RandomUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.data.ListUtils;

public class HLWListe<T extends ASchwimmer> implements Serializable {

    @Serial
    private static final long serialVersionUID = -7412435828304184620L;

    public static final int REIHENFOLGE_ZUFALL = 0;
    public static final int REIHENFOLGE_EINZELN = 1;
    public static final int REIHENFOLGE_MELDUNG = 2;
    public static final int REIHENFOLGE_GEGEN = 3;
    public static final int REIHENFOLGE_ZUSAMMEN = 4;

    public static final int PAUSE_MODE_TIME = 0;
    public static final int PAUSE_MODE_AGEGROUP = 1;

    // 23:59 = 1439 minutes
    public static final int TIME_MAX = 1439;
    public static final int TIME_TAG = TIME_MAX + 1;

    final AWettkampf<T> wk;

    LinkedList<LinkedList<HLWLauf<T>>> hlwliste;
    LinkedList<Time> startat;
    private Einteilung[] verteilung;

    private static Random random = RandomUtils.getRandomNumberGenerator();
    private static long seed = random.nextLong();

    /** Creates new Laufliste */
    public HLWListe(AWettkampf<T> wettkampf) {
        wk = wettkampf;
        hlwliste = null;
        startat = null;

        PropertyChangeListener pcl = new WettkampfChangeListener();
        wettkampf.addPropertyChangeListener(pcl);
    }

    private static <T extends ASchwimmer> LinkedList<T> vorsortierenVerteilt(AWettkampf<T> wk, LinkedList<T> ll) {
        if ((ll == null) || ll.isEmpty()) {
            return null;
        }

        int size = ll.size();
        int bahnen = wk.getIntegerProperty(ZW_LANES);

        int ganzahl = wk.getGliederungen().size();

        @SuppressWarnings("unchecked")
        LinkedList<T>[] listen = new LinkedList[ganzahl];
        for (int x = 0; x < ganzahl; x++) {
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
        @SuppressWarnings("unchecked")
        LinkedList<T>[] listentemp = new LinkedList[amount];
        System.arraycopy(listen, 0, listentemp, 0, amount);
        listen = listentemp;
        Arrays.sort(listen, Comparator.comparingInt(LinkedList::size));

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
        @SuppressWarnings("unchecked")
        T[][] laeufe = (T[][]) new ASchwimmer[anzahl][bahnen];
        int pos = 0;
        int bahn = 0;
        for (LinkedList<T> aListen : listen) {
            ListIterator<T> li = aListen.listIterator();
            while (li.hasNext()) {
                laeufe[pos][bahn] = li.next();
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
        return ll;
    }

    private static <T extends ASchwimmer> LinkedList<T> vorsortieren(AWettkampf<T> wk, LinkedList<T> ll) {
        if (ll == null) {
            return new LinkedList<>();
        }
        if (ll.isEmpty()) {
            return ll;
        }

        Collections.shuffle(ll, new Random(seed));
        switch (wk.getIntegerProperty(ZW_SORTING_ORDER)) {
        case REIHENFOLGE_EINZELN:
            ll = vorsortierenVerteilt(wk, ll);
            break;
        case REIHENFOLGE_GEGEN:
            Comparator<ASchwimmer> comparator1 = new Comparator<ASchwimmer>() {
                @Override
                public int compare(ASchwimmer o1, ASchwimmer o2) {
                    return o1.getGliederung().compareTo(o2.getGliederung());
                }
            };
            ll.sort(comparator1);
            break;
        case REIHENFOLGE_MELDUNG:
            Comparator<ASchwimmer> comparator2 = new Meldesorter(
                    wk.getIntegerProperty(PropertyConstants.ZW_REGISTERED_POINTS_INDEX, 0), false);
            ll.sort(comparator2);
            break;
        case REIHENFOLGE_ZUSAMMEN:
        case REIHENFOLGE_ZUFALL:
        default:
            break;
        }
        return ll;
    }

    private static <T extends ASchwimmer> LinkedList<T> nachsortieren(AWettkampf<T> wk, LinkedList<T> ll) {
        if (ll == null) {
            return new LinkedList<>();
        }
        if (ll.isEmpty()) {
            return ll;
        }

        switch (wk.getIntegerProperty(ZW_SORTING_ORDER)) {
        case REIHENFOLGE_EINZELN:
            break;
        case REIHENFOLGE_GEGEN:
            break;
        case REIHENFOLGE_MELDUNG:
            break;
        case REIHENFOLGE_ZUSAMMEN:
            ll.sort(Comparator.<ASchwimmer, String>comparing(ASchwimmer::getGliederung));
            break;
        case REIHENFOLGE_ZUFALL:
            break;
        default:
        }
        return ll;
    }

    public synchronized void erzeugen() {
        erzeugen(verteilung);
    }

    public synchronized void erzeugen(Einteilung[] aufteilung) {
        reset();

        if (wk.getBooleanProperty(ZW_EMPTY_LIST)) {
            add(0, 0);
            return;
        }

        seed = random.nextLong();

        if (aufteilung == null) {
            aufteilung = getStandardVerteilung();
            verteilung = null;
        } else {
            if (isStandardVerteilung(aufteilung)) {
                verteilung = null;
            } else {
                aufteilung = getVerteilung(aufteilung);
                verteilung = aufteilung;
            }
        }

        int puppen = wk.getIntegerProperty(ZW_LANES);

        LinkedList<HLWLauf<T>> current = hlwliste.getFirst();
        Time currentT = new Time(wk.getDoubleProperty(ZW_STARTTIME));

        hlwliste.clear();
        startat.clear();

        boolean ignoreak = wk.getBooleanProperty(ZW_IGNORE_AK_SWIMMERS);

        int[][] restarttimes;
        boolean[][] pauses;

        pauses = checkPauses(wk.getProperty(ZW_PAUSE_AGEGROUPS));
        restarttimes = checkTimes(wk.getProperty(ZW_PAUSE_RESTARTS));

        boolean respectQualifications = wk.getBooleanProperty(ZW_RESPECT_QUALIFICATIONS) && !wk.HasOpenQualifications();

        // Schwimmer nach Altersklassen trennen und anschließend sortieren,
        // um sie besser verarbeiten zu können
        LinkedList<T> schwimmer = new LinkedList<>();
        for (Einteilung anAufteilung : aufteilung) {
            Altersklasse ak = wk.getRegelwerk().getAk(anAufteilung.getAK());
            boolean male = anAufteilung.isMaennlich();
            if (ak.hasHLW()) {
                LinkedList<T> w = SearchUtils.getSchwimmer(wk, ak, male);
                if (ignoreak) {
                    SearchUtils.filterAusserKonkurrenz(w);
                }
                if (respectQualifications) {
                    w.removeIf(t -> !t.getQualifikation().isAccepted());
                }

                w = vorsortieren(wk, w);

                for (T s : w) {
                    for (int y = 0; y < s.getMaximaleHLW(); y++) {
                        schwimmer.addLast(s);
                    }
                }

                if (wk.getBooleanProperty(ZW_PAUSE) && (wk.getIntegerProperty(ZW_PAUSE_MODE) == PAUSE_MODE_AGEGROUP)) {
                    if (pauses[anAufteilung.getAK()][anAufteilung.isMaennlich() ? 1 : 0]) {
                        schwimmer = nachsortieren(wk, schwimmer);
                        verteilen(current, schwimmer, puppen);
                        schwimmer.clear();

                        hlwliste.addLast(current);
                        startat.addLast(currentT);

                        current = new LinkedList<>();
                        currentT = new Time(restarttimes[anAufteilung.getAK()][anAufteilung.isMaennlich() ? 1 : 0]);
                    }
                }
            }
        }

        if (!schwimmer.isEmpty()) {
            schwimmer = nachsortieren(wk, schwimmer);
            verteilen(current, schwimmer, puppen);
        }
        if (!current.isEmpty()) {
            hlwliste.addLast(current);
            startat.addLast(currentT);
        }

        try {
            if (wk.getBooleanProperty(ZW_PAUSE) && (wk.getIntegerProperty(ZW_PAUSE_MODE) == PAUSE_MODE_TIME)) {
                double duration = wk.getDoubleProperty(ZW_DURATION);
                double start = wk.getDoubleProperty(ZW_PAUSE_START);
                if (start <= currentT.getTime()) {
                    start += 24 * 60;
                }

                double time = (currentT.getTime() + current.size() * duration);

                hlwliste.clear();
                startat.clear();

                while (time > start) {
                    LinkedList<HLWLauf<T>> neu = new LinkedList<>();
                    Time neuT = new Time(start + wk.getIntegerProperty(ZW_PAUSE_DURATION));

                    while (time > start) {
                        HLWLauf<T> temp = current.getLast();
                        current.removeLast();
                        neu.addFirst(temp);

                        time -= duration;
                    }

                    hlwliste.addLast(current);
                    startat.addLast(currentT);

                    current = neu;
                    currentT = neuT;

                    time = (currentT.getTime() + current.size() * duration);

                    start = wk.getDoubleProperty(ZW_PAUSE_START);
                    if (start <= currentT.getTime()) {
                        start += 24 * 60;
                    }

                }

                if (!current.isEmpty()) {
                    hlwliste.addLast(current);
                    startat.addLast(currentT);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        refreshTime();
    }

    public static int[][] checkTimes(Object tmptimes) {
        int[][] restarttimes = null;
        if (tmptimes != null) {
            if (tmptimes instanceof int[][]) {
                restarttimes = (int[][]) tmptimes;
            } else if (tmptimes instanceof int[] tmp) {
                restarttimes = new int[tmp.length][2];
                for (int x = 0; x < tmp.length; x++) {
                    restarttimes[x][0] = tmp[x];
                    restarttimes[x][1] = tmp[x];
                }
            }
        }
        if (restarttimes == null) {
            restarttimes = new int[0][2];
        }
        return restarttimes;
    }

    public static boolean[][] checkPauses(Object tmpPauses) {
        boolean[][] pauses = null;
        if (tmpPauses != null) {
            if (tmpPauses instanceof boolean[][]) {
                pauses = (boolean[][]) tmpPauses;
            } else if (tmpPauses instanceof boolean[] tmp) {
                pauses = new boolean[tmp.length][2];
                for (int x = 0; x < tmp.length; x++) {
                    pauses[x][0] = tmp[x];
                    pauses[x][1] = false;
                }
            }
        }
        if (pauses == null) {
            pauses = new boolean[0][2];
        }
        return pauses;
    }

    public static <T extends ASchwimmer> void verteilen(LinkedList<HLWLauf<T>> hlwliste, LinkedList<T> schwimmer,
            int puppen) {
        if (schwimmer.isEmpty()) {
            return;
        }
        LinkedList<HLWLauf<T>> ll = new LinkedList<>();
        HLWLauf<T> lauf = new HLWLauf<>(puppen);
        ll.addFirst(lauf);
        for (T t : schwimmer) {
            if (lauf.isFull()) {
                lauf = new HLWLauf<>(puppen);
                ll.addLast(lauf);
            }
            lauf.addSchwimmer(t, Lauf.HLW);
        }

        hlwliste.addAll(ll);
    }

    public synchronized void remove(T s) {
        if (s == null) {
            return;
        }
        if ((hlwliste == null) || (hlwliste.isEmpty())) {
            return;
        }
        if (wk.getSchwimmeranzahl() == 0) {
            hlwliste = null;
            return;
        }

        for (LinkedList<HLWLauf<T>> hlwLaufs : hlwliste) {
            for (HLWLauf<T> l : hlwLaufs) {
                int zahl = l.getSchwimmer(s);
                while (zahl > -1) {
                    l.removeSchwimmer(zahl);
                    zahl = l.getSchwimmer(s);
                }
            }
        }
    }

    public HLWLauf<T> get(int[] index) {
        return hlwliste.get(index[0]).get(index[1]);
    }

    public boolean remove(int[] index) {
        boolean b = hlwliste.get(index[0]).remove(index[1]) != null;
        refreshTime();
        return b;
    }

    public synchronized boolean remove(int[] index, int bahn) {
        HLWLauf<T> temp = get(index);

        T s = temp.getSchwimmer(bahn);
        if (s != null) {
            return temp.removeSchwimmer(bahn);
        }
        return false;
    }

    public synchronized boolean set(T s, int[] index, int bahn) {
        if ((s == null) || (hlwliste == null)) {
            return false;
        }

        HLWLauf<T> temp = get(index);
        if (temp == null) {
            return false;
        }
        if ((bahn < 0) || (bahn > temp.getBahnen())) {
            return false;
        }

        return temp.setSchwimmer(s, -1, bahn);
    }

    public ListIterator<LinkedList<HLWLauf<T>>> getIterator() {
        return hlwliste.listIterator();
    }

    public LinkedList<HLWLauf<T>> getMergedHeats() {
        return hlwliste.stream().flatMap(Collection::stream).collect(Collectors.toCollection(LinkedList::new));
    }

    public synchronized void check(T s) {
        if (hlwliste == null) {
            return;
        }
        if (s == null) {
            return;
        }
        if (!wk.hasHLW()) {
            reset();
            return;
        }
        if (s.getAK().hasHLW()) {
            return;
        }
        remove(s);
    }

    public synchronized void check() {
        if (hlwliste == null) {
            return;
        }
        if (!wk.hasHLW()) {
            reset();
            return;
        }

        for (LinkedList<HLWLauf<T>> hlwLaufs : hlwliste) {
            for (Lauf<T> l : hlwLaufs) {
                for (int x = 0; x < l.getBahnen(); x++) {
                    T temp = l.getSchwimmer(x);
                    if (temp != null) {
                        if (!temp.getAK().hasHLW()) {
                            l.removeSchwimmer(x);
                        }
                    }
                }
            }
        }
    }

    public void add(int[] index) {
        if (index.length != 2) {
            throw new IllegalArgumentException("Length of index-array must be 2 and was " + index.length + ".");
        }
        add(index[0], index[1]);
    }

    public synchronized void add(int list, int index) {
        if (hlwliste == null) {
            return;
        }
        if (index < 0) {
            return;
        }
        if (hlwliste.size() < list) {
            list = hlwliste.size();
        }
        HLWLauf<T> neu = new HLWLauf<>(wk.getIntegerProperty(ZW_LANES));
        hlwliste.get(list).add(index, neu);
        refreshTime();
    }

    public void merge(int list1, int list2) {
        LinkedList<HLWLauf<T>> l1 = getLaufliste(list1);
        l1.addAll(getLaufliste(list2));
        hlwliste.remove(list2);
        refreshTime();
    }

    public void split(int[] index, Time start) {
        if (index[1] < 0) {
            LinkedList<HLWLauf<T>> liste = new LinkedList<>();
            liste.add(new HLWLauf<>(wk.getIntegerProperty(PropertyConstants.ZW_LANES)));
            hlwliste.add(index[0], liste);
            startat.add(index[0], start);
        } else {
            LinkedList<HLWLauf<T>>[] listen = ListUtils.split(hlwliste.get(index[0]), index[1]);
            for (int x = 0; x < 2; x++) {
                if (listen[x].isEmpty()) {
                    listen[x].add(new HLWLauf<>(wk.getIntegerProperty(PropertyConstants.ZW_LANES)));
                }
            }
            hlwliste.remove(index[0]);
            hlwliste.add(index[0], listen[1]);
            hlwliste.add(index[0], listen[0]);
            startat.add(index[0] + 1, start);
        }

        refreshTime();
    }

    public void resetAll() {
        hlwliste = null;
        startat = null;
        verteilung = null;
    }

    public synchronized void reset() {
        if (hlwliste == null) {
            hlwliste = new LinkedList<>();
            startat = new LinkedList<>();
        } else {
            hlwliste.clear();
            startat.clear();
        }

        hlwliste.add(new LinkedList<>());
        startat.add(new Time(wk.getDoubleProperty(PropertyConstants.ZW_STARTTIME)));
    }

    public int getLauflistenCount() {
        if (hlwliste == null) {
            return 0;
        }
        return hlwliste.size();
    }

    public LinkedList<HLWLauf<T>> getLaufliste(int index) {
        return hlwliste.get(index);
    }

    public Time getStarttime(int index) {
        return startat.get(index);
    }

    public void setStarttime(int index, Time t) {
        startat.set(index, t);
        refreshTime();
    }

    private Time getUhrzeit(Time start, int lauf) {
        if (lauf < 0) {
            lauf = 0;
        }

        double zeit = start.getTime() + (wk.getDoubleProperty(ZW_DURATION) * lauf);
        while (zeit >= TIME_TAG) {
            zeit -= TIME_TAG;
        }

        return new Time(zeit);
    }

    public synchronized void refreshTime() {
        if (hlwliste == null) {
            return;
        }

        ListIterator<LinkedList<HLWLauf<T>>> lli = hlwliste.listIterator();
        ListIterator<Time> sli = startat.listIterator();

        while (lli.hasNext()) {
            ListIterator<HLWLauf<T>> li = lli.next().listIterator();
            Time start = sli.next();
            int lauf = 0;
            while (li.hasNext()) {
                HLWLauf<T> hlw = li.next();
                hlw.setName(getUhrzeit(start, lauf));
                lauf++;
            }
        }
    }

    public Einteilung[] getStandardVerteilung() {
        LinkedList<Einteilung> daten = new LinkedList<>();
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            Altersklasse ak = wk.getRegelwerk().getAk(x);
            if (ak.hasHLW() && SearchUtils.hasSchwimmer(wk, ak)) {
                daten.addLast(new Einteilung(x, false));
                daten.addLast(new Einteilung(x, true));
            }
        }
        return daten.toArray(Einteilung[]::new);
    }

    public Einteilung[] getVerteilung() {
        return getVerteilung(verteilung);
    }

    public Einteilung[] getVerteilung(Einteilung[] aufteilung) {
        if (aufteilung == null) {
            return getStandardVerteilung();
        }
        Einteilung[] daten = getStandardVerteilung();

        // Add configures entries
        LinkedList<Einteilung> result = new LinkedList<>();
        for (Einteilung anAufteilung : aufteilung) {
            boolean found = false;
            for (Einteilung einteilung : daten) {
                if (einteilung.equals(anAufteilung)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                result.addLast(anAufteilung);
            }
        }

        // Add missing entries
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
        return result.toArray(Einteilung[]::new);
    }

    public boolean hasStandardVerteilung() {
        return isStandardVerteilung(getVerteilung());
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

    public boolean isEmpty() {
        return (hlwliste == null) || (hlwliste.isEmpty()) || (hlwliste.getFirst().isEmpty());
    }

    public final class WettkampfChangeListener implements PropertyChangeListener {
        @Serial
        private static final long serialVersionUID = -2220683491274451995L;

        @Override
        public void propertyChanged(Object source, String property) {
            if (ZW_LANES.equals(property)) {
                int b = wk.getIntegerProperty(ZW_LANES);
                if (hlwliste != null) {
                    for (LinkedList<HLWLauf<T>> hlwLaufs : hlwliste) {
                        for (HLWLauf<T> hlwLauf : hlwLaufs) {
                            hlwLauf.setBahnen(b);
                        }
                    }
                }
            }
        }
    }

    private static final class Meldesorter implements Comparator<ASchwimmer> {

        private final int index;
        private final int prefix;

        public Meldesorter(int index, boolean ascending) {
            this.index = index;
            prefix = (ascending ? 1 : -1);
        }

        @Override
        public int compare(ASchwimmer o1, ASchwimmer o2) {
            return (int) (prefix * ((o2.getMeldepunkte(index) - o1.getMeldepunkte(index)) * 100));
        }
    }

    public static class Einteilung implements Serializable {

        private final int ak;
        private final boolean male;

        public Einteilung() {
            this(0, false);
        }

        public Einteilung(int ak, boolean male) {
            this.ak = ak;
            this.male = male;
        }

        public int getAK() {
            return ak;
        }

        public boolean isMaennlich() {
            return male;
        }
    }

    public static void migrator3(Element node) {
        for (Element einteilung : node.element("verteilung").elements()) {
            for (Element entry : einteilung.elements().toArray(Element[]::new)) {
                if (entry.getName().equals("first")) {
                    entry.setName("ak");
                } else if (entry.getName().equals("second")) {
                    entry.setName("male");
                }
            }
        }
    }
}