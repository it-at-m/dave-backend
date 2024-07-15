package de.muenchen.dave.configuration;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
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

    @Value(value = "${elasticsearch.http-ca-certificate}")
    private String httpCaCertificate;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(this.host + ":" + this.port)
                .usingSsl(httpCaCertificate)
                .withBasicAuth(this.user, this.password)
                .withConnectTimeout(Duration.ofSeconds(connectTimeout))
                .withSocketTimeout(Duration.ofSeconds(socketTimeout))
                .build();
    }

}
