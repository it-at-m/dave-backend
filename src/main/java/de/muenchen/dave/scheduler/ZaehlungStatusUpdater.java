package de.muenchen.dave.scheduler;

import de.muenchen.dave.configuration.LogExecutionTime;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.services.persist.InternalZaehlungPersistierungsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({ "!konexternal && !prodexternal && !unittest" })
public class ZaehlungStatusUpdater {

    private final InternalZaehlungPersistierungsService internalZaehlungPersistierungsService;

    @Scheduled(cron = "${dave.zaehlung.status-updater.cron}")
    @SchedulerLock(
            name = "ZaehlungStatusUpdater", lockAtMostFor = "${dave.zaehlung.status-updater.shedlock}",
            lockAtLeastFor = "${dave.zaehlung.status-updater.shedlock}"
    )
    @Transactional
    @LogExecutionTime
    public void updateStatus() {
        // To assert that the lock is held (prevents misconfiguration errors)
        LockAssert.assertLocked();
        log.info("Scheduler ZaehlungStatusUpdater start");
        try {
            internalZaehlungPersistierungsService.updateStatusOfInstrucedZaehlungen();
        } catch (BrokenInfrastructureException bie) {
            log.error("Die Aktualisierung der beauftragten Zählungen ist fehlgeschlagen.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("Scheduler ZaehlungStatusUpdater end");
    }

}
