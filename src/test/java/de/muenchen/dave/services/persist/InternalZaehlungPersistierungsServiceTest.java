package de.muenchen.dave.services.persist;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.mapper.PkwEinheitMapper;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.repositories.relationaldb.PkwEinheitRepository;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

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

    private InternalZaehlungPersistierungsService service;

    @BeforeEach
    void setUp() {
        service = Mockito.spy(new InternalZaehlungPersistierungsService(indexService, zeitintervallPersistierungsService, pkwEinheitRepository,
                zeitintervallMapper, pkwEinheitMapper));
    }

    @Test
    void testSetAdditionalDataToZeitintervall_Laengsverkehr_FJS() {
        final var zeitintervall = new Zeitintervall();

        final var zaehlung = new Zaehlung();
        final var zaehlungId = UUID.randomUUID().toString();
        zaehlung.setId(zaehlungId);
        zaehlung.setZaehlart("FJS");
        zaehlung.setZaehldauer("24h");

        // Bewegungsbeziehung in der Zaehlung (Elasticsearch-Objekt) bereitstellen
        final var bewegungsId = UUID.randomUUID().toString();
        final var esLaengs = new de.muenchen.dave.domain.elasticsearch.Laengsverkehr();
        esLaengs.setId(bewegungsId);
        esLaengs.setKnotenarm(5);
        esLaengs.setRichtung(Bewegungsrichtung.EIN);
        esLaengs.setStrassenseite(Himmelsrichtung.N);
        zaehlung.setLaengsverkehr(List.of(esLaengs));

        final var bearbeite = new BearbeiteLaengsverkehrDTO();
        bearbeite.setKnotenarm(5);
        bearbeite.setRichtung(Bewegungsrichtung.EIN);
        bearbeite.setStrassenseite(Himmelsrichtung.N);

        // createHochrechnung stubben
        final var hochrechnung = new Hochrechnung();
        doReturn(hochrechnung).when(service).createHochrechnung(Mockito.any(), Mockito.any(), Mockito.any());

        final var result = service.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, bearbeite);

        assertNotNull(result);
        assertEquals(UUID.fromString(zaehlungId), result.getZaehlungId());
        assertEquals(UUID.fromString(bewegungsId), result.getBewegungsbeziehungId());
        assertSame(hochrechnung, result.getHochrechnung());

        final Laengsverkehr la = result.getLaengsverkehr();
        assertNotNull(la);
        assertEquals(bearbeite.getKnotenarm(), la.getKnotenarm());
        assertEquals(bearbeite.getRichtung(), la.getRichtung());
        assertEquals(bearbeite.getStrassenseite(), la.getStrassenseite());
    }

    @Test
    void testGetBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto_FindsMatching() {
        final var zaehlung = new Zaehlung();
        // Zaehlart setzen, damit die Methode weiß, nach QU zu suchen
        zaehlung.setZaehlart("QU");
        final var esQ = new de.muenchen.dave.domain.elasticsearch.Querungsverkehr();
        esQ.setId(UUID.randomUUID().toString());
        esQ.setKnotenarm(2);
        esQ.setRichtung(Himmelsrichtung.S);
        zaehlung.setQuerungsverkehr(List.of(esQ));

        final var bearbeite = new BearbeiteQuerungsverkehrDTO();
        bearbeite.setKnotenarm(2);
        bearbeite.setRichtung(Himmelsrichtung.S);

        final var opt = service.getBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto(zaehlung, bearbeite);
        assertTrue(opt.isPresent());
        assertEquals(esQ.getId(), opt.get().getId());
    }

    @Test
    void testGetBewegungsbeziehungFromBearbeiteBewegungsbeziehungDto_NoMatch() {
        final var zaehlung = new Zaehlung();
        // Zaehlart setzen, damit die Methode weiß, nach QU zu suchen
        zaehlung.setZaehlart("QU");
        final var esQ = new de.muenchen.dave.domain.elasticsearch.Querungsverkehr();
        esQ.setId(UUID.randomUUID().toString());
        esQ.setKnotenarm(2);
        esQ.setRichtung(Himmelsrichtung.S);
        zaehlung.setQuerungsverkehr(List.of(esQ));

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

        final var bewegung = new de.muenchen.dave.domain.elasticsearch.Querungsverkehr();
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

        final var bewegung = new de.muenchen.dave.domain.elasticsearch.Laengsverkehr();
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

        final var bewegung = new de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung();
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

    @Test
    void testGetKoordinateZaehlstelleWhenZaehlungWithinDistance() {
        final var zaehlung = new Zaehlung();
        final var zaehlungPoint = new GeoPoint(48.137154, 11.576124); // München
        zaehlung.setPunkt(zaehlungPoint);

        final var zaehlstelle = new Zaehlstelle();
        // exakt gleiche Koordinate -> distance 0
        zaehlstelle.setPunkt(new GeoPoint(48.137154, 11.576124));

        final var result = service.getKoordinateZaehlstelleWhenZaehlungWithinDistance(100.0, zaehlstelle, zaehlung);
        // Da Positionen gleich sind, sollte die Koordinate der Zaehlstelle zurückgegeben werden
        assertEquals(zaehlstelle.getPunkt(), result);

        // Große Entfernung: Unterschiede
        zaehlstelle.setPunkt(new GeoPoint(0.0, 0.0));
        final var result2 = service.getKoordinateZaehlstelleWhenZaehlungWithinDistance(100.0, zaehlstelle, zaehlung);
        // sollte die Koordinate der Zaehlung zurückgeben
        assertEquals(zaehlung.getPunkt(), result2);
    }

}
