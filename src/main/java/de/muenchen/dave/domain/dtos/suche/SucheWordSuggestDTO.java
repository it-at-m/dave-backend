package de.muenchen.dave.domain.dtos.suche;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SucheWordSuggestDTO {

    @NotNull
    String text;

}
