package de.muenchen.dave.domain.dtos.bearbeiten;

import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.domain.dtos.ZeitintervallDTO;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Transient;

@Data
public class BearbeiteFahrbeziehungDTO {

    String id;

    @Transient
    Long version;

    Boolean isKreuzung;

    // Kreuzung
    Integer von;

    Integer nach;

    // Kreisverkehr
    Integer knotenarm;

    Boolean hinein;

    Boolean heraus;

    Boolean vorbei;

    // Knoten-Kanten-Modell
    String vonknotvonstrnr;

    String nachknotvonstrnr;

    String von_strnr;

    String vonknotennachstrnr;

    String nachknotnachstrnr;

    String nach_strnr;

    HochrechnungsfaktorDTO hochrechnungsfaktor;

    List<ZeitintervallDTO> zeitintervalle;

}
