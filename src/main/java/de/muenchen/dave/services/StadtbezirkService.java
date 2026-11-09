package de.muenchen.dave.services;

import de.muenchen.dave.domain.dtos.StadtbezirkAndViertelDTO;
import de.muenchen.dave.domain.dtos.StadtbezirkDTO;
import de.muenchen.dave.domain.dtos.StadtbezirkViertelDTO;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.StadtbezirkViertelMapper;
import de.muenchen.dave.repositories.relationaldb.StadtbezirkRepository;
import de.muenchen.dave.repositories.relationaldb.StadtbezirkViertelRepository;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class StadtbezirkService {

    private final StadtbezirkRepository stadtbezirkRepository;
    private final StadtbezirkViertelRepository stadtbezirkViertelRepository;
    private final StadtbezirkMapper stadtbezirkMapper;
    private final StadtbezirkViertelMapper stadtbezirkViertelMapper;

    public List<StadtbezirkViertelDTO> findStadtbezirkViertelByStadt(final String stadt) {
        return stadtbezirkViertelMapper.entities2Dtos(stadtbezirkViertelRepository.findAllByStadtbezirk_Stadt(stadt));
    }

    public List<StadtbezirkDTO> findStadtbezirkByStadt(final String stadt) {
        return stadtbezirkMapper.entities2Dtos(stadtbezirkRepository.findByStadt(stadt));
    }

    public StadtbezirkDTO findStadtbezirkByNummer(final Integer stadtbezirkNummer) {
        return stadtbezirkMapper.entity2Dto(stadtbezirkRepository.findByNummer(stadtbezirkNummer));
    }

    //    @Cacheable(value = CachingConfiguration., key = "{#p0}") TODO: necessary?
    public String getStadtbezirkName(final Integer stadtbezirkNummer) {
        StadtbezirkDTO stadtbezirk = findStadtbezirkByNummer(stadtbezirkNummer);
        return stadtbezirk != null ? stadtbezirk.getName() : "Unbekannt";
    }

    public StadtbezirkAndViertelDTO findStadtbezirkeAndViertelByStadt(final String stadt) {
        var dto = new StadtbezirkAndViertelDTO();
        List<StadtbezirkDTO> stadtbezirke = findStadtbezirkByStadt(stadt);
        dto.setStadtbezirke(stadtbezirke);

        List<StadtbezirkViertelDTO> stadtviertel = findStadtbezirkViertelByStadt(stadt);
        dto.setStadtbezirkViertel(stadtviertel);
        return dto;
    }
}
