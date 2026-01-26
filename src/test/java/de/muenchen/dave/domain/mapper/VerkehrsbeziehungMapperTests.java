package de.muenchen.dave.domain.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import de.muenchen.dave.domain.dtos.BearbeiteFahrbeziehungKreuzungDTORandomFactory;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.elasticsearch.FahrbeziehungKreuzungRandomFactory;
import de.muenchen.dave.domain.elasticsearch.Verkehrsbeziehung;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class VerkehrsbeziehungMapperTests {

    private final BewegungsbeziehungMapper mapper = new BewegungsbeziehungMapperImpl();

    @Test
    public void testDto2bean() {
        BearbeiteVerkehrsbeziehungDTO dto = BearbeiteFahrbeziehungKreuzungDTORandomFactory.getOne();
        Verkehrsbeziehung bean = this.mapper.dto2Bean(dto);

        assertThat(bean.getVon(), is(equalTo(dto.getVon())));
        assertThat(bean.getNach(), is(equalTo(dto.getNach())));

    }

    @Test
    public void testBean2dto() {
        Verkehrsbeziehung bean = FahrbeziehungKreuzungRandomFactory.getOne();
        BearbeiteVerkehrsbeziehungDTO dto = this.mapper.bean2Dto(bean);

        assertThat(dto.getVon(), is(equalTo(bean.getVon())));
        assertThat(dto.getNach(), is(equalTo(bean.getNach())));
    }
}
