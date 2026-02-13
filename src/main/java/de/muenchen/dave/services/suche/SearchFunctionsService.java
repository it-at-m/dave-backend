package de.muenchen.dave.services.suche;

import de.muenchen.dave.domain.dtos.suche.SucheWordSuggestDTO;
import java.util.List;

public interface SearchFunctionsService {

    List<SucheWordSuggestDTO> getSuggestions(final String q);

}
