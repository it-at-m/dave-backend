package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnungsfaktor;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.mapper.ZaehlungMapper;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.relational.repository.HochrechnungsfaktorRepository;
import de.muenchen.dave.relational.zaehlstellen.LaengsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.QuerungsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.VerkehrsbeziehungEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.services.HochrechnungsfaktorService;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BewegungsbeziehungSynchronisierungsService {

    private final ZaehlungMapper zaehlungMapper;

    private final EntityManager entityManager;
    private final HochrechnungsfaktorRepository hochrechnungsfaktorRepository;

    public void synchronize(
            BearbeiteZaehlungDTO dto,
            ZaehlungEntity zaehlung
    ) throws DataNotFoundException {

        if (Zaehlart.FJS.name().equals(dto.getZaehlart())) {
            final var laengsverkehr = dto.getLaengsverkehr();
            if (CollectionUtils.isNotEmpty(laengsverkehr)) {

                 /*
                  Initial condition : The service receives only a list of the active elements.
                  Remove deleted items.
                 */
                final Set<UUID> existingIds = laengsverkehr.stream()
                        .map(BearbeiteLaengsverkehrDTO::getId)
                        .filter(StringUtils::isNotBlank)
                        .map(UUID::fromString)
                        .collect(Collectors.toSet());

                zaehlung.getLaengsverkehre().removeIf(
                        entity -> !existingIds.contains(entity.getId()));

                // Process remaining and new items.
                for (BearbeiteLaengsverkehrDTO lDto : laengsverkehr) {
                    final LaengsverkehrEntity updateEntity;
                    if (lDto.getId() == null) {
                        updateEntity = new LaengsverkehrEntity();
                        zaehlung.getLaengsverkehre().add(updateEntity);
                    } else {
                        var foundEntity = zaehlung.getLaengsverkehre().stream().filter(e -> e.getId().equals(UUID.fromString(lDto.getId())))
                                .findFirst();
                        updateEntity = foundEntity.orElseThrow(() -> new DataNotFoundException("Die Laengsverkehr-ID konnte in der Zaehlung nicht gefunden werden: " + lDto.getId()));
                    }
                    this.zaehlungMapper.updateEntity(lDto, updateEntity);

                    updateEntity.setHochrechnungsfaktor(
                            synchronizeHochrechnungsfaktor(
                                    lDto.getHochrechnungsfaktor()));
                }
            }
        } else if (Zaehlart.QU.name().equals(dto.getZaehlart())) {

            final var querungsverkehr = dto.getQuerungsverkehr();
            if (CollectionUtils.isNotEmpty(querungsverkehr)) {
                /*
                  Initial condition : The service receives only a list of the active elements.
                  Remove deleted items.
                 */
                final Set<UUID> existingIds = querungsverkehr.stream()
                        .map(BearbeiteQuerungsverkehrDTO::getId)
                        .filter(StringUtils::isNotBlank)
                        .map(UUID::fromString)
                        .collect(Collectors.toSet());

                zaehlung.getQuerungsverkehre().removeIf(
                        entity -> !existingIds.contains(entity.getId()));

                // Process remaining and new items.
                for (BearbeiteQuerungsverkehrDTO lDto : querungsverkehr) {
                    final QuerungsverkehrEntity updateEntity;
                    if (lDto.getId() == null) {
                        updateEntity = new QuerungsverkehrEntity();
                        zaehlung.getQuerungsverkehre().add(updateEntity);
                    } else {
                        var foundEntity = zaehlung.getQuerungsverkehre().stream().filter(e -> e.getId().equals(UUID.fromString(lDto.getId())))
                                .findFirst();
                        updateEntity = foundEntity.orElseThrow(() -> new DataNotFoundException("Die Querungsverkehr-ID konnte in der Zaehlung nicht gefunden werden: " + lDto.getId()));
                    }
                    this.zaehlungMapper.updateEntity(lDto, updateEntity);

                    updateEntity.setHochrechnungsfaktor(
                            synchronizeHochrechnungsfaktor(
                                    lDto.getHochrechnungsfaktor()));
                }
            }
        } else {

            final var verkehrsbeziehungen = dto.getVerkehrsbeziehungen();
            if (CollectionUtils.isNotEmpty(verkehrsbeziehungen)) {
                 /*
                    Initial condition : The service receives only a list of the active elements.
                    Remove deleted items.
                 */
                final Set<UUID> existingIds = verkehrsbeziehungen.stream()
                        .map(BearbeiteVerkehrsbeziehungDTO::getId)
                        .filter(StringUtils::isNotBlank)
                        .map(UUID::fromString)
                        .collect(Collectors.toSet());

                zaehlung.getVerkehrsbeziehungen().removeIf(
                        entity -> !existingIds.contains(entity.getId()));

                // Process remaining and new items.
                for (BearbeiteVerkehrsbeziehungDTO lDto : verkehrsbeziehungen) {
                    final VerkehrsbeziehungEntity updateEntity;
                    if (lDto.getId() == null) {
                        updateEntity = new VerkehrsbeziehungEntity();
                        zaehlung.getVerkehrsbeziehungen().add(updateEntity);
                    } else {
                        var foundEntity = zaehlung.getVerkehrsbeziehungen().stream().filter(e -> e.getId().equals(UUID.fromString(lDto.getId())))
                                .findFirst();
                        updateEntity = foundEntity.orElseThrow(() -> new DataNotFoundException("Die Verkehrsbeziehung-ID konnte in der Zaehlung nicht gefunden werden: " + lDto.getId()));
                    }
                    this.zaehlungMapper.updateEntity(lDto, updateEntity);

                    updateEntity.setHochrechnungsfaktor(
                            synchronizeHochrechnungsfaktor(
                                    lDto.getHochrechnungsfaktor()));
                }
            }
        }
    }

    private Hochrechnungsfaktor synchronizeHochrechnungsfaktor(
            final HochrechnungsfaktorDTO dto
    ) throws DataNotFoundException {
        if (dto == null) {
            return null;
        }

        final Hochrechnungsfaktor faktor;
        if (dto.getId() == null) {
            faktor = new Hochrechnungsfaktor();
            HochrechnungsfaktorService.setDtoDataToEntity(dto, faktor);
            entityManager.persist(faktor);
        } else {
            faktor = hochrechnungsfaktorRepository.findById(dto.getId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "Hochrechnungsfaktor nicht gefunden: " + dto.getId()));

            HochrechnungsfaktorService.setDtoDataToEntity(dto, faktor);
        }
        return faktor;
    }

}
