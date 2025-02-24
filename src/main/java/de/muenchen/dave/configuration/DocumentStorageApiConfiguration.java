package de.muenchen.dave.configuration;

import de.muenchen.dave.documentstorage.gen.api.LageplanApi;
import de.muenchen.dave.documentstorage.gen.daten.ApiClient;
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
public class DocumentStorageApiConfiguration {

    @Value("${document-storage.url:}")
    public String documentStorageUrl;

    /**
     * Erstellt ein {@link LageplanApi} Bean für Requests an den document-storage
     */
    @Bean
    @Profile("no-security")
    public LageplanApi documentStorageApi() {
        final WebClient webClient = WebClient.builder().build();
        final ApiClient apiClient = this.documentStorageApiClient(webClient);
        return new LageplanApi(apiClient);
    }

    @Bean
    @Profile("!no-security")
    public LageplanApi securedDocumentStorageApi(final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService) {
        final WebClient webClient = this.webClient(clientRegistrationRepository, authorizedClientService);
        final ApiClient apiClient = documentStorageApiClient(webClient);
        return new LageplanApi(apiClient);
    }

    private ApiClient documentStorageApiClient(final WebClient webClient) {
        final var apiClient = new ApiClient(webClient);
        apiClient.setBasePath(documentStorageUrl);
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
