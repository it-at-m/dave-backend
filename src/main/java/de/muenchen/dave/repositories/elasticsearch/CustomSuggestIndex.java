package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.domain.elasticsearch.CustomSuggest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CustomSuggestIndex extends ElasticsearchRepository<CustomSuggest, String> {

    List<CustomSuggest> findAllByNameAndFkid(String name, String fkid);

    void deleteAllByFkid(String fkid);

}
