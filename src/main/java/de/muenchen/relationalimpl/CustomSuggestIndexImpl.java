package de.muenchen.relationalimpl;

import de.muenchen.dave.domain.elasticsearch.CustomSuggest;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomSuggestIndexImpl implements CustomSuggestIndex {

    public List<CustomSuggest> findAllByNameAndFkid(String name, String fkid) {
        //TODO not implemented yet
        return null;
    }

    public void deleteAllByFkid(String fkid) {
        //TODO not implemented yet
    }

    public void saveAll(List<CustomSuggest> suggestions) {
        //TODO not implemented yet
    }

}
