package de.muenchen.elasticimpl;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ZaehlstelleIndexImpl implements ZaehlstelleIndex {

    private final ZaehlstelleIndexElasticRepository zaehlstelleIndexElasticRepository;

    public ZaehlstelleIndexImpl(final ZaehlstelleIndexElasticRepository zaehlstelleIndexElasticRepository) {
        this.zaehlstelleIndexElasticRepository = zaehlstelleIndexElasticRepository;
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
        if (var1 == null) {
            return null;
        }
        if (var1.getId() == null || var1.getId().isBlank()) {
            var1.setId(UUID.randomUUID().toString());
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

    @Override
    public Zaehlung initializeZaehlung(Zaehlung zaehlung, String zaehlstelleId) {
        // Set Zaehlung ID
        if (StringUtils.isEmpty(zaehlung.getId())) {
            zaehlung.setId(UUID.randomUUID().toString());
        }
        // Set Fahrbeziehung ID
        if (CollectionUtils.isNotEmpty(zaehlung.getFahrbeziehungen())) {
            zaehlung.getFahrbeziehungen().stream()
                    .filter(fahrbeziehung -> StringUtils.isEmpty(fahrbeziehung.getId()))
                    .forEach(fahrbeziehung -> fahrbeziehung.setId(UUID.randomUUID().toString()));
        }

        return zaehlung;
    }

}
