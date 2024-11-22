package de.muenchen.dave.domain.dtos.laden;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class LadeZaehldatenSteplineForMessstelleDTO extends LadeZaehldatenSteplineDTO {

    private String mstId;

}
