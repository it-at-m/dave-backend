package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.DienstleisterDTO;
import de.muenchen.dave.domain.dtos.OpenZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.mapper.PkwEinheitMapper;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.PkwEinheitRepository;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.util.geo.CoordinateUtil;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InternalZaehlungPersistierungsService extends ZaehlungPersistierungsService {

    private final PkwEinheitMapper pkwEinheitMapper;

    private final PkwEinheitRepository pkwEinheitRepository;

    @Value(value = "${dave.radius.distance-check-meter}")
    private int radiusDistanceCheck;

    public InternalZaehlungPersistierungsService(final ZaehlstelleIndexService indexService,
            final ZeitintervallPersistierungsService zeitintervallPersistierungsService,
            final PkwEinheitRepository pkwEinheitRepository,
            final ZeitintervallMapper zeitintervallMapper,
            final PkwEinheitMapper pkwEinheitMapper) {
        super(indexService, zeitintervallPersistierungsService, zeitintervallMapper);
        this.pkwEinheitMapper = pkwEinheitMapper;
        this.pkwEinheitRepository = pkwEinheitRepository;
    }

    /**
     * Löscht eine Zählung anhand der ID;
     *
     * @param zaehlungId zu löschende Zählung
     * @return gelöscht/nicht gelÖscht
     * @throws DataNotFoundException beim Loeschen der Zaehlung aus dem Index
     * @throws BrokenInfrastructureException beim Loeschen der Zaehlung aus dem Index
     */
    @Transactional
    public boolean deleteZaehlung(final String zaehlungId) throws DataNotFoundException, BrokenInfrastructureException {
        // Zeitintervalle zur Zaehlung löschen
        boolean isDeleted = this.zeitintervallPersistierungsService.deleteZaehlung(zaehlungId);

        // Zaehlung im Index löschen
        if (isDeleted) {
            isDeleted = this.indexService.deleteZaehlung(zaehlungId);
        }
        return isDeleted;
    }

    /**
     * In der Methode wird die Zaehlung im Elasticsearch-Server gespeichert.
     *
     * @param zaehlungDto die Zaehlung zum speichern.
     * @param zaehlstelleId die Id der dazugehörigen Zählstelle
     * @return die Id der gespeicherten Zaehlung.
     * @throws BrokenInfrastructureException Beim Schreiben in den Index
     * @throws DataNotFoundException Beim Schreiben in den Index
     */
    @Transactional
    public BackendIdDTO saveZaehlung(final BearbeiteZaehlungDTO zaehlungDto, final String zaehlstelleId)
            throws BrokenInfrastructureException, DataNotFoundException {
        final BackendIdDTO backendIdDto = new BackendIdDTO();

        // Setzen der PKW-Einheiten
        if (ObjectUtils.isEmpty(zaehlungDto.getPkwEinheit())) {
            this.setYoungestPkwEinheitFromRelationalDatabase(zaehlungDto);
        }

        final Zaehlung zaehlung;
        if (StringUtils.isEmpty(zaehlungDto.getId())) {
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
        backendIdDto.setId(zaehlung.getId());
        return backendIdDto;
    }

    /**
     * In der Methode wird die Zaehlung im Elasticsearch-Server und es werden die Zeitintervalle in der
     * relationalen Datenbank gespeichert.
     *
     * @param zaehlungDto die Zaehlung zum speichern.
     * @param zaehlstelleId die Id der dazugehörigen Zählstelle
     * @return die Id der gespeicherten Zaehlung.
     * @throws BrokenInfrastructureException Beim Schreiben in den Index
     * @throws DataNotFoundException Beim Schreiben/Laden in/aus den/dem Index
     */
    @Transactional
    public BackendIdDTO saveZaehlungWithZeitintervalle(final BearbeiteZaehlungDTO zaehlungDto, final String zaehlstelleId)
            throws BrokenInfrastructureException, DataNotFoundException {
        final BackendIdDTO backendIdDto = new BackendIdDTO();

        // Setzen der PKW-Einheiten
        if (ObjectUtils.isEmpty(zaehlungDto.getPkwEinheit())) {
            this.setYoungestPkwEinheitFromRelationalDatabase(zaehlungDto);
        }

        final Zaehlstelle zaehlstelle = this.indexService.getZaehlstelle(zaehlstelleId);

        // Zaehlung persitieren - ohne Suggestions
        final Zaehlung zaehlung = this.indexService.erstelleZaehlung(
                zaehlungDto,
                zaehlstelleId);

        // Koordinate prüfen.
        zaehlung.setPunkt(
                this.getKoordinateZaehlstelleWhenZaehlungWithinDistance(this.radiusDistanceCheck, zaehlstelle, zaehlung));

        // Zeitintervalle persistieren
        final List<Zeitintervall> zeitintervalleToPersist = new ArrayList<>();
        zaehlungDto.getVerkehrsbeziehungen().forEach(verkehrsbeziehung -> {
            verkehrsbeziehung.getZeitintervalle().stream()
                    .map(this.zeitintervallMapper::zeitintervallDtoToZeitintervall)
                    .map(zeitintervall -> this.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, verkehrsbeziehung))
                    .forEach(zeitintervalleToPersist::add);
        });

        this.zeitintervallPersistierungsService.aufbereitenUndPersistieren(zeitintervalleToPersist,
                List.of(Zaehldauer.DAUER_2_X_4_STUNDEN, Zaehldauer.DAUER_13_STUNDEN, Zaehldauer.DAUER_16_STUNDEN)
                        .contains(Zaehldauer.valueOf(zaehlung.getZaehldauer())));

        // Fahrzeugkategorien und -klassen setzen
        zaehlung.setKategorien(this.getFahrzeugKategorienAndFahrzeugklassen(zeitintervalleToPersist));

        this.indexService.erneuereZaehlung(zaehlung, zaehlstelle.getId());

        // Rückgabe der ZaehlungsId
        backendIdDto.setId(zaehlung.getId());
        return backendIdDto;
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
     * @param bearbeiteVerkehrsbeziehung zum Setzen der zusätzlichen Daten.
     * @return den {@link Zeitintervall} in welchem die zusätzlichen Informationen gesetzt sind.
     */
    public Zeitintervall setAdditionalDataToZeitintervall(
            final Zeitintervall zeitintervall,
            final Zaehlung zaehlung,
            final BearbeiteVerkehrsbeziehungDTO bearbeiteVerkehrsbeziehung) {
        zeitintervall.setZaehlungId(UUID.fromString(zaehlung.getId()));

        this.getFromBearbeiteVerkehrsbeziehungDto(zaehlung, bearbeiteVerkehrsbeziehung)
                .ifPresent(verkehrsbeziehung -> zeitintervall.setBewegungsbeziehungId(UUID.fromString(verkehrsbeziehung.getId())));

        zeitintervall.setHochrechnung(
                this.createHochrechnung(
                        zeitintervall,
                        bearbeiteVerkehrsbeziehung.getHochrechnungsfaktor(),
                        zaehlung.getZaehldauer()));

        zeitintervall.setVerkehrsbeziehung(this.mapToVerkehrsbeziehungForZeitintervall(bearbeiteVerkehrsbeziehung));
        return zeitintervall;
    }

    /**
     * Diese Methode gibt die {@link Verkehrsbeziehung} der {@link Zaehlung} zurück, welche durch die
     * {@link BearbeiteVerkehrsbeziehungDTO} repräsentiert wird.
     *
     * @param zaehlung aus der die {@link Verkehrsbeziehung} geholt und zurückgegeben werden soll.
     * @param bearbeiteVerkehrsbeziehung welche die Basis zum Suchen der {@link Verkehrsbeziehung}
     *            darstellt.
     * @return die gefundene {@link Verkehrsbeziehung}.
     */
    public Optional<Verkehrsbeziehung> getFromBearbeiteVerkehrsbeziehungDto(
            final Zaehlung zaehlung,
            final BearbeiteVerkehrsbeziehungDTO bearbeiteVerkehrsbeziehung) {
        return zaehlung.getVerkehrsbeziehungen().stream()
                .filter(verkehrsbeziehung -> this.isSameVerkehrsbeziehung(bearbeiteVerkehrsbeziehung, verkehrsbeziehung))
                .findFirst();
    }

    /**
     * Diese Methode prüft ob die beiden Verkehrsbeziehungsobjekte in den Parametern
     * die selbe Verkehrsbeziehung einer Kreuzung oder eines Kreisverkehrs repräsentieren.
     *
     * @param bearbeiteVerkehrsbeziehungDTO zur Prüfung auf repräsentation der selben Verkehrsbeziehung.
     * @param verkehrsbeziehung zur Prüfung auf repräsentation der selben Verkehrsbeziehung.
     * @return true falls die selbe Verkehrsbeziehung einer Kreuzung oder eines Kreisverkehrs
     *         repräsentiert wird.
     */
    public boolean isSameVerkehrsbeziehung(
            final BearbeiteVerkehrsbeziehungDTO bearbeiteVerkehrsbeziehungDTO,
            final Verkehrsbeziehung verkehrsbeziehung) {
        return Objects.equals(bearbeiteVerkehrsbeziehungDTO.getIsKreuzung(), verkehrsbeziehung.getIsKreuzung())
                // Kreuzung
                && Objects.equals(bearbeiteVerkehrsbeziehungDTO.getVon(), verkehrsbeziehung.getVon())
                && Objects.equals(bearbeiteVerkehrsbeziehungDTO.getNach(), verkehrsbeziehung.getNach())
                // Kreisverkehr
                && Objects.equals(bearbeiteVerkehrsbeziehungDTO.getKnotenarm(), verkehrsbeziehung.getKnotenarm())
                && Objects.equals(bearbeiteVerkehrsbeziehungDTO.getHinein(), verkehrsbeziehung.getHinein())
                && Objects.equals(bearbeiteVerkehrsbeziehungDTO.getHeraus(), verkehrsbeziehung.getHeraus())
                && Objects.equals(bearbeiteVerkehrsbeziehungDTO.getVorbei(), verkehrsbeziehung.getVorbei());
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
     * Diese Methode erstellt die {@link de.muenchen.dave.domain.Verkehrsbeziehung} zum Anfügen an einen
     * {@link Zeitintervall}.
     *
     * @param bearbeiteVerkehrsbeziehung aus dem die {@link de.muenchen.dave.domain.Verkehrsbeziehung}
     *            zum Anfügen
     *            an
     *            einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link de.muenchen.dave.domain.Verkehrsbeziehung} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public de.muenchen.dave.domain.Verkehrsbeziehung mapToVerkehrsbeziehungForZeitintervall(final BearbeiteVerkehrsbeziehungDTO bearbeiteVerkehrsbeziehung) {
        final de.muenchen.dave.domain.Verkehrsbeziehung verkehrsbeziehung = new de.muenchen.dave.domain.Verkehrsbeziehung();
        if (BooleanUtils.isTrue(bearbeiteVerkehrsbeziehung.getIsKreuzung())) {
            verkehrsbeziehung.setVon(bearbeiteVerkehrsbeziehung.getVon());
            verkehrsbeziehung.setNach(bearbeiteVerkehrsbeziehung.getNach());
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
     * Die Methode ermittelt die Koordinate für die Zaehlung auf Basis der im ersten Parameter
     * übergebenen Distanz.
     *
     * @param radiusDistanceCheck Der Radius für die Prüfung der Distanz.
     * @param zaehlstelle Die {@link Zaehlstelle} zur Prüfung des Abstands zur {@link Zaehlung}.
     * @param zaehlung Die {@link Zaehlung} zur Prüfung des Abstands zur {@link Zaehlstelle}.
     * @return die Koordinate der {@link Zaehlstelle}, falls die Koordinate der {@link Zaehlung} sich
     *         innerhalb des durch den Radius definierten Umkreis um die
     *         {@link Zaehlstelle} befindet. Ansonsten wird die Koordinate der {@link Zaehlung}
     *         zurückgegeben.
     */
    public GeoPoint getKoordinateZaehlstelleWhenZaehlungWithinDistance(final double radiusDistanceCheck,
            final Zaehlstelle zaehlstelle,
            final Zaehlung zaehlung) {
        GeoPoint position = zaehlung.getPunkt();
        if (CoordinateUtil.arePositionsWithinGivenDistance(
                radiusDistanceCheck,
                zaehlstelle.getPunkt(),
                zaehlung.getPunkt())) {
            log.debug("Die Koordinate der Zaehlstelle wird verwendet.");
            position = zaehlstelle.getPunkt();
        }
        return position;
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

    public BackendIdDTO updateDienstleisterkennung(final String zaehlungId, final DienstleisterDTO dienstleisterDTO)
            throws DataNotFoundException, BrokenInfrastructureException {
        final BackendIdDTO backendIdDto = new BackendIdDTO();

        final Zaehlung zaehlungToUpdate = this.indexService.getZaehlung(zaehlungId);
        zaehlungToUpdate.setDienstleisterkennung(dienstleisterDTO.getKennung());
        final Zaehlstelle zaehlstelleByZaehlungId = this.indexService.getZaehlstelleByZaehlungId(zaehlungId);

        // updateZaehlung
        this.indexService.erneuereZaehlung(
                zaehlungToUpdate,
                zaehlstelleByZaehlungId.getId());

        // Rückgabe der ZaehlungsId
        backendIdDto.setId(zaehlungId);
        return backendIdDto;
    }
}
