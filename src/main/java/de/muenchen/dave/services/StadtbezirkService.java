package de.muenchen.dave.services;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.dtos.StadtbezirkAndViertelDTO;
import de.muenchen.dave.domain.dtos.StadtbezirkDTO;
import de.muenchen.dave.domain.dtos.StadtbezirkViertelDTO;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.StadtbezirkViertelMapper;
import de.muenchen.dave.repositories.relationaldb.StadtbezirkRepository;
import de.muenchen.dave.repositories.relationaldb.StadtbezirkViertelRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StadtbezirkService {

    private static final String STADTBEZIRK_UNBEKANNT = "Unbekannt";

    private final StadtbezirkRepository stadtbezirkRepository;
    private final StadtbezirkViertelRepository stadtbezirkViertelRepository;
    private final StadtbezirkMapper stadtbezirkMapper;
    private final StadtbezirkViertelMapper stadtbezirkViertelMapper;
    private final ConfigurationService configurationService;

    public List<StadtbezirkViertelDTO> getStadtbezirkViertelByStadt(final String stadt) {
        return stadtbezirkViertelMapper.entities2Dtos(stadtbezirkViertelRepository.findAllByStadtbezirk_Stadt(stadt));
    }

    public List<StadtbezirkDTO> getStadtbezirkByStadt(final String stadt) {
        return stadtbezirkMapper.entities2Dtos(stadtbezirkRepository.findByStadt(stadt));
    }

    public StadtbezirkDTO getStadtbezirkByStadtAndNummer(final String stadt, final Integer stadtbezirkNummer) {
        return stadtbezirkMapper.entity2Dto(stadtbezirkRepository.findByStadtAndNummer(stadt, stadtbezirkNummer));
    }

    @Cacheable(
            value = CachingConfiguration.STADTBEZIRK_NAME,
            key = "{@configurationService.configuration.tenant.city, #p0}"
    )
    public String getStadtbezirkName(final Integer stadtbezirkNummer) {
        final String stadt = configurationService.getConfiguration().getTenant().getCity();
        if (stadt == null)
            return STADTBEZIRK_UNBEKANNT;
        final StadtbezirkDTO stadtbezirk = getStadtbezirkByStadtAndNummer(stadt, stadtbezirkNummer);
        return stadtbezirk != null ? stadtbezirk.getName() : STADTBEZIRK_UNBEKANNT;
    }

    public StadtbezirkAndViertelDTO getStadtbezirkeAndViertel() {
        var dto = new StadtbezirkAndViertelDTO();
        final String city = configurationService.getConfiguration().getTenant().getCity();
        if (city != null) {
            List<StadtbezirkDTO> stadtbezirke = getStadtbezirkByStadt(city);
            dto.setStadtbezirke(stadtbezirke);

            List<StadtbezirkViertelDTO> stadtviertel = getStadtbezirkViertelByStadt(city);
            dto.setStadtbezirkViertel(stadtviertel);
        }
        return dto;
    }
}
