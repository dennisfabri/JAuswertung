/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.heats;

import static de.df.jauswertung.daten.PropertyConstants.HEAT_CHANGE_BLOCKED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_AKS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LAUF_LIST_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LOAD_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_TN;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PROPERTIES_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_DELETED;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public final class MLauflistePlugin extends ANullPlugin {

    private static final String ITEM_NEW = I18n.get("New");
    private static final String ITEM_EDIT = I18n.get("Edit");
    private static final String ITEM_SHOW = I18n.get("Show");
    private static final String ITEM_BLOCK = I18n.get("Lock");
    private static final String ITEM_MENU = I18n.get("Laufliste");
    private static final String MENU = I18n.get("Prepare");
    private static final String ATTENTION = I18n.get("Attention");
    private static final String LAUF_LIST_LOST;
    private static final String LAUF_LIST_LOST_NOTE;

    static {
        LAUF_LIST_LOST = I18n.get("LaufListWillBeLostContinue");
        LAUF_LIST_LOST_NOTE = I18n.get("LaufListWillBeLostContinue.Note");
    }

    CorePlugin core = null;
    private JMenuItem[] menu = null;
    JButton[] buttons = null;

    private JMenuItem neu = null;
    private JMenuItem show = null;
    private JMenuItem edit = null;
    JMenuItem block = null;

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);
        core = (CorePlugin) c.getFeature("de.df.jauswertung.core", uid);

        initMenues();
        initButtons();
        disableButtons();
    }

    private void initButtons() {
        buttons = new JButton[1];
        buttons[0] = new JButton(IconManager.getSmallIcon("laufliste"));
        buttons[0].setToolTipText(I18n.getToolTip("EditHeatlist"));
        buttons[0].addActionListener(new EditShowActionListener());
    }

    /**
     * 
     */
    private void initMenues() {
        // Main
        neu = new JMenuItem(ITEM_NEW, IconManager.getSmallIcon("new"));
        neu.setToolTipText(I18n.getToolTip("NewHeatlist"));
        neu.addActionListener(new NewActionListener());
        edit = new JMenuItem(ITEM_EDIT, IconManager.getSmallIcon("edit"));
        edit.setToolTipText(I18n.getToolTip("EditHeatlist"));
        edit.addActionListener(new EditShowActionListener());
        show = new JMenuItem(ITEM_SHOW);
        show.setToolTipText(I18n.getToolTip("ShowHeatlist"));
        show.addActionListener(new ShowActionListener());
        block = new JCheckBoxMenuItem(ITEM_BLOCK);
        block.setToolTipText(I18n.getToolTip("LockHeatlist"));
        block.addActionListener(new BlockActionListener());

        menu = new JMenuItem[1];
        menu[0] = new JMenu(ITEM_MENU);
        menu[0].setIcon(IconManager.getSmallIcon("laufliste"));
        menu[0].add(neu);
        menu[0].add(new JSeparator());
        menu[0].add(edit);
        menu[0].add(show);
        menu[0].add(new JSeparator());
        menu[0].add(block);
    }

    private void disableButtons() {
        setButtons(false, false, true);
    }

    private void setButtons(boolean notEmpty, boolean enabled, boolean visible) {
        if (!visible) {
            menu[0].setVisible(false);
            buttons[0].setEnabled(false);
            return;
        }

        menu[0].setVisible(true);
        buttons[0].setVisible(true);

        boolean nb = !block.isSelected();
        if (!nb && !enabled) {
            // Empty competitions cannot be blocked
            nb = true;
        }
        boolean on = (notEmpty && nb) || enabled;
        menu[0].setEnabled(on);
        buttons[0].setEnabled(enabled);
        neu.setEnabled(nb && notEmpty);
        block.setEnabled(enabled);
        show.setEnabled(enabled);
        edit.setEnabled(enabled && nb);
    }

    void updateButtons() {
        AWettkampf<?> wk = core.getWettkampf();
        if (wk.isHeatBased()) {
            setButtons(true, false, false);
        } else {
            setButtons(wk.hasSchwimmer(),
                    (wk.getLaufliste().getLaufliste() != null) && (wk.getLaufliste().getLaufliste().size() > 0), true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.beta.plugin.AuswertungPlugIn#getSupportedMenues()
     */
    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(MENU, 510, menu, 100) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if ((due.getChangeReason() & REASON_NEW_WK) > 0) {
            block.setSelected(false);
            updateButtons();
            return;
        }
        if ((due.getChangeReason() & REASON_LOAD_WK) > 0) {
            @SuppressWarnings("rawtypes")
            AWettkampf wk = core.getWettkampf();
            block.setSelected(wk.getBooleanProperty(HEAT_CHANGE_BLOCKED));
            updateButtons();
            return;
        }
        long bitmap = REASON_LAUF_LIST_CHANGED | REASON_PROPERTIES_CHANGED | REASON_AKS_CHANGED | REASON_NEW_TN
                | REASON_SWIMMER_CHANGED
                | REASON_SWIMMER_DELETED;
        if ((due.getChangeReason() & bitmap) > 0) {
            updateButtons();
        }
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        return new ButtonInfo[] { new ButtonInfo(buttons, 300) };
    }

    class EditShowActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            editLaufliste();
        }
    }

    class ShowActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            zeigeLaufliste();
        }
    }

    class NewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            neueLaufliste();
        }
    }

    class BlockActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            core.getWettkampf().setProperty(HEAT_CHANGE_BLOCKED, block.isSelected());
            if (block.isSelected()) {
                buttons[0].setToolTipText(I18n.getToolTip("ShowHeatlist"));
            } else {
                buttons[0].setToolTipText(I18n.getToolTip("EditHeatlist"));
            }
            EditHeatlistUtils.notifyPropertyChanged(getController(), MLauflistePlugin.this);
        }
    }

    void editLaufliste() {
        if (block.isSelected()) {
            zeigeLaufliste();
        } else {
            bearbeiteLaufliste();
        }
    }

    void zeigeLaufliste() {
        EditHeatlistUtils.laufliste(getController(), this, core.getWettkampf(), false, null);
    }

    void bearbeiteLaufliste() {
        boolean editable = !block.isSelected();
        EditHeatlistUtils.laufliste(getController(), this, core.getWettkampf(), editable, null);
    }

    @SuppressWarnings("rawtypes")
    void neueLaufliste() {
        boolean ok = true;

        AWettkampf wk = core.getWettkampf();
        if ((wk.getLaufliste().getLaufliste() != null) && (wk.getLaufliste().getLaufliste().size() > 0)) {
            ok = DialogUtils.askAndWarn(getController().getWindow(), ATTENTION, LAUF_LIST_LOST, LAUF_LIST_LOST_NOTE);
        }
        if (ok) {
            JNewHeatsWizard jnhw = new JNewHeatsWizard(getController().getWindow(), this, core);
            jnhw.start();
        }
    }

}