package de.df.jauswertung.ares.gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.ares.export.AresExportSet;
import de.df.jauswertung.ares.export.AresExportSetFile;
import de.df.jauswertung.ares.export.AresExportSetFormatException;
import de.df.jauswertung.ares.export.AresWriter;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.jlist.ModifiableListModel;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JAresWriter extends JFrame {

    public JAresWriter() throws HeadlessException {
        super(I18n.get("AresWriter"));

        initComponents();
        initMenu();
        initEvents();
        initUI();
        checkStates();

        setMinimumSize(new Dimension(400, 300));
        this.pack();
        setIconImages(IconManager.getTitleImages());
        UIStateUtils.uistatemanage(this);
    }

    private static final SimpleFileFilter EXPORT_SET_FILTER = new SimpleFileFilter(I18n.get("AresWriter.ExportSet"),
            AresExportSetFile.EXTENSION);

    private final AresExportSetFile exportSetFile = new AresExportSetFile();
    private AresExportSet savedExportSet = new AresExportSet(List.of(), null);

    private ModifiableListModel<FileLocation> filesmodel;
    private JList<FileLocation> files;
    private JTextField directory;
    private JButton add;
    private JButton remove;
    private JMenuItem saveExportSet;
    private JButton export;

    private void initComponents() {
        filesmodel = new ModifiableListModel<>();
        files = new JList<>(filesmodel);
        files.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        directory = new JWarningTextField(true, false);
        add = new JButton(IconManager.getSmallIcon("more"));
        remove = new JButton(IconManager.getSmallIcon("less"));

        export = new JButton(I18n.get("Export"));
    }

    private void initMenu() {
        JMenuItem open = new JMenuItem(I18n.get("Open"), IconManager.getSmallIcon("openfile"));
        open.setAccelerator(I18n.getKeyStroke("openfile"));
        open.addActionListener(e -> doOpenExportSet());
        saveExportSet = new JMenuItem(I18n.get("Save"), IconManager.getSmallIcon("savefile"));
        saveExportSet.setAccelerator(I18n.getKeyStroke("savefile"));
        saveExportSet.addActionListener(e -> doSaveExportSet());

        JMenuItem close = new JMenuItem(I18n.get("Close"));
        close.addActionListener(e -> doClose());

        JMenu datei = new JMenu(I18n.get("File"));
        datei.add(open);
        datei.add(saveExportSet);
        datei.addSeparator();
        datei.add(close);

        JMenuBar menubar = new JMenuBar();
        menubar.add(datei);
        setJMenuBar(menubar);
    }

    private void initUI() {
        FormLayout layout = new FormLayout(
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu");
        setLayout(layout);
        add(add, CC.xy(2, 2));
        add(remove, CC.xy(4, 2));
        add(new JScrollPane(files), CC.xyw(2, 4, 7));
        add(new JLabel(I18n.get("AresWriter.ExportDirectory")), CC.xyw(2, 6, 7));
        add(directory, CC.xyw(2, 8, 5));
        add(export, CC.xy(8, 8));
    }

    private void initEvents() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doClose();
            }
        });
        export.addActionListener(e -> {
            doExport();
        });
        add.addActionListener(e -> {
            doAdd();
        });
        remove.addActionListener(e -> {
            doRemove();
        });
        directory.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkStates();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkStates();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkStates();
            }
        });
    }

    private void checkStates() {
        checkRemove();
        checkSaveExportSet();
        checkExport();
    }

    private void checkSaveExportSet() {
        saveExportSet.setEnabled(filesmodel.size() > 0);
    }

    private void checkExport() {
        boolean ok = files.getModel().getSize() > 0;
        if (ok) {
            File dir = new File(directory.getText());
            ok = dir.exists() && dir.isDirectory();
        }
        export.setEnabled(ok);
    }

    private void checkRemove() {
        boolean ok = files.getModel().getSize() > 0;
        remove.setEnabled(ok);

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void doExport() {
        String dir = directory.getText();
        try {
            String[] filenames = new String[filesmodel.size()];
            for (int x = 0; x < filesmodel.size(); x++) {
                filenames[x] = filesmodel.getElementAt(x).getFilename();
            }

            new AresWriter().write(filenames, dir);
        } catch (IOException ex) {
            ex.printStackTrace();
            DialogUtils.showException(this, I18n.get("Error"), ex.getMessage(),
                    I18n.get("ExceptionDuringExport.Note"), ex);
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            DialogUtils.showException(this, I18n.get("Error"), I18n.get("ExceptionDuringExport"),
                    I18n.get("ExceptionDuringExport.Note"), ex);
            return;
        }
        DialogUtils.inform(this, I18n.get("ExportSuccessfull", dir), I18n.get("ExportSuccessfull.Note", dir));
    }

    private void doAdd() {
        String fn = FileChooserUtils.openFile(this, new SimpleFileFilter(I18n.get("Competition"), "wk"));
        if (fn != null && !fn.isEmpty()) {
            filesmodel.addLast(new FileLocation(fn));
        }
        checkStates();
    }

    private void doRemove() {
        if (files.isSelectionEmpty()) {
            return;
        }
        filesmodel.remove(files.getSelectedIndex());
        checkStates();
    }

    private void doOpenExportSet() {
        String fn = FileChooserUtils.openFile(this, EXPORT_SET_FILTER);
        if (fn == null || fn.isEmpty()) {
            return;
        }
        AresExportSet set;
        try {
            set = exportSetFile.read(Path.of(fn));
        } catch (IOException | AresExportSetFormatException | InvalidPathException ex) {
            log.warn("Could not open Ares export set {}", fn, ex);
            DialogUtils.error(this, I18n.get("Error"), I18n.get("OpenFailed"), I18n.get("OpenFailed.Note", fn));
            return;
        }
        filesmodel.clear();
        set.competitions().forEach(c -> filesmodel.addLast(new FileLocation(c.toString())));
        directory.setText(set.directory() == null ? "" : set.directory().toString());
        savedExportSet = createExportSet();
        checkStates();
        warnAboutMissingCompetitions(set.competitions());
    }

    private void warnAboutMissingCompetitions(List<Path> competitions) {
        String missing = competitions.stream().filter(c -> !c.toFile().isFile()).map(Path::toString)
                .collect(Collectors.joining("\n"));
        if (!missing.isEmpty()) {
            DialogUtils.warn(this, I18n.get("AresWriter.MissingCompetitions"),
                    I18n.get("AresWriter.MissingCompetitions.Note", missing));
        }
    }

    private void doSaveExportSet() {
        String fn = FileChooserUtils.saveFile(this, EXPORT_SET_FILTER);
        if (fn == null || fn.isEmpty()) {
            return;
        }
        if (new File(fn).exists() && !DialogUtils.ask(this, I18n.get("OverwriteFileQuestion", fn),
                I18n.get("OverwriteFileQuestion.Note", fn))) {
            return;
        }
        try {
            AresExportSet set = createExportSet();
            exportSetFile.write(set, Path.of(fn));
            savedExportSet = set;
        } catch (IOException | InvalidPathException ex) {
            log.warn("Could not save Ares export set {}", fn, ex);
            DialogUtils.error(this, I18n.get("Error"), I18n.get("SaveFailedText", fn),
                    I18n.get("SaveFailedText.Note"));
        }
    }

    private AresExportSet createExportSet() {
        List<Path> competitions = filesmodel.getAllElements().stream().map(f -> Path.of(f.getFilename())).toList();
        String dir = directory.getText().trim();
        return new AresExportSet(competitions, dir.isEmpty() ? null : Path.of(dir));
    }

    private boolean hasUnsavedChanges() {
        try {
            return !createExportSet().equals(savedExportSet);
        } catch (InvalidPathException ex) {
            return true;
        }
    }

    private void doClose() {
        if (hasUnsavedChanges() && !DialogUtils.askAndWarn(this, I18n.get("QuitEditorTitle", I18n.get("AresWriter")),
                I18n.get("QuitEditorText", I18n.get("AresWriter")), I18n.get("QuitEditor.Note"))) {
            return;
        }
        setVisible(false);
        EDTUtils.niceExit();
    }
}
