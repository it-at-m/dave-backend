package de.muenchen.dave.configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.IntegrityCheckerConfig;
import com.hazelcast.config.MapConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * This class provides the caches. To disable the caching functionality delete this class, remove
 * the corresponding bean creation methods or remove the
 * annotation {@link EnableCaching} above the class definition.
 */
@Configuration
@EnableCaching
@Slf4j
public class CachingConfiguration {

    public static final String SUCHE_ERHEBUNGSSTELLE = "SUCHE_ERHEBUNGSSTELLE";

    public static final String SUCHE_ERHEBUNGSSTELLE_DATENPORTAL = "SUCHE_ERHEBUNGSSTELLE_DATENPORTAL";

    public static final String LADE_PROCESSED_ZAEHLDATEN = "LADE_PROCESSED_ZAEHLDATEN";

    public static final String LADE_BELASTUNGSPLAN_DTO = "LADE_BELASTUNGSPLAN_DTO";

    public static final String LADE_ZAEHLDATEN_ZEITREIHE_DTO = "LADE_ZAEHLDATEN_ZEITREIHE_DTO";

    public static final String READ_ZAEHLSTELLE_DTO = "READ_ZAEHLSTELLE_DTO";

    public static final String OPTIONSMENUE_SETTINGS_FOR_MESSSTELLEN = "OPTIONSMENUE_SETTINGS_FOR_MESSSTELLEN";

    @Value("${hazelcast.instance:data_hazl_instance}")
    public String hazelcastInstanceName;
    @Value("${hazelcast.group-name:data_hazl_group}")
    public String groupConfigName;
    @Value("${hazelcast.openshift-service-name:backend}")
    public String openshiftServiceName;
    @Value("${hazelcast.openshift-namespace:dave}")
    public String openshiftNamespace;
    // 60*60*12 = 7200 = 2h
    @Value("${hazelcast.max-idle-time-seconds.suchergebnisse:7200}")
    public int maxIdleTimeSecondsSuchergebnisse;
    // 60*30 = 1800 = 30m
    @Value("${hazelcast.max-idle-time-seconds.zaehldaten:1800}")
    public int maxIdleTimeSecondsZaehldaten;

    @Bean
    @Profile({ "local", "docker", "unittest" })
    public Config localConfig() {

        final var config = new Config();
        config.setInstanceName(this.hazelcastInstanceName);
        config.setClusterName(this.groupConfigName);

        this.mapConfig(config);

        final var joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig()
                .setEnabled(true)
                .addMember("127.0.0.1");

        // Integrity Check
        final var integrityCheckerConfig = new IntegrityCheckerConfig();
        integrityCheckerConfig.setEnabled(true);
        config.setIntegrityCheckerConfig(integrityCheckerConfig);

        return config;
    }

    @Bean
    @Profile({ "dev", "kon", "prod", "hotfix", "demo" })
    public Config config() {

        log.info("Value hazelcast.instance: {}", this.hazelcastInstanceName);
        log.info("Value hazelcast.group-name: {}", this.groupConfigName);
        log.info("Value hazelcast.openshift-service-name: {}", this.openshiftServiceName);
        log.info("Value hazelcast.max-idle-time-seconds.suchergebnisse: {}", this.maxIdleTimeSecondsSuchergebnisse);
        log.info("Value hazelcast.max-idle-time-seconds.zaehldaten; {}", this.maxIdleTimeSecondsZaehldaten);

        final var config = new Config();
        config.setInstanceName(this.hazelcastInstanceName);
        config.setClusterName(this.groupConfigName);

        this.mapConfig(config);

        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                //If we dont set a specific name, it would call -all- services within a namespace
                .setProperty("service-name", this.openshiftServiceName)
                .setProperty("namespace", this.openshiftNamespace);

        // Integrity Check
        final var integrityCheckerConfig = new IntegrityCheckerConfig();
        integrityCheckerConfig.setEnabled(true);
        config.setIntegrityCheckerConfig(integrityCheckerConfig);

        return config;
    }

    private void mapConfig(final Config config) {
        config.addMapConfig(this.getMapConfig(SUCHE_ERHEBUNGSSTELLE, this.maxIdleTimeSecondsSuchergebnisse));
        config.addMapConfig(this.getMapConfig(SUCHE_ERHEBUNGSSTELLE_DATENPORTAL, this.maxIdleTimeSecondsSuchergebnisse));
        config.addMapConfig(this.getMapConfig(LADE_BELASTUNGSPLAN_DTO, this.maxIdleTimeSecondsZaehldaten));
        config.addMapConfig(this.getMapConfig(LADE_PROCESSED_ZAEHLDATEN, this.maxIdleTimeSecondsZaehldaten));
        config.addMapConfig(this.getMapConfig(LADE_ZAEHLDATEN_ZEITREIHE_DTO, this.maxIdleTimeSecondsZaehldaten));
        config.addMapConfig(this.getMapConfig(READ_ZAEHLSTELLE_DTO, this.maxIdleTimeSecondsZaehldaten));
        config.addMapConfig(this.getMapConfig(OPTIONSMENUE_SETTINGS_FOR_MESSSTELLEN, this.maxIdleTimeSecondsZaehldaten));
    }

    private MapConfig getMapConfig(final String name, final int maxIdleTime) {
        final var mapConfig = new MapConfig();
        mapConfig.setName(name);
        // Maximum time in seconds for each entry to stay idle in the map
        // 0 means infinite
        mapConfig.setMaxIdleSeconds(maxIdleTime);
        mapConfig.setBackupCount(0);
        return mapConfig;
    }

}
