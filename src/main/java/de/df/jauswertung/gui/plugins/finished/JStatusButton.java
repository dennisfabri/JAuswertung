package de.df.jauswertung.gui.plugins.finished;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.*;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;

public class JStatusButton extends JToggleButton {

    final JPopupMenu   dialog;

    final CorePlugin   core;

    private JLabel[][] text = new JLabel[0][3];

    public JStatusButton(CorePlugin c) {
        super(createArrow(IconManager.getSmallIcon("finishedinput")));
        core = c;

        setToolTipText(I18n.getToolTip("FinishedInput"));

        setHorizontalTextPosition(SwingConstants.LEFT);
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEnabled(false);
                dialog.show(JStatusButton.this, 0, JStatusButton.this.getHeight());
            }
        });
        dialog = initDialog();
    }

    private JPopupMenu initDialog() {
        return new JPopupMenu() {
            private static final long serialVersionUID = -4238370182220289817L;

            @Override
            public void setVisible(boolean b) {
                if (!b) {
                    JStatusButton.this.setSelected(false);
                    JStatusButton.this.setEnabled(true);
                } else {
                    updateData();
                }
                super.setVisible(b);
            }
        };
    }

    @SuppressWarnings({})
    void updateData() {
        AWettkampf<?> wk = core.getWettkampf();
        Regelwerk aks = wk.getRegelwerk();

        int offset = 1;

        int[] indizes = new int[aks.size()];
        int size = 0;
        for (int x = 0; x < aks.size(); x++) {
            if (SearchUtils.hasSchwimmer(wk, aks.getAk(x))) {
                indizes[x] = size;
                size++;
            } else {
                indizes[x] = -1;
            }
        }

        JLabel[][] text2 = new JLabel[size + offset][3];

        if (text.length < offset) {
            text2[0] = new JLabel[] { new JLabel(I18n.get("AgeGroup")), new JLabel(I18n.get("female")), new JLabel(I18n.get("male")) };
        } else {
            System.arraycopy(text, 0, text2, 0, offset);
        }

        for (int y = 0; y < aks.size(); y++) {
            if (indizes[y] >= 0) {
                int x = indizes[y];

                if (text.length > x + offset) {
                    text2[x + offset] = text[x + offset];
                } else {
                    text2[x + offset] = new JLabel[] { new JLabel(), new JLabel(), new JLabel() };
                }

                text2[x + offset][0].setText(aks.getAk(y).getName());

                for (int male = 0; male < 2; male++) {
                    int complete = wk.getToDisciplineComplete(y, male == 1);
                    if (!SearchUtils.hasSchwimmer(wk, aks.getAk(y), male == 1)) {
                        complete = -1;
                    }
                    String sextext = null;
                    switch (complete) {
                    case -1:
                        sextext = "";
                        break;
                    case 0:
                        sextext = "-";
                        break;
                    default:
                        sextext = "" + complete;
                        break;
                    }

                    text2[x + offset][1 + male].setText(sextext);
                }

            }
        }
        text = text2;

        dialog.removeAll();
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,center:default,4dlu,center:default,4dlu", FormLayoutUtils.createLayoutString(text2.length));
        layout.setColumnGroups(new int[][] { { 4, 6 } });
        dialog.setLayout(layout);

        for (int y = 0; y < text.length; y++) {
            for (int x = 0; x < text[y].length; x++) {
                dialog.add(text[y][x], CC.xy(2 + x * 2, 2 + y * 2));
            }
        }

        dialog.setBorder(BorderUtils.createLabeledBorder(I18n.get("DisciplinesCompleteTo")));

        dialog.pack();
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
        int width = 16;
        int height = 16;
        if (icon != null) {
            width = icon.getIconWidth();
            height = icon.getIconHeight();
        }
        BufferedImage i = new BufferedImage(width + 11, Math.max(height, 4), BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        if (icon != null) {
            g.drawImage(icon.getImage(), 0, 0, null);
        }

        int offsetx = width;
        int offsety = (height - 4) / 2;

        g.setColor(Color.BLACK);
        g.fillPolygon(new int[] { offsetx + 2, offsetx + 8, offsetx + 5 }, new int[] { offsety, offsety, offsety + 3 }, 3);

        return new ImageIcon(i);
    }
}