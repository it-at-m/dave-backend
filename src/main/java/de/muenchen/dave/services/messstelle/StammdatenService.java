package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.detektor.StammdatenMapper;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Die Klasse {@link StammdatenService} holt alle relevanten Messstellen aus MobidaM und
 * aktualisiert
 * die in Dave gespeichereten Daten.
 */
@Slf4j
@Service
@AllArgsConstructor
public class StammdatenService {

    private final MessstelleIndex messstelleIndex;

    private final StammdatenMapper stammdatenMapper;

    public void processingMessstellen(final List<Messstelle> messstellen) {
        log.debug("#processingMessstellen");
        // Daten aus Dave laden
        messstellen.forEach(messstelle -> {
            log.debug("#findById");
            messstelleIndex.findByNummer(messstelle.getNummer()).ifPresentOrElse(found -> {
                // Daten aktualisieren
                log.debug("#updateMessstelle");
                final Messstelle updated = this.updateMessstelle(found, messstelle);
                // Daten speichern
                this.saveMessstelle(updated);
            }, () -> {
                // Messstelle neu anlegen
                messstelle.setId(UUID.randomUUID().toString());
                messstelle.getMessquerschnitte().forEach(messquerschnitt -> messquerschnitt.setId(UUID.randomUUID().toString()));
                this.saveMessstelle(messstelle);
            });
        });

    }

    protected Messstelle updateMessstelle(final Messstelle existingMessstelle, final Messstelle newMessstelle) {
        final Messstelle updated = stammdatenMapper.updateMessstelle(existingMessstelle, newMessstelle);
        log.debug("Aktualisierte Messstelle: " + updated);
        return updated;
    }

    protected void saveMessstelle(final Messstelle toSave) {
        messstelleIndex.save(toSave);
    }

    /**
     * Erzeugt für eine konkrete Zählstelle ein Set an Vorschlägen.
     *
     * @param messstelle neue zu verschlagwortende Messstelle
     */
    private void createSuggestionsForMessstelle(final Messstelle messstelle) {
        // TODO customSuchwoerter
        //        final String suggestId = messstelle.getId();
        // In den Suchwörtern stehen alle Suggestions für die Zählstelle
        //        if (CollectionUtils.isNotEmpty(messstelle.getSuchwoerter())) {
        // TODO steht aktuell im IndexService
        //            this.createSuggestionsFromSuchwoerter(zaehlstelle.getSuchwoerter(), suggestId);
        //        }
    }
}
