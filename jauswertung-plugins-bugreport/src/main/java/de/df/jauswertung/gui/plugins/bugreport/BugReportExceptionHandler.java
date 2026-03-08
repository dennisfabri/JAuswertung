package de.df.jauswertung.gui.plugins.bugreport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class BugReportExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String[] IGNORE_CLASSES = new String[]{"com.xduke.xswing", "javax.help.MergeHelpUtilities",
            "sun.awt.shell.Win32ShellFolder2"};

    private static final String[] IGNORE_PACKAGES = new String[]{"com.sun.java.swing.plaf"};

    private static final String[] IGNORE_METHODS = new String[]{"shouldIgnore", "putClientProperty",
            "getLocationOnScreen_NoTreeLock"};

    private static final String[] IGNORE_MESSAGES = new String[]{
            "sun.awt.image.BufImgSurfaceData cannot be cast to sun.java2d.xr.XRSurfaceData",
            "component must be showing on the screen to determine its location",
            "Substance delegate used when Substance is not the current LAF",
            "Cannot invoke \"java.lang.ref.SoftReference.get()\" because \"this.lineCache\" is null"};

    /**
     *
     */
    private final BugreportPlugin bugreportPlugin;
    private boolean debug = false;

    public BugReportExceptionHandler(BugreportPlugin bugreportPlugin, boolean debug) {
        this.bugreportPlugin = bugreportPlugin;
        this.debug = debug;
    }

    private boolean isBug(Throwable e) {
        if (e == null || e.getMessage() == null) {
            return true;
        }
        for (String ignoreMessage : IGNORE_MESSAGES) {
            if (e.getMessage().contains(ignoreMessage)) {
                return false;
            }
        }
        return isBug(e.getStackTrace());
    }

    private boolean isBug(StackTraceElement[] stack) {
        for (StackTraceElement aStack : stack) {
            for (String ignoreMethod : IGNORE_METHODS) {
                if (aStack.getMethodName().contains(ignoreMethod)) {
                    return false;
                }
            }
            for (String ignoreClass : IGNORE_CLASSES) {
                if (aStack.getClassName().contains(ignoreClass)) {
                    return false;
                }
            }
            for (String ignorePackage : IGNORE_PACKAGES) {
                if (aStack.getClassName().contains(ignorePackage)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (throwable == null) {
            return;
        }
        if (debug) {
            log.error("Uncaught exception in thread {}", thread, throwable);
        }

        try {
            if (isBug(throwable)) {
                this.bugreportPlugin.show(thread, throwable);
            }
        } catch (Exception ex) {
            log.error("Exception in UncaughtExceptionHandler while handling Exception from Thread {}:{}\n-----\n{}\n-----",
                      thread == null ? "<unknown>" : thread.toString(),
                      throwable.getMessage(),
                      throwable.getStackTrace(),
                      ex);
        }
    }
}
