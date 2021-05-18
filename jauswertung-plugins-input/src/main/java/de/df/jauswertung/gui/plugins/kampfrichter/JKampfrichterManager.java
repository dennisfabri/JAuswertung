/*
 * Created on 23.02.2006
 */
package de.df.jauswertung.gui.plugins.kampfrichter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.lisasp.legacy.uistate.UIStateManager;
import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.FileFilters;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jutils.gui.JGlassFrame;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPluginManager;

class JKampfrichterManager extends JGlassFrame {

    private static final long serialVersionUID = 7129899504844113383L;

    @SuppressWarnings("rawtypes")
    private AWettkampf        wk;
    KampfrichterVerwaltung    kv;

    private boolean           changed          = false;

    private JButton           close;

    JKampfrichterTeamPanel    teams            = null;

    private JFrame            parent;
    private IPluginManager    controller;

    JKampfrichterManager(JFrame parent, CorePlugin core, IPluginManager controller) {
        this.parent = parent;
        this.controller = controller;
        wk = core.getWettkampf();
        kv = wk.getKampfrichterverwaltung();
        if (kv == null) {
            throw new NullPointerException("KV must not be null.");
        }
        initGUI();
        initMenu();
        pack();
        setSize(Math.max(getWidth(), 800), 600);
        WindowUtils.center(this, parent);
        UIStateUtils.uistatemanage(this, "JKampfrichterManager");

        WindowUtils.addEscapeAction(this, new Runnable() {
            @Override
            public void run() {
                setVisible(false);
            }
        });
    }

    private void initMenu() {
        JMenuItem neu = new JMenuItem(I18n.get("New"), IconManager.getSmallIcon("newfile"));
        neu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                neu();
            }
        });
        JMenuItem load = new JMenuItem(I18n.get("Open"), IconManager.getSmallIcon("openfile"));
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                laden();
            }
        });
        JMenuItem save = new JMenuItem(I18n.get("Save"), IconManager.getSmallIcon("saveasfile"));
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speichern();
            }
        });
        JMenuItem exit = new JMenuItem(I18n.get("Close"), IconManager.getSmallIcon("close"));
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        JMenu datei = new JMenu(I18n.get("File"));
        datei.add(neu);
        datei.add(load);
        datei.add(save);
        datei.add(new JSeparator());
        datei.add(exit);

        JMenuBar menu = new JMenuBar();
        menu.add(datei);

        setJMenuBar(menu);
    }

    @Override
    public void setVisible(boolean v) {
        UIStateManager.store(this);
        parent.setEnabled(!v);
        super.setVisible(v);
        if (!v) {
            save();
        }
    }

    private SimpleFileFilter FILTER = FileFilters.FF_REFEREES;

    void neu() {
        JNeueKV nkv = new JNeueKV(this);
        nkv.setVisible(true);
        KampfrichterVerwaltung neukv = nkv.getKampfrichter();
        if (neukv != null) {
            kv = neukv;
            updateGUI();
            changed = true;
        }
    }

    void laden() {
        String name = FileChooserUtils.openFile(controller.getWindow(), FILTER);
        if (name != null) {
            KampfrichterVerwaltung neu = InputManager.ladeKampfrichter(name);
            if (neu == null) {
                DialogUtils.warn(this, I18n.get("OpenFailed"), I18n.get("OpenFailedText", name), I18n.get("OpenFailedText.Note", name));
            } else {
                kv = neu;
                updateGUI();
                changed = true;
            }
        }
    }

    void speichern() {
        String name = FileChooserUtils.saveFile(controller.getWindow(), FILTER);
        if (name != null) {
            boolean result = true;
            if (new File(name).exists()) {
                result = DialogUtils.ask(this, I18n.get("OverwriteFileQuestion", name), I18n.get("OverwriteFileQuestion.Note", name));
            }
            if (result) {
                if (hasChanged()) {
                    save();
                }
                result = OutputManager.speichereKampfrichter(name, kv);
                if (!result) {
                    String meldung = I18n.get("SaveFailedText", name);
                    String note = I18n.get("SaveFailedText.Note", name);
                    DialogUtils.warn(this, I18n.get("SaveFailed"), meldung, note);
                }
            }
        }
    }

    private void initGUI() {
        setIconImages(IconManager.getTitleImages());
        setTitle(I18n.get("Refereemanagement"));

        close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
            }
        });

        FormLayout layout = new FormLayout("4dlu,0dlu:grow,fill:default,4dlu", "4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        setLayout(layout);

        add(close, CC.xy(3, 4));

        updateGUI();
    }

    void save() {
        if (teams.hasChanged()) {
            teams.save();
            changed = true;
        }
        if (changed) {
            wk.setKampfrichterverwaltung(kv);
            controller.sendDataUpdateEvent("RefereesChanged", UpdateEventConstants.REASON_REFEREES_CHANGED, null);
        }
    }

    boolean hasChanged() {
        if (teams.hasChanged()) {
            return true;
        }
        return changed;
    }

    void updateGUI() {
        EDTUtils.executeOnEDT(new Runnable() {
            @Override
            public void run() {
                if (teams != null) {
                    remove(teams);
                }
                teams = new JKampfrichterTeamPanel(kv);

                add(teams, CC.xyw(2, 2, 2, "fill,fill"));

                repaint();

            }
        });
        EDTUtils.sleep();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                repaint();
                validate();
            }
        });
    }

    public void setChanged() {
        changed = true;
    }
}