package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.ZaehlstelleKarteDTO;
import de.muenchen.dave.domain.dtos.ZaehlstelleTooltipDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleKarteDTO;
import de.muenchen.dave.domain.dtos.messstelle.MessstelleTooltipDTO;
import de.muenchen.dave.domain.elasticsearch.MessstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.ZaehlstelleRandomFactory;
import de.muenchen.dave.relational.messtellen.MessstelleEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlstelleEntity;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
class SucheMapperTests {

    private final SucheMapper mapper = new SucheMapperImpl();

    @Test
    void testMessstelleToMessstelleTooltipDTO() {
        final MessstelleEntity messstelle = MessstelleRandomFactory.getMessstelle();
        final StadtbezirkMapper stadtbezirkMapper = Mockito.mock(StadtbezirkMapper.class);
        when(stadtbezirkMapper.bezeichnungOf(any())).thenReturn("Schwabing-Neuhausen");
        final MessstelleTooltipDTO actual = mapper.messstelleToMessstelleTooltipDTO(messstelle, stadtbezirkMapper);

        final MessstelleTooltipDTO expected = new MessstelleTooltipDTO();
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
        final MessstelleEntity messstelle = MessstelleRandomFactory.getMessstelle();
        final StadtbezirkMapper stadtbezirkMapper = Mockito.mock(StadtbezirkMapper.class);
        when(stadtbezirkMapper.bezeichnungOf(any())).thenReturn("Schwabing-Neuhausen");
        final MessstelleKarteDTO actual = mapper.messstelleToMessstelleKarteDTO(messstelle, stadtbezirkMapper);
        final MessstelleKarteDTO expected = new MessstelleKarteDTO();
        expected.setId(messstelle.getId().toString());
        expected.setFachId(messstelle.getMstId());
        expected.setType("messstelle");
        expected.setLongitude(messstelle.getLongitude());
        expected.setLatitude(messstelle.getLatitude());
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
        final ZaehlstelleEntity zaehlstelle = ZaehlstelleRandomFactory.getOne();
        final ZaehlstelleKarteDTO actual = mapper.zaehlstelleToZaehlstelleKarteDTO(zaehlstelle);
        final ZaehlstelleKarteDTO expected = new ZaehlstelleKarteDTO();
        expected.setId(zaehlstelle.getId().toString());
        expected.setFachId(zaehlstelle.getNummer());
        expected.setType("zaehlstelle");
        expected.setLongitude(zaehlstelle.getLongitude());
        expected.setLatitude(zaehlstelle.getLatitude());
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
        final ZaehlstelleTooltipDTO actual = mapper.createZaehlstelleTooltip(stadtbezirk, stadtbezirknummer, nummer, anzahlZaehlungen,
                datumLetzteZaehlung, kreuzungsname);

        final ZaehlstelleTooltipDTO expected = new ZaehlstelleTooltipDTO();
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
