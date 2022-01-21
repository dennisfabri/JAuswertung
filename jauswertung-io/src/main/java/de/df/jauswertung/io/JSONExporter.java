package de.df.jauswertung.io;

public class JSONExporter extends EmptyExporter {
    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public boolean isSupported(ImportExportTypes type) {
        switch (type) {
        case TIMES:
            return true;
        default:
            return false;
        }
    }

    @Override
    public String[] getSuffixes() {
        return new String[] { "json" };
    }
}
