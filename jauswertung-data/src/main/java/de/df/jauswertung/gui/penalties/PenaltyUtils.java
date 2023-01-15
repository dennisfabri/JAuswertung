/*
 * Created on 03.05.2005
 */
package de.df.jauswertung.gui.penalties;

import java.text.NumberFormat;
import java.util.ListIterator;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.util.I18n;

public final class PenaltyUtils {

    private PenaltyUtils() {
        // Hide
    }

    public static String getPenaltyValue(Strafe strafe, Altersklasse ak) {
        if (strafe == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();

        if (ak != null) {
            strafe = new Strafe(strafe, ak.isStrafeIstDisqualifikation());
        }
        switch (strafe.getArt()) {
        case AUSSCHLUSS:
            text.append(I18n.get("DebarmentShort"));
            break;
        case DISQUALIFIKATION:
            text.append(I18n.get("DisqualifiedShort"));
            break;
        case NICHT_ANGETRETEN:
            text.append(I18n.get("DidNotStartShort"));
            break;
        case STRAFPUNKTE:
            int hoehe = strafe.getStrafpunkte();
            text.append(hoehe);
            break;
        case NICHTS:
            text.append("-");
            break;
        default:
            break;
        }
        return text.toString();
    }

    public static String getPenaltyShortText(Strafe strafe, Altersklasse ak) {
        if (strafe == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();

        if (ak != null) {
            strafe = new Strafe(strafe, ak.isStrafeIstDisqualifikation());
        }
        if (strafe.getShortname().length() > 0) {
            text.append(strafe.getShortname());
        } else {
            switch (strafe.getArt()) {
            case AUSSCHLUSS:
                text.append(I18n.get("DebarmentShort"));
                break;
            case DISQUALIFIKATION:
                text.append(I18n.get("DisqualifiedShort"));
                break;
            case NICHT_ANGETRETEN:
                text.append(I18n.get("DidNotStartShort"));
                break;
            case STRAFPUNKTE:
                int hoehe = strafe.getStrafpunkte();
                text.append(hoehe);
                break;
            case NICHTS:
                text.append(" ");
                break;
            default:
                break;
            }
        }
        return text.toString();
    }

    public static <T extends ASchwimmer> boolean hasPenalties(AWettkampf<T> wk) {
        if (wk == null) {
            return false;
        }

        ListIterator<T> li = wk.getSchwimmer().listIterator();
        while (li.hasNext()) {
            boolean result = hasPenalties(li.next());
            if (result) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasPenalties(ASchwimmer s) {
        Altersklasse ak = s.getAK();
        for (int x = 0; x < ak.getDiszAnzahl(); x++) {
            Strafe strafe = s.getAkkumulierteStrafe(x);
            if ((strafe != null) && (strafe.isStrafe())) {
                return true;
            }
        }
        return false;
    }

    public static String getPenaltyText(Strafe s, Altersklasse ak) {
        if (s == null) {
            return "";
        }
        s = new Strafe(s, ak.isStrafeIstDisqualifikation());
        StringBuilder text = new StringBuilder();
        if (s.getShortname().length() > 0) {
            text.append(s.getShortname());
            text.append(": ");
        }
        switch (s.getArt()) {
        case AUSSCHLUSS:
            text.append(I18n.get("Debarment"));
            break;
        case DISQUALIFIKATION:
            text.append(I18n.get("Disqualification"));
            break;
        case NICHT_ANGETRETEN:
            text.append(I18n.get("DidNotStart"));
            break;
        case NICHTS:
            text.append(I18n.get("NoPenalty"));
            break;
        case STRAFPUNKTE:
            text.append(NumberFormat.getInstance().format(s.getStrafpunkte()));
            break;
        default:
            text.append(" ");
            break;
        }
        return text.toString();
    }

    public static String getPenaltyMediumText(Strafe s, Altersklasse ak) {
        if (s == null) {
            return "";
        }
        s = new Strafe(s, ak.isStrafeIstDisqualifikation());

        String text1 = "";
        String text2 = "";
        if (s.getShortname().length() > 0) {
            text1 = s.getShortname();
            // text.append(": ");
        }
        switch (s.getArt()) {
        case AUSSCHLUSS:
            text2 = I18n.get("DebarmentShort");
            break;
        case DISQUALIFIKATION:
            if (!ak.isStrafeIstDisqualifikation() || s.getShortname().length() == 0) {
                text2 = I18n.get("DisqualificationShort");
            }
            break;
        case NICHT_ANGETRETEN:
            text2 = I18n.get("DidNotStartShort");
            break;
        case NICHTS:
            text2 = I18n.get("NoPenaltyShort");
            break;
        case STRAFPUNKTE:
            text2 = NumberFormat.getInstance().format(s.getStrafpunkte());
            break;
        default:
            break;
        }

        StringBuilder text = new StringBuilder();
        text.append(text1);
        if (text1.length() > 0 && text2.length() > 0) {
            text.append(": ");
        }
        text.append(text2);
        return text.toString();
    }
}