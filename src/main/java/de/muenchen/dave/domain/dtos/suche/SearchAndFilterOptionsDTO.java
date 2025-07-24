package de.muenchen.dave.domain.dtos.suche;

import de.muenchen.dave.domain.enums.Verkehrsart;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class SearchAndFilterOptionsDTO {

    private boolean searchInMessstellen;
    private boolean searchInZaehlstellen;
    @NotNull
    private List<Verkehrsart> messstelleVerkehrsart;
}
