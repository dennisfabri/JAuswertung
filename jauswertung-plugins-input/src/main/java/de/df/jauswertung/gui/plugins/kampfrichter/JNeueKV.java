package de.df.jauswertung.gui.plugins.kampfrichter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.buttonbar.JButtonBar;

import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.UIUtils;
import de.df.jutils.gui.util.WindowUtils;

/**
 * @author Dennis Fabri
 * @since 07.04.2004
 */
class JNeueKV extends JDialog {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3906935560202105655L;

    private static String[]   einzelNamen      = null;

    JButtonBar                einzel           = getEinzelBar();
    JButton                   cancel           = getCancelButton();

    KampfrichterVerwaltung    result           = null;
    JFrame                    parent           = null;

    static {
        readFilenames();
    }

    JNeueKV(JFrame parent) {
        super(parent, I18n.get("NeueKampfrichtereinteilung"), true);
        this.parent = parent;

        JPanel info = createInfoPanel();

        JPanel c = new JPanel();
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,fill:default,4dlu", "4dlu,fill:default:grow,4dlu,fill:default,4dlu");

        c.setLayout(layout);
        c.add(einzel, CC.xywh(2, 2, 2, 1));
        c.add(cancel, CC.xy(3, 4));

        add(info, BorderLayout.NORTH);
        add(c, BorderLayout.CENTER);

        setResizable(false);
        pack();
        UIStateUtils.uistatemanage(parent, this, "NeueKampfrichtereinteilung");
        pack();
        setSize(getWidth(), getHeight() + 40);

        addActions();
    }

    private JPanel createInfoPanel() {
        return UIUtils.createHeaderPanel(I18n.get("ChooseRefereePresets.Information"), I18n.get("ChooseRefereePresets.Note"));
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

    private static void readFilenames() {
        String dirname = Utils.getUserDir() + "referees";
        File dir = new File(dirname);
        if ((!dir.exists()) || (!dir.isDirectory())) {
            einzelNamen = new String[0];
            return;
        }
        einzelNamen = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                if (!name.toLowerCase().endsWith(".kr")) {
                    return false;
                }
                try {
                    if (InputManager.ladeKampfrichter(file.getCanonicalPath() + File.separator + name) == null) {
                        return false;
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                }
                return true;
            }
        });
        Arrays.sort(einzelNamen);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            result = null;
        }
        // dialog.setEnabled(!visible);
        super.setVisible(visible);
    }

    KampfrichterVerwaltung getKampfrichter() {
        return result;
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

    /**
     * 
     */
    private JButtonBar getEinzelBar() {
        JButtonBar buttonBar = new JButtonBar();
        buttonBar.setBorder(BorderUtils.createLabeledBorder(I18n.get("RefereeDefinitions"), true));

        for (String anEinzelNamen : einzelNamen) {
            JButton aks = new JButton(anEinzelNamen.substring(3, anEinzelNamen.length() - 3), IconManager.getBigIcon("new"));
            aks.addActionListener(new ClickEinzelListener(anEinzelNamen));
            buttonBar.add(aks);
        }
        return buttonBar;
    }

    private class ClickEinzelListener implements ActionListener {

        private String name = null;

        public ClickEinzelListener(String akname) {
            name = akname;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            KampfrichterVerwaltung kt = InputManager.ladeKampfrichter(name);
            if (kt == null) {
                throw new NullPointerException("Could not load " + name + ".");
            }
            result = kt;
            dispose();
        }
    }
}