package de.df.jauswertung.ares.export;

enum AresGender {
    Male("männlich", 'M'), Female("weiblich", 'W'), Mixed("mixed", 'X');

    private final String name;
    private final char shortName;

    AresGender(String name, char shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    public String toLine() {
        return "\"" + name + "\";\"" + shortName + "\"";
    }
}
