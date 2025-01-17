/*
 * Created on 12.05.2005
 */
package de.df.jauswertung.daten;

import java.util.Arrays;
import java.util.Random;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.util.AltersklassenUtils;
import de.df.jauswertung.util.RandomUtils;

public class MannschaftWettkampf extends AWettkampf<Mannschaft> {

    private static final long serialVersionUID = 7430614028771289718L;

    private Mannschaftsmitgliederexportkennung[] teammembersRegistrationIds = null;

    // Only for xstream
    @Deprecated
    public MannschaftWettkampf() {
        this(new Regelwerk(), new Strafen());
    }

    public MannschaftWettkampf(Regelwerk aks, Strafen s) {
        super(AltersklassenUtils.checkAKs(aks, false), s);
    }

    public Mannschaft createMannschaft(String name, boolean geschlecht, String gliederung, int ak, String bemerkung) {
        return new Mannschaft(this, name.trim(), geschlecht, gliederung.trim(), ak, bemerkung == null ? "" : bemerkung.trim());
    }

    public String[] getTeammembersRegistrationIds() {
        if (teammembersRegistrationIds == null) {
            return new String[0];
        }
        return Arrays.stream(teammembersRegistrationIds).map(t -> t.getId()).toArray(String[]::new);
    }

    public String[] getLast10TeammembersRegistrationIds() {
        if (teammembersRegistrationIds == null) {
            return new String[0];
        }
        return Arrays.stream(teammembersRegistrationIds).skip(1).limit(10).map(t -> t.getId()).toArray(String[]::new);
    }

    public String getCurrentTeammembersRegistrationsId() {
        if (teammembersRegistrationIds == null) {
            return "";
        }
        return teammembersRegistrationIds[0].getId();
    }

    public String createTeammembersRegistrationsId() {
        Random rng = RandomUtils.getRandomNumberGenerator();

        do {
            String id = createString(rng, 6);

            if (teammembersRegistrationIds == null) {
                teammembersRegistrationIds = new Mannschaftsmitgliederexportkennung[] {
                        new Mannschaftsmitgliederexportkennung(id) };
                changedNow();
                return id;
            }
            boolean found = false;
            for (Mannschaftsmitgliederexportkennung i : teammembersRegistrationIds) {
                if (i.getId().equals(id)) {
                    found = true;
                }
            }
            if (!found) {
                teammembersRegistrationIds = Arrays.copyOf(teammembersRegistrationIds,
                        teammembersRegistrationIds.length + 1);
                for (int x = teammembersRegistrationIds.length - 1; x >= 1; x--) {
                    teammembersRegistrationIds[x] = teammembersRegistrationIds[x - 1];
                }
                teammembersRegistrationIds[0] = new Mannschaftsmitgliederexportkennung(id);
                changedNow();
                return id;
            }
        } while (true);
    }

    @Override
    public boolean isEinzel() {
        return false;
    }

    private static final char[] validChars = "ABCDEFGHIJKLMNOPQRSTUVW23456789".toCharArray();

    private static char getChar(Random rng) {
        return validChars[rng.nextInt(validChars.length)];
    }

    public static String createString(Random rng, int length) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < length; x++) {
            sb.append(getChar(rng));
        }
        return sb.toString();
    }
}