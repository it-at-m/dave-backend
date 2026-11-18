package de.muenchen.dave.domain.mapper.detektor;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.mapper.FahrzeugklassenMapper;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.VerkehrsartMapper;
import de.muenchen.dave.geodateneai.gen.model.MessfaehigkeitDto;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.geodateneai.gen.model.UnauffaelligerTagDto;
import de.muenchen.dave.relational.messtellen.MessfaehigkeitEntity;
import de.muenchen.dave.relational.messtellen.MessquerschnittEntity;
import de.muenchen.dave.relational.messtellen.MessstelleEntity;
import de.muenchen.dave.util.SuchwortUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(
        uses = { FahrzeugklassenMapper.class, VerkehrsartMapper.class },
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface MessstelleReceiverMapper {

    MessstelleEntity createMessstelle(MessstelleDto dto, @Context StadtbezirkMapper stadtbezirkMapper);

    MessquerschnittEntity createMessquerschnitt(MessquerschnittDto dto);

    List<MessquerschnittEntity> createMessquerschnitte(List<MessquerschnittDto> dto);

    MessfaehigkeitEntity createMessfaehigkeit(MessfaehigkeitDto dto);

    List<MessfaehigkeitEntity> createMessfaehigkeit(List<MessfaehigkeitDto> dto);

    @AfterMapping
    default void dto2EntityAfterMapping(@MappingTarget MessstelleEntity bean, MessstelleDto dto, @Context StadtbezirkMapper stadtbezirkMapper) {

        if (bean.getId() == null) {
            bean.setId(UUID.randomUUID());
        }

        /*
         * Die umfangreichste Fahrzeugklasse der Messfähigkeiten einer {@link MessstelleDto}
         * bestimmt die Fahrzeugklasse der {@link Messstelle}.
         */
        final Set<MessfaehigkeitDto.FahrzeugklasseEnum> distinctFahrzeugklassenOfMessfaehigkeiten = CollectionUtils
                .emptyIfNull(dto.getMessfaehigkeiten())
                .stream()
                .map(MessfaehigkeitDto::getFahrzeugklasse)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (distinctFahrzeugklassenOfMessfaehigkeiten.contains(MessfaehigkeitDto.FahrzeugklasseEnum.ACHT_PLUS_EINS)) {
            bean.setFahrzeugklasse(Fahrzeugklasse.ACHT_PLUS_EINS);
        } else if (distinctFahrzeugklassenOfMessfaehigkeiten.contains(MessfaehigkeitDto.FahrzeugklasseEnum.ZWEI_PLUS_EINS)) {
            bean.setFahrzeugklasse(Fahrzeugklasse.ZWEI_PLUS_EINS);
        } else if (distinctFahrzeugklassenOfMessfaehigkeiten.contains(MessfaehigkeitDto.FahrzeugklasseEnum.SUMME_KFZ)) {
            bean.setFahrzeugklasse(Fahrzeugklasse.SUMME_KFZ);
        } else if (distinctFahrzeugklassenOfMessfaehigkeiten.contains(MessfaehigkeitDto.FahrzeugklasseEnum.RAD)) {
            bean.setFahrzeugklasse(Fahrzeugklasse.RAD);
        }

        // Suchworte setzen
        final Set<String> generatedSuchwoerter = SuchwortUtil.generateSuchworteOfMessstelle(bean, stadtbezirkMapper);

        bean.setSuchwoerter(new ArrayList<>());
        if (CollectionUtils.isNotEmpty(generatedSuchwoerter)) {
            bean.getSuchwoerter().addAll(generatedSuchwoerter);
        }

        if (CollectionUtils.isNotEmpty(bean.getCustomSuchwoerter())) {
            bean.getSuchwoerter().addAll(bean.getCustomSuchwoerter());
        }

        if (CollectionUtils.isEmpty(bean.getMessquerschnitte())) {
            bean.setMessquerschnitte(new ArrayList<>());
        }
    }

    @AfterMapping
    default void createMessquerschnittAfterMapping(@MappingTarget MessquerschnittEntity bean, MessquerschnittDto dto) {
        if (bean.getId() == null) {
            bean.setId(UUID.randomUUID());
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sichtbarDatenportal", ignore = true)
    @Mapping(target = "kommentar", ignore = true)
    @Mapping(target = "standort", ignore = true)
    @Mapping(target = "customSuchwoerter", ignore = true)
    @Mapping(target = "suchwoerter", ignore = true)
    @Mapping(target = "geprueft", ignore = true)
    @Mapping(target = "messquerschnitte", ignore = true)
    @Mapping(target = "lageplanVorhanden", ignore = true)
    @Mapping(target = "fahrzeugklasse", ignore = true)
    MessstelleEntity updateMessstelle(@MappingTarget MessstelleEntity existing, MessstelleDto dto, @Context StadtbezirkMapper stadtbezirkMapper);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "standort", ignore = true)
    MessquerschnittEntity updateMessquerschnitt(@MappingTarget MessquerschnittEntity existing, MessquerschnittDto dto, @Context StadtbezirkMapper stadtbezirkMapper);

    default ZaehldatenIntervall map(final MessfaehigkeitDto.IntervallEnum intervall) {
        final ZaehldatenIntervall mappingTarget;
        if (MessfaehigkeitDto.IntervallEnum.STUNDE_VIERTEL == intervall) {
            mappingTarget = ZaehldatenIntervall.STUNDE_VIERTEL;
        } else if (MessfaehigkeitDto.IntervallEnum.STUNDE_VIERTEL_EINGESCHRAENKT == intervall) {
            mappingTarget = ZaehldatenIntervall.STUNDE_VIERTEL_EINGESCHRAENKT;
        } else if (MessfaehigkeitDto.IntervallEnum.STUNDE_HALB == intervall) {
            mappingTarget = ZaehldatenIntervall.STUNDE_HALB;
        } else if (MessfaehigkeitDto.IntervallEnum.STUNDE_KOMPLETT == intervall) {
            mappingTarget = ZaehldatenIntervall.STUNDE_KOMPLETT;
        } else {
            mappingTarget = null;
        }
        return mappingTarget;
    }

    @Mapping(target = "kalendertag", ignore = true)
    UnauffaelligerTag dto2Entity(final UnauffaelligerTagDto dto, @Context final Kalendertag kalendertag);

    @AfterMapping
    default void dto2EntityAfterMapping(@MappingTarget final UnauffaelligerTag entity, @Context final Kalendertag kalendertag) {
        entity.setKalendertag(kalendertag);
    }

}
