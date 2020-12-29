package de.df.jauswertung.gui.plugins.http;

public enum ExportMode {

    Everything(1), Completed(2), Filtered(3);

    public final int value;

    private ExportMode(int v) {
        value = v;
    }
}
