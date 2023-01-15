package de.df.jauswertung.gui.plugins.aselection;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.JGlassFrame;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPluginManager;

class JSelectionFrame extends JGlassFrame {

    private static final long serialVersionUID = 3735076099011819345L;

    private final JFrame parent;
    private final CorePlugin core;
    private final FEditorPlugin editor;
    private final IPluginManager controller;

    private final String meldekey;

    private boolean changed = false;

    private JTabbedPane tabs = null;
    @SuppressWarnings("rawtypes")
    private JSelectionPanel[] panels = null;

    private final String i18nprefix;
    private final AMSelectionPlugin root;

    public JSelectionFrame(JFrame parent, AMSelectionPlugin root, CorePlugin core, FEditorPlugin editor,
            IPluginManager con, String i18nprefix,
            String meldekey) {
        super(I18n.get(i18nprefix + ".Title"));
        this.i18nprefix = i18nprefix;
        setIconImage(parent.getIconImage());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.parent = parent;
        this.core = core;
        this.editor = editor;
        this.root = root;
        this.controller = con;
        this.meldekey = meldekey;

        initGUI();
        add(getButtons(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(800, 600));
        pack();
        UIStateUtils.uistatemanage(parent, this, "JSelectionFrame");

        WindowUtils.addEscapeAction(this, this::doOk);
        WindowUtils.addEnterAction(this, this::doOk);
    }

    @Override
    public void setVisible(boolean v) {
        parent.setEnabled(!v);
        super.setVisible(v);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void initGUI() {
        setEnabled(false);

        initMenu();

        AWettkampf wk = core.getWettkampf();

        if (tabs != null) {
            remove(tabs);
        }
        tabs = new JTabbedPane();
        tabs.setBorder(BorderUtils.createSpaceBorder());
        add(tabs, BorderLayout.CENTER);
        panels = new JSelectionPanel[wk.getRegelwerk().size()];

        boolean found = false;
        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            LinkedList<ASchwimmer> liste = SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(x));
            if (liste.size() > 0) {
                found = true;

                panels[x] = new JSelectionPanel(this, root, editor, liste);
                JScrollPane scr = new JScrollPane(panels[x]);
                scr.setBorder(null);
                scr.getVerticalScrollBar().setUnitIncrement(10);
                scr.setPreferredSize(new Dimension(800, 600));
                scr.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                tabs.add(wk.getRegelwerk().getAk(x).getName(), scr);
            }
        }

        if (!found) {
            remove(tabs);
            tabs = null;
            panels = new JSelectionPanel[0];
        }

        setEnabled(true);
    }

    void callWizard() {
        doApply();
        new JSelectionWizard(this, root, core, controller, i18nprefix, meldekey).start();
        changed = true;
        doUpdate();
    }

    private void initMenu() {
        JMenuItem neu = new JMenuItem(I18n.get("Wizard"));
        neu.addActionListener(e -> {
            callWizard();
        });

        JMenuItem close = new JMenuItem(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(e -> {
            doOk();
        });

        JMenu menu = new JMenu(I18n.get("File"));
        menu.add(neu);
        menu.add(new JSeparator());
        menu.add(close);

        JMenuBar menubar = new JMenuBar();
        menubar.add(menu);
        setJMenuBar(menubar);
    }

    private JPanel getButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(e -> {
            doOk();
        });
        p.add(close);
        return p;
    }

    void doApply() {
        if (panels != null) {
            for (JSelectionPanel<?> panel : panels) {
                if (panel != null) {
                    if (panel.hasChanged()) {
                        changed = true;
                        panel.doSave();
                    }
                }
            }
            if (changed) {
                controller.sendDataUpdateEvent(I18n.get(i18nprefix + ".Event"),
                        UpdateEventConstants.REASON_SWIMMER_CHANGED, null);
                changed = false;
            }
        }
    }

    void doUpdate() {
        if (panels != null) {
            for (JSelectionPanel<?> panel : panels) {
                if (panel != null) {
                    panel.doUpdate();
                }
            }
        }
    }

    void doOk() {
        doApply();

        setVisible(false);
        dispose();
    }
}