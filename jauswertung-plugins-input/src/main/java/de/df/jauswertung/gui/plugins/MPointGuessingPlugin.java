/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public class MPointGuessingPlugin extends ANullPlugin {

    private static final String ITEM_STRING = I18n.get("GuessReportedPoints");
    private static final String MENU_STRING = I18n.get("Prepare");

    private JMenuItem           guess       = new JMenuItem(ITEM_STRING);

    IPluginManager              controller;
    CorePlugin                  core        = null;

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        controller = plugincontroller;
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    public MPointGuessingPlugin() {
        guess.setToolTipText(I18n.getToolTip("GuessReportedPoints"));
        guess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                guessPoints();
            }
        });
    }

    JStatusDialog               dialog = null;
    SwingWorker<Object, Object> sw     = null;

    void guessPoints() {
        dialog = new JStatusDialog(controller.getWindow(), core.getWettkampf().getRegelwerk());

        dialog.setValue(0);
        dialog.setMaximumValue(1 + core.getWettkampf().getRegelwerk().size() * 2);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dialog.start();
            }
        });

        if (sw == null) {
            sw = new SwingWorker<Object, Object>() {
                @Override
                @SuppressWarnings({ "unchecked" })
                protected Object doInBackground() throws Exception {
                    while (!dialog.isVisible()) {
                        Thread.sleep(100);
                    }

                    @SuppressWarnings("rawtypes")
                    AWettkampf wk = core.getWettkampf();
                    @SuppressWarnings("rawtypes")
                    AWettkampf copy = Utils.copy(wk);
                    EDTUtils.executeOnEDT(new Runnable() {
                        @Override
                        public void run() {
                            dialog.increaseValue();
                        }
                    });
                    ListIterator<ASchwimmer> li = copy.getSchwimmer().listIterator();
                    while (li.hasNext()) {
                        ASchwimmer s = li.next();
                        for (int x = 0; x < s.getAK().getDiszAnzahl(); x++) {
                            if (s.isDisciplineChosen(x)) {
                                s.setZeit(x, s.getMeldezeit(x));
                            }
                        }
                    }
                    boolean changed = false;
                    for (int x = 0; x < wk.getRegelwerk().size(); x++) {
                        for (int y = 0; y < 2; y++) {
                            EDTUtils.executeOnEDT(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.increaseValue();
                                }
                            });
                            if (SearchUtils.hasSchwimmer(copy, copy.getRegelwerk().getAk(x), y == 1)) {
                                // dialog.setText(copy.getAks().getAk(x)
                                // .getName());
                                JResultTable table = JResultTable.getResultTable(copy, copy.getRegelwerk().getAk(x), y == 1, false, true, 0);
                                for (int z = 0; z < table.getRowCount(); z++) {
                                    ASchwimmer s = SearchUtils.getSchwimmer(wk, table.getResult(z).getSchwimmer());
                                    if ((s != null) && (s.getMeldepunkte(0) < 0.005) && (table.getPunkte(z) > 0.005)) {
                                        s.setMeldepunkte(0, table.getPunkte(z));
                                        EDTUtils.executeOnEDT(new IncreaseNotifier(x, y));
                                        changed = true;
                                    }
                                }
                            }
                        }
                    }
                    if (changed) {
                        EDTUtils.executeOnEDT(new Runnable() {
                            @Override
                            public void run() {
                                dialog.finishing();
                            }
                        });
                        controller.sendDataUpdateEvent("GuessReportedPoints", UpdateEventConstants.REASON_SWIMMER_CHANGED, MPointGuessingPlugin.this);
                    }

                    return null;
                }

                @Override
                protected void done() {
                    // controller.getWindow().setEnabled(true);
                    dialog.finish();
                    sw = null;
                }
            };
        }
        sw.execute();
    }

    /*
     * (non-Javadoc)
     * @see
     * de.df.jauswertung.gui.beta.plugin.AuswertungPlugIn#getSupportedMenues()
     */
    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(MENU_STRING, 510, guess, 92) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        guess.setEnabled(core.getWettkampf().hasSchwimmer());
    }

    private final class IncreaseNotifier implements Runnable {

        private int x;
        private int y;

        public IncreaseNotifier(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void run() {
            dialog.increase(x, y == 1);
        }
    }

    private static class JStatusDialog extends JDialog {

        private static final long serialVersionUID = -6267515594453985792L;

        private JLabel[][]        stati;
        private int[][]           values;
        private JButton           ok;
        private JProgressBar      progress;

        private JFrame            parent;

        public JStatusDialog(JFrame parent, Regelwerk aks) {
            super(parent, I18n.get("GuessReportedPoints"), true);

            setResizable(false);

            WindowUtils.addEscapeAction(this);
            WindowUtils.addEnterAction(this);

            this.parent = parent;

            int anzahl = aks.size();
            stati = new JLabel[anzahl][2];
            values = new int[anzahl][2];

            ok = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });

            progress = new JProgressBar(0, 10);

            FormLayout layout = new FormLayout("4dlu,0dlu:grow,fill:default,20dlu,center:default,8dlu,center:default,0dlu:grow,4dlu",
                    FormLayoutUtils.createLayoutString(anzahl + 2));
            layout.setColumnGroups(new int[][] { { 5, 7 } });
            JPanel p = new JPanel(layout);

            p.add(new JLabel(I18n.geschlechtToStringSubject(aks, false)), CC.xy(5, 2));
            p.add(new JLabel(I18n.geschlechtToStringSubject(aks, true)), CC.xy(7, 2));
            for (int x = 0; x < anzahl; x++) {
                stati[x][0] = new JLabel("0");
                stati[x][1] = new JLabel("0");

                p.add(new JLabel(aks.getAk(x).getName()), CC.xy(3, 2 * x + 4));
                p.add(stati[x][0], CC.xy(5, 2 * x + 4));
                p.add(stati[x][1], CC.xy(7, 2 * x + 4));
            }

            p.add(ok, CC.xyw(2, 4 + 2 * stati.length, 7, "right,center"));
            p.add(progress, CC.xyw(2, 4 + 2 * stati.length, 7, "fill,center"));

            add(UIUtils.createHeaderPanel(I18n.get("GuessedPoints.Information"), I18n.get("GuessedPoints.Note")), BorderLayout.NORTH);
            add(p, BorderLayout.CENTER);

            pack();
            WindowUtils.center(this, parent);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    setVisible(false);
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    setVisible(false);
                }
            });
        }

        @Override
        public void setVisible(boolean b) {
            parent.setEnabled(!b);
            super.setVisible(b);
        }

        public void start() {
            setEnabled(false);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            ok.setVisible(false);
            progress.setVisible(true);
            progress.setValue(0);

            setVisible(true);
        }

        public void finishing() {
            progress.setIndeterminate(true);
        }

        public void setValue(int i) {
            progress.setValue(i);
        }

        public void setMaximumValue(int max) {
            progress.setMaximum(max);
        }

        public void increaseValue() {
            progress.setValue(progress.getValue() + 1);
        }

        public void finish() {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            setEnabled(true);
            progress.setVisible(false);
            ok.setVisible(true);
        }

        public void increase(int ak, boolean male) {
            values[ak][male ? 1 : 0]++;
            stati[ak][male ? 1 : 0].setText("" + values[ak][male ? 1 : 0]);
        }
    }
}