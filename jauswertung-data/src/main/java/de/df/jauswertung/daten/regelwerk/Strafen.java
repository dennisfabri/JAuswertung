package de.df.jauswertung.daten.regelwerk;

import de.df.jauswertung.gui.penalties.PenaltyUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

public final class Strafen implements Serializable {

    @Serial
    private static final long serialVersionUID = 1664311476306024507L;

    private static final String[] dnsNames = new String[]{"DNS", "D.N.S.", "n.a."};
    private static final String[] dnfNames = new String[]{"DNF", "D.N.F.", "n.b.", "S1"};
    private static final String[] wdNames = new String[]{"WD", "Withdraw"};

    private LinkedList<StrafenKapitel> strafen;

    /**
     * Creates new Strafen
     */
    public Strafen() {
        strafen = new LinkedList<>();
    }

    public LinkedList<StrafenKapitel> getKapitel() {
        return new LinkedList<>(strafen);
    }

    public final Strafe getStrafe(String code) {
        if (code.isEmpty()) {
            return null;
        }
        for (StrafenKapitel strafenKapitel : strafen) {
            Strafe s = strafenKapitel.getStrafe(code);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    public void addKapitel(StrafenKapitel k) {
        strafen.addLast(k);
    }

    public LinkedList<Strafe> getStrafenListe() {
        LinkedList<Strafe> ps = new LinkedList<>();

        for (StrafenKapitel strafenKapitel : getKapitel()) {
            ListIterator<StrafenParagraph> paragraph = strafenKapitel.getParagraphen().listIterator();
            while (paragraph.hasNext()) {
                for (Strafe str : paragraph.next().getStrafen()) {
                    ps.addLast(str);
                }
            }
        }

        ps.sort(new Comparator<Strafe>() {
            @Override
            public int compare(Strafe o1, Strafe o2) {
                return PenaltyUtils.getPenaltyShortText(o1, null)
                                   .compareToIgnoreCase(PenaltyUtils.getPenaltyShortText(o2, null));
            }
        });

        return ps;
    }

    public Strafe getNichtAngetreten() {
        for (String na : dnsNames) {
            Strafe s = getStrafe(na);
            if ((s != null) && (s.getArt() == Strafarten.NICHT_ANGETRETEN)) {
                return s;
            }
        }
        return Strafe.NICHT_ANGETRETEN;
    }

    public Strafe getDisqualifiziert() {
        Strafe s = getStrafe("S1");
        if ((s != null) && (s.getArt() == Strafarten.DISQUALIFIKATION)) {
            return s;
        }
        return Strafe.DISQUALIFIKATION;
    }

    public Strafe getDidNotFinish() {
        for (String na : dnfNames) {
            Strafe s = getStrafe(na);
            if ((s != null) && (s.getArt() == Strafarten.DISQUALIFIKATION)) {
                return s;
            }
        }
        return Strafe.NICHT_BEENDET;
    }

    public Strafe getWithdraw() {
        for (String na : wdNames) {
            Strafe s = getStrafe(na);
            if ((s != null) && (s.getArt() == Strafarten.NICHTS)) {
                return s;
            }
        }
        return Strafe.WITHDRAW;
    }
}