package de.muenchen.dave.domain.mapper;

import com.google.common.base.Splitter;
import de.muenchen.dave.domain.elasticsearch.MessquerschnittRandomFactory;
import de.muenchen.dave.domain.elasticsearch.MessstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleReceiverMapperImpl;
import de.muenchen.dave.geodateneai.gen.model.MessquerschnittDto;
import de.muenchen.dave.geodateneai.gen.model.MessstelleDto;
import de.muenchen.dave.services.IndexServiceUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Slf4j
class MessstelleReceiverMapperTests {

    private final MessstelleReceiverMapper mapper = new MessstelleReceiverMapperImpl();

    @Test
    void testDtoToMessstelle() {
        final MessstelleDto dto = MessstelleRandomFactory.getMessstelleDto();

        final Messstelle expected = new Messstelle();
        expected.setMstId(dto.getMstId());
        expected.setName(dto.getName());
        expected.setStatus(dto.getStatus());
        expected.setRealisierungsdatum(dto.getRealisierungsdatum());
        expected.setAbbaudatum(dto.getAbbaudatum());
        expected.setStadtbezirkNummer(dto.getStadtbezirkNummer());
        expected.setBemerkung(dto.getBemerkung());
        expected.setDatumLetztePlausibleMessung(dto.getDatumLetztePlausibleMessung());
        expected.setPunkt(new GeoPoint(dto.getXcoordinate(), dto.getYcoordinate()));
        expected.setSichtbarDatenportal(false);
        expected.setGeprueft(false);
        expected.setSuchwoerter(new ArrayList<>());
        final String stadtbezirkBezeichnung = IndexServiceUtils.getStadtbezirkBezeichnung(dto.getStadtbezirkNummer());
        final Set<String> stadtbezirke = new HashSet<>(Splitter.on("-").omitEmptyStrings().trimResults().splitToList(stadtbezirkBezeichnung));
        expected.getSuchwoerter().addAll(stadtbezirke);
        if (CollectionUtils.isNotEmpty(stadtbezirke) && stadtbezirke.size() > 1) {
            expected.getSuchwoerter().add(stadtbezirkBezeichnung);
        }
        expected.getSuchwoerter().add(dto.getName());
        expected.getSuchwoerter().add(dto.getMstId());

        expected.setMessquerschnitte(mapper.dtoToMessquerschnitt(dto.getMessquerschnitte()));

        Assertions.assertThat(this.mapper.dtoToMessstelle(dto))
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFieldsMatchingRegexes(".*")
                .isEqualTo(expected);
    }

    @Test
    void testDtoToMessquerschnitt() {
        final MessquerschnittDto dto = MessquerschnittRandomFactory.getMessquerschnittDto();
        final Messquerschnitt expected = new Messquerschnitt();
        expected.setMqId(dto.getMqId());
        expected.setPunkt(new GeoPoint(dto.getXcoordinate(), dto.getYcoordinate()));
        expected.setStrassenname(dto.getStrassenname());
        expected.setLage(dto.getLage());
        expected.setFahrtrichtung(dto.getFahrtrichtung());
        expected.setAnzahlFahrspuren(dto.getAnzahlFahrspuren());
        expected.setFahrzeugKlassen(dto.getFahrzeugKlassen());
        expected.setDetektierteVerkehrsarten(dto.getDetektierteVerkehrsarten());
        expected.setHersteller(dto.getHersteller());
        expected.setAnzahlDetektoren(dto.getAnzahlDetektoren());

        final Messquerschnitt actual = this.mapper.dtoToMessquerschnitt(dto);
        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("punkt")
                .isEqualTo(expected);

        Assertions.assertThat(actual.getPunkt().getLat()).isEqualTo(dto.getXcoordinate());
        Assertions.assertThat(actual.getPunkt().getLon()).isEqualTo(dto.getYcoordinate());
    }

    @Test
    void testUpdateMessstelle() {
        final Messstelle bean = MessstelleRandomFactory.getMessstelle();

        final MessstelleDto updatedData = MessstelleRandomFactory.getMessstelleDto();

        final Messstelle expected = new Messstelle();
        //geandert
        expected.setMstId(updatedData.getMstId());
        expected.setName(updatedData.getName());
        expected.setStatus(updatedData.getStatus());
        expected.setBemerkung(updatedData.getBemerkung());
        expected.setStadtbezirkNummer(updatedData.getStadtbezirkNummer());
        expected.setRealisierungsdatum(updatedData.getRealisierungsdatum());
        expected.setAbbaudatum(updatedData.getAbbaudatum());
        expected.setDatumLetztePlausibleMessung(updatedData.getDatumLetztePlausibleMessung());
        expected.setPunkt(new GeoPoint(updatedData.getXcoordinate(), updatedData.getYcoordinate()));
        expected.setSuchwoerter(new ArrayList<>());
        expected.getSuchwoerter().addAll(bean.getCustomSuchwoerter());
        expected.getSuchwoerter().add(updatedData.getMstId());
        expected.getSuchwoerter().add(updatedData.getName());
        final String stadtbezirk = IndexServiceUtils.getStadtbezirkBezeichnung(updatedData.getStadtbezirkNummer());
        final Set<String> stadtbezirke = new HashSet<>(Splitter.on("-").omitEmptyStrings().trimResults().splitToList(stadtbezirk));
        expected.getSuchwoerter().addAll(stadtbezirke);
        if (CollectionUtils.isNotEmpty(stadtbezirke) && stadtbezirke.size() > 1) {
            expected.getSuchwoerter().add(stadtbezirk);
        }

        // unveraendert
        expected.setId(bean.getId());
        expected.setSichtbarDatenportal(bean.getSichtbarDatenportal());
        expected.setGeprueft(bean.getGeprueft());
        expected.setKommentar(bean.getKommentar());
        expected.setStandort(bean.getStandort());
        expected.setCustomSuchwoerter(bean.getCustomSuchwoerter());
        expected.setMessquerschnitte(bean.getMessquerschnitte());

        final Messstelle actual = this.mapper.updateMessstelle(bean, updatedData);
        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFieldsMatchingRegexes(".*")
                .isEqualTo(expected);
    }
}
