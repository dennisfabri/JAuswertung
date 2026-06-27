package de.df.jauswertung.ares.export;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StringFileStream implements AutoCloseable{

    private static final String CHARSET = "ISO-8859-1";

    private final Path target;

    private List<String> lines = new ArrayList<>();

    public StringFileStream(String target) {
        this.target = Path.of(target);
    }

    public void println(String text) {
        lines.add(text);
    }

    @Override
    public void close() throws IOException {
        Files.writeString(target, String.join("\r\n", lines), Charset.forName(CHARSET));
    }
}
