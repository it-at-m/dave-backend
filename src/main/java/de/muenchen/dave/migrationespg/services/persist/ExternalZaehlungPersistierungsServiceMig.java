package de.muenchen.dave.migrationespg.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.migrationespg.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.external.ExternalBewegungsbeziehungDTO;
import de.muenchen.dave.domain.dtos.external.ExternalLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDTO;
import de.muenchen.dave.migrationespg.domain.dtos.external.ExternalZaehlungDTO;
import de.muenchen.dave.domain.enums.Wetter;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.LaengsverkehrEntity;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.QuerungsverkehrEntity;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.migrationespg.domain.mapper.KnotenarmMapperMig;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.migrationespg.services.security.AuthorizationServiceMig;
import de.muenchen.dave.services.SanitizationService;
import de.muenchen.dave.migrationespg.services.ZaehlstelleIndexServiceMig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalZaehlungPersistierungsServiceMig extends ZaehlungPersistierungsServiceMig {

    private final KnotenarmMapperMig knotenarmMapperMig;

    public ExternalZaehlungPersistierungsServiceMig(
            final ZaehlstelleIndexServiceMig indexService,
            final ZeitintervallPersistierungsServiceMig zeitintervallPersistierungsServiceMig,
            final ZeitintervallMapper zeitintervallMapper,
            final KnotenarmMapperMig knotenarmMapperMig,
            final SanitizationService sanitizationService,
            final AuthorizationServiceMig authorizationService) {
        super(indexService, zeitintervallPersistierungsServiceMig, zeitintervallMapper, sanitizationService, authorizationService);
        this.knotenarmMapperMig = knotenarmMapperMig;
    }

    /**
     * Methode zum Aktualisieren der Metadaten einer Zählung
     *
     * @param zaehlungDto enthält die Id und die neuen Metadaten
     * @return Id der aktualiserten ZaehlungEntity
     * @throws BrokenInfrastructureException Beim Erneuern der ZaehlstelleEntity im Index
     * @throws DataNotFoundException beim Laden der ZaehlstelleEntity im Index
     * @throws AccessDeniedException Wenn der Nutzer nicht berechtigt ist, die Zählung zu bearbeiten
     */
    public BackendIdDTO saveZaehlung(final ExternalZaehlungDTO zaehlungDto) throws DataNotFoundException, BrokenInfrastructureException, AccessDeniedException {
        log.debug("saveZaehlung");

        // Prüfen, ob der Dienstleister berechtigt ist, die Zählung zu bearbeiten / zu speichern
        authorizationService.assertCanModifyZaehlung(zaehlungDto.getId());

        final ZaehlstelleEntity zaehlstelleByZaehlungId = this.indexService.getZaehlstelleByZaehlungId(zaehlungDto.getId());
        for (final ZaehlungEntity zaehlung : zaehlstelleByZaehlungId.getZaehlungen()) {
            if (zaehlung.getId().equals(zaehlungDto.getId())) {
                // Datum, Zählart, Wetter, Zaehlsituation und ZaehlsituationErweitert koennen vom Dienstleister berarbeitet werden.
                zaehlung.setDatum(zaehlungDto.getDatum());
                zaehlung.setZaehlart(Zaehlart.valueOf(zaehlungDto.getZaehlart()));
                zaehlung.setWetter(Wetter.valueOf(zaehlungDto.getWetter()));
                zaehlung.setZaehlsituation(sanitizationService.sanitizeAllowedHtml(zaehlungDto.getZaehlsituation()));
                zaehlung.setZaehlsituationErweitert(sanitizationService.sanitizeAllowedHtml(zaehlungDto.getZaehlsituationErweitert()));

                // Aktualisieren der Knotenarme mit den Filenames
                zaehlung.setKnotenarme(this.knotenarmMapperMig.externalDtoList2beanList(zaehlungDto.getKnotenarme()));
                final List<ExternalBewegungsbeziehungDTO> bewegungsbeziehungen = getAllBewegungsbeziehungenFromZaehlung(zaehlungDto);

                // Verkehrsbeziehungen werden nach Zeitintervallen durchsucht
                if (CollectionUtils.isNotEmpty(bewegungsbeziehungen)) {
                    // Zeitintervalle persistieren
                    final var zeitintervalleToPersist = new ArrayList<Zeitintervall>();

                    final var bewegungsbeziehungIdsForZeitintervalleToDelete = bewegungsbeziehungen
                            .stream()
                            .peek(bewegungsbeziehung -> CollectionUtils.emptyIfNull(bewegungsbeziehung.getZeitintervalle())
                                    .stream()
                                    .map(this.zeitintervallMapper::zeitintervallDtoToZeitintervall)
                                    .map(zeitintervall -> this.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, bewegungsbeziehung))
                                    .forEach(zeitintervalleToPersist::add))
                            .map(ExternalBewegungsbeziehungDTO::getId)
                            .toList();

                    // Zeitintervalle zur Verkehrsbeziehung löschen, bevor neue gespeichert werden sollen
                    this.zeitintervallPersistierungsServiceMig
                            .deleteZeitintervalleByIdOfBewegungsbeziehung(bewegungsbeziehungIdsForZeitintervalleToDelete);

                    // Zeitintervall nur speichern, ohne was zu berechnen
                    if (CollectionUtils.isNotEmpty(zeitintervalleToPersist)) {
                        this.zeitintervallPersistierungsServiceMig.persistZeitintervalle(zeitintervalleToPersist);
                        // Fahrzeugkategorien und -klassen setzen
                        zaehlung.setKategorien(this.getFahrzeugKategorienAndFahrzeugklassen(zeitintervalleToPersist));
                    }
                }
                // For-Schleife beenden, da ZaehlungEntity bereits gefunden und bearbeitet wurde
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
     * - Die UUID der {@link ZaehlungEntity}
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
            final ZaehlungEntity zaehlung,
            final ExternalBewegungsbeziehungDTO externalBewegungsbeziehung) {
        zeitintervall.setZaehlungId(zaehlung.getId());
        zeitintervall.setBewegungsbeziehungId(UUID.fromString(externalBewegungsbeziehung.getId()));

        final Hochrechnung hochrechnung = this.createHochrechnung(
                zeitintervall,
                externalBewegungsbeziehung.getHochrechnungsfaktor(),
                String.valueOf(zaehlung.getZaehldauer()));
        zeitintervall.setHochrechnung(hochrechnung);

        // Setzen der Bewegungsbeziehung im Zeitintervall
        final var zaehlart = zaehlung.getZaehlart();
        if (Zaehlart.FJS.equals(zaehlart)) {
            final var externalLaengsverkehrToMap = (ExternalLaengsverkehrDTO) externalBewegungsbeziehung;
            final Laengsverkehr laengsverkehrForZeitintervall = this.createLaengsverkehrForZeitintervall(externalLaengsverkehrToMap);
            zeitintervall.setLaengsverkehr(laengsverkehrForZeitintervall);
        } else if (Zaehlart.QU.equals(zaehlart)) {
            final var externalQuerungsverkehrToMap = (ExternalQuerungsverkehrDTO) externalBewegungsbeziehung;
            final Querungsverkehr querungsverkehrForZeitintervall = this.createQuerungsverkehrForZeitintervall(externalQuerungsverkehrToMap);
            zeitintervall.setQuerungsverkehr(querungsverkehrForZeitintervall);
        } else {
            // alle anderen Zählarten
            final var externalVerkehrsbeziehungToMap = (ExternalVerkehrsbeziehungDTO) externalBewegungsbeziehung;
            final Verkehrsbeziehung verkehrsbeziehungForZeitintervall = this.createVerkehrsbeziehungForZeitintervall(
                    zaehlart,
                    externalVerkehrsbeziehungToMap);
            zeitintervall.setVerkehrsbeziehung(verkehrsbeziehungForZeitintervall);
        }

        return zeitintervall;
    }

    /**
     * Diese Methode erstellt die {@link de.muenchen.dave.domain.Verkehrsbeziehung} zum Anfügen an einen
     * {@link Zeitintervall}.
     *
     * @param zaehlart zur Unterscheidung ob {@link Zaehlart#QJS} oder eine andere Zählart für
     *            Verkehrsbeziehungen.
     * @param externalVerkehrsbeziehung aus dem die {@link de.muenchen.dave.domain.Verkehrsbeziehung}
     *            zum Anfügen an einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link de.muenchen.dave.domain.Verkehrsbeziehung} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public Verkehrsbeziehung createVerkehrsbeziehungForZeitintervall(
            final Zaehlart zaehlart,
            final ExternalVerkehrsbeziehungDTO externalVerkehrsbeziehung) {
        final var verkehrsbeziehung = new Verkehrsbeziehung();
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
     * Diese Methode erstellt die {@link QuerungsverkehrEntity} zum Anfügen
     * an einen {@link Zeitintervall}.
     *
     * @param externalQuerungsverkehr aus dem die {@link QuerungsverkehrEntity}
     *            zum Anfügen an einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link QuerungsverkehrEntity} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public Querungsverkehr createQuerungsverkehrForZeitintervall(final ExternalQuerungsverkehrDTO externalQuerungsverkehr) {
        final var querungsverkehr = new Querungsverkehr();
        querungsverkehr.setRichtung(externalQuerungsverkehr.getRichtung());
        querungsverkehr.setKnotenarm(externalQuerungsverkehr.getKnotenarm());
        return querungsverkehr;
    }

    /**
     * Diese Methode erstellt die {@link LaengsverkehrEntity} zum Anfügen
     * an einen {@link Zeitintervall}.
     *
     * @param externalLaengsverkehr aus dem die {@link LaengsverkehrEntity}
     *            zum Anfügen an einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link LaengsverkehrEntity} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public Laengsverkehr createLaengsverkehrForZeitintervall(final ExternalLaengsverkehrDTO externalLaengsverkehr) {
        final var laengsverkehr = new Laengsverkehr();
        laengsverkehr.setRichtung(externalLaengsverkehr.getRichtung());
        laengsverkehr.setStrassenseite(externalLaengsverkehr.getStrassenseite());
        laengsverkehr.setKnotenarm(externalLaengsverkehr.getKnotenarm());
        return laengsverkehr;
    }
}
