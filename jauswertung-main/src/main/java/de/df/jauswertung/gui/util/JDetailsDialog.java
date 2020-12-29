/**
 * 
 */
package de.df.jauswertung.gui.util;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jutils.gui.util.WindowUtils;

public class JDetailsDialog<T extends ASchwimmer> extends JDialog {

    private static final long serialVersionUID = -3176484810610448509L;

    private JRadioButton      all;
    JRadioButton              selected;
    JComboBox                 from;
    JComboBox                 to;
    private ButtonGroup       bg;

    JButton                   ok;
    JButton                   cancel;

    private boolean           isok             = false;

    public JDetailsDialog(JFrame parent, AWettkampf<T> wk, String whattoprint, boolean hlw) {
        super(parent, whattoprint, true);

        FormLayout layout = new FormLayout("4dlu,20dlu,fill:default,4dlu,fill:default,0dlu:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu:grow,fill:default,4dlu");
        setLayout(layout);

        all = new JRadioButton(I18n.get("PrintForAllHeats", whattoprint));
        selected = new JRadioButton(I18n.get("PrintForSelectedHeats", whattoprint));

        selected.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean b = selected.isSelected();
                from.setEnabled(b);
                to.setEnabled(b);
                updateButtons();
            }
        });

        bg = new ButtonGroup();
        bg.add(all);
        bg.add(selected);

        all.setSelected(true);

        from = new JComboBox(TableZWUtils.getHeatNames(wk, hlw));
        to = new JComboBox(TableZWUtils.getHeatNames(wk, hlw));

        from.setEnabled(false);
        to.setEnabled(false);

        from.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateButtons();
            }
        });
        to.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateButtons();
            }
        });

        add(all, CC.xyw(2, 2, 5));
        add(selected, CC.xyw(2, 4, 5));

        add(new JLabel(I18n.get("from")), CC.xy(3, 6));
        add(from, CC.xy(5, 6));
        add(new JLabel(I18n.get("to")), CC.xy(3, 8));
        add(to, CC.xy(5, 8));

        add(getButtons(), CC.xyw(2, 10, 5));

        to.setSelectedIndex(to.getItemCount() - 1);

        pack();
        WindowUtils.center(this, parent);
        WindowUtils.addEscapeAction(this);
        WindowUtils.addEnterAction(this, new Runnable() {
            @Override
            public void run() {
                if (ok.isEnabled()) {
                    doOk();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
    }

    void updateButtons() {
        boolean b = true;
        if (selected.isSelected()) {
            b = (from.getSelectedIndex() <= to.getSelectedIndex());
        }
        ok.setEnabled(b);
    }

    public boolean isOk() {
        return isok;
    }

    public boolean printAllHeats() {
        return all.isSelected();
    }

    public int getMinHeat() {
        return from.getSelectedIndex();
    }

    public int getMaxHeat() {
        return to.getSelectedIndex();
    }

    public void doOk() {
        isok = true;
        setVisible(false);
    }

    private JPanel getButtons() {
        FormLayout layout = new FormLayout("0dlu:grow,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
        layout.setColumnGroups(new int[][] { { 2, 4 } });
        JPanel buttonpanel = new JPanel(layout);

        ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        buttonpanel.add(ok, CC.xy(2, 2));
        buttonpanel.add(cancel, CC.xy(4, 2));

        return buttonpanel;
    }
}