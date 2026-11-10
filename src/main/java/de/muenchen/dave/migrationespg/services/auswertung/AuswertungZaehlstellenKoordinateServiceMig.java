package de.muenchen.dave.migrationespg.services.auswertung;

import de.muenchen.dave.domain.dtos.laden.LadeAuswertungZaehlstelleKoordinateDTO;
import de.muenchen.dave.migrationespg.domain.mapper.ZaehlstelleMapperMig;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.migrationespg.services.ZaehlstelleIndexServiceMig;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuswertungZaehlstellenKoordinateServiceMig {

    private final ZaehlstelleIndexServiceMig indexService;

    private final ZaehlstelleMapperMig zaehlstelleMapperMig;

    public AuswertungZaehlstellenKoordinateServiceMig(final ZaehlstelleIndexServiceMig indexService,
                                                      final ZaehlstelleMapperMig zaehlstelleMapperMig) {
        this.indexService = indexService;
        this.zaehlstelleMapperMig = zaehlstelleMapperMig;
    }

    /**
     * @return ein {@link LadeAuswertungZaehlstelleKoordinateDTO} für jede vorhandene
     *         {@link ZaehlstelleEntity}
     */
    public List<LadeAuswertungZaehlstelleKoordinateDTO> getAuswertungZaehlstellenKoordinate() {
        return indexService.getAllZaehlstellen().stream()
                .map(zaehlstelleMapperMig::bean2LadeAuswertungZaehlstelleKoordinateDto)
                .sorted(Comparator.comparing(LadeAuswertungZaehlstelleKoordinateDTO::getNummer))
                .collect(Collectors.toList());
    }

}
