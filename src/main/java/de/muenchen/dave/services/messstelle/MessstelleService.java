package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOverviewDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapper;
import de.muenchen.dave.services.CustomSuggestIndexService;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Die Klasse {@link MessstelleService} holt alle relevanten Messstellen aus MobidaM und
 * aktualisiert die in Dave gespeichereten Daten.
 */
@Slf4j
@Service
@AllArgsConstructor
public class MessstelleService {

    private final MessstelleIndexService messstelleIndexService;

    private final CustomSuggestIndexService customSuggestIndexService;

    private final MessstelleMapper messstelleMapper;
    private static final String KFZ = "KFZ";

    public ReadMessstelleInfoDTO readMessstelleInfo(final String messstelleId) {
        log.debug("#readMessstelleById");
        final Messstelle byIdOrThrowException = messstelleIndexService.findByIdOrThrowException(messstelleId);
        return messstelleMapper.bean2readDto(byIdOrThrowException);
    }

    public EditMessstelleDTO getMessstelleToEdit(final String messstelleId) {
        log.debug("#getMessstelleToEdit");
        final Messstelle byIdOrThrowException = messstelleIndexService.findByIdOrThrowException(messstelleId);
        return messstelleMapper.bean2editDto(byIdOrThrowException);
    }

    public BackendIdDTO updateMessstelle(final EditMessstelleDTO dto) {
        log.info("#updateMessstelle");
        final Messstelle actualMessstelle = messstelleIndexService.findByIdOrThrowException(dto.getId());
        final Messstelle aktualisiert = messstelleMapper.updateMessstelle(actualMessstelle, dto);
        customSuggestIndexService.updateSuggestionsForMessstelle(aktualisiert);
        final Messstelle messstelle = messstelleIndexService.saveMessstelle(aktualisiert);
        final BackendIdDTO backendIdDTO = new BackendIdDTO();
        backendIdDTO.setId(messstelle.getId());
        return backendIdDTO;
    }

    public List<MessstelleOverviewDTO> getAllMessstellenForOverview() {
        log.debug("#getAllMessstellenForOverview");
        final List<Messstelle> messstellen = messstelleIndexService.findAllMessstellen();
        return messstelleMapper.bean2overviewDto(messstellen);
    }

    public Set<String> getMessquerschnittNummern(final String messstelleId) {
        final Messstelle messstelle = messstelleIndexService.findByIdOrThrowException(messstelleId);
        final Set<String> result = new HashSet<>();
        messstelle.getMessquerschnitte().forEach(messquerschnitt -> result.add(messquerschnitt.getMqId()));
        return result;
    }

    public boolean isKfzMessstelle(final String messstelleId) {
        final Messstelle messstelle = messstelleIndexService.findByIdOrThrowException(messstelleId);
        return KFZ.equalsIgnoreCase(messstelle.getDetektierteVerkehrsarten());
    }

    public List<MessstelleAuswertungDTO> getAllVisibleMessstellenForAuswertung() {
        log.debug("#getAllVisibleMessstellenForAuswertung");
        final List<Messstelle> messstellen = messstelleIndexService.findAllVisibleMessstellen();
        final List<Messstelle> sorted = messstellen.stream().sorted(Comparator.comparing(Messstelle::getMstId)).collect(Collectors.toList());
        return messstelleMapper.bean2auswertungDto(sorted);
    }
}
