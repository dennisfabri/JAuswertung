package de.df.jauswertung.gui.plugins.emergency;

class RecWert {

    public RecWert(String ak, String d, boolean m, int z) {
        Altersklasse = ak;
        Disziplin = d;
        Maennlich = m;
        Zeit = z;
    }

    public String Altersklasse;
    public String Disziplin;
    public boolean Maennlich;
    public int FalscheZeit = 0;
    public int Zeit;
}