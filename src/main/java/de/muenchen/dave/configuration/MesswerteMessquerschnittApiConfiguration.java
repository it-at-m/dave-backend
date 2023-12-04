package de.muenchen.dave.configuration;

import de.muenchen.dave.geodateneai.gen.api.MesswerteMessquerschnittApi;
import de.muenchen.dave.geodateneai.gen.geodaten.ApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class MesswerteMessquerschnittApiConfiguration {

    @Value("${messwerte.messquerschnitt.url:}")
    public String messwerteMessquerschnittUrl;

    /**
     * Erstellt ein {@link MesswerteMessquerschnittApi} Bean für Requests an die Geodaten-EAI
     */
    @Bean
    @Profile("no-security")
    public MesswerteMessquerschnittApi messwerteMessquerschnittApi() {
        final WebClient webClient = WebClient.builder().build();
        final ApiClient apiClient = this.messwerteMessquerschnittApiClient(webClient);
        return new MesswerteMessquerschnittApi(apiClient);
    }

    @Bean
    @Profile("!no-security")
    public MesswerteMessquerschnittApi securedMesswerteMessquerschnittApi(final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService) {
        final WebClient webClient = this.webClient(clientRegistrationRepository, authorizedClientService);
        final ApiClient apiClient = messwerteMessquerschnittApiClient(webClient);
        return new MesswerteMessquerschnittApi(apiClient);
    }

    private ApiClient messwerteMessquerschnittApiClient(final WebClient webClient) {
        final var apiClient = new ApiClient(webClient);
        apiClient.setBasePath(messwerteMessquerschnittUrl);
        return apiClient;
    }

    private WebClient webClient(
            final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService) {
        final var oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService));
        oauth.setDefaultClientRegistrationId("keycloak");
        return WebClient.builder()
                .apply(oauth.oauth2Configuration())
                .build();
    }
}
