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
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

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
                .antMatcher("/**").authorizeRequests()
                .antMatchers(
                        "/lade-auswertung-spitzenstunde",
                        "/lade-auswertung-zaehlstellen-koordinate",
                        "/lade-auswertung-visum")
                .permitAll()
                // allow access to /actuator/infoZaehlungStatusUpdater
                .antMatchers("/actuator/info").permitAll()
                // allow access to /actuator/health for OpenShift Health Check
                .antMatchers("/actuator/health").permitAll()
                // allow access to /actuator/health/liveness for OpenShift Liveness Check
                .antMatchers("/actuator/health/liveness").permitAll()
                // allow access to /actuator/health/readiness for OpenShift Readiness Check
                .antMatchers("/actuator/health/readiness").permitAll()
                // allow access to /actuator/metrics for Prometheus monitoring in OpenShift
                .antMatchers("/actuator/metrics").permitAll()
                // h2-console
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/**").authenticated()
                .and()
                // support frames for same-origin (e.g. h2-console)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                // exlucde csrf for h2-console
                .csrf().ignoringAntMatchers("/h2-console/**")
                .and()
                .oauth2ResourceServer()
                .jwt()
                // Verwenden eines CustomConverters um die Rechte vom UserInfoEndpunkt zu extrahieren.
                .jwtAuthenticationConverter(this.customJwtAuthenticationConverter);
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
