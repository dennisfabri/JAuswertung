package de.df.jauswertung.gui.plugins.http;

import java.io.IOException;
import java.util.Locale;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.MethodNotSupportedException;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;

class DPRequestHandler implements HttpRequestHandler {

    private final DataProvider dp;

    public DPRequestHandler(DataProvider dp) {
        this.dp = dp;
    }

    @Override
    public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
            throws HttpException, IOException {
        String method = request.getMethod().toUpperCase(Locale.ENGLISH);
        if (!(method.equals("GET") || method.equals("POST"))) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        String target = request.getRequestUri();

        if (dp.knowsFile(target)) {
            DataProducer producer = new DataProducer(dp, target);
            response.setCode(HttpStatus.SC_OK);
            byte[] data = producer.writeTo();
            ByteArrayEntity body = new ByteArrayEntity(data, producer.getContentType());
            response.setEntity(body);
        } else {
            response.setCode(HttpStatus.SC_NOT_FOUND);
            StringEntity body = new StringEntity("<html><body><h1>Page not found</h1></body></html>",
                    ContentType.TEXT_HTML);
            response.setEntity(body);
        }
    }

}
