package de.df.jauswertung.test.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.util.DurchgestrichenerText;
import de.df.jauswertung.gui.util.SchwimmerDisziplin;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jauswertung.gui.util.TableHeatUtils;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jauswertung.io.CsvExporter;
import de.df.jauswertung.io.ExcelExporter;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.util.valueobjects.Startkarte;
import de.df.jutils.util.NullFeedback;

class GestrichenAusgabeTest {

    private static final int AK_OFFEN = 4;
    private static final int ANZAHL = 4;

    private EinzelWettkampf wk;
    private Teilnehmer abgemeldet;

    @BeforeEach
    void setup() {
        wk = new EinzelWettkampf(AgeGroupIOUtils.ladeAKs("src/test/resources/rulebooks/DLRG 2023.rwe"),
                InputManager.ladeStrafen("src/test/resources/rulebooks/DLRG 2023", true));
        for (int x = 1; x <= ANZAHL; x++) {
            Teilnehmer t = wk.createTeilnehmer("ID" + x, "Nachname" + x, "Vorname" + x, 2000, true, "Gliederung",
                    AK_OFFEN, "");
            t.setDisciplineChoice(0, true);
            t.setDisciplineChoice(1, true);
            wk.addSchwimmer(t);
        }
        wk.getLaufliste().erzeugen();
        abgemeldet = wk.getSchwimmer().getFirst();
        abgemeldet.addStrafe(1, Strafe.WITHDRAW);
    }

    @Test
    @SuppressWarnings("unchecked")
    void startkarteDerAbgemeldetenDisziplinIstLeer() {
        List<Startkarte> karten = SchwimmerUtils.toStartkarten(new AWettkampf[] { wk }, 1, false, true, 0, 0);

        assertEquals(2 * ANZAHL, karten.size());
        assertEquals(1, karten.stream().filter(k -> k.getName().equals(abgemeldet.getName())).count());
        assertEquals(1, karten.stream().filter(k -> k.getName().isEmpty()).count());
    }

    @Test
    void lauflisteStreichtNurDieAbgemeldeteDisziplin() {
        JTable table = TableHeatUtils.getLaufliste(wk, true);

        Map<String, Boolean> gestrichen = new HashMap<>();
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int column = 0; column < table.getColumnCount(); column++) {
                if (table.getValueAt(row, column) instanceof SchwimmerDisziplin<?> sd) {
                    Component c = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                    gestrichen.put(sd.getSchwimmer().getStartnummer() + "/" + sd.getDiscipline(),
                            istDurchgestrichen(c));
                }
            }
        }

        int sn = abgemeldet.getStartnummer();
        assertEquals(2 * ANZAHL, gestrichen.size());
        assertEquals(true, gestrichen.get(sn + "/1"));
        assertEquals(false, gestrichen.get(sn + "/0"));
        assertEquals(1, gestrichen.values().stream().filter(b -> b).count());
    }

    @Test
    void excelExportStreichtNurDieAbgemeldeteDisziplin() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(new ExcelExporter().heats(out, wk, new NullFeedback()));

        Map<String, Integer> gestrichen = new HashMap<>();
        Map<String, Integer> normal = new HashMap<>();
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            for (Row row : wb.getSheetAt(0)) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING
                            && cell.getStringCellValue().equals(abgemeldet.getName())) {
                        Map<String, Integer> ziel = wb.getFontAt(cell.getCellStyle().getFontIndex()).getStrikeout()
                                ? gestrichen
                                : normal;
                        ziel.merge(cell.getStringCellValue(), 1, Integer::sum);
                    }
                }
            }
        }

        assertEquals(Map.of(abgemeldet.getName(), 1), gestrichen);
        assertEquals(Map.of(abgemeldet.getName(), 1), normal);
    }

    @Test
    void csvExportEnthaeltNurDenText() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(new CsvExporter().heats(out, wk, new NullFeedback()));

        String csv = out.toString(StandardCharsets.UTF_8);
        assertEquals(2, csv.split(Pattern.quote(abgemeldet.getName()), -1).length - 1);
        assertFalse(csv.contains(DurchgestrichenerText.class.getSimpleName()));
    }

    private static boolean istDurchgestrichen(Component c) {
        List<JLabel> labels = new ArrayList<>();
        sammleLabels(c, labels);
        long gestrichen = labels.stream().filter(l -> l.getText().startsWith("<html><s>")).count();
        assertTrue(!labels.isEmpty() && (gestrichen == 0 || gestrichen == labels.size()));
        return gestrichen > 0;
    }

    private static void sammleLabels(Component c, List<JLabel> labels) {
        if (c instanceof JLabel l && !l.getText().isBlank()) {
            labels.add(l);
        }
        if (c instanceof JList<?> list) {
            for (int x = 0; x < list.getModel().getSize(); x++) {
                if (list.getModel().getElementAt(x) instanceof Component element) {
                    sammleLabels(element, labels);
                }
            }
        }
        if (c instanceof Container container) {
            for (Component child : container.getComponents()) {
                sammleLabels(child, labels);
            }
        }
    }
}
