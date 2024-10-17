package de.muenchen.dave.domain.dtos.messstelle;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class EditMessfaehigkeitDTO implements Serializable {

    private String gueltigBis;
    private String gueltigAb;
    private String fahrzeugklassen;
    private String intervall;
}
