package de.muenchen.dave.domain.dtos.suche;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SucheComplexSuggestsDTO {

    List<SucheWordSuggestDTO> wordSuggests = new ArrayList<>();

    List<SucheZaehlstelleSuggestDTO> zaehlstellenSuggests = new ArrayList<>();

    List<SucheZaehlungSuggestDTO> zaehlungenSuggests = new ArrayList<>();

    List<SucheMessstelleSuggestDTO> messstellenSuggests = new ArrayList<>();

}
