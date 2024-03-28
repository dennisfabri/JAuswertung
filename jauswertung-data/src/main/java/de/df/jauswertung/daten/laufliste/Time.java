/*
 * Created on 06.01.2006
 */
package de.df.jauswertung.daten.laufliste;

import java.io.Serial;
import java.io.Serializable;

import de.df.jauswertung.gui.util.I18n;

public class Time implements Comparable<Time>, Serializable {

    @Serial
    private static final long serialVersionUID = -6335301709914650979L;

    int hour;
    int minute;
    int second;

    public Time(int minutes) {
        this(minutes / 60, minutes % 60);
    }

    public Time(double minutes) {
        this((int) Math.floor(minutes));
        second = (int) Math.round(minutes * 60) % 60;
    }

    public Time(int hour, int minute) {
        this.hour = hour % 24;
        this.minute = minute % 60;
        this.second = 0;
    }

    @Override
    public String toString() {
        return I18n.get("TimeOfDayValue", hour, minute);
    }

    public int getTimeInMinutes() {
        return hour * 60 + minute;
    }

    public int getTimeInSeconds() {
        return (hour * 60 + minute) * 60 + second;
    }

    public double getTime() {
        return getTimeInMinutes() + 1.0 * second / 60.0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Time t) {
            return (t.hour == hour) && (t.minute == minute);
        }
        return false;
    }

    @Override
    public int compareTo(Time o) {
        return getTimeInSeconds() - o.getTimeInSeconds();
    }

    @Override
    public int hashCode() {
        return getTimeInSeconds();
    }
}