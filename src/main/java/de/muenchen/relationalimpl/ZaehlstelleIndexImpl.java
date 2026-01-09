package de.muenchen.relationalimpl;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.mapper.ZaehlstelleMapper;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.ZaehlstelleRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ZaehlstelleIndexImpl implements ZaehlstelleIndex {

    private final ZaehlstelleRepository zaehlstelleRepository;

    private final ZaehlstelleMapper zaehlstelleMapper;

    public ZaehlstelleIndexImpl(final ZaehlstelleRepository zaehlstelleRepository,
            final ZaehlstelleMapper zaehlstelleMapper) {
        this.zaehlstelleRepository = zaehlstelleRepository;
        this.zaehlstelleMapper = zaehlstelleMapper;
    }

        public void deleteAll() {
        zaehlstelleRepository.deleteAll();
    }

    public void deleteAll(Iterable<? extends Zaehlstelle> var1) {
        Iterable<de.muenchen.dave.domain.analytics.Zaehlstelle> analyticsList = zaehlstelleMapper.elasticlist2analyticslist(var1);
        zaehlstelleRepository.deleteAll(analyticsList);
    }

    public void deleteById(String var1) {
        zaehlstelleRepository.deleteById(UUID.fromString(var1));
    }

    public void delete(Zaehlstelle var1) {
        de.muenchen.dave.domain.analytics.Zaehlstelle zs = zaehlstelleMapper.elastic2analytics(new de.muenchen.dave.domain.analytics.Zaehlstelle(), var1);
        zaehlstelleRepository.delete(zs);
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
        Optional<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.findByZaehlungenId(id);
        return zs.map(zaehlstelleMapper::analytics2elastic);
    }

    public List<Zaehlstelle> findAllByNummerStartsWithAndStadtbezirkNummer(String nummer, Integer stadtbezirksnummer) {
        List<de.muenchen.dave.domain.analytics.Zaehlstelle> zs = zaehlstelleRepository.findAllByNummerStartsWithAndStadtbezirkNummer(nummer, stadtbezirksnummer);
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


}
