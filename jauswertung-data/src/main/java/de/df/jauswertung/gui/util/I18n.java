/*
 * Created on 29.11.2003
 */
package de.df.jauswertung.gui.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.KeyStroke;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Geschlecht;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jutils.exception.ParserException;
import de.df.jutils.i18n.EmptyResourceBundle;
import de.df.jutils.i18n.SafeTextProcessor;
import de.df.jutils.resourcebundle.IdentityResourceBundle;
import de.df.jutils.resourcebundle.MultipleResourceBundle;
import de.df.jutils.resourcebundle.SafeResourceBundle;

/**
 * @author Dennis Fabri
 */
public final class I18n {

    private I18n() {
        // Never called
    }

    private static ResourceBundle fallback    = null;
    private static SafeTextProcessor   instance    = null;
    private static SafeTextProcessor   disziplinen = null;

    public static synchronized SafeTextProcessor getInstance() {
        if (instance == null) {
            MultipleResourceBundle rb = new MultipleResourceBundle();
            try {
                ResourceBundle fileRB = ResourceBundle.getBundle("jauswertung", Locale.getDefault(), I18n.class.getClassLoader());
                rb.add(fileRB);
            } catch (RuntimeException re) {
                re.printStackTrace();
                // Nothing to do
            }
            rb.add(getFallbackResourceBundle());
            rb.add(new IdentityResourceBundle());
            instance = new SafeTextProcessor(new SafeResourceBundle(rb));
        }
        return instance;
    }

    public static synchronized SafeTextProcessor getDisziplinenInstance() {
        if (disziplinen == null) {
            MultipleResourceBundle rb = new MultipleResourceBundle();
            rb.setVerbose(false);
            try {
                ResourceBundle fileRB = ResourceBundle.getBundle("disziplinen", Locale.getDefault(), I18n.class.getClassLoader());
                rb.add(fileRB);
            } catch (RuntimeException re) {
                re.printStackTrace();
                // Nothing to do
            }
            // rb.add(getFallbackResourceBundle());
            // rb.add(new IdentityResourceBundle());
            SafeResourceBundle srb = new SafeResourceBundle(rb);
            srb.setVerbose(false);
            disziplinen = new SafeTextProcessor(srb);
        }
        return disziplinen;
    }

    public static String getDisziplinShort(String id) {
        try {
            String s1 = id.replace(" ", "").replace("\u00e4", "ae").replace("\u00FC", "ue").replace("\u00f6", "oe").replace("\u00c4", "Ae")
                    .replace("\u00dc", "Ue").replace("\u00d6", "oe").replace("\u00df", "ss");
            String s2 = getDisziplinenInstance().process(s1);
            if (s1.equals(s2)) {
                return id;
            }
            return s2;
        } catch (ParserException pe) {
            return id;
        }
    }

    private static synchronized ResourceBundle getFallbackResourceBundle() {
        if (fallback == null) {
            fallback = new EmptyResourceBundle();
        }
        return fallback;
    }

    public static String get(final String key, final Object... dynamics) {
        return getInstance().process(key, dynamics);
    }

    public static KeyStroke getKeyStroke(String key) {
        return KeyStroke.getKeyStroke(get("KeyStroke." + key));
    }

    public static String getToolTip(final String key, final Object... dynamics) {
        String s = get("ToolTip." + key, dynamics);
        if (s.trim().length() == 0) {
            return null;
        }
        return s;
    }

    public static String get(final String key) {
        return getInstance().process(key, (Object[]) null);
    }

    public static String geschlechtToString(Regelwerk rw, boolean male) {
        return male ? rw.getTranslation("male", I18n.get("male")) : rw.getTranslation("female", I18n.get("female"));
    }

    public static String geschlechtToString(ASchwimmer s) {
        boolean male = s.isMaennlich();
        Regelwerk rw = s.getRegelwerk();
        return male ? rw.getTranslation("male", I18n.get("male")) : rw.getTranslation("female", I18n.get("female"));
    }

    public static String geschlechtToStringSubject(Regelwerk rw, boolean male) {
        return male ? rw.getTranslation("Male", I18n.get("Male")) : rw.getTranslation("Female", I18n.get("Female"));
    }

    public static String geschlechtToStringSubject(ASchwimmer s) {
        return geschlechtToStringSubject(s.getRegelwerk(), s.isMaennlich());
    }

    public static String yearToShortString(int year) {
        if (year <= 0) {
            return "";
        }
        String jahrstring = "" + (year % 100);
        if ((year % 100) < 10) {
            jahrstring = "0" + jahrstring;
        }
        return jahrstring;
    }

    public static Object yearToShortObject(int year) {
        if (year <= 0) {
            return "";
        }
        return year % 100;
    }

    public static Object yearToObject(int year) {
        if (year <= 0) {
            return "";
        }
        return year;
    }

    public static String yearToString(int year) {
        if (year <= 0) {
            return "";
        }
        return "" + year;
    }

    public static String geschlechtToShortString(Regelwerk rw, boolean male) {
        return (male ? rw.getTranslation("maleShort", get("maleShort")) : rw.getTranslation("femaleShort", get("femaleShort")));
    }

    public static String geschlechtToShortString(ASchwimmer s) {
        boolean male = s.isMaennlich();
        Regelwerk rw = s.getRegelwerk();
        return (male ? rw.getTranslation("maleShort", get("maleShort")) : rw.getTranslation("femaleShort", get("femaleShort")));
    }

    public static String getRound(int round, boolean isFinal) {
        if (isFinal) {
            return I18n.get("Finale");
        }
        if (round == 0) {
            return I18n.get("Preheat");
        }
        return I18n.get("IntermediateHeatNr", round);
    }

    public static String getAgeGroupAsString(ASchwimmer s) {
        return s.getAK().getName() + " " + geschlechtToString(s.getRegelwerk(), s.isMaennlich());
    }

    public static String getAgeGroupAsString(Regelwerk rw, Altersklasse ak, boolean maennlich) {
        return ak.getName() + " " + geschlechtToString(rw, maennlich);
    }

    public static String getAgeGroupAsStringShort(Regelwerk rw, Altersklasse ak, boolean maennlich) {
        return ak.getName() + " " + geschlechtToShortString(rw, maennlich);
    }

    public static String getAgeGroupAsString(Regelwerk rw, String ak, boolean maennlich) {
        return ak + " " + geschlechtToString(rw, maennlich);
    }

    public static String getAgeGroupAsStringShort(ASchwimmer s) {
        return s.getAK().getName() + " " + geschlechtToShortString(s.getRegelwerk(), s.isMaennlich());
    }

    public static String booleanToYesNo(boolean b) {
        if (b) {
            return get("yes");
        }
        return get("no");
    }

    public static String getPenaltyShort(Strafe s) {
        if (s.getShortname().length() > 0) {
            return s.getShortname();
        }

        switch (s.getArt()) {
        case NICHTS:
            return I18n.get("NoPenaltyShort");
        case STRAFPUNKTE:
            return I18n.get("PenaltyPointsShort", s.getStrafpunkte());
        case NICHT_ANGETRETEN:
            return I18n.get("DidNotStartShort");
        case DISQUALIFIKATION:
            return I18n.get("DisqualificationShort");
        case AUSSCHLUSS:
            return I18n.get("DebarmentShort");
        default:
            return "";
        }
    }

    public static String getSexShortString(Geschlecht geschlecht) {
        switch (geschlecht) {
        case maennlich:
            return I18n.get("maleShort");
        case weiblich:
            return I18n.get("femaleShort");
        default:
            return "-";
        }
    }

    public static String getSexShortString(boolean isMale) {
        return getSexShortString(isMale ? Geschlecht.maennlich : Geschlecht.weiblich);
    }

    public static Object getYearOfBirth(int jahrgang) {
        if (jahrgang <= 0) {
            return "";
        }
        return jahrgang;
    }

    public static String getVersion() {
        return I18n.get("VersionJAuswertung", I18n.get("Version"));
    }

    public static <T extends ASchwimmer> String getDisciplineFullName(AWettkampf<T> wk, String id) {
        OWDisziplin<T> d = wk.getLauflisteOW().getDisziplin(id);
        Altersklasse ak = wk.getRegelwerk().getAk(d.akNummer);
        boolean isFinal = ak.isFinal(d.disziplin, d.maennlich, d.round);

        return String.format("%s - %s - %s", I18n.getAgeGroupAsString(wk.getRegelwerk(), ak, d.maennlich), ak.getDisziplin(d.disziplin, d.maennlich).getName(),
                I18n.getRound(d.round, isFinal));
    }

    public static <T extends ASchwimmer> String getDisciplineName(AWettkampf<T> wk, String id) {
        OWDisziplin<T> d = wk.getLauflisteOW().getDisziplin(id);
        Altersklasse ak = wk.getRegelwerk().getAk(d.akNummer);
        boolean isFinal = ak.isFinal(d.disziplin, d.maennlich, d.round);

        return String.format("%s - %s", ak.getDisziplin(d.disziplin, d.maennlich).getName(), I18n.getRound(d.round, isFinal));
    }
    
    public static String toString(Exception ex) {
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }
}