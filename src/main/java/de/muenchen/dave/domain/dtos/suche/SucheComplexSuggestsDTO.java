package de.muenchen.dave.domain.dtos.suche;

import java.util.List;
import lombok.Data;

@Data
public class SucheComplexSuggestsDTO {

    List<SucheWordSuggestDTO> wordSuggests;

    List<SucheZaehlstelleSuggestDTO> zaehlstellenSuggests;

    List<SucheZaehlungSuggestDTO> zaehlungenSuggests;

}
