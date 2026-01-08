package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.domain.elasticsearch.CustomSuggest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomSuggestIndex {

    private final CustomSuggestIndexElasticRepository customSuggestIndexElasticRepository;

    public CustomSuggestIndex(final CustomSuggestIndexElasticRepository customSuggestIndexElasticRepository) {
        this.customSuggestIndexElasticRepository = customSuggestIndexElasticRepository;
    }

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
