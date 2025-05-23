package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.configuration.LogExecutionTime;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapper;
import de.muenchen.dave.domain.model.MessstelleChangeMessage;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.services.CustomSuggestIndexService;
import de.muenchen.dave.services.email.EmailSendService;
import de.muenchen.dave.services.lageplan.LageplanService;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Die Klasse {@link MessstelleReceiver} holt alle relevanten Messstellen aus MobidaM und vergibt
 * diese dem {@link MessstelleService} zur weiteren Verarbeitung.
 * Soll nicht auf den externen Umgebungen laufen.
 */
@Slf4j
@Service
@AllArgsConstructor
@Profile({ "!konexternal && !prodexternal && !unittest" })
public class MessstelleReceiver {

    private final MessstelleIndexService messstelleIndexService;
    private final CustomSuggestIndexService customSuggestIndexService;
    private final StadtbezirkMapper stadtbezirkMapper;
    private final LageplanService lageplanService;
    private final EmailSendService emailSendService;
    private MessstelleApi messstelleApi;
    private MessstelleReceiverMapper messstelleReceiverMapper;
    private UnauffaelligeTageService unauffaelligeTageService;

    /**
     * Diese Methode lädt regelmäßig alle relevanten Messstellen aus MobidaM.
     * Der Zyklus kann in der application-xxx.yml mittels einer Property geändert werden.
     */
    @Scheduled(cron = "${dave.messstelle.cron}")
    @SchedulerLock(name = "loadMessstellenCron", lockAtMostFor = "${dave.messstelle.shedlock}", lockAtLeastFor = "${dave.messstelle.shedlock}")
    @Transactional
    @LogExecutionTime
    public void loadMessstellenCron() {
        // To assert that the lock is held (prevents misconfiguration errors)
        LockAssert.assertLocked();
        log.info("#loadMessstellen from MobidaM");
        try {
            // Daten aus MobidaM laden
            final List<MessstelleDto> body = loadMessstellen();
            // Stammdatenservice aufrufen
            this.processingMessstellen(body);
        } catch (final Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    @LogExecutionTime
    protected List<MessstelleDto> loadMessstellen() {
        return Objects.requireNonNull(messstelleApi.getMessstellenWithHttpInfo().block()).getBody();
    }

    protected void processingMessstellen(final List<MessstelleDto> messstellen) {
        log.debug("#processingMessstellenCron");
        // Daten aus Dave laden
        messstellen.parallelStream().forEach(messstelleDto -> {
            log.debug("#findById");
            messstelleIndexService.findByMstId(messstelleDto.getMstId())
                    .ifPresentOrElse(
                            found -> this.updateMessstelle(found, messstelleDto),
                            () -> this.createMessstelle(messstelleDto));
        });
    }

    /**
     * Die Methode legt für die im Parameter gegebenen Messstelle eine neuen Messstelle an.
     * Nach erfolgreichem Anlegen wird eine Infomail bezüglich der neuen Messstelle versandt.
     *
     * @param dto für Messstelle zum anlegen.
     */
    protected void createMessstelle(final MessstelleDto dto) {
        log.info("#createMessstelleCron");
        Messstelle newMessstelle = messstelleReceiverMapper.createMessstelle(dto, stadtbezirkMapper);
        customSuggestIndexService.createSuggestionsForMessstelle(newMessstelle);
        newMessstelle = messstelleIndexService.saveMessstelle(newMessstelle);
        this.sendMailForUpdatedOrChangedMessstelle(
                newMessstelle.getId(),
                newMessstelle.getMstId(),
                null, // neue Messstellen besitzen keinen alten Status
                newMessstelle.getStatus());
    }

    /**
     * Die Methode aktualisiert eine bereits gespeicherte Messstelle.
     * Nach erfolgreichen Anlegen und der Feststellung einer Statusänderung
     * wird eine Infomail bezüglich der Aktualisierung versandt.
     *
     * @param existingMessstelle als bereits gespeicherte Messstelle.
     * @param dto der Messstelle mit den zu aktualisierenden Daten.
     */
    protected void updateMessstelle(final Messstelle existingMessstelle, final MessstelleDto dto) {
        log.info("#updateMessstelleCron");
        final var statusMessstelleAlt = existingMessstelle.getStatus();
        final Messstelle toSave = messstelleReceiverMapper.updateMessstelle(existingMessstelle, dto, stadtbezirkMapper);
        try {
            toSave.setLageplanVorhanden(lageplanService.lageplanVorhanden(toSave.getMstId()));
        } catch (final Exception exception) {
            log.error("Für die Messstelle {} konnte kein Lageplan ermittelt werden.", toSave.getMstId());
        }
        final var updatedMessquerschnitte = updateMessquerschnitteOfMessstelle(toSave.getMessquerschnitte(), dto.getMessquerschnitte());
        toSave.setMessquerschnitte(updatedMessquerschnitte);
        customSuggestIndexService.updateSuggestionsForMessstelle(toSave);
        if (ObjectUtils.isEmpty(dto.getDatumLetztePlausibleMessung())) {
            unauffaelligeTageService.findFirstByMstIdOrderByKalendertagDatumDesc(toSave.getMstId())
                    .ifPresent(unauffaelligerTag -> toSave.setDatumLetztePlausibleMessung(unauffaelligerTag.getKalendertag().getDatum()));
        }
        final Messstelle updated = messstelleIndexService.saveMessstelle(toSave);
        final var statusMessstelleNeu = updated.getStatus();
        if (statusMessstelleAlt != statusMessstelleNeu) {
            this.sendMailForUpdatedOrChangedMessstelle(
                    updated.getId(),
                    updated.getMstId(),
                    statusMessstelleAlt,
                    statusMessstelleNeu);
        }
    }

    protected List<Messquerschnitt> updateMessquerschnitteOfMessstelle(
            final List<Messquerschnitt> messquerschnitte,
            final List<MessquerschnittDto> messquerschnitteDto) {
        if (CollectionUtils.isNotEmpty(messquerschnitteDto)) {
            messquerschnitteDto.forEach(messquerschnittDto -> {
                final AtomicBoolean messquerschnittDtoDoesNotExist = new AtomicBoolean(true);
                messquerschnitte.forEach(messquerschnitt -> {
                    if (messquerschnitt.getMqId().equalsIgnoreCase(messquerschnittDto.getMqId())) {
                        messstelleReceiverMapper.updateMessquerschnitt(messquerschnitt, messquerschnittDto, stadtbezirkMapper);
                        messquerschnittDtoDoesNotExist.set(false);
                    }
                });
                if (messquerschnittDtoDoesNotExist.get()) {
                    messquerschnitte.add(messstelleReceiverMapper.createMessquerschnitt(messquerschnittDto));
                }
            });
        }
        return messquerschnitte;
    }

    /**
     * Versendet eine Email mit den in den Parametern gegebenen Informationen.
     * Tritt beim Emailversand ein Fehler auf, so wird dieser Fehler geloggt.
     *
     * @param id als technische ID der Messstelle.
     * @param mstId als fachliche ID der Messstelle.
     * @param statusAlt als Status der Messstelle vor der Aktualisierung.
     * @param statusNeu als Status der Messstelle nach der Aktualisierung.
     */
    protected void sendMailForUpdatedOrChangedMessstelle(
            final String id,
            final String mstId,
            final MessstelleStatus statusAlt,
            final MessstelleStatus statusNeu) {
        final var messstelleChangeMessage = new MessstelleChangeMessage();
        messstelleChangeMessage.setTechnicalIdMst(id);
        messstelleChangeMessage.setMstId(mstId);
        messstelleChangeMessage.setStatusAlt(statusAlt);
        messstelleChangeMessage.setStatusNeu(statusNeu);
        try {
            emailSendService.sendMailForMessstelleChangeMessage(messstelleChangeMessage);
        } catch (final Exception exception) {
            log.error("Der Emailversand ist fehlgeschlagen.", exception);
        }
    }
}
