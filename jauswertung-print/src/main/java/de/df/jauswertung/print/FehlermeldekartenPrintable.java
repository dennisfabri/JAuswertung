/*
 * Created on 02.01.2005
 */
package de.df.jauswertung.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.JMiddleline;
import de.df.jutils.gui.border.ExtendedLineBorder;
import de.df.jutils.gui.layout.SimpleTableBuilder;
import de.df.jutils.print.PageMode;
import de.df.jutils.print.PrintManager;
import de.df.jutils.print.printables.AComponentMultiOnPagePrintable;

public final class FehlermeldekartenPrintable extends AComponentMultiOnPagePrintable {

    public FehlermeldekartenPrintable(PageMode mode) {
        super(mode);
    }

    static JComponent getUnderline() {
        JLabel label = new JLabel(" ");
        label.setBorder(new ExtendedLineBorder(Color.BLACK, 0, 0, 1, 0));
        return label;
    }

    static JComponent getMiddleline() {
        return new JMiddleline();
    }

    @Override
    public JPanel getPanel(int page, int offset) {
        JPanel panel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void add(Component comp, Object constraints) {
                Font font = PrintManager.getFont();
                if (font != null) {
                    comp.setFont(font);
                }
                super.add(comp, constraints);
            }
        };
        panel.setBackground(Color.WHITE);
        panel.setForeground(Color.BLACK);

        SimpleTableBuilder tb = new SimpleTableBuilder(panel, 2, true);
        tb.add(I18n.get("Fehlermeldekarte"), true, "center,center");

        tb.addSeparator(getMiddleline());

        tb.add(I18n.get("AgeGroup") + ":");
        tb.add(getUnderline(), 2);
        tb.add(I18n.get("SexChoice"), true, "center,fill");

        tb.add(I18n.get("Discipline") + ":");
        tb.add(getUnderline(), true);

        tb.add(I18n.get("Heat") + ":");
        tb.add(getUnderline());
        tb.add(I18n.get("Lane") + ":");
        tb.add(getUnderline());
        // tb.add(I18n.get("HeadAndLaneShort") + ":", false, "right,fill");
        // tb.add(getUnderline());

        tb.addSeparator(getMiddleline());

        tb.add(I18n.get("Beanstandungen"), true, "center,center");
        tb.add(getUnderline(), true);
        tb.add(getUnderline(), true);
        tb.add(getUnderline(), true);
        tb.add(getUnderline(), true);
        tb.add(getUnderline(), true);

        tb.addSeparator(getMiddleline());

        tb.add(I18n.get("SchwimmOderWenderichter") + ":");
        tb.add(getUnderline(), true);
        tb.add(getUnderline(), true);
        // tb.add(I18n.get("Lane") + ":", false, "right,bottom");
        // tb.add(getUnderline());

        tb.add(I18n.get("CodePunktabzug") + ":");
        tb.add(getUnderline(), true);
        tb.add(getUnderline(), true);

        tb.add(I18n.get("WettkampfleiterShort") + ":");
        tb.add(getUnderline(), true);

        // tb.add(I18n.get("Protokollfuehrer") + ":");
        // tb.add(getUnderline(), true);

        // tb.add(I18n.get("Appeal") + ":");
        // tb.add(I18n.get("AcceptedDenied"), true);

        // tb.add(I18n.get("Decision") + ":");
        // tb.add(I18n.get("AppealChangedOrReturned"), true);

        // tb.add(I18n.get("Schiedsgericht") + ":");
        // tb.add(getUnderline(), true);

        tb.addSeparator(getMiddleline());
        tb.add(I18n.get("ProgrammerShortInfo"), true, "right,center");

        return tb.getPanel(false, false);
    }

    @Override
    public boolean pageExists(int page) {
        return page == 0;
    }

}