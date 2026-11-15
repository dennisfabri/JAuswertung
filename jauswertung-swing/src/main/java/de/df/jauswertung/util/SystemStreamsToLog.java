package de.df.jauswertung.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends everything written to {@code System.out} and {@code System.err} to SLF4J, line by line. Text is only passed on
 * to the original stream while the logging system itself is writing, so a console appender that writes to
 * {@code System.out} keeps working and no line appears twice.
 */
public final class SystemStreamsToLog {

    private static final ThreadLocal<Boolean> LOGGING = ThreadLocal.withInitial(() -> false);
    private static final StackWalker STACK = StackWalker.getInstance();
    private static final String LOGBACK_PACKAGE = "ch.qos.logback.";

    private static boolean installed = false;

    private SystemStreamsToLog() {
    }

    public static synchronized void install() {
        if (installed) {
            return;
        }
        installed = true;

        Logger out = LoggerFactory.getLogger("System.out");
        Logger err = LoggerFactory.getLogger("System.err");
        System.setOut(wrap(System.out, out::info));
        System.setErr(wrap(System.err, err::error));
    }

    /**
     * Returns a stream that reports each completed line to {@code sink}. Output produced while the sink is running or
     * by Logback itself (a console appender writing to the replaced stream) goes to {@code original} only, otherwise
     * logging would call itself endlessly.
     */
    static PrintStream wrap(PrintStream original, Consumer<String> sink) {
        return new PrintStream(new LineSplittingStream(original, sink), true, original.charset());
    }

    private static final class LineSplittingStream extends OutputStream {

        private final PrintStream original;
        private final Consumer<String> sink;
        private final Charset charset;
        private final ByteArrayOutputStream line = new ByteArrayOutputStream();

        private LineSplittingStream(PrintStream original, Consumer<String> sink) {
            this.original = original;
            this.sink = sink;
            this.charset = original.charset();
        }

        @Override
        public void write(int b) {
            if (isLoggingSystemWriting()) {
                original.write(b);
                return;
            }
            collect(b);
        }

        @Override
        public void write(byte[] bytes, int off, int len) {
            if (isLoggingSystemWriting()) {
                original.write(bytes, off, len);
                return;
            }
            for (int i = off; i < off + len; i++) {
                collect(bytes[i]);
            }
        }

        /**
         * Logback's console appender looks up {@code System.out} on every write, so a line logged directly through a
         * logger arrives here as well. Reporting it to the sink would log it a second time, and the appender drops
         * that nested call, so the line would never reach the console.
         */
        private static boolean isLoggingSystemWriting() {
            return LOGGING.get() || STACK.walk(
                    frames -> frames.anyMatch(frame -> frame.getClassName().startsWith(LOGBACK_PACKAGE)));
        }

        private void collect(int b) {
            synchronized (line) {
                if (b == '\n') {
                    flushLine();
                } else {
                    line.write(b);
                }
            }
        }

        @Override
        public void flush() {
            original.flush();
        }

        private void flushLine() {
            String text = line.toString(charset);
            line.reset();
            if (text.endsWith("\r")) {
                text = text.substring(0, text.length() - 1);
            }
            LOGGING.set(true);
            try {
                sink.accept(text);
            } finally {
                LOGGING.set(false);
            }
        }
    }
}
