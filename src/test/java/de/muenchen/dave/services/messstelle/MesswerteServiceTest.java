package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.geodateneai.gen.model.IntervalDto;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MesswerteServiceTest {

    @Test
    void isTimeToCompareEqualOrAfterStarttimeAndBeforeEndTime() {

        var timeToCompare = LocalTime.of(10, 0, 0);
        var result = MesswerteService.isTimeToCompareEqualOrAfterStarttimeAndBeforeEndTime(
                timeToCompare,
                LocalTime.of(10, 0, 0),
                LocalTime.of(10, 15, 0));
        Assertions.assertThat(result).isTrue();

        timeToCompare = LocalTime.of(10, 10, 0);
        result = MesswerteService.isTimeToCompareEqualOrAfterStarttimeAndBeforeEndTime(
                timeToCompare,
                LocalTime.of(10, 0, 0),
                LocalTime.of(10, 15, 0));
        Assertions.assertThat(result).isTrue();

        timeToCompare = LocalTime.of(10, 14, 59, 999999999);
        result = MesswerteService.isTimeToCompareEqualOrAfterStarttimeAndBeforeEndTime(
                timeToCompare,
                LocalTime.of(10, 0, 0),
                LocalTime.of(10, 15, 0));
        Assertions.assertThat(result).isTrue();

        timeToCompare = LocalTime.of(10, 15, 0);
        result = MesswerteService.isTimeToCompareEqualOrAfterStarttimeAndBeforeEndTime(
                timeToCompare,
                LocalTime.of(10, 0, 0),
                LocalTime.of(10, 15, 0));
        Assertions.assertThat(result).isFalse();

        timeToCompare = LocalTime.of(9, 59, 59, 999999999);
        result = MesswerteService.isTimeToCompareEqualOrAfterStarttimeAndBeforeEndTime(
                timeToCompare,
                LocalTime.of(10, 0, 0),
                LocalTime.of(10, 15, 0));
        Assertions.assertThat(result).isFalse();

    }

    @Test
    void getLatestUhrzeitBisOfIntervals() {
        final var intervals = new ArrayList<IntervalDto>();
        var interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 10, 10, 11, 15, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 10, 10, 11, 30, 0));
        intervals.add(interval);
        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 10, 10, 11, 30, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 10, 10, 11, 45, 0));
        intervals.add(interval);
        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 10, 10, 11, 45, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 10, 10, 12, 0, 0));
        intervals.add(interval);

        var result = MesswerteService.getLatestUhrzeitBisOfIntervals(intervals);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(LocalTime.of(12, 0, 0));

        result = MesswerteService.getLatestUhrzeitBisOfIntervals(new ArrayList<>());

        Assertions.assertThat(result).isNull();
    }

    @Test
    void getEarliestUhrzeitVonOfIntervals() {
        final var intervals = new ArrayList<IntervalDto>();
        var interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 10, 10, 11, 15, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 10, 10, 11, 30, 0));
        intervals.add(interval);
        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 10, 10, 11, 30, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 10, 10, 11, 45, 0));
        intervals.add(interval);
        interval = new IntervalDto();
        interval.setDatumUhrzeitVon(LocalDateTime.of(2024, 10, 10, 11, 45, 0));
        interval.setDatumUhrzeitBis(LocalDateTime.of(2024, 10, 10, 12, 0, 0));
        intervals.add(interval);

        var result = MesswerteService.getEarliestUhrzeitVonOfIntervals(intervals);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(LocalTime.of(11, 15, 0));

        result = MesswerteService.getEarliestUhrzeitVonOfIntervals(new ArrayList<>());

        Assertions.assertThat(result).isNull();
    }

}
