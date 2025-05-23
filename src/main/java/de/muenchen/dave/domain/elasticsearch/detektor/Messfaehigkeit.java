package de.muenchen.dave.domain.elasticsearch.detektor;

import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Messfaehigkeit {

    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate gueltigAb;
    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate gueltigBis;
    Fahrzeugklasse fahrzeugklasse;
    ZaehldatenIntervall intervall;
}
