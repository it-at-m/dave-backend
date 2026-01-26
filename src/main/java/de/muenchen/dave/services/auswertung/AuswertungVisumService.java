package de.muenchen.dave.services.auswertung;

import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.VerkehrsbeziehungVisumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungVisumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleVisumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungVisumDTO;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.mapper.ZaehlstelleMapper;
import de.muenchen.dave.domain.mapper.ZaehlungMapper;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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

    public static boolean isZaehlungRelevant(final Zaehlung zaehlung, final String jahr, final String monat) {
        return StringUtils.equals(jahr, zaehlung.getJahr())
                && StringUtils.equals(monat, zaehlung.getMonat());
    }

    /**
     * Diese Methode erstellt für eine Verkehrsbeziehung folgende Visum-relevanten Objekte.
     * <p>
     * Für eine Kreuzung wird eine VerkehrsbeziehungVisum für den Von-Knotenarm nach alle Knotenarme und ein
     * zweite VerkehrsbeziehungVisum für alle Knotenarme zum nach-Knotenarm erstellt.
     * <p>
     * Für den Kreisverkehr wird nur eine VerkehrsbeziehungVisum erstellt.
     *
     * @param verkehrsbeziehung Verkehrsbeziehung
     * @return die Visum-relevanten Verkehrsbeziehungsobjekte zur Extraktion der Zeitintervalle.
     */
    public static List<VerkehrsbeziehungVisumDTO> getVerkehrsbeziehungVisum(final Verkehrsbeziehung verkehrsbeziehung, final Zaehlung zaehlung) {
        final var verkehrsbeziehungenVisum = new ArrayList<VerkehrsbeziehungVisumDTO>();
        VerkehrsbeziehungVisumDTO verkehrsbeziehungVisum;
        boolean isKreuzung = verkehrsbeziehung.getIsKreuzung() == null ? !zaehlung.getKreisverkehr() : verkehrsbeziehung.getIsKreuzung();
        if (isKreuzung) {
            verkehrsbeziehungVisum = new VerkehrsbeziehungVisumDTO();
            verkehrsbeziehungVisum.setVon(verkehrsbeziehung.getVon());
            verkehrsbeziehungVisum.setNach(null);
            verkehrsbeziehungenVisum.add(verkehrsbeziehungVisum);
            verkehrsbeziehungVisum = new VerkehrsbeziehungVisumDTO();
            verkehrsbeziehungVisum.setVon(null);
            verkehrsbeziehungVisum.setNach(verkehrsbeziehung.getNach());
            verkehrsbeziehungenVisum.add(verkehrsbeziehungVisum);
        } else {
            verkehrsbeziehungVisum = new VerkehrsbeziehungVisumDTO();
            verkehrsbeziehungVisum.setVon(verkehrsbeziehung.getKnotenarm());
            verkehrsbeziehungVisum.setNach(null);
            verkehrsbeziehungenVisum.add(verkehrsbeziehungVisum);
            verkehrsbeziehungVisum = new VerkehrsbeziehungVisumDTO();
            verkehrsbeziehungVisum.setVon(null);
            verkehrsbeziehungVisum.setNach(verkehrsbeziehung.getKnotenarm());
            verkehrsbeziehungenVisum.add(verkehrsbeziehungVisum);
        }
        return verkehrsbeziehungenVisum;
    }

    /**
     * Diese Methode erstellt die Optionen zur extraktion der Daten in Methode
     * {@link AuswertungVisumService#ladeZaehldaten(VerkehrsbeziehungVisumDTO, Zaehlung)}.
     *
     * @param fahrbeziehungVisum zum setzen der von und nach-Knotenarme in den Optionen.
     * @param zaehlung zum setzen der Optionen.
     * @return die Optionen für die Datenextraktion.
     */
    public static OptionsDTO createOptions(final VerkehrsbeziehungVisumDTO fahrbeziehungVisum,
            final Zaehlung zaehlung) {
        final var options = ZaehldatenProcessingUtil.createHardcodedOptions(zaehlung);
        options.setVonKnotenarm(fahrbeziehungVisum.getVon());
        options.setNachKnotenarm(fahrbeziehungVisum.getNach());
        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        return options;
    }

    /**
     * Diese Methode ermittelt für den in den Parametern angegeben Monatszeitraum die durchgeführten
     * Zählungen je Zählstelle. Je relevante Verkehrsbeziehung werden
     * die Zahldaten an die Zählung angehangen. Für eine Kreuzung werden die Verkehrsbeziehung "x nach
     * alle"
     * und "alle nach x" betrachtet. Der Kreisverkehr
     * beinhaltet nur die Fahrbeziehungen "x nach alle".
     *
     * @param jahr welches ausgewertet werden soll.
     * @param monat im jahr welches ausgewertet werden soll.
     * @return durchgeführten Zählungen je Zählstelle mit den Zähldaten je relevante Verkehrsbeziehung.
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
                                final List<VerkehrsbeziehungVisumDTO> verkehrsbeziehungenVisum = CollectionUtils.emptyIfNull(zaehlung.getVerkehrsbeziehungen()).stream()
                                        .map(fz -> AuswertungVisumService.getVerkehrsbeziehungVisum(fz, zaehlung))
                                        .flatMap(Collection::stream)
                                        // Entfernen von eventuell auftretenden Duplikaten
                                        .distinct()
                                        .parallel()
                                        .map(verkehrsbeziehungVisum -> ladeZaehldaten(verkehrsbeziehungVisum, zaehlung))
                                        .collect(Collectors.toList());

                                // Setzen der zuvor extrahierten Zaehldaten in Zählungs-DTO für Visum.
                                final var zaehlungVisum = zaehlungMapper.bean2ladeVisumDto(zaehlung);
                                zaehlungVisum.setVerkehrsbeziehungen(verkehrsbeziehungenVisum);
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

    /**
     * Diese Methode holt die Zeitintervalle aus der Datenbank und gibt diese als Zaehldaten im Objekt
     * verkehrsbeziehungVisum zurück.
     *
     * @param verkehrsbeziehungVisum die für die Datenextraktion relevanten Knotenarme welche die
     *            Verkehrsbeziehung definieren.
     * @param zaehlung für welche die Daten extrahiert werden sollen.
     * @return die VerkehrsbeziehungVisum mit den Zaehldaten für die Zaehlung der Verkehrsbeziehung.
     */
    public VerkehrsbeziehungVisumDTO ladeZaehldaten(final VerkehrsbeziehungVisumDTO verkehrsbeziehungVisum,
                                                    final Zaehlung zaehlung) {
        List<LadeZaehldatumDTO> zaehldaten;
        try {
            zaehldaten = ladeZaehldatenService.ladeZaehldaten(
                    UUID.fromString(zaehlung.getId()),
                    createOptions(verkehrsbeziehungVisum, zaehlung)).getZaehldaten();
        } catch (DataNotFoundException dnfe) {
            zaehldaten = new ArrayList<>();
        }
        verkehrsbeziehungVisum.setZaehldaten(zaehldaten);
        return verkehrsbeziehungVisum;
    }

}
