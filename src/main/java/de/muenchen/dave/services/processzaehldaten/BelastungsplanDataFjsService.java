package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDto;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDto;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDto;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanFjsDataDto;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanFjsDto;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDto;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BelastungsplanDataFjsService extends AbstractBelastungsplanDataService {

    public AbstractLadeBelastungsplanDto<?> buildLadeBelastungsplanDTO(
            final OptionsDto options,
            final Zaehlung zaehlung,
            final List<Zeitintervall> zeitintervalle) {
        Map<Laengsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = MappingUtil.mapLaengsverkehre(
                options,
                zaehlung, zeitintervalle);

        var ladeBelastungsplan = new LadeBelastungsplanFjsDto();
        ladeBelastungsplan.setStreets(new String[8]);
        (ladeBelastungsplan).setValue1(getEmptyBelastungsplanData());
        (ladeBelastungsplan).setValue2(getEmptyBelastungsplanData());
        (ladeBelastungsplan).setValue3(getEmptyBelastungsplanData());

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
            Map<Laengsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeLaengsverkehr,
            final Zaehlung zaehlung) {
        final Map<Fahrzeug, AbstractBelastungsplanDataDto> returnValue = new HashMap<>();

        if (zaehlung.getKategorien().contains(Fahrzeug.RAD)) {
            returnValue.put(Fahrzeug.RAD,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.RAD, LadeZaehldatumDto::getFahrradfahrer, zaehldatenJeLaengsverkehr));
        }
        if (zaehlung.getKategorien().contains(Fahrzeug.FUSS)) {
            returnValue.put(Fahrzeug.FUSS,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.FUSS, LadeZaehldatumDto::getFussgaenger, zaehldatenJeLaengsverkehr));
        }
        return returnValue;
    }

    private BelastungsplanFjsDataDto getEmptyBelastungsplanData() {
        final BelastungsplanFjsDataDto data = new BelastungsplanFjsDataDto();
        fillEmptyBelastungsplanData(data);
        data.setValuesKnotenarme(new ArrayList<>());
        return data;
    }

    private BelastungsplanFjsDataDto buildBelastungsplanDataDTOForFahrzeug(
            final Fahrzeug fz,
            final Function<LadeZaehldatumDto, Integer> reader,
            final Map<Laengsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeLaengsverkehr) {
        final BelastungsplanFjsDataDto belastungsplanData = getEmptyBelastungsplanData();
        belastungsplanData.setFilled(true);
        belastungsplanData.setLabel(fz.getName());
        zaehldatenJeLaengsverkehr.forEach((laengsverkehr, tupelTageswertZaehldatum) -> addValueAndSum(laengsverkehr,
                BigDecimal.valueOf(Objects.requireNonNullElse(reader.apply(tupelTageswertZaehldatum.getLadeZaehldatum()), 0)),
                belastungsplanData));
        return belastungsplanData;
    }

    /**
     * Setzt den Zählwert anhand des {@link Laengsverkehr} an die richtige Stelle des
     * {@link BelastungsplanFjsDataDto}
     * und addiert ihn zur Summe von entsprechender Strassenseite und Knotenarm.
     */
    private void addValueAndSum(
            final Laengsverkehr laengsverkehr,
            final BigDecimal value,
            final BelastungsplanFjsDataDto belastungsplanData) {
        final BelastungsplanFjsDataDto.FjsKnotenarmValue fjsKnotenarmValue = belastungsplanData.getValuesKnotenarme().stream()
                .filter(kn -> Objects.equals(kn.getKnotenarm(), laengsverkehr.getKnotenarm())).findFirst()
                .orElseGet(() -> {
                    final var kv = new BelastungsplanFjsDataDto.FjsKnotenarmValue(laengsverkehr.getKnotenarm(), new ArrayList<>());
                    belastungsplanData.getValuesKnotenarme().add(kv);
                    return kv;
                });
        final BelastungsplanFjsDataDto.FjsStrassenseiteValue seiteValue = fjsKnotenarmValue.getValuesStrassenseiten().stream()
                .filter(seite -> seite.getStrassenseite() == laengsverkehr.getStrassenseite()).findFirst()
                .orElseGet(() -> {
                    final var sv = new BelastungsplanFjsDataDto.FjsStrassenseiteValue(laengsverkehr.getStrassenseite(), new ArrayList<>());
                    fjsKnotenarmValue.getValuesStrassenseiten().add(sv);
                    return sv;
                });
        Optional<BelastungsplanFjsDataDto.LaengsverkehrValue> laengsverkehrValue = seiteValue.getValuesLaengsverkehre().stream()
                .filter(lv -> lv.getRichtung() == laengsverkehr.getRichtung()).findFirst();
        if (laengsverkehrValue.isPresent()) {
            log.error("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen");
            throw new IllegalStateException("Fehler beim Berechnen der Daten");
        } else {
            seiteValue.getValuesLaengsverkehre().add(new BelastungsplanFjsDataDto.LaengsverkehrValue(laengsverkehr.getRichtung(), value));
            // Summiere Strassenseite
            if (seiteValue.getSumStrassenseite() == null) {
                seiteValue.setSumStrassenseite(value);
            } else {
                seiteValue.setSumStrassenseite(seiteValue.getSumStrassenseite().add(value));
            }
            // Summiere Knotenarm
            if (fjsKnotenarmValue.getSumKnotenarm() == null) {
                fjsKnotenarmValue.setSumKnotenarm(value);
            } else {
                fjsKnotenarmValue.setSumKnotenarm(fjsKnotenarmValue.getSumKnotenarm().add(value));
            }
        }
    }
}
