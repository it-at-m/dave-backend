/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.domain.dtos.laden;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import lombok.Data;

@Data
@JsonIgnoreProperties(
        value = {
                "startUhrzeit",
                "endeUhrzeit",
                "pkw",
                "lkw",
                "lastzuege",
                "busse",
                "kraftraeder",
                "fussgaenger",
                "pkwEinheiten"
        }
)
public class LadeZaehldatumTageswertDTO extends LadeZaehldatumDTO {

    private BigDecimal kfz;

    private BigDecimal schwerverkehr;

    private BigDecimal gueterverkehr;

    private Integer fahrradfahrer;
}
