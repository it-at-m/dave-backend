package de.muenchen.dave.spring.services;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.dtos.BearbeiteZaehlstelleDTORandomFactory;
import de.muenchen.dave.domain.dtos.NextZaehlstellennummerDTO;
import de.muenchen.dave.domain.dtos.OpenZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.ZaehlstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.elasticsearch.ZaehlungRandomFactory;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.ZaehlstelleMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
@Slf4j
public class ZaehlstelleIndexServiceSpringTest {

    @Autowired
    private ZaehlstelleIndexService service;

    @MockBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @MockBean
    private MessstelleIndex messstelleIndex;

    @MockBean
    private CustomSuggestIndex customSuggestIndex;

    @MockBean
    private ZaehlstelleMapper zaehlstelleMapper;

    @MockBean
    private StadtbezirkMapper stadtbezirkMapper;

    @Test
    public void getOpenZaehlungen() throws BrokenInfrastructureException {
        final Zaehlstelle zaehlstelle1 = ZaehlstelleRandomFactory.getOne();
        zaehlstelle1.setZaehlungen(new ArrayList<>());
        final Zaehlung zaehlung1 = ZaehlungRandomFactory.getOne();
        zaehlung1.setStatus(Status.ACCOMPLISHED.name());
        zaehlstelle1.getZaehlungen().add(zaehlung1);

        final Zaehlstelle zaehlstelle2 = ZaehlstelleRandomFactory.getOne();
        zaehlstelle2.setZaehlungen(new ArrayList<>());
        final Zaehlung zaehlung2 = ZaehlungRandomFactory.getOne();
        zaehlung2.setStatus(Status.ACTIVE.name());
        zaehlstelle2.getZaehlungen().add(zaehlung2);

        final Zaehlstelle zaehlstelle3 = ZaehlstelleRandomFactory.getOne();
        zaehlstelle3.setZaehlungen(new ArrayList<>());
        final Zaehlung zaehlung3 = ZaehlungRandomFactory.getOne();
        zaehlung3.setStatus(Status.INSTRUCTED.name());
        zaehlstelle3.getZaehlungen().add(zaehlung3);

        final Page<Zaehlstelle> resultFindAllByStatus = new PageImpl<>(Arrays.asList(
                zaehlstelle1,
                zaehlstelle2,
                zaehlstelle3));
        when(zaehlstelleIndex.findAllByStatus(anyString(), any())).thenReturn(resultFindAllByStatus);

        final List<OpenZaehlungDTO> openZaehlungen = this.service.getOpenZaehlungen();

        assertThat(openZaehlungen.size(), is(2));

        openZaehlungen.forEach(openZaehlungDTO -> {
            if (openZaehlungDTO.getStatus().equalsIgnoreCase(zaehlung1.getStatus())) {
                assertThat(openZaehlungDTO.getProjektName(), is(zaehlung1.getProjektName()));
                assertThat(openZaehlungDTO.getProjektNummer(), is(zaehlung1.getProjektNummer()));
                assertThat(openZaehlungDTO.getZaehlstellenNummer(), is(zaehlstelle1.getNummer()));
            }
            if (openZaehlungDTO.getStatus().equalsIgnoreCase(zaehlung2.getStatus())) {
                assertThat(openZaehlungDTO.getProjektName(), is(zaehlung2.getProjektName()));
                assertThat(openZaehlungDTO.getProjektNummer(), is(zaehlung2.getProjektNummer()));
                assertThat(openZaehlungDTO.getZaehlstellenNummer(), is(zaehlstelle2.getNummer()));
            }
        });
    }

    @Test
    public void erstelleZaehlstelle() throws BrokenInfrastructureException {
        final BearbeiteZaehlstelleDTO dto = BearbeiteZaehlstelleDTORandomFactory.getOne();
        final Zaehlstelle zaehlstelle1 = ZaehlstelleRandomFactory.getOne();

        when(stadtbezirkMapper.bezeichnungOf(any())).thenReturn("Schwabing");
        when(zaehlstelleMapper.bearbeiteDto2bean(dto, stadtbezirkMapper)).thenReturn(zaehlstelle1);
        when(zaehlstelleIndex.save(any())).thenReturn(zaehlstelle1);

        String id = this.service.erstelleZaehlstelle(dto);

        assertThat(id, is(notNullValue()));
        assertThat(id.length(), is(36));
    }

    @Test
    public void getNextZaehlstellennummer() {
        final Zaehlstelle zaehlstelle1 = ZaehlstelleRandomFactory.getOne();
        zaehlstelle1.setNummer("120105");
        zaehlstelle1.setStadtbezirkNummer(12);
        when(zaehlstelleIndex.findAllByNummerStartsWithAndStadtbezirkNummer("1201", 12)).thenReturn(List.of(zaehlstelle1));
        final NextZaehlstellennummerDTO nextCurrentNumber = this.service.getNextZaehlstellennummer("1201", 12);
        assertThat(nextCurrentNumber, is(notNullValue()));
        assertThat(nextCurrentNumber.getNummer(), is(equalTo("120106")));
    }

}
