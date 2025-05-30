package de.muenchen.dave.spring.services;

import static de.muenchen.dave.TestConstants.SPRING_NO_SECURITY_PROFILE;
import static de.muenchen.dave.TestConstants.SPRING_TEST_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.google.common.collect.Lists;
import de.muenchen.dave.DaveBackendApplication;
import de.muenchen.dave.configuration.CachingConfiguration;
import de.muenchen.dave.domain.dtos.ErhebungsstelleKarteDTO;
import de.muenchen.dave.domain.dtos.ZaehlartenKarteDTO;
import de.muenchen.dave.domain.dtos.ZaehlstelleKarteDTO;
import de.muenchen.dave.domain.dtos.suche.SearchAndFilterOptionsDTO;
import de.muenchen.dave.domain.dtos.suche.SucheComplexSuggestsDTO;
import de.muenchen.dave.domain.elasticsearch.CustomSuggest;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.repositories.elasticsearch.CustomSuggestIndex;
import de.muenchen.dave.repositories.elasticsearch.MessstelleIndex;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import de.muenchen.dave.services.SucheService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = { DaveBackendApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "spring.datasource.url=jdbc:h2:mem:dave;DB_CLOSE_ON_EXIT=FALSE" }
)
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE, SPRING_NO_SECURITY_PROFILE })
@Slf4j
public class SucheServiceSpringTests {

    @Autowired
    SucheService service;
    @Autowired
    CacheManager cacheManager;
    @MockitoBean
    private MessstelleIndex messstelleIndex;
    @MockitoBean
    private CustomSuggestIndex customSuggestIndex;
    @MockitoBean
    private ZaehlstelleIndex zaehlstelleIndex;
    @MockitoBean
    private ElasticsearchOperations elasticsearchOperations;
    @MockitoBean
    private ElasticsearchClient elasticsearchClient;

    @Test
    @WithMockUser(roles = { "FACHADMIN" })
    public void testComplexSuggest() throws IOException {
        final SearchAndFilterOptionsDTO searchAndFilterOptions = new SearchAndFilterOptionsDTO();
        searchAndFilterOptions.setSearchInZaehlstellen(true);
        searchAndFilterOptions.setSearchInMessstellen(true);

        Page<Zaehlstelle> resultComplexSuggest = new PageImpl<>(List.of(
                this.createSampleData().get(0),
                this.createSampleData().get(4)));
        when(zaehlstelleIndex.suggestSearch(any(), any())).thenReturn(resultComplexSuggest);
        when(elasticsearchOperations.getIndexCoordinatesFor(CustomSuggest.class)).thenReturn(IndexCoordinates.of("the-index"));
        when(elasticsearchClient.search(any(SearchRequest.class), eq(CustomSuggest.class)))
                .thenReturn(SearchResponse.of(
                        builder -> builder
                                .suggest(new HashMap<>())
                                .took(200)
                                .timedOut(false)
                                .shards(shardsBuilder -> shardsBuilder.failed(0).successful(1).total(1).failures(List.of()))
                                .hits(hitsBuilder -> hitsBuilder.hits(List.of()))));
        when(messstelleIndex.suggestSearch(any(), any())).thenReturn(new PageImpl<>(List.of()));

        SucheComplexSuggestsDTO dto1 = this.service.getComplexSuggest("Moo", searchAndFilterOptions, false);
        assertThat(dto1.getZaehlstellenSuggests(), is(not(empty())));
        assertThat(dto1.getZaehlstellenSuggests(), containsInAnyOrder(
                Matchers.hasProperty("id", is("01"))));

        resultComplexSuggest = new PageImpl<>(List.of(
                this.createSampleData().get(0),
                this.createSampleData().get(2),
                this.createSampleData().get(3)));
        when(zaehlstelleIndex.suggestSearch(any(), any())).thenReturn(resultComplexSuggest);

        SucheComplexSuggestsDTO dto2 = this.service.getComplexSuggest("7.", searchAndFilterOptions, false);
        assertThat(dto2.getZaehlstellenSuggests(), is(not(empty())));
        assertThat(dto2.getZaehlstellenSuggests(), containsInAnyOrder(
                Matchers.hasProperty("id", is("01")),
                Matchers.hasProperty("id", is("03")),
                Matchers.hasProperty("id", is("04"))));
        assertThat(dto2.getZaehlungenSuggests(), is(not(empty())));
        assertThat(dto2.getZaehlungenSuggests().size(), is(equalTo(3)));

        resultComplexSuggest = new PageImpl<>(List.of(this.createSampleData().get(2)));
        when(zaehlstelleIndex.suggestSearch(any(), any())).thenReturn(resultComplexSuggest);

        SucheComplexSuggestsDTO dto3 = this.service.getComplexSuggest("7. Fo", searchAndFilterOptions, false);
        assertThat(dto3.getZaehlstellenSuggests(), is(not(empty())));
        assertThat(dto3.getZaehlstellenSuggests(), containsInAnyOrder(
                Matchers.hasProperty("id", is("03"))));
        assertThat(dto3.getZaehlungenSuggests(), is(not(empty())));
        assertThat(dto3.getZaehlungenSuggests(), containsInAnyOrder(
                Matchers.hasProperty("text", is("07.02.2008 Foop"))));

        resultComplexSuggest = new PageImpl<>(List.of(
                this.createSampleData().get(3)));
        when(zaehlstelleIndex.suggestSearch(any(), any())).thenReturn(resultComplexSuggest);

        SucheComplexSuggestsDTO dto5 = this.service.getComplexSuggest("13.11 Ga", searchAndFilterOptions, false);
        assertThat(dto5.getZaehlstellenSuggests(), is(not(empty())));
        assertThat(dto5.getZaehlungenSuggests(), is(not(empty())));
        assertThat(dto5.getZaehlungenSuggests(), containsInAnyOrder(
                Matchers.hasProperty("text", is("13.11.2019 Gabi"))));

    }

    @Test
    @WithMockUser(roles = { "FACHADMIN" })
    public void testSucheZaehlstelleWithSonderzaehlungen() {

        Objects.requireNonNull(cacheManager.getCache(CachingConfiguration.SUCHE_ERHEBUNGSSTELLE)).clear();
        Objects.requireNonNull(cacheManager.getCache(CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL)).clear();

        final SearchAndFilterOptionsDTO searchAndFilterOptions = new SearchAndFilterOptionsDTO();
        searchAndFilterOptions.setSearchInZaehlstellen(true);
        searchAndFilterOptions.setSearchInMessstellen(true);

        Page<Zaehlstelle> resultComplexSuggest = new PageImpl<>(List.of(this.createSampleData().get(0)));
        when(zaehlstelleIndex.suggestSearch(any(), any())).thenReturn(resultComplexSuggest);
        when(messstelleIndex.suggestSearch(any(), any())).thenReturn(new PageImpl<>(List.of()));

        final Set<ErhebungsstelleKarteDTO> erhebungsstelleKarteDTOS = this.service.sucheErhebungsstelle("Z01", searchAndFilterOptions, false);
        assertThat(erhebungsstelleKarteDTOS, is(notNullValue()));
        assertThat(erhebungsstelleKarteDTOS.isEmpty(), is(false));
        assertThat(erhebungsstelleKarteDTOS.size(), is(1));
        assertThat(erhebungsstelleKarteDTOS.stream().findFirst().get().getLatitude(), is(equalTo(1.0)));
        assertThat(erhebungsstelleKarteDTOS.stream().findFirst().get().getLongitude(), is(equalTo(1.0)));

        Set<ZaehlartenKarteDTO> expected = new HashSet<>();
        ZaehlartenKarteDTO zaehlartKarte = new ZaehlartenKarteDTO();
        zaehlartKarte.setZaehlarten(new TreeSet<>(List.of("Q")));
        zaehlartKarte.setLatitude(1.0);
        zaehlartKarte.setLongitude(1.0);
        expected.add(zaehlartKarte);
        zaehlartKarte = new ZaehlartenKarteDTO();
        zaehlartKarte.setZaehlarten(new TreeSet<>(List.of("Q_")));
        zaehlartKarte.setLatitude(1.0);
        zaehlartKarte.setLongitude(2.0);
        expected.add(zaehlartKarte);

        assertThat(erhebungsstelleKarteDTOS.stream().filter(stelle -> stelle instanceof ZaehlstelleKarteDTO).map(ZaehlstelleKarteDTO.class::cast).findFirst()
                .get().getZaehlartenKarte(), is(expected));
    }

    @Test
    @WithMockUser(roles = { "ANWENDER" })
    public void testSucheZaehlstelleWithoutSonderzaehlungen() {

        Objects.requireNonNull(cacheManager.getCache(CachingConfiguration.SUCHE_ERHEBUNGSSTELLE)).clear();
        Objects.requireNonNull(cacheManager.getCache(CachingConfiguration.SUCHE_ERHEBUNGSSTELLE_DATENPORTAL)).clear();

        final SearchAndFilterOptionsDTO searchAndFilterOptions = new SearchAndFilterOptionsDTO();
        searchAndFilterOptions.setSearchInZaehlstellen(true);
        searchAndFilterOptions.setSearchInMessstellen(true);

        Page<Zaehlstelle> resultComplexSuggest = new PageImpl<>(List.of(this.createSampleData().get(0)));
        when(zaehlstelleIndex.suggestSearch(any(), any())).thenReturn(resultComplexSuggest);
        when(messstelleIndex.suggestSearch(any(), any())).thenReturn(new PageImpl<>(List.of()));

        final Set<ErhebungsstelleKarteDTO> erhebungsstelleKarteDTOS = this.service.sucheErhebungsstelle("Z01", searchAndFilterOptions, false);
        assertThat(erhebungsstelleKarteDTOS, is(notNullValue()));
        assertThat(erhebungsstelleKarteDTOS.isEmpty(), is(false));
        assertThat(erhebungsstelleKarteDTOS.size(), is(1));
        assertThat(erhebungsstelleKarteDTOS.stream().findFirst().get().getLatitude(), is(equalTo(1.0)));
        assertThat(erhebungsstelleKarteDTOS.stream().findFirst().get().getLongitude(), is(equalTo(1.0)));

        Set<ZaehlartenKarteDTO> expected = new HashSet<>();
        ZaehlartenKarteDTO zaehlartKarte = new ZaehlartenKarteDTO();
        zaehlartKarte.setZaehlarten(new TreeSet<>(List.of("Q_")));
        zaehlartKarte.setLatitude(1.0);
        zaehlartKarte.setLongitude(2.0);
        expected.add(zaehlartKarte);

        assertThat(erhebungsstelleKarteDTOS.stream().filter(stelle -> stelle instanceof ZaehlstelleKarteDTO).map(ZaehlstelleKarteDTO.class::cast).findFirst()
                .get().getZaehlartenKarte(), is(expected));
    }

    private List<Zaehlstelle> createSampleData() {
        // z1
        Zaehlstelle z1 = new Zaehlstelle();
        z1.setId("01");
        z1.setNummer("Z01");
        z1.setStadtbezirk("Moosach");
        z1.setPunkt(new GeoPoint(1, 1));
        z1.setSichtbarDatenportal(true);

        Zaehlung z1_1 = new Zaehlung();
        z1_1.setId("1_1");
        z1_1.setProjektName("Projektz11");
        z1_1.setDatum(LocalDate.parse("2016-05-07"));
        z1_1.setZaehlart("Q");
        z1_1.setPunkt(new GeoPoint(1, 1));
        z1_1.setSonderzaehlung(true);
        z1_1.setStatus(Status.ACTIVE.name());
        z1_1.setSuchwoerter(List.of("Moosach", "Projektz11"));

        Zaehlung z1_2 = new Zaehlung();
        z1_2.setId("1_2");
        z1_2.setProjektName("Projektz12");
        z1_2.setDatum(LocalDate.parse("2014-12-10"));
        z1_2.setZaehlart("Q_");
        z1_2.setPunkt(new GeoPoint(1, 2));
        z1_2.setSonderzaehlung(false);
        z1_2.setStatus(Status.ACTIVE.name());
        z1_2.setSuchwoerter(List.of("Moosach", "Projektz12"));

        z1.setZaehlungen(Lists.newArrayList(z1_1, z1_2));

        // z2
        Zaehlstelle z2 = new Zaehlstelle();
        z2.setId("02");
        z2.setNummer("Z02");
        z2.setStadtbezirk("Sendling");
        z2.setPunkt(new GeoPoint(2, 2));
        z2.setSichtbarDatenportal(true);

        Zaehlung z2_1 = new Zaehlung();
        z2_1.setId("2_1");
        z2_1.setProjektName("Projektz21");
        z2_1.setDatum(LocalDate.parse("2003-10-20"));
        z2_1.setZaehlart("QR");
        z2_1.setPunkt(new GeoPoint(2, 1));
        z2_1.setSonderzaehlung(false);
        z2_1.setStatus(Status.ACTIVE.name());
        z2_1.setSuchwoerter(List.of("Sendling", "Projektz21"));

        z2.setZaehlungen(Lists.newArrayList(z2_1));

        // z3
        Zaehlstelle z3 = new Zaehlstelle();
        z3.setId("03");
        z3.setNummer("Z03");
        z3.setStadtbezirk("Schwabing");
        z3.setPunkt(new GeoPoint(3, 3));
        z3.setSichtbarDatenportal(true);

        Zaehlung z3_1 = new Zaehlung();
        z3_1.setId("3_1");
        z3_1.setProjektName("Foop");
        z3_1.setDatum(LocalDate.parse("2008-02-07"));
        z3_1.setZaehlart("QR");
        z3_1.setPunkt(new GeoPoint(3, 1));
        z3_1.setSonderzaehlung(false);
        z3_1.setStatus(Status.ACTIVE.name());
        z3_1.setSuchwoerter(List.of("Schwabing", "Foop"));

        Zaehlung z3_2 = new Zaehlung();
        z3_2.setId("3_2");
        z3_2.setProjektName("Projektz32");
        z3_2.setDatum(LocalDate.parse("2014-06-18"));
        z3_2.setZaehlart("QR");
        z3_2.setPunkt(new GeoPoint(3, 2));
        z3_2.setSonderzaehlung(false);
        z3_2.setStatus(Status.ACTIVE.name());
        z3_2.setSuchwoerter(List.of("Schwabing", "Projektz32"));

        z3.setZaehlungen(Lists.newArrayList(z3_1, z3_2));

        // z4
        Zaehlstelle z4 = new Zaehlstelle();
        z4.setId("04");
        z4.setNummer("Z04");
        z4.setStadtbezirk("Bogenhausen");
        z4.setPunkt(new GeoPoint(4, 4));
        z4.setSichtbarDatenportal(true);

        Zaehlung z4_1 = new Zaehlung();
        z4_1.setProjektName("Hans");
        z4_1.setId("4_1");
        z4_1.setDatum(LocalDate.parse("2009-03-07"));
        z4_1.setZaehlart("QR");
        z4_1.setPunkt(new GeoPoint(4, 1));
        z4_1.setSonderzaehlung(false);
        z4_1.setStatus(Status.ACTIVE.name());
        z4_1.setSuchwoerter(List.of("Bogenhausen", "Hans"));

        Zaehlung z4_2 = new Zaehlung();
        z4_2.setId("4_2");
        z4_2.setProjektName("Petra");
        z4_2.setDatum(LocalDate.parse("2016-08-13"));
        z4_2.setZaehlart("QR");
        z4_2.setPunkt(new GeoPoint(4, 2));
        z4_2.setSonderzaehlung(false);
        z4_2.setStatus(Status.ACTIVE.name());
        z4_2.setSuchwoerter(List.of("Bogenhausen", "Petra"));

        Zaehlung z4_3 = new Zaehlung();
        z4_3.setId("4_3");
        z4_3.setProjektName("Gabi");
        z4_3.setDatum(LocalDate.parse("2019-11-13"));
        z4_3.setZaehlart("QR");
        z4_3.setPunkt(new GeoPoint(4, 3));
        z4_3.setSonderzaehlung(false);
        z4_3.setStatus(Status.ACTIVE.name());
        z4_3.setSuchwoerter(List.of("Bogenhausen", "Gabi"));

        z4.setZaehlungen(Lists.newArrayList(z4_1, z4_2, z4_3));

        // z5
        Zaehlstelle z5 = new Zaehlstelle();
        z5.setId("05");
        z5.setNummer("Z05");
        z5.setStadtbezirk("Moosach");
        z5.setPunkt(new GeoPoint(5, 5));

        return Lists.newArrayList(z1, z2, z3, z4, z5);
    }

}
