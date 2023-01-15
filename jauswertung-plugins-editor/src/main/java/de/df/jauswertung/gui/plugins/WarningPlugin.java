package de.df.jauswertung.gui.plugins;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.UIUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.UpdateEvent;

public class WarningPlugin extends AFeature {

    @Override
    public void dataUpdated(UpdateEvent due) {
        // Nothing to do
    }

    public void information(JFrame parent, String title, String text, String note, String key) {
        if (title == null) {
            title = I18n.get("Information");
        }

        if (key != null) {
            boolean b = Utils.getPreferences().getBoolean("info." + key, true);
            if (!b) {
                return;
            }

            JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.getWindowAncestor((Component) e.getSource()).setVisible(false);
                }
            });
            JCheckBox always = new JCheckBox(I18n.get("DoNotAskAgain"));

            JPanel buttons = new JPanel(
                    new FormLayout("4dlu,fill:default,4dlu:grow,fill:default,4dlu", "4dlu,fill:default,4dlu"));

            buttons.add(always, CC.xy(2, 2));
            buttons.add(ok, CC.xy(4, 2));

            JDialog jd = new JDialog(parent, title, true);
            WindowUtils.addEnterAction(jd);
            WindowUtils.addEscapeAction(jd);
            jd.setLayout(new FormLayout("fill:default:grow", "fill:default:grow,fill:default"));
            jd.add(UIUtils.createInfoPanel(text, note), CC.xy(1, 1));
            jd.add(buttons, CC.xy(1, 2));
            jd.pack();
            WindowUtils.center(jd, parent);
            jd.setVisible(true);

            if (always.isSelected()) {
                Utils.getPreferences().putBoolean("info." + key, false);
            }
        } else {
            DialogUtils.inform(parent, title, text, note);
        }
    }

    public void warn(JFrame parent, String title, String text, String note, String key) {
        if (title == null) {
            title = I18n.get("Information");
        }

        if (key != null) {
            boolean b = Utils.getPreferences().getBoolean("warn." + key, true);
            if (!b) {
                return;
            }

            JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.getWindowAncestor((Component) e.getSource()).setVisible(false);
                }
            });
            JCheckBox always = new JCheckBox(I18n.get("DoNotAskAgain"));

            JPanel buttons = new JPanel(
                    new FormLayout("4dlu,fill:default,4dlu:grow,fill:default,4dlu", "4dlu,fill:default,4dlu"));

            buttons.add(always, CC.xy(2, 2));
            buttons.add(ok, CC.xy(4, 2));

            JDialog jd = new JDialog(parent, title, true);
            WindowUtils.addEnterAction(jd);
            WindowUtils.addEscapeAction(jd);
            jd.setLayout(new FormLayout("fill:default:grow", "fill:default:grow,fill:default"));
            jd.add(UIUtils.createWarningPanel(text, note), CC.xy(1, 1));
            jd.add(buttons, CC.xy(1, 2));
            jd.pack();
            WindowUtils.center(jd, parent);
            jd.setVisible(true);

            if (always.isSelected()) {
                Utils.getPreferences().putBoolean("warn." + key, false);
            }
        } else {
            DialogUtils.warn(parent, title, text, note);
        }
    }
}