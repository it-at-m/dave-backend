package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.enums.TagesTyp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KalendertagRepository extends JpaRepository<Kalendertag, UUID> { //NOSONAR

    Optional<Kalendertag> findByDatum(final LocalDate datum);

    /**
     * Liefert eine Liste an Kalendertagen bis zum latestDate, ohne die excludedDates.
     *
     * @param excludedDates Liste an LocalDates, in denen das Datum nicht enthalten sein darf
     * @param latestDate bis zu diesem Datum soll gesucht werden
     * @return Liste an Kalendertagen
     */
    List<Kalendertag> findAllByDatumNotInAndDatumIsBefore(final List<LocalDate> excludedDates, final LocalDate latestDate);

    long countAllByDatumGreaterThanEqualAndDatumLessThanAndTagestypIn(final LocalDate startDateIncluded, final LocalDate endDateExcluded,
            final List<TagesTyp> tagestypen);
}
