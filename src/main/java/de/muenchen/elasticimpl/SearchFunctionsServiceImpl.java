package de.muenchen.elasticimpl;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import de.muenchen.dave.domain.dtos.suche.SucheWordSuggestDTO;
import de.muenchen.dave.domain.elasticsearch.CustomSuggest;
import de.muenchen.dave.services.suche.SearchFunctionsService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchFunctionsServiceImpl implements SearchFunctionsService {
    
    private static final Pattern DE_DATE = Pattern.compile("\\d{1,2}[.]\\d{0,2}[.]{0,1}\\d{0,4}");

    private final ElasticsearchClient elasticsearchClient;

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * Holt die Suggestions passend zur Query.
     *
     * Es findet die Verwendung des Completion Suggester Anwendung:
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters.html#completion-suggester
     *
     * @param q text
     * @return Liste an Vorschlaegen
     */
    @SneakyThrows
    @Override
    public List<SucheWordSuggestDTO> getSuggestions(final String q) {
        final String[] splittedWords = q.split(StringUtils.SPACE);
        final String query = splittedWords[splittedWords.length - 1];
        final String[] wordsForPrefix = ArrayUtils.subarray(splittedWords, 0, splittedWords.length - 1);
        final String prefix = Stream.of(wordsForPrefix)
                /*
                 * Es wird jedes Wort geprüft, ob es ein Datum ist
                 * und dann entsprechend aufbereitet, dass damit
                 * gesucht werden kann.
                 */
                .map(this::cleanseDateAndReturnIfWordIsDateOrJustReturnWord)
                .collect(Collectors.joining(StringUtils.SPACE))
                .concat(StringUtils.SPACE);

        /*
         * Erstellen der Query:
         * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters.html#querying
         */
        final var completionSuggester = new CompletionSuggester.Builder()
                .field("suggest")
                .fuzzy(fuzzyBuilder -> fuzzyBuilder.fuzziness("0"))
                .skipDuplicates(true)
                .size(3)
                .build();
        final var fieldSuggester = new FieldSuggester.Builder()
                .prefix(query)
                .completion(completionSuggester)
                .build();
        final var suggester = new Suggester.Builder()
                .suggesters("zaehlstelle-suggest", fieldSuggester)
                .build();
        final var searchRequest = new SearchRequest.Builder()
                .index(elasticsearchOperations.getIndexCoordinatesFor(CustomSuggest.class).getIndexName())
                .source(sourceBuilder -> sourceBuilder.filter(filterBuilder -> filterBuilder.includes("suggest")))
                .suggest(suggester)
                .build();

        /*
         * Ausführen der Query und Extrahieren der Suchwortvorschläge:
         * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters.html#querying
         *
         * Die nachfolgenden Aufrufe der Fluent-API bilden den Aufbau der Response eines
         * Completion-Suggester ab.
         */
        return elasticsearchClient
                .search(searchRequest, CustomSuggest.class)
                .suggest()
                .values()
                .stream()
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .flatMap(suggestion -> CollectionUtils.emptyIfNull(suggestion.completion().options()).stream())
                .map(CompletionSuggestOption::text)
                .filter(StringUtils::isNotEmpty)
                .filter(suggestedText -> {
                    if (isDatumsbereichSuggestion(prefix) && isDate(suggestedText)) {
                        final LocalDate prefixDate = getLocalDateOfString(wordsForPrefix[1]);
                        final LocalDate suggestedDate = getLocalDateOfString(suggestedText);
                        return suggestedDate.isAfter(prefixDate);
                    }
                    return true;
                })
                .map(suggestedText -> prefix + suggestedText)
                .map(SucheWordSuggestDTO::new)
                .toList();
    }

    /**
     * Hilfmethode, um zu testen, ob es sich um eine Suggestion nach einem Datumsbereich handelt.
     * Kriterien:
     * - Exakt 3 Suchbegriffe enthalten
     * - 'von' und 'bis' müssen enthalten sein
     * - 'von' ist Begriff 1, 'bis' ist Begriff 3
     * - Begriffe 2 ist ein Datum
     *
     * @param query Suchanfrage
     * @return true/false
     */
    private boolean isDatumsbereichSuggestion(final String query) {
        final String[] split = query.trim().split(StringUtils.SPACE);
        if (query.contains("von") && query.contains("bis") && split.length == 3) {
            return split[0].trim().equalsIgnoreCase("von") && split[2].trim().equalsIgnoreCase("bis") && this.isDate(split[1]);
        }
        return false;
    }
    
    /**
     * Wandelt einen String der Form dd.MM.YYYY in ein Localdate an
     *
     * @param dateAsString Datum als String
     * @return LocalDate
     */
    private LocalDate getLocalDateOfString(final String dateAsString) {
        final String[] splitted = this.cleanseDateAndReturnIfWordIsDateOrJustReturnWord(dateAsString).split("\\.");
        return LocalDate.of(Integer.parseInt(splitted[2]), Integer.parseInt(splitted[1]), Integer.parseInt(splitted[0]));
    }

    /**
     * Fügt in ein Datum führende Nullen ein und ergänzt das Jahr ggf. um die Tausender. Wenn es sich
     * nicht um ein Datum handelt, dann wird einfach der String
     * wieder zurück gegeben.
     *
     * @param word Suchwort
     * @return korrigiertes Datum oder ursprüngliches Wort
     */
    public String cleanseDateAndReturnIfWordIsDateOrJustReturnWord(final String word) {

        if (this.isDate(word)) {
            final String[] x = word.split("\\.");
            final StringBuilder result = new StringBuilder();

            if (x.length > 0) {
                final String d = x[0].length() < 2 ? 0 + x[0] : x[0];
                result.append(d).append(".");
            }

            if (x.length > 1) {
                final String m = x[1].length() < 2 ? 0 + x[1] : x[1];
                result.append(m).append(".");
            }

            if (x.length > 2) {
                int yearPrefix = 20;
                if (Integer.parseInt(x[2]) > 88) {
                    yearPrefix = 19;
                }
                final String y = x[2].length() < 4 ? yearPrefix + x[2] : x[2];
                result.append(y);
            }
            return result.toString();
        } else {
            return word;
        }
    }

    /**
     * Checkt, ob ein Suchstring ein Datum ist.
     *
     * @param x Suchstring
     * @return Datum oder nicht
     */
    public boolean isDate(final String x) {
        return DE_DATE.matcher(x).matches();
    }

}
