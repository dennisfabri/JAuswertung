package de.df.jauswertung.gui.plugins.print;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.CompetitionUtils.MedalCount;

class MedalTableModel extends AbstractTableModel {

    private static String[] titles = new String[] { I18n.get("Rank"), I18n.get("Organisation"),
            "  " + I18n.get("Gold") + "  ",
            "  " + I18n.get("Silver") + "  ", "  " + I18n.get("Bronze") + "  " };

    private final List<MedalCount> medals;

    MedalTableModel(List<MedalCount> medals) {
        this.medals = medals;
    }

    @Override
    public String getColumnName(int column) {
        return titles[column];
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public int getRowCount() {
        return medals.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        MedalCount medal = medals.get(rowIndex);
        switch (columnIndex) {
        case 0:
            int rank = medal.rank();
            if (rank <= 0) {
                return "";
            }
            return rank;
        case 1:
            return medal.gliederung();
        case 2:
            return medal.gold();
        case 3:
            return medal.silver();
        case 4:
            return medal.bronze();
        default:
            return null;
        }
    }
}
