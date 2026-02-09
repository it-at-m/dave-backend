package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.domain.Bewegungsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ZaehldatenExtractorService {

    final ZeitintervallRepository zeitintervallRepository;

    public Map<Bewegungsbeziehung, List<Zeitintervall>> extractZaehldaten(
            final UUID zaehlungId,
            final Zaehlung zaehlung,
            final OptionsDTO options
    ) {
        final List<Zeitintervall> zeitintervalle;
        final var zaehlart = Zaehlart.valueOf(zaehlung.getZaehlart());



        if (Zaehlart.FJS.equals(zaehlart)) {
            zeitintervalle = null;
        } else if (Zaehlart.QU.equals(zaehlart)) {
            zeitintervalle = null;
        } else if (Zaehlart.QJS.equals(zaehlart)) {
            zeitintervalle = null;
        } else {

            // alle nicht ausschließlichen Fuss und Radverkehrszählungen
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr;
            final Integer vonKnotenarm;
            final Integer nachKnotenarm;
            if (zaehlung.getKreisverkehr()) {
                /*
                 * In {@link de.muenchen.dave.domain.Verkehrsbeziehung} definiert das Attribut "von"
                 * den im Kreisverkehr jeweils betroffenen Knotenarm.
                 * Das Attribut "nach" ist immer "null".
                 */
                if (ObjectUtils.isNotEmpty(options.getVonKnotenarm()) && ObjectUtils.isEmpty(options.getNachKnotenarm())) {
                    // Hinein
                    vonKnotenarm = options.getVonKnotenarm();
                    fahrbewegungKreisverkehr = FahrbewegungKreisverkehr.HINEIN;
                } else if (ObjectUtils.isEmpty(options.getVonKnotenarm()) && ObjectUtils.isNotEmpty(options.getNachKnotenarm())) {
                    // Heraus
                    vonKnotenarm = options.getNachKnotenarm();
                    fahrbewegungKreisverkehr = FahrbewegungKreisverkehr.HERAUS;
                } else {
                    // Alles Hinein + Heraus + Vorbei
                    vonKnotenarm = null;
                    fahrbewegungKreisverkehr = null;
                }
                nachKnotenarm = null;
            } else {
                vonKnotenarm = options.getVonKnotenarm();
                nachKnotenarm = options.getNachKnotenarm();
                fahrbewegungKreisverkehr = null;
            }

            // NICHT KORREKT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            zeitintervalle =  zeitintervallRepository
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
                            zaehlungId,
                            options.getZeitblock().getStart(),
                            options.getZeitblock().getEnd(),
                            vonKnotenarm,
                            nachKnotenarm,
                            fahrbewegungKreisverkehr,
                            LadeZaehldatenService.getTypesAccordingChosenOptions(options));



        }


        return CollectionUtils.emptyIfNull(zeitintervalle)
                .stream()
                .collect(Collectors.groupingByConcurrent(zeitintervall -> this.getBewegungsbeziehungFromZeitintervallAccordingZaehlart(zeitintervall, zaehlart)));
    }


    protected Bewegungsbeziehung getBewegungsbeziehungFromZeitintervallAccordingZaehlart(final Zeitintervall zeitintervall, final Zaehlart zaehlart) {
        if (Zaehlart.FJS.equals(zaehlart)) {
            return zeitintervall.getLaengsverkehr();
        } else if (Zaehlart.QU.equals(zaehlart)) {
            return zeitintervall.getQuerungsverkehr();
        } else {
            return zeitintervall.getVerkehrsbeziehung();
        }
    }


    /**
     * Diese Methode erzeugt auf Basis der gewählten Verkehrsbeziehung sowie Bezeichners für Kreuzung
     * und Kreisverkehr die für die Datenextraktion relevante {@link FahrbewegungKreisverkehr}.
     *
     * @param von als Startknotenarm.
     * @param nach als Zielknotenarm
     * @param isKreisverkehr als Bezeichner ob erzeugung für Kreuzung oder Kreisverkehr.
     * @return null falls es sich um eine Kreuzung oder um einen Kreisverkehr mit
     *         Verkehrsbeziehungsauswahl "alle nach alle" handelt.
     *         {@link FahrbewegungKreisverkehr#HINEIN} falls es sich um eine Verkehrsbeziehungsauswahl
     *         mit "X nach alle" handelt.
     *         {@link FahrbewegungKreisverkehr#HERAUS} falls es sich um eine Verkehrsbeziehungsauswahl
     *         mit "alle nach X" handelt.
     */
    protected static FahrbewegungKreisverkehr createFahrbewegungKreisverkehr(final Integer von,
                                                                             final Integer nach,
                                                                             final Boolean isKreisverkehr) {
        final FahrbewegungKreisverkehr fahrbewegungKreisverkehr;
        if (isKreisverkehr) {
            if (ObjectUtils.isNotEmpty(von) && ObjectUtils.isEmpty(nach)) {
                fahrbewegungKreisverkehr = FahrbewegungKreisverkehr.HINEIN;
            } else if (ObjectUtils.isEmpty(von) && ObjectUtils.isNotEmpty(nach)) {
                fahrbewegungKreisverkehr = FahrbewegungKreisverkehr.HERAUS;
            } else {
                fahrbewegungKreisverkehr = null;
            }
        } else {
            fahrbewegungKreisverkehr = null;
        }
        return fahrbewegungKreisverkehr;
    }


}
