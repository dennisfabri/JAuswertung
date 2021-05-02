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
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.data.HashtableUtils;
import de.df.jutils.data.ListUtils;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.CenterLayout;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.layout.SimpleTableBuilder;
import de.df.jutils.util.StringTools;

public class JOrganizationSimilarityPanel extends JGlassPanel<JPanel> {

    private static final long           serialVersionUID = 8023494074221318513L;

    private CorePlugin                  core;
    private FEditorPlugin               editor;
    private JMissingInputFrame          parent;

    private Hashtable<String, String[]> similarities;

    private JPanel                      panel;

    private boolean                     changed          = false;

    public JOrganizationSimilarityPanel(JMissingInputFrame parent, CorePlugin core, FEditorPlugin editor) {
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
        JLabel info = new JLabel(I18n.get("SimilarityChecksInfo"));
        info.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information"), true));
        getGlassPanel().setLayout(new CenterLayout());
        getGlassPanel().add(info);
        similarities = new Hashtable<String, String[]>();

        setEnabled(false);
    }

    void updateData() {
        similarities.clear();

        AWettkampf<ASchwimmer> wk = core.getWettkampf();
        if (!wk.hasSchwimmer()) {
            return;
        }

        LinkedList<String[]> sim = new LinkedList<String[]>();

        String[] glds = wk.getGliederungen().toArray(new String[0]);
        Arrays.sort(glds);
        for (int x = 0; x < glds.length - 1; x++) {
            for (int y = x + 1; y < glds.length; y++) {
                if (Utils.areSimilar(glds[x], glds[y])) {
                    sim.add(new String[] { glds[x], glds[y] });
                }
            }
        }
        similarities = unite(sim);
    }

    private static Hashtable<String, String[]> unite(LinkedList<String[]> similarities) {
        Hashtable<String, LinkedList<String>> data = new Hashtable<String, LinkedList<String>>();
        for (String[] glds : similarities) {
            LinkedList<String> l = null;
            for (String gld : glds) {
                LinkedList<String> lx = data.get(gld);
                if (lx != null) {
                    if (l == null) {
                        l = lx;
                    } else {
                        ListUtils.addAll(l, lx);
                    }
                }
                if (l != null) {
                    ListUtils.addAll(l, glds);
                }
            }
            if (l == null) {
                l = new LinkedList<String>();
                ListUtils.addAll(l, glds);
            }
            for (String gld : l) {
                data.put(gld, l);
            }
        }
        Hashtable<String, String[]> sim = new Hashtable<String, String[]>();
        for (LinkedList<String> glds : data.values()) {
            String[] a = glds.toArray(new String[glds.size()]);
            Arrays.sort(a);
            sim.put(StringTools.concat("\n", a), a);
        }

        return sim;
    }

    void updateGUI() {
        setEnabled(similarities.size() > 0);
        panel.removeAll();

        if (!isEnabled()) {
            return;
        }

        List<String> ids = HashtableUtils.getKeys(similarities);
        Collections.sort(ids);

        panel.setLayout(new FormLayout(FormLayoutUtils.createGrowingLayoutString(1), FormLayoutUtils.createLayoutString(ids.size())));
        int y = 2;

        for (String id : ids) {
            String[] glds = similarities.get(id);

            StringBuilder name = new StringBuilder();
            name.append(glds[0]);
            for (int x = 1; x < glds.length; x++) {
                name.append(", ");
                name.append(glds[x]);
            }
            // dfb.add(new JLabelSeparator(), true);

            SimpleTableBuilder dfb = new SimpleTableBuilder(new JPanel(), new boolean[] { true, false }, false);
            for (String gld : glds) {
                JButton edit = new JTransparentButton(IconManager.getSmallIcon("edit"));
                edit.addActionListener(new Editor(gld));

                dfb.add(new JLabel(gld));
                dfb.add(edit);
            }

            JPanel p = new JPanel(new BorderLayout());
            // p.setUI(new GradientTaskPaneGroupUI());
            p.setBorder(BorderUtils.createLabeledBorder(name.toString()));
            p.add(dfb.getPanel(), BorderLayout.CENTER);
            // p.setExpanded(false);

            panel.add(p, CC.xy(2, y));
            y += 2;
        }

        // main.getPanel();
    }

    void changeData() {
        changed = true;
    }

    void check(ASchwimmer s) {
        // Nothing to do
    }

    void edit(String gld) {
        // TODO: Check QGld
        LinkedList<ASchwimmer> swimmers = SearchUtils.getSchwimmer(core.getWettkampf(), new String[] { gld }, false);

        boolean result = editor.editOrganization(parent, gld);
        if (result) {
            updateData();
            for (ASchwimmer a : swimmers) {
                parent.check(a);
            }
            parent.updateGUI();
        }
    }

    private class Editor implements ActionListener {

        private final String s;

        public Editor(String s) {
            this.s = s;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            edit(s);
        }
    }
}