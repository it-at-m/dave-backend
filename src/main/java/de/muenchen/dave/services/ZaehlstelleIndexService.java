package de.muenchen.dave.services;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.dtos.LeseZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.NextZaehlstellennummerDTO;
import de.muenchen.dave.domain.dtos.OpenZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.UpdateStatusDTO;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlungDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlstelleWithUnreadMessageDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungWithUnreadMessageDTO;
import de.muenchen.dave.domain.enums.Participant;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.ZaehlstelleMapper;
import de.muenchen.dave.domain.mapper.ZaehlungMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.exceptions.PlausibilityException;
import de.muenchen.dave.relational.repository.ZaehlstellenIndexRepository;
import de.muenchen.dave.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.services.persist.BewegungsbeziehungSynchronisierungsService;
import de.muenchen.dave.services.persist.KnotenarmSynchronisierungsService;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ZaehlstelleIndexService {

    private final ZaehlstellenIndexRepository zaehlstelleIndex;
    private final CustomSuggestIndexService customSuggestIndexService;
    private final ZaehlungMapper zaehlungMapper;
    private final ZaehlstelleMapper zaehlstelleMapper;
    private final ZeitauswahlService zeitauswahlService;
    private final ChatMessageService messageService;
    private final StadtbezirkMapper stadtbezirkMapper;
    private final SanitizationService sanitizationService;
    private final BewegungsbeziehungSynchronisierungsService bewegungsbeziehungSynchronisierungsService;
    private final KnotenarmSynchronisierungsService knotenarmSynchronisierungsService;
    private final EntityManager entityManager;

    @Value(value = "${elasticsearch.host}")
    private String elasticsearchHost;

    @Value(value = "${elasticsearch.port}")
    private String elasticsearchPort;

    public ZaehlstelleIndexService(final ZeitauswahlService zeitauswahlService,
                                   final ZaehlstelleMapper zaehlstelleMapper,
                                   final CustomSuggestIndexService customSuggestIndexService,
                                   final ZaehlungMapper zaehlungMapper,
                                   final ZaehlstellenIndexRepository zaehlstelleIndex,
                                   // @Lazy prevents circular dependency
                                   @Lazy final ChatMessageService messageService,
                                   final StadtbezirkMapper stadtbezirkMapper,
                                   final SanitizationService sanitizationService,
                                   final BewegungsbeziehungSynchronisierungsService bewegungsbeziehungSynchronisierungsService,
                                   final KnotenarmSynchronisierungsService knotenarmSynchronisierungsService, EntityManager entityManager) {
        this.zeitauswahlService = zeitauswahlService;
        this.zaehlstelleMapper = zaehlstelleMapper;
        this.customSuggestIndexService = customSuggestIndexService;
        this.zaehlungMapper = zaehlungMapper;
        this.zaehlstelleIndex = zaehlstelleIndex;
        this.messageService = messageService;
        this.stadtbezirkMapper = stadtbezirkMapper;
        this.sanitizationService = sanitizationService;
        this.bewegungsbeziehungSynchronisierungsService = bewegungsbeziehungSynchronisierungsService;
        this.knotenarmSynchronisierungsService = knotenarmSynchronisierungsService;
        this.entityManager = entityManager;
    }

    public BackendIdDTO speichereZaehlstelle(final BearbeiteZaehlstelleDTO zaehlstelle)
            throws BrokenInfrastructureException, DataNotFoundException, PlausibilityException {
        final var backendIdDto = new BackendIdDTO();
        sanitizationService.sanitizeBearbeiteZaehlstelleDto(zaehlstelle);
        if (StringUtils.isEmpty(zaehlstelle.getId())) {
            final var alreadySavedZaehlstelle = zaehlstelleIndex.findByNummer(zaehlstelle.getNummer());
            if (alreadySavedZaehlstelle.isPresent()) {
                final var message = "Es existiert bereits eine Zählstelle mit der Nummer " + zaehlstelle.getNummer();
                log.error(message);
                throw new PlausibilityException(message);
            }
            backendIdDto.setId(this.erstelleZaehlstelle(zaehlstelle));
        } else {
            backendIdDto.setId(this.erneuereZaehlstelle(zaehlstelle, UUID.fromString(zaehlstelle.getId())));
        }
        return backendIdDto;
    }

    /**
     * Zählstelle im Index speichern. Sollte Elasticsearch nicht erreichbar sein, dann wird eine
     * entsprechende Exception geworfen und eine Fehlermeldung
     * geloggt.
     *
     * @param zaehlstelle zu Speichernede ZaehlstelleEntity
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     */
    public void speichereZaehlstelleInDatenbank(final ZaehlstelleEntity zaehlstelle) throws BrokenInfrastructureException {
        try {
            final ZaehlstelleEntity save = this.zaehlstelleIndex.save(zaehlstelle);
            log.warn("Zählstelle {} wurde erfolgreich gespeichert", save.getId());
        } catch (final DataAccessResourceFailureException e) {
            log.error("cannot access elasticsearch index on {}:{}", this.elasticsearchHost, this.elasticsearchPort);
            throw new BrokenInfrastructureException();
        }
    }

    /**
     * Erstellt eine neue Zählstelle.
     *
     * @param zdto anzulegende Zählstelle als DTO
     * @return ID der
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     */
    public UUID erstelleZaehlstelle(final BearbeiteZaehlstelleDTO zdto) throws BrokenInfrastructureException {

        final ZaehlstelleEntity zaehlstelle = this.zaehlstelleMapper.bearbeiteDto2bean(zdto, stadtbezirkMapper);
        zaehlstelle.setId(UUID.randomUUID());
        zaehlstelle.setZaehlungen(new ArrayList<>());
        customSuggestIndexService.createSuggestionsForZaehlstelle(zaehlstelle);
        this.speichereZaehlstelleInDatenbank(zaehlstelle);
        return zaehlstelle.getId();
    }

    /**
     * Speichert die Daten zur Zählstelle, wenn diese erneuert wurden. Es wird erwartet, dass immer alle
     * Werte zur Zählung übergeben werden, auch welche, die
     * nicht verändert wurden. Werden diese nicht übergebn, so entstehen (ungewollt) leere Attribute.
     *
     * @param zdto DTO der Zählstelle
     * @param zaehlstelleId ID der Zählstelle
     * @return ID der Zählstelle
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     * @throws DataNotFoundException Wenn die Daten nicht geladen werden konnten
     */
    public UUID erneuereZaehlstelle(final BearbeiteZaehlstelleDTO zdto, final UUID zaehlstelleId)
            throws BrokenInfrastructureException, DataNotFoundException {
        final Optional<ZaehlstelleEntity> zsto = this.zaehlstelleIndex.findById(zaehlstelleId);
        if (zsto.isPresent()) {
            final ZaehlstelleEntity zaehlstelle = this.zaehlstelleMapper.bearbeiteDto2bean(zdto, stadtbezirkMapper);
            // ID muss erhalten bleiben
            zaehlstelle.setId(zaehlstelleId);
            // Die Zählungen müssen erhalten bleiben
            zaehlstelle.setZaehlungen(zsto.get().getZaehlungen());
            this.updateZaehlstelleWithLetzteZaehlung(zaehlstelle);
            customSuggestIndexService.updateSuggestionsForZaehlstelle(zaehlstelle);
            this.speichereZaehlstelleInDatenbank(zaehlstelle);
            return zaehlstelle.getId();
        } else {
            log.error("Keine Zählstelle zur id {} gefunden.", zaehlstelleId);
            throw new DataNotFoundException("Die Zählstelle konnte nicht aktualisiert werden.");
        }
    }

    /**
     * Erstellt eine neue Zählung, updatet die entsprechende Zählstelle und speichert alles im Index.
     *
     * @param zdto DTO der ZaehlungEntity
     * @param zaehlstelleId ID der Zählstelle
     * @return die {@link ZaehlungEntity}#getId()
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     * @throws DataNotFoundException Bei Ladefehlern
     */
    public ZaehlungEntity erstelleZaehlung(final BearbeiteZaehlungDTO zdto, final UUID zaehlstelleId) throws BrokenInfrastructureException, DataNotFoundException {

        final ZaehlungEntity zaehlung = this.zaehlungMapper.bearbeiteDto2bean(zdto);
        zaehlung.getKnotenarme().forEach(knotenarm -> knotenarm.setZaehlung(zaehlung));
        this.knotenarmSynchronisierungsService.synchronisiere(
                zdto,
                zaehlung);
        this.bewegungsbeziehungSynchronisierungsService.synchronisiere(
                zdto,
                zaehlung);

        entityManager.persist(zaehlung);

        // Zählstelle erneuern
        final Optional<ZaehlstelleEntity> zsto = this.zaehlstelleIndex.findById(zaehlstelleId);
        if (zsto.isPresent()) {
            final ZaehlstelleEntity zaehlstelleUpdated = this.updateZaehlstelleWithZaehlung(zsto.get(), zaehlung);
            customSuggestIndexService.createSuggestionsForZaehlung(zaehlung);
            this.speichereZaehlstelleInDatenbank(zaehlstelleUpdated);
        } else {
            log.error("Keine Zählstelle zur id {} gefunden.", zaehlstelleId);
            throw new DataNotFoundException("Die Zählung konnte nicht angelegt werden, da die dazugehörige Zählstelle nicht gefunden wurde. ");
        }
        return zaehlung;
    }

    /**
     * Erneuert die {@link ZaehlungEntity} welche an der {@link ZaehlstelleEntity}
     * - identifiziert entsprechend des Parameters zaehlstelleId - angefügt ist.
     *
     * @param zdto zur Erneuerung in der {@link ZaehlstelleEntity}.
     * @param zaehlstelleId zur Identifikation der {@link ZaehlstelleEntity}.
     * @return aktualisierte ZaehlungEntity
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     * @throws DataNotFoundException Bei Ladefehlern
     */
    public ZaehlungEntity erneuereZaehlung(
            final BearbeiteZaehlungDTO zdto,
            final UUID zaehlstelleId
    ) throws BrokenInfrastructureException, DataNotFoundException {

        final ZaehlstelleEntity zst = this.zaehlstelleIndex.findById(zaehlstelleId)
                .orElseThrow(() -> {
                    log.error("Keine Zählstelle zur id {} gefunden.", zaehlstelleId);
                    return new DataNotFoundException("Die Zählstelle konnte nicht gefunden werden.");
                });

        final ZaehlungEntity existingZaehlung = zst.getZaehlungen().stream()
                .filter(zaehlung -> zaehlung.getId().equals(zdto.getId()))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException(
                        "Die Zählung konnte in der Zählstelle nicht gefunden werden."));

        this.zaehlungMapper.updateEntity(zdto, existingZaehlung);
        this.knotenarmSynchronisierungsService.synchronisiere(
                zdto,
                existingZaehlung);
        this.bewegungsbeziehungSynchronisierungsService.synchronisiere(
                zdto,
                existingZaehlung);

        this.customSuggestIndexService.updateSuggestionsForZaehlung(existingZaehlung);
        this.updateZaehlstelleWithLetzteZaehlung(zst);
        this.speichereZaehlstelleInDatenbank(zst);

        return existingZaehlung;
    }

    /**
     * Erneuert die {@link ZaehlungEntity} welche an der {@link ZaehlstelleEntity}
     * - identifiziert entsprechend des Parameters zaehlstelleId - angefügt ist.
     * <p>
     * Falls die zu erneuernde {@link ZaehlungEntity} mehr Suchwörter beinhaltet als die bisherige
     * persistierte Zählung, so werden die neuen Suchwörter persistiert.
     *
     * @param zl zur Erneuerung in der {@link ZaehlstelleEntity}.
     * @param zaehlstelleId zur Identifikation der {@link ZaehlstelleEntity}.
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     */
    public void erneuereZaehlung(final ZaehlungEntity zl, final UUID zaehlstelleId) throws BrokenInfrastructureException {
        final Optional<ZaehlstelleEntity> zsto = this.zaehlstelleIndex.findById(zaehlstelleId);
        if (zsto.isPresent()) {
            final ZaehlstelleEntity zst = zsto.get();
            final List<String> persistedSuchwoerter = new ArrayList<>();
            for (int index = 0; index < zst.getZaehlungen().size(); index++) {
                // Ersetze bisherige Zählung durch erneuerte Zählung
                if (zst.getZaehlungen().get(index).getId().equals(zl.getId())) {
                    final List<String> suchwoerter = zst.getZaehlungen().get(index).getSuchwoerter();
                    if (CollectionUtils.isNotEmpty(suchwoerter)) {
                        persistedSuchwoerter.addAll(suchwoerter);
                    }
                    zst.getZaehlungen().set(index, zl);
                }
            }
            // Persistiere zusätzliche Suchwörter ohne die Suchwörter in "zl" zu verändern.
            final List<String> suchwoerterToUpdate = new ArrayList<>(zl.getSuchwoerter());
            suchwoerterToUpdate.removeIf(persistedSuchwoerter::contains);
            if (CollectionUtils.isNotEmpty(suchwoerterToUpdate)) {
                customSuggestIndexService.updateSuggestionsForZaehlung(zl);
            }
            // Speichere Zählstelle mit erneuerter Zählung
            this.speichereZaehlstelleInDatenbank(zst);
        }
    }

    /**
     * Setzt den Status des übergebenen Dienstleisters auf den übergebenen Wert, sodass dieser eine
     * Benachrichtigung im Frontend sehen kann.
     *
     * @param zaehlungId Zaehlungsid
     * @param participant Chat-Teilnehmer
     * @param status boolean
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     */
    public void setUnreadMessagesInZaehlungForParticipant(final UUID zaehlungId, final Participant participant, final Boolean status)
            throws BrokenInfrastructureException {
        final Optional<ZaehlstelleEntity> zsto = this.zaehlstelleIndex.findByZaehlungenId((zaehlungId));
        if (zsto.isPresent()) {
            final ZaehlstelleEntity zst = zsto.get();
            for (final ZaehlungEntity zaehlung : zst.getZaehlungen()) {
                if (zaehlung.getId().equals(zaehlungId)) {
                    if (participant.equals(Participant.DIENSTLEISTER)) {
                        zaehlung.setUnreadMessagesDienstleister(status);
                    } else if (participant.equals(Participant.MOBILITAETSREFERAT)) {
                        zaehlung.setUnreadMessagesMobilitaetsreferat(status);
                    }
                }
            }
            this.speichereZaehlstelleInDatenbank(zst);
        }
    }

    public boolean deleteZaehlung(final UUID zaehlungId) throws DataNotFoundException, BrokenInfrastructureException {
        boolean isDeleted = false;
        // ZaehlstelleEntity anhand der ZaehlungEntity laden
        final Optional<ZaehlstelleEntity> byZaehlungenId = this.zaehlstelleIndex.findByZaehlungenId(zaehlungId);

        if (byZaehlungenId.isPresent()) {
            final ZaehlstelleEntity zaehlstelle = byZaehlungenId.get();
            // Zu löschende Zählung entfernen
            isDeleted = zaehlstelle.getZaehlungen().removeIf(zaehlung -> zaehlung.getId().equals(zaehlungId));

            // Alle Vorschläge zur Zählung ebenfalls löschen
            customSuggestIndexService.deleteAllSuggestionsByFkid(zaehlungId);

            // Zählstelle speichern
            updateZaehlstelleWithLetzteZaehlung(zaehlstelle);
            this.speichereZaehlstelleInDatenbank(zaehlstelle);
        } else {
            throw new DataNotFoundException("Die zu löschende Zählung konnte nicht gefunden werden");
        }
        return isDeleted;
    }

    /**
     * Erneuert die Parameter einer Zählstelle, die von der letzten Zählung abhängig sind. Dabei ist es
     * irrelevant, ob die übergebene Zählung die letzte Zählung
     * ist.
     *
     * @param zaehlstelle ZaehlstelleEntity
     * @param zaehlung ZaehlungEntity
     * @return geaenderte ZaehlstelleEntity
     */
    public ZaehlstelleEntity updateZaehlstelleWithZaehlung(final ZaehlstelleEntity zaehlstelle, final ZaehlungEntity zaehlung) {
        log.warn("Aktualisiere Zählstelle {} mit Zählung {}", zaehlstelle.getId(), zaehlung.getDatum());
        zaehlstelle.getZaehlungen().add(zaehlung);
        this.updateZaehlstelleWithLetzteZaehlung(zaehlstelle);
        return zaehlstelle;
    }

    /**
     * Setzt die Felder der Zählstelle, die sich auf die zuletzt durchgeführte Zählung beziehen.
     *
     * @param zaehlstelle
     */
    private void updateZaehlstelleWithLetzteZaehlung(final ZaehlstelleEntity zaehlstelle) {
        final ZaehlungEntity letzteZaehlung = IndexServiceUtils.getLetzteAktiveZaehlung(zaehlstelle.getZaehlungen());
        if (letzteZaehlung != null) {
            zaehlstelle.setLetzteZaehlungMonat(letzteZaehlung.getMonat());
            zaehlstelle.setLetzteZaehlungMonatNummer(letzteZaehlung.getDatum().getMonthValue());
            zaehlstelle.setGrundLetzteZaehlung(letzteZaehlung.getZaehlsituation());
            zaehlstelle.setLetzteZaehlungJahr(Integer.parseInt(letzteZaehlung.getJahr()));
        } else {
            zaehlstelle.setLetzteZaehlungMonat("");
            zaehlstelle.setLetzteZaehlungMonatNummer(null);
            zaehlstelle.setGrundLetzteZaehlung("");
            zaehlstelle.setLetzteZaehlungJahr(null);
        }
    }

    public List<ZaehlstelleEntity> getAllZaehlstellen() {
        return IterableUtils.toList(
                this.zaehlstelleIndex.findAll());
    }

    public ZaehlstelleEntity getZaehlstelle(final String id) throws DataNotFoundException {
        log.debug("Zugriff auf #getZaehlstelle");
        return this.zaehlstelleIndex.findById(UUID.fromString(id))
                .orElseThrow(() -> new DataNotFoundException("Die gesuchte Zählstelle wurde nicht gefunden."));
    }

    public ZaehlstelleEntity getZaehlstelleByZaehlungId(final UUID id) throws DataNotFoundException {
        return this.zaehlstelleIndex.findByZaehlungenId(id)
                .orElseThrow(() -> new DataNotFoundException("Die gesuchte Zählstelle wurde nicht gefunden."));
    }

    private ZaehlungEntity ladeZaehlung(final UUID gesuchteZaehlungId) throws DataNotFoundException {
     final ZaehlstelleEntity zaehlstelle =
                this.zaehlstelleIndex.findByZaehlungenId(gesuchteZaehlungId)
                        .orElseThrow(() -> new DataNotFoundException("ZaehlstelleEntity not found"));

        return zaehlstelle.getZaehlungen().stream()
                .filter(zaehlungToCheck -> zaehlungToCheck.getId().equals(gesuchteZaehlungId))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("ZaehlungEntity not found"));
    }

    private ZaehlungEntity ladeZaehlung(final String zaehlstellenNummer,
            final Zaehlart zaehlart,
            final LocalDate zaehldatum) throws DataNotFoundException {
        final ZaehlstelleEntity zaehlstelle = this.zaehlstelleIndex.findByNummer(zaehlstellenNummer)
                .orElseThrow(() -> new DataNotFoundException("ZaehlstelleEntity not found"));
        return zaehlstelle.getZaehlungen().stream()
                .filter(zaehlung -> zaehlung.getZaehlart().equals(zaehlart)
                        && zaehlung.getDatum().isEqual(zaehldatum))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("ZaehlungEntity not found"));
    }

    public ZaehlungEntity getZaehlung(final UUID zaehlungId) throws DataNotFoundException {
        return this.ladeZaehlung(zaehlungId);
    }

    public ZaehlungEntity getZaehlung(final String zaehlstellenNummer,
            final Zaehlart zaehlart,
            final LocalDate zaehldatum) throws DataNotFoundException {
        return this.ladeZaehlung(zaehlstellenNummer, zaehlart, zaehldatum);
    }

    @Cacheable(value = CachingConfiguration.READ_ZAEHLSTELLE_DTO, key = "{#p0, #p1}")
    public LeseZaehlstelleDTO readZaehlstelleDTO(final String zaehlstelleId, final boolean isFachadmin) throws DataNotFoundException {
        log.debug("Zugriff auf #readZaehlstelleDTO");
        final LeseZaehlstelleDTO leseZaehlstelleDTO = this.zaehlstelleMapper.bean2LeseZaehlstelleDto(this.getZaehlstelle(zaehlstelleId));
        // Alle ZaehlungEntity mit einem Status != ACTIVE werden ausgefilter (wenn kein Fachadmin angemeldet ist)
        leseZaehlstelleDTO.setZaehlungen(
                leseZaehlstelleDTO.getZaehlungen().stream()
                        .filter(leseZaehlungDTO -> {
                            // Der Fachadmin darf auch nicht freigegebene Zählungen lesen
                            if (isFachadmin) {
                                return leseZaehlungDTO.getStatus().equalsIgnoreCase(Status.ACTIVE.name())
                                        || leseZaehlungDTO.getStatus().equalsIgnoreCase(Status.ACCOMPLISHED.name());
                            } else {
                                return leseZaehlungDTO.getStatus().equalsIgnoreCase(Status.ACTIVE.name());
                            }
                        })
                        .peek(leseZaehlungDTO -> leseZaehlungDTO
                                .setKategorien(CollectionUtils.emptyIfNull(leseZaehlungDTO.getKategorien()).stream().distinct().toList()))
                        .collect(Collectors.toList()));

        // Die Zeitauswahl wird hier in die Zählungen geschrieben
        leseZaehlstelleDTO.getZaehlungen().forEach(leseZaehlungDTO -> leseZaehlungDTO.setZeitauswahl(
                this.zeitauswahlService.determinePossibleZeitauswahl(
                        leseZaehlungDTO.getZaehldauer(),
                        leseZaehlungDTO.getId())));
        return leseZaehlstelleDTO;
    }

    public BearbeiteZaehlstelleDTO readEditZaehlstelleDTO(final String zaehlstelleId) throws DataNotFoundException {
        return this.zaehlstelleMapper.bean2bearbeiteDto(this.getZaehlstelle(zaehlstelleId), stadtbezirkMapper);
    }

    /**
     * Aufbau der Zaehlstellennummer Stelle 1 und 2: Stadtbezirksnummer ohne fuehrende 0 Stelle 3 und 4:
     * Stadtbezirksviertelnummer Stelle 5 und 6: laufende
     * Nummer (wird aus DB geholt)
     *
     * @param partOfzaehlstelleId Ersten 3-4 Zeichen der Zaehlstellennummer (ohne fuehrende 0)
     * @param stadtbezirksnummer Nummer des Stadtbezirks der ZaehlstelleEntity
     * @return naechste Zaehlstellennummer
     */
    public NextZaehlstellennummerDTO getNextZaehlstellennummer(final String partOfzaehlstelleId, final Integer stadtbezirksnummer) {
        final List<ZaehlstelleEntity> zaehlstellen = this.zaehlstelleIndex.findAllByNummerStartsWithAndStadtbezirkNummer(partOfzaehlstelleId, stadtbezirksnummer);
        final NextZaehlstellennummerDTO dto = new NextZaehlstellennummerDTO();
        if (CollectionUtils.isNotEmpty(zaehlstellen)) {
            final AtomicInteger lastZaehlstellennummer = new AtomicInteger(0);
            zaehlstellen.forEach(zaehlstelle -> {
                final int zaehlstellennummer = Integer.parseInt(zaehlstelle.getNummer());
                if (zaehlstellennummer > lastZaehlstellennummer.get()) {
                    lastZaehlstellennummer.set(zaehlstellennummer);
                }
            });
            dto.setNummer(String.valueOf(lastZaehlstellennummer.incrementAndGet()));
        } else {
            dto.setNummer(partOfzaehlstelleId + "01");
        }
        return dto;
    }

    /**
     * Ermittelt alle Zaehlungen, die für den Dienstleister relevant sind. Relevante Zählungen haben
     * einen der folgenden Status: Status.COUNTING,
     * Status.CORRECTION, Status.INSTRUCTED
     *
     * @param diensleisterkennung Kennung des Dienstleisters
     * @param isFachadmin Ist der User ein Fachadmin?
     * @return Liste mit allen für Extern relevanten Zählungen
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     */
    public List<ExternalZaehlungDTO> getZaehlungenForExternal(final String diensleisterkennung, final boolean isFachadmin)
            throws BrokenInfrastructureException {
        log.debug(String.format("getZaehlungenForExternal(|%s|, |%s|)", diensleisterkennung, isFachadmin));
        final List<ExternalZaehlungDTO> zaehlungen = new ArrayList<>();
        final PageRequest pageable = PageRequest.of(0, 10000);
        try {
            final String[] status = {
                    Status.COUNTING.name(),
                    Status.CORRECTION.name(),
                    Status.INSTRUCTED.name()
            };
            final List<String> statusAsList = Arrays.asList(status);

            // ElasticSearch Syntax zum Suchen nach dem Status
            // zaehlungen.status:INSTRUCTED OR zaehlungen.status:CORRECTION OR zaehlungen.status:COUNTING
            // Wird anhand des Arrays status zusammengebaut
            final String or = " OR ";
            final String field = "zaehlungen.status:";
            final StringBuilder query = new StringBuilder();
            for (int index = 0; index < status.length;) {
                query.append(field);
                query.append(status[index]);
                index++;
                if (index < status.length) {
                    query.append(or);
                }
            }

            // Da eine Liste mit Zaehlstellen zurück kommt, müssen alle relevanten Zäehlungen
            // anhand des Status herausgesucht werden. Ist eine Zählung relevant, so werden die
            // benötigten Daten aus der Zählstelle in das Objekt kopiert.
            final List<ZaehlstelleEntity> allByExternalStatus = this.zaehlstelleIndex.findAllByStatus(new String(query), pageable).toList();
            allByExternalStatus.forEach(zaehlstelle -> {
                this.zaehlstelleMapper.bean2ExternalDto(zaehlstelle).getZaehlungen().forEach(zaehlung -> {
                    // Wenn Fachadmin, dann anzeigen, sonst anhand der Dienstleisterkennung filtern
                    if (statusAsList.contains(zaehlung.getStatus())
                            && (StringUtils.equalsIgnoreCase(diensleisterkennung, zaehlung.getDienstleisterkennung()) || isFachadmin)) {
                        if (StringUtils.isNotEmpty(zaehlstelle.getKommentar())) {
                            zaehlung.setZaehlstelleKommentar(zaehlstelle.getKommentar());
                        }
                        if (StringUtils.isNotEmpty(zaehlstelle.getNummer())) {
                            zaehlung.setZaehlstelleNummer(zaehlstelle.getNummer());
                        }
                        if (StringUtils.isNotEmpty(zaehlstelle.getStadtbezirk())) {
                            zaehlung.setZaehlstelleStadtbezirk(zaehlstelle.getStadtbezirk());
                        }
                        zaehlungen.add(zaehlung);
                    }
                });
            });
        } catch (final DataAccessResourceFailureException e) {
            log.error("cannot access elasticsearch index on {}:{}", this.elasticsearchHost, this.elasticsearchPort);
            throw new BrokenInfrastructureException();
        }
        return zaehlungen;
    }

    /**
     * Ermittelt alle Zaehlungen, die noch nicht freigegeben sind. Relevante Zählungen haben einen der
     * folgenden Status: Status.COUNTING, Status.CORRECTION,
     * Status.INSTRUCTED, Status.ACCOMPLISHED und Status.CREATED
     *
     * @return Liste mit allen relevanten Zählungen
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     */
    public List<OpenZaehlungDTO> getOpenZaehlungen() throws BrokenInfrastructureException {
        final List<OpenZaehlungDTO> zaehlungen = new ArrayList<>();
        final PageRequest pageable = PageRequest.of(0, 10000);
        try {
            final String[] status = {
                    Status.CREATED.name(),
                    Status.INSTRUCTED.name(),
                    Status.COUNTING.name(),
                    Status.ACCOMPLISHED.name(),
                    Status.CORRECTION.name()
            };
            final List<String> statusAsList = Arrays.asList(status);

            // ElasticSearch Syntax zum Suchen nach dem Status
            // zaehlungen.status:INSTRUCTED OR zaehlungen.status:CORRECTION OR zaehlungen.status:COUNTING
            // Wird anhand des Arrays status zusammengebaut
            final String or = " OR ";
            final String field = "zaehlungen.status:";
            final StringBuilder query = new StringBuilder();
            for (int index = 0; index < status.length;) {
                query.append(field);
                query.append(status[index]);
                index++;
                if (index < status.length) {
                    query.append(or);
                }
            }

            // Da eine Liste mit Zaehlstellen zurück kommt, müssen alle relevanten Zäehlungen
            // anhand des Status herausgesucht werden. Ist eine Zählung relevant, so werden die
            // benötigten Daten aus der Zählstelle in das Objekt kopiert.
            final List<ZaehlstelleEntity> allByStatus = this.zaehlstelleIndex.findAllByStatus(new String(query), pageable).toList();
            allByStatus.forEach(zaehlstelle -> {
                zaehlstelle.getZaehlungen().forEach(zaehlung -> {
                    if (statusAsList.contains(zaehlung.getStatus())) {
                        final OpenZaehlungDTO openZaehlungDTO = this.zaehlungMapper.bean2OpenZaehlungDto(zaehlung);
                        if (StringUtils.isNotEmpty(zaehlstelle.getNummer())) {
                            openZaehlungDTO.setZaehlstellenNummer(zaehlstelle.getNummer());
                        }
                        if (zaehlstelle.getId() != null) {
                            openZaehlungDTO.setZaehlstellenId(zaehlstelle.getId().toString());
                        }
                        if (StringUtils.isNotEmpty(zaehlstelle.getStadtbezirk())) {
                            openZaehlungDTO.setStadtbezirk(zaehlstelle.getStadtbezirk());
                        }
                        zaehlungen.add(openZaehlungDTO);
                    }
                });
            });
        } catch (final DataAccessResourceFailureException e) {
            log.error("cannot access elasticsearch index on {}:{}", this.elasticsearchHost, this.elasticsearchPort);
            throw new BrokenInfrastructureException();
        }

        return zaehlungen;
    }

    /**
     * Methode zum Aktualisieren einer Zählstelle.
     *
     * @param zaehlstelle zum Aktualisieren
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     */
    public void erneuereZaehlstelle(final ZaehlstelleEntity zaehlstelle) throws BrokenInfrastructureException {
        log.debug("erneuereZaehlstelle");
        this.updateZaehlstelleWithLetzteZaehlung(zaehlstelle);
        customSuggestIndexService.updateSuggestionsForZaehlstelle(zaehlstelle);
        this.speichereZaehlstelleInDatenbank(zaehlstelle);
    }

    /**
     * Methode zum aktualisieren des Status von INSTRUCTED nach COUNTING
     *
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     */
    public void updateStatusOfInstrucedZaehlungen() throws BrokenInfrastructureException {
        // Alle Zählstellen mit Zählungen im Status INSTRUCTED suchen
        final List<ZaehlstelleEntity> allByZaehlungenStatusInstructed = this.zaehlstelleIndex.findAllByZaehlungenStatus(Status.INSTRUCTED);
        for (final ZaehlstelleEntity zaehlstelle : allByZaehlungenStatusInstructed) {
            for (final ZaehlungEntity zaehlung : zaehlstelle.getZaehlungen()) {
                if (Objects.equals(zaehlung.getStatus(), Status.INSTRUCTED)) {
                    // Wenn Das Datum der Zählung <= LocalDate.now() ist, dann Status auf COUNTING ändern
                    if (zaehlung.getDatum().isBefore(LocalDate.now()) || zaehlung.getDatum().isEqual(LocalDate.now())) {
                        this.updateStatusOfZaehlung(zaehlung, Status.COUNTING, zaehlstelle);
                    }
                }
            }
        }
    }

    private void updateStatusOfZaehlung(final ZaehlungEntity zaehlung, final Status newStatus, final ZaehlstelleEntity zaehlstelle) throws BrokenInfrastructureException {
        zaehlung.setStatus(newStatus);
        this.erneuereZaehlung(zaehlung, zaehlstelle.getId());
        final UpdateStatusDTO updateStatusDTO = new UpdateStatusDTO();
        updateStatusDTO.setStatus(zaehlung.getStatus());
        this.messageService.saveUpdateMessageForZaehlungStatus(zaehlung.getId(), updateStatusDTO);
    }

    public List<LadeZaehlstelleWithUnreadMessageDTO> readZaehlstellenWithUnreadMessages() {
        final List<LadeZaehlstelleWithUnreadMessageDTO> zaehlstellen = readZaehlstellenWithUnreadMessages(Participant.MOBILITAETSREFERAT.getParticipantId());
        zaehlstellen.forEach(zaehlstelle -> {
            final var filteredZaehlungen = zaehlstelle.getZaehlungen()
                    .stream()
                    .filter(LadeZaehlungWithUnreadMessageDTO::getUnreadMessagesMobilitaetsreferat)
                    .collect(Collectors.toList());
            zaehlstelle.setZaehlungen(filteredZaehlungen);
        });
        return zaehlstellen;
    }

    /**
     * Sucht alle Zählstellen mit ungelesenen Nachrichten für einen bestimmten Participant und gibt
     * diese zurück
     *
     * @param participantId Participant, bei dem ungelesene Nachrichten vorliegen
     * @return LadeZaehlstelleWithUnreadMessageDTOs bei denen für einen bestimmten Participant
     *         ungelesene Nachrichten vorliegen
     */
    public List<LadeZaehlstelleWithUnreadMessageDTO> readZaehlstellenWithUnreadMessages(final int participantId) {
        final List<ZaehlstelleEntity> zaehlstellen;
        if (Participant.DIENSTLEISTER.getParticipantId() == participantId) {
            zaehlstellen = this.zaehlstelleIndex.findAllByZaehlungenUnreadMessagesDienstleisterTrue();
        } else {
            zaehlstellen = this.zaehlstelleIndex.findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue();
        }
        return zaehlstellen
                .stream()
                .map(zaehlstelleMapper::bean2LadeZaehlstelleWithUnreadMessageDTO)
                .toList();
    }

    public boolean existsActiveZaehlungWithDienstleisterkennung(final String kennung) throws BrokenInfrastructureException {
        return this.getZaehlungenForExternal(kennung, false).size() > 0;
    }

    public void updateStatusOfZaehlung(final UUID zaehlungId, final Status newStatus) throws BrokenInfrastructureException {
        final Optional<ZaehlstelleEntity> zaehlstelle = this.zaehlstelleIndex.findByZaehlungenId(zaehlungId);
        if (zaehlstelle.isPresent()) {
            for (final ZaehlungEntity zaehlung : zaehlstelle.get().getZaehlungen()) {
                if (zaehlung.getId().equals(zaehlungId)) {
                    this.updateStatusOfZaehlung(zaehlung, newStatus, zaehlstelle.get());
                }
            }
        }
    }
}
