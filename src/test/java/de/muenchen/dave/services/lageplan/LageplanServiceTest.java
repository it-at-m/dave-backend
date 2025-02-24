package de.muenchen.dave.services.lageplan;

import de.muenchen.dave.documentstorage.gen.api.LageplanApi;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@Slf4j
class LageplanServiceTest {

    @Mock
    private LageplanApi lageplanApi;

    private LageplanService lageplanService;

    @BeforeEach
    void setup() {
        lageplanService = new LageplanService(lageplanApi);
    }

    @Test
    void testLageplanVorhanden_WithVorhanden() {

        String mstId = "9999";
        Mockito.when(lageplanApi.lageplanExistsWithHttpInfo(mstId)).thenReturn(Mono.just(ResponseEntity.of(Optional.of(true))));
        Boolean result = lageplanService.lageplanVorhanden(mstId);

        Assertions.assertThat(result)
                .isNotNull()
                .isEqualTo(true);
    }

        @Test
        void testLageplanVorhanden_WithNichtVorhanden() {

            String mstId = "9999";
            Mockito.when(lageplanApi.lageplanExistsWithHttpInfo(mstId)).thenReturn(Mono.just(ResponseEntity.of(Optional.of(false))));
            Boolean result = lageplanService.lageplanVorhanden(mstId);

            Assertions.assertThat(result)
                    .isNotNull()
                    .isEqualTo(false);
        }

        @Test
        void testLageplanVorhanden_WithException() {

            String mstId = "9999";
            Mockito.when(lageplanApi.lageplanExistsWithHttpInfo(mstId)).thenReturn(Mono.just(new ResponseEntity<>(null, HttpStatus.OK)));

            assertThrows(ResourceNotFoundException.class, () -> lageplanService.lageplanVorhanden(mstId));
        }
}
