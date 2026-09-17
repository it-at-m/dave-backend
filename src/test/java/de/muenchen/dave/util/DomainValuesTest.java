package de.muenchen.dave.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.enums.Zaehlart;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class DomainValuesTest {

    @Test
    void getCorrectZaehlartString() {
        String result = DomainValues.getCorrectZaehlartString(Zaehlart.N.toString());
        assertThat(result, is(StringUtils.EMPTY));

        result = DomainValues.getCorrectZaehlartString(Zaehlart.Q.toString());
        assertThat(result, is("Q"));

        result = DomainValues.getCorrectZaehlartString(Zaehlart.Q_.toString());
        assertThat(result, is("Q_"));
    }
}
