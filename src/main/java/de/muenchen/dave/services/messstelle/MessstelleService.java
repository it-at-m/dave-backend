package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOverviewDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messfaehigkeit;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapper;
import de.muenchen.dave.services.CustomSuggestIndexService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Die Klasse {@link MessstelleService} holt alle relevanten Messstellen aus MobidaM und
 * aktualisiert die in Dave gespeichereten Daten.
 */
@Slf4j
@Service
@AllArgsConstructor
public class MessstelleService {

    private static final String KFZ = "KFZ";
    private final MessstelleIndexService messstelleIndexService;
    private final CustomSuggestIndexService customSuggestIndexService;
    private final MessstelleMapper messstelleMapper;
    private final StadtbezirkMapper stadtbezirkMapper;

    public Messstelle getMessstelle(final String messstelleId) {
        return messstelleIndexService.findByIdOrThrowException(messstelleId);
    }

    public Messstelle getMessstelleByMstId(final String mstId) {
        return messstelleIndexService.findByMstIdOrThrowException(mstId);
    }

    public ReadMessstelleInfoDTO readMessstelleInfo(final String messstelleId) {
        final Messstelle byIdOrThrowException = messstelleIndexService.findByIdOrThrowException(messstelleId);
        return messstelleMapper.bean2readDto(byIdOrThrowException, stadtbezirkMapper);
    }

    public ReadMessstelleInfoDTO readMessstelleInfoByMstId(final String mstId) {
        final Messstelle byIdOrThrowException = messstelleIndexService.findByMstIdOrThrowException(mstId);
        return messstelleMapper.bean2readDto(byIdOrThrowException, stadtbezirkMapper);
    }

    public EditMessstelleDTO getMessstelleToEdit(final String messstelleId) {
        final Messstelle byIdOrThrowException = messstelleIndexService.findByIdOrThrowException(messstelleId);
        byIdOrThrowException.setMessfaehigkeiten(
                byIdOrThrowException.getMessfaehigkeiten().stream().sorted(Comparator.comparing(Messfaehigkeit::getGueltigAb).reversed()).collect(Collectors.toList()));
        return messstelleMapper.bean2editDto(byIdOrThrowException, stadtbezirkMapper);
    }

    public BackendIdDTO updateMessstelle(final EditMessstelleDTO dto) {
        final Messstelle actualMessstelle = messstelleIndexService.findByIdOrThrowException(dto.getId());
        final Messstelle aktualisiert = messstelleMapper.updateMessstelle(actualMessstelle, dto, stadtbezirkMapper);
        customSuggestIndexService.updateSuggestionsForMessstelle(aktualisiert);
        final Messstelle messstelle = messstelleIndexService.saveMessstelle(aktualisiert);
        final BackendIdDTO backendIdDTO = new BackendIdDTO();
        backendIdDTO.setId(messstelle.getId());
        return backendIdDTO;
    }

    public List<MessstelleOverviewDTO> getAllMessstellenForOverview() {
        final List<Messstelle> messstellen = messstelleIndexService.findAllMessstellen();
        return messstelleMapper.bean2overviewDto(messstellen);
    }

    public Set<String> getMessquerschnittIds(final String mstId) {
        final Messstelle messstelle = messstelleIndexService.findByMstIdOrThrowException(mstId);
        final Set<String> result = new HashSet<>();
        messstelle.getMessquerschnitte().forEach(messquerschnitt -> result.add(messquerschnitt.getMqId()));
        return result;
    }

    public Set<String> getMessquerschnittIdsByMessstelleId(final String messstelleId) {
        final Messstelle messstelle = messstelleIndexService.findByIdOrThrowException(messstelleId);
        final Set<String> result = new HashSet<>();
        messstelle.getMessquerschnitte().forEach(messquerschnitt -> result.add(messquerschnitt.getMqId()));
        return result;
    }

    public boolean isKfzMessstelle(final String messstelleId) {
        final Messstelle messstelle = messstelleIndexService.findByIdOrThrowException(messstelleId);
        return KFZ.equalsIgnoreCase(messstelle.getDetektierteVerkehrsarten());
    }

    public List<MessstelleAuswertungDTO> getAllVisibleMessstellenForAuswertungOrderByMstIdAsc() {
        final List<Messstelle> messstellen = messstelleIndexService.findAllVisibleMessstellen();
        final List<Messstelle> sorted = messstellen.stream().sorted(Comparator.comparing(Messstelle::getMstId)).collect(Collectors.toList());
        return messstelleMapper.bean2auswertungDto(sorted);
    }

    public Optional<Messquerschnitt> getOptionalOfMessquerschnittByMstId(final String mstId, final String mqId) {
        return messstelleIndexService.findByMstIdOrThrowException(mstId).getMessquerschnitte().stream()
                .filter(messquerschnitt -> mqId.equalsIgnoreCase(messquerschnitt.getMqId())).findFirst();
    }
}
