package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.StadtbezirkEntity;
import de.muenchen.dave.domain.StadtbezirksviertelEntity;
import de.muenchen.dave.domain.dtos.GebietseinheitenDTO;
import de.muenchen.dave.domain.dtos.StadtbezirkDTO;
import de.muenchen.dave.domain.dtos.StadtbezirksviertelDTO;
import de.muenchen.dave.domain.dtos.init.ConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.TenantConfigurationDTO;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.StadtbezirksviertelMapper;
import de.muenchen.dave.repositories.relationaldb.StadtbezirkRepository;
import de.muenchen.dave.repositories.relationaldb.StadtbezirksviertelRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StadtbezirkServiceTest {

    private StadtbezirkRepository stadtbezirkRepository;
    private StadtbezirksviertelRepository stadtbezirksviertelRepository;
    private StadtbezirkMapper stadtbezirkMapper;
    private StadtbezirksviertelMapper stadtbezirksviertelMapper;
    private StadtbezirkService stadtbezirkService;
    private TenantConfigurationDTO tenantConfiguration;

    @BeforeEach
    void setUp() {
        stadtbezirkRepository = mock(StadtbezirkRepository.class);
        stadtbezirksviertelRepository = mock(StadtbezirksviertelRepository.class);
        stadtbezirkMapper = mock(StadtbezirkMapper.class);
        stadtbezirksviertelMapper = mock(StadtbezirksviertelMapper.class);
        ConfigurationService configurationService = mock(ConfigurationService.class);
        tenantConfiguration = new TenantConfigurationDTO("", null, null);
        when(configurationService.getConfiguration()).thenReturn(new ConfigurationDTO(null, tenantConfiguration));
        stadtbezirkService = new StadtbezirkService(stadtbezirkRepository, stadtbezirksviertelRepository, stadtbezirkMapper, stadtbezirksviertelMapper,
                configurationService);
    }

    @Test
    void testGetStadtbezirkByStadt_returnsMappedStadtbezirke() {
        final String stadt = "München";
        final List<StadtbezirkEntity> entities = List.of(new StadtbezirkEntity());
        final List<StadtbezirkDTO> expected = List.of(new StadtbezirkDTO());
        when(stadtbezirkRepository.findByStadt(stadt)).thenReturn(entities);
        when(stadtbezirkMapper.entities2Dtos(entities)).thenReturn(expected);
        tenantConfiguration.setCity(stadt);

        final List<StadtbezirkDTO> result = stadtbezirkService.getStadtbezirkByStadt(stadt);

        assertThat(result, sameInstance(expected));
        verify(stadtbezirkRepository).findByStadt(stadt);
        verify(stadtbezirkMapper).entities2Dtos(entities);
    }

    @Test
    void testGetGebietseinheiten_returnsMappedGebietseinheiten() {
        final String stadt = "München";
        final List<StadtbezirkEntity> stadtbezirkEntities = List.of(new StadtbezirkEntity());
        final List<StadtbezirkDTO> stadtbezirkDtos = List.of(new StadtbezirkDTO());
        final List<StadtbezirksviertelEntity> stadtbezirksviertelEntities = List.of(new StadtbezirksviertelEntity());
        final List<StadtbezirksviertelDTO> stadtbezirksviertelDtos = List.of(new StadtbezirksviertelDTO());
        when(stadtbezirkRepository.findByStadt(stadt)).thenReturn(stadtbezirkEntities);
        when(stadtbezirkMapper.entities2Dtos(stadtbezirkEntities)).thenReturn(stadtbezirkDtos);
        when(stadtbezirksviertelRepository.findAllByStadtbezirk_Stadt(stadt)).thenReturn(stadtbezirksviertelEntities);
        when(stadtbezirksviertelMapper.entities2Dtos(stadtbezirksviertelEntities)).thenReturn(stadtbezirksviertelDtos);
        tenantConfiguration.setCity(stadt);

        final GebietseinheitenDTO result = stadtbezirkService.getGebietseinheiten();

        assertThat(result.getStadtbezirke(), sameInstance(stadtbezirkDtos));
        assertThat(result.getStadtbezirksviertel(), sameInstance(stadtbezirksviertelDtos));
        verify(stadtbezirkRepository).findByStadt(stadt);
        verify(stadtbezirkMapper).entities2Dtos(stadtbezirkEntities);
        verify(stadtbezirksviertelRepository).findAllByStadtbezirk_Stadt(stadt);
        verify(stadtbezirksviertelMapper).entities2Dtos(stadtbezirksviertelEntities);
    }

    @Test
    void testGetGebietseinheiten_cityNotConfigured() {
        final String stadt = "München";
        final List<StadtbezirkEntity> stadtbezirkEntities = List.of(new StadtbezirkEntity());
        final List<StadtbezirkDTO> stadtbezirkDtos = List.of(new StadtbezirkDTO());
        final List<StadtbezirksviertelEntity> stadtbezirksviertelEntities = List.of(new StadtbezirksviertelEntity());
        final List<StadtbezirksviertelDTO> stadtbezirksviertelDtos = List.of(new StadtbezirksviertelDTO());
        when(stadtbezirkRepository.findByStadt(stadt)).thenReturn(stadtbezirkEntities);
        when(stadtbezirkMapper.entities2Dtos(stadtbezirkEntities)).thenReturn(stadtbezirkDtos);
        when(stadtbezirksviertelRepository.findAllByStadtbezirk_Stadt(stadt)).thenReturn(stadtbezirksviertelEntities);
        when(stadtbezirksviertelMapper.entities2Dtos(stadtbezirksviertelEntities)).thenReturn(stadtbezirksviertelDtos);
        tenantConfiguration.setCity("");

        GebietseinheitenDTO result = stadtbezirkService.getGebietseinheiten();

        assertTrue(result.getStadtbezirke().isEmpty());
        assertTrue(result.getStadtbezirksviertel().isEmpty());

        verify(stadtbezirkRepository).findByStadt("");
        verify(stadtbezirksviertelRepository).findAllByStadtbezirk_Stadt("");

    }

    @Test
    void testGetStadtbezirkByStadtAndNummer_returnsMappedStadtbezirk() {
        final String stadt = "München";
        final Integer nummer = 1;
        final StadtbezirkEntity entity = new StadtbezirkEntity();
        final StadtbezirkDTO expected = new StadtbezirkDTO();
        when(stadtbezirkRepository.findByStadtAndNummer(stadt, nummer)).thenReturn(entity);
        when(stadtbezirkMapper.entity2Dto(entity)).thenReturn(expected);

        final StadtbezirkDTO result = stadtbezirkService.getStadtbezirkByStadtAndNummer(stadt, nummer);

        assertThat(result, sameInstance(expected));
        verify(stadtbezirkRepository).findByStadtAndNummer(stadt, nummer);
        verify(stadtbezirkMapper).entity2Dto(entity);
    }

    @Test
    void testGetStadtbezirkName_stadtbezirkExists() {
        final Integer nummer = 1;
        final StadtbezirkEntity entity = new StadtbezirkEntity();
        final StadtbezirkDTO stadtbezirk = new StadtbezirkDTO();
        stadtbezirk.setName("Altstadt-Lehel");
        when(stadtbezirkRepository.findByStadtAndNummer("München", nummer)).thenReturn(entity);
        when(stadtbezirkMapper.entity2Dto(entity)).thenReturn(stadtbezirk);
        tenantConfiguration.setCity("München");

        assertThat(stadtbezirkService.getStadtbezirkName(nummer), is("Altstadt-Lehel"));
        verify(stadtbezirkRepository).findByStadtAndNummer("München", nummer);
    }

    @Test
    void testGetStadtbezirkName_stadtbezirkDoesNotExist() {
        tenantConfiguration.setCity("München");
        when(stadtbezirkRepository.findByStadtAndNummer("München", 99)).thenReturn(null);
        when(stadtbezirkMapper.entity2Dto(null)).thenReturn(null);

        assertThat(stadtbezirkService.getStadtbezirkName(99), is("Unbekannt"));
        verify(stadtbezirkRepository).findByStadtAndNummer("München", 99);
    }

}
