/*
 * Created on 16.03.2006
 */
package de.df.jauswertung.gui.plugins.filter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

class JFilterSelection extends JDialog {

    private static final long serialVersionUID = -6440431620450596615L;

    private JList<String> list = new JList();

    public JFilterSelection(JFrame parent, LinkedList<String> names) {
        super(parent, I18n.get("FilterSelection"), true);

        WindowUtils.addEscapeAction(this, new Runnable() {
            @Override
            public void run() {
                doCancel();
            }
        });

        String[] n = names.toArray(new String[names.size()]);
        list.setListData(n);

        JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                doOk();
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                doCancel();
            }
        });

        JScrollPane scroller = new JScrollPane(list);

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu");

        setLayout(layout);

        add(scroller, CC.xyw(2, 2, 4, "fill,fill"));
        add(ok, CC.xy(3, 4));
        add(cancel, CC.xy(5, 4));

        pack();
        UIStateUtils.uistatemanage(parent, this, "FilterSelection");
    }

    void doOk() {
        selected = true;
        setVisible(false);
    }

    void doCancel() {
        selected = false;
        setVisible(false);
    }

    private boolean selected = false;

    @Override
    public void setVisible(boolean arg0) {
        SwingUtilities.getWindowAncestor(this).setEnabled(!arg0);
        super.setVisible(arg0);
    }

    public String[] getSelection() {
        if (!selected) {
            return null;
        }
        int[] indices = list.getSelectedIndices();
        if (indices.length == 0) {
            return null;
        }
        String[] names = new String[indices.length];
        for (int x = 0; x < names.length; x++) {
            names[x] = list.getModel().getElementAt(indices[x]).toString();
        }
        return names;
    }
}