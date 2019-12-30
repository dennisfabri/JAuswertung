/*
 * Created on 01.03.2005
 */
package de.df.jauswertung.gui.plugins.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.*;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPluginManager;

class JPenaltyAdditor<T extends ASchwimmer> extends JDialog {

    private interface IPenaltyAdditorStrategy {

        String getDisciplineNr();

        void doOk();

        String getDisciplineName();

    }

    private class PenaltyAdditorByTime implements IPenaltyAdditorStrategy {

        private final int disziplin;

        public PenaltyAdditorByTime(int disz) {
            this.disziplin = disz;
        }

        @Override
        public String getDisciplineNr() {
            return "" + (disziplin + 1);
        }

        @Override
        public void doOk() {
            schwimmer.addStrafe(disziplin, (Strafe) penalty.getSelectedItem());
            controller.sendDataUpdateEvent("SetPenalty", UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_PENALTY, schwimmer, disziplin,
                    null);
        }

        @Override
        public String getDisciplineName() {
            return schwimmer.getAK().getDisziplin(disziplin, true).getName();
        }

    }

    private class PenaltyAdditorByHeat implements IPenaltyAdditorStrategy {

        private final String id;

        public PenaltyAdditorByHeat(String disz) {
            id = disz;
        }

        @Override
        public void doOk() {
            schwimmer.addStrafe(id, (Strafe) penalty.getSelectedItem());
            controller.sendDataUpdateEvent("SetPenalty", UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_PENALTY, schwimmer, id, null);
        }

        @Override
        public String getDisciplineNr() {
            OWDisziplin<T> d = wk.getLauflisteOW().getDisziplin(id);
            return "" + (d.disziplin + 1);
        }

        @Override
        public String getDisciplineName() {
            return I18n.getDisciplineFullName(wk, id);
        }
    }

    private static final long serialVersionUID = 3256719580843227188L;

    IPenaltyAdditorStrategy   strategy         = null;
    final AWettkampf<T>       wk;
    final T                   schwimmer;
    JComboBox<Strafe>         penalty          = null;
    JButton                   ok               = null;
    final IPluginManager      controller;

    final LinkedList<Strafe>  penalties;

    private JPenaltyAdditor(IPluginManager controller, AWettkampf<T> wk, T s, Strafen strafen) {
        super(controller.getWindow(), I18n.get("PenaltyPointsInput"), true);
        this.controller = controller;

        this.wk = wk;
        schwimmer = s;

        penalties = strafen.getStrafenListe();
        ListIterator<Strafe> li = penalties.listIterator();
        while (li.hasNext()) {
            if (li.next().getShortname().length() == 0) {
                li.remove();
            }
        }
    }

    public JPenaltyAdditor(IPluginManager controller, AWettkampf<T> wk, T s, int disz, Strafen strafen) {
        this(controller, wk, s, strafen);

        strategy = new PenaltyAdditorByTime(disz);

        initialize();
        addActions();
    }

    public JPenaltyAdditor(IPluginManager controller, AWettkampf<T> wk, T s, String disz, Strafen strafen) {
        this(controller, wk, s, strafen);

        strategy = new PenaltyAdditorByHeat(disz);

        initialize();
        addActions();
    }

    @Override
    public void setVisible(boolean visible) {
        controller.getWindow().setEnabled(!visible);
        super.setVisible(visible);
    }

    private void addActions() {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = 3257572818995525944L;

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        Action enterAction = new AbstractAction() {
            private static final long serialVersionUID = 3257572818995525944L;

            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "ENTER");
        getRootPane().getActionMap().put("ENTER", enterAction);
    }

    void doOk() {
        strategy.doOk();
        setVisible(false);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderUtils.createSpaceBorder());
        setContentPane(panel);

        ok = new JButton(I18n.get("Ok"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                doOk();
            }
        });

        JButton cancel = new JButton(I18n.get("Cancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
            }
        });

        penalty = new JComboBox<Strafe>(penalties.toArray(new Strafe[penalties.size()]));
        penalty.setRenderer(new PenaltyShortTextListRenderer());

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4, 6 } });
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Input")));
        top.add(new JLabel(I18n.get("Penalty")), CC.xy(2, 2));

        top.add(penalty, CC.xy(4, 2));
        top.add(ok, CC.xy(4, 4));
        top.add(cancel, CC.xy(4, 6));

        layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4, 6, 8, 10 } });
        JPanel bottom = new JPanel(layout);
        bottom.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information")));
        bottom.add(new JLabel(I18n.get("Name")), CC.xy(2, 2));
        bottom.add(new JLabel(I18n.get("Organisation")), CC.xy(2, 4));
        bottom.add(new JLabel(I18n.get("AgeGroup")), CC.xy(2, 6));
        bottom.add(new JLabel(I18n.get("DisciplineNr")), CC.xy(2, 8));
        bottom.add(new JLabel(I18n.get("Discipline")), CC.xy(2, 10));

        bottom.add(new JLabel(schwimmer.getName()), CC.xy(4, 2));
        bottom.add(new JLabel(schwimmer.getGliederung()), CC.xy(4, 4));
        bottom.add(new JLabel(schwimmer.getAK().toString() + " " + I18n.geschlechtToString(schwimmer)), CC.xy(4, 6));
        bottom.add(new JLabel(strategy.getDisciplineNr()), CC.xy(4, 8));
        bottom.add(new JLabel(strategy.getDisciplineName()), CC.xy(4, 10));

        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);

        pack();
        WindowUtils.center(this, controller.getWindow());
        setResizable(false);
        UIStateUtils.uistatemanage(this, "JPenaltyAdditor");
        pack();

        penalty.requestFocus();
    }
}