package de.df.jauswertung.ares.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AresExportSetFileTest {

    @TempDir
    Path tempDir;

    private final AresExportSetFile setFile = new AresExportSetFile();

    @Test
    void readReturnsWrittenCompetitionsAndDirectory() throws Exception {
        Path file = tempDir.resolve("dm.aex");
        AresExportSet set = new AresExportSet(
                List.of(tempDir.resolve("einzel.wk"), tempDir.resolve("staffel/mannschaft.wk")),
                tempDir.resolve("ares"));

        setFile.write(set, file);

        assertEquals(set, setFile.read(file));
    }

    @Test
    void readReturnsSetWithoutDirectoryIfNoneWasWritten() throws Exception {
        Path file = tempDir.resolve("dm.aex");
        AresExportSet set = new AresExportSet(List.of(tempDir.resolve("einzel.wk")), null);

        setFile.write(set, file);

        assertEquals(set, setFile.read(file));
    }

    @Test
    void pathsNextToTheSetFileFollowWhenTheFolderIsMoved() throws Exception {
        Path original = tempDir.resolve("original");
        Path moved = tempDir.resolve("moved");
        Files.createDirectories(original);
        setFile.write(new AresExportSet(List.of(original.resolve("einzel.wk")), original.resolve("ares")),
                original.resolve("dm.aex"));

        Files.move(original, moved);

        assertEquals(new AresExportSet(List.of(moved.resolve("einzel.wk")), moved.resolve("ares")),
                setFile.read(moved.resolve("dm.aex")));
    }

    @Test
    void writtenFileDoesNotContainAbsolutePathsForFilesNextToIt() throws Exception {
        Path file = tempDir.resolve("dm.aex");

        setFile.write(new AresExportSet(List.of(tempDir.resolve("einzel.wk")), tempDir.resolve("ares")), file);

        assertFalse(Files.readString(file).contains(tempDir.toString()));
    }

    @Test
    void readRejectsFileThatIsNoAresExportSet() throws IOException {
        Path file = tempDir.resolve("dm.aex");
        Files.writeString(file, "this is not json");

        assertThrows(AresExportSetFormatException.class, () -> setFile.read(file));
    }

    @Test
    void readRejectsFileWithoutCompetitions() throws IOException {
        Path file = tempDir.resolve("dm.aex");
        Files.writeString(file, "{\"version\":1,\"directory\":\"ares\"}");

        assertThrows(AresExportSetFormatException.class, () -> setFile.read(file));
    }

    @Test
    void readRejectsUnknownVersion() throws IOException {
        Path file = tempDir.resolve("dm.aex");
        Files.writeString(file, "{\"version\":2,\"competitions\":[\"einzel.wk\"]}");

        AresExportSetFormatException e = assertThrows(AresExportSetFormatException.class, () -> setFile.read(file));
        assertEquals(file, e.getFile());
    }

    @Test
    void readRejectsInvalidPath() throws IOException {
        Path file = tempDir.resolve("dm.aex");
        Files.writeString(file, "{\"version\":1,\"competitions\":[\"ein\\u0000zel.wk\"]}");

        assertThrows(AresExportSetFormatException.class, () -> setFile.read(file));
    }
}
