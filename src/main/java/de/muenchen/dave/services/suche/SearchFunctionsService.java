package de.muenchen.dave.services.suche;

import java.util.List;

import de.muenchen.dave.domain.dtos.suche.SucheWordSuggestDTO;

public interface SearchFunctionsService {

    List<SucheWordSuggestDTO> getSuggestions(final String q);

}
