package de.df.jauswertung.gui.plugins.upload;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JToggleButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.web.iscupload.ResultUploader;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

public class MUploadPlugin extends ANullPlugin {

    private static final Logger log = LoggerFactory.getLogger(MUploadPlugin.class);

    private static final long ONE_MINUTE = 1 * 60 * 1000;
    private static final long FIFTEEN_SECOND = 15 * 1000;

    private ButtonInfo[] buttons;
    
    private CorePlugin core;

    private JToggleButton uploadButton;
    
    private ResultUploader iscResultUploader = new ResultUploader();

    private Timer uploadTimer;

    public MUploadPlugin() {
        uploadButton = new JToggleButton(IconManager.getSmallIcon("upload"));
        uploadButton.setToolTipText(I18n.getToolTip("ISCUpload"));
        uploadButton.addActionListener(event -> iscResultUploader.setActive(uploadButton.isSelected()));

        buttons = new ButtonInfo[1];
        buttons[0] = new ButtonInfo(uploadButton, 1010);

        long period = Utils.isInDevelopmentMode() ? FIFTEEN_SECOND : ONE_MINUTE;
        
        uploadTimer = new Timer("ISC Upload Timer", true);
        uploadTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    uploadResultsToISC();
                } catch (Exception ex) {
                    log.warn("Could not execute upload", ex);
                }
            }
        }, period, period);
    }

    private void uploadResultsToISC() {
        AWettkampf<?> wk = core.getFilteredWettkampf();
        iscResultUploader.uploadResultsToISC(wk);
    }

    @Override
    public void setController(IPluginManager controller, String pluginuid) {
        super.setController(controller, pluginuid);
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        return buttons;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        iscResultUploader.setDirtyFlag();
    }
}