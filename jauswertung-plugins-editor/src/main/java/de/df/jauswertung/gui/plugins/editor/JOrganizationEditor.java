/*
 * Created on 01.03.2005
 */
package de.df.jauswertung.gui.plugins.editor;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPluginManager;

class JOrganizationEditor extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 3256719580843227188L;

    JWarningTextField         value            = null;
    JButton                   ok               = null;

    final IPluginManager      controller;
    final CorePlugin          core;
    final String              gld;

    boolean                   changed          = false;

    /**
     * This is the default constructor
     */
    public JOrganizationEditor(IPluginManager controller, CorePlugin core, String gld, JFrame parent) {
        super(parent, I18n.get("OrganizationInput"), true);
        this.controller = controller;
        this.core = core;

        this.gld = gld;

        initialize();
        addActions();
    }

    public boolean hasChanged() {
        return changed;
    }

    @Override
    public void setVisible(boolean visible) {
        controller.getWindow().setEnabled(!visible);
        super.setVisible(visible);
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

    @SuppressWarnings("rawtypes")
    void doOk() {
        if (value.getText().length() == 0) {
            Toolkit.getDefaultToolkit().beep();
            value.requestFocus();
        } else {
            if (!value.getText().equals(gld)) {
                AWettkampf wk = core.getWettkampf();
                // TODO: Check QGld
                @SuppressWarnings("unchecked")
                LinkedList<ASchwimmer> s = SearchUtils.getSchwimmer(wk, new String[] { gld }, false);
                for (ASchwimmer a : s) {
                    a.setGliederung(value.getText());
                }
                controller.sendDataUpdateEvent("ChangeOrganization", UpdateEventConstants.REASON_SWIMMER_CHANGED, null, null, null);
                changed = true;
            } else {
                changed = false;
            }
            setVisible(false);
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
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                doOk();
            }
        });

        JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
            }
        });

        value = new JWarningTextField(true, false);
        value.setAutoSelectAll(true);
        value.setText(gld);
        value.requestFocus();

        FormLayout buttonLayout = new FormLayout("fill:default:grow,fill:default,4dlu,fill:default", "fill:default");
        buttonLayout.setColumnGroups(new int[][] { { 2, 4 } });
        JPanel buttons = new JPanel(buttonLayout);
        buttons.add(ok, CC.xy(2, 1));
        buttons.add(cancel, CC.xy(4, 1));

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4 } });
        JPanel top = new JPanel(layout);
        top.setBorder(BorderUtils.createLabeledBorder(I18n.get("Input")));
        top.add(new JLabel(I18n.get("Organization")), CC.xy(2, 2));

        top.add(value, CC.xy(4, 2));
        top.add(buttons, CC.xyw(2, 4, 3));

        layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4, 6, 8, 10 } });

        add(top, BorderLayout.CENTER);

        pack();
        setResizable(false);
        UIStateUtils.uistatemanage(controller.getWindow(), this, "JOrganizationEditor");
        pack();
    }
}