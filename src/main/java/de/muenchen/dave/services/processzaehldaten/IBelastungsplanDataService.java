package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;

import java.util.Map;
import java.util.function.Function;

public interface IBelastungsplanDataService {

    AbstractBelastungsplanDataDTO getEmptyBelastungsplanData();

    AbstractLadeBelastungsplanDTO<?> buildBelastungsplanData(final OptionsDTO options,
                                                                final Zaehlung zaehlung,
                                                                final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan);

    Map<Fahrzeug, AbstractBelastungsplanDataDTO> getBelastungsplanData(
            final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeVerkehrsbeziehung,
            final Zaehlung zaehlung);

//    AbstractBelastungsplanDataDTO buildBelastungsplanDataForFahrzeug(
//            final Fahrzeug fz,
//            final Function<LadeZaehldatumDTO, Integer> reader,
//            final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeVerkehrsbeziehung);

//    void checkForDuplicates(
//            AbstractBelastungsplanDataDTO belastungsplanDataDTO,
//            Verkehrsbeziehung verkehrsbeziehung);

    }
