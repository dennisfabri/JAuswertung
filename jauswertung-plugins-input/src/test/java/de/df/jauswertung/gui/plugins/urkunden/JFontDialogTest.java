package de.df.jauswertung.gui.plugins.urkunden;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class JFontDialogTest {

    private static final Font FONT = new Font("Serif", Font.BOLD, 18);

    private JFontDialog dialog;
    private JFrame frame;

    private void open() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SwingUtilities.invokeAndWait(() -> {
            frame = new JFrame();
            dialog = new JFontDialog(frame);
        });
    }

    private void close() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            dialog.dispose();
            frame.dispose();
        });
        removeSavedWindowState();
    }

    private static void removeSavedWindowState() throws BackingStoreException {
        Preferences root = Preferences.userRoot();
        if (root.nodeExists("uistate/null/FontDialog")) {
            root.node("uistate/null/FontDialog").removeNode();
        }
    }

    @Test
    void acceptedFontIsReturnedWithFamilyAndSize() throws Exception {
        open();
        try {
            SwingUtilities.invokeAndWait(() -> {
                dialog.setSelectedFont(FONT);
                dialog.doOk();
            });

            Font selected = dialog.getSelectedFont();

            assertEquals(FONT.getFamily(), selected.getFamily());
            assertEquals(18, selected.getSize());
        } finally {
            close();
        }
    }

    @Test
    void nothingIsReturnedWhenTheDialogWasNotAccepted() throws Exception {
        open();
        try {
            SwingUtilities.invokeAndWait(() -> dialog.setSelectedFont(FONT));

            assertNull(dialog.getSelectedFont());
        } finally {
            close();
        }
    }
}
