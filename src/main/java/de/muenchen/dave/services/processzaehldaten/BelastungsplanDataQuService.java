package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.*;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BelastungsplanDataQuService extends AbstractBelastungsplanDataService {

    public AbstractLadeBelastungsplanDTO<?> buildLadeBelastungsplanDTO(
            final OptionsDTO options,
            final Zaehlung zaehlung,
            final List<Zeitintervall> zeitintervalle) {
        Map<Querungsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = MappingUtil.mapQuerungsverkehre(
                options,
                zaehlung,
                zeitintervalle);

        var ladeBelastungsplan = new LadeBelastungsplanQuDTO();
        ladeBelastungsplan.setStreets(new String[8]);
        ladeBelastungsplan.setValue1(getEmptyBelastungsplanData());
        ladeBelastungsplan.setValue2(getEmptyBelastungsplanData());
        ladeBelastungsplan.setValue3(getEmptyBelastungsplanData());

        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> belastungsplanData = buildBelastungsplanDataMap(ladeZaehldatumBelastungsplan, zaehlung);
        zaehlung.getKnotenarme().forEach(knotenarm -> ladeBelastungsplan.getStreets()[knotenarm.getNummer() - 1] = knotenarm.getStrassenname());
        if (options.getRadverkehr() && belastungsplanData.containsKey(Fahrzeug.RAD)) {
            putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.RAD);
        } else if (options.getFussverkehr() && belastungsplanData.containsKey(Fahrzeug.FUSS)) {
            putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.FUSS);
        }
        markKIHochrechnung(zaehlung.getZaehldauer(), options.getZeitauswahl(), ladeBelastungsplan);
        return ladeBelastungsplan;
    }

    public Map<Fahrzeug, AbstractBelastungsplanDataDTO> buildBelastungsplanDataMap(
            Map<Querungsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeQuerungsverkehr,
            final Zaehlung zaehlung) {
        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> returnValue = new HashMap<>();

        if (zaehlung.getKategorien().contains(Fahrzeug.RAD)) {
            returnValue.put(Fahrzeug.RAD,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.RAD, LadeZaehldatumDTO::getFahrradfahrer, zaehldatenJeQuerungsverkehr));
        }
        if (zaehlung.getKategorien().contains(Fahrzeug.FUSS)) {
            returnValue.put(Fahrzeug.FUSS,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.FUSS, LadeZaehldatumDTO::getFussgaenger, zaehldatenJeQuerungsverkehr));
        }
        return returnValue;
    }

    private BelastungsplanQuDataDTO getEmptyBelastungsplanData() {
        final BelastungsplanQuDataDTO data = new BelastungsplanQuDataDTO();
        fillEmptyBelastungsplanData(data);
        data.setValuesKnotenarme(new ArrayList<>());
        return data;
    }

    private BelastungsplanQuDataDTO buildBelastungsplanDataDTOForFahrzeug(
            final Fahrzeug fz,
            final Function<LadeZaehldatumDTO, Integer> reader,
            final Map<Querungsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeQuerungsverkehr) {
        final BelastungsplanQuDataDTO belastungsplanData = getEmptyBelastungsplanData();
        belastungsplanData.setFilled(true);
        belastungsplanData.setLabel(fz.getName());
        zaehldatenJeQuerungsverkehr.forEach((querungsverkehr, tupelTageswertZaehldatum) -> addValueAndSum(querungsverkehr,
                BigDecimal.valueOf(Objects.requireNonNullElse(reader.apply(tupelTageswertZaehldatum.getLadeZaehldatum()), 0)),
                belastungsplanData));
        return belastungsplanData;
    }

    /**
     * Setzt den Zählwert anhand des {@link Querungsverkehr} an die richtige Stelle des
     * {@link BelastungsplanQuDataDTO} und addiert ihn zur Summe des entsprechenden Knotenarms.
     */
    private void addValueAndSum(
            final Querungsverkehr querungsverkehr,
            final BigDecimal value,
            final BelastungsplanQuDataDTO belastungsplanData) {
        final BelastungsplanQuDataDTO.KnotenarmValue knotenarmValue = belastungsplanData.getValuesKnotenarme().stream()
                .filter(kn -> Objects.equals(kn.getKnotenarm(), querungsverkehr.getKnotenarm())).findFirst()
                .orElseGet(() -> {
                    final var kv = new BelastungsplanQuDataDTO.KnotenarmValue(querungsverkehr.getKnotenarm(), new ArrayList<>());
                    belastungsplanData.getValuesKnotenarme().add(kv);
                    return kv;
                });
        Optional<BelastungsplanQuDataDTO.QuerungsverkehrValue> querungsverkehrValue = knotenarmValue.getValuesQuerungsverkehre().stream()
                .filter(qv -> qv.getRichtung() == querungsverkehr.getRichtung()).findFirst();
        if (querungsverkehrValue.isPresent()) {
            log.error("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen");
            throw new IllegalStateException("Fehler beim Berechnen der Daten");
        } else {
            knotenarmValue.getValuesQuerungsverkehre().add(new BelastungsplanQuDataDTO.QuerungsverkehrValue(querungsverkehr.getRichtung(), value));
            // Summiere Knotenarm
            if (knotenarmValue.getSumKnotenarm() == null) {
                knotenarmValue.setSumKnotenarm(value);
            } else {
                knotenarmValue.setSumKnotenarm(knotenarmValue.getSumKnotenarm().add(value));
            }
        }
    }
}
