package de.df.jauswertung.gui.plugins.heats;

import java.awt.Point;

/**
 * @author Dennis Mueller
 * @since 20. Juli 2001, 15:58
 */

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.Printable;
import java.util.LinkedList;
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
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.tasks.JTaskPaneGroup;
import com.xduke.xswing.DataTipManager;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.SchwimmerDisziplin;
import de.df.jauswertung.gui.util.TableHeatUtils;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.JInvisibleSplitPane;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.jtable.ColumnFittingMouseAdapter;
import de.df.jutils.gui.jtable.JFittingTable;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.plaf.GradientTaskPaneGroupUI;
import de.df.jutils.gui.renderer.ComfortListCellRenderer;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.print.JTablePrintable;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.PrintableCreator;
import de.df.jutils.util.StringTools;

class JLauflisteBearbeiten<T extends ASchwimmer> extends JFrame {

    final class TableMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent evt) {
            Point pos = getPositionInTable(evt.getPoint());
            schwimmerDetailsTabelle(pos);
            if (evt.isPopupTrigger()) {
                int row = pos.y;
                int col = pos.x;
                if ((row > -1) && (col > -1)) {
                    tabelle.setRowSelectionInterval(row, row);
                    tabelle.setColumnSelectionInterval(col, col);
                    if (col < 3) {
                        entfernen.setEnabled(false);
                    } else {
                        entfernen.setEnabled(wk.getLaufliste().getLaufliste().get(row).getSchwimmer(col - 3) != null);
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
            if ((!evt.isPopupTrigger()) && (evt.getClickCount() == 2)) {
                lauflisteAendern();
            } else {
                mousePressed(evt);
            }
        }
    }

    private static final long                        serialVersionUID = 4050200860149757749L;

    AWettkampf<T>                                    wk               = null;
    boolean                                          darfAendern      = false;
    private Window                                   parent           = null;

    private final int                                meldeindex;

    JPopupMenu                                       popup            = null;
    private JMenuItem                                renumberHeats    = null;
    private JMenuItem                                removeHeat       = null;
    JList<SchwimmerDisziplin<T>>                     vergabeliste     = null;

    JMenuItem                                        entfernen        = null;

    JFittingTable                                    tabelle          = new JFittingTable();

    private JLabel                                   altersklasse     = new JLabel();
    private JLabel                                   gliederung       = new JLabel();
    private JLabel                                   name             = new JLabel();
    private JLabel                                   startnummer      = new JLabel();
    private JLabel                                   ausserk          = new JLabel();
    private JLabel                                   bemerkung        = new JLabel();
    private JLabel                                   punkte           = new JLabel();
    private JLabel                                   disziplin        = new JLabel();

    private ISimpleCallback<JLauflisteBearbeiten<T>> callback         = null;

    T                                                swimmer          = null;
    private JSplitPane                               splitter         = null;

    private boolean                                  changed          = false;

    public JLauflisteBearbeiten(Window parent, AWettkampf<T> wettkampf, boolean darfAendern, ISimpleCallback<JLauflisteBearbeiten<T>> callback) {
        if (wettkampf == null) {
            throw new NullPointerException("Wettkampf<T> must not be null!");
        }
        this.parent = parent;
        this.callback = callback;
        wk = wettkampf;
        this.darfAendern = darfAendern;
        setIconImages(IconManager.getTitleImages());

        meldeindex = wk.getIntegerProperty(PropertyConstants.HEATS_REGISTERED_POINTS_INDEX, 0);

        initComponents();

        if (darfAendern) {
            initDragAndDrop();
        }

        WindowUtils.setSize(this, 800, 600);
        WindowUtils.checkMinimumSize(this);
        WindowUtils.center(this, parent);
        WindowUtils.maximize(this);
        setSplitter();

        UIStateUtils.uistatemanage(this, JLauflisteBearbeiten.class.getName());
    }

    private void setSplitter() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setSplitterI();
            }
        });
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
            vergabeliste.setTransferHandler(new ListTransferHandler());
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
        dispose();
    }

    @Override
    public void setVisible(boolean visible) {
        if (parent != null) {
            parent.setEnabled(!visible);
        }
        super.setVisible(visible);
        if (popup != null) {
            removeHeat.setEnabled(wk.getLaufliste().getLaufliste().size() > 1);
        }
        if ((!visible) && (callback != null)) {
            callback.callback(this);
        }
    }

    void addZeile(int offset) {
        setChanged();

        int y = tabelle.getSelectedRow() + offset;
        @SuppressWarnings("rawtypes")
        HeatTableModel htm = (HeatTableModel) tabelle.getModel();
        htm.addRow(y);
        removeHeat.setEnabled(wk.getLaufliste().getLaufliste().size() > 1);
        JTableUtils.setPreferredRowHeight(tabelle);
    }

    @SuppressWarnings("unchecked")
    void removeZeile() {
        setChanged();

        int y = tabelle.getSelectedRow();

        HeatTableModel<T> htm = (HeatTableModel<T>) tabelle.getModel();
        SchwimmerDisziplin<T>[] sd = htm.removeRow(y);
        for (SchwimmerDisziplin<T> aSd : sd) {
            ((HeatListModel<T>) vergabeliste.getModel()).addElement(aSd);
        }
        if (tabelle.getRowCount() == 0) {
            addZeile(0);
        }
        removeHeat.setEnabled(wk.getLaufliste().getLaufliste().size() > 1);
        JTableUtils.setPreferredRowHeight(tabelle);
    }

    void renumberHeats() {
        setChanged();

        int y = tabelle.getSelectedRow();

        int min = 1;

        if (y > 0) {
            LinkedList<Lauf<T>> ll = wk.getLaufliste().getLaufliste();
            Lauf<T> lauf = ll.get(y - 1);
            min = lauf.getLaufnummer() + 1;
        }

        int wert = DialogUtils.askForNumber(this, I18n.get("EnterHeatnumber"), I18n.get("Information.EnterHeatnumber", min), min, min + 1000);
        if (wert >= min) {
            wk.getLaufliste().neueNummerierung(y, wert);
            JTableUtils.setPreferredRowHeight(tabelle);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void lauflisteAendern() {
        setChanged();

        int x = tabelle.getSelectedColumn() - 3;
        int y = tabelle.getSelectedRow();

        LinkedList<Lauf<T>> ll = wk.getLaufliste().getLaufliste();
        Lauf<T> temp = ll.get(y);
        T s = temp.getSchwimmer(x);
        if (s != null) {
            SchwimmerDisziplin<T> sd = (SchwimmerDisziplin<T>) tabelle.getModel().getValueAt(y, x + 3);
            tabelle.getModel().setValueAt(new SchwimmerDisziplin<T>(), y, x + 3);
            ((HeatListModel<T>) vergabeliste.getModel()).addElement(sd);
            if (vergabeliste.getSelectedIndex() < 0) {
                vergabeliste.setSelectedIndex(0);
            }
        } else {
            int zahl = vergabeliste.getSelectedIndex();
            if ((zahl >= 0) && (zahl < vergabeliste.getModel().getSize())) {
                SchwimmerDisziplin<T> sd = vergabeliste.getModel().getElementAt(zahl);
                s = sd.getSchwimmer();
                if (s != null) {
                    tabelle.getModel().setValueAt(sd, y, x + 3);
                    displaySchwimmer(sd.getSchwimmer(), sd.getDiscipline());
                }
                ((HeatListModel) vergabeliste.getModel()).remove(zahl);
                vergabeliste.setSelectedIndex(zahl - 1);
                vergabeliste.setSelectedIndex(zahl);
            }
        }
        vergabeliste.updateUI();
        JTableUtils.setPreferredRowHeight(tabelle);
    }

    Point getPositionInTable(Point xy) {
        if (xy == null) {
            return null;
        }
        int row = tabelle.rowAtPoint(xy);
        int col = tabelle.columnAtPoint(xy);
        return new Point(col, row);
    }

    void schwimmerDetailsTabelle(Point p) {
        int x = tabelle.getSelectedColumn() - 3;
        int y = tabelle.getSelectedRow();
        if (p != null) {
            x = p.x - 3;
            y = p.y;
        }

        try {
            Laufliste<T> laufliste = wk.getLaufliste();
            if (laufliste == null) {
                throw new Exception();
            }

            Lauf<T> temp = laufliste.getLaufliste().get(y);
            if (temp == null) {
                throw new Exception();
            }

            T s = temp.getSchwimmer(x);
            displaySchwimmer(s, temp.getDisznummer(x));
        } catch (Exception e) {
            displaySchwimmer(null, 0);
            e.printStackTrace();
        }
    }

    int getPositionInList(Point xy) {
        int row = vergabeliste.locationToIndex(xy);
        return row;
    }

    void schwimmerDetailsListe(int row) {
        try {
            if (vergabeliste == null) {
                throw new Exception();
            }
            int y = row >= 0 ? row : vergabeliste.getSelectedIndex();
            SchwimmerDisziplin<T> sd = ((HeatListModel<T>) vergabeliste.getModel()).getElementAt(y);
            T s = sd.getSchwimmer();
            displaySchwimmer(s, sd.getDiscipline());
        } catch (Exception e) {
            displaySchwimmer(null, 0);
            e.printStackTrace();
        }
    }

    private void displaySchwimmer(T s, int disz) {
        swimmer = s;
        if (s == null) {
            startnummer.setText("");
            name.setText("");
            punkte.setText("");
            bemerkung.setText("");
            ausserk.setText("");
            gliederung.setText("");
            altersklasse.setText("");
            disziplin.setText("");
        } else {
            startnummer.setText(StartnumberFormatManager.format(s));
            name.setText(s.getName());
            gliederung.setText(s.getGliederungMitQGliederung());
            altersklasse.setText(s.getAK().toString() + " " + I18n.geschlechtToString(s)
                    + (s.getAK().getStartgruppe() == null ? "" : " / Startgruppe: " + s.getAK().getStartgruppe()) + (s.isAusserKonkurrenz() ? " (a.K.)" : ""));

            punkte.setText(StringTools.punkteString(s.getMeldepunkte(meldeindex)));
            bemerkung.setText(s.getBemerkung());
            ausserk.setText(StringTools.zeitString(s.getMeldezeit(disz)));
            disziplin.setText(s.getAK().getDisziplin(disz, s.isMaennlich()).getName());
        }
    }

    void removeEmptyHeats() {
        int old = wk.getLaufliste().getLaufliste().size();
        wk.getLaufliste().removeEmptyHeats();

        if (old != wk.getLaufliste().getLaufliste().size()) {
            setChanged();

            tabelle.setModel(new HeatTableModel<T>(wk));
            removeHeat.setEnabled(wk.getLaufliste().getLaufliste().size() > 1);

            EDTUtils.executeOnEDT(new Runnable() {
                @Override
                public void run() {
                    JTableUtils.setPreferredRowHeight(tabelle);
                    JTableUtils.setPreferredCellWidths(tabelle);
                }
            });
        }
    }

    void nummerierungErneuern() {

        boolean change = wk.getLaufliste().neueNummerierung();

        if (change) {
            setChanged();

            @SuppressWarnings("unchecked")
            HeatTableModel<T> htm = (HeatTableModel<T>) tabelle.getModel();
            htm.updateNumbers();
            EDTUtils.executeOnEDT(new Runnable() {
                @Override
                public void run() {
                    JTableUtils.setPreferredCellWidths(tabelle);
                }
            });
        }
    }

    Printable getPrintable() {
        JTable table = TableHeatUtils.getLaufliste(wk, PrintUtils.printEmptyLanes);
        Printable p = PrintManager.getPrintable(table, (String) null, JTablePrintable.OPT_ALL, true, true);
        return PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), I18n.get("Laufliste"), I18n.get("Laufliste"));
    }

    class PPrintableCreator implements PrintableCreator {

        @Override
        public Printable create() {
            return getPrintable();
        }
    }

    void drucken() {
        PrintManager.print(getPrintable(), I18n.get("Laufliste"), true, this);
    }

    void vorschau() {
        PrintManager.preview(this, new PPrintableCreator(), I18n.get("Laufliste"), IconManager.getIconBundle(), IconManager.getTitleImages());
    }

    private void initMenu() {
        JMenuItem drucken = new JMenuItem(I18n.get("Print"), IconManager.getSmallIcon("print"));
        drucken.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                drucken();
            }
        });

        JMenuItem vorschau = new JMenuItem(I18n.get("Preview"), IconManager.getSmallIcon("preview"));
        vorschau.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                vorschau();
            }
        });

        JMenuItem schliessen = new JMenuItem(I18n.get("Close"), IconManager.getSmallIcon("close"));
        schliessen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                schliessen();
            }
        });

        JMenu datei = new JMenu(I18n.get("File"));
        if (darfAendern) {
            JMenuItem neueNummerierung = new JMenuItem("Laufnummerierung zur\u00fccksetzen");
            neueNummerierung.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    nummerierungErneuern();
                }
            });

            JMenuItem emptyHeats = new JMenuItem(I18n.get("RemoveEmptyHeats"));
            emptyHeats.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    removeEmptyHeats();
                }
            });

            datei.add(neueNummerierung);
            datei.add(emptyHeats);
            datei.add(new JSeparator());
        }
        datei.add(vorschau);
        datei.add(drucken);
        datei.add(new JSeparator());
        datei.add(schliessen);

        JMenuBar menu = new JMenuBar();
        menu.add(datei);

        setJMenuBar(menu);
    }

    private void initPopup() {
        if (!darfAendern) {
            // Popup will never be shown in this mode
            return;
        }
        entfernen = new JMenuItem(I18n.get("Remove"));
        entfernen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                lauflisteAendern();
            }
        });

        JMenuItem zeileNeu = new JMenuItem(I18n.get("AddHeatAbove"));
        zeileNeu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addZeile(0);
            }
        });

        JMenuItem zeileNeuNach = new JMenuItem(I18n.get("AddHeatBelow"));
        zeileNeuNach.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addZeile(1);
            }
        });
        removeHeat = new JMenuItem(I18n.get("RemoveHeat"));
        removeHeat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                removeZeile();
            }
        });
        renumberHeats = new JMenuItem(I18n.get("RenumberHeatsFromHere"));
        renumberHeats.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                renumberHeats();
            }
        });

        popup = new JPopupMenu();
        popup.add(entfernen);
        popup.add(new JSeparator());
        popup.add(zeileNeu);
        popup.add(zeileNeuNach);
        popup.add(removeHeat);
        popup.add(new JSeparator());
        popup.add(renumberHeats);
    }

    private JPanel createInfopanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow," + "4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu," + "fill:default,4dlu");
        layout.setColumnGroups(new int[][] { { 2, 6 }, { 4, 8 } });

        JPanel infoPanel = new JPanel(layout);
        infoPanel.add(new JLabel(I18n.get("Startnumber") + ":"), CC.xy(2, 2));
        infoPanel.add(new JLabel(I18n.get("Name") + ":"), CC.xy(2, 4));
        infoPanel.add(new JLabel(I18n.get("Organisation") + ":"), CC.xy(2, 6));
        infoPanel.add(new JLabel(I18n.get("Comment") + ":"), CC.xy(2, 8));

        infoPanel.add(new JLabel(I18n.get("AgeGroup") + ":"), CC.xy(6, 2));
        infoPanel.add(new JLabel(I18n.get("AnouncedPoints") + ":"), CC.xy(6, 4));
        infoPanel.add(new JLabel(I18n.get("Meldezeit") + ":"), CC.xy(6, 6));
        infoPanel.add(new JLabel(I18n.get("Discipline") + ":"), CC.xy(6, 8));

        infoPanel.add(startnummer, CC.xy(4, 2));
        infoPanel.add(name, CC.xy(4, 4));
        infoPanel.add(gliederung, CC.xy(4, 6));
        infoPanel.add(altersklasse, CC.xy(8, 2));
        infoPanel.add(punkte, CC.xy(8, 4));
        infoPanel.add(ausserk, CC.xy(8, 6));
        infoPanel.add(bemerkung, CC.xy(4, 8));
        infoPanel.add(disziplin, CC.xy(8, 8));

        JTaskPaneGroup jt = new JTaskPaneGroup();
        jt.setUI(new GradientTaskPaneGroupUI());
        jt.setTitle(I18n.get("Information"));
        jt.add(infoPanel);

        return jt;
    }

    private JButton createCloseButton() {
        JButton ok = new JButton();
        ok.setIcon(IconManager.getSmallIcon("close"));
        ok.setText(I18n.get("Close"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                schliessen();
            }
        });
        return ok;
    }

    private void initFrame() {
        setTitle(I18n.get("Laufliste"));
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        WindowUtils.addEscapeAction(this, new Runnable() {
            @Override
            public void run() {
                schliessen();
            }
        });
    }

    private void initComponents() {
        initMenu();
        initPopup();
        initFrame();
        initTabelle();

        JScrollPane scroller = new JScrollPane(tabelle);
        scroller.setBorder(BorderUtils.createLabeledBorder(I18n.get("Laufliste")));

        setLayout(new FormLayout("4dlu,0px:grow,fill:default,4dlu", "4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu"));

        JPanel infopanel = createInfopanel();
        add(infopanel, CC.xyw(2, 4, 2));
        add(createCloseButton(), CC.xy(3, 6));

        if (darfAendern) {
            tabelle.addMouseListener(new TableMouseListener());

            initVergabeliste();
            JScrollPane scroller2 = new JScrollPane(vergabeliste);
            scroller2.setBorder(BorderUtils.createLabeledBorder(I18n.get("Waitlist")));

            splitter = new JInvisibleSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitter.setLeftComponent(scroller);
            splitter.setRightComponent(scroller2);
            splitter.setDividerLocation(0.9);

            add(splitter, CC.xyw(2, 2, 2));
        } else {
            add(scroller, CC.xyw(2, 2, 2));
        }
    }

    private void initVergabeliste() {
        vergabeliste = new JList<SchwimmerDisziplin<T>>(new HeatListModel<T>(wk));
        vergabeliste.setCellRenderer(new ComfortListCellRenderer());
        vergabeliste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // DataTipManager.get().register(vergabeliste);

        vergabeliste.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                schwimmerDetailsListe(getPositionInList(evt.getPoint()));
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                // mouseClicked(evt);
            }
        });
        vergabeliste.setSelectedIndex(0);
    }

    private void initTabelle() {
        tabelle.getTableHeader().setReorderingAllowed(false);

        tabelle.setRowSelectionAllowed(false);
        tabelle.setColumnSelectionAllowed(false);
        tabelle.setCellSelectionEnabled(true);
        tabelle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelle.setModel(new HeatTableModel<T>(wk));
        JTableUtils.setAlignmentRenderer(tabelle, new int[] { SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.LEFT },
                Utils.getPreferences().getBoolean("HHListLeftAlign", false) ? SwingConstants.LEFT : SwingConstants.CENTER);
        JTableUtils.setAlternatingTableCellRenderer(tabelle);
        JTableUtils.setPreferredCellWidths(tabelle);
        JTableUtils.setPreferredRowHeight(tabelle);

        tabelle.setShowGrid(true);

        ColumnFittingMouseAdapter.enable(tabelle);
        DataTipManager.get().register(tabelle);
    }

    public boolean isChanged() {
        return changed;
    }

    void setChanged() {
        changed = true;
    }
}