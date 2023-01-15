package de.df.jauswertung.gui.plugins.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.hc.core5.http.ContentType;

public class DataProducer {

    private final String name;
    private final DataProvider dp;

    public DataProducer(DataProvider dp, String target) {
        name = target;
        this.dp = dp;
    }

    public byte[] writeTo() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            dp.sendData(out, name);
            return out.toByteArray();
        }
    }

    public ContentType getContentType() {
        String line = name.toLowerCase(Locale.ENGLISH);
        if (line.endsWith(".wk")) {
            return ContentType.DEFAULT_BINARY;
        }
        if (line.endsWith(".pdf")) {
            return ContentType.DEFAULT_BINARY;
        }
        if (line.endsWith(".csv")) {
            return ContentType.DEFAULT_TEXT;
        }
        if (line.endsWith(".xml")) {
            return ContentType.TEXT_XML;
        }
        if (line.endsWith(".xls")) {
            return ContentType.DEFAULT_BINARY;
        }
        if (line.endsWith(".zip")) {
            return ContentType.DEFAULT_BINARY;
        }
        if (line.endsWith(".wkmm")) {
            return ContentType.DEFAULT_BINARY;
        }
        if (line.endsWith(".xlsx")) {
            return ContentType.DEFAULT_BINARY;
        }
        return ContentType.TEXT_HTML;
    }
}