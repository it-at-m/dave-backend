package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReadMessfaehigkeitDTO implements Serializable {

    @NotNull
    private String gueltigBis;
    @NotNull
    private String gueltigAb;
    @NotNull
    private Fahrzeugklasse fahrzeugklasse;
    @NotNull
    private ZaehldatenIntervall intervall;
}
