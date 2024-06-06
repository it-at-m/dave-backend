package de.muenchen.dave.domain.elasticsearch.detektor;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
public class Messfaehigkeit {

    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate gueltigAb;
    @Field(type = FieldType.Date, pattern = "dd.MM.uuuu")
    LocalDate gueltigBis;
    String fahrzeugklassen;
    String intervall;
}
