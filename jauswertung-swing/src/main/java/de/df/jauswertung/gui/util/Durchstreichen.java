package de.df.jauswertung.gui.util;

import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public final class Durchstreichen {

    private Durchstreichen() {
    }

    /**
     * Umhüllt die Standardrenderer, damit später gesetzte Hüllen wie die abwechselnden Zeilenfarben beim Drucken
     * weiter greifen. Spaltenrenderer hätten Vorrang vor ihnen.
     */
    public static void tabelle(JTable table) {
        Set<Class<?>> klassen = new HashSet<>();
        for (int x = 0; x < table.getColumnCount(); x++) {
            TableColumn column = table.getColumnModel().getColumn(x);
            if (column.getCellRenderer() != null) {
                column.setCellRenderer(new Renderer(column.getCellRenderer()));
            }
            Class<?> klasse = table.getColumnClass(x);
            if (klassen.add(klasse)) {
                table.setDefaultRenderer(klasse, new Renderer(table.getDefaultRenderer(klasse)));
            }
        }
    }

    private record Renderer(TableCellRenderer renderer) implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (istGestrichen(value)) {
                komponente(c);
            }
            return c;
        }
    }

    /**
     * Streicht über HTML durch, weil die Schriftattribute beim PDF-Export nicht gezeichnet werden.
     */
    public static void komponente(Component c) {
        if (c instanceof JLabel l && l.getText() != null && !l.getText().isBlank()) {
            l.setText(html(l.getText()));
        }
        if (c instanceof JList<?> list) {
            for (int x = 0; x < list.getModel().getSize(); x++) {
                if (list.getModel().getElementAt(x) instanceof Component element) {
                    komponente(element);
                }
            }
        }
        if (c instanceof Container container) {
            for (Component child : container.getComponents()) {
                komponente(child);
            }
        }
    }

    static String html(String text) {
        if (!text.regionMatches(true, 0, "<html>", 0, 6)) {
            return "<html><s>" + text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</s></html>";
        }
        if (text.contains("<s>")) {
            return text;
        }
        String start = text.contains("<body>") ? "<body>" : "<html>";
        String ende = text.contains("</body>") ? "</body>" : "</html>";
        int anfang = text.indexOf(start) + start.length();
        int schluss = text.lastIndexOf(ende);
        if (schluss < anfang) {
            schluss = text.length();
        }
        return text.substring(0, anfang) + "<s>" + text.substring(anfang, schluss) + "</s>" + text.substring(schluss);
    }

    private static boolean istGestrichen(Object value) {
        return value instanceof DurchgestrichenerText
                || (value instanceof SchwimmerDisziplin<?> sd && sd.startetNicht());
    }
}
