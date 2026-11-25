package de.muenchen.dave.services.auswertung;

import de.muenchen.dave.domain.dtos.laden.LadeAuswertungZaehlstelleKoordinateDto;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.mapper.ZaehlstelleMapper;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuswertungZaehlstellenKoordinateService {

    private final ZaehlstelleIndexService indexService;

    private final ZaehlstelleMapper zaehlstelleMapper;

    public AuswertungZaehlstellenKoordinateService(final ZaehlstelleIndexService indexService,
            final ZaehlstelleMapper zaehlstelleMapper) {
        this.indexService = indexService;
        this.zaehlstelleMapper = zaehlstelleMapper;
    }

    /**
     * @return ein {@link LadeAuswertungZaehlstelleKoordinateDto} für jede vorhandene
     *         {@link Zaehlstelle}
     */
    public List<LadeAuswertungZaehlstelleKoordinateDto> getAuswertungZaehlstellenKoordinate() {
        return indexService.getAllZaehlstellen().stream()
                .map(zaehlstelleMapper::bean2LadeAuswertungZaehlstelleKoordinateDto)
                .sorted(Comparator.comparing(LadeAuswertungZaehlstelleKoordinateDto::getNummer))
                .collect(Collectors.toList());
    }

}
