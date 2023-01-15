/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.aselection;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LOAD_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_TN;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PROPERTIES_CHANGED;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public abstract class AMSelectionPlugin extends ANullPlugin {

    private static final String MENU = I18n.get("Execute");

    // private String uid = null;
    CorePlugin core = null;
    FEditorPlugin editor = null;

    private JMenuItem[] menu = null;

    @SuppressWarnings("rawtypes")
    private static LinkedList<ISelector> selectors = new LinkedList<ISelector>();

    private final String i18nprefix;
    private final String meldekey;

    private ISelection selection;

    public AMSelectionPlugin(String i18nprefix, String meldekey, ISelection selection) {
        this.i18nprefix = i18nprefix;
        this.meldekey = meldekey;
        this.selection = selection;
        menu = getMenuItems();
        disableButtons();
    }

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);
        core = (CorePlugin) c.getFeature("de.df.jauswertung.core", uid);
        editor = (FEditorPlugin) c.getFeature("de.df.jauswertung.editor", uid);
    }

    public ISelection getSelection() {
        return selection;
    }

    /**
     * 
     */
    private JMenuItem[] getMenuItems() {
        JMenuItem[] items = new JMenuItem[2];

        items[0] = new JMenuItem(I18n.get("New"));
        items[0].setToolTipText(I18n.getToolTip(i18nprefix + ".New"));
        items[0].addActionListener(new NewActionListener());

        items[1] = new JMenuItem(I18n.get("Edit"));
        items[1].setToolTipText(I18n.getToolTip(i18nprefix + ".Edit"));
        items[1].addActionListener(new ShowActionListener());

        JMenuItem[] m = new JMenuItem[1];
        m[0] = new JMenu(I18n.get(i18nprefix + ".Title"));
        m[0].add(items[0]);
        m[0].add(items[1]);

        return m;
    }

    @SuppressWarnings("rawtypes")
    ISelector[] getSelectors() {
        return selectors.toArray(new ISelector[selectors.size()]);
    }

    private void disableButtons() {
        setButtons(false);
    }

    private void setButtons(boolean enabled) {
        menu[0].setEnabled(enabled);
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(MENU, 520, menu, 991) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if ((due.getChangeReason() & REASON_NEW_WK) > 0) {
            disableButtons();
            return;
        }
        long bitmap = REASON_PROPERTIES_CHANGED | REASON_NEW_TN | REASON_LOAD_WK;
        if ((due.getChangeReason() & bitmap) > 0) {
            updateButtons();
        }
    }

    @SuppressWarnings("rawtypes")
    public void addSelector(ISelector sel) {
        if (sel == null) {
            throw new IllegalArgumentException("Selector must not be null.");
        }
        if (selectors.contains(sel)) {
            throw new IllegalStateException("Selector must not be added twice.");
        }
        selectors.addLast(sel);
    }

    void updateButtons() {
        setButtons(core.getWettkampf().hasSchwimmer());
    }

    class ShowActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            edit();
        }

    }

    class NewActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            neu();
        }

    }

    void edit() {
        JSelectionFrame jsf = new JSelectionFrame(getController().getWindow(), this, core, editor, getController(),
                i18nprefix, meldekey);
        ModalFrameUtil.showAsModal(jsf, getController().getWindow());
    }

    void neu() {
        JSelectionWizard jsf = new JSelectionWizard(getController().getWindow(), this, core, getController(),
                i18nprefix, meldekey);
        jsf.start();
        // ModalFrameUtil.showAsModal(jsf, controller.getWindow());
    }
}