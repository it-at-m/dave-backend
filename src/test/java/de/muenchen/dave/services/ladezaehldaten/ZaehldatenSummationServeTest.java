package de.muenchen.dave.services.ladezaehldaten;

import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.*;
import de.muenchen.dave.util.DaveConstants;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class ZaehldatenSummationServeTest {

    ZeitintervallSummationService test = new ZeitintervallSummationService();

    LocalDateTime time1 = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0));
    LocalDateTime time2 = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 15));


    @Test
    void twoIntervals() {
        Verkehrsbeziehung movement1 = new Verkehrsbeziehung();
        movement1.setVon(1);
        movement1.setNach(2);
        Verkehrsbeziehung movement2 = new Verkehrsbeziehung();
        movement2.setVon(1);
        movement2.setNach(3);
        final UUID id = UUID.randomUUID();
        Zeitintervall intervall11 = TestUtils.createZeitintervall(id, time1, 20,1,2, null);
        Zeitintervall intervall12 = TestUtils.createZeitintervall(id, time2, 50,1,3, null);

        Zeitintervall intervall21 = TestUtils.createZeitintervall(id, time1, 30,1,2, null);
        Zeitintervall intervall22 = TestUtils.createZeitintervall(id, time2, 70,1,3, null);

        //nicht korrekt berechnet
        intervall11.setSortingIndex(11008006);
        intervall21.setSortingIndex(11008006);

        intervall12.setSortingIndex(11008000);
        intervall22.setSortingIndex(11008000);

        intervall11.setBewegungsbeziehungId(UUID.randomUUID());
        intervall11.setLaengsverkehr(new Laengsverkehr());
        intervall11.setQuerungsverkehr(new Querungsverkehr());


        Map<Bewegungsbeziehung, List<Zeitintervall>> map = new HashMap<>();
        map.put(movement1, List.of(intervall11,intervall12));
        map.put(movement2, List.of(intervall21, intervall22));

        List<Zeitintervall> testIntervals = test.sumZeitintervelleOverBewegungsbeziehung(map);

        Zeitintervall intervallCompare1 = TestUtils.createZeitintervall(id, time1, 50,1,2, null);
        Zeitintervall intervallCompare2 = TestUtils.createZeitintervall(id, time2, 120,1,3, null);

        intervallCompare1.setBewegungsbeziehungId(null);
        intervallCompare1.setVerkehrsbeziehung(null);

        intervallCompare2.setBewegungsbeziehungId(null);
        intervallCompare2.setVerkehrsbeziehung(null);

        intervallCompare1.setSortingIndex(11008006);
        intervallCompare2.setSortingIndex(11008000);

        List<Zeitintervall> compare= List.of(intervallCompare1, intervallCompare2);

        //ZählungsID noch fehlerhaft in der zu testenden Klasse
        assertThat(testIntervals, is(compare));
    }
}
