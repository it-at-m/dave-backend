package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.detektor.MessstelleCronMapper;
import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.services.CustomSuggestIndexService;
import java.time.LocalDate;
import java.util.List;
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
//        final List<MessstelleDto> body = Objects.requireNonNull(messstelleApi.getMessstellenWithHttpInfo().block()).getBody(); TODO: reintegrate
        // TODO: remove
        MessstelleDto dto1 = new MessstelleDto();
        dto1.setMstId("4203");
        dto1.setName("MST 4203");
        dto1.setStatus("in Betrieb");
        dto1.setRealisierungsdatum(LocalDate.parse("2019-01-01"));
        dto1.setStadtbezirkNummer(1);
        dto1.setBemerkung("Bemerkung");
        dto1.setDatumLetztePlausibleMeldung(LocalDate.parse("2019-01-01"));
        dto1.setXcoordinate(48.1571117572882);
        dto1.setYcoordinate(11.46706127145418);
        MessquerschnittDto mqDto1 = new MessquerschnittDto();
        mqDto1.setMqId("42031");
        mqDto1.setMstId("4203");
        MessquerschnittDto mqDto2 = new MessquerschnittDto();
        mqDto2.setMqId("42032");
        mqDto2.setMstId("4203");
        dto1.setMessquerschnitte(List.of(mqDto1, mqDto2));

        // Stammdatenservice aufrufen
        this.processingMessstellenCron(List.of(dto1));
    }

    private void processingMessstellenCron(final List<MessstelleDto> messstellen) {
        log.debug("#processingMessstellenCron");
        // Daten aus Dave laden
        messstellen.forEach(messstelleDto -> {
            log.debug("#findById");
            messstelleIndexService.findByNummer(messstelleDto.getMstId()).ifPresentOrElse(found -> this.updateMessstelleCron(found, messstelleDto),
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
