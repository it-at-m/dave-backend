package de.muenchen.elasticimpl;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MessstelleIndexImpl implements MessstelleIndex {

    private final MessstelleIndexElasticRepository messstelleIndexElasticRepository;

    public MessstelleIndexImpl(final MessstelleIndexElasticRepository messstelleIndexElasticRepository) {
        this.messstelleIndexElasticRepository = messstelleIndexElasticRepository;
    }

    public void deleteAll() {
        messstelleIndexElasticRepository.deleteAll();
    }

    public void deleteAll(Iterable<? extends Messstelle> var1) {
        messstelleIndexElasticRepository.deleteAll(var1);
    }

    public void deleteById(String var1) {
        messstelleIndexElasticRepository.deleteById(var1);
    }

    public void delete(Messstelle var1) {
        messstelleIndexElasticRepository.delete(var1);
    }

    public Messstelle save(Messstelle var1) {
        return messstelleIndexElasticRepository.save(var1);
    }

    public Iterable<Messstelle> saveAll(Iterable<Messstelle> var1) {
        return messstelleIndexElasticRepository.saveAll(var1);
    }

    public Optional<Messstelle> findById(String var1) {
        return messstelleIndexElasticRepository.findById(var1);
    }

    public Page<Messstelle> suggestSearch(String query, Pageable pageable) {
        return messstelleIndexElasticRepository.suggestSearch(query, pageable);
    }

    public List<Messstelle> findAll() {
        return messstelleIndexElasticRepository.findAll();
    }

    public List<Messstelle> findAllBySichtbarDatenportalIsTrue() {
        return messstelleIndexElasticRepository.findAllBySichtbarDatenportalIsTrue();
    }

    public Optional<Messstelle> findByMstId(String mstId) {
        return messstelleIndexElasticRepository.findByMstId(mstId);
    }

    public Optional<Messstelle> findByMessquerschnitteId(String id) {
        return messstelleIndexElasticRepository.findByMessquerschnitteId(id);
    }
}
