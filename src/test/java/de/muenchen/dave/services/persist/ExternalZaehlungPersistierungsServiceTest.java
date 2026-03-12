package de.muenchen.dave.services.persist;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Laengsverkehr;
import de.muenchen.dave.domain.Querungsverkehr;
import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.external.ExternalLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.external.ExternalVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.mapper.KnotenarmMapper;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit-Tests für die Methode setAdditionalDataToZeitintervall der Klasse
 * ExternalZaehlungPersistierungsService.
 *
 * Es werden verschiedene Pfade getestet:
 * - FJS -> Laengsverkehr
 * - QU -> Querungsverkehr
 * - Kreuzung (isKreuzung=true) -> Verkehrsbeziehung mit von/nach/strassenseite
 * - Kreisverkehr ohne gesetzte Fahrbewegungsattribute -> fehlende Fahrbewegung
 */
@ExtendWith(MockitoExtension.class)
class ExternalZaehlungPersistierungsServiceTest {

    @Mock
    private ZaehlstelleIndexService indexService;

    @Mock
    private ZeitintervallPersistierungsService zeitintervallPersistierungsService;

    @Mock
    private ZeitintervallMapper zeitintervallMapper;

    @Mock
    private KnotenarmMapper knotenarmMapper;

    private ExternalZaehlungPersistierungsService service;

    @BeforeEach
    void setUp() {
        // Erzeuge ein Spy-Objekt, so dass einzelne Hilfsmethoden (z.B. createHochrechnung)
        // bei Bedarf gestubbt werden können.
        service = Mockito
                .spy(new ExternalZaehlungPersistierungsService(indexService, zeitintervallPersistierungsService, zeitintervallMapper, knotenarmMapper));
    }

    @Test
    void testSetAdditionalDataToZeitintervall_Laengsverkehr_FJS() {
        // Arrange: Zeitintervall, Zaehlung und ExternalLaengsverkehrDTO vorbereiten
        final var zeitintervall = new Zeitintervall();
        zeitintervall.setStartUhrzeit(LocalDateTime.now());
        zeitintervall.setEndeUhrzeit(LocalDateTime.now().plusHours(1));

        final var zaehlung = new Zaehlung();
        final var zaehlungId = UUID.randomUUID().toString();
        zaehlung.setId(zaehlungId);
        // FJS führt zur Erzeugung von Laengsverkehr
        zaehlung.setZaehlart("FJS");
        zaehlung.setZaehldauer("24h");

        final var external = new ExternalLaengsverkehrDTO();
        final var bewegungsbeziehungId = UUID.randomUUID().toString();
        external.setId(bewegungsbeziehungId);
        external.setKnotenarm(5);
        external.setRichtung(null);
        external.setStrassenseite(null);

        // Stub createHochrechnung, um eine Vorhersagbarkeit der Hochrechnung zu haben
        final var hochrechnung = new Hochrechnung();
        hochrechnung.setFaktorKfz(BigDecimal.valueOf(2.5));
        doReturn(hochrechnung).when(service).createHochrechnung(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());

        // Act
        final var result = service.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, external);

        // Assert
        assertNotNull(result);
        assertEquals(UUID.fromString(zaehlungId), result.getZaehlungId());
        assertEquals(UUID.fromString(bewegungsbeziehungId), result.getBewegungsbeziehungId());
        assertSame(hochrechnung, result.getHochrechnung());

        final Laengsverkehr laengs = result.getLaengsverkehr();
        assertNotNull(laengs, "Laengsverkehr sollte für FJS gesetzt werden");
        assertEquals(external.getKnotenarm(), laengs.getKnotenarm());
        assertEquals(external.getRichtung(), laengs.getRichtung());
        assertEquals(external.getStrassenseite(), laengs.getStrassenseite());
    }

    @Test
    void testSetAdditionalDataToZeitintervall_Querungsverkehr_QU() {
        // Arrange: Zeitintervall, Zaehlung und ExternalQuerungsverkehrDTO vorbereiten
        final var zeitintervall = new Zeitintervall();
        zeitintervall.setStartUhrzeit(LocalDateTime.now());
        zeitintervall.setEndeUhrzeit(LocalDateTime.now().plusHours(1));

        final var zaehlung = new Zaehlung();
        final var zaehlungId = UUID.randomUUID().toString();
        zaehlung.setId(zaehlungId);
        // QU führt zur Erzeugung von Querungsverkehr
        zaehlung.setZaehlart("QU");
        zaehlung.setZaehldauer("24h");

        final var external = new ExternalQuerungsverkehrDTO();
        final var bewegungsbeziehungId = UUID.randomUUID().toString();
        external.setId(bewegungsbeziehungId);
        external.setKnotenarm(7);
        external.setRichtung(null);

        // Stub createHochrechnung
        final var hochrechnung = new Hochrechnung();
        doReturn(hochrechnung).when(service).createHochrechnung(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());

        // Act
        final var result = service.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, external);

        // Assert
        assertNotNull(result);
        assertEquals(UUID.fromString(zaehlungId), result.getZaehlungId());
        assertEquals(UUID.fromString(bewegungsbeziehungId), result.getBewegungsbeziehungId());
        assertSame(hochrechnung, result.getHochrechnung());

        final Querungsverkehr q = result.getQuerungsverkehr();
        assertNotNull(q, "Querungsverkehr sollte für QU gesetzt werden");
        assertEquals(external.getKnotenarm(), q.getKnotenarm());
        assertEquals(external.getRichtung(), q.getRichtung());
    }

    @Test
    void testSetAdditionalDataToZeitintervall_Verkehrsbeziehung_Kreuzung() {
        // Arrange: Zeitintervall, Zaehlung und ExternalVerkehrsbeziehungDTO (Kreuzung)
        final var zeitintervall = new Zeitintervall();
        zeitintervall.setStartUhrzeit(LocalDateTime.now());
        zeitintervall.setEndeUhrzeit(LocalDateTime.now().plusHours(1));

        final var zaehlung = new Zaehlung();
        final var zaehlungId = UUID.randomUUID().toString();
        zaehlung.setId(zaehlungId);
        // Wähle eine Zaehlart, die nicht FJS und nicht QU ist (z.B. "N")
        zaehlung.setZaehlart("N");
        zaehlung.setZaehldauer("24h");

        final var external = new ExternalVerkehrsbeziehungDTO();
        final var bewegungsbeziehungId = UUID.randomUUID().toString();
        external.setId(bewegungsbeziehungId);
        // Kreuzungspfad
        external.setIsKreuzung(Boolean.TRUE);
        external.setVon(1);
        external.setNach(2);
        external.setStrassenseite(null);

        // Stub createHochrechnung
        final var hochrechnung = new Hochrechnung();
        doReturn(hochrechnung).when(service).createHochrechnung(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());

        // Act
        final var result = service.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, external);

        // Assert
        assertNotNull(result);
        assertEquals(UUID.fromString(zaehlungId), result.getZaehlungId());
        assertEquals(UUID.fromString(bewegungsbeziehungId), result.getBewegungsbeziehungId());
        assertSame(hochrechnung, result.getHochrechnung());

        final Verkehrsbeziehung v = result.getVerkehrsbeziehung();
        assertNotNull(v, "Verkehrsbeziehung sollte für Kreuzung gesetzt werden");
        assertEquals(external.getVon(), v.getVon());
        assertEquals(external.getNach(), v.getNach());
        assertEquals(external.getStrassenseite(), v.getStrassenseite());
    }

    @Test
    void testSetAdditionalDataToZeitintervall_Verkehrsbeziehung_Kreisverkehr_KeineFahrbewegung() {
        // Arrange: Zeitintervall, Zaehlung und ExternalVerkehrsbeziehungDTO (Kreisverkehr ohne Flags)
        final var zeitintervall = new Zeitintervall();
        zeitintervall.setStartUhrzeit(LocalDateTime.now());
        zeitintervall.setEndeUhrzeit(LocalDateTime.now().plusHours(1));

        final var zaehlung = new Zaehlung();
        final var zaehlungId = UUID.randomUUID().toString();
        zaehlung.setId(zaehlungId);
        // Wähle eine Zaehlart, die nicht FJS und nicht QU ist (z.B. "N")
        zaehlung.setZaehlart("N");
        zaehlung.setZaehldauer("24h");

        final var external = new ExternalVerkehrsbeziehungDTO();
        final var bewegungsbeziehungId = UUID.randomUUID().toString();
        external.setId(bewegungsbeziehungId);
        // Kreisverkehrspfad (isKreuzung=false), aber keine Hinein/Heraus/Vorbei Flags gesetzt
        external.setIsKreuzung(Boolean.FALSE);
        external.setKnotenarm(42);
        external.setHinein(Boolean.FALSE);
        external.setHeraus(Boolean.FALSE);
        external.setVorbei(Boolean.FALSE);

        // Stub createHochrechnung
        final var hochrechnung = new Hochrechnung();
        doReturn(hochrechnung).when(service).createHochrechnung(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());

        // Act
        final var result = service.setAdditionalDataToZeitintervall(zeitintervall, zaehlung, external);

        // Assert
        assertNotNull(result);
        assertEquals(UUID.fromString(zaehlungId), result.getZaehlungId());
        assertEquals(UUID.fromString(bewegungsbeziehungId), result.getBewegungsbeziehungId());
        assertSame(hochrechnung, result.getHochrechnung());

        final Verkehrsbeziehung v = result.getVerkehrsbeziehung();
        assertNotNull(v, "Verkehrsbeziehung sollte gesetzt werden (Knotenarm)");
        assertEquals(external.getKnotenarm(), v.getVon());
        // Da keine Fahrbewegung gesetzt wurde, sollte das Attribut fahrbewegungKreisverkehr null sein
        assertNull(v.getFahrbewegungKreisverkehr(), "Keine Fahrbewegung gesetzt -> FahrbewegungKreisverkehr sollte null sein");
    }

}
