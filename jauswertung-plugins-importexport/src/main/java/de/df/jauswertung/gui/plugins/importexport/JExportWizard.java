/*
 * Created on 11.07.2004
 */
package de.df.jauswertung.gui.plugins.importexport;

import static de.df.jauswertung.io.ExportManager.NAMES;

import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.bugreport.BugreportPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.WizardUIElementsProvider;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.io.ImportExportTypes;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.autocomplete.FileAutoCompleter;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.InfiniteProgressUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.wizard.AWizardPage;
import de.df.jutils.gui.wizard.CancelListener;
import de.df.jutils.gui.wizard.FinishListener;
import de.df.jutils.gui.wizard.JWizard;
import de.df.jutils.gui.wizard.JWizardFrame;
import de.df.jutils.gui.wizard.PageSwitchListener;
import de.df.jutils.gui.wizard.UpdateListener;
import de.df.jutils.gui.wizard.WizardInfoPage;
import de.df.jutils.gui.wizard.WizardOptionPage;
import de.df.jutils.util.Feedback;

/**
 * @author Dennis Fabri @date 11.07.2004
 */
class JExportWizard extends JWizardFrame implements FinishListener, CancelListener {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3545515093153625141L;

    static final String[] COMPETITIONPARTS = new String[] { I18n.get("PersonalCompetition"),
            I18n.get("TeamCompetition") };
    static final String[] FORMATS = ExportManager.getSupportedFormats();

    CorePlugin core = null;
    BugreportPlugin bugreport = null;

    JTypeChooser type = null;
    JFormatChooser format = null;
    JFilePanel file = null;
    JResultPanel result = null;
    String organisation = null;

    public JExportWizard(JFrame parent, CorePlugin c, BugreportPlugin br, ImportExportMode mode, String organisation) {
        super(parent, I18n.get("Export"), WizardUIElementsProvider.getInstance(), false);
        if (c == null) {
            throw new NullPointerException("coreplugin must not be null");
        }
        if (parent == null) {
            throw new NullPointerException("Parent window must not be null");
        }
        core = c;
        bugreport = br;
        this.organisation = organisation;

        JWizard wizard = getWizard();

        type = new JTypeChooser(mode);
        wizard.addPage(type);

        format = new JFormatChooser();
        wizard.addPage(format);

        file = new JFilePanel();
        wizard.addPage(file);

        result = new JResultPanel();
        wizard.addPage(result);

        wizard.addListener(this);

        setIconImage(parent.getIconImage());
        pack();
        UIStateUtils.uistatemanage(parent, this, "JExportWizard");
        pack();

        setResizable(false);
    }

    /**
     * 
     */
    void browseFile() {
        SimpleFileFilter filter = new SimpleFileFilter(ExportManager.getName(FORMATS[format.getSelectedIndex()]),
                ExportManager.getSuffixes(FORMATS[format.getSelectedIndex()]));
        String name = FileChooserUtils.saveFile(this, filter);
        if (name != null) {
            file.filename.setText(name);
        }
        file.update();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            getWizard().setFinishButtonEnabled(false);
        }
        super.setVisible(visible);
    }

    @Override
    public synchronized void finish() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        EDTUtils.setEnabled(this, false);

        SwingWorker<ResultInfo, Object> sw = new SwingWorker<ResultInfo, Object>() {

            @Override
            protected ResultInfo doInBackground() throws Exception {
                try {
                    return finishExport();
                } catch (IOException io) {
                    return new ResultInfo(ResultType.Fail, io);
                } catch (Exception e) {
                    bugreport.handle(e);
                    return new ResultInfo(ResultType.Fail, e);
                }
            }

            @Override
            protected void done() {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                ResultInfo ri = null;
                try {
                    ri = get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // Nothing to do
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    // Nothing to do
                }
                if (ri == null) {
                    ri = new ResultInfo(ResultType.Fail);
                }
                switch (ri.getResult()) {
                case Success:
                    JExportWizard.this.setEnabled(true);
                    JExportWizard.this.setVisible(false);
                    break;
                case Fail:
                    JExportWizard.this.setEnabled(true);
                    Exception ex = ri.getException();
                    if (ex == null) {
                        DialogUtils.error(JExportWizard.this, I18n.get("Error"), I18n.get("ExportFailed"),
                                I18n.get("ExportFailed.Note"));
                    } else if (ex instanceof IOException) {
                        ex.printStackTrace();
                        DialogUtils.error(JExportWizard.this, I18n.get("Error"), I18n.get("ErrorOnFileWrite"),
                                I18n.get("ErrorOnFileWrite.Note"));
                    } else {
                        ex.printStackTrace();
                        DialogUtils.error(JExportWizard.this, I18n.get("Error"), I18n.get("ExportFailed"),
                                I18n.get("ExportFailedWith.Note", I18n.toString(ex)));
                    }
                }
                setText("");
            }
        };
        sw.execute();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    ResultInfo finishExport() throws IOException {
        AWettkampf wk = core.getWettkampf();
        if (organisation != null) {
            wk = Utils.copy(wk);
            LinkedList<ASchwimmer> swimmers = wk.getSchwimmer();
            for (ASchwimmer s : swimmers) {
                if (!s.getGliederungMitQGliederung().equals(organisation)) {
                    wk.removeSchwimmer(s);
                }
            }
        }
        ImportExportTypes ioType = ImportExportTypes.getByValue(type.getSelectedIndex());
        boolean success = ExportManager.export(ioType, file.getText(), FORMATS[format.getSelectedIndex()], wk,
                new ExportFeedback());
        return (success ? new ResultInfo(ResultType.Success) : new ResultInfo(ResultType.Fail));
    }

    class ExportFeedback implements Feedback {

        @Override
        public void showFeedback(String text) {
            InfiniteProgressUtils.setTextAsync(JExportWizard.this, text);
        }
    }

    class JTypeChooser extends WizardOptionPage implements PageSwitchListener {

        private final ImportExportMode mode;

        /**
         * @param wc
         * @param title
         * @param options @param enabled
         */
        public JTypeChooser(ImportExportMode mode) {
            super(getWizard(), I18n.get("ChooseAType"), I18n.get("Export.ChooseAType.Information"), NAMES);
            this.mode = mode;
        }

        @Override
        @SuppressWarnings({ "unchecked" })
        public void pageSwitch(boolean forward) {
            if (getWizard().isCurrentPage(this)) {
                @SuppressWarnings("rawtypes")
                AWettkampf wk = core.getWettkampf();
                for (ImportExportTypes type : ImportExportTypes.values()) {
                    boolean enabled = (mode == ImportExportMode.Normal) || (type == ImportExportTypes.TEAMMEMBERS);
                    setEnabled(type.getValue(), enabled && ExportManager.isEnabled(wk, type));
                }
                update();
            }
        }
    }

    class JFormatChooser extends WizardOptionPage implements PageSwitchListener {

        /**
         * @param wc
         * @param title
         * @param options @param enabled
         */
        public JFormatChooser() {
            super(getWizard(), I18n.get("ChooseAFormat"), I18n.get("Export.ChooseAFormat.Information"), FORMATS);
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (getWizard().isCurrentPage(this)) {
                ImportExportTypes t = ImportExportTypes.getByValue(type.getSelectedIndex());
                for (int x = 0; x < FORMATS.length; x++) {
                    setEnabled(x, ExportManager.isSupported(FORMATS[x], t));
                }
                update();
            }
        }
    }

    class JFilePanel extends AWizardPage implements UpdateListener, PageSwitchListener {

        JTextField filename = new JTextField();
        private final JPanel page;

        /**
         * @param arg0
         */
        public JFilePanel() {
            super(I18n.get("ChooseAFile"), I18n.get("Export.ChooseAFile.Information"));
            FileAutoCompleter.addFileAutoCompleter(filename);

            page = new JPanel(new FormLayout("4dlu,fill:default:grow,4dlu,fill:default,4dlu",
                    "4dlu,fill:default:grow,fill:default,fill:default:grow,4dlu")) {
                private static final long serialVersionUID = 0L;

                @Override
                public void requestFocus() {
                    super.requestFocus();
                    filename.requestFocus();
                }
            };

            page.add(filename, CC.xy(2, 3));
            filename.addActionListener(arg0 -> {
                update();
            });
            filename.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent arg0) {
                    update();
                }

                @Override
                public void keyTyped(KeyEvent arg0) {
                    update();
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
            button.addActionListener(arg0 -> {
                browseFile();
            });
            page.add(button, CC.xy(4, 3));
        }

        public String getText() {
            return filename.getText();
        }

        @Override
        public void update() {
            if (getWizard().isCurrentPage(this)) {
                if (filename.getText().length() == 0) {
                    getWizard().setNextButtonEnabled(false);
                    return;
                }

                // Switch Next-Button
                boolean enabled = false;

                String name = filename.getText();

                String[] suffixes = ExportManager.getSuffixes(FORMATS[format.getSelectedIndex()]);
                for (String suffixe : suffixes) {
                    if (name.toLowerCase().endsWith("." + suffixe)) {
                        enabled = true;
                    }
                }

                if (enabled) {
                    name = filename.getText();
                    File destfile = new File(name);
                    if (destfile.exists() && destfile.isDirectory()) {
                        enabled = false;
                    } else {
                        int i = name.lastIndexOf(File.separator);
                        if (i >= 0) {
                            String path = name.substring(0, i);
                            File dir = new File(path);
                            if ((!dir.exists()) || (!dir.isDirectory())) {
                                enabled = false;
                            }
                        }
                    }
                }
                getWizard().setNextButtonEnabled(enabled);
            }
        }

        @Override
        public void pageSwitch(boolean forward) {
            String name = filename.getText();
            if (name.length() > 0) {
                boolean found = false;

                String[] suffixes = ExportManager.getSuffixes(FORMATS[format.getSelectedIndex()]);
                for (String suffixe : suffixes) {
                    if (name.toLowerCase().endsWith("." + suffixe)) {
                        found = true;
                    }
                }
                if (!found) {
                    int slash = name.indexOf(File.separator);
                    int dot = name.indexOf('.');
                    if (slash < dot) {
                        name = name.substring(0, dot) + "." + suffixes[0];
                    }
                    filename.setText(name);
                }
            }
            update();
        }

        @Override
        public JComponent getPage() {
            return page;
        }
    }

    class JResultPanel extends WizardInfoPage implements UpdateListener, PageSwitchListener {

        /**
         * @param arg0
         */
        public JResultPanel() {
            super(I18n.get("ExportInformation"), I18n.get("ExportInformation.Information"),
                    new String[] { I18n.get("ExportType"), I18n.get("FileFormat"), I18n.get("Filename") }, null);
        }

        @Override
        public void update() {
            if (getWizard().isCurrentPage(this)) {
                setValue(0, NAMES[type.getSelectedIndex()]);
                setValue(1, FORMATS[format.getSelectedIndex()]);
                setValue(2, file.getText());
                getWizard().setNextButtonEnabled(false);
                getWizard().setFinishButtonEnabled(true);
            }
        }

        @Override
        public void pageSwitch(boolean forward) {
            update();
        }
    }

    @Override
    public void cancel() {
        setVisible(false);
    }
}