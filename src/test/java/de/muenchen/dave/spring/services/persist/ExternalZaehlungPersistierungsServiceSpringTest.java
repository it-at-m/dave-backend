package de.muenchen.dave.spring.services.persist;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.dtos.external.ExternalZaehlungDTO;
import de.muenchen.dave.domain.elasticsearch.ZaehlstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.ZaehlungRandomFactory;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.relational.repository.MessstellenIndexRepository;
import de.muenchen.dave.relational.repository.ZaehlstellenIndexRepository;
import de.muenchen.dave.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.services.persist.ExternalZaehlungPersistierungsService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
@Slf4j
class ExternalZaehlungPersistierungsServiceSpringTest {

    @MockitoBean
    private MessstellenIndexRepository messstelleIndex;

    @MockitoBean
    private ZaehlstellenIndexRepository zaehlstelleIndex;

    @Autowired
    private ExternalZaehlungPersistierungsService externalZaehlungPersistierungsService;

    @Test
    public void getZaehlungenForExternal() throws BrokenInfrastructureException {

        final List<ZaehlstelleEntity> indexResult = new ArrayList<>();

        ZaehlstelleEntity zst = ZaehlstelleRandomFactory.getOne();
        zst.setNummer("120105");
        zst.setStadtbezirkNummer(12);
        zst.setZaehlungen(Arrays.asList(
                ZaehlungRandomFactory.getOne(),
                ZaehlungRandomFactory.getOne(),
                ZaehlungRandomFactory.getOne(),
                ZaehlungRandomFactory.getOne(),
                ZaehlungRandomFactory.getOne()));
        zst.getZaehlungen().get(0).setStatus(Status.INSTRUCTED);
        zst.getZaehlungen().get(1).setStatus(Status.ACCOMPLISHED);
        zst.getZaehlungen().get(2).setStatus(Status.CREATED);
        zst.getZaehlungen().get(3).setStatus(Status.ACTIVE);
        zst.getZaehlungen().get(4).setStatus(Status.INACTIVE);
        indexResult.add(zst);

        zst = ZaehlstelleRandomFactory.getOne();
        zst.setNummer("120107");
        zst.setStadtbezirkNummer(12);
        zst.setZaehlungen(List.of(ZaehlungRandomFactory.getOne()));
        zst.getZaehlungen().get(0).setStatus(Status.CORRECTION);
        indexResult.add(zst);

        zst = ZaehlstelleRandomFactory.getOne();
        zst.setNummer("120107");
        zst.setStadtbezirkNummer(12);
        zst.setZaehlungen(List.of(ZaehlungRandomFactory.getOne()));
        zst.getZaehlungen().get(0).setStatus(Status.COUNTING);
        indexResult.add(zst);

        when(this.zaehlstelleIndex.findAllByStatus(anyString(), any())).thenReturn(new PageImpl<>(indexResult));

        final List<ExternalZaehlungDTO> zaehlungenForExternal = this.externalZaehlungPersistierungsService
                .getZaehlungenForExternal(zst.getZaehlungen().get(0).getDienstleisterkennung(), false);
        assertThat(zaehlungenForExternal.size(), is(3));
        assertThat(zaehlungenForExternal.get(0).getStatus(), is(Status.INSTRUCTED.name()));
        assertThat(zaehlungenForExternal.get(1).getStatus(), is(Status.CORRECTION.name()));
        assertThat(zaehlungenForExternal.get(2).getStatus(), is(Status.COUNTING.name()));
    }

}
