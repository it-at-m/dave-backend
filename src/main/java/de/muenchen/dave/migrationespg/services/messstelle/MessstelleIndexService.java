package de.muenchen.dave.migrationespg.services.messstelle;

import de.muenchen.dave.migrationespg.domain.relational.messtellen.MessstelleEntity;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import de.muenchen.dave.migrationespg.repositories.relationaldb.MessstellenIndexRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    private final MessstellenIndexRepository messstelleIndex;

    /**
     * Speichert die übergebene M
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     * essstelle, nachdem alle Messquerschnitte
     * ohne Detektoren entfernt wurden.
     * Es werden nur Messquerschnitte mit AnzahlDetektoren > 0 beibehalten.
     *
     * @param toSave die zu speichernde Messstelle; ihre Messquerschnitte werden ggf. modifiziert
     * @return die gespeicherte Messstelle
     */
    public MessstelleEntity saveMessstelleWithDetectors(final MessstelleEntity toSave) {
        final var messquerschnitteWithDetectors = CollectionUtils.emptyIfNull(toSave.getMessquerschnitte())
                .stream()
                .filter(messquerschnitt -> ObjectUtils.getIfNull(messquerschnitt.getAnzahlDetektoren(), 0) > 0)
                .toList();
        toSave.setMessquerschnitte(messquerschnitteWithDetectors);
        return saveMessstelle(toSave);
    }

    public MessstelleEntity saveMessstelle(final MessstelleEntity toSave) {
        log.info("#saveMessstelle");
        return messstelleIndex.save(toSave);
    }

    public Optional<MessstelleEntity> findByMstId(final String messstellenNummer) {
        return messstelleIndex.findByMstId(messstellenNummer);
    }

    public MessstelleEntity findByMstIdOrThrowException(final String messstellenNummer) {
        log.debug("Zugriff auf #findByMstIdOrThrowException");
        return this.messstelleIndex.findByMstId(messstellenNummer)
                .orElseThrow(() -> new ResourceNotFoundException("Die gesuchte Messstelle wurde nicht gefunden."));
    }

    public MessstelleEntity findByIdOrThrowException(final String messstelleId) {
        log.debug("Zugriff auf #findByIdOrThrowException");
        return this.messstelleIndex.findById(UUID.fromString(messstelleId))
                .orElseThrow(() -> new ResourceNotFoundException("Die gesuchte Messstelle wurde nicht gefunden."));
    }

    public List<MessstelleEntity> findAllMessstellen() {
        return messstelleIndex.findAll();
    }

    public List<MessstelleEntity> findAllVisibleMessstellen() {
        return messstelleIndex.findAllBySichtbarDatenportalIsTrue();
    }

}
