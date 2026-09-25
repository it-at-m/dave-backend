package de.muenchen.dave.configuration;

import de.muenchen.dave.openholidays.gen.api.HolidaysApi;
import de.muenchen.dave.openholidays.gen.geodaten.ApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

@Configuration
@RequiredArgsConstructor
@Profile({ "!konexternal && !prodexternal && !unittest" })
public class OpenHolidaysApiConfiguration {

    @Value("${dave.holidays.url}")
    public String openholidaysUrl;

    @Value("${internet.proxy}")
    public String proxyUrl;

    /**
     * Erstellt eine unsecured {@link HolidaysApi} Bean für Requests an openholidaysapi.org.
     */
    @Bean
    public HolidaysApi holidaysApi() {
        final WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient()))
                .build();
        final ApiClient apiClient = this.documentStorageApiClient(webClient);
        return new HolidaysApi(apiClient);
    }

    private ApiClient documentStorageApiClient(final WebClient webClient) {
        final var apiClient = new ApiClient(webClient);
        apiClient.setBasePath(openholidaysUrl);
        return apiClient;
    }

    private HttpClient httpClient() {
        return HttpClient.create().proxy(typeSpec -> typeSpec.type(ProxyProvider.Proxy.HTTP).host(proxyUrl).port(80).build());
    }
}
