package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlungDTO;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
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

    public ExternalZaehlungPersistierungsService(final ZaehlstelleIndexService indexService,
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
     * @throws BrokenInfrastructureException Beim erneuern der Zaehlstelle im Index
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

                // Verkehrsbeziehungen werden nach Zeitintervallen durchsucht
                if (CollectionUtils.isNotEmpty(zaehlungDto.getVerkehrsbeziehungen())) {
                    // Zeitintervalle persistieren
                    final var zeitintervalleToPersist = new ArrayList<Zeitintervall>();

                    final var bewegungsbeziehungIdsForZeitintervalleToDelete = zaehlungDto
                            .getVerkehrsbeziehungen()
                            .stream()
                            .peek(bewegungsbeziehung -> {
                                if (CollectionUtils.isNotEmpty(bewegungsbeziehung.getZeitintervalle())) {
                                    bewegungsbeziehung.getZeitintervalle()
                                            .stream()
                                            .map(this.zeitintervallMapper::zeitintervallDtoToZeitintervall)
                                            .map(zeitintervall -> this.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, bewegungsbeziehung))
                                            .forEach(zeitintervalleToPersist::add);
                                }
                            })
                            .map(ExternalVerkehrsbeziehungDTO::getId)
                            .toList();

                    // Zeitintervalle zur Verkehrsbeziehung löschen, bevor neue gespeichert werden sollen
                    this.zeitintervallPersistierungsService
                            .deleteZeitintervalleByIdOfVerkehrsbeziehungQuerverkehrOrLaengsverkehr(bewegungsbeziehungIdsForZeitintervalleToDelete);

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
        final BackendIdDTO backendIdDto = new BackendIdDTO();
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
     * @param verkehrsbeziehung zum Setzen der zusätzlichen Daten.
     * @return den {@link Zeitintervall} in welchem die zusätzlichen Informationen gesetzt sind.
     */
    public Zeitintervall setAdditionalDataToZeitintervall(
            final Zeitintervall zeitintervall,
            final Zaehlung zaehlung,
            final ExternalVerkehrsbeziehungDTO verkehrsbeziehung) {
        zeitintervall.setZaehlungId(UUID.fromString(zaehlung.getId()));
        zeitintervall.setBewegungsbeziehungId(UUID.fromString(verkehrsbeziehung.getId()));
        zeitintervall.setVerkehrsbeziehung(this.mapToVerkehrsbeziehungForZeitintervall(verkehrsbeziehung));

        zeitintervall.setHochrechnung(
                this.createHochrechnung(
                        zeitintervall,
                        verkehrsbeziehung.getHochrechnungsfaktor(),
                        zaehlung.getZaehldauer()));
        return zeitintervall;
    }

    /**
     * Diese Methode erstellt die {@link de.muenchen.dave.domain.Verkehrsbeziehung} zum Anfügen an einen
     * {@link Zeitintervall}.
     *
     * @param verkehrsbeziehungDto aus dem die {@link de.muenchen.dave.domain.Verkehrsbeziehung} zum
     *            Anfügen
     *            an
     *            einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link de.muenchen.dave.domain.Verkehrsbeziehung} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public de.muenchen.dave.domain.Verkehrsbeziehung mapToVerkehrsbeziehungForZeitintervall(final ExternalVerkehrsbeziehungDTO verkehrsbeziehungDto) {
        final de.muenchen.dave.domain.Verkehrsbeziehung verkehrsbeziehung = new de.muenchen.dave.domain.Verkehrsbeziehung();
        if (BooleanUtils.isTrue(verkehrsbeziehungDto.getIsKreuzung())) {
            verkehrsbeziehung.setVon(verkehrsbeziehungDto.getVon());
            verkehrsbeziehung.setNach(verkehrsbeziehungDto.getNach());
        } else {
            verkehrsbeziehung.setVon(verkehrsbeziehungDto.getKnotenarm());
            final Optional<FahrbewegungKreisverkehr> fahrbewegungKreisverkehrOptional = FahrbewegungKreisverkehr.createEnumFrom(verkehrsbeziehungDto);
            if (fahrbewegungKreisverkehrOptional.isPresent()) {
                verkehrsbeziehung.setFahrbewegungKreisverkehr(fahrbewegungKreisverkehrOptional.get());
            } else {
                log.error("Attribute für Kreisverkehr sind nicht korrekt gesetzt: {}", verkehrsbeziehungDto);
            }
        }
        return verkehrsbeziehung;
    }
}
