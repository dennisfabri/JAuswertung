package de.df.jauswertung.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

public final class HttpUtils {

    private HttpUtils() {
        // Hide constructor
    }

    private static HttpClientBuilder builder = HttpClientBuilder.create();

    public static byte[] download(String url) throws IOException {
        synchronized (builder) {
            HttpClient httpclient = builder.build();
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            InputStream instream = entity.getContent();
            byte[] tmp = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int i = instream.read(tmp);
            while (i >= 0) {
                bos.write(tmp, 0, i);
                i = instream.read(tmp);
            }
            return bos.toByteArray();
        }
    }

    public static HttpEntity textToEntity(String text) {
        return new StringEntity(text, "utf-8");
    }

    public static int post(String uri, String text) {
        return post(uri, textToEntity(text));
    }

    public static int post(String uri, HttpEntity data) {
        try {
            CloseableHttpClient httpclient = createAcceptSelfSignedCertificateClient(); // HttpClients.createDefault();

            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(data);
            CloseableHttpResponse response = httpclient.execute(httpPost);

            int result = response.getStatusLine().getStatusCode();
            try {
                HttpEntity entity = response.getEntity();
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            httpclient.close();
            return result;
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            // e.printStackTrace();
            return -1;
        }
    }

    private static CloseableHttpClient createAcceptSelfSignedCertificateClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        // use the TrustSelfSignedStrategy to allow Self Signed Certificates
        SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();

        // we can optionally disable hostname verification.
        // if you don't want to further weaken the security, you don't have to include this.
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

        // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
        // and allow all hosts verifier.
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);

        // finally create the HttpClient using HttpClient factory methods and assign the ssl socket factory
        return HttpClients.custom().setSSLSocketFactory(connectionFactory).build();
    }
}
