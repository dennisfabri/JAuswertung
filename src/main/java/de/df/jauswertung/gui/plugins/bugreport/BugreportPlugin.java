/*
 * Created on 05.04.2004
 */
package de.df.jauswertung.gui.plugins.bugreport;

import javax.swing.JFrame;

import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

/**
 * @author Dennis M�ller @date 05.04.2004
 */
public class BugreportPlugin extends AFeature {

    CorePlugin core  = null;
    JFrame     frame = null;
    JBugReport br    = null;

    public BugreportPlugin() {
        br = new JBugReport(null);
        WindowUtils.center(br);
    }

    @Override
    public void setController(IPluginManager controller, String newUid) {
        super.setController(controller, newUid);
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", newUid);
        if (core == null) {
            throw new NullPointerException("CorePlugin needed!");
        }

        new InitializerThread(Utils.isInDevelopmentMode()).start();
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (frame == null) {
            try {
                frame = getController().getWindow();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
            if (frame != null) {
                br = new JBugReport(frame);
            }
        }
    }

    private final class InitializerThread extends Thread {

        private boolean debug = false;

        public InitializerThread(boolean debug) {
            setName("BugreportPlugin.InitializerThread:" + " Setting ExceptionHandler");
            setDaemon(true);
            setPriority(Thread.MIN_PRIORITY);
            this.debug = debug;
        }

        @Override
        public void run() {
            Thread.setDefaultUncaughtExceptionHandler(new BugReportExceptionHandler(BugreportPlugin.this, debug));
        }
    }

    public void handle(Exception e) {
        new BugReportExceptionHandler(BugreportPlugin.this, Utils.isInDevelopmentMode()).uncaughtException(Thread.currentThread(), e);
    }
}