package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.repositories.relationaldb.CityDistrictRepository;
import de.muenchen.dave.services.ConfigurationService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StadtbezirkMapper {

    private Map<String, String> stadtbezirkeMap;

    private final CityDistrictRepository cityDistrictRepository;

    private final ConfigurationService configurationService;

    public String bezeichnungOf(@NonNull Integer stadtbezirkNummer) {
        var cityEntity = configurationService.findByKeyname("city");
        String city = "München";
        if (cityEntity == null || cityEntity.getValuefield().isEmpty()) {
            log.warn("City not found in configuration, defaulting to 'München'");
            city = "München";
        } else {
            city = cityEntity.getValuefield();
        }

        var districtName = cityDistrictRepository.findByNumberAndCity(stadtbezirkNummer, city);

        if (districtName == null) {
            log.warn("No city district found for number: {} and city: {}", stadtbezirkNummer, city);
            return "Unbekannt";
        }

        return districtName.getName();
    }
}
