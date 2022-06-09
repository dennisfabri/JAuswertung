package de.df.jauswertung.io.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

public final class HttpUtils {

    private HttpUtils() {
        // Hide constructor
    }

    private static HttpClientBuilder builder = HttpClientBuilder.create();

    public static byte[] download(String url) throws IOException {
        synchronized (builder) {
            return builder.build().execute(new HttpGet(url), resp -> {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resp.getEntity().writeTo(baos);
                return baos.toByteArray();
            });
        }
    }

    public static HttpEntity textToEntity(String text) {
        return new StringEntity(text, StandardCharsets.UTF_8);
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

            int result = response.getCode();
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

    private static CloseableHttpClient createAcceptSelfSignedCertificateClient()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        // use the TrustSelfSignedStrategy to allow Self Signed Certificates
        SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();

        // we can optionally disable hostname verification.
        // if you don't want to further weaken the security, you don't have to include
        // this.
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create().setSslContext(sslContext)
                        .setHostnameVerifier(allowAllHosts).setTlsVersions(TLS.V_1_3, TLS.V_1_2).build())
                .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(Timeout.ofSeconds(5)).build())
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT).setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setConnectionTimeToLive(TimeValue.ofMinutes(1L)).build();
        return HttpClients.custom().setConnectionManager(connectionManager)
                .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(Timeout.ofSeconds(60))
                        .setResponseTimeout(Timeout.ofSeconds(60)).setCookieSpec(StandardCookieSpec.STRICT).build())
                .build();
    }
}
