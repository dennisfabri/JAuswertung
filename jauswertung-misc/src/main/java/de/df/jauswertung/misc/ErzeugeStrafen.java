/*
 * Created on 23.02.2006
 */
package de.df.jauswertung.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.daten.regelwerk.StrafenKapitel;
import de.df.jauswertung.daten.regelwerk.StrafenParagraph;
import de.df.jauswertung.io.ExcelReader;
import de.df.jauswertung.io.OutputManager;

public final class ErzeugeStrafen {
    private enum TeamPenaltyType {
        Member, Change
    }

    private static class TeamPenaltyDefinition {
        private final String code;
        private final TeamPenaltyType type;

        public TeamPenaltyDefinition(String code, TeamPenaltyType type) {
            this.code = code;
            this.type = type;
        }

        public boolean isMatching(Strafe s) {
            if (s == null) {
                return false;
            }
            return s.getShortname().equals(code);
        }

        public Collection<Strafe> expand(Strafe s) {

            ArrayList<Strafe> strafen = new ArrayList<>();
            strafen.add(s);
            if (!isMatching(s)) {
                return strafen;
            }
            switch (type) {
            case Change:
                for (int x = 1; x < 4; x++) {
                    String name = String.format("%s (%s. auf den %s. Schwimmer)", s.getName(), "" + x, "" + (x + 1));
                    String shortname = String.format("%s#%s", s.getShortname(), "" + (x + 1));
                    strafen.add(new Strafe(name, shortname, s.getArt(), s.getStrafpunkte()));
                }
                break;
            case Member:
                for (int x = 1; x <= 4; x++) {
                    String name = String.format("%s (%s. Schwimmer)", s.getName(), "" + x);
                    String shortname = String.format("%s#%s", s.getShortname(), "" + x);
                    strafen.add(new Strafe(name, shortname, s.getArt(), s.getStrafpunkte()));
                }
                break;
            default:
                break;
            }
            return strafen;
        }
    }

    private ErzeugeStrafen() {
        // Hide constructor
    }

    private static TeamPenaltyDefinition[] teamDefs = new TeamPenaltyDefinition[] {
            new TeamPenaltyDefinition("R1", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("R2", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("R3", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("R4", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("W1", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("W2", TeamPenaltyType.Change),
            new TeamPenaltyDefinition("W3", TeamPenaltyType.Change),
            new TeamPenaltyDefinition("W4", TeamPenaltyType.Change),
            new TeamPenaltyDefinition("W5", TeamPenaltyType.Change),
            new TeamPenaltyDefinition("W6", TeamPenaltyType.Change),
            new TeamPenaltyDefinition("W7", TeamPenaltyType.Change),
            new TeamPenaltyDefinition("S1", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("S4", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("S5", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("H1", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("H2", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("P1", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("P2", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("P3", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("P4", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("G3", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("G4", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("G5", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("G7", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("G8", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("G9", TeamPenaltyType.Member),
            new TeamPenaltyDefinition("G10", TeamPenaltyType.Member), };

    private static Collection<Strafe> expand(Strafe s, TeamPenaltyDefinition[] defs) {
        for (TeamPenaltyDefinition def : defs) {
            if (def.isMatching(s)) {
                return def.expand(s);
            }
        }
        return Arrays.asList(s);
    }

    public static void main(String[] args) throws IOException {
        int jahr = 2025;

        strafen("jauswertung/src/test/files/Strafen/Strafen " + jahr + ".xls",
                "jauswertung-files/src/main/resources/penalties/default.def",
                null);
        strafen("jauswertung/src/test/files/Strafen/Strafen " + jahr + ".xls",
                "jauswertung-files/src/main/resources/penalties/defaultm.def",
                teamDefs);
        // strafen("required/Strafen/Strafen International - Pool.xls",
        // "include/main/penalties/International - Pool.def");
        // strafen("required/Strafen/Strafen International - Pool.xls",
        // "include/main/penalties/International - Pool Mixed.def");
        // strafen("required/Strafen/Strafen International - OCEAN.xls",
        // "include/main/penalties/International - OCEAN.def");

    }

    private static void strafen(String source, String dest, TeamPenaltyDefinition[] defs) throws IOException {
        if (defs == null) {
            defs = new TeamPenaltyDefinition[0];
        }

        Strafen s = new Strafen();

        List<String> kapitelnamen = new LinkedList<>();
        Map<String, Map<String, List<Strafe>>> h = new HashMap<>();
        Map<String, List<String>> hp = new HashMap<>();

        Object[][] table = ExcelReader.sheetToTable(source, 0);

        for (int x = 1; x < table.length; x++) {
            String kapitel = table[x][0].toString().trim();
            Map<String, List<Strafe>> ht = h.computeIfAbsent(kapitel, key -> {
                Map<String, List<Strafe>> value = new HashMap<>();
                kapitelnamen.addLast(key);
                hp.put(key, new LinkedList<>());
                return value;
            });
            String bereich = table[x][1].toString().trim();
            List<Strafe> ll = ht.get(bereich);
            if (ll == null) {
                hp.get(kapitel).addLast(bereich);
                ll = new LinkedList<>();
                ht.put(bereich, ll);
            }
            Strafarten art = Strafarten.NICHTS;
            int hoehe = 0;

            Object artid = table[x][4];
            if (artid instanceof Number number) {
                art = Strafarten.STRAFPUNKTE;
                hoehe = number.intValue();
            } else {
                String st = artid.toString();
                if (st.equals("Disqualifikation")) {
                    art = Strafarten.DISQUALIFIKATION;
                } else {
                    if (st.equals("Ausschluss")) {
                        art = Strafarten.AUSSCHLUSS;
                    } else {
                        if (st.equals("n.a.")) {
                            art = Strafarten.NICHT_ANGETRETEN;
                        } else {
                            throw new RuntimeException("" + x + ": Unknown penalty " + st);
                        }
                    }
                }
            }
            ll.addLast(new Strafe(table[x][3].toString().trim(), table[x][2].toString().trim(), art, hoehe));
        }

        int x = 1;
        for (String kapitel : kapitelnamen) {
            StrafenKapitel sk = new StrafenKapitel(x);

            System.out.println(kapitel);
            sk.setName(kapitel);

            s.addKapitel(sk);

            Map<String, List<Strafe>> ll = h.get(kapitel);

            int y = 1;
            for (String bereich : hp.get(kapitel)) {
                System.out.println("  " + bereich);
                StrafenParagraph sp = new StrafenParagraph(y);
                sp.setName(bereich);

                sk.addParagraph(sp);

                for (Strafe st : ll.get(bereich)) {
                    for (Strafe st2 : expand(st, defs)) {
                        sp.addStrafe(st2);
                    }
                }

                y++;
            }
            x++;
        }

        StrafenKapitel sk = new StrafenKapitel(x);
        sk.setName("Sonstiges");

        StrafenParagraph sp = new StrafenParagraph(1);
        sp.setName("Benutzerdefiniert");
        sp.setPar("");

        sp.addStrafe(Strafe.NICHTS);
        sp.addStrafe(new Strafe("", "", Strafarten.STRAFPUNKTE, 0));
        sp.addStrafe(new Strafe("", "", Strafarten.STRAFPUNKTE, 50));
        sp.addStrafe(new Strafe("", "", Strafarten.STRAFPUNKTE, 100));
        sp.addStrafe(new Strafe("", "", Strafarten.STRAFPUNKTE, 200));
        sp.addStrafe(Strafe.NICHT_ANGETRETEN);
        sp.addStrafe(Strafe.NICHT_BEENDET);
        sp.addStrafe(Strafe.DISQUALIFIKATION);
        sp.addStrafe(Strafe.AUSSCHLUSS);

        sk.addParagraph(sp);
        s.addKapitel(sk);

        System.out.println("Schreibe Datei");

        OutputManager.speichereObject(dest, s);

        System.out.println("Fertig");
    }
}