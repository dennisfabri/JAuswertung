/*
 * Created on 23.02.2006
 */
package de.df.jauswertung.misc;

import java.io.IOException;
import java.util.*;

import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.io.*;

public final class ErzeugeStrafen {
    private static enum TeamPenaltyType {
        Member, Change
    }

    private static class TeamPenaltyDefinition {
        private final String          code;
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

            ArrayList<Strafe> strafen = new ArrayList<Strafe>();
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
        int jahr = 2020;

        strafen("src/test/files/Strafen/Strafen " + jahr + ".xls", "src/main/files/penalties/default.def",
                null);
        strafen("src/test/files/Strafen/Strafen " + jahr + ".xls", "src/main/files/penalties/defaultm.def",
                teamDefs);
        // strafen("required/Strafen/Strafen International - Pool.xls",
        // "include/main/penalties/International - Pool.def");
        // strafen("required/Strafen/Strafen International - Pool.xls",
        // "include/main/penalties/International - Pool Mixed.def");
        // strafen("required/Strafen/Strafen International - Ocean.xls",
        // "include/main/penalties/International - Ocean.def");

    }

    private static void strafen(String source, String dest, TeamPenaltyDefinition[] defs) throws IOException {
        if (defs == null) {
            defs = new TeamPenaltyDefinition[0];
        }

        Strafen s = new Strafen();

        LinkedList<String> kapitelnamen = new LinkedList<String>();
        Hashtable<String, Hashtable<String, LinkedList<Strafe>>> h = new Hashtable<String, Hashtable<String, LinkedList<Strafe>>>();
        Hashtable<String, LinkedList<String>> hp = new Hashtable<String, LinkedList<String>>();

        Object[][] table = ExcelReader.sheetToTable(source, 0);

        for (int x = 1; x < table.length; x++) {
            String kapitel = table[x][0].toString().trim();
            Hashtable<String, LinkedList<Strafe>> ht = h.get(kapitel);
            if (ht == null) {
                ht = new Hashtable<String, LinkedList<Strafe>>();
                h.put(kapitel, ht);
                kapitelnamen.addLast(kapitel);
                hp.put(kapitel, new LinkedList<String>());
            }
            String bereich = table[x][1].toString().trim();
            LinkedList<Strafe> ll = ht.get(bereich);
            if (ll == null) {
                hp.get(kapitel).addLast(bereich);
                ll = new LinkedList<Strafe>();
                ht.put(bereich, ll);
            }
            Strafarten art = Strafarten.NICHTS;
            int hoehe = 0;

            Object artid = table[x][4];
            if (artid instanceof Number) {
                art = Strafarten.STRAFPUNKTE;
                hoehe = ((Number) artid).intValue();
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

            Hashtable<String, LinkedList<Strafe>> ll = h.get(kapitel);

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
        sp.addStrafe(Strafe.DISQUALIFIKATION);
        sp.addStrafe(Strafe.AUSSCHLUSS);

        sk.addParagraph(sp);
        s.addKapitel(sk);

        System.out.println("Schreibe Datei");

        OutputManager.speichereObject(dest, s);

        System.out.println("Fertig");
    }
}