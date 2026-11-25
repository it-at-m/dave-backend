package de.muenchen.dave.domain.mapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.muenchen.dave.domain.dtos.ZaehlstelleKarteDto;
import de.muenchen.dave.domain.dtos.ZaehlstelleTooltipDto;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleKarteDto;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleTooltipDto;
import de.muenchen.dave.domain.elasticsearch.MessstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.ZaehlstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.detektor.Messstelle;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Slf4j
class SucheMapperTests {

    private final SucheMapper mapper = new SucheMapperImpl();

    @Test
    void testMessstelleToMessstelleTooltipDTO() {
        final Messstelle messstelle = MessstelleRandomFactory.getMessstelle();
        final StadtbezirkMapper stadtbezirkMapper = Mockito.mock(StadtbezirkMapper.class);
        when(stadtbezirkMapper.bezeichnungOf(any())).thenReturn("Schwabing-Neuhausen");
        final MessstelleTooltipDto actual = mapper.messstelleToMessstelleTooltipDTO(messstelle, stadtbezirkMapper);

        final MessstelleTooltipDto expected = new MessstelleTooltipDto();
        expected.setMstId(messstelle.getMstId());
        expected.setStandort(messstelle.getStandort());
        expected.setStadtbezirk("Schwabing-Neuhausen");
        expected.setStadtbezirknummer(messstelle.getStadtbezirkNummer());
        expected.setRealisierungsdatum(messstelle.getRealisierungsdatum().toString());
        expected.setAbbaudatum(messstelle.getAbbaudatum().toString());
        expected.setDatumLetztePlausibleMessung(messstelle.getDatumLetztePlausibleMessung().toString());
        expected.setDetektierteVerkehrsart(messstelle.getDetektierteVerkehrsart().name());

        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void testMessstelleToMessstelleKarteDTO() {
        final Messstelle messstelle = MessstelleRandomFactory.getMessstelle();
        final StadtbezirkMapper stadtbezirkMapper = Mockito.mock(StadtbezirkMapper.class);
        when(stadtbezirkMapper.bezeichnungOf(any())).thenReturn("Schwabing-Neuhausen");
        final MessstelleKarteDto actual = mapper.messstelleToMessstelleKarteDTO(messstelle, stadtbezirkMapper);
        final MessstelleKarteDto expected = new MessstelleKarteDto();
        expected.setId(messstelle.getId());
        expected.setFachId(messstelle.getMstId());
        expected.setType("messstelle");
        expected.setLongitude(messstelle.getPunkt().getLon());
        expected.setLatitude(messstelle.getPunkt().getLat());
        expected.setStatus(messstelle.getStatus());
        expected.setSichtbarDatenportal(messstelle.getSichtbarDatenportal());

        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("tooltip")
                .isEqualTo(expected);
    }

    @Test
    void testZaehlstelleToZaehlstelleKarteDTO() {
        final Zaehlstelle zaehlstelle = ZaehlstelleRandomFactory.getOne();
        final ZaehlstelleKarteDto actual = mapper.zaehlstelleToZaehlstelleKarteDTO(zaehlstelle);
        final ZaehlstelleKarteDto expected = new ZaehlstelleKarteDto();
        expected.setId(zaehlstelle.getId());
        expected.setFachId(zaehlstelle.getNummer());
        expected.setType("zaehlstelle");
        expected.setLongitude(zaehlstelle.getPunkt().getLon());
        expected.setLatitude(zaehlstelle.getPunkt().getLat());
        expected.setSichtbarDatenportal(zaehlstelle.getSichtbarDatenportal());

        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("tooltip", "letzteZaehlungId", "zaehlartenKarte")
                .isEqualTo(expected);

    }

    @Test
    void testCreateZaehlstelleTooltip() {
        final String stadtbezirk = "stadtbezirk";
        final Integer stadtbezirknummer = 1;
        final String nummer = "123456";
        final Integer anzahlZaehlungen = 12;
        final String datumLetzteZaehlung = "2024-02-16";
        final String kreuzungsname = "kreuzungsname";
        final ZaehlstelleTooltipDto actual = mapper.createZaehlstelleTooltip(stadtbezirk, stadtbezirknummer, nummer, anzahlZaehlungen,
                datumLetzteZaehlung, kreuzungsname);

        final ZaehlstelleTooltipDto expected = new ZaehlstelleTooltipDto();
        expected.setZaehlstellennnummer(nummer);
        expected.setStadtbezirk(stadtbezirk);
        expected.setStadtbezirknummer(stadtbezirknummer);
        expected.setAnzahlZaehlungen(anzahlZaehlungen);
        expected.setDatumLetzteZaehlung(datumLetzteZaehlung);
        expected.setKreuzungsname(kreuzungsname);

        Assertions.assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);

    }

}
