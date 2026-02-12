package de.muenchen.dave.domain.elasticsearch;

import de.muenchen.dave.domain.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Verkehrsbeziehung extends Bewegungsbeziehung {

    private Boolean isKreuzung;

    // Kreuzung
    private Integer von;

    private Integer nach;

    // Kreisverkehr
    private Integer knotenarm;

    private Boolean hinein;

    private Boolean heraus;

    private Boolean vorbei;

    // Definiert den neben der Straße fließenden Verkehr (z.B. Radverkehr auf Radweg am nördlich gelegenen Straßenrand)
    private Himmelsrichtung strassenseite;

    // Knoten-Kanten-Modell
    private String vonknotvonstrnr;

    private String nachknotvonstrnr;

    private String von_strnr;

    private String vonknotennachstrnr;

    private String nachknotnachstrnr;

    private String nach_strnr;

    private Hochrechnungsfaktor hochrechnungsfaktor;

}
