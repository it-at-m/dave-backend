package de.muenchen.dave.repositories.elasticsearch;

import de.muenchen.dave.domain.elasticsearch.CustomSuggest;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CustomSuggestIndex extends ElasticsearchRepository<CustomSuggest, String> {

    List<CustomSuggest> findAllByNameAndFkid(String name, String fkid);

    void deleteAllByFkid(String fkid);

}
