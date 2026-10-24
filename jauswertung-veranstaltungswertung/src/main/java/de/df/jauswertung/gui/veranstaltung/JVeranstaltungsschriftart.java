package de.df.jauswertung.gui.veranstaltung;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;

import de.df.jutils.gui.fontchooser.JFontChooser;

class JVeranstaltungsschriftart extends JPanel {

    private final JFontChooser font = new JFontChooser();
    private boolean chosen;

    public JVeranstaltungsschriftart() {
        setLayout(new BorderLayout(5, 5));
        add(font, BorderLayout.CENTER);
        font.addPropertyChangeListener(JFontChooser.SELECTED_FONT_CHANGED_KEY, evt -> chosen = true);
    }

    public Font getSelectedFont() {
        return chosen ? font.getSelectedFont() : null;
    }
}