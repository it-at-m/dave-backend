package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumTageswertDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.services.processzaehldaten.ProcessZaehldatenBelastungsplanService;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

public class ProcessZaehldatenBelastungsplanServiceTest {

    @Test
    public void isKreisverkehr() {
        final Verkehrsbeziehung fahrbeziehung = new Verkehrsbeziehung();

        fahrbeziehung.setVon(1);
        fahrbeziehung.setNach(null);
        fahrbeziehung.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HINEIN);
        boolean result = TestUtils.privateStaticMethodCall(
                "isKreisverkehr",
                ProcessZaehldatenBelastungsplanService.class,
                ArrayUtils.toArray(Verkehrsbeziehung.class),
                ArrayUtils.toArray(fahrbeziehung),
                Boolean.class);
        assertThat(result, is(true));

        fahrbeziehung.setVon(1);
        fahrbeziehung.setNach(null);
        fahrbeziehung.setFahrbewegungKreisverkehr(null);
        result = TestUtils.privateStaticMethodCall(
                "isKreisverkehr",
                ProcessZaehldatenBelastungsplanService.class,
                ArrayUtils.toArray(Verkehrsbeziehung.class),
                ArrayUtils.toArray(fahrbeziehung),
                Boolean.class);
        assertThat(result, is(false));

        fahrbeziehung.setVon(1);
        fahrbeziehung.setNach(2);
        fahrbeziehung.setFahrbewegungKreisverkehr(null);
        result = TestUtils.privateStaticMethodCall(
                "isKreisverkehr",
                ProcessZaehldatenBelastungsplanService.class,
                ArrayUtils.toArray(Verkehrsbeziehung.class),
                ArrayUtils.toArray(fahrbeziehung),
                Boolean.class);
        assertThat(result, is(false));

        fahrbeziehung.setVon(null);
        fahrbeziehung.setNach(null);
        fahrbeziehung.setFahrbewegungKreisverkehr(null);
        result = TestUtils.privateStaticMethodCall(
                "isKreisverkehr",
                ProcessZaehldatenBelastungsplanService.class,
                ArrayUtils.toArray(Verkehrsbeziehung.class),
                ArrayUtils.toArray(fahrbeziehung),
                Boolean.class);
        assertThat(result, is(false));
    }

    @Test
    public void getBelastunsplanData() {
        final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeFahrbeziehung = new HashMap<>();
        Verkehrsbeziehung fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setVon(2);
        fahrbeziehung.setNach(3);

        LadeZaehldatumDTO ladeZaehldatum = new LadeZaehldatumDTO();
        ladeZaehldatum.setPkw(1);
        ladeZaehldatum.setLkw(2);
        ladeZaehldatum.setLastzuege(3);
        ladeZaehldatum.setBusse(4);
        ladeZaehldatum.setKraftraeder(5);
        ladeZaehldatum.setFahrradfahrer(6);
        ladeZaehldatum.setFussgaenger(7);
        ladeZaehldatum.setPkwEinheiten(100);

        zaehldatenJeFahrbeziehung.put(fahrbeziehung, new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(false, ladeZaehldatum));

        fahrbeziehung = new Verkehrsbeziehung();
        fahrbeziehung.setVon(5);
        fahrbeziehung.setNach(2);

        ladeZaehldatum = new LadeZaehldatumDTO();
        ladeZaehldatum.setPkw(10);
        ladeZaehldatum.setLkw(20);
        ladeZaehldatum.setLastzuege(30);
        ladeZaehldatum.setBusse(40);
        ladeZaehldatum.setKraftraeder(50);
        ladeZaehldatum.setFahrradfahrer(60);
        ladeZaehldatum.setFussgaenger(70);
        ladeZaehldatum.setPkwEinheiten(1000);

        zaehldatenJeFahrbeziehung.put(fahrbeziehung, new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(false, ladeZaehldatum));

        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setKategorien(Arrays.asList(Fahrzeug.KFZ, Fahrzeug.SV, Fahrzeug.GV, Fahrzeug.SV_P, Fahrzeug.GV_P, Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.N.toString());
        zaehlung.setKreisverkehr(false);

        final Map<Fahrzeug, BelastungsplanDataDTO> belastungsplanData = new ProcessZaehldatenBelastungsplanService(null, null, null)
                .getBelastungsplanData(zaehldatenJeFahrbeziehung, zaehlung);

        assertThat(belastungsplanData.get(Fahrzeug.KFZ).getValues()[1][2], is(BigDecimal.valueOf(15)));
        assertThat(belastungsplanData.get(Fahrzeug.SV).getValues()[1][2], is(BigDecimal.valueOf(9)));
        assertThat(belastungsplanData.get(Fahrzeug.GV).getValues()[1][2], is(BigDecimal.valueOf(5)));
        assertThat(belastungsplanData.get(Fahrzeug.RAD).getValues()[1][2], is(BigDecimal.valueOf(6)));
        assertThat(belastungsplanData.get(Fahrzeug.FUSS).getValues()[1][2], is(BigDecimal.valueOf(7)));

        assertThat(belastungsplanData.get(Fahrzeug.KFZ).getValues()[4][1], is(BigDecimal.valueOf(150)));
        assertThat(belastungsplanData.get(Fahrzeug.SV).getValues()[4][1], is(BigDecimal.valueOf(90)));
        assertThat(belastungsplanData.get(Fahrzeug.GV).getValues()[4][1], is(BigDecimal.valueOf(50)));
        assertThat(belastungsplanData.get(Fahrzeug.RAD).getValues()[4][1], is(BigDecimal.valueOf(60)));
        assertThat(belastungsplanData.get(Fahrzeug.FUSS).getValues()[4][1], is(BigDecimal.valueOf(70)));

    }

    @Test
    void calculateDifferenzdatenDTO() {
        final LadeBelastungsplanDTO dto1 = new LadeBelastungsplanDTO();
        BelastungsplanDataDTO belastungsplanData = new BelastungsplanDataDTO();
        belastungsplanData.setValues(getBigDecimalTwoDimArrayAsc());
        belastungsplanData.setFilled(true);
        belastungsplanData.setLabel(Fahrzeug.KFZ.getName());
        belastungsplanData.setSum(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2),
                BigDecimal.valueOf(3), BigDecimal.valueOf(4), BigDecimal.valueOf(5),
                BigDecimal.valueOf(6), BigDecimal.valueOf(7), BigDecimal.valueOf(8) });
        belastungsplanData.setSumIn(new BigDecimal[] { BigDecimal.valueOf(11), BigDecimal.valueOf(22),
                BigDecimal.valueOf(33), BigDecimal.valueOf(44), BigDecimal.valueOf(55),
                BigDecimal.valueOf(66), BigDecimal.valueOf(77), BigDecimal.valueOf(88) });
        belastungsplanData.setSumOut(new BigDecimal[] { BigDecimal.valueOf(9), BigDecimal.valueOf(8),
                BigDecimal.valueOf(7), BigDecimal.valueOf(6), BigDecimal.valueOf(5),
                BigDecimal.valueOf(4), BigDecimal.valueOf(3), BigDecimal.valueOf(2) });
        dto1.setValue1(belastungsplanData);
        dto1.setValue2(belastungsplanData);
        dto1.setValue3(belastungsplanData);

        belastungsplanData = new BelastungsplanDataDTO();
        belastungsplanData.setValues(getBigDecimalTwoDimArrayDesc());
        belastungsplanData.setFilled(true);
        belastungsplanData.setLabel(Fahrzeug.KFZ.getName());
        belastungsplanData.setSumIn(new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2),
                BigDecimal.valueOf(3), BigDecimal.valueOf(4), BigDecimal.valueOf(5),
                BigDecimal.valueOf(6), BigDecimal.valueOf(7), BigDecimal.valueOf(8) });
        belastungsplanData.setSumOut(new BigDecimal[] { BigDecimal.valueOf(11), BigDecimal.valueOf(22),
                BigDecimal.valueOf(33), BigDecimal.valueOf(44), BigDecimal.valueOf(55),
                BigDecimal.valueOf(66), BigDecimal.valueOf(77), BigDecimal.valueOf(88) });
        belastungsplanData.setSum(new BigDecimal[] { BigDecimal.valueOf(9), BigDecimal.valueOf(8),
                BigDecimal.valueOf(7), BigDecimal.valueOf(6), BigDecimal.valueOf(5),
                BigDecimal.valueOf(4), BigDecimal.valueOf(3), BigDecimal.valueOf(2) });

        final LadeBelastungsplanDTO dto2 = new LadeBelastungsplanDTO();
        dto2.setValue1(belastungsplanData);
        dto2.setValue2(belastungsplanData);
        dto2.setValue3(belastungsplanData);

        dto1.setStreets(new String[] { "Arnulfstraße", "Joseph-Spital-Straße", "Sonnenstraße", null, null, null, "Joseph-Spital-Straße", null });
        dto2.setStreets(new String[] { "Arnulfstraße", "Joseph-Spital-Str.", "Sonnenstraße", null, "Abengauerweg", null, "Joseph-Spital-Str.", null });

        final LadeBelastungsplanDTO calculated = ProcessZaehldatenBelastungsplanService.calculateDifferenzdatenDTO(dto1, dto2);

        assertThat(calculated.getValue1().getValues(), is(getDifferenzwert()));
        assertThat(calculated.getValue2().getValues(), is(getDifferenzwert()));
        assertThat(calculated.getValue3().getValues(), is(getDifferenzwert()));
        assertThat(calculated.getStreets(),
                is(new String[] { "Arnulfstraße", "Joseph-Spital-Straße", "Sonnenstraße", null, "Abengauerweg", null, "Joseph-Spital-Straße", null }));

        // value1 === value2 === value3
        assertThat(calculated.getValue1().getSumIn(), is(new BigDecimal[] { BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(30),
                BigDecimal.valueOf(40), BigDecimal.valueOf(50), BigDecimal.valueOf(60), BigDecimal.valueOf(70), BigDecimal.valueOf(80) }));
        assertThat(calculated.getValue1().getSumOut(), is(new BigDecimal[] { BigDecimal.valueOf(-2), BigDecimal.valueOf(-14), BigDecimal.valueOf(-26),
                BigDecimal.valueOf(-38), BigDecimal.valueOf(-50), BigDecimal.valueOf(-62), BigDecimal.valueOf(-74), BigDecimal.valueOf(-86) }));
        assertThat(calculated.getValue1().getSum(), is(new BigDecimal[] { BigDecimal.valueOf(-8), BigDecimal.valueOf(-6), BigDecimal.valueOf(-4),
                BigDecimal.valueOf(-2), BigDecimal.valueOf(0), BigDecimal.valueOf(2), BigDecimal.valueOf(4), BigDecimal.valueOf(6) }));
    }

    @Test
    void subtractMatrice() {
        final BigDecimal[][] basis = getBigDecimalTwoDimArrayAsc();

        final BigDecimal[][] vergleich = new BigDecimal[4][4];
        vergleich[0] = new BigDecimal[] { BigDecimal.valueOf(16), BigDecimal.valueOf(15), BigDecimal.valueOf(14), BigDecimal.valueOf(13) };
        vergleich[1] = new BigDecimal[] { BigDecimal.valueOf(12), BigDecimal.valueOf(11), BigDecimal.valueOf(10), BigDecimal.valueOf(9) };
        vergleich[2] = new BigDecimal[] { BigDecimal.valueOf(8), BigDecimal.valueOf(7), BigDecimal.valueOf(6), BigDecimal.valueOf(5) };
        vergleich[3] = new BigDecimal[] { BigDecimal.valueOf(4), BigDecimal.valueOf(3), BigDecimal.valueOf(2), BigDecimal.valueOf(1) };

        final BigDecimal[][] differenz = ProcessZaehldatenBelastungsplanService.subtractMatrice(basis, vergleich);
        assertThat(differenz,
                is(getDifferenzwert()));
    }

    @Test
    public void roundToNearestIfRoundingIsChoosen() {
        final Integer nearestValueToRound = 100;
        final OptionsDTO options = new OptionsDTO();
        options.setWerteHundertRunden(false);
        LadeZaehldatumDTO ladeZaehldatumDTO = new LadeZaehldatumDTO();
        ladeZaehldatumDTO.setType("TEST");
        ladeZaehldatumDTO.setStartUhrzeit(LocalTime.of(8, 0));
        ladeZaehldatumDTO.setEndeUhrzeit(LocalTime.of(9, 0));
        ladeZaehldatumDTO.setPkw(151);
        ladeZaehldatumDTO.setLkw(251);
        ladeZaehldatumDTO.setFahrradfahrer(49);
        ladeZaehldatumDTO.setFussgaenger(51);

        LadeZaehldatumDTO result = ProcessZaehldatenBelastungsplanService.roundToNearestIfRoundingIsChoosen(ladeZaehldatumDTO, nearestValueToRound, options);
        assertThat(result, is(ladeZaehldatumDTO));

        options.setWerteHundertRunden(true);
        result = ProcessZaehldatenBelastungsplanService.roundToNearestIfRoundingIsChoosen(ladeZaehldatumDTO, nearestValueToRound, options);
        LadeZaehldatumTageswertDTO expectedTageswert = new LadeZaehldatumTageswertDTO();
        expectedTageswert.setType("TEST");
        expectedTageswert.setStartUhrzeit(LocalTime.of(8, 0));
        expectedTageswert.setEndeUhrzeit(LocalTime.of(9, 0));
        expectedTageswert.setPkw(200);
        expectedTageswert.setLkw(300);
        expectedTageswert.setFahrradfahrer(0);
        expectedTageswert.setFussgaenger(100);
        expectedTageswert.setKfz(BigDecimal.valueOf(400));
        expectedTageswert.setSchwerverkehr(BigDecimal.valueOf(300));
        expectedTageswert.setGueterverkehr(BigDecimal.valueOf(300));
        assertThat(result, is(expectedTageswert));
    }

    @Test
    public void roundIfNotNullOrZero() {
        final Integer nearestValueToRound = 100;
        Integer valueToRoundInt = 49;
        Integer resultInt = ProcessZaehldatenBelastungsplanService.roundIfNotNullOrZero(valueToRoundInt, nearestValueToRound);
        assertThat(resultInt, is(0));

        valueToRoundInt = 50;
        resultInt = ProcessZaehldatenBelastungsplanService.roundIfNotNullOrZero(valueToRoundInt, nearestValueToRound);
        assertThat(resultInt, is(100));

        valueToRoundInt = 149;
        resultInt = ProcessZaehldatenBelastungsplanService.roundIfNotNullOrZero(valueToRoundInt, nearestValueToRound);
        assertThat(resultInt, is(100));

        valueToRoundInt = 150;
        resultInt = ProcessZaehldatenBelastungsplanService.roundIfNotNullOrZero(valueToRoundInt, nearestValueToRound);
        assertThat(resultInt, is(200));

        BigDecimal valueToRoundBd = BigDecimal.valueOf(49);
        BigDecimal resultBd = ProcessZaehldatenBelastungsplanService.roundIfNotNullOrZero(valueToRoundBd, nearestValueToRound);
        assertThat(resultBd, is(BigDecimal.ZERO));

        valueToRoundBd = BigDecimal.valueOf(50);
        resultBd = ProcessZaehldatenBelastungsplanService.roundIfNotNullOrZero(valueToRoundBd, nearestValueToRound);
        assertThat(resultBd, is(BigDecimal.valueOf(100)));

        valueToRoundBd = BigDecimal.valueOf(149);
        resultBd = ProcessZaehldatenBelastungsplanService.roundIfNotNullOrZero(valueToRoundBd, nearestValueToRound);
        assertThat(resultBd, is(BigDecimal.valueOf(100)));

        valueToRoundBd = BigDecimal.valueOf(150);
        resultBd = ProcessZaehldatenBelastungsplanService.roundIfNotNullOrZero(valueToRoundBd, nearestValueToRound);
        assertThat(resultBd, is(BigDecimal.valueOf(200)));
    }

    private BigDecimal[][] getBigDecimalTwoDimArrayAsc() {
        final BigDecimal[][] twoDimArray = new BigDecimal[4][4];
        twoDimArray[0] = new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) };
        twoDimArray[1] = new BigDecimal[] { BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.valueOf(7), BigDecimal.valueOf(8) };
        twoDimArray[2] = new BigDecimal[] { BigDecimal.valueOf(9), BigDecimal.valueOf(10), BigDecimal.valueOf(11), BigDecimal.valueOf(12) };
        twoDimArray[3] = new BigDecimal[] { BigDecimal.valueOf(13), BigDecimal.valueOf(14), BigDecimal.valueOf(15), BigDecimal.valueOf(16) };

        return twoDimArray;
    }

    private BigDecimal[][] getBigDecimalTwoDimArrayDesc() {
        final BigDecimal[][] twoDimArray = new BigDecimal[4][4];
        twoDimArray[0] = new BigDecimal[] { BigDecimal.valueOf(16), BigDecimal.valueOf(15), BigDecimal.valueOf(14), BigDecimal.valueOf(13) };
        twoDimArray[1] = new BigDecimal[] { BigDecimal.valueOf(12), BigDecimal.valueOf(11), BigDecimal.valueOf(10), BigDecimal.valueOf(9) };
        twoDimArray[2] = new BigDecimal[] { BigDecimal.valueOf(8), BigDecimal.valueOf(7), BigDecimal.valueOf(6), BigDecimal.valueOf(5) };
        twoDimArray[3] = new BigDecimal[] { BigDecimal.valueOf(4), BigDecimal.valueOf(3), BigDecimal.valueOf(2), BigDecimal.valueOf(1) };

        return twoDimArray;
    }

    private BigDecimal[][] getDifferenzwert() {
        return new BigDecimal[][] { { BigDecimal.valueOf(-15), BigDecimal.valueOf(-13), BigDecimal.valueOf(-11), BigDecimal.valueOf(-9) },
                { BigDecimal.valueOf(-7), BigDecimal.valueOf(-5), BigDecimal.valueOf(-3), BigDecimal.valueOf(-1) },
                { BigDecimal.valueOf(1), BigDecimal.valueOf(3), BigDecimal.valueOf(5), BigDecimal.valueOf(7) },
                { BigDecimal.valueOf(9), BigDecimal.valueOf(11), BigDecimal.valueOf(13), BigDecimal.valueOf(15) } };
    }

}
