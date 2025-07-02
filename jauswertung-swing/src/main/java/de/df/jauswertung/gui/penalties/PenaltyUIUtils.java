package de.df.jauswertung.gui.penalties;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.vergleicher.SchwimmerNameVergleicher;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;
import de.df.jutils.gui.layout.SimpleListBuilder;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.print.PrintManager;

public class PenaltyUIUtils {
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

    public interface IPenaltyCallback {
        void setStrafe(Strafe s);
    }

    static class PenaltyCallback implements IPenaltyCallback {

        private boolean called = false;
        private Strafe s = null;

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

    public static <T extends ASchwimmer> JPanel[] getPenalties(AWettkampf<T> wk, boolean kurz) {
        return getPenalties(wk, -1, null, false, kurz);
    }

    public static <T extends ASchwimmer> JPanel[] getPenalties(AWettkampf<T> wk, int index, boolean[][] selected,
            boolean ignoreNA, boolean kurz) {
        Hashtable<String, JPanel> panels = new Hashtable<>();
        add(panels, wk, index, selected, false, null, ignoreNA, kurz);

        LinkedList<String> indizes = new LinkedList<>(panels.keySet());
        Collections.sort(indizes);

        if (indizes.isEmpty()) {
            return null;
        }

        LinkedList<JPanel> result = new LinkedList<>();
        for (String indize : indizes) {
            result.addLast(panels.get(indize));
        }

        return result.toArray(new JPanel[0]);
    }

    @SuppressWarnings({})
    public static <T extends ASchwimmer> void add(Hashtable<String, JPanel> panels, AWettkampf<T> wk, int index,
            boolean[][] selected, boolean border,
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

                    Collections.sort(liste, new SchwimmerNameVergleicher<>());

                    if (!liste.isEmpty()) {
                        for (T t : liste) {
                            add(panels, wk, index, t, border, pl, ignoreNA, kurz);
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
    private static <T extends ASchwimmer> void add(Hashtable<String, JPanel> panels, AWettkampf<T> wk, int index, T s,
            boolean border, PenaltyListener pl,
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

    private static <T extends ASchwimmer> void createPenaltyPanels(Hashtable<String, JPanel> panels, AWettkampf<T> wk,
            T s, boolean border, PenaltyListener pl,
            Altersklasse ak, int discipline, boolean ignoreNA, boolean kurz) {
        if (kurz) {
            createPenaltyPanelsShort(panels, wk, s, border, pl, ak, discipline, ignoreNA);
        } else {
            createPenaltyPanelsLong(panels, wk, s, border, pl, ak, discipline, ignoreNA);
        }
    }

    private static <T extends ASchwimmer> void createPenaltyPanelsShort(Hashtable<String, JPanel> panels,
            AWettkampf<T> wk, T s, boolean border,
            PenaltyListener pl, Altersklasse ak, int discipline, boolean ignoreNA) {
        LinkedList<Strafe> ls = s.getStrafen(discipline);
        ListIterator<Strafe> li = ls.listIterator();
        Lauf<T> lauf = null;
        if ((ls.size() > 0) && (discipline >= 0)) {
            lauf = wk.getLaufliste().suche(s, discipline);
        }

        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();

        // Penaltyoffset for sorting
        int offset = 0;

        while (li.hasNext()) {
            Strafe strafe = li.next();

            if ((strafe.getArt() != Strafarten.NICHT_ANGETRETEN) || (!ignoreNA)) {
                SimpleListBuilder sfm = new SimpleListBuilder(pl == null ? 1 : 4);
                sfm.setColor(Color.BLACK);
                if (pl == null) {
                    sfm.setFont(PrintManager.getFont());
                }
                if (lauf != null) {
                    sfm.addText(I18n.get("Heat") + " " + lauf.getName(scheme) + ", " + I18n.get("Lane") + " "
                            + (lauf.getIndex(s, discipline) + 1));
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
                    disciplinestring = I18n
                            .getDisziplinShort(s.getAK().getDisziplin(discipline, s.isMaennlich()).getName());
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
                            .createLabeledBorder(s.getName() + " - " + s.getGliederung() + " - " + s.getAK().toString()
                                    + " " + I18n.geschlechtToString(s)));
                }

                int laufindex = -1;
                if (wk.hasLaufliste()) {
                    laufindex = wk.getLaufliste().sucheIndex(s, discipline);
                }
                int bahnen = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
                panels.put(getID(laufindex, (lauf != null ? lauf.getIndex(s, discipline) : 0),
                        (wk.hasLaufliste() ? wk.getLaufliste().getLaufliste().size() : 0), bahnen, s.getName(),
                        s.getStartnummer(), offset, discipline),
                        sfm.getPanel());

                offset++;
            }
        }
    }

    private static <T extends ASchwimmer> void createPenaltyPanelsLong(Hashtable<String, JPanel> panels,
            AWettkampf<T> wk, T s, boolean border,
            PenaltyListener pl, Altersklasse ak, int discipline, boolean ignoreNA) {
        LinkedList<Strafe> ls = s.getStrafen(discipline);
        ListIterator<Strafe> li = ls.listIterator();
        Lauf<T> lauf = null;
        if ((!ls.isEmpty()) && (discipline >= 0)) {
            lauf = wk.getLaufliste().suche(s, discipline);
        }

        HeatsNumberingScheme scheme = wk.getHeatsNumberingScheme();

        // Penaltyoffset for sorting
        int offset = 0;

        while (li.hasNext()) {
            Strafe strafe = li.next();

            if ((strafe.getArt() != Strafarten.NICHT_ANGETRETEN) || (!ignoreNA)) {
                SimpleFormBuilder sfm = new SimpleFormBuilder(pl == null ? 1 : 4);
                sfm.setColor(Color.BLACK);
                if (pl == null) {
                    sfm.setFont(PrintManager.getFont());
                }
                if (lauf != null) {
                    sfm.add(I18n.get("Heat") + " / " + I18n.get("Lane") + ":  ",
                            lauf.getName(scheme) + " / " + (lauf.getIndex(s, discipline) + 1));
                }
                sfm.add(I18n.get("Name") + ":  ", s.getName());
                sfm.add(I18n.get("Organisation") + ":  ", s.getGliederung());
                sfm.add(I18n.get("AgeGroup") + ":  ", s.getAK().toString() + " " + I18n.geschlechtToString(s));
                if (discipline != ASchwimmer.DISCIPLINE_NUMBER_SELF) {
                    sfm.add(I18n.get("Discipline") + ":  ",
                            s.getAK().getDisziplin(discipline, s.isMaennlich()).getName());
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
                            .createLabeledBorder(s.getName() + " - " + s.getGliederung() + " - " + s.getAK().toString()
                                    + " " + I18n.geschlechtToString(s)));
                }

                int laufindex = -1;
                if (wk.hasLaufliste()) {
                    laufindex = wk.getLaufliste().sucheIndex(s, discipline);
                }
                int bahnen = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
                panels.put(getID(laufindex, (lauf != null ? lauf.getIndex(s, discipline) : 0),
                        (wk.hasLaufliste() ? wk.getLaufliste().getLaufliste().size() : 0), bahnen, s.getName(),
                        s.getStartnummer(), offset, discipline),
                        sfm.getPanel());

                offset++;
            }
        }
    }

    private static String getID(int laufindex, int bahnindex, int laufanzahl, int bahnenanzahl, String name, int sn,
            int offset, int discipline) {
        StringBuilder result = new StringBuilder();
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
        private IPenaltyCallback pc;

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
        private final AWettkampf wettkampf;
        private final ASchwimmer schwimmer;
        private final int disziplin;
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
}
