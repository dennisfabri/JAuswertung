package de.df.jauswertung.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class TextFileUtils {

    public static String[] fileToStringArray(String directory, String datei, String[] backup, boolean toLowerCase) {
        try {
            List<String> lines = Files.readAllLines(Path.of(directory, datei), StandardCharsets.UTF_8);
            if (toLowerCase) {
                return lines.stream().map(line -> line.toLowerCase(Locale.ROOT)).toArray(String[]::new);
            }
            return lines.toArray(new String[0]);
        } catch (IOException e) {
            return backup;
        }
    }

    public static boolean StringArrayToFile(String directory, String datei, String[] data) {
        try {
            Files.writeString(Path.of(directory, datei), String.join("\n", data), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

}
