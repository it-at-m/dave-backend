package de.muenchen.dave.spring;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.TestUtils;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanDTO;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import de.muenchen.dave.services.processzaehldaten.ProcessZaehldatenBelastungsplanService;
import de.muenchen.dave.util.DaveConstants;
import de.muenchen.dave.util.dataimport.ZeitintervallSortingIndexUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
public class ProcessZaehldatenBelastungsplanServiceSpringTest {

    @Autowired
    private ProcessZaehldatenBelastungsplanService processZaehldatenBelastungsplanService;

    @MockitoBean
    private ZeitintervallRepository zeitintervallRepository;

    @MockitoBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @MockitoBean
    private MessstelleIndex messstelleIndex;

    @MockitoBean
    private CustomSuggestIndex customSuggestIndex;

    @MockitoBean
    private LadeZaehldatenService ladeZaehldatenService;

    @Test
    public void ladeProcessedZaehldatenBelastungsplanKreuzung() throws DataNotFoundException {
        final UUID zaehlungId = UUID.randomUUID();
        final OptionsDTO options = new OptionsDTO();
        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        options.setKraftfahrzeugverkehr(true);
        options.setSchwerverkehr(true);
        options.setGueterverkehr(true);
        options.setFussverkehr(false);
        options.setRadverkehr(false);
        options.setSchwerverkehrsanteilProzent(false);
        options.setGueterverkehrsanteilProzent(false);

        final List<Zeitintervall> zeitintervalle = new ArrayList<>();

        Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MIN));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59)));
        zeitintervall.setType(TypeZeitintervall.GESAMT);
        zeitintervall.setPkw(1);
        zeitintervall.setLkw(2);
        zeitintervall.setLastzuege(3);
        zeitintervall.setBusse(4);
        zeitintervall.setKraftraeder(5);
        zeitintervall.setFahrradfahrer(6);
        zeitintervall.setFussgaenger(7);
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        zeitintervall.getVerkehrsbeziehung().setVon(5);
        zeitintervall.getVerkehrsbeziehung().setNach(2);
        zeitintervalle.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MIN));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59)));
        zeitintervall.setType(TypeZeitintervall.GESAMT);
        zeitintervall.setPkw(10);
        zeitintervall.setLkw(20);
        zeitintervall.setLastzuege(30);
        zeitintervall.setBusse(40);
        zeitintervall.setKraftraeder(50);
        zeitintervall.setFahrradfahrer(60);
        zeitintervall.setFussgaenger(70);
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        zeitintervall.getVerkehrsbeziehung().setVon(2);
        zeitintervall.getVerkehrsbeziehung().setNach(5);
        zeitintervalle.add(zeitintervall);

        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(zaehlungId.toString());
        Knotenarm knotenarm2 = new Knotenarm();
        knotenarm2.setNummer(2);
        knotenarm2.setStrassenname("knotenarm_2");
        Knotenarm knotenarm5 = new Knotenarm();
        knotenarm5.setNummer(5);
        knotenarm5.setStrassenname("knotenarm_5");
        zaehlung.setKnotenarme(Arrays.asList(knotenarm2, knotenarm5));
        zaehlung.setKategorien(Arrays.asList(Fahrzeug.KFZ, Fahrzeug.SV, Fahrzeug.GV, Fahrzeug.SV_P, Fahrzeug.GV_P, Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.N.toString());
        zaehlung.setKreisverkehr(false);
        zaehlung.setPkwEinheit(new PkwEinheit());

        Zaehlstelle zaehlstelle = new Zaehlstelle();
        zaehlstelle.setZaehlungen(List.of(zaehlung));

        when(zaehlstelleIndex.findByZaehlungenId(zaehlungId.toString())).thenReturn(Optional.ofNullable(zaehlstelle));
        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndTypeOrderBySortingIndexAsc(
                        zaehlungId,
                        Zeitblock.ZB_00_24.getStart(),
                        Zeitblock.ZB_00_24.getEnd(),
                        Zeitblock.ZB_00_24.getTypeZeitintervall()))
                .thenReturn(zeitintervalle);

        LadeBelastungsplanDTO ladeBelastungsplan = processZaehldatenBelastungsplanService
                .ladeProcessedZaehldatenBelastungsplan(zaehlungId.toString(), options);

        assertThat(ladeBelastungsplan.getValue1().getValues()[4][1], is(BigDecimal.valueOf(15)));
        assertThat(ladeBelastungsplan.getValue2().getValues()[4][1], is(BigDecimal.valueOf(9)));
        assertThat(ladeBelastungsplan.getValue3().getValues()[4][1], is(BigDecimal.valueOf(5)));

        assertThat(ladeBelastungsplan.getValue1().getValues()[1][4], is(BigDecimal.valueOf(150)));
        assertThat(ladeBelastungsplan.getValue2().getValues()[1][4], is(BigDecimal.valueOf(90)));
        assertThat(ladeBelastungsplan.getValue3().getValues()[1][4], is(BigDecimal.valueOf(50)));

        assertThat(ladeBelastungsplan.getValue1().getSumIn(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(150), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(15), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));
        assertThat(ladeBelastungsplan.getValue1().getSumOut(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(15), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(150), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));
        assertThat(ladeBelastungsplan.getValue1().getSum(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(165), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(165), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));

        assertThat(ladeBelastungsplan.getValue2().getSumIn(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(90), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(9), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));
        assertThat(ladeBelastungsplan.getValue2().getSumOut(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(9), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(90), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));
        assertThat(ladeBelastungsplan.getValue2().getSum(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(99), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(99), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));

        assertThat(ladeBelastungsplan.getValue3().getSumIn(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(50), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(5), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));
        assertThat(ladeBelastungsplan.getValue3().getSumOut(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(5), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(50), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));
        assertThat(ladeBelastungsplan.getValue3().getSum(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(55), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(55), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));

        final String[] strassen = { null, "knotenarm_2", null, null, "knotenarm_5", null, null, null };
        assertThat(ladeBelastungsplan.getStreets(), is(strassen));
    }

    @Test
    public void ladeProcessedZaehldatenBelastungsplanKreisverkehr() throws DataNotFoundException {
        final UUID zaehlungId = UUID.randomUUID();
        final OptionsDTO options = new OptionsDTO();
        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        options.setZeitblock(Zeitblock.ZB_00_24);
        options.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        options.setKraftfahrzeugverkehr(true);
        options.setSchwerverkehr(true);
        options.setGueterverkehr(true);
        options.setFussverkehr(false);
        options.setRadverkehr(false);
        options.setSchwerverkehrsanteilProzent(false);
        options.setGueterverkehrsanteilProzent(false);

        final List<Zeitintervall> zeitintervalle = new ArrayList<>();

        Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MIN));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59)));
        zeitintervall.setType(TypeZeitintervall.GESAMT);
        zeitintervall.setPkw(1);
        zeitintervall.setLkw(2);
        zeitintervall.setLastzuege(3);
        zeitintervall.setBusse(4);
        zeitintervall.setKraftraeder(5);
        zeitintervall.setFahrradfahrer(6);
        zeitintervall.setFussgaenger(7);
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        zeitintervall.getVerkehrsbeziehung().setVon(5);
        zeitintervall.getVerkehrsbeziehung().setNach(2);
        zeitintervalle.add(zeitintervall);

        zeitintervall = new Zeitintervall();
        zeitintervall.setZaehlungId(zaehlungId);
        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.MIN));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(23, 59)));
        zeitintervall.setType(TypeZeitintervall.GESAMT);
        zeitintervall.setPkw(10);
        zeitintervall.setLkw(20);
        zeitintervall.setLastzuege(30);
        zeitintervall.setBusse(40);
        zeitintervall.setKraftraeder(50);
        zeitintervall.setFahrradfahrer(60);
        zeitintervall.setFussgaenger(70);
        zeitintervall.setVerkehrsbeziehung(new Verkehrsbeziehung());
        zeitintervall.getVerkehrsbeziehung().setVon(2);
        zeitintervall.getVerkehrsbeziehung().setNach(5);
        zeitintervalle.add(zeitintervall);

        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(zaehlungId.toString());
        Knotenarm knotenarm2 = new Knotenarm();
        knotenarm2.setNummer(2);
        knotenarm2.setStrassenname("knotenarm_2");
        Knotenarm knotenarm5 = new Knotenarm();
        knotenarm5.setNummer(5);
        knotenarm5.setStrassenname("knotenarm_5");
        zaehlung.setKnotenarme(Arrays.asList(knotenarm2, knotenarm5));
        zaehlung.setKategorien(Arrays.asList(Fahrzeug.KFZ, Fahrzeug.SV, Fahrzeug.GV, Fahrzeug.SV_P, Fahrzeug.GV_P, Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.N.toString());
        zaehlung.setKreisverkehr(true);
        zaehlung.setPkwEinheit(new PkwEinheit());

        Zaehlstelle zaehlstelle = new Zaehlstelle();
        zaehlstelle.setZaehlungen(List.of(zaehlung));

        when(zaehlstelleIndex.findByZaehlungenId(zaehlungId.toString())).thenReturn(Optional.ofNullable(zaehlstelle));
        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndTypeOrderBySortingIndexAsc(
                        zaehlungId,
                        Zeitblock.ZB_00_24.getStart(),
                        Zeitblock.ZB_00_24.getEnd(),
                        Zeitblock.ZB_00_24.getTypeZeitintervall()))
                .thenReturn(zeitintervalle);

        LadeBelastungsplanDTO ladeBelastungsplan = processZaehldatenBelastungsplanService
                .ladeProcessedZaehldatenBelastungsplan(zaehlungId.toString(), options);

        System.err.println(ladeBelastungsplan);

        assertThat(ladeBelastungsplan.getValue1().getValues()[4][1], is(BigDecimal.valueOf(15)));
        assertThat(ladeBelastungsplan.getValue2().getValues()[4][1], is(BigDecimal.valueOf(9)));
        assertThat(ladeBelastungsplan.getValue3().getValues()[4][1], is(BigDecimal.valueOf(5)));

        assertThat(ladeBelastungsplan.getValue1().getValues()[1][4], is(BigDecimal.valueOf(150)));
        assertThat(ladeBelastungsplan.getValue2().getValues()[1][4], is(BigDecimal.valueOf(90)));
        assertThat(ladeBelastungsplan.getValue3().getValues()[1][4], is(BigDecimal.valueOf(50)));

        assertThat(ladeBelastungsplan.getValue1().getSumIn(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(15), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));
        assertThat(ladeBelastungsplan.getValue1().getSumOut(), nullValue());
        assertThat(ladeBelastungsplan.getValue1().getSum(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));

        assertThat(ladeBelastungsplan.getValue2().getSumIn(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(9), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));
        assertThat(ladeBelastungsplan.getValue2().getSumOut(), nullValue());
        assertThat(ladeBelastungsplan.getValue2().getSum(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));

        assertThat(ladeBelastungsplan.getValue3().getSumIn(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(5), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));
        assertThat(ladeBelastungsplan.getValue3().getSumOut(), nullValue());
        assertThat(ladeBelastungsplan.getValue3().getSum(), is(new BigDecimal[] { BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0),
                BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0) }));

        final String[] strassen = { null, "knotenarm_2", null, null, "knotenarm_5", null, null, null };
        assertThat(ladeBelastungsplan.getStreets(), is(strassen));
    }

    @Test
    void extractZeitintervalleSpitzenstunde() {
        final UUID zaehlungId = UUID.randomUUID();
        final OptionsDTO options = new OptionsDTO();
        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        options.setZeitblock(Zeitblock.ZB_06_10);
        options.setZeitauswahl(LadeZaehldatenService.ZEITAUSWAHL_SPITZENSTUNDE_KFZ);
        options.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);
        options.setKraftfahrzeugverkehr(true);
        options.setSchwerverkehr(true);
        options.setGueterverkehr(true);
        options.setFussverkehr(false);
        options.setRadverkehr(false);
        options.setSchwerverkehrsanteilProzent(false);
        options.setGueterverkehrsanteilProzent(false);

        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(zaehlungId.toString());
        Knotenarm knotenarm2 = new Knotenarm();
        knotenarm2.setNummer(2);
        knotenarm2.setStrassenname("knotenarm_2");
        Knotenarm knotenarm5 = new Knotenarm();
        knotenarm5.setNummer(5);
        knotenarm5.setStrassenname("knotenarm_5");
        zaehlung.setKnotenarme(Arrays.asList(knotenarm2, knotenarm5));
        zaehlung.setKategorien(Arrays.asList(Fahrzeug.KFZ, Fahrzeug.SV, Fahrzeug.GV, Fahrzeug.SV_P, Fahrzeug.GV_P, Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.N.toString());
        zaehlung.setKreisverkehr(false);

        Zeitintervall spitzenstunde = new Zeitintervall();
        spitzenstunde.setZaehlungId(zaehlungId);
        spitzenstunde.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 15)));
        spitzenstunde.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 15)));
        spitzenstunde.setType(TypeZeitintervall.GESAMT);
        spitzenstunde.setPkw(1);
        spitzenstunde.setLkw(1);
        spitzenstunde.setLastzuege(1);
        spitzenstunde.setBusse(1);
        spitzenstunde.setKraftraeder(1);
        spitzenstunde.setFahrradfahrer(1);
        spitzenstunde.setFussgaenger(1);
        spitzenstunde.setVerkehrsbeziehung(new Verkehrsbeziehung());
        spitzenstunde.getVerkehrsbeziehung().setVon(5);
        spitzenstunde.getVerkehrsbeziehung().setNach(2);
        spitzenstunde.setType(TypeZeitintervall.SPITZENSTUNDE_KFZ);
        spitzenstunde.setSortingIndex(ZeitintervallSortingIndexUtil.getSortingIndexWithinBlock(spitzenstunde));
        List<Zeitintervall> spitzenstunden = new ArrayList<>();
        spitzenstunden.add(spitzenstunde);

        when(ladeZaehldatenService.extractZeitintervalle(any(), any(), any(), any(), any(), nullable(FahrbewegungKreisverkehr.class), anySet()))
                .thenReturn(spitzenstunden);

        List<Zeitintervall> zeitintervalle = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 15));
        for (int index = 0; index < 4; index++) {
            zeitintervalle.add(TestUtils.createZeitintervall(
                    zaehlungId,
                    startTime,
                    1,
                    1,
                    2,
                    null));
            startTime = startTime.plusMinutes(15);
        }
        startTime = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 15));
        for (int index = 0; index < 4; index++) {
            zeitintervalle.add(TestUtils.createZeitintervall(
                    zaehlungId,
                    startTime,
                    2,
                    2,
                    1,
                    null));
            startTime = startTime.plusMinutes(15);
        }
        startTime = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 15));
        for (int index = 0; index < 4; index++) {
            zeitintervalle.add(TestUtils.createZeitintervall(
                    zaehlungId,
                    startTime,
                    3,
                    1,
                    null,
                    null));
            startTime = startTime.plusMinutes(15);
        }
        startTime = LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 15));
        for (int index = 0; index < 4; index++) {
            zeitintervalle.add(TestUtils.createZeitintervall(
                    zaehlungId,
                    startTime,
                    4,
                    2,
                    null,
                    null));
            startTime = startTime.plusMinutes(15);
        }

        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndTypeOrderBySortingIndexAsc(
                        zaehlungId,
                        LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 15)),
                        LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 15)),
                        TypeZeitintervall.STUNDE_VIERTEL))
                .thenReturn(zeitintervalle);

        final List<Zeitintervall> result = processZaehldatenBelastungsplanService.extractZeitintervalleSpitzenstunde(zaehlung, options);

        result.sort(Comparator.comparingInt(Zeitintervall::getPkw));

        assertThat(result.size(), is(4));

        assertThat(result.get(0).getPkw(), is(4));
        assertThat(result.get(0).getType(), is(TypeZeitintervall.SPITZENSTUNDE_KFZ));
        assertThat(result.get(0).getStartUhrzeit(), is(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 15))));
        assertThat(result.get(0).getEndeUhrzeit(), is(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 15))));
        assertThat(result.get(0).getVerkehrsbeziehung().getVon(), is(1));
        assertThat(result.get(0).getVerkehrsbeziehung().getNach(), is(2));

        assertThat(result.get(1).getPkw(), is(8));
        assertThat(result.get(1).getType(), is(TypeZeitintervall.SPITZENSTUNDE_KFZ));
        assertThat(result.get(1).getStartUhrzeit(), is(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 15))));
        assertThat(result.get(1).getEndeUhrzeit(), is(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 15))));
        assertThat(result.get(1).getVerkehrsbeziehung().getVon(), is(2));
        assertThat(result.get(1).getVerkehrsbeziehung().getNach(), is(1));

        assertThat(result.get(2).getPkw(), is(12));
        assertThat(result.get(2).getType(), is(TypeZeitintervall.SPITZENSTUNDE_KFZ));
        assertThat(result.get(2).getStartUhrzeit(), is(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 15))));
        assertThat(result.get(2).getEndeUhrzeit(), is(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 15))));
        assertThat(result.get(2).getVerkehrsbeziehung().getVon(), is(1));
        assertThat(result.get(2).getVerkehrsbeziehung().getNach(), is(nullValue()));

        assertThat(result.get(3).getPkw(), is(16));
        assertThat(result.get(3).getType(), is(TypeZeitintervall.SPITZENSTUNDE_KFZ));
        assertThat(result.get(3).getStartUhrzeit(), is(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(8, 15))));
        assertThat(result.get(3).getEndeUhrzeit(), is(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(9, 15))));
        assertThat(result.get(3).getVerkehrsbeziehung().getVon(), is(2));
        assertThat(result.get(3).getVerkehrsbeziehung().getNach(), is(nullValue()));
    }
}
