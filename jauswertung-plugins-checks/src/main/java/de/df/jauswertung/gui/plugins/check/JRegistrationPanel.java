/*
 * Created on 19.02.2006
 */
package de.df.jauswertung.gui.plugins.check;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.CenterLayout;
import de.df.jutils.gui.layout.SimpleTableBuilder;

public class JRegistrationPanel extends JGlassPanel<JPanel> {

    private static final long  serialVersionUID = 8023494074221318513L;

    private CorePlugin         core;
    private FEditorPlugin      editor;
    private JMissingInputFrame parent;

    private ASchwimmer[]       swimmers;
    private JButton[]          edit;

    private JPanel             panel;

    private boolean            changed          = false;

    public JRegistrationPanel(JMissingInputFrame parent, CorePlugin core, FEditorPlugin editor) {
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

    private void init() {
        JLabel info = new JLabel(I18n.get("RegistrationChecksInfo"));
        info.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information"), true));
        getGlassPanel().setLayout(new CenterLayout());
        getGlassPanel().add(info);
        swimmers = new ASchwimmer[0];
        edit = new JButton[0];

        setEnabled(false);
    }

    void updateData() {
        AWettkampf<ASchwimmer> wk = core.getWettkampf();
        if (!wk.hasSchwimmer()) {
            swimmers = new ASchwimmer[0];
            return;
        }

        HashMap<String, LinkedList<ASchwimmer>> table = new HashMap<>();

        LinkedList<ASchwimmer> nomembers = new LinkedList<>();
        {
            for (ASchwimmer s : wk.getSchwimmer()) {
                String key = s.getName() + "#" + s.getAKNummer() + "#" + s.isMaennlich();
                LinkedList<ASchwimmer> ll = null;
                if (table.get(key) != null) {
                    ll = table.get(key);
                } else {
                    ll = new LinkedList<>();
                    table.put(key, ll);
                }
                ll.add(s);

                if (!s.isDisciplineChoiceValid()) {
                    nomembers.add(s);
                }
            }
        }

        for (String key : table.keySet()) {
            LinkedList<ASchwimmer> ll = table.get(key);
            if (ll.size() > 1) {
                for (ASchwimmer s : ll) {
                    if (!nomembers.contains(s)) {
                        nomembers.add(s);
                    }
                }
            }
        }

        updateSwimmers(nomembers);
    }

    private void updateSwimmers(LinkedList<ASchwimmer> nomembers) {
        swimmers = nomembers.toArray(new ASchwimmer[nomembers.size()]);
        sortSwimmers();
    }

    private void sortSwimmers() {
        Arrays.sort(swimmers, new Comparator<ASchwimmer>() {
            @Override
            public int compare(ASchwimmer o1, ASchwimmer o2) {
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
        SimpleTableBuilder dfb = new SimpleTableBuilder(panel, new boolean[] { false, true, true, true, false }, false);
        dfb.add(new JLabel(I18n.get("StartnumberShort")), "center,center");
        dfb.add(new JLabel(I18n.get("Name")), "center,center");
        dfb.add(new JLabel(I18n.get("Organisation")), "center,center");
        dfb.add(new JLabel(I18n.get("AgeGroup")), "center,center");
        dfb.add(new JLabel());

        edit = new JButton[swimmers.length];
        for (int x = 0; x < swimmers.length; x++) {
            ASchwimmer s = swimmers[x];

            edit[x] = new JTransparentButton(IconManager.getSmallIcon("edit"));
            edit[x].addActionListener(new Editor(x));

            dfb.add(new JLabel(StartnumberFormatManager.format(s)));
            dfb.add(new JLabel(s.getName()));
            dfb.add(new JLabel(s.getGliederungMitQGliederung()));
            dfb.add(new JLabel(I18n.getAgeGroupAsString(s)));
            dfb.add(edit[x]);
        }

        dfb.getPanel();
    }

    void changeHLW(int index) {
        changed = true;
    }

    void check(ASchwimmer s) {
        sortSwimmers();

        boolean found = false;
        String key = s.getName() + "#" + s.getAKNummer();

        LinkedList<ASchwimmer> temp = new LinkedList<ASchwimmer>();
        for (ASchwimmer swimmer : swimmers) {
            if (swimmer.equals(s)) {
                return;
            }
            String xkey = swimmer.getName() + "#" + swimmer.getAKNummer();
            if (key.equals(xkey)) {
                found = true;
            }

            temp.addLast(swimmer);
        }

        if (found || (!s.isDisciplineChoiceValid())) {
            temp.addLast(s);
            updateSwimmers(temp);
        }
    }

    void edit(int index) {
        editor.editSchwimmer(parent, swimmers[index], false);
        parent.check(swimmers[index]);
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