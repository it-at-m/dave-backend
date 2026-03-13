package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.external.ExternalBewegungsbeziehungDTO;
import de.muenchen.dave.domain.dtos.external.ExternalLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlungDTO;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.mapper.KnotenarmMapper;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalZaehlungPersistierungsService extends ZaehlungPersistierungsService {

    private final KnotenarmMapper knotenarmMapper;

    public ExternalZaehlungPersistierungsService(
            final ZaehlstelleIndexService indexService,
            final ZeitintervallPersistierungsService zeitintervallPersistierungsService,
            final ZeitintervallMapper zeitintervallMapper,
            final KnotenarmMapper knotenarmMapper) {
        super(indexService, zeitintervallPersistierungsService, zeitintervallMapper);
        this.knotenarmMapper = knotenarmMapper;
    }

    /**
     * Methode zum Aktualisieren der Metadaten einer Zählung
     *
     * @param zaehlungDto enthält die Id und die neuen Metadaten
     * @return Id der aktualiserten Zaehlung
     * @throws BrokenInfrastructureException Beim Erneuern der Zaehlstelle im Index
     * @throws DataNotFoundException beim Laden der Zaehlstelle im Index
     */
    public BackendIdDTO saveZaehlung(final ExternalZaehlungDTO zaehlungDto) throws DataNotFoundException, BrokenInfrastructureException {
        log.debug("saveZaehlung");
        final Zaehlstelle zaehlstelleByZaehlungId = this.indexService.getZaehlstelleByZaehlungId(zaehlungDto.getId());
        for (final Zaehlung zaehlung : zaehlstelleByZaehlungId.getZaehlungen()) {
            if (zaehlung.getId().equalsIgnoreCase(zaehlungDto.getId())) {
                // Datum, Zählart, Wetter, Zaehlsituation und ZaehlsituationErweitert koennen vom Dienstleister berarbeitet werden.
                zaehlung.setDatum(zaehlungDto.getDatum());
                zaehlung.setZaehlart(zaehlungDto.getZaehlart());
                zaehlung.setWetter(zaehlungDto.getWetter());
                zaehlung.setZaehlsituation(zaehlungDto.getZaehlsituation());
                zaehlung.setZaehlsituationErweitert(zaehlungDto.getZaehlsituationErweitert());

                // Aktualisieren der Knotenarme mit den Filenames
                zaehlung.setKnotenarme(this.knotenarmMapper.externalDtoList2beanList(zaehlungDto.getKnotenarme()));
                final var bewegungsbeziehungen = getAllBewegungsbeziehungenFromZaehlung(zaehlungDto);

                // Verkehrsbeziehungen werden nach Zeitintervallen durchsucht
                if (CollectionUtils.isNotEmpty(bewegungsbeziehungen)) {
                    // Zeitintervalle persistieren
                    final var zeitintervalleToPersist = new ArrayList<Zeitintervall>();

                    final var bewegungsbeziehungIdsForZeitintervalleToDelete = bewegungsbeziehungen
                            .stream()
                            .peek(bewegungsbeziehung -> {
                                CollectionUtils.emptyIfNull(bewegungsbeziehung.getZeitintervalle())
                                        .stream()
                                        .map(this.zeitintervallMapper::zeitintervallDtoToZeitintervall)
                                        .map(zeitintervall -> this.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, bewegungsbeziehung))
                                        .forEach(zeitintervalleToPersist::add);
                            })
                            .map(ExternalBewegungsbeziehungDTO::getId)
                            .toList();

                    // Zeitintervalle zur Verkehrsbeziehung löschen, bevor neue gespeichert werden sollen
                    this.zeitintervallPersistierungsService
                            .deleteZeitintervalleByIdOfBewegungsbeziehung(bewegungsbeziehungIdsForZeitintervalleToDelete);

                    // Zeitintervall nur speichern, ohne was zu berechnen
                    if (CollectionUtils.isNotEmpty(zeitintervalleToPersist)) {
                        this.zeitintervallPersistierungsService.persistZeitintervalle(zeitintervalleToPersist);
                        // Fahrzeugkategorien und -klassen setzen
                        zaehlung.setKategorien(this.getFahrzeugKategorienAndFahrzeugklassen(zeitintervalleToPersist));
                    }
                }
                // For-Schleife beenden, da Zaehlung bereits gefunden und bearbeitet wurde
                break;
            }
        }
        this.indexService.erneuereZaehlstelle(zaehlstelleByZaehlungId);

        // Rückgabe der ZaehlungsId
        final var backendIdDto = new BackendIdDTO();
        backendIdDto.setId(zaehlungDto.getId());
        return backendIdDto;
    }

    public List<ExternalZaehlungDTO> getZaehlungenForExternal(final String dienstleisterKennung, final boolean isFachadmin)
            throws BrokenInfrastructureException {
        return this.indexService.getZaehlungenForExternal(dienstleisterKennung, isFachadmin);
    }

    /**
     * Diese Methode setzt zusätzliche Informationen in den {@link Zeitintervall}.
     * <p>
     * - Die UUID der {@link Zaehlung}
     * - Die UUID der {@link Verkehrsbeziehung}
     * - Die {@link Hochrechnung}
     * - Die {@link de.muenchen.dave.domain.Verkehrsbeziehung}
     *
     * @param zeitintervall in welchem die zusätzlichen Informationen gesetzt werden sollen.
     * @param zaehlung zum Setzen der zusätzlichen Daten.
     * @param externalBewegungsbeziehung zum Setzen der zusätzlichen Daten.
     * @return den {@link Zeitintervall} in welchem die zusätzlichen Informationen gesetzt sind.
     */
    public Zeitintervall setAdditionalDataToZeitintervall(
            final Zeitintervall zeitintervall,
            final Zaehlung zaehlung,
            final ExternalBewegungsbeziehungDTO externalBewegungsbeziehung) {
        zeitintervall.setZaehlungId(UUID.fromString(zaehlung.getId()));
        zeitintervall.setBewegungsbeziehungId(UUID.fromString(externalBewegungsbeziehung.getId()));

        final var hochrechnung = this.createHochrechnung(
                zeitintervall,
                externalBewegungsbeziehung.getHochrechnungsfaktor(),
                zaehlung.getZaehldauer());
        zeitintervall.setHochrechnung(hochrechnung);

        // Setzen der Bewegungsbeziehung im Zeitintervall
        final var zaehlart = Zaehlart.valueOf(zaehlung.getZaehlart());
        if (Zaehlart.FJS.equals(zaehlart)) {
            final var externalLaengsverkehrToMap = (ExternalLaengsverkehrDTO) externalBewegungsbeziehung;
            final var laengsverkehrForZeitintervall = this.createLaengsverkehrForZeitintervall(externalLaengsverkehrToMap);
            zeitintervall.setLaengsverkehr(laengsverkehrForZeitintervall);
        } else if (Zaehlart.QU.equals(zaehlart)) {
            final var externalQuerungsverkehrToMap = (ExternalQuerungsverkehrDTO) externalBewegungsbeziehung;
            final var querungsverkehrForZeitintervall = this.createQuerungsverkehrForZeitintervall(externalQuerungsverkehrToMap);
            zeitintervall.setQuerungsverkehr(querungsverkehrForZeitintervall);
        } else {
            // alle anderen Zählarten
            final var externalVerkehrsbeziehungToMap = (ExternalVerkehrsbeziehungDTO) externalBewegungsbeziehung;
            final var verkehrsbeziehungForZeitintervall = this.createVerkehrsbeziehungForZeitintervall(zaehlart, externalVerkehrsbeziehungToMap);
            zeitintervall.setVerkehrsbeziehung(verkehrsbeziehungForZeitintervall);
        }

        return zeitintervall;
    }

    /**
     * Diese Methode erstellt die {@link de.muenchen.dave.domain.Verkehrsbeziehung} zum Anfügen an einen
     * {@link Zeitintervall}.
     *
     * @param zaehlart zur Unterscheidung ob {@link Zaehlart#QU} oder eine andere Zählart für
     *            Verkehrsbeziehungen.
     * @param externalVerkehrsbeziehung aus dem die {@link de.muenchen.dave.domain.Verkehrsbeziehung}
     *            zum Anfügen an einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link de.muenchen.dave.domain.Verkehrsbeziehung} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public de.muenchen.dave.domain.Verkehrsbeziehung createVerkehrsbeziehungForZeitintervall(
            final Zaehlart zaehlart,
            final ExternalVerkehrsbeziehungDTO externalVerkehrsbeziehung) {
        final var verkehrsbeziehung = new de.muenchen.dave.domain.Verkehrsbeziehung();
        if (BooleanUtils.isTrue(externalVerkehrsbeziehung.getIsKreuzung()) || Zaehlart.QJS.equals(zaehlart)) {
            verkehrsbeziehung.setVon(externalVerkehrsbeziehung.getVon());
            verkehrsbeziehung.setNach(externalVerkehrsbeziehung.getNach());
            verkehrsbeziehung.setStrassenseite(externalVerkehrsbeziehung.getStrassenseite());
        } else {
            verkehrsbeziehung.setVon(externalVerkehrsbeziehung.getKnotenarm());
            final Optional<FahrbewegungKreisverkehr> fahrbewegungKreisverkehrOptional = FahrbewegungKreisverkehr.createEnumFrom(externalVerkehrsbeziehung);
            if (fahrbewegungKreisverkehrOptional.isPresent()) {
                verkehrsbeziehung.setFahrbewegungKreisverkehr(fahrbewegungKreisverkehrOptional.get());
            } else {
                log.error("Attribute für Kreisverkehr sind nicht korrekt gesetzt: {}", externalVerkehrsbeziehung);
            }
        }
        return verkehrsbeziehung;
    }

    /**
     * Diese Methode erstellt die {@link de.muenchen.dave.domain.Querungsverkehr} zum Anfügen
     * an einen {@link Zeitintervall}.
     *
     * @param externalQuerungsverkehr aus dem die {@link de.muenchen.dave.domain.Querungsverkehr}
     *            zum Anfügen an einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link de.muenchen.dave.domain.Querungsverkehr} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public de.muenchen.dave.domain.Querungsverkehr createQuerungsverkehrForZeitintervall(final ExternalQuerungsverkehrDTO externalQuerungsverkehr) {
        final var querungsverkehr = new de.muenchen.dave.domain.Querungsverkehr();
        querungsverkehr.setRichtung(externalQuerungsverkehr.getRichtung());
        querungsverkehr.setKnotenarm(externalQuerungsverkehr.getKnotenarm());
        return querungsverkehr;
    }

    /**
     * Diese Methode erstellt die {@link de.muenchen.dave.domain.Laengsverkehr} zum Anfügen
     * an einen {@link Zeitintervall}.
     *
     * @param externalLaengsverkehr aus dem die {@link de.muenchen.dave.domain.Laengsverkehr}
     *            zum Anfügen an einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link de.muenchen.dave.domain.Laengsverkehr} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public de.muenchen.dave.domain.Laengsverkehr createLaengsverkehrForZeitintervall(final ExternalLaengsverkehrDTO externalLaengsverkehr) {
        final var laengsverkehr = new de.muenchen.dave.domain.Laengsverkehr();
        laengsverkehr.setRichtung(externalLaengsverkehr.getRichtung());
        laengsverkehr.setStrassenseite(externalLaengsverkehr.getStrassenseite());
        laengsverkehr.setKnotenarm(externalLaengsverkehr.getKnotenarm());
        return laengsverkehr;
    }
}
