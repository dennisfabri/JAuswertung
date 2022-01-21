package de.df.jauswertung.io;

import java.io.OutputStream;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jutils.util.Feedback;

public abstract class EmptyExporter implements IExporter {

    @Override
    public <T extends ASchwimmer> boolean registration(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean heats(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean heatsoverview(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertung(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean bestezeiten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean zeiten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean results(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean protocol(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean startkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungStartkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean referees(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean penalties(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean teammembers(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungResults(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean heattimes(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }
}
