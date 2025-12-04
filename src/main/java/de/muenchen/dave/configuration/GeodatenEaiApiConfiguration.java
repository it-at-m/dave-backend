package de.muenchen.dave.configuration;

import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.api.MesswerteApi;
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
public class GeodatenEaiApiConfiguration {

    @Value("${geodaten.eai.url:}")
    public String geodatenEaiUrl;

    /**
     * Erstellt ein {@link MesswerteApi} Bean für Requests an die Geodaten-EAI
     */
    @Bean
    @Profile("no-security")
    public MesswerteApi messwerteApi() {
        final WebClient webClient = WebClient.builder().build();
        final ApiClient apiClient = this.geodatenEaiApiClient(webClient);
        return new MesswerteApi(apiClient);
    }

    @Bean
    @Profile("no-security")
    public MessstelleApi messstelleApi() {
        final WebClient webClient = WebClient.builder().build();
        final ApiClient apiClient = this.geodatenEaiApiClient(webClient);
        return new MessstelleApi(apiClient);
    }

    @Bean
    @Profile("!no-security")
    public MesswerteApi securedMesswerteApi(final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService) {
        final WebClient webClient = this.webClient(clientRegistrationRepository, authorizedClientService);
        final ApiClient apiClient = geodatenEaiApiClient(webClient);
        return new MesswerteApi(apiClient);
    }

    @Bean
    @Profile("!no-security")
    public MessstelleApi securedMessstelleApi(final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService) {
        final WebClient webClient = this.webClient(clientRegistrationRepository, authorizedClientService);
        final ApiClient apiClient = geodatenEaiApiClient(webClient);
        return new MessstelleApi(apiClient);
    }

    private ApiClient geodatenEaiApiClient(final WebClient webClient) {
        final var apiClient = new ApiClient(webClient);
        apiClient.setBasePath(geodatenEaiUrl);
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
