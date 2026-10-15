package de.df.jauswertung.gui.plugins;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.JButton;
import javax.swing.JMenuItem;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
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

public abstract class AFilePlugin extends ANullPlugin {

    record SaveActions(boolean save, boolean saveAs, boolean saveButton) {
    }

    private final SimpleFileFilter filter;
    private final RecentlyUsedFiles recentlyFiles;
    private final BiConsumer<IPluginManager, CorePlugin> newFile;

    private IPluginManager controller;
    private CorePlugin core;

    private JMenuItem[] menu;
    private JMenuItem[] recent;
    private JButton[] buttons;
    private JMenuItem saveMenu;
    private JMenuItem saveAsMenu;
    private JButton saveButton;

    protected AFilePlugin(SimpleFileFilter filter, String recentlyUsedKey) {
        this(filter, recentlyUsedKey, null);
    }

    protected AFilePlugin(SimpleFileFilter filter, String recentlyUsedKey,
            BiConsumer<IPluginManager, CorePlugin> newFile) {
        this.filter = filter;
        this.newFile = newFile;
        recentlyFiles = RecentlyUsedFiles.open(Utils.getPreferences(), recentlyUsedKey);
    }

    static SaveActions saveActions(boolean changed, boolean hasFilename) {
        boolean save = changed && hasFilename;
        return new SaveActions(save, true, save || !hasFilename);
    }

    protected void prepare(CorePlugin c) {
    }

    @Override
    public void setController(IPluginManager c, String uid) {
        super.setController(c, uid);
        controller = c;
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", uid);
        prepare(core);
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

    private void initialize() {
        List<JMenuItem> menuItems = new ArrayList<>();
        List<JButton> buttonList = new ArrayList<>();

        if (newFile != null) {
            JMenuItem newMenu = new JMenuItem(I18n.get("New"), IconManager.getSmallIcon("newfile"));
            newMenu.setToolTipText(I18n.getToolTip("NewCompetition"));
            newMenu.setAccelerator(I18n.getKeyStroke("newfile"));
            newMenu.addActionListener(e -> newFile.accept(controller, core));
            menuItems.add(newMenu);

            JButton newButton = new JButton(IconManager.getSmallIcon("newfile"));
            newButton.setToolTipText(I18n.getToolTip("NewCompetition"));
            newButton.addActionListener(e -> newFile.accept(controller, core));
            buttonList.add(newButton);
        }

        JMenuItem openMenu = new JMenuItem(I18n.get("Open"), IconManager.getSmallIcon("openfile"));
        openMenu.setToolTipText(I18n.getToolTip("OpenCompetition"));
        openMenu.setAccelerator(I18n.getKeyStroke("openfile"));
        openMenu.addActionListener(e -> openFile());
        menuItems.add(openMenu);

        saveMenu = new JMenuItem(I18n.get("Save"), IconManager.getSmallIcon("savefile"));
        saveMenu.setToolTipText(I18n.getToolTip("SaveCompetition"));
        saveMenu.setAccelerator(I18n.getKeyStroke("savefile"));
        saveMenu.addActionListener(e -> saveFile());
        menuItems.add(saveMenu);

        saveAsMenu = new JMenuItem(I18n.get("SaveAs"), IconManager.getSmallIcon("saveasfile"));
        saveAsMenu.setToolTipText(I18n.getToolTip("SaveAsCompetition"));
        saveAsMenu.addActionListener(e -> saveAsFile());
        menuItems.add(saveAsMenu);

        JButton openButton = new JButton(IconManager.getSmallIcon("openfile"));
        openButton.setToolTipText(I18n.getToolTip("OpenCompetition"));
        openButton.addActionListener(e -> openFile());
        buttonList.add(openButton);

        saveButton = new JButton(IconManager.getSmallIcon("savefile"));
        saveButton.setToolTipText(I18n.getToolTip("SaveCompetition"));
        saveButton.addActionListener(e -> saveOrSaveAs());
        buttonList.add(saveButton);

        menu = menuItems.toArray(new JMenuItem[0]);
        buttons = buttonList.toArray(new JButton[0]);

        initRecentlyUsedFiles();

        updateSaveActions(false);
    }

    private void initRecentlyUsedFiles() {
        recent = new JMenuItem[recentlyFiles.getLength() + 1];
        for (int x = 0; x < recentlyFiles.getLength(); x++) {
            int index = x;
            recent[x] = new JMenuItem("");
            recent[x].addActionListener(e -> {
                openFile(recentlyFiles.getName(index));
                recentlyFiles.update(index);
            });
        }

        JMenuItem empty = new JMenuItem(I18n.get("Empty"));
        empty.setFont(empty.getFont().deriveFont(Font.ITALIC));
        empty.setEnabled(false);
        recent[recentlyFiles.getLength()] = empty;

        recentlyFiles.addListener(this::updateRecentlyUsedFiles);

        updateRecentlyUsedFiles();
    }

    private void updateRecentlyUsedFiles() {
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

    private void updateSaveActions(boolean changed) {
        SaveActions actions = saveActions(changed, core.getFilename() != null);
        saveMenu.setEnabled(actions.save());
        saveAsMenu.setEnabled(actions.saveAs());
        saveButton.setEnabled(actions.saveButton());
    }

    private void openFile() {
        if (controller.acceptWarning()) {
            String name = FileChooserUtils.openFile(controller.getWindow(), filter);
            if (name != null) {
                if (load(name)) {
                    recentlyFiles.add(name);
                }
            }
        }
    }

    private void openFile(String name) {
        if ((name != null) && controller.acceptWarning() && load(name)) {
            String dir = new File(name).getParent();
            if (dir != null) {
                FileChooserUtils.setBaseDir(dir);
            }
        }
    }

    private boolean load(String name) {
        if (core.load(name)) {
            return true;
        }
        DialogUtils.warn(controller.getWindow(), I18n.get("OpenFailedText", name),
                I18n.get("OpenFailedText.Note", name));
        return false;
    }

    private void saveFile() {
        if (core.getFilename() == null) {
            saveAsFile();
        } else if (core.save()) {
            updateSaveActions(false);
        } else {
            String name = core.getFilename();
            DialogUtils.warn(controller.getWindow(), I18n.get("SaveFailedText", name),
                    I18n.get("SaveFailedText.Note", name));
        }
    }

    private void saveAsFile() {
        String name = FileChooserUtils.saveFile(controller.getWindow(), filter);
        if (name == null) {
            return;
        }
        if (new File(name).exists() && !DialogUtils.ask(controller.getWindow(),
                I18n.get("OverwriteFileQuestion", name), I18n.get("OverwriteFileQuestion.Note", name))) {
            return;
        }
        if (core.saveAs(name)) {
            updateSaveActions(false);
            recentlyFiles.add(name);
        } else {
            DialogUtils.warn(controller.getWindow(), I18n.get("SaveFailedText", name),
                    I18n.get("SaveFailedText.Note", name));
        }
    }

    private void saveOrSaveAs() {
        if (saveMenu.isEnabled()) {
            saveFile();
        } else {
            saveAsFile();
        }
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(I18n.get("File"), 100, menu, 100),
                new MenuInfo(I18n.get("File"), 100, recent, 500) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        updateSaveActions((due.getChangeReason() & UpdateEventConstants.REASON_LOAD_WK) == 0);
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        return new ButtonInfo[] { new ButtonInfo(buttons, 100) };
    }

    @Override
    public void shutDown() {
        recentlyFiles.save(Utils.getPreferences());
    }
}
