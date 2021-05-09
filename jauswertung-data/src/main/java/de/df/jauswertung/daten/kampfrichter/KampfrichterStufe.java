package de.df.jauswertung.daten.kampfrichter;

public enum KampfrichterStufe {

    KEINE, F1, E2, F1E2, F1E3, F1D3, F1E23, F1E2D3, E1, E12, E13, E1D3, E123, E12D3, D12, D12E3, D123;

    @Override
    public String toString() {
        switch (this) {
        case F1:
            return "F1";
        case E2:
            return "E2";
        case F1E2:
            return "F1,E2";
        case F1E2D3:
            return "F1,E2,D3";
        case F1E23:
            return "F1,E2,E3";
        case F1D3:
            return "F1,D3";
        case F1E3:
            return "F1,E3";
        case E1:
            return "E1";
        case E13:
            return "E1,E3";
        case E1D3:
            return "E1,D3";
        case E12:
            return "E1,E2";
        case E12D3:
            return "E1,E2,D3";
        case E123:
            return "E1,E2,E3";
        case D12:
            return "D1";
        case D123:
            return "D1,D3";
        case D12E3:
            return "D1,E3";
        case KEINE:
            return "-";
        }
        return "";
    }

    public static String[] getBaseLevels() {
        return new String[] { "-", "F1", "E1", "E2", "E3", "D1", "D3" };
    }

    public static KampfrichterStufe getLevelFromBaseLevelsIndex(int index) {
        switch (index) {
        default:
            return KEINE;
        case 1:
            return F1;
        case 2:
            return E1;
        case 3:
            return E2;
        case 4:
            return F1E3;
        case 5:
            return D12;
        case 6:
            return F1D3;
        }
    }

    public static int getBaseIndex(KampfrichterStufe level) {
        switch (level) {
        case KEINE:
            return 0;
        case F1:
            return 1;
        case E1:
            return 2;
        case E2:
            return 3;
        case F1E3:
            return 4;
        case D12:
            return 5;
        case F1D3:
            return 6;
        default:
            throw new IndexOutOfBoundsException("Refereelevel is no baselevel: " + level.toString());
        }
    }

    public int[] toLevels() {
        int[] result = new int[3];
        switch (this) {
        case F1:
            result[0] = 1;
            result[1] = 0;
            result[2] = 0;
            break;
        case E2:
            result[0] = 0;
            result[1] = 2;
            result[2] = 0;
            break;
        case F1E2:
            result[0] = 1;
            result[1] = 2;
            result[2] = 0;
            break;
        case F1E2D3:
            result[0] = 1;
            result[1] = 2;
            result[2] = 3;
            break;
        case F1E23:
            result[0] = 1;
            result[1] = 2;
            result[2] = 2;
            break;
        case F1D3:
            result[0] = 1;
            result[1] = 0;
            result[2] = 3;
            break;
        case F1E3:
            result[0] = 1;
            result[1] = 0;
            result[2] = 2;
            break;
        case E1:
            result[0] = 2;
            result[1] = 0;
            result[2] = 0;
            break;
        case E13:
            result[0] = 2;
            result[1] = 0;
            result[2] = 2;
            break;
        case E1D3:
            result[0] = 2;
            result[1] = 0;
            result[2] = 3;
            break;
        case E12:
            result[0] = 2;
            result[1] = 2;
            result[2] = 0;
            break;
        case E12D3:
            result[0] = 2;
            result[1] = 2;
            result[2] = 3;
            break;
        case E123:
            result[0] = 2;
            result[1] = 2;
            result[2] = 2;
            break;
        case D12:
            result[0] = 3;
            result[1] = 3;
            result[2] = 0;
            break;
        case D123:
            result[0] = 3;
            result[1] = 3;
            result[2] = 3;
            break;
        case D12E3:
            result[0] = 3;
            result[1] = 3;
            result[2] = 2;
            break;
        case KEINE:
            result[0] = 0;
            result[1] = 0;
            result[2] = 0;
            break;
        }
        return result;
    }

    public boolean isAtLeast(KampfrichterStufe ks) {
        if (ks == null) {
            throw new NullPointerException();
        }
        KampfrichterStufe mit = mit(ks);
        return equals(mit);
    }

    public static KampfrichterStufe levelsToStufe(int[] levels) {
        for (int x = 0; x < 3; x++) {
            levels[x] = Math.max(Math.min(levels[x], 3), 0);
        }
        if (levels[1] > 1) {
            levels[1] = 2;
        }
        if (levels[2] == 1) {
            levels[2] = 2;
        }
        if (levels[2] > 0) {
            levels[0] = Math.max(levels[0], 1);
        }
        if (levels[0] == 3) {
            levels[1] = 2;
        }

        int level = levels[0] * 100 + levels[1] * 10 + levels[2];
        switch (level) {
        case 0:
            return KEINE;
        case 10:
        case 20:
            return E2;
        case 100:
            return F1;
        case 102:
            return F1E3;
        case 103:
            return F1D3;
        case 120:
            return F1E2;
        case 122:
            return F1E23;
        case 123:
            return F1E2D3;
        case 200:
            return E1;
        case 202:
            return E13;
        case 203:
            return E1D3;
        case 220:
            return E12;
        case 222:
            return E123;
        case 223:
            return E12D3;
        case 320:
            return D12;
        case 322:
            return D12E3;
        case 323:
            return D123;
        }
        return KEINE;
    }

    public KampfrichterStufe ohne(KampfrichterStufe ks) {
        int[] levels = toLevels();
        switch (ks) {
        case F1:
            levels[0] = 0;
            levels[1] = (levels[1] > 0 ? 2 : 0);
            levels[2] = 0;
            break;
        case E2:
            levels[0] = (levels[0] > 2 ? 2 : levels[0]);
            levels[1] = 0;
            levels[2] = levels[2];
            break;
        case F1E2:
            levels[0] = 0;
            levels[1] = 0;
            levels[2] = 0;
            break;
        case F1E2D3:
            levels[0] = (levels[0] > 2 ? 2 : levels[0]);
            levels[1] = 0;
            levels[2] = (levels[2] > 2 ? 2 : levels[2]);
            break;
        case F1E23:
            levels[0] = (levels[0] > 2 ? 2 : levels[0]);
            levels[1] = 0;
            levels[2] = 0;
            break;
        case F1D3:
            levels[0] = levels[0];
            levels[1] = levels[1];
            levels[2] = (levels[2] > 2 ? 2 : levels[2]);
            break;
        case F1E3:
            levels[0] = levels[0];
            levels[1] = levels[1];
            levels[2] = 0;
            break;
        case E1:
            levels[0] = (levels[0] > 1 ? 1 : levels[1]);
            levels[1] = levels[1];
            levels[2] = levels[2];
            break;
        case E13:
            levels[0] = (levels[0] > 1 ? 1 : levels[1]);
            levels[1] = levels[1];
            levels[2] = 0;
            break;
        case E1D3:
            levels[0] = (levels[0] > 1 ? 1 : levels[1]);
            levels[1] = levels[1];
            levels[2] = (levels[2] > 2 ? 2 : levels[2]);
            break;
        case E12:
            levels[0] = (levels[0] > 1 ? 1 : levels[1]);
            levels[1] = 0;
            levels[2] = levels[2];
            break;
        case E12D3:
            levels[0] = (levels[0] > 1 ? 1 : levels[1]);
            levels[1] = 0;
            levels[2] = (levels[2] > 2 ? 2 : levels[2]);
            break;
        case E123:
            levels[0] = (levels[0] > 1 ? 1 : levels[1]);
            levels[1] = 0;
            levels[2] = 0;
            break;
        case D12:
            levels[0] = (levels[0] > 2 ? 2 : levels[1]);
            levels[1] = (levels[1] > 0 ? 2 : 0);
            levels[2] = levels[2];
            break;
        case D123:
            levels[0] = (levels[0] > 2 ? 2 : levels[1]);
            levels[1] = (levels[1] > 0 ? 2 : 0);
            levels[2] = (levels[2] > 2 ? 2 : levels[2]);
            break;
        case D12E3:
            levels[0] = (levels[0] > 2 ? 2 : levels[1]);
            levels[1] = (levels[1] > 0 ? 2 : 0);
            levels[2] = 0;
            break;
        case KEINE:
            break;
        }
        return levelsToStufe(levels);
    }

    public KampfrichterStufe mit(KampfrichterStufe ks) {
        int[] levels1 = toLevels();
        int[] levels2 = ks.toLevels();
        for (int x = 0; x < levels1.length; x++) {
            levels1[x] = Math.max(levels1[x], levels2[x]);
        }
        return levelsToStufe(levels1);
    }
}