package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDto;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDto;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDto;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanQjsDataDto;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanQjsDto;
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
public class BelastungsplanDataQjsService extends AbstractBelastungsplanDataService {

    public AbstractBelastungsplanDataDto getEmptyBelastungsplanData() {
        final BelastungsplanQjsDataDto data = new BelastungsplanQjsDataDto();
        fillEmptyBelastungsplanData(data);
        data.setSumAll(BigDecimal.ZERO);
        data.setValuesStrassenseite(new ArrayList<>());
        data.setValuesVerkehrsbeziehungen(new ArrayList<>());
        return data;
    }

    public AbstractLadeBelastungsplanDto<?> buildLadeBelastungsplanDTO(
            final OptionsDto options,
            final Zaehlung zaehlung,
            final List<Zeitintervall> zeitintervalle) {
        Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = MappingUtil
                .mapVerkehrsbeziehungen(options, zaehlung, zeitintervalle);

        var ladeBelastungsplan = new LadeBelastungsplanQjsDto();
        ladeBelastungsplan.setStreets(new String[8]);
        ladeBelastungsplan.setValue1((BelastungsplanQjsDataDto) getEmptyBelastungsplanData());
        ladeBelastungsplan.setValue2((BelastungsplanQjsDataDto) getEmptyBelastungsplanData());
        ladeBelastungsplan.setValue3((BelastungsplanQjsDataDto) getEmptyBelastungsplanData());

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
            final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeVerkehrsbeziehung,
            final Zaehlung zaehlung) {
        final Map<Fahrzeug, AbstractBelastungsplanDataDto> returnValue = new HashMap<>();

        if (zaehlung.getKategorien().contains(Fahrzeug.RAD)) {
            returnValue.put(Fahrzeug.RAD,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.RAD, LadeZaehldatumDto::getFahrradfahrer, zaehldatenJeVerkehrsbeziehung));
        }
        if (zaehlung.getKategorien().contains(Fahrzeug.FUSS)) {
            returnValue.put(Fahrzeug.FUSS,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.FUSS, LadeZaehldatumDto::getFussgaenger, zaehldatenJeVerkehrsbeziehung));
        }
        return returnValue;
    }

    private AbstractBelastungsplanDataDto buildBelastungsplanDataDTOForFahrzeug(
            final Fahrzeug fz,
            final Function<LadeZaehldatumDto, Integer> reader,
            final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeVerkehrsbeziehung) {
        final BelastungsplanQjsDataDto belastungsplanData = (BelastungsplanQjsDataDto) getEmptyBelastungsplanData();
        belastungsplanData.setFilled(true);
        belastungsplanData.setLabel(fz.getName());
        zaehldatenJeVerkehrsbeziehung.forEach((verkehrsbeziehung, tupelTageswertZaehldatum) -> {
            final BigDecimal currentValue = BigDecimal
                    .valueOf(Objects.requireNonNullElse(reader.apply(tupelTageswertZaehldatum.getLadeZaehldatum()), 0));
            var value = new BelastungsplanQjsDataDto.VerkehrsbeziehungValue(verkehrsbeziehung.getVon(), verkehrsbeziehung.getNach(),
                    verkehrsbeziehung.getStrassenseite(), currentValue);
            belastungsplanData.getValuesVerkehrsbeziehungen().add(value);

            Optional<BelastungsplanQjsDataDto.QjsStrassenseiteValue> valueStrassenseite = belastungsplanData.getValuesStrassenseite().stream()
                    .filter(bez -> bez.getStrassenseite() == verkehrsbeziehung.getStrassenseite()).findFirst();
            if (valueStrassenseite.isPresent()) {
                valueStrassenseite.get().setValue(valueStrassenseite.get().getValue().add(currentValue));
            } else {
                BelastungsplanQjsDataDto.QjsStrassenseiteValue valueStrassenseite2 = new BelastungsplanQjsDataDto.QjsStrassenseiteValue(
                        verkehrsbeziehung.getStrassenseite());
                valueStrassenseite2.setValue(currentValue);
                belastungsplanData.getValuesStrassenseite().add(valueStrassenseite2);
            }
            belastungsplanData.setSumAll(belastungsplanData.getSumAll().add(currentValue));
        });
        return belastungsplanData;
    }

}
