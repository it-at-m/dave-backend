package de.muenchen.dave.services;

import de.muenchen.dave.domain.ConfigurationEntity;
import de.muenchen.dave.domain.dtos.init.ConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.MapConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.ZaehlstelleConfigurationDTO;
import de.muenchen.dave.domain.enums.ConfigDataTypes;
import de.muenchen.dave.repositories.relationaldb.ConfigurationRepository;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class ConfigurationService {

    private final ConfigurationRepository repository;

    private double latitude = 52.41988232741599;
    private double longitude = 10.779998775029739;
    private int zoom = 12;
    private boolean zaehlstelleAutomaticNumberAssignment = true;
    private String linkDocumentationCsvFileForUploadZaehlung = "https://github.com/it-at-m/dave/blob/main/docs/src/de/documentation-csv-for-upload.md";
    private String city = "München";

    public ConfigurationDTO getConfiguration() {

        for (ConfigurationEntity ce : repository.findAll()) {
            if ("city".equals(ce.getKeyname())) {
                city = ce.getValuefield();
            }
            if ("location_lat".equals(ce.getKeyname())) {
                latitude = Double.parseDouble(ce.getValuefield());
            }
            if ("location_lon".equals(ce.getKeyname())) {
                longitude = Double.parseDouble(ce.getValuefield());
            }
            if ("zoom".equals(ce.getKeyname())) {
                zoom = Integer.parseInt(ce.getValuefield());
            }
            if ("zaehlstelleAutomaticNumberAssignment".equals(ce.getKeyname())) {
                zaehlstelleAutomaticNumberAssignment = Boolean.parseBoolean(ce.getValuefield());
            }
            if ("linkDocumentationCsvFileForUploadZaehlung".equals(ce.getKeyname())) {
                linkDocumentationCsvFileForUploadZaehlung = ce.getValuefield();
            }
        }
        ZaehlstelleConfigurationDTO zaehlstelleConfig = new ZaehlstelleConfigurationDTO(
                zaehlstelleAutomaticNumberAssignment,
                linkDocumentationCsvFileForUploadZaehlung);
        MapConfigurationDTO mapConfiguration = new MapConfigurationDTO("" + latitude, "" + longitude, zoom);
        ConfigurationDTO configuration = new ConfigurationDTO(mapConfiguration, zaehlstelleConfig, city);
        return configuration;
    }

    public List<ConfigurationEntity> findAll() {
        return repository.findAll();
    }

    public ConfigurationEntity findByKeyname(String keyname) {
        return repository.findByKeyname(keyname);
    }

    public String getConfiguredCity() {
        ConfigurationEntity cityConfig = findByKeyname("city");
        if (cityConfig == null || cityConfig.getValuefield().isEmpty()) {
            log.warn("City not found in configuration, defaulting to 'München'");
            return "München";
        }
        return cityConfig.getValuefield();
    }

    public List<ConfigurationEntity> saveOrUpdateList(List<ConfigurationEntity> configs) throws IllegalArgumentException {
        configs.forEach(this::testTypeCorrectness);
        return repository.saveAll(configs);
    }

    public ConfigurationEntity saveOrUpdate(ConfigurationEntity config) throws IllegalArgumentException {
        testTypeCorrectness(config);

        ConfigurationEntity existingConfig = repository.findByKeyname(config.getKeyname());
        if (existingConfig != null) {
            existingConfig.setValuefield(config.getValuefield());
            existingConfig.setCategory(config.getCategory());
            existingConfig.setDatatype(config.getDatatype());
            return repository.save(existingConfig);
        } else {
            return repository.save(config);
        }
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    private void testTypeCorrectness(ConfigurationEntity config) throws IllegalArgumentException {
        ConfigDataTypes type = config.getDatatype();
        if (type == null) {
            throw new IllegalArgumentException("No type information found for: " + config.toString());
        }
        switch (type) {
        case INTEGER:
            try {
                Integer.parseInt(config.getValuefield());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Valuefield for key " + config.getKeyname() + " is not a valid INTEGER.");
            }
            break;
        case DOUBLE:
            try {
                Double.parseDouble(config.getValuefield());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Valuefield for key " + config.getKeyname() + " is not a valid DOUBLE.");
            }
            break;

        case BOOLEAN:
            if (!"true".equalsIgnoreCase(config.getValuefield())
                    && !"false".equalsIgnoreCase(config.getValuefield())) {
                throw new IllegalArgumentException(
                        "Valuefield for key " + config.getKeyname() + " is not a valid BOOLEAN.");
            }
            break;
        default:
            // String requires no special handling
            break;
        }
    }
}
