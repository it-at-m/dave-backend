package de.muenchen.dave.services.messstelle;

import de.muenchen.dave.domain.UnauffaelligerTag;
import de.muenchen.dave.domain.enums.TagesTyp;
import de.muenchen.dave.repositories.relationaldb.UnauffaelligeTageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnauffaelligeTageService {

    private final UnauffaelligeTageRepository unauffaelligeTageRepository;

    public List<UnauffaelligerTag> getUnauffaelligeTageForMessstelle(final String mstId) {
        return unauffaelligeTageRepository.findByMstId(mstId);
    }

    public long countAllUnauffaelligetageByMstIdAndTimerangeAndTagestypen(
            final String mstId,
            final LocalDate startDateIncluded,
            final LocalDate endDateIncluded,
            final List<TagesTyp> tagesTyp) {
        return unauffaelligeTageRepository.countAllByMstIdAndKalendertagDatumGreaterThanEqualAndKalendertagDatumLessThanEqualAndKalendertagTagestypIn(
                mstId,
                startDateIncluded,
                endDateIncluded,
                tagesTyp);
    }

}
