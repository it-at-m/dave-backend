package de.muenchen.dave.domain.dtos.external;

import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDto;
import de.muenchen.dave.domain.dtos.ZeitintervallDto;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public abstract class ExternalBewegungsbeziehungDTO implements Serializable {

    private String id;

    private List<ZeitintervallDto> zeitintervalle;

    private HochrechnungsfaktorDto hochrechnungsfaktor;

}
