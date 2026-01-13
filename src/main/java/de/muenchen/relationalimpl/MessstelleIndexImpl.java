package de.muenchen.relationalimpl;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MessstelleIndexImpl implements MessstelleIndex {

    public void deleteAll() {
        //TODO not implemented yet
    }

    public void deleteAll(Iterable<? extends Messstelle> var1) {
        //TODO not implemented yet
    }

    public void deleteById(String var1) {
        //TODO not implemented yet
    }

    public void delete(Messstelle var1) {
        //TODO not implemented yet
    }

    public Messstelle save(Messstelle var1) {
        //TODO not implemented yet
        return null;
    }

    public Iterable<Messstelle> saveAll(Iterable<Messstelle> var1) {
        //TODO not implemented yet
        return null;
    }

    public Optional<Messstelle> findById(String var1) {
        //TODO not implemented yet
        return Optional.empty();
    }

    public Page<Messstelle> suggestSearch(String query, Pageable pageable) {
        //TODO not implemented yet
        return null;
    }

    public List<Messstelle> findAll() {
        //TODO not implemented yet
        return null;
    }

    public List<Messstelle> findAllBySichtbarDatenportalIsTrue() {
        //TODO not implemented yet
        return null;
    }

    public Optional<Messstelle> findByMstId(String mstId) {
        //TODO not implemented yet
        return Optional.empty();
    }

    public Optional<Messstelle> findByMessquerschnitteId(String id) {
        //TODO not implemented yet
        return Optional.empty();
    }
}
