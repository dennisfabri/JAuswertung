/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.teammembersinput;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.CorePlugin.Filemode;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.FileFilters;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.IRemoteActionListener;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.RemoteAction;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.util.RecentlyUsedFiles;

/**
 * @author Dennis Fabri
 * @date 05.04.2004
 */
public class MFilePlugin extends ANullPlugin {

    private static final SimpleFileFilter FILTER_WK = FileFilters.FF_TEAMMEMBERS;
    private static final SimpleFileFilter[] FILTERS = { FILTER_WK };

    private IPluginManager controller;
    private CorePlugin core;
    private JMenuItem[] menu;
    private JMenuItem[] recent;
    private JButton[] buttons;

    RecentlyUsedFiles recentlyFiles = RecentlyUsedFiles.open(Utils.getPreferences(), "Teammembers");

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);
        controller = c;
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", uid);
        core.setMode(Filemode.Teammembers);
        initialize();
        controller.addRemoteActionListener(new IRemoteActionListener() {

            @Override
            public boolean remoteAction(RemoteAction ra) {
                if (ra.getVerb().equals("open")) {
                    openFile(ra.getCommand());
                    return true;
                }
                return false;
            }

            @Override
            public void consumedRemoteAction(RemoteAction ra) {
                // Nothing to do
            }
        });
    }

    /**
     * 
     */
    private void initialize() {
        // init ActionListener
        // ActionListener neu = new NewActionListener();
        ActionListener open = new OpenActionListener();
        ActionListener save = new SaveActionListener();
        ActionListener save2 = new Save2ActionListener();
        ActionListener saveAs = new SaveAsActionListener();

        // init menu
        menu = new JMenuItem[3];
        // menu[0] = new JMenuItem(I18n.get("New"),
        // IconManager.getSmallIcon("newfile"));
        menu[0] = new JMenuItem(I18n.get("Open"), IconManager.getSmallIcon("openfile"));
        menu[1] = new JMenuItem(I18n.get("Save"), IconManager.getSmallIcon("savefile"));
        menu[2] = new JMenuItem(I18n.get("SaveAs"), IconManager.getSmallIcon("saveasfile"));

        // init buttons
        buttons = new JButton[2];
        // buttons[0] = new JButton(IconManager.getSmallIcon("newfile"));
        buttons[0] = new JButton(IconManager.getSmallIcon("openfile"));
        buttons[1] = new JButton(IconManager.getSmallIcon("savefile"));

        // Set ToolTips
        // menu[0].setToolTipText(I18n.getToolTip("NewCompetition"));
        menu[0].setToolTipText(I18n.getToolTip("OpenCompetition"));
        menu[1].setToolTipText(I18n.getToolTip("SaveCompetition"));
        menu[2].setToolTipText(I18n.getToolTip("SaveAsCompetition"));
        // buttons[0].setToolTipText(I18n.getToolTip("NewCompetition"));
        buttons[0].setToolTipText(I18n.getToolTip("OpenCompetition"));
        buttons[1].setToolTipText(I18n.getToolTip("SaveCompetition"));

        // set ActionListener
        // menu[0].addActionListener(neu);
        menu[0].addActionListener(open);
        menu[1].addActionListener(save);
        menu[2].addActionListener(saveAs);
        // buttons[0].addActionListener(neu);
        buttons[0].addActionListener(open);
        buttons[1].addActionListener(save2);

        // menu[0].setAccelerator(I18n.getKeyStroke("newfile"));
        menu[0].setAccelerator(I18n.getKeyStroke("openfile"));
        menu[1].setAccelerator(I18n.getKeyStroke("savefile"));

        initRecentlyUsedFiles();

        // Set initial state
        setSaveIcons(false);
    }

    private void initRecentlyUsedFiles() {
        // init recently used Files
        recent = new JMenuItem[recentlyFiles.getLength() + 1];
        for (int x = 0; x < recentlyFiles.getLength(); x++) {
            recent[x] = new JMenuItem("");
            recent[x].addActionListener(new OpenRecentActionListener(x));
        }

        JMenuItem empty = new JMenuItem(I18n.get("Empty"));
        empty.setFont(empty.getFont().deriveFont(Font.ITALIC));
        empty.setEnabled(false);
        recent[recentlyFiles.getLength()] = empty;

        recentlyFiles.addListener(this::updateRecentlyUsedFiles);

        updateRecentlyUsedFiles();
    }

    void recentFile(String name) {
        recentlyFiles.add(name);
    }

    void updateRecentlyUsedFiles() {
        int used = 0;
        for (int x = 0; x < recentlyFiles.getLength(); x++) {
            String name = recentlyFiles.getName(x);
            if (name == null) {
                recent[x].setVisible(false);
            } else {
                recent[x].setText("" + (x + 1) + " " + name);
                recent[x].setVisible(true);
                used++;
            }
        }
        recent[recentlyFiles.getLength()].setVisible(used == 0);
    }

    private void setSaveIcons(boolean enabled) {
        menu[1].setEnabled(enabled);
        menu[2].setEnabled(enabled || (core.getFilename() != null));
        buttons[1].setEnabled(enabled || (core.getFilename() == null));
    }

    void openFile() {
        if (controller.acceptWarning()) {
            JFrame window = controller.getWindow();

            String name = FileChooserUtils.openFile(window, FILTERS);
            if (name != null) {
                if (!core.load(name)) {
                    String meldung = I18n.get("OpenFailedText", name);
                    String note = I18n.get("OpenFailedText.Note", name);
                    DialogUtils.warn(controller.getWindow(), meldung, note);
                } else {
                    recentFile(name);
                }
            }
        }
    }

    void openFile(String name) {
        if (name != null) {
            if (controller.acceptWarning()) {
                if (!core.load(name)) {
                    String meldung = I18n.get("OpenFailedText", name);
                    String note = I18n.get("OpenFailedText.Note", name);
                    DialogUtils.warn(controller.getWindow(), meldung, note);
                }
            }
        }
    }

    void saveFile() {
        try {
            if (core.save()) {
                setSaveIcons(false);
            } else {
                String name = core.getFilename();
                String meldung = I18n.get("SaveFailedText", name);
                String note = I18n.get("SaveFailedText.Note", name);
                DialogUtils.warn(controller.getWindow(), meldung, note);
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            saveAsFile();
        }
    }

    void saveAsFile() {
        String name = FileChooserUtils.saveFile(controller.getWindow(), FILTER_WK);
        if (name != null) {
            boolean result = true;
            if (new File(name).exists()) {
                result = DialogUtils.ask(controller.getWindow(), I18n.get("OverwriteFileQuestion", name),
                        I18n.get("OverwriteFileQuestion.Note", name));
            }
            if (result) {
                if (core.saveAs(name)) {
                    setSaveIcons(false);
                    recentFile(name);
                } else {
                    String meldung = I18n.get("SaveFailedText", name);
                    String note = I18n.get("SaveFailedText.Note", name);
                    DialogUtils.warn(controller.getWindow(), meldung, note);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.beta.plugin.AuswertungPlugIn#getSupportedMenues()
     */
    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(I18n.get("File"), 100, menu, 100),
                new MenuInfo(I18n.get("File"), 100, recent, 500) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        setSaveIcons(
                (core.getFilename() != null) && ((due.getChangeReason() & UpdateEventConstants.REASON_LOAD_WK) == 0));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.beta.plugin.AuswertungPlugIn#getQuickButtons()
     */
    @Override
    public ButtonInfo[] getQuickButtons() {
        return new ButtonInfo[] { new ButtonInfo(buttons, 100) };
    }

    final class SaveAsActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            saveAsFile();
        }
    }

    final class SaveActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            saveFile();
        }
    }

    final class Save2ActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            save2Action();
        }
    }

    final class OpenActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            openFile();
        }
    }

    final class OpenRecentActionListener implements ActionListener {

        private int index = 0;

        public OpenRecentActionListener(int index) {
            this.index = index;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            openFile(recentlyFiles.getName(index));
            recentlyFiles.update(index);
        }
    }

    @Override
    public void shutDown() {
        recentlyFiles.save(Utils.getPreferences());
    }

    void save2Action() {
        if (menu[1].isEnabled()) {
            saveFile();
        } else {
            saveAsFile();
        }
    }
}