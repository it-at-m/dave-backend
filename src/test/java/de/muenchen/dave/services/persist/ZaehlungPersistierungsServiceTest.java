package de.muenchen.dave.services.persist;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.Fahrbeziehung;
import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Fahrzeug;
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
        zaehlung.setId(uuidZaehlung.toString());
        final List<Verkehrsbeziehung> fahrbeziehungen = new ArrayList<>();
        final UUID uuidFahrbeziehung1 = UUID.randomUUID();
        Verkehrsbeziehung verkehrsbeziehung1 = new Verkehrsbeziehung();
        verkehrsbeziehung1.setId(uuidFahrbeziehung1.toString());
        verkehrsbeziehung1.setIsKreuzung(true);
        verkehrsbeziehung1.setVon(1);
        verkehrsbeziehung1.setNach(2);
        fahrbeziehungen.add(verkehrsbeziehung1);
        final UUID uuidFahrbeziehung2 = UUID.randomUUID();
        Verkehrsbeziehung verkehrsbeziehung2 = new Verkehrsbeziehung();
        verkehrsbeziehung2.setId(uuidFahrbeziehung2.toString());
        verkehrsbeziehung2.setIsKreuzung(true);
        verkehrsbeziehung2.setVon(1);
        verkehrsbeziehung2.setNach(5);
        fahrbeziehungen.add(verkehrsbeziehung2);
        zaehlung.setVerkehrsbeziehungen(fahrbeziehungen);

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

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungDto.setIsKreuzung(true);
        fahrbeziehungDto.setVon(1);
        fahrbeziehungDto.setNach(2);
        fahrbeziehungDto.setHochrechnungsfaktor(hochrechnungsfaktorDto);

        Zeitintervall result = internalZaehlungPersistierungsService.setAdditionalDataToZeitintervall(
                zeitintervall,
                zaehlung,
                fahrbeziehungDto);

        final Zeitintervall expected = new Zeitintervall();
        expected.setFahrbeziehungId(uuidFahrbeziehung1);
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

        Fahrbeziehung expectedFahrbeziehung = new Fahrbeziehung();
        expectedFahrbeziehung.setVon(1);
        expectedFahrbeziehung.setNach(2);
        expected.setFahrbeziehung(expectedFahrbeziehung);

        assertThat(result, is(expected));
    }

    @Test
    public void getFromBearbeiteFahrbeziehungDto() {
        final Zaehlung zaehlung = new Zaehlung();
        final List<Verkehrsbeziehung> fahrbeziehungen = new ArrayList<>();
        Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        fahrbeziehungen.add(verkehrsbeziehung);
        verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(5);
        fahrbeziehungen.add(verkehrsbeziehung);
        zaehlung.setVerkehrsbeziehungen(fahrbeziehungen);

        assertThat(zaehlung.getVerkehrsbeziehungen().size(), is(2));

        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungDto.setIsKreuzung(true);
        fahrbeziehungDto.setVon(1);
        fahrbeziehungDto.setNach(2);

        Optional<Verkehrsbeziehung> result = internalZaehlungPersistierungsService.getFromBearbeiteFahrbeziehungDto(zaehlung,
                fahrbeziehungDto);
        verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        Optional<Verkehrsbeziehung> expected = Optional.of(verkehrsbeziehung);
        assertThat(result, is(expected));
    }

    @Test
    public void isSameFahrbeziehung() {
        BearbeiteVerkehrsbeziehungDTO fahrbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        Verkehrsbeziehung verkehrsbeziehung = new Verkehrsbeziehung();
        boolean result = internalZaehlungPersistierungsService.isSameFahrbeziehung(fahrbeziehungDto, verkehrsbeziehung);
        assertThat(result, is(true));

        fahrbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungDto.setIsKreuzung(true);
        verkehrsbeziehung = new Verkehrsbeziehung();
        result = internalZaehlungPersistierungsService.isSameFahrbeziehung(fahrbeziehungDto, verkehrsbeziehung);
        assertThat(result, is(false));

        fahrbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungDto.setIsKreuzung(true);
        fahrbeziehungDto.setVon(1);
        fahrbeziehungDto.setNach(2);
        verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        result = internalZaehlungPersistierungsService.isSameFahrbeziehung(fahrbeziehungDto, verkehrsbeziehung);
        assertThat(result, is(true));

        fahrbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungDto.setIsKreuzung(true);
        fahrbeziehungDto.setVon(1);
        fahrbeziehungDto.setNach(2);
        verkehrsbeziehung = new Verkehrsbeziehung();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        verkehrsbeziehung.setKnotenarm(1); // Unterscheidet sich zu "fahrbeziehungDto"
        result = internalZaehlungPersistierungsService.isSameFahrbeziehung(fahrbeziehungDto, verkehrsbeziehung);
        assertThat(result, is(false));

    }

    @Test
    public void mapToFahrbeziehungForZeitintervallInternal() {
        final BearbeiteVerkehrsbeziehungDTO fahrbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        fahrbeziehungDto.setIsKreuzung(true);
        fahrbeziehungDto.setVon(1);
        fahrbeziehungDto.setNach(2);

        Fahrbeziehung result = internalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        Fahrbeziehung expected = new Fahrbeziehung();
        expected.setVon(1);
        expected.setNach(2);
        assertThat(result, is(expected));

        fahrbeziehungDto.setHeraus(true);
        fahrbeziehungDto.setHinein(true);
        fahrbeziehungDto.setVorbei(true);
        result = internalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
        expected.setVon(1);
        expected.setNach(2);
        assertThat(result, is(expected));

        fahrbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        fahrbeziehungDto.setKnotenarm(5);
        fahrbeziehungDto.setHeraus(true);
        fahrbeziehungDto.setHinein(false);
        fahrbeziehungDto.setVorbei(false);
        result = internalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HERAUS);
        assertThat(result, is(expected));

        fahrbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        fahrbeziehungDto.setKnotenarm(5);
        fahrbeziehungDto.setHeraus(false);
        fahrbeziehungDto.setHinein(true);
        fahrbeziehungDto.setVorbei(false);
        result = internalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HINEIN);
        assertThat(result, is(expected));

        fahrbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        fahrbeziehungDto.setKnotenarm(5);
        fahrbeziehungDto.setHeraus(false);
        fahrbeziehungDto.setHinein(false);
        fahrbeziehungDto.setVorbei(true);
        result = internalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.VORBEI);
        assertThat(result, is(expected));

        fahrbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        fahrbeziehungDto.setKnotenarm(null);
        fahrbeziehungDto.setHeraus(false);
        fahrbeziehungDto.setHinein(false);
        fahrbeziehungDto.setVorbei(true);
        result = internalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.VORBEI);
        assertThat(result, is(expected));

        fahrbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        fahrbeziehungDto.setKnotenarm(null);
        fahrbeziehungDto.setHeraus(false);
        fahrbeziehungDto.setHinein(false);
        fahrbeziehungDto.setVorbei(false);
        result = internalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
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

    // External
    @Test
    public void setAdditionalDataToZeitintervallExternal() {
        final UUID uuidZaehlung = UUID.randomUUID();
        final Zaehlung zaehlung = new Zaehlung();
        zaehlung.setId(uuidZaehlung.toString());
        zaehlung.setPkwEinheit(new de.muenchen.dave.domain.elasticsearch.PkwEinheit());
        final List<Verkehrsbeziehung> fahrbeziehungen = new ArrayList<>();
        final UUID uuidFahrbeziehung1 = UUID.randomUUID();
        Verkehrsbeziehung verkehrsbeziehung1 = new Verkehrsbeziehung();
        verkehrsbeziehung1.setId(uuidFahrbeziehung1.toString());
        verkehrsbeziehung1.setIsKreuzung(true);
        verkehrsbeziehung1.setVon(1);
        verkehrsbeziehung1.setNach(2);
        fahrbeziehungen.add(verkehrsbeziehung1);
        final UUID uuidFahrbeziehung2 = UUID.randomUUID();
        Verkehrsbeziehung verkehrsbeziehung2 = new Verkehrsbeziehung();
        verkehrsbeziehung2.setId(uuidFahrbeziehung2.toString());
        verkehrsbeziehung2.setIsKreuzung(true);
        verkehrsbeziehung2.setVon(1);
        verkehrsbeziehung2.setNach(5);
        fahrbeziehungen.add(verkehrsbeziehung2);
        zaehlung.setVerkehrsbeziehungen(fahrbeziehungen);

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

        final ExternalVerkehrsbeziehungDTO fahrbeziehungDto = new ExternalVerkehrsbeziehungDTO();
        fahrbeziehungDto.setId(verkehrsbeziehung1.getId());
        fahrbeziehungDto.setIsKreuzung(true);
        fahrbeziehungDto.setVon(1);
        fahrbeziehungDto.setNach(2);
        fahrbeziehungDto.setHochrechnungsfaktor(hochrechnungsfaktorDto);

        Mockito.when(pkwEinheitMapper.elastic2Entity(zaehlung.getPkwEinheit())).thenReturn(pkwEinheit);

        Zeitintervall result = externalZaehlungPersistierungsService.setAdditionalDataToZeitintervall(
                zeitintervall,
                zaehlung,
                fahrbeziehungDto);

        final Zeitintervall expected = new Zeitintervall();
        expected.setFahrbeziehungId(uuidFahrbeziehung1);
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

        Fahrbeziehung expectedFahrbeziehung = new Fahrbeziehung();
        expectedFahrbeziehung.setVon(1);
        expectedFahrbeziehung.setNach(2);
        expected.setFahrbeziehung(expectedFahrbeziehung);

        assertThat(result, is(expected));
    }

    @Test
    public void mapToFahrbeziehungForZeitintervallExternal() {
        final ExternalVerkehrsbeziehungDTO fahrbeziehungDto = new ExternalVerkehrsbeziehungDTO();
        fahrbeziehungDto.setIsKreuzung(true);
        fahrbeziehungDto.setVon(1);
        fahrbeziehungDto.setNach(2);

        Fahrbeziehung result = externalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        Fahrbeziehung expected = new Fahrbeziehung();
        expected.setVon(1);
        expected.setNach(2);
        assertThat(result, is(expected));

        fahrbeziehungDto.setHeraus(true);
        fahrbeziehungDto.setHinein(true);
        fahrbeziehungDto.setVorbei(true);
        result = externalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
        expected.setVon(1);
        expected.setNach(2);
        assertThat(result, is(expected));

        fahrbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        fahrbeziehungDto.setKnotenarm(5);
        fahrbeziehungDto.setHeraus(true);
        fahrbeziehungDto.setHinein(false);
        fahrbeziehungDto.setVorbei(false);
        result = externalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HERAUS);
        assertThat(result, is(expected));

        fahrbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        fahrbeziehungDto.setKnotenarm(5);
        fahrbeziehungDto.setHeraus(false);
        fahrbeziehungDto.setHinein(true);
        fahrbeziehungDto.setVorbei(false);
        result = externalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.HINEIN);
        assertThat(result, is(expected));

        fahrbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        fahrbeziehungDto.setKnotenarm(5);
        fahrbeziehungDto.setHeraus(false);
        fahrbeziehungDto.setHinein(false);
        fahrbeziehungDto.setVorbei(true);
        result = externalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
        expected.setVon(5);
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.VORBEI);
        assertThat(result, is(expected));

        fahrbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        fahrbeziehungDto.setKnotenarm(null);
        fahrbeziehungDto.setHeraus(false);
        fahrbeziehungDto.setHinein(false);
        fahrbeziehungDto.setVorbei(true);
        result = externalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
        expected.setFahrbewegungKreisverkehr(FahrbewegungKreisverkehr.VORBEI);
        assertThat(result, is(expected));

        fahrbeziehungDto.setIsKreuzung(false);
        expected.setVon(null);
        expected.setNach(null);
        fahrbeziehungDto.setKnotenarm(null);
        fahrbeziehungDto.setHeraus(false);
        fahrbeziehungDto.setHinein(false);
        fahrbeziehungDto.setVorbei(false);
        result = externalZaehlungPersistierungsService.mapToFahrbeziehungForZeitintervall(fahrbeziehungDto);
        expected = new Fahrbeziehung();
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
}
