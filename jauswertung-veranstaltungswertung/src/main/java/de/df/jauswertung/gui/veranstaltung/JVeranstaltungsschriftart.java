package de.df.jauswertung.gui.veranstaltung;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;

import de.df.jutils.gui.fontchooser.JFontChooser;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.border.BorderUtils;

class JVeranstaltungsschriftart extends JPanel {

    @SuppressWarnings("unused")
    private final JVeranstaltungswertung parent;

    private final JFontChooser font = new JFontChooser();
    private boolean chosen;

    public JVeranstaltungsschriftart(JVeranstaltungswertung parent) {
        this.parent = parent;
        setBorder(BorderUtils.createLabeledBorder(I18n.get("Font")));
        setLayout(new BorderLayout(5, 5));
        add(font, BorderLayout.CENTER);
        font.addPropertyChangeListener(JFontChooser.SELECTED_FONT_CHANGED_KEY, evt -> chosen = true);
    }

    public Font getSelectedFont() {
        return chosen ? font.getSelectedFont() : null;
    }
}