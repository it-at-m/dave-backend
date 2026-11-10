package de.muenchen.dave.migrationespg.services.messstelle;

import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleOverviewDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessfaehigkeitDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.enums.Verkehrsart;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.migrationespg.domain.mapper.detektor.MessstelleMapperMig;
import de.muenchen.dave.migrationespg.domain.relational.messtellen.MessfaehigkeitEntity;
import de.muenchen.dave.migrationespg.domain.relational.messtellen.MessquerschnittEntity;
import de.muenchen.dave.migrationespg.domain.relational.messtellen.MessstelleEntity;
import de.muenchen.dave.migrationespg.services.CustomSuggestIndexServiceMig;
import de.muenchen.dave.migrationespg.services.SanitizationServiceMig;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Die Klasse {@link MessstelleServiceMig} holt alle relevanten Messstellen aus MobidaM und
 * aktualisiert die in Dave gespeichereten Daten.
 */
@Slf4j
@Service
@AllArgsConstructor
public class MessstelleServiceMig {

    private final MessstelleIndexServiceMig messstelleIndexServiceMig;
    private final CustomSuggestIndexServiceMig customSuggestIndexServiceMig;
    private final MessstelleMapperMig messstelleMapperMig;
    private final StadtbezirkMapper stadtbezirkMapper;
    private final SanitizationServiceMig sanitizationServiceMig;

    public MessstelleEntity getMessstelle(final String messstelleId) {
        return messstelleIndexServiceMig.findByIdOrThrowException(messstelleId);
    }

    public MessstelleEntity getMessstelleByMstId(final String mstId) {
        return messstelleIndexServiceMig.findByMstIdOrThrowException(mstId);
    }

    public ReadMessstelleInfoDTO readMessstelleInfo(final String messstelleId) {
        final var messstelle = messstelleIndexServiceMig.findByIdOrThrowException(messstelleId);
        return messstelleMapperMig.bean2readDto(messstelle, stadtbezirkMapper);
    }

    public ReadMessstelleInfoDTO readMessstelleInfoByMstId(final String mstId) {
        final MessstelleEntity byIdOrThrowException = messstelleIndexServiceMig.findByMstIdOrThrowException(mstId);
        return messstelleMapperMig.bean2readDto(byIdOrThrowException, stadtbezirkMapper);
    }

    public EditMessstelleDTO getMessstelleToEdit(final String messstelleId) {
        final MessstelleEntity messstelle = messstelleIndexServiceMig.findByIdOrThrowException(messstelleId);
        messstelle.setMessfaehigkeiten(
                messstelle.getMessfaehigkeiten()
                        .stream()
                        .sorted(Comparator.comparing(MessfaehigkeitEntity::getGueltigAb).reversed())
                        .collect(Collectors.toList()));
        messstelle.setMessquerschnitte(
                messstelle.getMessquerschnitte()
                        .stream()
                        .sorted(Comparator.comparing(MessquerschnittEntity::getMqId))
                        .collect(Collectors.toList()));
        return messstelleMapperMig.bean2editDto(messstelle, stadtbezirkMapper);
    }

    public BackendIdDTO updateMessstelle(final EditMessstelleDTO dto) {
        final MessstelleEntity actualMessstelle = messstelleIndexServiceMig.findByIdOrThrowException(dto.getId());
        final MessstelleEntity aktualisiert = messstelleMapperMig.updateMessstelle(actualMessstelle, dto, stadtbezirkMapper);
        sanitizationServiceMig.sanitizeMessstelle(aktualisiert);
        customSuggestIndexServiceMig.updateSuggestionsForMessstelle(aktualisiert);
        final MessstelleEntity messstelle = messstelleIndexServiceMig.saveMessstelle(aktualisiert);
        final BackendIdDTO backendIdDTO = new BackendIdDTO();
        backendIdDTO.setId(String.valueOf(messstelle.getId()));
        return backendIdDTO;
    }

    public List<MessstelleOverviewDTO> getAllMessstellenForOverview() {
        final List<MessstelleEntity> messstellen = messstelleIndexServiceMig.findAllMessstellen();
        return messstelleMapperMig.bean2overviewDto(messstellen, stadtbezirkMapper);
    }

    public Set<String> getMessquerschnittIdsByMessstelleId(final String messstelleId) {
        final MessstelleEntity messstelle = messstelleIndexServiceMig.findByIdOrThrowException(messstelleId);
        final Set<String> result = new HashSet<>();
        messstelle.getMessquerschnitte().forEach(messquerschnitt -> result.add(messquerschnitt.getMqId()));
        return result;
    }

    public boolean isKfzMessstelle(final String messstelleId) {
        final MessstelleEntity messstelle = messstelleIndexServiceMig.findByIdOrThrowException(messstelleId);
        return Verkehrsart.KFZ.equals(messstelle.getDetektierteVerkehrsart());
    }

    public List<MessstelleAuswertungDTO> getAllVisibleMessstellenForAuswertungOrderByMstIdAsc() {
        final List<MessstelleEntity> messstellen = messstelleIndexServiceMig.findAllVisibleMessstellen();
        final List<MessstelleEntity> sorted = messstellen.stream().sorted(Comparator.comparing(MessstelleEntity::getMstId)).collect(Collectors.toList());
        return messstelleMapperMig.bean2auswertungDto(sorted);
    }

    public Optional<MessquerschnittEntity> getOptionalOfMessquerschnittByMstId(final String mstId, final String mqId) {
        return messstelleIndexServiceMig.findByMstIdOrThrowException(mstId).getMessquerschnitte().stream()
                .filter(messquerschnitt -> mqId.equalsIgnoreCase(messquerschnitt.getMqId())).findFirst();
    }

    /**
     * Die Methode extrahiert die Messfähigkeiten der Messstelle für den angegebenen Zeitraum gegeben in
     * den Parametern.
     *
     * Extrahierte Messfähigkeiten welche sich mit dem Anfangs- und Enddatum des Zeitraums überschneiden
     * beginnen,
     * beginnen oder enden mit dem Start- oder Enddatum des in den Parametern gegebenen Zeitraums.
     *
     * @param mstId
     * @param startDateZeitraum
     * @param endDateZeitraum
     * @return die extrahierten Messfähigkeiten.
     */
    public List<ReadMessfaehigkeitDTO> getMessfaehigkeitenForZeitraumForMessstelle(
            final String mstId,
            final LocalDate startDateZeitraum,
            final LocalDate endDateZeitraum) {
        final var messstelle = readMessstelleInfoByMstId(mstId);
        return messstelle
                .getMessfaehigkeiten()
                .stream()
                .filter(messfaehigkeit -> {
                    final var messfaehigkeitGueltigAb = Objects.isNull(messfaehigkeit.getGueltigAb())
                            ? null
                            : LocalDate.parse(messfaehigkeit.getGueltigAb());
                    final var messfaehigkeitGueltigBis = Objects.isNull(messfaehigkeit.getGueltigBis())
                            ? null
                            : LocalDate.parse(messfaehigkeit.getGueltigBis());

                    final var isGueltigAbBetween = isDateBetweenZeitraumInklusive(
                            messfaehigkeitGueltigAb,
                            startDateZeitraum,
                            endDateZeitraum);

                    final var isGueltigBisBetween = isDateBetweenZeitraumInklusive(
                            messfaehigkeitGueltigBis,
                            startDateZeitraum,
                            endDateZeitraum);

                    final var isStartDateBetween = isDateBetweenZeitraumInklusive(
                            startDateZeitraum,
                            messfaehigkeitGueltigAb,
                            messfaehigkeitGueltigBis);

                    final var isEndDateBetween = isDateBetweenZeitraumInklusive(
                            endDateZeitraum,
                            messfaehigkeitGueltigAb,
                            messfaehigkeitGueltigBis);

                    return isGueltigAbBetween ||
                            isGueltigBisBetween ||
                            isStartDateBetween ||
                            isEndDateBetween;
                })
                .map(messfaehigkeit -> setEarlierDateOrLaterDateInMessfaehigkeit(messfaehigkeit, startDateZeitraum, endDateZeitraum))
                .toList();
    }

    protected boolean isDateBetweenZeitraumInklusive(
            final LocalDate date,
            final LocalDate startDateZeitraum,
            final LocalDate endDateZeitraum) {
        return !(Objects.isNull(date) || Objects.isNull(startDateZeitraum) || Objects.isNull(endDateZeitraum)) &&
                (date.isEqual(startDateZeitraum) || date.isAfter(startDateZeitraum)) &&
                (date.isEqual(endDateZeitraum) || date.isBefore(endDateZeitraum));
    }

    protected ReadMessfaehigkeitDTO setEarlierDateOrLaterDateInMessfaehigkeit(
            final ReadMessfaehigkeitDTO messfaehigkeit,
            final LocalDate startDateZeitraum,
            final LocalDate endDateZeitraum) {
        final var messfaehigkeitGueltigAb = Objects.isNull(messfaehigkeit.getGueltigAb())
                ? null
                : LocalDate.parse(messfaehigkeit.getGueltigAb());
        final var messfaehigkeitGueltigBis = Objects.isNull(messfaehigkeit.getGueltigBis())
                ? null
                : LocalDate.parse(messfaehigkeit.getGueltigBis());

        if (!isDateBetweenZeitraumInklusive(messfaehigkeitGueltigAb, startDateZeitraum, endDateZeitraum)) {
            final var newMessfaehigkeitGueltigAb = getLaterDate(messfaehigkeitGueltigAb, startDateZeitraum);
            messfaehigkeit.setGueltigAb(newMessfaehigkeitGueltigAb.toString());
        }

        if (!isDateBetweenZeitraumInklusive(messfaehigkeitGueltigBis, startDateZeitraum, endDateZeitraum)) {
            final var newMessfaehigkeitGueltigBis = getEarlierDate(messfaehigkeitGueltigBis, endDateZeitraum);
            messfaehigkeit.setGueltigBis(newMessfaehigkeitGueltigBis.toString());
        }

        return messfaehigkeit;
    }

    protected LocalDate getEarlierDate(final LocalDate date, final LocalDate dateToCompare) {
        final LocalDate earlierDate;
        if (Objects.isNull(date) && !Objects.isNull(dateToCompare)) {
            earlierDate = dateToCompare;
        } else if (!Objects.isNull(date) && Objects.isNull(dateToCompare)) {
            earlierDate = date;
        } else if (Objects.isNull(date) && Objects.isNull(dateToCompare)) {
            earlierDate = null;
        } else if (date.isBefore(dateToCompare)) {
            earlierDate = date;
        } else {
            earlierDate = dateToCompare;
        }
        return earlierDate;
    }

    protected LocalDate getLaterDate(final LocalDate date, final LocalDate dateToCompare) {
        final LocalDate laterDate;
        if (Objects.isNull(date) && !Objects.isNull(dateToCompare)) {
            laterDate = dateToCompare;
        } else if (!Objects.isNull(date) && Objects.isNull(dateToCompare)) {
            laterDate = date;
        } else if (Objects.isNull(date) && Objects.isNull(dateToCompare)) {
            laterDate = null;
        } else if (date.isAfter(dateToCompare)) {
            laterDate = date;
        } else {
            laterDate = dateToCompare;
        }
        return laterDate;
    }
}
