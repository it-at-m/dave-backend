package de.muenchen.dave.services.auswertung;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.FahrbeziehungVisumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungVisumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleVisumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungVisumDTO;
import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.mapper.ZaehlstelleMapper;
import de.muenchen.dave.domain.mapper.ZaehlungMapper;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuswertungVisumService {

    private final ZaehlstelleIndex zaehlstelleIndex;

    private final ZaehlstelleMapper zaehlstelleMapper;

    private final ZaehlungMapper zaehlungMapper;

    private final LadeZaehldatenService ladeZaehldatenService;

    public AuswertungVisumService(final ZaehlstelleIndex zaehlstelleIndex,
            final ZaehlstelleMapper zaehlstelleMapper,
            final ZaehlungMapper zaehlungMapper,
            final LadeZaehldatenService ladeZaehldatenService) {
        this.zaehlstelleIndex = zaehlstelleIndex;
        this.zaehlstelleMapper = zaehlstelleMapper;
        this.zaehlungMapper = zaehlungMapper;
        this.ladeZaehldatenService = ladeZaehldatenService;
    }

    /**
     * Diese Methode ermittelt für den in den Parametern angegeben Monatszeitraum die durchgeführten
     * Zählungen je Zählstelle.
     * Je relevante Fahrbeziehung werden die Zahldaten an die Zählung angehangen.
     * Für eine Kreuzung werden die Fahrbeziehung "x nach alle" und "alle nach x" betrachtet.
     * Der Kreisverkehr beinhaltet nur die Fahrbeziehungen "x nach alle".
     *
     * @param jahr welches ausgewertet werden soll.
     * @param monat im jahr welches ausgewertet werden soll.
     * @return durchgeführten Zählungen je Zählstelle mit den Zähldaten je relevante Fahrbeziehung.
     */
    public LadeAuswertungVisumDTO getAuswertungVisum(final Integer jahr, final Integer monat) {
        final var auswertungVisum = new LadeAuswertungVisumDTO();
        auswertungVisum.setZaehlstellen(getZaehlstellenWithZaehlungenAndZaehldatenForYearAndMonth(jahr, monat));
        return auswertungVisum;
    }

    public List<LadeZaehlstelleVisumDTO> getZaehlstellenWithZaehlungenAndZaehldatenForYearAndMonth(final Integer jahr, final Integer monat) {
        final var monatTextuell = ZaehldatenProcessingUtil.getMonatTextuell(monat);
        return zaehlstelleIndex.findAllByZaehlungenJahr(jahr.toString()).parallelStream()
                .map(zaehlstelle -> {

                    // Durcharbeiten der Zählungen für das im Parameter gegebene Jahr und Monat
                    final List<LadeZaehlungVisumDTO> relevantZaehlungenVisum = zaehlstelle.getZaehlungen().stream()
                            .filter(zaehlung -> isZaehlungRelevant(zaehlung, jahr.toString(), monatTextuell))
                            .parallel()
                            .map(zaehlung -> {
                                // Extrahieren der Zähldaten für alle Fahrbeziehungen einer Zählung
                                final List<FahrbeziehungVisumDTO> fahrbeziehungenVisum = zaehlung.getFahrbeziehungen().stream()
                                        .map(AuswertungVisumService::getFahrbeziehungenVisum)
                                        .flatMap(Collection::stream)
                                        // Entfernen von eventuell auftretenden Duplikaten
                                        .distinct()
                                        .parallel()
                                        .map(fahrbeziehungVisum -> ladeZaehldaten(fahrbeziehungVisum, zaehlung))
                                        .collect(Collectors.toList());

                                // Setzen der zuvor extrahierten Zaehldaten in Zählungs-DTO für Visum.
                                final var zaehlungVisum = zaehlungMapper.bean2ladeVisumDto(zaehlung);
                                zaehlungVisum.setFahrbeziehungen(fahrbeziehungenVisum);
                                return zaehlungVisum;
                            })
                            .collect(Collectors.toList());

                    // Setzen der relevanten Zählungen samt Zaehldaten in Zaehlstellen-DTO für Visum.
                    // Falls für den Monat keine Zählung existiert wird für die Zählstelle null zurückgegeben.
                    final LadeZaehlstelleVisumDTO zaehlstelleVisum;
                    if (relevantZaehlungenVisum.isEmpty()) {
                        zaehlstelleVisum = null;
                    } else {
                        zaehlstelleVisum = zaehlstelleMapper.bean2ladeVisumDto(zaehlstelle);
                        zaehlstelleVisum.setZaehlungen(relevantZaehlungenVisum);
                    }
                    return zaehlstelleVisum;
                })
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    public static boolean isZaehlungRelevant(final Zaehlung zaehlung, final String jahr, final String monat) {
        return StringUtils.equals(jahr, zaehlung.getJahr())
                && StringUtils.equals(monat, zaehlung.getMonat());
    }

    /**
     * Diese Methode erstellt für eine Fahrbeziehung folgende Visum-relevanten Objekte.
     * <p>
     * Für eine Kreuzung wird eine FahrbeziehungVisum für den Von-Knotenarm nach alle Knotenarme und
     * ein zweite FahrbeziehungVisum für alle Knotenarme zum nach-Knotenarm erstellt.
     * <p>
     * Für den Kreisverkehr wird nur eine FahrbeziehungVisum erstellt.
     *
     * @param fahrbeziehung Fahrbeziehung
     * @return die Visum-relevanten Fahrbeziehungsobjekte zur Extraktion der Zeitintervalle.
     */
    public static List<FahrbeziehungVisumDTO> getFahrbeziehungenVisum(final Fahrbeziehung fahrbeziehung) {
        final List<FahrbeziehungVisumDTO> fahrbeziehungenVisum = new ArrayList<>();
        FahrbeziehungVisumDTO fahrbeziehungVisum;
        if (BooleanUtils.isTrue(fahrbeziehung.getIsKreuzung())) {
            fahrbeziehungVisum = new FahrbeziehungVisumDTO();
            fahrbeziehungVisum.setVon(fahrbeziehung.getVon());
            fahrbeziehungVisum.setNach(null);
            fahrbeziehungenVisum.add(fahrbeziehungVisum);
            fahrbeziehungVisum = new FahrbeziehungVisumDTO();
            fahrbeziehungVisum.setVon(null);
            fahrbeziehungVisum.setNach(fahrbeziehung.getNach());
            fahrbeziehungenVisum.add(fahrbeziehungVisum);
        } else {
            fahrbeziehungVisum = new FahrbeziehungVisumDTO();
            fahrbeziehungVisum.setVon(fahrbeziehung.getKnotenarm());
            fahrbeziehungVisum.setNach(null);
            fahrbeziehungenVisum.add(fahrbeziehungVisum);
            fahrbeziehungVisum = new FahrbeziehungVisumDTO();
            fahrbeziehungVisum.setVon(null);
            fahrbeziehungVisum.setNach(fahrbeziehung.getKnotenarm());
            fahrbeziehungenVisum.add(fahrbeziehungVisum);
        }
        return fahrbeziehungenVisum;
    }

    /**
     * Diese Methode erstellt die Optionen zur extraktion der Daten in Methode
     * {@link AuswertungVisumService#ladeZaehldaten(FahrbeziehungVisumDTO, Zaehlung)}.
     *
     * @param fahrbeziehungVisum zum setzen der von und nach-Knotenarme in den Optionen.
     * @param zaehlung zum setzen der Optionen.
     * @return die Optionen für die Datenextraktion.
     */
    public static OptionsDTO createOptions(final FahrbeziehungVisumDTO fahrbeziehungVisum,
            final Zaehlung zaehlung) {
        final var options = ZaehldatenProcessingUtil.createHardcodedOptions(zaehlung);
        options.setVonKnotenarm(fahrbeziehungVisum.getVon());
        options.setNachKnotenarm(fahrbeziehungVisum.getNach());
        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        return options;
    }

    /**
     * Diese Methode holt die Zeitintervalle aus der Datenbank und gibt diese als Zaehldaten
     * im Objekt fahrbeziehungVisum zurück.
     *
     * @param fahrbeziehungVisum die für die Datenextraktion relevanten Knotenarme welche die
     *            Fahrbeziehung definieren.
     * @param zaehlung für welche die Daten extrahiert werden sollen.
     * @return die fahrbeziehungVisum mit den Zaehldaten für die Zaehlung der Fahrbeziehung.
     */
    public FahrbeziehungVisumDTO ladeZaehldaten(final FahrbeziehungVisumDTO fahrbeziehungVisum,
            final Zaehlung zaehlung) {
        List<LadeZaehldatumDTO> zaehldaten;
        try {
            zaehldaten = ladeZaehldatenService.ladeZaehldaten(
                    UUID.fromString(zaehlung.getId()),
                    createOptions(fahrbeziehungVisum, zaehlung)).getZaehldaten();
        } catch (DataNotFoundException dnfe) {
            zaehldaten = new ArrayList<>();
        }
        fahrbeziehungVisum.setZaehldaten(zaehldaten);
        return fahrbeziehungVisum;
    }

}
