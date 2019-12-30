/*
 * Created on 01.03.2005
 */
package de.df.jauswertung.gui.plugins.editor;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.ZWUtils;
import de.df.jutils.gui.JDoubleField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.util.StringTools;

class JZWEditor extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 3256719580843227188L;

    ASchwimmer                schwimmer        = null;
    JDoubleField[]            integer          = null;
    JButton                   ok               = null;
    IPluginManager            controller       = null;
    final JFrame              parent;

    /**
     * This is the default constructor
     */
    public JZWEditor(IPluginManager c, ASchwimmer s) {
        super(c.getWindow(), I18n.get("ZWInput"), true);
        controller = c;
        parent = c.getWindow();

        schwimmer = s;

        initialize();
        addActions();
    }

    @Override
    public void setVisible(boolean visible) {
        controller.getWindow().setEnabled(!visible);
        super.setVisible(visible);
    }

    private void addActions() {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = 3257572818995525944L;

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        Action enterAction = new AbstractAction() {
            private static final long serialVersionUID = 3257572818995525944L;

            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "ENTER");
        getRootPane().getActionMap().put("ENTER", enterAction);
    }

    void doOk() {
        for (int index = 0; index < schwimmer.getMaximaleHLW(); index++) {
            if ((!integer[index].isValidDouble()) && (integer[index].getText().length() > 0) && (!integer[index].isSpecialString())) {
                Toolkit.getDefaultToolkit().beep();
                integer[index].requestFocus();
                return;
            }
        }

        for (int index = 0; index < schwimmer.getMaximaleHLW(); index++) {
            if ((!integer[index].isValidDouble()) && (integer[index].getText().length() > 0)) {
                if (integer[index].isSpecialString()) {
                    schwimmer.setHLWState(index, ZWUtils.getHLWState(schwimmer.getWettkampf(), integer[index].getText()));
                    controller.sendDataUpdateEvent("ChangeZWPoints", UpdateEventConstants.REASON_POINTS_CHANGED, schwimmer, -1, null);
                }
            } else {
                if (integer[index].getText().length() == 0) {
                    schwimmer.setHLWState(index, HLWStates.NOT_ENTERED);
                } else {
                    schwimmer.setHLWPunkte(index, integer[index].getDouble());
                }
                controller.sendDataUpdateEvent("ChangeZWPoints", UpdateEventConstants.REASON_POINTS_CHANGED, schwimmer, -1, null);
            }
        }
        setVisible(false);
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

        ok = new JButton(I18n.get("Ok"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                doOk();
            }
        });

        JButton cancel = new JButton(I18n.get("Cancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
            }
        });

        Strafe s = schwimmer.getWettkampf().getStrafen().getNichtAngetreten();

        integer = new JDoubleField[schwimmer.getMaximaleHLW()];
        for (int x = 0; x < integer.length; x++) {
            integer[x] = new JDoubleField(10000, false);
            integer[x].setAutoSelectAll(true);
            if (s.getShortname().length() == 0) {
                integer[x].setSpecialStrings(new String[] { "n", I18n.get("DidNotStartShort") });
            } else {
                integer[x].setSpecialStrings(new String[] { "n", I18n.get("DidNotStartShort"), s.getShortname() });
            }
            integer[x].requestFocus();
            switch (schwimmer.getHLWState(x)) {
            case ENTERED:
                integer[x].setDouble(schwimmer.getHLWPunkte(x));
                break;
            case NICHT_ANGETRETEN: {
                String text = I18n.get("DidNotStartShort");
                if (s.getShortname().length() > 0) {
                    text = s.getShortname();
                }
                integer[x].setText(text);
                break;
            }
            case DISQALIFIKATION: {
                String text = I18n.get("DisqualificationShort");
                if (s.getShortname().length() > 0) {
                    text = s.getShortname();
                }
                integer[x].setText(text);
                break;
            }
            case NOT_ENTERED:
                integer[x].setDouble(JDoubleField.EMPTY_FIELD);
                break;
            }
            integer[x].getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent arg0) {
                    boolean enabled = true;
                    for (JDoubleField anInteger : integer) {
                        if (!anInteger.isValidDouble() && !anInteger.isSpecialString()) {
                            enabled = false;
                            break;
                        }
                    }
                    ok.setEnabled(enabled);
                }

                @Override
                public void removeUpdate(DocumentEvent arg0) {
                    insertUpdate(arg0);
                }

                @Override
                public void changedUpdate(DocumentEvent arg0) {
                    insertUpdate(arg0);
                }
            });
        }

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(integer.length + 2));
        FormLayoutUtils.setRowGroups(layout, integer.length + 2);
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Input")));
        if (integer.length == 1) {
            top.add(new JLabel(I18n.get("Points")), CC.xy(2, 2));
            top.add(integer[0], CC.xy(4, 2));
        } else {
            for (int x = 0; x < integer.length; x++) {
                top.add(new JLabel(I18n.get("PointsX", StringTools.ABC[x])), CC.xy(2, x * 2 + 2));
                top.add(integer[x], CC.xy(4, x * 2 + 2));
            }
        }
        top.add(ok, CC.xy(4, integer.length * 2 + 2));
        top.add(cancel, CC.xy(4, integer.length * 2 + 4));

        layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4, 6 } });
        JPanel bottom = new JPanel(layout);
        bottom.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information")));
        bottom.add(new JLabel(I18n.get("Name")), CC.xy(2, 2));
        bottom.add(new JLabel(I18n.get("Organisation")), CC.xy(2, 4));
        bottom.add(new JLabel(I18n.get("AgeGroup")), CC.xy(2, 6));

        bottom.add(new JLabel(schwimmer.getName()), CC.xy(4, 2));
        bottom.add(new JLabel(schwimmer.getGliederung()), CC.xy(4, 4));
        bottom.add(new JLabel(schwimmer.getAK().toString() + " " + I18n.geschlechtToString(schwimmer)), CC.xy(4, 6));

        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);

        pack();
        WindowUtils.center(this, parent);
        setResizable(false);
        UIStateUtils.uistatemanage(this, getClass().toString());
        pack();
    }
}