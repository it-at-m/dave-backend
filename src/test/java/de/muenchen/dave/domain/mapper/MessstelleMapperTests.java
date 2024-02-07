package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.messstelle.EditMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.EditMessstelleDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleTooltipDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessquerschnittDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleInfoDTO;
import de.muenchen.dave.domain.elasticsearch.MessquerschnittRandomFactory;
import de.muenchen.dave.domain.elasticsearch.MessstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.detektor.Messquerschnitt;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.enums.Stadtbezirk;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapperImpl;
import de.muenchen.dave.services.IndexServiceUtils;
import de.muenchen.dave.util.SuchwortUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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
        expected.setStadtbezirk(Stadtbezirk.bezeichnungOf(bean.getStadtbezirkNummer()));
        expected.setLongitude(bean.getPunkt().getLon());
        expected.setLatitude(bean.getPunkt().getLat());
        expected.setStandort(bean.getStandort());
        expected.setDatumLetztePlausibleMessung(bean.getDatumLetztePlausibleMessung());
        expected.setRealisierungsdatum(bean.getRealisierungsdatum());
        expected.setAbbaudatum(bean.getAbbaudatum());
        expected.setMessquerschnitte(this.mapper.bean2readDto(bean.getMessquerschnitte()));
        expected.setKommentar(bean.getKommentar());

        final MessstelleTooltipDTO tooltip = new MessstelleTooltipDTO();
        tooltip.setMstId(bean.getMstId());
        tooltip.setStandort(bean.getStandort());
        tooltip.setStadtbezirk(IndexServiceUtils.getStadtbezirkBezeichnung(bean.getStadtbezirkNummer()));
        tooltip.setStadtbezirknummer(bean.getStadtbezirkNummer());
        tooltip.setRealisierungsdatum(bean.getRealisierungsdatum().toString());
        tooltip.setAbbaudatum(bean.getAbbaudatum().toString());
        tooltip.setDatumLetztePlausibleMessung(bean.getDatumLetztePlausibleMessung().toString());
        tooltip.setDetektierteVerkehrsarten(bean.getMessquerschnitte().get(0).getDetektierteVerkehrsarten());

        Assertions.assertThat(this.mapper.bean2readDto(bean))
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
        expected.setStadtbezirk(Stadtbezirk.bezeichnungOf(bean.getStadtbezirkNummer()));
        expected.setRealisierungsdatum(bean.getRealisierungsdatum().toString());
        expected.setAbbaudatum(bean.getAbbaudatum().toString());
        expected.setDatumLetztePlausibleMessung(bean.getDatumLetztePlausibleMessung().toString());
        expected.setLongitude(bean.getPunkt().getLon());
        expected.setLatitude(bean.getPunkt().getLat());

        expected.setSichtbarDatenportal(bean.getSichtbarDatenportal());
        expected.setGeprueft(bean.getGeprueft());
        expected.setKommentar(bean.getKommentar());
        expected.setStandort(bean.getStandort());
        expected.setCustomSuchwoerter(bean.getCustomSuchwoerter());
        expected.setMessquerschnitte(this.mapper.bean2editDto(bean.getMessquerschnitte()));

        Assertions.assertThat(this.mapper.bean2editDto(bean))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void testUpdateMessstelle() {
        final Messstelle bean = MessstelleRandomFactory.getMessstelle();

        final EditMessstelleDTO updatedData = new EditMessstelleDTO();
        updatedData.setId("darf nicht aktualisiert werden");
        updatedData.setMstId("darf nicht aktualisiert werden");
        updatedData.setName("darf nicht aktualisiert werden");
        updatedData.setStatus("darf nicht aktualisiert werden");
        updatedData.setBemerkung("darf nicht aktualisiert werden");
        updatedData.setStadtbezirkNummer(666);
        updatedData.setRealisierungsdatum("1999-11-11");
        updatedData.setAbbaudatum("1999-11-11");
        updatedData.setDatumLetztePlausibleMessung("1999-11-11");
        updatedData.setLongitude(6.66);
        updatedData.setLatitude(6.66);

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
        expected.setPunkt(bean.getPunkt());

        expected.setSichtbarDatenportal(updatedData.getSichtbarDatenportal());
        expected.setGeprueft(true);
        expected.setKommentar(updatedData.getKommentar());
        expected.setStandort(updatedData.getStandort());
        expected.setSuchwoerter(new ArrayList<>());
        expected.getSuchwoerter().addAll(SuchwortUtil.generateSuchworteOfMessstelle(bean));
        expected.getSuchwoerter().addAll(updatedData.getCustomSuchwoerter());
        expected.setCustomSuchwoerter(updatedData.getCustomSuchwoerter());

        Messstelle actual = this.mapper.updateMessstelle(bean, updatedData);
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
        expected.setFahrzeugKlassen(bean.getFahrzeugKlassen());
        expected.setDetektierteVerkehrsarten(bean.getDetektierteVerkehrsarten());
        expected.setHersteller(bean.getHersteller());
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
        expected.setFahrzeugKlassen(bean.getFahrzeugKlassen());
        expected.setDetektierteVerkehrsarten(bean.getDetektierteVerkehrsarten());
        expected.setHersteller(bean.getHersteller());
        expected.setAnzahlDetektoren(bean.getAnzahlDetektoren());

        Assertions.assertThat(this.mapper.bean2editDto(bean))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}
