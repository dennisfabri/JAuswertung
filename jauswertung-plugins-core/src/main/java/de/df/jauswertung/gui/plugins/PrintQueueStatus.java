package de.df.jauswertung.gui.plugins;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.JPFrame;
import de.df.jutils.print.PrintQueue;

/**
 * Shows the state of the print queue in the status bar and reports failed jobs.
 */
public final class PrintQueueStatus implements PrintQueue.IPrintCallback {

    interface ErrorDisplay {
        void show(JFrame parent, String title, String message, String note);
    }

    private static final int RETRY_DELAY = 500;

    private final Supplier<? extends JFrame> window;
    private final Consumer<String> statusText;
    private final ErrorDisplay errorDisplay;
    private final int retryDelay;

    public PrintQueueStatus(Supplier<JPFrame> window) {
        this(window, text -> window.get().setStatusBarText(text), DialogUtils::warn, RETRY_DELAY);
    }

    PrintQueueStatus(Supplier<? extends JFrame> window, Consumer<String> statusText, ErrorDisplay errorDisplay,
            int retryDelay) {
        this.window = window;
        this.statusText = statusText;
        this.errorDisplay = errorDisplay;
        this.retryDelay = retryDelay;
    }

    @Override
    public void jobAdded(String job, int jobs) {
        jobStarted(job, jobs);
    }

    @Override
    public void jobStarted(String job, int jobs) {
        setStatusText(I18n.get("PrintingJobOfJobs", job, jobs));
    }

    @Override
    public void jobFinished(String job, int jobs) {
        setStatusText("");
    }

    @Override
    public void jobError(String job, int jobs, String title, String message, String note) {
        SwingUtilities.invokeLater(() -> showWhenEnabled(title, message, note));
        setStatusText("");
    }

    @Override
    public void queueEmpty() {
        setStatusText(null);
    }

    private void setStatusText(String text) {
        SwingUtilities.invokeLater(() -> statusText.accept(text));
    }

    // A dialog over the disabled main window (e.g. while a print preview is open) would be hidden behind it.
    private void showWhenEnabled(String title, String message, String note) {
        if (window.get().isEnabled()) {
            errorDisplay.show(window.get(), title, message, note);
            return;
        }
        Timer retry = new Timer(retryDelay, null);
        retry.addActionListener(e -> {
            if (window.get().isEnabled()) {
                retry.stop();
                errorDisplay.show(window.get(), title, message, note);
            }
        });
        retry.start();
    }
}
