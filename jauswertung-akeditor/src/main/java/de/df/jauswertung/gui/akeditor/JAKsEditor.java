/*
 * Created on 13.11.2003
 */
package de.df.jauswertung.gui.akeditor;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.Printable;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.AboutDialogCreator;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.FileFilters;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.print.RulebookSettingsPrintable;
import de.df.jauswertung.util.ergebnis.FormelDLRG2007;
import de.df.jutils.gui.JGlassFrame;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.gui.window.JAboutDialog;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;

/**
 * @author Dennis Fabri
 */
public class JAKsEditor extends JGlassFrame {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3761966081656437047L;

    private AKsEditorPanel editor = null;
    private JPanel panel = new JPanel(new BorderLayout(5, 5));
    private boolean fullAccess = false;
    private Regelwerk aks = null;
    private String name = null;
    private boolean geaendert = false;
    private boolean appliedchange = false;
    private Window parent = null;
    private JAboutDialog about = null;
    private JNeueAK neueAK = new JNeueAK(this);
    private GroupsPanel groupspanel = new GroupsPanel(this);
    private AKsFormulaPanel formula = new AKsFormulaPanel(this);
    private AKsZusatzwertungPanel zusatz = new AKsZusatzwertungPanel(this);
    private AKsSexesPanel sexes = new AKsSexesPanel(this);
    private AKsGeneralPanel general = new AKsGeneralPanel(this);
    private AKsGesamtwertungPanel gesamtwertung = new AKsGesamtwertungPanel(this);
    private JButton ok;
    private JButton apply;
    private JButton cancel;

    private JTabbedPane tabs;

    private ISimpleCallback<JAKsEditor> callback = null;

    private boolean updating = false;
    private boolean einzel = false;

    public JAKsEditor(String akname) {
        this();
        if (akname != null) {
            doOpen(akname);
        }
    }

    public JAKsEditor() {
        this((Window) null);
    }

    private JAKsEditor(Window parent) {
        this(parent, false);
    }

    public JAKsEditor(boolean einzel) {
        this(null, einzel);
    }

    public JAKsEditor(Window parent, Regelwerk currentAks, boolean einzel, boolean[] empty) {
        this(parent, currentAks, false, einzel, empty);
    }

    private JAKsEditor(Window parent, boolean einzel) {
        this(parent, AgeGroupIOUtils.getDefaultAKs(einzel), true, einzel, null);
    }

    private JAKsEditor(Window p, Regelwerk currentAks, boolean hasFullAccess, boolean einzel, boolean[] empty) {
        if (p != null) {
            parent = p;
            parent.setEnabled(false);
        }
        setTitle(I18n.get("RulebookEditor"));
        setIconImages(IconManager.getTitleImages());
        this.aks = currentAks;
        this.fullAccess = hasFullAccess;
        this.einzel = einzel;

        setSettings();

        initPanel();
        initEditor(empty);
        initMenues();
        initButtons();
        initWindow();
        if (!hasFullAccess) {
            addActions();
        }
    }

    private void addActions() {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = 3257572818995525944L;

            @Override
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    public void setCallback(ISimpleCallback<JAKsEditor> cb) {
        callback = cb;
    }

    void notifyChange() {
        if (updating) {
            return;
        }
        if (geaendert) {
            return;
        }
        geaendert = true;
        updateButtons();
    }

    public void updateButtons() {
        if (geaendert) {
            ok.setEnabled(true);
            apply.setEnabled(true);
            if (fullAccess) {
                cancel.setText(I18n.get("Close"));
            } else {
                cancel.setText(I18n.get("Cancel"));
            }
        } else {
            ok.setEnabled(false);
            apply.setEnabled(false);
            cancel.setText(I18n.get("Close"));
        }
    }

    public boolean hasChanged() {
        return appliedchange;
    }

    private void initMenues() {
        JMenuBar menu = new JMenuBar();

        JMenu datei = new JMenu(I18n.get("File"));
        JMenuItem neu = new JMenuItem(I18n.get("New"));
        neu.setIcon(IconManager.getSmallIcon("newfile"));
        neu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                neueAK.setVisible(true);
                if (neueAK.getAltersklassen() != null) {
                    doNew(neueAK.getAltersklassen(), neueAK.isEinzelAltersklassen());
                }
            }
        });

        JMenuItem open = new JMenuItem(I18n.get("Open"));
        open.setIcon(IconManager.getSmallIcon("openfile"));
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOpen();
            }
        });
        JMenuItem save = new JMenuItem(I18n.get("Save"));
        save.setIcon(IconManager.getSmallIcon("savefile"));
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSave();
            }
        });
        JMenuItem saveas = new JMenuItem(I18n.get("SaveAs"));
        saveas.setIcon(IconManager.getSmallIcon("saveasfile"));
        saveas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSaveAs();
            }
        });

        JMenuItem preview = new JMenuItem(I18n.get("Preview"));
        preview.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preview();
            }
        });
        JMenuItem print = new JMenuItem(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                print();
            }
        });

        JMenuItem schliessen = new JMenuItem(I18n.get("Close"));
        schliessen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        });
        if (fullAccess) {
            datei.add(neu);
            datei.add(open);
            datei.add(save);

            schliessen.setText(I18n.get("Exit"));
            schliessen.setIcon(IconManager.getSmallIcon("exit"));
        } else {
            schliessen.setIcon(IconManager.getSmallIcon("cancel"));
        }
        datei.add(saveas);
        datei.add(new JSeparator());
        datei.add(preview);
        datei.add(print);
        datei.add(new JSeparator());
        datei.add(schliessen);

        JMenu info = new JMenu(I18n.get("?"));
        JMenuItem info2 = new JMenuItem(I18n.get("Info"));
        info2.setIcon(IconManager.getSmallIcon("info"));
        info2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (about == null) {
                    about = AboutDialogCreator.create(JAKsEditor.this);
                }
                about.setVisible(true);
            }
        });
        info.add(info2);

        menu.add(datei);
        JMenu[] menues = editor.getMenues();
        for (JMenu menue : menues) {
            menu.add(menue);
        }
        menu.add(info);
        setJMenuBar(menu);
    }

    private void setSettings() {
        updating = true;
        if (aks == null) {
            aks = new Regelwerk(false, FormelDLRG2007.ID);
        }

        groupspanel.setSettings(aks);
        formula.setSettings(aks);
        general.setSettings(aks);
        zusatz.setSettings(aks);
        sexes.setSettings(aks);
        formula.setSettings(aks);
        gesamtwertung.setSettings(aks);
        updating = false;
    }

    public void getSettings(Regelwerk akx) {
        editor.getAKs(akx);
        formula.getSettings(akx);
        general.getSettings(akx);
        zusatz.getSettings(akx);
        sexes.getSettings(akx);
        gesamtwertung.getSettings(akx);
        groupspanel.getSettings(akx);
    }

    private void initEditor(boolean[] empty) {
        editor = new AKsEditorPanel(this, aks, einzel, empty);
        JPanel p1 = new JPanel(new FormLayout("4dlu,fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(3)));
        p1.setBorder(BorderUtils.createSpaceBorder());
        p1.add(general, CC.xy(2, 2));
        p1.add(zusatz, CC.xy(2, 4));
        p1.add(sexes, CC.xy(2, 6));

        tabs = new JTabbedPane();
        tabs.add(I18n.get("General"), p1);
        tabs.add(I18n.get("Formula"), formula);
        tabs.add(I18n.get("AgeGroups"), editor);
        tabs.add(I18n.get("StartAndResultgroups"), groupspanel);
        // tabs.add(I18n.get("Resultgroups"), groupspanel);
        tabs.add(I18n.get("GroupEvaluation"), gesamtwertung);

        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                JTabbedPane pane = (JTabbedPane) evt.getSource();
                // int sel = pane.getSelectedIndex();
                // boolean active = sel == 1;
                boolean active = pane.getSelectedComponent() == editor;
                JMenu[] menues = editor.getMenues();
                for (JMenu menue : menues) {
                    menue.setEnabled(active);
                }
            }
        });
        tabs.setSelectedIndex(2);

        panel.add(tabs, BorderLayout.CENTER);

        setSettings();
    }

    private void initPanel() {
        panel.setBorder(BorderUtils.createSpaceBorder());
        getContentPane().add(panel);
    }

    private void initWindow() {
        pack();
        setSize(Math.max(getWidth(), 600), 700);
        WindowUtils.center(this, parent);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new EditorWindowListener());
        UIStateUtils.uistatemanage(this, "RulebookEditor");
    }

    private void initButtons() {
        FormLayout layout = new FormLayout("default:grow,fill:default,4dlu,fill:default,4dlu,fill:default", "default");
        layout.setColumnGroups(new int[][] { { 2, 4, 6 } });
        JPanel buttons = new JPanel(layout);
        ok = new JButton(I18n.get("Ok"));
        ok.setIcon(IconManager.getSmallIcon("ok"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        });
        apply = new JButton(I18n.get("Apply"));
        apply.setIcon(IconManager.getSmallIcon("apply"));
        apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doApply();
            }
        });
        cancel = new JButton(I18n.get("Close"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        });
        if (!fullAccess) {
            buttons.add(ok, CC.xy(2, 1));
            buttons.add(apply, CC.xy(4, 1));
            cancel.setIcon(IconManager.getSmallIcon("cancel"));
        } else {
            cancel.setText(I18n.get("Exit"));
            cancel.setIcon(IconManager.getSmallIcon("exit"));
        }
        buttons.add(cancel, CC.xy(6, 1));
        panel.add(buttons, BorderLayout.SOUTH);

        updateButtons();
    }

    private void doReinit() {
        // panel.removeAll();
        setSettings();
        editor = new AKsEditorPanel(this, aks, einzel, null);
        int index = tabs.getSelectedIndex();
        tabs.removeTabAt(2);
        tabs.insertTab(I18n.get("AgeGroups"), null, editor, "", 2);
        if (index == 2) {
            tabs.setSelectedIndex(2);
        }
        initMenues();
        SwingUtilities.updateComponentTreeUI(this);
    }

    public int[] getIndizes() {
        return editor.getIndizes();
    }

    void doNew(Regelwerk neu, @SuppressWarnings("hiding") boolean einzel) {
        if (neu != null) {
            name = null;
            aks = neu;
            this.einzel = einzel;
            doReinit();
        }
    }

    boolean doApply() {
        appliedchange = true;
        Regelwerk akx = new Regelwerk(aks.isEinzel(), aks.getFormelID());
        getSettings(akx);
        if (!akx.isValid()) {
            DialogUtils.warn(this, I18n.get("Title.RulebookValidationerror"),
                    I18n.get("Information.RulebookValidationerror"), I18n.get("Note.RulebookValidationerror"));
            return false;
        }

        getSettings(aks);

        geaendert = false;
        updateButtons();
        return true;
    }

    void doOpen() {
        SimpleFileFilter[] ff = new SimpleFileFilter[] { FileFilters.FF_RULEBOOKS };
        String name2 = FileChooserUtils.openFile(this, ff);
        if (name2 != null) {
            doOpen(name2);
        }
    }

    private void doOpen(String filename) {
        Regelwerk aks2 = InputManager.ladeAKs(filename);
        if (aks2 != null) {
            this.name = filename;
            aks = aks2;
            einzel = (name.endsWith(".rwe") || name.endsWith(".ake"));
            doReinit();
        } else {
            DialogUtils.wichtigeMeldung(null, I18n.get("OpenFailedText", filename));
        }
    }

    void doSave() {
        if (name == null) {
            doSaveAs();
        } else {
            if (doApply()) {
                OutputManager.speichereAKs(name, aks);
            }
        }
    }

    void doSaveAs() {
        if (!doApply()) {
            return;
        }
        SimpleFileFilter ff = FileFilters.FF_RULEBOOK_SINGLE;
        if (!aks.isEinzel()) {
            ff = FileFilters.FF_RULEBOOK_TEAM;
        }
        String name2 = FileChooserUtils.saveFile(this, ff);
        if (name2 != null) {
            name = name2;
            doSave();
        }
    }

    void doOk() {
        if (doApply()) {
            doClose();
        }
    }

    private boolean accept() {
        if (fullAccess) {
            setEnabled(false);
            boolean result = DialogUtils.askAndWarn(this, I18n.get("QuitEditorTitle", I18n.get("RulebookEditor")),
                    I18n.get("QuitEditorText", I18n.get("RulebookEditor")),
                    I18n.get("QuitEditor.Note", I18n.get("RulebookEditor")));
            setEnabled(true);
            return result;
        }
        return true;
    }

    void doClose() {
        if (geaendert) {
            if (!accept()) {
                return;
            }
        }
        if (parent != null) {
            parent.setEnabled(true);
        }
        EDTUtils.setVisible(this, false);
        if (fullAccess && (parent == null)) {
            EDTUtils.niceExit();
        }
    }

    void print() {
        PrintExecutor.print(getPrintable(), I18n.get("Rulebook"), this);
    }

    void preview() {
        PrintExecutor.preview(this, new AKPC(), I18n.get("Rulebook"), IconManager.getIconBundle(),
                IconManager.getTitleImages());
    }

    Printable getPrintable() {
        Regelwerk a = new Regelwerk(aks.isEinzel(), aks.getFormelID());
        getSettings(a);
        editor.getAKs(a);
        return PrintManager.getFinalPrintable(new RulebookSettingsPrintable(a, true), new Date(), true,
                I18n.get("Rulebook"));
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b && (callback != null)) {
            SwingUtilities.invokeLater(() -> callback.callback(JAKsEditor.this));
        }
    }

    class AKPC implements PrintableCreator {

        @Override
        public Printable create() {
            return getPrintable();
        }
    }

    class EditorWindowListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            doClose();
        }
    }

    public void updateStartgroups(String[] startgroups) {
        editor.updateStartgroups(startgroups);
    }

    public void updateResultgroups(String[] resultgroups) {
        editor.updateResultgroups(resultgroups);
    }

}