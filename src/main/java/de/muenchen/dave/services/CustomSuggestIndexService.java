package de.muenchen.dave.services;

import de.muenchen.dave.configuration.LogExecutionTime;
import de.muenchen.dave.domain.elasticsearch.CustomSuggest;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class CustomSuggestIndexService {

    private static final int DEFAULT_WEIGHT = 100;
    private static final String SUCHWORT = "suchwort";
    private final CustomSuggestIndex customSuggestIndex;

    /**
     * Erzeugt für eine konkrete Zählstelle ein Set an Vorschlägen.
     *
     * @param zaehlstelle neue zu verschlagwortende Zaehlstelle
     */
    public void createSuggestionsForZaehlstelle(final Zaehlstelle zaehlstelle) {
        final String suggestId = zaehlstelle.getId();
        // In den Suchwörtern stehen alle Suggestions für die Zählstelle
        if (CollectionUtils.isNotEmpty(zaehlstelle.getSuchwoerter())) {
            this.createSuggestionsFromSuchwoerter(zaehlstelle.getSuchwoerter(), suggestId);
        }
    }

    /**
     * Erzeugt für eine konkrete Zählung ein Set an Vorschlägen.
     *
     * @param zaehlung neue zu verschlagwortende Zaehlung
     */
    public void createSuggestionsForZaehlung(final Zaehlung zaehlung) {
        final String suggestId = zaehlung.getId();
        // In den Suchwörtern stehen alle Suggestions für die Zählung
        this.createSuggestionsFromSuchwoerter(zaehlung.getSuchwoerter(), suggestId);
    }

    /**
     * Erzeugt für eine konkrete Messstelle ein Set an Vorschlägen.
     *
     * @param messstelle neue zu verschlagwortende Messstelle
     */
    public void createSuggestionsForMessstelle(final Messstelle messstelle) {
        final String suggestId = messstelle.getId();
        // In den Suchwörtern stehen alle Suggestions für die Zählstelle
        if (CollectionUtils.isNotEmpty(messstelle.getSuchwoerter())) {
            this.createSuggestionsFromSuchwoerter(messstelle.getSuchwoerter(), suggestId);
        }
    }

    /**
     * Aktualisiert für eine konkrete Zählung ein Set an Vorschlägen.
     *
     * @param zaehlung neue zu verschlagwortende Zaehlung
     */
    public void updateSuggestionsForZaehlung(final Zaehlung zaehlung) {
        this.deleteAllSuggestionsByFkid(zaehlung.getId());
        this.createSuggestionsForZaehlung(zaehlung);
    }

    /**
     * Aktualistiert für eine konkrete Zählung ein Set an Vorschlägen.
     *
     * @param zaehlstelle neue zu verschlagwortende Zaehlung
     */
    @LogExecutionTime
    public void updateSuggestionsForZaehlstelle(final Zaehlstelle zaehlstelle) {
        this.deleteAllSuggestionsByFkid(zaehlstelle.getId());
        this.createSuggestionsForZaehlstelle(zaehlstelle);
    }

    /**
     * Aktualisiert für eine konkrete Messstelle ein Set an Vorschlägen.
     *
     * @param messstelle neue zu verschlagwortende Messstelle
     */
    public void updateSuggestionsForMessstelle(final Messstelle messstelle) {
        this.deleteAllSuggestionsByFkid(messstelle.getId());
        this.createSuggestionsForMessstelle(messstelle);
    }

    /**
     * Löscht alle Vorschläge zu einem bestimmen Fremdschlüssel.
     *
     * @param suggestFkid Fremdschlüssel
     */
    @LogExecutionTime
    public void deleteAllSuggestionsByFkid(final String suggestFkid) {
        this.customSuggestIndex.deleteAllByFkid(suggestFkid);
    }

    /**
     * Erzeugt für die Suchwörter ein Set an Vorschlägen.
     *
     * @param suchwoerter Suchwörter einer Zählstelle oder Zählung
     * @param suggestId ID der zugehörigen Zählstelle oder Zählung
     */
    @LogExecutionTime
    private void createSuggestionsFromSuchwoerter(final List<String> suchwoerter, final String suggestId) {
        if (CollectionUtils.isNotEmpty(suchwoerter)) {
            final List<CustomSuggest> suggestionList = new ArrayList<>();
            final Set<String> suchwoerterAsSet = new HashSet<>(suchwoerter);
            suchwoerterAsSet.forEach(element -> {
                final Completion completion = new Completion(new String[] { element });
                completion.setWeight(DEFAULT_WEIGHT);
                suggestionList.add(new CustomSuggest(UUID.randomUUID().toString(), SUCHWORT, suggestId, completion));
            });
            this.customSuggestIndex.saveAll(suggestionList);
        }

    }
}
