package de.df.jauswertung.daten;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Mannschaft.java Created on 9. Februar 2001, 12:12
 *
 * @author Dennis Fabri @version 0.0
 */
public class Mannschaft extends ASchwimmer {

    @XStreamAsAttribute
    private String name = "";
    private Mannschaftsmitglied[] mitglieder2;
    private int[][] starter;

    Mannschaft(MannschaftWettkampf mwk, String name, boolean geschlecht, String gliederung, int ak, String bemerkung) {
        super(mwk, geschlecht, gliederung, ak, bemerkung);
        setName(name);
    }

    @Override
    public String getName() {
        return name;
    }

    public final void setName(String sName) {
        if (sName == null) {
            throw new IllegalArgumentException("Name must not be null!");
        }
        name = sName.replace('\t', ' ').trim();
    }

    /**
     * Gibt an, wie oft der Schwimmer an der HLW teilnehmen muss.
     *
     * @return Anzahl der HLW-Teilnahmen.
     */
    @Override
    public int getMaximaleHLW() {
        return getMinMembers();
    }

    @Override
    public int getMinMembers() {
        return getAK().getMinMembers();

    }

    @Override
    public int getMaxMembers() {
        return getAK().getMaxMembers();
    }

    public Mannschaftsmitglied getMannschaftsmitglied(int index) {
        if (index < 0 || index >= getMaxMembers()) {
            throw new IllegalArgumentException("Value must at least 0 and lower than " + getMaxMembers() + ".");
        }

        initMembers();
        return mitglieder2[index];
    }

    private void initMembers() {
        if (mitglieder2 == null) {
            mitglieder2 = new Mannschaftsmitglied[getMaxMembers()];
            for (int x = 0; x < mitglieder2.length; x++) {
                mitglieder2[x] = new Mannschaftsmitglied();
            }
        } else if (mitglieder2.length < getMaxMembers()) {
            Mannschaftsmitglied[] neu = new Mannschaftsmitglied[getMaxMembers()];
            System.arraycopy(mitglieder2, 0, neu, 0, mitglieder2.length);
            for (int x = mitglieder2.length; x < neu.length; x++) {
                neu[x] = new Mannschaftsmitglied();
            }
            mitglieder2 = neu;
        }
    }

    public String getMitgliedsname(int index) {
        initMembers();

        if (index >= mitglieder2.length) {
            return "";
        }

        return mitglieder2[index].getName();
    }

    public String getStarter(int disz, String separator) {
        initMembers();
        int[] starter = getStarter(disz);
        if (starter == null || starter.length == 0) {
            return "";
        }
        ArrayList<Mannschaftsmitglied> mm = new ArrayList<>();
        boolean[] marker = new boolean[mitglieder2.length];
        for (int index : starter) {
            if (index > 0 && index <= mitglieder2.length && !marker[index - 1]) {
                mm.add(mitglieder2[index - 1]);
                marker[index - 1] = true;
            }
        }
        if (!mm.isEmpty()) {
            return getMitgliedernamen(mm.toArray(Mannschaftsmitglied[]::new), separator);
        }
        if (getDisciplineChoiceCount() > 1) {
            return "";
        }
        return getMitgliedernamen(separator);
    }

    public String getStarterShort(int disz, String separator) {
        initMembers();
        int[] starter = getStarter(disz);
        if (starter == null || starter.length == 0) {
            return "";
        }
        ArrayList<Mannschaftsmitglied> mm = new ArrayList<>();
        boolean[] marker = new boolean[mitglieder2.length];
        for (int index : starter) {
            if (index > 0 && index <= mitglieder2.length && !marker[index - 1]) {
                mm.add(mitglieder2[index - 1]);
                marker[index - 1] = true;
            }
        }
        if (!mm.isEmpty()) {
            return getMitgliedernamenShort(mm.toArray(Mannschaftsmitglied[]::new), separator);
        }
        if (getDisciplineChoiceCount() > 1) {
            return "";
        }
        return getMitgliedernamenShort(separator);
    }

    private static String getMitgliedernamenShort(Mannschaftsmitglied[] members, String separator) {
        int additional = -1;
        List<String> namen = new ArrayList<>();
        boolean repeat = false;
        int maxlength = 0;
        do {
            additional++;
            namen.clear();
            repeat = false;

            for (Mannschaftsmitglied m : members) {
                if (!m.isEmpty()) {
                    StringBuilder name = new StringBuilder();
                    if (additional > 0) {
                        String v = m.getVorname();
                        if (!v.isEmpty()) {
                            maxlength = Math.max(maxlength, v.length());
                            if (v.length() <= additional) {
                                name.append(v);
                                name.append(" ");
                            } else {
                                name.append(v, 0, additional);
                                name.append(".");
                            }
                        }
                    }
                    name.append(m.getNachname());
                    if (namen.contains(name.toString())) {
                        repeat = true;
                    }
                    namen.add(name.toString());
                }
            }
            if (additional > maxlength) {
                repeat = false;
            }
        } while (repeat);

        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String name : namen) {
            if (!first) {
                sb.append(separator);
            }
            sb.append(name);
            first = false;
        }
        return sb.toString();
    }

    public String getMitgliedernamenShort(String separator) {
        initMembers();
        return getMitgliedernamenShort(mitglieder2, separator);
    }

    public String getMitgliedernamen(String separator) {
        initMembers();
        return getMitgliedernamen(mitglieder2, separator);
    }

    private static String getMitgliedernamen(Mannschaftsmitglied[] mm, String separator) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (Mannschaftsmitglied m : mm) {
            if (!m.isEmpty()) {
                if (!first) {
                    sb.append(separator);
                }
                sb.append(m.getName());
                first = false;
            }
        }
        return sb.toString();
    }

    public String[] getMitgliedernamen() {
        initMembers();

        String[] namen = new String[mitglieder2.length];
        for (int x = 0; x < namen.length; x++) {
            if (mitglieder2[x] == null || mitglieder2[x].isEmpty()) {
                namen[x] = "";
            } else {
                namen[x] = mitglieder2[x].getName();
            }
            if (namen[x].isEmpty()) {
                namen[x] = "-";
            }
        }
        return namen;
    }

    public int getMannschaftsmitgliederAnzahl() {
        return getMannschaftsmitgliederAnzahl(false);
    }

    public int getMannschaftsmitgliederAnzahl(boolean strict) {
        initMembers();

        int count = 0;
        for (Mannschaftsmitglied m : mitglieder2) {
            if (strict) {
                if (m.isComplete()) {
                    count++;
                }
            } else {
                if (m.HasName()) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean hasMannschaftsmitglieder() {
        initMembers();
        return getMannschaftsmitgliederAnzahl() > 0;
    }

    public boolean isMannschaftsmitgliederComplete() {
        initMembers();

        int count = 0;
        for (Mannschaftsmitglied m : mitglieder2) {
            if (m.isComplete()) {
                count++;
            }
        }
        return count >= this.getMinMembers();
    }

    public void clearMannschaftsmitglieder() {
        initMembers();
        for (Mannschaftsmitglied m : mitglieder2) {
            m.clear();
        }
    }

    @Override
    public int[] getStarter(int disz) {
        if (!isDisciplineChosen(disz)) {
            return null;
        }
        initStarter();
        return starter[disz];
    }

    @Override
    public synchronized void setStarter(int disciplineIndex, int[] st) {
        initStarter();
        if (st == null) {
            Arrays.fill(starter[disciplineIndex], 0);
        } else {
            Arrays.fill(starter[disciplineIndex], 0);
            System.arraycopy(st, 0, starter[disciplineIndex], 0, Math.min(st.length, starter[disciplineIndex].length));
        }
    }

    @Override
    public void setStarter(int[][] c) {
        if (c == null) {
            throw new NullPointerException("Argument must not be null!");
        }
        initStarter();
        if (c.length == getDisciplineChoiceCount()) {
            int y = 0;
            for (int[] aC : c) {
                while (!isDisciplineChosen(y)) {
                    y++;
                }
                setStarter(y, aC);
                y++;
            }
            return;
        }

        if (starter.length != c.length) {
            throw new IllegalArgumentException(
                    "Starterarray must have correct size, which is either the "
                            + "number of disciplines in its Agegroup or the number of chosen disciplines ("
                            + c.length + " != " + starter.length + " and " + getAK().getDiszAnzahl() + ").");
        }
        System.arraycopy(c, 0, starter, 0, c.length);
    }

    private void initStarter() {
        if (starter == null) {
            starter = new int[getAK().getDiszAnzahl()][getMaxMembers()];
        } else if (starter.length != getAK().getDiszAnzahl()) {
            int[][] copy = new int[getAK().getDiszAnzahl()][getMaxMembers()];
            System.arraycopy(starter, 0, copy, 0, Math.min(copy.length, starter.length));
            starter = copy;
        }
        for (int x = 0; x < starter.length; x++) {
            if (starter[x] == null) {
                starter[x] = new int[getMaxMembers()];
            } else if (starter[x].length != getMaxMembers()) {
                int[] copy = new int[getMaxMembers()];
                System.arraycopy(starter[x], 0, copy, 0, Math.min(copy.length, starter[x].length));
                starter[x] = copy;
            }
        }
    }
}
