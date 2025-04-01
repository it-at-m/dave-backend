package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.enums.TagesTyp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnauffaelligeTageRepository extends JpaRepository<UnauffaelligerTag, UUID> {

    List<UnauffaelligerTag> findByMstId(final String mstId);

    /**
     * @return den jüngsten unauffälligen Tag bezogen auf den referenzierten Kalendertag.
     */
    Optional<UnauffaelligerTag> findTopByOrderByKalendertagDatumDesc();

    long countAllByMstIdAndKalendertagDatumGreaterThanEqualAndKalendertagDatumLessThanAndKalendertagTagestypIn(final String mstId,
            final LocalDate startDateIncluded,
            final LocalDate endDateExcluded, final List<TagesTyp> tagesTyp);
}
