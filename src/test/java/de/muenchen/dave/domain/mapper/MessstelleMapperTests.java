package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.messstelle.EditMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleTooltipDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessquerschnittAuswertungDTO;
import de.muenchen.dave.domain.dtos.messstelle.auswertung.MessstelleAuswertungDTO;
import de.muenchen.dave.domain.elasticsearch.MessquerschnittRandomFactory;
import de.muenchen.dave.domain.elasticsearch.MessstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapperImpl;
import de.muenchen.dave.util.SuchwortUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
class MessstelleMapperTests {

    private final MessstelleMapper mapper = new MessstelleMapperImpl();

    @Test
    void testBean2readDto() {
        final Messstelle bean = MessstelleRandomFactory.getMessstelle();

        final ReadMessstelleInfoDTO expected = new ReadMessstelleInfoDTO();
        expected.setId(bean.getId());
        expected.setMstId(bean.getMstId());
        expected.setStadtbezirkNummer(bean.getStadtbezirkNummer());
        expected.setStadtbezirk("Schwabing-West");
        expected.setLongitude(bean.getPunkt().getLon());
        expected.setLatitude(bean.getPunkt().getLat());
        expected.setStandort(bean.getStandort());
        expected.setDatumLetztePlausibleMessung(bean.getDatumLetztePlausibleMessung());
        expected.setRealisierungsdatum(bean.getRealisierungsdatum());
        expected.setAbbaudatum(bean.getAbbaudatum());
        expected.setMessquerschnitte(this.mapper.bean2readDto(bean.getMessquerschnitte()));
        expected.setMessfaehigkeiten(this.mapper.messfaehigkeitBean2ReadMessfaehigkeitDto(bean.getMessfaehigkeiten()));
        expected.setHersteller(bean.getHersteller());
        expected.setFahrzeugKlassen(bean.getFahrzeugKlassen());
        expected.setDetektierteVerkehrsarten(bean.getDetektierteVerkehrsarten());
        expected.setKommentar(bean.getKommentar());
        expected.setLageplanVorhanden(bean.getLageplanVorhanden());

        final MessstelleTooltipDTO tooltip = new MessstelleTooltipDTO();
        tooltip.setMstId(bean.getMstId());
        tooltip.setStandort(bean.getStandort());
        tooltip.setStadtbezirk("Schwabing-West");
        tooltip.setStadtbezirknummer(bean.getStadtbezirkNummer());
        tooltip.setRealisierungsdatum(bean.getRealisierungsdatum().toString());
        tooltip.setAbbaudatum(bean.getAbbaudatum().toString());
        tooltip.setDatumLetztePlausibleMessung(bean.getDatumLetztePlausibleMessung().toString());
        tooltip.setDetektierteVerkehrsarten(bean.getDetektierteVerkehrsarten());

        final StadtbezirkMapper stadtbezirkMapper = Mockito.mock(StadtbezirkMapper.class);
        when(stadtbezirkMapper.bezeichnungOf(any())).thenReturn("Schwabing-West");

        Assertions.assertThat(this.mapper.bean2readDto(bean, stadtbezirkMapper))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void testBean2editDto() {
        final Messstelle bean = MessstelleRandomFactory.getMessstelle();

        final EditMessstelleDTO expected = new EditMessstelleDTO();
        expected.setId(bean.getId());
        expected.setMstId(bean.getMstId());
        expected.setName(bean.getName());
        expected.setStatus(bean.getStatus().toString());
        expected.setBemerkung(bean.getBemerkung());
        expected.setStadtbezirkNummer(bean.getStadtbezirkNummer());
        expected.setStadtbezirk("Schwabing-West");
        expected.setRealisierungsdatum(bean.getRealisierungsdatum().toString());
        expected.setAbbaudatum(bean.getAbbaudatum().toString());
        expected.setDatumLetztePlausibleMessung(bean.getDatumLetztePlausibleMessung().toString());
        expected.setLongitude(bean.getPunkt().getLon());
        expected.setLatitude(bean.getPunkt().getLat());
        expected.setHersteller(bean.getHersteller());
        expected.setFahrzeugKlassen(bean.getFahrzeugKlassen());
        expected.setDetektierteVerkehrsarten(bean.getDetektierteVerkehrsarten());

        expected.setSichtbarDatenportal(bean.getSichtbarDatenportal());
        expected.setGeprueft(bean.getGeprueft());
        expected.setKommentar(bean.getKommentar());
        expected.setStandort(bean.getStandort());
        expected.setCustomSuchwoerter(bean.getCustomSuchwoerter());
        expected.setMessquerschnitte(this.mapper.bean2editDto(bean.getMessquerschnitte()));
        expected.setMessfaehigkeiten(this.mapper.messfaehigkeitBean2EditMessfaehigkeitDto(bean.getMessfaehigkeiten()));
        expected.setLageplanVorhanden(bean.getLageplanVorhanden());

        final StadtbezirkMapper stadtbezirkMapper = Mockito.mock(StadtbezirkMapper.class);
        when(stadtbezirkMapper.bezeichnungOf(any())).thenReturn("Schwabing-West");

        Assertions.assertThat(this.mapper.bean2editDto(bean, stadtbezirkMapper))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void testUpdateMessstelle() {
        final Messstelle bean = MessstelleRandomFactory.getMessstelle();
        final StadtbezirkMapper stadtbezirkMapper = Mockito.mock(StadtbezirkMapper.class);
        when(stadtbezirkMapper.bezeichnungOf(any())).thenReturn("Schwabing-Neuhausen");

        final EditMessstelleDTO updatedData = new EditMessstelleDTO();
        updatedData.setId("darf nicht aktualisiert werden");
        updatedData.setMstId("darf nicht aktualisiert werden");
        updatedData.setName("darf nicht aktualisiert werden");
        updatedData.setStatus("darf nicht aktualisiert werden");
        updatedData.setBemerkung("darf nicht aktualisiert werden");
        updatedData.setHersteller("darf nicht aktualisiert werden");
        updatedData.setFahrzeugKlassen("darf nicht aktualisiert werden");
        updatedData.setDetektierteVerkehrsarten("darf nicht aktualisiert werden");
        updatedData.setStadtbezirkNummer(666);
        updatedData.setRealisierungsdatum("1999-11-11");
        updatedData.setAbbaudatum("1999-11-11");
        updatedData.setDatumLetztePlausibleMessung("1999-11-11");
        updatedData.setLongitude(6.66);
        updatedData.setLatitude(6.66);
        updatedData.setMessfaehigkeiten(Collections.emptyList());
        updatedData.setLageplanVorhanden(false);

        updatedData.setSichtbarDatenportal(!bean.getSichtbarDatenportal());
        updatedData.setGeprueft(!bean.getGeprueft());
        updatedData.setKommentar("neuer Kommentar");
        updatedData.setStandort("neuer Standort");
        final List<String> customSuchwoerter = new ArrayList<>();
        customSuchwoerter.add("wir");
        customSuchwoerter.add("sind");
        customSuchwoerter.add("neu");
        updatedData.setCustomSuchwoerter(customSuchwoerter);
        updatedData.setMessquerschnitte(this.mapper.bean2editDto(bean.getMessquerschnitte()));

        final Messstelle expected = new Messstelle();
        expected.setStadtbezirkNummer(bean.getStadtbezirkNummer());
        expected.setId(bean.getId());
        expected.setMstId(bean.getMstId());
        expected.setName(bean.getName());
        expected.setStatus(bean.getStatus());
        expected.setBemerkung(bean.getBemerkung());
        expected.setRealisierungsdatum(bean.getRealisierungsdatum());
        expected.setAbbaudatum(bean.getAbbaudatum());
        expected.setDatumLetztePlausibleMessung(bean.getDatumLetztePlausibleMessung());
        expected.setMessquerschnitte(bean.getMessquerschnitte());
        expected.setPunkt(new GeoPoint(updatedData.getLatitude(), updatedData.getLongitude()));
        expected.setHersteller(bean.getHersteller());
        expected.setFahrzeugKlassen(bean.getFahrzeugKlassen());
        expected.setDetektierteVerkehrsarten(bean.getDetektierteVerkehrsarten());
        expected.setMessfaehigkeiten(bean.getMessfaehigkeiten());
        expected.setLageplanVorhanden(bean.getLageplanVorhanden());

        expected.setSichtbarDatenportal(updatedData.getSichtbarDatenportal());
        expected.setGeprueft(true);
        expected.setKommentar(updatedData.getKommentar());
        expected.setStandort(updatedData.getStandort());
        expected.setSuchwoerter(new ArrayList<>());
        expected.getSuchwoerter().addAll(SuchwortUtil.generateSuchworteOfMessstelle(bean, stadtbezirkMapper));
        expected.getSuchwoerter().addAll(updatedData.getCustomSuchwoerter());
        expected.setCustomSuchwoerter(updatedData.getCustomSuchwoerter());

        final Messstelle actual = this.mapper.updateMessstelle(bean, updatedData, stadtbezirkMapper);
        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void teatBean2readDtoQuerschnitt() {
        final Messquerschnitt bean = MessquerschnittRandomFactory.getMessquerschnitt();

        final ReadMessquerschnittDTO expected = new ReadMessquerschnittDTO();
        expected.setId(bean.getId());
        expected.setMqId(bean.getMqId());
        expected.setLongitude(bean.getPunkt().getLon());
        expected.setLatitude(bean.getPunkt().getLat());
        expected.setStrassenname(bean.getStrassenname());
        expected.setLageMessquerschnitt(bean.getLageMessquerschnitt());
        expected.setStandort(bean.getStandort());
        expected.setFahrtrichtung(bean.getFahrtrichtung());
        expected.setAnzahlFahrspuren(bean.getAnzahlFahrspuren());
        expected.setAnzahlDetektoren(bean.getAnzahlDetektoren());

        Assertions.assertThat(this.mapper.bean2readDto(bean))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void testBean2editDtoQuerschnitt() {
        final Messquerschnitt bean = MessquerschnittRandomFactory.getMessquerschnitt();

        final EditMessquerschnittDTO expected = new EditMessquerschnittDTO();
        expected.setId(bean.getId());
        expected.setMqId(bean.getMqId());
        expected.setLongitude(bean.getPunkt().getLon());
        expected.setLatitude(bean.getPunkt().getLat());
        expected.setStrassenname(bean.getStrassenname());
        expected.setLageMessquerschnitt(bean.getLageMessquerschnitt());
        expected.setStandort(bean.getStandort());
        expected.setFahrtrichtung(bean.getFahrtrichtung());
        expected.setAnzahlFahrspuren(bean.getAnzahlFahrspuren());
        expected.setAnzahlDetektoren(bean.getAnzahlDetektoren());

        Assertions.assertThat(this.mapper.bean2editDto(bean))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void bean2auswertungMqDto() {
        final Messquerschnitt bean = MessquerschnittRandomFactory.getMessquerschnitt();

        final MessquerschnittAuswertungDTO expected = new MessquerschnittAuswertungDTO();
        expected.setMqId(bean.getMqId());
        expected.setStandort(bean.getStandort());
        expected.setFahrtrichtung(bean.getFahrtrichtung());

        Assertions.assertThat(this.mapper.bean2auswertungMqDto(List.of(bean)))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(List.of(expected));
    }

    @Test
    void bean2auswertungDto() {
        final Messstelle bean = MessstelleRandomFactory.getMessstelle();

        final MessstelleAuswertungDTO expected = new MessstelleAuswertungDTO();
        expected.setMstId(bean.getMstId());
        expected.setStandort(bean.getStandort());
        expected.setDetektierteVerkehrsarten(bean.getDetektierteVerkehrsarten());
        expected.setMessquerschnitte(this.mapper.bean2auswertungMqDto(bean.getMessquerschnitte()));

        Assertions.assertThat(this.mapper.bean2auswertungDto(List.of(bean)))
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(expected));
    }
}
