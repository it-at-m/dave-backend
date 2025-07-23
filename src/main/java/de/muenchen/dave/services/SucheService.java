package de.muenchen.dave.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import com.google.common.collect.Lists;
import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.dtos.ErhebungsstelleKarteDTO;
import de.muenchen.dave.domain.dtos.ZaehlartenKarteDTO;
import de.muenchen.dave.domain.dtos.ZaehlstelleKarteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleKarteDTO;
import de.muenchen.dave.domain.dtos.suche.SearchAndFilterOptionsDTO;
import de.muenchen.dave.domain.dtos.suche.SucheComplexSuggestsDTO;
import de.muenchen.dave.domain.dtos.suche.SucheMessstelleSuggestDTO;
import de.muenchen.dave.domain.dtos.suche.SucheWordSuggestDTO;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlstelleSuggestDTO;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlungSuggestDTO;
import de.muenchen.dave.domain.elasticsearch.CustomSuggest;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.domain.mapper.StadtbezirkMapper;
import de.muenchen.dave.domain.mapper.SucheMapper;
import de.muenchen.dave.domain.mapper.ZaehlstelleMapper;
import de.muenchen.dave.domain.mapper.ZaehlungMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapper;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.security.SecurityContextInformationExtractor;
import de.muenchen.dave.util.DaveConstants;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SucheService {

    private static final Pattern DE_DATE = Pattern.compile("\\d{1,2}[.]\\d{0,2}[.]{0,1}\\d{0,4}");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DaveConstants.DATE_FORMAT);

    private final ZaehlstelleIndex zaehlstelleIndex;

    private final ZaehlstelleMapper zaehlstelleMapper;

    private final MessstelleIndex messstelleIndex;

    private final MessstelleMapper messstelleMapper;

    private final ZaehlungMapper zaehlungMapper;

    private final SucheMapper sucheMapper;

    private final StadtbezirkMapper stadtbezirkMapper;

    private final ElasticsearchClient elasticsearchClient;

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * Diese Methode ermittelt aus den im Parameter übergebenen {@link Zaehlung}en, die nach Koordinaten
     * gruppierten Zaehlarten.
     *
     * @param zaehlungen zum ermitteln nach Koordinate gruppierten Zählarten.
     * @return die nach Koordinaten gruppierten Zaehlarten.
     */
    public static Set<ZaehlartenKarteDTO> mapZaehlungenToZaehlartenKarte(final List<Zaehlung> zaehlungen) {
        final Set<ZaehlartenKarteDTO> zaehlartenKarteSet = new HashSet<>();
        CollectionUtils.emptyIfNull(zaehlungen).forEach(zaehlung -> {
            log.debug("Zaehlung: {}", zaehlung.toString());
            final ZaehlartenKarteDTO zaehlartenKarte = new ZaehlartenKarteDTO();
            zaehlartenKarte.setLatitude(zaehlung.getPunkt().getLat());
            zaehlartenKarte.setLongitude(zaehlung.getPunkt().getLon());
            final Optional<ZaehlartenKarteDTO> optZaehlartenKarte = zaehlartenKarteSet.stream()
                    .filter(zaehlartenKarteDTO -> zaehlartenKarteDTO.equals(zaehlartenKarte))
                    .findFirst();
            if (optZaehlartenKarte.isPresent()) {
                optZaehlartenKarte.get().getZaehlarten().add(zaehlung.getZaehlart());
            } else {
                final Set<String> zaehlarten = new TreeSet<>();
                zaehlarten.add(zaehlung.getZaehlart());
                zaehlartenKarte.setZaehlarten(zaehlarten);
                zaehlartenKarteSet.add(zaehlartenKarte);
            }
        });
        return zaehlartenKarteSet;
    }

    /**
     * Die Methode filtert vom Ergebnis {@link SucheService#getComplexSuggest} alle nicht sichtbaren
     * Zaehl-/Messstellen- und Zaehlungssuggests aus dem
     * zurückgegebenen Objekt.
     *
     * @param query Suchquery
     * @param searchAndFilterOptions Filteroptionen
     * @return DTO mit alle Suchvorschlaegen
     */
    public SucheComplexSuggestsDTO getComplexSuggestSichtbarDatenportal(
            final String query,
            final SearchAndFilterOptionsDTO searchAndFilterOptions) {
        final var sucheComplexSuggests = this.getComplexSuggest(query, searchAndFilterOptions, false);
        final var zaehlstellenSuggests = ListUtils.emptyIfNull(sucheComplexSuggests.getZaehlstellenSuggests())
                .stream()
                .filter(sucheZaehlstelleSuggest -> ObjectUtils.isEmpty(sucheZaehlstelleSuggest.getSichtbarDatenportal())
                        || sucheZaehlstelleSuggest.getSichtbarDatenportal())
                .collect(Collectors.toList());
        sucheComplexSuggests.setZaehlstellenSuggests(zaehlstellenSuggests);
        final var zaehlungenSuggests = ListUtils.emptyIfNull(sucheComplexSuggests.getZaehlungenSuggests())
                .stream()
                .filter(sucheZaehlungSuggest -> ObjectUtils.isEmpty(sucheZaehlungSuggest.getSichtbarDatenportal())
                        || sucheZaehlungSuggest.getSichtbarDatenportal())
                .collect(Collectors.toList());
        sucheComplexSuggests.setZaehlungenSuggests(zaehlungenSuggests);
        final var messstellenSuggests = ListUtils.emptyIfNull(sucheComplexSuggests.getMessstellenSuggests())
                .stream()
                .filter(sucheMessstelleSuggest -> ObjectUtils.isEmpty(sucheMessstelleSuggest.getSichtbarDatenportal())
                        || sucheMessstelleSuggest.getSichtbarDatenportal())
                .collect(Collectors.toList());
        sucheComplexSuggests.setMessstellenSuggests(messstellenSuggests);
        return sucheComplexSuggests;
    }

    /**
     * Erstellt eine Vorschlagsliste für die "search as you type" Suche. Diese besteht aus
     * Suchvorschlägen, Vorschläge für eine bestimmte Suchstelle und
     * Vorschläge für eine bestimmte Zählung.
     *
     * @param query Suchquery
     * @param searchAndFilterOptions Filteroptionen
     * @param isAdminportal Ist true, wenn die Anfrage vom Adminportal kommt, sonst false
     * @return DTO mit alle Suchvorschlaegen
     */
    public SucheComplexSuggestsDTO getComplexSuggest(
            final String query,
            final SearchAndFilterOptionsDTO searchAndFilterOptions,
            final boolean isAdminportal) {
        final String q = this.createQueryString(query);
        log.debug("query '{}'", q);

        final SucheComplexSuggestsDTO dto = new SucheComplexSuggestsDTO();

        if (searchAndFilterOptions.isSearchInZaehlstellen()) {
            // Zählstellen
            final Page<Zaehlstelle> zaehlstellen = this.zaehlstelleIndex.suggestSearch(q, PageRequest.of(0, 3));

            final List<SucheZaehlstelleSuggestDTO> sucheZaehlstelleSuggestDTOS = this.filterZaehlungen(zaehlstellen.toList(), isAdminportal).stream()
                    .map(this.zaehlstelleMapper::bean2SucheZaehlstelleSuggestDto)
                    .collect(Collectors.toList());

            dto.setZaehlstellenSuggests(sucheZaehlstelleSuggestDTOS);

            // Zählungen
            final List<Zaehlung> zaehlungen = this.findZaehlung(zaehlstellen, query);
            final List<SucheZaehlungSuggestDTO> sucheZaehlungSuggestDtos = zaehlungen.stream()
                    .map(this.zaehlungMapper::bean2SucheZaehlungSuggestDto)
                    .collect(Collectors.toList());

            sucheZaehlungSuggestDtos.forEach(zaehlung -> zaehlstellen.get().forEach(zst -> {
                if (!zst.getZaehlungen().isEmpty()) {
                    final Optional<Zaehlung> first = zst.getZaehlungen().stream()
                            .filter(zaehlungFilter -> zaehlungFilter.getId().equalsIgnoreCase(zaehlung.getId())).findFirst();
                    if (first.isPresent()) {
                        zaehlung.setZaehlstelleId(zst.getId());
                        zaehlung.setSichtbarDatenportal(zst.getSichtbarDatenportal());
                    }
                }
            }));

            dto.setZaehlungenSuggests(sucheZaehlungSuggestDtos);
        }

        // Wörter
        dto.setWordSuggests(this.getSuggestions(query));

        if (searchAndFilterOptions.isSearchInMessstellen()) {
            // Messstellen
            dto.setMessstellenSuggests(getMessstellenSuggest(q, searchAndFilterOptions));
        }

        return dto;
    }

    /**
     * Die Methode filtert vom Ergebnis {@link SucheService#sucheErhebungsstelle} alle nicht sichtbaren
     * Zaehl-/Messstellen aus dem zurückgegebenen Set.
     * <p>
     * Gibt alle sichtbaren Zähl-/Messstellen zurück. Eine Zähl-/Messstelle gilt als unsichtbar sobald
     * das Attribut "sichtbarDatenportal" false ist.
     *
     * @param query Suchquery
     * @param searchAndFilterOptions Filteroptionen
     * @return passende Zaehl-/Messstellen
     */
    @Cacheable(value = CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL, key = "{#p0, #p1}")
    public Set<ErhebungsstelleKarteDTO> sucheErhebungsstelleSichtbarDatenportal(
            final String query,
            final SearchAndFilterOptionsDTO searchAndFilterOptions) {
        log.debug("Zugriff auf den Service #sucheErhebungsstelleSichtbarDatenportal");
        final Set<ErhebungsstelleKarteDTO> searchResult = sucheErhebungsstelle(query, searchAndFilterOptions, false);
        return searchResult
                .stream()
                .filter(erhebungsstelleKarteDTO -> ObjectUtils.isEmpty(erhebungsstelleKarteDTO.getSichtbarDatenportal())
                        || erhebungsstelleKarteDTO.getSichtbarDatenportal())
                .collect(Collectors.toSet());
    }

    /**
     * Sucht alle freigegebenen Zähl-/Messstellen und gibt diese an die DTO-Erstellung weiter.
     *
     * @param query Eine Suchquery zur Suche von Zähl-/Messstellen. Bei leerer Suchquery sollen alle
     *            Zähl-/Messstellen gefunden werden.
     * @param searchAndFilterOptions Filteroptionen
     * @param isAdminportal Ist true, wenn die Anfrage vom Adminportal kommt, sonst false
     * @return Set von befüllten ErhebungsstellenDTOs der gesuchten Zähl-/Messstellen
     */
    @Cacheable(value = CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, key = "{#p0, #p1}")
    public Set<ErhebungsstelleKarteDTO> sucheErhebungsstelle(
            final String query,
            final SearchAndFilterOptionsDTO searchAndFilterOptions,
            final boolean isAdminportal) {
        log.debug("Zugriff auf den Service #sucheErhebungsstelle");
        final Set<ErhebungsstelleKarteDTO> searchResult = new HashSet<>();
        if (searchAndFilterOptions.isSearchInMessstellen()) {
            searchResult.addAll(sucheMessstelle(query, searchAndFilterOptions));
        }
        if (searchAndFilterOptions.isSearchInZaehlstellen()) {
            searchResult.addAll(sucheZaehlstelle(query, searchAndFilterOptions, isAdminportal));
        }
        return searchResult;
    }

    /**
     * Gibt alle Zählstellen zurück, die auf die Query passen.
     *
     * @param query Eine Suchquery
     * @param searchAndFilterOptions Filteroptionen
     * @param isAdminportal Ist true, wenn die Anfrage vom Adminportal kommt, sonst false
     * @return Ein Set von befüllten ErhebungsstelleKarteDTOs
     */
    private Set<ZaehlstelleKarteDTO> sucheZaehlstelle(final String query, final SearchAndFilterOptionsDTO searchAndFilterOptions, final boolean isAdminportal) {
        final List<Zaehlstelle> zaehlstellen;
        final PageRequest pageable = PageRequest.of(0, 10000);
        if (StringUtils.isEmpty(query)) {
            zaehlstellen = this.zaehlstelleIndex.findAll();
        } else {
            if (this.isDatumsbereichSuche(query)) {
                final String[] words = query.split(StringUtils.SPACE);
                final LocalDate afterDate = this.getLocalDateOfString(words[1]);
                final LocalDate beforeDate = this.getLocalDateOfString(words[3]);
                log.debug("Date between '{}' and '{}'", afterDate, beforeDate);
                // Alle Aktive Zählungen laden
                // Alle Zählstellen suchen, die Zählungen im Bereich haben
                final Set<Zaehlstelle> relevantZaelstellen = new HashSet<>();
                this.zaehlstelleIndex.findAll().forEach(zaehlstelle -> zaehlstelle.getZaehlungen().forEach(zaehlung -> {
                    if (this.isDateEqualOrAfter(zaehlung.getDatum(), afterDate) && this.isDateEqualOrBefore(zaehlung.getDatum(), beforeDate)) {
                        relevantZaelstellen.add(zaehlstelle);
                    }
                }));
                zaehlstellen = new ArrayList<>(relevantZaelstellen);
            } else {
                final String q = this.createQueryString(query);
                log.debug("#sucheZaehlstelle '{}', filter '{}'", q, searchAndFilterOptions);
                zaehlstellen = this.zaehlstelleIndex.suggestSearch(q, pageable).toList();
            }
        }
        return this.getZaehlstelleKarteDTOS(zaehlstellen, isAdminportal);
    }

    /**
     * Erstellt eine Liste an Suchvorschlägen für die Messstellen, die auf die Query passen.
     *
     * @param query Eine Suchquery
     * @return Ein Set von befüllten SucheMessstelleSuggestDTOs
     */
    private List<SucheMessstelleSuggestDTO> getMessstellenSuggest(final String query, final SearchAndFilterOptionsDTO searchAndFilterOptions) {
        final Page<Messstelle> messstellen = this.messstelleIndex.suggestSearch(query, PageRequest.of(0, 3));
        final List<SucheMessstelleSuggestDTO> sucheMessstelleSuggestDTOS = messstellen.stream()
                .filter(messstelle -> CollectionUtils.emptyIfNull(searchAndFilterOptions.getMessstelleVerkehrsart()).contains(messstelle.getDetektierteVerkehrsart()))
                .map(this.messstelleMapper::bean2SucheMessstelleSuggestDto)
                .collect(Collectors.toList());
        log.debug("Found {} messstelle(n)", sucheMessstelleSuggestDTOS.size());
        return sucheMessstelleSuggestDTOS;
    }

    /**
     * Erstellt eine Liste an Messstellen, die auf die Query passen.
     *
     * @param query Eine Suchquery
     * @return Ein Set von befüllten ErhebungsstelleKarteDTOs
     */
    private Set<MessstelleKarteDTO> sucheMessstelle(final String query, final SearchAndFilterOptionsDTO searchAndFilterOptions) {
        final List<Messstelle> messstellen;
        final PageRequest pageable = PageRequest.of(0, 10000);
        if (StringUtils.isEmpty(query)) {
            messstellen = this.messstelleIndex.findAll();
        } else {
            final String q = this.createQueryString(query);
            log.debug("query '{}'", q);
            messstellen = this.messstelleIndex.suggestSearch(q, pageable).toList();
        }
        final var filteredMessstellen = messstellen.stream().filter(messstelle -> CollectionUtils.emptyIfNull(searchAndFilterOptions.getMessstelleVerkehrsart()).contains(messstelle.getDetektierteVerkehrsart())).toList();
        return sucheMapper.messstelleToMessstelleKarteDTO(filteredMessstellen, stadtbezirkMapper);
    }

    private boolean isDateEqualOrAfter(final LocalDate datum, final LocalDate datumAfter) {
        return datum.isEqual(datumAfter) || datum.isAfter(datumAfter);
    }

    private boolean isDateEqualOrBefore(final LocalDate datum, final LocalDate datumBefore) {
        return datum.isBefore(datumBefore) || datum.isEqual(datumBefore);
    }

    /**
     * Hilfmethode, um zu testen, ob es sich um eine Suche nach einem Datumsbereich handelt. Kriterien:
     * - Exakt 4 Suchbegriffe enthalten
     * - 'von' und 'bis' müssen enthalten sein
     * - 'von' ist Begriff 1, 'bis' ist Begriff 3
     * - Begriffe 2 und 4 sind ein Datum
     *
     * @param query Suchanfrage
     * @return true/false
     */
    private boolean isDatumsbereichSuche(final String query) {
        final String[] split = query.trim().split(StringUtils.SPACE);
        if (query.contains("von") && query.contains("bis") && split.length == 4) {
            return split[0].trim().equalsIgnoreCase("von") && split[2].trim().equalsIgnoreCase("bis") && this.isDate(split[1]) && this.isDate(split[3]);
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
     * Es dürfen im Datenportal nur Zählungen angezeigt werden, die ACTIVE sind. Alle anderen werden
     * hier ausgefiltert. Desweiteren darf ein normaler Anwender
     * keine Sonderzählungen sehen, diese werden ebenfalls ausgefiltert. Wenn eine Zählstelle nach dem
     * Filtern keine Zählungen mehr enthält, so wird dies auch
     * entfernt.
     *
     * @param zaehlstellen zu filtern
     * @param noFilter Ist true, wenn die Anfrage vom Adminportal kommt, sonst false
     * @return Liste mit erlaubten Záhlstellen
     */
    private List<Zaehlstelle> filterZaehlungen(final List<Zaehlstelle> zaehlstellen, final boolean noFilter) {

        // Für das Adminportal wird nichts gefiltert => Fachadmin ist angemeldet
        if (noFilter && SecurityContextInformationExtractor.isFachadmin()) {
            return zaehlstellen;
        }

        final boolean isAnwender = SecurityContextInformationExtractor.isAnwender();

        // Nur die Sichtbaren Zaehlstellen durchsuchen
        zaehlstellen.stream()
                .filter(Zaehlstelle::getSichtbarDatenportal)
                .forEach(zaehlstelle -> zaehlstelle.setZaehlungen(
                        zaehlstelle.getZaehlungen().stream()
                                // Alle Zaehlung mit einem Status != ACTIVE werden ausgefilter
                                .filter(zaehlung -> zaehlung.getStatus().equalsIgnoreCase(Status.ACTIVE.name()))
                                // Alle Zaehlungen ausfiltern, die Sonderzaehlungen sind, wenn es sich um einen Anwender handelt
                                .filter(zaehlung -> {
                                    if (isAnwender) {
                                        return !zaehlung.getSonderzaehlung();
                                    } else {
                                        return true;
                                    }
                                })
                                .collect(Collectors.toList())));
        // Alle Zählstelle ausfiltern, die keine Zaehlungen mehr enthalten
        return zaehlstellen.stream()
                .filter(zaehlstelle -> CollectionUtils.isNotEmpty(zaehlstelle.getZaehlungen()))
                .collect(Collectors.toList());
    }

    /**
     * Befüllt ZaehlstelleKarteDTOs mit den entsprechenden Daten zum Anzeigen auf einer Karte und
     * liefert diese zurück
     *
     * @param zaehlstellen Zaehlstellen, die in ZaehlstelleKarteDTOs umgewandelt werden sollen
     * @param isAdminportal Ist true, wenn die Anfrage vom Adminportal kommt, sonst false
     * @return Ein Set von befüllten ZaehlstelleKarteDTOs
     */
    private Set<ZaehlstelleKarteDTO> getZaehlstelleKarteDTOS(final List<Zaehlstelle> zaehlstellen, final boolean isAdminportal) {
        final Set<ZaehlstelleKarteDTO> zaehlstelleKarteDTOSet = new HashSet<>();

        for (final Zaehlstelle zaehlstelle : this.filterZaehlungen(zaehlstellen, isAdminportal)) {
            Zaehlung letzeZaehlung = null;
            if (CollectionUtils.isNotEmpty(zaehlstelle.getZaehlungen())) {
                letzeZaehlung = IndexServiceUtils.getLetzteAktiveZaehlung(zaehlstelle.getZaehlungen());
            }

            final String stadtbezirk = zaehlstelle.getStadtbezirk();
            final Integer stadtbezirksnummer = zaehlstelle.getStadtbezirkNummer();
            final String nummer = zaehlstelle.getNummer();
            final String kreuzungsname = letzeZaehlung == null ? "" : letzeZaehlung.getKreuzungsname();
            final Integer anzahlZaehlungen = CollectionUtils.isEmpty(zaehlstelle.getZaehlungen())
                    ? 0
                    : zaehlstelle.getZaehlungen().size();
            final String datumLetzteZaehlung = zaehlstelle.getLetzteZaehlungMonat()
                    + StringUtils.SPACE
                    + zaehlstelle.getLetzteZaehlungJahr();

            final ZaehlstelleKarteDTO zaehlstelleKarteDTO = sucheMapper.zaehlstelleToZaehlstelleKarteDTO(zaehlstelle);

            zaehlstelleKarteDTO.setLetzteZaehlungId(
                    letzeZaehlung == null
                            ? ""
                            : letzeZaehlung.getId());

            zaehlstelleKarteDTO.setTooltip(
                    sucheMapper.createZaehlstelleTooltip(
                            stadtbezirk,
                            stadtbezirksnummer,
                            nummer,
                            anzahlZaehlungen,
                            datumLetzteZaehlung,
                            kreuzungsname));

            zaehlstelleKarteDTO.setZaehlartenKarte(
                    mapZaehlungenToZaehlartenKarte(zaehlstelle.getZaehlungen()));

            zaehlstelleKarteDTOSet.add(zaehlstelleKarteDTO);
        }

        return zaehlstelleKarteDTOSet;
    }

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
    private List<SucheWordSuggestDTO> getSuggestions(final String q) {
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
     * Findet alle Zählungen, die zur Suchanfrage passen könnten.
     *
     * @param pz zeahlstellen
     * @param query suchanfrage
     * @return liste zaehlungen
     */
    private List<Zaehlung> findZaehlung(final Page<Zaehlstelle> pz, final String query) {
        return pz.get()
                .map(zst -> this.checkZaehlstelleForZaehlung(zst, query))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Check die Zählstelle, ob sich darin Zählungen befinden, die direkt angezeigt werden können. Im
     * ersten Schritt werden hierfür das Datum und der
     * Projektname hergenommen. Bei Bedarf können diese zwei Parameter auch durch weitere Attrubute
     * erweitert werden.
     *
     * @param zaehlstelle zu pruefen
     * @param query Suchwoerter
     * @return passende Zaehlung
     */
    public Optional<Zaehlung> checkZaehlstelleForZaehlung(final Zaehlstelle zaehlstelle, final String query) {
        final Optional<Zaehlung> optionalZaehlung;
        if (!zaehlstelle.getZaehlungen().isEmpty()) {
            final List<String> words = Lists.newArrayList(query.split(StringUtils.SPACE));
            optionalZaehlung = zaehlstelle.getZaehlungen().stream()
                    .filter(z -> this.filterZaehlung(words, z))
                    .findAny();
        } else {
            optionalZaehlung = Optional.empty();
        }
        return optionalZaehlung;
    }

    /**
     * Prüft, ob eines der Suchworte auf die angegebenen Attribute einer Zählung passt.
     *
     * @param words Liste der Suchworte
     * @param z Zaehlung
     * @return gefunden = true
     */
    public boolean filterZaehlung(final List<String> words, final Zaehlung z) {
        final Optional<String> finding = words.stream()
                .filter(
                        w -> z.getDatum().format(DATE_TIME_FORMATTER).startsWith(this.cleanseDateAndReturnIfWordIsDateOrJustReturnWord(w)) ||
                                z.getSuchwoerter().stream().anyMatch(s -> StringUtils.startsWithIgnoreCase(s, w)))
                .findAny();
        return finding.isPresent();
    }

    /**
     * Erstellt den Query String mit suffix Wildcards. Dadurch muss der Anwender sich gar keine Gedanken
     * machen, ob er jetzt eine Wildcard benötigt, oder
     * nicht.
     *
     * @param query Suchquery
     * @return Suchquery mit Wildcards
     */
    public String createQueryString(final String query) {
        final var wildcard = "* ";
        final String[] words = query.split(StringUtils.SPACE);
        return Stream.of(words)
                /*
                 * Es wird jedes Wort geprüft, ob es ein Datum ist
                 * und dann entsprechend aufbereitet, dass damit
                 * gesucht werden kann.
                 */
                .map(this::cleanseDateAndReturnIfWordIsDateOrJustReturnWord)
                .collect(Collectors.joining(wildcard))
                .concat(wildcard)
                .trim();
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

}
