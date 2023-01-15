package de.df.jauswertung.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.print.Printable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.JUnderline;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.ExtendedHeaderFooterPrintable;

public class UnterschriftPrintable extends ExtendedHeaderFooterPrintable {

    public UnterschriftPrintable(Printable p) {
        super(p, null, getFooter(), SwingConstants.LEFT, SwingConstants.CENTER, PrintManager.getFont());
    }

    private static JLabel getLabel(String id) {
        JLabel l = new JLabel(I18n.get(id));
        if (PrintManager.getFont() != null) {
            l.setFont(PrintManager.getFont());
        }
        return l;
    }

    private static Component getFooter() {
        String layout = "0dlu,fill:default,1dlu,0dlu:grow,0dlu,0dlu:grow,"
                + "1dlu,fill:default,1dlu,0dlu:grow,0dlu,0dlu:grow,"
                + "1dlu,fill:default,1dlu,0dlu:grow,0dlu,0dlu:grow," + "1dlu,fill:default,1dlu,0dlu:grow,"
                + "1dlu,fill:default,1dlu,0dlu:grow,";
        JPanel p = new JPanel(new FormLayout(layout, "4dlu,fill:default,0dlu"));

        p.setBackground(Color.WHITE);
        p.add(getLabel("Wettkampfleiter"), CC.xy(2, 2));
        p.add(new JUnderline(), CC.xyw(4, 2, 3, "fill,fill"));
        p.add(getLabel("Protocol"), CC.xy(8, 2));
        p.add(new JUnderline(), CC.xyw(10, 2, 3, "fill,fill"));
        p.add(getLabel("Schiedsgericht"), CC.xy(14, 2));
        p.add(new JUnderline(), CC.xyw(16, 2, 3, "fill,fill"));
        p.add(getLabel("Aushang"), CC.xy(20, 2));
        p.add(new JUnderline(), CC.xy(22, 2));
        p.add(getLabel("EndeDerEinspruchsfrist"), CC.xy(24, 2));
        p.add(new JUnderline(), CC.xy(26, 2));

        return p;
    }
}
