package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessstelleIndex {

    default void deleteAll() {
        //TODO not implemented yet
    }

    default void deleteAll(Iterable<? extends Messstelle> var1) {
        //TODO not implemented yet
    }

    default void deleteById(String var1) {
        //TODO not implemented yet
    }

    default void delete(Messstelle var1) {
        //TODO not implemented yet
    }

    default Messstelle save(Messstelle var1) {
        //TODO not implemented yet
        return null;
    }

    default Optional<Messstelle> findById(String var1) {
        //TODO not implemented yet
        return Optional.empty();
    }

    default Page<Messstelle> suggestSearch(String query, Pageable pageable) {
        //TODO not implemented yet
        return null;
    }

    default List<Messstelle> findAll() {
        //TODO not implemented yet
        return null;
    }

    default List<Messstelle> findAllBySichtbarDatenportalIsTrue() {
        //TODO not implemented yet
        return null;
    }

    default Optional<Messstelle> findByMstId(String mstId) {
        //TODO not implemented yet
        return Optional.empty();
    }

    default Optional<Messstelle> findByMessquerschnitteId(String id) {
        //TODO not implemented yet
        return Optional.empty();
    }
}
