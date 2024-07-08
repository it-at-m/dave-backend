package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.configuration.StadtbezirkMapperConfig;
import java.util.Map;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StadtbezirkMapper {

    @Resource(name = StadtbezirkMapperConfig.BEAN_STADTBEZIRK_MAPPING_PROPERTIES)
    private Map<String, String> stadtbezirkeMap;

    public String bezeichnungOf(@NonNull Integer stadtbezirkNummer) {
        return stadtbezirkeMap.get(stadtbezirkNummer.toString());
    }
}
