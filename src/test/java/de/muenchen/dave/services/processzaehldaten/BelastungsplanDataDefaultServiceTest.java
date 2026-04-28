package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.dtos.laden.AbstractBelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehlart;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BelastungsplanDataDefaultServiceTest {

    @Test
    public void testGetBelastungsplanData() {
        final Map<Verkehrsbeziehung, ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum> zaehldatenJeVerkehrsbeziehung = new HashMap<>();
        Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setVon(2);
        verkehrsbeziehung.setNach(3);

        LadeZaehldatumDTO ladeZaehldatum = new LadeZaehldatumDTO();
        ladeZaehldatum.setPkw(1);
        ladeZaehldatum.setLkw(2);
        ladeZaehldatum.setLastzuege(3);
        ladeZaehldatum.setBusse(4);
        ladeZaehldatum.setKraftraeder(5);
        ladeZaehldatum.setFahrradfahrer(6);
        ladeZaehldatum.setFussgaenger(7);
        ladeZaehldatum.setPkwEinheiten(100);

        zaehldatenJeVerkehrsbeziehung.put(verkehrsbeziehung, new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(false, ladeZaehldatum));

        verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setVon(5);
        verkehrsbeziehung.setNach(2);

        ladeZaehldatum = new LadeZaehldatumDTO();
        ladeZaehldatum.setPkw(10);
        ladeZaehldatum.setLkw(20);
        ladeZaehldatum.setLastzuege(30);
        ladeZaehldatum.setBusse(40);
        ladeZaehldatum.setKraftraeder(50);
        ladeZaehldatum.setFahrradfahrer(60);
        ladeZaehldatum.setFussgaenger(70);
        ladeZaehldatum.setPkwEinheiten(1000);

        zaehldatenJeVerkehrsbeziehung.put(verkehrsbeziehung, new ProcessZaehldatenBelastungsplanService.TupelTageswertZaehldatum(false, ladeZaehldatum));

        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setKategorien(Arrays.asList(Fahrzeug.KFZ, Fahrzeug.SV, Fahrzeug.GV, Fahrzeug.SV_P, Fahrzeug.GV_P, Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.N.toString());
        zaehlung.setKreisverkehr(false);

        final Map<Fahrzeug, AbstractBelastungsplanDataDTO> belastungsplanData = new BelastungsplanDataDefaultService()
                .getBelastungsplanData(zaehldatenJeVerkehrsbeziehung, zaehlung);

        assertThat(((BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.KFZ)).getValues()[1][2], is(BigDecimal.valueOf(15)));
        assertThat(((BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.SV)).getValues()[1][2], is(BigDecimal.valueOf(9)));
        assertThat(((BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.GV)).getValues()[1][2], is(BigDecimal.valueOf(5)));
        assertThat(((BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.RAD)).getValues()[1][2], is(BigDecimal.valueOf(6)));
        assertThat(((BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.FUSS)).getValues()[1][2], is(BigDecimal.valueOf(7)));

        assertThat(((BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.KFZ)).getValues()[4][1], is(BigDecimal.valueOf(150)));
        assertThat(((BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.SV)).getValues()[4][1], is(BigDecimal.valueOf(90)));
        assertThat(((BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.GV)).getValues()[4][1], is(BigDecimal.valueOf(50)));
        assertThat(((BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.RAD)).getValues()[4][1], is(BigDecimal.valueOf(60)));
        assertThat(((BelastungsplanDataDTO) belastungsplanData.get(Fahrzeug.FUSS)).getValues()[4][1], is(BigDecimal.valueOf(70)));

    }

    @Test
    public void testIsKreisverkehr() {
        final Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();

        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(null);
        verkehrsbeziehung.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HINEIN);
        boolean result = TestUtils.privateStaticMethodCall(
                "isKreisverkehr",
                BelastungsplanDataDefaultService.class,
                ArrayUtils.toArray(Verkehrsbeziehung.class),
                ArrayUtils.toArray(verkehrsbeziehung),
                Boolean.class);
        assertThat(result, is(true));

        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(null);
        verkehrsbeziehung.setFahrbewegungKreisverkehr(null);
        result = TestUtils.privateStaticMethodCall(
                "isKreisverkehr",
                BelastungsplanDataDefaultService.class,
                ArrayUtils.toArray(Verkehrsbeziehung.class),
                ArrayUtils.toArray(verkehrsbeziehung),
                Boolean.class);
        assertThat(result, is(false));

        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        verkehrsbeziehung.setFahrbewegungKreisverkehr(null);
        result = TestUtils.privateStaticMethodCall(
                "isKreisverkehr",
                BelastungsplanDataDefaultService.class,
                ArrayUtils.toArray(Verkehrsbeziehung.class),
                ArrayUtils.toArray(verkehrsbeziehung),
                Boolean.class);
        assertThat(result, is(false));

        verkehrsbeziehung.setVon(null);
        verkehrsbeziehung.setNach(null);
        verkehrsbeziehung.setFahrbewegungKreisverkehr(null);
        result = TestUtils.privateStaticMethodCall(
                "isKreisverkehr",
                BelastungsplanDataDefaultService.class,
                ArrayUtils.toArray(Verkehrsbeziehung.class),
                ArrayUtils.toArray(verkehrsbeziehung),
                Boolean.class);
        assertThat(result, is(false));
    }

}
