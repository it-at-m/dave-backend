package de.muenchen.dave.services.persist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.external.ExternalLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlungDTO;
import de.muenchen.dave.domain.elasticsearch.Laengsverkehr;
import de.muenchen.dave.domain.elasticsearch.Querungsverkehr;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.mapper.PkwEinheitMapper;
import de.muenchen.dave.repositories.relationaldb.PkwEinheitRepository;
import de.muenchen.dave.util.DaveConstants;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

class ZaehlungPersistierungsServiceTest {

    private final InternalZaehlungPersistierungsService internalZaehlungPersistierungsService;
    private final ExternalZaehlungPersistierungsService externalZaehlungPersistierungsService;

    private final PkwEinheitRepository pkwEinheitRepository;
    private final PkwEinheitMapper pkwEinheitMapper;

    public ZaehlungPersistierungsServiceTest() {
        pkwEinheitRepository = Mockito.mock(PkwEinheitRepository.class);
        pkwEinheitMapper = Mockito.mock(PkwEinheitMapper.class);
        internalZaehlungPersistierungsService = new InternalZaehlungPersistierungsService(
                null,
                null,
                pkwEinheitRepository,
                null,
                null);
        externalZaehlungPersistierungsService = new ExternalZaehlungPersistierungsService(
                null,
                null,
                null,
                null);
    }

    // Internal
    @Test
    public void setAdditionalDataToZeitintervallInternal() {
        final UUID uuidZaehlung = UUID.randomUUID();
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.N.name());
        zaehlung.setId(uuidZaehlung.toString());
        final List<de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung> verkehrsbeziehungen = new ArrayList<>();
        final UUID uuidVerkehrsbeziehung1 = UUID.randomUUID();
        de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung verkehrsbeziehung1 = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
        verkehrsbeziehung1.setId(uuidVerkehrsbeziehung1.toString());
        verkehrsbeziehung1.setIsKreuzung(true);
        verkehrsbeziehung1.setVon(1);
        verkehrsbeziehung1.setNach(2);
        verkehrsbeziehungen.add(verkehrsbeziehung1);
        final UUID uuidVerkehrsbeziehung2 = UUID.randomUUID();
        de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung verkehrsbeziehung2 = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
        verkehrsbeziehung2.setId(uuidVerkehrsbeziehung2.toString());
        verkehrsbeziehung2.setIsKreuzung(true);
        verkehrsbeziehung2.setVon(1);
        verkehrsbeziehung2.setNach(5);
        verkehrsbeziehungen.add(verkehrsbeziehung2);
        zaehlung.setVerkehrsbeziehungen(verkehrsbeziehungen);

        final PkwEinheit pkwEinheit = new PkwEinheit();
        pkwEinheit.setPkw(BigDecimal.valueOf(1));
        pkwEinheit.setLkw(BigDecimal.valueOf(2));
        pkwEinheit.setLastzuege(BigDecimal.valueOf(3));
        pkwEinheit.setBusse(BigDecimal.valueOf(4));
        pkwEinheit.setKraftraeder(BigDecimal.valueOf(5));
        pkwEinheit.setFahrradfahrer(BigDecimal.valueOf(6));

        Mockito.when(pkwEinheitRepository.findTopByOrderByCreatedTimeDesc()).thenReturn(Optional.of(pkwEinheit));

        final Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setPkw(1);
        zeitintervall.setLkw(2);
        zeitintervall.setLastzuege(3);
        zeitintervall.setBusse(4);
        zeitintervall.setKraftraeder(5);
        zeitintervall.setFahrradfahrer(6);
        zeitintervall.setFussgaenger(7);
        final HochrechnungsfaktorDTO hochrechnungsfaktorDto = new HochrechnungsfaktorDTO();
        hochrechnungsfaktorDto.setKfz(2.0);
        hochrechnungsfaktorDto.setSv(3.0);
        hochrechnungsfaktorDto.setGv(4.0);

        final BearbeiteVerkehrsbeziehungDTO verkehrsbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        verkehrsbeziehungDto.setIsKreuzung(true);
        verkehrsbeziehungDto.setVon(1);
        verkehrsbeziehungDto.setNach(2);
        verkehrsbeziehungDto.setHochrechnungsfaktor(hochrechnungsfaktorDto);

        Zeitintervall result = internalZaehlungPersistierungsService.setAdditionalDataToZeitintervall(
                zeitintervall,
                zaehlung,
                verkehrsbeziehungDto);

        final Zeitintervall expected = new Zeitintervall();
        expected.setBewegungsbeziehungId(uuidVerkehrsbeziehung1);
        expected.setZaehlungId(uuidZaehlung);
        expected.setPkw(1);
        expected.setLkw(2);
        expected.setLastzuege(3);
        expected.setBusse(4);
        expected.setKraftraeder(5);
        expected.setFahrradfahrer(6);
        expected.setFussgaenger(7);

        final Hochrechnung expectedHochrechnung = new Hochrechnung();
        expectedHochrechnung.setFaktorKfz(BigDecimal.valueOf(2.0));
        expectedHochrechnung.setFaktorSv(BigDecimal.valueOf(3.0));
        expectedHochrechnung.setFaktorGv(BigDecimal.valueOf(4.0));
        expectedHochrechnung.setHochrechnungKfz(BigDecimal.valueOf(30.0));
        expectedHochrechnung.setHochrechnungSv(BigDecimal.valueOf(27.0));
        expectedHochrechnung.setHochrechnungGv(BigDecimal.valueOf(20.0));
        expected.setHochrechnung(expectedHochrechnung);

        Verkehrsbeziehung expectedVerkehrsbeziehung = new Verkehrsbeziehung();
        expectedVerkehrsbeziehung.setVon(1);
        expectedVerkehrsbeziehung.setNach(2);
        expected.setVerkehrsbeziehung(expectedVerkehrsbeziehung);

        assertThat(result, is(expected));
    }

    @Test
    public void getFromBearbeiteVerkehrsbeziehungDto() {
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setZaehlart(Zaehlart.N.name());
        final List<de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung> verkehrsbeziehungen = new ArrayList<>();
        de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung verkehrsbeziehung = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        verkehrsbeziehungen.add(verkehrsbeziehung);
        verkehrsbeziehung = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(5);
        verkehrsbeziehungen.add(verkehrsbeziehung);
        zaehlung.setVerkehrsbeziehungen(verkehrsbeziehungen);

        assertThat(zaehlung.getVerkehrsbeziehungen().size(), is(2));

        final BearbeiteVerkehrsbeziehungDTO verkehrsbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        verkehrsbeziehungDto.setIsKreuzung(true);
        verkehrsbeziehungDto.setVon(1);
        verkehrsbeziehungDto.setNach(2);

        Optional<de.muenchen.dave.domain.elasticsearch.Bewegungsbeziehung> result = internalZaehlungPersistierungsService
                .getBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto(
                        zaehlung,
                        verkehrsbeziehungDto);
        verkehrsbeziehung = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        Optional<de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung> expected = Optional.of(verkehrsbeziehung);
        assertThat(result, is(expected));
    }

    @Test
    public void isSameVerkehrsbeziehung() {
        BearbeiteVerkehrsbeziehungDTO verkehrsbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung verkehrsbeziehung = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
        boolean result = internalZaehlungPersistierungsService.isSameBewegungsbeziehung(Zaehlart.N, verkehrsbeziehungDto, verkehrsbeziehung);
        assertThat(result, is(true));

        verkehrsbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        verkehrsbeziehungDto.setIsKreuzung(true);
        verkehrsbeziehung = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
        result = internalZaehlungPersistierungsService.isSameBewegungsbeziehung(Zaehlart.N, verkehrsbeziehungDto, verkehrsbeziehung);
        assertThat(result, is(false));

        verkehrsbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        verkehrsbeziehungDto.setIsKreuzung(true);
        verkehrsbeziehungDto.setVon(1);
        verkehrsbeziehungDto.setNach(2);
        verkehrsbeziehung = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        result = internalZaehlungPersistierungsService.isSameBewegungsbeziehung(Zaehlart.N, verkehrsbeziehungDto, verkehrsbeziehung);
        assertThat(result, is(true));

        verkehrsbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        verkehrsbeziehungDto.setIsKreuzung(true);
        verkehrsbeziehungDto.setVon(1);
        verkehrsbeziehungDto.setNach(2);
        verkehrsbeziehung = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        verkehrsbeziehung.setKnotenarm(1); // Unterscheidet sich zu "verkehrsbeziehungDto"
        result = internalZaehlungPersistierungsService.isSameBewegungsbeziehung(Zaehlart.N, verkehrsbeziehungDto, verkehrsbeziehung);
        assertThat(result, is(false));

    }

    @Test
    public void mapToVerkehrsbeziehungForZeitintervall() {
        final BearbeiteVerkehrsbeziehungDTO verkehrsbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        verkehrsbeziehungDto.setIsKreuzung(true);
        verkehrsbeziehungDto.setVon(1);
        verkehrsbeziehungDto.setNach(2);

        Verkehrsbeziehung result = internalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        Verkehrsbeziehung expected = new Verkehrsbeziehung();
        expected.setVon(1);
        expected.setNach(2);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setHeraus(true);
        verkehrsbeziehungDto.setHinein(true);
        verkehrsbeziehungDto.setVorbei(true);
        result = internalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        expected.setVon(1);
        expected.setNach(2);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        verkehrsbeziehungDto.setKnotenarm(5);
        verkehrsbeziehungDto.setHeraus(true);
        verkehrsbeziehungDto.setHinein(false);
        verkehrsbeziehungDto.setVorbei(false);
        result = internalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HERAUS);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        verkehrsbeziehungDto.setKnotenarm(5);
        verkehrsbeziehungDto.setHeraus(false);
        verkehrsbeziehungDto.setHinein(true);
        verkehrsbeziehungDto.setVorbei(false);
        result = internalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HINEIN);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        verkehrsbeziehungDto.setKnotenarm(5);
        verkehrsbeziehungDto.setHeraus(false);
        verkehrsbeziehungDto.setHinein(false);
        verkehrsbeziehungDto.setVorbei(true);
        result = internalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.VORBEI);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        verkehrsbeziehungDto.setKnotenarm(null);
        verkehrsbeziehungDto.setHeraus(false);
        verkehrsbeziehungDto.setHinein(false);
        verkehrsbeziehungDto.setVorbei(true);
        result = internalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.VORBEI);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        verkehrsbeziehungDto.setKnotenarm(null);
        verkehrsbeziehungDto.setHeraus(false);
        verkehrsbeziehungDto.setHinein(false);
        verkehrsbeziehungDto.setVorbei(false);
        result = internalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        assertThat(result, is(expected));
    }

    @Test
    public void getKoordinateZaehlstelleWhenZaehlungWithinDistance() {
        final Zaehlstelle zaehlstelle = new Zaehlstelle();
        zaehlstelle.setPunkt(new GeoPoint(48.133453766001075, 11.54928191096542));
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setPunkt(new GeoPoint(48.13251574474983, 11.549094156334377));

        // Zaehlung ist innerhalb des Radius von 106 Meter um Zaehlstelle.
        GeoPoint result = internalZaehlungPersistierungsService.getKoordinateZaehlstelleWhenZaehlungWithinDistance(
                106,
                zaehlstelle,
                zaehlung);
        assertThat(result, is(zaehlstelle.getPunkt()));

        // Zaehlung ist ausserhalb des Radius von 105 Meter um Zaehlstelle.
        result = internalZaehlungPersistierungsService.getKoordinateZaehlstelleWhenZaehlungWithinDistance(
                105,
                zaehlstelle,
                zaehlung);
        assertThat(result, is(zaehlung.getPunkt()));
    }

    @Test
    public void mapToVerkehrsbeziehungForZeitintervallExternal() {
        final ExternalVerkehrsbeziehungDTO verkehrsbeziehungDto = new ExternalVerkehrsbeziehungDTO();
        verkehrsbeziehungDto.setIsKreuzung(true);
        verkehrsbeziehungDto.setVon(1);
        verkehrsbeziehungDto.setNach(2);

        Verkehrsbeziehung result = externalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        Verkehrsbeziehung expected = new Verkehrsbeziehung();
        expected.setVon(1);
        expected.setNach(2);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setHeraus(true);
        verkehrsbeziehungDto.setHinein(true);
        verkehrsbeziehungDto.setVorbei(true);
        result = externalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        expected.setVon(1);
        expected.setNach(2);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        verkehrsbeziehungDto.setKnotenarm(5);
        verkehrsbeziehungDto.setHeraus(true);
        verkehrsbeziehungDto.setHinein(false);
        verkehrsbeziehungDto.setVorbei(false);
        result = externalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HERAUS);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        verkehrsbeziehungDto.setKnotenarm(5);
        verkehrsbeziehungDto.setHeraus(false);
        verkehrsbeziehungDto.setHinein(true);
        verkehrsbeziehungDto.setVorbei(false);
        result = externalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HINEIN);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        verkehrsbeziehungDto.setKnotenarm(5);
        verkehrsbeziehungDto.setHeraus(false);
        verkehrsbeziehungDto.setHinein(false);
        verkehrsbeziehungDto.setVorbei(true);
        result = externalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.VORBEI);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        verkehrsbeziehungDto.setKnotenarm(null);
        verkehrsbeziehungDto.setHeraus(false);
        verkehrsbeziehungDto.setHinein(false);
        verkehrsbeziehungDto.setVorbei(true);
        result = externalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.VORBEI);
        assertThat(result, is(expected));

        verkehrsbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        verkehrsbeziehungDto.setKnotenarm(null);
        verkehrsbeziehungDto.setHeraus(false);
        verkehrsbeziehungDto.setHinein(false);
        verkehrsbeziehungDto.setVorbei(false);
        result = externalZaehlungPersistierungsService.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, verkehrsbeziehungDto);
        expected = new Verkehrsbeziehung();
        assertThat(result, is(expected));
    }

    // Both
    @Test
    public void createHochrechnung() {
        final Zeitintervall zeitintervall = new Zeitintervall();
        zeitintervall.setPkw(1);
        zeitintervall.setLkw(2);
        zeitintervall.setLastzuege(3);
        zeitintervall.setBusse(4);
        zeitintervall.setKraftraeder(5);
        zeitintervall.setFahrradfahrer(6);
        zeitintervall.setFussgaenger(7);
        final HochrechnungsfaktorDTO hochrechnungsfaktorDto = new HochrechnungsfaktorDTO();
        hochrechnungsfaktorDto.setKfz(2.0);
        hochrechnungsfaktorDto.setSv(3.0);
        hochrechnungsfaktorDto.setGv(4.0);

        Hochrechnung result = internalZaehlungPersistierungsService.createHochrechnung(zeitintervall, hochrechnungsfaktorDto,
                Zaehldauer.DAUER_24_STUNDEN.toString());
        Hochrechnung expected = new Hochrechnung();
        expected.setFaktorKfz(BigDecimal.valueOf(2.0));
        expected.setFaktorSv(BigDecimal.valueOf(3.0));
        expected.setFaktorGv(BigDecimal.valueOf(4.0));
        expected.setHochrechnungKfz(BigDecimal.valueOf(30.0));
        expected.setHochrechnungSv(BigDecimal.valueOf(27.0));
        expected.setHochrechnungGv(BigDecimal.valueOf(20.0));
        assertThat(result, is(expected));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(14, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 0)));
        result = internalZaehlungPersistierungsService.createHochrechnung(zeitintervall, hochrechnungsfaktorDto, Zaehldauer.DAUER_16_STUNDEN.toString());
        expected = new Hochrechnung();
        expected.setFaktorKfz(BigDecimal.ZERO);
        expected.setFaktorSv(BigDecimal.ZERO);
        expected.setFaktorGv(BigDecimal.ZERO);
        expected.setHochrechnungKfz(BigDecimal.valueOf(0));
        expected.setHochrechnungSv(BigDecimal.valueOf(0));
        expected.setHochrechnungGv(BigDecimal.valueOf(0));
        assertThat(result, is(expected));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(14, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(15, 0)));
        result = internalZaehlungPersistierungsService.createHochrechnung(zeitintervall, hochrechnungsfaktorDto, Zaehldauer.DAUER_13_STUNDEN.toString());
        expected = new Hochrechnung();
        expected.setFaktorKfz(BigDecimal.ZERO);
        expected.setFaktorSv(BigDecimal.ZERO);
        expected.setFaktorGv(BigDecimal.ZERO);
        expected.setHochrechnungKfz(BigDecimal.valueOf(0));
        expected.setHochrechnungSv(BigDecimal.valueOf(0));
        expected.setHochrechnungGv(BigDecimal.valueOf(0));
        assertThat(result, is(expected));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 15)));
        result = internalZaehlungPersistierungsService.createHochrechnung(zeitintervall, hochrechnungsfaktorDto, Zaehldauer.DAUER_16_STUNDEN.toString());
        expected = new Hochrechnung();
        expected.setFaktorKfz(BigDecimal.ZERO);
        expected.setFaktorSv(BigDecimal.ZERO);
        expected.setFaktorGv(BigDecimal.ZERO);
        expected.setHochrechnungKfz(BigDecimal.valueOf(0));
        expected.setHochrechnungSv(BigDecimal.valueOf(0));
        expected.setHochrechnungGv(BigDecimal.valueOf(0));
        assertThat(result, is(expected));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 15)));
        result = internalZaehlungPersistierungsService.createHochrechnung(zeitintervall, hochrechnungsfaktorDto, Zaehldauer.DAUER_13_STUNDEN.toString());
        expected = new Hochrechnung();
        expected.setFaktorKfz(BigDecimal.ZERO);
        expected.setFaktorSv(BigDecimal.ZERO);
        expected.setFaktorGv(BigDecimal.ZERO);
        expected.setHochrechnungKfz(BigDecimal.valueOf(0));
        expected.setHochrechnungSv(BigDecimal.valueOf(0));
        expected.setHochrechnungGv(BigDecimal.valueOf(0));
        assertThat(result, is(expected));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 15)));
        result = internalZaehlungPersistierungsService.createHochrechnung(zeitintervall, hochrechnungsfaktorDto, Zaehldauer.DAUER_16_STUNDEN.toString());
        expected = new Hochrechnung();
        expected.setFaktorKfz(BigDecimal.valueOf(2.0));
        expected.setFaktorSv(BigDecimal.valueOf(3.0));
        expected.setFaktorGv(BigDecimal.valueOf(4.0));
        expected.setHochrechnungKfz(BigDecimal.valueOf(30.0));
        expected.setHochrechnungSv(BigDecimal.valueOf(27.0));
        expected.setHochrechnungGv(BigDecimal.valueOf(20.0));
        assertThat(result, is(expected));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 0)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(6, 15)));
        result = internalZaehlungPersistierungsService.createHochrechnung(zeitintervall, hochrechnungsfaktorDto, Zaehldauer.DAUER_13_STUNDEN.toString());
        expected = new Hochrechnung();
        expected.setFaktorKfz(BigDecimal.valueOf(2.0));
        expected.setFaktorSv(BigDecimal.valueOf(3.0));
        expected.setFaktorGv(BigDecimal.valueOf(4.0));
        expected.setHochrechnungKfz(BigDecimal.valueOf(30.0));
        expected.setHochrechnungSv(BigDecimal.valueOf(27.0));
        expected.setHochrechnungGv(BigDecimal.valueOf(20.0));
        assertThat(result, is(expected));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(18, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)));
        result = internalZaehlungPersistierungsService.createHochrechnung(zeitintervall, hochrechnungsfaktorDto, Zaehldauer.DAUER_16_STUNDEN.toString());
        expected = new Hochrechnung();
        expected.setFaktorKfz(BigDecimal.valueOf(2.0));
        expected.setFaktorSv(BigDecimal.valueOf(3.0));
        expected.setFaktorGv(BigDecimal.valueOf(4.0));
        expected.setHochrechnungKfz(BigDecimal.valueOf(30.0));
        expected.setHochrechnungSv(BigDecimal.valueOf(27.0));
        expected.setHochrechnungGv(BigDecimal.valueOf(20.0));
        assertThat(result, is(expected));

        zeitintervall.setStartUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(18, 45)));
        zeitintervall.setEndeUhrzeit(LocalDateTime.of(DaveConstants.DEFAULT_LOCALDATE, LocalTime.of(19, 0)));
        result = internalZaehlungPersistierungsService.createHochrechnung(zeitintervall, hochrechnungsfaktorDto, Zaehldauer.DAUER_13_STUNDEN.toString());
        expected = new Hochrechnung();
        expected.setFaktorKfz(BigDecimal.valueOf(2.0));
        expected.setFaktorSv(BigDecimal.valueOf(3.0));
        expected.setFaktorGv(BigDecimal.valueOf(4.0));
        expected.setHochrechnungKfz(BigDecimal.valueOf(30.0));
        expected.setHochrechnungSv(BigDecimal.valueOf(27.0));
        expected.setHochrechnungGv(BigDecimal.valueOf(20.0));
        assertThat(result, is(expected));
    }

    @Test
    public void getFahrzeugKategorienAndFahrzeugklassen() {
        final Zeitintervall zeitintervall = new Zeitintervall();

        List<Fahrzeug> result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        List<Fahrzeug> expected = new ArrayList<>();
        assertThat(result, is(expected));

        zeitintervall.setPkw(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = Arrays.asList(Fahrzeug.KFZ, Fahrzeug.PKW, Fahrzeug.PKW_EINHEIT);
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = Arrays.asList(Fahrzeug.KFZ, Fahrzeug.PKW, Fahrzeug.LKW, Fahrzeug.GV, Fahrzeug.SV, Fahrzeug.GV_P, Fahrzeug.SV_P, Fahrzeug.PKW_EINHEIT);
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = Arrays.asList(Fahrzeug.KFZ, Fahrzeug.PKW, Fahrzeug.LKW, Fahrzeug.GV, Fahrzeug.SV, Fahrzeug.LZ, Fahrzeug.GV_P, Fahrzeug.SV_P,
                Fahrzeug.PKW_EINHEIT);
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        zeitintervall.setBusse(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = Arrays.asList(Fahrzeug.KFZ, Fahrzeug.PKW, Fahrzeug.LKW, Fahrzeug.GV, Fahrzeug.SV, Fahrzeug.LZ, Fahrzeug.BUS, Fahrzeug.GV_P, Fahrzeug.SV_P,
                Fahrzeug.PKW_EINHEIT);
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        zeitintervall.setBusse(1);
        zeitintervall.setKraftraeder(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = Arrays.asList(Fahrzeug.KFZ, Fahrzeug.PKW, Fahrzeug.LKW, Fahrzeug.GV, Fahrzeug.SV, Fahrzeug.LZ, Fahrzeug.BUS, Fahrzeug.KRAD, Fahrzeug.GV_P,
                Fahrzeug.SV_P, Fahrzeug.PKW_EINHEIT);
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        zeitintervall.setBusse(1);
        zeitintervall.setKraftraeder(1);
        zeitintervall.setFahrradfahrer(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = Arrays.asList(Fahrzeug.KFZ, Fahrzeug.PKW, Fahrzeug.LKW, Fahrzeug.GV, Fahrzeug.SV, Fahrzeug.LZ, Fahrzeug.BUS, Fahrzeug.KRAD, Fahrzeug.RAD,
                Fahrzeug.GV_P, Fahrzeug.SV_P, Fahrzeug.PKW_EINHEIT);
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        zeitintervall.setBusse(1);
        zeitintervall.setKraftraeder(1);
        zeitintervall.setFahrradfahrer(1);
        zeitintervall.setFussgaenger(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = Arrays.asList(Fahrzeug.KFZ, Fahrzeug.PKW, Fahrzeug.LKW, Fahrzeug.GV, Fahrzeug.SV, Fahrzeug.LZ, Fahrzeug.BUS, Fahrzeug.KRAD, Fahrzeug.RAD,
                Fahrzeug.FUSS, Fahrzeug.GV_P, Fahrzeug.SV_P, Fahrzeug.PKW_EINHEIT);
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));
    }

    @Test
    public void getAllBewegungsbeziehungenFromExternalZaehlung() {
        // Fall: Alle Listen sind null -> Ergebnis ist leer
        final ExternalZaehlungDTO externalZaehlungDTO = new ExternalZaehlungDTO();
        externalZaehlungDTO.setLaengsverkehr(null);
        externalZaehlungDTO.setQuerungsverkehr(null);
        externalZaehlungDTO.setVerkehrsbeziehungen(null);

        List<de.muenchen.dave.domain.dtos.external.ExternalBewegungsbeziehungDTO> result = externalZaehlungPersistierungsService
                .getAllBewegungsbeziehungenFromZaehlung(externalZaehlungDTO);
        List<de.muenchen.dave.domain.dtos.external.ExternalBewegungsbeziehungDTO> expected = new ArrayList<>();
        assertThat(result, is(expected));

        // Fall: Gemischte Listen, eine Liste enthält ein null-Element -> nulls werden gefiltert
        final ExternalLaengsverkehrDTO l1 = new ExternalLaengsverkehrDTO();
        l1.setId("v1");
        final ExternalQuerungsverkehrDTO q2 = new ExternalQuerungsverkehrDTO();
        q2.setId("v2");
        externalZaehlungDTO.setLaengsverkehr(Arrays.asList(l1, null));
        externalZaehlungDTO.setQuerungsverkehr(Collections.singletonList(q2));
        externalZaehlungDTO.setVerkehrsbeziehungen(Collections.emptyList());

        result = externalZaehlungPersistierungsService.getAllBewegungsbeziehungenFromZaehlung(externalZaehlungDTO);
        expected = Arrays.asList(l1, q2);
        assertThat(result, is(expected));
    }

    @Test
    public void getAllBewegungsbeziehungenFromBearbeiteZaehlung() {
        // Fall: Alle Listen sind null -> Ergebnis ist leer
        final BearbeiteZaehlungDTO bearbeiteZaehlungDTO = new BearbeiteZaehlungDTO();
        bearbeiteZaehlungDTO.setLaengsverkehr(null);
        bearbeiteZaehlungDTO.setQuerungsverkehr(null);
        bearbeiteZaehlungDTO.setVerkehrsbeziehungen(null);

        List<de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteBewegungsbeziehungDTO> result = internalZaehlungPersistierungsService
                .getAllBewegungsbeziehungenFromZaehlung(bearbeiteZaehlungDTO);
        List<de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteBewegungsbeziehungDTO> expected = new ArrayList<>();
        assertThat(result, is(expected));

        // Fall: Gemischte Listen mit unterschiedlichen Elementen und null -> nur nicht-null Elemente zurückgeben
        final BearbeiteLaengsverkehrDTO l1 = new BearbeiteLaengsverkehrDTO();
        l1.setId("bv1");
        final BearbeiteQuerungsverkehrDTO q2 = new BearbeiteQuerungsverkehrDTO();
        q2.setId("bv2");
        bearbeiteZaehlungDTO.setLaengsverkehr(Arrays.asList(l1, null));
        bearbeiteZaehlungDTO.setQuerungsverkehr(Collections.singletonList(q2));
        bearbeiteZaehlungDTO.setVerkehrsbeziehungen(Collections.emptyList());

        result = internalZaehlungPersistierungsService.getAllBewegungsbeziehungenFromZaehlung(bearbeiteZaehlungDTO);
        expected = Arrays.asList(l1, q2);
        assertThat(result, is(expected));
    }

    @Test
    public void getAllBewegungsbeziehungenFromZaehlung() {
        // Fall: Alle Listen sind null -> Ergebnis ist leer
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setLaengsverkehr(null);
        zaehlung.setQuerungsverkehr(null);
        zaehlung.setVerkehrsbeziehungen(null);

        List<de.muenchen.dave.domain.elasticsearch.Bewegungsbeziehung> result = internalZaehlungPersistierungsService
                .getAllBewegungsbeziehungenFromZaehlung(zaehlung);
        List<de.muenchen.dave.domain.elasticsearch.Bewegungsbeziehung> expected = new ArrayList<>();
        assertThat(result, is(expected));

        // Fall: Gemischte Listen mit konkreten Bewegungsbeziehungen (Laengsverkehr, Querungsverkehr, Verkehrsbeziehung)
        final Laengsverkehr l = new Laengsverkehr();
        l.setKnotenarm(1);
        final Querungsverkehr q = new Querungsverkehr();
        q.setKnotenarm(2);
        final de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung vb = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
        vb.setVon(3);
        vb.setNach(4);

        zaehlung.setLaengsverkehr(Arrays.asList(l, null));
        zaehlung.setQuerungsverkehr(Collections.singletonList(q));
        zaehlung.setVerkehrsbeziehungen(Collections.singletonList(vb));

        result = internalZaehlungPersistierungsService.getAllBewegungsbeziehungenFromZaehlung(zaehlung);
        expected = Arrays.asList(l, q, vb);
        assertThat(result, is(expected));
    }
}
