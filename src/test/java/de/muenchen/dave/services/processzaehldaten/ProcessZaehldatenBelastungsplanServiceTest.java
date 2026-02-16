package de.muenchen.dave.services.processzaehldaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.Verkehrsbeziehung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.OptionsDTO;
import de.muenchen.dave.domain.dtos.laden.LadeBelastungsplanDTO;
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
        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndTypeOrderBySortingIndexAsc(
                        UUID.fromString(zaehlung.getId()),
                        options.getZeitblock().getStart(),
                        options.getZeitblock().getEnd(),
                        options.getZeitblock().getTypeZeitintervall()))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), Fahrzeug.KFZ),
                        createTestZeitintervall(zaehlung.getId(), Fahrzeug.RAD)));

        LadeBelastungsplanDTO dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("KFZ", dto.getValue1().getLabel());
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
        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndTypeOrderBySortingIndexAsc(
                        UUID.fromString(zaehlung.getId()),
                        options.getZeitblock().getStart(),
                        options.getZeitblock().getEnd(),
                        options.getZeitblock().getTypeZeitintervall()))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), Fahrzeug.KFZ),
                        createTestZeitintervall(zaehlung.getId(), Fahrzeug.FUSS)));

        LadeBelastungsplanDTO dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("KFZ", dto.getValue1().getLabel());
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
        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndTypeOrderBySortingIndexAsc(
                        UUID.fromString(zaehlung.getId()),
                        options.getZeitblock().getStart(),
                        options.getZeitblock().getEnd(),
                        options.getZeitblock().getTypeZeitintervall()))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), Fahrzeug.RAD),
                        createTestZeitintervall(zaehlung.getId(), Fahrzeug.FUSS)));

        LadeBelastungsplanDTO dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("RAD", dto.getValue1().getLabel());
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
        when(zeitintervallRepository
                .findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndTypeOrderBySortingIndexAsc(
                        UUID.fromString(zaehlung.getId()),
                        options.getZeitblock().getStart(),
                        options.getZeitblock().getEnd(),
                        options.getZeitblock().getTypeZeitintervall()))
                .thenReturn(List.of(
                        createTestZeitintervall(zaehlung.getId(), Fahrzeug.FUSS)));

        LadeBelastungsplanDTO dto = service.ladeProcessedZaehldatenBelastungsplan(zaehlung.getId(), options);

        assertEquals("FUSS", dto.getValue1().getLabel());
    }

    private Zeitintervall createTestZeitintervall(final String zaehlungId, final Fahrzeug fahrzeug) {
        Zeitintervall zeitintervall = new Zeitintervall();
        if (fahrzeug == Fahrzeug.PKW)
            zeitintervall.setPkw(7);
        if (fahrzeug == Fahrzeug.RAD)
            zeitintervall.setFahrradfahrer(random.nextInt());
        if (fahrzeug == Fahrzeug.FUSS)
            zeitintervall.setFussgaenger(random.nextInt());

        zeitintervall.setStartUhrzeit(LocalDateTime.now());
        zeitintervall.setEndeUhrzeit(LocalDateTime.now().plusMinutes(15));
        zeitintervall.setType(TypeZeitintervall.STUNDE_VIERTEL);
        zeitintervall.setZaehlungId(UUID.fromString(zaehlungId));
        Verkehrsbeziehung vb = new Verkehrsbeziehung();
        vb.setVon(1);
        zeitintervall.setVerkehrsbeziehung(vb);
        return zeitintervall;
    }

    private Zaehlung createTestZaehlung(final List<Fahrzeug> fahrzeuge) {
        Zaehlung zaehlung = ZaehlungRandomFactory.getOne();
        zaehlung.setKreisverkehr(false);
        zaehlung.setKategorien(fahrzeuge);
        zaehlung.setZaehlart(Zaehlart.N.name());
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
