/*
 * Created on 01.04.2006
 */
package de.df.jauswertung.io;

import java.util.Hashtable;

import javax.swing.SwingConstants;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.ExtendedColor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTStylesheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyles;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STTableStyleType;

public class ExcelStyles {

    // Light blue of the banded rows in Excel's default table style (Blue, Accent 1, Lighter 80%), as ARGB
    private static final byte[] FILL_ARGB = { (byte) 0xFF, (byte) 0xDD, (byte) 0xEB, (byte) 0xF7 };
    private static final byte[] BLACK_ARGB = { (byte) 0xFF, 0, 0, 0 };

    private static final String TABLE_STYLE = "JAuswertung";

    private Hashtable<String, CellStyle> styles;
    private Hashtable<Integer, CellStyle> durchgestrichen = new Hashtable<>();
    private Font durchgestrichenFont;
    private Workbook wb;
    private DataFormat dataformat;
    private ExtendedColor fillColor;
    private boolean tableStyleCreated;

    public ExcelStyles(Workbook book) {
        wb = book;
        dataformat = wb.createDataFormat();
        fillColor = wb.getCreationHelper().createExtendedColor();
        fillColor.setARGBHex("FFDDEBF7");
        styles = new Hashtable<>();
    }

    private int booleanToBit(boolean b) {
        return b ? 1 : 0;
    }

    private CellStyle createCellStyle(BorderStyle top, BorderStyle right, BorderStyle bottom, BorderStyle left,
            boolean colored, HorizontalAlignment align,
            boolean wrap) {
        CellStyle cellStyle = wb.createCellStyle();
        if (colored) {
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setFillForegroundColor(fillColor);
        }
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(align);
        if (top != BorderStyle.NONE) {
            cellStyle.setBorderTop(top);
            cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        }
        if (right != BorderStyle.NONE) {
            cellStyle.setBorderRight(right);
            cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        }
        if (bottom != BorderStyle.NONE) {
            cellStyle.setBorderBottom(bottom);
            cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        }
        if (left != BorderStyle.NONE) {
            cellStyle.setBorderLeft(left);
            cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        }
        cellStyle.setWrapText(wrap);
        return cellStyle;
    }

    public CellStyle getDurchgestrichen(CellStyle style) {
        return durchgestrichen.computeIfAbsent(style.getIndex() & 0xFFFF, i -> {
            if (durchgestrichenFont == null) {
                durchgestrichenFont = wb.createFont();
                durchgestrichenFont.setStrikeout(true);
            }
            CellStyle result = wb.createCellStyle();
            result.cloneStyleFrom(style);
            result.setFont(durchgestrichenFont);
            return result;
        });
    }

    private static HorizontalAlignment toHorizontalAlignment(int align) {
        switch (align) {
        case SwingConstants.CENTER:
            return HorizontalAlignment.CENTER;
        case SwingConstants.RIGHT:
            return HorizontalAlignment.RIGHT;
        case SwingConstants.LEFT:
        default:
            return HorizontalAlignment.LEFT;
        }
    }

    public CellStyle getStyle(int align, boolean top, boolean right, boolean bottom, boolean left, boolean colored,
            String format, boolean grouping,
            boolean wrap) {
        return getStyle(top, right, bottom, left, colored, format, toHorizontalAlignment(align), grouping, wrap);
    }

    /**
     * Style for cells inside an Excel table: no borders and no fill, so that the table style shows through.
     */
    public CellStyle getTableStyle(int align, String format, boolean wrap) {
        return getTableStyle(toHorizontalAlignment(align), format, wrap);
    }

    /**
     * Name of the table style for Excel tables: header row in light blue with bold text, data rows alternating white
     * and light blue (first data row white). Being part of the table style, the stripes stay correct when sorting or
     * filtering. The style is added to the workbook on first use.
     */
    public String getTableStyleName() {
        if (!tableStyleCreated) {
            createTableStyle(((XSSFWorkbook) wb).getStylesSource());
            tableStyleCreated = true;
        }
        return TABLE_STYLE;
    }

    private static void createTableStyle(StylesTable stylesTable) {
        CTDxf header = CTDxf.Factory.newInstance();
        CTFont font = header.addNewFont();
        font.addNewB().setVal(true);
        font.addNewColor().setRgb(BLACK_ARGB);
        addFill(header);

        CTDxf stripe = CTDxf.Factory.newInstance();
        addFill(stripe);

        // putDxf returns the number of dxfs, i.e. the index of the added one plus one
        int headerId = stylesTable.putDxf(header) - 1;
        int stripeId = stylesTable.putDxf(stripe) - 1;

        CTStylesheet stylesheet = stylesTable.getCTStylesheet();
        CTTableStyles tableStyles = stylesheet.isSetTableStyles() ? stylesheet.getTableStyles()
                : stylesheet.addNewTableStyles();
        CTTableStyle style = tableStyles.addNewTableStyle();
        style.setName(TABLE_STYLE);
        style.setPivot(false);
        addElement(style, STTableStyleType.HEADER_ROW, headerId);
        addElement(style, STTableStyleType.SECOND_ROW_STRIPE, stripeId);
        style.setCount(style.sizeOfTableStyleElementArray());
        tableStyles.setCount(tableStyles.sizeOfTableStyleArray());
    }

    private static void addFill(CTDxf dxf) {
        CTPatternFill fill = dxf.addNewFill().addNewPatternFill();
        fill.setPatternType(STPatternType.SOLID);
        fill.addNewFgColor().setRgb(FILL_ARGB);
        fill.addNewBgColor().setRgb(FILL_ARGB);
    }

    private static void addElement(CTTableStyle style, STTableStyleType.Enum type, int dxfId) {
        var element = style.addNewTableStyleElement();
        element.setType(type);
        element.setDxfId(dxfId);
    }

    public CellStyle getTableStyle(HorizontalAlignment align, String format, boolean wrap) {
        String key = "t" + booleanToBit(wrap) + "x" + align + "x" + format;
        CellStyle style = styles.get(key);
        if (style != null) {
            return style;
        }
        style = createCellStyle(BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, false, align,
                wrap);
        if (format != null) {
            style.setDataFormat(dataformat.getFormat(format));
        }
        styles.put(key, style);
        return style;
    }

    public CellStyle getStyle(boolean top, boolean right, boolean bottom, boolean left, boolean colored,
            String format, HorizontalAlignment align,
            boolean grouping, boolean wrap) {
        StringBuilder sb = new StringBuilder();
        sb.append(booleanToBit(top));
        sb.append(booleanToBit(right));
        sb.append(booleanToBit(bottom));
        sb.append(booleanToBit(left));
        sb.append(booleanToBit(colored));
        sb.append(booleanToBit(wrap));
        sb.append("x");
        sb.append(align);
        sb.append("x");
        sb.append(format);
        CellStyle style = styles.get(sb.toString());
        if (style != null) {
            return style;
        }

        BorderStyle innerborder = BorderStyle.DOTTED;
        if (grouping) {
            innerborder = BorderStyle.NONE;
        }
        BorderStyle outerborder = BorderStyle.DOTTED;
        if (grouping) {
            outerborder = BorderStyle.THIN;
        }

        style = createCellStyle(top ? BorderStyle.THIN : innerborder, right ? BorderStyle.THIN : outerborder,
                bottom ? BorderStyle.THIN : innerborder,
                left ? BorderStyle.THIN : outerborder, colored, align, wrap);
        if (format != null) {
            style.setDataFormat(dataformat.getFormat(format));
        }
        styles.put(sb.toString(), style);
        return style;
    }
}