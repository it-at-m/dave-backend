package de.muenchen.dave.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.enums.Zeitauswahl;
import org.junit.jupiter.api.Test;

class OptionsUtilTest {

    @Test
    public void isZeitauswahlSpitzenstunde() {
        assertThat(OptionsUtil.isZeitauswahlSpitzenstunde(Zeitauswahl.SPITZENSTUNDE_RAD.getCapitalizedName()), is(true));
        assertThat(OptionsUtil.isZeitauswahlSpitzenstunde(Zeitauswahl.SPITZENSTUNDE_KFZ.getCapitalizedName()), is(true));
        assertThat(OptionsUtil.isZeitauswahlSpitzenstunde(Zeitauswahl.SPITZENSTUNDE_FUSS.getCapitalizedName()), is(true));
        assertThat(OptionsUtil.isZeitauswahlSpitzenstunde(Zeitauswahl.TAGESWERT.getCapitalizedName()), is(false));
        assertThat(OptionsUtil.isZeitauswahlSpitzenstunde("some-other-string"), is(false));
    }
}
