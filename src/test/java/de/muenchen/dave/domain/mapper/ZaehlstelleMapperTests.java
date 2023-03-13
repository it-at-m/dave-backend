package de.muenchen.dave.domain.mapper;

import com.google.common.base.Splitter;
import de.muenchen.dave.domain.dtos.BearbeiteZaehlstelleDTORandomFactory;
import de.muenchen.dave.domain.dtos.LeseZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlstelleDTO;
import de.muenchen.dave.domain.dtos.laden.LadeAuswertungZaehlstelleKoordinateDTO;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlstelleSuggestDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.ZaehlstelleRandomFactory;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
public class ZaehlstelleMapperTests {

    private final ZaehlstelleMapper mapper = new ZaehlstelleMapperImpl();

    @Test
    public void testBearbeiteDto2bean() {
        BearbeiteZaehlstelleDTO dto = BearbeiteZaehlstelleDTORandomFactory.getOne();
        Zaehlstelle bean = this.mapper.bearbeiteDto2bean(dto);

        final Set<String> stadtbezirke = new HashSet<>(Splitter.on("-").omitEmptyStrings().trimResults().splitToList(bean.getStadtbezirk()));
        final List<String> expected = new ArrayList<>(dto.getCustomSuchwoerter());
        if (stadtbezirke.size() > 1) {
            expected.addAll(stadtbezirke);
        }
        expected.add(bean.getStadtbezirk());
        assertThat(bean, hasProperty("nummer", equalTo(dto.getNummer())));
        assertThat(bean, hasProperty("stadtbezirkNummer", equalTo(dto.getStadtbezirkNummer())));

        assertThat(bean, hasProperty("suchwoerter", containsInAnyOrder(expected.toArray(new String[0]))));
        assertThat(bean, hasProperty("punkt", equalTo(dto.getPunkt())));

        dto.setPunkt(null);
        bean = this.mapper.bearbeiteDto2bean(dto);
        assertThat(bean, hasProperty("punkt", equalTo(new GeoPoint(dto.getLat(), dto.getLng()))));

        assertThat(bean, hasProperty("zaehlungen", notNullValue()));
        assertThat(bean.getZaehlungen().size(), equalTo(dto.getZaehlungen().size()));
        assertThat(bean.getZaehlungen().get(0).getKreuzungsname(), equalTo(dto.getZaehlungen().get(0).getKreuzungsname()));
    }

    @Test
    public void testBean2BearbeiteDto() {
        Zaehlstelle bean = ZaehlstelleRandomFactory.getOne();
        BearbeiteZaehlstelleDTO dto = this.mapper.bean2bearbeiteDto(bean);

        assertThat(dto, hasProperty("nummer", equalTo(bean.getNummer())));
        assertThat(dto, hasProperty("stadtbezirkNummer", equalTo(bean.getStadtbezirkNummer())));
        assertThat(dto, hasProperty("customSuchwoerter", equalTo(bean.getCustomSuchwoerter())));
        assertThat(dto, hasProperty("punkt", equalTo(bean.getPunkt())));

        assertThat(dto, hasProperty("zaehlungen", notNullValue()));
        assertThat(dto.getZaehlungen().size(), equalTo(bean.getZaehlungen().size()));
        assertThat(dto.getZaehlungen().get(0).getKreuzungsname(), equalTo(bean.getZaehlungen().get(0).getKreuzungsname()));
    }

    @Test
    public void testToSucheCounterSuggestDto() {
        Zaehlstelle bean = ZaehlstelleRandomFactory.getOne();
        SucheZaehlstelleSuggestDTO dto = this.mapper.bean2SucheZaehlstelleSuggestDto(bean);

        assertThat(dto, hasProperty("id", equalTo(bean.getId())));
        assertThat(dto, hasProperty("text", equalTo(bean.getNummer() + StringUtils.SPACE + bean.getStadtbezirk())));
    }

    @Test
    public void testBean2LeseZaehlstelleDto() {
        Zaehlstelle bean = ZaehlstelleRandomFactory.getOne();
        LeseZaehlstelleDTO dto = this.mapper.bean2LeseZaehlstelleDto(bean);

        // Check Zählstelle
        assertThat(dto.getId(), is(equalTo(bean.getId())));
        assertThat(dto.getNummer(), is(equalTo(bean.getNummer())));
        assertThat(dto.getStadtbezirk(), is(equalTo(bean.getStadtbezirk())));
        assertThat(dto.getStadtbezirkNummer(), is(equalTo(bean.getStadtbezirkNummer())));
        assertThat(dto.getLat(), is(equalTo(bean.getPunkt().getLat())));
        assertThat(dto.getLng(), is(equalTo(bean.getPunkt().getLon())));

        // Check Zählungen
        assertThat(dto.getZaehlungen().size(), is(equalTo(bean.getZaehlungen().size())));

    }

    @Test
    public void bean2LadeAuswertungZaehlstelleKoordinateDto() {
        final Zaehlstelle zaehlstelle = new Zaehlstelle();
        zaehlstelle.setZaehlungen(new ArrayList<>());
        zaehlstelle.setNummer("123456");
        zaehlstelle.setKommentar("Ein Kommentar");
        zaehlstelle.setPunkt(new GeoPoint(48.1234567, 10.1234567));
        Zaehlung zaehlung = new Zaehlung();
        zaehlung.setDatum(LocalDate.of(2019, 1, 1));
        zaehlstelle.getZaehlungen().add(zaehlung);

        final LadeAuswertungZaehlstelleKoordinateDTO result =
                mapper.bean2LadeAuswertungZaehlstelleKoordinateDto(zaehlstelle);
        final LadeAuswertungZaehlstelleKoordinateDTO expected = new LadeAuswertungZaehlstelleKoordinateDTO();
        expected.setNummer("123456");
        expected.setKommentar("Ein Kommentar");
        expected.setLetzteZaehlung(LocalDate.of(2019, 1, 1));
        expected.setLat(48.1234567);
        expected.setLng(10.1234567);
        assertThat(result, is(expected));
    }

}
