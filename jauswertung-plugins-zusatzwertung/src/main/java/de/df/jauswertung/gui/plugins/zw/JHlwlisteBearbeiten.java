package de.df.jauswertung.gui.plugins.zw;

/**
 * @author Dennis Fabri
 * @since 29. Juli 2001, 15:58
 */

import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TooManyListenersException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.tasks.JTaskPaneGroup;
import com.xduke.xswing.DataTipManager;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.Duration;
import de.df.jauswertung.daten.laufliste.HLWLauf;
import de.df.jauswertung.daten.laufliste.HLWListe;
import de.df.jauswertung.daten.laufliste.Time;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.ZWTableCellRenderer;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.JInvisibleSplitPane;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.jtable.ColumnFittingMouseAdapter;
import de.df.jutils.gui.jtable.JFittingTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.plaf.GradientTaskPaneGroupUI;
import de.df.jutils.gui.renderer.ComfortListCellRenderer;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

class JHlwlisteBearbeiten<T extends ASchwimmer> extends JFrame {

    final class TableMouseListener extends MouseAdapter {
        @SuppressWarnings("unchecked")
        @Override
        public void mousePressed(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                int row = tabelle.rowAtPoint(evt.getPoint());
                int col = tabelle.columnAtPoint(evt.getPoint());
                if ((row > -1) && (col > -1)) {
                    tabelle.setRowSelectionInterval(row, row);
                    tabelle.setColumnSelectionInterval(col, col);

                    ZWTableModel<T> model = (ZWTableModel<T>) tabelle.getModel();
                    int[] index = model.rowToIndex(row);
                    if (index[0] == 0) {
                        editPause.setText(I18n.get("SetStarttime"));
                    } else {
                        editPause.setText(I18n.get("EditPause"));
                    }
                    if (model.isPause(row)) {
                        removeHeat.setEnabled(false);
                        removePause.setEnabled(true);
                    } else {
                        removeHeat.setEnabled(wk.getHLWListe().getLaufliste(index[0]).size() > 1);
                        removePause.setEnabled(false);
                    }

                    if (col < 2) {
                        entfernen.setEnabled(false);
                    } else {
                        if (model.isPause(row)) {
                            entfernen.setEnabled(false);
                        } else {
                            entfernen.setEnabled(wk.getHLWListe().get(index).getSchwimmer(col - 2) != null);
                        }
                    }
                    popup.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            mousePressed(evt);
        }

        @Override
        public void mouseClicked(MouseEvent evt) {
            if ((tabelle.getSelectedRow() > -1) && (!evt.isPopupTrigger()) && (evt.getClickCount() == 2)) {
                hlwAendern();
            } else {
                mousePressed(evt);
            }
        }
    }

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3256720697500643376L;

    AWettkampf<T> wk = null;
    boolean darfAendern = false;
    Window parent = null;

    JMenuItem entfernen;

    private final int meldeindex;

    JFittingTable tabelle = null;
    JPopupMenu popup = null;
    JMenuItem removeHeat = null;
    JMenuItem removePause = null;
    JMenuItem editPause = null;
    private JLabel startnummer = new JLabel();
    private JLabel name = new JLabel();
    private JLabel gliederung = new JLabel();
    private JLabel altersklasse = new JLabel();
    private JLabel punkte = new JLabel();
    private JLabel bemerkung = new JLabel();
    private JLabel ausserk = new JLabel();
    JList vergabeliste = null;

    T swimmer = null;
    private JSplitPane splitter = null;

    private ISimpleCallback<JHlwlisteBearbeiten<T>> callback = null;

    private boolean changed = false;

    public JHlwlisteBearbeiten(Window parent, AWettkampf<T> wettkampf, boolean mayChange,
            ISimpleCallback<JHlwlisteBearbeiten<T>> callback) {
        if (wettkampf == null) {
            throw new NullPointerException();
        }
        this.parent = parent;
        this.callback = callback;
        wk = wettkampf;
        darfAendern = mayChange;
        setIconImages(IconManager.getTitleImages());

        meldeindex = wk.getIntegerProperty(PropertyConstants.ZW_REGISTERED_POINTS_INDEX, 0);

        initComponents();
        displaySchwimmer(null);

        if (mayChange) {
            initDragAndDrop();
        }

        WindowUtils.setSize(this, 800, 600);
        WindowUtils.checkMinimumSize(this);
        WindowUtils.maximize(this);
        setSplitter();
        UIStateUtils.uistatemanage(parent, this, JHlwlisteBearbeiten.class.getName());
    }

    // *****************************************************************

    private void setSplitter() {
        SwingUtilities.invokeLater(this::setSplitterI);
    }

    void setSplitterI() {
        if (splitter == null) {
            return;
        }
        int width = splitter.getWidth();
        int left = Math.max((int) (0.8 * width), width - 150);
        splitter.setDividerLocation(left);
    }

    @SuppressWarnings("rawtypes")
    private void initDragAndDrop() {
        try {
            vergabeliste.setDragEnabled(true);
            vergabeliste.setTransferHandler(new ListTransferHandler<T>());
            new JListDropTargetListener<T>(vergabeliste);

            tabelle.setDragEnabled(true);
            tabelle.setTransferHandler(new TableTransferHandler<T>(this, wk));
            new JTableDropTargetListener(tabelle);
        } catch (TooManyListenersException e) {
            vergabeliste.setDragEnabled(false);
            tabelle.setDragEnabled(false);
            e.printStackTrace();
        }

    }

    void schliessen() {
        setVisible(false);
    }

    @Override
    public void setVisible(boolean visible) {
        if (parent != null) {
            parent.setEnabled(!visible);
        }
        super.setVisible(visible);
        if ((!visible) && (callback != null)) {
            callback.callback(this);
        }
    }

    @SuppressWarnings("rawtypes")
    void addZeile(int offset) {
        setChanged();

        int y = tabelle.getSelectedRow() + offset;
        ((ZWTableModel) tabelle.getModel()).addRow(y);
    }

    @SuppressWarnings("rawtypes")
    void addPause(int offset) {
        int y = tabelle.getSelectedRow() + offset;
        JTimeDialog time = new JTimeDialog<T>(this, false, false);
        time.setTime(new Time(8 * 60));
        time.setVisible(true);
        Time restart = time.getTime();
        if (restart != null) {
            setChanged();
            ((ZWTableModel) tabelle.getModel()).addPause(y, restart);
        }
    }

    void editPause() {
        int y = tabelle.getSelectedRow();

        @SuppressWarnings("rawtypes")
        ZWTableModel model = (ZWTableModel) tabelle.getModel();

        int[] index = model.rowToIndex(y);

        JTimeDialog<T> time = new JTimeDialog<>(this, index[0] == 0, true);
        time.setTime(wk.getHLWListe().getStarttime(index[0]));
        time.setDuration(wk.getDoubleProperty(PropertyConstants.ZW_DURATION));
        time.setVisible(true);
        Time restart = time.getTime();
        if (restart != null) {
            setChanged();

            wk.getHLWListe().setStarttime(index[0], restart);
            Duration duration = time.getDuration();
            wk.setProperty(PropertyConstants.ZW_DURATION, duration.getTime());
            wk.getHLWListe().refreshTime();
            model.update();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void removeZeile() {
        ZWTableModel<T> model = ((ZWTableModel<T>) tabelle.getModel());
        int y = tabelle.getSelectedRow();
        if (model.isPause(y)) {
            return;
        }

        setChanged();

        ASchwimmer[] sh = model.removeRow(y);
        for (ASchwimmer aSh : sh) {
            if (aSh != null) {
                ((ZWListModel<T>) vergabeliste.getModel()).addElement(new SchwimmerZW(aSh, 1));
            }
        }
    }

    void removePause() {
        @SuppressWarnings("unchecked")
        ZWTableModel<T> model = ((ZWTableModel<T>) tabelle.getModel());
        int y = tabelle.getSelectedRow();
        if (!model.isPause(y)) {
            return;
        }
        model.removeRow(y);

        setChanged();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void hlwAendern() {
        if (!darfAendern) {
            return;
        }

        int x = tabelle.getSelectedColumn() - 2;
        int y = tabelle.getSelectedRow();

        ZWTableModel<T> model = (ZWTableModel<T>) tabelle.getModel();
        if (model.isPause(y)) {
            return;
        }

        int[] index = model.rowToIndex(y);

        LinkedList<HLWLauf<T>> ll = wk.getHLWListe().getLaufliste(index[0]);
        HLWLauf<T> temp = ll.get(index[1]);
        if (temp != null) {
            T s = temp.getSchwimmer(x);
            if (s != null) {
                T sh = (T) tabelle.getModel().getValueAt(y, x + 2);
                tabelle.getModel().setValueAt(null, y, x + 2);
                tabelle.setValueAt(temp.getAltersklasse(), y, 1);

                wk.getHLWListe().remove(index, x);
                ((ZWListModel<T>) vergabeliste.getModel()).addElement(new SchwimmerZW<>(sh, 1));

                displaySchwimmer(sh);
                if (vergabeliste.getSelectedIndex() < 0) {
                    vergabeliste.setSelectedIndex(0);
                }
            } else {
                int zahl = vergabeliste.getSelectedIndex();
                if ((zahl >= 0) && (zahl < vergabeliste.getModel().getSize())) {
                    SchwimmerZW<T> sh = (SchwimmerZW<T>) vergabeliste.getModel().getElementAt(zahl);
                    s = sh.getSchwimmer();
                    if (s != null) {
                        wk.getHLWListe().set(s, index, x);

                        tabelle.getModel().setValueAt(sh, y, x + 2);
                        ((ZWListModel) vergabeliste.getModel()).remove(zahl);
                        vergabeliste.setSelectedIndex(zahl - 1);
                        vergabeliste.setSelectedIndex(zahl);

                        displaySchwimmer(s);
                    }
                }
            }
            setChanged();

            wk.getHLWListe().refreshTime();
            tabelle.repaint();
        }
    }

    @SuppressWarnings("unchecked")
    void schwimmerDetailsTabelle() {
        int x = tabelle.getSelectedColumn() - 2;
        int y = tabelle.getSelectedRow();

        try {
            HLWListe<T> laufliste = wk.getHLWListe();
            if (laufliste == null) {
                throw new Exception();
            }

            ZWTableModel<T> model = (ZWTableModel<T>) tabelle.getModel();
            if (model.isPause(y)) {
                displaySchwimmer(null);
            } else {
                int[] index = model.rowToIndex(y);
                HLWLauf<T> temp = (y < 0 ? null : laufliste.get(index));
                if (temp == null) {
                    throw new Exception();
                }

                T s = temp.getSchwimmer(x);
                displaySchwimmer(s);
            }
        } catch (Exception e) {
            displaySchwimmer(null);
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    void schwimmerDetailsListe() {
        try {
            if (vergabeliste == null) {
                throw new Exception();
            }
            int y = vergabeliste.getSelectedIndex();
            T s = null;
            if (y >= 0) {
                s = ((SchwimmerZW<T>) vergabeliste.getModel().getElementAt(y)).getSchwimmer();
            }
            displaySchwimmer(s);
        } catch (Exception e) {
            displaySchwimmer(null);
            e.printStackTrace();
        }
    }

    private void displaySchwimmer(T s) {
        swimmer = s;
        if (s == null) {
            startnummer.setText("");
            name.setText("");
            punkte.setText("");
            bemerkung.setText("");
            ausserk.setText("");
            gliederung.setText("");
            altersklasse.setText("");
        } else {
            startnummer.setText(StartnumberFormatManager.format(s));
            name.setText(s.getName());
            gliederung.setText(s.getGliederungMitQGliederung());
            altersklasse.setText(s.getAK().toString() + " " + I18n.geschlechtToString(s));

            if (s.getMeldepunkte(meldeindex) > 0) {
                punkte.setText("" + s.getMeldepunkte(meldeindex));
            } else {
                punkte.setText("");
            }
            bemerkung.setText(s.getBemerkung());
            ausserk.setText(s.isAusserKonkurrenz() ? I18n.get("Yes") : I18n.get("No"));
        }
    }

    private void initTablePopup() {
        if (!darfAendern) {
            return;
        }

        entfernen = new JMenuItem(I18n.get("Remove"));
        entfernen.addActionListener(evt -> {
            hlwAendern();
        });

        JMenuItem addHeatAbove = new JMenuItem(I18n.get("AddHeatAbove"));
        addHeatAbove.addActionListener(evt -> {
            addZeile(0);
        });

        JMenuItem addHeatBelow = new JMenuItem(I18n.get("AddHeatBelow"));
        addHeatBelow.addActionListener(evt -> {
            addZeile(1);
        });

        removeHeat = new JMenuItem(I18n.get("RemoveHeat"));
        removeHeat.addActionListener(evt -> {
            removeZeile();
        });

        JMenuItem addPauseAbove = new JMenuItem(I18n.get("AddPauseAbove"));
        addPauseAbove.addActionListener(evt -> {
            addPause(0);
        });

        JMenuItem addPauseBelow = new JMenuItem(I18n.get("AddPauseBelow"));
        addPauseBelow.addActionListener(evt -> {
            addPause(1);
        });

        removePause = new JMenuItem(I18n.get("RemovePause"));
        removePause.addActionListener(evt -> {
            removePause();
        });

        editPause = new JMenuItem(I18n.get("EditPause"));
        editPause.addActionListener(evt -> {
            editPause();
        });

        popup = new JPopupMenu();
        popup.add(entfernen);
        popup.add(new JSeparator());
        popup.add(addHeatAbove);
        popup.add(addHeatBelow);
        popup.add(removeHeat);
        popup.add(new JSeparator());
        popup.add(addPauseAbove);
        popup.add(addPauseBelow);
        popup.add(removePause);
        popup.add(new JSeparator());
        popup.add(editPause);
    }

    private void initComponents() {
        initTablePopup();
        initWindow();
        initMenu();
        initTabelle();

        JScrollPane tabelleScroller = new JScrollPane(tabelle);
        tabelleScroller.setBorder(BorderUtils.createLabeledBorder(I18n.get("ZWList")));

        setLayout(new FormLayout("4dlu,0px:grow,fill:default,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu"));

        JPanel infopanel = createInfopanel();
        add(infopanel, CC.xyw(2, 4, 2));
        add(createButton(), CC.xy(3, 6));

        if (darfAendern) {
            initVergabeliste();

            JScrollPane vergabeScroller = new JScrollPane(vergabeliste);
            vergabeScroller.setBorder(BorderUtils.createLabeledBorder(I18n.get("Waitlist")));
            vergabeliste.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    schwimmerDetailsListe();
                }
            });

            splitter = new JInvisibleSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitter.setLeftComponent(tabelleScroller);
            splitter.setRightComponent(vergabeScroller);

            add(splitter, CC.xyw(2, 2, 2));
        } else {
            add(tabelleScroller, CC.xyw(2, 2, 2));
        }
    }

    private void initVergabeliste() {
        vergabeliste = new JList();
        vergabeliste.setDragEnabled(true);
        vergabeliste.setCellRenderer(new ComfortListCellRenderer());
        DataTipManager.get().register(vergabeliste);

        Hashtable<Integer, Integer> daten = new Hashtable<>();
        LinkedList<T> ll = wk.getSchwimmer();
        ZWListModel<T> dlm = new ZWListModel<>();

        ListIterator<T> li = ll.listIterator();
        while (li.hasNext()) {
            T s = li.next();
            if (s.getAK().hasHLW()) {
                daten.put(s.getStartnummer(), s.getMaximaleHLW());
            }
        }

        if (!wk.getHLWListe().isEmpty()) {
            ListIterator<LinkedList<HLWLauf<T>>> lli = wk.getHLWListe().getIterator();
            while (lli.hasNext()) {
                LinkedList<HLWLauf<T>> hlw = lli.next();
                ListIterator<HLWLauf<T>> lh = hlw.listIterator();
                while (lh.hasNext()) {
                    HLWLauf<T> lauf = lh.next();
                    for (int x = 0; x < lauf.getBahnen(); x++) {
                        T t = lauf.getSchwimmer(x);
                        if (t != null) {
                            int amount = daten.get(t.getStartnummer());
                            amount--;
                            daten.put(t.getStartnummer(), amount);
                        }
                    }
                }
            }
        }
        Enumeration<Integer> e = daten.keys();
        while (e.hasMoreElements()) {
            Integer sn = e.nextElement();
            int amount = daten.get(sn);
            if (amount > 0) {
                T s = SearchUtils.getSchwimmer(wk, sn);
                dlm.addElement(new SchwimmerZW<>(s, amount));
            }
        }

        vergabeliste.setModel(dlm);
        vergabeliste.setSelectedIndex(0);
        vergabeliste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void initTabelle() {
        tabelle = new JFittingTable();

        tabelle.getTableHeader().setReorderingAllowed(false);

        tabelle.setRowSelectionAllowed(false);
        tabelle.setColumnSelectionAllowed(false);
        tabelle.setCellSelectionEnabled(true);
        tabelle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabelle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                schwimmerDetailsTabelle();
            }
        });
        if (darfAendern) {
            tabelle.addMouseListener(new TableMouseListener());
        }
        ColumnFittingMouseAdapter.enable(tabelle);
        if (System.getProperty("os.name").toLowerCase().indexOf("linux") < 0) {
            DataTipManager.get().register(tabelle);
        }

        tabelle.setModel(new ZWTableModel<T>(wk));
        JTableUtils.setTableCellRenderer(tabelle, new ZWTableCellRenderer(false));
        JTableUtils.setPreferredCellSizes(tabelle);
    }

    private JButton createButton() {
        JButton close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(evt -> {
            schliessen();
        });

        return close;
    }

    private JPanel createInfopanel() {
        FormLayout layout = new FormLayout(
                "4dlu,fill:default,4dlu,fill:default:grow," + "4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setColumnGroups(new int[][] { { 2, 6 }, { 4, 8 } });

        JPanel infoPanel = new JPanel(layout);
        infoPanel.add(new JLabel(I18n.get("Startnumber") + ":"), CC.xy(2, 2));
        infoPanel.add(new JLabel(I18n.get("Name") + ":"), CC.xy(2, 4));
        infoPanel.add(new JLabel(I18n.get("Organisation") + ":"), CC.xy(2, 6));

        infoPanel.add(new JLabel(I18n.get("AgeGroup") + ":"), CC.xy(6, 2));
        infoPanel.add(new JLabel(I18n.get("AnouncedPoints") + ":"), CC.xy(6, 4));
        infoPanel.add(new JLabel(I18n.get("AusserKonkurrenz") + ":"), CC.xy(6, 6));
        infoPanel.add(startnummer, CC.xy(4, 2));
        infoPanel.add(name, CC.xy(4, 4));
        infoPanel.add(gliederung, CC.xy(4, 6));
        infoPanel.add(altersklasse, CC.xy(8, 2));
        infoPanel.add(punkte, CC.xy(8, 4));
        infoPanel.add(ausserk, CC.xy(8, 6));

        JTaskPaneGroup jt = new JTaskPaneGroup();
        jt.setUI(new GradientTaskPaneGroupUI());
        jt.setTitle(I18n.get("Information"));
        jt.add(infoPanel);

        return jt;
    }

    private void initMenu() {
        JMenuItem schliessen = new JMenuItem(I18n.get("Close"), IconManager.getSmallIcon("close"));
        schliessen.setToolTipText(I18n.get("Close"));
        schliessen.addActionListener(evt -> {
            schliessen();
        });

        JMenu datei = new JMenu(I18n.get("File"));

        datei.add(schliessen);

        JMenuBar menu = new JMenuBar();
        menu.add(datei);
        setJMenuBar(menu);
    }

    private void initWindow() {
        setTitle(I18n.get("ZWList"));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                schliessen();
            }
        });
        WindowUtils.addEscapeAction(this, this::schliessen);
    }

    public boolean isChanged() {
        return changed;
    }

    void setChanged() {
        changed = true;
    }
}