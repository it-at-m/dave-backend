package de.muenchen.dave.services.messstelle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.enums.Fahrzeugklasse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ValidierungServiceTest {

    private final ValidierungService validierungService = new ValidierungService(null, null);

    @Test
    void hasMinimuOfTwoUnauffaelligeTage() {
        long numberOfUnauffaelligeTage = 1;
        boolean result = validierungService.hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage);
        assertThat(result, is(false));

        numberOfUnauffaelligeTage = 2;
        result = validierungService.hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage);
        assertThat(result, is(true));

        numberOfUnauffaelligeTage = 10;
        result = validierungService.hasMinimuOfTwoUnauffaelligeTage(numberOfUnauffaelligeTage);
        assertThat(result, is(true));
    }

    @Test
    void hasMinimuOfFiftyPercentUnauffaelligeTage() {
        long numberOfRelevantKalendertage = 10;
        long numberOfUnauffaelligeTage = 1;
        boolean result = validierungService.hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        assertThat(result, is(false));

        numberOfUnauffaelligeTage = 5;
        result = validierungService.hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        assertThat(result, is(true));

        numberOfUnauffaelligeTage = 10;
        result = validierungService.hasMinimuOfFiftyPercentUnauffaelligeTage(numberOfUnauffaelligeTage, numberOfRelevantKalendertage);
        assertThat(result, is(true));
    }

    void areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen() {
        var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        var result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseAchtPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));
    }

    @Test
    void areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen() {
        var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        var result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseZweiPlusEinsChoosen(fahrzeugOptions);
        assertThat(result, is(false));
    }

    @Test
    void areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen() {
        var fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        var result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(true));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(false);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(true);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(true);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(true);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(true);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(true);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(true);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(true);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(true);
        fahrzeugOptions.setLieferwagen(false);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));

        fahrzeugOptions = new FahrzeugOptionsDTO();
        fahrzeugOptions.setKraftfahrzeugverkehr(true);
        fahrzeugOptions.setSchwerverkehr(false);
        fahrzeugOptions.setSchwerverkehrsanteilProzent(false);
        fahrzeugOptions.setGueterverkehr(false);
        fahrzeugOptions.setGueterverkehrsanteilProzent(false);
        fahrzeugOptions.setLastkraftwagen(false);
        fahrzeugOptions.setLastzuege(false);
        fahrzeugOptions.setBusse(false);
        fahrzeugOptions.setKraftraeder(false);
        fahrzeugOptions.setPersonenkraftwagen(false);
        fahrzeugOptions.setLieferwagen(true);
        fahrzeugOptions.setRadverkehr(true);
        result = validierungService.areFahrzeugoptionsForFahrzeugklasseSummeKfzChoosen(fahrzeugOptions);
        assertThat(result, is(false));
    }

    @Test
    void isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare() {
        var fahrzeugklasse = Fahrzeugklasse.ACHT_PLUS_EINS;
        var fahrzeugklasseToCompare = Fahrzeugklasse.ACHT_PLUS_EINS;
        var result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(true));

        fahrzeugklasse = Fahrzeugklasse.ZWEI_PLUS_EINS;
        fahrzeugklasseToCompare = Fahrzeugklasse.ACHT_PLUS_EINS;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(true));

        fahrzeugklasse = Fahrzeugklasse.SUMME_KFZ;
        fahrzeugklasseToCompare = Fahrzeugklasse.ACHT_PLUS_EINS;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(true));

        fahrzeugklasse = Fahrzeugklasse.RAD;
        fahrzeugklasseToCompare = Fahrzeugklasse.ACHT_PLUS_EINS;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(false));

        fahrzeugklasse = Fahrzeugklasse.ACHT_PLUS_EINS;
        fahrzeugklasseToCompare = Fahrzeugklasse.ZWEI_PLUS_EINS;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(false));

        fahrzeugklasse = Fahrzeugklasse.ZWEI_PLUS_EINS;
        fahrzeugklasseToCompare = Fahrzeugklasse.ZWEI_PLUS_EINS;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(true));

        fahrzeugklasse = Fahrzeugklasse.SUMME_KFZ;
        fahrzeugklasseToCompare = Fahrzeugklasse.ZWEI_PLUS_EINS;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(true));

        fahrzeugklasse = Fahrzeugklasse.RAD;
        fahrzeugklasseToCompare = Fahrzeugklasse.ZWEI_PLUS_EINS;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(false));

        fahrzeugklasse = Fahrzeugklasse.ACHT_PLUS_EINS;
        fahrzeugklasseToCompare = Fahrzeugklasse.SUMME_KFZ;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(false));

        fahrzeugklasse = Fahrzeugklasse.ZWEI_PLUS_EINS;
        fahrzeugklasseToCompare = Fahrzeugklasse.SUMME_KFZ;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(false));

        fahrzeugklasse = Fahrzeugklasse.SUMME_KFZ;
        fahrzeugklasseToCompare = Fahrzeugklasse.SUMME_KFZ;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(true));

        fahrzeugklasse = Fahrzeugklasse.RAD;
        fahrzeugklasseToCompare = Fahrzeugklasse.SUMME_KFZ;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(false));

        fahrzeugklasse = Fahrzeugklasse.ACHT_PLUS_EINS;
        fahrzeugklasseToCompare = Fahrzeugklasse.RAD;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(false));

        fahrzeugklasse = Fahrzeugklasse.ZWEI_PLUS_EINS;
        fahrzeugklasseToCompare = Fahrzeugklasse.RAD;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(false));

        fahrzeugklasse = Fahrzeugklasse.SUMME_KFZ;
        fahrzeugklasseToCompare = Fahrzeugklasse.RAD;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(false));

        fahrzeugklasse = Fahrzeugklasse.RAD;
        fahrzeugklasseToCompare = Fahrzeugklasse.RAD;
        result = validierungService.isFahrzeugklasseContainedInTheGivenFahrzeugklasseToCompare(fahrzeugklasse, fahrzeugklasseToCompare);
        assertThat(result, is(true));
    }
}
