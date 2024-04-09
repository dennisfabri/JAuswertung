package de.df.jauswertung.gui.util;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.io.util.ZWUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;

public class JZWStatusPanel extends JPanel {

    private JLabel[][] statusFields = new JLabel[0][0];
    private JLabel[] ageGroupNames = new JLabel[0];
    private final JLabel[] titles = new JLabel[2];
    private JLabel[] sex = new JLabel[0];

    public JZWStatusPanel() {
        titles[0] = new JLabel(I18n.get("Input.Missing"));
        titles[1] = new JLabel(I18n.get("Input.Entered"));
    }

    @SuppressWarnings({ "unchecked" })
    public void setData(@SuppressWarnings("rawtypes") AWettkampf wk) {
        int[][][] status = ZWUtils.getHLWStatus(wk);

        int size = 0;
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            if (aks.getAk(x).hasHLW()) {
                size++;
            }
        }

        if (size != ageGroupNames.length) {
            JLabel[] aknamesneu = new JLabel[size];
            JLabel[][] statineu = new JLabel[size * 2][titles.length];
            JLabel[] sexneu = new JLabel[2 * size];
            for (int x = 0; x < Math.min(size, ageGroupNames.length); x++) {
                aknamesneu[x] = ageGroupNames[x];
                for (int y = 0; y < 2; y++) {
                    sexneu[x * 2 + y] = sex[2 * x + y];
                    System.arraycopy(statusFields[x * 2 + y], 0, statineu[x * 2 + y], 0, titles.length);
                }
            }
            for (int x = Math.min(size, ageGroupNames.length); x < size; x++) {
                aknamesneu[x] = new JLabel();
                for (int y = 0; y < 2; y++) {
                    sexneu[x * 2 + y] = new JLabel(I18n.get(y == 1 ? "maleShort" : "femaleShort"));
                    for (int z = 0; z < titles.length; z++) {
                        statineu[x * 2 + y][z] = new JLabel();
                    }
                }
            }

            ageGroupNames = aknamesneu;
            sex = sexneu;
            statusFields = statineu;

            removeAll();

            FormLayout layout = new FormLayout(
                    "4dlu,fill:default," + FormLayoutUtils.createGrowingLayoutString(2 * size),
                    FormLayoutUtils.createLayoutString(2 + titles.length));
            setLayout(layout);

            for (int x = 0; x < titles.length; x++) {
                add(titles[x], CC.xy(2, 6 + 2 * x));
            }

            int pos = 0;
            for (int x = 0; x < aks.size(); x++) {
                if (aks.getAk(x).hasHLW()) {
                    add(ageGroupNames[pos], CC.xyw(4 + 4 * pos, 2, 3, "center,fill"));
                    add(sex[2 * pos], CC.xy(4 + 4 * pos, 4, "center,fill"));
                    add(sex[2 * pos + 1], CC.xy(4 + 4 * pos + 2, 4, "center,fill"));

                    for (int y = 0; y < status[x].length; y++) {
                        for (int z = 0; z < titles.length; z++) {
                            add(statusFields[2 * pos + y][z], CC.xy(4 + 4 * pos + 2 * y, 6 + 2 * z, "center,fill"));
                        }
                    }

                    pos++;
                }
            }
        }

        int pos = 0;
        for (int x = 0; x < aks.size(); x++) {
            if (aks.getAk(x).hasHLW()) {
                ageGroupNames[pos].setText(aks.getAk(x).getName());
                for (int y = 0; y < status[x].length; y++) {
                    statusFields[2 * pos + y][0].setText("" + status[x][y][0]);
                    statusFields[2 * pos + y][1].setText("" + status[x][y][4]);
                }

                pos++;
            }
        }
        this.invalidate();
    }
}