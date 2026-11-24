package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.DienstleisterDTO;
import de.muenchen.dave.domain.dtos.OpenZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteBewegungsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.mapper.PkwEinheitMapper;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.relational.repository.PkwEinheitRepository;
import de.muenchen.dave.relational.zaehlstellen.LaengsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.MappedBewegungsbeziehung;
import de.muenchen.dave.relational.zaehlstellen.QuerungsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.VerkehrsbeziehungEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.services.SanitizationService;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.security.AuthorizationService;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InternalZaehlungPersistierungsService extends ZaehlungPersistierungsService {

    private final PkwEinheitMapper pkwEinheitMapper;

    private final PkwEinheitRepository pkwEinheitRepository;

    public InternalZaehlungPersistierungsService(
            final ZaehlstelleIndexService indexService,
            final ZeitintervallPersistierungsService zeitintervallPersistierungsService,
            final PkwEinheitRepository pkwEinheitRepository,
            final ZeitintervallMapper zeitintervallMapper,
            final PkwEinheitMapper pkwEinheitMapper,
            final SanitizationService sanitizationService,
            final AuthorizationService authorizationService) {
        super(indexService, zeitintervallPersistierungsService, zeitintervallMapper, sanitizationService, authorizationService);
        this.pkwEinheitMapper = pkwEinheitMapper;
        this.pkwEinheitRepository = pkwEinheitRepository;
    }

    /**
     * Löscht eine Zählung anhand der ID;
     *
     * @param zaehlungId zu löschende Zählung
     * @return gelöscht/nicht gelÖscht
     * @throws DataNotFoundException beim Loeschen der ZaehlungEntity aus dem Index
     * @throws BrokenInfrastructureException beim Loeschen der ZaehlungEntity aus dem Index
     */
    @Transactional
    public boolean deleteZaehlung(final UUID zaehlungId) throws DataNotFoundException, BrokenInfrastructureException {
        // Zeitintervalle zur Zaehlung löschen
        boolean isDeleted = this.zeitintervallPersistierungsService.deleteZaehlung(zaehlungId);

        // ZaehlungEntity im Index löschen
        if (isDeleted) {
            isDeleted = this.indexService.deleteZaehlung(zaehlungId);
        }
        return isDeleted;
    }

    /**
     * In der Methode wird die ZaehlungEntity im Elasticsearch-Server gespeichert.
     *
     * @param zaehlungDto die ZaehlungEntity zum speichern.
     * @param zaehlstelleId die Id der dazugehörigen Zählstelle
     * @return die Id der gespeicherten ZaehlungEntity.
     * @throws BrokenInfrastructureException Beim Schreiben in den Index
     * @throws DataNotFoundException Beim Schreiben in den Index
     */
    @Transactional
    public BackendIdDTO saveZaehlung(final BearbeiteZaehlungDTO zaehlungDto, final UUID zaehlstelleId)
            throws BrokenInfrastructureException, DataNotFoundException {

        // Sanitize attributes of zaehlungDto
        this.sanitizationService.sanitizeBearbeiteZaehlungDto(zaehlungDto);

        // Setzen der PKW-Einheiten
        if (ObjectUtils.isEmpty(zaehlungDto.getPkwEinheit())) {
            this.setYoungestPkwEinheitFromRelationalDatabase(zaehlungDto);
        }

        // Fahrzeugkategorien und -klassen setzen
        zaehlungDto.setKategorien(getFahrzeugklassen(new HashSet<>(zaehlungDto.getKategorien())));

        final ZaehlungEntity zaehlung;
        if (zaehlungDto.getId()== null) {
            // createZaehlung
            zaehlung = this.indexService.erstelleZaehlung(
                    zaehlungDto,
                    zaehlstelleId);
        } else {
            // updateZaehlung
            zaehlung = this.indexService.erneuereZaehlung(
                    zaehlungDto,
                    zaehlstelleId);
        }

        // Rückgabe der ZaehlungsId
        final var backendIdDto = new BackendIdDTO();
        backendIdDto.setId(zaehlung.getId());
        return backendIdDto;
    }

    /**
     * Diese Methode setzt zusätzliche Informationen in den {@link Zeitintervall}.
     * <p>
     * - Die UUID der {@link ZaehlungEntity}
     * - Die UUID der {@link VerkehrsbeziehungEntity}
     * - Die {@link Hochrechnung}
     * - Die {@link de.muenchen.dave.domain.Verkehrsbeziehung}, den
     * {@link LaengsverkehrEntity}
     * oder den {@link QuerungsverkehrEntity}
     *
     * @param zeitintervall in welchem die zusätzlichen Informationen gesetzt werden sollen.
     * @param zaehlung zum Setzen der zusätzlichen Daten.
     * @param bearbeiteBewegungsbeziehung zum Setzen der zusätzlichen Daten.
     * @return den {@link Zeitintervall} in welchem die zusätzlichen Informationen gesetzt sind.
     */
    public Zeitintervall setAdditionalDataToZeitintervall(
            final Zeitintervall zeitintervall,
            final ZaehlungEntity zaehlung,
            final BearbeiteBewegungsbeziehungDTO bearbeiteBewegungsbeziehung) {
        zeitintervall.setZaehlungId(zaehlung.getId());

        // Setzen der Bewegungsbeziehungs-Id in Zeitintervall
        this.getBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto(zaehlung, bearbeiteBewegungsbeziehung)
                .ifPresent(bewegungsbeziehung -> zeitintervall.setBewegungsbeziehungId(bewegungsbeziehung.getId()));

        // Setzen der Hochrechnug in Zeitintervall
        final Hochrechnung hochrechnung = this.createHochrechnung(
                zeitintervall,
                bearbeiteBewegungsbeziehung.getHochrechnungsfaktor(),
                String.valueOf(zaehlung.getZaehldauer()));
        zeitintervall.setHochrechnung(hochrechnung);

        // Setzen der Bewegungsbeziehung im Zeitintervall
        final var zaehlart = zaehlung.getZaehlart();
        if (Zaehlart.FJS.equals(zaehlart)) {
            final var bearbeiteLaengsverkehrToMap = (BearbeiteLaengsverkehrDTO) bearbeiteBewegungsbeziehung;
            final Laengsverkehr laengsverkehrForZeitintervall = this.createLaengsverkehrForZeitintervall(bearbeiteLaengsverkehrToMap);
            zeitintervall.setLaengsverkehr(laengsverkehrForZeitintervall);
        } else if (Zaehlart.QU.equals(zaehlart)) {
            final var bearbeiteQuerungsverkehrToMap = (BearbeiteQuerungsverkehrDTO) bearbeiteBewegungsbeziehung;
            final Querungsverkehr querungsverkehrForZeitintervall = this
                    .createQuerungsverkehrForZeitintervall(bearbeiteQuerungsverkehrToMap);
            zeitintervall.setQuerungsverkehr(querungsverkehrForZeitintervall);
        } else {
            // alle anderen Zählarten
            final var bearbeiteVerkehrsbeziehungToMap = (BearbeiteVerkehrsbeziehungDTO) bearbeiteBewegungsbeziehung;
            final Verkehrsbeziehung verkehrsbeziehungForZeitintervall = this.createVerkehrsbeziehungForZeitintervall(
                    zaehlart,
                    bearbeiteVerkehrsbeziehungToMap);
            zeitintervall.setVerkehrsbeziehung(verkehrsbeziehungForZeitintervall);
        }

        return zeitintervall;
    }

    /**
     * Diese Methode gibt die {@link MappedBewegungsbeziehung} der {@link ZaehlungEntity} zurück, welche durch die
     * {@link BearbeiteVerkehrsbeziehungDTO} repräsentiert wird.
     *
     * @param zaehlung aus der die {@link MappedBewegungsbeziehung} geholt und zurückgegeben werden soll.
     * @param bearbeiteBewegungsbeziehung welche die Basis zum Suchen der {@link MappedBewegungsbeziehung}
     *            darstellt.
     * @return die gefundene {@link MappedBewegungsbeziehung}.
     */
    public Optional<? extends MappedBewegungsbeziehung> getBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto(
            final ZaehlungEntity zaehlung,
            final BearbeiteBewegungsbeziehungDTO bearbeiteBewegungsbeziehung) {

        final var zaehlart = zaehlung.getZaehlart();
        return getAllBewegungsbeziehungenFromZaehlung(zaehlung)
                .stream()
                .filter(bewegungsbeziehung -> this.isSameBewegungsbeziehung(zaehlart, bearbeiteBewegungsbeziehung, bewegungsbeziehung))
                .findFirst();
    }

    /**
     * Diese Methode prüft ob die beiden Bewegungsbeziehungsobjekte in den Parametern
     * die selbe Bewegungsbeziehung einer Kreuzung oder eines Kreisverkehrs repräsentieren.
     *
     * @param bearbeiteBewegungsbeziehung zur Prüfung auf repräsentation der selben Bewegungsbeziehung.
     * @param bewegungsbeziehung zur Prüfung auf repräsentation der selben Bewegungsbeziehung.
     * @return true falls die selbe Bewegungsbeziehung einer Kreuzung oder eines Kreisverkehrs
     *         repräsentiert wird.
     */
    public boolean isSameBewegungsbeziehung(
            final Zaehlart zaehlart,
            final BearbeiteBewegungsbeziehungDTO bearbeiteBewegungsbeziehung,
            final MappedBewegungsbeziehung bewegungsbeziehung) {
        if (Zaehlart.QU.equals(zaehlart)) {
            final var bearbeiteQuerungsverkehr = (BearbeiteQuerungsverkehrDTO) bearbeiteBewegungsbeziehung;
            final var verkehrsbeziehung = (QuerungsverkehrEntity) bewegungsbeziehung;
            return Objects.equals(bearbeiteQuerungsverkehr.getRichtung(), verkehrsbeziehung.getRichtung())
                    && Objects.equals(bearbeiteQuerungsverkehr.getKnotenarm(), verkehrsbeziehung.getKnotenarm());
        } else if (Zaehlart.FJS.equals(zaehlart)) {
            final var bearbeiteLaengsverkehr = (BearbeiteLaengsverkehrDTO) bearbeiteBewegungsbeziehung;
            final var laengsverkehr = (LaengsverkehrEntity) bewegungsbeziehung;
            return Objects.equals(bearbeiteLaengsverkehr.getRichtung(), laengsverkehr.getRichtung())
                    && Objects.equals(bearbeiteLaengsverkehr.getStrassenseite(), laengsverkehr.getStrassenseite())
                    && Objects.equals(bearbeiteLaengsverkehr.getKnotenarm(), laengsverkehr.getKnotenarm());
        } else {
            // alle anderen Zählarten
            final var bearbeiteVerkehrsbeziehung = (BearbeiteVerkehrsbeziehungDTO) bearbeiteBewegungsbeziehung;
            final var verkehrsbeziehung = (VerkehrsbeziehungEntity) bewegungsbeziehung;
            return Objects.equals(bearbeiteVerkehrsbeziehung.getIsKreuzung(), verkehrsbeziehung.getIsKreuzung())
                    // Kreuzung
                    && Objects.equals(bearbeiteVerkehrsbeziehung.getVon(), verkehrsbeziehung.getVon())
                    && Objects.equals(bearbeiteVerkehrsbeziehung.getNach(), verkehrsbeziehung.getNach())
            // Kreisverkehr
                    && Objects.equals(bearbeiteVerkehrsbeziehung.getKnotenarm(), verkehrsbeziehung.getKnotenarm())
                    && Objects.equals(bearbeiteVerkehrsbeziehung.getHinein(), verkehrsbeziehung.getHinein())
                    && Objects.equals(bearbeiteVerkehrsbeziehung.getHeraus(), verkehrsbeziehung.getHeraus())
                    && Objects.equals(bearbeiteVerkehrsbeziehung.getVorbei(), verkehrsbeziehung.getVorbei())
            // Strassenseite für Zählart QJS
                    && Objects.equals(bearbeiteVerkehrsbeziehung.getStrassenseite(), verkehrsbeziehung.getStrassenseite());
        }
    }

    /**
     * Diese Methode setzt die zuletzt persistierte PKW-Einheit aus der relationalen Datenbank in der im
     * Parameter übergebenen {@link BearbeiteZaehlungDTO}.
     *
     * @param zaehlungDto zum setzen der PKW-Einheiten.
     * @return {@link BearbeiteZaehlungDTO} des Parameters mit der gesetzten PKW-Einheit.
     */
    public BearbeiteZaehlungDTO setYoungestPkwEinheitFromRelationalDatabase(final BearbeiteZaehlungDTO zaehlungDto) {
        this.getYoungestPkwEinheitFromRelationalDatabase().ifPresent(
                pkwEinheit -> zaehlungDto.setPkwEinheit(this.pkwEinheitMapper.entity2bearbeiteDto(pkwEinheit)));
        return zaehlungDto;
    }

    /**
     * Diese Methode erstellt die {@link Verkehrsbeziehung} zum Anfügen an einen
     * {@link Zeitintervall}.
     *
     * @param zaehlart zur Unterscheidung ob {@link Zaehlart#QU} oder eine andere Zählart für
     *            Verkehrsbeziehungen.
     * @param bearbeiteVerkehrsbeziehung aus dem die {@link Verkehrsbeziehung}
     *            zum Anfügen an einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link Verkehrsbeziehung} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public Verkehrsbeziehung createVerkehrsbeziehungForZeitintervall(
            final Zaehlart zaehlart,
            final BearbeiteVerkehrsbeziehungDTO bearbeiteVerkehrsbeziehung) {
        final var verkehrsbeziehung = new Verkehrsbeziehung();
        if (BooleanUtils.isTrue(bearbeiteVerkehrsbeziehung.getIsKreuzung()) || Zaehlart.QU.equals(zaehlart)) {
            verkehrsbeziehung.setVon(bearbeiteVerkehrsbeziehung.getVon());
            verkehrsbeziehung.setNach(bearbeiteVerkehrsbeziehung.getNach());
            verkehrsbeziehung.setStrassenseite(bearbeiteVerkehrsbeziehung.getStrassenseite());
        } else {
            verkehrsbeziehung.setVon(bearbeiteVerkehrsbeziehung.getKnotenarm());
            final Optional<FahrbewegungKreisverkehr> fahrbewegungKreisverkehrOptional = FahrbewegungKreisverkehr.createEnumFrom(bearbeiteVerkehrsbeziehung);
            if (fahrbewegungKreisverkehrOptional.isPresent()) {
                verkehrsbeziehung.setFahrbewegungKreisverkehr(fahrbewegungKreisverkehrOptional.get());
            } else {
                log.error("Attribute für Kreisverkehr sind nicht korrekt gesetzt: {}", bearbeiteVerkehrsbeziehung);
            }
        }
        return verkehrsbeziehung;
    }

    /**
     * Diese Methode erstellt die {@link QuerungsverkehrEntity} zum Anfügen
     * an einen {@link Zeitintervall}.
     *
     * @param bearbeiteQuerungsverkehr aus dem die {@link QuerungsverkehrEntity}
     *            zum Anfügen an einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link QuerungsverkehrEntity} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public Querungsverkehr createQuerungsverkehrForZeitintervall(final BearbeiteQuerungsverkehrDTO bearbeiteQuerungsverkehr) {
        final var querungsverkehr = new Querungsverkehr();
        querungsverkehr.setRichtung(bearbeiteQuerungsverkehr.getRichtung());
        querungsverkehr.setKnotenarm(bearbeiteQuerungsverkehr.getKnotenarm());
        return querungsverkehr;
    }

    /**
     * Diese Methode erstellt die {@link LaengsverkehrEntity} zum Anfügen
     * an einen {@link Zeitintervall}.
     *
     * @param bearbeiteLaengsverkehr aus dem die {@link LaengsverkehrEntity}
     *            zum Anfügen an einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link LaengsverkehrEntity} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public Laengsverkehr createLaengsverkehrForZeitintervall(final BearbeiteLaengsverkehrDTO bearbeiteLaengsverkehr) {
        final var laengsverkehr = new Laengsverkehr();
        laengsverkehr.setRichtung(bearbeiteLaengsverkehr.getRichtung());
        laengsverkehr.setStrassenseite(bearbeiteLaengsverkehr.getStrassenseite());
        laengsverkehr.setKnotenarm(bearbeiteLaengsverkehr.getKnotenarm());
        return laengsverkehr;
    }

    /**
     * Diese Methode holt die zuletzt persistierte PKW-Einheit aus der relationalen Datenbank.
     *
     * @return die zuletzt persistierte {@link PkwEinheit}.
     */
    public Optional<PkwEinheit> getYoungestPkwEinheitFromRelationalDatabase() {
        final Optional<PkwEinheit> pkwEinheitOptional = this.pkwEinheitRepository.findTopByOrderByCreatedTimeDesc();
        if (!pkwEinheitOptional.isPresent()) {
            log.error("Keine Pkw-Einheiten in der Relationalen Databank vorhanden.");
        }
        return pkwEinheitOptional;
    }

    /**
     * Methode zum Aktualisieren des Status von beauftragten Zaehlungen
     *
     * @throws BrokenInfrastructureException Beim Schreiben in den Index
     */
    public void updateStatusOfInstrucedZaehlungen() throws BrokenInfrastructureException {
        this.indexService.updateStatusOfInstrucedZaehlungen();
    }

    public List<OpenZaehlungDTO> loadAllOpenZaehlungen() throws BrokenInfrastructureException {
        return this.indexService.getOpenZaehlungen();
    }

    public BackendIdDTO updateDienstleisterkennung(final UUID zaehlungId, final DienstleisterDTO dienstleisterDTO)
            throws DataNotFoundException, BrokenInfrastructureException {
        final BackendIdDTO backendIdDto = new BackendIdDTO();

        final ZaehlungEntity zaehlungToUpdate = this.indexService.getZaehlung(zaehlungId);
        zaehlungToUpdate.setDienstleisterkennung(dienstleisterDTO.getKennung());
        final ZaehlstelleEntity zaehlstelleByZaehlungId = this.indexService.getZaehlstelleByZaehlungId(zaehlungId);

        // updateZaehlung
        this.indexService.erneuereZaehlung(
                zaehlungToUpdate,
                zaehlstelleByZaehlungId.getId());

        // Rückgabe der ZaehlungsId
        backendIdDto.setId(zaehlungId);
        return backendIdDto;
    }

}
