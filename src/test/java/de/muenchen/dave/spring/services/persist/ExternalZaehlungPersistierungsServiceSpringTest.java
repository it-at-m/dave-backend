package de.muenchen.dave.spring.services.persist;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.dtos.bearbeiten.UpdateStatusDTO;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlungDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.ZaehlstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.elasticsearch.ZaehlungRandomFactory;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.exceptions.PlausibilityException;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.services.persist.ExternalZaehlungPersistierungsService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
@Slf4j
class ExternalZaehlungPersistierungsServiceSpringTest {

    @MockitoBean
    private CustomSuggestIndex customSuggestIndex;

    @MockitoBean
    private MessstelleIndex messstelleIndex;

    @MockitoBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @Autowired
    private ExternalZaehlungPersistierungsService externalZaehlungPersistierungsService;

    @Test
    public void getZaehlungenForExternal() throws BrokenInfrastructureException {

        final List<Zaehlstelle> indexResult = new ArrayList<>();

        Zaehlstelle zst = ZaehlstelleRandomFactory.getOne();
        zst.setNummer("120105");
        zst.setStadtbezirkNummer(12);
        zst.setZaehlungen(Arrays.asList(
                ZaehlungRandomFactory.getOne(),
                ZaehlungRandomFactory.getOne(),
                ZaehlungRandomFactory.getOne(),
                ZaehlungRandomFactory.getOne(),
                ZaehlungRandomFactory.getOne()));
        zst.getZaehlungen().get(0).setStatus(Status.INSTRUCTED.name());
        zst.getZaehlungen().get(1).setStatus(Status.ACCOMPLISHED.name());
        zst.getZaehlungen().get(2).setStatus(Status.CREATED.name());
        zst.getZaehlungen().get(3).setStatus(Status.ACTIVE.name());
        zst.getZaehlungen().get(4).setStatus(Status.INACTIVE.name());
        indexResult.add(zst);

        zst = ZaehlstelleRandomFactory.getOne();
        zst.setNummer("120107");
        zst.setStadtbezirkNummer(12);
        zst.setZaehlungen(List.of(ZaehlungRandomFactory.getOne()));
        zst.getZaehlungen().get(0).setStatus(Status.CORRECTION.name());
        indexResult.add(zst);

        zst = ZaehlstelleRandomFactory.getOne();
        zst.setNummer("120107");
        zst.setStadtbezirkNummer(12);
        zst.setZaehlungen(List.of(ZaehlungRandomFactory.getOne()));
        zst.getZaehlungen().get(0).setStatus(Status.COUNTING.name());
        indexResult.add(zst);

        when(this.zaehlstelleIndex.findAllByStatus(anyString(), any())).thenReturn(new PageImpl<>(indexResult));

        final List<ExternalZaehlungDTO> zaehlungenForExternal = this.externalZaehlungPersistierungsService
                .getZaehlungenForExternal(zst.getZaehlungen().get(0).getDienstleisterkennung(), false);
        assertThat(zaehlungenForExternal.size(), is(3));
        assertThat(zaehlungenForExternal.get(0).getStatus(), is(Status.INSTRUCTED.name()));
        assertThat(zaehlungenForExternal.get(1).getStatus(), is(Status.CORRECTION.name()));
        assertThat(zaehlungenForExternal.get(2).getStatus(), is(Status.COUNTING.name()));
    }

    @Test
    public void updateStatus() throws BrokenInfrastructureException, DataNotFoundException, PlausibilityException {
        final Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        zaehlstelle.setNummer("120105");
        zaehlstelle.setStadtbezirkNummer(12);
        final var zaehlungen = new ArrayList<Zaehlung>();
        zaehlungen.add(ZaehlungRandomFactory.getOne());
        zaehlstelle.setZaehlungen(zaehlungen);
        zaehlstelle.getZaehlungen().get(0).setStatus(Status.INSTRUCTED.name());

        final UpdateStatusDTO updateStatusDTO = new UpdateStatusDTO();
        updateStatusDTO.setStatus(Status.ACTIVE.name());
        updateStatusDTO.setZaehlungId(zaehlstelle.getZaehlungen().get(0).getId());

        when(this.zaehlstelleIndex.findByZaehlungenId(anyString())).thenReturn(Optional.of(zaehlstelle));

        when(this.zaehlstelleIndex.save(zaehlstelle)).thenReturn(zaehlstelle);

        this.externalZaehlungPersistierungsService.updateStatus(updateStatusDTO);

        assertThat(zaehlstelle.getZaehlungen().get(0).getStatus(), is(Status.ACTIVE.name()));
    }

}
