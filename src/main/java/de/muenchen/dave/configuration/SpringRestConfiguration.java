package de.muenchen.dave.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy.RepositoryDetectionStrategies;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class SpringRestConfiguration implements RepositoryRestConfigurer {

    /**
     * The method returns a {@link Validator} to get correct validation error messages.
     *
     * @return A {@link LocalValidatorFactoryBean} to get correct validation error messages.
     */
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * See {@link RepositoryRestConfigurer#configureRepositoryRestConfiguration}
     */
    @Override
    public void configureRepositoryRestConfiguration(final RepositoryRestConfiguration config, final CorsRegistry corsRegistry) {
        config.setRepositoryDetectionStrategy(RepositoryDetectionStrategies.DEFAULT);
    }

    /**
     * See {@link RepositoryRestConfigurer#configureValidatingRepositoryEventListener}
     */
    @Override
    public void configureValidatingRepositoryEventListener(final ValidatingRepositoryEventListener validatingListener) {
        validatingListener.addValidator("afterCreate", this.validator());
        validatingListener.addValidator("beforeCreate", this.validator());
        validatingListener.addValidator("afterSave", this.validator());
        validatingListener.addValidator("beforeSave", this.validator());
        validatingListener.addValidator("beforeLinkSave", this.validator());
        validatingListener.addValidator("afterLinkSave", this.validator());
    }

}
