package de.muenchen.dave.services;

import com.google.common.collect.Lists;
import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.dtos.ErhebungsstelleKarteDTO;
import de.muenchen.dave.domain.dtos.ZaehlartenKarteDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
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

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * Diese Methode ermittelt aus den im Parameter übergebenen {@link Zaehlung}en,
     * die nach Koordinaten gruppierten Zaehlarten.
     *
     * @param zaehlungen zum ermitteln nach Koordinate gruppierten Zählarten.
     * @return die nach Koordinaten gruppierten Zaehlarten.
     */
    public static Set<ZaehlartenKarteDTO> mapZaehlungenToZaehlartenKarte(final List<Zaehlung> zaehlungen) {
        final Set<ZaehlartenKarteDTO> zaehlartenKarteSet = new HashSet<>();
        CollectionUtils.emptyIfNull(zaehlungen).forEach(zaehlung -> {
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
     * Zaehlstellen- und Zaehlungssuggests aus dem zurückgegebenen Objekt.
     *
     * @param query Suchquery
     * @param noFilter Ist true, wenn die Anfrage vom Adminportal kommt, sonst false
     * @return DTO mit alle Suchvorschlaegen
     */
    public SucheComplexSuggestsDTO complexSuggestSichtbarDatenportal(final String query, final boolean noFilter) {
        final var sucheComplexSuggests = this.getComplexSuggest(query, noFilter);
        final var zaehlstellenSuggests = sucheComplexSuggests.getZaehlstellenSuggests()
                .stream()
                .filter(sucheZaehlstelleSuggest -> ObjectUtils.isEmpty(sucheZaehlstelleSuggest.getSichtbarDatenportal())
                        || sucheZaehlstelleSuggest.getSichtbarDatenportal())
                .collect(Collectors.toList());
        sucheComplexSuggests.setZaehlstellenSuggests(zaehlstellenSuggests);
        final var zaehlungenSuggests = sucheComplexSuggests.getZaehlungenSuggests()
                .stream()
                .filter(sucheZaehlungSuggest -> ObjectUtils.isEmpty(sucheZaehlungSuggest.getSichtbarDatenportal())
                        || sucheZaehlungSuggest.getSichtbarDatenportal())
                .collect(Collectors.toList());
        sucheComplexSuggests.setZaehlungenSuggests(zaehlungenSuggests);
        return sucheComplexSuggests;
    }

    /**
     * Erstellt eine Vorschlagsliste für die "search as you type" Suche. Diese
     * besteht aus Suchvorschlägen, Vorschläge für eine bestimmte Suchstelle und
     * Vorschläge für eine bestimmte Zählung.
     *
     * @param query Suchquery
     * @param noFilter Ist true, wenn die Anfrage vom Adminportal kommt, sonst false
     * @return DTO mit alle Suchvorschlaegen
     */
    public SucheComplexSuggestsDTO getComplexSuggest(final String query, final boolean noFilter) {
        final String q = this.createQueryString(query);
        log.debug("query '{}'", q);

        final SucheComplexSuggestsDTO dto = new SucheComplexSuggestsDTO();

        // Zählstellen
        final Page<Zaehlstelle> zaehlstellen = this.zaehlstelleIndex.suggestSearch(q, PageRequest.of(0, 3));

        final List<SucheZaehlstelleSuggestDTO> sucheZaehlstelleSuggestDTOS = this.filterZaehlungen(zaehlstellen.toList(), noFilter).stream()
                .map(this.zaehlstelleMapper::bean2SucheZaehlstelleSuggestDto)
                .collect(Collectors.toList());

        dto.setZaehlstellenSuggests(sucheZaehlstelleSuggestDTOS);

        // Zählungen
        final List<Zaehlung> zaehlungen = this.findZaehlung(zaehlstellen, query);
        final List<SucheZaehlungSuggestDTO> sucheZaehlungSuggestDtos = zaehlungen.stream()
                .map(this.zaehlungMapper::bean2SucheZaehlungSuggestDto)
                .collect(Collectors.toList());

        sucheZaehlungSuggestDtos.forEach(zaehlung -> {
            zaehlstellen.get().forEach(zst -> {
                if (!zst.getZaehlungen().isEmpty()) {
                    final Optional<Zaehlung> first = zst.getZaehlungen().stream()
                            .filter(zaehlungFilter -> zaehlungFilter.getId().equalsIgnoreCase(zaehlung.getId())).findFirst();
                    if (first.isPresent()) {
                        zaehlung.setZaehlstelleId(zst.getId());
                        zaehlung.setSichtbarDatenportal(zst.getSichtbarDatenportal());
                    }
                }
            });
        });

        dto.setZaehlungenSuggests(sucheZaehlungSuggestDtos);

        // Wörter
        dto.setWordSuggests(this.getSuggestions(query));

        // Messstellen
        dto.setMessstellenSuggests(getMessstellenSuggest(q));

        return dto;
    }

    /**
     * Die Methode filtert vom Ergebnis {@link SucheService#sucheErhebungsstelle} alle nicht sichtbaren
     * Zaehlstellen aus dem zurückgegebenen Set.
     * <p>
     * Gibt alle sichtbaren Zählstellen zurück.
     * Eine Zählstelle gilt als unsichtbar sobald das Attribut "sichtbarDatenportal" false ist.
     *
     * @param query Suchquery
     * @param noFilter Ist true, wenn die Anfrage vom Adminportal kommt, sonst false
     * @return passende Zaehlstellen
     */
    @Cacheable(value = CachingConfiguration.SUCHE_ZAEHLSTELLE_DATENPORTAL, key = "{#p0, #p1}")
    public Set<ErhebungsstelleKarteDTO> sucheZaehlstelleSichtbarDatenportal(final String query, final boolean noFilter) {
        log.debug("Zugriff auf den Service #sucheZaehlstelleSichtbarDatenportal");
        final var sichtbarDatenportal = true;
        return this.sucheErhebungsstelle(query, noFilter, sichtbarDatenportal);
    }

    /**
     * Sucht alle freigegebenen Zählstellen und gibt diese an getZaehlstelleKarteDTOS weiter.
     *
     * @param query Eine Suchquery zur Suche von Zähl-/Messstellen. Bei leerer Suchquery sollen alle
     *            Zähl-/Messstellen gefunden werden.
     * @param noFilter Ist true, wenn die Anfrage vom Adminportal kommt, sonst false
     * @return Set von befüllten ErhebungsstellenDTOs der gesuchten Zähl-/Messstellen
     */
    @Cacheable(value = CachingConfiguration.SUCHE_ERHEBUNGSSTELLE, key = "{#p0, #p1}")
    public Set<ErhebungsstelleKarteDTO> sucheErhebungsstelle(final String query, final boolean noFilter, final boolean sichtbarDatenportal) {
        log.debug("Zugriff auf den Service #sucheErhebungsstelle");
        final Set<ErhebungsstelleKarteDTO> zaehlstellen = sucheZaehlstelle(query, noFilter, sichtbarDatenportal);
        final Set<ErhebungsstelleKarteDTO> messstellen = sucheMessstelle(query, sichtbarDatenportal);
        return Stream.concat(zaehlstellen.stream(), messstellen.stream()).collect(Collectors.toSet());
    }

    /**
     * Gibt alle Zählstellen zurück, die auf die Query passen.
     *
     * @param query Eine Suchquery
     * @param noFilter Ist true, wenn die Anfrage vom Adminportal kommt, sonst false
     * @param sichtbarDatenportal Nur sichtbare Messstellen zurückgeben
     * @return Ein Set von befüllten ErhebungsstelleKarteDTOs
     */
    private Set<ErhebungsstelleKarteDTO> sucheZaehlstelle(final String query, final boolean noFilter, final boolean sichtbarDatenportal) {
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
                this.zaehlstelleIndex.findAll().forEach(zaehlstelle -> {
                    zaehlstelle.getZaehlungen().forEach(zaehlung -> {
                        if (this.isDateEqualOrAfter(zaehlung.getDatum(), afterDate) && this.isDateEqualOrBefore(zaehlung.getDatum(), beforeDate)) {
                            relevantZaelstellen.add(zaehlstelle);
                        }
                    });
                });
                zaehlstellen = new ArrayList<>(relevantZaelstellen);

            } else {
                final String q = this.createQueryString(query);
                log.debug("query '{}'", q);
                zaehlstellen = this.zaehlstelleIndex.suggestSearch(q, pageable).toList();
            }
        }
        return this.getZaehlstelleKarteDTOS(zaehlstellen, noFilter, sichtbarDatenportal);
    }

    /**
     * Erstellt eine Liste an Suchvorschlägen für die Messstellen, die auf die Query passen.
     *
     * @param query Eine Suchquery
     * @return Ein Set von befüllten SucheMessstelleSuggestDTOs
     */
    private List<SucheMessstelleSuggestDTO> getMessstellenSuggest(String query) {
        final Page<Messstelle> messstellen = this.messstelleIndex.suggestSearch(query, PageRequest.of(0, 3));
        final List<SucheMessstelleSuggestDTO> sucheMessstelleSuggestDTOS = messstellen.stream()
                .map(this.messstelleMapper::bean2SucheMessstelleSuggestDto)
                .collect(Collectors.toList());
        log.debug("Found {} messstelle(n)", sucheMessstelleSuggestDTOS.size());
        return sucheMessstelleSuggestDTOS;
    }

    /**
     * Erstellt eine Liste an Messstellen, die auf die Query passen.
     *
     * @param query Eine Suchquery
     * @param sichtbarDatenportal Nur sichtbare Messstellen zurückgeben
     * @return Ein Set von befüllten ErhebungsstelleKarteDTOs
     */
    private Set<ErhebungsstelleKarteDTO> sucheMessstelle(final String query, final boolean sichtbarDatenportal) {
        final List<Messstelle> messstellen;
        final PageRequest pageable = PageRequest.of(0, 10000);
        if (StringUtils.isEmpty(query)) {
            messstellen = this.messstelleIndex.findAll();
        } else {
            final String q = this.createQueryString(query);
            log.debug("query '{}'", q);
            messstellen = this.messstelleIndex.suggestSearch(q, pageable).toList();
        }
        return this.getMessstelleKarteDTOS(messstellen, sichtbarDatenportal);
    }

    /**
     * Befüllt ZaehlstelleKarteDTOs mit den entsprechenden Daten zum Anzeigen auf einer Karte und
     * liefert diese zurück
     *
     * @param zaehlstellen Zaehlstellen, die in ZaehlstelleKarteDTOs umgewandelt werden sollen
     * @param noFilter Ist true, wenn die Anfrage vom Adminportal kommt, sonst false
     * @return Ein Set von befüllten ZaehlstelleKarteDTOs
     */
    private Set<ErhebungsstelleKarteDTO> getZaehlstelleKarteDTOS(final List<Zaehlstelle> zaehlstellen, final boolean noFilter,
            final boolean sichtbarDatenportal) {
        final Set<ErhebungsstelleKarteDTO> erhebungsstelleKarteDTOSet = new HashSet<>();

        for (final Zaehlstelle zaehlstelle : this.filterZaehlungen(zaehlstellen, noFilter)) {
            if (sichtbarDatenportal && !zaehlstelle.getSichtbarDatenportal())
                continue;
            Zaehlung letzeZaehlung = null;
            if (CollectionUtils.isNotEmpty(zaehlstelle.getZaehlungen())) {
                letzeZaehlung = IndexServiceUtils.getLetzteZaehlung(zaehlstelle.getZaehlungen());
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

            final ErhebungsstelleKarteDTO erhebungsstelleKarteDTO = new ErhebungsstelleKarteDTO();
            erhebungsstelleKarteDTO.setId(zaehlstelle.getId());
            erhebungsstelleKarteDTO.setLatitude(zaehlstelle.getPunkt().getLat());
            erhebungsstelleKarteDTO.setLongitude(zaehlstelle.getPunkt().getLon());
            erhebungsstelleKarteDTO.setFachId(nummer);
            erhebungsstelleKarteDTO.setType("zaehlstelle");

            erhebungsstelleKarteDTO.setTooltip(
                    SucheMapper.createZaehlstelleTooltip(
                            stadtbezirk,
                            stadtbezirksnummer,
                            nummer,
                            anzahlZaehlungen,
                            datumLetzteZaehlung,
                            kreuzungsname));

            erhebungsstelleKarteDTOSet.add(erhebungsstelleKarteDTO);
        }

        return erhebungsstelleKarteDTOSet;
    }

    /**
     * Befüllt ZaehlstelleKarteDTOs mit den entsprechenden Daten zum Anzeigen auf einer Karte und
     * liefert diese zurück
     *
     * @param messstellen Zaehlstellen, die in ZaehlstelleKarteDTOs umgewandelt werden sollen
     * @param sichtbarDatenportal Nur sichtbare Messstellen zurückgeben
     * @return Ein Set von befüllten ZaehlstelleKarteDTOs
     */
    private Set<ErhebungsstelleKarteDTO> getMessstelleKarteDTOS(final List<Messstelle> messstellen,
            final boolean sichtbarDatenportal) {
        final Set<ErhebungsstelleKarteDTO> erhebungsstelleKarteDTOSet = new HashSet<>();

        for (final Messstelle messstelle : messstellen) {
            if (sichtbarDatenportal && !messstelle.getSichtbarDatenportal())
                continue;

            final ErhebungsstelleKarteDTO erhebungsstelleKarteDTO = new ErhebungsstelleKarteDTO();
            erhebungsstelleKarteDTO.setId(messstelle.getId());
            erhebungsstelleKarteDTO.setLatitude(messstelle.getPunkt().getLat());
            erhebungsstelleKarteDTO.setLongitude(messstelle.getPunkt().getLon());
            erhebungsstelleKarteDTO.setFachId(messstelle.getNummer());
            erhebungsstelleKarteDTO.setType("messstelle");

            erhebungsstelleKarteDTO.setTooltip(SucheMapper.createMessstelleTooltip(messstelle));

            erhebungsstelleKarteDTOSet.add(erhebungsstelleKarteDTO);
        }

        return erhebungsstelleKarteDTOSet;
    }

    private boolean isDateEqualOrAfter(final LocalDate datum, final LocalDate datumAfter) {
        return datum.isEqual(datumAfter) || datum.isAfter(datumAfter);
    }

    private boolean isDateEqualOrBefore(final LocalDate datum, final LocalDate datumBefore) {
        return datum.isBefore(datumBefore) || datum.isEqual(datumBefore);
    }

    /**
     * Hilfmethode, um zu testen, ob es sich um eine Suche nach einem Datumsbereich handelt.
     * Kriterien:
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
        final String[] splitted = this.cleanseDate(dateAsString).split("\\.");
        return LocalDate.of(Integer.parseInt(splitted[2]), Integer.parseInt(splitted[1]), Integer.parseInt(splitted[0]));
    }

    /**
     * Es dürfen im Datenportal nur Zählungen angezeigt werden, die ACTIVE sind. Alle anderen werden
     * hier ausgefiltert.
     * Desweiteren darf ein normaler Anwender keine Sonderzählungen sehen, diese werden ebenfalls
     * ausgefiltert.
     * Wenn eine Zählstelle nach dem Filtern keine Zählungen mehr enthält, so wird dies auch entfernt.
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
        zaehlstellen.forEach(zaehlstelle -> {
            zaehlstelle.setZaehlungen(
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
                            .collect(Collectors.toList()));
        });
        // Alle Zählstelle ausfiltern, die keine Zaehlungen mehr enthalten
        return zaehlstellen.stream()
                .filter(zaehlstelle -> CollectionUtils.isNotEmpty(zaehlstelle.getZaehlungen()))
                .collect(Collectors.toList());
    }

    /**
     * Holt die Suggestions passend zur Query.
     *
     * @param q text
     * @return Liste an Vorschlaegen
     */
    private List<SucheWordSuggestDTO> getSuggestions(final String q) {
        final StringBuilder queryBuilder = new StringBuilder();
        final String[] words = q.split(StringUtils.SPACE);
        final String query = words[words.length - 1];

        for (int i = 0; i < words.length - 1; i++) {
            // es wird jedes Wort geprüft, ob es ein Datum ist
            // und dann entsprechend aufbereitet, dass damit
            // gesucht werden kann.
            final String word = this.cleanseDate(words[i]);

            // Wildcard für jedes Suchwort.
            queryBuilder.append(word).append(StringUtils.SPACE);
        }
        final String prefix = new String(queryBuilder);

        final int maxHits = 3;
        final String zaehlstelle_suggest = "zaehlstelle-suggest";
        final CompletionSuggestionBuilder suggest = SuggestBuilders.completionSuggestion("suggest").prefix(query, Fuzziness.ZERO).skipDuplicates(true)
                .size(maxHits);
        final SearchResponse searchResponse = this.elasticsearchOperations.suggest(new SuggestBuilder().addSuggestion(zaehlstelle_suggest, suggest),
                this.elasticsearchOperations.getIndexCoordinatesFor(CustomSuggest.class));
        final List<SucheWordSuggestDTO> result = new ArrayList<>();

        final List<? extends Suggest.Suggestion.Entry.Option> options = searchResponse.getSuggest().getSuggestion(zaehlstelle_suggest).getEntries().get(0)
                .getOptions();
        options.forEach(o -> {
            final SucheWordSuggestDTO suggestDTO = new SucheWordSuggestDTO();
            suggestDTO.setText(prefix + o.getText().string());
            result.add(suggestDTO);
        });

        return result;
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
     * Check die Zählstelle, ob sich darin Zählungen befinden, die direkt angezeigt werden
     * können. Im ersten Schritt werden hierfür das Datum und der Projektname hergenommen.
     * Bei Bedarf können diese zwei Parameter auch durch weitere Attrubute erweitert werden.
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
            optionalZaehlung = Optional.ofNullable(null);
        }
        return optionalZaehlung;
    }

    /**
     * Prüft, ob eines der Suchworte auf die angegebenen Attribute
     * einer Zählung passt.
     *
     * @param words Liste der Suchworte
     * @param z Zaehlung
     * @return gefunden = true
     */
    public boolean filterZaehlung(final List<String> words, final Zaehlung z) {
        final Optional<String> finding = words.stream()
                .filter(
                        w -> z.getDatum().format(DATE_TIME_FORMATTER).startsWith(this.cleanseDate(w)) ||
                                z.getSuchwoerter().stream().anyMatch(s -> s.startsWith(w)))
                .findAny();
        return finding.isPresent();
    }

    /**
     * Erstellt den Query String mit suffix Wildcards. Dadurch muss der
     * Anwender sich gar keine Gedanken machen, ob er jetzt eine Wildcard
     * benötigt, oder nicht.
     *
     * @param query Suchquery
     * @return Suchquery mit Wildcards
     */
    public String createQueryString(final String query) {
        final StringBuilder queryBuilder = new StringBuilder();
        final String[] words = query.split(StringUtils.SPACE);
        for (int i = 0; i < words.length; i++) {
            // es wird jedes Wort geprüft, ob es ein Datum ist
            // und dann entsprechend aufbereitet, dass damit
            // gesucht werden kann.
            final String word = this.cleanseDate(words[i]);

            // Wildcard für jedes Suchwort.
            queryBuilder.append(word).append("* ");
        }
        return queryBuilder.toString().trim();
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
     * Fügt in ein Datum führende Nullen ein und ergänzt das
     * Jahr ggf. um die Tausender. Wenn es sich nicht um ein Datum handelt,
     * dann wird einfach der String wieder zurück gegeben.
     *
     * @param word Suchwort
     * @return korrigiertes Datum oder ursprüngliches Wort
     */
    public String cleanseDate(final String word) {

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
