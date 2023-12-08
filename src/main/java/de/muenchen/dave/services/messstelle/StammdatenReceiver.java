package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.detektor.StammdatenMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Die Klasse {@link StammdatenReceiver} holt alle relevanten Messstellen aus MobidaM
 * und uerbgibt diese dem {@link StammdatenService} zur weiteren Verarbeitung.
 */
@Slf4j
@Service
@AllArgsConstructor
public class StammdatenReceiver {

    private MessstelleApi messstelleApi;

    private StammdatenService stammdatenService;

    private StammdatenMapper stammdatenMapper;

    /**
     * Diese Methode laedt regelmaessig alle relevanten Messstellen aus MobidaM.
     * Wie oft das geschieht, kann in der application.yml geändert werden.
     */
    @Scheduled(cron = "${dave.messstelle.cron}")
    @Transactional
    public void loadMessstellen() {
        log.info("#loadMessstellen from MobidaM");
        // Daten aus MobidaM laden
        final List<MessstelleDto> body = Objects.requireNonNull(messstelleApi.getMessstellenWithHttpInfo().block()).getBody();
        // Daten auf Dave-Struktur mappen
        final List<Messstelle> messstellen = stammdatenMapper.dtoToMessstelle(body);
        // Stammdatenservice aufrufen
        stammdatenService.processingMessstellen(messstellen);
    }
}
