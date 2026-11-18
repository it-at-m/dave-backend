package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.BearbeiteVerkehrsbeziehungKreuzungDTORandomFactory;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.elasticsearch.VerkehrsbeziehungKreuzungRandomFactory;
import de.muenchen.dave.relational.zaehlstellen.VerkehrsbeziehungEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Slf4j
public class VerkehrsbeziehungMapperTests {

    private final BewegungsbeziehungMapper mapper = new BewegungsbeziehungMapperImpl();

    @Test
    public void testDto2bean() {
        BearbeiteVerkehrsbeziehungDTO dto = BearbeiteVerkehrsbeziehungKreuzungDTORandomFactory.getOne();
        VerkehrsbeziehungEntity bean = this.mapper.dto2Bean(dto);

        assertThat(bean.getVon(), is(equalTo(dto.getVon())));
        assertThat(bean.getNach(), is(equalTo(dto.getNach())));

    }

    @Test
    public void testBean2dto() {
        VerkehrsbeziehungEntity bean = VerkehrsbeziehungKreuzungRandomFactory.getOne();
        BearbeiteVerkehrsbeziehungDTO dto = this.mapper.bean2Dto(bean);

        assertThat(dto.getVon(), is(equalTo(bean.getVon())));
        assertThat(dto.getNach(), is(equalTo(bean.getNach())));
    }
}
