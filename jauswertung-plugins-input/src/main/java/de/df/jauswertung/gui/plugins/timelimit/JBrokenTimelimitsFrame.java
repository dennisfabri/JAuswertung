package de.df.jauswertung.gui.plugins.timelimit;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.UIStateUtils;

/**
 * Shows the times that break a time limit.
 */
class JBrokenTimelimitsFrame extends JFrame {

    private final JFrame parent;

    JBrokenTimelimitsFrame(JFrame parent, TableModel brokenTimelimits) {
        this.parent = parent;

        setLayout(new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default:grow,4dlu,fill:default,4dlu"));
        setTitle(I18n.get("Timelimits"));
        setIconImages(IconManager.getTitleImages());

        JTable table = new JTable(brokenTimelimits);
        table.setAutoCreateRowSorter(true);
        table.setDefaultEditor(Object.class, null);

        JButton close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(e -> doClose());

        add(new JScrollPane(table), CC.xy(2, 2));
        add(FormLayoutUtils.createButtonsPanel(close), CC.xy(2, 4, "right,fill"));

        setMinimumSize(new Dimension(600, 400));
        pack();
        UIStateUtils.uistatemanage(this);
    }

    private void doClose() {
        parent.setEnabled(true);
        setVisible(false);
        dispose();
    }
}
