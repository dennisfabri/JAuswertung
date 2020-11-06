package de.df.jauswertung.gui.veranstaltung;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;

import com.l2fprod.common.fontchooser.JFontChooser;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.border.BorderUtils;

class JVeranstaltungsschriftart extends JPanel {

    @SuppressWarnings("unused")
    private final JVeranstaltungswertung parent;

    private JFontChooser font = new JFontChooser();

    public JVeranstaltungsschriftart(JVeranstaltungswertung parent) {
        this.parent = parent;
        setBorder(BorderUtils.createLabeledBorder(I18n.get("Font")));
        setLayout(new BorderLayout(5, 5));
        add(font, BorderLayout.CENTER);
    }

    public Font getSelectedFont() {
        return font.getSelectedFont();
    }
}