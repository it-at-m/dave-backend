package de.muenchen.dave.domain.dtos.suche;

import lombok.Data;

import java.util.List;

@Data
public class SucheComplexSuggestsMessstelleDTO {

    List<SucheWordSuggestDTO> wordSuggests;

    List<SucheMessstelleSuggestDTO> messstellenSuggests;

}
