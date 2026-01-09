package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.domain.elasticsearch.CustomSuggest;
import java.util.List;

public interface CustomSuggestIndex {

    default List<CustomSuggest> findAllByNameAndFkid(String name, String fkid) {
        //TODO not implemented yet
        return null;
    }

    default void deleteAllByFkid(String fkid) {
        //TODO not implemented yet
    }

    default void saveAll(List<CustomSuggest> suggestions) {
        //TODO not implemented yet
    }

}
