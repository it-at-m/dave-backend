package de.muenchen.dave.domain.dtos.messstelle;

import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReadMessfaehigkeitDTO implements Serializable {

    private String gueltigBis;
    private String gueltigAb;
    private String fahrzeugklassen;
    private ZaehldatenIntervall intervall;
}
