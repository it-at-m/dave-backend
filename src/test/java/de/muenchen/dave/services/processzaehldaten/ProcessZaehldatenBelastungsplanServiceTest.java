package de.muenchen.dave.services.processzaehldaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.AbstractLadeBelastungsplanDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanDataDTO;
import de.muenchen.dave.domain.dtos.laden.BelastungsplanQJSDataDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanDTO;
import de.muenchen.dave.domain.elasticsearch.PkwEinheit;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.ZaehlstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.elasticsearch.ZaehlungRandomFactory;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.domain.enums.ZaehldatenIntervall;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import de.muenchen.dave.services.ladezaehldaten.LadeZaehldatenService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessZaehldatenBelastungsplanServiceTest {

    private static final Random random = new Random(System.currentTimeMillis());

    @Mock
    private ZeitintervallRepository zeitintervallRepository;
    @Mock
    private ZaehlstelleIndex zaehlstelleIndex;
    @Mock
    private LadeZaehldatenService ladeZaehldatenService;

    private ProcessZaehldatenBelastungsplanService service;

    @BeforeEach
    public void beforeEach() throws IllegalAccessException {
        service = new ProcessZaehldatenBelastungsplanService(zeitintervallRepository, zaehlstelleIndex, ladeZaehldatenService);
        Mockito.reset(zeitintervallRepository, zaehlstelleIndex, ladeZaehldatenService);
    }

    /**
     * Testet, ob die KFZ-Zähldaten hierarchisch über RAD-Daten eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithKfzAndRadData() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.KFZ, Fahrzeug.RAD));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.KFZ, Fahrzeug.RAD));
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.N,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), List.of(Fahrzeug.KFZ, Fahrzeug.RAD))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("KFZ", ((BelastungsplanQJSDataDTO) dto.getValue1()).getLabel());
    }

    /**
     * Testet, ob die KFZ-Zähldaten hierarchisch über FUSS-Daten eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithKfzAndFussData() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.KFZ, Fahrzeug.FUSS));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.KFZ, Fahrzeug.FUSS));
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.N,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), List.of(Fahrzeug.KFZ, Fahrzeug.FUSS))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("KFZ", ((BelastungsplanDataDTO) dto.getValue1()).getLabel());
    }

    /**
     * Testet, ob die RAD-Zähldaten hierarchisch über FUSS-Daten eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithRadAndFussData() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.RAD, Fahrzeug.FUSS));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.N,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), List.of(Fahrzeug.RAD, Fahrzeug.FUSS))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("RAD", ((BelastungsplanDataDTO) dto.getValue1()).getLabel());
    }

    /**
     * Testet, ob die FUSS-Zähldaten allein hierarchisch richtig eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithFussOnlyData() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.FUSS));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.FUSS));
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.N,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), List.of(Fahrzeug.FUSS))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("FUSS", ((BelastungsplanQJSDataDTO) dto.getValue1()).getLabel());
    }

    /**
     * Testet, ob die FUSS-Zähldaten allein bei QJS hierarchisch richtig eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithFussDataQJS() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.FUSS));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.QJS.name());
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.QJS,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervalle1(zaehlung.getId(), List.of(Fahrzeug.FUSS))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("FUSS", ((BelastungsplanQJSDataDTO) dto.getValue1()).getLabel());
    }

    /**
     * Testet, ob die RAD- und FUSS-Zähldaten bei QJS hierarchisch richtig eingeordnet werden.
     */
    @Test
    public void testLadeProcessedZaehldatenBelastungsplanWithRadAndFussDataQJS() throws DataNotFoundException {
        OptionsDTO options = createTestOptions(List.of(Fahrzeug.RAD));
        Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        Zaehlung zaehlung = createTestZaehlung(List.of(Fahrzeug.RAD, Fahrzeug.FUSS));
        zaehlung.setZaehlart(Zaehlart.QJS.name());
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstelleIndex.findByZaehlungenId(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));
        when(ladeZaehldatenService.extractZeitintervalle(
                UUID.fromString(zaehlung.getId()),
                Zaehlart.QJS,
                options.getZeitblock().getStart(),
                options.getZeitblock().getEnd(),
                options,
                false,
                Set.of(TypeZeitintervall.STUNDE_VIERTEL)))
                .thenReturn(List.of(
                        createTestZeitintervalle1(zaehlung.getId(), List.of(Fahrzeug.RAD, Fahrzeug.FUSS))));

        AbstractLadeBelastungsplanDTO<?> dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("RAD", ((BelastungsplanQJSDataDTO) dto.getValue1()).getLabel());
        assertTrue((((BelastungsplanQJSDataDTO) dto.getValue2()).getLabel()).isEmpty());
    }

    /**
     * Testet die Abzweigung für den Differenzdatenvergleich.
     */
    @Test
    void testGetBelastungsplanDTOWhenDifferenzFalse() throws Exception {
        ProcessZaehldatenBelastungsplanService serviceSpy = Mockito.spy(service);
        final OptionsDTO options = new OptionsDTO();
        options.setDifferenzdatenDarstellen(false);

        final LadeBelastungsplanDTO expected = new LadeBelastungsplanDTO();

        doReturn(expected).when(serviceSpy).ladeProcessedZaehldatenBelastungsplan(anyString(), any(OptionsDTO.class));

        final var result = serviceSpy.getBelastungsplanDTO("zaehlung-1", options);

        assertSame(expected, result);
        verify(serviceSpy, times(1)).ladeProcessedZaehldatenBelastungsplan(eq("zaehlung-1"), eq(options));
        verify(serviceSpy, never()).getDifferenzdatenBelastungsplanDTO(anyString(), any(OptionsDTO.class));
    }

    /**
     * Testet die Abzweigung für Einzelzähldaten.
     */
    @Test
    void testGetBelastungsplanDTOWhenDifferenzTrue() throws Exception {
        ProcessZaehldatenBelastungsplanService serviceSpy = Mockito.spy(service);
        final OptionsDTO options = new OptionsDTO();
        options.setDifferenzdatenDarstellen(true);
        options.setVergleichszaehlungsId("vergleich-1");

        final LadeBelastungsplanDTO expected = new LadeBelastungsplanDTO();

        doReturn(expected).when(serviceSpy).getDifferenzdatenBelastungsplanDTO(anyString(), any(OptionsDTO.class));

        final var result = serviceSpy.getBelastungsplanDTO("zaehlung-1", options);

        assertSame(expected, result);
        verify(serviceSpy, times(1)).getDifferenzdatenBelastungsplanDTO(eq("zaehlung-1"), eq(options));
        verify(serviceSpy, never()).ladeProcessedZaehldatenBelastungsplan(anyString(), any(OptionsDTO.class));
    }

    private Zeitintervall createTestZeitintervall(final String zaehlungId, final List<Fahrzeug> fahrzeuge) {
        Zeitintervall zeitintervall = new Zeitintervall();
        if (fahrzeuge.contains(Fahrzeug.PKW))
            zeitintervall.setPkw(random.nextInt());
        if (fahrzeuge.contains(Fahrzeug.RAD))
            zeitintervall.setFahrradfahrer(random.nextInt());
        if (fahrzeuge.contains(Fahrzeug.FUSS))
            zeitintervall.setFussgaenger(random.nextInt());

        zeitintervall.setStartUhrzeit(LocalDateTime.now());
        zeitintervall.setEndeUhrzeit(LocalDateTime.now().plusMinutes(15));
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setZaehlungId(UUID.fromString(zaehlungId));
        Verkehrsbeziehung vb = new Verkehrsbeziehung();
        vb.setVon(1);
        vb.setNach(3);
        zeitintervall.setVerkehrsbeziehung(vb);
        return zeitintervall;
    }

    private Zeitintervall createTestZeitintervalle1(final String zaehlungId, final List<Fahrzeug> fahrzeuge) {
        Zeitintervall zeitintervall = new Zeitintervall();
        if (fahrzeuge.contains(Fahrzeug.RAD))
            zeitintervall.setFahrradfahrer(random.nextInt());
        if (fahrzeuge.contains(Fahrzeug.FUSS))
            zeitintervall.setFussgaenger(random.nextInt());

        zeitintervall.setStartUhrzeit(LocalDateTime.now());
        zeitintervall.setEndeUhrzeit(LocalDateTime.now().plusMinutes(15));
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setZaehlungId(UUID.fromString(zaehlungId));
        Verkehrsbeziehung vb13N = new Verkehrsbeziehung();
        vb13N.setVon(1);
        vb13N.setNach(3);
        zeitintervall.setVerkehrsbeziehung(vb13N);
        return zeitintervall;
    }

    private Zaehlung createTestZaehlung(final List<Fahrzeug> fahrzeuge) {
        Zaehlung zaehlung = ZaehlungRandomFactory.getOne();
        zaehlung.setKreisverkehr(false);
        zaehlung.setKategorien(fahrzeuge);
        zaehlung.setZaehlart(Zaehlart.N.name());
        zaehlung.setPkwEinheit(new PkwEinheit());
        return zaehlung;
    }

    private OptionsDTO createTestOptions(final List<Fahrzeug> fahrzeuge) {
        OptionsDTO options = new OptionsDTO();
        options.setZeitblock(Zeitblock.ZB_06_19);
        options.setIntervall(ZaehldatenIntervall.STUNDE_VIERTEL);
        options.setGueterverkehrsanteilProzent(false);
        options.setFussverkehr(false);
        options.setRadverkehr(false);
        options.setKraftfahrzeugverkehr(false);
        options.setSchwerverkehr(false);
        options.setGueterverkehr(false);
        options.setLastzuege(false);
        options.setKraftraeder(false);
        options.setPersonenkraftwagen(false);
        options.setBusse(false);
        options.setSchwerverkehrsanteilProzent(false);
        if (fahrzeuge.contains(Fahrzeug.KFZ))
            options.setKraftfahrzeugverkehr(true);
        if (fahrzeuge.contains(Fahrzeug.RAD))
            options.setRadverkehr(true);
        if (fahrzeuge.contains(Fahrzeug.FUSS))
            options.setFussverkehr(true);
        return options;
    }

}
