package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

/**
 * Die Klasse {@link MessstelleIndexService} holt alle relevanten Messstellen aus MobidaM und
 * aktualisiert die in Dave gespeichereten Daten.
 */
@Slf4j
@Service
@AllArgsConstructor
public class MessstelleIndexService {

    private final MessstelleIndex messstelleIndex;

    /**
     * Speichert die übergebene Messstelle, nachdem alle Messquerschnitte
     * ohne Detektoren entfernt wurden.
     * Es werden nur Messquerschnitte mit AnzahlDetektoren > 0 beibehalten.
     *
     * @param toSave die zu speichernde Messstelle; ihre Messquerschnitte werden ggf. modifiziert
     * @return die gespeicherte Messstelle
     */
    public Messstelle saveMessstelleWithDetectors(final Messstelle toSave) {
        final var messquerschnitteWithDetectors = CollectionUtils.emptyIfNull(toSave.getMessquerschnitte())
                .stream()
                .filter(messquerschnitt -> ObjectUtils.getIfNull(messquerschnitt.getAnzahlDetektoren(), 0) > 0)
                .toList();
        toSave.setMessquerschnitte(messquerschnitteWithDetectors);
        return saveMessstelle(toSave);
    }

    public Messstelle saveMessstelle(final Messstelle toSave) {
        log.info("#saveMessstelle");
        return messstelleIndex.save(toSave);
    }

    public Optional<Messstelle> findByMstId(final String messstellenNummer) {
        return messstelleIndex.findByMstId(messstellenNummer);
    }

    public Messstelle findByMstIdOrThrowException(final String messstellenNummer) {
        log.debug("Zugriff auf #findByMstIdOrThrowException");
        return this.messstelleIndex.findByMstId(messstellenNummer)
                .orElseThrow(() -> new ResourceNotFoundException("Die gesuchte Messstelle wurde nicht gefunden."));
    }

    public Messstelle findByIdOrThrowException(final String messstelleId) {
        log.debug("Zugriff auf #findByIdOrThrowException");
        return this.messstelleIndex.findById(messstelleId)
                .orElseThrow(() -> new ResourceNotFoundException("Die gesuchte Messstelle wurde nicht gefunden."));
    }

    public List<Messstelle> findAllMessstellen() {
        return messstelleIndex.findAll();
    }

    public List<Messstelle> findAllVisibleMessstellen() {
        return messstelleIndex.findAllBySichtbarDatenportalIsTrue();
    }

}
