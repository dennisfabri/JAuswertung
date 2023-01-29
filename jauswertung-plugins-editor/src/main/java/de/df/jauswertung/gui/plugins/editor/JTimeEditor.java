/*
 * Created on 01.03.2005
 */
package de.df.jauswertung.gui.plugins.editor;

import java.awt.BorderLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JIntegerField.Validator;
import de.df.jutils.gui.JTimeField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPluginManager;

class JTimeEditor extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 3256719580843227188L;

    ASchwimmer schwimmer = null;
    int disziplin = 0;
    JIntegerField integer = null;
    JTimeField time = null;
    JButton ok = null;
    IPluginManager controller = null;

    /**
     * This is the default constructor
     */
    public JTimeEditor(IPluginManager controller, ASchwimmer s, int disz) {
        super(controller.getWindow(), I18n.get("TimeInput"), true);
        if (!s.isDisciplineChosen(disz)) {
            throw new IllegalArgumentException();
        }
        this.controller = controller;

        schwimmer = s;
        disziplin = disz;

        initialize();
        addActions();
    }

    @Override
    public void setVisible(boolean visible) {
        controller.getWindow().setEnabled(!visible);
        super.setVisible(visible);
    }

    private void addActions() {
        WindowUtils.addEscapeAction(this);
        WindowUtils.addEnterAction(this, this::doOk);
    }

    void doOk() {
        if ((integer.getText().length() > 0) && (!(integer.isValidInt() && time.isValidValue()))) {
            Toolkit.getDefaultToolkit().beep();
            integer.requestFocus();
        } else {
            int zeit = schwimmer.getZeit(disziplin);
            if (zeit != time.getTimeAsInt()) {
                schwimmer.setZeit(disziplin, time.getTimeAsInt());
                if (SchwimmerUtils.checkTimeAndNotify(this, schwimmer, disziplin)) {
                    controller.sendDataUpdateEvent("ChangeTime", UpdateEventConstants.REASON_POINTS_CHANGED, schwimmer,
                            disziplin, null);
                    setVisible(false);
                } else {
                    schwimmer.setZeit(disziplin, zeit);
                    integer.requestFocus();
                }
            }
        }
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderUtils.createSpaceBorder());
        setContentPane(panel);

        ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        ok.addActionListener(arg0 -> {
            doOk();
        });

        JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        cancel.addActionListener(arg0 -> {
            setVisible(false);
        });

        integer = new JIntegerField(JTimeField.MAX_TIME, true);
        integer.setValidator((Validator)value -> {
            value = value / 100;
            if ((value % 100) >= 60) {
                return false;
            }
            value = value / 100;
            return value < 1000;
        });
        integer.setAutoSelectAll(true);
        integer.requestFocus();
        integer.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                changedUpdate(arg0);
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                changedUpdate(arg0);
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                ok.setEnabled((integer.getText().length() == 0) || ((integer.isValidInt() && time.isValidValue())));
            }
        });

        time = new JTimeField(integer);
        if (schwimmer.getZeit(disziplin) == 0) {
            integer.setInt(JIntegerField.EMPTY_FIELD);
        } else {
            time.setTimeAsInt(schwimmer.getZeit(disziplin));
        }

        FormLayout buttonLayout = new FormLayout("fill:default:grow,fill:default,4dlu,fill:default", "fill:default");
        buttonLayout.setColumnGroups(new int[][] { { 2, 4 } });
        JPanel buttons = new JPanel(buttonLayout);
        buttons.add(ok, CC.xy(2, 1));
        buttons.add(cancel, CC.xy(4, 1));

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4, 6 } });
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Input")));
        top.add(new JLabel(I18n.get("Input")), CC.xy(2, 2));
        top.add(new JLabel(I18n.get("Time")), CC.xy(2, 4));

        top.add(integer, CC.xy(4, 2));
        top.add(time, CC.xy(4, 4));
        top.add(buttons, CC.xyw(2, 6, 3));

        layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4, 6, 8, 10 } });
        JPanel bottom = new JPanel(layout);
        bottom.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information")));
        bottom.add(new JLabel(I18n.get("Name")), CC.xy(2, 2));
        bottom.add(new JLabel(I18n.get("Organisation")), CC.xy(2, 4));
        bottom.add(new JLabel(I18n.get("AgeGroup")), CC.xy(2, 6));
        bottom.add(new JLabel(I18n.get("DisciplineNr")), CC.xy(2, 8));
        bottom.add(new JLabel(I18n.get("DisciplineName")), CC.xy(2, 10));

        bottom.add(new JLabel(schwimmer.getName()), CC.xy(4, 2));
        bottom.add(new JLabel(schwimmer.getGliederung()), CC.xy(4, 4));
        bottom.add(new JLabel(schwimmer.getAK().toString() + " " + I18n.geschlechtToString(schwimmer)), CC.xy(4, 6));
        bottom.add(new JLabel("" + (disziplin + 1)), CC.xy(4, 8));
        bottom.add(new JLabel(schwimmer.getAK().getDisziplin(disziplin, true).getName()), CC.xy(4, 10));

        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);

        pack();
        setResizable(false);
        UIStateUtils.uistatemanage(controller.getWindow(), this, "JTimeEditor");
        pack();
    }
}