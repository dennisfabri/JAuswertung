/*
 * Created on 07.11.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

/**
 * @author Dennis Fabri
 * @date 07.11.2004
 */
public class JSelectionDialog {

    public static interface PrintCallBack {
        void print(boolean[][] selected);
    }

    public static final int      AUTO_DISCIPLINES          = -2;
    public static final int      ALL_DISCIPLINES           = -3;

    private static final int     CONSTANTS_MIN             = -3;

    public static final boolean  MODE_AK_SELECTION         = false;
    public static final boolean  MODE_DISCIPLINE_SELECTION = true;

    private JDialog              dialog;
    private JButton              dialogOk;
    private JPanel               dialogPanel;
    private int                  selected                  = 0;
    private ButtonActionListener bal                       = new ButtonActionListener();
    private JCheckBox[][]        buttons                   = new JCheckBox[0][0];
    @SuppressWarnings("rawtypes")
    private AWettkampf           wk;
    private PrintCallBack        pcb;
    private JFrame               parent;
    private JLabel[][]           warning                   = new JLabel[0][0];
    private boolean[][]          selection                 = null;
    private int                  discipline                = ALL_DISCIPLINES;
    private boolean              mode                      = false;
    private double               minpoints                 = -1;

    @SuppressWarnings("rawtypes")
    public JSelectionDialog(JFrame window, AWettkampf wk, PrintCallBack printCallBack, String ok, boolean mode) {
        this.mode = mode;
        this.wk = wk;
        pcb = printCallBack;
        parent = window;
        initDialog(ok);
        addActions();
    }

    private void addActions() {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = 3257572818995525944L;

            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        };
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        dialog.getRootPane().getActionMap().put("ESCAPE", escapeAction);

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        Action enterAction = new AbstractAction() {
            private static final long serialVersionUID = 3257572818995525944L;

            @Override
            public void actionPerformed(ActionEvent e) {
                reallyPrint();
            }
        };
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "ENTER");
        dialog.getRootPane().getActionMap().put("ENTER", enterAction);
    }

    private void initDialog(String ok) {
        String title = "";
        if (mode == MODE_AK_SELECTION) {
            title = I18n.get("AgeGroupSelection");
        } else {
            title = I18n.get("DisciplineSelection");
        }
        dialog = new JDialog(parent, title, true);
        dialog.setResizable(false);

        JButton all = new JButton(I18n.get("SelectAll"));
        all.addActionListener(new AllActionListener());
        JButton none = new JButton(I18n.get("SelectNone"));
        none.addActionListener(new NoneActionListener());

        dialogOk = new JButton(ok, IconManager.getSmallIcon("print"));
        dialogOk.addActionListener(new ReallyPrintActionListener());
        dialogOk.setEnabled(false);
        JButton dialogCancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        dialogCancel.addActionListener(new CancelPrintActionListener());

        FormLayout layout = new FormLayout("default:grow,fill:default,4dlu,fill:default", "fill:default,4dlu,fill:default");
        layout.setColumnGroups(new int[][] { { 2, 4 } });
        layout.setRowGroups(new int[][] { { 1, 3 } });

        JPanel button = new JPanel(layout);
        button.add(all, CC.xy(2, 1));
        button.add(none, CC.xy(4, 1));
        button.add(dialogOk, CC.xy(2, 3));
        button.add(dialogCancel, CC.xy(4, 3));

        dialogPanel = new JPanel();
        // dialogPanel = new FormDebugPanel();

        dialog.setLayout(new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default,4dlu"));
        dialog.add(dialogPanel, CC.xywh(1, 2, 3, 1));
        dialog.add(button, CC.xy(2, 4));

        dialog.pack();
        WindowUtils.center(dialog, parent);
    }

    public void setTitle(String s) {
        dialog.setTitle(s);
    }

    @SuppressWarnings({ "unchecked" })
    private void refreshDialog() {
        boolean printEnabled = false;
        selected = 0;

        dialogPanel.removeAll();

        Regelwerk aks = wk.getRegelwerk();
        int amount = aks.size();

        String width = "4dlu,fill:default,8dlu:grow,center:default," + "4dlu,center:default,8dlu:grow,center:default," + "4dlu,center:default,8dlu:grow";
        FormLayout layout = new FormLayout(width, FormLayoutUtils.createLayoutString(amount + 1));

        dialogPanel.setLayout(layout);

        dialogPanel.add(new JLabel(I18n.geschlechtToString(aks, false)), CC.xyw(4, 2, 3, "center,center"));
        dialogPanel.add(new JLabel(I18n.geschlechtToString(aks, true)), CC.xyw(8, 2, 3, "center,center"));
        layout.setColumnGroups(new int[][] { { 4, 6, 8, 10 } });

        JCheckBox[][] newButtons = new JCheckBox[2][amount];
        JLabel[][] newWarning = new JLabel[2][amount];
        for (int y = 0; y < amount; y++) {
            String text = "";
            if (mode == MODE_AK_SELECTION) {
                text = aks.getAk(y).toString();
            } else {
                text = aks.getAk(y).getDisziplin(0, false).getName();
            }
            dialogPanel.add(new JLabel(text), CC.xy(2, 4 + 2 * y));

            for (int x = 0; x < 2; x++) {
                if ((x < buttons.length) && (y < buttons[x].length)) {
                    newButtons[x][y] = buttons[x][y];
                    newWarning[x][y] = warning[x][y];
                } else {
                    newButtons[x][y] = new JCheckBox();
                    newButtons[x][y].addActionListener(bal);
                    newWarning[x][y] = new JLabel(IconManager.getSmallIcon("warn"));
                    newWarning[x][y].setToolTipText(I18n.get("InputNotComplete"));
                }
                boolean enabled = (SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(y), x == 1).size() > 0);
                if (discipline == ASchwimmer.DISCIPLINE_NUMBER_ZW) {
                    enabled = enabled && wk.getRegelwerk().getAk(y).hasHLW();
                }
                if (enabled && (minpoints > 0)) {
                    enabled = false;
                    JResultTable result = JResultTable.getResultTable(wk, aks.getAk(y), x == 1, true, true, 0);
                    if (result.getRowCount() > 0) {
                        double points = result.getPunkte(0);
                        if (points > 0.005) {
                            enabled = true;
                        }
                    }
                }
                boolean check = checkDiscipline(y, x == 1);
                if (check && enabled) {
                    selected++;
                    printEnabled = true;
                }
                newButtons[x][y].setEnabled(enabled);
                newWarning[x][y].setVisible(enabled && !check);
                newButtons[x][y].setSelected(enabled && check && ((selection == null) || (selection[x][y])));

                dialogPanel.add(newButtons[x][y], CC.xy(4 + (4 * x), 4 + 2 * y));
                dialogPanel.add(newWarning[x][y], CC.xy(6 + (4 * x), 4 + 2 * y));
            }
        }
        buttons = newButtons;
        warning = newWarning;

        dialogOk.setEnabled(printEnabled);

        dialog.pack();
    }

    private boolean checkDiscipline(int ak, boolean male) {
        Altersklasse ag = wk.getRegelwerk().getAk(ak);
        if (discipline == ALL_DISCIPLINES) {
            if (!wk.isToDisciplineComplete(ag.getDiszAnzahl() - 1, ak, male)) {
                return false;
            }
            return wk.isHlwComplete(ak, male);
        }
        if (discipline == AUTO_DISCIPLINES) {
            return wk.isOneDisciplineComplete(ak, male);
        }
        if (discipline >= ag.getDiszAnzahl()) {
            discipline = ag.getDiszAnzahl() - 1;
        }
        return wk.isToDisciplineComplete(discipline, ak, male);
    }

    private boolean firstTime = true;

    public void setVisible(boolean visible) {
        if (visible) {
            refreshDialog();
            dialog.pack();
            if (firstTime) {
                WindowUtils.center(dialog, parent);
                UIStateUtils.uistatemanage(dialog, JSelectionDialog.class.getName());
                dialog.pack();

                firstTime = false;
            }
        }
        dialog.setVisible(visible);
    }

    void selectAll() {
        int s = 0;
        if (buttons != null) {
            for (JCheckBox[] button : buttons) {
                for (int y = 0; y < button.length; y++) {
                    if (button[y].isEnabled()) {
                        s++;
                        button[y].setSelected(true);
                    }
                }
            }
        }
        selected = s;
        dialogOk.setEnabled(s > 0);
    }

    void selectNone() {
        if (buttons != null) {
            for (JCheckBox[] button : buttons) {
                for (int y = 0; y < button.length; y++) {
                    if (button[y].isSelected()) {
                        button[y].setSelected(false);
                    }
                }
            }
        }
        selected = 0;
        dialogOk.setEnabled(false);
    }

    /**
     * @param arg0
     */
    void buttonPressed(ActionEvent arg0) {
        Object o = arg0.getSource();
        if (o instanceof JCheckBox) {
            if (((JCheckBox) o).isSelected()) {
                selected++;
            } else {
                selected--;
            }
        }
        dialogOk.setEnabled(selected > 0);
    }

    public void setMinPoints(double p) {
        minpoints = p;
    }

    /**
     * 
     */
    void reallyPrint() {
        dialog.setVisible(false);
        boolean[][] selectedButtons = new boolean[buttons.length][buttons[0].length];
        for (int x = 0; x < buttons.length; x++) {
            for (int y = 0; y < buttons[x].length; y++) {
                selectedButtons[x][y] = buttons[x][y].isSelected() && buttons[x][y].isEnabled();
            }
        }
        pcb.print(selectedButtons);
    }

    void cancel() {
        dialog.setVisible(false);
    }

    @SuppressWarnings("rawtypes")
    public void setCompetition(AWettkampf w) {
        if (w == null) {
            throw new NullPointerException();
        }
        wk = w;
    }

    public void setSelected(boolean[][] selected) {
        selection = selected;
    }

    public void setDiscipline(int disz) {
        if (disz < CONSTANTS_MIN) {
            throw new IllegalArgumentException("Discipline must not be lower than " + CONSTANTS_MIN + " but was " + disz + ".");
        }
        discipline = disz;
    }

    final class ButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            buttonPressed(arg0);
        }
    }

    final class AllActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            selectAll();
        }

    }

    final class NoneActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            selectNone();
        }

    }

    final class ReallyPrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            reallyPrint();
        }
    }

    final class CancelPrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            cancel();
        }
    }

}
