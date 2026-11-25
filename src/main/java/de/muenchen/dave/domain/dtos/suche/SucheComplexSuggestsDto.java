package de.muenchen.dave.domain.dtos.suche;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SucheComplexSuggestsDto {

    @NotNull
    List<SucheWordSuggestDto> wordSuggests = new ArrayList<>();
    @NotNull
    List<SucheZaehlstelleSuggestDto> zaehlstellenSuggests = new ArrayList<>();
    @NotNull
    List<SucheZaehlungSuggestDto> zaehlungenSuggests = new ArrayList<>();
    @NotNull
    List<SucheMessstelleSuggestDto> messstellenSuggests = new ArrayList<>();

}
