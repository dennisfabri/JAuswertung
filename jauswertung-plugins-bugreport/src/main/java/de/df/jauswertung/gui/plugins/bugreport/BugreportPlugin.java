package de.df.jauswertung.gui.plugins.bugreport;

import javax.swing.JFrame;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Dennis Mï¿½ller
 * @date 05.04.2004
 */
@Slf4j
public class BugreportPlugin extends AFeature {

    private CorePlugin core = null;
    private JFrame frame = null;
    private JBugReport br = null;

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
                log.warn("Problem during data update", npe);
            }
            if (frame != null) {
                br = new JBugReport(frame);
            }
        }
    }

    public void show(Thread causingThread, Throwable cause) {
        if (br == null) {
            log.error("BugReporting-Utility not ready to show bugreport dialog, printing stacktrace to log.", cause);
        } else {
            AWettkampf<?> wk = core != null ? core.getWettkampf() : null;
            br.setData(cause, causingThread, wk);
            EDTUtils.setVisible(br, true);
        }
    }

    private final class InitializerThread extends Thread {

        private final boolean debug;

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
        new BugReportExceptionHandler(BugreportPlugin.this, Utils.isInDevelopmentMode())
                .uncaughtException(Thread.currentThread(), e);
    }
}
