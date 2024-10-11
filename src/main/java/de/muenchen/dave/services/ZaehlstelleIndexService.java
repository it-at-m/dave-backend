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
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Participant;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.ZaehlstelleMapper;
import de.muenchen.dave.domain.mapper.ZaehlungMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ZaehlstelleIndexService {

    private final ZaehlstelleIndex zaehlstelleIndex;
    private final CustomSuggestIndexService customSuggestIndexService;
    private final ZaehlungMapper zaehlungMapper;
    private final ZaehlstelleMapper zaehlstelleMapper;
    private final ZeitauswahlService zeitauswahlService;
    private final ChatMessageService messageService;
    private final StadtbezirkMapper stadtbezirkMapper;
    @Value(value = "${elasticsearch.host}")
    private String elasticsearchHost;

    @Value(value = "${elasticsearch.port}")
    private String elasticsearchPort;

    public ZaehlstelleIndexService(final ZeitauswahlService zeitauswahlService,
            final ZaehlstelleMapper zaehlstelleMapper,
            final CustomSuggestIndexService customSuggestIndexService,
            final ZaehlungMapper zaehlungMapper,
            final ZaehlstelleIndex zaehlstelleIndex,
            // @Lazy prevents circular dependency
            @Lazy final ChatMessageService messageService,
            final StadtbezirkMapper stadtbezirkMapper) {
        this.zeitauswahlService = zeitauswahlService;
        this.zaehlstelleMapper = zaehlstelleMapper;
        this.customSuggestIndexService = customSuggestIndexService;
        this.zaehlungMapper = zaehlungMapper;
        this.zaehlstelleIndex = zaehlstelleIndex;
        this.messageService = messageService;
        this.stadtbezirkMapper = stadtbezirkMapper;
    }

    public BackendIdDTO speichereZaehlstelle(final BearbeiteZaehlstelleDTO zaehlstelle) throws BrokenInfrastructureException, DataNotFoundException {
        final BackendIdDTO backendIdDto = new BackendIdDTO();
        if (StringUtils.isEmpty(zaehlstelle.getId())) {
            backendIdDto.setId(this.erstelleZaehlstelle(zaehlstelle));
        } else {
            backendIdDto.setId(this.erneuereZaehlstelle(zaehlstelle, zaehlstelle.getId()));
        }
        return backendIdDto;
    }

    /**
     * Zählstelle im Index speichern. Sollte Elasticsearch nicht erreichbar sein, dann wird eine
     * entsprechende Exception geworfen und eine Fehlermeldung
     * geloggt.
     *
     * @param zaehlstelle zu Speichernede Zaehlstelle
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     */
    public void speichereZaehlstelleInDatenbank(final Zaehlstelle zaehlstelle) throws BrokenInfrastructureException {
        try {
            final Zaehlstelle save = this.zaehlstelleIndex.save(zaehlstelle);
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
    public String erstelleZaehlstelle(final BearbeiteZaehlstelleDTO zdto) throws BrokenInfrastructureException {

        final Zaehlstelle zaehlstelle = this.zaehlstelleMapper.bearbeiteDto2bean(zdto, stadtbezirkMapper);
        zaehlstelle.setId(UUID.randomUUID().toString());
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
    public String erneuereZaehlstelle(final BearbeiteZaehlstelleDTO zdto, final String zaehlstelleId)
            throws BrokenInfrastructureException, DataNotFoundException {
        final Optional<Zaehlstelle> zsto = this.zaehlstelleIndex.findById(zaehlstelleId);
        if (zsto.isPresent()) {
            final Zaehlstelle zaehlstelle = this.zaehlstelleMapper.bearbeiteDto2bean(zdto, stadtbezirkMapper);
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
     * @param zdto DTO der Zaehlung
     * @param zaehlstelleId ID der Zählstelle
     * @return die {@link Zaehlung}#getId()
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     * @throws DataNotFoundException Bei Ladefehlern
     */
    public Zaehlung erstelleZaehlung(final BearbeiteZaehlungDTO zdto, final String zaehlstelleId) throws BrokenInfrastructureException, DataNotFoundException {
        final Zaehlung zaehlung = this.zaehlungMapper.bearbeiteDto2bean(zdto);
        // Set Zaehlung ID
        if (StringUtils.isEmpty(zaehlung.getId())) {
            zaehlung.setId(UUID.randomUUID().toString());
        }
        // Set Fahrbeziehung ID
        if (CollectionUtils.isNotEmpty(zaehlung.getFahrbeziehungen())) {
            zaehlung.getFahrbeziehungen().stream()
                    .filter(fahrbeziehung -> StringUtils.isEmpty(fahrbeziehung.getId()))
                    .forEach(fahrbeziehung -> fahrbeziehung.setId(UUID.randomUUID().toString()));
        }

        // Zählstelle erneuern
        final Optional<Zaehlstelle> zsto = this.zaehlstelleIndex.findById(zaehlstelleId);
        if (zsto.isPresent()) {
            final Zaehlstelle zaehlstelleUpdated = this.updateZaehlstelleWithZaehlung(zsto.get(), zaehlung);
            customSuggestIndexService.createSuggestionsForZaehlung(zaehlung);
            this.speichereZaehlstelleInDatenbank(zaehlstelleUpdated);
        } else {
            log.error("Keine Zählstelle zur id {} gefunden.", zaehlstelleId);
            throw new DataNotFoundException("Die Zählung konnte nicht angelegt werden, da die dazugehörige Zählstelle nicht gefunden wurde. ");
        }
        return zaehlung;
    }

    /**
     * Erneuert die {@link Zaehlung} welche an der {@link Zaehlstelle}
     * - identifiziert entsprechend des Parameters zaehlstelleId - angefügt ist.
     *
     * @param zdto zur Erneuerung in der {@link Zaehlstelle}.
     * @param zaehlstelleId zur Identifikation der {@link Zaehlstelle}.
     * @return aktualisierte Zaehlung
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     * @throws DataNotFoundException Bei Ladefehlern
     */
    public Zaehlung erneuereZaehlung(final BearbeiteZaehlungDTO zdto, final String zaehlstelleId) throws BrokenInfrastructureException, DataNotFoundException {
        final Zaehlung zaehlung = this.zaehlungMapper.bearbeiteDto2bean(zdto);
        // Set Zaehlung ID
        if (StringUtils.isEmpty(zaehlung.getId())) {
            zaehlung.setId(UUID.randomUUID().toString());
        }
        // Set Fahrbeziehung ID
        if (CollectionUtils.isNotEmpty(zaehlung.getFahrbeziehungen())) {
            zaehlung.getFahrbeziehungen().stream()
                    .filter(fahrbeziehung -> StringUtils.isEmpty(fahrbeziehung.getId()))
                    .forEach(fahrbeziehung -> fahrbeziehung.setId(UUID.randomUUID().toString()));
        }

        // Zählstelle erneuern
        final Optional<Zaehlstelle> zsto = this.zaehlstelleIndex.findById(zaehlstelleId);
        if (zsto.isPresent()) {
            final Zaehlstelle zst = zsto.get();
            for (int index = 0; index < zst.getZaehlungen().size(); index++) {
                // Ersetze bisherige Zählung durch erneuerte Zählung
                if (zst.getZaehlungen().get(index).getId().equals(zaehlung.getId())) {
                    zst.getZaehlungen().remove(index);
                    zst.getZaehlungen().add(zaehlung);
                    break;
                }
            }
            customSuggestIndexService.updateSuggestionsForZaehlung(zaehlung);
            this.speichereZaehlstelleInDatenbank(zst);
        } else {
            log.error("Keine Zählstelle zur id {} gefunden.", zaehlstelleId);
            throw new DataNotFoundException("Die Zählung konnte nicht angelegt werden, da die dazugehörige Zählstelle nicht gefunden wurde. ");
        }
        return zaehlung;
    }

    /**
     * Erneuert die {@link Zaehlung} welche an der {@link Zaehlstelle}
     * - identifiziert entsprechend des Parameters zaehlstelleId - angefügt ist.
     * <p>
     * Falls die zu erneuernde {@link Zaehlung} mehr Suchwörter beinhaltet als die bisherige
     * persistierte Zählung, so werden die neuen Suchwörter persistiert.
     *
     * @param zl zur Erneuerung in der {@link Zaehlstelle}.
     * @param zaehlstelleId zur Identifikation der {@link Zaehlstelle}.
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     */
    public void erneuereZaehlung(final Zaehlung zl, final String zaehlstelleId) throws BrokenInfrastructureException {
        final Optional<Zaehlstelle> zsto = this.zaehlstelleIndex.findById(zaehlstelleId);
        if (zsto.isPresent()) {
            final Zaehlstelle zst = zsto.get();
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
    public void setUnreadMessagesInZaehlungForParticipant(final String zaehlungId, final Participant participant, final Boolean status)
            throws BrokenInfrastructureException {
        final Optional<Zaehlstelle> zsto = this.zaehlstelleIndex.findByZaehlungenId(zaehlungId);
        if (zsto.isPresent()) {
            final Zaehlstelle zst = zsto.get();
            for (final Zaehlung zaehlung : zst.getZaehlungen()) {
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

    public boolean deleteZaehlung(final String zaehlungId) throws DataNotFoundException, BrokenInfrastructureException {
        boolean isDeleted = false;
        // Zaehlstelle anhand der Zaehlung laden
        final Optional<Zaehlstelle> byZaehlungenId = this.zaehlstelleIndex.findByZaehlungenId(zaehlungId);

        if (byZaehlungenId.isPresent()) {
            final Zaehlstelle zaehlstelle = byZaehlungenId.get();
            // Zu löschende Zählung entfernen
            isDeleted = zaehlstelle.getZaehlungen().removeIf(zaehlung -> zaehlung.getId().equalsIgnoreCase(zaehlungId));

            // Alle Vorschläge zur Zählung ebenfalls löschen
            customSuggestIndexService.deleteAllSuggestionsByFkid(zaehlungId);

            // Zählstelle speichern
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
     * @param zaehlstelle Zaehlstelle
     * @param zaehlung Zaehlung
     * @return geaenderte Zaehlstelle
     */
    public Zaehlstelle updateZaehlstelleWithZaehlung(final Zaehlstelle zaehlstelle, final Zaehlung zaehlung) {
        log.warn("Aktualisiere Zählstelle {} mit Zählung {}", zaehlstelle.getId(), zaehlung.getDatum());
        zaehlstelle.getZaehlungen().add(zaehlung);
        if (zaehlung.getStatus().equalsIgnoreCase(Status.ACTIVE.name())) {
            this.updateZaehlstelleWithLetzteZaehlung(zaehlstelle);
        }
        return zaehlstelle;
    }

    /**
     * Setzt die Felder der Zählstelle, die sich auf die zuletzt durchgeführte Zählung beziehen.
     *
     * @param zaehlstelle
     */
    private void updateZaehlstelleWithLetzteZaehlung(final Zaehlstelle zaehlstelle) {
        final Zaehlung letzteZaehlung = IndexServiceUtils.getLetzteZaehlung(zaehlstelle.getZaehlungen());
        if (letzteZaehlung != null) {
            zaehlstelle.setLetzteZaehlungMonat(letzteZaehlung.getMonat());
            zaehlstelle.setLetzteZaehlungMonatNummer(letzteZaehlung.getDatum().getMonthValue());
            zaehlstelle.setGrundLetzteZaehlung(letzteZaehlung.getZaehlsituation());
            zaehlstelle.setLetzteZaehlungJahr(Integer.parseInt(letzteZaehlung.getJahr()));
        }
    }

    public List<Zaehlstelle> getAllZaehlstellen() {
        return IterableUtils.toList(
                this.zaehlstelleIndex.findAll());
    }

    public Zaehlstelle getZaehlstelle(final String id) throws DataNotFoundException {
        log.debug("Zugriff auf #getZaehlstelle");
        return this.zaehlstelleIndex.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Die gesuchte Zählstelle wurde nicht gefunden."));
    }

    public Zaehlstelle getZaehlstelleByZaehlungId(final String id) throws DataNotFoundException {
        return this.zaehlstelleIndex.findByZaehlungenId(id)
                .orElseThrow(() -> new DataNotFoundException("Die gesuchte Zählstelle wurde nicht gefunden."));
    }

    private Zaehlung ladeZaehlung(final String zaehlungId) throws DataNotFoundException {
        final Zaehlstelle zaehlstelle = this.zaehlstelleIndex.findByZaehlungenId(zaehlungId)
                .orElseThrow(() -> new DataNotFoundException("Zaehlstelle not found"));
        return zaehlstelle.getZaehlungen().stream()
                .filter(zaehlungToCheck -> zaehlungToCheck.getId().equals(zaehlungId))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Zaehlung not found"));
    }

    private Zaehlung ladeZaehlung(final String zaehlstellenNummer,
            final Zaehlart zaehlart,
            final LocalDate zaehldatum) throws DataNotFoundException {
        final Zaehlstelle zaehlstelle = this.zaehlstelleIndex.findByNummer(zaehlstellenNummer)
                .orElseThrow(() -> new DataNotFoundException("Zaehlstelle not found"));
        return zaehlstelle.getZaehlungen().stream()
                .filter(zaehlung -> zaehlung.getZaehlart().equals(zaehlart.toString())
                        && zaehlung.getDatum().isEqual(zaehldatum))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Zaehlung not found"));
    }

    public Zaehlung getZaehlung(final String zaehlungId) throws DataNotFoundException {
        return this.ladeZaehlung(zaehlungId);
    }

    public Zaehlung getZaehlung(final String zaehlstellenNummer,
            final Zaehlart zaehlart,
            final LocalDate zaehldatum) throws DataNotFoundException {
        return this.ladeZaehlung(zaehlstellenNummer, zaehlart, zaehldatum);
    }

    public LadeZaehlungDTO getLadeZaehlung(final String zaehlungId) throws DataNotFoundException {
        final LadeZaehlungDTO ladeZaehlungDTO = this.zaehlungMapper.bean2LadeDto(this.ladeZaehlung(zaehlungId));
        // Ermittlung möglicher Zeitauswahl
        ladeZaehlungDTO.setZeitauswahl(
                this.zeitauswahlService.determinePossibleZeitauswahl(
                        ladeZaehlungDTO.getZaehldauer(),
                        ladeZaehlungDTO.getId(),
                        ladeZaehlungDTO.getSonderzaehlung()));
        return ladeZaehlungDTO;
    }

    @Cacheable(value = CachingConfiguration.READ_ZAEHLSTELLE_DTO, key = "{#p0, #p1}")
    public LeseZaehlstelleDTO readZaehlstelleDTO(final String zaehlstelleId, final boolean isFachadmin) throws DataNotFoundException {
        log.debug("Zugriff auf #readZaehlstelleDTO");
        final LeseZaehlstelleDTO leseZaehlstelleDTO = this.zaehlstelleMapper.bean2LeseZaehlstelleDto(this.getZaehlstelle(zaehlstelleId));
        // Alle Zaehlung mit einem Status != ACTIVE werden ausgefilter (wenn kein Fachadmin angemeldet ist)
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
                        .collect(Collectors.toList()));

        // Die Zeitauswahl wird hier in die Zählungen geschrieben
        leseZaehlstelleDTO.getZaehlungen().forEach(leseZaehlungDTO -> leseZaehlungDTO.setZeitauswahl(
                this.zeitauswahlService.determinePossibleZeitauswahl(
                        leseZaehlungDTO.getZaehldauer(),
                        leseZaehlungDTO.getId(),
                        leseZaehlungDTO.getSonderzaehlung())));
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
     * @param stadtbezirksnummer Nummer des Stadtbezirks der Zaehlstelle
     * @return naechste Zaehlstellennummer
     */
    public NextZaehlstellennummerDTO getNextZaehlstellennummer(final String partOfzaehlstelleId, final Integer stadtbezirksnummer) {
        final List<Zaehlstelle> zaehlstellen = this.zaehlstelleIndex.findAllByNummerStartsWithAndStadtbezirkNummer(partOfzaehlstelleId, stadtbezirksnummer);
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
            final List<Zaehlstelle> allByExternalStatus = this.zaehlstelleIndex.findAllByStatus(new String(query), pageable).toList();
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
                        if (ObjectUtils.isNotEmpty(zaehlstelle.getPunkt()) && zaehlstelle.getPunkt().getLat() > 0 && zaehlstelle.getPunkt().getLon() > 0) {
                            zaehlung.setZaehlstellePunkt(zaehlstelle.getPunkt());
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
            final List<Zaehlstelle> allByStatus = this.zaehlstelleIndex.findAllByStatus(new String(query), pageable).toList();
            allByStatus.forEach(zaehlstelle -> {
                zaehlstelle.getZaehlungen().forEach(zaehlung -> {
                    if (statusAsList.contains(zaehlung.getStatus())) {
                        final OpenZaehlungDTO openZaehlungDTO = this.zaehlungMapper.bean2OpenZaehlungDto(zaehlung);
                        if (StringUtils.isNotEmpty(zaehlstelle.getNummer())) {
                            openZaehlungDTO.setZaehlstellenNummer(zaehlstelle.getNummer());
                        }
                        if (StringUtils.isNotEmpty(zaehlstelle.getId())) {
                            openZaehlungDTO.setZaehlstellenId(zaehlstelle.getId());
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
     * Methode zum aktualiseren einer Zählstelle.
     *
     * @param zaehlstelle zu aktualisierende
     * @throws BrokenInfrastructureException Bei Fehler in Verbindung mit ElasticSearch
     */
    public void erneuereZaehlstelle(final Zaehlstelle zaehlstelle) throws BrokenInfrastructureException {
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
        final List<Zaehlstelle> allByZaehlungenStatusInstructed = this.zaehlstelleIndex.findAllByZaehlungenStatus(Status.INSTRUCTED.name());
        for (final Zaehlstelle zaehlstelle : allByZaehlungenStatusInstructed) {
            for (final Zaehlung zaehlung : zaehlstelle.getZaehlungen()) {
                if (zaehlung.getStatus().equalsIgnoreCase(Status.INSTRUCTED.name())) {
                    // Wenn Das Datum der Zählung <= LocalDate.now() ist, dann Status auf COUNTING ändern
                    if (zaehlung.getDatum().isBefore(LocalDate.now()) || zaehlung.getDatum().isEqual(LocalDate.now())) {
                        this.updateStatusOfZaehlung(zaehlung, Status.COUNTING, zaehlstelle);
                    }
                }
            }
        }
    }

    private void updateStatusOfZaehlung(final Zaehlung zaehlung, final Status newStatus, final Zaehlstelle zaehlstelle) throws BrokenInfrastructureException {
        zaehlung.setStatus(newStatus.name());
        this.erneuereZaehlung(zaehlung, zaehlstelle.getId());
        final UpdateStatusDTO updateStatusDTO = new UpdateStatusDTO();
        updateStatusDTO.setStatus(zaehlung.getStatus());
        this.messageService.saveUpdateMessageForZaehlungStatus(zaehlung.getId(), updateStatusDTO);
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
        final List<Zaehlstelle> zaehlstellen;
        if (Participant.DIENSTLEISTER.getParticipantId() == participantId) {
            zaehlstellen = this.zaehlstelleIndex.findAllByZaehlungenUnreadMessagesDienstleisterTrue();
        } else {
            zaehlstellen = this.zaehlstelleIndex.findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue();
        }
        final List<LadeZaehlstelleWithUnreadMessageDTO> leseZaehlstelleDTOS = new ArrayList<>();
        zaehlstellen
                .forEach(zaehlstelle -> leseZaehlstelleDTOS.add(this.zaehlstelleMapper.bean2LadeZaehlstelleWithUnreadMessageDTO(zaehlstelle)));

        return leseZaehlstelleDTOS;
    }

    public boolean existsActiveZaehlungWithDienstleisterkennung(final String kennung) throws BrokenInfrastructureException {
        return this.getZaehlungenForExternal(kennung, false).size() > 0;
    }

    public void updateStatusOfZaehlung(final String zaehlungId, final Status newStatus) throws BrokenInfrastructureException {
        final Optional<Zaehlstelle> zaehlstelle = this.zaehlstelleIndex.findByZaehlungenId(zaehlungId);
        if (zaehlstelle.isPresent()) {
            for (final Zaehlung zaehlung : zaehlstelle.get().getZaehlungen()) {
                if (zaehlung.getId().equalsIgnoreCase(zaehlungId)) {
                    this.updateStatusOfZaehlung(zaehlung, newStatus, zaehlstelle.get());
                }
            }
        }
    }
}
