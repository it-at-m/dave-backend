package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.UpdateStatusDTO;
import de.muenchen.dave.domain.dtos.external.ExternalLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlungDTO;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.mapper.PkwEinheitMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.exceptions.PlausibilityException;
import de.muenchen.dave.relational.repository.PkwEinheitRepository;
import de.muenchen.dave.relational.zaehlstellen.KnotenarmEntity;
import de.muenchen.dave.relational.zaehlstellen.LaengsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.MappedBewegungsbeziehung;
import de.muenchen.dave.relational.zaehlstellen.QuerungsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.VerkehrsbeziehungEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.security.SecurityContextInformationExtractorService;
import de.muenchen.dave.services.SanitizationService;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.security.AuthorizationService;
import de.muenchen.dave.util.DaveConstants;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
                null,
                null,
                null);
        externalZaehlungPersistierungsService = new ExternalZaehlungPersistierungsService(
                null,
                null,
                null,
                null,
                null,
                null);
    }

    // Internal
    @Test
    public void setAdditionalDataToZeitintervallInternal() {
        final UUID uuidZaehlung = UUID.randomUUID();
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehlart(Zaehlart.N);
        zaehlung.setId(uuidZaehlung);
        final List<VerkehrsbeziehungEntity> verkehrsbeziehungen = new ArrayList<>();
        final UUID uuidVerkehrsbeziehung1 = UUID.randomUUID();
        VerkehrsbeziehungEntity verkehrsbeziehung1 = new VerkehrsbeziehungEntity();
        verkehrsbeziehung1.setId(uuidVerkehrsbeziehung1);
        verkehrsbeziehung1.setIsKreuzung(true);
        verkehrsbeziehung1.setVon(1);
        verkehrsbeziehung1.setNach(2);
        verkehrsbeziehungen.add(verkehrsbeziehung1);
        final UUID uuidVerkehrsbeziehung2 = UUID.randomUUID();
        VerkehrsbeziehungEntity verkehrsbeziehung2 = new VerkehrsbeziehungEntity();
        verkehrsbeziehung2.setId(uuidVerkehrsbeziehung2);
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
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehlart(Zaehlart.N);
        final List<VerkehrsbeziehungEntity> verkehrsbeziehungen = new ArrayList<>();
        VerkehrsbeziehungEntity verkehrsbeziehung = new VerkehrsbeziehungEntity();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        verkehrsbeziehungen.add(verkehrsbeziehung);
        verkehrsbeziehung = new VerkehrsbeziehungEntity();
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

        Optional<? extends MappedBewegungsbeziehung> result = internalZaehlungPersistierungsService
                .getBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto(
                        zaehlung,
                        verkehrsbeziehungDto);
        verkehrsbeziehung = new VerkehrsbeziehungEntity();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        Optional<VerkehrsbeziehungEntity> expected = Optional.of(verkehrsbeziehung);
        assertThat(result, is(expected));
    }

    @Test
    public void isSameVerkehrsbeziehung() {
        BearbeiteVerkehrsbeziehungDTO verkehrsbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        VerkehrsbeziehungEntity verkehrsbeziehung = new VerkehrsbeziehungEntity();
        boolean result = internalZaehlungPersistierungsService.isSameBewegungsbeziehung(Zaehlart.N, verkehrsbeziehungDto, verkehrsbeziehung);
        assertThat(result, is(true));

        verkehrsbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        verkehrsbeziehungDto.setIsKreuzung(true);
        verkehrsbeziehung = new VerkehrsbeziehungEntity();
        result = internalZaehlungPersistierungsService.isSameBewegungsbeziehung(Zaehlart.N, verkehrsbeziehungDto, verkehrsbeziehung);
        assertThat(result, is(false));

        verkehrsbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        verkehrsbeziehungDto.setIsKreuzung(true);
        verkehrsbeziehungDto.setVon(1);
        verkehrsbeziehungDto.setNach(2);
        verkehrsbeziehung = new VerkehrsbeziehungEntity();
        verkehrsbeziehung.setIsKreuzung(true);
        verkehrsbeziehung.setVon(1);
        verkehrsbeziehung.setNach(2);
        result = internalZaehlungPersistierungsService.isSameBewegungsbeziehung(Zaehlart.N, verkehrsbeziehungDto, verkehrsbeziehung);
        assertThat(result, is(true));

        verkehrsbeziehungDto = new BearbeiteVerkehrsbeziehungDTO();
        verkehrsbeziehungDto.setIsKreuzung(true);
        verkehrsbeziehungDto.setVon(1);
        verkehrsbeziehungDto.setNach(2);
        verkehrsbeziehung = new VerkehrsbeziehungEntity();
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

        Set<String> result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        Set<String> expected = new HashSet<>();
        assertThat(result, is(expected));

        zeitintervall.setPkw(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = new HashSet<>(List.of(Fahrzeug.KFZ.getName(), Fahrzeug.PKW.getName(), Fahrzeug.PKW_EINHEIT.getName()));
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = new HashSet<>(Arrays.asList(Fahrzeug.KFZ.getName(), Fahrzeug.PKW.getName(), Fahrzeug.LKW.getName(), Fahrzeug.GV.getName(), Fahrzeug.SV.getName(), Fahrzeug.GV_P.getName(), Fahrzeug.SV_P.getName(), Fahrzeug.PKW_EINHEIT.getName()));
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = new HashSet<>(Arrays.asList(Fahrzeug.KFZ.getName(), Fahrzeug.PKW.getName(), Fahrzeug.LKW.getName(), Fahrzeug.GV.getName(), Fahrzeug.SV.getName(), Fahrzeug.LZ.getName(), Fahrzeug.GV_P.getName(), Fahrzeug.SV_P.getName(),
                Fahrzeug.PKW_EINHEIT.getName()));
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        zeitintervall.setBusse(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = new HashSet<>(Arrays.asList(Fahrzeug.KFZ.getName(), Fahrzeug.PKW.getName(), Fahrzeug.LKW.getName(), Fahrzeug.GV.getName(), Fahrzeug.SV.getName(), Fahrzeug.LZ.getName(), Fahrzeug.BUS.getName(), Fahrzeug.GV_P.getName(), Fahrzeug.SV_P.getName(),
                Fahrzeug.PKW_EINHEIT.getName()));
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        zeitintervall.setBusse(1);
        zeitintervall.setKraftraeder(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = new HashSet<>(Arrays.asList(Fahrzeug.KFZ.getName(), Fahrzeug.PKW.getName(), Fahrzeug.LKW.getName(), Fahrzeug.GV.getName(), Fahrzeug.SV.getName(), Fahrzeug.LZ.getName(), Fahrzeug.BUS.getName(), Fahrzeug.KRAD.getName(), Fahrzeug.GV_P.getName(),
                Fahrzeug.SV_P.getName(), Fahrzeug.PKW_EINHEIT.getName()));
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        zeitintervall.setBusse(1);
        zeitintervall.setKraftraeder(1);
        zeitintervall.setFahrradfahrer(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = new HashSet<>(Arrays.asList(Fahrzeug.KFZ.getName(), Fahrzeug.PKW.getName(), Fahrzeug.LKW.getName(), Fahrzeug.GV.getName(), Fahrzeug.SV.getName(), Fahrzeug.LZ.getName(), Fahrzeug.BUS.getName(), Fahrzeug.KRAD.getName(), Fahrzeug.RAD.getName(),
                Fahrzeug.GV_P.getName(), Fahrzeug.SV_P.getName(), Fahrzeug.PKW_EINHEIT.getName()));
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));

        zeitintervall.setPkw(1);
        zeitintervall.setLkw(1);
        zeitintervall.setLastzuege(1);
        zeitintervall.setBusse(1);
        zeitintervall.setKraftraeder(1);
        zeitintervall.setFahrradfahrer(1);
        zeitintervall.setFussgaenger(1);
        result = externalZaehlungPersistierungsService.getFahrzeugKategorienAndFahrzeugklassen(Collections.singletonList(zeitintervall));
        expected = new HashSet<>(Arrays.asList(Fahrzeug.KFZ.getName(), Fahrzeug.PKW.getName(), Fahrzeug.LKW.getName(), Fahrzeug.GV.getName(), Fahrzeug.SV.getName(), Fahrzeug.LZ.getName(), Fahrzeug.BUS.getName(), Fahrzeug.KRAD.getName(), Fahrzeug.RAD.getName(),
                Fahrzeug.FUSS.getName(), Fahrzeug.GV_P.getName(), Fahrzeug.SV_P.getName(), Fahrzeug.PKW_EINHEIT.getName()));
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));
    }

    @Test
    public void getFahrzeugklassen() {
        Set<Fahrzeug> kategorien = new HashSet<>(
                Arrays.asList(Fahrzeug.PKW, Fahrzeug.LKW, Fahrzeug.LZ, Fahrzeug.BUS, Fahrzeug.KRAD, Fahrzeug.RAD, Fahrzeug.FUSS));
        List<Fahrzeug> expected = Arrays.asList(Fahrzeug.KFZ, Fahrzeug.PKW, Fahrzeug.LKW, Fahrzeug.GV, Fahrzeug.SV, Fahrzeug.LZ, Fahrzeug.BUS, Fahrzeug.KRAD,
                Fahrzeug.RAD, Fahrzeug.FUSS, Fahrzeug.GV_P, Fahrzeug.SV_P, Fahrzeug.PKW_EINHEIT);
        Set<Fahrzeug> result = externalZaehlungPersistierungsService.getFahrzeugklassen(kategorien);
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));
        assertThat(result.size(), is(expected.size()));

        // Fall: Bereits alle Fahrzeugklassen in kategorien enthalten -> Werden nicht erneut hinzugefügt
        kategorien = new HashSet<>(
                Arrays.asList(Fahrzeug.KFZ, Fahrzeug.PKW, Fahrzeug.LKW, Fahrzeug.GV, Fahrzeug.SV, Fahrzeug.LZ, Fahrzeug.BUS, Fahrzeug.KRAD, Fahrzeug.RAD,
                        Fahrzeug.FUSS, Fahrzeug.GV_P, Fahrzeug.SV_P, Fahrzeug.PKW_EINHEIT));
        result = externalZaehlungPersistierungsService.getFahrzeugklassen(kategorien);
        assertThat(new HashSet<>(result), is(new HashSet<>(expected)));
        assertThat(result.size(), is(expected.size()));
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
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setLaengsverkehre(null);
        zaehlung.setQuerungsverkehre(null);
        zaehlung.setVerkehrsbeziehungen(null);

        List<? extends MappedBewegungsbeziehung> result = internalZaehlungPersistierungsService
                .getAllBewegungsbeziehungenFromZaehlung(zaehlung);
        List<? extends MappedBewegungsbeziehung> expected = new ArrayList<>();
        assertThat(result, is(expected));

        // Fall: Gemischte Listen mit konkreten Bewegungsbeziehungen (Laengsverkehr, Querungsverkehr, Verkehrsbeziehung)
        final LaengsverkehrEntity l = new LaengsverkehrEntity();
        l.setKnotenarm(1);
        final QuerungsverkehrEntity q = new QuerungsverkehrEntity();
        q.setKnotenarm(2);
        final VerkehrsbeziehungEntity vb = new VerkehrsbeziehungEntity();
        vb.setVon(3);
        vb.setNach(4);

        zaehlung.setLaengsverkehre(Arrays.asList(l, null));
        zaehlung.setQuerungsverkehre(Collections.singletonList(q));
        zaehlung.setVerkehrsbeziehungen(Collections.singletonList(vb));

        result = internalZaehlungPersistierungsService.getAllBewegungsbeziehungenFromZaehlung(zaehlung);
        expected = Arrays.asList(l, q, vb);
        assertThat(result, is(expected));
    }

    @Test
    public void updateStatus_setToInstructed() throws BrokenInfrastructureException, DataNotFoundException, PlausibilityException {
        // Arrange: Mock ZaehlstelleEntity und ZaehlungEntity mit passender ID
        final ZaehlstelleEntity zaehlstelle = new ZaehlstelleEntity();
        zaehlstelle.setId(UUID.fromString("zs1"));
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setId(UUID.fromString("z1"));
        zaehlung.setStatus(Status.CREATED);
        zaehlung.setDienstleisterkennung("");
        zaehlstelle.setZaehlungen(java.util.Arrays.asList(zaehlung));

        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        when(mockIndexService.getZaehlstelleByZaehlungId(UUID.fromString("z1"))).thenReturn(zaehlstelle);
        when(mockSecService.isFachadmin()).thenReturn(true);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                new SanitizationService(),
                authorizationService);

        final UpdateStatusDTO dto = new UpdateStatusDTO();
        dto.setZaehlungId(UUID.fromString("z1"));
        dto.setStatus(Status.INSTRUCTED);
        dto.setDienstleisterkennung("DLK");

        // Act
        final BackendIdDTO result = svc.updateStatus(dto);

        // Assert
        assertThat(result.getId(), is("z1"));
        assertThat(zaehlung.getStatus(), is(Status.INSTRUCTED.name()));
        assertThat(zaehlung.getDienstleisterkennung(), is("DLK"));

        // Verifikation der Mockaufrufe
        verify(mockIndexService, times(1)).getZaehlstelleByZaehlungId(UUID.fromString("z1"));
        verify(mockIndexService, times(1)).erneuereZaehlstelle(zaehlstelle);
        verify(mockZeitService, never()).aufbereitenUndPersistierenZeitintervalleWhenNumberOfZeitintervalleIsPlausible(any(), anyInt());
        verify(mockZeitService, never()).deleteZeitintervalleForCorrection(anyString());
    }

    @Test
    public void updateStatus_setToAccomplished() throws BrokenInfrastructureException, DataNotFoundException, PlausibilityException {
        // Arrange: ZaehlungEntity mit Zaehldauer und ohne Verkehrsbeziehungen
        final ZaehlstelleEntity zaehlstelle = new ZaehlstelleEntity();
        zaehlstelle.setId(UUID.fromString("zs2"));
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setId(UUID.fromString("z2"));
        zaehlung.setStatus(Status.CREATED);
        zaehlung.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        zaehlstelle.setZaehlungen(java.util.Arrays.asList(zaehlung));

        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        when(mockIndexService.getZaehlstelleByZaehlungId(UUID.fromString("z2"))).thenReturn(zaehlstelle);
        when(mockSecService.isFachadmin()).thenReturn(true);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        final UpdateStatusDTO dto = new UpdateStatusDTO();
        dto.setZaehlungId(UUID.fromString("z2"));
        dto.setStatus(Status.ACCOMPLISHED);

        // Act
        final BackendIdDTO result = svc.updateStatus(dto);

        // Assert
        assertThat(result.getId(), is("z2"));
        assertThat(zaehlung.getStatus(), is(Status.ACCOMPLISHED.name()));

        // checkZeitintervalleIfPlausible muss aufgerufen werden
        verify(mockZeitService, times(1)).aufbereitenUndPersistierenZeitintervalleWhenNumberOfZeitintervalleIsPlausible(eq(zaehlung), anyInt());
        verify(mockIndexService, times(1)).erneuereZaehlstelle(zaehlstelle);
        verify(mockIndexService, times(1)).getZaehlstelleByZaehlungId(UUID.fromString("z2"));
    }

    @Test
    public void updateStatus_setToCorrection() throws BrokenInfrastructureException, DataNotFoundException, PlausibilityException {
        // Arrange: ZaehlungEntity mit Knotenarmen die Filenames besitzen
        final ZaehlstelleEntity zaehlstelle = new ZaehlstelleEntity();
        zaehlstelle.setId(UUID.fromString("zs3"));
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setId(UUID.fromString("z3"));
        zaehlung.setStatus(Status.CREATED);
        final KnotenarmEntity arm1 = new KnotenarmEntity();
        arm1.setFilename("file1.jpg");
        final KnotenarmEntity arm2 = new KnotenarmEntity();
        arm2.setFilename("file2.jpg");
        zaehlung.setKnotenarme(java.util.Arrays.asList(arm1, arm2));
        zaehlstelle.setZaehlungen(java.util.Arrays.asList(zaehlung));

        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        when(mockIndexService.getZaehlstelleByZaehlungId(UUID.fromString("z3"))).thenReturn(zaehlstelle);
        when(mockSecService.isFachadmin()).thenReturn(true);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        final UpdateStatusDTO dto = new UpdateStatusDTO();
        dto.setZaehlungId(UUID.fromString("z3"));
        dto.setStatus(Status.CORRECTION);

        // Act
        final BackendIdDTO result = svc.updateStatus(dto);

        // Assert
        assertThat(result.getId(), is("z3"));
        assertThat(zaehlung.getStatus(), is(Status.CORRECTION.name()));
        // Filenames wurden geleert
        zaehlung.getKnotenarme().forEach(a -> assertThat(a.getFilename(), is("")));

        verify(mockZeitService, times(1)).deleteZeitintervalleForCorrection("z3");
        verify(mockIndexService, times(1)).getZaehlstelleByZaehlungId(UUID.fromString("z3"));
        verify(mockIndexService, times(1)).erneuereZaehlstelle(zaehlstelle);

        // Weiterer Testfall: Keine passende ZaehlungEntity vorhanden -> es wird nichts verändert
    }

    @Test
    public void updateStatus_noMatchingZaehlung() throws BrokenInfrastructureException, DataNotFoundException, PlausibilityException {
        // Arrange: ZaehlstelleEntity ohne passende ZaehlungEntity
        final ZaehlstelleEntity zaehlstelle = new ZaehlstelleEntity();
        zaehlstelle.setId(UUID.fromString("zs4"));
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setId(UUID.fromString("z4"));
        zaehlstelle.setZaehlungen(java.util.Arrays.asList(zaehlung));

        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        when(mockIndexService.getZaehlstelleByZaehlungId(UUID.fromString("not-existing"))).thenReturn(zaehlstelle);
        when(mockSecService.isFachadmin()).thenReturn(true);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        final UpdateStatusDTO dto = new UpdateStatusDTO();
        dto.setZaehlungId(UUID.fromString("not-existing"));
        dto.setStatus(Status.ACTIVE);

        // Act
        final BackendIdDTO result = svc.updateStatus(dto);

        // Assert: Keine Änderung an vorhandener ZaehlungEntity
        assertThat(result.getId(), is("not-existing"));
        assertThat(zaehlung.getStatus(), is((String) null));

        verify(mockIndexService, times(1)).getZaehlstelleByZaehlungId(UUID.fromString("not-existing"));
        verify(mockIndexService, times(1)).erneuereZaehlstelle(zaehlstelle);
        verifyNoMoreInteractions(mockZeitService);
    }

    @Test
    public void updateStatus_indexServiceThrows() throws BrokenInfrastructureException, DataNotFoundException, PlausibilityException {
        // Arrange: IndexService wirft DataNotFoundException
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        when(mockIndexService.getZaehlstelleByZaehlungId(UUID.fromString("missing"))).thenThrow(new DataNotFoundException("not found"));

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        final UpdateStatusDTO dto = new UpdateStatusDTO();
        dto.setZaehlungId(UUID.fromString("missing"));
        dto.setStatus(Status.ACTIVE);

        // Act / Assert: Exception wird durchgereicht
        assertThrows(DataNotFoundException.class, () -> svc.updateStatus(dto));

        verify(mockIndexService, times(1)).getZaehlstelleByZaehlungId(UUID.fromString("missing"));
        verifyNoMoreInteractions(mockIndexService);
        verifyNoInteractions(mockZeitService);
    }

    @Test
    public void updateStatus_notAuthorizedUser_throwsAccessDeniedException() throws DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        final var id = UUID.randomUUID();
        final var zaehlstelle = new ZaehlstelleEntity();
        final var zaehlung = new ZaehlungEntity();
        zaehlung.setId(id);
        zaehlung.setDienstleisterkennung("dl1");
        zaehlstelle.getZaehlungen().add(zaehlung);

        when(mockIndexService.getZaehlung(id)).thenReturn(zaehlung);

        final UpdateStatusDTO dto = new UpdateStatusDTO();
        dto.setZaehlungId(id);
        dto.setStatus(Status.ACCOMPLISHED);

        // Nutzer mit einem anderen Username als der Dienstleisterkennung der Zählung und ohne Rolle Fachadmin setzen
        when(mockSecService.getAuthenticatedUsername()).thenReturn("tester");
        when(mockSecService.isFachadmin()).thenReturn(false);

        // Act and Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> svc.updateStatus(dto));
        assertEquals("Der Dienstleister ist nicht berechtigt, diese Zählung zu ändern.", ex.getMessage());
    }

    @Test
    public void getNumberOfNecessaryZeitintervalle_allListsNull_returnsZero() {
        // Arrange: ZaehlungEntity mit Zaehldauer 24h, aber alle Bewegungsbeziehungslisten null
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);
        zaehlung.setVerkehrsbeziehungen(null);
        zaehlung.setLaengsverkehre(null);
        zaehlung.setQuerungsverkehre(null);

        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        // Act
        final int result = svc.getNumberOfNecessaryZeitintervalle(zaehlung);

        // Assert
        // Keine Verkehrsbeziehungen -> Ergebnis 0
        assertThat(result, is(0));
        verifyNoInteractions(mockIndexService);
        verifyNoInteractions(mockZeitService);
    }

    @Test
    public void getNumberOfNecessaryZeitintervalle_multipleRelationshipTypes() {
        // Arrange: ZaehlungEntity 24h, mit 2 Verkehrsbeziehungen, 1 Laengsverkehr, querungsverkehr null => totalRelationships = 3
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);

        final VerkehrsbeziehungEntity vb1 = new VerkehrsbeziehungEntity();
        final VerkehrsbeziehungEntity vb2 = new VerkehrsbeziehungEntity();
        zaehlung.setVerkehrsbeziehungen(Arrays.asList(vb1, vb2));

        final LaengsverkehrEntity l = new LaengsverkehrEntity();
        zaehlung.setLaengsverkehre(Collections.singletonList(l));
        zaehlung.setQuerungsverkehre(null);

        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        // Act
        final int result = svc.getNumberOfNecessaryZeitintervalle(zaehlung);

        // Assert
        // DAUER_24_STUNDEN -> 96 intervals per relationship * 3 relationships = 288
        assertThat(result, is(96 * 3));
        verifyNoInteractions(mockIndexService);
        verifyNoInteractions(mockZeitService);
    }

    @Test
    public void getNumberOfNecessaryZeitintervalle_allTypesNonEmpty_2x4h() {
        // Arrange: DAUER_2_X_4_STUNDEN (32 intervals) and three lists each with 2 elements => totalRelationships = 6
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehldauer(Zaehldauer.DAUER_2_X_4_STUNDEN);

        final VerkehrsbeziehungEntity vb1 = new VerkehrsbeziehungEntity();
        final VerkehrsbeziehungEntity vb2 = new VerkehrsbeziehungEntity();
        zaehlung.setVerkehrsbeziehungen(Arrays.asList(vb1, vb2));

        final LaengsverkehrEntity l1 = new LaengsverkehrEntity();
        final LaengsverkehrEntity l2 = new LaengsverkehrEntity();
        zaehlung.setLaengsverkehre(Arrays.asList(l1, l2));

        final QuerungsverkehrEntity q1 = new QuerungsverkehrEntity();
        final QuerungsverkehrEntity q2 = new QuerungsverkehrEntity();
        zaehlung.setQuerungsverkehre(Arrays.asList(q1, q2));

        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        // Act
        final int result = svc.getNumberOfNecessaryZeitintervalle(zaehlung);

        // Assert: 32 * 6 = 192
        assertThat(result, is(32 * 6));
        verifyNoInteractions(mockIndexService);
        verifyNoInteractions(mockZeitService);
    }

    @Test
    public void getNumberOfNecessaryZeitintervalle_sonstigeReturnsZeroEvenWithRelationships() {
        // Arrange: SONSTIGE has 0 intervals regardless of relationships
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehldauer(Zaehldauer.SONSTIGE);
        final VerkehrsbeziehungEntity vb = new VerkehrsbeziehungEntity();
        zaehlung.setVerkehrsbeziehungen(Collections.singletonList(vb));

        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        // Act
        final int result = svc.getNumberOfNecessaryZeitintervalle(zaehlung);

        // Assert: SONSTIGE -> 0
        assertThat(result, is(0));
        verifyNoInteractions(mockIndexService);
        verifyNoInteractions(mockZeitService);
    }

    @Test
    public void getNumberOfNecessaryZeitintervalle_nullZaehldauer_throwsException() {
        // Arrange: null zaeldauer should cause NullPointerException when calling valueOf
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setZaehldauer(null);

        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        // Act / Assert
        assertThrows(NullPointerException.class, () -> svc.getNumberOfNecessaryZeitintervalle(zaehlung));
        verifyNoInteractions(mockIndexService);
        verifyNoInteractions(mockZeitService);
    }

    @Test
    public void updateStatusIfDateIsInThePast_dateInPast_callsUpdate() throws BrokenInfrastructureException, DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        final UUID id = UUID.fromString("zp1");
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setId(id);
        zaehlung.setDatum(LocalDate.now().minusDays(1));

        when(mockIndexService.getZaehlung(id)).thenReturn(zaehlung);

        final UpdateStatusDTO dto = new UpdateStatusDTO();
        dto.setZaehlungId(id);

        // Act
        svc.updateStatusIfDateIsInThePast(dto, Status.COUNTING);

        // Assert
        verify(mockIndexService, times(1)).getZaehlung(id);
        verify(mockIndexService, times(1)).updateStatusOfZaehlung(eq(id), eq(Status.COUNTING));
    }

    @Test
    public void updateStatusIfDateIsInThePast_dateIsToday_callsUpdate() throws BrokenInfrastructureException, DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        final UUID id = UUID.fromString("zt1");
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setId(id);
        zaehlung.setDatum(LocalDate.now());

        when(mockIndexService.getZaehlung(id)).thenReturn(zaehlung);

        final UpdateStatusDTO dto = new UpdateStatusDTO();
        dto.setZaehlungId(id);

        // Act
        svc.updateStatusIfDateIsInThePast(dto, Status.COUNTING);

        // Assert
        verify(mockIndexService, times(1)).getZaehlung(id);
        verify(mockIndexService, times(1)).updateStatusOfZaehlung(eq(id), eq(Status.COUNTING));
    }

    @Test
    public void updateStatusIfDateIsInThePast_dateInFuture_noUpdate() throws BrokenInfrastructureException, DataNotFoundException {
        // Arrange
        final ZaehlstelleIndexService mockIndexService = Mockito.mock(ZaehlstelleIndexService.class);
        final ZeitintervallPersistierungsService mockZeitService = Mockito.mock(ZeitintervallPersistierungsService.class);
        final SanitizationService mockSanitizationService = Mockito.mock(SanitizationService.class);
        final SecurityContextInformationExtractorService mockSecService = Mockito.mock(SecurityContextInformationExtractorService.class);
        final AuthorizationService authorizationService = new AuthorizationService(mockIndexService, mockSecService);

        final InternalZaehlungPersistierungsService svc = new InternalZaehlungPersistierungsService(
                mockIndexService,
                mockZeitService,
                pkwEinheitRepository,
                null,
                null,
                mockSanitizationService,
                authorizationService);

        final UUID id = UUID.fromString("zf1");
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setId(id);
        zaehlung.setDatum(LocalDate.now().plusDays(2));

        when(mockIndexService.getZaehlung(id)).thenReturn(zaehlung);

        final UpdateStatusDTO dto = new UpdateStatusDTO();
        dto.setZaehlungId(id);

        // Act
        svc.updateStatusIfDateIsInThePast(dto, Status.COUNTING);

        // Assert
        verify(mockIndexService, times(1)).getZaehlung(id);
        verify(mockIndexService, never()).updateStatusOfZaehlung(eq(id), eq(Status.COUNTING));
    }
}
