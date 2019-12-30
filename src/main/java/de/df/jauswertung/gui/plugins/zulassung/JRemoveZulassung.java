package de.df.jauswertung.gui.plugins.zulassung;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.*;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPluginManager;

public class JRemoveZulassung<T extends ASchwimmer> extends JDialog {

    private IPluginManager controller;
    private AWettkampf<T>  wk;

    private JRemoveZulassung(JFrame parent, IPluginManager controller, AWettkampf<T> wk, int[] quali) {
        super(parent, I18n.get("Zulassung"), true);
        setResizable(false);

        this.controller = controller;
        this.wk = wk;

        setLayout(new FormLayout("0dlu,fill:default,0dlu", "0dlu,fill:default,0dlu,fill:default,0dlu"));

        add(getInfo(quali), CC.xy(2, 2));
        add(getButtons(quali[2] + quali[5] + quali[6] > 0), CC.xy(2, 4));

        pack();
        WindowUtils.center(this, parent);
        UIStateUtils.uistatemanage(this, "JRemoveZulassung");
        pack();
    }

    private JCheckBox notQualified = new JCheckBox("", true);
    private JCheckBox nachruecker  = new JCheckBox("", false);
    private JCheckBox disabled     = new JCheckBox("", true);

    private JPanel getInfo(int[] quali) {
        JPanel p = new JPanel();
        FormLayout layout = new FormLayout("4dlu,4dlu,4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu", FormLayoutUtils.createLayoutString(14));
        layout.setRowGroups(new int[][] { { 2, 4, 6, 12, 14, 16, 18, 20, 22, 26 } });
        p.setLayout(layout);

        p.add(new JLabel(I18n.get("Qualified")), CC.xyw(2, 2, 7));

        p.add(new JLabel(I18n.get("Quali.Set")), CC.xy(4, 4));
        p.add(new JLabel("" + quali[1]), CC.xy(6, 4, "right,fill"));
        p.add(new JLabel(I18n.get("Quali.Direct")), CC.xy(4, 6));
        p.add(new JLabel("" + quali[0]), CC.xy(6, 6, "right,fill"));
        p.add(new JLabel(I18n.get("Quali.Qualified")), CC.xy(4, 8));
        p.add(new JLabel("" + quali[4]), CC.xy(6, 8, "right,fill"));
        p.add(new JSeparator(), CC.xyw(4, 10, 3, "fill,fill"));
        p.add(new JLabel(I18n.get("Gesamt")), CC.xy(4, 12));
        p.add(new JLabel("" + (quali[1] + quali[0] + quali[4])), CC.xy(6, 12, "right,fill"));

        p.add(new JLabel(I18n.get("NotQualified")), CC.xyw(2, 16, 7));

        p.add(new JLabel(I18n.get("Quali.NotQualified")), CC.xy(4, 18));
        p.add(new JLabel("" + quali[2]), CC.xy(6, 18, "right,fill"));
        p.add(notQualified, CC.xy(8, 18));
        p.add(new JLabel(I18n.get("Quali.Nachruecker")), CC.xy(4, 20));
        p.add(new JLabel("" + quali[6]), CC.xy(6, 20, "right,fill"));
        p.add(nachruecker, CC.xy(8, 20));
        p.add(new JLabel(I18n.get("Quali.Disabled")), CC.xy(4, 22));
        p.add(new JLabel("" + quali[5]), CC.xy(6, 22, "right,fill"));
        p.add(disabled, CC.xy(8, 22));
        p.add(new JSeparator(), CC.xyw(4, 24, 3, "fill,fill"));
        p.add(new JLabel(I18n.get("Gesamt")), CC.xy(4, 26));
        p.add(new JLabel("" + (quali[2] + quali[6] + quali[5])), CC.xy(6, 26, "right,fill"));

        notQualified.setEnabled(quali[2] > 0);
        nachruecker.setEnabled(quali[6] > 0);
        disabled.setEnabled(quali[5] > 0);

        if (!notQualified.isEnabled()) {
            notQualified.setSelected(false);
        }
        if (!nachruecker.isEnabled()) {
            nachruecker.setSelected(false);
        }
        if (!disabled.isEnabled()) {
            disabled.setSelected(false);
        }
        p.add(new JLabel(I18n.get("RemoveNonQualified.Information")), CC.xyw(2, 28, 7));

        return p;
    }

    private JPanel getButtons(boolean enabled) {
        JPanel p = new JPanel(new FormLayout("4dlu:grow,fill:default,4dlu,fill:default,4dlu", "0dlu,fill:default,4dlu"));

        JButton ok = new JButton(I18n.get("Remove"), IconManager.getSmallIcon("remove"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        });
        JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("close"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        if (!enabled) {
            ok.setEnabled(false);
            cancel.setText(I18n.get("Close"));
        }

        p.add(ok, CC.xy(2, 2));
        p.add(cancel, CC.xy(4, 2));

        return p;
    }

    void doOk() {
        LinkedList<T> swimmers = wk.getSchwimmer();
        for (T t : swimmers) {
            switch (t.getQualifikation()) {
            case NICHT_QUALIFIZIERT:
                if (notQualified.isSelected()) {
                    wk.removeSchwimmer(t);
                }
                break;
            case GESPERRT:
                if (disabled.isSelected()) {
                    wk.removeSchwimmer(t);
                }
                break;
            case NACHRUECKER:
                if (nachruecker.isSelected()) {
                    wk.removeSchwimmer(t);
                }
                break;
            default:
                break;
            }
        }
        setVisible(false);
        controller.sendDataUpdateEvent("Remove", UpdateEventConstants.REASON_SWIMMER_DELETED, null);
    }

    public static <T extends ASchwimmer> void start(JFrame parent, IPluginManager controller, CorePlugin core) {
        AWettkampf<T> wk = core.getWettkampf();
        LinkedList<T> swimmers = wk.getSchwimmer();

        int[] count = new int[7];
        for (int x = 0; x < count.length; x++) {
            count[x] = 0;
        }
        for (T t : swimmers) {
            Qualifikation q = t.getQualifikation();
            switch (q) {
            case DIREKT:
                count[0]++;
                break;
            case GESETZT:
                count[1]++;
                break;
            case NACHRUECKER:
                count[6]++;
                break;
            case NICHT_QUALIFIZIERT:
                count[2]++;
                break;
            case OFFEN:
                count[3]++;
                break;
            case QUALIFIZIERT:
                count[4]++;
                break;
            case GESPERRT:
                count[5]++;
                break;
            }
        }

        if (count[3] > 0) {
            DialogUtils.wichtigeMeldung(parent, I18n.get("NochOffeneZulassungen", count[3]));
            return;
        }

        new JRemoveZulassung<T>(parent, controller, wk, count).setVisible(true);
    }
}
