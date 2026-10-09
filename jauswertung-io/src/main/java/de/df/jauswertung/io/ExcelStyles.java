/*
 * Created on 01.04.2006
 */
package de.df.jauswertung.io;

import java.util.Hashtable;

import javax.swing.SwingConstants;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelStyles {

    private Hashtable<String, CellStyle> styles;
    private Workbook wb;
    private DataFormat dataformat;

    public ExcelStyles(Workbook book) {
        wb = book;
        dataformat = wb.createDataFormat();
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
            cellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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

    public CellStyle getStyle(int align, boolean top, boolean right, boolean bottom, boolean left, boolean colored,
            String format, boolean grouping,
            boolean wrap) {
        HorizontalAlignment a = HorizontalAlignment.LEFT;
        switch (align) {
        case SwingConstants.LEFT:
            a = HorizontalAlignment.LEFT;
            break;
        case SwingConstants.CENTER:
            a = HorizontalAlignment.CENTER;
            break;
        case SwingConstants.RIGHT:
            a = HorizontalAlignment.RIGHT;
            break;
        default:
            a = HorizontalAlignment.LEFT;
            break;
        }
        return getStyle(top, right, bottom, left, colored, format, a, grouping, wrap);
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