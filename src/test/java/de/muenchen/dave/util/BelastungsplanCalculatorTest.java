package de.muenchen.dave.util;

import static org.junit.jupiter.api.Assertions.*;

import de.muenchen.dave.domain.dtos.laden.BelastungsplanDataDTO;
import de.muenchen.dave.domain.enums.Fahrzeug;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class BelastungsplanCalculatorTest {

    @Test
    public void testSubtractMatrice() {
        BigDecimal[][] basis = {
                { BigDecimal.valueOf(5), BigDecimal.valueOf(3) },
                { BigDecimal.valueOf(2), BigDecimal.valueOf(1) }
        };
        BigDecimal[][] vergleich = {
                { BigDecimal.valueOf(1), BigDecimal.valueOf(1) },
                { BigDecimal.valueOf(1), BigDecimal.valueOf(1) }
        };

        BigDecimal[][] diff = BelastungsplanCalculator.subtractMatrice(basis, vergleich);

        assertEquals(2, diff.length);
        assertEquals(2, diff[0].length);
        assertEquals(BigDecimal.valueOf(4), diff[0][0]);
        assertEquals(BigDecimal.valueOf(2), diff[0][1]);
        assertEquals(BigDecimal.valueOf(1), diff[1][0]);
        assertEquals(BigDecimal.valueOf(0), diff[1][1]);
    }

    @Test
    public void testCalcSumsKreuzung_simple2x2() {
        // matrix:
        // [ a, b ]
        // [ c, d ]
        BigDecimal a = BigDecimal.valueOf(1);
        BigDecimal b = BigDecimal.valueOf(2);
        BigDecimal c = BigDecimal.valueOf(3);
        BigDecimal d = BigDecimal.valueOf(4);

        BigDecimal[][] values = {
                { a, b },
                { c, d }
        };

        Map<String, BigDecimal[]> sums = BelastungsplanCalculator.calcSumsKreuzung(values);

        BigDecimal[] sumIn = sums.get(BelastungsplanCalculator.SUM_IN);
        BigDecimal[] sumOut = sums.get(BelastungsplanCalculator.SUM_OUT);
        BigDecimal[] sum = sums.get(BelastungsplanCalculator.SUM);

        // For node 0: sumIn = a + b, sumOut = a + c, sum = a + a + b + c
        assertEquals(BigDecimal.valueOf(3), sumIn[0]);
        assertEquals(BigDecimal.valueOf(4), sumOut[0]);
        assertEquals(BigDecimal.valueOf(7), sum[0]);

        // For node 1: sumIn = c + d, sumOut = b + d, sum = c + b + d + d
        assertEquals(BigDecimal.valueOf(7), sumIn[1]);
        assertEquals(BigDecimal.valueOf(6), sumOut[1]);
        assertEquals(BigDecimal.valueOf(13), sum[1]);
    }

    @Test
    public void testCalcSumsKreisverkehr_simple2x3() {
        // each row: [inToCircle, passByArm, outFromCircle]
        BigDecimal[][] values = {
                { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3) },
                { BigDecimal.valueOf(4), BigDecimal.valueOf(5), BigDecimal.valueOf(6) }
        };

        Map<String, BigDecimal[]> sums = BelastungsplanCalculator.calcSumsKreisverkehr(values);

        BigDecimal[] sumIn = sums.get(BelastungsplanCalculator.SUM_IN);
        BigDecimal[] sum = sums.get(BelastungsplanCalculator.SUM);

        // For row0: sumIn = 1 + 2 = 3, sumBoth = 1 + 3 = 4
        assertEquals(BigDecimal.valueOf(3), sumIn[0]);
        assertEquals(BigDecimal.valueOf(4), sum[0]);

        // For row1: sumIn = 4 + 5 = 9, sumBoth = 4 + 6 = 10
        assertEquals(BigDecimal.valueOf(9), sumIn[1]);
        assertEquals(BigDecimal.valueOf(10), sum[1]);
    }

    @Test
    public void testCalculateAnteilProzent() {
        BigDecimal[] kfz = { BigDecimal.valueOf(10), BigDecimal.valueOf(20) };
        BigDecimal[] sv = { BigDecimal.valueOf(1), BigDecimal.valueOf(2) };

        try (MockedStatic<CalculationUtil> mocked = Mockito.mockStatic(CalculationUtil.class)) {
            // stub the static method for the two expected calls
            mocked.when(() -> CalculationUtil.calculateAnteilProzent(sv[0], kfz[0]))
                    .thenReturn(new BigDecimal("0.1"));
            mocked.when(() -> CalculationUtil.calculateAnteilProzent(sv[1], kfz[1]))
                    .thenReturn(new BigDecimal("0.1"));

            BigDecimal[] result = BelastungsplanCalculator.calculateAnteilProzent(kfz, sv);

            assertEquals(2, result.length);
            assertEquals(new BigDecimal("0.1"), result[0]);
            assertEquals(new BigDecimal("0.1"), result[1]);
        }
    }

    @Test
    public void testCalculateSumsForBelastungsplanDataDto_svPercentPath() {
        BelastungsplanDataDTO dto = new BelastungsplanDataDTO();
        dto.setLabel(Fahrzeug.SV_P.getName());

        // Prepare sumsKfz and sumsSv (arrays of length 2)
        Map<String, BigDecimal[]> sumsKfz = new HashMap<>();
        Map<String, BigDecimal[]> sumsSv = new HashMap<>();
        BigDecimal[] kfzIn = { BigDecimal.valueOf(10), BigDecimal.valueOf(20) };
        BigDecimal[] kfzOut = { BigDecimal.valueOf(30), BigDecimal.valueOf(40) };
        BigDecimal[] kfzSum = { BigDecimal.valueOf(40), BigDecimal.valueOf(60) };
        sumsKfz.put(BelastungsplanCalculator.SUM_IN, kfzIn);
        sumsKfz.put(BelastungsplanCalculator.SUM_OUT, kfzOut);
        sumsKfz.put(BelastungsplanCalculator.SUM, kfzSum);

        BigDecimal[] svIn = { BigDecimal.valueOf(1), BigDecimal.valueOf(2) };
        BigDecimal[] svOut = { BigDecimal.valueOf(3), BigDecimal.valueOf(4) };
        BigDecimal[] svSum = { BigDecimal.valueOf(4), BigDecimal.valueOf(6) };
        sumsSv.put(BelastungsplanCalculator.SUM_IN, svIn);
        sumsSv.put(BelastungsplanCalculator.SUM_OUT, svOut);
        sumsSv.put(BelastungsplanCalculator.SUM, svSum);

        try (MockedStatic<CalculationUtil> mocked = Mockito.mockStatic(CalculationUtil.class)) {
            // stub expected calls for each index and type
            mocked.when(() -> CalculationUtil.calculateAnteilProzent(svIn[0], kfzIn[0])).thenReturn(new BigDecimal("0.1"));
            mocked.when(() -> CalculationUtil.calculateAnteilProzent(svIn[1], kfzIn[1])).thenReturn(new BigDecimal("0.1"));

            mocked.when(() -> CalculationUtil.calculateAnteilProzent(svOut[0], kfzOut[0])).thenReturn(new BigDecimal("0.1"));
            mocked.when(() -> CalculationUtil.calculateAnteilProzent(svOut[1], kfzOut[1])).thenReturn(new BigDecimal("0.1"));

            mocked.when(() -> CalculationUtil.calculateAnteilProzent(svSum[0], kfzSum[0])).thenReturn(new BigDecimal("0.1"));
            mocked.when(() -> CalculationUtil.calculateAnteilProzent(svSum[1], kfzSum[1])).thenReturn(new BigDecimal("0.1"));

            BelastungsplanCalculator.calculateSumsForBelastungsplanDataDto(dto, sumsKfz, sumsSv, null, false);

            // DTO should now contain arrays populated with the mocked percentage values
            assertNotNull(dto.getSumIn());
            assertNotNull(dto.getSumOut());
            assertNotNull(dto.getSum());

            assertEquals(new BigDecimal("0.1"), dto.getSumIn()[0]);
            assertEquals(new BigDecimal("0.1"), dto.getSumIn()[1]);
            assertEquals(new BigDecimal("0.1"), dto.getSumOut()[0]);
            assertEquals(new BigDecimal("0.1"), dto.getSumOut()[1]);
            assertEquals(new BigDecimal("0.1"), dto.getSum()[0]);
            assertEquals(new BigDecimal("0.1"), dto.getSum()[1]);
        }
    }

    @Test
    public void testCalculateSumsForBelastungsplanDataDto_kreuzungPath() {
        BelastungsplanDataDTO dto = new BelastungsplanDataDTO();
        dto.setLabel("KFZ"); // not SV_P or GV_P, so will use calcSumsKreuzung
        BigDecimal[][] values = {
                { BigDecimal.valueOf(1), BigDecimal.valueOf(2) },
                { BigDecimal.valueOf(3), BigDecimal.valueOf(4) }
        };
        dto.setValues(values);

        BelastungsplanCalculator.calculateSumsForBelastungsplanDataDto(dto, null, null, null, false);

        // calcSumsKreuzung assertions (see earlier test)
        assertNotNull(dto.getSumIn());
        assertNotNull(dto.getSumOut());
        assertNotNull(dto.getSum());

        // For node 0: sumIn = 1 + 2 = 3, sumOut = 1 + 3 = 4, sum = 1+1+2+3 = 7
        assertEquals(BigDecimal.valueOf(3), dto.getSumIn()[0]);
        assertEquals(BigDecimal.valueOf(4), dto.getSumOut()[0]);
        assertEquals(BigDecimal.valueOf(7), dto.getSum()[0]);

        // For node 1:
        assertEquals(BigDecimal.valueOf(7), dto.getSumIn()[1]); // 3 + 4
        assertEquals(BigDecimal.valueOf(6), dto.getSumOut()[1]); // 2 + 4
        assertEquals(BigDecimal.valueOf(13), dto.getSum()[1]); // 3 + 2 + 4 + 4
    }
}
