package de.muenchen.dave.configuration;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicHeaderElementIterator;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.client.elc.rest5_client.Rest5Clients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "de.muenchen.dave.repositories.elasticsearch")
@Slf4j
public class CustomElasticsearchConfiguration extends ElasticsearchConfiguration {

    @Value(value = "${elasticsearch.password}")
    private String password;

    @Value(value = "${elasticsearch.user}")
    private String user;

    @Value(value = "${elasticsearch.host}")
    private String host;

    @Value(value = "${elasticsearch.port}")
    private int port;

    @Value(value = "${elasticsearch.connectTimeout}")
    private int connectTimeout;

    @Value(value = "${elasticsearch.socketTimeout}")
    private int socketTimeout;

    @Value(value = "${elasticsearch.http-ca-certificate:}")
    private String httpCaCertificate;

    @Override
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration.TerminalClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(this.host + ":" + this.port);

        // nur SSL/CACert setzen, wenn ein Wert vorhanden ist
        if (StringUtils.isNotBlank(this.httpCaCertificate)) {
            builder = ((ClientConfiguration.MaybeSecureClientConfigurationBuilder) builder).usingSsl(this.httpCaCertificate);
        }

        // ClientConfigurer anhängen
        builder = builder.withBasicAuth(this.user, this.password)
                .withConnectTimeout(Duration.ofSeconds(connectTimeout))
                .withSocketTimeout(Duration.ofSeconds(socketTimeout))
                .withClientConfigurer(Rest5Clients.ElasticsearchHttpClientConfigurationCallback
                        .from(clientBuilder -> {
                            /*
                             * Setzen der {@link org.apache.http.conn.ConnectionKeepAliveStrategy} in Millisekunden.
                             */
                            clientBuilder.setKeepAliveStrategy((httpResponse, httpContext) -> {
                                final BasicHeaderElementIterator headerIterator = new BasicHeaderElementIterator(
                                        httpResponse.headerIterator(HttpHeaders.KEEP_ALIVE));

                                while (headerIterator.hasNext()) {
                                    final var header = headerIterator.next();
                                    final var headerName = header.getName();
                                    final var headerValue = header.getValue();

                                    if (StringUtils.isNotEmpty(headerValue) && headerName.equalsIgnoreCase("timeout")) {
                                        try {
                                            final var timeoutSeconds = Long.parseLong(headerValue);
                                            // to millis
                                            return TimeValue.ofMilliseconds(timeoutSeconds * 1000);
                                        } catch (NumberFormatException ignore) {
                                        }
                                    }
                                }

                                // Connections nicht unendlich lange offen halten,
                                // da Netzwerk-Firewall sie sonst evtl. mit deny auslaufen lässt.
                                // 30000 Millisekunden
                                return TimeValue.ofMilliseconds(30 * 1000);
                            });
                            return clientBuilder;
                        }));

        return builder.build();
    }

}
