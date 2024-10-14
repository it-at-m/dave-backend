package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.KIZeitintervall;
import de.muenchen.dave.domain.Zeitintervall;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

@Slf4j
public class KIZeitintervallMapperTests {

    private final KIZeitintervallMapper mapper = new KIZeitintervallMapperImpl();

    @Test
    public void testZeitIntervallToKIZeitIntervall() {
        // Arrange

        // 10. Juni 2020 (Mittwoch) 05:15, Jahreszeit 3, Jahre seit 89: 31
        LocalDateTime timeStamp = LocalDateTime.of(2020, 6, 10, 5, 15);

        Zeitintervall zeitintervall = Zeitintervall.builder()
                .pkw(1)
                .lkw(2)
                .lastzuege(30)
                .busse(4)
                .kraftraeder(5)
                .fahrradfahrer(6)
                .startUhrzeit(timeStamp)
                .build();

        // Act
        KIZeitintervall kiZeitIntervall = mapper.zeitintervallToKIZeitintervall(zeitintervall);

        // Assert
        assertThat(kiZeitIntervall, hasProperty("rad", equalTo(zeitintervall.getFahrradfahrer())));
        assertThat(kiZeitIntervall, hasProperty("jahresZeit", equalTo(3)));
        assertThat(kiZeitIntervall, hasProperty("jahreSeit89", equalTo(31)));
        assertThat(kiZeitIntervall, hasProperty("montag", equalTo(0)));
        assertThat(kiZeitIntervall, hasProperty("dienstag", equalTo(0)));
        assertThat(kiZeitIntervall, hasProperty("mittwoch", equalTo(1)));
        assertThat(kiZeitIntervall, hasProperty("donnerstag", equalTo(0)));
        assertThat(kiZeitIntervall, hasProperty("freitag", equalTo(0)));
        assertThat(kiZeitIntervall, hasProperty("samstag", equalTo(0)));
        assertThat(kiZeitIntervall, hasProperty("sonntag", equalTo(0)));
    }

    @Test
    public void testZeitIntervallToKIZeitIntervallBefore89() {
        // Arrange

        // 10. Juni 1979, Jahre seit 89: negativ, daher 0
        LocalDateTime timeStamp = LocalDateTime.of(1979, 6, 10, 5, 15);

        Zeitintervall zeitintervall = Zeitintervall.builder()
                .startUhrzeit(timeStamp)
                .build();

        // Act
        KIZeitintervall kiZeitIntervall = mapper.zeitintervallToKIZeitintervall(zeitintervall);

        // Assert
        assertThat(kiZeitIntervall, hasProperty("jahreSeit89", equalTo(0)));
    }

}
