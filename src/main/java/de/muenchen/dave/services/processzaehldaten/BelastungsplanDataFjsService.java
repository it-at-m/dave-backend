package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanFjsDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanFjsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Service
@Slf4j
public class BelastungsplanDataFjsService extends AbstractBelastungsplanDataService{

    public AbstractBelastungsplanDataDTO getEmptyBelastungsplanData() {
        final BelastungsplanFjsDataDTO data = new BelastungsplanFjsDataDTO();
        fillEmptyBelastungsplanData(data);
        data.setSumAll(BigDecimal.ZERO);
        data.setValuesStrassenseite(new ArrayList<>());
        data.setValuesLaengsverkehr(new ArrayList<>());
        return data;
    }

    public AbstractLadeBelastungsplanDTO<?> buildBelastungsplanData(final OptionsDTO options,
                                                                       final Zaehlung zaehlung,
                                                                       final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan) {
        var ladeBelastungsplan = new LadeBelastungsplanFjsDTO();
        ladeBelastungsplan.setStreets(new String[8]);

        (ladeBelastungsplan).setValue1((BelastungsplanFjsDataDTO) getEmptyBelastungsplanData());
        (ladeBelastungsplan).setValue2((BelastungsplanFjsDataDTO) getEmptyBelastungsplanData());
        (ladeBelastungsplan).setValue3((BelastungsplanFjsDataDTO) getEmptyBelastungsplanData());
        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> belastungsplanData = getBelastungsplanData(ladeZaehldatumBelastungsplan, zaehlung);
        zaehlung.getKnotenarme().forEach(knotenarm -> ladeBelastungsplan.getStreets()[knotenarm.getNummer() - 1] = knotenarm.getStrassenname());
        if (options.getRadverkehr() && belastungsplanData.containsKey(Fahrzeug.RAD)) {
            putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.RAD);
        } else if (options.getFussverkehr() && belastungsplanData.containsKey(Fahrzeug.FUSS)) {
            putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.FUSS);
        }
        markKIHochrechnung(zaehlung.getZaehldauer(), options.getZeitauswahl(), ladeBelastungsplan);
        return ladeBelastungsplan;
    }

    public Map<Fahrzeug, AbstractBelastungsplanDataDTO> getBelastungsplanData(
            final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeVerkehrsbeziehung,
            final Zaehlung zaehlung) {
        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> returnValue = new HashMap<>();

        if (zaehlung.getKategorien().contains(Fahrzeug.RAD)) {
            returnValue.put(Fahrzeug.RAD,
                    buildBelastungsplanDataForFahrzeug(Fahrzeug.RAD, LadeZaehldatumDTO::getFahrradfahrer, zaehldatenJeVerkehrsbeziehung));
        }
        if (zaehlung.getKategorien().contains(Fahrzeug.FUSS)) {
            returnValue.put(Fahrzeug.FUSS,
                    buildBelastungsplanDataForFahrzeug(Fahrzeug.FUSS, LadeZaehldatumDTO::getFussgaenger, zaehldatenJeVerkehrsbeziehung));
        }
        return returnValue;
    }

    public BelastungsplanFjsDataDTO buildBelastungsplanDataForFahrzeug(
            final Fahrzeug fz,
            final Function<LadeZaehldatumDTO, Integer> reader,
            final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeVerkehrsbeziehung) {
        final BelastungsplanFjsDataDTO belastungsplanData = (BelastungsplanFjsDataDTO) getEmptyBelastungsplanData();
        belastungsplanData.setFilled(true);
        belastungsplanData.setLabel(fz.getName());
        belastungsplanData.setSumAll(BigDecimal.ZERO);
        belastungsplanData.setValuesStrassenseite(new ArrayList<>());
        zaehldatenJeVerkehrsbeziehung.forEach((verkehrsbeziehung, tupelTageswertZaehldatum) -> {
            checkForDuplicates(belastungsplanData, verkehrsbeziehung);
            var value = new BelastungsplanFjsDataDTO.LaengsverkehrValue(
                    verkehrsbeziehung.getVon(),
                    verkehrsbeziehung.getStrassenseite(),
                    BigDecimal.valueOf(Objects.requireNonNullElse(reader.apply(tupelTageswertZaehldatum.getLadeZaehldatum()), 0)));
            belastungsplanData.getValuesLaengsverkehr().add(value);

            Optional<BelastungsplanFjsDataDTO.StrassenseiteValue> valueStrassenseite = belastungsplanData.getValuesStrassenseite().stream()
                    .filter(bez -> bez.getStrassenseite() == verkehrsbeziehung.getStrassenseite()).findFirst();
            if (valueStrassenseite.isPresent()) {
                BigDecimal oldValue = valueStrassenseite.get().getValue();
                BigDecimal newValue = oldValue
                        .add(BigDecimal.valueOf(Objects.requireNonNullElse(reader.apply(tupelTageswertZaehldatum.getLadeZaehldatum()), 0)));
                valueStrassenseite.get().setValue(newValue);
            } else {
                BelastungsplanFjsDataDTO.StrassenseiteValue valueStrassenseite2 = new BelastungsplanFjsDataDTO.StrassenseiteValue(
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

    public void checkForDuplicates(
            AbstractBelastungsplanDataDTO data,
            Verkehrsbeziehung verkehrsbeziehung) {
        if (((BelastungsplanFjsDataDTO)data).getValuesLaengsverkehr().stream().anyMatch(bez -> (bez.getVon() == verkehrsbeziehung.getVon())
                && (bez.getStrassenseite() == verkehrsbeziehung.getStrassenseite()))) {
            log.error("Fehler beim Berechnen der Daten: doppelter Laengsverkehr");
            throw new IllegalStateException("Fehler beim Berechnen der Daten");
        }
    }


}
