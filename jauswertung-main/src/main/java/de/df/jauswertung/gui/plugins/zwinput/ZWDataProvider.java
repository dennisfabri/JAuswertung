package de.df.jauswertung.gui.plugins.zwinput;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import de.df.jauswertung.gui.plugins.http.HttpServerPlugin;
import de.df.jauswertung.gui.plugins.http.IDataProvider;
import de.df.jauswertung.util.BarcodeUtils;

public class ZWDataProvider implements IDataProvider {

    private final JZWBarcodeInputPanel barcode;
    private final HttpServerPlugin     http;
    private String                     key;
    private final String               prefix = "JA11";

    public ZWDataProvider(JZWBarcodeInputPanel barcode, HttpServerPlugin http) {
        this.barcode = barcode;
        this.http = http;
    }

    @Override
    public boolean provide(String path, OutputStream os) {
        boolean ok = barcode.input(path);
        return ok;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
        try {
            barcode.setAppcode(
                    BarcodeUtils.getQRCodeImage(prefix + "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + http.getPort() + "/" + key, 200));
        } catch (UnknownHostException e) {
            // Nothing to do
        }
        System.out.println("ZW-Key: " + key);
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean knowsFile(String path) {
        if (!path.startsWith(prefix)) {
            return false;
        }
        return barcode.acceptsInput(path.substring(4));
    }
}
