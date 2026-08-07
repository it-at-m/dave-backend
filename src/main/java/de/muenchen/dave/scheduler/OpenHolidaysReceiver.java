package de.muenchen.dave.scheduler;

import de.muenchen.dave.configuration.LogExecutionTime;
import de.muenchen.dave.services.OpenHolidaysService;
import de.muenchen.dave.services.messstelle.MessstelleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Die Klasse {@link OpenHolidaysReceiver} holt alle relevanten Messstellen aus MobidaM und vergibt
 * diese dem {@link MessstelleService} zur weiteren
 * Verarbeitung. Soll nicht auf den externen Umgebungen laufen.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile({ "!konexternal && !prodexternal && !unittest" })
public class OpenHolidaysReceiver {

    private final OpenHolidaysService openHolidaysService;

    /**
     * Diese Methode lädt regelmäßig alle relevanten Messstellen aus MobidaM. Der Zyklus kann in der
     * application-xxx.yml mittels einer Property geändert
     * werden.
     */
    @Scheduled(cron = "${dave.holidays.cron}")
    @SchedulerLock(name = "loadHolidaysCron", lockAtMostFor = "${dave.holidays.shedlock}", lockAtLeastFor = "${dave.holidays.shedlock}")
    @Transactional
    @LogExecutionTime
    public void loadHolidaysCron() {
        // To assert that the lock is held (prevents misconfiguration errors)
        LockAssert.assertLocked();
        log.info("#loadHolidays from OpenHolidaysApi.org");
        try {
            openHolidaysService.createKalendertageForNextYear();
        } catch (final Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

}
