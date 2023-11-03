package de.muenchen.dave.configuration;

import de.muenchen.dave.geodateneai.gen.api.MessdatenApi;
import de.muenchen.dave.geodateneai.gen.geodaten.ApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class MessdatenApiConfiguration {

    /**
     * Erstellt ein {@link MessdatenApi} Bean für Requests an die Geodaten-EAI
     *
     * @param messdatenUrl URL für den Service-Aufruf von der Geodaten-Eai.
     */
    @Bean
    public MessdatenApi messdatenApi(@Value("${messdaten.url:}") final String messdatenUrl) {
        return new MessdatenApi(this.messdatenApiClient(messdatenUrl));
    }

    private ApiClient messdatenApiClient(final String messdatenUrl) {
        final var webClient = WebClient.builder().build();
        final var apiClient = new ApiClient(webClient);
        apiClient.setBasePath(messdatenUrl);
        return apiClient;
    }

}
