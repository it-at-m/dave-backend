package de.muenchen.dave.configuration;

import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = DaveBackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE })
class SecuringWebAppTest {

    @Autowired
    MockMvc api;

    @MockBean
    MessstelleIndex messstelleIndex;

    @MockBean
    CustomSuggestIndex customSuggestIndex;

    @MockBean
    ZaehlstelleIndex zaehlstelleIndex;

    @Test
    void accessUnsecuredResourceLadeAuswertungSpitzenstundeThenOk() throws Exception {
        api.perform(get("/lade-auswertung-spitzenstunde"))
                .andExpect(status().isOk());
    }
    @Test
    void accessUnsecuredResourceLadeAuswertungZaehlstellenKoordinateThenOk() throws Exception {
        api.perform(get("/lade-auswertung-zaehlstellen-koordinate"))
                .andExpect(status().isOk());
    }
    @Test
    void accessSecuredResourceRootThenUnauthorized() throws Exception {
        api.perform(get("/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessSecuredResourceActuatorThenUnauthorized() throws Exception {
        api.perform(get("/actuator"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessUnsecuredResourceActuatorHealthThenOk() throws Exception {
        api.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void accessUnsecuredResourceActuatorInfoThenOk() throws Exception {
        api.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    void accessUnsecuredResourceActuatorMetricsThenOk() throws Exception {
        api.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
    }

    @Test
    void accessUnsecuredResourceV3ApiDocsThenOk() throws Exception {
        api.perform(get("/h2-console/**"))
                .andExpect(status().isOk());
    }

}
