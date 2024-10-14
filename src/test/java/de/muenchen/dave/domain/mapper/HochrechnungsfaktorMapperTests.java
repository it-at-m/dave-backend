package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.BearbeiteHochrechnungsfaktorDTORandomFactory;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.domain.elasticsearch.Hochrechnungsfaktor;
import de.muenchen.dave.domain.elasticsearch.HochrechnungsfaktorRandomFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

@Slf4j
public class HochrechnungsfaktorMapperTests {

    private final HochrechnungsfaktorMapper mapper = new HochrechnungsfaktorMapperImpl();

    @Test
    public void testBean2dto() {
        Hochrechnungsfaktor bean = HochrechnungsfaktorRandomFactory.getOne();
        HochrechnungsfaktorDTO dto = this.mapper.beanElastic2Dto(bean);

        assertThat(dto, hasProperty("kfz", equalTo(bean.getKfz())));
        assertThat(dto, hasProperty("sv", equalTo(bean.getSv())));
        assertThat(dto, hasProperty("gv", equalTo(bean.getGv())));
    }

    @Test
    public void testDto2bean() {
        HochrechnungsfaktorDTO dto = BearbeiteHochrechnungsfaktorDTORandomFactory.getOne();
        Hochrechnungsfaktor bean = this.mapper.dto2beanElastic(dto);

        assertThat(bean, hasProperty("kfz", equalTo(dto.getKfz())));
        assertThat(bean, hasProperty("sv", equalTo(dto.getSv())));
        assertThat(bean, hasProperty("gv", equalTo(dto.getGv())));

    }
}
