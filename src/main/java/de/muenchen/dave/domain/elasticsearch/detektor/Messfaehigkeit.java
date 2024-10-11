package de.muenchen.dave.domain.elasticsearch.detektor;

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
    String fahrzeugklassen;
    String intervall;
}
