package de.muenchen.dave.spring.services.ladezaehldaten;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatenTableDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumTageswertDTO;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.util.DaveConstants;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
class LadeZaehldatenServiceSpringTest {

    private List<Zeitintervall> zeitintervalle;

    private UUID zaehlungId;

    @Autowired
    private LadeZaehldatenService ladeZaehldatenService;

    @MockBean
    private ZeitintervallRepository zeitintervallRepository;

    @MockBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @MockBean
    private MessstelleIndex messstelleIndex;

    @MockBean
    private CustomSuggestIndex customSuggestIndex;

    @BeforeEach
    public void beforeEach() {
        zaehlungId = UUID.randomUUID();
        zeitintervalle = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(0, 0));
        for (int index = 0; index < 96; index++) {
            zeitintervalle.add(TestUtils.createZeitintervall(
                    zaehlungId,
                    startTime,
                    1,
                    1,
                    2,
                    null));
            startTime = startTime.plusMinutes(15);
        }
        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndFahrbeziehungVonAndFahrbeziehungNachAndFahrbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anySet())).thenReturn(zeitintervalle);

    }

    @Test
    public void shouldZeitintervallBeReturned() {
        final Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setSortingIndex(ZeitintervallSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_KFZ);
        Boolean result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_00_06),
                Boolean.class);
        assertThat(result, is(false));

        zeitintervall.setSortingIndex(ZeitintervallSortingIndexUtil.SORTING_INDEX_GESAMT_DAY);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_00_06),
                Boolean.class);
        assertThat(result, is(false));

        zeitintervall.setSortingIndex(ZeitintervallSortingIndexUtil.SORTING_INDEX_GESAMT_DAY);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_00_24),
                Boolean.class);
        assertThat(result, is(true));

        zeitintervall.setSortingIndex(1);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_00_24),
                Boolean.class);
        assertThat(result, is(true));

        zeitintervall.setSortingIndex(1);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_00_06),
                Boolean.class);
        assertThat(result, is(true));

        zeitintervall.setSortingIndex(12000000);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_06_19),
                Boolean.class);
        assertThat(result, is(false));

        zeitintervall.setSortingIndex(22000000);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_06_19),
                Boolean.class);
        assertThat(result, is(false));

        zeitintervall.setSortingIndex(32000000);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_06_19),
                Boolean.class);
        assertThat(result, is(false));

        zeitintervall.setSortingIndex(42000000);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_06_19),
                Boolean.class);
        assertThat(result, is(false));

        zeitintervall.setSortingIndex(52000000);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_06_19),
                Boolean.class);
        assertThat(result, is(false));

        zeitintervall.setSortingIndex(1);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_06_19),
                Boolean.class);
        assertThat(result, is(true));

        zeitintervall.setSortingIndex(52000000);
        result = TestUtils.privateStaticMethodCall(
                "shouldZeitintervallBeReturned",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, Zeitblock.class),
                ArrayUtils.toArray(zeitintervall, Zeitblock.ZB_06_10),
                Boolean.class);
        assertThat(result, is(true));
    }

    @Test
    public void mapToZaehldatum() {
        OptionsDTO options = new OptionsDTO();
        options.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setType(TypeZeitintervall.GESAMT);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)));
        zeitintervall.setHochrechnung(new Hochrechnung());
        zeitintervall.getHochrechnung().setHochrechnungKfz(BigDecimal.valueOf(10.8));
        zeitintervall.getHochrechnung().setHochrechnungGv(BigDecimal.valueOf(5.4));
        zeitintervall.getHochrechnung().setHochrechnungSv(null);
        zeitintervall.getHochrechnung().setHochrechnungRad(100);
        final PkwEinheit pkwEinheit = new PkwEinheit();
        pkwEinheit.setPkw(BigDecimal.valueOf(2));
        pkwEinheit.setLkw(BigDecimal.ONE);
        pkwEinheit.setLastzuege(BigDecimal.ONE);
        pkwEinheit.setBusse(BigDecimal.ONE);
        pkwEinheit.setKraftraeder(BigDecimal.ONE);
        pkwEinheit.setFahrradfahrer(BigDecimal.ONE);
        LadeZaehldatumDTO result = TestUtils.privateStaticMethodCall(
                "mapToZaehldatum",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, PkwEinheit.class, OptionsDTO.class),
                ArrayUtils.toArray(zeitintervall, pkwEinheit, options),
                LadeZaehldatumDTO.class);
        LadeZaehldatumTageswertDTO expectedTageswert = new LadeZaehldatumTageswertDTO();
        expectedTageswert.setType(LadeZaehldatenService.TAGESWERT);
        expectedTageswert.setStartUhrzeit(LocalTime.of(6, 0));
        expectedTageswert.setEndeUhrzeit(LocalTime.of(19, 0));
        expectedTageswert.setKfz(BigDecimal.valueOf(11));
        expectedTageswert.setGueterverkehr(BigDecimal.valueOf(5));
        expectedTageswert.setSchwerverkehr(BigDecimal.ZERO);
        expectedTageswert.setFahrradfahrer(100);
        assertThat(result, is(expectedTageswert));

        options.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        zeitintervall = new Zeitintervall();
        zeitintervall.setType(TypeZeitintervall.GESAMT);
        zeitintervall.setStartUhrzeit(LocalDateTime.MIN);
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59)));
        zeitintervall.setPkw(5);
        zeitintervall.setLkw(0);
        result = TestUtils.privateStaticMethodCall(
                "mapToZaehldatum",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, PkwEinheit.class, OptionsDTO.class),
                ArrayUtils.toArray(zeitintervall, pkwEinheit, options),
                LadeZaehldatumDTO.class);
        LadeZaehldatumDTO expected = new LadeZaehldatumDTO();
        expected.setType(LadeZaehldatenService.GESAMT);
        expected.setStartUhrzeit(LocalTime.MIN);
        expected.setEndeUhrzeit(LocalTime.of(23, 59));
        expected.setPkw(5);
        expected.setLkw(0);
        expected.setLastzuege(null);
        expected.setBusse(null);
        expected.setKraftraeder(null);
        expected.setFahrradfahrer(null);
        expected.setFussgaenger(null);
        expected.setPkwEinheiten(10);
        assertThat(result, is(expected));

        options.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        zeitintervall.setType(TypeZeitintervall.STUNDE_KOMPLETT);
        result = TestUtils.privateStaticMethodCall(
                "mapToZaehldatum",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, PkwEinheit.class, OptionsDTO.class),
                ArrayUtils.toArray(zeitintervall, pkwEinheit, options),
                LadeZaehldatumDTO.class);
        expected.setType(null);
        assertThat(result, is(expected));

        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        zeitintervall.setType(TypeZeitintervall.STUNDE_KOMPLETT);
        result = TestUtils.privateStaticMethodCall(
                "mapToZaehldatum",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, PkwEinheit.class, OptionsDTO.class),
                ArrayUtils.toArray(zeitintervall, pkwEinheit, options),
                LadeZaehldatumDTO.class);
        expected.setType(LadeZaehldatenService.STUNDE);
        assertThat(result, is(expected));

        zeitintervall.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        // Spitzenstunde eines Blockes, NICHT die gleitende Spitzenstunde des Tages!
        zeitintervall.setSortingIndex(12000000);
        result = TestUtils.privateStaticMethodCall(
                "mapToZaehldatum",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, PkwEinheit.class, OptionsDTO.class),
                ArrayUtils.toArray(zeitintervall, pkwEinheit, options),
                LadeZaehldatumDTO.class);
        expected.setType(LadeZaehldatenService.SPITZENSTUNDE_BLOCK_KFZ);
        assertThat(result, is(expected));

        // Spitzenstunde des Tages, NICHT die eines Blockes
        zeitintervall.setSortingIndex(ZeitintervallSortingIndexUtil.SORTING_INDEX_SPITZEN_STUNDE_DAY_KFZ);
        result = TestUtils.privateStaticMethodCall(
                "mapToZaehldatum",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, PkwEinheit.class, OptionsDTO.class),
                ArrayUtils.toArray(zeitintervall, pkwEinheit, options),
                LadeZaehldatumDTO.class);
        expected.setType(LadeZaehldatenService.SPITZENSTUNDE_TAG_KFZ);
        assertThat(result, is(expected));

        zeitintervall.setType(TypeZeitintervall.BLOCK);
        result = TestUtils.privateStaticMethodCall(
                "mapToZaehldatum",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(Zeitintervall.class, PkwEinheit.class, OptionsDTO.class),
                ArrayUtils.toArray(zeitintervall, pkwEinheit, options),
                LadeZaehldatumDTO.class);
        expected.setType(LadeZaehldatenService.BLOCK);
        assertThat(result, is(expected));
    }

    @Test
    public void getTypesAccordingChosenOptions() {
        OptionsDTO options = new OptionsDTO();

        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        Set<TypeZeitintervall> result = TestUtils.privateStaticMethodCall(
                "getTypesAccordingChosenOptions",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(OptionsDTO.class),
                ArrayUtils.toArray(options),
                Set.class);
        Set<TypeZeitintervall> expected = SetUtils.hashSet(TypeZeitintervall.STUNDE_VIERTEL);
        assertThat(result, is(expected));

        options.setIntervall(ZaehldatenIntervall.STUNDE_HALB);
        result = TestUtils.privateStaticMethodCall(
                "getTypesAccordingChosenOptions",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(OptionsDTO.class),
                ArrayUtils.toArray(options),
                Set.class);
        expected = SetUtils.hashSet(TypeZeitintervall.STUNDE_HALB);
        assertThat(result, is(expected));

        options.setIntervall(ZaehldatenIntervall.STUNDE_KOMPLETT);
        result = TestUtils.privateStaticMethodCall(
                "getTypesAccordingChosenOptions",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(OptionsDTO.class),
                ArrayUtils.toArray(options),
                Set.class);
        expected = SetUtils.hashSet(TypeZeitintervall.STUNDE_KOMPLETT);
        assertThat(result, is(expected));

        options.setIntervall(null);
        options.setSpitzenstunde(false);
        options.setSpitzenstundeKfz(false);
        options.setSpitzenstundeRad(false);
        options.setSpitzenstundeFuss(false);
        result = TestUtils.privateStaticMethodCall(
                "getTypesAccordingChosenOptions",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(OptionsDTO.class),
                ArrayUtils.toArray(options),
                Set.class);
        expected = SetUtils.hashSet();
        assertThat(result, is(expected));

        options.setIntervall(null);
        options.setSpitzenstunde(true);
        options.setSpitzenstundeKfz(true);
        options.setSpitzenstundeRad(false);
        options.setSpitzenstundeFuss(false);
        result = TestUtils.privateStaticMethodCall(
                "getTypesAccordingChosenOptions",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(OptionsDTO.class),
                ArrayUtils.toArray(options),
                Set.class);
        expected = SetUtils.hashSet(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        assertThat(result, is(expected));

        options.setIntervall(null);
        options.setSpitzenstunde(true);
        options.setSpitzenstundeKfz(true);
        options.setSpitzenstundeRad(false);
        options.setSpitzenstundeFuss(true);
        result = TestUtils.privateStaticMethodCall(
                "getTypesAccordingChosenOptions",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(OptionsDTO.class),
                ArrayUtils.toArray(options),
                Set.class);
        expected = SetUtils.hashSet(
                TypeZeitintervall.SPITZENSTUNDE_KFZ,
                TypeZeitintervall.SPITZENSTUNDE_FUSS);
        assertThat(result, is(expected));

        options.setIntervall(null);
        options.setSpitzenstunde(true);
        options.setSpitzenstundeKfz(true);
        options.setSpitzenstundeRad(true);
        options.setSpitzenstundeFuss(true);
        result = TestUtils.privateStaticMethodCall(
                "getTypesAccordingChosenOptions",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(OptionsDTO.class),
                ArrayUtils.toArray(options),
                Set.class);
        expected = SetUtils.hashSet(
                TypeZeitintervall.SPITZENSTUNDE_KFZ,
                TypeZeitintervall.SPITZENSTUNDE_RAD,
                TypeZeitintervall.SPITZENSTUNDE_FUSS);
        assertThat(result, is(expected));

        options.setSpitzenstunde(false);
        options.setBlocksumme(true);
        options.setZeitblock(Zeitblock.ZB_00_06);
        result = TestUtils.privateStaticMethodCall(
                "getTypesAccordingChosenOptions",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(OptionsDTO.class),
                ArrayUtils.toArray(options),
                Set.class);
        expected = SetUtils.hashSet(TypeZeitintervall.BLOCK);
        assertThat(result, is(expected));

        options.setSpitzenstunde(false);
        options.setBlocksumme(true);
        options.setZeitblock(Zeitblock.ZB_06_22);
        result = TestUtils.privateStaticMethodCall(
                "getTypesAccordingChosenOptions",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(OptionsDTO.class),
                ArrayUtils.toArray(options),
                Set.class);
        expected = SetUtils.hashSet(TypeZeitintervall.BLOCK_SPEZIAL);
        assertThat(result, is(expected));

        options.setBlocksumme(false);
        options.setTagessumme(true);
        options.setZeitblock(Zeitblock.ZB_06_10);
        result = TestUtils.privateStaticMethodCall(
                "getTypesAccordingChosenOptions",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(OptionsDTO.class),
                ArrayUtils.toArray(options),
                Set.class);
        expected = SetUtils.hashSet(TypeZeitintervall.GESAMT);
        assertThat(result, is(expected));

        options.setBlocksumme(false);
        options.setTagessumme(true);
        options.setZeitblock(Zeitblock.ZB_06_22);
        result = TestUtils.privateStaticMethodCall(
                "getTypesAccordingChosenOptions",
                LadeZaehldatenService.class,
                ArrayUtils.toArray(OptionsDTO.class),
                ArrayUtils.toArray(options),
                Set.class);
        expected = new HashSet<>();
        assertThat(result, is(expected));

    }

    @Test
    public void isZeitintervallForTageswert() {
        final Zeitintervall zeitintervall = new Zeitintervall();
        final OptionsDTO chosenOptions = new OptionsDTO();
        zeitintervall.setType(TypeZeitintervall.GESAMT);
        chosenOptions.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        boolean result = LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, chosenOptions);
        assertThat(result, is(true));

        zeitintervall.setType(TypeZeitintervall.GESAMT);
        chosenOptions.setZaehldauer(Zaehldauer.DAUER_13_STUNDEN);
        result = LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, chosenOptions);
        assertThat(result, is(true));

        zeitintervall.setType(TypeZeitintervall.GESAMT);
        chosenOptions.setZaehldauer(Zaehldauer.DAUER_16_STUNDEN);
        result = LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, chosenOptions);
        assertThat(result, is(true));

        zeitintervall.setType(TypeZeitintervall.GESAMT);
        chosenOptions.setZaehldauer(Zaehldauer.SONSTIGE);
        result = LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, chosenOptions);
        assertThat(result, is(true));

        zeitintervall.setType(TypeZeitintervall.GESAMT);
        chosenOptions.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        result = LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, chosenOptions);
        assertThat(result, is(false));

        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        chosenOptions.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        result = LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, chosenOptions);
        assertThat(result, is(false));

        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        chosenOptions.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        result = LadeZaehldatenService.isZeitintervallForTageswert(zeitintervall, chosenOptions);
        assertThat(result, is(false));
    }

    @Test
    public void createFahrbewegungKreisverkehr() {
        // Kreuzung
        assertThat(LadeZaehldatenService.createFahrbewegungKreisverkehr(1, 2, false), is(nullValue()));
        assertThat(LadeZaehldatenService.createFahrbewegungKreisverkehr(null, null, false), is(nullValue()));
        assertThat(LadeZaehldatenService.createFahrbewegungKreisverkehr(1, null, false), is(nullValue()));
        assertThat(LadeZaehldatenService.createFahrbewegungKreisverkehr(null, 2, false), is(nullValue()));

        // Kreisverkehr
        assertThat(LadeZaehldatenService.createFahrbewegungKreisverkehr(1, 2, true), is(nullValue()));
        assertThat(LadeZaehldatenService.createFahrbewegungKreisverkehr(null, null, true), is(nullValue()));
        assertThat(LadeZaehldatenService.createFahrbewegungKreisverkehr(1, null, true), is(FahrbewegungKreisverkehr.HINEIN));
        assertThat(LadeZaehldatenService.createFahrbewegungKreisverkehr(null, 2, true), is(FahrbewegungKreisverkehr.HERAUS));
    }

    @Test
    public void ladeZaehldaten() throws DataNotFoundException {
        final Zaehlstelle zaehlstelle = new Zaehlstelle();
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(zaehlungId.toString());
        zaehlung.setPkwEinheit(new PkwEinheit());
        zaehlung.setKreisverkehr(false);
        zaehlstelle.setZaehlungen(Arrays.asList(zaehlung));

        when(zaehlstelleIndex.findByZaehlungenId(zaehlungId.toString())).thenReturn(Optional.ofNullable(zaehlstelle));
        final OptionsDTO chosenOptions = new OptionsDTO();
        chosenOptions.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        chosenOptions.setZeitblock(Zeitblock.ZB_00_24);
        chosenOptions.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        zeitintervalle.get(0).setType(TypeZeitintervall.BLOCK);
        LadeZaehldatenTableDTO ladeZaehldatenTable = ladeZaehldatenService.ladeZaehldaten(zaehlungId, chosenOptions);

        assertThat(ladeZaehldatenTable.getZaehldaten().size(), is(96));

        LadeZaehldatumDTO expected = new LadeZaehldatumDTO();
        expected.setType(LadeZaehldatenService.BLOCK);
        expected.setStartUhrzeit(LocalTime.MIN);
        expected.setEndeUhrzeit(LocalTime.of(0, 15));
        expected.setPkw(1);
        expected.setLkw(1);
        expected.setLastzuege(1);
        expected.setBusse(1);
        expected.setKraftraeder(1);
        expected.setFahrradfahrer(1);
        expected.setFussgaenger(1);
        expected.setPkwEinheiten(0);

        assertThat(ladeZaehldatenTable.getZaehldaten().get(0), is(expected));

        expected = new LadeZaehldatumDTO();
        expected.setStartUhrzeit(LocalTime.of(23, 45));
        expected.setEndeUhrzeit(LocalTime.of(23, 59));
        expected.setPkw(1);
        expected.setLkw(1);
        expected.setLastzuege(1);
        expected.setBusse(1);
        expected.setKraftraeder(1);
        expected.setFahrradfahrer(1);
        expected.setFussgaenger(1);
        expected.setPkwEinheiten(0);

        assertThat(ladeZaehldatenTable.getZaehldaten().get(95), is(expected));
    }

}
