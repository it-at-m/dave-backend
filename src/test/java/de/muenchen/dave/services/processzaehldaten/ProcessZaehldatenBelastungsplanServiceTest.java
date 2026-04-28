package de.muenchen.dave.services.processzaehldaten;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanQjsDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumTageswertDTO;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.ZaehlstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.elasticsearch.ZaehlungRandomFactory;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.BelastungsplanCalculator;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessZaehldatenBelastungsplanServiceTest {

    private static final Random random = new Random(System.currentTimeMillis());

    @Mock
    private ZeitintervallRepository zeitintervallRepository;
    @Mock
    private ZaehlstelleIndex zaehlstelleIndex;
    @Mock
    private LadeZaehldatenService ladeZaehldatenService;

    private ProcessZaehldatenBelastungsplanService service;

    @BeforeEach
    public void beforeEach() throws IllegalAccessException {
        service = new ProcessZaehldatenBelastungsplanService(zeitintervallRepository, zaehlstelleIndex, ladeZaehldatenService, new BelastungsplanDataServiceFactory(new BelastungsplanDataDefaultService(), new BelastungsplanDataQjsService(), new BelastungsplanDataFjsService()));
        Mockito.reset(zeitintervallRepository, zaehlstelleIndex, ladeZaehldatenService);
    }

    /**
     * Testet, ob die KFZ-Zähldaten hierarchisch über RAD-Daten eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithKfzAndRadData() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.KFZ, Fahrzeug.RAD));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.KFZ, Fahrzeug.RAD));
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.N,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), List.of(Fahrzeug.KFZ, Fahrzeug.RAD))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("KFZ", ((BelastungsplanDataDTO) dto.getValue1()).getLabel());
    }

    /**
     * Testet, ob die KFZ-Zähldaten hierarchisch über FUSS-Daten eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithKfzAndFussData() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.KFZ, Fahrzeug.FUSS));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.KFZ, Fahrzeug.FUSS));
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.N,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), List.of(Fahrzeug.KFZ, Fahrzeug.FUSS))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("KFZ", ((BelastungsplanDataDTO) dto.getValue1()).getLabel());
    }

    /**
     * Testet, ob die RAD-Zähldaten hierarchisch über FUSS-Daten eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithRadAndFussData() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.RAD, Fahrzeug.FUSS));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.N,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), List.of(Fahrzeug.RAD, Fahrzeug.FUSS))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("RAD", ((BelastungsplanDataDTO) dto.getValue1()).getLabel());
    }

    /**
     * Testet, ob die FUSS-Zähldaten allein hierarchisch richtig eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithFussOnlyData() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.FUSS));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.FUSS));
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.N,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), List.of(Fahrzeug.FUSS))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("FUSS", ((BelastungsplanDataDTO) dto.getValue1()).getLabel());
    }

    /**
     * Testet, ob die FUSS-Zähldaten allein bei QJS hierarchisch richtig eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithFussDataQjs() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.FUSS));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.QJS.name());
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.QJS,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), List.of(Fahrzeug.FUSS))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("FUSS", ((BelastungsplanQjsDataDTO) dto.getValue1()).getLabel());
    }

    /**
     * Testet, ob die RAD-Zähldaten bei QJS hierarchisch richtig eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithRadAndFussDataQjs() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.RAD));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.QJS.name());
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.QJS,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), List.of(Fahrzeug.RAD, Fahrzeug.FUSS))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("RAD", ((BelastungsplanQjsDataDTO) dto.getValue1()).getLabel());
        assertTrue((((BelastungsplanQjsDataDTO) dto.getValue2()).getLabel()).isEmpty());
    }

    /**
     * Testet die Abzweigung für den Differenzdatenvergleich.
     */
    @Test
    void testGetBelastungsplanDTOWhenDifferenzFalse() throws Exception {
        ProcessZaehldatenBelastungsplanService serviceSpy = Mockito.spy(service);
        final OptionsDTO options = new OptionsDTO();
        options.setDifferenzdatenDarstellen(false);

        final LadeBelastungsplanDTO expected = new LadeBelastungsplanDTO();

        doReturn(expected).when(serviceSpy).ladeProcessedZaehldatenBelastungsplan(anyString(), any(OptionsDTO.class));
        Zaehlstelle zaehlstelle = new Zaehlstelle();
        Zaehlung zaehlung1 = new Zaehlung();
        zaehlung1.setId("zaehlung-1");
        zaehlung1.setZaehlart(Zaehlart.N.name());
        zaehlstelle.getZaehlungen().add(zaehlung1);
        when(zaehlstelleIndex.findByZaehlungenId("zaehlung-1")).thenReturn(Optional.of((zaehlstelle)));

        final var result = serviceSpy.getBelastungsplanDTO("zaehlung-1", options);

        assertSame(expected, result);
        verify(serviceSpy, times(1)).ladeProcessedZaehldatenBelastungsplan(eq("zaehlung-1"), eq(options));
        verify(serviceSpy, never()).getDifferenzdatenBelastungsplanDTO(anyString(), any(OptionsDTO.class));
    }

    /**
     * Testet die Abzweigung für Einzelzähldaten.
     */
    @Test
    void testGetBelastungsplanDTOWhenDifferenzTrue() throws Exception {
        ProcessZaehldatenBelastungsplanService serviceSpy = Mockito.spy(service);
        final OptionsDTO options = new OptionsDTO();
        options.setDifferenzdatenDarstellen(true);
        options.setVergleichszaehlungsId("vergleich-1");

        final LadeBelastungsplanDTO expected = new LadeBelastungsplanDTO();

        doReturn(expected).when(serviceSpy).getDifferenzdatenBelastungsplanDTO(anyString(), any(OptionsDTO.class));
        Zaehlstelle zaehlstelle = new Zaehlstelle();
        Zaehlung zaehlung1 = new Zaehlung();
        zaehlung1.setId("zaehlung-1");
        zaehlung1.setZaehlart(Zaehlart.N.name());
        zaehlstelle.getZaehlungen().add(zaehlung1);
        Zaehlung zaehlung2 = new Zaehlung();
        zaehlung2.setId("vergleich-1");
        zaehlstelle.getZaehlungen().add(zaehlung2);
        when(zaehlstelleIndex.findByZaehlungenId("zaehlung-1")).thenReturn(Optional.of((zaehlstelle)));
        when(zaehlstelleIndex.findByZaehlungenId("vergleich-1")).thenReturn(Optional.of((zaehlstelle)));

        final var result = serviceSpy.getBelastungsplanDTO("zaehlung-1", options);

        assertSame(expected, result);
        verify(serviceSpy, times(1)).getDifferenzdatenBelastungsplanDTO(eq("zaehlung-1"), eq(options));
        verify(serviceSpy, never()).ladeProcessedZaehldatenBelastungsplan(anyString(), any(OptionsDTO.class));
    }

    @Test
    void testCalculateDifferenzdatenDTO() {
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

        assertThat((calculated.getValue1()).getValues(), is(getDifferenzwert()));
        assertThat((calculated.getValue2()).getValues(), is(getDifferenzwert()));
        assertThat((calculated.getValue3()).getValues(), is(getDifferenzwert()));
        assertThat(calculated.getStreets(),
                is(new String[] { "Arnulfstraße", "Joseph-Spital-Straße", "Sonnenstraße", null, "Abengauerweg", null, "Joseph-Spital-Straße", null }));

        // value1 === value2 === value3
        assertThat((calculated.getValue1()).getSumIn(),
                is(new BigDecimal[] { BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(30),
                        BigDecimal.valueOf(40), BigDecimal.valueOf(50), BigDecimal.valueOf(60), BigDecimal.valueOf(70), BigDecimal.valueOf(80) }));
        assertThat((calculated.getValue1()).getSumOut(),
                is(new BigDecimal[] { BigDecimal.valueOf(-2), BigDecimal.valueOf(-14), BigDecimal.valueOf(-26),
                        BigDecimal.valueOf(-38), BigDecimal.valueOf(-50), BigDecimal.valueOf(-62), BigDecimal.valueOf(-74), BigDecimal.valueOf(-86) }));
        assertThat((calculated.getValue1()).getSum(),
                is(new BigDecimal[] { BigDecimal.valueOf(-8), BigDecimal.valueOf(-6), BigDecimal.valueOf(-4),
                        BigDecimal.valueOf(-2), BigDecimal.valueOf(0), BigDecimal.valueOf(2), BigDecimal.valueOf(4), BigDecimal.valueOf(6) }));
    }

    @Test
    void testSubtractMatrice() {
        final BigDecimal[][] basis = getBigDecimalTwoDimArrayAsc();

        final BigDecimal[][] vergleich = new BigDecimal[4][4];
        vergleich[0] = new BigDecimal[] { BigDecimal.valueOf(16), BigDecimal.valueOf(15), BigDecimal.valueOf(14), BigDecimal.valueOf(13) };
        vergleich[1] = new BigDecimal[] { BigDecimal.valueOf(12), BigDecimal.valueOf(11), BigDecimal.valueOf(10), BigDecimal.valueOf(9) };
        vergleich[2] = new BigDecimal[] { BigDecimal.valueOf(8), BigDecimal.valueOf(7), BigDecimal.valueOf(6), BigDecimal.valueOf(5) };
        vergleich[3] = new BigDecimal[] { BigDecimal.valueOf(4), BigDecimal.valueOf(3), BigDecimal.valueOf(2), BigDecimal.valueOf(1) };

        final BigDecimal[][] differenz = BelastungsplanCalculator.subtractMatrice(basis, vergleich);
        assertThat(differenz,
                is(getDifferenzwert()));
    }

    @Test
    public void testRoundToNearestIfRoundingIsChoosen() {
        final int nearestValueToRound = 100;
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
    public void testRoundIfNotNullOrZero() {
        final int nearestValueToRound = 100;
        int valueToRoundInt = 49;
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

    /**
     * Testet die Extraktion der für die ausgewählte "Spitzenstunde" relevanten Zeitintervalle einer
     * Zählung.
     */
    @Test
    void testExtractZeitintervalleSpitzenstunde() {
        final UUID zaehlungId = UUID.randomUUID();
        final Zaehlung zaehlung = Mockito.mock(Zaehlung.class);
        when(zaehlung.getId()).thenReturn(zaehlungId.toString());
        when(zaehlung.getZaehlart()).thenReturn(Zaehlart.N.name());
        when(zaehlung.getKreisverkehr()).thenReturn(Boolean.FALSE);

        final OptionsDTO options = new OptionsDTO();
        options.setZeitauswahl(LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_KFZ);
        options.setZeitblock(Zeitblock.ZB_06_10);

        try (MockedStatic<ZeitintervallGleitendeSpitzenstundeUtil> utilMock = Mockito.mockStatic(ZeitintervallGleitendeSpitzenstundeUtil.class)) {
            final LinkedList<Zeitintervall> spitzenstunden = new LinkedList<>();
            final Zeitintervall overall = new Zeitintervall();
            overall.setStartUhrzeit(LocalDateTime.of(2022, 1, 1, 7, 0));
            overall.setEndeUhrzeit(LocalDateTime.of(2022, 1, 1, 8, 0));
            spitzenstunden.add(overall);

            when(ladeZaehldatenService.extractZeitintervalleSpitzenstundeFor15MinuteIntervals(eq(zaehlungId), any(), any(), any(OptionsDTO.class)))
                    .thenReturn(spitzenstunden);

            final List<Zeitintervall> zeitintervalle = List.of(new Zeitintervall());
            when(zeitintervallRepository
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndTypeInOrderBySortingIndexAsc(
                            eq(zaehlungId), eq(overall.getStartUhrzeit()), eq(overall.getEndeUhrzeit()), eq(Set.of(TypeZeitintervall.STUNDE_VIERTEL))))
                    .thenReturn(zeitintervalle);

            final List<Zeitintervall> gleitende = new ArrayList<>();
            final Zeitintervall g = new Zeitintervall();
            g.setPkw(99);
            g.setSortingIndex(1);
            gleitende.add(g);

            utilMock.when(
                    () -> ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenByBewegungsbeziehung(eq(zaehlungId), any(), any(), any(), any()))
                    .thenReturn(gleitende);

            final List<Zeitintervall> result = service.extractZeitintervalleSpitzenstunde(zaehlung, options);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(LocalDateTime.of(2022, 1, 1, 7, 0), result.getFirst().getStartUhrzeit());
            assertEquals(99, result.getFirst().getPkw());

            utilMock.verify(
                    () -> ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstundenByBewegungsbeziehung(eq(zaehlungId), any(), any(), any(), any()),
                    times(1));
            verify(ladeZaehldatenService, times(1)).extractZeitintervalleSpitzenstundeFor15MinuteIntervals(eq(zaehlungId), any(), eq(Boolean.FALSE),
                    any(OptionsDTO.class));
            verify(zeitintervallRepository, times(1))
                    .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndTypeInOrderBySortingIndexAsc(
                            eq(zaehlungId), eq(overall.getStartUhrzeit()), eq(overall.getEndeUhrzeit()), eq(Set.of(TypeZeitintervall.STUNDE_VIERTEL)));
        }
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

    private Zeitintervall createTestZeitintervall(final String zaehlungId, final List<Fahrzeug> fahrzeuge) {
        Zeitintervall zeitintervall = new Zeitintervall();
        if (fahrzeuge.contains(Fahrzeug.PKW))
            zeitintervall.setPkw(random.nextInt());
        if (fahrzeuge.contains(Fahrzeug.RAD))
            zeitintervall.setFahrradfahrer(random.nextInt());
        if (fahrzeuge.contains(Fahrzeug.FUSS))
            zeitintervall.setFussgaenger(random.nextInt());

        zeitintervall.setStartUhrzeit(LocalDateTime.now());
        zeitintervall.setEndeUhrzeit(LocalDateTime.now().plusMinutes(15));
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setZaehlungId(UUID.fromString(zaehlungId));
        Verkehrsbeziehung vb = new Verkehrsbeziehung();
        vb.setVon(1);
        vb.setNach(3);
        zeitintervall.setVerkehrsbeziehung(vb);
        return zeitintervall;
    }

    private Zaehlung createTestZaehlung(final List<Fahrzeug> fahrzeuge) {
        Zaehlung zaehlung = ZaehlungRandomFactory.getOne();
        zaehlung.setKreisverkehr(false);
        zaehlung.setKategorien(fahrzeuge);
        zaehlung.setZaehlart(Zaehlart.N.name());
        zaehlung.setPkwEinheit(new PkwEinheit());
        return zaehlung;
    }

    private OptionsDTO createTestOptions(final List<Fahrzeug> fahrzeuge) {
        OptionsDTO options = new OptionsDTO();
        options.setZeitblock(Zeitblock.ZB_06_19);
        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        options.setGueterverkehrsanteilProzent(false);
        options.setFussverkehr(false);
        options.setRadverkehr(false);
        options.setKraftfahrzeugverkehr(false);
        options.setSchwerverkehr(false);
        options.setGueterverkehr(false);
        options.setLastzuege(false);
        options.setKraftraeder(false);
        options.setPersonenkraftwagen(false);
        options.setBusse(false);
        options.setSchwerverkehrsanteilProzent(false);
        if (fahrzeuge.contains(Fahrzeug.KFZ))
            options.setKraftfahrzeugverkehr(true);
        if (fahrzeuge.contains(Fahrzeug.RAD))
            options.setRadverkehr(true);
        if (fahrzeuge.contains(Fahrzeug.FUSS))
            options.setFussverkehr(true);
        return options;
    }

}
