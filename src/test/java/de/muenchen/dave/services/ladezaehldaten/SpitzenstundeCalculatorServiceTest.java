package de.muenchen.dave.services.ladezaehldaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.util.dataimport.ZeitintervallGleitendeSpitzenstundeUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpitzenstundeCalculatorServiceTest {

    @Mock
    private ZeitintervallMapper zeitintervallMapper;

    @InjectMocks
    private SpitzenstundeCalculatorService service;

    private UUID zaehlungId;
    private Zeitblock zeitblock;

    @BeforeEach
    void setUp() {
        zaehlungId = UUID.randomUUID();
        zeitblock = Zeitblock.ZB_06_10;
    }

    private Zeitintervall createZeitintervall(TypeZeitintervall type, int pkw) {
        return Zeitintervall.builder()
                .type(type)
                .pkw(pkw)
                .startUhrzeit(LocalDateTime.of(2020, 1, 1, 6, 0))
                .endeUhrzeit(LocalDateTime.of(2020, 1, 1, 6, 15))
                .build();
    }

    @Test
    void testGetZeitintervalleRelevantForCalculation_chooseViertel() {
        List<Zeitintervall> list = List.of(
                createZeitintervall(TypeZeitintervall.STUNDE_VIERTEL, 1),
                createZeitintervall(TypeZeitintervall.STUNDE_HALB, 2),
                createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 3));

        var relevant = service.getZeitintervalleRelevantForCalculationOfSpitzenstunde(list,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL, TypeZeitintervall.STUNDE_HALB));

        assertEquals(1, relevant.size());
        assertEquals(TypeZeitintervall.STUNDE_VIERTEL, relevant.get(0).getType());
    }

    @Test
    void testGetZeitintervalleRelevantForCalculation_chooseHalb_when_noViertelInTypes() {
        List<Zeitintervall> list = List.of(
                createZeitintervall(TypeZeitintervall.STUNDE_VIERTEL, 1),
                createZeitintervall(TypeZeitintervall.STUNDE_HALB, 2));

        var relevant = service.getZeitintervalleRelevantForCalculationOfSpitzenstunde(list, Set.of(TypeZeitintervall.STUNDE_HALB));

        // types enthält nur STUNDE_HALB -> relevant sollte STUNDE_HALB sein
        assertEquals(1, relevant.size());
        assertEquals(TypeZeitintervall.STUNDE_HALB, relevant.get(0).getType());
    }

    @Test
    void testGetZeitintervalleRelevantForCalculation_chooseKomplett_when_onlyKomplett() {
        List<Zeitintervall> list = List.of(
                createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 5),
                createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 6));

        var relevant = service.getZeitintervalleRelevantForCalculationOfSpitzenstunde(list, Set.of(TypeZeitintervall.STUNDE_KOMPLETT));

        assertEquals(2, relevant.size());
        assertTrue(relevant.stream().allMatch(z -> TypeZeitintervall.STUNDE_KOMPLETT.equals(z.getType())));
    }

    @Test
    void testGetZeitintervalleRelevantForCalculation_returnsEmpty_when_noMatchingTypeInList() {
        List<Zeitintervall> list = List.of(
                createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 5));

        var relevant = service.getZeitintervalleRelevantForCalculationOfSpitzenstunde(list, Set.of(TypeZeitintervall.STUNDE_VIERTEL));

        // Liste enthält kein STUNDE_VIERTEL -> Filter ergibt eine leere Liste
        assertTrue(relevant.isEmpty());
    }

    @Test
    void testCalculateSpitzenstunde_filtersByRequestedTypes_and_usesMapperDeepCopy_and_callsUtil() {
        List<Zeitintervall> input = new ArrayList<>();
        input.add(createZeitintervall(TypeZeitintervall.STUNDE_VIERTEL, 1));
        input.add(createZeitintervall(TypeZeitintervall.STUNDE_VIERTEL, 2));

        // deepCopy gibt eine Kopie zurück, die vom Service verwendet wird
        List<Zeitintervall> copyList = new ArrayList<>(input);
        Mockito.when(zeitintervallMapper.deepCopy(input)).thenReturn(copyList);

        // Bereite Rückgabewert der Util vor, der verschiedene Spitzenstundentypen enthält
        var spitzenKfz = createZeitintervall(TypeZeitintervall.SPITZENSTUNDE_KFZ, 100);
        var spitzenRad = createZeitintervall(TypeZeitintervall.SPITZENSTUNDE_RAD, 10);
        var spitzenFuss = createZeitintervall(TypeZeitintervall.SPITZENSTUNDE_FUSS, 5);
        List<Zeitintervall> utilReturn = List.of(spitzenKfz, spitzenRad, spitzenFuss);

        // Wir fragen STUNDE_VIERTEL als Quellintervalle an und erwarten SPITZENSTUNDE_KFZ sowie SPITZENSTUNDE_FUSS als Ergebnis-Typen
        Set<TypeZeitintervall> requestedTypes = Set.of(TypeZeitintervall.STUNDE_VIERTEL, TypeZeitintervall.SPITZENSTUNDE_KFZ,
                TypeZeitintervall.SPITZENSTUNDE_FUSS);

        try (MockedStatic<ZeitintervallGleitendeSpitzenstundeUtil> utilities = Mockito.mockStatic(ZeitintervallGleitendeSpitzenstundeUtil.class)) {
            // Stube die statische Util-Methode für beliebige passende Argumente
            utilities.when(() -> ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(utilReturn);

            // Fordere nur KFZ- und FUSS-Spitzenstundentypen an (zusätzlich STUNDE_VIERTEL zur Bestimmung relevanter Intervalle)
            var result = service.calculateSpitzenstundeForGivenZeitintervalle(zaehlungId, zeitblock, input, requestedTypes);

            // deepCopy sollte verwendet worden sein
            verify(zeitintervallMapper).deepCopy(input);

            // Util hat 3 Einträge zurückgegeben, der Service filtert jedoch nur die in requestedTypes enthaltenen Typen
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(z -> TypeZeitintervall.SPITZENSTUNDE_KFZ.equals(z.getType())));
            assertTrue(result.stream().anyMatch(z -> TypeZeitintervall.SPITZENSTUNDE_FUSS.equals(z.getType())));
        }
    }

    @Test
    void testCalculateSpitzenstunde_returnsEmpty_when_utilReturnsEmpty_or_zaehlungIdNull() {
        List<Zeitintervall> input = List.of(createZeitintervall(TypeZeitintervall.STUNDE_KOMPLETT, 1));
        Mockito.when(zeitintervallMapper.deepCopy(input)).thenReturn(new ArrayList<>(input));

        Set<TypeZeitintervall> requestedTypes = Set.of(TypeZeitintervall.SPITZENSTUNDE_KFZ);

        try (MockedStatic<ZeitintervallGleitendeSpitzenstundeUtil> utilities = Mockito.mockStatic(ZeitintervallGleitendeSpitzenstundeUtil.class)) {
            utilities.when(
                    () -> ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(Mockito.isNull(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(List.of());

            var resultNullZaehlung = service.calculateSpitzenstundeForGivenZeitintervalle(null, zeitblock, input, requestedTypes);
            assertTrue(resultNullZaehlung.isEmpty());

            // auch wenn die Util für eine nicht-null ID eine leere Liste zurückgibt
            utilities
                    .when(() -> ZeitintervallGleitendeSpitzenstundeUtil.getGleitendeSpitzenstunden(Mockito.eq(zaehlungId), Mockito.any(), Mockito.any(),
                            Mockito.any()))
                    .thenReturn(List.of());

            var resultEmpty = service.calculateSpitzenstundeForGivenZeitintervalle(zaehlungId, zeitblock, input, requestedTypes);
            assertTrue(resultEmpty.isEmpty());
        }
    }

}
