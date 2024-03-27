/*
 * Created on 27.04.2005
 */
package de.df.jauswertung.gui.plugins.heats;

import static de.df.jauswertung.daten.PropertyConstants.*;
import static de.df.jauswertung.daten.laufliste.Laufeinteilungsmodus.fromValue;
import static java.util.Arrays.stream;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.blogspot.rabbithole.JSmoothList;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.Reihenfolge;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.WizardUIElementsProvider;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JRotatingLabel;
import de.df.jutils.gui.jlist.JListUtils;
import de.df.jutils.gui.jlist.ModifiableListModel;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.UIUtils;
import de.df.jutils.gui.wizard.*;
import de.df.jutils.plugin.IFeature;
import de.df.jutils.util.ArrayUtils;
import de.df.jutils.util.StringTools;

public final class JNewHeatsWizard<T extends ASchwimmer> extends JWizardDialog
        implements FinishListener, CancelListener {

    private static final long serialVersionUID = 3617856365452997169L;

    private static final boolean DSM = Utils.isInDevelopmentModeFor("DSM");

    private final AWettkampf<T> wk;
    private final IFeature origin;

    private final NumberPage numbers;
    private final TypePage type;
    private final LanesPage lanes;
    private final AKSortPage aksort;
    private final SortPage sort;
    private final JMeldepunktePanel melde;
    private final OptionsPage options;
    private final JSwitchAgegroupPanel moving;
    private final JFrame parent;

    public JNewHeatsWizard(JFrame parent, IFeature origin, CorePlugin core) {
        this(parent, origin, core.getWettkampf(), false);
    }

    public JNewHeatsWizard(JFrame parent, IFeature origin, AWettkampf<T> wk, boolean reducedMode) {
        super(parent, I18n.get("GenerateNewHeats"), WizardUIElementsProvider.getInstance(), false);
        this.origin = origin;
        this.parent = parent;
        this.wk = wk;

        numbers = new NumberPage(reducedMode);
        type = new TypePage();
        lanes = new LanesPage();
        aksort = new AKSortPage();
        sort = new SortPage();
        options = new OptionsPage();
        melde = new JMeldepunktePanel();
        if (DSM && !reducedMode) {
            moving = new JSwitchAgegroupPanel();
        } else {
            moving = null;
        }

        type.getPage();
        lanes.getPage();
        aksort.getPage();

        JWizard wizard = getWizard();

        wizard.addPage(numbers);
        if (!reducedMode) {
            wizard.addPage(type);
        }
        wizard.addPage(lanes);
        if (!reducedMode) {
            wizard.addPage(aksort);
        }
        wizard.addPage(sort);
        wizard.addPage(melde);
        wizard.addPage(options);
        if (moving != null) {
            wizard.addPage(moving);
        }
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

    static Integer[][] createArrays() {
        Integer[][] result = new Integer[2][0];
        result[0] = ArrayUtils.createIntegerArray(99, 1);
        result[1] = ArrayUtils.createIntegerArray(1000, 1);
        return result;
    }

    private class NumberPage extends WizardComboBoxPage<Integer> {
        public NumberPage(boolean reducedMode) {
            super(getWizard(),
                    I18n.get("GeneralSettings"),
                    I18n.get("NewHeats.GeneralSettings.Information"),
                    new String[] { I18n.get("NumberOfLanes"), I18n.get("FirstHeatNumber") },
                    null,
                    createArrays(),
                    new int[] { wk.getIntegerProperty(HEATS_LANES) - 1, wk.getIntegerProperty(HEATS_FIRST_HEAT) - 1 });
            if (reducedMode) {
                setEnabled(1, false);
            }
        }
    }

    private class JSwitchAgegroupPanel extends AWizardPage implements PageSwitchListener {

        @SuppressWarnings("unchecked")
        public JSwitchAgegroupPanel() {
            super(I18n.get("SwitchAgeGroups"), I18n.get("SwitchAgeGroups.Information"));

            this.wkx = JNewHeatsWizard.this.wk;

            Startgruppe[] sgs = wkx.getRegelwerk().getEffektiveStartgruppen();

            amount = new int[sgs.length][2];
            sum = new JLabel[amount.length][2];
            for (int x = 0; x < amount.length; x++) {
                for (int y = 0; y < 2; y++) {
                    amount[x][y] = SearchUtils.getSchwimmer(wkx, sgs[x], y == 1).size();
                    sum[x][y] = new JLabel();
                    sum[x][y].setHorizontalAlignment(SwingConstants.RIGHT);
                }
            }
            input = new JIntegerField[amount.length][amount.length][2];
            panel = initPage();
            for (int x = 0; x < amount.length; x++) {
                updateSGDisplay(x, 0);
                updateSGDisplay(x, 1);
            }
        }

        @SuppressWarnings({ "rawtypes" })
        private AWettkampf wkx;

        private final JIntegerField[][][] input;
        private final int[][] amount;
        private final JLabel[][] sum;
        private final JComponent panel;

        @Override
        public JComponent getPage() {
            return panel;
        }

        private JComponent initPage() {
            Startgruppe[] sgs = wkx.getRegelwerk().getEffektiveStartgruppen();

            JPanel[] px = new JPanel[2];

            for (int i = 0; i < 2; i++) {
                String columns = FormLayoutUtils.createLayoutString(sgs.length + 2);
                String rows = FormLayoutUtils.createLayoutString(sgs.length + 3);
                px[i] = new JPanel(new FormLayout(columns, rows));
                JPanel p = px[i];

                p.add(new JLabel(I18n.get("To")), CC.xy(2, 4, "right,top"));
                p.add(new JLabel(I18n.get("From")), CC.xy(2, 4, "left,bottom"));

                p.add(new JLabel(I18n.get("To")), CC.xy(4 + 2 * sgs.length, 4, "left,top"));
                p.add(new JLabel(I18n.get("From")), CC.xy(4 + 2 * sgs.length, 4, "right,bottom"));

                for (int x = 0; x < sgs.length; x++) {
                    p.add(new JRotatingLabel(sgs[x].getName()), CC.xy(4 + 2 * x, 4, "center,bottom"));

                    p.add(new JLabel(sgs[x].getName()), CC.xy(2, 6 + 2 * x));
                    p.add(new JLabel(sgs[x].getName()), CC.xy(4 + 2 * sgs.length, 6 + 2 * x));

                    p.add(sum[x][i], CC.xy(4 + 2 * x, 6 + 2 * sgs.length));

                    for (int y = 0; y < sgs.length; y++) {
                        int row = 6 + 2 * y;
                        int col = 4 + 2 * x;

                        input[x][y][i] = new JIntegerField(false, true);
                        input[x][y][i].setColumns(3);
                        input[x][y][i].setHorizontalAlignment(SwingConstants.RIGHT);
                        input[x][y][i].setAutoSelectAll(true);
                        if (x == y) {
                            input[x][y][i].setEnabled(false);
                        }
                        if (x != y) {
                            p.add(input[x][y][i], CC.xy(col, row, "fill,fill"));
                        }
                    }
                }
            }

            Object o = wkx.getProperty(PropertyConstants.DSM_MODE_DATA);
            if ((o != null) && (o instanceof int[][][])) {
                int[][][] data = (int[][][]) o;

                for (int x = 0; x < Math.min(sgs.length, data.length); x++) {
                    for (int y = 0; y < Math.min(sgs.length, data[x].length); y++) {
                        for (int z = 0; z < 2; z++) {
                            input[x][y][z].setInt(data[x][y][z]);
                        }
                    }
                }
            }

            for (int i = 0; i < 2; i++) {
                for (int x = 0; x < sgs.length; x++) {
                    for (int y = 0; y < sgs.length; y++) {
                        input[x][y][i].getDocument().addDocumentListener(new InputChangeListener(x, y, i));
                    }
                }
            }

            JTabbedPane tp = new JTabbedPane();
            tp.addTab(I18n.geschlechtToStringSubject(wkx.getRegelwerk(), false), UIUtils.surroundWithScroller(px[0]));
            tp.addTab(I18n.geschlechtToStringSubject(wkx.getRegelwerk(), true), UIUtils.surroundWithScroller(px[1]));
            return tp;
        }

        class InputChangeListener implements DocumentListener {

            private final int x, y, z;

            public InputChangeListener(int x, int y, int z) {
                this.x = x;
                this.y = y;
                this.z = z;
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                updateNumbers(x, y, z);
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                updateNumbers(x, y, z);
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                updateNumbers(x, y, z);
            }
        }

        void updateNumbers(int a, int b, int c) {
            // updateAKDisplay(a, c);
            updateSGDisplay(b, c);
        }

        void updateSGDisplay(int a, int male) {
            int[][][] data = getData();
            for (int x = 0; x < sum.length; x++) {
                for (int y = 0; y < 2; y++) {
                    int s = amount[x][y];
                    for (int i = 0; i < sum.length; i++) {
                        s = s + data[x][i][y] - data[i][x][y];
                    }
                    sum[x][y].setText("" + s);
                }
            }
            int value = 0;
            for (int x = 0; x < input.length; x++) {
                value += data[x][a][male];
            }
            boolean enabled = ((value == 0) && ((amount[a][0] > 0) || (amount[a][1] > 0)));
            for (int x = 0; x < input.length; x++) {
                if (x != a) {
                    input[a][x][male].setEnabled(enabled);
                    if (!enabled) {
                        if (input[a][x][male].getInt() != 0) {
                            input[a][x][male].setText("");
                        }
                    }
                }
                if (amount[x][male] == 0) {
                    for (int y = 0; y < input.length; y++) {
                        input[y][x][male].setEnabled(false);
                        if (input[y][x][male].getInt() != 0) {
                            input[y][x][male].setText("");
                        }
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void pageSwitch(boolean forward) {
            if (forward && getWizard().isCurrentPage(this)) {
                this.wkx = JNewHeatsWizard.this.wk;
                if (options.isSelected(7) && !wkx.HasOpenQualifications()) {
                    wkx = Utils.copy(wkx);
                    for (ASchwimmer t : ((AWettkampf<ASchwimmer>) wkx).getSchwimmer()) {
                        if (!t.getQualifikation().isAccepted()) {
                            wkx.removeSchwimmer(t);
                        }
                    }
                }
                Startgruppe[] sgs = wkx.getRegelwerk().getEffektiveStartgruppen();
                for (int x = 0; x < amount.length; x++) {
                    for (int y = 0; y < 2; y++) {
                        amount[x][y] = SearchUtils.getSchwimmer(wkx, sgs[x], y == 1).size();
                    }
                }
                for (int x = 0; x < amount.length; x++) {
                    updateSGDisplay(x, 0);
                    updateSGDisplay(x, 1);
                }
            }
        }

        public int[][][] getData() {
            int[][][] data = new int[input.length][input.length][2];
            for (int x = 0; x < data.length; x++) {
                for (int y = 0; y < data.length; y++) {
                    for (int z = 0; z < 2; z++) {
                        int value = input[x][y][z].getInt();
                        data[x][y][z] = value;
                    }
                }
            }
            return data;
        }
    }

    private class LanesPage extends AWizardPage implements PageSwitchListener {

        final class GlobalLanesSelectionListener implements ActionListener {

            private int index = 0;

            public GlobalLanesSelectionListener(int x) {
                index = x;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean state = !((JCheckBox) selections[0][index]).isSelected();
                for (Object[] selection1 : selections) {
                    JCheckBox jb = (JCheckBox) selection1[index];
                    jb.setSelected(state);
                }
            }
        }

        final class LanesSelectionListener implements ChangeListener {

            private final String name;
            private final int discipline;
            private boolean state;

            public LanesSelectionListener(String name, int discipline, boolean state) {
                this.name = name;
                this.discipline = discipline;
                this.state = state;
            }

            @Override
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof JCheckBox) {
                    JCheckBox cb = (JCheckBox) e.getSource();
                    if (cb.isSelected() != state) {
                        state = !state;
                        boolean[] s = selection.get(name);
                        s[discipline] = state;
                    }
                }
            }
        }

        private int bahnen;

        private JPanel panel = null;
        JPanel data = null;
        private JRadioButton auto = null;
        JRadioButton custom = null;

        private LinkedList<String> disciplines = new LinkedList<>();
        Hashtable<String, boolean[]> selection;
        Object[][] selections = null;

        public LanesPage() {
            super(I18n.get("LaneSelection"), I18n.get("LaneSelection.Information"));
            bahnen = wk.getIntegerProperty(PropertyConstants.HEATS_LANES);
            @SuppressWarnings("unchecked")
            Hashtable<String, boolean[]> s = (Hashtable<String, boolean[]>) wk.getProperty(HEATS_LANE_SELECTION);
            selection = new Hashtable<String, boolean[]>();
            if (s != null) {
                Enumeration<String> e = s.keys();
                while (e.hasMoreElements()) {
                    String d = e.nextElement();
                    boolean[] b = s.get(d);
                    boolean[] copy = new boolean[b.length];
                    System.arraycopy(b, 0, copy, 0, b.length);
                    selection.put(d, copy);
                }
            }
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (numbers.getSelectedItem(0) != bahnen) {
                updateDisplay(numbers.getSelectedItem(0));
            } else {
                if (forward) {
                    updateDisplay(numbers.getSelectedItem(0));
                }
            }
        }

        @Override
        public boolean leavePage(boolean forward) {
            if (!forward) {
                return true;
            }

            boolean result = true;

            Enumeration<String> e = selection.keys();
            while (e.hasMoreElements()) {
                String d = e.nextElement();
                boolean[] b = selection.get(d);
                boolean found = false;
                for (boolean aB : b) {
                    if (aB) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    DialogUtils.wichtigeMeldung(JNewHeatsWizard.this, I18n.get("Error.NoLaneSelected", d));
                    return false;
                }
            }

            return result;
        }

        private void updateDisplay(int anzahl) {
            bahnen = anzahl;
            data.removeAll();
            disciplines.clear();
            initDataPanel();
        }

        private void initDataPanel() {
            if (disciplines.isEmpty()) {
                Hashtable<String, boolean[]> old = selection;
                selection = new Hashtable<String, boolean[]>();
                Regelwerk aks = wk.getRegelwerk();
                for (int x = 0; x < aks.size(); x++) {
                    for (int y = 0; y < aks.getAk(x).getDiszAnzahl(); y++) {
                        String name = aks.getAk(x).getDisziplin(y, true).getName();
                        if (old.get(name) == null) {
                            boolean[] s = new boolean[bahnen];
                            for (int z = 0; z < bahnen; z++) {
                                s[z] = true;
                            }
                            selection.put(name, s);
                        } else {
                            boolean[] s = old.get(name);
                            if (s.length != bahnen) {
                                boolean[] s2 = new boolean[bahnen];
                                for (int z = 0; z < bahnen; z++) {
                                    if (z < s.length) {
                                        s2[z] = s[z];
                                    } else {
                                        s2[z] = true;
                                    }
                                }
                                selection.put(name, s2);
                            } else {
                                selection.put(name, s);
                            }
                        }
                    }
                }
                Enumeration<String> e = selection.keys();
                while (e.hasMoreElements()) {
                    disciplines.add(e.nextElement());
                }
                Collections.sort(disciplines);

                Object[] titles = new Object[bahnen + 1];
                titles[0] = I18n.get("Disciplines");
                for (int x = 1; x < titles.length; x++) {
                    JButton b = new JButton("" + x);
                    b.addActionListener(new GlobalLanesSelectionListener(x));
                    titles[x] = b;
                }

                LinkedList<Object[]> input = new LinkedList<>();
                ListIterator<String> li = disciplines.listIterator();
                while (li.hasNext()) {
                    String name = li.next();
                    boolean[] s = selection.get(name);

                    Object[] o = new Object[bahnen + 1];
                    o[0] = name;
                    for (int x = 1; x < titles.length; x++) {
                        JCheckBox cb = new JCheckBox("", s[x - 1]);
                        cb.addChangeListener(new LanesSelectionListener(name, x - 1, s[x - 1]));
                        o[x] = cb;
                    }

                    input.addLast(o);
                }

                selections = input.toArray(new Object[input.size()][0]);
                FormLayoutUtils.createTable(data,
                        I18n.get("AvailableLanes"),
                        titles,
                        input.toArray(new Object[input.size()][0]),
                        new int[] { SwingConstants.LEFT },
                        SwingConstants.CENTER,
                        false);

                data.setEnabled(data.isEnabled());
            }
        }

        @Override
        public JComponent getPage() {
            if (panel == null) {
                FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu",
                        "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu");
                layout.setRowGroups(new int[][] { { 2, 4 } });
                panel = new JPanel(layout);

                auto = new JRadioButton(I18n.get("UseAllLanes"), true);
                custom = new JRadioButton(I18n.get("Custom"), false);
                custom.addItemListener(e -> {
                    data.setEnabled(custom.isSelected());
                });

                data = new JPanel() {
                    private static final long serialVersionUID = -3188281347950780144L;

                    @Override
                    public void setEnabled(boolean arg0) {
                        super.setEnabled(arg0);
                        Component[] cs = getComponents();
                        for (Component c : cs) {
                            c.setEnabled(arg0);
                        }
                    }
                };

                initDataPanel();

                ButtonGroup bg = new ButtonGroup();
                bg.add(auto);
                bg.add(custom);

                custom.setSelected(wk.getProperty(PropertyConstants.HEATS_LANE_SELECTION) != null);
                data.setEnabled(custom.isSelected());

                JScrollPane scrollPane = new JScrollPane(data);
                scrollPane.setBorder(null);

                panel.add(auto, CC.xy(2, 2));
                panel.add(custom, CC.xy(2, 4));
                panel.add(scrollPane, CC.xy(2, 6));
            }
            return panel;
        }

        public Hashtable<String, boolean[]> getLaneSelection() {
            if (custom.isSelected()) {
                return selection;
            }
            return null;
        }

    }

    private class TypePage extends WizardOptionPage implements PageSwitchListener {
        public TypePage() {
            super(getWizard(),
                    I18n.get("TypeOfGeneration"),
                    I18n.get("TypeOfGeneration.Information"),
                    new String[] { I18n.get("Automatic"), I18n.get("EmptyHeatlist") },
                    (wk.getBooleanProperty(HEATS_EMPTY_LIST) ? 1 : 0));
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

    private static <T extends ASchwimmer> int getSortingIndex(AWettkampf<T> wk) {
        int sorting = wk.getIntegerProperty(HEATS_SORTING_ORDER);
        if (Reihenfolge.Regelwerk.equalsValue(sorting)) {
            sorting = Reihenfolge.length() - 1;
        }
        return sorting;
    }

    private class SortPage extends WizardOptionPage {
        public SortPage() {
            super(getWizard(),
                    I18n.get("SortingOfSwimmers"),
                    I18n.get("SortingOfSwimmers.Information"),
                    stream(Reihenfolge.values()).sorted(Comparator.comparingInt(Reihenfolge::getValue))
                            .map(r -> I18n.get("Sorting." + r.name()))
                            .toArray(String[]::new),
                    getSortingIndex(wk));
        }

        @Override
        public int getNextPageIndex() {
            if (getSelectedIndex() == 3) {
                return NEXT_PAGE_BY_INDEX;
            }
            return getWizard().getPageIndex(options);
        }
    }

    private static final String SPACING = "    ";

    private class OptionsPage extends WizardCheckboxPage implements PageSwitchListener {
        public OptionsPage() {
            super(getWizard(), I18n.get("Options"), I18n.get("Heats.Options.Information"), new String[] {
                    I18n.get("AvoidAlmostEmptyHeatsAtEndOfAgegroup"), SPACING + I18n.get("DoNotTouchFastestHeats"),
                    I18n.get("MixedHeats"), SPACING + I18n.get("MixedHeatsInFront"),
                    SPACING + I18n.get("JoinAlmostEmptyHeats"), I18n.get("RotateLanes"),
                    I18n.get("NormalAndAKInDifferentHeats"), I18n.get("RespectQualifications")
            }, new String[] {
                    I18n.getToolTip("AvoidAlmostEmptyHeatsAtEndOfAgegroup"), I18n.getToolTip("DoNotTouchFastestHeats"),
                    I18n.getToolTip("MixedHeats"), I18n.getToolTip("MixedHeatsInFront"),
                    I18n.getToolTip("JoinAlmostEmptyHeats"), I18n.getToolTip("RotateLanes"),
                    I18n.getToolTip("NormalAndAKInDifferentHeats"), I18n.getToolTip("RespectQualifications")
            }, new boolean[] {
                    wk.getBooleanProperty(HEATS_AVOID_ALMOST_EMPTY),
                    wk.getBooleanProperty(HEATS_AAE_FASTEST_HEAT_UNTOUCHED), wk.getBooleanProperty(HEATS_MIXED),
                    wk.getBooleanProperty(HEATS_MIXED_IN_FRONT), wk.getBooleanProperty(HEATS_JOIN_HEATS),
                    wk.getBooleanProperty(HEATS_ROTATE), wk.getBooleanProperty(HEATS_NOT_COMPETING_MIXED),
                    wk.getBooleanProperty(HEATS_RESPECT_QUALIFICATIONS)
            });
            addSelectionListener(e -> {
                updateEnabled();
            });
            updateEnabled();
        }

        @Override
        public void pageSwitch(boolean forward) {
            Reihenfolge reihenfolge = Reihenfolge.fromValue(sort.getSelectedIndex());
            setEnabled(5, switch (reihenfolge) {
            case Regelwerk, Meldezeiten, ILSPool, ILSPoolVorlauf, ILSOpenWater, ILSOpenWaterVorlauf, ZufallJeDisziplin -> false;
            default -> true;
            });
            setEnabled(7, !wk.HasOpenQualifications());
        }

        void updateEnabled() {
            setEnabled(3, isSelected(2));
            setEnabled(4, isSelected(2));
            setEnabled(1, isSelected(0));
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
            super(getWizard(),
                    I18n.get("AnouncedPoints"),
                    I18n.get("NewHeats.AnouncedPoints.Information"),
                    getOptions(wk),
                    null,
                    wk.getIntegerProperty(PropertyConstants.HEATS_REGISTERED_POINTS_INDEX, 0),
                    I18n.get("Heats.MeldepunkteInfo"),
                    SwingConstants.CENTER);
        }
    }

    private class AKSortPage extends AWizardPage {

        private JPanel panel;
        JRadioButton auto;
        JRadioButton custom;
        JRadioButton blocks;
        JList<Laufliste.Einteilung> listcustom;
        JList<Laufliste.BlockEinteilung> listblocks;
        CardLayout cards;
        JPanel editor;

        public AKSortPage() {
            super(I18n.get("AgeGroupAndDisciplineSorting"), I18n.get("AgeGroupAndDisciplineSorting.Information"));
            initPage();
        }

        public void initPage() {
            FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu",
                    "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu");
            layout.setRowGroups(new int[][] { { 2, 4, 6 } });
            panel = new JPanel(layout);

            auto = new JRadioButton(I18n.get("Automatic"), true);
            auto.addItemListener(e -> {
                cards.show(editor, "auto");
            });

            custom = new JRadioButton(I18n.get("HeatsDetailed"), false);
            custom.addItemListener(e -> {
                cards.show(editor, "custom");
            });
            blocks = new JRadioButton(I18n.get("HeatsByBlocks"), false);
            blocks.addItemListener(e -> {
                cards.show(editor, "blocks");
            });
            listcustom = getListCustom();
            listblocks = getListBlocks();

            ButtonGroup bg = new ButtonGroup();
            bg.add(auto);
            bg.add(custom);
            bg.add(blocks);

            cards = new CardLayout();
            editor = new JPanel(cards);
            editor.add(getDescriptionPanel(null, "Info.HeatsAutomatic"), "auto");
            editor.add(getDescriptionPanel(listcustom, "Info.HeatsDetailed"), "custom");
            editor.add(getDescriptionPanel(listblocks, "Info.HeatsByBlocks"), "blocks");
            cards.show(editor, "auto");

            switch (fromValue(wk.getLaufliste().getLastMode())) {
            case Auto:
                auto.setSelected(true);
                break;
            case Blocks:
                blocks.setSelected(true);
                break;
            case Einteilung:
                custom.setSelected(true);
                break;
            }

            panel.add(auto, CC.xy(2, 2));
            panel.add(blocks, CC.xy(2, 4));
            panel.add(custom, CC.xy(2, 6));
            panel.add(editor, CC.xy(2, 8));
        }

        private JPanel getDescriptionPanel(JList list, String text) {
            JPanel p = new JPanel(new BorderLayout(5, 5));
            if (list != null) {
                p.add(new JScrollPane(list), BorderLayout.CENTER);
            }
            p.add(new JLabel(I18n.get(text)), BorderLayout.NORTH);
            return p;
        }

        private JList<Laufliste.Einteilung> getListCustom() {
            JList<Laufliste.Einteilung> listex = new JSmoothList<>(wk.getLaufliste().getVerteilung());
            listex.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            JListUtils.setAlternatingListCellRenderer(listex, new DefaultListCellRenderer() {
                private static final long serialVersionUID = -7154403278324249315L;

                @Override
                public Component getListCellRendererComponent(JList liste, Object value, int index, boolean isSelected,
                        boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(liste, value, index, isSelected, cellHasFocus);
                    if (!(value instanceof Laufliste.Einteilung)) {
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
                    Laufliste.Einteilung data = (Laufliste.Einteilung) value;
                    Startgruppe sg = wk.getRegelwerk().getEffektiveStartgruppen()[data.getStartgruppe()];
                    Altersklasse ak = wk.getRegelwerk().getAKsForStartgroup(sg).getFirst();
                    l.setText(I18n.get("AgegroupSexDiscipline",
                            sg.getName(),
                            I18n.geschlechtToShortString(wk.getRegelwerk(), data.isMaennlich()),
                            ak.getDisziplin(data.getDisziplin(), true)));
                    return l;
                }
            });
            return listex;
        }

        private JList<Laufliste.BlockEinteilung> getListBlocks() {
            Laufliste.BlockEinteilung[] tmp = wk.getLaufliste().getBlocks();
            ModifiableListModel<Laufliste.BlockEinteilung> model = new ModifiableListModel<>(tmp);
            JList<Laufliste.BlockEinteilung> listex = new JSmoothList<>(model);
            listex.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            JListUtils.setAlternatingListCellRenderer(listex, new DefaultListCellRenderer() {
                private static final long serialVersionUID = -7154403278324249315L;

                @Override
                public Component getListCellRendererComponent(JList liste, Object value, int index, boolean isSelected,
                        boolean cellHasFocus) {
                    Component c = super.getListCellRendererComponent(liste, value, index, isSelected, cellHasFocus);
                    if (!(value instanceof Laufliste.BlockEinteilung)) {
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
                    Laufliste.BlockEinteilung data = (Laufliste.BlockEinteilung) value;
                    if (data.getStartgruppe() < 0) {
                        l.setText("--- Blockwechsel ---");
                    } else {
                        Startgruppe sg = wk.getRegelwerk().getEffektiveStartgruppen()[data.getStartgruppe()];
                        l.setText(I18n.get("AgeGroupSex",
                                sg.getName(),
                                I18n.geschlechtToString(wk.getRegelwerk(), data.isMaennlich())));
                    }
                    return l;
                }
            });

            listex.addMouseListener(new PopupListener(new BlocksPopup()));

            return listex;
        }

        class BlocksPopup extends JPopupMenu {

            private JMenuItem add = new JMenuItem(I18n.get("AddBlockswitch"), IconManager.getSmallIcon("new"));
            private JMenuItem del = new JMenuItem(I18n.get("DeleteBlockswitch"), IconManager.getSmallIcon("delete"));

            public BlocksPopup() {
                add.addActionListener(e -> {
                    @SuppressWarnings("unchecked")
                    ModifiableListModel<Laufliste.BlockEinteilung> model = (ModifiableListModel<Laufliste.BlockEinteilung>) listblocks
                            .getModel();
                    model.add(new Laufliste.BlockEinteilung(-1, false), listblocks.getSelectedIndex());
                });
                del.addActionListener(e -> {
                    @SuppressWarnings("unchecked")
                    ModifiableListModel<Laufliste.BlockEinteilung> model = (ModifiableListModel<Laufliste.BlockEinteilung>) listblocks
                            .getModel();
                    model.remove(listblocks.getSelectedIndex());
                });

                add(add);
                add(del);
            }

            @Override
            public void setVisible(boolean b) {
                if (b) {
                    int index = listblocks.getSelectedIndex();
                    Laufliste.BlockEinteilung data = new Laufliste.BlockEinteilung(0, false);
                    if (index >= 0) {
                        data = (Laufliste.BlockEinteilung) listblocks.getModel().getElementAt(index);
                    }
                    del.setEnabled(data.getStartgruppe() < 0);
                }
                super.setVisible(b);
            }
        }

        class PopupListener extends MouseAdapter {
            JPopupMenu popup;

            PopupListener(JPopupMenu popupMenu) {
                popup = popupMenu;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = listblocks.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        listblocks.setSelectedIndex(index);
                    }
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }

        @Override
        public JComponent getPage() {
            return panel;
        }

        public static final int AUTO = 0;
        public static final int BLOCKS = 1;
        public static final int DETAILED = 2;

        public int getSelection() {
            if (blocks.isSelected()) {
                return BLOCKS;
            }
            if (custom.isSelected()) {
                return DETAILED;
            }
            return AUTO;
        }

        public Laufliste.BlockEinteilung[] getBlocks() {
            if (getSelection() != BLOCKS) {
                throw new IllegalStateException();
            }
            ListModel model = listblocks.getModel();
            int size = model.getSize();
            Laufliste.BlockEinteilung[] result = new Laufliste.BlockEinteilung[size];
            for (int x = 0; x < result.length; x++) {
                result[x] = (Laufliste.BlockEinteilung) model.getElementAt(x);
            }
            return result;
        }

        public Laufliste.Einteilung[] getReihenfolge() {
            if (getSelection() == BLOCKS) {
                throw new IllegalStateException();
            }
            if (getSelection() == AUTO) {
                return wk.getLaufliste().getStandardVerteilung();
            }
            ListModel model = listcustom.getModel();
            Laufliste.Einteilung[] daten = new Laufliste.Einteilung[model.getSize()];
            for (int x = 0; x < daten.length; x++) {
                daten[x] = (Laufliste.Einteilung) model.getElementAt(x);
            }
            return daten;
        }
    }

    @Override
    public void finish() {
        EDTUtils.setEnabled(this, false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        EDTUtils.executeOnEDT(this::finishHeats);
        SwingWorker<Boolean, Object> sw = new SwingWorker<Boolean, Object>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    long eventtype = 0;
                    switch (aksort.getSelection()) {
                    default:
                    case AKSortPage.AUTO: {
                        wk.getLaufliste().erzeugen();
                        eventtype = UpdateEventConstants.REASON_LAUF_LIST_CHANGED;
                        break;
                    }
                    case AKSortPage.BLOCKS: {
                        boolean reordered = wk.getLaufliste().erzeugen(aksort.getBlocks());
                        eventtype = UpdateEventConstants.REASON_LAUF_LIST_CHANGED
                                | (reordered
                                        ? UpdateEventConstants.REASON_AKS_CHANGED
                                                | UpdateEventConstants.REASON_MELDEZEITEN_CHANGED
                                                | UpdateEventConstants.REASON_PENALTY
                                                | UpdateEventConstants.REASON_POINTS_CHANGED
                                                | UpdateEventConstants.REASON_SWIMMER_CHANGED
                                        : UpdateEventConstants.NOTHING);
                        break;
                    }
                    case AKSortPage.DETAILED: {
                        boolean reordered = wk.getLaufliste().erzeugen(aksort.getReihenfolge());
                        eventtype = UpdateEventConstants.REASON_LAUF_LIST_CHANGED
                                | (reordered
                                        ? UpdateEventConstants.REASON_AKS_CHANGED
                                                | UpdateEventConstants.REASON_MELDEZEITEN_CHANGED
                                                | UpdateEventConstants.REASON_PENALTY
                                                | UpdateEventConstants.REASON_POINTS_CHANGED
                                                | UpdateEventConstants.REASON_SWIMMER_CHANGED
                                        : UpdateEventConstants.NOTHING);
                        break;
                    }
                    }
                    origin.sendDataUpdateEvent("NewHeatlist", eventtype);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                EDTUtils.setEnabled(JNewHeatsWizard.this, true);
                setVisible(false);
            }
        };
        sw.execute();
    }

    void finishHeats() {
        wk.setProperty(HEATS_LANES, numbers.getSelectedItem(0));
        wk.setProperty(HEATS_FIRST_HEAT, numbers.getSelectedItem(1));
        int sorting = sort.getSelectedIndex();
        if (sorting + 1 == sort.getItemCount()) {
            sorting = Reihenfolge.Regelwerk.getValue();
        }
        wk.setProperty(HEATS_SORTING_ORDER, sorting);

        wk.setProperty(HEATS_AVOID_ALMOST_EMPTY, options.isSelected(0));
        wk.setProperty(HEATS_AAE_FASTEST_HEAT_UNTOUCHED, options.isSelected(1));
        wk.setProperty(HEATS_MIXED, options.isSelected(2));
        wk.setProperty(HEATS_MIXED_IN_FRONT, options.isSelected(3) && options.isSelected(2));
        wk.setProperty(HEATS_JOIN_HEATS, options.isSelected(4) && options.isSelected(2));
        wk.setProperty(HEATS_ROTATE, options.isSelected(5));
        wk.setProperty(HEATS_NOT_COMPETING_MIXED, options.isSelected(6));
        wk.setProperty(HEATS_RESPECT_QUALIFICATIONS, options.isSelected(7));

        wk.setProperty(HEATS_EMPTY_LIST, type.getSelectedIndex() == 1);
        wk.setProperty(HEATS_LANE_SELECTION, lanes.getLaneSelection());

        wk.setProperty(HEATS_REGISTERED_POINTS_INDEX, melde.getSelectedIndex());

        if (moving != null) {
            wk.setProperty(DSM_MODE_DATA, moving.getData());
        }
    }

    @Override
    public void cancel() {
        setVisible(false);
    }

}