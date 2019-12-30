package de.df.jauswertung.util.format;

public class StartnumberDigits implements IStartnumberFormat {

    @Override
    public String GetId() {
        return id;
    }

    private final int    limit;
    private final String id;

    StartnumberDigits(int digits) {
        int i = 1;
        for (int x = 0; x < digits; x++) {
            i *= 10;
        }
        limit = i;
        id = "Digits" + digits;
    }

    @Override
    public String Format(int i) {
        StringBuilder sb = new StringBuilder();

        int left = i / limit;
        int right = i % limit;

        sb.append(left);
        sb.append("-");
        sb.append(right);

        return sb.toString();
    }

    @Override
    public int Convert(String sn) {
        if (sn == null || sn.trim().length() == 0) {
            return 0;
        }
        String[] parts = sn.split("-");
        return Integer.parseInt(parts[0]) * limit + Integer.parseInt(parts[1]);
    }
}