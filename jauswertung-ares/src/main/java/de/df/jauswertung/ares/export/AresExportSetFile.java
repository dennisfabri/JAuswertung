package de.df.jauswertung.ares.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class AresExportSetFile {

    public static final String EXTENSION = "aex";

    private static final int VERSION = 1;

    private record Content(int version, List<String> competitions, String directory) {
    }

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void write(AresExportSet set, Path file) throws IOException {
        Path base = file.toAbsolutePath().getParent();
        List<String> competitions = set.competitions().stream().map(c -> relativize(base, c)).toList();
        String directory = set.directory() == null ? null : relativize(base, set.directory());
        objectMapper.writeValue(file.toFile(), new Content(VERSION, competitions, directory));
    }

    public AresExportSet read(Path file) throws IOException, AresExportSetFormatException {
        Content content;
        try {
            content = objectMapper.readValue(Files.readString(file), Content.class);
        } catch (JacksonException e) {
            throw new AresExportSetFormatException(file, e);
        }
        if (content.version() != VERSION) {
            throw new AresExportSetFormatException(file, "Unsupported version " + content.version());
        }
        if (content.competitions() == null || content.competitions().stream().anyMatch(c -> c == null || c.isBlank())) {
            throw new AresExportSetFormatException(file, "Missing competitions");
        }
        Path base = file.toAbsolutePath().getParent();
        try {
            List<Path> competitions = content.competitions().stream().map(c -> resolve(base, c)).toList();
            Path directory = content.directory() == null || content.directory().isBlank() ? null
                    : resolve(base, content.directory());
            return new AresExportSet(competitions, directory);
        } catch (InvalidPathException e) {
            throw new AresExportSetFormatException(file, e);
        }
    }

    private static String relativize(Path base, Path path) {
        Path absolute = path.toAbsolutePath().normalize();
        try {
            return base.relativize(absolute).toString().replace(File.separatorChar, '/');
        } catch (IllegalArgumentException e) {
            // Windows: paths on another drive cannot be expressed relative to the set file.
            return absolute.toString();
        }
    }

    private static Path resolve(Path base, String path) {
        return base.resolve(path).normalize();
    }
}
