/*
 * Created on 11.07.2004
 */
package de.df.jauswertung.gui.plugins.zulassung;

import java.awt.Cursor;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.WizardUIElementsProvider;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.layout.FormLayoutUtils;
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
import de.df.jutils.gui.wizard.WizardOptionPage;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Fabri
 * @date 11.07.2004
 */
class JZulassungWizard extends JWizardFrame implements FinishListener, CancelListener {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3545515093153625141L;

    static final String[] COMPETITIONPARTS = new String[] { I18n.get("PersonalCompetition"),
            I18n.get("TeamCompetition") };
    static final String[] FORMATS = ExportManager.getSupportedFormats();

    CorePlugin core = null;
    IPluginManager controller = null;

    JKeepQualifiedPanel keepqualified = null;
    JPreferProtocolPanel protocol = null;
    JAmountPanel amount = null;
    JMeldepunktePanel meldung = null;
    JDirektPanel direct = null;
    JHopersPanel hopers = null;
    JResultPanel result = null;
    Zulassung<ASchwimmer> zulassung;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public JZulassungWizard(JFrame parent, CorePlugin c, IPluginManager con) {
        super(parent, I18n.get("Zulassung"), WizardUIElementsProvider.getInstance(), false);
        if (c == null) {
            throw new NullPointerException("coreplugin must not be null");
        }
        if (con == null) {
            throw new NullPointerException("Controller must not be null");
        }
        if (parent == null) {
            throw new NullPointerException("Parent window must not be null");
        }
        core = c;
        controller = con;

        zulassung = new Zulassung(core.getWettkampf());

        JWizard wizard = getWizard();

        keepqualified = new JKeepQualifiedPanel();
        wizard.addPage(keepqualified);

        meldung = new JMeldepunktePanel();
        wizard.addPage(meldung);

        protocol = new JPreferProtocolPanel();
        wizard.addPage(protocol);

        amount = new JAmountPanel();
        wizard.addPage(amount);

        direct = new JDirektPanel();
        wizard.addPage(direct);

        hopers = new JHopersPanel();
        wizard.addPage(hopers);

        result = new JResultPanel();
        wizard.addPage(result);

        wizard.addListener(this);

        setIconImage(parent.getIconImage());
        pack();
        UIStateUtils.uistatemanage(parent, this, "JZulassungWizard");
        pack();

        setResizable(false);
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

        SwingWorker<Integer, Object> sw = new SwingWorker<Integer, Object>() {

            @Override
            protected Integer doInBackground() throws Exception {
                try {
                    return finishZulassung();
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            protected void done() {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                int success = 0;
                try {
                    success = get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // Nothing to do
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    // Nothing to do
                }
                if (success == 1) {
                    JZulassungWizard.this.setEnabled(true);
                    JZulassungWizard.this.setVisible(false);
                } else {
                    JZulassungWizard.this.setEnabled(true);
                    DialogUtils.error(JZulassungWizard.this, I18n.get("Error"), I18n.get("ZulassungFailed"),
                            I18n.get("ZulassungFailed.Note"));
                }
                setText("");
            }
        };
        sw.execute();
    }

    int finishZulassung() {
        zulassung.calculate(amount.getInts(), direct.getInts(), hopers.getInts(), keepqualified.getKeep(),
                protocol.getPreferProtocol(),
                meldung.getSelectedIndex());
        zulassung.execute();
        controller.sendDataUpdateEvent("Import", UpdateEventConstants.REASON_SWIMMER_CHANGED, null, null, null);
        return 1;
    }

    class ExportFeedback implements Feedback {

        @Override
        public void showFeedback(String text) {
            InfiniteProgressUtils.setTextAsync(JZulassungWizard.this, text);
        }
    }

    class JMuliIntPage extends AWizardPage implements PageSwitchListener {

        private JPanel panel = new JPanel();

        private JRadioButton single = null;
        private JRadioButton multi = null;

        private JIntegerField simpleInput = null;
        private JIntegerField[][] input = null;

        public int[][] getInts() {
            int[][] limits = new int[input.length][2];

            if (single.isSelected()) {
                int simple = simpleInput.getInt();
                for (int x = 0; x < limits.length; x++) {
                    for (int y = 0; y < 2; y++) {
                        limits[x][y] = simple;
                    }
                }
            } else {
                for (int x = 0; x < limits.length; x++) {
                    for (int y = 0; y < 2; y++) {
                        limits[x][y] = input[x][y].getInt();
                    }
                }
            }
            return limits;
        }

        /**
         * @param wc
         * @param title
         * @param options
         * @param enabled
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public JMuliIntPage(String title, String info, int[][] limits, int[][] amounts, String subtitle1,
                String subtitle2) {
            super(title, info);
            AWettkampf wk = core.getWettkampf();

            Regelwerk aks = wk.getRegelwerk();

            input = new JIntegerField[aks.size()][2];

            single = new JRadioButton(I18n.get("SetZulassungLimits.SingleInput"));
            multi = new JRadioButton(I18n.get("SetZulassungLimits.DetailedInput"));

            single.addChangeListener(e -> {
                modeChanged();
            });
            multi.addChangeListener(e -> {
                modeChanged();
            });

            simpleInput = new JIntegerField(false, true);
            simpleInput.setAutoSelectAll(true);
            simpleInput.setHorizontalAlignment(SwingConstants.RIGHT);

            ButtonGroup group = new ButtonGroup();
            group.add(single);
            group.add(multi);

            int value = limits[0][0];
            boolean same = true;
            int count = 0;
            for (int x = 0; x < aks.size(); x++) {
                for (int y = 0; y < 2; y++) {
                    input[x][y] = new JIntegerField(false, true);
                    input[x][y].setAutoSelectAll(true);
                    input[x][y].setInt(limits[x][y]);
                    input[x][y].setHorizontalAlignment(SwingConstants.RIGHT);
                    if (SearchUtils.hasSchwimmer(wk, aks.getAk(x))) {
                        count++;
                    }
                    if (value != limits[x][y]) {
                        same = false;
                    }
                }
            }

            if (same) {
                simpleInput.setInt(value);
                single.setSelected(true);
            } else {
                multi.setSelected(true);
            }

            panel.setLayout(new FormLayout(
                    "4dlu,12dlu,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu:grow,center:default,4dlu,center:default,0dlu:grow,4dlu",
                    FormLayoutUtils.createLayoutString(count + 1 + 3)));

            panel.add(single, CC.xyw(2, 2, 12));

            panel.add(new JLabel(I18n.get("Zulassung")), CC.xy(4, 4));
            panel.add(simpleInput, CC.xy(6, 4));

            panel.add(multi, CC.xyw(2, 6, 12));

            int pos = 0;
            for (int x = 0; x < input.length; x++) {
                if (SearchUtils.hasSchwimmer(wk, aks.getAk(x))) {
                    panel.add(new JLabel(aks.getAk(x).getName()), CC.xy(4, pos * 2 + 6 + 6));
                    for (int y = 0; y < 2; y++) {
                        panel.add(input[x][y], CC.xy(6 + 2 * y, pos * 2 + 6 + 6));
                        panel.add(new JLabel("" + amounts[x][y]), CC.xy(10 + 2 * y, pos * 2 + 6 + 6));
                    }
                    pos++;
                }
            }
            panel.add(new JLabel(subtitle1), CC.xyw(6, 2 + 6, 3, "center,center"));
            panel.add(new JLabel(I18n.geschlechtToString(aks, false)), CC.xy(6, 4 + 6, "center,center"));
            panel.add(new JLabel(I18n.geschlechtToString(aks, true)), CC.xy(8, 4 + 6, "center,center"));
            panel.add(new JLabel(subtitle2), CC.xyw(10, 2 + 6, 3, "center,center"));
            panel.add(new JLabel(I18n.geschlechtToString(aks, false)), CC.xy(10, 4 + 6, "center,center"));
            panel.add(new JLabel(I18n.geschlechtToString(aks, true)), CC.xy(12, 4 + 6, "center,center"));
        }

        private void modeChanged() {
            boolean simple = single.isSelected();

            simpleInput.setEnabled(simple);

            for (int x = 0; x < input.length; x++) {
                for (int y = 0; y < 2; y++) {
                    input[x][y].setEnabled(!simple);
                }
            }
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (getWizard().isCurrentPage(this)) {
                // Nothing to do
            }
        }

        @Override
        public JComponent getPage() {
            return panel;
        }
    }

    class JAmountPanel extends JMuliIntPage {

        public JAmountPanel() {
            super(I18n.get("SetZulassungLimits"), I18n.get("SetZulassungLimits.Information"),
                    core.getWettkampf().getZulassungslimits(),
                    CompetitionUtils.getSchwimmerAmounts(core.getWettkampf()), I18n.get("Zulassung"),
                    I18n.get("Registration"));
        }
    }

    class JDirektPanel extends JMuliIntPage {

        public JDirektPanel() {
            super(I18n.get("SetDirektqualifizierte"), I18n.get("SetDirektqualifizierte.Information"),
                    core.getWettkampf().getDirektqualifizierte(),
                    CompetitionUtils.getQOrganizationAmounts(core.getWettkampf()),
                    I18n.get("Zulassung.QualifiedDirectly"), I18n.get("Qualifikationsebene"));
        }
    }

    class JHopersPanel extends JMuliIntPage {

        public JHopersPanel() {
            super(I18n.get("SetHopers"), I18n.get("SetHopers.Information"), core.getWettkampf().getNachruecker(),
                    CompetitionUtils.getSchwimmerAmounts(core.getWettkampf()), I18n.get("Zulassung.Nachruecker"),
                    I18n.get("Registration"));
        }
    }

    private static class JKeepQualifiedPanel extends AWizardPage implements PageSwitchListener {

        private JCheckBox keep = new JCheckBox();
        private JPanel panel = new JPanel();

        /**
         * @param wc
         * @param title
         * @param options
         * @param enabled
         */
        public JKeepQualifiedPanel() {
            super(I18n.get("Zulassung.KeepQualified"), I18n.get("Zulassung.KeepQualified.Information"));

            panel.setLayout(new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                    FormLayoutUtils.createLayoutString(1)));

            panel.add(keep, CC.xy(2, 2));
            panel.add(new JLabel(I18n.get("Zulassung.KeepQualified")), CC.xy(4, 2));
        }

        public boolean getKeep() {
            return keep.isSelected();
        }

        @Override
        public void pageSwitch(boolean forward) {
            // Nothing to do
        }

        @Override
        public JComponent getPage() {
            return panel;
        }
    }

    class JPreferProtocolPanel extends AWizardPage implements PageSwitchListener {

        private JCheckBox preferProtocol = new JCheckBox();
        private JPanel panel = new JPanel();

        /**
         * @param wc
         * @param title
         * @param options
         * @param enabled
         */
        public JPreferProtocolPanel() {
            super(I18n.get("Zulassung.PreferProtocol"), I18n.get("Zulassung.PreferProtocol.Information"));

            @SuppressWarnings("rawtypes")
            AWettkampf wk = core.getWettkampf();
            preferProtocol.setSelected(wk.getBooleanProperty(PropertyConstants.PREFER_PROTOCOL));

            panel.setLayout(new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                    FormLayoutUtils.createLayoutString(1)));

            panel.add(preferProtocol, CC.xy(2, 2));
            panel.add(new JLabel(I18n.get("Zulassung.PreferProtocol")), CC.xy(4, 2));
        }

        public boolean getPreferProtocol() {
            return preferProtocol.isSelected();
        }

        @Override
        public void pageSwitch(boolean forward) {
            // Nothing to do
        }

        @Override
        public JComponent getPage() {
            return panel;
        }
    }

    class JResultPanel extends AWizardPage implements UpdateListener, PageSwitchListener {

        private JPanel panel = new JPanel();
        private JLabel[][] zugelassen;
        private JLabel[][] nichtzugelassen;
        private JLabel[][] nachruecker;

        /**
         * @param arg0
         */
        @SuppressWarnings({ "unchecked" })
        public JResultPanel() {
            super(I18n.get("ZulassungInformation"), I18n.get("ZulassungInformation.Information"));

            @SuppressWarnings("rawtypes")
            AWettkampf wk = core.getWettkampf();
            Regelwerk aks = wk.getRegelwerk();

            zugelassen = new JLabel[aks.size()][2];
            nichtzugelassen = new JLabel[aks.size()][2];
            nachruecker = new JLabel[aks.size()][2];

            int count = 0;
            for (int x = 0; x < aks.size(); x++) {
                for (int y = 0; y < 2; y++) {
                    zugelassen[x][y] = new JLabel();
                    nichtzugelassen[x][y] = new JLabel();
                    nachruecker[x][y] = new JLabel();
                }
                if (SearchUtils.hasSchwimmer(wk, aks.getAk(x))) {
                    count++;
                }
            }

            panel.setLayout(new FormLayout(
                    "4dlu,fill:default," + "12dlu,center:default,4dlu,center:default,"
                            + "12dlu,center:default,4dlu,center:default,"
                            + "12dlu,center:default,4dlu,center:default,4dlu",
                    FormLayoutUtils.createLayoutString(count + 2)));

            panel.add(new JLabel(I18n.get("Zugelassen")), CC.xyw(4, 2, 3, "center,fill"));
            panel.add(new JLabel(I18n.get("NichtZugelassen")), CC.xyw(8, 2, 3, "center,fill"));
            panel.add(new JLabel(I18n.get("DavonNachruecker")), CC.xyw(12, 2, 3, "center,fill"));

            for (int x = 0; x < 3; x++) {
                panel.add(new JLabel(I18n.geschlechtToString(aks, false)), CC.xy(4 + 4 * x, 4));
                panel.add(new JLabel(I18n.geschlechtToString(aks, true)), CC.xy(6 + 4 * x, 4));
            }

            int pos = 0;
            for (int x = 0; x < zugelassen.length; x++) {
                if (SearchUtils.hasSchwimmer(wk, aks.getAk(x))) {
                    panel.add(new JLabel(aks.getAk(x).getName()), CC.xy(2, pos * 2 + 6));
                    for (int y = 0; y < 2; y++) {
                        panel.add(zugelassen[x][y], CC.xy(4 + 2 * y, pos * 2 + 6));
                        panel.add(nichtzugelassen[x][y], CC.xy(8 + 2 * y, pos * 2 + 6));
                        panel.add(nachruecker[x][y], CC.xy(12 + 2 * y, pos * 2 + 6));
                    }
                    pos++;
                }
            }

        }

        @Override
        public void update() {
            if (getWizard().isCurrentPage(this)) {
                zulassung.calculate(amount.getInts(), direct.getInts(), hopers.getInts(), keepqualified.getKeep(),
                        false, 0);
                LinkedList<ASchwimmer>[][] qual = zulassung.getZugelassene();
                LinkedList<ASchwimmer>[][] nqual = zulassung.getNichtZugelassene();
                LinkedList<ASchwimmer>[][] disq = zulassung.getGesperrte();
                LinkedList<ASchwimmer>[][] nach = zulassung.getNachruecker();
                for (int x = 0; x < qual.length; x++) {
                    for (int y = 0; y < 2; y++) {
                        zugelassen[x][y].setText("" + qual[x][y].size());
                        nichtzugelassen[x][y]
                                .setText("" + (nqual[x][y].size() + disq[x][y].size() + nach[x][y].size()));
                        nachruecker[x][y].setText("" + (nach[x][y].size()));
                    }
                }
                getWizard().setFinishButtonEnabled(true);
            } else {
                getWizard().setFinishButtonEnabled(false);
            }
        }

        @Override
        public void pageSwitch(boolean forward) {
            update();
        }

        @Override
        public JComponent getPage() {
            return panel;
        }
    }

    @Override
    public void cancel() {
        setVisible(false);
    }

    static <T extends ASchwimmer> String[] getOptions(AWettkampf<T> wk) {
        LinkedList<T> swimmer = wk.getSchwimmer();
        int max = 1;
        for (T s : swimmer) {
            if (s.getMeldepunkteSize() > max) {
                max = s.getMeldepunkteSize();
            }
        }
        String[] text = new String[max];
        for (int x = 0; x < max; x++) {
            text[x] = I18n.get("AnouncedPointsNr", StringTools.ABC[x]);
        }
        return text;
    }

    class JMeldepunktePanel extends WizardOptionPage {

        public JMeldepunktePanel() {
            super(getWizard(), I18n.get("AnouncedPoints"), I18n.get("Zulassung.MeldepunkteInfo"),
                    getOptions(core.getWettkampf()), null,
                    core.getWettkampf().getIntegerProperty(PropertyConstants.ZULASSUNG_REGISTERED_POINTS_INDEX, 0),
                    null, SwingConstants.TOP);
        }
    }
}