package de.muenchen.dave.configuration;

import de.muenchen.dave.domain.BaseEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
public class EtagConfiguration {

    /**
     * Bean zur Aktivierung der Versionskontrolle bei Entitäten für die relationale Datenbank.
     * <p>
     * Das relevante Attribut ist {@link BaseEntity#getEntityVersion()}.
     *
     * @return new ShallowEtagHeaderFilter
     */
    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }

}
