package de.df.jauswertung.util.format;

import java.util.Arrays;
import java.util.Hashtable;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;

public class StartnumberFormatManager {

    private static StartnumberFormatManager instance = null;

    private final Hashtable<String, IStartnumberFormat> formats = new Hashtable<String, IStartnumberFormat>();

    private StartnumberFormatManager() {
        add(new StartnumberDefault());
        add(new StartnumberDigits(1));
        add(new StartnumberDigits(2));
    }

    private String[] getFormatsI() {
        String[] keys = formats.keySet().toArray(new String[formats.size()]);
        Arrays.sort(keys);
        return keys;
    }

    public void add(IStartnumberFormat format) {
        formats.put(format.GetId(), format);
    }

    private String format(String id, int sn) {
        return formats.get(id).Format(sn);
    }

    private int convert(String id, String sn) {
        return formats.get(id).Convert(sn);
    }

    private static StartnumberFormatManager getInstance() {
        if (instance == null) {
            instance = new StartnumberFormatManager();
        }
        return instance;
    }

    public static String format(ASchwimmer swimmer) {
        return getInstance().format(
                swimmer.getWettkampf().getStringProperty(PropertyConstants.STARTNUMBERFORMAT, "Default"),
                swimmer.getStartnummer());
    }

    public static <T extends ASchwimmer> int convert(AWettkampf<T> wk, String sn) {
        return getInstance().convert(wk.getStringProperty(PropertyConstants.STARTNUMBERFORMAT, "Default"), sn);
    }

    public static String[] getFormats() {
        return getInstance().getFormatsI();
    }

    public static int GetIndex(String format) {
        String[] formats = getFormats();
        for (int x = 0; x < formats.length; x++) {
            if (formats[x].equals(format)) {
                return x;
            }
        }
        return -1;
    }
}
