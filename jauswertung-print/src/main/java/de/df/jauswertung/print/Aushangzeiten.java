package de.df.jauswertung.print;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Aushangzeiten(LocalDateTime aushang, LocalDateTime endeDerEinspruchsfrist) {

    private static final Duration EINSPRUCHSFRIST = Duration.ofMinutes(30);
    private static final DateTimeFormatter UHRZEIT = DateTimeFormatter.ofPattern("HH:mm");

    public static Aushangzeiten ab(LocalDateTime jetzt, int versatzInMinuten) {
        LocalDateTime aushang = jetzt.plusMinutes(versatzInMinuten);
        return new Aushangzeiten(aushang, aushang.plus(EINSPRUCHSFRIST));
    }

    public String aushangText() {
        return UHRZEIT.format(aushang);
    }

    public String endeDerEinspruchsfristText() {
        return UHRZEIT.format(endeDerEinspruchsfrist);
    }
}
