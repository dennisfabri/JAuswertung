package de.df.jauswertung.gui.veranstaltung;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.Utils;
import de.df.jutils.data.HashtableUtils;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.SimpleListBuilder;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.UIUtils;
import de.df.jutils.gui.util.WindowUtils;

class JSimilarityCheck extends JDialog {

    private final Hashtable<String, LinkedList<String>>[] similar;
    private final String[]                                names;

    @SuppressWarnings("rawtypes")
    public JSimilarityCheck(JFrame parent, AWettkampf[] wks, String[] names) {
        super(parent, I18n.get("Similarities"), ModalityType.APPLICATION_MODAL);
        if (names.length != wks.length) {
            throw new IllegalArgumentException("Arrays must be of equal size.");
        }
        this.names = names;
        similar = check(wks);
        init();
        pack();
        WindowUtils.center(parent);
        UIStateUtils.uistatemanage(this, "SimilaritiesCheck");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Hashtable<String, LinkedList<String>>[] check(AWettkampf[] wks) {
        LinkedList<String>[] glds = new LinkedList[wks.length];
        String[][] gldslong = new String[wks.length][0];
        for (int x = 0; x < wks.length; x++) {
            gldslong[x] = (String[]) wks[x].getGliederungen().toArray(new String[0]);
            glds[x] = wks[x].getGliederungen();
        }

        // Remove organizations that are present in at least two competitions
        for (int x = 0; x < wks.length; x++) {
            for (int y = 0; y < wks.length; y++) {
                if (x != y) {
                    String[] compare = gldslong[x];
                    ListIterator<String> li2 = glds[y].listIterator();
                    while (li2.hasNext()) {
                        String gld2 = li2.next();
                        for (String g : compare) {
                            if (g.equals(gld2)) {
                                li2.remove();
                            }
                        }
                    }
                }
            }
        }

        Hashtable<String, LinkedList<String>>[] similar = new Hashtable[wks.length];
        for (int x = 0; x < wks.length; x++) {
            similar[x] = new Hashtable<String, LinkedList<String>>();
            for (String gld : glds[x]) {
                LinkedList<String> alternatives = new LinkedList<String>();
                for (int y = 0; y < wks.length; y++) {
                    if (x != y) {
                        for (String alt : gldslong[y]) {
                            if (Utils.areSimilar(gld, alt)) {
                                if (!alternatives.contains(alt)) {
                                    alternatives.add(alt);
                                }
                            }
                        }
                    }
                }
                if (!alternatives.isEmpty()) {
                    Collections.sort(alternatives);
                    similar[x].put(gld, alternatives);
                }
            }
        }

        return similar;
    }

    private JComponent createPanel(Hashtable<String, LinkedList<String>> data) {
        if (data.isEmpty()) {
            return null;
        }
        SimpleListBuilder slb = new SimpleListBuilder();
        LinkedList<String> keys = new LinkedList<String>();
        for (String key : HashtableUtils.getKeyIterable(data)) {
            keys.add(key);
        }
        Collections.sort(keys);
        for (String key : keys) {
            LinkedList<String> glds = data.get(key);
            SimpleListBuilder slb2 = new SimpleListBuilder();
            for (String gld : glds) {
                slb2.add(new JLabel(gld));
            }
            JPanel p = slb2.getPanel();
            p.setBorder(BorderUtils.createLabeledBorder(key));
            slb.add(p);
        }
        return slb.getPanel(false);
    }

    private void init() {
        if (similar == null) {
            return;
        }
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        setLayout(layout);

        JTabbedPane tabs = new JTabbedPane();
        for (int x = 0; x < names.length; x++) {
            JComponent c = createPanel(similar[x]);
            if (c != null) {
                tabs.addTab(names[x], UIUtils.surroundWithScroller(c));
            }
        }
        if (tabs.getTabCount() > 0) {
            add(tabs, CC.xy(2, 2));
        } else {
            add(getNoDataPanel(), CC.xy(2, 2, "center,center"));
        }
        JButton close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        add(close, CC.xy(2, 4, "right,fill"));
    }

    private JComponent getNoDataPanel() {
        JLabel text = new JLabel(I18n.get("Information.SoSimilaritiesFound"));
        text.setBorder(BorderUtils.createLabeledBorder(I18n.get("Title.NoSimilarities"), true));
        return text;
    }
}