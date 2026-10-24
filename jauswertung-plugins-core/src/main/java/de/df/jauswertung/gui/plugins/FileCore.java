package de.df.jauswertung.gui.plugins;

/**
 * The file operations of a core plugin that {@link AFilePlugin} offers in the file menu.
 */
public interface FileCore {

    boolean load(String filename);

    boolean save();

    boolean saveAs(String filename);

    String getFilename();
}
