package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanQjsDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanQjsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitauswahl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
@Slf4j
public abstract class AbstractBelastungsplanDataService implements IBelastungsplanDataService{

    @SuppressWarnings("unchecked")
    protected <T extends AbstractBelastungsplanDataDTO> void putFirstValueInBelastungsplan(AbstractLadeBelastungsplanDTO<T> ladeBelastungsplan,
                                                                                         Map<Fahrzeug, ? extends AbstractBelastungsplanDataDTO> belastungsplanData,
                                                                                         Fahrzeug value) {
        ladeBelastungsplan.setValue3(ladeBelastungsplan.getValue2());
        ladeBelastungsplan.setValue2(ladeBelastungsplan.getValue1());
        ladeBelastungsplan.setValue1((T) belastungsplanData.get(value));
    }

    protected void markKIHochrechnung(final String zaehldauer, final String zeitauswahl,
                                    final AbstractLadeBelastungsplanDTO<? extends AbstractBelastungsplanDataDTO> ladeBelastungsplanSum) {
        // KI-Hochgerechnete Werte sollen im Belastungsplan entsprechend gekennzeichnet werden
        if (Zeitauswahl.TAGESWERT.getCapitalizedName().equals(zeitauswahl) && List.of(Zaehldauer.DAUER_2_X_4_STUNDEN.toString(),
                Zaehldauer.DAUER_13_STUNDEN.toString(), Zaehldauer.DAUER_16_STUNDEN.toString()).contains(zaehldauer)) {
            Stream.of(
                            ladeBelastungsplanSum.getValue1(),
                            ladeBelastungsplanSum.getValue2(),
                            ladeBelastungsplanSum.getValue3()).filter(v -> "RAD".equals(v.getLabel()))
                    .forEach(v -> v.setLabel("RAD (KI-Hochrechnung)"));
        }
    }

    protected static void fillEmptyBelastungsplanData(AbstractBelastungsplanDataDTO data) {
        data.setLabel("");
        data.setFilled(false);
    }

}
