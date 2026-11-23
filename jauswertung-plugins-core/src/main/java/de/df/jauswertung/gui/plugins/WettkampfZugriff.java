package de.df.jauswertung.gui.plugins;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WettkampfZugriff {
    private final Semaphore sperre = new Semaphore(1);

    public Freigabe versuchen() {
        return sperre.tryAcquire() ? new Freigabe() : null;
    }

    public final class Freigabe implements AutoCloseable, Runnable {
        private final AtomicBoolean offen = new AtomicBoolean(true);

        private Freigabe() {
        }

        @Override
        public void close() {
            if (offen.getAndSet(false)) {
                sperre.release();
            }
        }

        @Override
        public void run() {
            close();
        }
    }
}
