package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanFjsDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanFjsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
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

    public AbstractLadeBelastungsplanDTO<?> buildLadeBelastungsplanDTO(
            final OptionsDTO options,
            final Zaehlung zaehlung,
            final List<Zeitintervall> zeitintervalle) {
        Map<Laengsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = MappingUtil.mapLaengsverkehre(
                options,
                zaehlung, zeitintervalle);

        var ladeBelastungsplan = new LadeBelastungsplanFjsDTO();
        ladeBelastungsplan.setStreets(new String[8]);
        (ladeBelastungsplan).setValue1(getEmptyBelastungsplanData());
        (ladeBelastungsplan).setValue2(getEmptyBelastungsplanData());
        (ladeBelastungsplan).setValue3(getEmptyBelastungsplanData());

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
            Map<Laengsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeLaengsverkehr,
            final Zaehlung zaehlung) {
        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> returnValue = new HashMap<>();

        if (zaehlung.getKategorien().contains(Fahrzeug.RAD)) {
            returnValue.put(Fahrzeug.RAD,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.RAD, LadeZaehldatumDTO::getFahrradfahrer, zaehldatenJeLaengsverkehr));
        }
        if (zaehlung.getKategorien().contains(Fahrzeug.FUSS)) {
            returnValue.put(Fahrzeug.FUSS,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.FUSS, LadeZaehldatumDTO::getFussgaenger, zaehldatenJeLaengsverkehr));
        }
        return returnValue;
    }

    private BelastungsplanFjsDataDTO getEmptyBelastungsplanData() {
        final BelastungsplanFjsDataDTO data = new BelastungsplanFjsDataDTO();
        fillEmptyBelastungsplanData(data);
        data.setValuesKnotenarme(new ArrayList<>());
        return data;
    }

    private BelastungsplanFjsDataDTO buildBelastungsplanDataDTOForFahrzeug(
            final Fahrzeug fz,
            final Function<LadeZaehldatumDTO, Integer> reader,
            final Map<Laengsverkehr, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeLaengsverkehr) {
        final BelastungsplanFjsDataDTO belastungsplanData = getEmptyBelastungsplanData();
        belastungsplanData.setFilled(true);
        belastungsplanData.setLabel(fz.getName());
        zaehldatenJeLaengsverkehr.forEach((laengsverkehr, tupelTageswertZaehldatum) -> addValueAndSum(laengsverkehr,
                BigDecimal.valueOf(Objects.requireNonNullElse(reader.apply(tupelTageswertZaehldatum.getLadeZaehldatum()), 0)),
                belastungsplanData));
        return belastungsplanData;
    }

    /**
     * Setzt den Zählwert anhand des {@link Laengsverkehr} an die richtige Stelle des {@link BelastungsplanFjsDataDTO}
     * und addiert ihn zur Summe von entsprechender Strassenseite und Knotenarm.
     */
    private void addValueAndSum(
            final Laengsverkehr laengsverkehr,
            final BigDecimal value,
            final BelastungsplanFjsDataDTO belastungsplanData) {
        final BelastungsplanFjsDataDTO.KnotenarmValue knotenarmValue = belastungsplanData.getValuesKnotenarme().stream()
                .filter(kn -> Objects.equals(kn.getKnotenarm(), laengsverkehr.getKnotenarm())).findFirst()
                .orElseGet(() -> {
                    final var kv = new BelastungsplanFjsDataDTO.KnotenarmValue(laengsverkehr.getKnotenarm(), new ArrayList<>());
                    belastungsplanData.getValuesKnotenarme().add(kv);
                    return kv;
                });
        final BelastungsplanFjsDataDTO.StrassenseiteValue seiteValue = knotenarmValue.getValuesStrassenseiten().stream()
                .filter(seite -> seite.getStrassenseite() == laengsverkehr.getStrassenseite()).findFirst()
                .orElseGet(() -> {
                    final var sv = new BelastungsplanFjsDataDTO.StrassenseiteValue(laengsverkehr.getStrassenseite(), new ArrayList<>());
                    knotenarmValue.getValuesStrassenseiten().add(sv);
                    return sv;
                });
        Optional<BelastungsplanFjsDataDTO.LaengsverkehrValue> laengsverkehrValue = seiteValue.getValuesLaengsverkehre().stream()
                .filter(lv -> lv.getRichtung() == laengsverkehr.getRichtung()).findFirst();
        if (laengsverkehrValue.isPresent()) {
            log.error("Fehler beim Berechnen der Daten: doppelte Bewegungsbeziehungen");
            throw new IllegalStateException("Fehler beim Berechnen der Daten");
        } else {
            seiteValue.getValuesLaengsverkehre().add(new BelastungsplanFjsDataDTO.LaengsverkehrValue(laengsverkehr.getRichtung(), value));
            // Summiere Strassenseite
            if (seiteValue.getSumStrassenseite() == null) {
                seiteValue.setSumStrassenseite(value);
            } else {
                seiteValue.setSumStrassenseite(seiteValue.getSumStrassenseite().add(value));
            }
            // Summiere Knotenarm
            if (knotenarmValue.getSumKnotenarm() == null) {
                knotenarmValue.setSumKnotenarm(value);
            } else {
                knotenarmValue.setSumKnotenarm(knotenarmValue.getSumKnotenarm().add(value));
            }
        }
    }
}
