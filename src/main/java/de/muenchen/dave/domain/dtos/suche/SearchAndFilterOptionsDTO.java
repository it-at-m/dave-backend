package de.muenchen.dave.domain.dtos.suche;

import de.muenchen.dave.domain.enums.Verkehrsart;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SearchAndFilterOptionsDTO {

    private boolean searchInMessstellen;
    private boolean searchInZaehlstellen;
    @NotNull
    private List<Verkehrsart> messstelleVerkehrsart;
}
