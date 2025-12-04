package de.muenchen.dave.services;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.repositories.relationaldb.KalendertagRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KalendertagService {

    private final KalendertagRepository kalendertagRepository;

    /**
     * Liefert eine Liste an Kalendertagen bis zum latestDate, ohne die excludedDates.
     *
     * @param excludedDates Liste an LocalDates, deren Kalendertag nicht benötigt werden.
     * @param latestDate bis zu diesem Datum soll gesucht werden
     * @return Liste an Kalendertagen
     */
    public List<Kalendertag> getAllKalendertageWhereDatumNotInExcludedDatesAndDatumIsBeforeLatestDate(
            final List<LocalDate> excludedDates,
            final LocalDate latestDate) {
        return kalendertagRepository.findAllByDatumNotInAndDatumIsBefore(excludedDates, latestDate);
    }

    public long countAllKalendertageByDatumAndTagestypen(
            final LocalDate startDateIncluded,
            final LocalDate endDateIncluded,
            final List<TagesTyp> tagestypen) {
        return kalendertagRepository.countAllByDatumGreaterThanEqualAndDatumLessThanEqualAndTagestypIn(startDateIncluded, endDateIncluded, tagestypen);
    }

}
