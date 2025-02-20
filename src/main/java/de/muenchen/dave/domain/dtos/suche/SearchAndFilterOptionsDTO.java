package de.muenchen.dave.domain.dtos.suche;

import de.muenchen.dave.domain.dtos.common.StartAndEndDate;
import lombok.Data;

@Data
public class SearchAndFilterOptionsDTO {

    boolean searchInMessstellen;
    boolean searchInZaehlstellen;
    StartAndEndDate zeitraum;
}
