package de.df.jauswertung.io.value;

import java.util.Objects;

public class ZWStartnummer {

    private final int startnummer;
    private final int index;

    public ZWStartnummer(int startnummer, int index) {
        super();
        this.startnummer = startnummer;
        this.index = index;
    }

    public int getStartnummer() {
        return startnummer;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ZWStartnummer other = (ZWStartnummer) obj;
        return index == other.index && startnummer == other.startnummer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, startnummer);
    }

    @Override
    public String toString() {
        return String.format("%d-%d", startnummer, index);
    }
}