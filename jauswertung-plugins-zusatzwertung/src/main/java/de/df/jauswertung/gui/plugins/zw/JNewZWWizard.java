/*
 * Created on 27.04.2005
 */
package de.df.jauswertung.gui.plugins.zw;

import static de.df.jauswertung.daten.PropertyConstants.ZW_DURATION;
import static de.df.jauswertung.daten.PropertyConstants.ZW_EMPTY_LIST;
import static de.df.jauswertung.daten.PropertyConstants.ZW_IGNORE_AK_SWIMMERS;
import static de.df.jauswertung.daten.PropertyConstants.ZW_LANES;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE_AGEGROUPS;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE_DURATION;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE_MODE;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE_RESTARTS;
import static de.df.jauswertung.daten.PropertyConstants.ZW_PAUSE_START;
import static de.df.jauswertung.daten.PropertyConstants.ZW_REGISTERED_POINTS_INDEX;
import static de.df.jauswertung.daten.PropertyConstants.ZW_RESPECT_QUALIFICATIONS;
import static de.df.jauswertung.daten.PropertyConstants.ZW_SORTING_ORDER;
import static de.df.jauswertung.daten.PropertyConstants.ZW_STARTTIME;

import java.awt.Component;
import java.awt.Cursor;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.blogspot.rabbithole.JSmoothList;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.Duration;
import de.df.jauswertung.daten.laufliste.HLWListe;
import de.df.jauswertung.daten.laufliste.Time;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.WizardUIElementsProvider;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.jlist.JListUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.wizard.AWizardPage;
import de.df.jutils.gui.wizard.CancelListener;
import de.df.jutils.gui.wizard.FinishListener;
import de.df.jutils.gui.wizard.JWizard;
import de.df.jutils.gui.wizard.JWizardDialog;
import de.df.jutils.gui.wizard.PageSwitchListener;
import de.df.jutils.gui.wizard.WizardCheckboxPage;
import de.df.jutils.gui.wizard.WizardIntegerPage;
import de.df.jutils.gui.wizard.WizardOptionPage;
import de.df.jutils.util.StringTools;

final class JNewZWWizard extends JWizardDialog implements FinishListener, CancelListener {

    private static final Duration[] DURATIONS = new Duration[] { new Duration(2), new Duration(3), new Duration(4),
            new Duration(5), new Duration(7, 30),
            new Duration(10), new Duration(15), new Duration(20) };

    private static final Duration[] PAUSE_DURATIONS = new Duration[] { new Duration(15), new Duration(30),
            new Duration(45), new Duration(60),
            new Duration(90), new Duration(120), new Duration(180), new Duration(240), new Duration(300),
            new Duration(360) };

    private static final long serialVersionUID = 3617856365452997169L;

    @SuppressWarnings("rawtypes")
    final AWettkampf wk;
    final CorePlugin core;
    final MZWPlugin origin;

    private final NumberPage numbers;
    private final SortPage sort;
    private final TypePage type;
    final TimePage times;
    private final FreeTimePage freetime;
    final OptionsPage options;
    final AKSortPage aksort;
    final JMeldepunktePanel melde;

    private final JFrame parent;

    public JNewZWWizard(JFrame parent, MZWPlugin origin, CorePlugin core) {
        super(parent, I18n.get("GenerateNewZW"), WizardUIElementsProvider.getInstance(), false);
        this.core = core;
        this.parent = parent;
        this.origin = origin;
        wk = core.getWettkampf();

        numbers = new NumberPage();
        type = new TypePage();
        aksort = new AKSortPage();
        sort = new SortPage();
        times = new TimePage();
        freetime = new FreeTimePage();
        options = new OptionsPage();
        melde = new JMeldepunktePanel();

        JWizard wizard = getWizard();

        wizard.addPage(numbers);
        wizard.addPage(type);
        wizard.addPage(aksort);
        wizard.addPage(sort);
        wizard.addPage(melde);
        wizard.addPage(times);
        wizard.addPage(freetime);
        wizard.addPage(options);
        wizard.addListener(this);

        wizard.setFinishButtonEnabled(true);

        pack();
        UIStateUtils.uistatemanage(parent, this, "JNewHeatsWizard");
    }

    @Override
    public void setVisible(boolean b) {
        if (parent != null) {
            parent.setEnabled(!b);
        }
        super.setVisible(b);
    }

    private class NumberPage extends WizardIntegerPage {
        public NumberPage() {
            super(getWizard(), I18n.get("GeneralSettings"), I18n.get("NewZW.GeneralSettings.Information"),
                    new String[] { I18n.get("NumberOfLanes") }, null,
                    new int[] { wk.getIntegerProperty(ZW_LANES) }, new int[] { 1 }, new int[] { 20 });
        }
    }

    class SortPage extends WizardOptionPage {
        public SortPage() {
            super(getWizard(), I18n.get("SortingOfSwimmers"), I18n.get("NewZW.SortingOfSwimmers.Information"),
                    new String[] { I18n.get("Randomly"), I18n.get("SameOrganisationDifferentHeats"),
                            I18n.get("SortByAnouncedPoints"),
                            I18n.get("SameOrganisationSameHeat"), I18n.get("SortByOrganisationIgnoreAgegroups") },
                    wk.getIntegerProperty(ZW_SORTING_ORDER));
        }

        @Override
        public int getNextPageIndex() {
            if (getSelectedIndex() == 2) {
                return NEXT_PAGE_BY_INDEX;
            }
            return getWizard().getPageIndex(times);
        }
    }

    private class OptionsPage extends WizardCheckboxPage implements PageSwitchListener {

        public OptionsPage() {
            super(getWizard(), I18n.get("Options"), I18n.get("NewZW.Options.Information"),
                    new String[] { I18n.get("ZW.IgnoreAKSwimmers"), I18n.get("RespectQualifications") },
                    new boolean[] { wk.getBooleanProperty(ZW_IGNORE_AK_SWIMMERS),
                            wk.getBooleanProperty(ZW_RESPECT_QUALIFICATIONS) });
        }

        @Override
        public void pageSwitch(boolean forward) {
            setEnabled(1, !wk.HasOpenQualifications());
        }
    }

    private class TypePage extends WizardOptionPage implements PageSwitchListener {
        public TypePage() {
            super(getWizard(), I18n.get("TypeOfGeneration"), I18n.get("NewZW.TypeOfGeneration.Information"),
                    new String[] { I18n.get("Automatic"), I18n.get("Empty") },
                    (wk.getBooleanProperty(ZW_EMPTY_LIST) ? 1 : 0));
        }

        private void updateButton() {
            if (getWizard().isCurrentPage(this)) {
                getWizard().setNextButtonEnabled(getSelectedIndex() == 0);
            }
        }

        @Override
        public void update() {
            super.update();
            updateButton();
        }

        @Override
        public int getNextPageIndex() {
            if (getSelectedIndex() == 1) {
                return NO_NEXT_PAGE;
            }
            return super.getNextPageIndex();
        }

        @Override
        public void pageSwitch(boolean forward) {
            updateButton();
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

    class JMeldepunktePanel extends WizardOptionPage {

        public JMeldepunktePanel() {
            super(getWizard(), I18n.get("AnouncedPoints"), I18n.get("NewZW.AnouncedPoints.Information"),
                    getOptions(core.getWettkampf()), null,
                    core.getWettkampf().getIntegerProperty(PropertyConstants.ZW_REGISTERED_POINTS_INDEX, 0),
                    I18n.get("ZW.MeldepunkteInfo"),
                    SwingConstants.CENTER);
        }
    }

    private class TimePage extends AWizardPage {

        private JPanel panel;
        private JComboBox time;
        private JComboBox duration;

        public TimePage() {
            super(I18n.get("Starttime"), I18n.get("NewZW.Starttime.Information"));

            FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                    "4dlu:grow,fill:default,4dlu,fill:default,4dlu:grow");
            layout.setRowGroups(new int[][] { { 2, 4 }, { 1, 5 } });
            panel = new JPanel(layout);

            panel.add(new JLabel(I18n.get("TimeOfDay") + ":"), CC.xy(2, 2));
            panel.add(new JLabel(I18n.get("Duration") + ":"), CC.xy(2, 4));

            time = new JComboBox(generateTimes());
            duration = new JComboBox(generateDurations());

            panel.add(time, CC.xy(4, 2));
            panel.add(duration, CC.xy(4, 4));

            setTime(wk.getIntegerProperty(ZW_STARTTIME));
            setDuration(wk.getDoubleProperty(ZW_DURATION));
        }

        public void setTime(int i) {
            setTime(new Time(i));
        }

        public void setDuration(double i) {
            setDuration(new Duration((int) Math.floor(i), (int) (Math.round(i * 60) % 60)));
        }

        public void setTime(Time t) {
            ComboBoxModel model = time.getModel();
            for (int x = 0; x < model.getSize(); x++) {
                Time t1 = (Time) model.getElementAt(x);
                if (t1.equals(t)) {
                    time.setSelectedIndex(x);
                    return;
                }
            }
        }

        public void setDuration(Duration t) {
            ComboBoxModel model = duration.getModel();
            for (int x = 0; x < model.getSize(); x++) {
                Duration t1 = (Duration) model.getElementAt(x);
                if (t1.equals(t)) {
                    duration.setSelectedIndex(x);
                    return;
                }
            }
        }

        public int getTime() {
            return ((Time) time.getSelectedItem()).getTimeInMinutes();
        }

        public double getDuration() {
            return ((Duration) duration.getSelectedItem()).getTime();
        }

        @Override
        public JComponent getPage() {
            return panel;
        }
    }

    private class FreeTimePage extends AWizardPage implements PageSwitchListener {

        private final class RestarttimeUpdater implements ChangeListener {

            private final int x;

            public RestarttimeUpdater(int index) {
                x = index;
            }

            @Override
            public void stateChanged(ChangeEvent arg0) {
                restarttime[x].setEnabled(ak[x].isSelected() && ak[x].isEnabled());
            }
        }

        final class SelectionListener implements ChangeListener {
            @Override
            public void stateChanged(ChangeEvent e) {
                time.setEnabled(checktime.isSelected());
                duration.setEnabled(checktime.isSelected());

                for (int x = 0; x < ak.length; x++) {
                    ak[x].setEnabled(checkak.isSelected());
                    restarttime[x].setEnabled(ak[x].isSelected() && ak[x].isEnabled());
                }
            }
        }

        private JPanel panel;

        JRadioButton nopause;
        JRadioButton checktime;
        JRadioButton checkak;
        private ButtonGroup buttongroup;

        JComboBox time;
        JComboBox duration;

        JCheckBox[] ak;
        JComboBox[] restarttime;

        public FreeTimePage() {
            super(I18n.get("Pause"), I18n.get("NewZW.Pause.Information"));

            try {
                nopause = new JRadioButton(I18n.get("NoPause"));
                nopause.addChangeListener(new SelectionListener());
                checktime = new JRadioButton(I18n.get("AtTimeOfDay"));
                checktime.addChangeListener(new SelectionListener());
                checkak = new JRadioButton(I18n.get("PauseAfterAgeGroup"));
                checkak.addChangeListener(new SelectionListener());

                buttongroup = new ButtonGroup();
                buttongroup.add(nopause);
                buttongroup.add(checktime);
                buttongroup.add(checkak);

                FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu",
                        "4dlu:grow,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default,"
                                + "4dlu,fill:default,4dlu:grow");
                panel = new JPanel(layout);

                panel.add(nopause, CC.xy(2, 2));
                panel.add(checktime, CC.xy(2, 4));
                panel.add(getTimePanel(), CC.xy(2, 6));
                panel.add(checkak, CC.xy(2, 8));
                panel.add(getAgegroupPanel(), CC.xy(2, 10));

                setTime(wk.getIntegerProperty(ZW_PAUSE_START));
                setDuration(wk.getDoubleProperty(ZW_PAUSE_DURATION));
                setRestarttime(wk.getProperty(ZW_PAUSE_RESTARTS));
                setAgegroup(wk.getProperty(ZW_PAUSE_AGEGROUPS));
                nopause.setSelected(!wk.getBooleanProperty(ZW_PAUSE));

                if (!nopause.isSelected()) {
                    boolean b = HLWListe.PAUSE_MODE_AGEGROUP == wk.getIntegerProperty(ZW_PAUSE_MODE);
                    checktime.setSelected(!b);
                    checkak.setSelected(b);
                }

                if ((!checkak.isEnabled()) && checkak.isSelected()) {
                    checktime.setSelected(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private JPanel getTimePanel() {
            time = new JComboBox(generateTimes());
            time.setEnabled(false);
            duration = new JComboBox(generatePauseDurations());
            duration.setEnabled(false);

            FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,0dlu",
                    "0dlu,fill:default,4dlu,fill:default,0dlu");
            layout.setRowGroups(new int[][] { { 2, 4 } });
            JPanel checkpanel = new JPanel(layout);

            checkpanel.add(new JLabel(I18n.get("TimeOfDay") + ":"), CC.xy(2, 2));
            checkpanel.add(time, CC.xy(4, 2));
            checkpanel.add(new JLabel(I18n.get("Duration") + ":"), CC.xy(2, 4));
            checkpanel.add(duration, CC.xy(4, 4));

            return checkpanel;
        }

        @SuppressWarnings("unchecked")
        private JPanel getAgegroupPanel() {
            int count = -1;
            for (int x = 0; x < wk.getRegelwerk().size(); x++) {
                if (wk.getRegelwerk().getAk(x).hasHLW() && SearchUtils.hasSchwimmer(wk, wk.getRegelwerk().getAk(x))) {
                    count += 2;
                }
            }
            restarttime = new JComboBox[count];
            ak = new JCheckBox[count];
            for (int x = 0; x < ak.length; x++) {
                restarttime[x] = new JComboBox(generateTimes());
                restarttime[x].setEnabled(false);
                ak[x] = new JCheckBox();
                ak[x].setEnabled(false);
                ak[x].addChangeListener(new RestarttimeUpdater(x));
            }

            SimpleFormBuilder sfb = new SimpleFormBuilder();

            for (int x = 0; x < ak.length; x++) {
                sfb.add(ak[x], restarttime[x]);
            }

            if ((restarttime.length <= 0) && (checkak.isSelected())) {
                nopause.setSelected(true);
            }
            checkak.setEnabled(restarttime.length > 0);

            return sfb.getPanel();
        }

        public boolean isPauseEnabled() {
            return !nopause.isSelected();
        }

        public int getPauseMode() {
            if (checktime.isSelected()) {
                return HLWListe.PAUSE_MODE_TIME;
            }
            return HLWListe.PAUSE_MODE_AGEGROUP;
        }

        public void setTime(int i) {
            setTime(new Time(i));
        }

        public void setDuration(double i) {
            setDuration(new Duration((int) Math.floor(i), (int) Math.round(i * 60) / 60));
        }

        public void setRestarttime(Object o) {
            int[][] i = HLWListe.checkTimes(o);
            for (int y = 0; y < restarttime.length; y++) {
                HLWListe.Einteilung e = aksort.getEinteilung(y);
                if (e.getAK() < i.length) {
                    Time t = new Time(i[e.getAK()][e.isMaennlich() ? 1 : 0]);
                    setRestarttime(y, t);
                }
            }
        }

        public void setAgegroup(Object o) {
            boolean[][] i = HLWListe.checkPauses(o);
            for (int y = 0; y < ak.length; y++) {
                HLWListe.Einteilung e = aksort.getEinteilung(y);
                if (e.getAK() < i.length) {
                    ak[y].setSelected(i[e.getAK()][e.isMaennlich() ? 1 : 0]);
                }
            }
        }

        public void setTime(Time t) {
            ComboBoxModel model = time.getModel();
            for (int x = 0; x < model.getSize(); x++) {
                Time t1 = (Time) model.getElementAt(x);
                if (t1.equals(t)) {
                    time.setSelectedIndex(x);
                    return;
                }
            }
        }

        public void setDuration(Duration t) {
            ComboBoxModel model = duration.getModel();
            for (int x = 0; x < model.getSize(); x++) {
                Duration t1 = (Duration) model.getElementAt(x);
                if (t1.equals(t)) {
                    duration.setSelectedIndex(x);
                    return;
                }
            }
        }

        public void setRestarttime(int index, Time t) {
            ComboBoxModel model = restarttime[index].getModel();
            for (int x = 0; x < model.getSize(); x++) {
                Time t1 = (Time) model.getElementAt(x);
                if (t1.equals(t)) {
                    restarttime[index].setSelectedIndex(x);
                    return;
                }
            }
        }

        public int getTime() {
            return ((Time) time.getSelectedItem()).getTimeInMinutes();
        }

        public double getDuration() {
            return ((Duration) duration.getSelectedItem()).getTime();
        }

        public int[][] getRestarttime() {
            HLWListe.Einteilung[] reihenfolge = aksort.getReihenfolge();
            int[][] result = new int[wk.getRegelwerk().size()][2];
            for (int x = 0; x < restarttime.length; x++) {
                result[reihenfolge[x].getAK()][reihenfolge[x].isMaennlich() ? 1
                        : 0] = ((Time) restarttime[x].getSelectedItem()).getTimeInMinutes();
            }
            return result;
        }

        public boolean[][] getAgegroup() {
            HLWListe.Einteilung[] reihenfolge = aksort.getReihenfolge();
            boolean[][] result = new boolean[wk.getRegelwerk().size()][2];
            for (int x = 0; x < ak.length; x++) {
                int akn = reihenfolge[x].getAK();
                int male = reihenfolge[x].isMaennlich() ? 1 : 0;
                boolean value = ak[x].isSelected();
                result[akn][male] = value;
            }
            return result;
        }

        @Override
        public JComponent getPage() {
            return panel;
        }

        @Override
        public void pageSwitch(boolean forward) {
            try {
                if (forward && getWizard().isCurrentPage(this)) {
                    HLWListe.Einteilung[] reihenfolge = aksort.getReihenfolge();
                    Regelwerk aks = wk.getRegelwerk();
                    for (int x = 0; x < ak.length; x++) {
                        ak[x].setText(
                                I18n.get("PauseAfterXUntil", I18n.getAgeGroupAsString(aks,
                                        aks.getAk(reihenfolge[x].getAK()), reihenfolge[x].isMaennlich())));
                    }
                    setRestarttime(wk.getProperty(ZW_PAUSE_RESTARTS));
                    setAgegroup(wk.getProperty(ZW_PAUSE_AGEGROUPS));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class AKSortPage extends AWizardPage {

        private JPanel panel;
        private JRadioButton auto;
        JRadioButton custom;
        JList list;

        public AKSortPage() {
            super(I18n.get("AgeGroupSorting"), I18n.get("NewZW.AgeGroupSorting.Information"));
            initPage();
        }

        public void initPage() {
            FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu",
                    "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu");
            layout.setRowGroups(new int[][] { { 2, 4 } });
            panel = new JPanel(layout);

            auto = new JRadioButton(I18n.get("Automatic"), true);
            custom = new JRadioButton(I18n.get("Custom"), false);
            custom.addItemListener(e -> {
                list.setEnabled(custom.isSelected());
            });
            list = new JSmoothList<HLWListe.Einteilung>(wk.getHLWListe().getVerteilung());
            list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            JListUtils.setAlternatingListCellRenderer(list, new DefaultListCellRenderer() {
                private static final long serialVersionUID = -7154403278324249315L;

                @Override
                public Component getListCellRendererComponent(JList liste, Object value, int index, boolean isSelected,
                        boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(liste, value, index, isSelected, cellHasFocus);
                    if (!(value instanceof HLWListe.Einteilung)) {
                        return c;
                    }
                    JLabel l = null;
                    if (c instanceof JLabel) {
                        l = (JLabel) c;
                    } else {
                        l = new JLabel();
                        l.setBackground(c.getBackground());
                        l.setForeground(c.getForeground());
                        l.setFont(c.getFont());
                    }
                    HLWListe.Einteilung e = (HLWListe.Einteilung) value;
                    int data = e.getAK();
                    boolean male = e.isMaennlich();
                    Altersklasse ak = wk.getRegelwerk().getAk(data);
                    l.setText(I18n.getAgeGroupAsString(wk.getRegelwerk(), ak, male));
                    return l;
                }
            });
            list.setEnabled(false);

            ButtonGroup bg = new ButtonGroup();
            bg.add(auto);
            bg.add(custom);

            if ((list.getModel().getSize() <= 1) && (custom.isSelected())) {
                auto.setSelected(true);
            }
            custom.setEnabled(list.getModel().getSize() > 1);
            if (wk.getHLWListe().hasStandardVerteilung()) {
                auto.setSelected(true);
            } else {
                custom.setSelected(true);
            }

            panel.add(auto, CC.xy(2, 2));
            panel.add(custom, CC.xy(2, 4));
            panel.add(new JScrollPane(list), CC.xy(2, 6));
        }

        @Override
        public JComponent getPage() {
            return panel;
        }

        public boolean isAutomatic() {
            return auto.isSelected();
        }

        public HLWListe.Einteilung[] getReihenfolge() {
            if (isAutomatic()) {
                return wk.getHLWListe().getStandardVerteilung();
            }
            ListModel model = list.getModel();
            HLWListe.Einteilung[] daten = new HLWListe.Einteilung[model.getSize()];
            for (int x = 0; x < daten.length; x++) {
                daten[x] = (HLWListe.Einteilung) model.getElementAt(x);
            }
            return daten;
        }

        public HLWListe.Einteilung getEinteilung(int x) {
            if (isAutomatic()) {
                HLWListe.Einteilung[] verteilung = wk.getHLWListe().getStandardVerteilung();
                if (verteilung.length <= x) {
                    throw new IllegalArgumentException(
                            "Index higher than or equal to array-length: " + x + " >= " + verteilung.length);
                }
                return verteilung[x];
            }
            ListModel model = list.getModel();
            return (HLWListe.Einteilung) model.getElementAt(x);
        }
    }

    @Override
    public void finish() {
        EDTUtils.setEnabled(this, false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        EDTUtils.executeOnEDT(this::finishHLW);

        @SuppressWarnings("rawtypes")
        SwingWorker sw = new SwingWorker<Boolean, Object>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (aksort.isAutomatic()) {
                    origin.erzeugeHlwliste();
                } else {
                    origin.erzeugeHlwliste(aksort.getReihenfolge());
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                EDTUtils.setEnabled(JNewZWWizard.this, true);
                setVisible(false);
            }
        };
        sw.execute();
    }

    public void finishHLW() {
        wk.setProperty(ZW_EMPTY_LIST, type.getSelectedIndex() == 1);
        wk.setProperty(ZW_LANES, numbers.getInt(0));
        wk.setProperty(ZW_SORTING_ORDER, sort.getSelectedIndex());
        wk.setProperty(ZW_STARTTIME, times.getTime());
        wk.setProperty(ZW_DURATION, times.getDuration());
        wk.setProperty(ZW_PAUSE, freetime.isPauseEnabled());
        wk.setProperty(ZW_PAUSE_START, freetime.getTime());
        wk.setProperty(ZW_PAUSE_DURATION, freetime.getDuration());
        wk.setProperty(ZW_PAUSE_AGEGROUPS, freetime.getAgegroup());
        wk.setProperty(ZW_PAUSE_RESTARTS, freetime.getRestarttime());
        wk.setProperty(ZW_PAUSE_MODE, freetime.getPauseMode());
        wk.setProperty(ZW_IGNORE_AK_SWIMMERS, options.isSelected(0));
        wk.setProperty(ZW_RESPECT_QUALIFICATIONS, options.isSelected(1));
        wk.setProperty(ZW_REGISTERED_POINTS_INDEX, melde.getSelectedIndex());
    }

    @Override
    public void cancel() {
        setVisible(false);
    }

    static Time[] generateTimes() {
        int minutes = 5;
        Time[] strings = new Time[24 * 60 / minutes];
        int pos = 0;
        for (int x = 0; x < 24; x++) {
            for (int y = 0; y < 60; y += minutes) {
                strings[pos] = new Time(x, y);
                pos++;
            }
        }
        return strings;
    }

    static Duration[] generateDurations() {
        return DURATIONS;
    }

    static Duration[] generatePauseDurations() {
        return PAUSE_DURATIONS;
    }
}