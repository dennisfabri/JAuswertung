/*
 * Created on 19.02.2006
 */
package de.df.jauswertung.gui.plugins.check;

import java.awt.Component;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.Wettkampfart;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.kampfrichter.Kampfrichter;
import de.df.jauswertung.daten.kampfrichter.KampfrichterEinheit;
import de.df.jauswertung.daten.kampfrichter.KampfrichterPosition;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.laufliste.HLWLauf;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.regelwerk.Startunterlagen;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.AltersklassenUtils;
import de.df.jauswertung.util.ZielrichterentscheidUtils;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.layout.SimpleFormBuilder;

public class JInfoPanel extends JGlassPanel<JPanel> {

    private static final long serialVersionUID = 8023494074221318513L;

    private CorePlugin core;

    private JPanel panel;

    private JLabel[] infos;
    private boolean[] state;
    private Object[] text;

    private static final int INPUT_COMPLETE_TIMES = 0;
    private static final int INPUT_HIGH_TIMES = 1;
    private static final int INPUT_LOW_TIMES = 2;
    private static final int INPUT_HLW_COMPLETE = 3;
    private static final int INPUT_HLW_VALUES = 4;
    private static final int LISTS_HEATS_ALL = 5;
    private static final int LISTS_HEATS_MIXED = 6;
    private static final int LISTS_HLW_ALL = 7;
    private static final int OPT_YEAR_OF_BIRTH = 8;
    private static final int OPT_TEAMMEMBERS = 9;
    private static final int REG_NAMES = 10;
    private static final int REG_DISCIPLINES = 11;
    private static final int REG_STARTPASS = 12;
    private static final int REF_INPUT_COMPLETE = 13;
    private static final int REF_AMOUNT = 14;
    private static final int REF_INPUT_LEVEL = 15;
    private static final int ZRENTSCHEID = 16;

    private static final int AMOUNT = 17;

    private static boolean[] IS_ERROR = new boolean[] { true, false, false, true, false, false, false, false, false,
            false, true, true, false,
            true, true, false, true };

    private static String[] I18N = new String[] { "ChecksTimes", "ChecksHighTimes", "ChecksLowTimes",
            "ChecksZWComplete", "ChecksZWValues",
            "ChecksHeatsComplete", "ChecksMixedHeats", "ChecksZWList", "ChecksYearOfBirth", "ChecksTeamMembers",
            "ChecksNames", "ChecksDisciplines",
            "ChecksStartunterlagen", "ChecksRefereesComplete", "ChecksRefereesAmount", "ChecksRefereesLevel",
            "ChecksZielrichterentscheide" };

    public JInfoPanel(JMissingInputFrame parent, CorePlugin core, FEditorPlugin editor) {
        super(new JPanel());
        panel = getComponent();
        this.core = core;
        init();
    }

    private void init() {
        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();

        infos = new JLabel[AMOUNT];
        state = new boolean[AMOUNT];
        text = new Object[AMOUNT];
        for (int x = 0; x < AMOUNT; x++) {
            infos[x] = new JLabel();
            state[x] = false;
            text[x] = null;
        }

        panel.removeAll();
        SimpleFormBuilder dfb = new SimpleFormBuilder(panel);

        dfb.addSeparator(I18n.get("ImportantChecks"));
        dfb.add(I18n.get("Registration"), (Component) null);
        dfb.add("    " + I18n.get("Names"), infos[REG_NAMES], null, "right,fill");
        dfb.add("    " + I18n.get("Disciplines"), infos[REG_DISCIPLINES], null, "right,fill");
        dfb.add("    " + I18n.get("Startunterlagenkontrolle"), infos[REG_STARTPASS], null, "right,fill");
        dfb.add("    " + I18n.get("Zielrichterentscheide"), infos[ZRENTSCHEID], null, "right,fill");

        dfb.add(I18n.get("Times"), (Component) null);
        dfb.add("    " + I18n.get("InputComplete"), infos[INPUT_COMPLETE_TIMES], null, "right,fill");
        if (wk.isDLRGBased()) {
            dfb.add("    " + I18n.get("HighTimes"), infos[INPUT_HIGH_TIMES], null, "right,fill");
            dfb.add("    " + I18n.get("LowTimes"), infos[INPUT_LOW_TIMES], null, "right,fill");

        }
        dfb.add(I18n.get("ZWPoints"), (Component) null);
        dfb.add("    " + I18n.get("InputComplete"), infos[INPUT_HLW_COMPLETE], null, "right,fill");
        dfb.add("    " + I18n.get("InputExpectedValues"), infos[INPUT_HLW_VALUES], null, "right,fill");

        dfb.addRow();
        dfb.addSeparator(I18n.get("ListChecks"));
        dfb.add(I18n.get("Laufliste"), (Component) null);
        dfb.add("    " + I18n.get("AllSwimmersIncluded"), infos[LISTS_HEATS_ALL], null, "right,fill");
        dfb.add("    " + I18n.get("DisciplinesSeparated"), infos[LISTS_HEATS_MIXED], null, "right,fill");
        dfb.add(I18n.get("ZWList"), (Component) null);
        dfb.add("    " + I18n.get("AllSwimmersIncluded"), infos[LISTS_HLW_ALL], null, "right,fill");

        dfb.addRow();
        dfb.addSeparator(I18n.get("RefereeChecks"));
        dfb.add(I18n.get("Referees"), infos[REF_INPUT_COMPLETE], null, "right,fill");
        dfb.add(I18n.get("Refereelevels"), infos[REF_INPUT_LEVEL], null, "right,fill");
        dfb.add(I18n.get("AmountOfReferees"), infos[REF_AMOUNT], null, "right,fill");

        dfb.addRow();
        dfb.addSeparator(I18n.get("OptionalChecks"));
        if (wk instanceof EinzelWettkampf) {
            dfb.add(I18n.get("YearOfBirth"), infos[OPT_YEAR_OF_BIRTH], null, "right,fill");
        } else {
            dfb.add(I18n.get("Teammembers"), infos[OPT_TEAMMEMBERS], null, "right,fill");
        }

        dfb.getPanel();
    }

    @SuppressWarnings({})
    synchronized <T extends ASchwimmer> void updateData() {
        try {
            AWettkampf<T> wk = core.getWettkampf();

            // Check Time Input
            state[INPUT_COMPLETE_TIMES] = wk.isDisciplinesComplete();
            text[INPUT_COMPLETE_TIMES] = wk.getToDisciplineComplete() + 2;
            if (state[INPUT_COMPLETE_TIMES]) {
                text[INPUT_COMPLETE_TIMES] = 0;
            }

            // Check High and low times
            if (wk.isDLRGBased()) {
                boolean low = true;
                boolean high = true;

                int amountL = 0;
                int amountH = 0;

                LinkedList<T> swimmers = wk.getSchwimmer();
                ListIterator<T> li = swimmers.listIterator();
                while (li.hasNext()) {
                    T s = li.next();
                    for (int x = 0; x < s.getAK().getDiszAnzahl(); x++) {
                        int zeit = s.getZeit(x);
                        double rec = s.getAK().getDisziplin(x, s.isMaennlich()).getRec();
                        if (zeit > 0) {
                            if (rec > zeit) {
                                low = false;
                                amountL++;
                            }
                            if (3 * rec < zeit) {
                                high = false;
                                amountH++;
                            }
                        }
                    }
                }
                state[INPUT_LOW_TIMES] = low;
                text[INPUT_LOW_TIMES] = amountL;
                state[INPUT_HIGH_TIMES] = high;
                text[INPUT_HIGH_TIMES] = amountH;
            }

            {
                // Check HLW Input
                // Check Names
                // Check Discipline selection
                // Check Startunterlagenkontrolle
                int hlw = 0;
                int values = 0;
                int startpass = 0;

                Hashtable<String, String> tableN = new Hashtable<String, String>();
                int names = 0;
                int disciplines = 0;

                LinkedList<T> swimmers = wk.getSchwimmer();
                ListIterator<T> li = swimmers.listIterator();
                while (li.hasNext()) {
                    T s = li.next();
                    if (!s.hasHLWSet()) {
                        hlw++;
                    }
                    long punkte = Math.round(s.getHLWPunkte() * 100);
                    if ((punkte % 20000 != 0) || (punkte > 1L * s.getMinMembers() * 20000)) {
                        values++;
                    }

                    String key = s.getName() + "#" + s.getAKNummer() + "#" + s.isMaennlich();
                    if (tableN.get(key) != null) {
                        names++;
                    } else {
                        tableN.put(key, key);
                    }

                    if (!s.isDisciplineChoiceValid()) {
                        disciplines++;
                    }

                    if (s.getStartunterlagen() == Startunterlagen.PRUEFEN) {
                        startpass++;
                    }
                }
                state[INPUT_HLW_COMPLETE] = (hlw == 0);
                text[INPUT_HLW_COMPLETE] = hlw;
                state[INPUT_HLW_VALUES] = (values == 0);
                text[INPUT_HLW_VALUES] = values;

                state[REG_NAMES] = (names == 0);
                text[REG_NAMES] = names;
                state[REG_DISCIPLINES] = (disciplines == 0);
                text[REG_DISCIPLINES] = disciplines;
                state[REG_STARTPASS] = (startpass > 0);
                text[REG_STARTPASS] = swimmers.size() - startpass;
            }

            {
                // Check Heatlist
                boolean mixed = false;
                int amount = 0;

                int anzahl = 0;

                ListIterator<T> swimmers = wk.getSchwimmer().listIterator();
                while (swimmers.hasNext()) {
                    T s = swimmers.next();
                    anzahl += s.getDisciplineChoiceCount();
                }

                LinkedList<Lauf<T>> liste = wk.getLaufliste().getLaufliste();

                if (liste != null) {
                    ListIterator<Lauf<T>> li = liste.listIterator();
                    while (li.hasNext()) {
                        Lauf<T> l = li.next();
                        anzahl -= l.getAnzahl();
                        if (!l.isOnlyOneDiscipline()) {
                            amount++;
                            mixed = true;
                        }
                    }
                }

                state[LISTS_HEATS_ALL] = (anzahl == 0);
                text[LISTS_HEATS_ALL] = anzahl;
                state[LISTS_HEATS_MIXED] = !mixed;
                text[LISTS_HEATS_MIXED] = amount;
            }

            {
                // Check HLW-List
                int anzahl = 0;

                boolean ignoreAk = wk.getBooleanProperty(PropertyConstants.ZW_IGNORE_AK_SWIMMERS);

                ListIterator<T> swimmers = wk.getSchwimmer().listIterator();
                while (swimmers.hasNext()) {
                    T s = swimmers.next();
                    if (s.getAK().hasHLW()) {
                        if ((!ignoreAk) || (!s.isAusserKonkurrenz())) {
                            anzahl += s.getMinMembers();
                        }
                    }
                }

                if (wk.getHLWListe() != null) {
                    for (int x = 0; x < wk.getHLWListe().getLauflistenCount(); x++) {
                        LinkedList<HLWLauf<T>> liste = wk.getHLWListe().getLaufliste(x);

                        if (liste != null) {
                            ListIterator<HLWLauf<T>> li = liste.listIterator();
                            while (li.hasNext()) {
                                Lauf<?> l = li.next();
                                if (!ignoreAk) {
                                    anzahl -= l.getAnzahl();
                                } else {
                                    for (int y = 0; y < l.getBahnen(); y++) {
                                        ASchwimmer s = l.getSchwimmer(y);
                                        if ((s != null) && (!s.isAusserKonkurrenz())) {
                                            anzahl--;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                state[LISTS_HLW_ALL] = (anzahl == 0);
                text[LISTS_HLW_ALL] = anzahl;
            }

            // Check years of birth
            if (wk instanceof EinzelWettkampf) {
                int amount = 0;
                EinzelWettkampf ewk = (EinzelWettkampf) wk;
                ListIterator<Teilnehmer> li = ewk.getSchwimmer().listIterator();
                while (li.hasNext()) {
                    Teilnehmer s = li.next();
                    if (s.getJahrgang() == 0) {
                        amount++;
                    } else if (!s.fitsAgeGroup()) {
                        amount++;
                    }
                }
                state[OPT_YEAR_OF_BIRTH] = (amount == 0);
                text[OPT_YEAR_OF_BIRTH] = amount;
            } else {
                state[OPT_YEAR_OF_BIRTH] = true;
                text[OPT_YEAR_OF_BIRTH] = "";
            }

            // Check teammembers
            if (wk instanceof MannschaftWettkampf) {
                int amount = 0;
                MannschaftWettkampf ewk = (MannschaftWettkampf) wk;
                ListIterator<Mannschaft> li = ewk.getSchwimmer().listIterator();
                while (li.hasNext()) {
                    Mannschaft m = li.next();
                    int a = m.getMannschaftsmitgliederAnzahl();
                    if ((a < m.getMinMembers()) || (a > m.getMaxMembers())) {
                        amount++;
                    }
                }
                state[OPT_TEAMMEMBERS] = (amount == 0);
                text[OPT_TEAMMEMBERS] = amount;
            } else {
                state[OPT_TEAMMEMBERS] = true;
                text[OPT_TEAMMEMBERS] = "";
            }

            {
                // Kampfrichter
                state[REF_INPUT_COMPLETE] = false;
                state[REF_INPUT_LEVEL] = false;
                state[REF_AMOUNT] = false;
                text[REF_INPUT_COMPLETE] = null;
                text[REF_INPUT_LEVEL] = null;
                text[REF_AMOUNT] = null;

                KampfrichterVerwaltung kv = wk.getKampfrichterverwaltung();
                if (kv != null) {
                    int index = 0;
                    switch ((Wettkampfart) wk.getProperty(PropertyConstants.ART_DES_WETTKAMPFS)) {
                    case SonstigerWettkampf:
                    case Freundschaftswettkampf:
                        index = 0;
                        break;
                    case Bezirksmeisterschaften:
                        index = 1;
                        break;
                    case DeutscheMeisterschaften:
                    case Landesmeisterschaften:
                        index = 2;
                        break;
                    }
                    int[] amounts = new int[REFEREES.length];
                    for (int x = 0; x < amounts.length; x++) {
                        amounts[x] = REFEREES_AMOUNT[index][x];
                        switch (index) {
                        default:
                        case 0:
                            if (REFEREES[x].equals("Zeitnehmer")) {
                                amounts[x] = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
                            }
                            break;
                        case 1:
                            if (REFEREES[x].equals("Zeitnehmer")) {
                                amounts[x] = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
                            }
                            if (REFEREES[x].equals("Wenderichter")) {
                                amounts[x] = (wk.getIntegerProperty(PropertyConstants.HEATS_LANES) + 1) / 2;
                            }
                            break;
                        case 2:
                            if (REFEREES[x].equals("Wettkampfleiter")) {
                                amounts[x] = AltersklassenUtils.getUniqueDisciplinesCount(wk);
                            }
                            if (REFEREES[x].equals("Zeitnehmer")) {
                                if (wk.getBooleanProperty(PropertyConstants.ELEKTRONISCHE_ZEITNAHME)) {
                                    amounts[x] = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
                                } else {
                                    amounts[x] = 2 * wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
                                }
                            }
                            if (REFEREES[x].equals("Wenderichter")) {
                                amounts[x] = (wk.getIntegerProperty(PropertyConstants.HEATS_LANES) + 1) / 2;
                            }
                            if (REFEREES[x].equals("Schwimmrichter")) {
                                amounts[x] = (wk.getIntegerProperty(PropertyConstants.HEATS_LANES) + 1) / 2;
                            }
                            break;
                        }
                    }

                    int input = 0;
                    int level = 0;
                    int amount = 0;

                    for (int x = 0; x < kv.getEinheitenCount(); x++) {
                        KampfrichterEinheit ke = kv.getEinheit(x);
                        String[] positionen = ke.getPositionen();
                        for (String pos : positionen) {
                            KampfrichterPosition position = ke.getPosition(pos);
                            Kampfrichter[] karis = ke.getKampfrichter(pos);
                            if ((karis == null) || (karis.length == 0)) {
                                karis = new Kampfrichter[] { new Kampfrichter() };
                            }
                            for (int y = 0; y < REFEREES.length; y++) {
                                if (pos.equals(REFEREES[y]) && (amounts[y] > 0)) {
                                    for (Kampfrichter kari : karis) {
                                        if (kari.getName().trim().length() == 0) {
                                            input++;
                                        } else {
                                            if (!kari.getStufe().isAtLeast(position.getMinimaleStufe())) {
                                                level++;
                                            }
                                        }
                                    }
                                    if (karis.length < amounts[y]) {
                                        amount++;
                                    }
                                    amounts[y] = 0;
                                    break;
                                }
                            }
                        }
                    }

                    state[REF_INPUT_COMPLETE] = (input == 0);
                    text[REF_INPUT_COMPLETE] = input;
                    state[REF_INPUT_LEVEL] = (level == 0);
                    text[REF_INPUT_LEVEL] = level;
                    state[REF_AMOUNT] = (amount == 0);
                    text[REF_AMOUNT] = amount;
                }
            }

            {
                // Zielrichterentscheid
                LinkedList<Zielrichterentscheid<T>>[] zes = ZielrichterentscheidUtils.checkZielrichterentscheide(wk);
                state[ZRENTSCHEID] = (zes[1].size() == 0);
                text[ZRENTSCHEID] = zes[1].size();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String[] REFEREES = new String[] { "Veranstaltungsleiter", "Veranstaltungssprecher",
            "Protokollführer", "Leiter",
            "Schiedsrichter", "Wettkampfleiter", "Starter", "Auswerter", "Zeitnehmerobmann", "Zeitnehmer",
            "Wenderichter", "Schwimmrichter", "Zielrichter" };

    public static final int[][] REFEREES_AMOUNT = new int[][] { { 1, 1, 1, 0, 1, 1, 1, 1, 1, -1, 0, 0, 1 },
            { 1, 1, 1, 0, 1, 1, 1, 1, 1, -1, -1, 2, 3 },
            { 1, 1, 1, 1, 2, -1, 1, 1, 1, -1, -1, -1, 3 } };

    private static Icon getIcon(boolean isError, boolean state) {
        if (!state) {
            if (isError) {
                return IconManager.getSmallIcon("cancel");
            }
            return IconManager.getSmallIcon("warn");
        }
        return IconManager.getSmallIcon("ok");
    }

    synchronized void updateGUI() {
        for (int x = 0; x < state.length; x++) {
            infos[x].setIcon(getIcon(IS_ERROR[x], state[x]));
            if (text[x] != null) {
                String s = I18n.getToolTip(I18N[x], text[x]);
                if ((s == null) || s.equals("null") || (s.length() == 0)) {
                    s = null;
                } else {
                    s = s.trim();
                }
                infos[x].setToolTipText(s);
            } else {
                infos[x].setToolTipText(null);
            }
        }
    }

    void check(ASchwimmer s) {
        // Nothing to do
    }
}