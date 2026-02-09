package de.muenchen.dave.domain.dtos.external;

import de.muenchen.dave.domain.dtos.ZeitintervallDTO;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public abstract class ExternalBewegungsbeziehungDTO implements Serializable {

    private String id;

    private List<ZeitintervallDTO> zeitintervalle;

}
