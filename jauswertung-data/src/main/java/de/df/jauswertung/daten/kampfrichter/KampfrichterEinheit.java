/*
 * Created on 23.02.2006
 */
package de.df.jauswertung.daten.kampfrichter;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

public class KampfrichterEinheit implements Serializable {

    private static final long serialVersionUID = -5183109430325434863L;

    private String name = "";
    private LinkedList<KampfrichterPosition> positionen = new LinkedList<KampfrichterPosition>();
    private Hashtable<String, LinkedList<Kampfrichter>> kampfrichter = new Hashtable<String, LinkedList<Kampfrichter>>();

    public KampfrichterEinheit(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }

    public boolean isEmpty() {
        return positionen.isEmpty();
    }

    public String[] getPositionen() {
        String[] pos = new String[positionen.size()];
        KampfrichterPosition[] kpos = positionen.toArray(new KampfrichterPosition[positionen.size()]);
        for (int x = 0; x < pos.length; x++) {
            pos[x] = kpos[x].getPosition();
        }
        return pos;
    }

    public KampfrichterStufe[] getMinimaleStufen() {
        KampfrichterStufe[] pos = new KampfrichterStufe[positionen.size()];
        KampfrichterPosition[] kpos = positionen.toArray(new KampfrichterPosition[positionen.size()]);
        for (int x = 0; x < pos.length; x++) {
            pos[x] = kpos[x].getMinimaleStufe();
        }
        return pos;
    }

    public KampfrichterStufe getMinimaleStufe(String pos) {
        KampfrichterPosition[] kpos = positionen.toArray(new KampfrichterPosition[positionen.size()]);
        for (KampfrichterPosition kpo : kpos) {
            if (kpo.getPosition().equalsIgnoreCase(pos)) {
                return kpo.getMinimaleStufe();
            }
        }
        return null;
    }

    public void setKampfrichter(String position, LinkedList<Kampfrichter> kr) {
        if (position == null) {
            throw new NullPointerException();
        }
        if (kr == null) {
            throw new NullPointerException();
        }
        if (!hasPosition(position)) {
            throw new IllegalArgumentException("Position \"" + position + "\" not found.");
        }
        kampfrichter.put(position, kr);
    }

    public boolean hasPosition(String position) {
        if (position == null) {
            throw new NullPointerException();
        }
        ListIterator<KampfrichterPosition> li = positionen.listIterator();
        while (li.hasNext()) {
            KampfrichterPosition kp = li.next();
            if (kp.getPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }

    public KampfrichterPosition getPosition(String position) {
        if (position == null) {
            throw new NullPointerException();
        }
        ListIterator<KampfrichterPosition> li = positionen.listIterator();
        while (li.hasNext()) {
            KampfrichterPosition kp = li.next();
            if (kp.getPosition().equals(position)) {
                return kp;
            }
        }
        return null;
    }

    public void addKampfrichter(String position) {
        addKampfrichter(position, new Kampfrichter());
    }

    public void renamePosition(KampfrichterPosition pos, String neuername) {
        pos = getPosition(pos.getPosition());
        LinkedList<Kampfrichter> karis = kampfrichter.get(pos.getPosition());
        if (karis != null) {
            kampfrichter.remove(pos.getPosition());
            kampfrichter.put(neuername, karis);
        }
        pos.setPosition(neuername);
    }

    public void addKampfrichter(String position, Kampfrichter kr) {
        if (position == null) {
            throw new NullPointerException();
        }
        if (kr == null) {
            throw new NullPointerException();
        }
        if (!hasPosition(position)) {
            throw new IllegalArgumentException("Position \"" + position + "\" not found.");
        }
        LinkedList<Kampfrichter> karis = kampfrichter.get(position);
        if (karis == null) {
            karis = new LinkedList<Kampfrichter>();
            kampfrichter.put(position, karis);
        }
        karis.addLast(kr);
    }

    public Kampfrichter[] getKampfrichter(String position) {
        if (position == null) {
            throw new NullPointerException();
        }
        try {
            return kampfrichter.get(position).toArray(new Kampfrichter[kampfrichter.get(position).size()]);
        } catch (NullPointerException npe) {
            return new Kampfrichter[0];
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        if (n == null) {
            throw new NullPointerException();
        }
        name = n;
    }

    public void addPosition(String position) {
        addPosition(position, KampfrichterStufe.KEINE, 1);
    }

    public void addPosition(String position, KampfrichterStufe stufe) {
        addPosition(position, stufe, 1);
    }

    public void addPosition(String position, KampfrichterStufe stufe, int anzahl) {
        if (position == null) {
            throw new NullPointerException();
        }
        if (stufe == null) {
            throw new NullPointerException();
        }
        KampfrichterPosition kp = new KampfrichterPosition(position, stufe);
        if (positionen.contains(kp)) {
            throw new IllegalArgumentException("\"" + kp + "\" already exists!");
        }
        positionen.addLast(kp);
        for (int x = 0; x < anzahl; x++) {
            addKampfrichter(position);
        }
    }

    public void removePosition(@SuppressWarnings("hiding") String name) {
        ListIterator<KampfrichterPosition> li = positionen.listIterator();
        while (li.hasNext()) {
            KampfrichterPosition kp = li.next();
            if (kp.getPosition().equalsIgnoreCase(name)) {
                li.remove();
                kampfrichter.remove(kp.getPosition());
            }
        }
    }

    public static String stufeToText(KampfrichterStufe s, boolean show) {
        if (s.equals(KampfrichterStufe.KEINE) && (!show)) {
            return "";
        }
        return s.toString();
    }

    public String[][] getInhalt() {
        LinkedList<String[]> result = new LinkedList<String[]>();
        for (KampfrichterPosition pos : positionen) {
            LinkedList<Kampfrichter> karis = kampfrichter.get(pos.getPosition());
            boolean empty = true;
            if ((karis != null) && (!karis.isEmpty())) {
                ListIterator<Kampfrichter> li = karis.listIterator();
                while (li.hasNext()) {
                    Kampfrichter kr = li.next();
                    if (kr.getName().trim().length() > 0) {
                        empty = false;

                        String[] r = new String[6];
                        r[0] = pos.getPosition();
                        r[1] = kr.getName().trim();
                        r[2] = kr.getGliederung().trim();
                        r[3] = kr.getBemerkung().trim();
                        r[4] = stufeToText(kr.getStufe(), true);
                        r[5] = stufeToText(pos.getMinimaleStufe(), false);

                        result.addLast(r);
                    }
                }
            }
            if (empty) {
                String[] r = new String[6];
                r[0] = pos.getPosition();
                r[1] = "";
                r[2] = "";
                r[3] = "";
                r[4] = "";
                r[5] = stufeToText(pos.getMinimaleStufe(), false);

                result.addLast(r);
            }
        }
        return result.toArray(new String[result.size()][0]);
    }

    public void clearInput() {
        for (KampfrichterPosition s : positionen) {
            LinkedList<Kampfrichter> kr = new LinkedList<Kampfrichter>();
            kr.addLast(new Kampfrichter());
            kampfrichter.put(s.getPosition(), kr);
        }
    }

    public void exchange(int x, int y) {
        if (y < 0 || x < 0) {
            throw new IllegalArgumentException("Arguments must be at least 0.");
        }
        if (y >= positionen.size() || x >= positionen.size()) {
            throw new IllegalArgumentException("Arguments must be lower than the amount of entries.");
        }
        if (y == x) {
            throw new IllegalArgumentException("Arguments must not be equal.");
        }
        if (y < x) {
            int tmp = x;
            x = y;
            y = tmp;
        }

        KampfrichterPosition kpX = positionen.get(x);
        KampfrichterPosition kpY = positionen.get(y);
        positionen.set(x, kpY);
        positionen.set(y, kpX);
    }

    public int size() {
        return positionen.size();
    }
}