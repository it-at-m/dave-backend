package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.detektor.StammdatenMapper;
import de.muenchen.dave.services.CustomSuggestIndexService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Die Klasse {@link StammdatenService} holt alle relevanten Messstellen aus MobidaM und
 * aktualisiert die in Dave gespeichereten Daten.
 */
@Slf4j
@Service
@AllArgsConstructor
public class StammdatenService {

    private final MessstelleIndexService messstelleIndexService;

    private final CustomSuggestIndexService customSuggestIndexService;

    private final StammdatenMapper stammdatenMapper;

    public void processingMessstellen(final List<Messstelle> messstellen) {
        log.debug("#processingMessstellen");
        // Daten aus Dave laden
        messstellen.forEach(messstelle -> {
            log.debug("#findById");
            messstelleIndexService.findByNummer(messstelle.getNummer()).ifPresentOrElse(found -> this.updateMessstelle(found, messstelle),
                    () -> this.createMessstelle(messstelle));
        });

    }

    protected void createMessstelle(final Messstelle newMessstelle) {
        log.info("#createMessstelle");
        newMessstelle.setId(UUID.randomUUID().toString());
        newMessstelle.getMessquerschnitte().forEach(messquerschnitt -> messquerschnitt.setId(UUID.randomUUID().toString()));
        customSuggestIndexService.createSuggestionsForMessstelle(newMessstelle);
        messstelleIndexService.saveMessstelle(newMessstelle);
    }

    protected void updateMessstelle(final Messstelle existingMessstelle, final Messstelle newMessstelle) {
        log.info("#updateMessstelle");
        final Messstelle updated = stammdatenMapper.updateMessstelle(existingMessstelle, newMessstelle);
        customSuggestIndexService.updateSuggestionsForMessstelle(updated);
        messstelleIndexService.saveMessstelle(updated);
    }
}
