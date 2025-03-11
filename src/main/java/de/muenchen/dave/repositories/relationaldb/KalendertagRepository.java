package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.Kalendertag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KalendertagRepository extends JpaRepository<Kalendertag, UUID> { //NOSONAR

    Optional<Kalendertag> findByDatum(final LocalDate datum);

    List<Kalendertag> findAllByDatumNotInAndDatumIsBefore(final List<LocalDate> excludedDates, final LocalDate latestDate);
}
