package de.muenchen.dave.domain.dtos.suche;

import lombok.Data;

import java.util.List;

@Data
public class SucheComplexSuggestsDTO {

    List<SucheWordSuggestDTO> wordSuggests;

    List<SucheZaehlstelleSuggestDTO> zaehlstellenSuggests;

    List<SucheZaehlungSuggestDTO> zaehlungenSuggests;

}
