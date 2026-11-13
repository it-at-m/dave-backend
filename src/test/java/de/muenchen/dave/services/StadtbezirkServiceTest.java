package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.StadtbezirkEntity;
import de.muenchen.dave.domain.StadtbezirkViertelEntity;
import de.muenchen.dave.domain.dtos.StadtbezirkAndViertelDTO;
import de.muenchen.dave.domain.dtos.StadtbezirkDTO;
import de.muenchen.dave.domain.dtos.StadtbezirkViertelDTO;
import de.muenchen.dave.domain.dtos.init.ConfigurationDTO;
import de.muenchen.dave.domain.dtos.init.TenantConfigurationDTO;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.StadtbezirkViertelMapper;
import de.muenchen.dave.repositories.relationaldb.StadtbezirkRepository;
import de.muenchen.dave.repositories.relationaldb.StadtbezirkViertelRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StadtbezirkServiceTest {

    private StadtbezirkRepository stadtbezirkRepository;
    private StadtbezirkViertelRepository stadtbezirkViertelRepository;
    private StadtbezirkMapper stadtbezirkMapper;
    private StadtbezirkViertelMapper stadtbezirkViertelMapper;
    private StadtbezirkService stadtbezirkService;
    private TenantConfigurationDTO tenantConfiguration;

    @BeforeEach
    void setUp() {
        stadtbezirkRepository = mock(StadtbezirkRepository.class);
        stadtbezirkViertelRepository = mock(StadtbezirkViertelRepository.class);
        stadtbezirkMapper = mock(StadtbezirkMapper.class);
        stadtbezirkViertelMapper = mock(StadtbezirkViertelMapper.class);
        ConfigurationService configurationService = mock(ConfigurationService.class);
        tenantConfiguration = new TenantConfigurationDTO("", null, null);
        when(configurationService.getConfiguration()).thenReturn(new ConfigurationDTO(null, tenantConfiguration));
        stadtbezirkService = new StadtbezirkService(stadtbezirkRepository, stadtbezirkViertelRepository, stadtbezirkMapper, stadtbezirkViertelMapper,
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
    void testGetStadtbezirkeAndViertel_returnsMappedStadtbezirkeAndViertel() {
        final String stadt = "München";
        final List<StadtbezirkEntity> stadtbezirkEntities = List.of(new StadtbezirkEntity());
        final List<StadtbezirkDTO> stadtbezirkDtos = List.of(new StadtbezirkDTO());
        final List<StadtbezirkViertelEntity> stadtbezirkViertelEntities = List.of(new StadtbezirkViertelEntity());
        final List<StadtbezirkViertelDTO> stadtbezirkViertelDtos = List.of(new StadtbezirkViertelDTO());
        when(stadtbezirkRepository.findByStadt(stadt)).thenReturn(stadtbezirkEntities);
        when(stadtbezirkMapper.entities2Dtos(stadtbezirkEntities)).thenReturn(stadtbezirkDtos);
        when(stadtbezirkViertelRepository.findAllByStadtbezirk_Stadt(stadt)).thenReturn(stadtbezirkViertelEntities);
        when(stadtbezirkViertelMapper.entities2Dtos(stadtbezirkViertelEntities)).thenReturn(stadtbezirkViertelDtos);
        tenantConfiguration.setCity(stadt);

        final StadtbezirkAndViertelDTO result = stadtbezirkService.getStadtbezirkeAndViertel();

        assertThat(result.getStadtbezirke(), sameInstance(stadtbezirkDtos));
        assertThat(result.getStadtbezirkViertel(), sameInstance(stadtbezirkViertelDtos));
        verify(stadtbezirkRepository).findByStadt(stadt);
        verify(stadtbezirkMapper).entities2Dtos(stadtbezirkEntities);
        verify(stadtbezirkViertelRepository).findAllByStadtbezirk_Stadt(stadt);
        verify(stadtbezirkViertelMapper).entities2Dtos(stadtbezirkViertelEntities);
    }

    @Test
    void testGetStadtbezirkAndViertel_cityNotConfigured() {
        final String stadt = "München";
        final List<StadtbezirkEntity> stadtbezirkEntities = List.of(new StadtbezirkEntity());
        final List<StadtbezirkDTO> stadtbezirkDtos = List.of(new StadtbezirkDTO());
        final List<StadtbezirkViertelEntity> stadtbezirkViertelEntities = List.of(new StadtbezirkViertelEntity());
        final List<StadtbezirkViertelDTO> stadtbezirkViertelDtos = List.of(new StadtbezirkViertelDTO());
        when(stadtbezirkRepository.findByStadt(stadt)).thenReturn(stadtbezirkEntities);
        when(stadtbezirkMapper.entities2Dtos(stadtbezirkEntities)).thenReturn(stadtbezirkDtos);
        when(stadtbezirkViertelRepository.findAllByStadtbezirk_Stadt(stadt)).thenReturn(stadtbezirkViertelEntities);
        when(stadtbezirkViertelMapper.entities2Dtos(stadtbezirkViertelEntities)).thenReturn(stadtbezirkViertelDtos);
        tenantConfiguration.setCity("");

        StadtbezirkAndViertelDTO result = stadtbezirkService.getStadtbezirkeAndViertel();

        assertTrue(result.getStadtbezirke().isEmpty());
        assertTrue(result.getStadtbezirkViertel().isEmpty());

        verify(stadtbezirkRepository).findByStadt("");
        verify(stadtbezirkViertelRepository).findAllByStadtbezirk_Stadt("");

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
