package de.df.jauswertung.gui.plugins.meldezeiten;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JIntegerField.Validator;
import de.df.jutils.gui.JTimeField;
import de.df.jutils.gui.layout.FormLayoutUtils;

class JMeldezeitenPanel<T extends ASchwimmer> extends JPanel {

    private static final long serialVersionUID = -5048680404158227209L;

    private AWettkampf<T> wk;
    private Altersklasse ak;
    private boolean male;

    private JMeldezeiten<T> parent;

    ASchwimmer[] swimmer;
    private JIntegerField[][] integer;
    private JTimeField[][] times;

    public JMeldezeitenPanel(JMeldezeiten<T> p, AWettkampf<T> w, Altersklasse a, boolean m) {
        parent = p;
        wk = w;
        ak = a;
        male = m;

        int disziplinen = ak.getDiszAnzahl();

        swimmer = SearchUtils.getSchwimmer(wk, ak, male)
                .toArray(new ASchwimmer[SearchUtils.getSchwimmer(wk, ak, male).size()]);

        String horizontal = "4dlu,fill:default,4dlu,fill:default,"
                + FormLayoutUtils.createGrowingLayoutString(disziplinen * 2);

        int[][] columngroups = new int[2][0];
        columngroups[0] = new int[disziplinen + 1];
        columngroups[1] = new int[disziplinen * 2];

        columngroups[0][0] = 1;

        StringBuilder sb = new StringBuilder();
        sb.append("8dlu:grow,fill:default,4dlu,fill:default");
        for (int x = 0; x < disziplinen; x++) {
            sb.append(",4dlu:grow,fill:default,4dlu,fill:default");
            columngroups[0][1 + x] = 5 + 4 * x;
            columngroups[1][2 * x] = 6 + 4 * x;
            columngroups[1][2 * x + 1] = 8 + 4 * x;
        }
        sb.append(",4dlu:grow");
        horizontal = sb.toString();

        FormLayout layout = new FormLayout(horizontal, FormLayoutUtils.createLayoutString(swimmer.length + 1));
        layout.setColumnGroups(columngroups);
        setLayout(layout);
        add(new JLabel(I18n.get("Name")), CC.xy(2, 2, "center,center"));
        add(new JLabel(I18n.get("Organisation")), CC.xy(4, 2, "center,center"));
        for (int x = 0; x < disziplinen; x++) {
            add(new JLabel(a.getDisziplin(x, true).getName()), CC.xyw(6 + 4 * x, 2, 3, "center,center"));
        }

        integer = new JIntegerField[swimmer.length][disziplinen];
        times = new JTimeField[swimmer.length][disziplinen];
        for (int x = 0; x < swimmer.length; x++) {
            add(new JLabel(swimmer[x].getName()), CC.xy(2, 4 + 2 * x));
            add(new JLabel(swimmer[x].getGliederungMitQGliederung()), CC.xy(4, 4 + 2 * x));
            for (int y = 0; y < disziplinen; y++) {
                integer[x][y] = new JIntegerField(JIntegerField.EMPTY_FIELD, JTimeField.MAX_TIME, false, true);
                integer[x][y].setHorizontalAlignment(SwingConstants.RIGHT);
                times[x][y] = new JTimeField(integer[x][y]);
                integer[x][y].setValidator((Validator)value -> {
                    value = value / 100;
                    if ((value % 100) >= 60) {
                        return false;
                    }
                    value = value / 100;
                    return value < 1000;
                });
                times[x][y].setTimeAsInt(swimmer[x].getMeldezeit(y));
                integer[x][y].getDocument().addDocumentListener(new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent arg0) {
                        changedUpdate(arg0);
                    }

                    @Override
                    public void removeUpdate(DocumentEvent arg0) {
                        changedUpdate(arg0);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent arg0) {
                        notifyParent();
                    }
                });
                add(integer[x][y], CC.xy(6 + 4 * y, 4 + 2 * x));
                add(times[x][y], CC.xy(8 + 4 * y, 4 + 2 * x));

                integer[x][y].setEnabled(swimmer[x].isDisciplineChosen(y));
                integer[x][y].setAutoSelectAll(true);
                integer[x][y].setHorizontalAlignment(SwingConstants.RIGHT);
            }
        }
    }

    public boolean isDataValid() {
        for (JTimeField[] time : times) {
            for (int y = 0; y < time.length; y++) {
                if (!time[y].isValidValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void accept() {
        for (int x = 0; x < times.length; x++) {
            for (int y = 0; y < times[x].length; y++) {
                swimmer[x].setMeldezeit(y, times[x][y].getTimeAsInt());
            }
        }
    }

    void notifyParent() {
        parent.isChanging();
    }
}