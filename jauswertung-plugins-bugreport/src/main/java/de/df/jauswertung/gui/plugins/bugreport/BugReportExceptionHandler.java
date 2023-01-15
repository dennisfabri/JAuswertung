package de.df.jauswertung.gui.plugins.bugreport;

import java.io.PrintStream;

import de.df.jauswertung.daten.AWettkampf;

final class BugReportExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String[] IGNORE_CLASSES = new String[] { "com.xduke.xswing", "javax.help.MergeHelpUtilities",
            "sun.awt.shell.Win32ShellFolder2" };

    private static final String[] IGNORE_PACKAGES = new String[] { "com.sun.java.swing.plaf" };

    private static final String[] IGNORE_METHODS = new String[] { "shouldIgnore", "putClientProperty",
            "getLocationOnScreen_NoTreeLock" };

    private static final String[] IGNORE_MESSAGES = new String[] {
            "sun.awt.image.BufImgSurfaceData cannot be cast to sun.java2d.xr.XRSurfaceData",
            "component must be showing on the screen to determine its location",
            "Substance delegate used when Substance is not the current LAF" };

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
        for (int y = 0; y < IGNORE_MESSAGES.length; y++) {
            if (e.getMessage().contains(IGNORE_MESSAGES[y])) {
                return false;
            }
        }
        return isBug(e.getStackTrace());
    }

    private boolean isBug(StackTraceElement[] stack) {
        if (stack.length == 0) {
            return true;
        }

        for (StackTraceElement aStack : stack) {
            for (int y = 0; y < IGNORE_METHODS.length; y++) {
                if (aStack.getMethodName().indexOf(IGNORE_METHODS[y]) >= 0) {
                    return false;
                }
            }
            for (int y = 0; y < IGNORE_CLASSES.length; y++) {
                if (aStack.getClassName().indexOf(IGNORE_CLASSES[y]) >= 0) {
                    return false;
                }
            }
            for (int y = 0; y < IGNORE_PACKAGES.length; y++) {
                if (aStack.getClassName().indexOf(IGNORE_PACKAGES[y]) >= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e == null) {
            return;
        }
        if (debug) {
            e.printStackTrace();
        }

        PrintStream err = System.err;
        try {
            if (isBug(e)) {
                @SuppressWarnings("rawtypes")
                Class c = null;
                @SuppressWarnings("rawtypes")
                AWettkampf wk = null;
                if (t != null) {
                    c = t.getClass();
                }
                if (this.bugreportPlugin.core != null) {
                    wk = this.bugreportPlugin.core.getWettkampf();
                }
                if (this.bugreportPlugin.br == null) {
                    err.println("BugReporting-Utility not ready.");
                    err.println("Printing Error:");
                    e.printStackTrace();
                } else {
                    this.bugreportPlugin.br.setData(e, t, c, wk);
                    this.bugreportPlugin.br.setVisible(true);
                }
            }
        } catch (Exception th) {
            err.println("Exception in UncaughtExceptionHandler:");
            err.println("Handling Exception from Thread " + (t == null ? "<unknown>" : t.toString()) + ":");
            e.printStackTrace();
            err.println("Generated Exception:");
            th.printStackTrace();
        }
    }
}