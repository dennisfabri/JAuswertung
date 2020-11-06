/*
 * Altersklassen.java Created on 10. Februar 2001, 02:15
 */

package de.df.jauswertung.daten.regelwerk;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;
import java.util.zip.CRC32;

import org.dom4j.Element;

import com.pmease.commons.xmt.VersionedDocument;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jutils.util.StringTools;

/**
 * Verwaltet die Altersklassen
 * 
 * @author Dennis Fabri
 * @version 0.3
 */
public class Regelwerk implements Serializable {

    private static final long          serialVersionUID         = -2388841285709038732L;

    /**
     * Enthaelt alle Alterklassen.
     */
    private Altersklasse[]             aks;
    @XStreamAsAttribute
    private String                     formel;
    @XStreamAsAttribute
    private String                     beschreibung             = "";

    private Translation                translations             = new Translation();

    @XStreamAsAttribute
    private int                        zusatzwertungBasispunkte = 200;

    @XStreamAsAttribute
    private GroupEvaluationMode        gesamtwertungsmodus      = GroupEvaluationMode.All;
    @XStreamAsAttribute
    private boolean                    gesamtwertung            = false;
    @XStreamAsAttribute
    private boolean                    gesamtwertungHart        = false;
    @XStreamAsAttribute
    private Skalierungsmodus           gesamtwertungSkalieren   = Skalierungsmodus.KEINER;

    @XStreamAsAttribute
    private String                     zusatzwertung            = "Zusatzwertung";
    @XStreamAsAttribute
    private String                     zusatzwertungKurz        = "ZW";

    private LinkedList<Startgruppe>    startgruppen             = new LinkedList<Startgruppe>();
    private LinkedList<Wertungsgruppe> wertungsgruppen          = new LinkedList<Wertungsgruppe>();

    /**
     * Gibt an, ob es sich um Einzel- oder Mannschaftsaltersklassen handelt.
     */
    private final boolean              einzel;

    /**
     * Creates new Altersklassen
     * 
     * @param _einzel
     *            Einzel- oder Mannschaftsaltersklassen
     */
    public Regelwerk(boolean isEinzel, String formel) {
        einzel = isEinzel;
        this.formel = formel;
        aks = new Altersklasse[0];
    }

    public String getTranslation(String key, String defaultValue) {
        if (translations == null) {
            translations = new Translation();
        }
        return translations.get(key, defaultValue);
    }

    public void setTranslation(String key, String value) {
        if (translations == null) {
            translations = new Translation();
        }
        translations.put(key, value);
    }

    // Keep for compatibility with XStream
    @Deprecated
    public Regelwerk() {
        this(false, "");
    }

    /**
     * Erzeugt "anzahl" neue Altersklassen.
     * 
     * @param anzahl
     *            Gibt die Anzahl der zu erzeugenden Altersklassen an.
     * @param _einzel
     *            Mannschafts- oder Einzelaltersklassen
     */
    public Regelwerk(int anzahl, boolean isEinzel, String formel) {
        einzel = isEinzel;
        this.formel = formel;
        aks = new Altersklasse[anzahl];
        for (int x = 0; x < anzahl; x++) {
            aks[x] = new Altersklasse(x);
        }
    }

    public boolean addStartgruppe(Startgruppe name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null!");
        }
        if (startgruppen == null) {
            startgruppen = new LinkedList<Startgruppe>();
        }
        if (startgruppen.contains(name)) {
            return false;
        }
        startgruppen.add(name);
        return true;
    }

    public boolean removeStartgruppe(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null!");
        }
        if (name.trim().length() == 0) {
            throw new IllegalArgumentException("Name must not be empty!");
        }
        if (startgruppen == null) {
            startgruppen = new LinkedList<Startgruppe>();
        }
        boolean found = false;
        ListIterator<Startgruppe> li = startgruppen.listIterator();
        while (li.hasNext()) {
            Startgruppe sg = li.next();
            if (sg.getName().equals(name)) {
                li.remove();
                found = true;
            }
        }
        return found;
    }

    public Startgruppe[] getStartgruppen() {
        if (startgruppen == null) {
            return new Startgruppe[0];
        }
        return startgruppen.toArray(new Startgruppe[startgruppen.size()]);
    }

    public int getStartgruppenindex(String name) {
        Startgruppe[] sgs = getEffektiveStartgruppen();
        for (int x = 0; x < sgs.length; x++) {
            if (sgs[x].getName().equals(name)) {
                return x;
            }
        }
        return -1;
    }

    public int getStartgruppenindex(Altersklasse ak) {
        return getStartgruppenindex(getStartgruppe(ak).getName());
    }

    public Startgruppe getStartgruppe(String name) {
        if ((name == null) || (name.length() == 0)) {
            throw new IllegalArgumentException("Name of startgroup must not be null or empty but was <" + name + ">.");
        }
        if (startgruppen == null) {
            return null;
        }
        for (Startgruppe sg : startgruppen) {
            if (sg.getName().equals(name)) {
                return sg;
            }
        }
        return null;
    }

    public void setStartgruppen(Startgruppe[] gruppen) {
        startgruppen = new LinkedList<Startgruppe>();
        for (Startgruppe gruppe : gruppen) {
            startgruppen.add(gruppe);
        }
        for (int x = 0; x < aks.length; x++) {
            if (aks[x].getStartgruppe() != null) {
                String name = aks[x].getStartgruppe();
                aks[x].setStartgruppe(null);
                for (Startgruppe gruppe : startgruppen) {
                    if (name.equals(gruppe.getName())) {
                        aks[x].setStartgruppe(gruppe.getName());
                    }
                }
            }
        }
    }

    public Wertungsgruppe[] getWertungsgruppen() {
        return getWertungsgruppen(false);
    }

    public Wertungsgruppe[] getWertungsgruppen(boolean nurMehrkampf) {
        if (wertungsgruppen == null) {
            return new Wertungsgruppe[0];
        }
        if (nurMehrkampf) {
            LinkedList<Wertungsgruppe> wgs = new LinkedList<>();
            for (Wertungsgruppe wg : wertungsgruppen) {
                if (wg.isProtokollMitMehrkampfwertung()) {
                    wgs.add(wg);
                }
            }
            return wgs.toArray(new Wertungsgruppe[wgs.size()]);
        }
        return wertungsgruppen.toArray(new Wertungsgruppe[wertungsgruppen.size()]);
    }

    public void setWertungsgruppen(Wertungsgruppe[] gruppen) {
        wertungsgruppen = new LinkedList<Wertungsgruppe>();
        for (Wertungsgruppe gruppe : gruppen) {
            wertungsgruppen.add(gruppe);
        }
        for (int x = 0; x < aks.length; x++) {
            if (aks[x].getWertungsgruppe() != null) {
                String name = aks[x].getWertungsgruppe();
                aks[x].setWertungsgruppe(null);
                for (Wertungsgruppe gruppe : wertungsgruppen) {
                    if (name.equals(gruppe.getName())) {
                        aks[x].setWertungsgruppe(gruppe.getName());
                    }
                }
            }
        }
    }

    public boolean addWertungsgruppe(Wertungsgruppe gruppe) {
        if (wertungsgruppen == null) {
            wertungsgruppen = new LinkedList<Wertungsgruppe>();
        }
        if (wertungsgruppen.contains(gruppe)) {
            return false;
        }
        wertungsgruppen.add(gruppe);
        return true;
    }

    public boolean removeWertungsgruppe(Wertungsgruppe gruppe) {
        if (gruppe == null) {
            throw new IllegalArgumentException("Group must not be null!");
        }
        if (wertungsgruppen == null) {
            wertungsgruppen = new LinkedList<Wertungsgruppe>();
        }
        return wertungsgruppen.remove(gruppe);
    }

    public void setSize(int count) {
        if (count == aks.length) {
            return;
        }
        Altersklasse[] aks2 = new Altersklasse[count];
        int max = Math.min(count, aks.length);
        System.arraycopy(aks, 0, aks2, 0, max);
        for (int x = max; x < count; x++) {
            aks2[x] = new Altersklasse(x);
        }
        aks = aks2;
    }

    public int size() {
        return aks.length;
    }

    public int indexOf(Altersklasse ak) {
        for (int x = 0; x < aks.length; x++) {
            if (aks[x] == ak) {
                return x;
            }
        }
        return -1;
    }

    public Altersklasse getAk(int index) {
        return aks[index];
    }

    public void setAk(int index, Altersklasse ak) {
        if (ak != null) {
            aks[index] = ak;
        }
    }

    public int getIndex(String name) {
        name = name.toLowerCase().trim();
        for (int x = 0; x < aks.length; x++) {
            if (aks[x].getName().toLowerCase().trim().equals(name)) {
                return x;
            }
        }
        for (int x = 0; x < aks.length; x++) {
            String s = aks[x].toString().toLowerCase().trim();
            if (s.equals("jugend " + name)) {
                return x;
            }
            if (s.equals("jugend" + name)) {
                return x;
            }
            if (s.equals("ak " + name)) {
                return x;
            }
            if (s.equals("ak" + name)) {
                return x;
            }
            String s1 = s.replace(" ", "");
            if (s1.equals(name) || s1.equals("ak" + name)) {
                return x;
            }
            name = name.replace("&", "/").replace("-", "/").replace("_", "/");
            if (s.equals(name) || s.equals("ak " + name)) {
                return x;
            }
            s = s.replace(" / ", "/");
            if (s.equals(name) || s.equals("ak " + name)) {
                return x;
            }
            s = s.replace("/", " ");
            if (s.equals(name) || s.equals("ak " + name)) {
                return x;
            }
        }
        return -1;
    }

    public int getAkNachAlter(int alter) {
        for (int x = 0; x < aks.length; x++) {
            boolean found = true;
            Altersklasse ak = aks[x];
            if (ak.getMinimumAlter() > 0) {
                if (alter < ak.getMinimumAlter()) {
                    found = false;
                }
            }
            if (ak.getMaximumAlter() > 0) {
                if (alter > ak.getMaximumAlter()) {
                    found = false;
                }
            }
            if (found) {
                return x;
            }
        }
        return -1;
    }

    public boolean hasHlw() {
        for (Altersklasse ak : aks) {
            if (ak.hasHLW()) {
                return true;
            }
        }
        return false;
    }

    public int getHLWCount() {
        int count = 0;
        for (Altersklasse ak : aks) {
            if (ak.hasHLW()) {
                count++;
            }
        }
        return count;

    }

    /**
     * @return Returns the einzel.
     */
    public boolean isEinzel() {
        return einzel;
    }

    public String getFormelID() {
        return formel;
    }

    public void setFormelID(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        formel = id;
    }

    public int getMaxDisciplineCount() {
        int number = 0;
        for (int x = 0; x < size(); x++) {
            number = Math.max(number, aks[x].getDiszAnzahl());
        }
        return number;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public boolean hasGesamtwertung() {
        return gesamtwertung;
    }

    public void setGesamtwertung(boolean gesamtwertung) {
        this.gesamtwertung = gesamtwertung;
    }

    public boolean isGesamtwertungHart() {
        return gesamtwertungHart;
    }

    public void setGesamtwertungHart(boolean gesamtwertungHart) {
        this.gesamtwertungHart = gesamtwertungHart;
    }

    public Skalierungsmodus getGesamtwertungSkalieren() {
        return gesamtwertungSkalieren;
    }

    public void setGesamtwertungSkalieren(Skalierungsmodus gesamtwertungSkalieren) {
        this.gesamtwertungSkalieren = gesamtwertungSkalieren;
    }

    public GroupEvaluationMode getGesamtwertungsmodus() {
        return gesamtwertungsmodus;
    }

    public void setGesamtwertungsmodus(GroupEvaluationMode gesamtwertungsmodus) {
        this.gesamtwertungsmodus = gesamtwertungsmodus;
    }

    public String getChecksum() {
        return getChecksum(aks);
    }

    private static final String[] BASIC_NAMES_JUNIORS = new String[] { "ak12", "ak1112", "ak1314", "ak1516", "ak1718", "akoffen" };

    private static final String[] BASIC_NAMES_MASTERS = new String[] { "ak25", "ak30", "ak35", "ak45", "ak50", "ak55", "ak60", "ak65", "ak70", "ak75", "ak80",
            "ak85", "ak90", "ak95", "ak100", "ak120", "ak140", "ak170", "ak200", "ak240", "ak280" };

    public String getJuniorsChecksum() {
        return getChecksum(BASIC_NAMES_JUNIORS);
    }

    public String getMastersChecksum() {
        return getChecksum(BASIC_NAMES_MASTERS);
    }

    public String getChecksum(String[] aknames) {
        LinkedList<Altersklasse> akx = new LinkedList<Altersklasse>();
        akx.addAll(Arrays.asList(aks));
        ListIterator<Altersklasse> li = akx.listIterator();
        while (li.hasNext()) {
            Altersklasse ak = li.next();
            String name = ak.getName().toLowerCase().replace(" ", "").replace("/", "").replace("&", "").replace("-", "").replace("+", "");
            boolean found = false;
            for (String basicName : aknames) {
                if (name.equals(basicName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                li.remove();
            }
        }

        if (akx.isEmpty()) {
            return "-";
        }

        return getChecksum(akx.toArray(new Altersklasse[akx.size()]));
    }

    private static String getChecksum(Altersklasse[] aks) {
        Altersklasse[] temp = Arrays.copyOf(aks, aks.length);
        Arrays.sort(temp, new Comparator<Altersklasse>() {
            @Override
            public int compare(Altersklasse o1, Altersklasse o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        PrintStream ps = new PrintStream(bos);
        for (Altersklasse aTemp : temp) {
            ps.print(aTemp.getChecksum());
            ps.print("|");
        }
        CRC32 crc = new CRC32();
        crc.update(bos.toByteArray());
        return StringTools.asCode(crc.getValue());
    }

    @SuppressWarnings("unused")
    private void migrate1(VersionedDocument dom, Stack<Integer> versions) {
        migrator1(dom.getRootElement());
    }

    public static void migrator1(Element node) {
        node.addElement("startgruppen");
        node.addElement("wertungsgruppen");
        for (Object ak : node.element("aks").elements()) {
            Altersklasse.migrator1((Element) ak);
        }
    }

    @SuppressWarnings("unused")
    private void migrate2(VersionedDocument dom, Stack<Integer> versions) {
        migrator2(dom.getRootElement());
    }

    public static void migrator2(Element node) {
        node.addElement("zusatzwertung").setText("HLW");
        node.addElement("zusatzwertungKurz").setText("HLW");
        node.addElement("zusatzwertungBasispunkte").setText("200");
        for (Object ak : node.element("aks").elements()) {
            Altersklasse.migrator2((Element) ak);
        }
    }

    public boolean isValid() {
        Wertungsgruppe[] wgs = getWertungsgruppen();
        if (FormelManager.isHeatBased(getFormelID())) {
            if (wgs.length > 0) {
                return false;
            }
        }
        for (Wertungsgruppe wg : wgs) {
            Altersklasse base = null;
            for (Altersklasse ak : aks) {
                for (int x = 0; x < ak.getDiszAnzahl(); x++) {
                    for (int y = 0; y < 2; y++) {
                        if (!ak.getDisziplin(x, y == 1).isValid()) {
                            return false;
                        }
                    }
                }
                if ((ak.getWertungsgruppe() != null) && (ak.getWertungsgruppe().equals(wg.getName()))) {
                    if (base == null) {
                        base = ak;
                    } else {
                        if (base.hasHLW() != ak.hasHLW()) {
                            return false;
                        }
                        if (base.getDiszAnzahl() != ak.getDiszAnzahl()) {
                            return false;
                        }
                        for (int x = 0; x < base.getDiszAnzahl(); x++) {
                            for (int y = 0; y < 2; y++) {
                                Disziplin dx = base.getDisziplin(x, y == 1);
                                Disziplin dy = ak.getDisziplin(x, y == 1);
                                // if (!dx.equals(dy)) {
                                if (!dx.getName().equals(dy.getName())) {
                                    return false;
                                }

                            }
                        }
                    }
                }
            }
        }

        Startgruppe[] sgs = getStartgruppen();
        if (FormelManager.isHeatBased(getFormelID())) {
            if (sgs.length > 0) {
                return false;
            }
        }
        for (Startgruppe sg : sgs) {
            Altersklasse base = null;
            for (Altersklasse ak : aks) {
                if ((ak.getStartgruppe() != null) && (ak.getStartgruppe().equals(sg.getName()))) {
                    if (base == null) {
                        base = ak;
                    } else {
                        if (base.getDiszAnzahl() != ak.getDiszAnzahl()) {
                            return false;
                        }
                        for (int x = 0; x < base.getDiszAnzahl(); x++) {
                            for (int y = 0; y < 2; y++) {
                                Disziplin dx = base.getDisziplin(x, y == 1);
                                Disziplin dy = ak.getDisziplin(x, y == 1);
                                if (!dx.getName().equals(dy.getName())) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Altersklasse ak : aks) {
            if (!ak.isValid()) {
                return false;
            }
        }
        return true;
    }

    public Wertungsgruppe getWertungsgruppe(String name) {
        if (wertungsgruppen == null) {
            return null;
        }
        for (Wertungsgruppe wg : wertungsgruppen) {
            if (wg.getName().equals(name)) {
                return wg;
            }
        }
        return null;
    }

    public Altersklasse[] getAks() {
        Altersklasse[] result = new Altersklasse[aks.length];
        for (int x = 0; x < aks.length; x++) {
            result[x] = aks[x];
        }
        return result;
    }

    public synchronized void addAk(Altersklasse ak) {
        if (ak == null) {
            throw new IllegalArgumentException("Agegroup must not be null.");
        }
        Altersklasse[] naks = new Altersklasse[aks.length + 1];
        for (int x = 0; x < aks.length; x++) {
            naks[x] = aks[x];
        }
        naks[aks.length] = ak;
        aks = naks;
    }

    public synchronized Startgruppe[] getEffektiveStartgruppen() {
        LinkedList<Startgruppe> sgs = new LinkedList<Startgruppe>();
        for (Altersklasse ak : aks) {
            Startgruppe sg = getStartgruppe(ak);
            if (!sgs.contains(sg)) {
                sgs.add(sg);
            }
        }
        return sgs.toArray(new Startgruppe[sgs.size()]);
    }

    public Startgruppe getStartgruppe(Altersklasse ak) {
        Startgruppe sg = null;
        if (ak.getStartgruppe() != null) {
            sg = getStartgruppe(ak.getStartgruppe());
        }
        if (sg == null) {
            sg = ak.getInterneStartgruppe();
        }
        return sg;
    }

    public synchronized LinkedList<Altersklasse> getAKsForStartgroup(Startgruppe sg) {
        LinkedList<Altersklasse> aksLocal = new LinkedList<Altersklasse>();
        for (Altersklasse ak : this.aks) {
            if (getStartgruppe(ak).equals(sg)) {
                aksLocal.add(ak);
            }
        }
        return aksLocal;
    }

    public String getZusatzwertung() {
        return zusatzwertung;
    }

    public void setZusatzwertung(String zw) {
        if (zw == null || zw.isEmpty()) {
            throw new IllegalArgumentException("zw must not be null or emtpy.");
        }
        zusatzwertung = zw;
    }

    public String getZusatzwertungShort() {
        return zusatzwertungKurz;
    }

    public void setZusatzwertungKurz(String zw) {
        if (zw == null || zw.isEmpty()) {
            throw new IllegalArgumentException("zw must not be null or emtpy.");
        }
        zusatzwertungKurz = zw;
    }

    public int getZusatzwertungBasispunkte() {
        return zusatzwertungBasispunkte;
    }

    public void setZusatzwertungBasispunkte(int zusatzwertungBasispunkte) {
        if (zusatzwertungBasispunkte < 0) {
            throw new IllegalArgumentException("points must be at least 0.");
        }
        this.zusatzwertungBasispunkte = zusatzwertungBasispunkte;
    }

    public int getRundenId(OWSelection ows) {
        try {
            return getAk(ows.akNummer).getDisziplin(ows.discipline, ows.male).getRundenId(ows.round);
        } catch (Exception ex) {
            return 0;
        }
    }

    public int getRundenId(OWDisziplin<?> owd) {
        try {
            return getAk(owd.akNummer).getDisziplin(owd.disziplin, owd.maennlich).getRundenId(owd.round);
        } catch (Exception ex) {
            return 0;
        }
    }
}