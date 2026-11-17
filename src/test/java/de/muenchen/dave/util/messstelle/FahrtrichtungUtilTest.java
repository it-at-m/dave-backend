package de.muenchen.dave.util.messstelle;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FahrtrichtungUtilTest {

    @Test
    void getLongTextOfFahrtrichtung() {
        Assertions.assertThat(FahrtrichtungUtil.getLongTextOfFahrtrichtung("N")).isNotNull().isEqualTo("Nord");
        Assertions.assertThat(FahrtrichtungUtil.getLongTextOfFahrtrichtung("O")).isNotNull().isEqualTo("Ost");
        Assertions.assertThat(FahrtrichtungUtil.getLongTextOfFahrtrichtung("S")).isNotNull().isEqualTo("Süd");
        Assertions.assertThat(FahrtrichtungUtil.getLongTextOfFahrtrichtung("W")).isNotNull().isEqualTo("West");
        Assertions.assertThat(FahrtrichtungUtil.getLongTextOfFahrtrichtung("Blub")).isNotNull().isEqualTo("Blub");
        Assertions.assertThat(FahrtrichtungUtil.getLongTextOfFahrtrichtung(null)).isEqualTo("unbekannt");
    }
}
