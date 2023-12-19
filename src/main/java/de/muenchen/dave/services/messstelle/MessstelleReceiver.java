package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.detektor.MessstelleCronMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.services.CustomSuggestIndexService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private MessstelleCronMapper messstelleMapper;

    /**
     * Diese Methode laedt regelmaessig alle relevanten Messstellen aus MobidaM. Wie oft das geschieht,
     * kann in der application-xxx.yml geändert werden.
     */
    @Scheduled(cron = "${dave.messstelle.cron}")
    @Transactional
    public void loadMessstellenCron() {
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
        final Messstelle newMessstelle = messstelleMapper.dtoToMessstelle(dto);
        newMessstelle.setId(UUID.randomUUID().toString());
        newMessstelle.getMessquerschnitte().forEach(messquerschnitt -> messquerschnitt.setId(UUID.randomUUID().toString()));
        customSuggestIndexService.createSuggestionsForMessstelle(newMessstelle);
        messstelleIndexService.saveMessstelle(newMessstelle);
    }

    private void updateMessstelleCron(final Messstelle existingMessstelle, final MessstelleDto dto) {
        log.info("#updateMessstelleCron");
        final Messstelle updated = messstelleMapper.updateMessstelle(existingMessstelle, dto);
        customSuggestIndexService.updateSuggestionsForMessstelle(updated);
        messstelleIndexService.saveMessstelle(updated);
    }
}
