/*
 * Created on 14.10.2005
 */
package de.df.jauswertung.io;

import de.df.jutils.gui.filefilter.SimpleFileFilter;

public final class FileFilters {

    public static final SimpleFileFilter FF_AGE_GROUPS;
    public static final SimpleFileFilter FF_AGE_GROUP_SINGLE;
    public static final SimpleFileFilter FF_AGE_GROUP_TEAM;

    public static final SimpleFileFilter FF_RULEBOOKS;
    public static final SimpleFileFilter FF_RULEBOOK_SINGLE;
    public static final SimpleFileFilter FF_RULEBOOK_TEAM;

    public static final SimpleFileFilter FF_RULEBOOKS_SINGLE;
    public static final SimpleFileFilter FF_RULEBOOKS_TEAM;

    public static final SimpleFileFilter FF_TEAMMEMBERS;
    public static final SimpleFileFilter FF_COMPETITION;
    public static final SimpleFileFilter FF_REFEREES;

    public static final SimpleFileFilter FF_DOCUMENT;

    static {
        FF_AGE_GROUPS = new SimpleFileFilter("Regelwerk", new String[] { "ake", "akm" });
        FF_AGE_GROUP_SINGLE = new SimpleFileFilter("Regelwerk (Einzel)", "ake");
        FF_AGE_GROUP_TEAM = new SimpleFileFilter("Regelwerk (Mannschaft)", "akm");

        FF_RULEBOOKS = new SimpleFileFilter("Regelwerk", new String[] { "rwe", "rwm", "ake", "akm" });
        FF_RULEBOOK_SINGLE = new SimpleFileFilter("Regelwerk (Einzel)", "rwe");
        FF_RULEBOOK_TEAM = new SimpleFileFilter("Regelwerk (Mannschaft)", "rwm");

        FF_RULEBOOKS_SINGLE = new SimpleFileFilter("Regelwerk (Einzel)", new String[] { "rwe", "ake" });
        FF_RULEBOOKS_TEAM = new SimpleFileFilter("Regelwerk (Mannschaft)", new String[] { "rwm", "akm" });

        FF_TEAMMEMBERS = new SimpleFileFilter("Mannschaftsmitglieder", "wkmm");
        FF_COMPETITION = new SimpleFileFilter("Wettkampf", "wk");
        FF_REFEREES = new SimpleFileFilter("Kampfrichter", "kr");
        FF_DOCUMENT = new SimpleFileFilter("Urkunde", "wku");
    }

    private FileFilters() {
        // Hide constructor
    }
}