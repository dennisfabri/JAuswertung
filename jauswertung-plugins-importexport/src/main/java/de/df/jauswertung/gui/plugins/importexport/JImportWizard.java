/*
 * Created on 11.07.2004
 */
package de.df.jauswertung.gui.plugins.importexport;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;

import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.record.RecordInputStream.LeftoverDataException;
import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.xduke.xswing.DataTipManager;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.exception.NotEnabledException;
import de.df.jauswertung.exception.NotSupportedException;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.WizardUIElementsProvider;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.io.IImporter;
import de.df.jauswertung.io.ImportExportTypes;
import de.df.jauswertung.io.ImportManager;
import de.df.jauswertung.io.TableEntryException;
import de.df.jauswertung.io.TableException;
import de.df.jauswertung.io.TableFormatException;
import de.df.jauswertung.io.value.ZWStartnummer;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.autocomplete.FileAutoCompleter;
import de.df.jutils.gui.border.ShadowBorder;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.wizard.AWizardPage;
import de.df.jutils.gui.wizard.CancelListener;
import de.df.jutils.gui.wizard.FinishListener;
import de.df.jutils.gui.wizard.JWizard;
import de.df.jutils.gui.wizard.JWizardFrame;
import de.df.jutils.gui.wizard.PageSwitchListener;
import de.df.jutils.gui.wizard.UpdateListener;
import de.df.jutils.gui.wizard.WizardOptionPage;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.StringTools;
import de.df.jutils.util.SystemOutFeedback;

/**
 * @author Dennis Fabri
 * @date 11.07.2004
 */
class JImportWizard extends JWizardFrame implements FinishListener, CancelListener {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3545515093153625141L;

    CorePlugin core;
    IPluginManager controller;

    JTypeChooser type = null;
    JFormatChooser format = null;
    JFilePage file = null;
    Object results = null;

    public JImportWizard(JFrame parent, CorePlugin c, IPluginManager con, ImportExportMode mode) {
        super(parent, I18n.get("Import"), WizardUIElementsProvider.getInstance(), false);
        if (c == null) {
            throw new NullPointerException("coreplugin must not be null");
        }
        if (parent == null) {
            throw new NullPointerException("Parent window must not be null");
        }
        core = c;
        controller = con;

        JWizard wizard = getWizard();

        wizard.addListener(this);

        type = new JTypeChooser(mode);
        wizard.addPage(type);

        format = new JFormatChooser();
        wizard.addPage(format);

        file = new JFilePage();
        wizard.addPage(file);

        wizard.addPage(new JProgressPage());

        wizard.addPage(new JImportPage());

        setResizable(false);
        setIconImage(parent.getIconImage());
        pack();
        setSize(getWidth(), getHeight() + 10);
        UIStateUtils.uistatemanage(parent, this, JImportWizard.class.getName());
        pack();
        setSize(getWidth(), getHeight() + 10);

        addActions();
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

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            results = null;
            getWizard().setFinishButtonEnabled(false);
        }
        super.setVisible(visible);
    }

    /**
     * 
     */
    void browseFile() {
        IImporter i = ImportManager.getImporter(format.getSelectedItemname());
        String[] names = null;
        if (ImportManager.isMultifileImportAllowed(ImportExportTypes.getByValue(type.getSelectedIndex()))) {
            names = FileChooserUtils.openFiles(JImportWizard.this, new SimpleFileFilter(i.getName(), i.getSuffixes()));
        } else {
            String name = FileChooserUtils.openFile(JImportWizard.this,
                    new SimpleFileFilter(i.getName(), i.getSuffixes()));
            if (name != null) {
                names = new String[] { name };
            }
        }
        if (names != null) {
            file.filename.setText(StringTools.concatenateFilenames(names));
        }
        file.update();
    }

    @Override
    public void finish() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        EDTUtils.setEnabled(this, false);

        SwingWorker<Boolean, Object> sw = new SwingWorker<Boolean, Object>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                return finishImport();
            }

            @Override
            protected void done() {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                boolean success = false;
                try {
                    success = get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // Nothing to do
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    // Nothing to do
                }
                if (success) {
                    JImportWizard.this.setEnabled(true);
                    JImportWizard.this.setVisible(false);
                } else {
                    DialogUtils.warn(JImportWizard.this, I18n.get("ImportFailed"), I18n.get("ImportFailed.Note"));
                    JImportWizard.this.setEnabled(true);
                }
            }
        };
        sw.execute();
    }

    @SuppressWarnings({ "rawtypes" })
    boolean finishImport() {
        Object data = null;
        if (results != null) {
            data = ImportManager.finishImport(ImportExportTypes.getByValue(type.getSelectedIndex()), core.getWettkampf(), results,
                    new SystemOutFeedback());
            results = null;
            if (data != null) {
                switch (ImportExportTypes.getByValue(type.getSelectedIndex())) {
                case REGISTRATION:
                    controller.sendDataUpdateEvent("Import",
                            UpdateEventConstants.REASON_NEW_TN | UpdateEventConstants.REASON_GLIEDERUNG_CHANGED, data,
                            null, null);
                    break;
                case HEATLIST:
                    core.setWettkampf((AWettkampf) data, false, "Import");
                    controller.sendDataUpdateEvent("Import", UpdateEventConstants.REASON_LAUF_LIST_CHANGED, data, null,
                            null);
                    break;
                case ZWLIST:
                    core.setWettkampf((AWettkampf) data, false, "Import");
                    controller.sendDataUpdateEvent("Import", UpdateEventConstants.REASON_ZW_LIST_CHANGED, data, null,
                            null);
                    break;
                case HEATTIMES:
                case RESULTS:
                    core.setWettkampf((AWettkampf) data, false, "Import");
                    controller.sendDataUpdateEvent("Import",
                            UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_PENALTY, data,
                            null, null);
                    break;
                case REFEREES:
                    controller.sendDataUpdateEvent("Import", UpdateEventConstants.REASON_REFEREES_CHANGED, data, null,
                            null);
                    break;
                case TEAMMEMBERS:
                    controller.sendDataUpdateEvent("Import", UpdateEventConstants.REASON_SWIMMER_CHANGED, data, null,
                            null);
                    break;
                case ZW_RESULTS:
                    controller.sendDataUpdateEvent("Import", UpdateEventConstants.REASON_POINTS_CHANGED, data, null,
                            null);
                    break;
                default:
                    controller.sendDataUpdateEvent("Import", UpdateEventConstants.REASON_EVERYTHING_CHANGED, data, null,
                            null);
                    break;
                }
            }
        }
        return (data != null);
    }

    private class JTypeChooser extends WizardOptionPage {

        private final ImportExportMode mode;

        public JTypeChooser(ImportExportMode mode) {
            super(getWizard(), I18n.get("ChooseAType"), I18n.get("Import.ChooseAType.Information"),
                    ExportManager.NAMES);
            this.mode = mode;
            for (ImportExportTypes t : ImportExportTypes.values()) {
                boolean enabled = (mode == ImportExportMode.Normal) || (t == ImportExportTypes.TEAMMEMBERS);
                setEnabled(t.getValue(), enabled && ImportManager.isEnabled(core.getWettkampf(), t));
            }
        }

        @Override
        public void update() {
            for (ImportExportTypes t : ImportExportTypes.values()) {
                boolean enabled = (mode == ImportExportMode.Normal) || (t == ImportExportTypes.TEAMMEMBERS);
                setEnabled(t.getValue(), enabled && ImportManager.isEnabled(core.getWettkampf(), t));
            }
            super.update();
        }
    }

    private class JFormatChooser extends WizardOptionPage implements PageSwitchListener {

        /**
         * @param wc
         * @param title
         * @param options
         * @param enabled
         */
        public JFormatChooser() {
            super(getWizard(), I18n.get("ChooseAFormat"), I18n.get("Import.ChooseAFormat.Information"),
                    ImportManager.getSupportedFormats());
            pageSwitch(true);
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (getWizard().isCurrentPage(this)) {
                String[] names = ImportManager.getSupportedFormats();
                for (int x = 0; x < names.length; x++) {
                    setEnabled(x, ImportManager.isSupported(names[x], ImportExportTypes.getByValue(type.getSelectedIndex())));
                }
            }
            getWizard().notifyUpdate();
        }
    }

    private class JFilePage extends AWizardPage implements UpdateListener, PageSwitchListener {

        JTextField filename = new JTextField();

        private JPanel panel = null;

        /**
         * @param arg0
         */
        public JFilePage() {
            super(I18n.get("ChooseAFile"), I18n.get("Import.ChooseAFile.Information"));
            FileAutoCompleter.addFileAutoCompleter(filename);

            panel = new JPanel(new FormLayout("4dlu,fill:default:grow,4dlu,fill:default,4dlu",
                    "4dlu,fill:default:grow,fill:default,fill:default:grow,4dlu")) {
                private static final long serialVersionUID = 0L;

                @Override
                public void requestFocus() {
                    super.requestFocus();
                    filename.requestFocus();
                }
            };

            panel.add(filename, CC.xy(2, 3));
            filename.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    update();
                }
            });
            filename.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        getWizard().pageSwitch(true);
                    } else {
                        update();
                    }
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        getWizard().pageSwitch(true);
                    } else {
                        update();
                    }
                }
            });
            filename.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent arg0) {
                    update();
                }

                @Override
                public void mouseExited(MouseEvent arg0) {
                    update();
                }

            });
            JButton button = new JButton("...");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    browseFile();
                }
            });
            panel.add(button, CC.xy(4, 3));
        }

        public String getText() {
            return filename.getText();
        }

        @Override
        public void pageSwitch(boolean forward) {
            updateSuffixes();
            update();
        }

        private void updateSuffixes() {
            String fname = filename.getText();
            String[] names = fname.split(";");
            for (int y = 0; y < names.length; y++) {
                String name = names[y].trim();
                if (name.length() > 0) {
                    boolean found = false;

                    String[] suffixes = ImportManager.getImporter(format.getSelectedItemname()).getSuffixes();
                    for (String suffixe : suffixes) {
                        if (name.toLowerCase().endsWith(suffixe)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        int slash = name.indexOf(File.separator);
                        int dot = name.indexOf('.');
                        if (slash < dot) {
                            name = name.substring(0, dot) + suffixes[0];
                        }
                    }
                }
                names[y] = name;
            }
            filename.setText(StringTools.concatenateFilenames(names));
        }

        @Override
        public void update() {
            if (getWizard().isCurrentPage(this)) {
                if (filename.getText().length() == 0) {
                    getWizard().setNextButtonEnabled(false);
                    getWizard().setFinishButtonEnabled(false);
                    return;
                }

                // Switch Next-Button
                boolean enabled = checkFiles();
                getWizard().setNextButtonEnabled(enabled);
                if (!enabled) {
                    getWizard().setFinishButtonEnabled(false);
                }
            }
        }

        private boolean checkFiles() {
            String name = filename.getText().trim();
            if (name.length() == 0) {
                return false;
            }
            String[] names = name.split(";");
            if (names.length == 0) {
                return false;
            }
            if (!ImportManager.isMultifileImportAllowed(ImportExportTypes.getByValue(type.getSelectedIndex()))) {
                if (names.length > 1) {
                    return false;
                }
            }
            for (String name1 : names) {
                boolean e = checkFile(name1.trim());
                if (!e) {
                    return false;
                }
            }
            return true;
        }

        private boolean checkFile(String name) {
            boolean enabled = false;

            String[] suffixes = ImportManager.getImporter(format.getSelectedItemname()).getSuffixes();
            for (String suffixe : suffixes) {
                if (name.toLowerCase().endsWith(suffixe.toLowerCase())) {
                    enabled = true;
                }
            }

            if (enabled) {
                File destfile = new File(name);
                if (destfile.exists() && destfile.isDirectory()) {
                    enabled = false;
                } else {
                    int i = name.lastIndexOf(File.separator);
                    if (i < 0) {
                        i = name.lastIndexOf("/");
                    }
                    if (i >= 0) {
                        String path = name.substring(0, i);
                        File dir = new File(path);
                        if ((!dir.exists()) || (!dir.isDirectory())) {
                            enabled = false;
                        }
                    }
                }
            }
            return enabled;
        }

        @Override
        public JComponent getPage() {
            return panel;
        }
    }

    private class JProgressPage extends AWizardPage implements PageSwitchListener, UpdateListener {

        private JPanel panel;
        private JScrollPane scroller;
        JTextArea text;

        public JProgressPage() {
            super(I18n.get("Progress"), I18n.get("Import.Progress.Information"));

            FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default:grow,4dlu");
            panel = new JPanel(layout);
            text = new JTextArea();
            text.setEditable(false);
            text.setAutoscrolls(true);
            text.setLineWrap(true);
            text.setFont(panel.getFont());
            text.setTabSize(2);
            scroller = new JScrollPane(text);
            scroller.setBorder(new ShadowBorder());

            panel.add(scroller, CC.xy(2, 2));
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (!getWizard().isCurrentPage(this)) {
                return;
            }
            getWizard().setFinishButtonEnabled(false);
            if (!forward) {
                return;
            }
            update(true);
        }

        private void update(boolean message) {
            if (getWizard().isCurrentPage(this)) {
                getWizard().setNextButtonEnabled(false);
            }

            text.setText("");

            Runnable r = new ImportRunner();
            if (message) {
                new Thread(r, "ProgressPageUpdate").start();
            } else {
                r.run();
            }
        }

        @Override
        public JComponent getPage() {
            return panel;
        }

        @Override
        public void update() {
            // update(false);
        }

        void insertText(String t) {
            EDTUtils.executeOnEDT(new TextRunnable(t));
        }

        private final class ImportRunner implements Runnable {
            @Override
            public void run() {
                boolean display = (getWizard().isCurrentPage(JProgressPage.this));

                AWettkampf<?> wk = core.getWettkampf();

                String fname = file.getText().trim();
                if (fname.length() != 0) {
                    String[] names = fname.split(";");
                    processFiles(wk, names, display);
                }
                if (getWizard().isCurrentPage(JProgressPage.this)) {
                    getWizard().setNextButtonEnabled(results != null);
                }
            }

            void processFiles(AWettkampf<?> wk, String[] names, boolean display) {
                try {
                    results = ImportManager.importData(ImportExportTypes.getByValue(type.getSelectedIndex()), names, format.getSelectedItemname(), wk,
                            new Feedback() {
                                @Override
                                public void showFeedback(String t) {
                                    insertText(t);
                                }
                            });
                } catch (TableEntryException tee) {
                    if (display) {
                        DialogUtils.warn(JImportWizard.this,
                                I18n.get("EntryError",
                                        StringTools.getCellName(tee.getSheet(), tee.getRow(), tee.getColumn())),
                                tee.getData(), null);
                    }
                    results = null;
                } catch (TableFormatException tfe) {
                    if (display) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(I18n.get("TheFollowingRequiredFieldsWereNotFound"));
                        sb.append(ImportManager.indizesToNames(tfe.getData(), ""));
                        DialogUtils.warn(JImportWizard.this, I18n.get("Error"), sb.toString(),
                                I18n.get("CheckColumnheaders"));
                    }
                    results = null;
                } catch (TableException e) {
                    e.printStackTrace();
                    DialogUtils.warn(JImportWizard.this, I18n.get("Error"), e.getMessage(), I18n.get("CheckFile"));
                } catch (NotSupportedException e) {
                    e.printStackTrace();
                    DialogUtils.warn(JImportWizard.this, I18n.get("Error"), e.toString(), null);
                } catch (NotEnabledException e) {
                    e.printStackTrace();
                    DialogUtils.warn(JImportWizard.this, I18n.get("Error"), e.toString(), null);
                } catch (IOException e) {
                    e.printStackTrace();
                    DialogUtils.warn(JImportWizard.this, I18n.get("Error"), I18n.get("ReadErrorOccured"),
                            I18n.get("CheckFile"));
                } catch (OldExcelFormatException old) {
                    DialogUtils.warn(JImportWizard.this, I18n.get("Error"), I18n.get("ReadErrorOccured"),
                            I18n.get("OldFileExcelFormat"));
                } catch (LeftoverDataException lde) {
                    DialogUtils.warn(JImportWizard.this, I18n.get("Error"), I18n.get("ReadErrorOccured"),
                            I18n.get("ErrorInExcelFormat"));
                }
            }
        }

        private class TextRunnable implements Runnable {

            private final String t;

            public TextRunnable(String t) {
                this.t = t;
            }

            @Override
            public void run() {
                try {
                    if (text.getDocument().getLength() > 0) {
                        text.getDocument().insertString(text.getDocument().getLength(), "\n", null);
                    }
                    text.getDocument().insertString(text.getDocument().getLength(), t, null);
                    text.setCaretPosition(text.getDocument().getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class JImportPage extends AWizardPage implements PageSwitchListener, UpdateListener {

        private JPanel panel;
        private JScrollPane scroller;
        private JList<ASchwimmer> data;
        private JLabel amount;

        public JImportPage() {
            super(I18n.get("ImportData"), I18n.get("ImportData.Information"));

            FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                    "4dlu,fill:default,4dlu,fill:default:grow,4dlu");
            panel = new JPanel(layout);
            amount = new JLabel();
            data = new JList<ASchwimmer>();
            data.setCellRenderer(new SchwimmerListCellRenderer());
            DataTipManager.get().register(data);
            scroller = new JScrollPane(data);
            scroller.setBorder(new ShadowBorder());

            panel.add(amount, CC.xy(4, 2));
            panel.add(new JLabel(I18n.get("Amount")), CC.xy(2, 2));
            panel.add(scroller, CC.xyw(2, 4, 3, "fill,fill"));
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (!getWizard().isCurrentPage(this)) {
                return;
            }
            update(true);
        }

        private void update(boolean message) {
            data.setListData(new ASchwimmer[0]);
            amount.setText("");

            AWettkampf<ASchwimmer> wk = core.getWettkampf();

            getWizard().setFinishButtonEnabled((results != null) && message);
            if (results != null) {
                switch (ImportExportTypes.getByValue(type.getSelectedIndex())) {
                case REGISTRATION: {
                    @SuppressWarnings("unchecked")
                    LinkedList<ASchwimmer> r = (LinkedList<ASchwimmer>) results;
                    ListIterator<ASchwimmer> li = r.listIterator();
                    while (li.hasNext()) {
                        boolean added = wk.addSchwimmer(li.next());
                        assert added;
                    }
                    li = r.listIterator();
                    while (li.hasNext()) {
                        wk.removeSchwimmer(li.next());
                    }
                    data.setListData(r.toArray(new ASchwimmer[r.size()]));
                    amount.setText("" + r.size());
                    break;
                }
                case TEAMMEMBERS: {
                    @SuppressWarnings("unchecked")
                    Hashtable<String, String[]> names = (Hashtable<String, String[]>) results;
                    Enumeration<String> sns = names.keys();
                    LinkedList<ASchwimmer> r = new LinkedList<ASchwimmer>();
                    HashSet<Integer> foundSN = new HashSet<Integer>();
                    while (sns.hasMoreElements()) {
                        String sntext = sns.nextElement();
                        int sn = Integer.parseInt(sntext.substring(0, sntext.length() - 1));
                        if (!foundSN.contains(sn)) {
                            r.addLast(SearchUtils.getSchwimmer(wk, sn));
                            foundSN.add(sn);
                        }
                    }
                    data.setListData(r.toArray(new ASchwimmer[r.size()]));
                    amount.setText("" + r.size());
                    break;
                }
                case ZW_RESULTS: {
                    @SuppressWarnings("unchecked")
                    Hashtable<ZWStartnummer, Double> names = (Hashtable<ZWStartnummer, Double>) results;
                    Enumeration<ZWStartnummer> sns = names.keys();
                    LinkedList<ASchwimmer> r = new LinkedList<ASchwimmer>();
                    while (sns.hasMoreElements()) {
                        r.addLast(SearchUtils.getSchwimmer(wk, sns.nextElement().getStartnummer()));
                    }
                    data.setListData(r.toArray(new ASchwimmer[r.size()]));
                    amount.setText("" + r.size());
                    break;
                }
                default:
                }
            }
        }

        @Override
        public JComponent getPage() {
            return panel;
        }

        @Override
        public void update() {
            update(false);
        }
    }

    @Override
    public void cancel() {
        setVisible(false);
    }
}