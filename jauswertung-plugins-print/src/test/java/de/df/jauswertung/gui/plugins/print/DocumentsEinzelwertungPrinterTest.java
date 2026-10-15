package de.df.jauswertung.gui.plugins.print;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LOAD_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PROPERTIES_CHANGED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.awt.Component;
import java.awt.Container;
import java.util.Hashtable;

import javax.swing.JButton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.plugin.IPluginManager;

class DocumentsEinzelwertungPrinterTest {

    private EinzelWettkampf wk;
    private DocumentsEinzelwertungPrinter printer;

    @BeforeEach
    void setUp() {
        Disziplin[] disziplinen = { new Disziplin("A", 1, 50, 1), new Disziplin("B", 1, 50, 1) };
        Altersklasse ak = new Altersklasse("AK", new Disziplin[][] { disziplinen, disziplinen }, false);
        ak.setEinzelwertung(true);
        Regelwerk regelwerk = new Regelwerk(1, true, "");
        regelwerk.setAk(0, ak);

        wk = new EinzelWettkampf(regelwerk, new Strafen());
        wk.addSchwimmer(wk.createTeilnehmer("1", "Muster", "Max", 2010, true, "DLRG Musterstadt", 0, ""));

        printer = new DocumentsEinzelwertungPrinter(mock(IPluginManager.class), mock(CorePlugin.class));
    }

    @Test
    void cannotPrintWithoutCertificateTemplate() {
        printer.checkMeldungen(wk, REASON_LOAD_WK);

        assertFalse(button("Print").isEnabled());
        assertFalse(button("Preview").isEnabled());
    }

    @Test
    void canPrintOnceTheCertificateTemplateIsSet() {
        printer.checkMeldungen(wk, REASON_LOAD_WK);

        wk.setProperty(PropertyConstants.URKUNDE_EINZELWERTUNG, new Hashtable<?, ?>[] { new Hashtable<>() });
        printer.checkMeldungen(wk, REASON_PROPERTIES_CHANGED);

        assertTrue(button("Print").isEnabled());
        assertTrue(button("Preview").isEnabled());
    }

    private JButton button(String key) {
        JButton button = find(printer.getPanel(), I18n.get(key));
        if (button == null) {
            throw new AssertionError("No button " + key);
        }
        return button;
    }

    private static JButton find(Container container, String text) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton b && text.equals(b.getText())) {
                return b;
            }
            if (c instanceof Container inner) {
                JButton b = find(inner, text);
                if (b != null) {
                    return b;
                }
            }
        }
        return null;
    }
}
