package de.muenchen.dave.spring.services.auswertung;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungZaehlstelleKoordinateDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.services.IndexService;
import de.muenchen.dave.services.auswertung.AuswertungZaehlstellenKoordinateService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE",
        "refarch.gracefulshutdown.pre-wait-seconds=0" })
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
@Slf4j
class AuswertungZaehlstellenKoordinateServiceTest {

    @Autowired
    private AuswertungZaehlstellenKoordinateService auswertungZaehlstellenKoordinateService;

    @MockBean
    private IndexService indexService;

    @Test
    void getAuswertungZaehlstellenKoordinate() {
        final List<Zaehlstelle> zaehlstellen = new ArrayList<>();

        Zaehlstelle zaehlstelle = new Zaehlstelle();
        zaehlstelle.setZaehlungen(new ArrayList<>());
        zaehlstelle.setNummer("654321");
        zaehlstelle.setKommentar("Ein Kommentar");
        zaehlstelle.setPunkt(new GeoPoint(40.1234567, 9.1234567));
        Zaehlung zaehlung = new Zaehlung();
        zaehlung.setDatum(LocalDate.of(2018, 1, 1));
        zaehlstelle.getZaehlungen().add(zaehlung);
        zaehlstellen.add(zaehlstelle);

        zaehlstelle = new Zaehlstelle();
        zaehlstelle.setZaehlungen(new ArrayList<>());
        zaehlstelle.setNummer("123456");
        zaehlstelle.setKommentar("Ein Kommentar");
        zaehlstelle.setPunkt(new GeoPoint(48.1234567, 10.1234567));
        zaehlung = new Zaehlung();
        zaehlung.setDatum(LocalDate.of(2019, 1, 1));
        zaehlstelle.getZaehlungen().add(zaehlung);
        zaehlstellen.add(zaehlstelle);

        when(indexService.getAllZaehlstellen()).thenReturn(zaehlstellen);

        final List<LadeAuswertungZaehlstelleKoordinateDTO> resultList = auswertungZaehlstellenKoordinateService.getAuswertungZaehlstellenKoordinate();
        final List<LadeAuswertungZaehlstelleKoordinateDTO> expectedList = new ArrayList<>();
        LadeAuswertungZaehlstelleKoordinateDTO expected = new LadeAuswertungZaehlstelleKoordinateDTO();
        expected.setNummer("123456");
        expected.setKommentar("Ein Kommentar");
        expected.setLetzteZaehlung(LocalDate.of(2019, 1, 1));
        expected.setLat(48.1234567);
        expected.setLng(10.1234567);
        expectedList.add(expected);

        expected = new LadeAuswertungZaehlstelleKoordinateDTO();
        expected.setNummer("654321");
        expected.setKommentar("Ein Kommentar");
        expected.setLetzteZaehlung(LocalDate.of(2018, 1, 1));
        expected.setLat(40.1234567);
        expected.setLng(9.1234567);
        expectedList.add(expected);

        assertThat(resultList, is(expectedList));
    }

}
