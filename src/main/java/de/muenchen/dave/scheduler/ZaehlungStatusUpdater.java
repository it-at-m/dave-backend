package de.muenchen.dave.scheduler;

import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.services.persist.InternalZaehlungPersistierungsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
public class ZaehlungStatusUpdater {

    private final InternalZaehlungPersistierungsService internalZaehlungPersistierungsService;

    public ZaehlungStatusUpdater(final InternalZaehlungPersistierungsService internalZaehlungPersistierungsService) {
        this.internalZaehlungPersistierungsService = internalZaehlungPersistierungsService;
    }

    @Scheduled(cron = "${dave.zaehlung.status.updater}", zone = "Europe/Berlin")
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
