package de.df.jauswertung.gui.plugins.timelimit;

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.Timelimit;
import de.df.jauswertung.daten.Timelimitchecktype;
import de.df.jauswertung.daten.Timelimits;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.Excel2007Utils;
import de.df.jauswertung.io.ExcelReader;
import de.df.jauswertung.io.ImportUtils;
import de.df.jauswertung.io.TableFormatException;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.io.FileUtils;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.NullFeedback;
import de.df.jutils.util.StringTools;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class JTimelimitsPanel extends JPanel {

    private static final String[] TITLES    = new String[] { "Disziplin", "Zeit", "Min (Alter)", "Max (Alter)", "Altersklasse", "Geschlecht" };

    private Timelimits            current   = null;

    private final JTextField            name      = new JWarningTextField(true, false);
    private final JTextField            shortname = new JWarningTextField(true, false);
    private final JComboBox<String>     limittype = new JComboBox<>(new String[] { I18n.get("UpperLimit"), I18n.get("LowerLimit") });

    private final ExtendedTableModel    model     = new ExtendedTableModel(new Object[0][4], new String[] { "Disziplin", "Zeit", "Min (Alter)", "Max (Alter)" });
    private final JTable                table     = new JTable(model);

    private JButton               importButton;
    private JButton               exportButton;

    private JPanel                panel;
    private JGlassPanel<JPanel>   glass;

    private final JFrame                parent;
    
    private final Sex[] sexes;

    JTimelimitsPanel(JFrame parent, Sex female, Sex male) {
        this.parent = parent;
        this.sexes = new Sex[] {female, male};

        createPanel();
        createGlassPanel();

        setLayout(new BorderLayout());
        add(glass, BorderLayout.CENTER);

        setEnabled(false);
    }

    public boolean isDataValid() {
        return true;
    }

    boolean save() {
        if (current == null) {
            return true;
        }
        if (!isDataValid()) {
            return false;
        }

        current.setName(name.getText());
        current.setShortname(shortname.getText());
        current.setCheck(limittype.getSelectedIndex() == 0 ? Timelimitchecktype.UPPER_LIMIT : Timelimitchecktype.LOWER_LIMIT);
        return true;
    }

    public void setData(Timelimits data) {
        current = data;

        if (current == null) {
            name.setText("");
            shortname.setText("");
            limittype.setSelectedIndex(0);
            model.setDataVector(toData(null), TITLES);
            setEnabled(false);
        } else {
            name.setText(current.getName());
            shortname.setText(current.getShortname());
            limittype.setSelectedIndex(current.getCheck() == Timelimitchecktype.UPPER_LIMIT ? 0 : 1);
            model.setDataVector(toData(current.getLimits()), TITLES);
            setEnabled(true);
        }
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);
        glass.setEnabled(isEnabled);
    }

    private void createGlassPanel() {
        glass = new JGlassPanel<>(panel);
    }

    private JPanel createButtons() {
        importButton = new JButton(I18n.get("Import"));
        importButton.addActionListener(e -> doImport());

        exportButton = new JButton(I18n.get("Export"));
        exportButton.addActionListener(e -> doExport());

        exportButton.setEnabled(false);

        return FormLayoutUtils.createButtonsPanel(importButton, exportButton);
    }

    private void createPanel() {
        panel = new JPanel();
        panel.setLayout(new FormLayout("0dlu,fill:default,4dlu,fill:default:grow,0dlu",
                "0dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,0dlu"));

        panel.add(new JLabel(I18n.get("Name")), CC.xy(2, 2));
        panel.add(new JLabel(I18n.get("Shortname")), CC.xy(2, 4));
        panel.add(new JLabel(I18n.get("Limittype")), CC.xy(2, 6));

        panel.add(name, CC.xy(4, 2));
        panel.add(shortname, CC.xy(4, 4));
        panel.add(limittype, CC.xy(4, 6));

        panel.add(new JScrollPane(table), CC.xyw(2, 8, 3));

        panel.add(createButtons(), CC.xyw(2, 10, 3, "right, fill"));
    }

    private void doImport() {
        String filename = FileChooserUtils.openFile(parent, I18n.get("Import"), new SimpleFileFilter("Microsoft Excel", "xls", "xlsx"));
        if (filename != null) {
            try {
                Feedback fb = new NullFeedback();

                fb.showFeedback(I18n.get("LoadingFile"));

                FileInputStream is = new FileInputStream(filename);
                byte[] data = FileUtils.readFile(is);

                Object[][][] tables = null;
                String[] titles;

                try {
                    HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(data));
                    tables = ExcelReader.sheetsToTable(wb);
                    titles = new String[tables.length];
                    for (int x = 0; x < tables.length; x++) {
                        titles[x] = wb.getSheetName(x);
                    }
                } catch (OfficeXmlFileException e) {
                    XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(data));
                    tables = Excel2007Utils.sheetsToTable(wb);
                    titles = new String[tables.length];
                    for (int x = 0; x < tables.length; x++) {
                        titles[x] = wb.getSheetName(x);
                    }
                }
                Timelimit[] limits = tablesToLimits(fb, titles, tables);
                current.setLimits(limits);
                model.setDataVector(toData(limits), TITLES);
            } catch (IOException io) {
                DialogUtils.warn(parent, "Datei konnte nicht geladen werden.", String.format("Probleme beim Laden der Datei: {}", io.getMessage()));
                log.info("", io);
            } catch (TableFormatException e) {
                DialogUtils.warn(parent, "Das Geschlecht konnte nicht bestimmt werden.", "Für einen Wert in der Spalte 'Geschlecht' konnte kein passender Wert gefunden werden.");
                log.info("Format Problem:", e);
            }
        }
    }

    private Object[][] toData(Timelimit[] limits) {
        LinkedList<Object[]> lines = new LinkedList<>();
        if (limits != null) {
            for (Timelimit l : limits) {
                lines.add(new Object[] { l.getDisziplin(), StringTools.zeitString(l.getTime()), l.getMinage() == 0 ? "" : l.getMinage(),
                        l.getMaxage() == 0 ? "" : l.getMaxage(), l.getAgegroup(), l.isMale() ? "m" : "w" });
            }
        }
        return lines.toArray(lines.toArray(new Object[lines.size()][0]));
    }

    private Timelimit[] tablesToLimits(Feedback fb, String[] titles, Object[][][] tables) throws TableFormatException{
        LinkedList<Timelimit> limits = new LinkedList<>();
        for (int x = 0; x < titles.length; x++) {
            Collection<Timelimit> tmp = tableToLimits(fb, titles[x], tables[x]);
            if (tmp == null) {
                DialogUtils.warn(parent, I18n.get("CouldNotImportFile"), I18n.get("Note.CouldNotImportFile"));
                limits.clear();
                break;
            }
            limits.addAll(tmp);
        }
        return limits.toArray(new Timelimit[limits.size()]);
    }

    private Collection<Timelimit> tableToLimits(Feedback fb, String string, Object[][] data) throws TableFormatException{
        LinkedList<Timelimit> limits = new LinkedList<>();

        if (getString(data, 0, 0).equals("Disziplin") && getString(data, 1, 0).equals("Zeit") && getString(data, 2, 0).equals("Alter min")
                && getString(data, 3, 0).equals("Alter max") && getString(data, 4, 0).equals("Altersklasse") && getString(data, 5, 0).equals("Geschlecht")) {
            for (int x = 1; x < data.length; x++) {
                Object[] row = data[x];
                String disziplin = getString(row, 0);
                int zeit = getTime(row, 1);
                int agemin = getInt(row, 2);
                int agemax = getInt(row, 3);
                String agegroup = getString(row, 4);
                boolean isMale = getSex(row, 5);
                if (agemin < 0 || agemax < 0) {
                    throw new TableFormatException(new int[] {x, 2}, "", "");
                }
                limits.add(new Timelimit(disziplin, zeit, agemin, agemax, agegroup, isMale));
            }
        }

        return limits;
    }

    private String getString(Object[][] data, int x, int y) {
        try {
            return data[y][x].toString().trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private String getString(Object[] row, int x) {
        try {
            return row[x].toString().trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private int getTime(Object[] row, int x) {
        try {
            return ImportUtils.getZeit(row, x, 0, "", "");
        } catch (Exception ex) {
            return 0;
        }
    }

    private int getInt(Object[] row, int x) {
        try {
            if (getString(row, x).equals("")) {
                return 0;
            }
            if (row[x] instanceof Number) {
                Number n = (Number) row[x];
                return n.intValue();
            }
            return Integer.parseInt(getString(row, x));
        } catch (Exception ex) {
            return -1;
        }
    }

    private boolean getSex(Object[] row, int x) throws TableFormatException {
        String s = getString(row, x);
        return Arrays.stream(sexes).filter(sex -> sex.matches(s)).map(sex -> sex.isMale()).findFirst().orElseThrow(() -> new TableFormatException(new int[] {x}, "", ""));
    }

    private void doExport() {

    }
}