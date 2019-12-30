/*
 * Export.java Created on 2. Oktober 2002, 12:37
 */

package de.df.jauswertung.io;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.SpreadsheetUtils;
import de.df.jutils.gui.jtable.ExtendedTableModel;
import de.df.jutils.gui.jtable.ExtendedTableModel.TitleCell;
import de.df.jutils.gui.jtable.ExtendedTableModel.TitleRow;
import de.df.jutils.io.csv.FixedDecimal;
import de.df.jutils.io.csv.Seconds;

/**
 * Diese Klasse sorgt fuer den Export der Ergebnisse in Excel-Dateien
 * 
 * @author dennis
 */
public class ExcelWriter {

    public static final short PAGESIZE_LETTER           = (short) 1;
    public static final short PAGESIZE_LEGAL            = (short) 5;
    public static final short PAGESIZE_EXECUTIVE        = (short) 7;
    public static final short PAGESIZE_A4               = (short) 9;
    public static final short PAGESIZE_A5               = (short) 11;
    public static final short PAGESIZE_ENVELOPE_10      = (short) 20;
    public static final short PAGESIZE_ENVELOPE_DL      = (short) 27;
    public static final short PAGESIZE_ENVELOPE_C5      = (short) 28;
    public static final short PAGESIZE_ENVELOPE_MONARCH = (short) 37;

    private static HSSFCell createCell(HSSFRow row, int col, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(col);
        cell.setCellStyle(style);
        return cell;
    }

    private static HSSFCell createMergedCell(HSSFSheet sheet, int x, int y, int width, int height, HSSFCellStyle style, String value) {
        for (int i = y; i < y + height; i++) {
            HSSFRow row = sheet.getRow(i);
            if (row == null) {
                row = sheet.createRow(i);
            }
            for (int j = x; j < x + width; j++) {
                createCell(row, j, style);
            }
        }
        sheet.addMergedRegion(new CellRangeAddress(y, y + Math.max(height - 1, 0), x, x + Math.max(width - 1, 0)));
        HSSFCell cell = sheet.getRow(y).getCell(x);
        cell.setCellValue(new HSSFRichTextString(value));
        return cell;
    }

    private static <T extends ASchwimmer> HSSFSheet createSheet(HSSFWorkbook wb, String title, boolean landscape, int repeatrows, int repeatcols,
            String competition) {
        String atitle = SpreadsheetUtils.toSheetName(title);
        String xtitle = atitle;
        int trycount = 0;
        String abtitle = atitle;
        if (atitle.length() > 30) {
            abtitle = atitle.substring(0, 30);
        }
        while (wb.getSheet(xtitle) != null) {
            trycount++;
            xtitle = abtitle + trycount;
        }
        System.out.println("Creating Sheet \"" + xtitle + "\"");
        HSSFSheet sheet = wb.createSheet(xtitle);
        // int index = wb.getNumberOfSheets() - 1;
        //
        // int startcols = 0;
        // if (repeatcols == 0) {
        // startcols = -1;
        // }
        // int startrows = 0;
        // if (repeatrows == 0) {
        // startrows = -1;
        // }
        // wb.setRepeatingRowsAndColumns(index, startcols, repeatcols - 1,
        // startrows, repeatrows - 1);

        HSSFPrintSetup ps = sheet.getPrintSetup();
        sheet.setAutobreaks(true);
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 100);

        ps.setLandscape(landscape);
        ps.setPaperSize(PAGESIZE_A4);
        sheet.setHorizontallyCenter(true);

        HSSFFooter footer = sheet.getFooter();
        footer.setRight(I18n.get("ExcelFooterRight", HeaderFooter.page(), HeaderFooter.numPages()));
        footer.setLeft(I18n.get("ProgrammerInfo"));
        HSSFHeader header = sheet.getHeader();
        header.setCenter(HeaderFooter.tab());
        Date datum = new Date();
        DateFormat datumsformat = DateFormat.getDateInstance();
        header.setRight(I18n.get("ExcelHeaderRight", datumsformat.format(datum), DateFormat.getTimeInstance().format(datum)));
        if ((competition != null) && (competition.trim().length() > 0)) {
            header.setLeft(I18n.get("ExcelHeaderLeft", competition.trim()));
        }
        return sheet;
    }

    public static void write(OutputStream name, ExtendedTableModel[] tms, int groupsize, int repeatrows, int repeatcols, String competition)
            throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        ExcelStyles styles = new ExcelStyles(wb);

        for (int z = 0; z < tms.length; z++) {
            buildSheet(tms, wb, styles, z, groupsize, repeatrows, repeatcols, competition);
        }
        wb.write(name);
    }

    public static void write(OutputStream name, ExtendedTableModel[][] tms, int groupsize, Integer[] repeatrows, Integer[] repeatcols, String[] titles,
            String competition) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        ExcelStyles styles = new ExcelStyles(wb);

        for (int z = 0; z < tms.length; z++) {
            buildSheet(tms, wb, styles, z, groupsize, repeatrows[z], repeatcols[z], titles, competition);
        }
        wb.write(name);
    }

    private static void buildSheet(ExtendedTableModel[] tms, HSSFWorkbook wb, ExcelStyles styles, int tmIndex, int groupsize, int repeatrows, int repeatcols,
            String competition) {
        ExtendedTableModel[][] tmx = new ExtendedTableModel[tms.length][1];
        for (int x = 0; x < tms.length; x++) {
            tmx[x][0] = tms[x];
        }
        buildSheet(tmx, wb, styles, tmIndex, groupsize, repeatrows, repeatcols, null, competition);
    }

    /**
     * @param tms
     * @param wb
     * @param styles
     * @param z
     */
    private static void buildSheet(ExtendedTableModel[][] tms, HSSFWorkbook wb, ExcelStyles styles, int tmIndex, int groupsize, int repeatrows, int repeatcols,
            String[] names, String competition) {
        ExtendedTableModel[] tm = tms[tmIndex];

        String titel = tm[0].getName();
        if (names != null) {
            titel = names[tmIndex];
        }

        HSSFSheet sheet = createSheet(wb, SpreadsheetUtils.toSheetName(titel), tm[0].isLandscape(), repeatrows, repeatcols, competition);
        int offset = 0;
        for (ExtendedTableModel aTm : tm) {
            for (int x = 0; x < aTm.getColumnCount(); x++) {
                String[] content = new String[aTm.getRowCount() + 2];
                content[0] = aTm.getColumnName(x);
                content[1] = aTm.getColumnFormat(x);
                if (content[1] == null) {
                    content[1] = "";
                }
                for (int y = 0; y < aTm.getRowCount(); y++) {
                    // This is not correct, but better than nothing
                    String s = determineStringvalue(aTm.getValueAt(y, x), aTm.getColumnFormat(x));
                    if (s.contains("\n")) {
                        String[] splittet = s.split("\n");
                        s = splittet[0];
                        for (String t : splittet) {
                            if (t.length() > s.length()) {
                                s = t;
                            }
                        }
                    }
                    content[y + 2] = s;
                }
                sheet.setColumnWidth(x, SpreadsheetUtils.guessMaximumWidth(content));
            }

            offset = buildSheetTitles(styles, aTm, sheet, offset, groupsize > 1);
            offset = buildSheetContent(styles, aTm, sheet, offset, groupsize) + 1;
        }
    }

    /**
     * @param styles
     * @param tm
     * @param sheet
     * @param offset
     */
    private static int buildSheetContent(ExcelStyles styles, ExtendedTableModel tm, HSSFSheet sheet, int offset, int groupsize) {
        // Write Table content
        for (short y = 0; y < tm.getRowCount(); y++) {
            HSSFRow row = sheet.createRow(y + offset);
            boolean top = (y % groupsize == 0);
            if (groupsize == 0) {
                top = (y == 0);
            }
            boolean bottom = (y % groupsize) + 1 == groupsize;
            boolean colored = (((y / groupsize) % 2) != 0);
            for (short x = 0; x < tm.getColumnCount(); x++) {
                // Is left most cell
                boolean left = (x == 0);
                // Is right most cell
                boolean right = (x + 1 == tm.getColumnCount());
                // Get Style
                HSSFCellStyle style = styles.getStyle(tm.getColumnAlignment(x), top, right, bottom, left, colored, tm.getColumnFormat(x), groupsize > 1, false);
                HSSFCell cell = createCell(row, x, style);

                Object o = tm.getValueAt(y, x);
                if (o == null) {
                    o = "";
                } else if (o instanceof Number) {
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellValue(((Number) o).doubleValue());
                } else if (o instanceof FixedDecimal) {
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellValue(((FixedDecimal) o).getValue());
                } else if (o instanceof Seconds) {
                    double s = ((Seconds) o).getSeconds();
                    cell.setCellValue(s / (60 * 60 * 24 * 100));
                } else {
                    String value = determineStringvalue(o, tm.getColumnFormat(x));
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue(new HSSFRichTextString(value));
                    if (value.contains("\n")) {
                        cell.setCellStyle(
                                styles.getStyle(tm.getColumnAlignment(x), top, right, bottom, left, colored, tm.getColumnFormat(x), groupsize > 1, true));
                    }
                }
            }
        }
        return tm.getRowCount() + offset;
    }

    private static String determineStringvalue(Object o, String format) {
        String value = "";
        if (o != null) {
            if (o instanceof Number) {
                if ((format == null) || (format.length() == 0) || format.equals("0")) {
                    value = NumberFormat.getIntegerInstance().format(((Number) o).longValue());
                } else {
                    if (format.indexOf(".") >= 0) {
                        int decimal = format.length() - format.indexOf(".") - 1;
                        NumberFormat nf = NumberFormat.getNumberInstance();
                        nf.setMinimumFractionDigits(decimal);
                        nf.setMaximumFractionDigits(decimal);
                        value = nf.format(((Number) o).doubleValue());
                    }
                }
            } else if (o instanceof String[]) {
                String[] text = (String[]) o;
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (String line : text) {
                    if (!first) {
                        sb.append("\n");
                    }
                    first = false;
                    if (line != null) {
                        sb.append(line);
                    }
                }
                value = sb.toString();
            } else if (o instanceof Object[]) {
                Object[] text = (Object[]) o;
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (Object line : text) {
                    if (!first) {
                        sb.append("\n");
                    }
                    first = false;
                    if (line != null) {
                        sb.append(line.toString());
                    }
                }
                value = sb.toString();
            } else {
                value = o.toString();
            }
        }
        return value;
    }

    /**
     * @param styles
     * @param tm
     * @param sheet
     * @return
     */
    private static int buildSheetTitles(ExcelStyles styles, ExtendedTableModel tm, HSSFSheet sheet, int offset, boolean grouping) {
        // Create title
        if (tm.getExtendedTitleRows() == null) {
            // Simple tableheader has one row
            HSSFRow row = sheet.createRow((short) offset);
            offset++;
            for (short x = 0; x < tm.getColumnCount(); x++) {
                // Is left most cell
                boolean left = (x == 0);
                // Is right most cell
                boolean right = (x + 1 == tm.getColumnCount());
                // Get title
                String s = tm.getColumnName(x);
                // Get Style
                HSSFCellStyle style = styles.getStyle(true, right, true, left, true, null, HorizontalAlignment.CENTER, grouping, s.contains("\n"));
                HSSFCell cell = createCell(row, x, style);
                cell.setCellType(CellType.STRING);
                cell.setCellValue(new HSSFRichTextString(s));
            }
        } else {
            // Create extended title
            HSSFRow[] rows = new HSSFRow[tm.getExtendedTitlesRowCount()];
            boolean[][] used = new boolean[tm.getExtendedTitlesRowCount()][tm.getExtendedTitlesColumnCount()];

            // Prepare rows and cells
            for (short y = 0; y < tm.getExtendedTitlesRowCount(); y++) {
                rows[y] = sheet.createRow(y + offset);
                for (short x = 0; x < tm.getExtendedTitlesColumnCount(); x++) {
                    used[y][x] = false;
                    // Is left most cell
                    boolean left = (x == 0);
                    // Is right most cell
                    boolean right = (x + 1 == tm.getColumnCount());
                    boolean top = (y == 0);
                    boolean bottom = (y + 1 == tm.getExtendedTitlesRowCount());

                    // Get Style
                    HSSFCellStyle style = styles.getStyle(top, right, bottom, left, true, null, HorizontalAlignment.CENTER, grouping, false);
                    HSSFCell cell = createCell(rows[y], x, style);
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue(new HSSFRichTextString(""));
                }
            }

            TitleRow[] etr = tm.getExtendedTitleRows();
            for (int y = 0; y < etr.length; y++) {
                for (int x = 0; x < etr[y].cells.length; x++) {
                    TitleCell cell = etr[y].cells[x];
                    if ((cell.span.width == 1) && (cell.span.height == 1)) {
                        int pos = 0;
                        for (int w = 0; w < used[y].length; w++) {
                            if (!used[y][w]) {
                                pos = w;
                                used[y][w] = true;
                                break;
                            }
                        }
                        rows[y].getCell(pos).setCellValue(new HSSFRichTextString(cell.title));
                    } else {
                        int pos = 0;
                        for (int w = 0; w < used[y].length; w++) {
                            if (!used[y][w]) {
                                pos = w;
                                break;
                            }
                        }

                        int width = (short) cell.span.width;
                        int height = (short) cell.span.height;

                        // Is left most cell
                        boolean left = (pos == 0);
                        // Is right most cell
                        boolean right = (pos + width >= tm.getColumnCount());
                        boolean top = (y == 0);
                        boolean bottom = (y + height >= tm.getExtendedTitlesRowCount());

                        HSSFCellStyle style = styles.getStyle(top, right, bottom, left, true, null, HorizontalAlignment.CENTER, grouping, false);
                        createMergedCell(sheet, pos, y + offset, width, height, style, cell.title);

                        for (int w = y; w < y + height; w++) {
                            for (int v = pos; v < pos + width; v++) {
                                used[w][v] = true;
                            }
                        }
                    }
                }
            }
            offset += tm.getExtendedTitlesRowCount();
        }
        return offset;
    }
}