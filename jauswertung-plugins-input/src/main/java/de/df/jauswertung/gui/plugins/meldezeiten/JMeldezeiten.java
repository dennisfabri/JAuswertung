package de.df.jauswertung.gui.plugins.meldezeiten;

import java.awt.CardLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

class JMeldezeiten<T extends ASchwimmer> extends JFrame {

    private static final long                serialVersionUID = -2312160133856798878L;

    private final AWettkampf<T>              wk;
    private boolean                          changed          = false;
    @SuppressWarnings("rawtypes")
    private JMeldezeitenPanel[][]            panels;
    private CardLayout                       cards;
    private JPanel                           cardpanel;

    private JComboBox<String>                agegroups;
    private JComboBox<String>                sex;

    private JButton                          ok;
    private JButton                          cancel;

    private JFrame                           parent;

    private ISimpleCallback<JMeldezeiten<T>> callback;

    public JMeldezeiten(JFrame p, AWettkampf<T> w, ISimpleCallback<JMeldezeiten<T>> cb) {
        super(I18n.get("Meldezeiten"));
        setIconImages(IconManager.getTitleImages());
        parent = p;

        callback = cb;

        wk = w;

        cards = new CardLayout();
        cardpanel = new JPanel(cards);

        Regelwerk rw = w.getRegelwerk();

        panels = new JMeldezeitenPanel[rw.size()][2];

        String[] ag = new String[rw.size()];
        for (int x = 0; x < ag.length; x++) {
            ag[x] = rw.getAk(x).getName();
        }
        agegroups = new JComboBox<String>(ag);
        sex = new JComboBox<String>(new String[] { I18n.geschlechtToShortString(rw, false), I18n.geschlechtToShortString(rw, true) });
        agegroups.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                selectPanel();
            }
        });
        sex.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                selectPanel();
            }
        });

        ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                accept();
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
            }

        });
        FormLayout buttonLayout = new FormLayout("fill:default:grow,fill:default,4dlu,fill:default", "fill:default");
        buttonLayout.setColumnGroups(new int[][] { { 2, 4 } });
        JPanel buttons = new JPanel(buttonLayout);
        buttons.add(ok, CC.xy(2, 1));
        buttons.add(cancel, CC.xy(4, 1));

        FormLayout layout = new FormLayout(
                "4dlu,fill:default:grow,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        layout.setColumnGroups(new int[][] { { 3, 7 }, { 5, 9 } });

        setLayout(layout);

        add(new JLabel(I18n.get("AgeGroup")), CC.xy(3, 2));
        add(new JLabel(I18n.get("Sex")), CC.xy(7, 2));
        add(agegroups, CC.xy(5, 2));
        add(sex, CC.xy(9, 2));
        add(cardpanel, CC.xyw(2, 4, 11));
        add(buttons, CC.xyw(2, 6, 11));

        showZeiten(0, false);

        pack();
        setExtendedState(Frame.MAXIMIZED_BOTH);
        WindowUtils.addEscapeAction(this);
        WindowUtils.addEnterAction(this, new Runnable() {
            @Override
            public void run() {
                accept();
            }
        });
        UIStateUtils.uistatemanage(parent, this, "JMeldezeiten");
    }

    private void showZeiten(int ak, boolean male) {
        int index = (male ? 1 : 0);
        if (panels[ak][index] == null) {
            this.setEnabled(false);
            panels[ak][index] = new JMeldezeitenPanel<T>(this, wk, wk.getRegelwerk().getAk(ak), male);
            JScrollPane scroller = new JScrollPane(panels[ak][index]);
            scroller.getHorizontalScrollBar().setUnitIncrement(10);
            scroller.getVerticalScrollBar().setUnitIncrement(10);
            cardpanel.add(scroller, "" + ak + "-" + index);
            this.setEnabled(true);
        }
        cards.show(cardpanel, "" + ak + "-" + index);
    }

    void selectPanel() {
        int x = agegroups.getSelectedIndex();
        int y = sex.getSelectedIndex();
        showZeiten(x, y == 1);
    }

    @Override
    public void setVisible(boolean arg0) {
        parent.setEnabled(!arg0);
        super.setVisible(arg0);
        if ((!arg0) && (callback != null)) {
            callback.callback(this);
        }
    }

    @SuppressWarnings("rawtypes")
    void accept() {
        for (JMeldezeitenPanel[] panel : panels) {
            for (int y = 0; y < 2; y++) {
                if (panel[y] != null) {
                    panel[y].accept();
                }
            }
        }
        setVisible(false);
    }

    public void isChanging() {
        changed = true;
        checkValidity();
    }

    @SuppressWarnings("rawtypes")
    public void checkValidity() {
        boolean valid = true;
        for (JMeldezeitenPanel[] panel : panels) {
            for (int y = 0; y < 2; y++) {
                if (panel[y] != null) {
                    if (!panel[y].isDataValid()) {
                        valid = false;
                        break;
                    }
                }
            }
        }
        ok.setEnabled(valid);
    }

    public boolean hasChanged() {
        return changed;
    }
}