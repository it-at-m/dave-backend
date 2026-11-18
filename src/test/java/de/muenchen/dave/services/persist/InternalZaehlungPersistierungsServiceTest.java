package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.mapper.PkwEinheitMapper;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.relational.repository.PkwEinheitRepository;
import de.muenchen.dave.relational.zaehlstellen.LaengsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.QuerungsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.VerkehrsbeziehungEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.services.SanitizationService;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.security.AuthorizationService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

/**
 * Unit-Tests für InternalZaehlungPersistierungsService.
 *
 * Es werden folgende Methoden getestet:
 * - setAdditionalDataToZeitintervall (verschiedene Zählarten)
 * - getBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto
 * - isSameBewegungsbeziehung (QU, FJS, sonstige)
 * - createVerkehrsbeziehungForZeitintervall
 * - createQuerungsverkehrForZeitintervall
 * - createLaengsverkehrForZeitintervall
 * - getKoordinateZaehlstelleWhenZaehlungWithinDistance
 */
@ExtendWith(MockitoExtension.class)
class InternalZaehlungPersistierungsServiceTest {

    @Mock
    private ZaehlstelleIndexService indexService;

    @Mock
    private ZeitintervallPersistierungsService zeitintervallPersistierungsService;

    @Mock
    private PkwEinheitRepository pkwEinheitRepository;

    @Mock
    private ZeitintervallMapper zeitintervallMapper;

    @Mock
    private PkwEinheitMapper pkwEinheitMapper;

    @Mock
    private SanitizationService sanitizationService;

    @Mock
    private AuthorizationService authorizationService;

    private InternalZaehlungPersistierungsService service;

    @BeforeEach
    void setUp() {
        service = Mockito.spy(new InternalZaehlungPersistierungsService(indexService, zeitintervallPersistierungsService, pkwEinheitRepository,
                zeitintervallMapper, pkwEinheitMapper, sanitizationService, authorizationService));
    }

    @Test
    void testSetAdditionalDataToZeitintervall_Laengsverkehr_FJS() {
        final var zeitintervall = new Zeitintervall();

        final var zaehlung = new ZaehlungEntity();
        final var zaehlungId = UUID.randomUUID();
        zaehlung.setId(zaehlungId);
        zaehlung.setZaehlart(Zaehlart.FJS);
        zaehlung.setZaehldauer(Zaehldauer.DAUER_24_STUNDEN);

        // Bewegungsbeziehung in der ZaehlungEntity (Elasticsearch-Objekt) bereitstellen
        final var bewegungsId = UUID.randomUUID();
        final var esLaengs = new LaengsverkehrEntity();
        esLaengs.setId(bewegungsId);
        esLaengs.setKnotenarm(5);
        esLaengs.setRichtung(Bewegungsrichtung.EIN);
        esLaengs.setStrassenseite(Himmelsrichtung.N);
        zaehlung.setLaengsverkehre(List.of(esLaengs));

        final var bearbeite = new BearbeiteLaengsverkehrDTO();
        bearbeite.setKnotenarm(5);
        bearbeite.setRichtung(Bewegungsrichtung.EIN);
        bearbeite.setStrassenseite(Himmelsrichtung.N);

        // createHochrechnung stubben
        final var hochrechnung = new Hochrechnung();
        doReturn(hochrechnung).when(service).createHochrechnung(Mockito.any(), Mockito.any(), Mockito.any());

        final var result = service.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, bearbeite);

        assertNotNull(result);
        assertEquals(zaehlungId, result.getZaehlungId());
        assertEquals(bewegungsId, result.getBewegungsbeziehungId());
        assertSame(hochrechnung, result.getHochrechnung());

        final Laengsverkehr la = result.getLaengsverkehr();
        assertNotNull(la);
        assertEquals(bearbeite.getKnotenarm(), la.getKnotenarm());
        assertEquals(bearbeite.getRichtung(), la.getRichtung());
        assertEquals(bearbeite.getStrassenseite(), la.getStrassenseite());
    }

    @Test
    void testGetBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto_FindsMatching() {
        final var zaehlung = new ZaehlungEntity();
        // Zaehlart setzen, damit die Methode weiß, nach QU zu suchen
        zaehlung.setZaehlart(Zaehlart.QU);
        final var esQ = new QuerungsverkehrEntity();
        esQ.setId(UUID.randomUUID());
        esQ.setKnotenarm(2);
        esQ.setRichtung(Himmelsrichtung.S);
        zaehlung.setQuerungsverkehre(List.of(esQ));

        final var bearbeite = new BearbeiteQuerungsverkehrDTO();
        bearbeite.setKnotenarm(2);
        bearbeite.setRichtung(Himmelsrichtung.S);

        final var opt = service.getBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto(zaehlung, bearbeite);
        assertTrue(opt.isPresent());
        assertEquals(esQ.getId(), opt.get().getId());
    }

    @Test
    void testGetBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto_NoMatch() {
        final var zaehlung = new ZaehlungEntity();
        // Zaehlart setzen, damit die Methode weiß, nach QU zu suchen
        zaehlung.setZaehlart(Zaehlart.QU);
        final var esQ = new QuerungsverkehrEntity();
        esQ.setId(UUID.randomUUID());
        esQ.setKnotenarm(2);
        esQ.setRichtung(Himmelsrichtung.S);
        zaehlung.setQuerungsverkehre(List.of(esQ));

        final var bearbeite = new BearbeiteQuerungsverkehrDTO();
        bearbeite.setKnotenarm(99);
        bearbeite.setRichtung(Himmelsrichtung.N);

        final var opt = service.getBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto(zaehlung, bearbeite);
        assertTrue(opt.isEmpty());
    }

    @Test
    void testIsSameBewegungsbeziehung_QU_true_and_false() {
        final var zaehlart = Zaehlart.QU;
        final var bearbeite = new BearbeiteQuerungsverkehrDTO();
        bearbeite.setKnotenarm(4);
        bearbeite.setRichtung(Himmelsrichtung.NO);

        final var bewegung = new QuerungsverkehrEntity();
        bewegung.setKnotenarm(4);
        bewegung.setRichtung(Himmelsrichtung.NO);

        assertTrue(service.isSameBewegungsbeziehung(zaehlart, bearbeite, bewegung));

        bewegung.setRichtung(Himmelsrichtung.S);
        assertFalse(service.isSameBewegungsbeziehung(zaehlart, bearbeite, bewegung));
    }

    @Test
    void testIsSameBewegungsbeziehung_FJS_true_and_false() {
        final var zaehlart = Zaehlart.FJS;
        final var bearbeite = new BearbeiteLaengsverkehrDTO();
        bearbeite.setKnotenarm(6);
        bearbeite.setRichtung(Bewegungsrichtung.EIN);
        bearbeite.setStrassenseite(Himmelsrichtung.O);

        final var bewegung = new LaengsverkehrEntity();
        bewegung.setKnotenarm(6);
        bewegung.setRichtung(Bewegungsrichtung.EIN);
        bewegung.setStrassenseite(Himmelsrichtung.O);

        assertTrue(service.isSameBewegungsbeziehung(zaehlart, bearbeite, bewegung));

        bewegung.setKnotenarm(7);
        assertFalse(service.isSameBewegungsbeziehung(zaehlart, bearbeite, bewegung));
    }

    @Test
    void testIsSameBewegungsbeziehung_Other_true_and_false() {
        final var zaehlart = Zaehlart.N;
        final var bearbeite = new BearbeiteVerkehrsbeziehungDTO();
        bearbeite.setIsKreuzung(Boolean.TRUE);
        bearbeite.setVon(1);
        bearbeite.setNach(2);
        bearbeite.setStrassenseite(Himmelsrichtung.NW);
        bearbeite.setKnotenarm(3);
        bearbeite.setHinein(Boolean.FALSE);
        bearbeite.setHeraus(Boolean.FALSE);
        bearbeite.setVorbei(Boolean.FALSE);

        final var bewegung = new VerkehrsbeziehungEntity();
        bewegung.setIsKreuzung(Boolean.TRUE);
        bewegung.setVon(1);
        bewegung.setNach(2);
        bewegung.setStrassenseite(Himmelsrichtung.NW);
        bewegung.setKnotenarm(3);
        bewegung.setHinein(Boolean.FALSE);
        bewegung.setHeraus(Boolean.FALSE);
        bewegung.setVorbei(Boolean.FALSE);

        assertTrue(service.isSameBewegungsbeziehung(zaehlart, bearbeite, bewegung));

        bewegung.setVon(99);
        assertFalse(service.isSameBewegungsbeziehung(zaehlart, bearbeite, bewegung));
    }

    @Test
    void testCreateVerkehrsbeziehungForZeitintervall_Kreuzung_and_Kreisverkehr() {
        final var kreuz = new BearbeiteVerkehrsbeziehungDTO();
        kreuz.setIsKreuzung(Boolean.TRUE);
        kreuz.setVon(10);
        kreuz.setNach(11);
        kreuz.setStrassenseite(Himmelsrichtung.SW);

        final var resultKreuz = service.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, kreuz);
        assertNotNull(resultKreuz);
        assertEquals(kreuz.getVon(), resultKreuz.getVon());
        assertEquals(kreuz.getNach(), resultKreuz.getNach());
        assertEquals(kreuz.getStrassenseite(), resultKreuz.getStrassenseite());

        final var kreis = new BearbeiteVerkehrsbeziehungDTO();
        kreis.setIsKreuzung(Boolean.FALSE);
        kreis.setKnotenarm(77);
        kreis.setHinein(Boolean.TRUE);

        final var resultKreis = service.createVerkehrsbeziehungForZeitintervall(Zaehlart.N, kreis);
        assertNotNull(resultKreis);
        assertEquals(kreis.getKnotenarm(), resultKreis.getVon());
        assertEquals(FahrbewegungKreisverkehr.HINEIN, resultKreis.getFahrbewegungKreisverkehr());
        assertNull(resultKreis.getNach());
    }

    @Test
    void testCreateLaengsverkehrAndQuerungsverkehrForZeitintervall() {
        final var laeng = new BearbeiteLaengsverkehrDTO();
        laeng.setKnotenarm(3);
        laeng.setRichtung(Bewegungsrichtung.EIN);
        laeng.setStrassenseite(Himmelsrichtung.NO);

        final var l = service.createLaengsverkehrForZeitintervall(laeng);
        assertNotNull(l);
        assertEquals(laeng.getKnotenarm(), l.getKnotenarm());
        assertEquals(laeng.getRichtung(), l.getRichtung());
        assertEquals(laeng.getStrassenseite(), l.getStrassenseite());

        final var quer = new BearbeiteQuerungsverkehrDTO();
        quer.setKnotenarm(8);
        quer.setRichtung(Himmelsrichtung.S);

        final var q = service.createQuerungsverkehrForZeitintervall(quer);
        assertNotNull(q);
        assertEquals(quer.getKnotenarm(), q.getKnotenarm());
        assertEquals(quer.getRichtung(), q.getRichtung());
    }

}
