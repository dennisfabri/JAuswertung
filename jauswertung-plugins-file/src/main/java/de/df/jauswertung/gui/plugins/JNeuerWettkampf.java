package de.df.jauswertung.gui.plugins;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
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

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.buttonbar.JButtonBar;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.FileFilters;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.UIUtils;
import de.df.jutils.gui.util.WindowUtils;

/**
 * @author Dennis Fabri
 * @since 07.04.2004
 */
public class JNeuerWettkampf extends JDialog {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3906935560202105655L;

    private static String[] einzelNamen = null;
    private static String[] mannschaftNamen = null;
    private static String[] einzelTooltip = null;
    private static String[] mannschaftTooltip = null;

    JButtonBar einzel = getEinzelBar();
    JButtonBar staffel = getMannschaftStaffelBar();
    JButton cancel = getCancelButton();

    @SuppressWarnings("rawtypes")
    AWettkampf result = null;
    JFrame parent = null;

    static {
        readFilenames();
    }

    public JNeuerWettkampf(JFrame parent) {
        super(parent, I18n.get("NewCompetition"), true);
        this.parent = parent;

        JPanel info = createInfoPanel();

        Container c = getContentPane();
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,fill:default,4dlu",
                "0dlu,fill:default,4dlu,fill:default:grow," + "4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 4, 6 } });
        c.setLayout(layout);
        c.add(info, CC.xyw(1, 2, 4));
        c.add(einzel, CC.xyw(2, 4, 2));
        c.add(staffel, CC.xyw(2, 6, 2));
        c.add(cancel, CC.xy(3, 8));
        setResizable(false);
        pack();
        WindowUtils.center(this, parent);
        UIStateUtils.uistatemanage(this, "JNeuerWettkampf");
        pack();
        setSize(getWidth(), getHeight() + 80);

        addActions();
    }

    private JPanel createInfoPanel() {
        return UIUtils.createHeaderPanel(I18n.get("Information.ChooseRulebook"), null);
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
        String dirname = Utils.getUserDir() + "aks";
        File dir = new File(dirname);
        if ((!dir.exists()) || (!dir.isDirectory())) {
            einzelNamen = new String[0];
            mannschaftNamen = einzelNamen;
            return;
        }
        einzelNamen = dir.list((file, name) -> {
            if ((!name.toLowerCase().endsWith(".ake")) && (!name.toLowerCase().endsWith(".rwe"))) {
                return false;
            }
            try {
                if (AgeGroupIOUtils.ladeAKs(file.getCanonicalPath() + File.separator + name) == null) {
                    return false;
                }
            } catch (IOException e) {
                // e.printStackTrace();
            }
            return true;
        });
        Arrays.sort(einzelNamen);
        mannschaftNamen = dir.list((file, name) -> {
            if ((!name.toLowerCase().endsWith(".akm")) && (!name.toLowerCase().endsWith(".rwm"))) {
                return false;
            }
            try {
                if (AgeGroupIOUtils.ladeAKs(file.getCanonicalPath() + File.separator + name) == null) {
                    return false;
                }
            } catch (IOException e) {
                // e.printStackTrace();
            }
            return true;
        });
        Arrays.sort(mannschaftNamen);

        einzelTooltip = new String[einzelNamen.length];
        for (int x = 0; x < einzelNamen.length; x++) {
            Regelwerk aks = AgeGroupIOUtils.ladeAKs(dirname + File.separator + einzelNamen[x]);
            if (aks != null) {
                einzelTooltip[x] = aks.getBeschreibung();
                if (einzelTooltip[x].length() == 0) {
                    einzelTooltip[x] = null;
                }
            }
        }

        mannschaftTooltip = new String[mannschaftNamen.length];
        for (int x = 0; x < mannschaftNamen.length; x++) {
            Regelwerk aks = AgeGroupIOUtils.ladeAKs(dirname + File.separator + mannschaftNamen[x]);
            if (aks != null) {
                mannschaftTooltip[x] = aks.getBeschreibung();
                if (mannschaftTooltip[x].length() == 0) {
                    mannschaftTooltip[x] = null;
                }
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            result = null;
        }
        // parent.setEnabled(!visible);
        super.setVisible(visible);
    }

    @SuppressWarnings("rawtypes")
    public AWettkampf getWettkampf() {
        return result;
    }

    private JButton getCancelButton() {
        JButton c = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        c.addActionListener(event -> {
            setVisible(false);
        });
        return c;
    }

    private JButtonBar getMannschaftStaffelBar() {
        JButtonBar buttonBar = new JButtonBar();
        buttonBar.setBorder(BorderUtils.createLabeledBorder(I18n.get("TeamCompetitions"), true));

        for (int x = 0; x < mannschaftNamen.length; x++) {
            JButton aks = new JButton(mannschaftNamen[x].substring(0, mannschaftNamen[x].length() - 4),
                    IconManager.getBigIcon("new"));
            aks.setToolTipText(mannschaftTooltip[x]);
            aks.addActionListener(new ClickMannschaftActionListener(mannschaftNamen[x]));
            buttonBar.add(aks);
        }
        JButton benutzer = new JButton(I18n.get("Custom"), IconManager.getBigIcon("openfile"));
        benutzer.addActionListener(new ClickCustomActionListener(ClickCustomActionListener.TYPE_STAFFEL));
        buttonBar.add(benutzer);

        return buttonBar;
    }

    /**
     * 
     */
    private JButtonBar getEinzelBar() {
        JButtonBar buttonBar = new JButtonBar();
        buttonBar.setBorder(BorderUtils.createLabeledBorder(I18n.get("PersonalCompetitions"), true));

        for (int x = 0; x < einzelNamen.length; x++) {
            JButton aks = new JButton(einzelNamen[x].substring(0, einzelNamen[x].length() - 4),
                    IconManager.getBigIcon("new"));
            aks.setToolTipText(einzelTooltip[x]);
            aks.addActionListener(new ClickEinzelListener(einzelNamen[x]));
            buttonBar.add(aks);
        }
        JButton benutzer = new JButton(I18n.get("Custom"), IconManager.getBigIcon("openfile"));
        benutzer.addActionListener(new ClickCustomActionListener(ClickCustomActionListener.TYPE_EINZEL));
        buttonBar.add(benutzer);

        return buttonBar;
    }

    private class ClickEinzelListener implements ActionListener {

        private String name = null;

        public ClickEinzelListener(String akname) {
            name = akname;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            result = new EinzelWettkampf(AgeGroupIOUtils.getAKs(name), InputManager.ladeStrafen(name, true));
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
            result = new MannschaftWettkampf(AgeGroupIOUtils.getAKs(name), InputManager.ladeStrafen(name, false));
            dispose();
        }
    }

    private class ClickCustomActionListener implements ActionListener {

        public static final int TYPE_EINZEL = 0;
        public static final int TYPE_STAFFEL = 2;

        private int type = 0;

        public ClickCustomActionListener(int t) {
            type = t;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            SimpleFileFilter ff = null;
            switch (type) {
            case TYPE_STAFFEL:
                ff = FileFilters.FF_RULEBOOKS_TEAM;
                break;
            default:
                ff = FileFilters.FF_RULEBOOKS_SINGLE;
                break;
            }
            String name = FileChooserUtils.openFile(parent, ff);
            if (name != null) {
                Regelwerk aks = AgeGroupIOUtils.ladeAKs(name);
                if (aks != null) {
                    switch (type) {
                    case TYPE_STAFFEL:
                        result = new MannschaftWettkampf(aks, InputManager.ladeStrafen(name, false));
                        break;
                    case TYPE_EINZEL:
                        result = new EinzelWettkampf(aks, InputManager.ladeStrafen(name, true));
                        break;
                    default:
                        return;
                    }
                    dispose();
                } else {
                    DialogUtils.error(parent, I18n.get("Error"), I18n.get("OpenFailedText", name),
                            I18n.get("OpenFailed.Note", name));
                }
            }
        }
    }
}