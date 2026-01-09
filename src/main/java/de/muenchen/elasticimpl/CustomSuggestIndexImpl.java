package de.muenchen.elasticimpl;

import de.muenchen.dave.domain.elasticsearch.CustomSuggest;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomSuggestIndexImpl implements CustomSuggestIndex {

    private final CustomSuggestIndexElasticRepository customSuggestIndexElasticRepository;

    public CustomSuggestIndexImpl(final CustomSuggestIndexElasticRepository customSuggestIndexElasticRepository) {
        this.customSuggestIndexElasticRepository = customSuggestIndexElasticRepository;
    }

    public List<CustomSuggest> findAllByNameAndFkid(String name, String fkid) {
        return customSuggestIndexElasticRepository.findAllByNameAndFkid(name, fkid);
    }

    public void deleteAllByFkid(String fkid) {
        customSuggestIndexElasticRepository.deleteAllByFkid(fkid);
    }

    public void saveAll(List<CustomSuggest> suggestions) {
        customSuggestIndexElasticRepository.saveAll(suggestions);
    }

}
