package de.df.jauswertung.gui.plugins.http;

import java.io.OutputStream;

public interface IDataProvider {
    public boolean provide(String path, OutputStream os);

    public void setKey(String key);

    public boolean knowsFile(String path);
}
