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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.data.HashtableUtils;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.CenterLayout;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.layout.SimpleTableBuilder;
import de.df.jutils.util.StringTools;

public class JTeamnamesPanel extends JGlassPanel<JPanel> {

    private static final long               serialVersionUID = 8023494074221318513L;

    private CorePlugin                      core;
    private FEditorPlugin                   editor;
    private JMissingInputFrame              parent;

    private Hashtable<String, ASchwimmer[]> organizations;

    private JPanel                          panel;

    private boolean                         changed          = false;

    public JTeamnamesPanel(JMissingInputFrame parent, CorePlugin core, FEditorPlugin editor) {
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
        JLabel info = new JLabel(I18n.get("Information.TeamnamesChecks"));
        info.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information"), true));
        getGlassPanel().setLayout(new CenterLayout());
        getGlassPanel().add(info);
        organizations = new Hashtable<String, ASchwimmer[]>();

        setEnabled(false);
    }

    void updateData() {
        organizations.clear();

        AWettkampf<ASchwimmer> wk = core.getWettkampf();
        if (!wk.hasSchwimmer()) {
            return;
        }

        String[] glds = wk.getGliederungen().toArray(new String[0]);
        Arrays.sort(glds);
        for (String gld : glds) {
            // TODO: Check QGld
            LinkedList<ASchwimmer> swimmers = SearchUtils.getSchwimmer(wk, new String[] { gld }, false);
            if ((swimmers != null) && (!swimmers.isEmpty())) {
                LinkedList<ASchwimmer> result = new LinkedList<ASchwimmer>();
                for (ASchwimmer s : swimmers) {
                    if (!fits(s.getName(), gld)) {
                        result.addLast(s);
                    }
                }
                if (!result.isEmpty()) {
                    organizations.put(gld, result.toArray(new ASchwimmer[result.size()]));
                }
            }
        }
    }

    private boolean fits(String name, String gld) {
        name = name.trim();
        gld = gld.trim();
        if (name.equals(gld)) {
            return true;
        }
        if (name.indexOf(gld) != 0) {
            return false;
        }
        int index = name.lastIndexOf(" ");
        if ((gld.length() == index) && (StringTools.isInteger(name.substring(index + 1)))) {
            return true;
        }
        return false;
    }

    void updateGUI() {
        setEnabled(organizations.size() > 0);
        panel.removeAll();

        if (!isEnabled()) {
            return;
        }

        LinkedList<String> ids = HashtableUtils.getKeys(organizations);
        Collections.sort(ids);

        panel.setLayout(new FormLayout(FormLayoutUtils.createGrowingLayoutString(1), FormLayoutUtils.createLayoutString(ids.size())));
        int y = 2;

        for (String id : ids) {
            ASchwimmer[] glds = organizations.get(id);

            StringBuffer name = new StringBuffer();
            name.append(id);
            // name.append(glds[0].getName());
            // for (int x = 0; x < glds.length; x++) {
            // name.append(", ");
            // name.append(glds[x].getName());
            // }
            // dfb.add(new JLabelSeparator(), true);

            SimpleTableBuilder dfb = new SimpleTableBuilder(new JPanel(), new boolean[] { true, false }, false);
            for (ASchwimmer gld : glds) {
                JButton edit = new JTransparentButton(IconManager.getSmallIcon("edit"));
                edit.addActionListener(new Editor(gld));

                dfb.add(new JLabel(StartnumberFormatManager.format(gld) + " " + gld.getName()));
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

    void edit(ASchwimmer gld) {
        editor.edit(parent, (Mannschaft) gld, true);

        updateData();
        parent.updateGUI();
    }

    private class Editor implements ActionListener {

        private final ASchwimmer s;

        public Editor(ASchwimmer s) {
            this.s = s;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            edit(s);
        }
    }
}