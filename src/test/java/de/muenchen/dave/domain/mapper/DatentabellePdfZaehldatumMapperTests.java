package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.laden.messwerte.LadeMesswerteDTO;
import de.muenchen.dave.domain.dtos.messstelle.FahrzeugOptionsDTO;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldaten;
import de.muenchen.dave.domain.pdf.helper.DatentabellePdfZaehldatum;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class DatentabellePdfZaehldatumMapperTests {

    private final DatentabellePdfZaehldatumMapper mapper = new DatentabellePdfZaehldatumMapperImpl();

    @Test
    void fahrzeugOptionsToDatentabellePdfZaehldaten() {
        final FahrzeugOptionsDTO fahrzeugOptionsDTO = new FahrzeugOptionsDTO();
        fahrzeugOptionsDTO.setKraftfahrzeugverkehr(false);
        fahrzeugOptionsDTO.setSchwerverkehr(true);
        fahrzeugOptionsDTO.setGueterverkehr(false);
        fahrzeugOptionsDTO.setSchwerverkehrsanteilProzent(true);
        fahrzeugOptionsDTO.setGueterverkehrsanteilProzent(false);
        fahrzeugOptionsDTO.setRadverkehr(true);
        fahrzeugOptionsDTO.setFussverkehr(false);
        fahrzeugOptionsDTO.setLastkraftwagen(true);
        fahrzeugOptionsDTO.setLastzuege(false);
        fahrzeugOptionsDTO.setBusse(true);
        fahrzeugOptionsDTO.setKraftraeder(false);
        fahrzeugOptionsDTO.setPersonenkraftwagen(true);
        fahrzeugOptionsDTO.setLieferwagen(false);

        final DatentabellePdfZaehldaten expected = new DatentabellePdfZaehldaten();
        expected.setShowPersonenkraftwagen(fahrzeugOptionsDTO.isPersonenkraftwagen());
        expected.setShowLastkraftwagen(fahrzeugOptionsDTO.isLastkraftwagen());
        expected.setShowLastzuege(fahrzeugOptionsDTO.isLastzuege());
        expected.setShowLieferwagen(fahrzeugOptionsDTO.isLieferwagen());
        expected.setShowBusse(fahrzeugOptionsDTO.isBusse());
        expected.setShowKraftraeder(fahrzeugOptionsDTO.isKraftraeder());
        expected.setShowRadverkehr(fahrzeugOptionsDTO.isRadverkehr());
        expected.setShowFussverkehr(fahrzeugOptionsDTO.isFussverkehr());
        expected.setShowKraftfahrzeugverkehr(fahrzeugOptionsDTO.isKraftfahrzeugverkehr());
        expected.setShowSchwerverkehr(fahrzeugOptionsDTO.isSchwerverkehr());
        expected.setShowGueterverkehr(fahrzeugOptionsDTO.isGueterverkehr());
        expected.setShowSchwerverkehrsanteilProzent(fahrzeugOptionsDTO.isSchwerverkehrsanteilProzent());
        expected.setShowGueterverkehrsanteilProzent(fahrzeugOptionsDTO.isGueterverkehrsanteilProzent());

        Assertions.assertThat(mapper.fahrzeugOptionsToDatentabellePdfZaehldaten(fahrzeugOptionsDTO))
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("activeTabsFahrzeugtypen", "activeTabsFahrzeugklassen", "activeTabsAnteile", "showPkwEinheiten")
                .isEqualTo(expected);
    }

    @Test
    void ladeMesswerteDTO2bean() {
        final LadeMesswerteDTO dto = new LadeMesswerteDTO();
        dto.setSortingIndex(1);
        dto.setType("Stunde");
        dto.setStartUhrzeit(LocalTime.of(0, 0));
        dto.setEndeUhrzeit(LocalTime.of(0, 15));
        dto.setPkw(1);
        dto.setLkw(2);
        dto.setLfw(3);
        dto.setLastzuege(4);
        dto.setBusse(5);
        dto.setKraftraeder(6);
        dto.setFahrradfahrer(7);
        dto.setFussgaenger(8);
        dto.setKfz(9);
        dto.setSchwerverkehr(10);
        dto.setGueterverkehr(11);
        dto.setAnteilSchwerverkehrAnKfzProzent(1.2);
        dto.setAnteilGueterverkehrAnKfzProzent(1.3);

        final DatentabellePdfZaehldatum expected = new DatentabellePdfZaehldatum();
        expected.setType(dto.getType());
        expected.setStartUhrzeit("00:00");
        expected.setEndeUhrzeit("00:15");
        expected.setPkw(dto.getPkw());
        expected.setLkw(dto.getLkw());
        expected.setLastzuege(dto.getLastzuege());
        expected.setLfw(dto.getLfw());
        expected.setBusse(dto.getBusse());
        expected.setKraftraeder(dto.getKraftraeder());
        expected.setFahrradfahrer(dto.getFahrradfahrer());
        expected.setFussgaenger(dto.getFussgaenger());
        expected.setKfz(BigDecimal.valueOf(dto.getKfz()));
        expected.setSchwerverkehr(BigDecimal.valueOf(dto.getSchwerverkehr()));
        expected.setGueterverkehr(BigDecimal.valueOf(dto.getGueterverkehr()));
        expected.setAnteilSchwerverkehrAnKfzProzent(BigDecimal.valueOf(dto.getAnteilSchwerverkehrAnKfzProzent()));
        expected.setAnteilGueterverkehrAnKfzProzent(BigDecimal.valueOf(dto.getAnteilGueterverkehrAnKfzProzent()));

        Assertions.assertThat(this.mapper.ladeMesswerteDTO2bean(dto))
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("pkwEinheiten", "gesamt")
                .isEqualTo(expected);
    }
}
