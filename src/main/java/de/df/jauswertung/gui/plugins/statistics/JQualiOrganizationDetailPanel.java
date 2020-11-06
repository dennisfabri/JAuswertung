/*
 * Created on 19.02.2006
 */
package de.df.jauswertung.gui.plugins.statistics;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.StatisticsUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.util.UIUtils;

public class JQualiOrganizationDetailPanel extends JGlassPanel<JPanel> {

    private static final long             serialVersionUID = 8023494074221318513L;

    private Hashtable<String, JComponent> presentation     = new Hashtable<String, JComponent>();
    private CardLayout                    cards            = new CardLayout();
    private JPanel                        view             = new JPanel(cards);

    private AWettkampf<ASchwimmer>        wk;

    public JQualiOrganizationDetailPanel(CorePlugin core) {
        super(new JPanel());

        wk = Utils.copy(core.getWettkampf());
        LinkedList<ASchwimmer> g = wk.getSchwimmer();
        ListIterator<ASchwimmer> li = g.listIterator();
        while (li.hasNext()) {
            ASchwimmer t = li.next();
            if (t.getQualifikationsebene().length() > 0) {
                t.setGliederung(t.getQualifikationsebene());
                t.setQualifikationsebene("");
            } else {
                li.remove();
            }
        }

        init();
    }

    private void init() {
        setName(I18n.get("QualiOrganizationDetails"));

        setLayout(new BorderLayout());
        add(createTop(), BorderLayout.NORTH);
        add(view, BorderLayout.CENTER);
    }

    private JPanel createTop() {
        String[] g = StatisticsUtils.getGliederungenWithSwimmers(wk).toArray(new String[0]);

        final JComboBox box = new JComboBox(g);
        if (g.length > 0) {
            box.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    changeSelection(box);
                }
            });
            box.setSelectedIndex(0);
            changeSelection(box);
        } else {
            box.setEnabled(false);
        }

        JPanel p = new JPanel(new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu:grow", "4dlu,fill:default,4dlu"));
        p.add(new JLabel(I18n.get("Organization")), CC.xy(2, 2));
        p.add(box, CC.xy(4, 2));

        return p;
    }

    void changeSelection(JComboBox box) {
        String g = (String) box.getSelectedItem();
        if (g == null) {
            box.setSelectedIndex(0);
            g = (String) box.getItemAt(0);
        }

        JComponent p = null;

        if (!presentation.containsKey(g)) {
            p = StatisticsUtils.generateGliederungStats(wk, g);

            JPanel outer = new JPanel(new BorderLayout());
            outer.setBorder(BorderUtils.createLabeledBorder(g, false, true, true));
            outer.setName(g);
            outer.add(UIUtils.surroundWithScroller(p), BorderLayout.CENTER);

            presentation.put(g, outer);
            view.add(outer, g);

            outer.revalidate();
        }

        cards.show(view, g);
        view.revalidate();
    }
}