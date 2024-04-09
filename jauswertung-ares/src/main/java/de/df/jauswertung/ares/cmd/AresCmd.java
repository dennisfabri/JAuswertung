package de.df.jauswertung.ares.cmd;

import de.df.jauswertung.ares.export.AresWriter;

import java.io.IOException;

import static java.util.Arrays.stream;

public class AresCmd {

    public static void main(String[] args) throws IOException {
        String dir = args[0];
        String[] filenames = stream(args).skip(1).toArray(String[]::new);
        new AresWriter().write(filenames, dir);
    }
}
