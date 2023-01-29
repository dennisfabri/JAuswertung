package de.df.jauswertung.gui.penalties;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.xduke.xswing.DataTipManager;

import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.daten.regelwerk.StrafenKapitel;
import de.df.jauswertung.daten.regelwerk.StrafenParagraph;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.PenaltyImageListCellRenderer;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.jlist.JListUtils;

/**
 * @author Dennis Fabri
 */
class JStrafenkatalogPanel extends JPanel {

    private static final long serialVersionUID = -3908181088972213538L;

    private DefaultMutableTreeNode root = new DefaultMutableTreeNode();

    private JTree kategorien = new JTree(root);

    private JList<Strafe> strafen = new JList<>();
    private JLabel name = new JLabel();
    private JLabel paragraph = new JLabel();
    private JLabel strafpunkte = new JLabel();
    private Strafen strafenkatalog = null;
    private Strafe strafe = null;

    public JStrafenkatalogPanel(Strafe str, Strafen s) {
        strafenkatalog = s;
        init(str);
    }

    // ***********************************************************************

    private void init(Strafe str) {
        boolean aktiv = (str != null);
        refresh();

        kategorien.setRootVisible(false);
        kategorien.setExpandsSelectedPaths(true);

        strafen.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        kategorien.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        strafen.setCellRenderer(new PenaltyImageListCellRenderer());
        JListUtils.setAlternatingListCellRenderer(strafen);

        JSplitPane horizontalSplitter = new JSplitPane();

        addDataTips();

        // Dynamische Konfiguration der GUI
        JPanel buttonPanel = createButtons(aktiv);
        // ScrollPane einrichten
        JScrollPane kategorienScroller = new JScrollPane(kategorien);
        kategorienScroller.setBorder(BorderUtils.createLineLabeledBorder(I18n.get("Chapter")));
        kategorienScroller.setMinimumSize(new Dimension(200, 300));
        JScrollPane strafenScroller = new JScrollPane(strafen);
        strafenScroller.setBorder(BorderUtils.createLineLabeledBorder(I18n.get("PenaltyCatalog")));
        strafenScroller.setMinimumSize(new Dimension(200, 300));

        horizontalSplitter.setDividerSize(5);
        horizontalSplitter.setResizeWeight(0.2);
        horizontalSplitter.setBorder(null);
        horizontalSplitter.setLeftComponent(kategorienScroller);
        horizontalSplitter.setRightComponent(strafenScroller);

        // infoPanel zusammensetzen
        JPanel infoPanel = new JPanel();
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu");
        layout.setRowGroups(new int[][] { { 2, 4, 6 } });
        infoPanel.setLayout(layout);
        infoPanel.add(new JLabel(I18n.get("Description") + ":"), CC.xy(2, 2));
        infoPanel.add(name, CC.xy(4, 2));
        infoPanel.add(new JLabel(I18n.get("Paragraph") + ":"), CC.xy(2, 4));
        infoPanel.add(paragraph, CC.xy(4, 4));
        infoPanel.add(new JLabel(I18n.get("Penalty") + ":"), CC.xy(2, 6));
        infoPanel.add(strafpunkte, CC.xy(4, 6));
        infoPanel.setBorder(BorderUtils.createLabeledBorder(I18n.get("Details")));

        // Panel zusammensetzen
        layout = new FormLayout("4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu,fill:default,4dlu");
        setLayout(layout);
        add(horizontalSplitter, CC.xy(2, 2));
        add(infoPanel, CC.xy(2, 4));
        add(buttonPanel, CC.xy(2, 6));

        // Listener hinzufügen
        initListeners(aktiv);
        refresh();
        if (str != null) {
            sucheStrafe(str);
        }
    }

    private JPanel createButtons(boolean aktiv) {
        // ButtonPanel einrichten
        JButton ok;
        if (aktiv) {
            ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        } else {
            ok = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        }
        ok.addActionListener(arg0 -> {
            doOk();
        });
        JButton abbrechen = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        abbrechen.setVisible(aktiv);
        abbrechen.addActionListener(evt -> {
            doCancel();
        });

        FormLayout buttonLayout;
        if (aktiv) {
            buttonLayout = new FormLayout("0px:grow,fill:default,4dlu,fill:default", "fill:default");
            buttonLayout.setColumnGroups(new int[][] { { 2, 4 } });
        } else {
            buttonLayout = new FormLayout("0px:grow,fill:default", "fill:default");
        }
        JPanel buttonPanel = new JPanel(buttonLayout);
        if (aktiv) {
            buttonPanel.add(abbrechen, CC.xy(4, 1));
        }
        buttonPanel.add(ok, CC.xy(2, 1));
        return buttonPanel;
    }

    private void addDataTips() {
        // Listen optimieren
        DataTipManager.get().register(kategorien);
        DataTipManager.get().register(strafen);
    }

    private void sucheStrafe(Strafe s) {
        if (s == null) {
            return;
        }
        if (strafenkatalog == null) {
            return;
        }
        s = simplifyPenalty(s);
        LinkedList<StrafenKapitel> skll = strafenkatalog.getKapitel();
        if (skll == null) {
            return;
        }
        strafe = s;
        if (selectStrafe(s, skll)) {
            return;
        }
        if (skll.size() > 0) {
            StrafenKapitel sk = skll.getLast();
            if (sk != null) {
                LinkedList<StrafenParagraph> spll = sk.getParagraphen();
                if ((spll != null) && (spll.size() > 0)) {
                    StrafenParagraph sp = spll.getLast();
                    if (sp != null) {
                        sp.addStrafe(s);

                        TreeNode c = root.getChildAt(root.getChildCount() - 1);
                        TreeNode p = c.getChildAt(0);

                        TreePath tp = new TreePath(new Object[] { root, c, p });
                        kategorien.setSelectionPath(tp);

                        strafen.setSelectedIndex(strafen.getModel().getSize() - 1);
                    }
                }
            }
        }
    }

    private static Strafe simplifyPenalty(Strafe s) {
        if (s == null) {
            s = Strafe.NICHTS;
        }
        if (s.equals(new Strafe("", "", Strafarten.STRAFPUNKTE, 0))) {
            s = Strafe.NICHTS;
        }
        if (s.equals(new Strafe("", "", Strafarten.NICHT_ANGETRETEN, 0))) {
            s = Strafe.NICHT_ANGETRETEN;
        }
        if (s.equals(new Strafe("", "", Strafarten.DISQUALIFIKATION, 0))) {
            s = Strafe.DISQUALIFIKATION;
        }
        if (s.equals(new Strafe("", "", Strafarten.AUSSCHLUSS, 0))) {
            s = Strafe.AUSSCHLUSS;
        }
        return s;
    }

    /**
     * @param s
     * @param skll
     */
    private boolean selectStrafe(Strafe s, LinkedList<StrafenKapitel> skll) {
        ListIterator<StrafenKapitel> skli = skll.listIterator();
        while (skli.hasNext()) {
            int ski = skli.nextIndex();
            StrafenKapitel sk = skli.next();
            if (sk != null) {
                LinkedList<StrafenParagraph> spll = sk.getParagraphen();
                if (spll != null) {
                    ListIterator<StrafenParagraph> spli = spll.listIterator();
                    while (spli.hasNext()) {
                        int spi = spli.nextIndex();
                        StrafenParagraph sp = spli.next();
                        if (selectStrafe(s, ski, spi, sp)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param s
     * @param ki
     * @param pi
     * @param sp
     */
    private boolean selectStrafe(Strafe s, int ki, int pi, StrafenParagraph sp) {
        if (sp != null) {
            LinkedList<Strafe> sll = sp.getStrafen();
            if (sll != null) {
                ListIterator<Strafe> sli = sll.listIterator();
                while (sli.hasNext()) {
                    int si = sli.nextIndex();
                    Strafe jetzt = sli.next();
                    if (s.equals(jetzt)) {
                        TreeNode c = root.getChildAt(ki);
                        TreeNode p = c.getChildAt(pi);

                        TreePath tp = new TreePath(new Object[] { root, c, p });
                        kategorien.setSelectionPath(tp);

                        strafen.setSelectedIndex(si);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ***********************************************************************
    private void setStrafe() {
        int[] indizes = getSelectedPath();

        LinkedList<StrafenKapitel> lk = strafenkatalog.getKapitel();
        LinkedList<StrafenParagraph> lp = lk.get(Math.max(0, indizes[0])).getParagraphen();
        LinkedList<Strafe> ls = lp.get(Math.max(0, indizes[1])).getStrafen();
        strafe = ls.get(Math.max(0, strafen.getSelectedIndex()));
    }

    public Strafe getStrafe() {
        return strafe;
    }

    private void refresh() {
        refreshKapitel();
        refreshStrafen();
        refreshDisplay();
    }

    private void refreshKapitel() {
        root.removeAllChildren();

        LinkedList<StrafenKapitel> ll = strafenkatalog.getKapitel();
        ListIterator<StrafenKapitel> li = ll.listIterator();
        while (li.hasNext()) {
            StrafenKapitel k = li.next();
            DefaultMutableTreeNode kapitel = new DefaultMutableTreeNode(k.getName());
            LinkedList<StrafenParagraph> para = k.getParagraphen();
            ListIterator<StrafenParagraph> lx = para.listIterator();
            while (lx.hasNext()) {
                DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(lx.next().getName());
                kapitel.add(dmtn);
            }
            root.add(kapitel);
        }

        ((DefaultTreeModel) kategorien.getModel()).setRoot(root);
        expandAll();
    }

    private void expandAll() {
        int row = 0;
        while (row < kategorien.getRowCount()) {
            kategorien.expandRow(row);
            row++;
        }
    }

    public int[] getSelectedPath() {
        TreePath tp = kategorien.getSelectionPath();
        if (tp == null) {
            return new int[] { 0, 0 };
        }
        TreeNode t1 = (TreeNode) tp.getPathComponent(1);
        int ki = root.getIndex(t1);
        if (tp.getPathCount() < 3) {
            return new int[] { ki, 0 };
        }
        TreeNode t2 = (TreeNode) tp.getPathComponent(2);
        if (t2 == null) {
            return new int[] { ki, 0 };
        }
        int pi = t1.getIndex(t2);
        return new int[] { ki, pi };
    }

    private void refreshStrafen() {
        LinkedList<StrafenKapitel> lk = strafenkatalog.getKapitel();

        int[] indizes = getSelectedPath();

        LinkedList<StrafenParagraph> lp = lk.get(indizes[0]).getParagraphen();
        LinkedList<Strafe> ls = lp.get(indizes[1]).getStrafen();
        Strafe[] daten = new Strafe[ls.size()];
        ListIterator<Strafe> li = ls.listIterator();
        if (li.hasNext()) {
            int x = 0;
            do {
                daten[x] = li.next();
                x++;
            } while (li.hasNext());
        }
        strafen.setListData(daten);
        strafen.setSelectedIndex(0);
    }

    private void refreshDisplay() {
        setStrafe();
        if (strafe == null) {
            return;
        }
        name.setText(strafe.getName());
        paragraph.setText(strafe.getShortname());
        strafpunkte.setText(I18n.get("NoPenalty"));
        if (strafe.getArt() == Strafarten.STRAFPUNKTE) {
            strafpunkte.setText(I18n.get("PointsPenalty", strafe.getStrafpunkte()));
        }
        if (strafe.getArt() == Strafarten.DISQUALIFIKATION) {
            strafpunkte.setText(I18n.get("Disqualification"));
        }
        if (strafe.getArt() == Strafarten.AUSSCHLUSS) {
            strafpunkte.setText(I18n.get("Debarment"));
        }
        if (strafe.getArt() == Strafarten.NICHT_ANGETRETEN) {
            strafpunkte.setText(I18n.get("DidNotStart"));
        }
    }

    private void beenden() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w.isVisible()) {
            w.setVisible(false);
        }
    }

    private void initListeners(boolean mayselect) {
        kategorien.addTreeSelectionListener(e -> {
            paragraphSelected();
        });
        strafen.addListSelectionListener(evt -> {
            strafeSelected();
        });
        if (mayselect) {
            strafen.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.isConsumed()) {
                        return;
                    }
                    if (e.getClickCount() >= 2) {
                        doOk();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    // mouseReleased(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    // nothing
                }
            });
        }
    }

    void doOk() {
        setStrafe();
        beenden();
    }

    void doCancel() {
        strafe = null;
        beenden();
    }

    void kapitelSelected() {
        refreshStrafen();
        refreshDisplay();
    }

    void paragraphSelected() {
        refreshStrafen();
        refreshDisplay();
    }

    void strafeSelected() {
        refreshDisplay();
    }
}