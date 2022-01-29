package de.df.jauswertung.gui.plugins.upload;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.web.iscupload.ResultUploader;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MUploadPlugin extends ANullPlugin {

    private static final Logger log = LoggerFactory.getLogger(MUploadPlugin.class);

    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    private static final long THIRTY_SECONDS = 30 * 1000;

    private MenuInfo[] menues;
    private CorePlugin core;

    private JMenuItem upload;
    private ResultUploader iscResultUploader = new ResultUploader();

    private Timer uploadTimer;

    public MUploadPlugin() {
        upload = new JCheckBoxMenuItem(I18n.get("UploadToISC"));
        upload.addActionListener(event -> iscResultUploader.setActive(upload.isSelected()));
        upload.setToolTipText(I18n.getToolTip("UploadToISC"));

        menues = new MenuInfo[1];
        menues[0] = new MenuInfo(I18n.get("Edit"), 500, upload, 1800);

        long period = Utils.isInDevelopmentMode() ? THIRTY_SECONDS : FIVE_MINUTES;
        
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
    public MenuInfo[] getMenues() {
        return menues;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        iscResultUploader.setDirtyFlag();
    }
}