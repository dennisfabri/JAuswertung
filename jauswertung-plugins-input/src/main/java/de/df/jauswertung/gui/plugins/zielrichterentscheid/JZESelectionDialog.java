package de.df.jauswertung.gui.plugins.zielrichterentscheid;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

@SuppressWarnings("serial")
final class JZESelectionDialog<T extends ASchwimmer> extends JDialog {

    private JList liste = new JList();
    private final JFrame parent;
    private Zielrichterentscheid<T> result = null;

    private JZESelectionDialog(JFrame parent, LinkedList<Zielrichterentscheid<T>> possible) {
        super(parent, I18n.get("Add"), true);
        this.parent = parent;

        liste.setListData(possible.toArray());
        liste.setCellRenderer(new ZielrichterentscheidListCellRenderer());
        liste.setSelectedIndex(0);
        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        liste.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getClickCount() >= 2) && (!e.isPopupTrigger())) {
                    doOk();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseClicked(e);
            }
        });

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        setLayout(layout);

        JScrollPane scr = new JScrollPane(liste);
        scr.setBorder(null);

        add(scr, CC.xy(2, 2));
        add(getButtons(), CC.xy(2, 4));

        WindowUtils.addEscapeAction(this);
        WindowUtils.addEnterAction(this, this::doOk);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        pack();
        UIStateUtils.uistatemanage(parent, this, "JZESelectionDialog");
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            result = null;
        }
        parent.setEnabled(!b);
        super.setVisible(b);
    }

    @SuppressWarnings("unchecked")
    void doOk() {
        result = (Zielrichterentscheid<T>) liste.getSelectedValue();
        setVisible(false);
    }

    private JPanel getButtons() {
        FormLayout layout = new FormLayout("0dlu:grow,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
        JPanel p = new JPanel(layout);

        JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        ok.addActionListener(e -> {
            doOk();
        });
        JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        cancel.addActionListener(e -> {
            setVisible(false);
        });

        p.add(ok, CC.xy(2, 2));
        p.add(cancel, CC.xy(4, 2));

        return p;
    }

    public Zielrichterentscheid<T> getResult() {
        return result;
    }

    public static <T extends ASchwimmer> Zielrichterentscheid<T> getZielrichterentscheid(JFrame parent,
            LinkedList<Zielrichterentscheid<T>> zes) {
        JZESelectionDialog<T> jzes = new JZESelectionDialog<>(parent, zes);
        jzes.setVisible(true);
        return jzes.getResult();
    }
}