package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.OpenZaehlungDTO;
import de.muenchen.dave.domain.dtos.OptionsLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.OptionsQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.OptionsVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungVisumDTO;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlungSuggestDTO;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.relational.zaehlstellen.LaengsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.QuerungsverkehrEntity;
import de.muenchen.dave.relational.zaehlstellen.VerkehrsbeziehungEntity;
import de.muenchen.dave.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.services.IndexServiceUtils;
import de.muenchen.dave.util.SuchwortUtil;
import de.muenchen.dave.util.DaveConstants;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class ZaehlungMapper {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DaveConstants.DATE_FORMAT);

    @Autowired
    public BewegungsbeziehungMapper bewegungsbeziehungMapper;

    public abstract ZaehlungEntity bearbeiteDto2bean(BearbeiteZaehlungDTO dto);

    public abstract LadeZaehlungVisumDTO bean2ladeVisumDto(ZaehlungEntity bean);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "knotenarme", ignore = true)
    @Mapping(target = "verkehrsbeziehungen", ignore = true)
    @Mapping(target = "laengsverkehre", ignore = true)
    @Mapping(target = "querungsverkehre", ignore = true)
    public abstract void updateEntity(
            BearbeiteZaehlungDTO dto,
            @MappingTarget ZaehlungEntity entity
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "hochrechnungsfaktor", ignore = true)
    public abstract void updateEntity(
            BearbeiteVerkehrsbeziehungDTO dto,
            @MappingTarget VerkehrsbeziehungEntity entity
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "hochrechnungsfaktor", ignore = true)
    public abstract void updateEntity(
            BearbeiteLaengsverkehrDTO dto,
            @MappingTarget LaengsverkehrEntity entity
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "hochrechnungsfaktor", ignore = true)
    public abstract void updateEntity(
            BearbeiteQuerungsverkehrDTO dto,
            @MappingTarget QuerungsverkehrEntity entity
    );

   @AfterMapping
   public void toZaehlung(@MappingTarget ZaehlungEntity bean, BearbeiteZaehlungDTO dto) {
        if (ObjectUtils.isNotEmpty(dto.getDatum())) {
            bean.setJahr(String.valueOf(dto.getDatum().getYear()));
            bean.setMonat(ZaehldatenProcessingUtil.getMonatTextuell(dto.getDatum()));
            bean.setJahreszeit(IndexServiceUtils.jahreszeitenDetector(dto.getDatum()));
            bean.setTagesTyp(IndexServiceUtils.getTagesTyp(dto.getDatum()));
        }

        if (dto.getLat() > 0 && dto.getLng() > 0) {
            bean.setLatitude(dto.getLat());
            bean.setLongitude(dto.getPunkt().getLon());
        }

        if (StringUtils.isNotEmpty(dto.getZaehlart())) {
            bean.setGeographie(Zaehlart.getBedeutungForZaehlart(dto.getZaehlart()));
        }

        bean.setKreuzungsname(IndexServiceUtils.createKreuzungsname(bean.getKreuzungsname(), bean));

        final Set<String> generatedSuchwoerter = SuchwortUtil.generateSuchworteOfZaehlung(bean);

        if (CollectionUtils.isEmpty(bean.getSuchwoerter())) {
            bean.setSuchwoerter(new ArrayList<>());
        }
        if (CollectionUtils.isNotEmpty(generatedSuchwoerter)) {
            bean.getSuchwoerter().addAll(generatedSuchwoerter);
        }
        if (CollectionUtils.isNotEmpty(dto.getCustomSuchwoerter())) {
            bean.getSuchwoerter().addAll(dto.getCustomSuchwoerter());
        }
    }

    abstract LadeZaehlungDTO bean2LadeDto(ZaehlungEntity bean);

    @AfterMapping
    public void toLadeZaehlungDTO(@MappingTarget LadeZaehlungDTO dto, ZaehlungEntity bean) {
        dto.setLat(bean.getLatitude());
        dto.setLng(bean.getLongitude());
    }

    @AfterMapping
    public void toZaehlung(@MappingTarget ZaehlungEntity bean, LadeZaehlungDTO dto) {
        bean.setJahr(String.valueOf(dto.getDatum().getYear()));
        bean.setMonat(ZaehldatenProcessingUtil.getMonatTextuell(dto.getDatum()));
        bean.setJahreszeit(IndexServiceUtils.jahreszeitenDetector(dto.getDatum()));

        bean.setLatitude(dto.getLat());
        bean.setLongitude(dto.getLng());

        if (Zaehlart.FJS.name().equals(dto.getZaehlart())) {
            final var laengsverkehr = dto.getLaengsverkehr();
            if (CollectionUtils.isNotEmpty(laengsverkehr)) {
                bean.setLaengsverkehre(new ArrayList<>());
                laengsverkehr.forEach(bearbeiteLaengsverkehr -> bean.getLaengsverkehre()
                        .add(bewegungsbeziehungMapper.dto2Bean(bearbeiteLaengsverkehr)));
            }
        } else if (Zaehlart.QU.name().equals(dto.getZaehlart())) {
            final var querungsverkehr = dto.getQuerungsverkehr();
            if (CollectionUtils.isNotEmpty(querungsverkehr)) {
                bean.setQuerungsverkehre(new ArrayList<>());
                querungsverkehr.forEach(bearbeiteQuerungsverkehr -> bean.getQuerungsverkehre()
                        .add(bewegungsbeziehungMapper.dto2Bean(bearbeiteQuerungsverkehr)));
            }
        } else {
            final var verkehrsbeziehungen = dto.getVerkehrsbeziehungen();
            if (CollectionUtils.isNotEmpty(verkehrsbeziehungen)) {
                bean.setVerkehrsbeziehungen(new ArrayList<>());
                verkehrsbeziehungen.forEach(bearbeiteVerkehrsbeziehung -> bean.getVerkehrsbeziehungen()
                        .add(bewegungsbeziehungMapper.dto2Bean(bearbeiteVerkehrsbeziehung)));
            }
        }
    }

    public abstract SucheZaehlungSuggestDTO bean2SucheZaehlungSuggestDto(ZaehlungEntity bean);

    @AfterMapping
    public void toSucheZaehlungSuggestDTO(@MappingTarget SucheZaehlungSuggestDTO dto, ZaehlungEntity bean) {
        dto.setText(bean.getDatum().format(DATE_TIME_FORMATTER) + StringUtils.SPACE + bean.getProjektName());
    }

    public abstract OpenZaehlungDTO bean2OpenZaehlungDto(final ZaehlungEntity bean);

    public abstract List<OptionsVerkehrsbeziehungDTO> mapVerkehrsbeziehungen(final List<VerkehrsbeziehungEntity> verkehrsbeziehungen);

    public abstract List<OptionsQuerungsverkehrDTO> mapQuerungsverkehre(final List<QuerungsverkehrEntity> querungsverkehre);

    public abstract List<OptionsLaengsverkehrDTO> mapLaengsverkehre(final List<LaengsverkehrEntity> laengsverkehre);
}
