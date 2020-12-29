/*
 * Created on 06.01.2006
 */
package de.df.jauswertung.daten.laufliste;

import java.io.Serializable;

import de.df.jauswertung.gui.util.I18n;

public class Duration implements Serializable {

    private static final long serialVersionUID = 351707174206635887L;

    private int               minutes;
    private int               seconds;

    public Duration(double duration) {
        this((int) Math.floor(duration), (int) Math.round((duration - Math.floor(duration)) * 60));
    }

    public Duration(int minutes) {
        this(minutes, 0);
    }

    public Duration(int minutes, int seconds) {
        if (seconds >= 60) {
            throw new IllegalArgumentException("seconds must be lower than 60 but was " + seconds);
        }
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public int getTimeInMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public double getTime() {
        double result = minutes;
        result += 1.0 * seconds / 60.0;
        return result;
    }

    @Override
    public String toString() {
        if (minutes < 60) {
            if (seconds == 0) {
                return I18n.get("NumberOfMinutes", minutes);
            }
            return I18n.get("NumberOfMinutesAndSeconds", minutes, seconds);
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        if ((mins == 0) && (seconds == 0)) {
            return I18n.get("NumberOfHours", hours);
        }
        return I18n.get("NumberOfHoursAndMinutesAndSeconds", hours, mins, seconds);
    }

    @Override
    public int hashCode() {
        return ("" + minutes).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Duration) {
            Duration t = (Duration) o;
            if (t.minutes == minutes) {
                return true;
            }
        }
        return false;
    }
}