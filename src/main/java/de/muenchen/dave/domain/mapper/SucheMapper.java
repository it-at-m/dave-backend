package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.ZaehlstelleKarteDTO;
import de.muenchen.dave.domain.dtos.ZaehlstelleTooltipDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleKarteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleTooltipDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SucheMapper {

    String UNBEKANNT = "unbekannt";

    MessstelleTooltipDTO messstelleToMessstelleTooltipDTO(final Messstelle messstelle, @Context final StadtbezirkMapper stadtbezirkMapper);

    MessstelleKarteDTO messstelleToMessstelleKarteDTO(final Messstelle messstelle, @Context final StadtbezirkMapper stadtbezirkMapper);

    Set<MessstelleKarteDTO> messstelleToMessstelleKarteDTO(final List<Messstelle> messstellen, @Context final StadtbezirkMapper stadtbezirkMapper);

    @AfterMapping
    default void messstelleToMessstelleKarteDTOAfterMapping(
            @MappingTarget final MessstelleKarteDTO dto,
            final Messstelle bean,
            @Context final StadtbezirkMapper stadtbezirkMapper) {
        dto.setType("messstelle");
        dto.setFachId(bean.getMstId());
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
        dto.setTooltip(messstelleToMessstelleTooltipDTO(bean, stadtbezirkMapper));
    }

    @AfterMapping
    default void messstelleToMessstelleTooltipDTOAfterMapping(
            @MappingTarget final MessstelleTooltipDTO dto,
            final Messstelle bean,
            @Context final StadtbezirkMapper stadtbezirkMapper) {
        dto.setStadtbezirk(stadtbezirkMapper.bezeichnungOf(bean.getStadtbezirkNummer()));
        dto.setStadtbezirknummer(bean.getStadtbezirkNummer());

        final var realisierungsdatum = ObjectUtils.isNotEmpty(bean.getRealisierungsdatum())
                ? bean.getRealisierungsdatum().toString()
                : UNBEKANNT;
        dto.setRealisierungsdatum(realisierungsdatum);

        final var abbaudatum = ObjectUtils.isNotEmpty(bean.getAbbaudatum())
                ? bean.getAbbaudatum().toString()
                : (MessstelleStatus.ABGEBAUT.equals(bean.getStatus()) ? UNBEKANNT : StringUtils.EMPTY);
        dto.setAbbaudatum(abbaudatum);

        final var datumLetztePlausibleMessung = ObjectUtils.isNotEmpty(bean.getDatumLetztePlausibleMessung())
                ? bean.getDatumLetztePlausibleMessung().toString()
                : UNBEKANNT;
        dto.setDatumLetztePlausibleMessung(datumLetztePlausibleMessung);
    }

    /**
     * Erstellt ein TooltipDTO für die Metainformationen einer Zählstelle. Das DTO wird im Frontend als
     * MouseOver bei einem Marker in der Karte angezeigt.
     *
     * @param stadtbezirk Stadtbezirksname
     * @param stadtbezirknummer Stadtbezirksnummer als Long
     * @param nummer Zaehlstellennummer
     * @param anzahlZaehlungen Anzahl der einer Zählstelle zugehörigen Zählungen als Integer
     * @param datumLetzteZaehlung Datum der letzten Zählung im Format dd.MM.yyyy als String
     * @param kreuzungsname Kreuzungsname als String
     * @return TooltipDTO mit allen benötigten Feldern
     */
    default ZaehlstelleTooltipDTO createZaehlstelleTooltip(
            final String stadtbezirk,
            final Integer stadtbezirknummer,
            final String nummer,
            final Integer anzahlZaehlungen,
            final String datumLetzteZaehlung,
            final String kreuzungsname) {
        final ZaehlstelleTooltipDTO zaehlstelleTooltipDTO = new ZaehlstelleTooltipDTO();
        zaehlstelleTooltipDTO.setKreuzungsname(kreuzungsname);
        zaehlstelleTooltipDTO.setAnzahlZaehlungen(anzahlZaehlungen);
        zaehlstelleTooltipDTO.setStadtbezirk(stadtbezirk);
        zaehlstelleTooltipDTO.setStadtbezirknummer(stadtbezirknummer);
        zaehlstelleTooltipDTO.setZaehlstellennnummer(nummer);
        zaehlstelleTooltipDTO.setDatumLetzteZaehlung(datumLetzteZaehlung);
        return zaehlstelleTooltipDTO;
    }

    ZaehlstelleKarteDTO zaehlstelleToZaehlstelleKarteDTO(final Zaehlstelle zaehlstelle);

    @AfterMapping
    default void zaehlstelleToZaehlstelleKarteDTOAfterMapping(
            @MappingTarget final ZaehlstelleKarteDTO dto,
            final Zaehlstelle bean) {
        dto.setType("zaehlstelle");
        dto.setFachId(bean.getNummer());
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
    }
}
