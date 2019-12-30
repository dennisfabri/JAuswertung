/*
 * Created on 19.02.2006
 */
package de.df.jauswertung.gui.plugins.check;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.*;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.CenterLayout;
import de.df.jutils.gui.layout.SimpleTableBuilder;

public class JMembersInputPanel extends JGlassPanel<JPanel> {

    private enum Error {
        Incomplete, TooYoung, TooOld, WrongSex
    }

    private class TeamContainer {
        Mannschaft team;
        Error      error;

        public TeamContainer(Mannschaft t, Error e) {
            team = t;
            error = e;
        }
    }

    private static final long  serialVersionUID = 8023494074221318513L;

    private CorePlugin         core;
    private FEditorPlugin      editor;
    private JMissingInputFrame parent;

    private TeamContainer[]    swimmers;
    private JButton[]          edit;

    private JPanel             panel;

    private boolean            changed          = false;

    private boolean            strict           = false;

    public JMembersInputPanel(JMissingInputFrame parent, CorePlugin core, FEditorPlugin editor) {
        super(new JPanel());
        panel = getComponent();
        this.core = core;
        this.editor = editor;
        this.parent = parent;

        AWettkampf<?> wk = core.getWettkampf();
        strict = wk.getBooleanProperty(PropertyConstants.NAMENTLICHE_MELDUNG_STRIKT, false);

        init();
    }

    public boolean hasChanged() {
        return changed;
    }

    public void unsetChanged() {
        changed = false;
    }

    private void init() {
        JLabel info = new JLabel(I18n.get("NoMissingMembersInfo"));
        info.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information"), true));
        getGlassPanel().setLayout(new CenterLayout());
        getGlassPanel().add(info);
        swimmers = new TeamContainer[0];
        edit = new JButton[0];

        setEnabled(false);
    }

    void updateData() {
        AWettkampf<?> xwk = core.getWettkampf();
        if (!xwk.hasSchwimmer()) {
            swimmers = new TeamContainer[0];
            return;
        }
        if (!(xwk instanceof MannschaftWettkampf)) {
            swimmers = new TeamContainer[0];
            return;
        }
        MannschaftWettkampf wk = (MannschaftWettkampf) xwk;
        LinkedList<TeamContainer> nomembers = new LinkedList<TeamContainer>();

        LinkedList<Mannschaft> swimmerlist = wk.getSchwimmer();
        ListIterator<Mannschaft> li = swimmerlist.listIterator();
        while (li.hasNext()) {
            Mannschaft s = li.next();
            TeamContainer tc = checkTeam(wk, s);
            if (tc != null) {
                nomembers.add(tc);
            }
        }
        updateSwimmers(nomembers);
    }

    private void updateSwimmers(LinkedList<TeamContainer> nomembers) {
        swimmers = nomembers.toArray(new TeamContainer[nomembers.size()]);
        sortSwimmers();
    }

    private void sortSwimmers() {
        Arrays.sort(swimmers, new Comparator<TeamContainer>() {
            @Override
            public int compare(TeamContainer tc1, TeamContainer tc2) {
                Mannschaft o1 = tc1.team;
                Mannschaft o2 = tc2.team;
                if (o1.getAKNummer() != o2.getAKNummer()) {
                    return o1.getAKNummer() - o2.getAKNummer();
                }
                if (o1.isMaennlich() != o2.isMaennlich()) {
                    return (o1.isMaennlich() ? 1 : -1);
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    void updateGUI() {
        setEnabled(swimmers.length > 0);

        panel.removeAll();
        SimpleTableBuilder dfb = new SimpleTableBuilder(panel, new boolean[] { false, true, true, true, true, false, false }, false);
        dfb.add(new JLabel(I18n.get("StartnumberShort")), "center,center");
        dfb.add(new JLabel(I18n.get("Name")), "center,center");
        dfb.add(new JLabel(I18n.get("Organisation")), "center,center");
        dfb.add(new JLabel(I18n.get("AgeGroup")), "center,center");
        dfb.add(new JLabel(I18n.get("Teammembers")), "center,center");
        dfb.add(new JLabel(I18n.get("Problem")), "center,center");
        dfb.add(new JLabel());

        edit = new JButton[swimmers.length];
        for (int x = 0; x < swimmers.length; x++) {
            TeamContainer tc = swimmers[x];
            Mannschaft s = tc.team;

            String error = "";
            switch (tc.error) {
            case Incomplete:
                error = I18n.get("Incomplete");
                break;
            case TooOld:
                error = I18n.get("TooOld");
                break;
            case TooYoung:
                error = I18n.get("TooYoung");
                break;
            case WrongSex:
                error = I18n.get("WrongSex");
                break;
            }

            edit[x] = new JTransparentButton(IconManager.getSmallIcon("edit"));
            edit[x].addActionListener(new Editor(x));

            dfb.add(new JLabel(StartnumberFormatManager.format(s)));
            dfb.add(new JLabel(s.getName()));
            dfb.add(new JLabel(s.getGliederung()));
            dfb.add(new JLabel(I18n.getAgeGroupAsString(s)));
            dfb.add(new JLabel(s.getMitgliedernamen(", ")));
            dfb.add(new JLabel(error));
            dfb.add(edit[x]);
        }

        dfb.getPanel();
    }

    private TeamContainer checkTeam(MannschaftWettkampf wk, Mannschaft t) {
        int count = t.getMannschaftsmitgliederAnzahl(strict);
        if ((count < t.getMinMembers()) || (count > t.getMaxMembers())) {
            return new TeamContainer(t, Error.Incomplete);
        }

        count = t.getMannschaftsmitgliederAnzahl(true);
        if (count < t.getMinMembers()) {
            // Nicht genug Daten für weitere Berechnungen
            return null;
        }

        int year = wk.getIntegerProperty(PropertyConstants.YEAR_OF_COMPETITION);

        Altersklasse ak = t.getAK();
        int minMembers = ak.getMinMembers();
        int maxMembers = ak.getMaxMembers();
        LinkedList<Integer> ages = new LinkedList<Integer>();
        int male = 0;
        int female = 0;
        for (int x = 0; x < maxMembers; x++) {
            Mannschaftsmitglied mm = t.getMannschaftsmitglied(x);
            if (mm.isComplete()) {
                ages.add(year - mm.getJahrgang());
            }
            switch (mm.getGeschlecht()) {
            case maennlich:
                male++;
                break;
            case weiblich:
                female++;
                break;
            default:
                break;
            }
        }
        if (t.isMaennlich()) {
            if (t.getMannschaftsmitgliederAnzahl() <= female) {
                return new TeamContainer(t, Error.WrongSex);
            }
        } else {
            if (male > 0) {
                return new TeamContainer(t, Error.WrongSex);
            }
        }
        Collections.sort(ages);
        int sumOfAges = 0;
        count = 0;
        for (Integer age : ages) {
            if (count < minMembers) {
                sumOfAges += age;
            }
            if (ak.getMinimumAlter() > 0 && ak.getMinimumAlter() > age) {
                return new TeamContainer(t, Error.TooYoung);
            }
            if (ak.getMaximumAlter() > 0 && ak.getMaximumAlter() < age) {
                return new TeamContainer(t, Error.TooOld);
            }
            count++;
        }
        if (ak.getMinimumAlterInSumme() > 0 && ak.getMinimumAlterInSumme() > sumOfAges) {
            return new TeamContainer(t, Error.TooYoung);
        }
        if (ak.getMaximumAlterInSumme() > 0 && ak.getMaximumAlterInSumme() < sumOfAges) {
            return new TeamContainer(t, Error.TooOld);
        }

        return null;
    }

    void check(ASchwimmer a) {
        if (!(a instanceof Mannschaft)) {
            return;
        }

        Mannschaft s = (Mannschaft) a;

        LinkedList<TeamContainer> tcs = new LinkedList<TeamContainer>();
        for (TeamContainer tc : swimmers) {
            if (tc.team.getStartnummer() != s.getStartnummer()) {
                tcs.add(tc);
            }
        }
        MannschaftWettkampf wk = core.getMannschaftWettkampf();
        TeamContainer tc = checkTeam(wk, s);
        if (tc != null) {
            tcs.add(tc);
        }

        updateSwimmers(tcs);
    }

    void edit(int index) {
        editor.editSchwimmer(parent, swimmers[index].team, false);
        parent.check(swimmers[index].team);
        parent.updateGUI();
    }

    private class Editor implements ActionListener {

        private final int index;

        public Editor(int x) {
            index = x;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            edit(index);
        }
    }
}