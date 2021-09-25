package de.df.jauswertung.gui.plugins.starter;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Eingabe;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

public class JStarterEditor extends JFrame {

    public static void editStarter(JFrame parent, Mannschaft m, Runnable onFinished) {
        JStarterEditor se = new JStarterEditor(m, onFinished);
        WindowUtils.center(se, parent);
        ModalFrameUtil.showAsModal(se, parent);
    }

    private final Mannschaft                 mannschaft;
    private final MannschaftWettkampf        wk;
    private final boolean                    heatBased;

    private final JLabel                     name        = new JLabel();
    private final JLabel                     gliederung  = new JLabel();
    private final JTabbedPane                tabs        = new JTabbedPane();

    private final Runnable                   onFinish;

    private ArrayList<JStarterPerDiscipline> disciplines = new ArrayList<>();

    @SuppressWarnings("rawtypes")
    public JStarterEditor(Mannschaft m, Runnable onFinished) {
        setTitle(I18n.get("EditStarter"));
        setIconImages(IconManager.getTitleImages());

        onFinish = onFinished;

        setMinimumSize(new Dimension(400, 600));

        mannschaft = m;
        wk = (MannschaftWettkampf) (AWettkampf) m.getWettkampf();
        SimpleFormBuilder sfb = new SimpleFormBuilder();
        sfb.add(I18n.get("Name"), name);
        sfb.add(I18n.get("Organization"), gliederung);

        name.setText(mannschaft.getName());
        gliederung.setText(mannschaft.getGliederungMitQGliederung());

        heatBased = m.getWettkampf().isHeatBased();

        if (!heatBased) {
            for (int x = 0; x < m.getAK().getDiszAnzahl(); x++) {
                JStarterPerDiscipline d = new JStarterPerDiscipline(m, x);
                disciplines.add(d);
                tabs.add(d.getTitle(), d);
            }
        } else {
            OWDisziplin<Mannschaft>[] dis = wk.getLauflisteOW().getDisziplinen();
            for (int x = 0; x < mannschaft.getAK().getDiszAnzahl(); x++) {
                if (mannschaft.isDisciplineChosen(x)) {
                    JStarterPerDiscipline d = new JStarterPerDiscipline(m, x);
                    disciplines.add(d);
                    tabs.add(d.getTitle(), d);
                }
            }
            for (OWDisziplin<Mannschaft> dx : dis) {
                if (dx.getSchwimmer() != null && SearchUtils.hasSchwimmer(dx.getSchwimmer(), mannschaft.getStartnummer())) {
                    Eingabe e = mannschaft.getEingabe(dx.Id, true);
                    if (e != null) {
                        JStarterPerDiscipline d = new JStarterPerDiscipline(m, dx);
                        disciplines.add(d);
                        tabs.add(d.getTitle(), d);
                    }
                }
            }
        }

        JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doApply();
                JStarterEditor.this.setVisible(false);
                JStarterEditor.this.dispose();
            }
        });
        JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JStarterEditor.this.setVisible(false);
                JStarterEditor.this.dispose();
            }
        });

        FormLayout fl = new FormLayout("0dlu,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
        fl.setColumnGroup(2, 4);
        JPanel buttons = new JPanel(fl);
        buttons.add(ok, CC.xy(2, 2));
        buttons.add(cancel, CC.xy(4, 2));

        setLayout(new FormLayout(FormLayoutUtils.createGrowingLayoutString(1), "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu"));
        add(sfb.getPanel(), CC.xy(2, 2));
        add(tabs, CC.xy(2, 4));
        add(buttons, CC.xy(2, 6, "right,fill"));
        pack();
        UIStateUtils.uistatemanage(this);
    }

    private void doApply() {
        boolean isOk = false;
        for (JStarterPerDiscipline o : disciplines) {
            isOk |= o.doApply();
        }
        if (isOk && onFinish != null) {
            onFinish.run();
        }
    }
}