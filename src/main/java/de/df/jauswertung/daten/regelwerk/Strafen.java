/*
 * Strafen.java Created on 7. August 2001, 21:39
 */

package de.df.jauswertung.daten.regelwerk;

/**
 * @author Dennis Fabri
 * @version
 */

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import de.df.jauswertung.gui.penalties.PenaltyUtils;

public final class Strafen implements Serializable {

    private static final long          serialVersionUID = 1664311476306024507L;

    private LinkedList<StrafenKapitel> strafen;

    /** Creates new Strafen */
    public Strafen() {
        strafen = new LinkedList<StrafenKapitel>();
    }

    public LinkedList<StrafenKapitel> getKapitel() {
        return new LinkedList<StrafenKapitel>(strafen);
    }

    public final Strafe getStrafe(String code) {
        if (code.length() == 0) {
            return null;
        }
        ListIterator<StrafenKapitel> li = strafen.listIterator();
        while (li.hasNext()) {
            Strafe s = li.next().getStrafe(code);
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
        LinkedList<Strafe> ps = new LinkedList<Strafe>();

        ListIterator<StrafenKapitel> kapitel = getKapitel().listIterator();
        while (kapitel.hasNext()) {
            ListIterator<StrafenParagraph> paragraph = kapitel.next().getParagraphen().listIterator();
            while (paragraph.hasNext()) {
                ListIterator<Strafe> strafenli = paragraph.next().getStrafen().listIterator();
                while (strafenli.hasNext()) {
                    Strafe str = strafenli.next();
                    ps.addLast(str);
                }
            }
        }

        Collections.sort(ps, new Comparator<Strafe>() {
            @Override
            public int compare(Strafe o1, Strafe o2) {
                return PenaltyUtils.getPenaltyShortText(o1, null).compareToIgnoreCase(PenaltyUtils.getPenaltyShortText(o2, null));
            }
        });

        return ps;
    }

    private static final String[] dnsNames     = new String[] { "DNS", "D.N.S.", "n.a." };
    private static final String[] dnfNames     = new String[] { "DNF", "D.N.F.", "S1" };
    private static final String[] wdNames      = new String[] { "WD", "Withdraw" };

    private static final Strafe   DIDNOTFINISH = new Strafe("Did not finish", "DNF", Strafarten.DISQUALIFIKATION, 0);
    private static final Strafe   WITHDRAW     = new Strafe("Withdraw", "WD", Strafarten.NICHTS, 0);

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
        return DIDNOTFINISH;
    }

    public Strafe getWithdraw() {
        for (String na : wdNames) {
            Strafe s = getStrafe(na);
            if ((s != null) && (s.getArt() == Strafarten.NICHTS)) {
                return s;
            }
        }
        return WITHDRAW;
    }
}