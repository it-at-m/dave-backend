package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.DienstleisterDTO;
import de.muenchen.dave.domain.dtos.OpenZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteBewegungsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.elasticsearch.Bewegungsbeziehung;
import de.muenchen.dave.domain.elasticsearch.Laengsverkehr;
import de.muenchen.dave.domain.elasticsearch.Querungsverkehr;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Zaehlart;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
        final var coordinate = this.getKoordinateZaehlstelleWhenZaehlungWithinDistance(
                this.radiusDistanceCheck,
                zaehlstelle,
                zaehlung);
        zaehlung.setPunkt(coordinate);

        // Zeitintervalle persistieren
        final List<Zeitintervall> zeitintervalleToPersist = new ArrayList<>();
        final var bewegungsbeziehungen = getAllBewegungsbeziehungenFromZaehlung(zaehlungDto);

        bewegungsbeziehungen.forEach(bewegungsbeziehung -> {
            bewegungsbeziehung.getZeitintervalle().stream()
                    .map(this.zeitintervallMapper::zeitintervallDtoToZeitintervall)
                    .map(zeitintervall -> this.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, bewegungsbeziehung))
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
     * - Die {@link de.muenchen.dave.domain.Verkehrsbeziehung}, den
     * {@link de.muenchen.dave.domain.Laengsverkehr}
     * oder den {@link de.muenchen.dave.domain.Querungsverkehr}
     *
     * @param zeitintervall in welchem die zusätzlichen Informationen gesetzt werden sollen.
     * @param zaehlung zum Setzen der zusätzlichen Daten.
     * @param bearbeiteBewegungsbeziehung zum Setzen der zusätzlichen Daten.
     * @return den {@link Zeitintervall} in welchem die zusätzlichen Informationen gesetzt sind.
     */
    public Zeitintervall setAdditionalDataToZeitintervall(
            final Zeitintervall zeitintervall,
            final Zaehlung zaehlung,
            final BearbeiteBewegungsbeziehungDTO bearbeiteBewegungsbeziehung) {
        zeitintervall.setZaehlungId(UUID.fromString(zaehlung.getId()));

        // Setzen der Bewegungsbeziehungs-Id in Zeitintervall
        this.getBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto(zaehlung, bearbeiteBewegungsbeziehung)
                .ifPresent(bewegungsbeziehung -> zeitintervall.setBewegungsbeziehungId(UUID.fromString(bewegungsbeziehung.getId())));

        // Setzen der Hochrechnug in Zeitintervall
        // TODO: Hochrechnungfaktor an BearbeiteLaengsverkehrDTO und BearbeiteQuerungsverkehrDTO sowie an alle anderen Betroffen Model- und Entitätsklassen anhängen.
        final var hochrechnung = this.createHochrechnung(
                zeitintervall,
                bearbeiteBewegungsbeziehung.getHochrechnungsfaktor(),
                zaehlung.getZaehldauer());
        zeitintervall.setHochrechnung(hochrechnung);

        // Setzen der Bewegungsbeziehung im Zeitintervall
        final var zaehlart = Zaehlart.valueOf(zaehlung.getZaehlart());
        if (Zaehlart.FJS.equals(zaehlart)) {
            final var bearbeiteLaengsverkehrToMap = (BearbeiteLaengsverkehrDTO) bearbeiteBewegungsbeziehung;
            final var laengsverkehrForZeitintervall = this.createLaengsverkehrForZeitintervall(bearbeiteLaengsverkehrToMap);
            zeitintervall.setLaengsverkehr(laengsverkehrForZeitintervall);
        } else if (Zaehlart.QU.equals(zaehlart)) {
            final var bearbeiteQuerungsverkehrToMap = (BearbeiteQuerungsverkehrDTO) bearbeiteBewegungsbeziehung;
            final var querungsverkehrForZeitintervall = this.createQuerungsverkehrForZeitintervall(bearbeiteQuerungsverkehrToMap);
            zeitintervall.setQuerungsverkehr(querungsverkehrForZeitintervall);
        } else {
            // alle anderen Zählarten
            final var bearbeiteVerkehrsbeziehungToMap = (BearbeiteVerkehrsbeziehungDTO) bearbeiteBewegungsbeziehung;
            final var verkehrsbeziehungForZeitintervall = this.createVerkehrsbeziehungForZeitintervall(zaehlart, bearbeiteVerkehrsbeziehungToMap);
            zeitintervall.setVerkehrsbeziehung(verkehrsbeziehungForZeitintervall);
        }

        return zeitintervall;
    }

    /**
     * Diese Methode gibt die {@link Bewegungsbeziehung} der {@link Zaehlung} zurück, welche durch die
     * {@link BearbeiteVerkehrsbeziehungDTO} repräsentiert wird.
     *
     * @param zaehlung aus der die {@link Bewegungsbeziehung} geholt und zurückgegeben werden soll.
     * @param bearbeiteBewegungsbeziehung welche die Basis zum Suchen der {@link Bewegungsbeziehung}
     *            darstellt.
     * @return die gefundene {@link Bewegungsbeziehung}.
     */
    public Optional<Bewegungsbeziehung> getBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto(
            final Zaehlung zaehlung,
            final BearbeiteBewegungsbeziehungDTO bearbeiteBewegungsbeziehung) {

        final var zaehlart = Zaehlart.valueOf(zaehlung.getZaehlart());
        return getAllBewegungsbeziehungenFromZaehlung(zaehlung)
                .stream()
                .filter(bewegungsbeziehung -> this.isSameVerkehrsbeziehung(zaehlart, bearbeiteBewegungsbeziehung, bewegungsbeziehung))
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
    public boolean isSameVerkehrsbeziehung(
            final Zaehlart zaehlart,
            final BearbeiteBewegungsbeziehungDTO bearbeiteBewegungsbeziehung,
            final Bewegungsbeziehung bewegungsbeziehung) {
        if (Zaehlart.QU.equals(zaehlart)) {
            final var bearbeiteQuerungsverkehr = (BearbeiteQuerungsverkehrDTO) bearbeiteBewegungsbeziehung;
            final var verkehrsbeziehung = (Querungsverkehr) bewegungsbeziehung;
            return Objects.equals(bearbeiteQuerungsverkehr.getRichtung(), verkehrsbeziehung.getRichtung())
                    && Objects.equals(bearbeiteQuerungsverkehr.getKnotenarm(), verkehrsbeziehung.getKnotenarm());
        } else if (Zaehlart.FJS.equals(zaehlart)) {
            final var bearbeiteLaengsverkehr = (BearbeiteLaengsverkehrDTO) bearbeiteBewegungsbeziehung;
            final var laengsverkehr = (Laengsverkehr) bewegungsbeziehung;
            return Objects.equals(bearbeiteLaengsverkehr.getRichtung(), laengsverkehr.getRichtung())
                    && Objects.equals(bearbeiteLaengsverkehr.getStrassenseite(), laengsverkehr.getStrassenseite())
                    && Objects.equals(bearbeiteLaengsverkehr.getKnotenarm(), laengsverkehr.getKnotenarm());
        } else {
            // alle anderen Zählarten
            final var bearbeiteVerkehrsbeziehung = (BearbeiteVerkehrsbeziehungDTO) bearbeiteBewegungsbeziehung;
            final var verkehrsbeziehung = (Verkehrsbeziehung) bewegungsbeziehung;
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
     * Diese Methode erstellt die {@link de.muenchen.dave.domain.Verkehrsbeziehung} zum Anfügen an einen
     * {@link Zeitintervall}.
     *
     * @param zaehlart zur Unterscheidung ob {@link Zaehlart#QU} oder eine andere Zählart für
     *            Verkehrsbeziehungen.
     * @param bearbeiteVerkehrsbeziehung aus dem die {@link de.muenchen.dave.domain.Verkehrsbeziehung}
     *            zum Anfügen
     *            an
     *            einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link de.muenchen.dave.domain.Verkehrsbeziehung} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public de.muenchen.dave.domain.Verkehrsbeziehung createVerkehrsbeziehungForZeitintervall(
            final Zaehlart zaehlart,
            final BearbeiteVerkehrsbeziehungDTO bearbeiteVerkehrsbeziehung) {
        final var verkehrsbeziehung = new de.muenchen.dave.domain.Verkehrsbeziehung();
        if (Zaehlart.QU.equals(zaehlart)) {
            verkehrsbeziehung.setVon(bearbeiteVerkehrsbeziehung.getVon());
            verkehrsbeziehung.setNach(bearbeiteVerkehrsbeziehung.getNach());
            // TODO: Straßenseite einfügen.
        } else if (BooleanUtils.isTrue(bearbeiteVerkehrsbeziehung.getIsKreuzung())) {
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
     * Diese Methode erstellt die {@link de.muenchen.dave.domain.Querungsverkehr} zum Anfügen
     * an einen {@link Zeitintervall}.
     *
     * @param bearbeiteQuerungsverkehr aus dem die {@link de.muenchen.dave.domain.Querungsverkehr}
     *            zum Anfügen an einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link de.muenchen.dave.domain.Querungsverkehr} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public de.muenchen.dave.domain.Querungsverkehr createQuerungsverkehrForZeitintervall(final BearbeiteQuerungsverkehrDTO bearbeiteQuerungsverkehr) {
        final var querungsverkehr = new de.muenchen.dave.domain.Querungsverkehr();
        // TODO: Setzen des Knotenarms
        querungsverkehr.setRichtung(bearbeiteQuerungsverkehr.getRichtung());
        return querungsverkehr;
    }

    /**
     * Diese Methode erstellt die {@link de.muenchen.dave.domain.Laengsverkehr} zum Anfügen
     * an einen {@link Zeitintervall}.
     *
     * @param bearbeiteLaengsverkehr aus dem die {@link de.muenchen.dave.domain.Laengsverkehr}
     *            zum Anfügen an einen {@link Zeitintervall} erstellt werden soll.
     * @return die {@link de.muenchen.dave.domain.Laengsverkehr} zum Anfügen an einen
     *         {@link Zeitintervall}
     */
    public de.muenchen.dave.domain.Laengsverkehr createLaengsverkehrForZeitintervall(final BearbeiteLaengsverkehrDTO bearbeiteLaengsverkehr) {
        final var laengsverkehr = new de.muenchen.dave.domain.Laengsverkehr();
        // TODO: Setzen des Knotenarms
        laengsverkehr.setRichtung(bearbeiteLaengsverkehr.getRichtung());
        laengsverkehr.setStrassenseite(bearbeiteLaengsverkehr.getStrassenseite());
        return laengsverkehr;
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

    protected List<BearbeiteBewegungsbeziehungDTO> getAllBewegungsbeziehungenFromZaehlung(final BearbeiteZaehlungDTO zaehlung) {
        final var bewegungsbeziehungen = new LinkedList<BearbeiteBewegungsbeziehungDTO>();
        final var laengsverkehr = CollectionUtils.emptyIfNull(zaehlung.getLaengsverkehr());
        bewegungsbeziehungen.addAll(laengsverkehr);
        final var querungsverkehr = CollectionUtils.emptyIfNull(zaehlung.getQuerungsverkehr());
        bewegungsbeziehungen.addAll(querungsverkehr);
        final var verkehrsbeziehungen = CollectionUtils.emptyIfNull(zaehlung.getVerkehrsbeziehungen());
        bewegungsbeziehungen.addAll(verkehrsbeziehungen);
        return bewegungsbeziehungen;
    }

    protected List<Bewegungsbeziehung> getAllBewegungsbeziehungenFromZaehlung(final Zaehlung zaehlung) {
        final var bewegungsbeziehungen = new LinkedList<Bewegungsbeziehung>();
        final var laengsverkehr = CollectionUtils.emptyIfNull(zaehlung.getLaengsverkehr());
        bewegungsbeziehungen.addAll(laengsverkehr);
        final var querungsverkehr = CollectionUtils.emptyIfNull(zaehlung.getQuerungsverkehr());
        bewegungsbeziehungen.addAll(querungsverkehr);
        final var verkehrsbeziehungen = CollectionUtils.emptyIfNull(zaehlung.getVerkehrsbeziehungen());
        bewegungsbeziehungen.addAll(verkehrsbeziehungen);
        return bewegungsbeziehungen;
    }
}
