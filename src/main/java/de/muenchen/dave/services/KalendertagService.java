package de.muenchen.dave.services;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.repositories.relationaldb.KalendertagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

    public long countAllKalendertageByDatumAndTagestyp(
            final LocalDate startDateIncluded,
            final LocalDate endDateExcluded,
            final List<TagesTyp> tagestypen) {
        return kalendertagRepository.countAllByDatumGreaterThanEqualAndDatumLessThanAndTagestypIn(startDateIncluded, endDateExcluded, tagestypen);
    }

}
