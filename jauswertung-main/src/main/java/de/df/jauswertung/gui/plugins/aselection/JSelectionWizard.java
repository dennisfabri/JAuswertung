/*
 * Created on 11.07.2004
 */
package de.df.jauswertung.gui.plugins.aselection;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.WizardUIElementsProvider;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.vergleicher.SchwimmerMeldepunkteVergleicher;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.InfiniteProgressUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.gui.wizard.AWizardPage;
import de.df.jutils.gui.wizard.CancelListener;
import de.df.jutils.gui.wizard.FinishListener;
import de.df.jutils.gui.wizard.JWizard;
import de.df.jutils.gui.wizard.JWizardDialog;
import de.df.jutils.gui.wizard.PageSwitchListener;
import de.df.jutils.gui.wizard.WizardCheckboxPage;
import de.df.jutils.gui.wizard.WizardIntegerPage;
import de.df.jutils.gui.wizard.WizardOptionPage;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.RandomUtils;
import de.df.jutils.util.StringTools;

/**
 * @author Dennis Fabri
 * @date 11.07.2004
 */
class JSelectionWizard extends JWizardDialog implements FinishListener, CancelListener {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long       serialVersionUID = 3545515093153625141L;

    private static final int        BUILD_IN         = 5;

    static final String[]           COMPETITIONPARTS = new String[] { I18n.get("PersonalCompetition"), I18n.get("TeamCompetition") };
    static final String[]           FORMATS          = ExportManager.getSupportedFormats();

    final CorePlugin                core;
    final IPluginManager            controller;

    final JWizard                   wizard;
    final JModeChooser              mode;
    final JAmountChooser            amount;
    final JOptionsChooser           options;
    final JMeldepunkteChooser       melde;
    final JMeldepunkteABPanel       melde2;

    final String                    meldekey;

    private final String            i18nprefix;
    private final AMSelectionPlugin root;

    private Random                  random           = RandomUtils.getRandomNumberGenerator(RandomUtils.Generators.MersenneTwister);

    public JSelectionWizard(JFrame parent, AMSelectionPlugin root, CorePlugin c, IPluginManager con, String i18nprefix, String meldekey) {
        super(parent, I18n.get(i18nprefix + ".Title"), WizardUIElementsProvider.getInstance(), false);
        if (c == null) {
            throw new NullPointerException("coreplugin must not be null");
        }
        if (parent == null) {
            throw new NullPointerException("Parent window must not be null");
        }
        if (con == null) {
            throw new NullPointerException("Controller must not be null");
        }
        if (root == null) {
            throw new NullPointerException("Mainplugin must not be null");
        }
        this.i18nprefix = i18nprefix;
        this.root = root;
        core = c;
        controller = con;
        this.meldekey = meldekey;

        wizard = getWizard();

        mode = new JModeChooser();
        wizard.addPage(mode);

        options = new JOptionsChooser();
        wizard.addPage(options);

        amount = new JAmountChooser();
        wizard.addPage(amount);

        melde = new JMeldepunkteChooser();
        wizard.addPage(melde);

        melde2 = new JMeldepunkteABPanel();
        wizard.addPage(melde2);

        wizard.addListener(this);

        pack();
        WindowUtils.center(this, parent);
        UIStateUtils.uistatemanage(this, "JSelectionWizard");
        pack();

        // setResizable(false);
    }

    @Override
    public void setVisible(boolean visible) {
        wizard.setFinishButtonEnabled(true);
        super.setVisible(visible);
    }

    @Override
    public synchronized void finish() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        EDTUtils.setEnabled(this, false);

        SwingWorker<Boolean, Object> sw = new SwingWorker<Boolean, Object>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    return finishSelection();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void done() {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                JSelectionWizard.this.setEnabled(true);
                JSelectionWizard.this.setVisible(false);
                setText("");
            }
        };
        sw.execute();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    boolean finishSelection() {
        random.setSeed(System.nanoTime());

        LinkedList<? extends ASchwimmer> swimmer = core.getWettkampf().getSchwimmer();
        unselect(swimmer);

        if (!options.isSelected(0)) {
            LinkedList<ASchwimmer> swimmer2 = new LinkedList<ASchwimmer>();
            for (ASchwimmer s : swimmer) {
                boolean na = false;
                boolean times = false;
                for (int x = 0; x < s.getAK().getDiszAnzahl(); x++) {
                    if (s.getAkkumulierteStrafe(x).getArt() == Strafarten.NICHT_ANGETRETEN) {
                        na = true;
                    }
                    if (s.isDisciplineChosen(x) && s.getZeit(x) > 0) {
                        times = true;
                        break;
                    }
                }
                if (times || !na) {
                    swimmer2.add(s);
                }
            }
            swimmer = swimmer2;
        }

        if (mode.getSelectedIndex() < BUILD_IN) {
            switch (mode.getSelectedIndex()) {
            default:
            case 0: {
                // None
                // Nothing to do
                break;
            }
            case 1: {
                // All
                ListIterator<? extends ASchwimmer> li = swimmer.listIterator();
                while (li.hasNext()) {
                    root.getSelection().setValue(li.next(), true);
                }
                break;
            }
            case 2: {
                select(swimmer, amount.getInt(0));
                break;
            }
            case 3: {
                // Anzahl je Gliederung
                AWettkampf wk = core.getWettkampf();
                LinkedList<String> gliederungen = wk.getGliederungenMitQGliederung();
                ListIterator<String> gli = gliederungen.listIterator();
                while (gli.hasNext()) {
                    select(SearchUtils.getSchwimmer(wk, new String[] { gli.next() }, true), amount.getInt(0));
                }
                break;
            }
            case 4: {
                // Anzahl je Altersklasse
                AWettkampf wk = core.getWettkampf();
                wk.setProperty(meldekey, melde2.getSelectedIndex());
                for (int x = 0; x < wk.getRegelwerk().size(); x++) {
                    for (int y = 0; y < 2; y++) {
                        LinkedList<ASchwimmer> liste = SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(x), y == 1);
                        if (!liste.isEmpty()) {
                            if (melde.isSelected()) {
                                SearchUtils.filtereMeldepunkte(liste, melde.getValue(x, y == 1), melde2.getSelectedIndex());
                            }
                            select(liste, amount.getInt(0), melde.isWeighted());
                        }
                    }
                }
                break;
            }
            }
        } else {
            int index = mode.getSelectedIndex() - BUILD_IN;
            ISelector[] selectors = root.getSelectors();
            ISelector sel = selectors[index];
            sel.select(core.getWettkampf(), root);
        }
        // Utils.getPreferences().putBoolean(i18nprefix + ".IncludeNotStarted",
        // options.isSelected(0));

        controller.sendDataUpdateEvent(I18n.get(i18nprefix + ".Event"), UpdateEventConstants.REASON_SWIMMER_CHANGED, null);

        return true;
    }

    private void unselect(LinkedList<? extends ASchwimmer> swimmer) {
        ListIterator<? extends ASchwimmer> li = swimmer.listIterator();
        while (li.hasNext()) {
            root.getSelection().setValue(li.next(), false);
        }
    }

    /**
     * Schwimmer werden gewichted gewählt. Dabei hat der Schwimmer mit der
     * höchsten Meldepunktzahl ein Gewicht von 1, der 2. ein Gewicht von
     * (3/4)^1, der 3. ein Gewicht von (3/4)^2, ...
     * 
     * @param swimmer
     * @param anzahl
     * @param weighted
     */
    private void select(LinkedList<? extends ASchwimmer> swimmer, int anzahl, boolean weighted) {
        if (anzahl >= swimmer.size()) {
            select(swimmer, anzahl);
            return;
        }
        if (!weighted) {
            select(swimmer, anzahl);
            return;
        }

        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();
        int index = wk.getIntegerProperty(meldekey, 0);

        Collections.sort(swimmer, new SchwimmerMeldepunkteVergleicher<ASchwimmer>(true, index));
        boolean[] selected = new boolean[swimmer.size()];
        double[] values = new double[swimmer.size()];
        double sum = 0;
        double base = 4.0;
        double quote = (base - 1) / base;
        double value = 1;
        for (int x = 0; x < selected.length; x++) {
            selected[x] = false;
            sum += value;
            values[x] = sum;
            value = value * quote;
            if (value < 1e-6) {
                value = 1e-6;
            }
        }

        while (anzahl > 0) {
            double rnd = random.nextDouble() * sum;
            for (int x = 0; x < selected.length; x++) {
                if (values[x] > rnd) {
                    if (!selected[x]) {
                        selected[x] = true;
                        anzahl--;
                        root.getSelection().setValue(swimmer.get(x), true);
                    }
                    break;
                }
            }
        }
    }

    private void select(LinkedList<? extends ASchwimmer> swimmer, int anzahl) {
        if (anzahl < swimmer.size()) {
            Collections.shuffle(swimmer);
        }

        int z = 0;
        ListIterator<? extends ASchwimmer> li = swimmer.listIterator();
        while (li.hasNext()) {
            root.getSelection().setValue(li.next(), true);
            z++;
            if (z >= anzahl) {
                break;
            }
        }
    }

    class ExportFeedback implements Feedback {

        @Override
        public void showFeedback(String text) {
            InfiniteProgressUtils.setTextAsync(JSelectionWizard.this, text);
        }
    }

    @SuppressWarnings("rawtypes")
    public String[] getModes() {
        LinkedList<String> result = new LinkedList<String>();
        result.addLast(I18n.get("None"));
        result.addLast(I18n.get("All"));
        result.addLast(I18n.get("FixedAmount"));
        result.addLast(I18n.get("FixedAmountPerOrganisation"));
        result.addLast(I18n.get("FixedAmountPerAgeGroup"));
        ISelector[] add = root.getSelectors();
        for (ISelector anAdd : add) {
            result.addLast(anAdd.getName());
        }
        return result.toArray(new String[result.size()]);
    }

    class JModeChooser extends WizardOptionPage implements PageSwitchListener {

        /**
         * @param wc
         * @param title
         * @param options
         * @param enabled
         */
        public JModeChooser() {
            super(wizard, I18n.get("ChooseAMode"), I18n.get("ChooseAMode.Information"), getModes());
            wizard.setNextButtonEnabled(false);
        }

        @Override
        public void update() {
            super.update();
            if (getSelectedIndex() < 1) {
                wizard.setNextButtonEnabled(false);
            } else if (getSelectedIndex() >= BUILD_IN) {
                wizard.setNextButtonEnabled(false);
            } else {
                wizard.setNextButtonEnabled(true);
            }
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (wizard.isCurrentPage(this)) {
                update();
            }
        }
    }

    class JAmountChooser extends WizardIntegerPage implements PageSwitchListener {

        /**
         * @param wc
         * @param title
         * @param options
         * @param enabled
         */
        public JAmountChooser() {
            super(wizard, I18n.get("ChooseAnAmount"), I18n.get("ChooseAnAmount.Information"), new String[] { I18n.get("Amount") }, new boolean[] { true },
                    new int[] { 10 }, new int[] { 1 }, new int[] { 1000 });
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (wizard.isCurrentPage(this)) {
                if (mode.getSelectedIndex() != BUILD_IN - 1) {
                    wizard.setNextButtonEnabled(false);
                } else {
                    wizard.setNextButtonEnabled(true);
                }
            }
        }
    }

    class JOptionsChooser extends WizardCheckboxPage implements PageSwitchListener {

        /**
         * @param wc
         * @param title
         * @param options
         * @param enabled
         */
        public JOptionsChooser() {
            super(wizard, I18n.get("SelectOptions"), I18n.get("SelectOptions.Information"), new String[] { I18n.get("IncludeNotStarted") },
                    new String[] { I18n.getToolTip("IncludeNotStarted") },
                    new boolean[] { Utils.getPreferences().getBoolean(i18nprefix + ".IncludeNotStarted", true) });
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (wizard.isCurrentPage(this)) {
                wizard.setNextButtonEnabled(mode.getSelectedIndex() > 1);
            }
        }

        @Override
        public void update() {
            super.update();
            if (wizard.isCurrentPage(this)) {
                wizard.setNextButtonEnabled(mode.getSelectedIndex() > 1);
            }
        }
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

    class JMeldepunkteABPanel extends WizardOptionPage {

        public JMeldepunkteABPanel() {
            super(wizard, I18n.get("AnouncedPoints"), I18n.get("Selection.MeldepunkteInfo"), getOptions(core.getWettkampf()), null,
                    core.getWettkampf().getIntegerProperty(meldekey, 0), null, SwingConstants.TOP);
        }
    }

    class JMeldepunkteChooser extends AWizardPage implements PageSwitchListener {

        private final class ButtonUpdater implements ActionListener {

            public ButtonUpdater() {
                // Nothing to do
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                updateButtons();
            }
        }

        private JScrollPane       scroller = new JScrollPane();

        private JPanel            panel    = new JPanel();
        private JCheckBox         enable   = new JCheckBox(I18n.get("UseReportedPoints"));
        private JCheckBox         weighted = new JCheckBox(I18n.get("WeightedDrawing"));
        private JIntegerField[][] input;

        /**
         * @param wc
         * @param title
         * @param options
         * @param enabled
         */
        public JMeldepunkteChooser() {
            super(I18n.get("ChooseByReportedPoints"), I18n.get("ChooseByReportedPoints.Information"));
            createPanel();
        }

        public boolean isSelected() {
            return enable.isSelected();
        }

        public boolean isWeighted() {
            return weighted.isSelected();
        }

        public int getValue(int ak, boolean male) {
            return input[ak][male ? 1 : 0].getInt();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void createPanel() {
            enable.addActionListener(new ButtonUpdater());
            weighted.addActionListener(new ButtonUpdater());

            AWettkampf wk = core.getWettkampf();
            int size = wk.getRegelwerk().size();
            for (int x = 0; x < wk.getRegelwerk().size(); x++) {
                if (SearchUtils.hasSchwimmer(wk, wk.getRegelwerk().getAk(x))) {
                    size = x + 1;
                }
            }
            FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu",
                    FormLayoutUtils.createLayoutString(3 + size));
            panel.setLayout(layout);

            panel.add(weighted, CC.xyw(2, 2, 5));
            panel.add(enable, CC.xyw(2, 4, 5));
            panel.add(new JLabel(I18n.get("femaleShort")), CC.xy(4, 6, "center,fill"));
            panel.add(new JLabel(I18n.get("maleShort")), CC.xy(6, 6, "center,fill"));

            input = new JIntegerField[size][2];
            for (int x = 0; x < size; x++) {
                panel.add(new JLabel(wk.getRegelwerk().getAk(x).getName()), CC.xy(2, 8 + 2 * x));
                for (int y = 0; y < 2; y++) {
                    input[x][y] = new JIntegerField(false, true);
                    input[x][y].setAutoSelectAll(true);
                    panel.add(input[x][y], CC.xy(4 + 2 * y, 8 + 2 * x));
                    input[x][y].setInt(SearchUtils.getSchwimmer(wk, wk.getRegelwerk().getAk(x), y == 1).size());
                }
            }
            // enable.setSelected(true);
            enable.setSelected(false);
            updateButtons();

            scroller = new JScrollPane(panel);
        }

        @Override
        public JComponent getPage() {
            return scroller;
        }

        void updateButtons() {
            boolean enabled = enable.isSelected();
            for (JIntegerField[] anInput : input) {
                for (int y = 0; y < anInput.length; y++) {
                    anInput[y].setEnabled(enabled);
                }
            }
            // weighted.setEnabled(enabled);
            wizard.setNextButtonEnabled(enable.isSelected() || weighted.isSelected());
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (forward && wizard.isCurrentPage(this)) {
                updateButtons();
            }
        }
    }

    @Override
    public void cancel() {
        setVisible(false);
    }
}