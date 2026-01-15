package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ZaehlstelleIndex {

    Zaehlung initializeZaehlung(Zaehlung zaehlung, String zaehlstelleId);

    default void deleteAll() {
        //TODO not implemented yet
    }

    default void deleteAll(Iterable<? extends Zaehlstelle> var1) {
        //TODO not implemented yet
    }

    default void deleteById(String var1) {
        //TODO not implemented yet
    }

    default void delete(Zaehlstelle var1) {
        //TODO not implemented yet
    }

    default Zaehlstelle save(Zaehlstelle var1) {
        //TODO not implemented yet
        return null;
    }

    default Iterable<Zaehlstelle> saveAll(Iterable<Zaehlstelle> var1) {
        //TODO not implemented yet
        return null;
    }

    default Optional<Zaehlstelle> findById(String var1) {
        //TODO not implemented yet
        return Optional.empty();
    }

    default Page<Zaehlstelle> suggestSearch(String query, Pageable pageable) {
        //TODO not implemented yet
        return null;
    }

    default Page<Zaehlstelle> findAllByStatus(String query, Pageable pageable) {
        //TODO not implemented yet
        return null;
    }

    default List<Zaehlstelle> findAll() {
        //TODO not implemented yet
        return null;
    }

    default Optional<Zaehlstelle> findByZaehlungenId(String id) {
        //TODO not implemented yet
        return Optional.empty();
    }

    default List<Zaehlstelle> findAllByNummerStartsWithAndStadtbezirkNummer(String nummer, Integer stadtbezirksnummer) {
        //TODO not implemented yet
        return null;
    }

    default Optional<Zaehlstelle> findByNummer(String nummer) {
        //TODO not implemented yet
        return Optional.empty();
    }

    default List<Zaehlstelle> findAllByZaehlungenStatus(String status) {
        //TODO not implemented yet
        return null;
    }

    default List<Zaehlstelle> findAllByZaehlungenJahr(String jahr) {
        //TODO not implemented yet
        return null;
    }

    default List<Zaehlstelle> findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue() {
        //TODO not implemented yet
        return null;
    }

    default List<Zaehlstelle> findAllByZaehlungenUnreadMessagesDienstleisterTrue() {
        //TODO not implemented yet
        return null;
    }

}
