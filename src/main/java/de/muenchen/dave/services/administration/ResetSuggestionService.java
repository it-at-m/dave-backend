package de.muenchen.dave.services.administration;

import de.muenchen.dave.domain.enums.ErhebungsstelleType;
import de.muenchen.dave.services.CustomSuggestIndexService;
import de.muenchen.dave.services.ZaehlstelleIndexService;
import de.muenchen.dave.services.messstelle.MessstelleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResetSuggestionService {

    private final CustomSuggestIndexService customSuggestIndexService;
    private final ZaehlstelleIndexService zaehlstelleIndexService;
    private final MessstelleService messstelleService;

    @Transactional
    public void resetAllSuggestions() {
        customSuggestIndexService.deleteAll();
        generateSuggestionsForAllZaehlstellen();
        generateSuggestionsForAllMessstellen();
    }

    @Transactional
    public void resetSuggestionsOfAllZaehlstellen() {
        customSuggestIndexService.deleteByErhebungsstelleType(ErhebungsstelleType.ZAEHLSTELLE);
        generateSuggestionsForAllZaehlstellen();
    }

    @Transactional
    public void resetSuggestionsOfAllMessstellen() {
        customSuggestIndexService.deleteByErhebungsstelleType(ErhebungsstelleType.MESSSTELLE);
        generateSuggestionsForAllMessstellen();
    }

    private void generateSuggestionsForAllZaehlstellen() {
        zaehlstelleIndexService.generateSuggestionsForAllZaehlstellenAndZaehlungen();
    }

    private void generateSuggestionsForAllMessstellen() {
        messstelleService.generateSuggestionsForAllMessstelle();
    }
}
