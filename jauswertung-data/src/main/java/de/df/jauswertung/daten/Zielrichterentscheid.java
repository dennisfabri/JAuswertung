package de.df.jauswertung.daten;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.util.SearchUtils;

public class Zielrichterentscheid<T extends ASchwimmer> implements Serializable {

    private static final long serialVersionUID = -3488380754349447007L;

    @XStreamAsAttribute
    private int disziplin = 0;
    private LinkedList<T> swimmers = new LinkedList<T>();

    public Zielrichterentscheid() {
        // Nothing to do
    }

    public Zielrichterentscheid(Collection<T> s) {
        addSchwimmer(s);
    }

    public void setDisziplin(int disz) {
        if (disziplin == disz) {
            return;
        }
        if (disz < 0) {
            throw new IllegalArgumentException(disz + " must be at least 0.");
        }
        for (T t : swimmers) {
            if (t.getAK().getDiszAnzahl() <= disz) {
                throw new IllegalArgumentException(disz + " must be lower than " + t.getAK().getDiszAnzahl() + ".");
            }
        }
        disziplin = disz;
    }

    public void addSchwimmer(T s) {
        if (swimmers.contains(s)) {
            return;
        }
        if (s.getAK().getDiszAnzahl() <= disziplin) {
            if (s.getAK().getDiszAnzahl() <= disziplin) {
                throw new IllegalArgumentException(
                        disziplin + " must be lower than " + s.getAK().getDiszAnzahl() + ".");
            }
        }
        swimmers.addLast(s);
    }

    public void addSchwimmer(Collection<T> s) {
        for (T t : s) {
            addSchwimmer(t);
        }
    }

    public void setSchwimmer(Collection<T> s) {
        swimmers.clear();
        addSchwimmer(s);
    }

    public LinkedList<T> getSchwimmer() {
        check();
        return new LinkedList<T>(swimmers);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public String toString() {
        String heat = "";
        if (!swimmers.isEmpty()) {
            AWettkampf wk = swimmers.getFirst().getWettkampf();
            Laufliste<T> laufliste = wk.getLaufliste();
            if ((laufliste != null) && (!laufliste.getLaufliste().isEmpty())) {
                LinkedList<Lauf<T>> ll = laufliste.getLaufliste();
                ListIterator<Lauf<T>> li = ll.listIterator();
                while (li.hasNext()) {
                    Lauf<T> l = li.next();
                    for (int x = 0; x < l.getBahnen(); x++) {
                        if ((l.getSchwimmer(x) != null) && (l.getDisznummer(x) == disziplin)) {
                            T t = l.getSchwimmer(x);
                            if (swimmers.contains(t)) {
                                String h = l.toString();
                                if (heat.length() == 0) {
                                    heat = h;
                                } else if (!heat.equals(h)) {
                                    return heat + ", ...";
                                }
                            }
                        }
                    }
                }
            }
        }
        return heat;
    }

    public boolean check() {
        boolean ok = true;
        ListIterator<T> li = swimmers.listIterator();
        while (li.hasNext()) {
            T t = li.next();
            if (SearchUtils.getSchwimmer(t.getWettkampf(), t) != t) {
                li.remove();
                ok = false;
            } else if (t.getAK().getDiszAnzahl() <= disziplin) {
                li.remove();
                ok = false;
            } else if (!t.isDisciplineChosen(disziplin)) {
                li.remove();
                ok = false;
            }
        }
        return ok;
    }

    public boolean isValid() {
        check();
        if (swimmers.isEmpty()) {
            return false;
        }
        if (swimmers.size() < 2) {
            return false;
        }

        ListIterator<T> li = swimmers.listIterator();
        int zeit = li.next().getZeit(disziplin);
        if (zeit == 0) {
            return false;
        }
        while (li.hasNext()) {
            T t = li.next();
            if (!t.isDisciplineChosen(disziplin)) {
                return false;
            }
            if (zeit != t.getZeit(disziplin)) {
                return false;
            }
        }

        AWettkampf<T> wk = swimmers.getFirst().getWettkampf();
        Laufliste<T> laufliste = wk.getLaufliste();
        if ((laufliste != null) && (laufliste.getLaufliste() != null) && (!laufliste.getLaufliste().isEmpty())) {
            LinkedList<Lauf<T>> ll = laufliste.getLaufliste();
            ListIterator<Lauf<T>> lix = ll.listIterator();

            Lauf<T> lauf = null;

            while (lix.hasNext() && (lauf == null)) {
                Lauf<T> l = lix.next();
                for (int x = 0; x < l.getBahnen(); x++) {
                    if ((l.getSchwimmer(x) != null) && (l.getDisznummer(x) == disziplin)) {
                        T t = l.getSchwimmer(x);
                        if (t.equals(swimmers.getFirst())) {
                            lauf = l;
                            break;
                        }
                    }
                }
            }

            if (lauf == null) {
                return false;
            }
            int counter = 0;
            for (int x = 0; x < lauf.getBahnen(); x++) {
                if ((lauf.getSchwimmer(x) != null) && (lauf.getDisznummer(x) == disziplin)) {
                    if (swimmers.contains(lauf.getSchwimmer(x))) {
                        counter++;
                    }
                }
            }
            if (counter != swimmers.size()) {
                return false;
            }
        }

        return true;
    }

    public int getZeit() {
        if (swimmers.isEmpty()) {
            return -1;
        }
        int zeit = swimmers.getFirst().getZeit(disziplin);
        for (ASchwimmer t : swimmers) {
            if (zeit != t.getZeit(disziplin)) {
                return -1;
            }
        }
        return zeit;
    }

    public int getDisziplin() {
        return disziplin;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Zielrichterentscheid)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Zielrichterentscheid<T> ze = (Zielrichterentscheid<T>) obj;
        if (disziplin != ze.disziplin) {
            return false;
        }
        if (swimmers.size() != ze.swimmers.size()) {
            return false;
        }
        int counter = 0;
        for (T s : swimmers) {
            for (T t : ze.swimmers) {
                if (t.equals(s)) {
                    counter++;
                }
            }
        }
        if (counter != swimmers.size()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return swimmers.size();
    }
}