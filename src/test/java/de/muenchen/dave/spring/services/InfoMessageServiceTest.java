package de.muenchen.dave.spring.services;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.domain.InfoMessage;
import de.muenchen.dave.domain.dtos.InfoMessageDTO;
import de.muenchen.dave.domain.relationaldb.InfoMessageRandomFactory;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.repositories.relationaldb.InfoMessageRepository;
import de.muenchen.dave.services.InfoMessageService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
@Slf4j
public class InfoMessageServiceTest {

    @MockitoBean
    private ZaehlstelleIndex zaehlstelleIndex;

    @MockitoBean
    private MessstelleIndex messstelleIndex;

    @MockitoBean
    private CustomSuggestIndex customSuggestIndex;

    @Autowired
    private InfoMessageService infoMessageService;

    @Autowired
    private InfoMessageRepository infoMessageRepository;

    @BeforeEach
    void init() {
        infoMessageRepository.deleteAll();
    }

    @AfterEach
    void teardown() {
        infoMessageRepository.deleteAll();
    }

    @Test
    public void saveInfoMessage() {
        final var infoMessages = new ArrayList<InfoMessage>();
        final var infoMessageAktiv = InfoMessageRandomFactory.getOneAktiv();
        infoMessageAktiv.setContent("infoMessageAktiv");
        infoMessages.add(infoMessageAktiv);
        final var infoMessageInaktiv1 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv1.setContent("infoMessageInaktiv1");
        infoMessages.add(infoMessageInaktiv1);
        final var infoMessageInaktiv2 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv2.setContent("infoMessageInaktiv2");
        infoMessages.add(infoMessageInaktiv2);
        final var infoMessageInaktiv3 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv3.setContent("infoMessageInaktiv3");
        infoMessages.add(infoMessageInaktiv3);
        final var infoMessageInaktiv4 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv4.setContent("infoMessageInaktiv4");
        infoMessages.add(infoMessageInaktiv4);
        final var infoMessageInaktiv5 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv5.setContent("infoMessageInaktiv5");
        infoMessages.add(infoMessageInaktiv5);
        final var infoMessageInaktiv6 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv6.setContent("infoMessageInaktiv6");
        infoMessages.add(infoMessageInaktiv6);
        final var infoMessageInaktiv7 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv7.setContent("infoMessageInaktiv7");
        infoMessages.add(infoMessageInaktiv7);
        final var infoMessageInaktiv8 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv8.setContent("infoMessageInaktiv8");
        infoMessages.add(infoMessageInaktiv8);
        final var infoMessageInaktiv9 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv9.setContent("infoMessageInaktiv9");
        infoMessages.add(infoMessageInaktiv9);
        final var infoMessageInaktiv10 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv10.setContent("infoMessageInaktiv10");
        infoMessages.add(infoMessageInaktiv10);

        saveInReverseOrder(infoMessages);

        assertThat(infoMessageRepository.findAll().size(), is(11));

        final var infoMessageDto = new InfoMessageDTO();
        infoMessageDto.setContent("XXX");
        infoMessageDto.setGueltigVon(LocalDate.now());
        infoMessageDto.setGueltigBis(infoMessageDto.getGueltigVon().plusDays(1));
        infoMessageDto.setAktiv(true);

        List<InfoMessageDTO> result = infoMessageService.saveInfoMessage(infoMessageDto);

        assertThat(infoMessageRepository.findAll().size(), is(11));
        assertThat(result.size(), is(11));

        assertThat(result.get(0).getContent(), is("XXX"));
        assertThat(result.get(1).getContent(), is("infoMessageAktiv"));
        assertThat(result.get(2).getContent(), is("infoMessageInaktiv1"));
        assertThat(result.get(3).getContent(), is("infoMessageInaktiv2"));
        assertThat(result.get(4).getContent(), is("infoMessageInaktiv3"));
        assertThat(result.get(5).getContent(), is("infoMessageInaktiv4"));
        assertThat(result.get(6).getContent(), is("infoMessageInaktiv5"));
        assertThat(result.get(7).getContent(), is("infoMessageInaktiv6"));
        assertThat(result.get(8).getContent(), is("infoMessageInaktiv7"));
        assertThat(result.get(9).getContent(), is("infoMessageInaktiv8"));
        assertThat(result.get(10).getContent(), is("infoMessageInaktiv9"));
    }

    @Test
    public void loadActiveInfoMessage() {
        final var infoMessages = new ArrayList<InfoMessage>();
        final var infoMessageAktiv1 = InfoMessageRandomFactory.getOneAktiv();
        infoMessageAktiv1.setContent("infoMessageAktiv1");
        infoMessages.add(infoMessageAktiv1);
        final var infoMessageAktiv2 = InfoMessageRandomFactory.getOneAktiv();
        infoMessageAktiv2.setContent("infoMessageAktiv2");
        infoMessages.add(infoMessageAktiv2);
        final var infoMessageInaktiv1 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv1.setContent("infoMessageInaktiv1");
        infoMessages.add(infoMessageInaktiv1);
        final var infoMessageInaktiv2 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv2.setContent("infoMessageInaktiv2");
        infoMessages.add(infoMessageInaktiv2);
        final var infoMessageInaktiv3 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv3.setContent("infoMessageInaktiv3");
        infoMessages.add(infoMessageInaktiv3);
        final var infoMessageInaktiv4 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv4.setContent("infoMessageInaktiv4");
        infoMessages.add(infoMessageInaktiv4);
        final var infoMessageInaktiv5 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv5.setContent("infoMessageInaktiv5");
        infoMessages.add(infoMessageInaktiv5);
        final var infoMessageInaktiv6 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv6.setContent("infoMessageInaktiv6");
        infoMessages.add(infoMessageInaktiv6);
        final var infoMessageInaktiv7 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv7.setContent("infoMessageInaktiv7");
        infoMessages.add(infoMessageInaktiv7);
        final var infoMessageInaktiv8 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv8.setContent("infoMessageInaktiv8");
        infoMessages.add(infoMessageInaktiv8);
        final var infoMessageInaktiv9 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv9.setContent("infoMessageInaktiv9");
        infoMessages.add(infoMessageInaktiv9);
        final var infoMessageInaktiv10 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv10.setContent("infoMessageInaktiv10");
        infoMessages.add(infoMessageInaktiv10);

        saveInReverseOrder(infoMessages);

        assertThat(infoMessageRepository.findAll().size(), is(12));

        final var result = infoMessageService.loadActiveInfoMessage();

        assertThat(result.getContent(), is("infoMessageAktiv1"));
    }

    @Test
    public void loadAllInfoMessagesWithAktivMessage() {
        final var infoMessages = new ArrayList<InfoMessage>();
        final var infoMessageAktiv = InfoMessageRandomFactory.getOneAktiv();
        infoMessageAktiv.setContent("infoMessageAktiv");
        infoMessages.add(infoMessageAktiv);
        final var infoMessageInaktiv1 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv1.setContent("infoMessageInaktiv1");
        infoMessages.add(infoMessageInaktiv1);
        final var infoMessageInaktiv2 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv2.setContent("infoMessageInaktiv2");
        infoMessages.add(infoMessageInaktiv2);
        final var infoMessageInaktiv3 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv3.setContent("infoMessageInaktiv3");
        infoMessages.add(infoMessageInaktiv3);
        final var infoMessageInaktiv4 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv4.setContent("infoMessageInaktiv4");
        infoMessages.add(infoMessageInaktiv4);
        final var infoMessageInaktiv5 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv5.setContent("infoMessageInaktiv5");
        infoMessages.add(infoMessageInaktiv5);
        final var infoMessageInaktiv6 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv6.setContent("infoMessageInaktiv6");
        infoMessages.add(infoMessageInaktiv6);
        final var infoMessageInaktiv7 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv7.setContent("infoMessageInaktiv7");
        infoMessages.add(infoMessageInaktiv7);
        final var infoMessageInaktiv8 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv8.setContent("infoMessageInaktiv8");
        infoMessages.add(infoMessageInaktiv8);
        final var infoMessageInaktiv9 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv9.setContent("infoMessageInaktiv9");
        infoMessages.add(infoMessageInaktiv9);
        final var infoMessageInaktiv10 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv10.setContent("infoMessageInaktiv10");
        infoMessages.add(infoMessageInaktiv10);

        saveInReverseOrder(infoMessages);

        assertThat(infoMessageRepository.findAll().size(), is(11));

        List<InfoMessageDTO> result = infoMessageService.loadAllInfoMessages();

        assertThat(result.size(), is(11));

        assertThat(result.get(0).getContent(), is("infoMessageAktiv"));
        assertThat(result.get(1).getContent(), is("infoMessageInaktiv1"));
        assertThat(result.get(2).getContent(), is("infoMessageInaktiv2"));
        assertThat(result.get(3).getContent(), is("infoMessageInaktiv3"));
        assertThat(result.get(4).getContent(), is("infoMessageInaktiv4"));
        assertThat(result.get(5).getContent(), is("infoMessageInaktiv5"));
        assertThat(result.get(6).getContent(), is("infoMessageInaktiv6"));
        assertThat(result.get(7).getContent(), is("infoMessageInaktiv7"));
        assertThat(result.get(8).getContent(), is("infoMessageInaktiv8"));
        assertThat(result.get(9).getContent(), is("infoMessageInaktiv9"));
        assertThat(result.get(10).getContent(), is("infoMessageInaktiv10"));
    }

    @Test
    public void loadAllInfoMessagesWithInaktivMessage() {
        final var infoMessages = new ArrayList<InfoMessage>();
        final var infoMessageInaktiv1 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv1.setContent("infoMessageInaktiv1");
        infoMessages.add(infoMessageInaktiv1);
        final var infoMessageInaktiv2 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv2.setContent("infoMessageInaktiv2");
        infoMessages.add(infoMessageInaktiv2);
        final var infoMessageInaktiv3 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv3.setContent("infoMessageInaktiv3");
        infoMessages.add(infoMessageInaktiv3);
        final var infoMessageInaktiv4 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv4.setContent("infoMessageInaktiv4");
        infoMessages.add(infoMessageInaktiv4);
        final var infoMessageInaktiv5 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv5.setContent("infoMessageInaktiv5");
        infoMessages.add(infoMessageInaktiv5);
        final var infoMessageInaktiv6 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv6.setContent("infoMessageInaktiv6");
        infoMessages.add(infoMessageInaktiv6);
        final var infoMessageInaktiv7 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv7.setContent("infoMessageInaktiv7");
        infoMessages.add(infoMessageInaktiv7);
        final var infoMessageInaktiv8 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv8.setContent("infoMessageInaktiv8");
        infoMessages.add(infoMessageInaktiv8);
        final var infoMessageInaktiv9 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv9.setContent("infoMessageInaktiv9");
        infoMessages.add(infoMessageInaktiv9);
        final var infoMessageInaktiv10 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv10.setContent("infoMessageInaktiv10");
        infoMessages.add(infoMessageInaktiv10);

        saveInReverseOrder(infoMessages);

        assertThat(infoMessageRepository.findAll().size(), is(10));

        List<InfoMessageDTO> result = infoMessageService.loadAllInfoMessages();

        assertThat(result.size(), is(11));

        assertThat(result.get(0).getContent(), is(nullValue()));
        assertThat(result.get(1).getContent(), is("infoMessageInaktiv1"));
        assertThat(result.get(2).getContent(), is("infoMessageInaktiv2"));
        assertThat(result.get(3).getContent(), is("infoMessageInaktiv3"));
        assertThat(result.get(4).getContent(), is("infoMessageInaktiv4"));
        assertThat(result.get(5).getContent(), is("infoMessageInaktiv5"));
        assertThat(result.get(6).getContent(), is("infoMessageInaktiv6"));
        assertThat(result.get(7).getContent(), is("infoMessageInaktiv7"));
        assertThat(result.get(8).getContent(), is("infoMessageInaktiv8"));
        assertThat(result.get(9).getContent(), is("infoMessageInaktiv9"));
        assertThat(result.get(10).getContent(), is("infoMessageInaktiv10"));

    }

    @Test
    public void setAllInfoMessagesInactiveAndDeleteInactiveExceptAllowedInfoMessages() {
        final var infoMessages = new ArrayList<InfoMessage>();
        final var infoMessageAktiv = InfoMessageRandomFactory.getOneAktiv();
        infoMessageAktiv.setContent("infoMessageAktiv");
        infoMessages.add(infoMessageAktiv);
        final var infoMessageInaktiv1 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv1.setContent("infoMessageInaktiv1");
        infoMessages.add(infoMessageInaktiv1);
        final var infoMessageInaktiv2 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv2.setContent("infoMessageInaktiv2");
        infoMessages.add(infoMessageInaktiv2);
        final var infoMessageInaktiv3 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv3.setContent("infoMessageInaktiv3");
        infoMessages.add(infoMessageInaktiv3);
        final var infoMessageInaktiv4 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv4.setContent("infoMessageInaktiv4");
        infoMessages.add(infoMessageInaktiv4);
        final var infoMessageInaktiv5 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv5.setContent("infoMessageInaktiv5");
        infoMessages.add(infoMessageInaktiv5);
        final var infoMessageInaktiv6 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv6.setContent("infoMessageInaktiv6");
        infoMessages.add(infoMessageInaktiv6);
        final var infoMessageInaktiv7 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv7.setContent("infoMessageInaktiv7");
        infoMessages.add(infoMessageInaktiv7);
        final var infoMessageInaktiv8 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv8.setContent("infoMessageInaktiv8");
        infoMessages.add(infoMessageInaktiv8);
        final var infoMessageInaktiv9 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv9.setContent("infoMessageInaktiv9");
        infoMessages.add(infoMessageInaktiv9);
        final var infoMessageInaktiv10 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv10.setContent("infoMessageInaktiv10");
        infoMessages.add(infoMessageInaktiv10);

        saveInReverseOrder(infoMessages);

        assertThat(infoMessageRepository.findAll().size(), is(11));

        List<InfoMessage> result = infoMessageRepository.findAllByOrderByCreatedTimeDesc();

        assertThat(result.get(0).getAktiv(), is(true));
        assertThat(result.get(1).getAktiv(), is(false));
        assertThat(result.get(2).getAktiv(), is(false));
        assertThat(result.get(3).getAktiv(), is(false));
        assertThat(result.get(4).getAktiv(), is(false));
        assertThat(result.get(5).getAktiv(), is(false));
        assertThat(result.get(6).getAktiv(), is(false));
        assertThat(result.get(7).getAktiv(), is(false));
        assertThat(result.get(8).getAktiv(), is(false));
        assertThat(result.get(9).getAktiv(), is(false));
        assertThat(result.get(10).getAktiv(), is(false));

        infoMessageService.setAllInfoMessagesInactiveAndDeleteInactiveExceptAllowedInfoMessages();

        assertThat(infoMessageRepository.findAll().size(), is(10));

        result = infoMessageRepository.findAllByOrderByCreatedTimeDesc();

        assertThat(result.get(0).getAktiv(), is(false));
        assertThat(result.get(0).getContent(), is("infoMessageAktiv"));
        assertThat(result.get(1).getAktiv(), is(false));
        assertThat(result.get(1).getContent(), is("infoMessageInaktiv1"));
        assertThat(result.get(2).getAktiv(), is(false));
        assertThat(result.get(2).getContent(), is("infoMessageInaktiv2"));
        assertThat(result.get(3).getAktiv(), is(false));
        assertThat(result.get(3).getContent(), is("infoMessageInaktiv3"));
        assertThat(result.get(4).getAktiv(), is(false));
        assertThat(result.get(4).getContent(), is("infoMessageInaktiv4"));
        assertThat(result.get(5).getAktiv(), is(false));
        assertThat(result.get(5).getContent(), is("infoMessageInaktiv5"));
        assertThat(result.get(6).getAktiv(), is(false));
        assertThat(result.get(6).getContent(), is("infoMessageInaktiv6"));
        assertThat(result.get(7).getAktiv(), is(false));
        assertThat(result.get(7).getContent(), is("infoMessageInaktiv7"));
        assertThat(result.get(8).getAktiv(), is(false));
        assertThat(result.get(8).getContent(), is("infoMessageInaktiv8"));
        assertThat(result.get(9).getAktiv(), is(false));
        assertThat(result.get(9).getContent(), is("infoMessageInaktiv9"));

    }

    @Test
    public void deleteInactiveInfoMessagesExceptAllowedInfoMessages() {
        final var infoMessages = new ArrayList<InfoMessage>();
        final var infoMessageAktiv = InfoMessageRandomFactory.getOneAktiv();
        infoMessageAktiv.setContent("infoMessageAktiv");
        infoMessages.add(infoMessageAktiv);
        final var infoMessageInaktiv1 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv1.setContent("infoMessageInaktiv1");
        infoMessages.add(infoMessageInaktiv1);
        final var infoMessageInaktiv2 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv2.setContent("infoMessageInaktiv2");
        infoMessages.add(infoMessageInaktiv2);
        final var infoMessageInaktiv3 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv3.setContent("infoMessageInaktiv3");
        infoMessages.add(infoMessageInaktiv3);
        final var infoMessageInaktiv4 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv4.setContent("infoMessageInaktiv4");
        infoMessages.add(infoMessageInaktiv4);
        final var infoMessageInaktiv5 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv5.setContent("infoMessageInaktiv5");
        infoMessages.add(infoMessageInaktiv5);
        final var infoMessageInaktiv6 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv6.setContent("infoMessageInaktiv6");
        infoMessages.add(infoMessageInaktiv6);
        final var infoMessageInaktiv7 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv7.setContent("infoMessageInaktiv7");
        infoMessages.add(infoMessageInaktiv7);
        final var infoMessageInaktiv8 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv8.setContent("infoMessageInaktiv8");
        infoMessages.add(infoMessageInaktiv8);
        final var infoMessageInaktiv9 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv9.setContent("infoMessageInaktiv9");
        infoMessages.add(infoMessageInaktiv9);
        final var infoMessageInaktiv10 = InfoMessageRandomFactory.getOneInaktiv();
        infoMessageInaktiv10.setContent("infoMessageInaktiv10");
        infoMessages.add(infoMessageInaktiv10);

        saveInReverseOrder(infoMessages);

        assertThat(infoMessageRepository.findAll().size(), is(11));

        infoMessageService.deleteInactiveInfoMessagesExceptAllowedInfoMessages();

        assertThat(infoMessageRepository.findAll().size(), is(10));

        List<InfoMessage> result = infoMessageRepository.findAllByOrderByCreatedTimeDesc();

        assertThat(result.get(0).getContent(), is("infoMessageAktiv"));
        assertThat(result.get(1).getContent(), is("infoMessageInaktiv1"));
        assertThat(result.get(2).getContent(), is("infoMessageInaktiv2"));
        assertThat(result.get(3).getContent(), is("infoMessageInaktiv3"));
        assertThat(result.get(4).getContent(), is("infoMessageInaktiv4"));
        assertThat(result.get(5).getContent(), is("infoMessageInaktiv5"));
        assertThat(result.get(6).getContent(), is("infoMessageInaktiv6"));
        assertThat(result.get(7).getContent(), is("infoMessageInaktiv7"));
        assertThat(result.get(8).getContent(), is("infoMessageInaktiv8"));
        assertThat(result.get(9).getContent(), is("infoMessageInaktiv9"));

    }

    private void saveInReverseOrder(final List<InfoMessage> infoMessages) {
        for (var index = infoMessages.size() - 1; index >= 0; index--) {
            infoMessageRepository.saveAndFlush(infoMessages.get(index));
            try {
                Thread.sleep(5);
            } catch (InterruptedException ie) {
                // do nothing
            }
        }
    }

}
