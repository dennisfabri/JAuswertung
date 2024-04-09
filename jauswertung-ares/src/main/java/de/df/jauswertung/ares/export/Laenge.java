package de.df.jauswertung.ares.export;

record Laenge(int laenge, String distance) implements Comparable<Laenge> {
    @Override
    public int compareTo(Laenge o) {
        return Integer.compare(laenge, o.laenge);
    }

}
