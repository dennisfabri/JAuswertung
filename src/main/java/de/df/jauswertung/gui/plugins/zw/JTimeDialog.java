package de.df.jauswertung.gui.plugins.zw;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.laufliste.Duration;
import de.df.jauswertung.daten.laufliste.Time;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.WindowUtils;

final class JTimeDialog<T extends ASchwimmer> extends JDialog {

    private static final long serialVersionUID = -8665331196332280641L;

    JComboBox                 time             = null;
    JComboBox                 duration         = null;

    Time                      result           = null;
    Duration                  result2          = null;

    public JTimeDialog(JHlwlisteBearbeiten<T> parent, boolean starttime, boolean hasDuration) {
        super(parent, I18n.get("SetTime"), true);

        time = new JComboBox(createTimes());
        duration = new JComboBox(createDurations());

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu," + (hasDuration ? "fill:default,4dlu,fill:default,4dlu," : "") + "fill:default,4dlu,fill:default,4dlu,fill:default,4dlu");
        setLayout(layout);

        add(new JLabel(I18n.get(starttime ? "EnterStarttime" : "EnterEndOfPause")), CC.xyw(2, 2, 3, "fill,fill"));
        add(new JLabel(I18n.get("TimeOfDay")), CC.xy(2, 4));
        add(time, CC.xy(4, 4));

        if (hasDuration) {
            add(new JLabel(I18n.get("DurationOfZW")), CC.xyw(2, 6, 3, "fill,fill"));
            add(new JLabel(I18n.get("Duration")), CC.xy(2, 8));
            add(duration, CC.xy(4, 8));
        }

        add(createButtons(), CC.xyw(2, 6 + (hasDuration ? 4 : 0), 3, "right,fill"));

        setResizable(false);
        pack();
        WindowUtils.center(this, parent);

        WindowUtils.addEnterAction(this, new Runnable() {
            @Override
            public void run() {
                doOk();
            }
        });

        WindowUtils.addEscapeAction(this);
    }

    public void setTime(Time t) {
        time.setSelectedIndex(t.getTimeInMinutes() / 15);
    }

    public void setDuration(double d) {
        duration.setSelectedIndex(0);
        for (int x = 0; x < duration.getModel().getSize(); x++) {
            Duration du = (Duration) duration.getItemAt(x);
            if (du.getTime() >= d) {
                duration.setSelectedIndex(x);
                break;
            }
        }
    }

    private Duration[] createDurations() {
        return new Duration[] { new Duration(2), new Duration(3), new Duration(4), new Duration(5), new Duration(6), new Duration(7.5), new Duration(8),
                new Duration(9), new Duration(10), new Duration(15), new Duration(20) };
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            result = null;
        }
        super.setVisible(b);
    }

    private JPanel createButtons() {
        FormLayout layout = new FormLayout("0dlu,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
        JPanel panel = new JPanel(layout);

        JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        });

        JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        panel.add(ok, CC.xy(2, 2));
        panel.add(cancel, CC.xy(4, 2));

        return panel;
    }

    private Time[] createTimes() {
        int size = 24 * 60 / 15;
        Time[] times = new Time[size];
        for (int x = 0; x < size; x++) {
            times[x] = new Time(x * 15);
        }
        return times;
    }

    public Time getTime() {
        return result;
    }

    public Duration getDuration() {
        return result2;
    }

    void doOk() {
        if (time.getSelectedIndex() >= 0) {
            result = (Time) time.getSelectedItem();
            result2 = (Duration) duration.getSelectedItem();
        } else {
            result = null;
            result2 = null;
        }
        setVisible(false);
    }

    void doCancel() {
        result = null;
        setVisible(false);
    }
}