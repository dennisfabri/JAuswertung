/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.gui.window.JOptionsDialog;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public class MOptionenPlugin extends ANullPlugin {

    private static final String       ITEM_STRING = I18n.get("Options");
    private static final String       MENU_STRING = I18n.get("Extras");

    private JOptionsDialog            dialog;
    private JTabbedPane               tabs;

    private LinkedList<OptionsPlugin> plugins;

    private JMenuItem[]               menu;

    public MOptionenPlugin() {
        super();
        plugins = new LinkedList<OptionsPlugin>();
    }

    @Override
    public void setController(IPluginManager c, String newUid) {
        super.setController(c, newUid);
        initGUI();
    }

    /**
     * @param c
     */
    private void initGUI() {
        menu = new JMenuItem[1];
        menu[0] = new JMenuItem(ITEM_STRING);
        menu[0].setToolTipText(I18n.getToolTip("Options"));
        menu[0].addActionListener(new OptionenActionListener());
        menu[0].setEnabled(false);
    }

    private void initDialog() {
        if (dialog == null) {
            LinkedList<OptionsPlugin> plugs = plugins;
            plugins = new LinkedList<OptionsPlugin>();
            tabs = new JTabbedPane();
            ListIterator<OptionsPlugin> li = plugs.listIterator();
            while (li.hasNext()) {
                addOptionsPlugin(li.next());
            }

            dialog = new JOptionsDialog(getController().getWindow(), ITEM_STRING, true, tabs, IconManager.getIconBundle());
            dialog.addOptionsListener(new MainOptionsListener());
            dialog.pack();
            UIStateUtils.uistatemanage(dialog, "OptionsWindow");
            WindowUtils.center(dialog, getController().getWindow());
        }
    }

    public void addOptionsPlugin(OptionsPlugin plugin) {
        if (plugin == null) {
            throw new NullPointerException();
        }
        if (tabs != null) {
            JPanel[] p = plugin.getPanels();
            if ((p == null) || (p.length == 0)) {
                return;
            }
            for (JPanel aP : p) {
                tabs.add(aP);
                tabs.setToolTipTextAt(tabs.getTabCount() - 1, aP.getToolTipText());
            }
        }
        plugins.addLast(plugin);
        menu[0].setEnabled(true);
    }

    public void removeOptionsPlugin(OptionsPlugin plugin) {
        if (plugin == null) {
            throw new NullPointerException();
        }
        if (plugins.remove(plugin)) {
            if (tabs != null) {
                JPanel[] p = plugin.getPanels();
                for (JPanel aP : p) {
                    tabs.remove(aP);
                }
                dialog.pack();
            }
            if (plugins.size() == 0) {
                menu[0].setEnabled(false);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * de.df.jauswertung.gui.beta.plugin.AuswertungPlugIn#getSupportedMenues()
     */
    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(MENU_STRING, 900, menu, 1000) };
    }

    class OptionenActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            displayDialog();
        }
    }

    public void addPanel(JPanel panel) {
        menu[0].setEnabled(true);
        tabs.add(panel);
    }

    public void notifyChange() {
        if (dialog != null) {
            dialog.setChanged(true);
        }
        checkOk();
    }

    void accept() {
        OptionsPlugin[] copy = plugins.toArray(new OptionsPlugin[plugins.size()]);
        for (OptionsPlugin aCopy : copy) {
            try {
                aCopy.apply();
            } catch (Exception t) {
                t.printStackTrace();
                // Nothing to do
            }
        }
    }

    void cancel() {
        ListIterator<OptionsPlugin> li = plugins.listIterator();
        while (li.hasNext()) {
            try {
                li.next().cancel();
            } catch (Exception t) {
                t.printStackTrace();
            }
        }
        dialog.setChanged(true);
    }

    public void checkOk() {
        if (dialog == null) {
            return;
        }
        boolean ok = true;
        for (OptionsPlugin option : plugins) {
            if (!option.isOk()) {
                ok = false;
                break;
            }
        }
        dialog.setOk(ok);
    }

    /**
     * 
     */
    void displayDialog() {
        initDialog();
        Dimension size = dialog.getSize();
        dialog.pack();
        // There are some problems with JFontChooser, which lead to a to small
        // height.
        if (dialog.getHeight() + 50 > size.height) {
            size.height = dialog.getHeight() + 50;
        }
        dialog.setSize(size);
        dialog.setChanged(false);
        dialog.setVisible(true);
    }

    class MainOptionsListener implements JOptionsDialog.OptionsListener {

        @Override
        public void apply() {
            accept();
        }

        @Override
        public void cancel() {
            MOptionenPlugin.this.cancel();
        }
    }

    public static interface OptionsPlugin {

        JPanel[] getPanels();

        void apply();

        void cancel();

        boolean isOk();
    }
}