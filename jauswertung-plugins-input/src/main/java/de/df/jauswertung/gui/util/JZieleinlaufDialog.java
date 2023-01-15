package de.df.jauswertung.gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

public class JZieleinlaufDialog<T extends ASchwimmer> extends JDialog {

    private final class ZieleinlaufActionListener implements ActionListener {

        private ISimpleCallback<Boolean> cb;
        private boolean result;

        public ZieleinlaufActionListener(ISimpleCallback<Boolean> cb, boolean result) {
            this.cb = cb;
            this.result = result;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            cb.callback(result);
        }
    }

    private static final long serialVersionUID = 4830320298919358601L;

    private JFrame parent;

    public JZieleinlaufDialog(JFrame parent, Lauf<T> lauf, ISimpleCallback<Boolean> cb) {
        super(parent, I18n.get("Zieleinlauf"), true);
        String[][] data = getData(lauf);

        init(parent, data, cb);
    }

    public JZieleinlaufDialog(JFrame parent, OWLauf<T> lauf, ISimpleCallback<Boolean> cb) {
        super(parent, I18n.get("Zieleinlauf"), true);
        String[][] data = getData(lauf);

        init(parent, data, cb);
    }

    private void init(JFrame parent, String[][] data, ISimpleCallback<Boolean> cb) {
        this.parent = parent;

        int rows = data.length;

        JComponent buttons = null;

        if (cb != null) {
            FormLayout layout = new FormLayout("0dlu,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
            layout.setColumnGroups(new int[][] { { 2, 4 } });
            JPanel p = new JPanel(layout);
            JButton ok = new JButton(I18n.get("Next"), IconManager.getSmallIcon("ok"));
            ok.addActionListener(new ZieleinlaufActionListener(cb, true));
            JButton close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
            close.addActionListener(new ZieleinlaufActionListener(cb, false));

            p.add(ok, CC.xy(2, 2));
            p.add(close, CC.xy(4, 2));

            buttons = p;
        } else {
            JButton ok = new JButton(I18n.get("Ok"));
            ok.addActionListener(e -> {
                setVisible(false);
            });
            buttons = ok;
        }

        FormLayout layout = new FormLayout("4dlu:grow,center:default,4dlu,center:default,0dlu:grow,4dlu",
                FormLayoutUtils.createLayoutString(rows + 2));
        setLayout(layout);

        add(new JLabel(I18n.get("Rank")), CC.xy(2, 2));
        add(new JLabel(I18n.get("Lane")), CC.xy(4, 2));
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < 2; y++) {
                add(new JLabel(data[x][y]), CC.xy(2 + 2 * y, 4 + 2 * x));
            }
        }
        add(buttons, CC.xyw(2, 4 + 2 * rows, 4, "right,fill"));

        WindowUtils.addEscapeAction(this, new WindowUtils.CloseRunnable(this));

        pack();
        int width = getWidth();
        int height = getHeight();
        UIStateUtils.uistatemanage(parent, this, "Zieleinlauf");
        setSize(width, height);
        setResizable(false);
    }

    @Override
    public void setVisible(boolean b) {
        parent.setEnabled(!b);
        super.setVisible(b);
    }

    private T getSchwimmer(Object lauf, int pos) {
        if (lauf instanceof Lauf) {
            Lauf<T> f = (Lauf<T>) lauf;
            return f.getSchwimmer(pos);
        }
        OWLauf<T> f = (OWLauf<T>) lauf;
        return f.getSchwimmer(pos);
    }

    private int getBahnen(Object lauf) {
        if (lauf instanceof Lauf) {
            Lauf<T> f = (Lauf<T>) lauf;
            return f.getBahnen();
        }
        OWLauf<T> f = (OWLauf<T>) lauf;
        return f.getBahnen();
    }

    private int getZeit(Object lauf, int pos) {
        T s = getSchwimmer(lauf, pos);
        if (lauf instanceof Lauf) {
            Lauf<T> f = (Lauf<T>) lauf;
            return s.getZeit(f.getDisznummer(pos));
        }
        OWLauf<T> f = (OWLauf<T>) lauf;
        return s.getZeit(f.getDisciplineId());
    }

    private Strafarten getStrafe(Object lauf, int pos) {
        T s = getSchwimmer(lauf, pos);
        if (lauf instanceof Lauf) {
            Lauf<T> f = (Lauf<T>) lauf;
            return s.getAkkumulierteStrafe(f.getDisznummer(pos)).getArt();
        }
        OWLauf<T> f = (OWLauf<T>) lauf;
        return s.getAkkumulierteStrafe(f.getDisciplineId()).getArt();
    }

    private String[][] getData(Object lauf) {
        String[][] result = new String[getBahnen(lauf)][2];
        for (int x = 0; x < result.length; x++) {
            result[x][0] = "" + (x + 1) + ".";
            result[x][1] = "";
        }

        boolean finished = false;

        long time = 0;
        int index = 0;
        int nextindex = 0;

        do {
            long low = Long.MAX_VALUE;
            for (int x = 0; x < result.length; x++) {
                T s = getSchwimmer(lauf, x);
                if (s != null) {
                    long zeit = getZeit(lauf, x);
                    switch (getStrafe(lauf, x)) {
                    case DISQUALIFIKATION:
                        if (zeit != 0) {
                            break;
                        }
                        zeit = Long.MAX_VALUE;
                        break;
                    case NICHT_ANGETRETEN:
                        zeit = Long.MAX_VALUE;
                        break;
                    default:
                        break;
                    }
                    if ((zeit > time) && (zeit < low)) {
                        low = zeit;
                    }
                }
            }
            if (low == Long.MAX_VALUE) {
                finished = true;
            } else {
                for (int x = 0; x < result.length; x++) {
                    T s = getSchwimmer(lauf, x);
                    if (s != null) {
                        long zeit = getZeit(lauf, x);
                        if (getStrafe(lauf, x) == Strafarten.NICHT_ANGETRETEN) {
                            zeit = Long.MAX_VALUE;
                        }
                        if (zeit == low) {
                            if (result[index][1].equals("")) {
                                result[index][1] = "" + (x + 1);
                            } else {
                                result[index][1] += ", " + (x + 1);
                                result[nextindex][0] = "-";
                                result[nextindex][1] = "-";
                            }
                            nextindex++;
                        }
                    }
                }
                time = low;
                index = nextindex;
            }
        } while (!finished);

        return result;
    }
}