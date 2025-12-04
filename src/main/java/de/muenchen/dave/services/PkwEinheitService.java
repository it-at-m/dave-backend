package de.muenchen.dave.services;

import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.domain.dtos.PkwEinheitDTO;
import de.muenchen.dave.domain.mapper.PkwEinheitMapper;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.PkwEinheitRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PkwEinheitService {

    private final PkwEinheitRepository pkwEinheitRepository;

    private final PkwEinheitMapper pkwEinheitMapper;

    public PkwEinheitService(final PkwEinheitRepository pkwEinheitRepository,
            final PkwEinheitMapper pkwEinheitMapper) {
        this.pkwEinheitRepository = pkwEinheitRepository;
        this.pkwEinheitMapper = pkwEinheitMapper;
    }

    /**
     * Diese Methode speichert eine {@link PkwEinheit} in der Relationalen Datenbank.
     *
     * @param pkwEinheitDto zu speicherndes DTO
     * @return die gespeicherte {@link PkwEinheit} als {@link PkwEinheitDTO}.
     */
    public PkwEinheitDTO savePkwEinheit(final PkwEinheitDTO pkwEinheitDto) {
        PkwEinheit pkwEinheit = pkwEinheitMapper.bearbeiteDto2entity(pkwEinheitDto);
        pkwEinheit = pkwEinheitRepository.saveAndFlush(pkwEinheit);
        return pkwEinheitMapper.entity2bearbeiteDto(pkwEinheit);
    }

    /**
     * Diese Methode extrahiert die zuletzt gespeicherte {@link PkwEinheit} und gibt diese zurück.
     *
     * @return die zuletzte gespeicherte {@link PkwEinheit} als {@link PkwEinheitDTO}.
     * @throws DataNotFoundException Bei ladefehlern
     */
    public PkwEinheitDTO getLatestPkwEinheiten() throws DataNotFoundException {
        final Optional<PkwEinheit> pkwEinheitOptional = pkwEinheitRepository.findTopByOrderByCreatedTimeDesc();
        return pkwEinheitOptional
                .map(pkwEinheitMapper::entity2bearbeiteDto)
                .orElseThrow(() -> new DataNotFoundException("Keine PkwEinheit gefunden."));
    }

}
