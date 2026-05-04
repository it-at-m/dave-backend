package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanQjsDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanQjsDTO;
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
public class BelastungsplanDataQjsService extends AbstractBelastungsplanDataService {

    public AbstractBelastungsplanDataDTO getEmptyBelastungsplanData() {
        final BelastungsplanQjsDataDTO data = new BelastungsplanQjsDataDTO();
        fillEmptyBelastungsplanData(data);
        data.setSumAll(BigDecimal.ZERO);
        data.setValuesStrassenseite(new ArrayList<>());
        data.setValuesVerkehrsbeziehungen(new ArrayList<>());
        return data;
    }

    public AbstractLadeBelastungsplanDTO<?> buildLadeBelastungsplanDTO(final OptionsDTO options,
            final Zaehlung zaehlung,
            final List<Zeitintervall> zeitintervalle) {
        Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan = MappingUtil
                .mapVerkehrsbeziehungen(options, zaehlung, zeitintervalle);

        var ladeBelastungsplan = new LadeBelastungsplanQjsDTO();
        ladeBelastungsplan.setStreets(new String[8]);
        (ladeBelastungsplan).setValue1((BelastungsplanQjsDataDTO) getEmptyBelastungsplanData());
        (ladeBelastungsplan).setValue2((BelastungsplanQjsDataDTO) getEmptyBelastungsplanData());
        (ladeBelastungsplan).setValue3((BelastungsplanQjsDataDTO) getEmptyBelastungsplanData());

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
            final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeVerkehrsbeziehung,
            final Zaehlung zaehlung) {
        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> returnValue = new HashMap<>();

        if (zaehlung.getKategorien().contains(Fahrzeug.RAD)) {
            returnValue.put(Fahrzeug.RAD,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.RAD, LadeZaehldatumDTO::getFahrradfahrer, zaehldatenJeVerkehrsbeziehung));
        }
        if (zaehlung.getKategorien().contains(Fahrzeug.FUSS)) {
            returnValue.put(Fahrzeug.FUSS,
                    buildBelastungsplanDataDTOForFahrzeug(Fahrzeug.FUSS, LadeZaehldatumDTO::getFussgaenger, zaehldatenJeVerkehrsbeziehung));
        }
        return returnValue;
    }

    private AbstractBelastungsplanDataDTO buildBelastungsplanDataDTOForFahrzeug(
            final Fahrzeug fz,
            final Function<LadeZaehldatumDTO, Integer> reader,
            final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeVerkehrsbeziehung) {
        final BelastungsplanQjsDataDTO belastungsplanData = (BelastungsplanQjsDataDTO) getEmptyBelastungsplanData();
        belastungsplanData.setFilled(true);
        belastungsplanData.setLabel(fz.getName());
        zaehldatenJeVerkehrsbeziehung.forEach((verkehrsbeziehung, tupelTageswertZaehldatum) -> {
            checkForDuplicates(belastungsplanData, verkehrsbeziehung);
            var value = new BelastungsplanQjsDataDTO.VerkehrsbeziehungValue(verkehrsbeziehung.getVon(), verkehrsbeziehung.getNach(),
                    verkehrsbeziehung.getStrassenseite(),
                    BigDecimal.valueOf(Objects.requireNonNullElse(reader.apply(tupelTageswertZaehldatum.getLadeZaehldatum()), 0)));
            belastungsplanData.getValuesVerkehrsbeziehungen().add(value);

            Optional<BelastungsplanQjsDataDTO.StrassenseiteValue> valueStrassenseite = belastungsplanData.getValuesStrassenseite().stream()
                    .filter(bez -> bez.getStrassenseite() == verkehrsbeziehung.getStrassenseite()).findFirst();
            if (valueStrassenseite.isPresent()) {
                BigDecimal oldValue = valueStrassenseite.get().getValue();
                BigDecimal newValue = oldValue
                        .add(BigDecimal.valueOf(Objects.requireNonNullElse(reader.apply(tupelTageswertZaehldatum.getLadeZaehldatum()), 0)));
                valueStrassenseite.get().setValue(newValue);
            } else {
                BelastungsplanQjsDataDTO.StrassenseiteValue valueStrassenseite2 = new BelastungsplanQjsDataDTO.StrassenseiteValue(
                        verkehrsbeziehung.getStrassenseite());
                valueStrassenseite2.setValue(BigDecimal.valueOf(Objects.requireNonNullElse(reader.apply(tupelTageswertZaehldatum.getLadeZaehldatum()), 0)));
                belastungsplanData.getValuesStrassenseite().add(valueStrassenseite2);
            }
            belastungsplanData
                    .setSumAll(belastungsplanData.getSumAll()
                            .add(BigDecimal.valueOf(Objects.requireNonNullElse(reader.apply(tupelTageswertZaehldatum.getLadeZaehldatum()), 0))));
        });
        return belastungsplanData;
    }

    private void checkForDuplicates(
            AbstractBelastungsplanDataDTO data,
            Verkehrsbeziehung verkehrsbeziehung) {
        if (((BelastungsplanQjsDataDTO) data).getValuesVerkehrsbeziehungen().stream().anyMatch(bez -> (bez.getVon() == verkehrsbeziehung.getVon())
                && (bez.getNach() == verkehrsbeziehung.getNach()) && (bez.getStrassenseite() == verkehrsbeziehung.getStrassenseite()))) {
            log.error("Fehler beim Berechnen der Daten: doppelte Verkehrsbeziehungen");
            throw new IllegalStateException("Fehler beim Berechnen der Daten");
        }
    }

}
