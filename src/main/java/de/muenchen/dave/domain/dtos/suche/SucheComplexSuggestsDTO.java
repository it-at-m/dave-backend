package de.muenchen.dave.domain.dtos.suche;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SucheComplexSuggestsDTO {

    @NotNull
    List<SucheWordSuggestDTO> wordSuggests = new ArrayList<>();
    @NotNull
    List<SucheZaehlstelleSuggestDTO> zaehlstellenSuggests = new ArrayList<>();
    @NotNull
    List<SucheZaehlungSuggestDTO> zaehlungenSuggests = new ArrayList<>();
    @NotNull
    List<SucheMessstelleSuggestDTO> messstellenSuggests = new ArrayList<>();

}
