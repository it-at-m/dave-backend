package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.enums.TagesTyp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

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

    long countAllByDatumGreaterThanEqualAndDatumLessThanEqualAndTagestypIn(final LocalDate startDateIncluded, final LocalDate endDateIncluded,
            final List<TagesTyp> tagestypen);

    Optional<Kalendertag> findByNextStartDateToLoadUnauffaelligeTageIsTrue();
}
