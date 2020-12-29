/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.zw;

import static de.df.jauswertung.daten.PropertyConstants.ZW_CHANGE_BLOCKED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_AKS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LOAD_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_TN;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PROPERTIES_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_DELETED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_ZW_LIST_CHANGED;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.HLWListe;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.PluginUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public class MZWPlugin extends ANullPlugin {

    private static final String ITEM_NEW   = I18n.get("New");
    private static final String ITEM_EDIT  = I18n.get("Edit");
    private static final String ITEM_SHOW  = I18n.get("Show");
    private static final String ITEM_BLOCK = I18n.get("Lock");
    private static final String ITEM_MENU  = I18n.get("ZWList");
    private static final String MENU       = I18n.get("Edit");
    private static final String HLW_LIST_LOST;
    private static final String HLW_LIST_LOST_NOTE;

    static {
        HLW_LIST_LOST = I18n.get("ZWListWillBeLostContinue");
        HLW_LIST_LOST_NOTE = I18n.get("ZWListWillBeLostContinue.Note");
    }

    CorePlugin          core    = null;

    private JMenuItem[] menu    = null;
    JButton[]           buttons = null;

    private JMenuItem   neu     = null;
    private JMenuItem   show    = null;
    private JMenuItem   edit    = null;
    JMenuItem           block   = null;

    public MZWPlugin() {
        menu = getMenuItems();
        buttons = getButtons();
        disableButtons();
    }

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);
        core = (CorePlugin) c.getFeature("de.df.jauswertung.core", uid);
    }

    private JButton[] getButtons() {
        JButton[] buttonarray = new JButton[0];
        // buttonarray[0] = new JButton(IconManager.getSmallIcon("zw"));
        // buttonarray[0].setToolTipText(I18n.getToolTip("EditZWList"));
        // buttonarray[0].addActionListener(new EditShowActionListener());
        return buttonarray;
    }

    /**
     * 
     */
    private JMenuItem[] getMenuItems() {
        // Main
        neu = new JMenuItem(ITEM_NEW, IconManager.getSmallIcon("new"));
        neu.setToolTipText(I18n.getToolTip("NewZWList"));
        neu.addActionListener(new NewActionListener());
        edit = new JMenuItem(ITEM_EDIT, IconManager.getSmallIcon("edit"));
        edit.setToolTipText(I18n.getToolTip("EditZWList"));
        edit.addActionListener(new EditShowActionListener());
        show = new JMenuItem(ITEM_SHOW);
        show.setToolTipText(I18n.getToolTip("ShowZWList"));
        show.addActionListener(new ShowActionListener());
        block = new JCheckBoxMenuItem(ITEM_BLOCK);
        block.setToolTipText(I18n.getToolTip("LockZWList"));
        block.addActionListener(new BlockActionListener());

        JMenuItem[] hlwMenu = new JMenuItem[1];
        hlwMenu[0] = new JMenu(ITEM_MENU);
        hlwMenu[0].setIcon(IconManager.getSmallIcon("zw"));
        hlwMenu[0].add(neu);
        hlwMenu[0].add(new JSeparator());
        hlwMenu[0].add(edit);
        hlwMenu[0].add(show);
        hlwMenu[0].add(new JSeparator());
        hlwMenu[0].add(block);

        return hlwMenu;
    }

    private void disableButtons() {
        setButtons(false, false);
    }

    private void setButtons(boolean notEmpty, boolean enabled) {
        boolean nb = !block.isSelected();
        if (!nb && !enabled) {
            // Empty competitions cannot be blocked
            nb = true;
        }
        boolean on = (notEmpty && nb) || enabled;
        for (int x = 0; x < menu.length; x++) {
            menu[x].setEnabled(on);
        }
        for (int x = 0; x < buttons.length; x++) {
            buttons[x].setEnabled(enabled);
        }
        neu.setEnabled(nb && notEmpty);
        block.setEnabled(enabled);
        show.setEnabled(enabled);
        edit.setEnabled(enabled && nb);
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(MENU, 500, menu, 110) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if ((due.getChangeReason() & REASON_NEW_WK) > 0) {
            disableButtons();
            block.setSelected(false);
            return;
        }
        if ((due.getChangeReason() & REASON_LOAD_WK) > 0) {
            @SuppressWarnings("rawtypes")
            AWettkampf wk = core.getWettkampf();
            block.setSelected(wk.getBooleanProperty(ZW_CHANGE_BLOCKED));
            updateButtons();
            return;
        }
        long bitmap = REASON_ZW_LIST_CHANGED | REASON_PROPERTIES_CHANGED | REASON_NEW_TN | REASON_AKS_CHANGED | REASON_SWIMMER_CHANGED | REASON_SWIMMER_DELETED;
        if ((due.getChangeReason() & bitmap) > 0) {
            updateButtons();
        }
    }

    void updateButtons() {
        setButtons(core.getWettkampf().hasHLW(), !core.getWettkampf().getHLWListe().isEmpty());
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        return new ButtonInfo[] { new ButtonInfo(buttons, 350) };
    }

    class EditShowActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            edit(true);
        }
    }

    class ShowActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            edit(false);
        }

    }

    class NewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            neu();
        }

    }

    class BlockActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            doBlock();
        }
    }

    void doBlock() {
        core.getWettkampf().setProperty(ZW_CHANGE_BLOCKED, block.isSelected());
        if (block.isSelected()) {
            buttons[0].setToolTipText(I18n.getToolTip("ShowZWList"));
        } else {
            buttons[0].setToolTipText(I18n.getToolTip("EditZWList"));
        }

        getController().sendDataUpdateEvent("ChangeProperties", UpdateEventConstants.REASON_PROPERTIES_CHANGED, MZWPlugin.this);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void edit(boolean editable) {
        boolean blocked = (!editable) || block.isSelected();
        ISimpleCallback<JHlwlisteBearbeiten<ASchwimmer>> sc = null;
        if (!blocked) {
            sc = new ISimpleCallback<JHlwlisteBearbeiten<ASchwimmer>>() {
                @Override
                public void callback(JHlwlisteBearbeiten<ASchwimmer> t) {
                    editCallback(t);
                }
            };
        }
        ModalFrameUtil.showAsModal(new JHlwlisteBearbeiten(getController().getWindow(), core.getWettkampf(), !blocked, sc), getController().getWindow());
    }

    @SuppressWarnings("rawtypes")
    void neu() {
        boolean ok = true;
        AWettkampf wk = core.getWettkampf();
        if (!wk.getHLWListe().isEmpty()) {
            if (!DialogUtils.askAndWarn(getController().getWindow(), HLW_LIST_LOST, HLW_LIST_LOST_NOTE)) {
                ok = false;
            }
        }
        if (ok) {
            new JNewZWWizard(getController().getWindow(), this, core).start();
        }
    }

    void erzeugeHlwliste() {
        PluginUtils.erzeugeHlwliste(getController(), this, core.getWettkampf());
    }

    void erzeugeHlwliste(HLWListe.Einteilung[] einteilung) {
        PluginUtils.erzeugeHlwliste(getController(), this, core.getWettkampf(), einteilung);
    }

    void editCallback(JHlwlisteBearbeiten<ASchwimmer> t) {
        if (t.isChanged()) {
            getController().sendDataUpdateEvent("ChangeZW", UpdateEventConstants.REASON_ZW_LIST_CHANGED, MZWPlugin.this);
        }
    }
}