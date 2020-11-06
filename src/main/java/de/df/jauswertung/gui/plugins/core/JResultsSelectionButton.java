package de.df.jauswertung.gui.plugins.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;

public class JResultsSelectionButton extends JToggleButton {

    final JPopupMenu         dialog;

    final IWettkampfProvider core;

    private JLabel[]         disciplines = new JLabel[0];

    private JLabel[]         agegroups   = new JLabel[0];
    private JLabel[]         sexes       = new JLabel[0];
    private JLabel[]         parts       = new JLabel[0];
    private int[]            indizes     = new int[0];

    private JCheckBox[][]    selection   = new JCheckBox[0][0];
    private JCheckBox[]      hlw         = new JCheckBox[0];

    private JButton[]        plus        = new JButton[0];
    private JButton[]        minus       = new JButton[0];

    private ActionListener   plusAL      = new ActionListener() {

                                             @Override
                                             public void actionPerformed(ActionEvent e) {
                                                 JButton b = (JButton) (e.getSource());
                                                 selectColumn(Integer.parseInt(b.getName()));
                                             }
                                         };

    private ActionListener   minusAL     = new ActionListener() {

                                             @Override
                                             public void actionPerformed(ActionEvent e) {
                                                 JButton b = (JButton) (e.getSource());
                                                 unselectColumn(Integer.parseInt(b.getName()));
                                             }
                                         };

    public JResultsSelectionButton(IWettkampfProvider core) {
        super(createArrow(IconManager.getSmallIcon("finishedinput")));
        this.core = core;

        setToolTipText(I18n.getToolTip("DataFilterSelection"));

        setHorizontalTextPosition(SwingConstants.LEFT);
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEnabled(false);
                dialog.show(JResultsSelectionButton.this, 0, JResultsSelectionButton.this.getHeight());
            }
        });
        dialog = initDialog();
    }

    protected void selectColumn(int column) {
        for (int x = 0; x < selection[column].length; x++) {
            if (selection[column][x].isEnabled()) {
                selection[column][x].setSelected(true);
            }
        }
        if (column % 2 == 0) {
            if (hlw[column / 2].isEnabled()) {
                hlw[column / 2].setSelected(true);
            }
        }
    }

    protected void unselectColumn(int column) {
        for (int x = 0; x < selection[column].length; x++) {
            selection[column][x].setSelected(false);
        }
        if (column % 2 == 0) {
            hlw[column / 2].setSelected(false);
        }
    }

    private JPopupMenu initDialog() {
        return new JPopupMenu() {
            private static final long serialVersionUID = -4238370182220289817L;

            @Override
            public void setVisible(boolean b) {
                if (!b) {
                    JResultsSelectionButton.this.setSelected(false);
                    JResultsSelectionButton.this.setEnabled(true);
                } else {
                    updateData();
                }
                super.setVisible(b);
            }
        };
    }

    public static String createLayoutString(int rowsOrColumns) {
        int groups = rowsOrColumns / 2;
        String group = ",fill:default,1dlu,fill:default,4dlu";
        StringBuffer result = new StringBuffer("4dlu,fill:default,4dlu");
        for (int x = 0; x < groups; x++) {
            result.append(group);
        }
        return result.toString();
    }

    void updateData() {
        AWettkampf<?> wk = core.getWettkampf();
        Regelwerk aks = wk.getRegelwerk();
        int diszanzahl = aks.getMaxDisciplineCount();

        dialog.removeAll();

        if (indizes.length != aks.size()) {
            indizes = new int[aks.size()];
        }
        int size = 0;
        for (int x = 0; x < aks.size(); x++) {
            if (SearchUtils.hasSchwimmer(wk, aks.getAk(x))) {
                indizes[x] = size;
                size++;
            } else {
                indizes[x] = -1;
            }
        }

        int offset = 1;

        agegroups = ToNewArray(agegroups, size);
        sexes = ToNewArray(sexes, size * 2);
        parts = ToNewArray(parts, size * 4);
        disciplines = ToNewArray(disciplines, diszanzahl + 1);

        selection = ToNewArray(selection, parts.length, diszanzahl);
        hlw = ToNewArray(hlw, sexes.length);
        plus = ToNewArray(plus, parts.length, plusAL);
        minus = ToNewArray(minus, parts.length, minusAL);

        FormLayout layout = new FormLayout(createLayoutString(parts.length), FormLayoutUtils.createLayoutString(diszanzahl + 1 + 3 + 2));
        int[][] cgs = new int[1][parts.length];
        for (int x = 0; x < cgs[0].length; x++) {
            cgs[0][x] = 2 + offset * 2 + x * 2;
        }
        dialog.setLayout(layout);

        // Left labels
        for (int x = 0; x < diszanzahl; x++) {
            disciplines[x].setText(I18n.get("DisciplineNumber", x + 1));
            dialog.add(disciplines[x], CC.xy(2, 8 + 2 * x));
        }
        disciplines[diszanzahl].setText(I18n.get("AdditionalPointsShort"));
        dialog.add(disciplines[diszanzahl], CC.xy(2, 8 + 2 * diszanzahl));

        // Top labels and center checkboxes
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            int pos = indizes[x];
            if (pos >= 0) {
                agegroups[pos].setText(ak.getName());
                dialog.add(agegroups[pos], CC.xyw(2 + offset * 2 + pos * 8, 2, 7, "center,center"));
                for (int y = 0; y < 2; y++) {
                    int poss = pos * 2 + y;
                    sexes[poss].setText(I18n.get(y == 0 ? "femaleShort" : "maleShort"));
                    dialog.add(sexes[poss], CC.xyw(2 + offset * 2 + poss * 4, 4, 3, "center,center"));
                    for (int z = 0; z < 2; z++) {
                        int posp = poss * 2 + z;
                        parts[posp].setText(I18n.get(z == 0 ? "Time" : "PenaltyShort"));
                        dialog.add(parts[posp], CC.xy(2 + (offset + posp) * 2, 6, "center,center"));

                        for (int i = 0; i < diszanzahl; i++) {
                            selection[posp][i].setEnabled(i < ak.getDiszAnzahl());
                            if (!selection[posp][i].isEnabled()) {
                                selection[posp][i].setSelected(false);
                            }
                            dialog.add(selection[posp][i], CC.xy(2 + (offset + posp) * 2, 8 + 2 * i, "center,center"));
                        }
                    }
                    hlw[poss].setEnabled(ak.hasHLW());
                    if (!hlw[poss].isEnabled()) {
                        hlw[poss].setSelected(false);
                    }
                    dialog.add(hlw[poss], CC.xy(2 + offset * 2 + poss * 4, 8 + 2 * diszanzahl, "center,center"));
                }
            }
        }
        // +/--Buttons
        for (int i = 0; i < plus.length; i++) {
            plus[i].setText("+");
            dialog.add(plus[i], CC.xy(2 + offset * 2 + i * 2, 8 + 2 * diszanzahl + 2));
            minus[i].setText("-");
            dialog.add(minus[i], CC.xy(2 + offset * 2 + i * 2, 8 + 2 * diszanzahl + 4));
        }

        dialog.setBorder(BorderUtils.createLabeledBorder(I18n.get("DisciplinesCompleteTo")));

        dialog.pack();

    }

    private JButton[] ToNewArray(JButton[] source, int newSize, ActionListener al) {
        JButton[] agegroups2 = null;
        if (newSize > source.length) {
            agegroups2 = new JButton[newSize];
            System.arraycopy(source, 0, agegroups2, 0, source.length);
            for (int x = source.length; x < newSize; x++) {
                agegroups2[x] = new JButton();
                agegroups2[x].setMargin(new Insets(1, 1, 1, 1));
                agegroups2[x].setName("" + x);
                if (al != null) {
                    agegroups2[x].addActionListener(al);
                }
            }
        } else if (newSize == source.length) {
            agegroups2 = source;
        } else {
            agegroups2 = new JButton[newSize];
            System.arraycopy(source, 0, agegroups2, 0, newSize);
        }
        return agegroups2;
    }

    private static JCheckBox[][] ToNewArray(JCheckBox[][] source, int length, int diszanzahl) {
        if (source.length == length && ((length == 0 && source.length == 0) || source[0].length == diszanzahl)) {
            return source;
        }
        JCheckBox[][] selection2 = source;
        if (length > source.length) {
            selection2 = new JCheckBox[length][0];
            System.arraycopy(source, 0, selection2, 0, source.length);
            for (int x = source.length; x < length; x++) {
                selection2[x] = new JCheckBox[0];
            }
        } else if (length < source.length) {
            selection2 = new JCheckBox[length][0];
            System.arraycopy(source, 0, selection2, 0, length);
        }
        for (int x = 0; x < length; x++) {
            selection2[x] = ToNewArray(selection2[x], diszanzahl);
        }
        return selection2;
    }

    private static JLabel[] ToNewArray(JLabel[] source, int newSize) {
        JLabel[] agegroups2 = null;
        if (newSize > source.length) {
            agegroups2 = new JLabel[newSize];
            System.arraycopy(source, 0, agegroups2, 0, source.length);
            for (int x = source.length; x < newSize; x++) {
                agegroups2[x] = new JLabel();
            }
        } else if (newSize == source.length) {
            agegroups2 = source;
        } else {
            agegroups2 = new JLabel[newSize];
            System.arraycopy(source, 0, agegroups2, 0, newSize);
        }
        return agegroups2;
    }

    private static JCheckBox[] ToNewArray(JCheckBox[] source, int newSize) {
        JCheckBox[] agegroups2 = null;
        if (newSize > source.length) {
            agegroups2 = new JCheckBox[newSize];
            System.arraycopy(source, 0, agegroups2, 0, source.length);
            for (int x = source.length; x < newSize; x++) {
                agegroups2[x] = new JCheckBox();
            }
        } else if (newSize == source.length) {
            agegroups2 = source;
        } else {
            agegroups2 = new JCheckBox[newSize];
            System.arraycopy(source, 0, agegroups2, 0, newSize);
        }
        return agegroups2;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            if (dialog.isVisible()) {
                dialog.setVisible(false);
            }
        }
        super.setEnabled(enabled);
    }

    private static Icon createArrow(ImageIcon icon) {
        BufferedImage i = new BufferedImage(icon.getIconWidth() + 11, Math.max(icon.getIconHeight(), 4), BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        g.drawImage(icon.getImage(), 0, 0, null);

        int offsetx = icon.getIconWidth();
        int offsety = (icon.getIconHeight() - 4) / 2;

        g.setColor(Color.BLACK);
        g.fillPolygon(new int[] { offsetx + 2, offsetx + 8, offsetx + 5 }, new int[] { offsety, offsety, offsety + 3 }, 3);

        return new ImageIcon(i);
    }

    @SuppressWarnings("rawtypes")
    public AgegroupResultSelection[] getSelection(AWettkampf wk) {
        LinkedList<AgegroupResultSelection> result = new LinkedList<AgegroupResultSelection>();
        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < indizes.length; x++) {
            if (indizes[x] >= 0) {
                result.addAll(getResultForAgegroup(indizes[x], x, aks.getAk(x)));
            }
        }
        return result.toArray(new AgegroupResultSelection[result.size()]);
    }

    private LinkedList<AgegroupResultSelection> getResultForAgegroup(int i, int ak, Altersklasse ag) {
        LinkedList<AgegroupResultSelection> result = new LinkedList<AgegroupResultSelection>();
        for (int x = 0; x < 2; x++) {
            boolean[] times = new boolean[ag.getDiszAnzahl()];
            boolean[] penalties = new boolean[ag.getDiszAnzahl()];
            boolean hasHlw = false;

            hasHlw = !ag.hasHLW() || hlw[i * 2 + x].isSelected();
            for (int y = 0; y < times.length; y++) {
                times[y] = selection[i * 4 + 2 * x + 0][y].isSelected();
                penalties[y] = selection[i * 4 + 2 * x + 1][y].isSelected();
            }

            result.add(new AgegroupResultSelection(ak, x == 1, times, penalties, hasHlw));
        }
        return result;
    }
}