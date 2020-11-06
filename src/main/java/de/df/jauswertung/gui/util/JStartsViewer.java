/*
 * Created on 28.01.2007
 */
package de.df.jauswertung.gui.util;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.UIUtils;
import de.df.jutils.gui.util.WindowUtils;

public class JStartsViewer<T extends ASchwimmer> extends JDialog {

    private static final long serialVersionUID = -3805525839643937074L;

    private T                 schwimmer;
    private AWettkampf<T>     wk;

    public JStartsViewer(JFrame parent, AWettkampf<T> w, T s) {
        super(parent, I18n.get("HeatsViewer"), true);
        schwimmer = s;
        wk = w;

        initGUI();
        pack();
        WindowUtils.addEnterAction(this);
        WindowUtils.addEscapeAction(this);
        WindowUtils.center(this, parent);
        UIStateUtils.uistatemanage(this, "JStartViewer");
        EDTUtils.pack(this);
    }

    private void initGUI() {
        Altersklasse ak = schwimmer.getAK();
        int anzahl = ak.getDiszAnzahl();

        String[] disziplin = new String[anzahl];
        String[] lauf = new String[anzahl];
        String[] bahn = new String[anzahl];

        for (int x = 0; x < disziplin.length; x++) {
            disziplin[x] = ak.getDisziplin(x, schwimmer.isMaennlich()).getName();
            if (schwimmer.isDisciplineChosen(x)) {
                lauf[x] = I18n.get("NichtZugeordnetShort");
                bahn[x] = "";
            } else {
                lauf[x] = "-";
                bahn[x] = "-";
            }
        }

        LinkedList<Lauf<T>> heats = wk.getLaufliste().getLaufliste();
        if (heats == null) {
            // Do Nothing
        } else {
            ListIterator<Lauf<T>> li = heats.listIterator();
            while (li.hasNext()) {
                Lauf<T> l = li.next();
                for (int x = 0; x < l.getBahnen(); x++) {
                    T s = l.getSchwimmer(x);
                    if (s == schwimmer) {
                        int i = l.getDisznummer(x);
                        lauf[i] = l.getName();
                        bahn[i] = "" + (x + 1);
                    }
                }
            }
        }

        JButton close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        // add(new JLabel(schwimmer.getName()), CC.xyw(2, 2, 5, "center,fill"));
        // add(new JLabel(ak.getName() + " "
        // + I18n.geschlechtToString(schwimmer.isMaennlich())), CC.xyw(2,
        // 4, 5, "center,fill"));

        FormLayout layout = new FormLayout(FormLayoutUtils.createLayoutString(3), FormLayoutUtils.createLayoutString(anzahl + 2));

        FormLayoutUtils.setRowGroups(layout, anzahl + 1);

        JPanel bottom = new JPanel(layout);

        int offset = 0;

        bottom.add(new JLabel(I18n.get("Discipline")), CC.xy(2, 2 + offset, "center,center"));
        bottom.add(new JLabel(I18n.get("Heat")), CC.xy(4, 2 + offset, "center,center"));
        bottom.add(new JLabel(I18n.get("Lane")), CC.xy(6, 2 + offset, "center,center"));

        for (int x = 0; x < anzahl; x++) {
            bottom.add(new JLabel(disziplin[x]), CC.xy(2, 4 + (2 * x) + offset));
            bottom.add(new JLabel(lauf[x]), CC.xy(4, 4 + (2 * x) + offset, "center,fill"));
            bottom.add(new JLabel(bahn[x]), CC.xy(6, 4 + (2 * x) + offset, "center,fill"));
        }

        bottom.add(close, CC.xyw(2, 4 + 2 * anzahl + offset, 5, "right,fill"));

        add(UIUtils.createHeaderPanel(schwimmer.getName(), ak.getName() + " " + I18n.geschlechtToString(schwimmer)), BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean b) {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            w.setEnabled(!b);
        }
        super.setVisible(b);
    }
}