package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.geodateneai.gen.api.MessstelleApi;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Die Klasse {@link StammdatenReceiveService} holt alle relevanten Messstellen aus MobidaM
 * und aktulisiert die in Dave gespeichereten Daten.
 */
@Slf4j
@Service
@AllArgsConstructor
public class StammdatenReceiveService {

    private MessstelleApi messstelleApi;

    /**
     * Diese Methode verbindet sich mit dem EMail-Postfach und verarbeitet alle neuen E-Mails.
     * Wie oft gecheckt wird, kann in der application.yml geändert werden.
     * Falls keine E-Mail Adresse oder die dummy-Email-Adresse konfiguriert ist, wird nichts getan.
     */
    @Scheduled(cron = "${dave.messstelle.cron}")
    public void loadMessstellen() {
        // Daten aus MobidaM laden
        final List<MessstelleDto> body = messstelleApi.getMessstellenWithHttpInfo().block().getBody();
        // Daten aus Dave laden
        body.forEach(messstelleDto -> {
            messstelleDto.getMstId();
        });
        // Daten aktualisieren

        // Daten speichern
    }
}
