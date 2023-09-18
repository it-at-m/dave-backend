package de.muenchen.dave.util.dataimport;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.util.DaveConstants;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ZeitintervallSortingIndexUtilTest {

    @Test
    public void getSortingIndexWithinBlock() {
        Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setType(TypeZeitintervall.GESAMT);
        int result = ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(zeitintervall);
        assertThat(result, is(0));

        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        result = ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(zeitintervall);
        assertThat(result, is(0));

        zeitintervall.setType(TypeZeitintervall.STUNDE_KOMPLETT);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 0)));
        result = ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(zeitintervall);
        assertThat(result, is(11008008));

        zeitintervall.setType(TypeZeitintervall.STUNDE_HALB);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 0)));
        result = ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(zeitintervall);
        assertThat(result, is(11008006));

        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 45)));
        result = ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(zeitintervall);
        assertThat(result, is(11007006));

        zeitintervall.setType(TypeZeitintervall.BLOCK);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        result = ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(zeitintervall);
        assertThat(result, is(15000000));

    }

    @Test
    public void getFirstStepSortingIndex() {
        Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 45)));

        int result = ZeitintervallSortingIndexUtil.getFirstStepSortingIndex(zeitintervall);
        assertThat(result, is(10000000));

        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 45)));

        result = ZeitintervallSortingIndexUtil.getFirstStepSortingIndex(zeitintervall);
        assertThat(result, is(20000000));

        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 45)));

        result = ZeitintervallSortingIndexUtil.getFirstStepSortingIndex(zeitintervall);
        assertThat(result, is(30000000));

        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 45)));

        result = ZeitintervallSortingIndexUtil.getFirstStepSortingIndex(zeitintervall);
        assertThat(result, is(40000000));

        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 45)));

        result = ZeitintervallSortingIndexUtil.getFirstStepSortingIndex(zeitintervall);
        assertThat(result, is(50000000));

        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(10, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 45)));

        result = ZeitintervallSortingIndexUtil.getFirstStepSortingIndex(zeitintervall);
        assertThat(result, is(0));
    }

    @Test
    public void getSecondStepSortingIndex() {
        Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        int result = ZeitintervallSortingIndexUtil.getSecondStepSortingIndex(zeitintervall);
        assertThat(result, is(1000000));

        zeitintervall.setType(TypeZeitintervall.STUNDE_KOMPLETT);
        result = ZeitintervallSortingIndexUtil.getSecondStepSortingIndex(zeitintervall);
        assertThat(result, is(1000000));

        zeitintervall.setType(TypeZeitintervall.STUNDE_KOMPLETT);
        result = ZeitintervallSortingIndexUtil.getSecondStepSortingIndex(zeitintervall);
        assertThat(result, is(1000000));

        zeitintervall.setType(TypeZeitintervall.BLOCK);
        result = ZeitintervallSortingIndexUtil.getSecondStepSortingIndex(zeitintervall);
        assertThat(result, is(5000000));

        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        result = ZeitintervallSortingIndexUtil.getSecondStepSortingIndex(zeitintervall);
        assertThat(result, is(0));

        zeitintervall.setType(TypeZeitintervall.GESAMT);
        result = ZeitintervallSortingIndexUtil.getSecondStepSortingIndex(zeitintervall);
        assertThat(result, is(0));
    }

    @Test
    public void getThirdAndFourthStepSortingIndex() {
        Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 45)));

        int result = ZeitintervallSortingIndexUtil.getThirdAndFourthStepSortingIndex(zeitintervall);
        assertThat(result, is(3002));

        zeitintervall.setType(TypeZeitintervall.STUNDE_HALB);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 30)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)));

        result = ZeitintervallSortingIndexUtil.getThirdAndFourthStepSortingIndex(zeitintervall);
        assertThat(result, is(4002));

        zeitintervall.setType(TypeZeitintervall.STUNDE_KOMPLETT);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(1, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(2, 0)));

        result = ZeitintervallSortingIndexUtil.getThirdAndFourthStepSortingIndex(zeitintervall);
        assertThat(result, is(8008));
    }

    @Test
    public void getQuarterHourIndexForTime() {
        Integer quarterHourIndex = ZeitintervallSortingIndexUtil.getQuarterHourIndexForTime(LocalTime.of(0, 0));
        assertThat(quarterHourIndex, is(0));

        quarterHourIndex = ZeitintervallSortingIndexUtil.getQuarterHourIndexForTime(LocalTime.of(0, 15));
        assertThat(quarterHourIndex, is(1));

        quarterHourIndex = ZeitintervallSortingIndexUtil.getQuarterHourIndexForTime(LocalTime.of(1, 15));
        assertThat(quarterHourIndex, is(5));

        quarterHourIndex = ZeitintervallSortingIndexUtil.getQuarterHourIndexForTime(LocalTime.of(12, 45));
        assertThat(quarterHourIndex, is(51));

        quarterHourIndex = ZeitintervallSortingIndexUtil.getQuarterHourIndexForTime(LocalTime.of(15, 0));
        assertThat(quarterHourIndex, is(60));

        quarterHourIndex = ZeitintervallSortingIndexUtil.getQuarterHourIndexForTime(LocalTime.of(15, 30));
        assertThat(quarterHourIndex, is(62));

        quarterHourIndex = ZeitintervallSortingIndexUtil.getQuarterHourIndexForTime(LocalTime.of(23, 45));
        assertThat(quarterHourIndex, is(95));

        quarterHourIndex = ZeitintervallSortingIndexUtil.getQuarterHourIndexForTime(LocalTime.of(23, 59));
        assertThat(quarterHourIndex, is(96));

        quarterHourIndex = ZeitintervallSortingIndexUtil.getQuarterHourIndexForTime(LocalTime.MAX);
        assertThat(quarterHourIndex, is(96));
    }

}
