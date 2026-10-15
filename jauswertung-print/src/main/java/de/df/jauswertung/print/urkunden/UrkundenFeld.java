package de.df.jauswertung.print.urkunden;

import java.awt.Font;
import java.awt.Rectangle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class UrkundenFeld {

    private Rectangle bounds;
    private String text;
    private Font font;
    private int alignment;
}
