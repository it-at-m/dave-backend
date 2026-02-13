package de.muenchen.relationalimpl;

import de.muenchen.dave.domain.dtos.suche.SucheWordSuggestDTO;
import de.muenchen.dave.services.suche.SearchFunctionsService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SearchFunctionsServiceImpl implements SearchFunctionsService {

    @Override
    public List<SucheWordSuggestDTO> getSuggestions(String q) {
        SucheWordSuggestDTO dto = new SucheWordSuggestDTO();
        return List.of(dto);
    }

}
