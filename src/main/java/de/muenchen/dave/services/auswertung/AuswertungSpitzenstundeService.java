package de.muenchen.dave.services.auswertung;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungSpitzenstundeDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.mapper.LadeZaehldatumMapper;
import de.muenchen.dave.domain.mapper.ZaehlungMapper;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.exceptions.IncorrectZeitauswahlException;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtilNg;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuswertungSpitzenstundeService {

    private static final String EXCEPTION_NO_SPITZENSTUNDE = "Keine Spitzenstunde vorhanden";

    private final ZeitintervallRepository zeitintervallRepository;

    private final ZaehlstelleIndexService indexService;

    private final LadeZaehldatumMapper ladeZaehldatumMapper;

    private final LadeZaehldatenService ladeZaehldatenService;

    private final ZaehlungMapper zaehlungMapper;

    /**
     * Die Methode zur Ausführung der Spitzenstundenauswertung.
     *
     * @param zaehlstellenNummer der Zählstelle für welche die Zähung stattgefunden hat.
     * @param zaehlart der Zählung.
     * @param zaehldatum der Zählung.
     * @param zeitblock für welchen die Spitzentundeauswertung gemacht werden soll.
     * @param zeitauswahl welche die Ausprägung
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ},
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_RAD} oder
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS}
     *            haben darf.
     * @return die Spitzentundenauswertung
     * @throws DataNotFoundException sobald im {@link ZaehlstelleIndexService} keine {@link Zaehlung}
     *             oder {@link Zaehlstelle} vorhanden ist.
     * @throws IncorrectZeitauswahlException sobald die Zeitauswahl nicht vom Typ
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ},
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_RAD} oder
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS} ist.
     */
    public List<LadeAuswertungSpitzenstundeDTO> getAuswertungSpitzenstunde(final String zaehlstellenNummer,
            final Zaehlart zaehlart,
            final LocalDate zaehldatum,
            final Zeitblock zeitblock,
            final String zeitauswahl) throws DataNotFoundException, IncorrectZeitauswahlException {
        final Zaehlung zaehlung = indexService.getZaehlung(zaehlstellenNummer, zaehlart, zaehldatum);
        final Zaehlstelle zaehlstelle = indexService.getZaehlstelleByZaehlungId(zaehlung.getId());
        final List<LadeAuswertungSpitzenstundeDTO> ladeAuswertungSpitzenstunden = getSpitzenstunden(
                zaehlung,
                zeitblock,
                zeitauswahl,
                BooleanUtils.isTrue(zaehlung.getKreisverkehr()));
        // Anreichern mit Zählungs- und Zählstellenattributen
        ladeAuswertungSpitzenstunden.forEach(ladeAuswertungSpitzenstunde -> {
            ladeAuswertungSpitzenstunde.setNummerZaehlstelle(zaehlstelle.getNummer());
            ladeAuswertungSpitzenstunde.setStadtbezirk(zaehlstelle.getStadtbezirk());
            ladeAuswertungSpitzenstunde.setStadtbezirkNummer(zaehlstelle.getStadtbezirkNummer());
            ladeAuswertungSpitzenstunde.setDatum(zaehlung.getDatum());
            ladeAuswertungSpitzenstunde.setZaehlart(zaehlung.getZaehlart());
            ladeAuswertungSpitzenstunde.setKreuzungsname(zaehlung.getKreuzungsname());
            ladeAuswertungSpitzenstunde.setSonderzaehlung(zaehlung.getSonderzaehlung());
            ladeAuswertungSpitzenstunde.setZaehlsituation(zaehlung.getZaehlsituation());
            ladeAuswertungSpitzenstunde.setZaehlsituationErweitert(zaehlung.getZaehlsituationErweitert());
        });
        // Sortieren
        ladeAuswertungSpitzenstunden.sort(
                Comparator.comparingInt(LadeAuswertungSpitzenstundeDTO::getVon)
                        .thenComparingInt(LadeAuswertungSpitzenstundeDTO::getNach));
        return ladeAuswertungSpitzenstunden;
    }

    /**
     * Die Methode zur Ausführung der Spitzenstundenauswertung.
     *
     * @param zaehlung Zaehlung
     * @param zeitblock für welchen die Spitzentundeauswertung gemacht werden soll.
     * @param zeitauswahl welche die Ausprägung
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ},
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_RAD} oder
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS} haben
     *            darf.
     * @param kreisverkehr hat Ausprägung true falls die Zeitintervalle der Spitzenstunde für einen
     *            Kreisverkehr extrahiert werden sollen, anderfalls false.
     * @return die Liste der einzelnen Verkehrsbeziehungen der Spitzenstunde.
     * @throws IncorrectZeitauswahlException sobald die Zeitauswahl nicht vom Typ
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ},
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_RAD} oder
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS} ist.
     * @throws DataNotFoundException falls keine Spitzenstunde für den gewählten Zeitblock gefunden
     *             wurde.
     */
    public List<LadeAuswertungSpitzenstundeDTO> getSpitzenstunden(
            final Zaehlung zaehlung,
            final Zeitblock zeitblock,
            final String zeitauswahl,
            final boolean kreisverkehr) throws IncorrectZeitauswahlException, DataNotFoundException {

        return extractSpitzenstundenAllVerkehrsbeziehungen(
                zaehlung,
                zeitblock,
                zeitauswahl,
                kreisverkehr).stream()
                .map(spStdVerkehrsbeziehung -> mapToAuswertungSpitzenstundeDTO(spStdVerkehrsbeziehung, zaehlung.getPkwEinheit()))
                .collect(Collectors.toList());
    }

    /**
     * Die Methode zur Ausführung der Spitzenstundenauswertung.
     *
     * @param zaehlung die Zaehlung
     * @param zeitblock für welchen die Spitzentundeauswertung gemacht werden soll.
     * @param zeitauswahl tbd
     * @param kreisverkehr hat Ausprägung true falls die Zeitintervalle der Spitzenstunde für einen
     *            Kreisverkehr extrahiert werden sollen, anderfalls
     *            false.
     * @return die Liste der einzelnen Verkehrsbeziehungen der Spitzenstunde.
     * @throws DataNotFoundException falls keine Spitzenstunde gefunden wurde.
     */
    public List<Zeitintervall> extractSpitzenstundenAllVerkehrsbeziehungen(
            final Zaehlung zaehlung,
            final Zeitblock zeitblock,
            final String zeitauswahl,
            final boolean kreisverkehr) throws DataNotFoundException, IncorrectZeitauswahlException {

        // Setup data extraction
        final TypeZeitintervall typeSpitzenstunde = ZeitintervallGleitendeSpitzenstundeUtilNg.getRelevantTypeZeitintervallFromZeitauswahl(zeitauswahl);
        final Integer sortingIndex = getSortingIndex(zeitblock, typeSpitzenstunde);
        final var zaehldatenIntervall = ZaehldatenIntervall.STUNDE_VIERTEL;
        final var options = new OptionsDTO();
        options.setChosenVerkehrsbeziehungen(zaehlungMapper.mapVerkehrsbeziehungen(zaehlung.getVerkehrsbeziehungen()));
        options.setChosenLaengsverkehre(zaehlungMapper.mapLaengsverkehre(zaehlung.getLaengsverkehr()));
        options.setChosenQuerungsverkehre(zaehlungMapper.mapQuerungsverkehre(zaehlung.getQuerungsverkehr()));
        options.setZeitblock(zeitblock);
        options.setZeitauswahl(zeitauswahl);
        options.setIntervall(zaehldatenIntervall);
        final var zaehlart = Zaehlart.valueOf(zaehlung.getZaehlart());

        // Extrahieren der Spitzenstunde der Zählung über alle Verkehrsbeziehungen.
        final var extractedSpitzenstunde = ladeZaehldatenService.extractZeitintervalleSpitzenstunden(
                UUID.fromString(zaehlung.getId()),
                zaehlart,
                zaehlung.getKreisverkehr(),
                options);

        // Spitzenstunde alle nach alle
        final Zeitintervall spitzenstunde = Optional.ofNullable(extractedSpitzenstunde.getLast())
                .orElseThrow(() -> new DataNotFoundException(EXCEPTION_NO_SPITZENSTUNDE));
        // Extrahieren der Zeitintervalle je Verkehrsbeziehung welche die Spitzstunde ausmachen.
        final List<Zeitintervall> spitzenstundeZeitintervalle;
        final var isZaehlartQjsFjSOrQu = Set.of(Zaehlart.QJS, Zaehlart.FJS, Zaehlart.QU).contains(zaehlart);
        if (kreisverkehr && !isZaehlartQjsFjSOrQu) {
            spitzenstundeZeitintervalle = zeitintervallRepository
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeOrderBySortingIndexAsc(
                            UUID.fromString(zaehlung.getId()),
                            spitzenstunde.getStartUhrzeit(),
                            spitzenstunde.getEndeUhrzeit(),
                            FahrbewegungKreisverkehr.HINEIN,
                            zaehldatenIntervall.getTypeZeitintervall());
        } else {
            spitzenstundeZeitintervalle = zeitintervallRepository
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndVerkehrsbeziehungNachNotNullAndTypeOrderBySortingIndexAsc(
                            UUID.fromString(zaehlung.getId()),
                            spitzenstunde.getStartUhrzeit(),
                            spitzenstunde.getEndeUhrzeit(),
                            zaehldatenIntervall.getTypeZeitintervall());
        }

        return ZeitintervallGleitendeSpitzenstundeUtilNg
                .getGleitendeSpitzenstundenByBewegungsbeziehung(
                        UUID.fromString(zaehlung.getId()),
                        zeitblock,
                        zaehlart,
                        spitzenstundeZeitintervalle,
                        Set.of(zaehldatenIntervall.getTypeZeitintervall()))
                .stream()
                .peek(zeitintervall -> {
                    zeitintervall.setStartUhrzeit(spitzenstunde.getStartUhrzeit());
                    zeitintervall.setEndeUhrzeit(spitzenstunde.getEndeUhrzeit());
                })
                .toList();
    }

    /**
     * Diese Methode führt ein Mapping des {@link Zeitintervall}s auf
     * {@link LadeAuswertungSpitzenstundeDTO} durch.
     *
     * @param spitzenstunde als {@link Zeitintervall}.
     * @param pkwEinheit als {@link PkwEinheit}
     * @return {@link LadeAuswertungSpitzenstundeDTO} des {@link Zeitintervall}s.
     */
    public LadeAuswertungSpitzenstundeDTO mapToAuswertungSpitzenstundeDTO(final Zeitintervall spitzenstunde,
            final PkwEinheit pkwEinheit) {
        final LadeZaehldatumDTO ladeZaehldatumDTO = LadeZaehldatenService.mapToZaehldatum(
                spitzenstunde,
                pkwEinheit,
                new OptionsDTO());
        final LadeAuswertungSpitzenstundeDTO ladeZaehldatumWithDirectionDTO = ladeZaehldatumMapper
                .ladeZaehldatumDtoToLadeAuswertungSpitzenstundeDto(ladeZaehldatumDTO);
        ladeZaehldatumWithDirectionDTO.setVon(spitzenstunde.getVerkehrsbeziehung().getVon());
        ladeZaehldatumWithDirectionDTO.setNach(spitzenstunde.getVerkehrsbeziehung().getNach());
        return ladeZaehldatumWithDirectionDTO;
    }

    /**
     * Ermittelt den Sortierindex der Spitzenstunde abhängig vom {@link Zeitblock} und von
     * {@link TypeZeitintervall} betreffend die Spitzenstunde. Der in dieser
     * Methode erstellte Sortierindex identifiziert somit die relevante Spitzenstunde für den gewählten
     * Zeitblock.
     *
     * @param zeitblock als {@link Zeitblock}
     * @param typeSpitzenstunde als {@link TypeZeitintervall}
     * @return der Sortierindex welcher auch im {@link Zeitintervall} der Spitzenstunde hinterlegt ist.
     */
    public int getSortingIndex(
            final Zeitblock zeitblock,
            final TypeZeitintervall typeSpitzenstunde) {
        // Erforderlich um mit Util-Methoden den SortingIndex zu ermitteln
        final Zeitintervall dummyZeitintervallForIndexCreation = new Zeitintervall();
        dummyZeitintervallForIndexCreation.setStartUhrzeit(zeitblock.getStart().plusMinutes(15));
        dummyZeitintervallForIndexCreation.setEndeUhrzeit(zeitblock.getStart().plusMinutes(30));
        final int sortingIndex;
        if (typeSpitzenstunde.equals(TypeZeitintervall.SPITZENSTUNDE_KFZ)) {
            sortingIndex = ZeitintervallGleitendeSpitzenstundeUtilNg.getSortingIndexKfz(dummyZeitintervallForIndexCreation, zeitblock);
        } else if (typeSpitzenstunde.equals(TypeZeitintervall.SPITZENSTUNDE_RAD)) {
            sortingIndex = ZeitintervallGleitendeSpitzenstundeUtilNg.getSortingIndexRad(dummyZeitintervallForIndexCreation, zeitblock);
        } else if (typeSpitzenstunde.equals(TypeZeitintervall.SPITZENSTUNDE_FUSS)) {
            sortingIndex = ZeitintervallGleitendeSpitzenstundeUtilNg.getSortingIndexFuss(dummyZeitintervallForIndexCreation, zeitblock);
        } else {
            sortingIndex = -1;
        }
        return sortingIndex;
    }

}
