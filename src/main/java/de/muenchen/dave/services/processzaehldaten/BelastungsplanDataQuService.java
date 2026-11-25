package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDto;
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

    public AbstractLadeBelastungsplanDto<?> buildLadeBelastungsplanDTO(
            final OptionsDto options,
            final Zaehlung zaehlung,
            final List<Zeitintervall> zeitintervalle) {
        Map<Querungsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = MappingUtil.mapQuerungsverkehre(
                options,
                zaehlung,
                zeitintervalle);

        var ladeBelastungsplan = new LadeBelastungsplanQuDto();
        ladeBelastungsplan.setStreets(new String[8]);
        ladeBelastungsplan.setValue1(getEmptyBelastungsplanData());
        ladeBelastungsplan.setValue2(getEmptyBelastungsplanData());
        ladeBelastungsplan.setValue3(getEmptyBelastungsplanData());

        final Map<Fahrzeug, AbstractBelastungsplanDataDto> belastungsplanData = buildBelastungsplanDataMap(ladeZaehldatumBelastungsplan, zaehlung);
        zaehlung.getKnotenarme().forEach(knotenarm -> ladeBelastungsplan.getStreets()[knotenarm.getNummer() - 1] = knotenarm.getStrassenname());
        if (options.getRadverkehr() && belastungsplanData.containsKey(Fahrzeug.RAD)) {
            putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.RAD);
        } else if (options.getFussverkehr() && belastungsplanData.containsKey(Fahrzeug.FUSS)) {
            putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.FUSS);
        }
        markKIHochrechnung(zaehlung.getZaehldauer(), options.getZeitauswahl(), ladeBelastungsplan);
        return ladeBelastungsplan;
    }

    public Map<Fahrzeug, AbstractBelastungsplanDataDto> buildBelastungsplanDataMap(
            Map<Querungsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeQuerungsverkehr,
            final Zaehlung zaehlung) {
        final Map<Fahrzeug, AbstractBelastungsplanDataDto> returnValue = new HashMap<>();

        if (zaehlung.getKategorien().contains(Fahrzeug.RAD)) {
            returnValue.put(Fahrzeug.RAD,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.RAD, LadeZaehldatumDto::getFahrradfahrer, zaehldatenJeQuerungsverkehr));
        }
        if (zaehlung.getKategorien().contains(Fahrzeug.FUSS)) {
            returnValue.put(Fahrzeug.FUSS,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.FUSS, LadeZaehldatumDto::getFussgaenger, zaehldatenJeQuerungsverkehr));
        }
        return returnValue;
    }

    private BelastungsplanQuDataDto getEmptyBelastungsplanData() {
        final BelastungsplanQuDataDto data = new BelastungsplanQuDataDto();
        fillEmptyBelastungsplanData(data);
        data.setValuesKnotenarme(new ArrayList<>());
        return data;
    }

    private BelastungsplanQuDataDto buildBelastungsplanDataDTOForFahrzeug(
            final Fahrzeug fz,
            final Function<LadeZaehldatumDto, Integer> reader,
            final Map<Querungsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeQuerungsverkehr) {
        final BelastungsplanQuDataDto belastungsplanData = getEmptyBelastungsplanData();
        belastungsplanData.setFilled(true);
        belastungsplanData.setLabel(fz.getName());
        zaehldatenJeQuerungsverkehr.forEach((querungsverkehr, tupelTageswertZaehldatum) -> addValueAndSum(querungsverkehr,
                BigDecimal.valueOf(Objects.requireNonNullElse(reader.apply(tupelTageswertZaehldatum.getLadeZaehldatum()), 0)),
                belastungsplanData));
        return belastungsplanData;
    }

    /**
     * Setzt den Zählwert anhand des {@link Querungsverkehr} an die richtige Stelle des
     * {@link BelastungsplanQuDataDto} und addiert ihn zur Summe des entsprechenden Knotenarms.
     */
    private void addValueAndSum(
            final Querungsverkehr querungsverkehr,
            final BigDecimal value,
            final BelastungsplanQuDataDto belastungsplanData) {
        final BelastungsplanQuDataDto.QuKnotenarmValue quKnotenarmValue = belastungsplanData.getValuesKnotenarme().stream()
                .filter(kn -> Objects.equals(kn.getKnotenarm(), querungsverkehr.getKnotenarm())).findFirst()
                .orElseGet(() -> {
                    final var kv = new BelastungsplanQuDataDto.QuKnotenarmValue(querungsverkehr.getKnotenarm(), new ArrayList<>());
                    belastungsplanData.getValuesKnotenarme().add(kv);
                    return kv;
                });
        Optional<BelastungsplanQuDataDto.QuerungsverkehrValue> querungsverkehrValue = quKnotenarmValue.getValuesQuerungsverkehre().stream()
                .filter(qv -> qv.getRichtung() == querungsverkehr.getRichtung()).findFirst();
        if (querungsverkehrValue.isPresent()) {
            log.error("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen");
            throw new IllegalStateException("Fehler beim Berechnen der Daten");
        } else {
            quKnotenarmValue.getValuesQuerungsverkehre().add(new BelastungsplanQuDataDto.QuerungsverkehrValue(querungsverkehr.getRichtung(), value));
            // Summiere Knotenarm
            if (quKnotenarmValue.getSumKnotenarm() == null) {
                quKnotenarmValue.setSumKnotenarm(value);
            } else {
                quKnotenarmValue.setSumKnotenarm(quKnotenarmValue.getSumKnotenarm().add(value));
            }
        }
    }
}
