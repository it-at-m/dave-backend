package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanQjsDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.util.BelastungsplanCalculator;
import de.muenchen.dave.util.CalculationUtil;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BelastungsplanDataDefaultService extends AbstractBelastungsplanDataService{

    public BelastungsplanDataDTO getEmptyBelastungsplanData() {
        final BelastungsplanDataDTO data = new BelastungsplanDataDTO();
        fillEmptyBelastungsplanData(data);
        data.setPercent(false);
        data.setValues(getEmptyDatastructure());
        return data;
    }

    public AbstractLadeBelastungsplanDTO<?> buildBelastungsplanData(final OptionsDTO options,
                                                                    final Zaehlung zaehlung, final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> ladeZaehldatumBelastungsplan) {
        var ladeBelastungsplan = new LadeBelastungsplanDTO();
        ladeBelastungsplan.setStreets(new String[8]);
        (ladeBelastungsplan).setValue1(getEmptyBelastungsplanData());
        (ladeBelastungsplan).setValue2(getEmptyBelastungsplanData());
        (ladeBelastungsplan).setValue3(getEmptyBelastungsplanData());

        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> belastungsplanData = getBelastungsplanData(ladeZaehldatumBelastungsplan, zaehlung);
        zaehlung.getKnotenarme().forEach(knotenarm -> ladeBelastungsplan.getStreets()[knotenarm.getNummer() - 1] = knotenarm.getStrassenname());
        ladeBelastungsplan.setKreisverkehr(zaehlung.getKreisverkehr());

        // Wenn KFZ, GV, SV, GV_Prozent oder SV_Prozent gesetzt ist, dürfen RAD und FUSS nicht angezeigt werden
        if ((options.getGueterverkehrsanteilProzent() && belastungsplanData.containsKey(Fahrzeug.GV_P))
                || (options.getSchwerverkehrsanteilProzent() && belastungsplanData.containsKey(Fahrzeug.SV_P))
                || (options.getGueterverkehr() && belastungsplanData.containsKey(Fahrzeug.GV))
                || (options.getSchwerverkehr() && belastungsplanData.containsKey(Fahrzeug.SV))
                || (options.getKraftfahrzeugverkehr() && belastungsplanData.containsKey(Fahrzeug.KFZ))) {
            if (options.getGueterverkehrsanteilProzent() && belastungsplanData.containsKey(Fahrzeug.GV_P)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.GV_P);
            }
            if (options.getSchwerverkehrsanteilProzent() && belastungsplanData.containsKey(Fahrzeug.SV_P)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.SV_P);
            }
            if (options.getGueterverkehr() && belastungsplanData.containsKey(Fahrzeug.GV)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.GV);
            }
            if (options.getSchwerverkehr() && belastungsplanData.containsKey(Fahrzeug.SV)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.SV);
            }
            if (options.getKraftfahrzeugverkehr() && belastungsplanData.containsKey(Fahrzeug.KFZ)) {
                putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.KFZ);
            }
        } else if (options.getRadverkehr() && belastungsplanData.containsKey(Fahrzeug.RAD)) {
            putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.RAD);
        } else if (options.getFussverkehr() && belastungsplanData.containsKey(Fahrzeug.FUSS)) {
            putFirstValueInBelastungsplan(ladeBelastungsplan, belastungsplanData, Fahrzeug.FUSS);
        }

        LadeBelastungsplanDTO ladeBelastungsplanSum = this.calculateSumsForLadeBelastungsplanDto(ladeBelastungsplan,
                (BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.KFZ),
                (BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.SV), (BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.GV));

        markKIHochrechnung(zaehlung.getZaehldauer(), options.getZeitauswahl(), ladeBelastungsplanSum);

        return ladeBelastungsplanSum;
    }

    /**
     * Liefert eine {@link BelastungsplanDataDTO} pro Fahrzeugklasse mit den Daten für den
     * Belastungsplan
     *
     * @param zaehldatenJeVerkehrsbeziehung aus der DB ermittelten Werte
     * @param zaehlung wird benötigt zur überprüfung, ob welche Fahrzeug gezählt wurden
     * @return eine Map mit Key: Fahrzeug und Value:BelastungsplanDataDTO.
     */
    public Map<Fahrzeug, AbstractBelastungsplanDataDTO> getBelastungsplanData(
            final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeVerkehrsbeziehung,
            final Zaehlung zaehlung) {
        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> returnValue = new HashMap<>();

        final BelastungsplanDataDTO belastungsplanDataKfz = new BelastungsplanDataDTO();
        belastungsplanDataKfz.setFilled(zaehlung.getKategorien().contains(Fahrzeug.KFZ));
        belastungsplanDataKfz.setPercent(false);
        belastungsplanDataKfz.setLabel(Fahrzeug.KFZ.getName());
        belastungsplanDataKfz.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataSv = new BelastungsplanDataDTO();
        belastungsplanDataSv.setFilled(zaehlung.getKategorien().contains(Fahrzeug.SV));
        belastungsplanDataSv.setPercent(false);
        belastungsplanDataSv.setLabel(Fahrzeug.SV.getName());
        belastungsplanDataSv.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataGv = new BelastungsplanDataDTO();
        belastungsplanDataGv.setFilled(zaehlung.getKategorien().contains(Fahrzeug.GV));
        belastungsplanDataGv.setPercent(false);
        belastungsplanDataGv.setLabel(Fahrzeug.GV.getName());
        belastungsplanDataGv.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataRad = new BelastungsplanDataDTO();
        belastungsplanDataRad.setFilled(zaehlung.getKategorien().contains(Fahrzeug.RAD));
        belastungsplanDataRad.setPercent(false);
        belastungsplanDataRad.setLabel(Fahrzeug.RAD.getName());
        belastungsplanDataRad.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataFuss = new BelastungsplanDataDTO();
        belastungsplanDataFuss.setFilled(zaehlung.getKategorien().contains(Fahrzeug.FUSS));
        belastungsplanDataFuss.setPercent(false);
        belastungsplanDataFuss.setLabel(Fahrzeug.FUSS.getName());
        belastungsplanDataFuss.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataSvProzent = new BelastungsplanDataDTO();
        belastungsplanDataSvProzent.setFilled(belastungsplanDataKfz.isFilled() && belastungsplanDataSv.isFilled());
        belastungsplanDataSvProzent.setPercent(true);
        belastungsplanDataSvProzent.setLabel(Fahrzeug.SV_P.getName());
        belastungsplanDataSvProzent.setValues(getEmptyDatastructure());

        final BelastungsplanDataDTO belastungsplanDataGvProzent = new BelastungsplanDataDTO();
        belastungsplanDataGvProzent.setFilled(belastungsplanDataKfz.isFilled() && belastungsplanDataGv.isFilled());
        belastungsplanDataGvProzent.setPercent(true);
        belastungsplanDataGvProzent.setLabel(Fahrzeug.GV_P.getName());
        belastungsplanDataGvProzent.setValues(getEmptyDatastructure());

        zaehldatenJeVerkehrsbeziehung.forEach((verkehrsbeziehung, tupelTageswertZaehldatum) -> {
            final int index1;
            final int index2;
            if (isKreisverkehr(verkehrsbeziehung)) {
                // Von-Knotennummer - 1
                index1 = verkehrsbeziehung.getVon() - 1;
                // HINEIN = 0, VORBEI = 1, HERAUS = 2
                index2 = switch (verkehrsbeziehung.getFahrbewegungKreisverkehr()) {
                    case HINEIN -> 0;
                    case VORBEI -> 1;
                    case HERAUS -> 2;
                };
            } else {
                index1 = verkehrsbeziehung.getVon() - 1;
                index2 = verkehrsbeziehung.getNach() - 1;
            }

            belastungsplanDataKfz.getValues()[index1][index2] = tupelTageswertZaehldatum.getLadeZaehldatum().getKfz();
            belastungsplanDataSv.getValues()[index1][index2] = tupelTageswertZaehldatum.getLadeZaehldatum().getSchwerverkehr();
            belastungsplanDataGv.getValues()[index1][index2] = tupelTageswertZaehldatum.getLadeZaehldatum().getGueterverkehr();

            if (belastungsplanDataSvProzent.isFilled()) {
                belastungsplanDataSvProzent.getValues()[index1][index2] = CalculationUtil
                        .calculateAnteilProzent(belastungsplanDataSv.getValues()[index1][index2], belastungsplanDataKfz.getValues()[index1][index2]);
            }
            if (belastungsplanDataGvProzent.isFilled()) {
                belastungsplanDataGvProzent.getValues()[index1][index2] = CalculationUtil
                        .calculateAnteilProzent(belastungsplanDataGv.getValues()[index1][index2], belastungsplanDataKfz.getValues()[index1][index2]);
            }

            belastungsplanDataRad.getValues()[index1][index2] = BigDecimal.valueOf(
                    Objects.requireNonNullElse(
                            tupelTageswertZaehldatum.getLadeZaehldatum().getFahrradfahrer(),
                            0));

            if (!tupelTageswertZaehldatum.getIsTageswert()) {
                belastungsplanDataFuss.getValues()[index1][index2] = BigDecimal.valueOf(
                        Objects.requireNonNullElse(
                                tupelTageswertZaehldatum.getLadeZaehldatum().getFussgaenger(),
                                0));
            }
        });

        if (belastungsplanDataKfz.isFilled()) {
            returnValue.put(Fahrzeug.KFZ, belastungsplanDataKfz);
        }
        if (belastungsplanDataSv.isFilled()) {
            returnValue.put(Fahrzeug.SV, belastungsplanDataSv);
        }
        if (belastungsplanDataGv.isFilled()) {
            returnValue.put(Fahrzeug.GV, belastungsplanDataGv);
        }
        if (belastungsplanDataRad.isFilled()) {
            returnValue.put(Fahrzeug.RAD, belastungsplanDataRad);
        }
        if (belastungsplanDataFuss.isFilled()) {
            returnValue.put(Fahrzeug.FUSS, belastungsplanDataFuss);
        }
        if (belastungsplanDataSvProzent.isFilled()) {
            returnValue.put(Fahrzeug.SV_P, belastungsplanDataSvProzent);
        }
        if (belastungsplanDataGvProzent.isFilled()) {
            returnValue.put(Fahrzeug.GV_P, belastungsplanDataGvProzent);
        }
        return returnValue;
    }

    /**
     * Reichert das übergebene LadeBelastungsplanDTO-Objekt um die Summen der einzelnen Knotenarme an.
     *
     * @param ladeBelastungsplan LadeBelastungsplanDTO-Objekt, welches um die Summen angereichert werden
     *            soll
     * @param dataKfz Datengrundlage von KFZ zur Berechnung der %-Anteile
     * @param dataSv Datengrundlage von SV zur Berechnung der SV%-Anteile
     * @param dataGv Datengrundlage von GV zur Berechnung der GV%-Anteile
     * @return gibt das um alle Summen erweiterte LadeBelastungsplanDTO-Objekt zurück
     */
    private LadeBelastungsplanDTO calculateSumsForLadeBelastungsplanDto(final LadeBelastungsplanDTO ladeBelastungsplan, final BelastungsplanDataDTO dataKfz,
                                                                        final BelastungsplanDataDTO dataSv, final BelastungsplanDataDTO dataGv) {

        Map<String, BigDecimal[]> sumsKfz = null;
        Map<String, BigDecimal[]> sumsSv = null;
        Map<String, BigDecimal[]> sumsGv = null;

        if (dataKfz != null && dataKfz.isFilled()) {
            if (ladeBelastungsplan.isKreisverkehr()) {
                sumsKfz = BelastungsplanCalculator.calcSumsKreisverkehr(dataKfz.getValues());
            } else {
                sumsKfz = BelastungsplanCalculator.calcSumsKreuzung(dataKfz.getValues());
            }
        }
        if (dataSv != null && dataSv.isFilled()) {
            if (ladeBelastungsplan.isKreisverkehr()) {
                sumsSv = BelastungsplanCalculator.calcSumsKreisverkehr(dataSv.getValues());
            } else {
                sumsSv = BelastungsplanCalculator.calcSumsKreuzung(dataSv.getValues());
            }
        }
        if (dataGv != null && dataGv.isFilled()) {
            if (ladeBelastungsplan.isKreisverkehr()) {
                sumsGv = BelastungsplanCalculator.calcSumsKreisverkehr(dataGv.getValues());
            } else {
                sumsGv = BelastungsplanCalculator.calcSumsKreuzung(dataGv.getValues());
            }
        }

        if (ladeBelastungsplan.getValue1().isFilled()) {
            ladeBelastungsplan.setValue1(
                    BelastungsplanCalculator.calculateSumsForBelastungsplanDataDto(ladeBelastungsplan.getValue1(), sumsKfz, sumsSv, sumsGv,
                            ladeBelastungsplan.isKreisverkehr()));
        }

        if (ladeBelastungsplan.getValue2().isFilled()) {
            ladeBelastungsplan.setValue2(
                    BelastungsplanCalculator.calculateSumsForBelastungsplanDataDto(ladeBelastungsplan.getValue2(), sumsKfz, sumsSv, sumsGv,
                            ladeBelastungsplan.isKreisverkehr()));
        }

        if (ladeBelastungsplan.getValue3().isFilled()) {
            ladeBelastungsplan.setValue3(
                    BelastungsplanCalculator.calculateSumsForBelastungsplanDataDto(ladeBelastungsplan.getValue3(), sumsKfz, sumsSv, sumsGv,
                            ladeBelastungsplan.isKreisverkehr()));
        }

        return ladeBelastungsplan;
    }

    /**
     * Die Grafiken im Frontend erwarten pro Verkehrsbeziehung einen einzelnen Wert. Um an alle Werte
     * mittels Index zugreifen zu können ist ein 2-Stufiges Array
     * erforderlich. Ebene 1: Enthält alle Werte für die Von-Spuren Ebene 2: Enthält die Werte für die
     * Nach-Spur pro Von-Spur Bsp.: [ [Nach_1, Nach_2, ...,
     * Nach_8] // Von_1 ... [Nach_1, Nach_2, ..., Nach_8] // Von_8 ]
     *
     * @return mit BigDecimal.ZERO initialisierte Datenstruktur
     */
    private static BigDecimal[][] getEmptyDatastructure() {
        final BigDecimal[][] datastructure = new BigDecimal[8][8];
        Arrays.stream(datastructure).forEach(data -> Arrays.fill(data, BigDecimal.ZERO));
        return datastructure;
    }

    private static boolean isKreisverkehr(final Verkehrsbeziehung verkehrsbeziehung) {
        return ObjectUtils.isNotEmpty(verkehrsbeziehung.getFahrbewegungKreisverkehr())
                && ObjectUtils.isEmpty(verkehrsbeziehung.getNach());
    }


}
