package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.ZaehlstelleKarteDTO;
import de.muenchen.dave.domain.dtos.ZaehlstelleTooltipDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleKarteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleTooltipDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import java.util.List;
import java.util.Set;

import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SucheMapper {

    MessstelleTooltipDTO messstelleToMessstelleTooltipDTO(final Messstelle messstelle, @Context StadtbezirkMapper stadtbezirkMapper);

    MessstelleKarteDTO messstelleToMessstelleKarteDTO(final Messstelle messstelle, @Context StadtbezirkMapper stadtbezirkMapper);

    Set<MessstelleKarteDTO> messstelleToMessstelleKarteDTO(final List<Messstelle> messstellen);

    @AfterMapping
    default void messstelleToMessstelleKarteDTOAfterMapping(@MappingTarget MessstelleKarteDTO dto, Messstelle bean,
            @Context StadtbezirkMapper stadtbezirkMapper) {
        dto.setType("messstelle");
        dto.setFachId(bean.getMstId());
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
        dto.setTooltip(messstelleToMessstelleTooltipDTO(bean, stadtbezirkMapper));
    }

    @AfterMapping
    default void messstelleToMessstelleTooltipDTOAfterMapping(@MappingTarget MessstelleTooltipDTO dto, Messstelle bean,
            @Context StadtbezirkMapper stadtbezirkMapper) {
        dto.setStadtbezirk(stadtbezirkMapper.bezeichnungOf(bean.getStadtbezirkNummer()));
        dto.setStadtbezirknummer(bean.getStadtbezirkNummer());
        dto.setRealisierungsdatum(bean.getRealisierungsdatum().toString());
        if (bean.getAbbaudatum() != null)
            dto.setAbbaudatum(bean.getAbbaudatum().toString());
        dto.setDatumLetztePlausibleMessung(bean.getDatumLetztePlausibleMessung().toString());
    }

    /**
     * Erstellt ein TooltipDTO für die Metainformationen einer Zählstelle.
     * Das DTO wird im Frontend als MouseOver bei einem Marker in der Karte
     * angezeigt.
     *
     * @param stadtbezirk Stadtbezirksname
     * @param stadtbezirknummer Stadtbezirksnummer als Long
     * @param nummer Zaehlstellennummer
     * @param anzahlZaehlungen Anzahl der einer Zählstelle zugehörigen Zählungen als Integer
     * @param datumLetzteZaehlung Datum der letzten Zählung im Format dd.MM.yyyy als String
     * @param kreuzungsname Kreuzungsname als String
     * @return TooltipDTO mit allen benötigten Feldern
     */
    default ZaehlstelleTooltipDTO createZaehlstelleTooltip(final String stadtbezirk,
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
    default void zaehlstelleToZaehlstelleKarteDTOAfterMapping(@MappingTarget ZaehlstelleKarteDTO dto, Zaehlstelle bean) {
        dto.setType("zaehlstelle");
        dto.setFachId(bean.getNummer());
        dto.setLatitude(bean.getPunkt().getLat());
        dto.setLongitude(bean.getPunkt().getLon());
    }
}
