package de.df.jauswertung.gui.plugins.kampfrichter;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.kampfrichter.*;
import de.df.jauswertung.gui.util.*;
import de.df.jutils.graphics.ColorUtils;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.ShadowBorder;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.GraphicsUtils;

class JKampfrichterPositionPanel extends JPanel {

    private static final long   serialVersionUID = -2978602096884551760L;

    private String              position;
    private KampfrichterEinheit einheit;

    private JPanel              top;
    private JLabel              title;

    JKampfrichterEinheitPanel   parent;

    boolean                     changed          = false;

    private JWarningTextField[] names            = new JWarningTextField[0];
    private JWarningTextField[] glds             = new JWarningTextField[0];
    private JTextPane[]         texts            = new JTextPane[0];
    private JKariStufenButton[] levels           = new JKariStufenButton[0];
    private JButton[]           delete           = new JButton[0];

    private JButton             edit;
    private JButton             up;
    private JButton             down;
    private JButton             neu              = new JTransparentButton(IconManager.getSmallIcon("new"));

    private DocumentListener    documentListener;
    private ChangeListener      changeListener;
    private KeyListener         keyListener;

    public JKampfrichterPositionPanel(JKampfrichterEinheitPanel kep, KampfrichterEinheit ke, String pos) {
        if (pos == null) {
            throw new NullPointerException();
        }
        if (ke == null) {
            throw new NullPointerException();
        }
        position = pos;
        einheit = ke;

        parent = kep;

        keyListener = new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    nextRow(false);
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    nextRow(true);
                    evt.consume();
                } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    previousRow(true);
                    evt.consume();
                }
            }
        };

        initGUI();
    }

    void nextRow(boolean keepColumn) {
        rowChange(false, keepColumn);
    }

    void previousRow(boolean keepColumn) {
        rowChange(true, keepColumn);
    }

    private void rowChange(boolean moveUp, boolean keepColumn) {
        int px = -1;
        int py = -1;
        for (int x = 0; x < names.length; x++) {
            if (names[x].hasFocus()) {
                px = x;
                py = 0;
                break;
            }
        }
        if (px < 0) {
            for (int x = 0; x < glds.length; x++) {
                if (glds[x].hasFocus()) {
                    px = x;
                    py = 1;
                    break;
                }
            }
        }
        if (px < 0) {
            // Position not found
            return;
        }
        if (moveUp) {
            px--;
        } else {
            px++;
        }
        if (px < 0 || px == names.length) {
            return;
        }
        if (!keepColumn) {
            py = 0;
        }
        if (py == 0) {
            names[px].requestFocus();
        } else {
            glds[px].requestFocus();
        }
    }

    private void setTraversalKeys(JTextPane textArea) {
        Set<AWTKeyStroke> set = new HashSet<AWTKeyStroke>(textArea.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        set.add(KeyStroke.getKeyStroke("TAB"));
        textArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);

        set = new HashSet<AWTKeyStroke>(textArea.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        set.add(KeyStroke.getKeyStroke("shift TAB"));
        textArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, set);
    }

    private JTextPane createTextPane() {
        JTextPane tp = new JTextPane();
        setTraversalKeys(tp);
        return tp;
    }

    String getPosition() {
        return position;
    }

    void save() {
        LinkedList<Kampfrichter> kr = new LinkedList<Kampfrichter>();
        for (int x = 0; x < texts.length; x++) {
            KampfrichterStufe stufe = levels[x].getStufe();
            if (stufe == null) {
                stufe = KampfrichterStufe.KEINE;
            }
            Kampfrichter k = new Kampfrichter(names[x].getText(), glds[x].getText(), texts[x].getText(), stufe);
            kr.addLast(k);
        }
        einheit.setKampfrichter(position, kr);
    }

    boolean hasChanged() {
        return changed;
    }

    private void setTitle() {
        String text = position;
        if (einheit.getMinimaleStufe(position) != null) {
            text = I18n.get("RefereePositionAndLevel", position, einheit.getMinimaleStufe(position).toString(),
                    (einheit.getMinimaleStufe(position).equals(KampfrichterStufe.KEINE) ? 0 : 1));
        }

        title.setText(text);
    }

    private void setBorder() {
        setBorder(new ShadowBorder());
    }

    private void initGUI() {
        setBorder();

        neu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changed = true;
                appendRow();
            }
        });
        neu.setToolTipText(I18n.getToolTip("AddReferee"));

        Kampfrichter[] karis = einheit.getKampfrichter(position);
        if (karis.length == 0) {
            karis = new Kampfrichter[1];
            karis[0] = new Kampfrichter();
        }

        names = new JWarningTextField[karis.length];
        glds = new JWarningTextField[karis.length];
        texts = new JTextPane[karis.length];
        levels = new JKariStufenButton[karis.length];
        delete = new JButton[karis.length];

        documentListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                changed = true;
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changed = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed = true;
            }
        };
        changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                changed = true;
            }
        };

        for (int x = 0; x < karis.length; x++) {
            createRow(x, karis[x].getName(), karis[x].getGliederung(), karis[x].getBemerkung(), karis[x].getStufe(), karis.length > 1);
        }

        createTop();
        buildUI();

        setTitle();
    }

    private Color defaultForeground;

    @SuppressWarnings("serial")
    private void createTop() {
        title = new JLabel();
        Color c = UIManager.getColor("InternalFrame.activeTitleForeground");
        if (c == null) {
            Color titleBackgroundGradientStart = UIManager.getColor("InternalFrame.activeTitleBackground");
            Color titleBackgroundGradientEnd = UIManager.getColor("InternalFrame.activeTitleGradient");

            if (titleBackgroundGradientStart == null) {
                titleBackgroundGradientStart = UIManager.getColor("InternalFrame.borderColor");
            }
            if (titleBackgroundGradientEnd == null) {
                titleBackgroundGradientEnd = UIManager.getColor("InternalFrame.borderShadow");
            }
            if (titleBackgroundGradientStart == null) {
                titleBackgroundGradientStart = Color.GRAY;
            }
            if (titleBackgroundGradientEnd == null) {
                titleBackgroundGradientEnd = Color.LIGHT_GRAY;
            }

            Color medium = ColorUtils.calculateColor(titleBackgroundGradientStart, titleBackgroundGradientEnd, 0.5);
            c = ColorUtils.invert(medium);
        }
        // Workaround fuer falsche Farbwerte unter Windows XP
        c = new Color(c.getRGB());
        defaultForeground = c;
        title.setForeground(c);
        top = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                Color start = UIManager.getColor("InternalFrame.activeTitleBackground");
                Color end = UIManager.getColor("InternalFrame.activeTitleGradient");

                if (start == null) {
                    start = UIManager.getColor("InternalFrame.borderColor");
                }
                if (end == null) {
                    end = UIManager.getColor("InternalFrame.borderShadow");
                }
                if (start == null) {
                    start = Color.GRAY;
                }
                if (end == null) {
                    end = Color.LIGHT_GRAY;
                }

                Color mix = GraphicsUtils.paintGradient((Graphics2D) g, 0, 0, getWidth(), getHeight(), start, end);
                title.setForeground(ColorUtils.contrastColor(mix, defaultForeground));

                super.paintComponent(g);
            }
        };
        top.setOpaque(false);

        up = new JTransparentButton(IconManager.getSmallIcon("up"));
        up.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.moveUp(position);
                changed = true;
            }
        });
        // up.setToolTipText(I18n.getToolTip("EditRefereeposition"));

        down = new JTransparentButton(IconManager.getSmallIcon("down"));
        down.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.moveDown(position);
                changed = true;
            }
        });
        // down.setToolTipText(I18n.getToolTip("EditRefereeposition"));

        edit = new JTransparentButton(IconManager.getSmallIcon("edit"));
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editPosition();
            }
        });
        edit.setToolTipText(I18n.getToolTip("EditRefereeposition"));

        JButton remove = new JTransparentButton(IconManager.getSmallIcon("remove"));
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.removePosition(JKampfrichterPositionPanel.this);
            }
        });
        remove.setToolTipText(I18n.getToolTip("RemoveRefereeposition"));

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu,fill:default,1dlu,fill:default,4dlu,fill:default,1dlu,fill:default,4dlu",
                "1dlu,fill:default,1dlu");
        top.setLayout(layout);

        top.add(title, CC.xy(2, 2));

        top.add(up, CC.xy(4, 2));
        top.add(down, CC.xy(6, 2));
        top.add(edit, CC.xy(8, 2));
        top.add(remove, CC.xy(10, 2));

        updatePosition();
    }

    private void buildUI() {
        removeAll();
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu,fill:default:grow,4dlu,fill:default,4dlu,0px:grow,4dlu,fill:default,4dlu",
                "0dlu,fill:default,4dlu,fill:default," + FormLayoutUtils.createLayoutString(2 * texts.length));
        layout.setColumnGroups(new int[][] { { 2, 4, 8 } });
        FormLayoutUtils.setRowGroups(layout, 2, 2 * texts.length);
        setLayout(layout);

        add(top, CC.xyw(1, 2, 11, "fill,fill"));

        add(new JLabel(I18n.get("Name")), CC.xy(2, 4));
        add(new JLabel(I18n.get("Organization")), CC.xy(4, 4));
        add(new JLabel(I18n.get("Level")), CC.xy(6, 4));
        add(new JLabel(I18n.get("Comment")), CC.xy(8, 4));
        for (int x = 0; x < texts.length; x++) {
            add(names[x], CC.xy(2, 6 + 4 * x, "fill,fill"));
            add(glds[x], CC.xy(4, 6 + 4 * x, "fill,fill"));
            add(levels[x], CC.xy(6, 6 + 4 * x, "fill,fill"));
            add(new JScrollPane(texts[x]), CC.xywh(8, 6 + 4 * x, 1, 3, "fill,fill"));
            add(delete[x], CC.xywh(10, 6 + 4 * x, 1, 3, "fill,top"));
        }
        add(neu, CC.xy(10, texts.length * 4 + 4));

        updateUI();
    }

    private void createRow(int x, String name, String gliederung, String bemerkung, KampfrichterStufe stufe, boolean enabled) {
        names[x] = new JWarningTextField(name);
        names[x].setColumns(20);
        names[x].getDocument().addDocumentListener(documentListener);
        names[x].addKeyListener(keyListener);
        names[x].setAutoSelectAll(true);
        glds[x] = new JWarningTextField(gliederung);
        glds[x].setColumns(20);
        glds[x].getDocument().addDocumentListener(documentListener);
        glds[x].addKeyListener(keyListener);
        glds[x].setAutoSelectAll(true);
        texts[x] = createTextPane();
        texts[x].setText(bemerkung);
        texts[x].getDocument().addDocumentListener(documentListener);
        texts[x].addKeyListener(keyListener);
        levels[x] = new JKariStufenButton(stufe);
        levels[x].addStateListener(changeListener);
        levels[x].addKeyListener(keyListener);
        delete[x] = new JTransparentButton(IconManager.getSmallIcon("delete"));
        delete[x].addActionListener(new RemoveListener());
        delete[x].setEnabled(enabled);
        delete[x].setToolTipText(I18n.getToolTip("RemoveReferee"));
    }

    void appendRow() {
        int length = names.length + 1;

        JWarningTextField[] namesNew = new JWarningTextField[length];
        JWarningTextField[] gldsNew = new JWarningTextField[length];
        JTextPane[] textsNew = new JTextPane[length];
        JKariStufenButton[] levelsNew = new JKariStufenButton[length];
        JButton[] deleteNew = new JButton[length];

        for (int x = 0; x < texts.length; x++) {
            namesNew[x] = names[x];
            gldsNew[x] = glds[x];
            textsNew[x] = texts[x];
            levelsNew[x] = levels[x];
            deleteNew[x] = delete[x];
            deleteNew[x].setEnabled(true);
        }

        names = namesNew;
        glds = gldsNew;
        texts = textsNew;
        levels = levelsNew;
        delete = deleteNew;

        int x = length - 1;
        createRow(x, "", "", "", KampfrichterStufe.KEINE, true);

        buildUI();
    }

    void removeRow(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        JButton b = (JButton) o;
        for (int x = 0; x < delete.length; x++) {
            if (delete[x] == b) {
                removeRow(x);
                return;
            }
        }
        throw new IllegalArgumentException();
    }

    void removeRow(int row) {
        if (row >= names.length) {
            return;
        }

        int length = names.length - 1;

        JWarningTextField[] namesNew = new JWarningTextField[length];
        JWarningTextField[] gldsNew = new JWarningTextField[length];
        JTextPane[] textsNew = new JTextPane[length];
        JKariStufenButton[] levelsNew = new JKariStufenButton[length];
        JButton[] deleteNew = new JButton[length];

        for (int x = 0; x < row; x++) {
            namesNew[x] = names[x];
            gldsNew[x] = glds[x];
            textsNew[x] = texts[x];
            levelsNew[x] = levels[x];
            deleteNew[x] = delete[x];
            deleteNew[x].setEnabled(length > 1);
        }

        for (int x = row; x < length; x++) {
            namesNew[x] = names[x + 1];
            gldsNew[x] = glds[x + 1];
            textsNew[x] = texts[x + 1];
            levelsNew[x] = levels[x + 1];
            deleteNew[x] = delete[x + 1];
            deleteNew[x].setEnabled(length > 1);
        }

        names = namesNew;
        glds = gldsNew;
        texts = textsNew;
        levels = levelsNew;
        delete = deleteNew;

        buildUI();
    }

    protected void editPosition() {
        KampfrichterPosition pos = einheit.getPosition(position);
        new JKampfrichterPositionEditor((JFrame) SwingUtilities.getWindowAncestor(parent), einheit, pos).setVisible(true);
        position = pos.getPosition();
        setTitle();
    }

    class RemoveListener implements ActionListener {

        // private final int row;

        public RemoveListener() {
            // this.row = row;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            changed = true;
            removeRow(e.getSource());
        }
    }

    public void updatePosition() {
        up.setEnabled(!parent.isFirst(position));
        down.setEnabled(!parent.isLast(position));
    }
}