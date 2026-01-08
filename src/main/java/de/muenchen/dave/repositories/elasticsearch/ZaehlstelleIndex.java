package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.mapper.ZaehlstelleMapper;
import de.muenchen.dave.repositories.relationaldb.ZaehlstelleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ZaehlstelleIndex {

    private final ZaehlstelleIndexElasticRepository zaehlstelleIndexElasticRepository;

    private final ZaehlstelleRepository zaehlstelleRepository;

    private final ZaehlstelleMapper zaehlstelleMapper;

    public ZaehlstelleIndex(final ZaehlstelleIndexElasticRepository zaehlstelleIndexElasticRepository,
            final ZaehlstelleRepository zaehlstelleRepository,
            final ZaehlstelleMapper zaehlstelleMapper) {
        this.zaehlstelleIndexElasticRepository = zaehlstelleIndexElasticRepository;
        this.zaehlstelleRepository = zaehlstelleRepository;
        this.zaehlstelleMapper = zaehlstelleMapper;
    }

    public void deleteAll() {
        zaehlstelleIndexElasticRepository.deleteAll();
    }

    public void deleteAll(Iterable<? extends Zaehlstelle> var1) {
        zaehlstelleIndexElasticRepository.deleteAll(var1);
    }

    public void deleteById(String var1) {
        zaehlstelleIndexElasticRepository.deleteById(var1);
    }

    public void delete(Zaehlstelle var1) {
        zaehlstelleIndexElasticRepository.delete(var1);
    }

    public Zaehlstelle save(Zaehlstelle var1) {
        de.muenchen.dave.domain.analytics.Zaehlstelle zaehlstelleEntity = new de.muenchen.dave.domain.analytics.Zaehlstelle();
        if (var1 == null) {
            return null;
        } else if (var1.getId() != null && !var1.getId().isBlank()) {
            zaehlstelleEntity = zaehlstelleRepository.findById(UUID.fromString(var1.getId()))
                    .orElse(zaehlstelleEntity);
        }
        zaehlstelleEntity = zaehlstelleMapper.elastic2analytics(zaehlstelleEntity, var1);
        zaehlstelleEntity = zaehlstelleRepository.save(zaehlstelleEntity);
        if (var1.getId() == null || var1.getId().isBlank()) {
            var1.setId(zaehlstelleEntity.getId().toString());
            //zaehlstelle.setId(UUID.randomUUID().toString());
        }
        return zaehlstelleIndexElasticRepository.save(var1);
    }

    public Iterable<Zaehlstelle> saveAll(Iterable<Zaehlstelle> var1) {
        if (var1 == null) {
            return null;
        }
        List<Zaehlstelle> zaehlstellenList = new java.util.ArrayList<>();
        for (Zaehlstelle zaehlstelle : var1) {
            zaehlstellenList.add(this.save(zaehlstelle));
        }
        return zaehlstellenList;
    }

    public Optional<Zaehlstelle> findById(String var1) {
        return zaehlstelleIndexElasticRepository.findById(var1);
    }

    public Page<Zaehlstelle> suggestSearch(String query, Pageable pageable) {
        return zaehlstelleIndexElasticRepository.suggestSearch(query, pageable);
    }

    public Page<Zaehlstelle> findAllByStatus(String query, Pageable pageable) {
        return zaehlstelleIndexElasticRepository.findAllByStatus(query, pageable);
    }

    public List<Zaehlstelle> findAll() {
        return zaehlstelleIndexElasticRepository.findAll();
    }

    public Optional<Zaehlstelle> findByZaehlungenId(String id) {
        return zaehlstelleIndexElasticRepository.findByZaehlungenId(id);
    }

    public List<Zaehlstelle> findAllByNummerStartsWithAndStadtbezirkNummer(String nummer, Integer stadtbezirksnummer) {
        return zaehlstelleIndexElasticRepository.findAllByNummerStartsWithAndStadtbezirkNummer(nummer, stadtbezirksnummer);
    }

    public Optional<Zaehlstelle> findByNummer(String nummer) {
        return zaehlstelleIndexElasticRepository.findByNummer(nummer);
    }

    public List<Zaehlstelle> findAllByZaehlungenStatus(String status) {
        return zaehlstelleIndexElasticRepository.findAllByZaehlungenStatus(status);
    }

    public List<Zaehlstelle> findAllByZaehlungenJahr(String jahr) {
        return zaehlstelleIndexElasticRepository.findAllByZaehlungenJahr(jahr);
    }

    public List<Zaehlstelle> findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue() {
        return zaehlstelleIndexElasticRepository.findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue();
    }

    public List<Zaehlstelle> findAllByZaehlungenUnreadMessagesDienstleisterTrue() {
        return zaehlstelleIndexElasticRepository.findAllByZaehlungenUnreadMessagesDienstleisterTrue();
    }

}
