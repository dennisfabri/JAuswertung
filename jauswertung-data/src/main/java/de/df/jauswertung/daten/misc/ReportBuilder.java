package de.df.jauswertung.daten.misc;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Collection;

public class ReportBuilder {

    private final StringBuilder sb = new StringBuilder();

    ReportBuilder() {
        sb.append('\n');
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public void addTitle(String title) {
        sb.append(title).append('\n');
    }

    public void addField(String field, Object value) {
        addField(field, value, null);
    }

    public void addField(String field, Object value, int indent) {
        addField(indentString(indent) + field, value);
    }

    public void addField(String field, Object value, Object details) {
        if (value == null) {
            return;
        }

        if (details == null) {
            if (value instanceof Collection) {
                addField(field, (Collection<?>) value, 0);
            } else {
                sb.append("%s: %s\n".formatted(field, value));
            }
        } else {
            sb.append("%s: %s (%s)\n".formatted(field, value, details));
        }
    }

    public void addField(String field, Collection<?> value, int indent) {
        if (value == null) {
            return;
        }
        String subIndent = indentString(indent + 2);

        sb.append("%s%s:\n".formatted(indentString(indent), field));
        for (Object v : value) {
            sb.append("%s%s\n".formatted(subIndent, v));
        }
    }

    private String indentString(int indent) {
        return " ".repeat(Math.max(0, indent));
    }

    private void addMultilineField(String field, String value, String details) {
        if (value == null) {
            return;
        }
        sb.append("%s:\n%s\n".formatted(field, value));
        if (details != null) {
            sb.append(details);
            sb.append('\n');
        }
    }

    public void addException(String text, Throwable e) {
        addMultilineField(text, e.getMessage(), formatStacktrace(e));
    }

    private static String formatStacktrace(Throwable e) {
        try (CharArrayWriter caw = new CharArrayWriter(); PrintWriter pw = new PrintWriter(caw)) {
            e.printStackTrace(pw);
            return caw.toString();
        }
    }
}
