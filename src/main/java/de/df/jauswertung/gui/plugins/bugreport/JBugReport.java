package de.df.jauswertung.gui.plugins.bugreport;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.misc.BugReport;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.OutputManager;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;

/**
 * @author Dennis Fabri
 * @since 15. Oktober 2001, 22:25
 */
final class JBugReport extends JDialog {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3907214861925364528L;

    BugReport                 br               = null;

    JTextArea                 daten            = null;
    JTextArea                 info             = null;
    JButton                   anonymize        = new JButton();
    JButton                   speichern        = null;

    private UpdateRunnable    updater          = new UpdateRunnable();

    JBugReport(Frame parent) {
        super(parent, parent != null);
        initComponents();
        addActions();
        update();
    }

    @SuppressWarnings("rawtypes")
    void setData(Throwable e, Thread t, Class ort, Object daten) {
        br = new BugReport(e, t, ort, daten);
        EDTUtils.executeOnEDT(updater);
    }

    void update() {
        info.setText("");
        anonymize.setEnabled((br != null) && (br.getDaten() != null));
        speichern.setEnabled(br != null);
        if (br == null) {
            daten.setText("");
        } else {
            daten.setText(br.getData());
        }
    }

    @Override
    public void setVisible(boolean vis) {
        if (!vis) {
            br = null;
            update();
        }
        super.setVisible(vis);
    }

    private void addActions() {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = 3257572818995525944L;

            @Override
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    void closeWindow() {
        setVisible(false);
        dispose();
    }

    void save() {
        try {
            String name = FileChooserUtils.saveFile(this, new SimpleFileFilter("Bug", "bug"));
            if (name != null) {
                br.setInfo(info.getText());
                br.setData(daten.getText());
                boolean keinFehler = OutputManager.speichereBugReport(name, br);
                if (!keinFehler) {
                    DialogUtils.wichtigeMeldung(null, I18n.get("SaveFailed"));
                }
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
            DialogUtils.wichtigeMeldung(null, I18n.get("SaveFailed"));
        }
    }

    private void initComponents() {
        setTitle(I18n.get("BugReport"));
        addWindowListener(new CloseWindowAdapter());

        // Info
        JTextPane text = new JTextPane();
        text.setText(I18n.get("BugReportInfo"));
        text.setEditable(false);
        // text.setEnabled(false);
        text.setSelectionColor(text.getBackground());
        text.setSelectedTextColor(text.getForeground());
        text.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information"), true));

        // Daten
        daten = new JTextArea();
        daten.setLineWrap(false);
        JScrollPane scroller = new JScrollPane(daten);
        scroller.setBorder(BorderUtils.createLabeledBorder(I18n.get("DataOfBugreport")));
        scroller.setPreferredSize(new Dimension(400, 200));

        info = new JTextArea();
        info.setLineWrap(false);
        JScrollPane scroller2 = new JScrollPane(info);
        scroller2.setBorder(BorderUtils.createLabeledBorder(I18n.get("InformationOfBugreport")));
        scroller2.setPreferredSize(new Dimension(400, 200));

        // Buttons
        anonymize.setText(I18n.get("RemoveData"));
        anonymize.addActionListener(new AnonymizeActionListener());

        speichern = new JButton(I18n.get("Save"));
        speichern.addActionListener(new SaveActionListener());

        JButton schliessen = new JButton(I18n.get("Close"));
        schliessen.addActionListener(new CloseActionListener());

        // Putting together
        FormLayout layout = new FormLayout("4dlu,0px:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu," + "fill:default:grow,4dlu,fill:default,4dlu");
        layout.setColumnGroups(new int[][] { { 3, 5, 7 } });

        setLayout(layout);
        add(text, CC.xyw(2, 2, 6));
        add(scroller2, CC.xyw(2, 4, 6));
        add(scroller, CC.xyw(2, 6, 6));
        add(anonymize, CC.xy(3, 8));
        add(speichern, CC.xy(5, 8));
        add(schliessen, CC.xy(7, 8));
        setLocation(100, 100);
        setPreferredSize(new Dimension(500, 600));
        pack();
    }

    void anonymize() {
        try {
            br.anonymize();
            anonymize.setEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
            // Nothing to do
        }
    }

    final class CloseWindowAdapter extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent evt) {
            closeWindow();
        }
    }

    final class AnonymizeActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            anonymize();
        }
    }

    final class SaveActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            save();
        }
    }

    final class CloseActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            closeWindow();
        }
    }

    final class UpdateRunnable implements Runnable {

        @Override
        public void run() {
            update();
        }
    }
}