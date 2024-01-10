/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.services.CustomSuggestIndexService;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Die Klasse {@link MessstelleReceiver} holt alle relevanten Messstellen aus MobidaM und uerbgibt
 * diese dem {@link MessstelleService} zur weiteren Verarbeitung.
 * Soll nicht auf den externen Umgebungen laufen.
 */
@Slf4j
@Service
@AllArgsConstructor
@Profile({ "!konexternal && !prodexternal" })
public class MessstelleReceiver {

    private MessstelleApi messstelleApi;

    private final MessstelleIndexService messstelleIndexService;

    private final CustomSuggestIndexService customSuggestIndexService;

    private MessstelleReceiverMapper messstelleReceiverMapper;

    /**
     * Diese Methode laedt regelmaessig alle relevanten Messstellen aus MobidaM. Wie oft das geschieht,
     * kann in der application-xxx.yml geändert werden.
     */
    @Scheduled(cron = "${dave.messstelle.cron}")
    @SchedulerLock(name = "loadMessstellenCron", lockAtMostFor = "${dave.messstelle.shedlock}", lockAtLeastFor = "${dave.messstelle.shedlock}")
    @Transactional
    public void loadMessstellenCron() {
        // To assert that the lock is held (prevents misconfiguration errors)
        LockAssert.assertLocked();
        log.info("#loadMessstellen from MobidaM");
        // Daten aus MobidaM laden
        final List<MessstelleDto> body = Objects.requireNonNull(messstelleApi.getMessstellenWithHttpInfo().block()).getBody();
        // Stammdatenservice aufrufen
        this.processingMessstellenCron(body);
    }

    private void processingMessstellenCron(final List<MessstelleDto> messstellen) {
        log.debug("#processingMessstellenCron");
        // Daten aus Dave laden
        messstellen.forEach(messstelleDto -> {
            log.debug("#findById");
            messstelleIndexService.findByMstId(messstelleDto.getMstId()).ifPresentOrElse(found -> this.updateMessstelleCron(found, messstelleDto),
                    () -> this.createMessstelleCron(messstelleDto));
        });
    }

    private void createMessstelleCron(final MessstelleDto dto) {
        log.info("#createMessstelleCron");
        final Messstelle newMessstelle = messstelleReceiverMapper.createMessstelle(dto);
        customSuggestIndexService.createSuggestionsForMessstelle(newMessstelle);
        messstelleIndexService.saveMessstelle(newMessstelle);
    }

    private void updateMessstelleCron(final Messstelle existingMessstelle, final MessstelleDto dto) {
        log.info("#updateMessstelleCron");
        final Messstelle updated = messstelleReceiverMapper.updateMessstelle(existingMessstelle, dto);
        updated.getMessquerschnitte().forEach(messquerschnitt -> {
            if (CollectionUtils.isNotEmpty(dto.getMessquerschnitte())) {
                dto.getMessquerschnitte().forEach(dto1 -> {
                    boolean doesNotExist = true;
                    if (messquerschnitt.getMqId().equalsIgnoreCase(dto1.getMqId())) {
                        messstelleReceiverMapper.updateMessquerschnitt(messquerschnitt, dto1);
                        doesNotExist = false;
                    }
                    if (doesNotExist) {
                        updated.getMessquerschnitte().add(messstelleReceiverMapper.createMessquerschnitte(dto1));
                    }

                });
            }
        });

        customSuggestIndexService.updateSuggestionsForMessstelle(updated);
        messstelleIndexService.saveMessstelle(updated);
    }
}
