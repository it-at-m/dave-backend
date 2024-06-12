package de.muenchen.dave.domain.dtos.messstelle;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EditMessfaehigkeitDTO implements Serializable {

    private String gueltigBis;
    private String gueltigAb;
    private String fahrzeugklassen;
    private String intervall;
}
