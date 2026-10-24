package de.df.jauswertung.gui.veranstaltung;

import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;

class JVeranstaltungsausgabe extends JPanel {

    public JVeranstaltungsausgabe(Ausgabekontext parent) {
        setLayout(new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu"));

        PrinterCollection pc = new PrinterCollection("Results");
        pc.add(new ResultsPrinter(parent));
        pc.add(new SiegerlistePrinter(parent));
        pc.doLayoutOneColumn();

        add(pc.getPanel(), CC.xy(2, 2));
    }
}