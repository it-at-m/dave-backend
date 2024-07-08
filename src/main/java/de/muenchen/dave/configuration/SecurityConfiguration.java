/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.configuration;

import de.muenchen.dave.security.CustomJwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * The central class for configuration of all security aspects.
 */
@Configuration
@Profile("!no-security")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    /**
     * Absichern der Rest-Endpunkte mit Definition der Ausnahmen.
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception falls was passiert
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request ->
                        request
                                .requestMatchers(
                                        AntPathRequestMatcher.antMatcher("/lade-auswertung-spitzenstunde"),
                                        AntPathRequestMatcher.antMatcher( "/lade-auswertung-zaehlstellen-koordinate"),
                                        AntPathRequestMatcher.antMatcher("/lade-auswertung-visum"),
                                        // allow access to /actuator/info
                                        AntPathRequestMatcher.antMatcher("/actuator/info"),
                                        // allow access to /actuator/health for OpenShift Health Check
                                        AntPathRequestMatcher.antMatcher("/actuator/health"),
                                        // allow access to /actuator/health/liveness for OpenShift Liveness Check
                                        AntPathRequestMatcher.antMatcher("/actuator/health/liveness"),
                                        // allow access to /actuator/health/readiness for OpenShift Readiness Check
                                        AntPathRequestMatcher.antMatcher("/actuator/health/readiness"),
                                        // allow access to /actuator/metrics for Prometheus monitoring in OpenShift
                                        AntPathRequestMatcher.antMatcher("/actuator/metrics"),
                                        // h2-console
                                        AntPathRequestMatcher.antMatcher("/h2-console/**")
                                )
                                .permitAll()
                                .requestMatchers(AntPathRequestMatcher.antMatcher("/**"))
                                .authenticated()
                )
                .headers(httpSecurityHeadersConfigurer ->
                        // support frames for same-origin (e.g. h2-console)
                        httpSecurityHeadersConfigurer.frameOptions(
                                HeadersConfigurer.FrameOptionsConfig::sameOrigin
                        )
                )
                .csrf(httpSecurityCsrfConfigurer ->
                        // exclude csrf for h2-console
                        httpSecurityCsrfConfigurer.ignoringRequestMatchers(
                                AntPathRequestMatcher.antMatcher("/h2-console/**")
                        )
                )
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(jwt ->
                                // Verwenden eines CustomConverters um die Rechte vom UserInfoEndpunkt zu extrahieren.
                                jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter)
                        )
                );
        return http.build();
    }

    @Bean
    public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceAndManager(
            final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService) {

        final OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials()
                .build();

        final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
