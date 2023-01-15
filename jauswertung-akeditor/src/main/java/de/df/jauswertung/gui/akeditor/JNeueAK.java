/*
 * Created on 07.04.2004
 */
package de.df.jauswertung.gui.akeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.buttonbar.JButtonBar;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.util.UIStateUtils;

/**
 * @author Dennis Fabri
 * @date 07.04.2004
 */
public class JNeueAK extends JDialog {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3906935560202105655L;

    private static String[] einzelNamen = null;
    private static String[] mannschaftNamen = null;

    JButtonBar einzel = getEinzelBar();
    JButtonBar staffel = getMannschaftStaffelBar();
    JButton cancel = getCancelButton();
    Regelwerk result = null;

    boolean einzelak = false;

    static {
        readFilenames();
    }

    public JNeueAK(JFrame parent) {
        super(parent, I18n.get("NewAgeGroups"), true);

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,fill:default,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4 } });
        setLayout(layout);
        add(einzel, CC.xywh(2, 2, 2, 1));
        add(staffel, CC.xywh(2, 4, 2, 1));
        add(cancel, CC.xy(3, 6));
        setResizable(false);
        UIStateUtils.uistatemanage(this, JNeueAK.class.getName());
        pack();
        setSize(getWidth(), getHeight() + 80);

        addActions();
    }

    private static void readFilenames() {
        File dir = new File("aks");
        if ((!dir.exists()) || (!dir.isDirectory())) {
            einzelNamen = new String[0];
            mannschaftNamen = einzelNamen;
            return;
        }
        einzelNamen = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.toLowerCase().endsWith(".ake") || name.toLowerCase().endsWith(".rwe");
            }
        });
        mannschaftNamen = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.toLowerCase().endsWith(".akm") || name.toLowerCase().endsWith(".rwm");
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            result = null;
        }
        super.setVisible(visible);
    }

    public Regelwerk getAltersklassen() {
        return result;
    }

    public boolean isEinzelAltersklassen() {
        return einzelak;
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
    }

    private JButton getCancelButton() {
        JButton c = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        c.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setVisible(false);
            }
        });
        return c;
    }

    private JButtonBar getMannschaftStaffelBar() {
        JButtonBar buttonBar = new JButtonBar();
        buttonBar.setBorder(BorderUtils.createLabeledBorder(I18n.get("TeamCompetitions"), true));

        for (String aMannschaftNamen : mannschaftNamen) {
            JButton aks = new JButton(aMannschaftNamen.substring(0, aMannschaftNamen.length() - 4),
                    IconManager.getBigIcon("new"));
            aks.addActionListener(new ClickMannschaftActionListener(aMannschaftNamen));
            buttonBar.add(aks);
        }

        return buttonBar;
    }

    /**
     * 
     */
    private JButtonBar getEinzelBar() {
        JButtonBar buttonBar = new JButtonBar();
        buttonBar.setBorder(BorderUtils.createLabeledBorder(I18n.get("PersonalCompetitions"), true));

        for (String anEinzelNamen : einzelNamen) {
            JButton aks = new JButton(anEinzelNamen.substring(0, anEinzelNamen.length() - 4),
                    IconManager.getBigIcon("new"));
            aks.addActionListener(new ClickEinzelActionListener(anEinzelNamen));
            buttonBar.add(aks);
        }

        return buttonBar;
    }

    private class ClickEinzelActionListener implements ActionListener {

        private String name = null;

        public ClickEinzelActionListener(String akname) {
            name = akname;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            result = AgeGroupIOUtils.getAKs(name);
            einzelak = true;
            dispose();
        }
    }

    private class ClickMannschaftActionListener implements ActionListener {

        private String name = null;

        public ClickMannschaftActionListener(String akname) {
            name = akname;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            result = AgeGroupIOUtils.getAKs(name);
            einzelak = false;
            dispose();
        }
    }
}