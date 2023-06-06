package de.muenchen.dave.domain.elasticsearch;

import java.io.Serializable;
import lombok.Data;

@Data
public class Fahrbeziehung implements Serializable {

    String id;

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

    Hochrechnungsfaktor hochrechnungsfaktor;

}
