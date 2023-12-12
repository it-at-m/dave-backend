package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.messstelle.MessstelleTooltipDTO;
import de.muenchen.dave.domain.dtos.messstelle.ReadMessstelleDTO;
import de.muenchen.dave.domain.elasticsearch.MessstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapper;
import de.muenchen.dave.domain.mapper.detektor.MessstelleMapperImpl;
import de.muenchen.dave.services.IndexServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class MessstelleMapperTests {

    private final MessstelleMapper mapper = new MessstelleMapperImpl();

    @Test
    void testBean2readDto() {
        final Messstelle bean = MessstelleRandomFactory.getOne();

        final ReadMessstelleDTO expected = new ReadMessstelleDTO();
        expected.setId(bean.getId());
        expected.setNummer(bean.getNummer());
        expected.setName(bean.getName());
        expected.setStatus(bean.getStatus());
        expected.setBemerkung(bean.getBemerkung());
        expected.setStadtbezirkNummer(bean.getStadtbezirkNummer());
        expected.setRealisierungsdatum(bean.getRealisierungsdatum().toString());
        expected.setAbbaudatum(bean.getAbbaudatum().toString());
        expected.setDatumLetztePlausibleMeldung(bean.getDatumLetztePlausibleMeldung().toString());
        expected.setLongitude(bean.getPunkt().getLon());
        expected.setLatitude(bean.getPunkt().getLat());
        final MessstelleTooltipDTO tooltip = new MessstelleTooltipDTO();
        tooltip.setNummer(bean.getNummer());
        tooltip.setStandort(bean.getStandort());
        tooltip.setStadtbezirk(IndexServiceUtils.getStadtbezirkBezeichnung(bean.getStadtbezirkNummer()));
        tooltip.setStadtbezirknummer(bean.getStadtbezirkNummer());
        tooltip.setRealisierungsdatum(bean.getRealisierungsdatum().toString());
        tooltip.setAbbaudatum(bean.getAbbaudatum().toString());
        tooltip.setDatumLetztePlausibleMeldung(bean.getDatumLetztePlausibleMeldung().toString());
        tooltip.setDetektierteVerkehrsarten(bean.getMessquerschnitte().get(0).getDetektierteVerkehrsarten());

        expected.setTooltip(tooltip);
        expected.setSichtbarDatenportal(bean.getSichtbarDatenportal());
        expected.setGeprueft(bean.getGeprueft());
        expected.setKommentar(bean.getKommentar());
        expected.setStandort(bean.getStandort());
        expected.setCustomSuchwoerter(bean.getCustomSuchwoerter());
        expected.setMessquerschnitte(this.mapper.bean2readDto(bean.getMessquerschnitte()));

        Assertions.assertThat(this.mapper.bean2readDto(bean))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

}
