package de.df.jauswertung.gui.plugins.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

class DPRequestHandler implements HttpRequestHandler {

    private final DataProvider dp;

    public DPRequestHandler(DataProvider dp) {
        this.dp = dp;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!(method.equals("GET") || method.equals("POST"))) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        String target = request.getRequestLine().getUri();

        if (dp.knowsFile(target)) {
            DataProducer producer = new DataProducer(dp, target);
            response.setStatusCode(HttpStatus.SC_OK);
            EntityTemplate body = new EntityTemplate(producer);
            response.setEntity(body);
        } else {
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            EntityTemplate body = new EntityTemplate(new ContentProducer() {
                @Override
                public void writeTo(final OutputStream outstream) throws IOException {
                    OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                    writer.write("<html><body><h1>");
                    writer.write("Page not found");
                    writer.write("</h1></body></html>");
                    writer.flush();
                }
            });
            body.setContentType("text/html; charset=UTF-8");
            response.setEntity(body);
        }
    }

}
