package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapper;
import de.muenchen.dave.services.CustomSuggestIndexService;
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

    public ReadMessstelleDTO readMessstelleById(final String messstelleId) {
        log.debug("#readMessstelleById");
        final Messstelle byIdOrThrowException = messstelleIndexService.findByIdOrThrowException(messstelleId);
        // Mapping auf ReadMessstelleDto
        return messstelleMapper.bean2readDto(byIdOrThrowException);
    }

    public EditMessstelleDTO getMessstelleToEdit(final String messstelleId) {
        log.debug("#getMessstelleToEdit");
        final Messstelle byIdOrThrowException = messstelleIndexService.findByIdOrThrowException(messstelleId);
        // Mapping auf BearbeiteMessstelleDTO
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
}
