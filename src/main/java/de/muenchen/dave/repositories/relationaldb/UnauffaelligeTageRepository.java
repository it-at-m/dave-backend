package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.enums.TagesTyp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnauffaelligeTageRepository extends JpaRepository<UnauffaelligerTag, UUID> {

    List<UnauffaelligerTag> findByMstId(final String mstId);

    Optional<UnauffaelligerTag> findFirstByMstIdOrderByKalendertagDatumDesc(final String mstId);

    long countAllByMstIdAndKalendertagDatumGreaterThanEqualAndKalendertagDatumLessThanEqualAndKalendertagTagestypIn(
            final String mstId,
            final LocalDate startDateIncluded,
            final LocalDate endDateIncluded,
            final List<TagesTyp> tagesTyp);

    void deleteAllByKalendertagDatum(final LocalDate kalendertagDatum);
}
