package de.muenchen.dave.domain.dtos.messstelle.auswertung;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;

@Data
public class AuswertungMessstelle {

    private String mstId;

    // Jeder Zeitraum ist ein Eintrag
    private List<Auswertung> auswertungenProZeitraum = new ArrayList<>();

    // Jeder Zeitraum ist ein Eintrag in der Liste pro MQ
    private Map<String, List<Auswertung>> auswertungenProMq = new TreeMap<>();
}
