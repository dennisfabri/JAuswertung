package de.df.jauswertung.util.format;

public class StartnumberDefault implements IStartnumberFormat {

    @Override
    public String GetId() {
        return "Default";
    }

    @Override
    public String Format(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(i);
        return sb.toString();
    }

    @Override
    public int Convert(String sn) {
        if (sn == null || sn.trim().length() == 0) {
            return 0;
        }
        return Integer.parseInt(sn);
    }
}