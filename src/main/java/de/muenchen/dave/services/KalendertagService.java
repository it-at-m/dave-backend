package de.muenchen.dave.services;

import de.muenchen.dave.domain.Kalendertag;
import de.muenchen.dave.repositories.relationaldb.KalendertagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KalendertagService {

    private final KalendertagRepository kalendertagRepository;

    public List<Kalendertag> getAllKalendertageWhereDatumNotInAndDatumIsBefore(final List<LocalDate> notIn, final LocalDate beforeDate) {
        return kalendertagRepository.findAllByDatumNotInAndDatumIsBefore(notIn, beforeDate);
    }

}
