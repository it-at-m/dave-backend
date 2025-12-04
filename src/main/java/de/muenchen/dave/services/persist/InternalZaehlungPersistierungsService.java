package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.DienstleisterDTO;
import de.muenchen.dave.domain.dtos.OpenZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteFahrbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
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
        zaehlungDto.getFahrbeziehungen().forEach(fahrbeziehungDto -> {
            fahrbeziehungDto.getZeitintervalle().stream()
                    .map(this.zeitintervallMapper::zeitintervallDtoToZeitintervall)
                    .map(zeitintervall -> this.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, fahrbeziehungDto))
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
     * - Die UUID der {@link Fahrbeziehung}
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
            final BearbeiteFahrbeziehungDTO fahrbeziehungDto) {
        zeitintervall.setZaehlungId(UUID.fromString(zaehlung.getId()));

        this.getFromBearbeiteFahrbeziehungDto(zaehlung, fahrbeziehungDto)
                .ifPresent(fahrbeziehung -> zeitintervall.setFahrbeziehungId(UUID.fromString(fahrbeziehung.getId())));

        zeitintervall.setHochrechnung(
                this.createHochrechnung(
                        zeitintervall,
                        fahrbeziehungDto.getHochrechnungsfaktor(),
                        zaehlung.getZaehldauer()));

        zeitintervall.setFahrbeziehung(this.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto));
        return zeitintervall;
    }

    /**
     * Diese Methode gibt die {@link Fahrbeziehung} der {@link Zaehlung} zurück, welche durch die
     * {@link BearbeiteFahrbeziehungDTO} repräsentiert wird.
     *
     * @param zaehlung aus der die {@link Fahrbeziehung} geholt und zurückgegeben werden soll.
     * @param fahrbeziehungDto welche die Basis zum Suchen der {@link Fahrbeziehung} darstellt.
     * @return die gefundene {@link Fahrbeziehung}.
     */
    public Optional<Fahrbeziehung> getFromBearbeiteFahrbeziehungDto(final Zaehlung zaehlung,
            final BearbeiteFahrbeziehungDTO fahrbeziehungDto) {
        return zaehlung.getFahrbeziehungen().stream()
                .filter(fahrbeziehung -> this.isSameFahrbeziehung(fahrbeziehungDto, fahrbeziehung))
                .findFirst();
    }

    /**
     * Diese Methode prüft ob die beiden Fahrbeziehungsobjekte in den Parametern die selbe Fahrbeziehung
     * einer Kreuzung oder eines Kreisverkehrs
     * repräsentieren.
     *
     * @param fahrbeziehungDto zur Prüfung auf repräsentation der selben Fahrbeziehung.
     * @param fahrbeziehung zur Prüfung auf repräsentation der selben Fahrbeziehung.
     * @return true falls die selbe Fahrbeziehung einer Kreuzung oder eines Kreisverkehrs repräsentiert
     *         wird.
     */
    public boolean isSameFahrbeziehung(final BearbeiteFahrbeziehungDTO fahrbeziehungDto,
            final Fahrbeziehung fahrbeziehung) {
        return Objects.equals(fahrbeziehungDto.getIsKreuzung(), fahrbeziehung.getIsKreuzung())
                // Kreuzung
                && Objects.equals(fahrbeziehungDto.getVon(), fahrbeziehung.getVon())
                && Objects.equals(fahrbeziehungDto.getNach(), fahrbeziehung.getNach())
                // Kreisverkehr
                && Objects.equals(fahrbeziehungDto.getKnotenarm(), fahrbeziehung.getKnotenarm())
                && Objects.equals(fahrbeziehungDto.getHinein(), fahrbeziehung.getHinein())
                && Objects.equals(fahrbeziehungDto.getHeraus(), fahrbeziehung.getHeraus())
                && Objects.equals(fahrbeziehungDto.getVorbei(), fahrbeziehung.getVorbei());
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
     * Diese Methode erstellt die {@link de.muenchen.dave.domain.Fahrbeziehung} zum Anfügen an einen
     * {@link Zeitintervall}.
     *
     * @param fahrbeziehungDto aus dem die {@link de.muenchen.dave.domain.Fahrbeziehung} zum Anfügen an
     *            einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link de.muenchen.dave.domain.Fahrbeziehung} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public de.muenchen.dave.domain.Fahrbeziehung mapToFahrbeziehungForZeitintervall(final BearbeiteFahrbeziehungDTO fahrbeziehungDto) {
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
