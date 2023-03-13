package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.BearbeiteFahrbeziehungKreuzungDTORandomFactory;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteFahrbeziehungDTO;
import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
import de.muenchen.dave.domain.elasticsearch.FahrbeziehungKreuzungRandomFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Slf4j
public class FahrbeziehungMapperTests {

    private final FahrbeziehungMapper mapper = new FahrbeziehungMapperImpl();

    @Test
    public void testDto2bean() {
        BearbeiteFahrbeziehungDTO dto = BearbeiteFahrbeziehungKreuzungDTORandomFactory.getOne();
        Fahrbeziehung bean = this.mapper.bearbeiteFahrbeziehungDto2bean(dto);

        assertThat(bean.getVon(), is(equalTo(dto.getVon())));
        assertThat(bean.getNach(), is(equalTo(dto.getNach())));

    }

    @Test
    public void testBean2dto() {
        Fahrbeziehung bean = FahrbeziehungKreuzungRandomFactory.getOne();
        BearbeiteFahrbeziehungDTO dto = this.mapper.bean2bearbeiteFahrbeziehunDto(bean);

        assertThat(dto.getVon(), is(equalTo(bean.getVon())));
        assertThat(dto.getNach(), is(equalTo(bean.getNach())));
    }
}
