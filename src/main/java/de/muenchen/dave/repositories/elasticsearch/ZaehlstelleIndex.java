package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ZaehlstelleIndex {

    Zaehlung initializeZaehlung(Zaehlung zaehlung, String zaehlstelleId);

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    void deleteAll();

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    void deleteAll(Iterable<? extends Zaehlstelle> var1);

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    void deleteById(String var1);

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    void delete(Zaehlstelle var1);

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    Zaehlstelle save(Zaehlstelle var1);

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    Iterable<Zaehlstelle> saveAll(Iterable<Zaehlstelle> var1);

    Optional<Zaehlstelle> findById(String var1);

    Page<Zaehlstelle> suggestSearch(String query, Pageable pageable);

    Page<Zaehlstelle> findAllByStatus(String query, Pageable pageable);

    List<Zaehlstelle> findAll();

    Optional<Zaehlstelle> findByZaehlungenId(String id);

    List<Zaehlstelle> findAllByNummerStartsWithAndStadtbezirkNummer(String nummer, Integer stadtbezirksnummer);

    Optional<Zaehlstelle> findByNummer(String nummer);

    List<Zaehlstelle> findAllByZaehlungenStatus(String status);

    List<Zaehlstelle> findAllByZaehlungenJahr(String jahr);

    List<Zaehlstelle> findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue();

    List<Zaehlstelle> findAllByZaehlungenUnreadMessagesDienstleisterTrue();

}
