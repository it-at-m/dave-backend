package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.ZaehlstelleTooltipDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleTooltipDTO;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.services.IndexServiceUtils;
import org.apache.commons.collections4.CollectionUtils;

public class SucheMapper {

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
    public static ZaehlstelleTooltipDTO createZaehlstelleTooltip(final String stadtbezirk,
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

    /**
     * Erstellt ein TooltipDTO für die Metainformationen einer Messstelle.
     * Das DTO wird im Frontend als MouseOver bei einem Marker in der Karte
     * angezeigt.
     *
     * @param messstelle Messstelle
     * @return MessstelleTooltipDTO
     */
    public static MessstelleTooltipDTO createMessstelleTooltip(
            final Messstelle messstelle) {
        final MessstelleTooltipDTO tooltipDTO = new MessstelleTooltipDTO();
        tooltipDTO.setMstId(messstelle.getNummer());
        tooltipDTO.setStandort(messstelle.getStandort());
        tooltipDTO.setStadtbezirk(IndexServiceUtils.getStadtbezirkBezeichnung(messstelle.getStadtbezirkNummer()));
        tooltipDTO.setStadtbezirknummer(messstelle.getStadtbezirkNummer());
        tooltipDTO.setRealisierungsdatum(messstelle.getRealisierungsdatum() == null ? "" : messstelle.getRealisierungsdatum().toString());
        tooltipDTO.setAbbaudatum(messstelle.getAbbaudatum() == null ? "" : messstelle.getAbbaudatum().toString());
        tooltipDTO.setDatumLetztePlausibleMessung(messstelle.getDatumLetztePlausibleMeldung() == null ? "unbekannt" : messstelle.getDatumLetztePlausibleMeldung().toString());
        if (CollectionUtils.isNotEmpty(messstelle.getMessquerschnitte())) {
            tooltipDTO.setDetektierteVerkehrsarten(messstelle.getMessquerschnitte().get(0).getDetektierteVerkehrsarten());
        }
        return tooltipDTO;
    }

}
