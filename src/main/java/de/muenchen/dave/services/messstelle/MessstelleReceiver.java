/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.services.CustomSuggestIndexService;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Context;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Die Klasse {@link MessstelleReceiver} holt alle relevanten Messstellen aus MobidaM und uerbgibt
 * diese dem {@link MessstelleService} zur weiteren
 * Verarbeitung. Soll nicht auf den externen Umgebungen laufen.
 */
@Slf4j
@Service
@AllArgsConstructor
@Profile({ "!konexternal && !prodexternal && !test" })
public class MessstelleReceiver {

    private MessstelleApi messstelleApi;

    private final MessstelleIndexService messstelleIndexService;

    private final CustomSuggestIndexService customSuggestIndexService;

    private MessstelleReceiverMapper messstelleReceiverMapper;

    private final StadtbezirkMapper stadtbezirkMapper;

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
        final Messstelle newMessstelle = messstelleReceiverMapper.createMessstelle(dto, stadtbezirkMapper);
        customSuggestIndexService.createSuggestionsForMessstelle(newMessstelle);
        messstelleIndexService.saveMessstelle(newMessstelle);
    }

    private void updateMessstelleCron(final Messstelle existingMessstelle, final MessstelleDto dto) {
        log.info("#updateMessstelleCron");
        final Messstelle updated = messstelleReceiverMapper.updateMessstelle(existingMessstelle, dto, stadtbezirkMapper);
        updated.setMessquerschnitte(updateMessquerschnitteOfMessstelle(updated.getMessquerschnitte(), dto.getMessquerschnitte()));
        customSuggestIndexService.updateSuggestionsForMessstelle(updated);
        messstelleIndexService.saveMessstelle(updated);
    }

    protected List<Messquerschnitt> updateMessquerschnitteOfMessstelle(final List<Messquerschnitt> messquerschnitte,
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
}
