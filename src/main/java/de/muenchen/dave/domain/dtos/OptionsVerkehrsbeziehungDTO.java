package de.muenchen.dave.domain.dtos;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class OptionsVerkehrsbeziehungDTO extends OptionsBewegungsbeziehungDTO {

    private Integer von;

    private Integer nach;

    private Himmelsrichtung strassenseite;

}
