package de.muenchen.dave.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.relational.messtellen.MessstelleEntity;
import de.muenchen.dave.domain.relational.zaehlstellen.KnotenarmEntity;
import de.muenchen.dave.domain.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.domain.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.repositories.relationaldb.ZaehlstellenIndexRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SuchwoerterNeuServiceTest {

    private SuchwoerterNeuService service;

    private ZaehlstellenIndexRepository zaehlstellenIndexRepository;

    @BeforeEach
    void setUp() {
        zaehlstellenIndexRepository = Mockito.mock(ZaehlstellenIndexRepository.class);
        service = new SuchwoerterNeuService(zaehlstellenIndexRepository);
    }

    @Test
    void testReassignSuchwoerterNeu_onZaehlstelleEntity() {
        final KnotenarmEntity knotenarm = new KnotenarmEntity();
        knotenarm.setStrassenname("Straße");

        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setProjektName("Projekt");
        zaehlung.setProjektNummer("P1");
        zaehlung.setKreuzungsname("Kreuzung");
        zaehlung.setKommentar("Zählungskommentar");
        zaehlung.setZaehlsituation("Situation");
        zaehlung.setZaehlsituationErweitert("Erweiterte Situation");
        zaehlung.setKnotenarme(List.of(knotenarm));

        final ZaehlstelleEntity zaehlstelle = new ZaehlstelleEntity();
        zaehlstelle.setNummer("123");
        zaehlstelle.setKommentar("Stellenkommentar");
        zaehlstelle.setSuchwoerter(List.of("Suchwort"));
        zaehlstelle.setCustomSuchwoerter(List.of("nicht übernehmen"));
        zaehlstelle.setZaehlungen(List.of(zaehlung));

        service.reassignSuchwoerterNeu(zaehlstelle);

        assertEquals(
                "123 Stellenkommentar Suchwort Projekt P1 Kreuzung Zählungskommentar Situation Erweiterte Situation Straße",
                zaehlstelle.getSuchwoerterNeu());
    }

    @Test
    void testReassignSuchwoerterNeu_onMessstelleEntity() {
        final MessstelleEntity messstelle = new MessstelleEntity();
        messstelle.setMstId("MST-1");
        messstelle.setName("Messstellenname");

        service.reassignSuchwoerterNeu(messstelle);

        assertEquals("MST-1 Messstellenname", messstelle.getSuchwoerterNeu());
    }

    @Test
    void testReassignSuchwoerterNeu_onZaehlungEntity() {
        final ZaehlungEntity zaehlung = new ZaehlungEntity();
        zaehlung.setId(UUID.randomUUID());
        zaehlung.setProjektName("Aktualisiertes Projekt");

        final ZaehlstelleEntity zaehlstelle = new ZaehlstelleEntity();
        zaehlstelle.setNummer("123");
        zaehlstelle.setZaehlungen(List.of(zaehlung));
        when(zaehlstellenIndexRepository.findByZaehlungen_Id(zaehlung.getId())).thenReturn(Optional.of(zaehlstelle));

        service.reassignSuchwoerterNeu(zaehlung);

        assertEquals("123 Aktualisiertes Projekt", zaehlstelle.getSuchwoerterNeu());
        verify(zaehlstellenIndexRepository).save(zaehlstelle);
    }
}
