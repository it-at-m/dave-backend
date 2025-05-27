package de.muenchen.dave.services.messstelle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ValidierungServiceTest {

    private final ValidierungService serviceToTest = new ValidierungService(null, null);

    @Test
    void hasMinimuOfTwoUnauffaelligeTage() {
        long numberOfUnauffaelligeTage = 1;
        boolean result = serviceToTest.hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage);
        assertThat(result, is(false));

        numberOfUnauffaelligeTage = 2;
        result = serviceToTest.hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage);
        assertThat(result, is(true));

        numberOfUnauffaelligeTage = 10;
        result = serviceToTest.hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage);
        assertThat(result, is(true));
    }

    @Test
    void hasMinimuOfFiftyPercentUnauffaelligeTage() {
        long numberOfRelevantKalendertage = 10;
        long numberOfUnauffaelligeTage = 1;
        boolean result = serviceToTest.hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        assertThat(result, is(false));

        numberOfUnauffaelligeTage = 5;
        result = serviceToTest.hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        assertThat(result, is(true));

        numberOfUnauffaelligeTage = 10;
        result = serviceToTest.hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        assertThat(result, is(true));
    }
}
