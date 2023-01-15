package de.df.jauswertung.ares.gui;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.ares.export.AresWriterDefault;
import de.df.jauswertung.ares.export.AresWriterFinals;
import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.jlist.ModifiableListModel;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;

public class JAresWriter extends JFrame {

    public JAresWriter() throws HeadlessException {
        super(I18n.get("AresWriter"));

        initComponents();
        initEvents();
        initUI();
        checkStates();

        setMinimumSize(new Dimension(400, 300));
        this.pack();
        setIconImages(IconManager.getTitleImages());
        UIStateUtils.uistatemanage(this);
    }

    private ModifiableListModel<FileLocation> filesmodel;
    private JList<FileLocation> files;
    private JTextField directory;
    private JButton add;
    private JButton remove;
    private JButton export;
    private JButton close;

    private void initComponents() {
        filesmodel = new ModifiableListModel<>();
        files = new JList<>(filesmodel);
        files.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        directory = new JWarningTextField(true, false);
        add = new JButton(IconManager.getSmallIcon("more"));
        remove = new JButton(IconManager.getSmallIcon("less"));

        export = new JButton(I18n.get("Export"));
        close = new JButton(I18n.get("Close"));
    }

    private void initUI() {
        FormLayout layout = new FormLayout(
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setColumnGroups(new int[][] { new int[] { 8, 10 } });
        setLayout(layout);
        add(add, CC.xy(2, 2));
        add(remove, CC.xy(4, 2));
        add(new JScrollPane(files), CC.xyw(2, 4, 9));
        add(new JLabel("Export-Verzeichnis:"), CC.xyw(2, 6, 9));
        add(directory, CC.xyw(2, 8, 9));
        add(export, CC.xy(8, 10));
        add(close, CC.xy(10, 10));
    }

    private void initEvents() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                EDTUtils.niceExit();
            }
        });
        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doExport();
            }
        });
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JAresWriter.this.setVisible(false);
                EDTUtils.niceExit();
            }
        });
        add.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doAdd();
            }
        });
        remove.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doRemove();
            }
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
        checkExport();
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
            if (filesmodel == null || filesmodel.size() == 0) {
                DialogUtils.warn(this, I18n.get("NoFilesSelected"), I18n.get("NoFilesSelected.Note"));
                return;
            }

            AWettkampf[] wks = new AWettkampf[filesmodel.size()];
            for (int x = 0; x < filesmodel.size(); x++) {
                String filename = filesmodel.getElementAt(x).getFilename();
                wks[x] = InputManager.ladeWettkampf(filename);
                if (wks[x] == null) {
                    DialogUtils.warn(this, I18n.get("CouldNotOpenFile", filename),
                            I18n.get("CouldNotOpenFile.Note", filename));
                    return;
                }
            }
            writeAres(wks, dir);
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

    private <T extends ASchwimmer> void writeAres(AWettkampf<T>[] wks, String dir) throws IOException {
        boolean hasFinals = FormelManager.isHeatBased(wks[0].getRegelwerk().getFormelID());
        for (int x = 1; x < wks.length; x++) {
            if (hasFinals != FormelManager.isHeatBased(wks[x].getRegelwerk().getFormelID())) {
                DialogUtils.warn(this, I18n.get("CouldNotOpenFile"), I18n.get("CouldNotOpenFile.Note"));
                return;
            }
        }
        if (hasFinals) {
            AresWriterFinals.writeAres(wks, dir);
            // AresWriterFinals.writeAnzeigetafel(wks, dir);
        } else {
            AresWriterDefault.writeAres(wks, dir);
        }
    }
}
