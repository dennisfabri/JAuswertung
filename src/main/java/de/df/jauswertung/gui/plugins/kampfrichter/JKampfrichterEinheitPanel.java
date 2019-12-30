/*
 * Created on 24.02.2006
 */
package de.df.jauswertung.gui.plugins.kampfrichter;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.kampfrichter.KampfrichterEinheit;
import de.df.jauswertung.gui.util.*;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.layout.FormLayoutUtils;

class JKampfrichterEinheitPanel extends JPanel {

    private static final long            serialVersionUID = -1471802559987003688L;

    boolean                              changed          = false;
    private KampfrichterEinheit          einheit;

    private JKampfrichterPositionPanel[] texts            = null;

    private JButton                      neu;

    public JKampfrichterEinheitPanel(KampfrichterEinheit ke) {
        einheit = ke;

        initGUI();
    }

    void removePosition(JKampfrichterPositionPanel kpp) {
        for (int x = 0; x < texts.length; x++) {
            if (texts[x] == kpp) {
                removePosition(x);
                return;
            }
        }
    }

    void removePosition(int row) {
        JKampfrichterPositionPanel rem = texts[row];
        einheit.removePosition(rem.getPosition());

        JKampfrichterPositionPanel[] temp = new JKampfrichterPositionPanel[texts.length - 1];
        for (int x = 0; x < temp.length; x++) {
            temp[x] = texts[x + (x < row ? 0 : 1)];
        }
        texts = temp;
        updateGUI();
    }

    void addPosition() {
        addPosition(texts.length);
    }

    void addPosition(int row) {
        JKampfrichterPositionPanel[] temp = new JKampfrichterPositionPanel[texts.length + 1];
        for (int x = 0; x < texts.length; x++) {
            temp[x + (x < row ? 0 : 1)] = texts[x];
        }
        String neutext = I18n.get("New");
        String name = neutext;
        int x = 0;
        while (einheit.getPosition(name) != null) {
            x++;
            name = neutext + " (" + x + ")";
        }
        einheit.addPosition(name);

        temp[row] = new JKampfrichterPositionPanel(this, einheit, name);
        texts = temp;
        updateGUI();
    }

    public boolean isFirst(String kp) {
        String[] positionen = einheit.getPositionen();
        if (positionen == null || positionen.length == 0) {
            return true;
        }
        return (positionen[0].equals(kp));
    }

    public boolean isLast(String kp) {
        String[] positionen = einheit.getPositionen();
        if (positionen == null || positionen.length == 0) {
            return true;
        }
        return (positionen[positionen.length - 1].equals(kp));
    }

    private void initGUI() {
        String[] positionen = einheit.getPositionen();

        texts = new JKampfrichterPositionPanel[positionen.length];
        for (int x = 0; x < texts.length; x++) {
            texts[x] = new JKampfrichterPositionPanel(this, einheit, positionen[x]);
        }

        neu = new JTransparentButton(IconManager.getSmallIcon("new"));
        neu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPosition();
            }
        });
        neu.setToolTipText(I18n.getToolTip("AddRefereeposition"));

        updateGUI();
    }

    private void updateGUI() {
        removeAll();
        setLayout(new FlowLayout());

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(texts.length + 1));

        setLayout(layout);
        for (int x = 0; x < texts.length; x++) {
            add(texts[x], CC.xy(2, 2 + x * 2));
        }
        add(neu, CC.xy(2, 2 + 2 * texts.length, "right,fill"));

        SwingUtilities.updateComponentTreeUI(this);
    }

    void save() {
        for (JKampfrichterPositionPanel text : texts) {
            text.save();
        }
    }

    boolean hasChanged() {
        for (JKampfrichterPositionPanel pos : texts) {
            if (pos.hasChanged()) {
                return true;
            }
        }
        return changed;
    }

    private void replace(int x, int y) {
        einheit.exchange(x, y);
        JKampfrichterPositionPanel kp = texts[x];
        texts[x] = texts[y];
        texts[y] = kp;

        remove(texts[x]);
        remove(texts[y]);

        add(texts[x], CC.xy(2, 2 + x * 2));
        add(texts[y], CC.xy(2, 2 + y * 2));

        texts[x].updatePosition();
        texts[y].updatePosition();

        updateUI();
    }

    public void moveUp(String position) {
        String[] positionen = einheit.getPositionen();
        if (positionen == null || positionen.length == 0) {
            return;
        }
        int pos = -1;
        for (int x = 0; x < positionen.length; x++) {
            if (positionen[x].equals(position)) {
                pos = x;
                break;
            }
        }
        replace(pos - 1, pos);
    }

    public void moveDown(String position) {
        String[] positionen = einheit.getPositionen();
        if (positionen == null || positionen.length == 0) {
            return;
        }
        int pos = -1;
        for (int x = 0; x < positionen.length; x++) {
            if (positionen[x].equals(position)) {
                pos = x;
                break;
            }
        }
        replace(pos, pos + 1);
    }
}