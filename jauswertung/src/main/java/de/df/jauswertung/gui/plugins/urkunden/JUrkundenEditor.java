/*
 * Created on 17.03.2007
 */
package de.df.jauswertung.gui.plugins.urkunden;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.FileFilters;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.util.GraphUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.border.ShadowBorder;
import de.df.jutils.gui.layout.CenterLayout;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.print.PageSetup;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.api.PrintableCreator;
import de.df.jutils.print.elements.RulerBorder;

public class JUrkundenEditor<T extends ASchwimmer> extends JFrame {

    private static final long serialVersionUID = -523804656941976436L;

    private JFrame parent;
    private AWettkampf<T> wk;
    private IPluginManager controller;

    mxGraph graph;
    mxGraphComponent jgraph;
    JPanel inner;

    Point clicked;

    JPopupMenu menu;
    JMenuItem font;
    JMenuItem delete;
    JMenu alignment;
    private JMenuItem alignleft;
    private JMenuItem alignright;
    private JMenuItem aligncenter;

    private JButton left;
    private JButton center;
    private JButton right;
    private JButton fontbutton;

    JToggleButton add;
    JPanel graphcontainer;

    Font lastfont = PrintManager.getFont();

    private boolean grid = true;

    JDialog help;
    JToggleButton helper;

    private final boolean einzelwertung;

    public JUrkundenEditor(JFrame parent, AWettkampf<T> wk, IPluginManager pm, boolean einzelwertung) {
        super(I18n.get("Documenteditor"));
        setIconImage(parent.getIconImage());

        this.parent = parent;
        this.wk = wk;
        this.controller = pm;
        this.einzelwertung = einzelwertung;

        grid = Utils.getPreferences().getBoolean("GridEnabled", true);

        init();

        pack();
        WindowUtils.setSize(this, 800, 600);
        WindowUtils.center(this, parent);
        UIStateUtils.uistatemanage(this, I18n.get("Documenteditor"));
    }

    @SuppressWarnings({ "unchecked" })
    private void init() {
        graph = GraphUtils.createGraph(false);
        jgraph = GraphUtils.createDisplay(graph, false);

        mxGraphComponent.mxGraphControl gc = jgraph.getGraphControl();

        gc.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_DELETE)) {
                    removeSelectedFields();
                }
            }
        });
        gc.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (add.isSelected()) {
                        clicked = new Point(e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isConsumed()) {
                    return;
                }
                if (e.isPopupTrigger()) {
                    menu.show(jgraph, e.getX(), e.getY());
                }
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (add.isSelected()) {
                        GraphUtils.addTextfield(graph, clicked.getX(), clicked.getY(), e.getX() - clicked.getX(),
                                e.getY() - clicked.getY(), null, true);
                        add.setSelected(false);
                    }
                }
                clicked = null;
            }
        });

        String key = PropertyConstants.URKUNDE;
        if (einzelwertung) {
            key = PropertyConstants.URKUNDE_EINZELWERTUNG;
        }
        GraphUtils.populateGraph(graph, (Hashtable<String, Object>[]) wk.getProperty(key), true, false);

        inner = new JPanel(new BorderLayout());
        inner.add(jgraph, BorderLayout.CENTER);
        inner.setBorder(new CompoundBorder(new ShadowBorder(), new RulerBorder()));

        graphcontainer = new JPanel(new CenterLayout());
        graphcontainer.add(inner);
        add(new JScrollPane(graphcontainer));
        updateSize();

        initButtons();
        initMenu();
        initPopup();
        initHelp();

        updateGrid(grid);
    }

    private void initHelp() {
        help = new JDialog(this, I18n.get("Help"), false) {

            private static final long serialVersionUID = 8645811343569575779L;

            @Override
            public void setVisible(boolean b) {
                Utils.getPreferences().putBoolean("DisplayDocumentsHelp", b);
                if (helper.isSelected() != b) {
                    helper.setSelected(b);
                }
                super.setVisible(b);
            }
        };
        JLabel label = new JLabel(I18n.get("Documents.Help"));
        label.setBorder(new EmptyBorder(5, 5, 5, 5));

        help.add(label);
        help.pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        help.setLocation(d.width - help.getWidth() - 50, (d.height - help.getHeight()) / 2);

        UIStateUtils.uistatemanage(help, "JUrkundenEditor.Help");
        help.pack();
        help.setResizable(false);
    }

    private void initPopup() {
        delete = new JMenuItem(I18n.get("Remove"));
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                removeSelectedFields();
            }
        });

        font = new JMenuItem(I18n.get("Font"));
        font.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFontDialog jfd = new JFontDialog(JUrkundenEditor.this);
                jfd.setSelectedFont(lastfont);
                jfd.setVisible(true);
                if (jfd.getSelectedFont() != null) {
                    setCellFont(jfd.getSelectedFont());
                }
            }
        });

        alignleft = new JMenuItem(I18n.get("Leftalign"), IconManager.getSmallIcon("leftalign"));
        alignleft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAlignment(SwingConstants.LEFT);
            }
        });
        aligncenter = new JMenuItem(I18n.get("Centeralign"), IconManager.getSmallIcon("centeralign"));
        aligncenter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAlignment(SwingConstants.CENTER);
            }
        });
        alignright = new JMenuItem(I18n.get("Rightalign"), IconManager.getSmallIcon("rightalign"));
        alignright.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAlignment(SwingConstants.RIGHT);
            }
        });

        alignment = new JMenu(I18n.get("Alignment"));
        alignment.add(alignleft);
        alignment.add(aligncenter);
        alignment.add(alignright);

        menu = new JPopupMenu() {
            private static final long serialVersionUID = -7894428422908256188L;

            @Override
            public void setVisible(boolean b) {
                if (b) {
                    boolean e = !graph.isSelectionEmpty();
                    font.setEnabled(e);
                    alignment.setEnabled(e);
                }
                super.setVisible(b);
            }
        };

        menu.add(delete);
        menu.add(new JSeparator());
        menu.add(font);
        menu.add(alignment);
    }

    private void initButtons() {
        JToolBar tb = new JToolBar();
        tb.setLayout(new FlowLayout(FlowLayout.LEFT));
        tb.setBorder(new EmptyBorder(new Insets(1, 5, 1, 5)));
        tb.setFloatable(false);
        add(tb, BorderLayout.NORTH);

        add = new JToggleButton(IconManager.getSmallIcon("newtextfield"));
        add.setToolTipText(I18n.getToolTip("DocumentNewTextfield"));

        JToggleButton gridButton = new JToggleButton(IconManager.getSmallIcon("grid"));
        gridButton.setToolTipText(I18n.getToolTip("SetGrid"));
        gridButton.setSelected(grid);
        gridButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JToggleButton b = (JToggleButton) e.getSource();
                updateGrid(b.isSelected());
            }
        });

        helper = new JToggleButton(I18n.get("Help"));
        helper.setSelected(true);
        helper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                help.setVisible(helper.isSelected());
            }
        });

        left = new JButton(IconManager.getSmallIcon("leftalign"));
        left.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAlignment(SwingConstants.LEFT);
            }
        });
        center = new JButton(IconManager.getSmallIcon("centeralign"));
        center.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAlignment(SwingConstants.CENTER);
            }
        });
        right = new JButton(IconManager.getSmallIcon("rightalign"));
        right.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAlignment(SwingConstants.RIGHT);
            }
        });

        fontbutton = new JButton(IconManager.getSmallIcon("fonts"));
        fontbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!cellsSelected()) {
                    return;
                }
                JFontDialog jfd = new JFontDialog(JUrkundenEditor.this);
                jfd.setSelectedFont(lastfont);
                jfd.setVisible(true);
                if (jfd.getSelectedFont() != null) {
                    setCellFont(jfd.getSelectedFont());
                }
            }
        });

        tb.add(add);
        tb.addSeparator();
        tb.add(left);
        tb.add(center);
        tb.add(right);
        tb.addSeparator();
        tb.add(fontbutton);
        tb.addSeparator();
        tb.add(gridButton);
        tb.addSeparator();
        tb.add(helper);
    }

    private static class DocumentsCreator implements PrintableCreator {

        private Hashtable<String, Object>[] cells;

        public DocumentsCreator(Hashtable<String, Object>[] cells) {
            this.cells = cells;
        }

        @Override
        public Printable create() {
            return GraphUtils.getDummyPrintable(cells);
        }
    }

    private SimpleFileFilter FILTER = FileFilters.FF_DOCUMENT;

    @SuppressWarnings("unchecked")
    void laden() {
        String name = FileChooserUtils.openFile(this, FILTER);
        if (name != null) {
            Hashtable<String, Object>[] neu = (Hashtable<String, Object>[]) InputManager.ladeObject(name);
            if (neu == null) {
                String meldung = I18n.get("OpenFailed", name);
                String note = I18n.get("OpenFailed.Note", name);
                DialogUtils.warn(this, meldung, note);
            } else {
                graph.getModel().beginUpdate();
                GraphUtils.clear(graph);
                GraphUtils.populateGraph(graph, neu, true, false);
                graph.getModel().endUpdate();
            }
        }
    }

    void speichern() {
        String name = FileChooserUtils.saveFile(controller.getWindow(), FILTER);
        if (name != null) {
            boolean result = true;
            if (new File(name).exists()) {
                result = DialogUtils.ask(this, I18n.get("OverwriteFileQuestion", name),
                        I18n.get("OverwriteFileQuestion.Note", name));
            }
            if (result) {
                result = OutputManager.speichereObject(name, GraphUtils.collectGraph(graph));
                if (!result) {
                    String meldung = I18n.get("SaveFailedText", name);
                    String note = I18n.get("SaveFailedText.Note", name);
                    DialogUtils.warn(this, meldung, note);
                }
            }
        }
    }

    private void initMenu() {
        JMenuItem setup = new JMenuItem(I18n.get("Pagesetup"));
        setup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean b = PageSetup.show(I18n.get("Document"));
                if (b) {
                    updateSize();
                }
            }
        });

        JMenuItem preview = new JMenuItem(I18n.get("Preview"));
        preview.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PrintExecutor.preview(JUrkundenEditor.this, createGraphDocument(),
                        I18n.get("Document"), IconManager.getIconBundle(), IconManager.getTitleImages());
            }

            private DocumentsCreator createGraphDocument() {
                return new DocumentsCreator(GraphUtils.collectGraph(graph));
            }
        });

        JMenuItem close = new JMenuItem(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        JMenuItem neu = new JMenuItem(I18n.get("New"), IconManager.getSmallIcon("new"));
        neu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphUtils.clear(graph);
            }
        });

        JMenuItem load = new JMenuItem(I18n.get("Open"), IconManager.getSmallIcon("openfile"));
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                laden();
            }
        });
        JMenuItem save = new JMenuItem(I18n.get("Save"), IconManager.getSmallIcon("saveasfile"));
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speichern();
            }
        });

        JMenu file = new JMenu(I18n.get("File"));
        file.add(neu);
        file.add(load);
        file.add(save);
        file.add(new JSeparator());
        file.add(setup);
        file.add(preview);
        file.add(new JSeparator());
        file.add(close);

        JMenuBar menubar = new JMenuBar();
        menubar.add(file);

        setJMenuBar(menubar);
    }

    void updateSize() {
        PageFormat pf = PageSetup.getPageFormat(I18n.get("Document"));
        double width = pf.getWidth();
        double height = pf.getHeight();

        jgraph.setSize((int) width, (int) height);

        Dimension d = new Dimension((int) width, (int) height);
        jgraph.setPreferredSize(d);
        jgraph.setMinimumSize(d);
        jgraph.setMaximumSize(d);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                graphcontainer.updateUI();
            }
        });
    }

    void updateGrid(boolean b) {
        grid = b;
        graph.setGridEnabled(grid);
        // graph.setGridVisible(grid);

        Utils.getPreferences().putBoolean("GridEnabled", grid);
    }

    void removeSelectedFields() {
        if (!graph.isSelectionEmpty()) {
            Object[] cells = graph.getSelectionCells();
            // cells = graph.getDescendants(cells);
            graph.getModel().beginUpdate();
            for (Object cell : cells) {
                graph.getModel().remove(cell);
            }
            graph.getModel().endUpdate();
        }
    }

    void setAlignment(int align) {
        if (!graph.isSelectionEmpty()) {
            // GraphLayoutCache glc = graph.getGraphLayoutCache();
            Object[] cells = graph.getSelectionCells();
            for (Object o : cells) {
                if (o instanceof mxCell) {
                    mxCell c = (mxCell) o;
                    // mxConstants.setHorizontalAlignment(c.getAttributes(),
                    // align);
                    // glc.editCell(c, c.getAttributes());
                    GraphUtils.setAlign(graph, c, align);
                }
            }
        }
    }

    void setCellFont(Font f) {
        if (f == null) {
            return;
        }
        lastfont = f;
        if (!graph.isSelectionEmpty()) {
            Object[] cells = graph.getSelectionCells();
            for (Object o : cells) {
                if (o instanceof mxCell) {
                    mxCell c = (mxCell) o;
                    GraphUtils.setFont(graph, c, f);
                    // mxConstants.setFont(c.getAttributes(), f);
                    // glc.editCell(c, c.getAttributes());
                } else {
                    System.err.println(o.getClass().toString());
                }
            }
        }
    }

    boolean cellsSelected() {
        if (graph.isSelectionEmpty()) {
            return false;
        }
        Object[] cells = graph.getSelectionCells();
        return (cells != null) && (cells.length > 0);
    }

    @Override
    public void setVisible(boolean v) {
        parent.setEnabled(!v);
        if (!v) {
            String key = PropertyConstants.URKUNDE;
            if (einzelwertung) {
                key = PropertyConstants.URKUNDE_EINZELWERTUNG;
            }
            wk.setProperty(key, GraphUtils.collectGraph(graph));
            controller.sendDataUpdateEvent("DocumentChanged", UpdateEventConstants.REASON_PROPERTIES_CHANGED, null);
        }
        super.setVisible(v);
        if (Utils.getPreferences().getBoolean("DisplayDocumentsHelp", true)) {
            help.setVisible(v);
        }
    }
}