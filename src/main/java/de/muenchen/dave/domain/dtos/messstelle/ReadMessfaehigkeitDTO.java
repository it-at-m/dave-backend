package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ReadMessfaehigkeitDTO implements Serializable {

    private String gueltigBis;
    private String gueltigAb;
    private String fahrzeugklassen;
    private ZaehldatenIntervall intervall;
}
