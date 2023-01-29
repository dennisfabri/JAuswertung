package de.df.jauswertung.gui.plugins.zielrichterentscheid;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.ZielrichterentscheidUtils;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

final class JZielrichterentscheidCheckDialog<T extends ASchwimmer> extends JDialog {

    private static final long serialVersionUID = -1245349186646716314L;

    private final JFrame parent;
    private final IPluginManager controller;
    private final AWettkampf<T> wk;
    private final Runnable next;

    private LinkedList<Zielrichterentscheid<T>> validze = null;

    private JZielrichterentscheidCheckDialog(JFrame parent, IPluginManager manager, AWettkampf<T> wk,
            Runnable callback) {
        super(parent, I18n.get("Zielrichterentscheid"), true);
        this.parent = parent;
        this.controller = manager;
        this.wk = wk;
        this.next = callback;

        initGUI();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                setVisible(false);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        WindowUtils.addEscapeAction(this);
        WindowUtils.addEnterAction(this, this::doOk);

        pack();
        UIStateUtils.uistatemanage(parent, this, "JZielrichterentscheidCheckDialog");
    }

    private void initGUI() {
        LinkedList<Zielrichterentscheid<T>>[] zes = ZielrichterentscheidUtils.checkZielrichterentscheide(wk);

        validze = zes[0];

        JList valid = new JList(zes[0].toArray());
        JList invalid = new JList(zes[1].toArray());

        valid.setCellRenderer(new ZielrichterentscheidListCellRenderer());
        invalid.setCellRenderer(new ZielrichterentscheidListCellRenderer());

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        layout.setColumnGroups(new int[][] { { 2, 4 } });
        setLayout(layout);

        add(new JLabel(I18n.get("ZielrichterentscheidCheckInformation")), CC.xyw(2, 2, 3, "fill,fill"));
        add(getScrollPane("Valid", valid), CC.xy(2, 4));
        add(getScrollPane("Invalid", invalid), CC.xy(4, 4));
        add(getButtons(), CC.xyw(2, 6, 3, "fill,fill"));
    }

    private JScrollPane getScrollPane(String title, JComponent c) {
        JScrollPane scr = new JScrollPane(c);
        scr.setBorder(BorderUtils.createLabeledBorder(I18n.get(title)));
        scr.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scr;
    }

    private JPanel getButtons() {
        FormLayout layout = new FormLayout("0dlu:grow,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
        layout.setColumnGroups(new int[][] { { 2, 4 } });
        JPanel p = new JPanel(layout);

        JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        ok.addActionListener(e -> {
            doOk();
        });
        JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        cancel.addActionListener(e -> {
            doCancel();
        });

        p.add(ok, CC.xy(2, 2));
        p.add(cancel, CC.xy(4, 2));

        return p;
    }

    void doOk() {
        wk.setZielrichterentscheide(validze);
        controller.sendDataUpdateEvent(new UpdateEvent("Zielrichterentscheid",
                UpdateEventConstants.REASON_ZIELRICHTERENTSCHEID_CHANGED, validze, null, null));
        setVisible(false);
        SwingUtilities.invokeLater(next);
    }

    void doCancel() {
        setVisible(false);
    }

    @Override
    public void setVisible(boolean b) {
        parent.setEnabled(!b);
        super.setVisible(b);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void start(IPluginManager controller, AWettkampf wk, Runnable callback) {
        if (ZielrichterentscheidUtils.checkZielrichterentscheide(wk)[1].isEmpty()) {
            callback.run();
            return;
        }
        JZielrichterentscheidCheckDialog jzcd = new JZielrichterentscheidCheckDialog(controller.getWindow(), controller,
                wk, callback);
        jzcd.setVisible(true);
    }
}