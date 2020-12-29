package de.df.jauswertung.gui.veranstaltung;

import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.border.BorderUtils;

class JVeranstaltungsausgabe extends JPanel {

    final JVeranstaltungswertung parent;

    public JVeranstaltungsausgabe(JVeranstaltungswertung parent) {
        this.parent = parent;
        setBorder(BorderUtils.createLabeledBorder(I18n.get("Output")));
        setLayout(new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu"));

        PrinterCollection pc = new PrinterCollection("Results");
        pc.add(new ResultsPrinter(parent));
        pc.add(new SiegerlistePrinter(parent));
        pc.add(new ResultsExporter(parent));
        pc.doLayoutOneColumn();

        add(pc.getPanel(), CC.xy(2, 2));
    }
}