package de.muenchen.dave.configuration;

import de.muenchen.dave.geodateneai.gen.api.MesswerteMessquerschnittApi;
import de.muenchen.dave.geodateneai.gen.geodaten.ApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class MesswerteMessquerschnittApiConfiguration {

    /**
     * Erstellt ein {@link MesswerteMessquerschnittApi} Bean für Requests an die Geodaten-EAI
     *
     * @param messwerteMessquerschnittUrl URL für den Service-Aufruf von der Geodaten-Eai.
     */
    @Bean
    public MesswerteMessquerschnittApi messwerteMessquerschnittApi(@Value("${messwerte.messquerschnitt.url:}") final String messwerteMessquerschnittUrl) {
        return new MesswerteMessquerschnittApi(this.messwerteMessquerschnittApiClient(messwerteMessquerschnittUrl));
    }

    private ApiClient messwerteMessquerschnittApiClient(final String messwerteMessquerschnittUrl) {
        final var webClient = WebClient.builder().build();
        final var apiClient = new ApiClient(webClient);
        apiClient.setBasePath(messwerteMessquerschnittUrl);
        return apiClient;
    }

}
