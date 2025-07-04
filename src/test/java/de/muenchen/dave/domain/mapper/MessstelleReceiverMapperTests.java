package de.muenchen.dave.domain.mapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.base.Splitter;
import de.muenchen.dave.domain.elasticsearch.MessquerschnittRandomFactory;
import de.muenchen.dave.domain.elasticsearch.MessstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.MessstelleStatus;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapperImpl;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Slf4j
class MessstelleReceiverMapperTests {

    private MessstelleReceiverMapper mapper;

    @BeforeEach
    void beforeEach() throws IllegalAccessException {
        mapper = new MessstelleReceiverMapperImpl();
        FieldUtils.writeField(mapper, "fahrzeugklassenMapper", new FahrzeugklassenMapperImpl(), true);
        FieldUtils.writeField(mapper, "verkehrsartMapper", new VerkehrsartMapperImpl(), true);
    }

    @Test
    void testCreateMessstelle() {
        final MessstelleDto dto = MessstelleRandomFactory.getMessstelleDto();

        final StadtbezirkMapper stadtbezirkMapper = Mockito.mock(StadtbezirkMapper.class);
        when(stadtbezirkMapper.bezeichnungOf(any())).thenReturn("Schwabing-West");
        final Messstelle expected = new Messstelle();
        expected.setMstId(dto.getMstId());
        expected.setName(dto.getName());
        expected.setStatus(MessstelleStatus.valueOf(dto.getStatus().getValue()));
        expected.setRealisierungsdatum(dto.getRealisierungsdatum());
        expected.setAbbaudatum(dto.getAbbaudatum());
        expected.setStadtbezirkNummer(dto.getStadtbezirkNummer());
        expected.setBemerkung(dto.getBemerkung());
        expected.setDatumLetztePlausibleMessung(dto.getDatumLetztePlausibleMessung());
        expected.setPunkt(new GeoPoint(dto.getLatitude(), dto.getLongitude()));
        expected.setSichtbarDatenportal(false);
        expected.setGeprueft(false);
        expected.setSuchwoerter(new ArrayList<>());
        expected.setHersteller(dto.getHersteller());
        expected.setFahrzeugklasse(new FahrzeugklassenMapperImpl().map(dto.getFahrzeugklasse()));
        expected.setDetektierteVerkehrsart(new VerkehrsartMapperImpl().map(dto.getDetektierteVerkehrsarten()));
        final String stadtbezirkBezeichnung = "Schwabing-West";
        final Set<String> stadtbezirke = new HashSet<>(Splitter.on("-").omitEmptyStrings().trimResults().splitToList(stadtbezirkBezeichnung));
        expected.getSuchwoerter().addAll(stadtbezirke);
        if (CollectionUtils.isNotEmpty(stadtbezirke) && stadtbezirke.size() > 1) {
            expected.getSuchwoerter().add(stadtbezirkBezeichnung);
        }
        expected.getSuchwoerter().add(dto.getName());
        expected.getSuchwoerter().add(dto.getMstId());

        expected.setMessquerschnitte(mapper.createMessquerschnitte(dto.getMessquerschnitte()));
        expected.setMessfaehigkeiten(mapper.createMessfaehigkeit(dto.getMessfaehigkeiten()));

        final Messstelle messstelle = this.mapper.createMessstelle(dto, stadtbezirkMapper);
        Assertions.assertThat(messstelle)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFieldsMatchingRegexes(".*")
                .ignoringFields("id", "messquerschnitte.id")
                .isEqualTo(expected);
        Assertions.assertThat(messstelle.getId())
                .isNotNull();
        messstelle.getMessquerschnitte().forEach(messquerschnitt -> Assertions.assertThat(messquerschnitt.getId())
                .isNotNull());
    }

    @Test
    void testDtoToMessquerschnitt() {
        final MessquerschnittDto dto = MessquerschnittRandomFactory.getMessquerschnittDto();
        final Messquerschnitt expected = new Messquerschnitt();
        expected.setMqId(dto.getMqId());
        expected.setPunkt(new GeoPoint(dto.getLatitude(), dto.getLongitude()));
        expected.setStrassenname(dto.getStrassenname());
        expected.setLageMessquerschnitt(dto.getLageMessquerschnitt());
        expected.setFahrtrichtung(dto.getFahrtrichtung());
        expected.setAnzahlFahrspuren(dto.getAnzahlFahrspuren());
        expected.setAnzahlDetektoren(dto.getAnzahlDetektoren());

        final Messquerschnitt actual = this.mapper.createMessquerschnitt(dto);
        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id", "punkt")
                .isEqualTo(expected);

        Assertions.assertThat(actual.getPunkt().getLat()).isEqualTo(dto.getLatitude());
        Assertions.assertThat(actual.getPunkt().getLon()).isEqualTo(dto.getLongitude());
        Assertions.assertThat(actual.getId())
                .isNotNull();
    }

    @Test
    void testUpdateMessstelle() {
        final Messstelle bean = MessstelleRandomFactory.getMessstelle();

        final MessstelleDto updatedData = MessstelleRandomFactory.getMessstelleDto();

        final Messstelle expected = new Messstelle();
        //geandert
        expected.setMstId(updatedData.getMstId());
        expected.setName(updatedData.getName());
        expected.setStatus(MessstelleStatus.valueOf(updatedData.getStatus().getValue()));
        expected.setBemerkung(updatedData.getBemerkung());
        expected.setStadtbezirkNummer(updatedData.getStadtbezirkNummer());
        expected.setRealisierungsdatum(updatedData.getRealisierungsdatum());
        expected.setAbbaudatum(updatedData.getAbbaudatum());
        expected.setHersteller(updatedData.getHersteller());
        expected.setFahrzeugklasse(new FahrzeugklassenMapperImpl().map(updatedData.getFahrzeugklasse()));
        expected.setDetektierteVerkehrsart(new VerkehrsartMapperImpl().map(updatedData.getDetektierteVerkehrsarten()));
        expected.setSuchwoerter(new ArrayList<>());
        expected.getSuchwoerter().addAll(bean.getCustomSuchwoerter());
        expected.getSuchwoerter().add(updatedData.getMstId());
        expected.getSuchwoerter().add(updatedData.getName());
        final String stadtbezirk = "Schwabing-West";
        final Set<String> stadtbezirke = new HashSet<>(Splitter.on("-").omitEmptyStrings().trimResults().splitToList(stadtbezirk));
        expected.getSuchwoerter().addAll(stadtbezirke);
        if (CollectionUtils.isNotEmpty(stadtbezirke) && stadtbezirke.size() > 1) {
            expected.getSuchwoerter().add(stadtbezirk);
        }
        expected.setMessfaehigkeiten(this.mapper.createMessfaehigkeit(updatedData.getMessfaehigkeiten()));

        // unveraendert
        expected.setId(bean.getId());
        expected.setSichtbarDatenportal(bean.getSichtbarDatenportal());
        expected.setGeprueft(bean.getGeprueft());
        expected.setLageplanVorhanden(bean.getLageplanVorhanden());
        expected.setKommentar(bean.getKommentar());
        expected.setStandort(bean.getStandort());
        expected.setCustomSuchwoerter(bean.getCustomSuchwoerter());
        expected.setPunkt(bean.getPunkt());
        expected.setMessquerschnitte(bean.getMessquerschnitte());
        expected.setDatumLetztePlausibleMessung(bean.getDatumLetztePlausibleMessung());

        final StadtbezirkMapper stadtbezirkMapper = Mockito.mock(StadtbezirkMapper.class);
        when(stadtbezirkMapper.bezeichnungOf(any())).thenReturn("Schwabing-West");
        final Messstelle actual = this.mapper.updateMessstelle(bean, updatedData, stadtbezirkMapper);
        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFieldsMatchingRegexes(".*")
                .isEqualTo(expected);
    }

    @Test
    void statusMapping() {
        final MessstelleDto messstelleDto = MessstelleRandomFactory.getMessstelleDto();
        final StadtbezirkMapper stadtbezirkMapper = Mockito.mock(StadtbezirkMapper.class);
        when(stadtbezirkMapper.bezeichnungOf(any())).thenReturn("Schwabing-West");
        messstelleDto.setStatus(MessstelleDto.StatusEnum.IN_BESTAND);
        final Messstelle messstelle = mapper.createMessstelle(messstelleDto, stadtbezirkMapper);
        Assertions.assertThat(messstelle.getStatus()).isEqualTo(MessstelleStatus.IN_BESTAND);
    }
}
