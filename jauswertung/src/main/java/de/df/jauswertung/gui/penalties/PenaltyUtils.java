/*
 * Created on 03.05.2005
 */
package de.df.jauswertung.gui.penalties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.vergleicher.SchwimmerNameVergleicher;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;
import de.df.jutils.gui.layout.SimpleListBuilder;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.print.PrintManager;

public final class PenaltyUtils {

    private PenaltyUtils() {
        // Hide
    }

    public static void showPenalties(JFrame parent, Strafen s) {
        if (parent == null) {
            new JStrafenkatalogFrame(null, s).setVisible(true);
        } else {
            ModalFrameUtil.showAsModal(new JStrafenkatalogFrame(parent, s), parent);
        }
    }

    public static void showPenalties(Strafen s) {
        JStrafenkatalogFrame jsk = new JStrafenkatalogFrame(null, s);
        jsk.setVisible(true);
    }

    public static Strafe showPenalties(JDialog p, Strafen s, Strafe str) {
        JStrafenkatalogDialog sk = new JStrafenkatalogDialog(p, str, s);
        sk.setVisible(true);
        PenaltyCallback pci = new PenaltyCallback();
        Thread t = new ShowPenaltiesThread(sk, pci);
        t.run();
        sk.dispose();
        return pci.getStrafe();
    }

    public static interface IPenaltyCallback {
        void setStrafe(Strafe s);
    }

    static class PenaltyCallback implements IPenaltyCallback {

        private boolean called = false;
        private Strafe  s      = null;

        @Override
        public void setStrafe(Strafe s) {
            called = true;
            this.s = s;
        }

        public Strafe getStrafe() {
            return s;
        }

        public boolean isCalled() {
            return called;
        }
    }

    public static String getPenaltyValue(Strafe strafe, Altersklasse ak) {
        if (strafe == null) {
            return "";
        }
        StringBuffer text = new StringBuffer();

        if (ak != null) {
            strafe = new Strafe(strafe, ak.isStrafeIstDisqualifikation());
        }
        switch (strafe.getArt()) {
        case AUSSCHLUSS:
            text.append(I18n.get("DebarmentShort"));
            break;
        case DISQUALIFIKATION:
            text.append(I18n.get("DisqualifiedShort"));
            break;
        case NICHT_ANGETRETEN:
            text.append(I18n.get("DidNotStartShort"));
            break;
        case STRAFPUNKTE:
            int hoehe = strafe.getStrafpunkte();
            text.append(hoehe);
            break;
        case NICHTS:
            text.append("-");
            break;
        default:
            break;
        }
        return text.toString();
    }

    public static String getPenaltyShortText(Strafe strafe, Altersklasse ak) {
        if (strafe == null) {
            return "";
        }
        StringBuffer text = new StringBuffer();

        if (ak != null) {
            strafe = new Strafe(strafe, ak.isStrafeIstDisqualifikation());
        }
        if (strafe.getShortname().length() > 0) {
            text.append(strafe.getShortname());
        } else {
            switch (strafe.getArt()) {
            case AUSSCHLUSS:
                text.append(I18n.get("DebarmentShort"));
                break;
            case DISQUALIFIKATION:
                text.append(I18n.get("DisqualifiedShort"));
                break;
            case NICHT_ANGETRETEN:
                text.append(I18n.get("DidNotStartShort"));
                break;
            case STRAFPUNKTE:
                int hoehe = strafe.getStrafpunkte();
                text.append(hoehe);
                break;
            case NICHTS:
                text.append(" ");
                break;
            default:
                break;
            }
        }
        return text.toString();
    }

    public static <T extends ASchwimmer> boolean hasPenalties(AWettkampf<T> wk) {
        if (wk == null) {
            return false;
        }

        ListIterator<T> li = wk.getSchwimmer().listIterator();
        while (li.hasNext()) {
            boolean result = hasPenalties(li.next());
            if (result) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasPenalties(ASchwimmer s) {
        Altersklasse ak = s.getAK();
        for (int x = 0; x < ak.getDiszAnzahl(); x++) {
            Strafe strafe = s.getAkkumulierteStrafe(x);
            if ((strafe != null) && (strafe.isStrafe())) {
                return true;
            }
        }
        return false;
    }

    public static JPanel[] getPenalties(CorePlugin core, int index, boolean[][] selected, boolean ignoreNA, boolean kurz) {
        return getPenalties(core, index, selected, null, ignoreNA, kurz);
    }

    public static JPanel[] getPenalties(CorePlugin core, int index, boolean[][] selected, PenaltyListener pl, boolean ignoreNA, boolean kurz) {
        return getPenalties(core, index, selected, pl != null, pl, ignoreNA, kurz);
    }

    public static JPanel[] getPenalties(CorePlugin core, PenaltyListener pl, boolean ignoreNA, boolean kurz) {
        return getPenalties(core, -1, null, pl != null, pl, ignoreNA, kurz);
    }

    private static JPanel[] getPenalties(CorePlugin core, int index, boolean[][] selected, boolean border, PenaltyListener pl, boolean ignoreNA, boolean kurz) {
        Hashtable<String, JPanel> panels = new Hashtable<String, JPanel>();
        add(panels, core.getWettkampf(), index, selected, border, pl, ignoreNA, kurz);

        LinkedList<String> indizes = new LinkedList<String>();
        for (String key : panels.keySet()) {
            indizes.add(key);
        }
        Collections.sort(indizes);

        LinkedList<JPanel> result = new LinkedList<JPanel>();
        ListIterator<String> li = indizes.listIterator();
        while (li.hasNext()) {
            result.addLast(panels.get(li.next()));
        }

        return result.toArray(new JPanel[result.size()]);
    }

    public static <T extends ASchwimmer> JPanel[] getPenalties(AWettkampf<T> wk) {
        return getPenalties(wk, -1, null, false, false);
    }

    public static <T extends ASchwimmer> JPanel[] getPenaltiesShort(AWettkampf<T> wk) {
        return getPenalties(wk, -1, null, false, true);
    }

    public static <T extends ASchwimmer> JPanel[] getPenalties(AWettkampf<T> wk, int index, boolean[][] selected, boolean ignoreNA) {
        return getPenalties(wk, index, selected, ignoreNA, false);
    }

    public static <T extends ASchwimmer> JPanel[] getPenalties(AWettkampf<T> wk, int index, boolean[][] selected, boolean ignoreNA, boolean kurz) {
        Hashtable<String, JPanel> panels = new Hashtable<String, JPanel>();
        add(panels, wk, index, selected, false, null, ignoreNA, kurz);

        LinkedList<String> indizes = new LinkedList<String>();
        for (String key : panels.keySet()) {
            indizes.add(key);
        }
        Collections.sort(indizes);

        if (indizes.size() == 0) {
            return null;
        }

        LinkedList<JPanel> result = new LinkedList<JPanel>();
        ListIterator<String> li = indizes.listIterator();
        while (li.hasNext()) {
            result.addLast(panels.get(li.next()));
        }

        return result.toArray(new JPanel[result.size()]);
    }

    public static <T extends ASchwimmer> JPanel[] getPenaltiesShort(AWettkampf<T> wk, int index, boolean[][] selected, boolean ignoreNA) {
        return getPenalties(wk, index, selected, ignoreNA, true);
    }

    @SuppressWarnings({})
    private static <T extends ASchwimmer> void add(Hashtable<String, JPanel> panels, AWettkampf<T> wk, int index, boolean[][] selected, boolean border,
            PenaltyListener pl, boolean ignoreNA, boolean kurz) {
        if (wk == null) {
            return;
        }

        Regelwerk aks = wk.getRegelwerk();
        if (index >= aks.getMaxDisciplineCount()) {
            index = -1;
        }
        for (int x = 0; x < aks.size(); x++) {
            for (int y = 0; y < 2; y++) {
                if (isSelected(selected, x, y)) {
                    LinkedList<T> liste = SearchUtils.getSchwimmer(wk, aks.getAk(x), y == 1);

                    Collections.sort(liste, new SchwimmerNameVergleicher<T>());
                    // Collections.sort(liste, new SchwimmerAKVergleicher());

                    if ((liste != null) && (liste.size() > 0)) {
                        ListIterator<T> li = liste.listIterator();
                        while (li.hasNext()) {
                            add(panels, wk, index, li.next(), border, pl, ignoreNA, kurz);
                        }
                    }
                }
            }
        }
    }

    private static boolean isSelected(boolean[][] selected, int i, int j) {
        if (selected == null) {
            return true;
        }
        return selected[j][i];
    }

    @SuppressWarnings({})
    private static <T extends ASchwimmer> void add(Hashtable<String, JPanel> panels, AWettkampf<T> wk, int index, T s, boolean border, PenaltyListener pl,
            boolean ignoreNA, boolean kurz) {

        Altersklasse ak = s.getAK();
        int start = 0;
        int stop = ak.getDiszAnzahl();
        if (index == -1) {
            createPenaltyPanels(panels, wk, s, border, pl, ak, ASchwimmer.DISCIPLINE_NUMBER_SELF, ignoreNA, kurz);
        } else {
            if (index >= stop) {
                return;
            }
            start = index;
            stop = start + 1;
        }
        for (int x = start; x < stop; x++) {
            if (s.isDisciplineChosen(x)) {
                createPenaltyPanels(panels, wk, s, border, pl, ak, x, ignoreNA, kurz);
            }
        }
    }

    private static <T extends ASchwimmer> void createPenaltyPanels(Hashtable<String, JPanel> panels, AWettkampf<T> wk, T s, boolean border, PenaltyListener pl,
            Altersklasse ak, int discipline, boolean ignoreNA, boolean kurz) {
        if (kurz) {
            createPenaltyPanelsShort(panels, wk, s, border, pl, ak, discipline, ignoreNA);
        } else {
            createPenaltyPanelsLong(panels, wk, s, border, pl, ak, discipline, ignoreNA);
        }
    }

    private static <T extends ASchwimmer> void createPenaltyPanelsShort(Hashtable<String, JPanel> panels, AWettkampf<T> wk, T s, boolean border,
            PenaltyListener pl, Altersklasse ak, int discipline, boolean ignoreNA) {
        LinkedList<Strafe> ls = s.getStrafen(discipline);
        ListIterator<Strafe> li = ls.listIterator();
        Lauf<T> lauf = null;
        if ((ls.size() > 0) && (discipline >= 0)) {
            lauf = wk.getLaufliste().suche(s, discipline);
        }

        // Penaltyoffset for sorting
        int offset = 0;

        while (li.hasNext()) {
            Strafe strafe = li.next();

            if ((strafe.getArt() != Strafarten.NICHT_ANGETRETEN) || (!ignoreNA)) {
                SimpleListBuilder sfm = new SimpleListBuilder(pl == null ? 1 : 4);
                if (pl == null) {
                    sfm.setFont(PrintManager.getFont());
                }
                if (lauf != null) {
                    sfm.addText(I18n.get("Heat") + " " + lauf.getName() + ", " + I18n.get("Lane") + " " + (lauf.getIndex(s, discipline) + 1));
                }

                String name = s.getName();
                String quali = "";
                if (s.getQualifikationsebene().length() > 0) {
                    quali = " (" + s.getQualifikationsebene() + ")";
                }
                String org = s.getGliederung() + quali;
                String akstring = I18n.getAgeGroupAsString(s);
                String disciplinestring = "";
                if (discipline != ASchwimmer.DISCIPLINE_NUMBER_SELF) {
                    disciplinestring = I18n.getDisziplinShort(s.getAK().getDisziplin(discipline, s.isMaennlich()).getName());
                } else {
                    disciplinestring = "";
                }
                String strafestring = PenaltyUtils.getPenaltyText(strafe, ak);

                sfm.addText(name + ", " + org);
                sfm.addText(akstring + ", " + disciplinestring);
                sfm.addText(strafestring);
                if (pl != null) {
                    JButton button = new JButton(I18n.get("Edit"));
                    button.addActionListener(new PenaltyActionListener(wk, s, discipline, pl));
                    sfm.addButton(button);
                }

                if (border) {
                    sfm.setBorder(BorderUtils
                            .createLabeledBorder(s.getName() + " - " + s.getGliederung() + " - " + s.getAK().toString() + " " + I18n.geschlechtToString(s)));
                }

                int laufindex = -1;
                if (wk.hasLaufliste()) {
                    laufindex = wk.getLaufliste().sucheIndex(s, discipline);
                }
                int bahnen = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
                panels.put(getID(laufindex, (lauf != null ? lauf.getIndex(s, discipline) : 0),
                        (wk.hasLaufliste() ? wk.getLaufliste().getLaufliste().size() : 0), bahnen, s.getName(), s.getStartnummer(), offset, discipline),
                        sfm.getPanel());

                offset++;
            }
        }
    }

    private static <T extends ASchwimmer> void createPenaltyPanelsLong(Hashtable<String, JPanel> panels, AWettkampf<T> wk, T s, boolean border,
            PenaltyListener pl, Altersklasse ak, int discipline, boolean ignoreNA) {
        LinkedList<Strafe> ls = s.getStrafen(discipline);
        ListIterator<Strafe> li = ls.listIterator();
        Lauf<T> lauf = null;
        if ((ls.size() > 0) && (discipline >= 0)) {
            lauf = wk.getLaufliste().suche(s, discipline);
        }

        // Penaltyoffset for sorting
        int offset = 0;

        while (li.hasNext()) {
            Strafe strafe = li.next();

            if ((strafe.getArt() != Strafarten.NICHT_ANGETRETEN) || (!ignoreNA)) {
                SimpleFormBuilder sfm = new SimpleFormBuilder(pl == null ? 1 : 4);
                if (pl == null) {
                    sfm.setFont(PrintManager.getFont());
                }
                if (lauf != null) {
                    sfm.add(I18n.get("Heat") + " / " + I18n.get("Lane") + ":  ", lauf.getName() + " / " + (lauf.getIndex(s, discipline) + 1));
                }
                sfm.add(I18n.get("Name") + ":  ", s.getName());
                sfm.add(I18n.get("Organisation") + ":  ", s.getGliederung());
                sfm.add(I18n.get("AgeGroup") + ":  ", s.getAK().toString() + " " + I18n.geschlechtToString(s));
                if (discipline != ASchwimmer.DISCIPLINE_NUMBER_SELF) {
                    sfm.add(I18n.get("Discipline") + ":  ", s.getAK().getDisziplin(discipline, s.isMaennlich()).getName());
                } else {
                    sfm.add(I18n.get("Discipline") + ":  ", "-");
                }
                sfm.add(I18n.get("Penalty") + ":  ", PenaltyUtils.getPenaltyText(strafe, ak));
                JLabel label = new JLabel("<html><body>" + strafe.getName() + "</body></html>");
                label.setVerticalAlignment(SwingConstants.TOP);
                sfm.add(I18n.get("Statement") + ":  ", label, SimpleFormBuilder.GrowModel.Resize);
                if (pl != null) {
                    JButton button = new JButton(I18n.get("Edit"));
                    button.addActionListener(new PenaltyActionListener(wk, s, discipline, pl));
                    sfm.addButton(button);
                }

                if (border) {
                    sfm.setBorder(BorderUtils
                            .createLabeledBorder(s.getName() + " - " + s.getGliederung() + " - " + s.getAK().toString() + " " + I18n.geschlechtToString(s)));
                }

                int laufindex = -1;
                if (wk.hasLaufliste()) {
                    laufindex = wk.getLaufliste().sucheIndex(s, discipline);
                }
                int bahnen = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
                panels.put(getID(laufindex, (lauf != null ? lauf.getIndex(s, discipline) : 0),
                        (wk.hasLaufliste() ? wk.getLaufliste().getLaufliste().size() : 0), bahnen, s.getName(), s.getStartnummer(), offset, discipline),
                        sfm.getPanel());

                offset++;
            }
        }
    }

    private static String getID(int laufindex, int bahnindex, int laufanzahl, int bahnenanzahl, String name, int sn, int offset, int discipline) {
        StringBuffer result = new StringBuffer();
        if (laufanzahl > 0) {
            int llength = Math.max(("" + laufanzahl).length(), 2);
            int blength = Math.max(("" + bahnenanzahl).length(), 2);

            String text = "" + laufindex;
            for (int x = 0; x < llength - text.length(); x++) {
                result.append("0");
            }
            result.append(laufindex);

            result.append("x");

            text = "" + bahnindex;
            for (int x = 0; x < blength - text.length(); x++) {
                result.append("0");
            }
            result.append(bahnindex);
            result.append("x");
        }

        result.append(discipline);
        result.append("x");
        result.append(name);
        result.append("x");
        result.append(sn);
        result.append("x");
        result.append(offset);

        return result.toString();
    }

    private static final class ShowPenaltiesThread extends Thread {

        private JStrafenkatalogDialog sk;
        private IPenaltyCallback      pc;

        public ShowPenaltiesThread(JStrafenkatalogDialog sk, IPenaltyCallback pc) {
            super("ShowPenaltiesThread");
            this.sk = sk;
            this.pc = pc;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                // Nothing to do
            }
            while (sk.isVisible()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // Nothing to do
                }
            }
            Strafe strafe = sk.getStrafe();
            pc.setStrafe(strafe);
        }
    }

    private static final class PenaltyActionListener implements ActionListener {

        @SuppressWarnings("rawtypes")
        private final AWettkampf      wettkampf;
        private final ASchwimmer      schwimmer;
        private final int             disziplin;
        private final PenaltyListener listener;

        @SuppressWarnings("rawtypes")
        public PenaltyActionListener(AWettkampf wk, ASchwimmer s, int d, PenaltyListener pl) {
            wettkampf = wk;
            schwimmer = s;
            disziplin = d;
            listener = pl;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            listener.edit(wettkampf, schwimmer, disziplin);
        }
    }

    public static interface PenaltyListener {
        @SuppressWarnings("rawtypes")
        void edit(AWettkampf wk, ASchwimmer s, int disziplin);
    }

    public static String getPenaltyText(Strafe s, Altersklasse ak) {
        if (s == null) {
            return "";
        }
        s = new Strafe(s, ak.isStrafeIstDisqualifikation());
        StringBuffer text = new StringBuffer();
        if (s.getShortname().length() > 0) {
            text.append(s.getShortname());
            text.append(": ");
        }
        switch (s.getArt()) {
        case AUSSCHLUSS:
            text.append(I18n.get("Debarment"));
            break;
        case DISQUALIFIKATION:
            text.append(I18n.get("Disqualification"));
            break;
        case NICHT_ANGETRETEN:
            text.append(I18n.get("DidNotStart"));
            break;
        case NICHTS:
            text.append(I18n.get("NoPenalty"));
            break;
        case STRAFPUNKTE:
            text.append(NumberFormat.getInstance().format(s.getStrafpunkte()));
            break;
        default:
            text.append(" ");
            break;
        }
        return text.toString();
    }

    public static String getPenaltyMediumText(Strafe s, Altersklasse ak) {
        if (s == null) {
            return "";
        }
        s = new Strafe(s, ak.isStrafeIstDisqualifikation());

        String text1 = "";
        String text2 = "";
        if (s.getShortname().length() > 0) {
            text1 = s.getShortname();
            // text.append(": ");
        }
        switch (s.getArt()) {
        case AUSSCHLUSS:
            text2 = I18n.get("DebarmentShort");
            break;
        case DISQUALIFIKATION:
            if (!ak.isStrafeIstDisqualifikation() || s.getShortname().length() == 0) {
                text2 = I18n.get("DisqualificationShort");
            }
            break;
        case NICHT_ANGETRETEN:
            text2 = I18n.get("DidNotStartShort");
            break;
        case NICHTS:
            text2 = I18n.get("NoPenaltyShort");
            break;
        case STRAFPUNKTE:
            text2 = NumberFormat.getInstance().format(s.getStrafpunkte());
            break;
        default:
            break;
        }

        StringBuffer text = new StringBuffer();
        text.append(text1);
        if (text1.length() > 0 && text2.length() > 0) {
            text.append(": ");
        }
        text.append(text2);
        return text.toString();
    }
}