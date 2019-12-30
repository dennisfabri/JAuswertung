package de.df.jauswertung.gui.plugins.kampfrichter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.kampfrichter.*;
import de.df.jauswertung.gui.util.*;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

public class JKampfrichterPositionEditor extends JDialog {

    private final KampfrichterPosition position;
    private final KampfrichterEinheit  einheit;

    private JWarningTextField          name;
    private JComboBox<String>          stufe;

    private JButton                    ok;
    private JButton                    close;

    public JKampfrichterPositionEditor(JFrame parent, KampfrichterEinheit einheit, KampfrichterPosition pos) {
        super(parent, I18n.get("Refereeposition"), true);
        this.einheit = einheit;
        this.position = pos;

        initGUI();
        addActions();
        pack();
        WindowUtils.center(this, parent);
        UIStateUtils.uistatemanage(this, "JKampfrichterPositionEditor");
    }

    private void addActions() {
        WindowUtils.addEscapeAction(this);
        WindowUtils.addEnterAction(this, new Runnable() {
            @Override
            public void run() {
                doOk();
            }
        });
    }

    private void initGUI() {
        LinkedList<String> known = new LinkedList<String>();
        for (String pos : einheit.getPositionen()) {
            if (!pos.equals(position.getPosition())) {
                known.addLast(pos);
            }
        }
        name = new JWarningTextField(position.getPosition());
        name.setForbiddenStrings(known.toArray(new String[known.size()]));
        name.setRequired(true);
        name.setForced(true);

        stufe = new JComboBox<String>(KampfrichterStufe.getBaseLevels());
        stufe.setSelectedIndex(KampfrichterStufe.getBaseIndex(position.getMinimaleStufe()));

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default,4dlu:grow,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4 } });
        setLayout(layout);

        add(new JLabel("Position"), CC.xy(2, 2));
        add(name, CC.xy(4, 2));

        add(new JLabel("Level"), CC.xy(2, 4));
        add(stufe, CC.xy(4, 4));

        add(getButtons(), CC.xyw(2, 6, 3, "right,fill"));
    }

    private KampfrichterStufe getStufe() {
        return KampfrichterStufe.getLevelFromBaseLevelsIndex(stufe.getSelectedIndex());
    }

    void apply() {
        einheit.renamePosition(position, name.getText());
        position.setMinimaleStufe(getStufe());
    }

    void doOk() {
        if (name.isOk()) {
            apply();
            setVisible(false);
        }
    }

    void doCancel() {
        setVisible(false);
    }

    private JPanel getButtons() {
        ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        });
        close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        FormLayout layout = new FormLayout("0dlu,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
        layout.setColumnGroups(new int[][] { { 2, 4 } });
        JPanel p = new JPanel(layout);
        p.add(ok, CC.xy(2, 2));
        p.add(close, CC.xy(4, 2));
        return p;
    }
}