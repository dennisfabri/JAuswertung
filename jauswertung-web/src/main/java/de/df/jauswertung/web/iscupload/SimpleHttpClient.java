package de.df.jauswertung.web.iscupload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHttpClient {

    private static Logger log = LoggerFactory.getLogger(SimpleHttpClient.class);

    public boolean put(String url, String resultsAsCsv) throws IOException {
        HttpPut putRequest = new HttpPut(url);
        putRequest.setEntity(new BasicHttpEntity(
                new ByteArrayInputStream(resultsAsCsv.getBytes(StandardCharsets.UTF_8)), ContentType.TEXT_PLAIN));

        try (CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(putRequest)) {
            log.debug("Upload with response code {}: {}", response.getCode(), response.getReasonPhrase());
            return isValid(response.getCode());
        }
    }

    private boolean isValid(int returnCode) {
        return 200 <= returnCode && returnCode < 300;
    }
}
