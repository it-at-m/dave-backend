package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteKnotenarmDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.mapper.KnotenarmMapper;
import de.muenchen.dave.relational.zaehlstellen.KnotenarmEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnotenarmSynchronisierungsService {

    private final KnotenarmMapper knotenarmMapper;

    public void synchronisiere(
            final BearbeiteZaehlungDTO dto,
            final ZaehlungEntity zaehlung
    ) {
        final List<BearbeiteKnotenarmDTO> knotenarmDtos = ListUtils.emptyIfNull(dto.getKnotenarme());

        /*
         Initial condition : The service receives only a list of the active elements.
         Remove deleted items.
         */
        final List<BearbeiteKnotenarmDTO> dtos = ListUtils.emptyIfNull(dto.getKnotenarme());

        final Set<Integer> vorhandeneNummern = dtos.stream()
                .map(BearbeiteKnotenarmDTO::getNummer)
                .collect(Collectors.toSet());

        zaehlung.getKnotenarme().removeIf(
                entity -> !vorhandeneNummern.contains(entity.getNummer()));


        // Process remaining and new items.
        for (final BearbeiteKnotenarmDTO knotenarmDto : knotenarmDtos) {
            final KnotenarmEntity knotenarm = zaehlung.getKnotenarme().stream()
                    .filter(entity -> entity.getNummer().equals(knotenarmDto.getNummer()))
                    .findFirst()
                    .orElseGet(() -> {
                        final KnotenarmEntity newEntity = new KnotenarmEntity();
                        newEntity.setZaehlung(zaehlung);
                        zaehlung.getKnotenarme().add(newEntity);
                        return newEntity;
                    });

            this.knotenarmMapper.updateEntity(knotenarmDto, knotenarm);
        }

        zaehlung.getKnotenarme().removeIf(entity ->
                knotenarmDtos.stream()
                        .noneMatch(dtoKnotenarm ->
                                entity.getNummer().equals(dtoKnotenarm.getNummer())));
    }
}
