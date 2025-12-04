package de.muenchen.dave.services.lageplan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.muenchen.dave.documentstorage.gen.api.LageplanApi;
import de.muenchen.dave.documentstorage.gen.model.DocumentDto;
import de.muenchen.dave.exceptions.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
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
        Mockito.when(lageplanApi.lageplanExistsWithHttpInfo(mstId)).thenReturn(
                Mono.just(ResponseEntity.of(Optional.of(true))));
        Boolean result = lageplanService.lageplanVorhanden(mstId);

        assertThat(result, is(true));
    }

    @Test
    void testLageplanVorhanden_WithNichtVorhanden() {

        String mstId = "9999";
        Mockito.when(lageplanApi.lageplanExistsWithHttpInfo(mstId)).thenReturn(
                Mono.just(ResponseEntity.of(Optional.of(false))));
        Boolean result = lageplanService.lageplanVorhanden(mstId);

        assertThat(result, is(false));
    }

    @Test
    void testLageplanVorhanden_WithException() {

        String mstId = "9999";
        Mockito.when(lageplanApi.lageplanExistsWithHttpInfo(mstId)).thenReturn(
                Mono.just(new ResponseEntity<>(null, HttpStatus.OK)));

        assertThrows(ResourceNotFoundException.class, () -> lageplanService.lageplanVorhanden(mstId));
    }

    @Test
    void testLadeLageplan_WithVorhanden() {

        String mstId = "9999";
        String url = "http://s3k.muenchen.de/test.pdf";
        DocumentDto dto = new DocumentDto();
        dto.setUrl(url);
        Mockito.when(lageplanApi.getLageplanWithHttpInfo(mstId)).thenReturn(
                Mono.just(ResponseEntity.of(Optional.of(dto))));
        DocumentDto result = lageplanService.ladeLageplan(mstId);

        DocumentDto expected = new DocumentDto();
        expected.setUrl(url);

        assertThat(result, is(expected));
    }

    @Test
    void testLadeLageplan_WithNichtVorhanden() {

        String mstId = "9999";
        Mockito.when(lageplanApi.getLageplanWithHttpInfo(mstId)).thenReturn(
                Mono.just(new ResponseEntity<>(null, HttpStatus.OK)));

        assertThrows(ResourceNotFoundException.class, () -> lageplanService.ladeLageplan(mstId));
    }

    @Test
    void testLadeLageplan_WithException() {

        String mstId = "9999";
        Mockito.when(lageplanApi.getLageplanWithHttpInfo(mstId)).thenReturn(
                Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));

        assertThrows(ResourceNotFoundException.class, () -> lageplanService.ladeLageplan(mstId));
    }
}
