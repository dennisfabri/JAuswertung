package de.df.jauswertung.gui.plugins.heatsow.define;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.functional.BooleanConsumer;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.UIStateUtils;

import static java.lang.String.format;

public class JOWHeatsEditWindow<T extends ASchwimmer> extends JFrame {

    private JList<String> aks;
    private JOWHeatsAKEditPanel[] panels;

    private JButton ok;
    private JButton cancel;

    private JMenuBar menu;

    private JScrollPane right;

    private BooleanConsumer callback;

    private boolean isOk = false;

    public JOWHeatsEditWindow(AWettkampf<T> wk, BooleanConsumer callback) {
        setTitle(I18n.get("DefineRounds"));
        setIconImages(IconManager.getTitleImages());
        this.callback = callback;
        buildMenu();
        buildUI(wk);
        pack();
        setMinimumSize(new Dimension(400, 300));
        UIStateUtils.uistatemanage(this, "JOWHeatsEditWindow");
    }

    private void buildMenu() {
        JMenuItem close = new JMenuItem(I18n.get("Close"));
        close.addActionListener(arg0 -> {
            doCancel();
        });
        JMenuItem copy = new JMenuItem(I18n.get("CopyFemaleToMale"));
        copy.addActionListener(arg0 -> {
            for (int x = 0; x < panels.length / 2; x++) {
                panels[2 * x + 1].copyFrom(panels[2 * x]);
            }
        });
        JMenuItem enumerate1 = new JMenuItem(I18n.get("EnumerateOddEven"));
        enumerate1.addActionListener(arg0 -> {
            int offset1 = 1;
            int offset2 = 1;
            for (int x = 0; x < panels.length / 2; x++) {
                offset1 = panels[2 * x].enumerate1(offset1, false);
                offset2 = panels[2 * x + 1].enumerate1(offset2, true);
            }
        });
        JMenuItem enumerate2 = new JMenuItem(I18n.get("EnumerateContinous"));
        enumerate2.addActionListener(arg0 -> {
            int offset = 1;
            for (int x = 0; x < panels.length; x++) {
                offset = panels[x].enumerate2(offset);
            }
        });

        JMenu file = new JMenu(I18n.get("File"));
        file.add(close);

        JMenu edit = new JMenu(I18n.get("Edit"));
        edit.add(copy);
        edit.add(enumerate1);
        edit.add(enumerate2);

        menu = new JMenuBar();
        menu.add(file);
        menu.add(edit);

        this.setJMenuBar(menu);
    }

    private void buildUI(AWettkampf<T> wk) {

        List<String> tempData = new ArrayList<>();
        for (Altersklasse ak : wk.getRegelwerk().getAks()) {
            tempData.add(I18n.getAgeGroupAsString(wk.getRegelwerk(), ak, false));
            tempData.add(I18n.getAgeGroupAsString(wk.getRegelwerk(), ak, true));
        }
        String[] data = tempData.toArray(new String[tempData.size()]);
        aks = new JList<>(data);
        aks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        aks.addListSelectionListener(e -> {
            setSelected(aks.getSelectedIndex());
        });

        panels = new JOWHeatsAKEditPanel[wk.getRegelwerk().size() * 2];

        JScrollPane pane1 = new JScrollPane(aks);
        right = new JScrollPane();

        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            for (int y = 0; y < 2; y++) {
                panels[x * 2 + y] = new JOWHeatsAKEditPanel(wk.getRegelwerk(), wk.getRegelwerk().getAk(x), y == 1);
                // p.add(panels[x], CC.xy(2, x * 4 + y * 2 + 2));
            }
        }

        JPanel panel = new JPanel(new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu"));
        this.add(panel);

        panel.add(pane1, CC.xy(2, 2));
        panel.add(right, CC.xy(4, 2));
        panel.add(createButtons(), CC.xyw(2, 4, 3, "right,fill"));

        pane1.setBorder(BorderUtils.createLabeledBorder("Altersklassen"));
        right.setBorder(BorderUtils.createLabeledBorder("Disziplinen"));

        aks.setSelectedIndex(0);
    }

    private JPanel createButtons() {
        FormLayout layout = new FormLayout(FormLayoutUtils.createLayoutString(2, 4, 0),
                FormLayoutUtils.createLayoutString(1, 4, 0));
        layout.setColumnGroup(2, 4);
        JPanel p = new JPanel(layout);

        ok = new JButton(I18n.get("Ok"));
        cancel = new JButton(I18n.get("Cancel"));
        ok.addActionListener(e -> {
            doOk();
        });
        cancel.addActionListener(e -> {
            doCancel();
        });

        p.add(ok, CC.xy(2, 2));
        p.add(cancel, CC.xy(4, 2));

        return p;
    }

    private ValidationResult isInputOk() {
        ValidationResult result = ValidationResult.OK;
        ArrayList<Integer> ids = new ArrayList<>();
        for (JOWHeatsAKEditPanel panel : panels) {
            result = result.merge(panel.isInputValid());
            ids.addAll(panel.GetIds());
        }
        int last = -1;
        Collections.sort(ids);
        for (Integer i : ids) {
            if (i == last) {
                return new ValidationResult(format("Id %d ist doppelt vergeben.", i));
            }
            last = i;
        }
        return result;
    }

    private void doOk() {
        ValidationResult validation = isInputOk();
        if (!validation.isValid()) {
            DialogUtils.warn(this, "Die eingegebenen Daten sind nicht korrekt und können nicht gespeichert werden.",
                    validation.getMessage()+
                    "\n\nBitte korrigieren Sie die Eingabe.");
            return;
        }
        isOk = true;

        for (JOWHeatsAKEditPanel panel : panels) {
            panel.doSave();
        }

        if (callback != null) {
            callback.accept(isOk);
        }
        setVisible(false);
    }

    private void doCancel() {
        isOk = false;
        setVisible(false);
    }

    @Override
    public void setVisible(boolean v) {
        if (isVisible() == v) {
            return;
        }
        if (v) {
            isOk = false;
        }
        super.setVisible(v);
        if (v == false) {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }

    void setSelected(int x) {
        right.setViewportView(panels[x]);
    }

    public boolean isOk() {
        return isOk;
    }
}