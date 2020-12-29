/*
 * Created on 05.12.2004
 */
package de.df.jauswertung.gui.plugins;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lisasp.swing.filechooser.FileChooserUtils;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.JIntSpinner;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.util.StringTools;

public class OBackupPlugin extends AFeature implements MOptionenPlugin.OptionsPlugin {

    private boolean           enabled;
    private String            directory;
    int                       intervall;

    private JPanel            optionsPanel;
    private JCheckBox         backup;
    private JWarningTextField dir;
    private JIntSpinner       time;
    private JButton           dots;

    MOptionenPlugin           optionen;
    CorePlugin                core;

    public OBackupPlugin() {
        // Nothing to do
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);

        optionen = (MOptionenPlugin) plugincontroller.getFeature("de.df.jauswertung.options", pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);

        enabled = Utils.getPreferences().getBoolean("BackupEnabled", false);
        directory = Utils.getPreferences().get("BackupDirectory", "");
        intervall = Utils.getPreferences().getInt("BackupIntervall", 30);

        if (!isDirectoryOk(directory)) {
            enabled = false;
        }
        if (intervall <= 0) {
            intervall = 30;
        }

        optionen.addOptionsPlugin(this);

        new TimedBackup().start();
    }

    /*
     * (non-Javadoc)
     * @see
     * de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#getPanel()
     */
    @Override
    public JPanel[] getPanels() {
        if (optionsPanel == null) {
            initPanel();
        }
        return new JPanel[] { optionsPanel };
    }

    private void initPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4, 6 } });
        optionsPanel = new JPanel(layout);
        optionsPanel.setName(I18n.get("Backup"));

        dir = new JWarningTextField(true, false) {
            private static final long serialVersionUID = 8453456541539574447L;

            @Override
            public boolean isValidString() {
                if (!super.isValidString()) {
                    return false;
                }
                return OBackupPlugin.this.isOk();
            }
        };
        dir.setEnabled(enabled);
        dir.setText(directory);
        dir.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent arg0) {
                optionen.notifyChange();
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                optionen.notifyChange();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                optionen.notifyChange();
            }
        });
        dots = new JButton("...");
        dots.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                chooseDirectory();
            }
        });

        time = new JIntSpinner(intervall, 5, 120, 5);
        time.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                optionen.notifyChange();
            }
        });

        backup = new JCheckBox();
        backup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                optionen.notifyChange();

                updateButtons();
            }
        });
        backup.setSelected(enabled);

        updateButtons();

        optionsPanel.add(new JLabel(I18n.get("Backup")), CC.xy(2, 2));
        optionsPanel.add(backup, CC.xyw(4, 2, 3));
        optionsPanel.add(new JLabel(I18n.get("Directory")), CC.xy(2, 4));
        optionsPanel.add(dir, CC.xy(4, 4));
        optionsPanel.add(dots, CC.xy(6, 4));
        optionsPanel.add(new JLabel(I18n.get("Minutes")), CC.xy(2, 6));
        optionsPanel.add(time, CC.xyw(4, 6, 3));
    }

    @Override
    public void apply() {
        enabled = backup.isSelected();
        if (enabled) {
            intervall = time.getInt();
            directory = dir.getText();
        }

        Utils.getPreferences().putBoolean("BackupEnabled", enabled);
        if (enabled) {
            Utils.getPreferences().put("BackupDirectory", directory);
            Utils.getPreferences().putInt("BackupIntervall", intervall);
        }
    }

    @Override
    public void cancel() {
        backup.setSelected(enabled);
        time.setInt(intervall);
        dir.setText(directory);

        updateButtons();
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        // Nothing to do here
    }

    private static boolean isDirectoryOk(String d) {
        if (d.length() == 0) {
            return false;
        }
        File f = new File(d);
        return f.exists() && f.isDirectory();
    }

    @Override
    public boolean isOk() {
        if (backup == null || !backup.isSelected()) {
            return true;
        }
        return isDirectoryOk(dir.getText());
    }

    void updateButtons() {
        boolean b = backup.isSelected();
        dir.setEnabled(b);
        dots.setEnabled(b);
        time.setEnabled(b);
    }

    void chooseDirectory() {
        Window activeWindow = javax.swing.FocusManager.getCurrentManager().getActiveWindow();
        String d = FileChooserUtils.chooseDirectory(activeWindow);
        if (d != null) {
            dir.setText(d);
        }
    }

    @Override
    public void shutDown() {
        run = false;
        while (!finished) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                // Nothing to do
            }
        }
    }

    void doBackup() {
        if (enabled) {
            core.saveCopyAs(directory + File.separator + "backup-" + StringTools.getCompactDateTimeString() + ".wk");
        }
    }

    boolean run      = true;
    boolean finished = true;

    private class TimedBackup extends Thread {

        // 1 Minute = 1000 * 60
        private final static long DELAY = 1000 * 60;
        private long              last  = System.currentTimeMillis();

        public TimedBackup() {
            setName("OBackupPlugin$AutomaticSave");
            setDaemon(true);
            setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            finished = false;
            while (run) {
                synchronized (core.getWettkampf()) {
                    if (run) {
                        doBackup();
                    }
                }
                long current = System.currentTimeMillis();
                while ((current < last + DELAY * intervall) && run) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        // Nothing to do
                    }
                    current = System.currentTimeMillis();
                }
                last = System.currentTimeMillis();
            }
            finished = true;
        }
    }
}