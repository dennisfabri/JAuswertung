package de.df.jauswertung.gui.plugins.http;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.ContentProducer;

public class DataProducer implements ContentProducer {

    private final String       name;
    private final DataProvider dp;

    public DataProducer(DataProvider dp, String target) {
        name = target;
        this.dp = dp;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        dp.sendData(out, name);
    }
}