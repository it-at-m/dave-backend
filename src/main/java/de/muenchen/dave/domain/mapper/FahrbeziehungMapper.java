package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteFahrbeziehungDTO;
import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FahrbeziehungMapper {

    /**
     * bearbeite auf bean (für Kreuzung)
     *
     * @param dto BearbeiteFahrbeziehungDTO
     * @return gemappte Fahrbeziehung
     */
    @Mapping(target = "hochrechnungsfaktor.version", source = "hochrechnungsfaktor.entityVersion")
    Fahrbeziehung bearbeiteFahrbeziehungDto2bean(BearbeiteFahrbeziehungDTO dto);

    /**
     * bean auf bearbeite (für Kreuzung)
     *
     * @param bean Fahrbeziehung
     * @return gemapptes BearbeiteFahrbeziehungDTO
     */
    @Mapping(target = "hochrechnungsfaktor.entityVersion", source = "hochrechnungsfaktor.version")
    BearbeiteFahrbeziehungDTO bean2bearbeiteFahrbeziehunDto(Fahrbeziehung bean);

    @Mapping(target = "id", source = "elastic.id")
    @Mapping(target = "version", source = "elastic.version")
    @Mapping(target = "isKreuzung", source = "elastic.isKreuzung")
    @Mapping(target = "von", source = "elastic.von")
    @Mapping(target = "nach", source = "elastic.nach")
    @Mapping(target = "knotenarm", source = "elastic.knotenarm")
    @Mapping(target = "hinein", source = "elastic.hinein")
    @Mapping(target = "heraus", source = "elastic.heraus")
    @Mapping(target = "vorbei", source = "elastic.vorbei")
    @Mapping(target = "vonknotvonstrnr", source = "elastic.vonknotvonstrnr")
    @Mapping(target = "nachknotvonstrnr", source = "elastic.nachknotvonstrnr")
    @Mapping(target = "von_strnr", source = "elastic.von_strnr")
    @Mapping(target = "vonknotennachstrnr", source = "elastic.vonknotennachstrnr")
    @Mapping(target = "nachknotnachstrnr", source = "elastic.nachknotnachstrnr")
    @Mapping(target = "nach_strnr", source = "elastic.nach_strnr")
    @Mapping(target = "hochrechnungsfaktor", source = "elastic.hochrechnungsfaktor")
    de.muenchen.dave.domain.analytics.Fahrbeziehung elastic2analytics(de.muenchen.dave.domain.analytics.Fahrbeziehung analytics,
            Fahrbeziehung elastic);
}
