package de.muenchen.dave.scheduler;

import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.services.persist.InternalZaehlungPersistierungsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@AllArgsConstructor
public class ZaehlungStatusUpdater {

    private final InternalZaehlungPersistierungsService internalZaehlungPersistierungsService;

    @Scheduled(cron = "${dave.zaehlung.status.updater}")
    public void updateStatus() {
        log.debug("Scheduler ZaehlungStatusUpdater start");
        try {
            internalZaehlungPersistierungsService.updateStatusOfInstrucedZaehlungen();
        } catch (BrokenInfrastructureException bie) {
            log.error("Die Aktualisierung der beauftragten Zählungen ist fehlgeschlagen.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.debug("Scheduler ZaehlungStatusUpdater end");
    }

}
