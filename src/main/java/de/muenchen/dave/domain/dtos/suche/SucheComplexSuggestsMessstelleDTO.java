package de.muenchen.dave.domain.dtos.suche;

import java.util.List;
import lombok.Data;

@Data
public class SucheComplexSuggestsMessstelleDTO {

    List<SucheWordSuggestDTO> wordSuggests;

    List<SucheMessstelleSuggestDTO> messstellenSuggests;

}
