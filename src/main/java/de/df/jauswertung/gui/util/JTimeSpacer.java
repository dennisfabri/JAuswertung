package de.df.jauswertung.gui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jutils.gui.JUnderline;

public class JTimeSpacer extends JPanel {

    public JTimeSpacer(boolean print) {
        FormLayout layout = new FormLayout(
                "0dlu,fill:default:grow,1dlu,fill:default," + "1dlu,fill:default:grow,1dlu,fill:default," + "1dlu,fill:default:grow,0dlu",
                "0dlu,fill:default:grow,0dlu");
        layout.setColumnGroups(new int[][] { { 2, 6, 10 }, { 4, 8 } });
        setLayout(layout);

        add(new JUnderline(), CC.xy(2, 2));
        add(new JLabel(":"), CC.xy(4, 2, "center,bottom"));
        add(new JUnderline(), CC.xy(6, 2));
        add(new JLabel(","), CC.xy(8, 2, "center,bottom"));
        add(new JUnderline(), CC.xy(10, 2));

        if (print) {
            setOpaque(true);
            setBackground(Color.WHITE);
        }
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        Component[] cs = getComponents();
        for (Component c : cs) {
            c.setFont(font);
        }
    }
}