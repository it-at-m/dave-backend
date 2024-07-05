/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.IntegrityCheckerConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import de.muenchen.dave.security.CustomUserInfoTokenServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * This class provides the caches.
 * To disable the caching functionality delete this class, remove the corresponding bean creation
 * methods
 * or remove the annotation {@link EnableCaching} above the class definition.
 */
@Configuration
@EnableCaching
public class CachingConfiguration {

    public static final String SUCHE_ZAEHLSTELLE = "SUCHE_ZAEHLSTELLE";
    public static final String SUCHE_ZAEHLSTELLE_DATENPORTAL = "SUCHE_ZAEHLSTELLE_DATENPORTAL";
    public static final String LADE_PROCESSED_ZAEHLDATEN = "LADE_PROCESSED_ZAEHLDATEN";
    public static final String LADE_BELASTUNGSPLAN_DTO = "LADE_BELASTUNGSPLAN_DTO";
    public static final String LADE_ZAEHLDATEN_ZEITREIHE_DTO = "LADE_ZAEHLDATEN_ZEITREIHE_DTO";
    public static final String READ_ZAEHLSTELLE_DTO = "READ_ZAEHLSTELLE_DTO";
    private static final int AUTHENTICATION_CACHE_EXPIRATION_TIME_SECONDS = 60;

    // 60*60*12 = 43200 = 12h
    private static final int MAX_IDLE_TIME_IN_SECONDS = 60 * 60 * 12;

    @Value("${hazelcast.instance:data_hazl_instance}")
    public String hazelcastInstanceName;
    @Value("${hazelcast.group-name:data_hazl_group}")
    public String groupConfigName;
    @Value("${hazelcast.openshift-service-name:backend}")
    public String openshiftServiceName;

    @Bean
    @Profile({ "local", "test" })
    public Config localConfig() {

        final Config config = new Config();
        config.setInstanceName(this.hazelcastInstanceName);
        config.setClusterName(this.groupConfigName);

        this.mapConfig(config);

        final JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig()
                .setEnabled(true)
                .addMember("127.0.0.1");

        // Integrity Check
        final IntegrityCheckerConfig integrityCheckerConfig = new IntegrityCheckerConfig();
        integrityCheckerConfig.setEnabled(true);
        config.setIntegrityCheckerConfig(integrityCheckerConfig);

        return config;
    }

    @Bean
    @Profile({ "dev", "kon", "prod", "hotfix", "demo" })
    public Config config() {

        final Config config = new Config();
        config.setInstanceName(this.hazelcastInstanceName);
        config.setClusterName(this.groupConfigName);

        this.mapConfig(config);

        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                //If we dont set a specific name, it would call -all- services within a namespace
                .setProperty("service-name", this.openshiftServiceName);

        // Integrity Check
        final IntegrityCheckerConfig integrityCheckerConfig = new IntegrityCheckerConfig();
        integrityCheckerConfig.setEnabled(true);
        config.setIntegrityCheckerConfig(integrityCheckerConfig);

        return config;
    }

    private void mapConfig(final Config config) {
        config.addMapConfig(this.getMapConfig(CustomUserInfoTokenServices.NAME_AUTHENTICATION_CACHE, AUTHENTICATION_CACHE_EXPIRATION_TIME_SECONDS)
                .setTimeToLiveSeconds(AUTHENTICATION_CACHE_EXPIRATION_TIME_SECONDS));
        config.addMapConfig(this.getMapConfig(SUCHE_ZAEHLSTELLE, 0));
        config.addMapConfig(this.getMapConfig(SUCHE_ZAEHLSTELLE_DATENPORTAL, 0));
        config.addMapConfig(this.getMapConfig(LADE_BELASTUNGSPLAN_DTO, MAX_IDLE_TIME_IN_SECONDS));
        config.addMapConfig(this.getMapConfig(LADE_PROCESSED_ZAEHLDATEN, MAX_IDLE_TIME_IN_SECONDS));
        config.addMapConfig(this.getMapConfig(LADE_ZAEHLDATEN_ZEITREIHE_DTO, MAX_IDLE_TIME_IN_SECONDS));
        config.addMapConfig(this.getMapConfig(READ_ZAEHLSTELLE_DTO, MAX_IDLE_TIME_IN_SECONDS));
    }

    private MapConfig getMapConfig(final String name, final int maxIdleTime) {
        final MapConfig mapConfig = new MapConfig();
        mapConfig.setName(name);
        // Maximum time in seconds for each entry to stay idle in the map
        // 0 means infinite
        mapConfig.setMaxIdleSeconds(maxIdleTime);
        return mapConfig;
    }

}
