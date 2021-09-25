/*
 * Created on 19.02.2006
 */
package de.df.jauswertung.gui.plugins.check;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.TextFileUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.data.HashtableUtils;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.CenterLayout;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.layout.SimpleTableBuilder;
import de.df.jutils.util.TimeMeasurement;

public class JNamesPanel extends JGlassPanel<JPanel> {

    private static final long     serialVersionUID = 8023494074221318513L;

    private static final String[] SURNAMES;
    private static final String[] FIRSTNAMES_MALE;
    private static final String[] FIRSTNAMES_FEMALE;

    static {
        SURNAMES = TextFileUtils.fileToStringArray("names", "Nachnamen.txt", new String[0], true);
        FIRSTNAMES_MALE = TextFileUtils.fileToStringArray("names", "VornamenM.txt", new String[0], true);
        FIRSTNAMES_FEMALE = TextFileUtils.fileToStringArray("names", "VornamenW.txt", new String[0], true);
    }

    private CorePlugin                      core;
    private FEditorPlugin                   editor;
    private JMissingInputFrame              parent;

    private Hashtable<String, Teilnehmer[]> organizations;
    private JPanel                          panel;

    private boolean                         changed = false;

    public JNamesPanel(JMissingInputFrame parent, CorePlugin core, FEditorPlugin editor) {
        super(new JPanel());
        panel = getComponent();
        this.core = core;
        this.editor = editor;
        this.parent = parent;
        init();
    }

    public boolean hasChanged() {
        return changed;
    }

    public void unsetChanged() {
        changed = false;
    }

    private synchronized void init() {
        JLabel info = new JLabel(I18n.get("Information.NamesCheck"));
        info.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information"), true));
        getGlassPanel().setLayout(new CenterLayout());
        getGlassPanel().add(info);
        organizations = new Hashtable<String, Teilnehmer[]>();

        setEnabled(false);
    }

    synchronized void updateData() {
        organizations.clear();

        AWettkampf<Teilnehmer> wk = core.getEinzelWettkampf();
        if (!wk.hasSchwimmer()) {
            return;
        }

        String[] glds = wk.getGliederungenMitQGliederung().toArray(new String[0]);
        Arrays.sort(glds);
        for (String gld : glds) {
            LinkedList<Teilnehmer> swimmers = SearchUtils.getSchwimmer(wk, new String[] { gld }, false);
            if ((swimmers != null) && (!swimmers.isEmpty())) {
                LinkedList<ASchwimmer> result = new LinkedList<ASchwimmer>();
                for (Teilnehmer s : swimmers) {
                    if (!fits(s)) {
                        result.addLast(s);
                    }
                }
                if (!result.isEmpty()) {
                    organizations.put(gld, result.toArray(new Teilnehmer[result.size()]));
                }
            }
        }
    }

    private class NameInfo {
        // result[0][0]
        public boolean SurnameIsSurname = false;
        // result[1][0]
        public boolean SurnameIsFirstname = false;
        // result[0][1]
        public boolean FirstnameIsSurname = false;
        // result[1][1]
        public boolean FirstnameIsFirstname = false;
    }

    private boolean fits(Teilnehmer t) {
        String nachname = t.getNachname().trim().toLowerCase();
        String vorname = t.getVorname().trim().toLowerCase();
        if (vorname.contains(" ")) {
            vorname = vorname.substring(0, vorname.indexOf(" "));
        }
        NameInfo result = new NameInfo();
        {
            for (int x = 0; x < SURNAMES.length; x++) {
                if (SURNAMES[x].equals(nachname)) {
                    result.SurnameIsSurname = true;
                } else if (SURNAMES[x].equals(vorname)) {
                    result.SurnameIsFirstname = true;
                }
            }
        }
        {
            String[] firstnames;
            if (t.isMaennlich()) {
                firstnames = FIRSTNAMES_MALE;
            } else {
                firstnames = FIRSTNAMES_FEMALE;
            }
            for (int x = 0; x < firstnames.length; x++) {
                if (firstnames[x].equals(vorname)) {
                    result.FirstnameIsFirstname = true;
                } else if (firstnames[x].equals(nachname)) {
                    result.FirstnameIsSurname = true;
                }
            }
        }

        // Class 1 Results:
        // Vorname und Nachname gefunden
        if (result.SurnameIsSurname && result.FirstnameIsFirstname) {
            return true;
        }

        // Class 2 Results:
        // Nachname gefunden und Vorname ist kein Nachname
        if (result.SurnameIsSurname && !result.FirstnameIsSurname) {
            System.out.println("Could not identify nametype: " + nachname + ", " + vorname);
            return true;
        }
        // Nachname ist kein Vorname und Vorname gefunden
        if (!result.SurnameIsFirstname && result.FirstnameIsFirstname) {
            System.out.println("Could not identify nametype: " + nachname + ", " + vorname);
            return true;
        }

        // Class 3 Results:
        // Nachname gefunden und Nachname ist kein Vorname
        if (result.SurnameIsSurname && !result.SurnameIsFirstname) {
            System.out.println("Could not identify nametype: " + nachname + ", " + vorname);
            return true;
        }
        // Vorname gefunden und Vorname ist kein Nachname
        if (result.FirstnameIsFirstname && !result.FirstnameIsSurname) {
            System.out.println("Could not identify nametype: " + nachname + ", " + vorname);
            return true;
        }

        // Class 4 Result:
        // Wir wissen nichts
        if (!result.FirstnameIsFirstname && !result.FirstnameIsSurname && !result.SurnameIsFirstname && !result.SurnameIsSurname) {
            System.out.println("Could not identify nametype: " + nachname + ", " + vorname);
            return true;
        }

        // Sonst
        System.out.println("Could not identify nametype: " + nachname + ", " + vorname);
        return false;
    }

    synchronized void updateGUI() {
        setEnabled(organizations.size() > 0);
        panel.removeAll();

        if (!isEnabled()) {
            return;
        }

        List<String> ids = HashtableUtils.getKeys(organizations);
        Collections.sort(ids);

        panel.setLayout(new FormLayout(FormLayoutUtils.createGrowingLayoutString(1), FormLayoutUtils.createLayoutString(ids.size())));
        int y = 2;

        for (String id : ids) {
            Teilnehmer[] glds = organizations.get(id);

            StringBuilder name = new StringBuilder();
            name.append(id);

            SimpleTableBuilder dfb = new SimpleTableBuilder(new JPanel(), new boolean[] { true, false, false }, false);
            for (Teilnehmer gld : glds) {
                JButton edit = new JTransparentButton(IconManager.getSmallIcon("edit"));
                edit.addActionListener(new Editor(gld));

                JButton toggle = new JTransparentButton(IconManager.getSmallIcon("togglenames"));
                toggle.addActionListener(new NameToggler(gld));

                dfb.add(new JLabel(StartnumberFormatManager.format(gld) + " " + gld.getName()));
                dfb.add(toggle);
                dfb.add(edit);
            }

            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderUtils.createLabeledBorder(name.toString()));
            p.add(dfb.getPanel(), BorderLayout.CENTER);

            panel.add(p, CC.xy(2, y));
            y += 2;
        }
    }

    void changeData() {
        changed = true;
    }

    void check(ASchwimmer s) {
        // Nothing to do
    }

    void Update() {
        TimeMeasurement tm = new TimeMeasurement(System.out, 1);
        tm.start("Updating");
        updateData();
        tm.finish("updateData");
        parent.updateGUI();
        tm.finish("updateGUI");
    }

    void edit(Teilnehmer gld) {
        editor.edit(parent, gld, true);

        Update();
    }

    private final class NameToggler implements ActionListener {

        private final Teilnehmer teilnehmer;

        public NameToggler(Teilnehmer t) {
            teilnehmer = t;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            editor.toggleNames(teilnehmer);

            Update();
        }
    }

    private class Editor implements ActionListener {

        private final Teilnehmer s;

        public Editor(Teilnehmer s) {
            this.s = s;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            edit(s);
        }
    }
}