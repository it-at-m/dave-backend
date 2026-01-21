package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.external.ExternalFahrbeziehungDTO;
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

                // Fahrbeziehungen werden nach Zeitintervallen durchsucht
                if (CollectionUtils.isNotEmpty(zaehlungDto.getFahrbeziehungen())) {
                    // Zeitintervalle persistieren
                    final var zeitintervalleToPersist = new ArrayList<Zeitintervall>();

                    final var fahrbeziehungsIdsForZeitintervalleToDelete = zaehlungDto
                            .getFahrbeziehungen()
                            .stream()
                            .peek(fahrbeziehungDto -> {
                                if (CollectionUtils.isNotEmpty(fahrbeziehungDto.getZeitintervalle())) {
                                    fahrbeziehungDto.getZeitintervalle()
                                            .stream()
                                            .map(this.zeitintervallMapper::zeitintervallDtoToZeitintervall)
                                            .map(zeitintervall -> this.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, fahrbeziehungDto))
                                            .forEach(zeitintervalleToPersist::add);
                                }
                            })
                            .map(ExternalFahrbeziehungDTO::getId)
                            .toList();

                    // Zeitintervalle zur Verkehrsbeziehung löschen, bevor neue gespeichert werden sollen
                    this.zeitintervallPersistierungsService.deleteZeitintervalleByFahrbeziehungId(fahrbeziehungsIdsForZeitintervalleToDelete);

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
     * - Die {@link de.muenchen.dave.domain.Fahrbeziehung}
     *
     * @param zeitintervall in welchem die zusätzlichen Informationen gesetzt werden sollen.
     * @param zaehlung zum Setzen der zusätzlichen Daten.
     * @param fahrbeziehungDto zum Setzen der zusätzlichen Daten.
     * @return den {@link Zeitintervall} in welchem die zusätzlichen Informationen gesetzt sind.
     */
    public Zeitintervall setAdditionalDataToZeitintervall(final Zeitintervall zeitintervall,
            final Zaehlung zaehlung,
            final ExternalFahrbeziehungDTO fahrbeziehungDto) {
        zeitintervall.setZaehlungId(UUID.fromString(zaehlung.getId()));
        zeitintervall.setFahrbeziehungId(UUID.fromString(fahrbeziehungDto.getId()));
        zeitintervall.setFahrbeziehung(this.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto));

        zeitintervall.setHochrechnung(
                this.createHochrechnung(
                        zeitintervall,
                        fahrbeziehungDto.getHochrechnungsfaktor(),
                        zaehlung.getZaehldauer()));
        return zeitintervall;
    }

    /**
     * Diese Methode erstellt die {@link de.muenchen.dave.domain.Fahrbeziehung} zum Anfügen an einen
     * {@link Zeitintervall}.
     *
     * @param fahrbeziehungDto aus dem die {@link de.muenchen.dave.domain.Fahrbeziehung} zum Anfügen an
     *            einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link de.muenchen.dave.domain.Fahrbeziehung} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public de.muenchen.dave.domain.Fahrbeziehung mapToFahrbeziehungForZeitintervall(final ExternalFahrbeziehungDTO fahrbeziehungDto) {
        final de.muenchen.dave.domain.Fahrbeziehung fahrbeziehung = new de.muenchen.dave.domain.Fahrbeziehung();
        if (BooleanUtils.isTrue(fahrbeziehungDto.getIsKreuzung())) {
            fahrbeziehung.setVon(fahrbeziehungDto.getVon());
            fahrbeziehung.setNach(fahrbeziehungDto.getNach());
        } else {
            fahrbeziehung.setVon(fahrbeziehungDto.getKnotenarm());
            final Optional<FahrbewegungKreisverkehr> fahrbewegungKreisverkehrOptional = FahrbewegungKreisverkehr.createEnumFrom(fahrbeziehungDto);
            if (fahrbewegungKreisverkehrOptional.isPresent()) {
                fahrbeziehung.setFahrbewegungKreisverkehr(fahrbewegungKreisverkehrOptional.get());
            } else {
                log.error("Attribute für Kreisverkehr sind nicht korrekt gesetzt: {}", fahrbeziehungDto);
            }
        }
        return fahrbeziehung;
    }
}
