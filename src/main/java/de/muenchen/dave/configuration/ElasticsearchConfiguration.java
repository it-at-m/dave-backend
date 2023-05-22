package de.muenchen.dave.configuration;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "de.muenchen.dave.repositories.elasticsearch")
@Slf4j
public class ElasticsearchConfiguration {

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

    @Bean
    public RestHighLevelClient client() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(this.host + ":" + this.port)
                .withBasicAuth(this.user, this.password)
                .withConnectTimeout(Duration.ofSeconds(connectTimeout))
                .withSocketTimeout(Duration.ofSeconds(socketTimeout))
                .build();

        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate(RestHighLevelClient client) {
        return new ElasticsearchRestTemplate(client);
    }

}
