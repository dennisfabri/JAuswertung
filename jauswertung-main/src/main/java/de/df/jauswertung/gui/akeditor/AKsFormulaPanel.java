/*
 * Created on 06.06.2004
 */
package de.df.jauswertung.gui.akeditor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.ergebnis.Formel;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jutils.gui.border.BorderUtils;

/**
 * @author Dennis Fabri
 * @date 06.06.2004
 */
class AKsFormulaPanel extends JPanel {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3257288011191498036L;
    private JRadioButton[]    namen            = null;
    private JLabel[]          formeln          = null;
    private JLabel[]          descriptions     = null;
    private ButtonGroup       buttons          = new ButtonGroup();
    private Formel<?>[]       fs               = FormelManager.getFormeln();
    JAKsEditor                options          = null;

    public AKsFormulaPanel(JAKsEditor jod) {
        initFormeln();
        initGUI();

        options = jod;
    }

    private void initFormeln() {
        namen = new JRadioButton[fs.length];
        formeln = new JLabel[fs.length];
        descriptions = new JLabel[fs.length];
        for (int x = 0; x < fs.length; x++) {
            namen[x] = new JRadioButton(fs[x].getName());
            formeln[x] = new JLabel(fs[x].getFormel());
            descriptions[x] = new JLabel(fs[x].getDescription());
            namen[x].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    options.notifyChange();
                }
            });

            buttons.add(namen[x]);
        }
    }

    private void initGUI() {
        setBorder(BorderUtils.createLabeledBorder(I18n.get("Formula")));
        FormLayout layout = new FormLayout("4dlu,fill:16dlu," + generateLayout(2), generateRowLayout(namen.length));
        JPanel p = new JPanel(layout);
        for (int x = 0; x < namen.length; x++) {
            p.add(namen[x], CC.xywh(2, 2 + (6 * x), 5, 1));
            p.add(new JLabel(I18n.get("Formula")), CC.xy(4, 4 + (6 * x)));
            p.add(formeln[x], CC.xy(6, 4 + (6 * x)));
            p.add(new JLabel(I18n.get("Description")), CC.xy(4, 6 + (6 * x)));
            p.add(descriptions[x], CC.xy(6, 6 + (6 * x)));
        }

        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);

        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);
    }

    private static String generateLayout(int count) {
        String s = "4dlu,fill:default,4dlu";
        for (int x = 1; x < count; x++) {
            s += ",fill:default,4dlu";
        }
        return s;
    }

    private static String generateRowLayout(int count) {
        return generateLayout(count * 3);
    }

    private void setFormel(String id) {
        for (int x = 0; x < fs.length; x++) {
            if (fs[x].getID().equals(id)) {
                namen[x].setSelected(true);
                namen[x].requestFocus();
                break;
            }
        }
    }

    private String getFormel() {
        for (int x = 0; x < namen.length; x++) {
            if (namen[x].isSelected()) {
                return fs[x].getID();
            }
        }
        return null;
    }

    public void setSettings(Regelwerk aks) {
        setFormel(aks.getFormelID());
    }

    public void getSettings(Regelwerk aks) {
        aks.setFormelID(getFormel());
    }
}