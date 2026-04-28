package de.muenchen.dave.services.processzaehldaten;

import de.muenchen.dave.util.BelastungsplanCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BelastungsplanCalculatorTest {

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

    private BigDecimal[][] getBigDecimalTwoDimArrayAsc() {
        final BigDecimal[][] twoDimArray = new BigDecimal[4][4];
        twoDimArray[0] = new BigDecimal[] { BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4) };
        twoDimArray[1] = new BigDecimal[] { BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.valueOf(7), BigDecimal.valueOf(8) };
        twoDimArray[2] = new BigDecimal[] { BigDecimal.valueOf(9), BigDecimal.valueOf(10), BigDecimal.valueOf(11), BigDecimal.valueOf(12) };
        twoDimArray[3] = new BigDecimal[] { BigDecimal.valueOf(13), BigDecimal.valueOf(14), BigDecimal.valueOf(15), BigDecimal.valueOf(16) };

        return twoDimArray;
    }

    private BigDecimal[][] getDifferenzwert() {
        return new BigDecimal[][] { { BigDecimal.valueOf(-15), BigDecimal.valueOf(-13), BigDecimal.valueOf(-11), BigDecimal.valueOf(-9) },
                { BigDecimal.valueOf(-7), BigDecimal.valueOf(-5), BigDecimal.valueOf(-3), BigDecimal.valueOf(-1) },
                { BigDecimal.valueOf(1), BigDecimal.valueOf(3), BigDecimal.valueOf(5), BigDecimal.valueOf(7) },
                { BigDecimal.valueOf(9), BigDecimal.valueOf(11), BigDecimal.valueOf(13), BigDecimal.valueOf(15) } };
    }



}
