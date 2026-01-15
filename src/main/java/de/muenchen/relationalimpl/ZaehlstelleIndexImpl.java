package de.muenchen.relationalimpl;

import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.mapper.FahrbeziehungMapper;
import de.muenchen.dave.domain.mapper.ZaehlstelleMapper;
import de.muenchen.dave.domain.mapper.ZaehlungMapper;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.ZaehlstelleRepository;
import de.muenchen.dave.repositories.relationaldb.ZaehlungRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ZaehlstelleIndexImpl implements ZaehlstelleIndex {

    private final ZaehlstelleRepository zaehlstelleRepository;

    private final ZaehlungRepository zaehlungRepository;

    private final ZaehlstelleMapper zaehlstelleMapper;

    private final ZaehlungMapper zaehlungMapper;

    private final FahrbeziehungMapper fahrbeziehungMapper;

    public ZaehlstelleIndexImpl(final ZaehlstelleRepository zaehlstelleRepository,
            final ZaehlungRepository zaehlungRepository,
            final ZaehlstelleMapper zaehlstelleMapper,
            final ZaehlungMapper zaehlungMapper,
            final FahrbeziehungMapper fahrbeziehungMapper) {
        this.zaehlstelleRepository = zaehlstelleRepository;
        this.zaehlungRepository = zaehlungRepository;
        this.zaehlstelleMapper = zaehlstelleMapper;
        this.zaehlungMapper = zaehlungMapper;
        this.fahrbeziehungMapper = fahrbeziehungMapper;
    }

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    public void deleteAll() {
        zaehlstelleRepository.deleteAll();
    }

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    public void deleteAll(Iterable<? extends Zaehlstelle> var1) {
        Iterable<de.muenchen.dave.domain.analytics.Zaehlstelle> analyticsList = zaehlstelleMapper.elasticlist2analyticslist(var1);
        zaehlstelleRepository.deleteAll(analyticsList);
    }

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    public void deleteById(String var1) {
        zaehlstelleRepository.deleteById(UUID.fromString(var1));
    }

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    public void delete(Zaehlstelle var1) {
        de.muenchen.dave.domain.analytics.Zaehlstelle zs = zaehlstelleMapper.elastic2analytics(new de.muenchen.dave.domain.analytics.Zaehlstelle(), var1,
                zaehlungMapper, fahrbeziehungMapper);
        zaehlstelleRepository.delete(zs);
    }

    @CacheEvict(
            value = { CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL,
                    CachingConfiguration.LADE_BELASTUNGSPLAN_DTO, CachingConfiguration.LADE_PROCESSED_ZAEHLDATEN,
                    CachingConfiguration.LADE_ZAEHLDATEN_ZEITREIHE_DTO, CachingConfiguration.READ_ZAEHLSTELLE_DTO },
            allEntries = true
    )
    public Zaehlstelle save(Zaehlstelle var1) {
        if (var1 == null) {
            return null;
        }

        de.muenchen.dave.domain.analytics.Zaehlstelle zaehlstelleEntity;
        if (var1.getId() != null && !var1.getId().isBlank()) {
            zaehlstelleEntity = zaehlstelleRepository.findById(UUID.fromString(var1.getId()))
                    .orElse(new de.muenchen.dave.domain.analytics.Zaehlstelle());
        } else {
            zaehlstelleEntity = new de.muenchen.dave.domain.analytics.Zaehlstelle();
        }

        //Iterable<de.muenchen.dave.domain.analytics.Zaehlung> zaehlungen = zaehlungMapper.elasticlist2analyticslist(var1.getZaehlungen());
        zaehlstelleEntity = zaehlstelleMapper.elastic2analytics(zaehlstelleEntity, var1, zaehlungMapper, fahrbeziehungMapper);
        //zaehlstelleEntity.setZaehlungen((List<de.muenchen.dave.domain.analytics.Zaehlung>) zaehlungen);
        zaehlstelleEntity = zaehlstelleRepository.save(zaehlstelleEntity);
        return zaehlstelleMapper.analytics2elastic(zaehlstelleEntity);
    }

    public Optional<Zaehlstelle> findById(String var1) {
        Optional<de.muenchen.dave.domain.analytics.Zaehlstelle> zaehlstelleEntity = zaehlstelleRepository.findById(UUID.fromString(var1));
        return zaehlstelleEntity.map(zaehlstelleMapper::analytics2elastic);
    }

    public Page<Zaehlstelle> suggestSearch(String query, Pageable pageable) {
        Page<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.suggestSearch(query, pageable);
        return zs.map(zaehlstelleMapper::analytics2elastic);
    }

    public Page<Zaehlstelle> findAllByStatus(String query, Pageable pageable) {
        Page<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.findAllByStatus(query, pageable);
        return zs.map(zaehlstelleMapper::analytics2elastic);
    }

    public List<Zaehlstelle> findAll() {
        List<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.findAll();
        return zs.stream().map(zaehlstelleMapper::analytics2elastic).toList();
    }

    public Optional<Zaehlstelle> findByZaehlungenId(String id) {
        Optional<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.findByZaehlungenId(UUID.fromString(id));
        return zs.map(zaehlstelleMapper::analytics2elastic);
    }

    public List<Zaehlstelle> findAllByNummerStartsWithAndStadtbezirkNummer(String nummer, Integer stadtbezirksnummer) {
        List<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.findAllByNummerStartsWithAndStadtbezirkNummer(nummer,
                stadtbezirksnummer);
        return zs.stream().map(zaehlstelleMapper::analytics2elastic).toList();
    }

    public Optional<Zaehlstelle> findByNummer(String nummer) {
        Optional<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.findByNummer(nummer);
        return zs.map(zaehlstelleMapper::analytics2elastic);
    }

    public List<Zaehlstelle> findAllByZaehlungenStatus(String status) {
        List<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.findAllByZaehlungenStatus(status);
        return zs.stream().map(zaehlstelleMapper::analytics2elastic).toList();
    }

    public List<Zaehlstelle> findAllByZaehlungenJahr(String jahr) {
        List<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.findAllByZaehlungenJahr(jahr);
        return zs.stream().map(zaehlstelleMapper::analytics2elastic).toList();
    }

    public List<Zaehlstelle> findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue() {
        List<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue();
        return zs.stream().map(zaehlstelleMapper::analytics2elastic).toList();
    }

    public List<Zaehlstelle> findAllByZaehlungenUnreadMessagesDienstleisterTrue() {
        List<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.findAllByZaehlungenUnreadMessagesDienstleisterTrue();
        return zs.stream().map(zaehlstelleMapper::analytics2elastic).toList();
    }

    @Override
    public Zaehlung initializeZaehlung(Zaehlung zaehlung, String zaehlstelleId) {
        if (zaehlung == null || zaehlstelleId == null || zaehlstelleId.isBlank()) {
            return null;
        }

        if (zaehlung.getId() == null || zaehlung.getId().isBlank()) {
            de.muenchen.dave.domain.analytics.Zaehlstelle zaehlstelle = zaehlstelleRepository.findById(UUID.fromString(zaehlstelleId)).orElseThrow();
            de.muenchen.dave.domain.analytics.Zaehlung zaehlungEntity = zaehlungMapper.elastic2analytics(new de.muenchen.dave.domain.analytics.Zaehlung(),
                    zaehlung, fahrbeziehungMapper);

            zaehlungEntity.setZaehlstelle(zaehlstelle);
            zaehlungEntity = zaehlungRepository.save(zaehlungEntity);
            return zaehlungMapper.analytics2elastic(zaehlungEntity);
        } else {
            return zaehlung;
        }
    }

}
