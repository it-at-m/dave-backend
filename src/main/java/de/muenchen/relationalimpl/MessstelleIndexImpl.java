package de.muenchen.relationalimpl;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.relationalimpl.mapper.MessfaehigkeitRelationalMapper;
import de.muenchen.relationalimpl.mapper.MessquerschnittRelationalMapper;
import de.muenchen.relationalimpl.mapper.MessstelleRelationalMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessstelleIndexImpl implements MessstelleIndex {

    private final MessstelleRepository messstelleRepository;
    private final MessstelleRelationalMapper messstelleMapper;
    private final MessquerschnittRelationalMapper messquerschnittMapper;
    private final MessfaehigkeitRelationalMapper messfaehigkeitMapper;

    public MessstelleIndexImpl(
            final MessstelleRepository messstelleRepository,
            final MessstelleRelationalMapper messstelleMapper,
            final MessquerschnittRelationalMapper messquerschnittMapper,
            final MessfaehigkeitRelationalMapper messfaehigkeitMapper) {
        this.messstelleRepository = messstelleRepository;
        this.messstelleMapper = messstelleMapper;
        this.messquerschnittMapper = messquerschnittMapper;
        this.messfaehigkeitMapper = messfaehigkeitMapper;
    }

    public void deleteAll() {
        messstelleRepository.deleteAll();
    }

    public void deleteAll(Iterable<? extends Messstelle> var1) {
        Iterable<de.muenchen.dave.domain.analytics.detektor.Messstelle> analyticsList = messstelleMapper.elasticlist2analyticslist(var1, messquerschnittMapper,
                messfaehigkeitMapper);
        messstelleRepository.deleteAll(analyticsList);
    }

    public void deleteById(String var1) {
        messstelleRepository.deleteById(UUID.fromString(var1));
    }

    public void delete(Messstelle var1) {
        de.muenchen.dave.domain.analytics.detektor.Messstelle ms = messstelleMapper.elastic2analytics(
                new de.muenchen.dave.domain.analytics.detektor.Messstelle(),
                var1,
                messquerschnittMapper,
                messfaehigkeitMapper);
        messstelleRepository.delete(ms);
    }

    public Messstelle save(Messstelle var1) {
        if (var1 == null) {
            return null;
        }

        de.muenchen.dave.domain.analytics.detektor.Messstelle messstelleEntity;
        if (var1.getId() != null && !var1.getId().isBlank()) {
            messstelleEntity = messstelleRepository.findById(UUID.fromString(var1.getId()))
                    .orElse(new de.muenchen.dave.domain.analytics.detektor.Messstelle());
        } else {
            messstelleEntity = new de.muenchen.dave.domain.analytics.detektor.Messstelle();
        }

        messstelleEntity = messstelleMapper.elastic2analytics(
                messstelleEntity,
                var1,
                messquerschnittMapper,
                messfaehigkeitMapper);
        messstelleEntity = messstelleRepository.save(messstelleEntity);
        return messstelleMapper.analytics2elastic(messstelleEntity);
    }

    public Iterable<Messstelle> saveAll(Iterable<Messstelle> var1) {
        if (var1 == null) {
            return null;
        }
        List<Messstelle> messstellenList = new java.util.ArrayList<>();
        for (Messstelle messstelle : var1) {
            messstellenList.add(this.save(messstelle));
        }
        return messstellenList;
    }

    public Optional<Messstelle> findById(String var1) {
        Optional<de.muenchen.dave.domain.analytics.detektor.Messstelle> messstelleEntity = messstelleRepository.findById(UUID.fromString(var1));
        return messstelleEntity.map(messstelleMapper::analytics2elastic);
    }

    public Page<Messstelle> suggestSearch(String query, Pageable pageable) {
        Page<de.muenchen.dave.domain.analytics.detektor.Messstelle> ms = messstelleRepository.suggestSearch(query, pageable);
        return ms.map(messstelleMapper::analytics2elastic);
    }

    public List<Messstelle> findAll() {
        List<de.muenchen.dave.domain.analytics.detektor.Messstelle> ms = messstelleRepository.findAll();
        return ms.stream().map(messstelleMapper::analytics2elastic).toList();
    }

    public List<Messstelle> findAllBySichtbarDatenportalIsTrue() {
        List<de.muenchen.dave.domain.analytics.detektor.Messstelle> ms = messstelleRepository.findAllBySichtbarDatenportalIsTrue();
        return ms.stream().map(messstelleMapper::analytics2elastic).toList();
    }

    public Optional<Messstelle> findByMstId(String mstId) {
        Optional<de.muenchen.dave.domain.analytics.detektor.Messstelle> ms = messstelleRepository.findByMstId(mstId);
        return ms.map(messstelleMapper::analytics2elastic);
    }

    public Optional<Messstelle> findByMessquerschnitteId(String id) {
        Optional<de.muenchen.dave.domain.analytics.detektor.Messstelle> ms = messstelleRepository.findByMessquerschnitteId(UUID.fromString(id));
        return ms.map(messstelleMapper::analytics2elastic);
    }
}
