/*
 * Created on 23.02.2006
 */
package de.df.jauswertung.daten.kampfrichter;

import java.util.Hashtable;
import java.util.LinkedList;

public class KampfrichterEinheit {

    private String name = "";
    private LinkedList<KampfrichterPosition> positionen = new LinkedList<>();
    /**
     * Only read from files written before the referees moved into their positions (keyed by the position name);
     * {@link #readResolve()} moves the entries into the positions and clears it.
     */
    private Hashtable<String, LinkedList<Kampfrichter>> kampfrichter;

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

    public KampfrichterStufe getMinimaleStufe(String pos) {
        KampfrichterPosition kp = getPosition(pos);
        return kp == null ? null : kp.getMinimaleStufe();
    }

    public void setKampfrichter(String position, LinkedList<Kampfrichter> kr) {
        if (position == null) {
            throw new NullPointerException();
        }
        if (kr == null) {
            throw new NullPointerException();
        }
        existingPosition(position).setKampfrichter(kr);
    }

    public boolean hasPosition(String position) {
        return getPosition(position) != null;
    }

    public KampfrichterPosition getPosition(String position) {
        if (position == null) {
            throw new NullPointerException();
        }
        for (KampfrichterPosition kp : positionen) {
            if (kp.getPosition().equals(position)) {
                return kp;
            }
        }
        return null;
    }

    private KampfrichterPosition existingPosition(String position) {
        KampfrichterPosition kp = getPosition(position);
        if (kp == null) {
            throw new IllegalArgumentException("Position \"" + position + "\" not found.");
        }
        return kp;
    }

    public void addKampfrichter(String position) {
        addKampfrichter(position, new Kampfrichter());
    }

    public void renamePosition(KampfrichterPosition pos, String neuername) {
        getPosition(pos.getPosition()).setPosition(neuername);
    }

    public void addKampfrichter(String position, Kampfrichter kr) {
        if (position == null) {
            throw new NullPointerException();
        }
        if (kr == null) {
            throw new NullPointerException();
        }
        existingPosition(position).addKampfrichter(kr);
    }

    public Kampfrichter[] getKampfrichter(String position) {
        KampfrichterPosition kp = getPosition(position);
        return kp == null ? new Kampfrichter[0] : kp.getKampfrichter();
    }

    public String getName() {
        return name;
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
        if (hasPosition(position)) {
            throw new IllegalArgumentException("\"" + position + "\" already exists!");
        }
        positionen.addLast(new KampfrichterPosition(position, stufe));
        for (int x = 0; x < anzahl; x++) {
            addKampfrichter(position);
        }
    }

    public void removePosition(@SuppressWarnings("hiding") String name) {
        positionen.removeIf(kp -> kp.getPosition().equals(name));
    }

    public static String stufeToText(KampfrichterStufe s, boolean show) {
        if (s.equals(KampfrichterStufe.KEINE) && (!show)) {
            return "";
        }
        return s.toString();
    }

    public String[][] getInhalt() {
        LinkedList<String[]> result = new LinkedList<>();
        for (KampfrichterPosition pos : positionen) {
            boolean empty = true;
            for (Kampfrichter kr : pos.getKampfrichterListe()) {
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

    /**
     * Called after loading, where no constructor runs. Moves the referees of files written before they moved into
     * their positions.
     */
    private Object readResolve() {
        if (kampfrichter != null) {
            for (KampfrichterPosition kp : positionen) {
                LinkedList<Kampfrichter> karis = kampfrichter.get(kp.getPosition());
                if (karis != null) {
                    kp.setKampfrichter(karis);
                }
            }
            kampfrichter = null;
        }
        return this;
    }
}
