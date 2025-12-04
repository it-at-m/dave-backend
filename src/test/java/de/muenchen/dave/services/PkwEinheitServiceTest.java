package de.muenchen.dave.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;

import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.dtos.PkwEinheitDTO;
import de.muenchen.dave.domain.mapper.PkwEinheitMapperImpl;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.PkwEinheitRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PkwEinheitServiceTest {

    private final PkwEinheitService pkwEinheitService;

    private final PkwEinheitRepository pkwEinheitRepository;

    public PkwEinheitServiceTest() {
        this.pkwEinheitRepository = Mockito.mock(PkwEinheitRepository.class);
        this.pkwEinheitService = new PkwEinheitService(
                this.pkwEinheitRepository,
                new PkwEinheitMapperImpl());

    }

    @Test
    public void savePkwEinheit() {
        final PkwEinheitDTO pkwEinheitDTO = new PkwEinheitDTO();
        pkwEinheitDTO.setPkw(BigDecimal.valueOf(1));
        pkwEinheitDTO.setLkw(BigDecimal.valueOf(2));
        pkwEinheitDTO.setLastzuege(BigDecimal.valueOf(3));
        pkwEinheitDTO.setBusse(BigDecimal.valueOf(4));
        pkwEinheitDTO.setKraftraeder(BigDecimal.valueOf(5));
        pkwEinheitDTO.setFahrradfahrer(BigDecimal.valueOf(6));

        final PkwEinheit pkwEinheit = new PkwEinheitMapperImpl().bearbeiteDto2entity(pkwEinheitDTO);
        Mockito.when(pkwEinheitRepository.saveAndFlush(any())).thenReturn(pkwEinheit);
        PkwEinheitDTO result = pkwEinheitService.savePkwEinheit(pkwEinheitDTO);
        assertThat(result, is(pkwEinheitDTO));
    }

    @Test
    public void getLatestPkwEinheiten() throws DataNotFoundException {
        final PkwEinheit pkwEinheit = new PkwEinheit();
        pkwEinheit.setPkw(BigDecimal.valueOf(1));
        pkwEinheit.setLkw(BigDecimal.valueOf(2));
        pkwEinheit.setLastzuege(BigDecimal.valueOf(3));
        pkwEinheit.setBusse(BigDecimal.valueOf(4));
        pkwEinheit.setKraftraeder(BigDecimal.valueOf(5));
        pkwEinheit.setFahrradfahrer(BigDecimal.valueOf(6));

        Mockito.when(pkwEinheitRepository.findTopByOrderByCreatedTimeDesc()).thenReturn(Optional.of(pkwEinheit));

        final PkwEinheitDTO expected = new PkwEinheitDTO();
        expected.setPkw(BigDecimal.valueOf(1));
        expected.setLkw(BigDecimal.valueOf(2));
        expected.setLastzuege(BigDecimal.valueOf(3));
        expected.setBusse(BigDecimal.valueOf(4));
        expected.setKraftraeder(BigDecimal.valueOf(5));
        expected.setFahrradfahrer(BigDecimal.valueOf(6));

        PkwEinheitDTO result = pkwEinheitService.getLatestPkwEinheiten();
        assertThat(result, is(expected));
    }

    @Test
    public void getLatestPkwEinheitenDataNotFoundException() {
        Mockito.when(pkwEinheitRepository.findTopByOrderByCreatedTimeDesc()).thenReturn(Optional.empty());
        Assertions.assertThrows(DataNotFoundException.class, pkwEinheitService::getLatestPkwEinheiten);
    }

}
