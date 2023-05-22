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
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.mapper.LadeZaehldatumMapper;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.exceptions.IncorrectZeitauswahlException;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.IndexService;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuswertungSpitzenstundeService {

    private static final String EXCEPTION_NO_SPITZENSTUNDE = "Keine Spitzenstunde vorhanden";

    private final ZeitintervallRepository zeitintervallRepository;

    private final IndexService indexService;

    private final LadeZaehldatumMapper ladeZaehldatumMapper;

    public AuswertungSpitzenstundeService(final ZeitintervallRepository zeitintervallRepository,
            final IndexService indexService,
            final LadeZaehldatumMapper ladeZaehldatumMapper) {
        this.zeitintervallRepository = zeitintervallRepository;
        this.indexService = indexService;
        this.ladeZaehldatumMapper = ladeZaehldatumMapper;

    }

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
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS} haben darf.
     * @return die Spitzentundenauswertung
     * @throws DataNotFoundException sobald im {@link IndexService} keine {@link Zaehlung} oder
     *             {@link Zaehlstelle} vorhanden ist.
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
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS} haben darf.
     * @param kreisverkehr hat Ausprägung true falls die Zeitintervalle der Spitzenstunde für einen
     *            Kreisverkehr
     *            extrahiert werden sollen, anderfalls false.
     * @return die Liste der einzelnen Fahrbeziehungen der Spitzenstunde.
     * @throws IncorrectZeitauswahlException sobald die Zeitauswahl nicht vom Typ
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ},
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_RAD} oder
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS} ist.
     * @throws DataNotFoundException falls keine Spitzenstunde für den gewählten Zeitblock gefunden
     *             wurde.
     */
    public List<LadeAuswertungSpitzenstundeDTO> getSpitzenstunden(final Zaehlung zaehlung,
            final Zeitblock zeitblock,
            final String zeitauswahl,
            final boolean kreisverkehr) throws IncorrectZeitauswahlException, DataNotFoundException {
        final TypeZeitintervall typeSpitzenstunde = getRelevantTypeZeitintervallFromZeitauswahl(zeitauswahl);
        return extractSpitzenstundenAllFahrbeziehungen(
                zaehlung.getId(),
                zeitblock,
                typeSpitzenstunde,
                kreisverkehr).stream()
                        .map(spStdFahrbeziehung -> mapToAuswertungSpitzenstundeDTO(spStdFahrbeziehung, zaehlung.getPkwEinheit()))
                        .collect(Collectors.toList());
    }

    /**
     * Die Methode zur Ausführung der Spitzenstundenauswertung.
     *
     * @param zaehlungId ID der Zaehlung
     * @param zeitblock für welchen die Spitzentundeauswertung gemacht werden soll.
     * @param typeSpitzenstunde welche die Ausprägung {@link TypeZeitintervall#SPITZENSTUNDE_KFZ},
     *            {@link TypeZeitintervall#SPITZENSTUNDE_RAD} oder
     *            {@link TypeZeitintervall#SPITZENSTUNDE_FUSS} haben darf.
     * @param kreisverkehr hat Ausprägung true falls die Zeitintervalle der Spitzenstunde für einen
     *            Kreisverkehr
     *            extrahiert werden sollen, anderfalls false.
     * @return die Liste der einzelnen Fahrbeziehungen der Spitzenstunde.
     * @throws DataNotFoundException falls keine Spitzenstunde gefunden wurde.
     */
    public List<Zeitintervall> extractSpitzenstundenAllFahrbeziehungen(final String zaehlungId,
            final Zeitblock zeitblock,
            final TypeZeitintervall typeSpitzenstunde,
            final boolean kreisverkehr) throws DataNotFoundException {
        final Integer sortingIndex = getSortingIndex(zeitblock, typeSpitzenstunde);
        // Extrahieren der Spitzenstunde der Zählung über alle Fahrbeziehungen.
        final Zeitintervall spitzenstunde = zeitintervallRepository.findByZaehlungIdAndTypeAndFahrbeziehungVonNullAndFahrbeziehungNachNullAndSortingIndex(
                UUID.fromString(zaehlungId),
                typeSpitzenstunde,
                sortingIndex).orElseThrow(() -> new DataNotFoundException(EXCEPTION_NO_SPITZENSTUNDE));
        // Extrahieren der Zeitintervalle je Fahrbeziehung welche die Spitzstunde ausmachen.
        final List<Zeitintervall> spitzenstundeZeitintevalle;
        if (kreisverkehr) {
            spitzenstundeZeitintevalle = zeitintervallRepository
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndFahrbeziehungVonNotNullAndFahrbeziehungFahrbewegungKreisverkehrAndTypeOrderBySortingIndexAsc(
                            UUID.fromString(zaehlungId),
                            spitzenstunde.getStartUhrzeit(),
                            spitzenstunde.getEndeUhrzeit(),
                            FahrbewegungKreisverkehr.HINEIN,
                            TypeZeitintervall.STUNDE_VIERTEL);
        } else {
            spitzenstundeZeitintevalle = zeitintervallRepository
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndFahrbeziehungVonNotNullAndFahrbeziehungNachNotNullAndTypeOrderBySortingIndexAsc(
                            UUID.fromString(zaehlungId),
                            spitzenstunde.getStartUhrzeit(),
                            spitzenstunde.getEndeUhrzeit(),
                            TypeZeitintervall.STUNDE_VIERTEL);
        }
        // Erstellen der aggregierten Spitzenstunde je Fahrbeziehung
        // aus den vorherigen vier Zeitintervallen je Fahrbeziehung.
        return ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(spitzenstundeZeitintevalle)
                .stream()
                .filter(zeitintervall -> zeitintervall.getType().equals(typeSpitzenstunde))
                .filter(zeitintervall -> zeitintervall.getSortingIndex().equals(sortingIndex))
                .collect(Collectors.toList());
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
        ladeZaehldatumWithDirectionDTO.setVon(spitzenstunde.getFahrbeziehung().getVon());
        ladeZaehldatumWithDirectionDTO.setNach(spitzenstunde.getFahrbeziehung().getNach());
        return ladeZaehldatumWithDirectionDTO;
    }

    /**
     * Ermittelt den Sortierindex der Spitzenstunde abhängig vom {@link Zeitblock} und
     * von {@link TypeZeitintervall} betreffend die Spitzenstunde.
     * Der in dieser Methode erstellte Sortierindex identifiziert somit die relevante
     * Spitzenstunde für den gewählten Zeitblock.
     *
     * @param zeitblock als {@link Zeitblock}
     * @param typeSpitzenstunde als {@link TypeZeitintervall}
     * @return der Sortierindex welcher auch im {@link Zeitintervall} der Spitzenstunde hinterlegt ist.
     */
    public int getSortingIndex(final Zeitblock zeitblock,
            final TypeZeitintervall typeSpitzenstunde) {
        // Erforderlich um mit Util-Methoden den SortingIndex zu ermitteln
        final Zeitintervall dummyZeitintervallForIndexCreation = new Zeitintervall();
        dummyZeitintervallForIndexCreation.setStartUhrzeit(zeitblock.getStart().plusMinutes(15));
        dummyZeitintervallForIndexCreation.setEndeUhrzeit(zeitblock.getStart().plusMinutes(30));
        final Integer sortingIndex;
        if (typeSpitzenstunde.equals(TypeZeitintervall.SPITZENSTUNDE_KFZ)) {
            sortingIndex = ZeitintervallGleitendeSpitzenstundeUtil.getSortingIndexKfz(dummyZeitintervallForIndexCreation, zeitblock);
        } else if (typeSpitzenstunde.equals(TypeZeitintervall.SPITZENSTUNDE_RAD)) {
            sortingIndex = ZeitintervallGleitendeSpitzenstundeUtil.getSortingIndexRad(dummyZeitintervallForIndexCreation, zeitblock);
        } else if (typeSpitzenstunde.equals(TypeZeitintervall.SPITZENSTUNDE_FUSS)) {
            sortingIndex = ZeitintervallGleitendeSpitzenstundeUtil.getSortingIndexFuss(dummyZeitintervallForIndexCreation, zeitblock);
        } else {
            sortingIndex = -1;
        }
        return sortingIndex;
    }

    /**
     * @param zeitauswahl welche die Ausprägung
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ},
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_RAD} oder
     *            {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS} haben darf.
     * @return den {@link TypeZeitintervall} welcher der Zeitauswahl entspricht.
     * @throws IncorrectZeitauswahlException sobald die Zeitauswahl nicht vom Typ
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_KFZ},
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_RAD} oder
     *             {@link LadeZaehldatenService#ZEITAUSWAHL_SPITZENSTUNDE_FUSS} ist.
     */
    public TypeZeitintervall getRelevantTypeZeitintervallFromZeitauswahl(final String zeitauswahl) throws IncorrectZeitauswahlException {
        final TypeZeitintervall typeSpitzenstunde;
        if (StringUtils.equals(zeitauswahl, LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_KFZ)) {
            typeSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_KFZ;
        } else if (StringUtils.equals(zeitauswahl, LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_RAD)) {
            typeSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_RAD;
        } else if (StringUtils.equals(zeitauswahl, LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_FUSS)) {
            typeSpitzenstunde = TypeZeitintervall.SPITZENSTUNDE_FUSS;
        } else {
            throw new IncorrectZeitauswahlException();
        }
        return typeSpitzenstunde;
    }

}
