package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.BearbeiteZaehlungDTORandomFactory;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlungSuggestDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.elasticsearch.ZaehlungRandomFactory;
import de.muenchen.dave.domain.enums.Wetter;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.services.IndexServiceUtils;
import de.muenchen.dave.util.DaveConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
public class ZaehlungMapperTests {

    private final ZaehlungMapper mapper = new ZaehlungMapperImpl();

    @Test
    public void testBearbeiteDto2bean() {
        BearbeiteZaehlungDTO dto = BearbeiteZaehlungDTORandomFactory.getOne();
        Zaehlung bean = this.mapper.bearbeiteDto2bean(dto);

        assertThat(bean, hasProperty("datum", equalTo(dto.getDatum())));
        assertThat(bean, hasProperty("zaehlart", equalTo(dto.getZaehlart())));
        assertThat(bean, hasProperty("tagesTyp", equalTo("Wochenende")));
        assertThat(bean, hasProperty("projektNummer", equalTo(dto.getProjektNummer())));
        assertThat(bean, hasProperty("projektName", equalTo(dto.getProjektName())));
        assertThat(bean, hasProperty("kategorien", containsInAnyOrder(dto.getKategorien().toArray())));
        assertThat(bean, hasProperty("zaehlsituation", equalTo(dto.getZaehlsituation())));
        assertThat(bean, hasProperty("zaehlsituationErweitert", equalTo(dto.getZaehlsituationErweitert())));
        assertThat(bean, hasProperty("zaehlIntervall", equalTo(dto.getZaehlIntervall())));
        assertThat(bean, hasProperty("wetter", equalTo(dto.getWetter())));
        assertThat(bean, hasProperty("punkt", equalTo(dto.getPunkt())));

        assertThat(bean, hasProperty("zaehldauer", equalTo(dto.getZaehldauer())));
        assertThat(bean, hasProperty("schulZeiten", equalTo(dto.getSchulZeiten())));
        final List<String> expected = new ArrayList<>(dto.getCustomSuchwoerter());
        expected.addAll(Wetter.valueOf(bean.getWetter()).getSuchwoerter());
        expected.addAll(Zaehldauer.valueOf(bean.getZaehldauer()).getSuchwoerter());
        expected.addAll(bean.getGeographie());
        expected.add(bean.getJahreszeit());
        expected.add(bean.getProjektName());
        expected.add(bean.getJahr());
        expected.add(bean.getMonat());
        expected.add(bean.getTagesTyp());
        expected.add(bean.getDatum().format(IndexServiceUtils.DDMMYYYY));
        expected.add(bean.getDatum().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMANY));

        if (bean.getSonderzaehlung()) {
            expected.add("Sonderzählung");
        }

        assertThat(bean, hasProperty("suchwoerter", containsInAnyOrder(expected.toArray(new String[0]))));

        // Datum mappings (TODO)
        assertThat(bean, hasProperty("jahr", equalTo(String.valueOf(dto.getDatum().getYear()))));
        assertThat(bean, hasProperty("monat", notNullValue()));
        assertThat(bean, hasProperty("jahreszeit", notNullValue()));

        // Sonderzählung mapping
        assertThat(bean, hasProperty("sonderzaehlung", is(dto.isSonderzaehlung())));

        dto.setPunkt(null);
        bean = this.mapper.bearbeiteDto2bean(dto);
        assertThat(bean, hasProperty("punkt", equalTo(new GeoPoint(dto.getLat(), dto.getLng()))));
    }

    @Test
    public void testBean2BearbeiteDto() {
        Zaehlung bean = ZaehlungRandomFactory.getOne();
        BearbeiteZaehlungDTO dto = this.mapper.bean2BearbeiteDto(bean);

        assertThat(dto, hasProperty("datum", equalTo(bean.getDatum())));
        assertThat(dto, hasProperty("zaehlart", equalTo(bean.getZaehlart())));
        assertThat(dto, hasProperty("punkt", equalTo(bean.getPunkt())));
        assertThat(dto, hasProperty("tagesTyp", equalTo("Wochenende")));
        assertThat(dto, hasProperty("projektNummer", equalTo(bean.getProjektNummer())));
        assertThat(dto, hasProperty("projektName", equalTo(bean.getProjektName())));
        assertThat(dto, hasProperty("kategorien", containsInAnyOrder(bean.getKategorien().toArray())));
        assertThat(dto, hasProperty("zaehlsituation", equalTo(bean.getZaehlsituation())));
        assertThat(dto, hasProperty("zaehlsituationErweitert", equalTo(bean.getZaehlsituationErweitert())));
        assertThat(dto, hasProperty("zaehlIntervall", equalTo(bean.getZaehlIntervall())));
        assertThat(dto, hasProperty("wetter", equalTo(bean.getWetter())));
        assertThat(dto, hasProperty("zaehldauer", equalTo(bean.getZaehldauer())));
        assertThat(dto, hasProperty("schulZeiten", equalTo(bean.getSchulZeiten())));
        assertThat(dto, hasProperty("customSuchwoerter", equalTo(bean.getCustomSuchwoerter())));

        assertThat(dto, hasProperty("sonderzaehlung", is(bean.getSonderzaehlung())));
    }

    @Test
    public void testBean2SucheZaehlungsSuggestDto() {
        Zaehlung bean = ZaehlungRandomFactory.getOne();
        SucheZaehlungSuggestDTO dto = this.mapper.bean2SucheZaehlungSuggestDto(bean);

        assertThat(dto, hasProperty("id", equalTo(bean.getId())));
        assertThat(dto, hasProperty("text", equalTo(bean.getDatum().format(DateTimeFormatter.ofPattern(DaveConstants.DATE_FORMAT)) + StringUtils.SPACE + bean.getProjektName())));
    }
}
