package de.muenchen.dave.domain.elasticsearch.detektor;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
public class Messfaehigkeit {

    @Transient
    String id;

    @Transient
    Long version;

    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate gueltigAb;
    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate gueltigBis;
    Fahrzeugklasse fahrzeugklasse;
    ZaehldatenIntervall intervall;
}
