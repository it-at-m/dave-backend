package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOverviewDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messfaehigkeit;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.Verkehrsart;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapper;
import de.muenchen.dave.services.CustomSuggestIndexService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
    private final StadtbezirkMapper stadtbezirkMapper;

    public Messstelle getMessstelle(final String messstelleId) {
        return messstelleIndexService.findByIdOrThrowException(messstelleId);
    }

    public Messstelle getMessstelleByMstId(final String mstId) {
        return messstelleIndexService.findByMstIdOrThrowException(mstId);
    }

    public ReadMessstelleInfoDTO readMessstelleInfo(final String messstelleId) {
        final var messstelle = messstelleIndexService.findByIdOrThrowException(messstelleId);
        return messstelleMapper.bean2readDto(messstelle, stadtbezirkMapper);
    }

    public ReadMessstelleInfoDTO readMessstelleInfoByMstId(final String mstId) {
        final Messstelle byIdOrThrowException = messstelleIndexService.findByMstIdOrThrowException(mstId);
        return messstelleMapper.bean2readDto(byIdOrThrowException, stadtbezirkMapper);
    }

    public EditMessstelleDTO getMessstelleToEdit(final String messstelleId) {
        final Messstelle messstelle = messstelleIndexService.findByIdOrThrowException(messstelleId);
        messstelle.setMessfaehigkeiten(
                messstelle.getMessfaehigkeiten()
                        .stream()
                        .sorted(Comparator.comparing(Messfaehigkeit::getGueltigAb).reversed())
                        .collect(Collectors.toList()));
        messstelle.setMessquerschnitte(
                messstelle.getMessquerschnitte()
                        .stream()
                        .sorted(Comparator.comparing(Messquerschnitt::getMqId))
                        .collect(Collectors.toList()));
        return messstelleMapper.bean2editDto(messstelle, stadtbezirkMapper);
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
        return messstelleMapper.bean2overviewDto(messstellen, stadtbezirkMapper);
    }

    public Set<String> getMessquerschnittIdsByMessstelleId(final String messstelleId) {
        final Messstelle messstelle = messstelleIndexService.findByIdOrThrowException(messstelleId);
        final Set<String> result = new HashSet<>();
        messstelle.getMessquerschnitte().forEach(messquerschnitt -> result.add(messquerschnitt.getMqId()));
        return result;
    }

    public boolean isKfzMessstelle(final String messstelleId) {
        final Messstelle messstelle = messstelleIndexService.findByIdOrThrowException(messstelleId);
        return Verkehrsart.KFZ.equals(messstelle.getDetektierteVerkehrsart());
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

    public void updateLetztePlausibleMessungOfMessstelle(final String mstId, final LocalDate letzePlausibleMessung) {
        messstelleIndexService.findByMstId(mstId).ifPresent(messstelle -> {
            messstelle.setDatumLetztePlausibleMessung(letzePlausibleMessung);
            messstelleIndexService.saveMessstelle(messstelle);
        });
    }
}
