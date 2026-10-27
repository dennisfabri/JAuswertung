package de.df.jauswertung.gui.plugins.elektronischezeit.sources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.lisasp.alphatiming.legacy.api.Heat;
import org.lisasp.alphatiming.legacy.api.InvalidLegacyXmlException;
import org.lisasp.alphatiming.legacy.api.LegacySerialization;

import de.df.jauswertung.gui.plugins.elektronischezeit.sources.TimesSourceException.Reason;

public class HttpTimesSource implements ITimesSource {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    /** AlphaTiming ends a request after 10 seconds itself. */
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(10);

    private record Response(int status, byte[] body) {
    }

    @Override
    public Heat[] getHeats() throws TimesSourceException {
        return load(SourcesConfig.getAddress(), CONNECT_TIMEOUT, RESPONSE_TIMEOUT);
    }

    static Heat[] load(String address, Duration connectTimeout, Duration responseTimeout) throws TimesSourceException {
        TimesServerAddress server = TimesServerAddress.parse(address).orElseThrow(() -> TimesSourceException.invalidAddress(address));
        return load(server, connectTimeout, responseTimeout);
    }

    static Heat[] load(TimesServerAddress server, Duration connectTimeout, Duration responseTimeout) throws TimesSourceException {
        Response response = download(server, connectTimeout, responseTimeout);
        if (response.status() != 200) {
            throw TimesSourceException.wrongServer(server, response.status());
        }
        try {
            return LegacySerialization.fromXML(new ByteArrayInputStream(response.body()));
        } catch (IOException e) {
            throw TimesSourceException.server(Reason.UNREADABLE_DATA, server, e);
        }
    }

    private static Response download(TimesServerAddress server, Duration connectTimeout, Duration responseTimeout) throws TimesSourceException {
        ConnectionConfig config = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(connectTimeout))
                .setSocketTimeout(Timeout.of(responseTimeout))
                .build();
        try (CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create().setDefaultConnectionConfig(config).build())
                .disableAutomaticRetries()
                .build()) {
            return client.execute(new HttpGet(server.heatsUrl()), r -> new Response(r.getCode(), EntityUtils.toByteArray(r.getEntity())));
        } catch (UnknownHostException e) {
            throw TimesSourceException.server(Reason.UNKNOWN_HOST, server, e);
        } catch (SocketTimeoutException e) {
            throw TimesSourceException.server(Reason.NO_ANSWER, server, e);
        } catch (ConnectException e) {
            throw TimesSourceException.server(Reason.NOT_RUNNING, server, e);
        } catch (IOException e) {
            throw TimesSourceException.server(Reason.CONNECTION_FAILED, server, e);
        }
    }
}
