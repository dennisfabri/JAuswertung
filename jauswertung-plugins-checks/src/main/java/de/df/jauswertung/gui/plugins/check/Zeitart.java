package de.df.jauswertung.gui.plugins.check;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.util.SchwimmerUtils;
import de.df.jauswertung.gui.util.TimeStatus;

enum Zeitart {
    ZEIT {
        @Override
        int getZeit(ASchwimmer s, int disziplin) {
            return s.getZeit(disziplin);
        }

        @Override
        void setZeit(ASchwimmer s, int disziplin, int zeit) {
            s.setZeit(disziplin, zeit);
        }

        @Override
        <T extends ASchwimmer> TimeStatus getStatus(AWettkampf<T> wk, T s, int disziplin) {
            return SchwimmerUtils.getTimeStatus(wk, s, disziplin);
        }

        @Override
        boolean isGeprueft(AWettkampf<?> wk) {
            return !wk.isHeatBased();
        }
    },
    MELDEZEIT {
        @Override
        int getZeit(ASchwimmer s, int disziplin) {
            return s.getMeldezeit(disziplin);
        }

        @Override
        void setZeit(ASchwimmer s, int disziplin, int zeit) {
            s.setMeldezeit(disziplin, zeit);
        }

        @Override
        <T extends ASchwimmer> TimeStatus getStatus(AWettkampf<T> wk, T s, int disziplin) {
            return SchwimmerUtils.getRegistrationTimeStatus(wk, s, disziplin);
        }

        @Override
        boolean isGeprueft(AWettkampf<?> wk) {
            return true;
        }
    };

    abstract int getZeit(ASchwimmer s, int disziplin);

    abstract void setZeit(ASchwimmer s, int disziplin, int zeit);

    abstract <T extends ASchwimmer> TimeStatus getStatus(AWettkampf<T> wk, T s, int disziplin);

    /** Times of competitions with heats per round are entered per round, not in the competition's times. */
    abstract boolean isGeprueft(AWettkampf<?> wk);
}
