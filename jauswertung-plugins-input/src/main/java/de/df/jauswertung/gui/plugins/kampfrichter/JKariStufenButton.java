package de.df.jauswertung.gui.plugins.kampfrichter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.kampfrichter.KampfrichterStufe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.layout.FormLayoutUtils;

public class JKariStufenButton extends JPanel {

    private final class StufenChangeListener implements ChangeListener, ItemListener, ActionListener {

        private final int index;
        private final KampfrichterStufe kstufe;

        public StufenChangeListener(int x, KampfrichterStufe k) {
            index = x;
            kstufe = k;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            stateChanged(null);
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            stateChanged(null);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            KampfrichterStufe ks = stufe;
            if (!levels[index].isSelected()) {
                ks = stufe.ohne(kstufe);
            } else {
                ks = stufe.mit(kstufe);
            }
            if (!ks.equals(stufe)) {
                setStufe(ks);
            }
            JKariStufenButton.this.stateChanged();
        }
    }

    private static final long serialVersionUID = 7825192585994417930L;

    JToggleButton level = null;
    JPopupMenu dialog = null;

    JCheckBox[] levels = null;

    KampfrichterStufe stufe = KampfrichterStufe.KEINE;

    public JKariStufenButton() {
        this(KampfrichterStufe.KEINE);
    }

    public JKariStufenButton(KampfrichterStufe stufe) {
        level = new JToggleButton(createArrow());
        level.setBounds(0, 0, 0, 0);
        level.setHorizontalTextPosition(SwingConstants.LEFT);
        level.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                level.setEnabled(false);
                dialog.show(level, 0, level.getHeight());
            }
        });
        initDialog();

        setLayout(new BorderLayout());
        add(level, BorderLayout.CENTER);

        setStufe(stufe);
    }

    private static final String[] LEVEL_NAMES = new String[] { "F1", "E1", "E2", "E3", "D1", "D3" };

    private void initDialog() {
        dialog = new JPopupMenu() {
            private static final long serialVersionUID = -4238370182220289817L;

            @Override
            public void setVisible(boolean b) {
                if (!b) {
                    level.setSelected(false);
                    level.setEnabled(true);
                }
                super.setVisible(b);
            }
        };

        levels = new JCheckBox[LEVEL_NAMES.length];
        for (int x = 0; x < LEVEL_NAMES.length; x++) {
            levels[x] = new JCheckBox(LEVEL_NAMES[x]);
            levels[x].setToolTipText(I18n.getToolTip("Kampfrichterstufe" + LEVEL_NAMES[x].replace("/", "")));
            levels[x].setOpaque(false);
        }

        levels[0].addActionListener(new StufenChangeListener(0, KampfrichterStufe.F1));
        levels[1].addActionListener(new StufenChangeListener(1, KampfrichterStufe.E1));
        levels[2].addActionListener(new StufenChangeListener(2, KampfrichterStufe.E2));
        levels[3].addActionListener(new StufenChangeListener(3, KampfrichterStufe.F1E3));
        levels[4].addActionListener(new StufenChangeListener(4, KampfrichterStufe.D12));
        levels[5].addActionListener(new StufenChangeListener(5, KampfrichterStufe.F1D3));

        FormLayout layout = new FormLayout(FormLayoutUtils.createLayoutString(3),
                FormLayoutUtils.createLayoutString(3));
        layout.setColumnGroups(new int[][] { { 2, 4, 6 } });
        layout.setRowGroups(new int[][] { { 2, 4, 6 } });
        dialog.setLayout(layout);

        dialog.add(levels[0], CC.xy(2, 2));
        dialog.add(levels[1], CC.xy(2, 4));
        dialog.add(levels[2], CC.xy(4, 4));
        dialog.add(levels[3], CC.xy(6, 4));
        dialog.add(levels[4], CC.xyw(2, 6, 3, "center,center"));
        dialog.add(levels[5], CC.xy(6, 6));

        dialog.pack();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            if (dialog.isVisible()) {
                dialog.setVisible(false);
            }
            level.setEnabled(false);
        } else {
            level.setEnabled(true);
        }
        super.setEnabled(enabled);
    }

    public void setStufe(KampfrichterStufe stufe) {
        this.stufe = stufe;

        level.setText(stufe.toString());

        boolean[] selected = new boolean[levels.length];

        int[] l = stufe.toLevels();
        selected[0] = l[0] > 0;
        selected[1] = l[0] > 1;
        selected[2] = l[1] > 0;
        selected[3] = l[2] > 1;
        selected[4] = l[0] > 2;
        selected[5] = l[2] > 2;

        for (int x = 0; x < levels.length; x++) {
            if (levels[x].isSelected() != selected[x]) {
                levels[x].setSelected(selected[x]);
            }
        }
    }

    public KampfrichterStufe getStufe() {
        return stufe;
    }

    private static Icon createArrow() {
        BufferedImage i = new BufferedImage(11, 4, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        g.setColor(Color.BLACK);
        g.fillPolygon(new int[] { 2, 8, 5 }, new int[] { 0, 0, 3 }, 3);

        return new ImageIcon(i);
    }

    private LinkedList<ChangeListener> changes = new LinkedList<ChangeListener>();

    void stateChanged() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener change : changes) {
            change.stateChanged(event);
        }
    }

    public void addStateListener(ChangeListener listener) {
        if (!changes.contains(listener)) {
            changes.add(listener);
        }
    }

    public void removeStateListener(ChangeListener listener) {
        changes.remove(listener);
    }
}
