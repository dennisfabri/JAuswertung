/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.importexport;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.bugreport.BugreportPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.Utils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis Fabri @date 05.04.2004
 */
public class MImportExportPlugin extends ANullPlugin {

    private static final String ITEM_IMPORT = I18n.get("ImportDots");
    private static final String ITEM_EXPORT = I18n.get("ExportDots");
    private static final String ITEM_AUTO = I18n.get("AutomaticSave");
    private static final String MENU = I18n.get("File");

    private JImportWizard importWizard = null;
    private JExportWizard exportWizard = null;

    CorePlugin core = null;
    BugreportPlugin bugreport = null;
    long click = System.currentTimeMillis();
    boolean run = true;

    JMenuItem[] menu = null;

    private final ImportExportMode mode;

    public MImportExportPlugin(ImportExportMode mode) {
        this.mode = mode;
    }

    public MImportExportPlugin() {
        this(ImportExportMode.Normal);
    }

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);
        core = (CorePlugin) c.getFeature("de.df.jauswertung.core", uid);
        if (core == null) {
            throw new NullPointerException("CorePlugin must not be null!");
        }
        bugreport = (BugreportPlugin) c.getFeature("de.df.jauswertung.bugreport", uid);
        if (bugreport == null) {
            throw new NullPointerException("BugreportPlugin must not be null!");
        }

        initMenues();
        menu[0].setToolTipText(I18n.getToolTip("Import"));
        menu[1].setToolTipText(I18n.getToolTip("Export"));
        menu[2].setEnabled(false);
        menu[2].setToolTipText(I18n.getToolTip("Autosave"));

        menu[0].setAccelerator(I18n.getKeyStroke("import"));
        menu[1].setAccelerator(I18n.getKeyStroke("export"));

        if (mode == ImportExportMode.Teammembers) {
            menu[2].setVisible(false);
        }

        new AutomaticSave().start();
    }

    synchronized JImportWizard getImportWizard() {
        if (importWizard == null) {
            importWizard = new JImportWizard(getController().getWindow(), core, getController(), mode);
        }
        return importWizard;
    }

    synchronized JExportWizard getExportWizard() {
        if (exportWizard == null) {
            exportWizard = new JExportWizard(getController().getWindow(), core, bugreport, mode,
                    getSelectedOrganisation());
        }
        return exportWizard;
    }

    protected String getSelectedOrganisation() {
        return null;
    }

    /**
     * 
     */
    private void initMenues() {
        // Main
        menu = new JMenuItem[3];
        menu[0] = new JMenuItem(ITEM_IMPORT);
        menu[0].addActionListener(arg0 -> {
            getImportWizard().start();
        });
        menu[1] = new JMenuItem(ITEM_EXPORT);
        menu[1].addActionListener(arg0 -> {
            getExportWizard().start();
        });
        menu[2] = new JCheckBoxMenuItem(ITEM_AUTO);
        menu[2].addActionListener(arg0 -> {
            Utils.getPreferences().putBoolean("AutomaticSave", menu[2].isSelected());
            click = System.currentTimeMillis();
        });
        menu[2].setSelected(Utils.getPreferences().getBoolean("AutomaticSave", true));
        click = System.currentTimeMillis();
    }

    synchronized void autosave() {
        if (menu[2].isSelected()) {
            String name = core.getFilename();
            if (name == null) {
                return;
            }
            if (!(name.toLowerCase().endsWith(".wk"))) {
                return;
            }
            String pre = name.substring(0, name.length() - 3);
            String suffix = name.substring(name.length() - 3);
            String filename = pre + "-autosave" + suffix;
            core.saveCopyAs(filename);
        }
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(MENU, 100, menu, 200) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if ((due.getChangeReason() & UpdateEventConstants.REASON_FILENAME_CHANGED) > 0) {
            if (core.getFilename() == null) {
                menu[2].setEnabled(false);
                // menu[2].setSelected(false);
            } else {
                menu[2].setEnabled(true);
            }
        }
    }

    private class AutomaticSave extends Thread {

        // 5 Minuten = 1000 * 60 * 5
        private long delay = 1000 * 60 * 5;

        public AutomaticSave() {
            setName("MImportExportPlugin$AutomaticSave");
            setDaemon(true);
            setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            while (true) {
                synchronized (core.getWettkampf()) {
                    if (!run) {
                        return;
                    }
                    autosave();
                }
                long time = System.currentTimeMillis();
                while ((time < click + delay) && run) {
                    long wait = Math.min(click + delay - time, 100);
                    try {
                        Thread.sleep(wait);
                    } catch (Exception e) {
                        // Nothing to do
                    }
                    time = System.currentTimeMillis();
                }
                click = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void shutDown() {
        synchronized (core.getWettkampf()) {
            run = false;
        }
    }
}