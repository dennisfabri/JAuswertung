/*
 * Created on 24.01.2006
 */
package de.df.jauswertung.io;

public final class ImportConstants {

    private ImportConstants() {
        // Hide
    }

    private static final boolean[] REQUIRED_INDIZES_SINGLE;
    private static final boolean[] REQUIRED_INDIZES_TEAM;

    private static final boolean[] REQUIRED_INDIZES_FOR_UPDATE_SINGLE;
    private static final boolean[] REQUIRED_INDIZES_FOR_UPDATE_TEAM;

    public static final int        NAME              = 0;
    public static final int        NACHNAME          = 1;
    public static final int        VORNAME           = 2;
    public static final int        PUNKTE1           = 3;
    public static final int        PUNKTE2           = 4;
    public static final int        BEMERKUNG         = 5;
    public static final int        GLIEDERUNG        = 6;
    public static final int        ALTERSKLASSE      = 7;
    public static final int        GESCHLECHT        = 8;
    public static final int        JAHRGANG          = 9;
    public static final int        AUSSER_KONKURRENZ = 10;
    public static final int        STARTNUMMER       = 11;
    public static final int        DISCIPLINES       = 12;
    public static final int        MELDEZEITEN       = 13;
    public static final int        MEMBERS           = 14;
    public static final int        STARTPASS         = 15;
    public static final int        ZW                = 16;
    public static final int        CATEGORY          = 17;
    public static final int        POSITION          = 18;
    public static final int        STUFE             = 19;
    public static final int        QUALI_LEVEL       = 20;
    public static final int        QUALI             = 21;
    public static final int        PROTOCOL1         = 22;
    public static final int        PROTOCOL2         = 23;
    public static final int        NACHNAME1         = 24;
    public static final int        VORNAME1          = 25;
    public static final int        GESCHLECHT1       = 26;
    public static final int        JAHRGANG1         = 27;
    public static final int        NACHNAME2         = 28;
    public static final int        VORNAME2          = 29;
    public static final int        GESCHLECHT2       = 30;
    public static final int        JAHRGANG2         = 31;
    public static final int        NACHNAME3         = 32;
    public static final int        VORNAME3          = 33;
    public static final int        GESCHLECHT3       = 34;
    public static final int        JAHRGANG3         = 35;
    public static final int        NACHNAME4         = 36;
    public static final int        VORNAME4          = 37;
    public static final int        GESCHLECHT4       = 38;
    public static final int        JAHRGANG4         = 39;
    public static final int        NACHNAME5         = 40;
    public static final int        VORNAME5          = 41;
    public static final int        GESCHLECHT5       = 42;
    public static final int        JAHRGANG5         = 43;
    public static final int        NACHNAME6         = 44;
    public static final int        VORNAME6          = 45;
    public static final int        GESCHLECHT6       = 46;
    public static final int        JAHRGANG6         = 47;
    public static final int        NACHNAME7         = 48;
    public static final int        VORNAME7          = 49;
    public static final int        GESCHLECHT7       = 50;
    public static final int        JAHRGANG7         = 51;
    public static final int        NACHNAME8         = 52;
    public static final int        VORNAME8          = 53;
    public static final int        GESCHLECHT8       = 54;
    public static final int        JAHRGANG8         = 55;
    public static final int        NACHNAME9         = 56;
    public static final int        VORNAME9          = 57;
    public static final int        GESCHLECHT9       = 58;
    public static final int        JAHRGANG9         = 59;
    public static final int        NACHNAME10        = 60;
    public static final int        VORNAME10         = 61;
    public static final int        GESCHLECHT10      = 62;
    public static final int        JAHRGANG10        = 63;
    public static final int        NACHNAME11        = 64;
    public static final int        VORNAME11         = 65;
    public static final int        GESCHLECHT11      = 66;
    public static final int        JAHRGANG11        = 67;
    public static final int        NACHNAME12        = 68;
    public static final int        VORNAME12         = 69;
    public static final int        GESCHLECHT12      = 70;
    public static final int        JAHRGANG12        = 71;

    public static final int        MAX_INDEX         = 72;

    public static final int        INDEX_COUNT       = MAX_INDEX + 1;

    static {
        REQUIRED_INDIZES_SINGLE = new boolean[INDEX_COUNT];
        REQUIRED_INDIZES_TEAM = new boolean[INDEX_COUNT];
        REQUIRED_INDIZES_FOR_UPDATE_SINGLE = new boolean[INDEX_COUNT];
        REQUIRED_INDIZES_FOR_UPDATE_TEAM = new boolean[INDEX_COUNT];
        for (int x = 0; x < INDEX_COUNT; x++) {
            REQUIRED_INDIZES_SINGLE[x] = false;
            REQUIRED_INDIZES_TEAM[x] = false;
            REQUIRED_INDIZES_FOR_UPDATE_SINGLE[x] = false;
            REQUIRED_INDIZES_FOR_UPDATE_TEAM[x] = false;
        }
        
        REQUIRED_INDIZES_SINGLE[GLIEDERUNG] = true;
        REQUIRED_INDIZES_SINGLE[ALTERSKLASSE] = true;
        REQUIRED_INDIZES_SINGLE[GESCHLECHT] = true;
        REQUIRED_INDIZES_SINGLE[NACHNAME] = true;
        REQUIRED_INDIZES_SINGLE[VORNAME] = true;

        REQUIRED_INDIZES_TEAM[GLIEDERUNG] = true;
        REQUIRED_INDIZES_TEAM[ALTERSKLASSE] = true;
        REQUIRED_INDIZES_TEAM[GESCHLECHT] = true;
        REQUIRED_INDIZES_TEAM[NAME] = true;

    
        REQUIRED_INDIZES_FOR_UPDATE_SINGLE[STARTNUMMER] = true;
        REQUIRED_INDIZES_FOR_UPDATE_SINGLE[NACHNAME] = true;
        REQUIRED_INDIZES_FOR_UPDATE_SINGLE[VORNAME] = true;

        REQUIRED_INDIZES_FOR_UPDATE_TEAM[STARTNUMMER] = true;
        REQUIRED_INDIZES_FOR_UPDATE_TEAM[NAME] = true;
}

    public static boolean[] getRequiredIndizes(boolean team) {
        if (team) {
            return REQUIRED_INDIZES_TEAM;
        }
        return REQUIRED_INDIZES_SINGLE;
    }
    public static boolean[] getRequiredIndizesForUpdate(boolean team) {
        if (team) {
            return REQUIRED_INDIZES_FOR_UPDATE_TEAM;
        }
        return REQUIRED_INDIZES_FOR_UPDATE_SINGLE;
    }
}