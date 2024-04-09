package de.df.jauswertung.ares.export;

record Discipline(String name, String aresDiscipline, int amount, int length) {
    Discipline(String name, String aresDiscipline, int amount, int length) {
        this.name = name;
        this.aresDiscipline = aresDiscipline;
        this.amount = amount;
        this.length = length;
        if (length == 0) {
            throw new IllegalArgumentException("Length must not be 0");
        }
    }

    public String getDistance() {
        if (amount > 1) {
            return amount + "*" + length + "m";
        }
        return (amount * length) + "m";
    }
}
