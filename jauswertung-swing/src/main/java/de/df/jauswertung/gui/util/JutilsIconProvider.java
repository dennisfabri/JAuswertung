package de.df.jauswertung.gui.util;

import javax.swing.ImageIcon;

import de.df.jutils.swing.icons.Icons.IconProvider;

/**
 * Supplies JAuswertung's icons to jutils, e.g. for the validation markers of JWarningTextField.
 */
final class JutilsIconProvider implements IconProvider {

    @Override
    public ImageIcon getErrorIcon() {
        return IconManager.getSmallIcon("validationerror");
    }

    @Override
    public ImageIcon getWarningIcon() {
        return IconManager.getSmallIcon("validationwarning");
    }
}
