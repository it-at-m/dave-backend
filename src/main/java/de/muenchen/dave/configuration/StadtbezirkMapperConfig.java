/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

/**
 * Config for mapping stadtbezirke.
 */
@Configuration
@Slf4j
public class StadtbezirkMapperConfig {

    public static final String BEAN_STADTBEZIRK_MAPPING_PROPERTIES = "stadtbezirkMappingProperties";
    private static final String CLASS_PATH = "classpath:";

    @Value("${dave.stadtbezirk-mapping-config-url}")
    private String stadtbezirkMappingConfigUrl;

    @Bean(name = StadtbezirkMapperConfig.BEAN_STADTBEZIRK_MAPPING_PROPERTIES)
    public PropertiesFactoryBean stadtbezikMappingProperties() {
        final PropertiesFactoryBean bean = new PropertiesFactoryBean();
        log.info("Using Stadtbezirk Mapping from {}", stadtbezirkMappingConfigUrl);
        final org.springframework.core.io.Resource resource;
        if (stadtbezirkMappingConfigUrl.startsWith(CLASS_PATH)) {
            resource = new ClassPathResource(stadtbezirkMappingConfigUrl.substring(CLASS_PATH.length()));
        } else {
            resource = new FileSystemResource(stadtbezirkMappingConfigUrl);
        }
        bean.setLocation(resource);
        return bean;
    }

}
