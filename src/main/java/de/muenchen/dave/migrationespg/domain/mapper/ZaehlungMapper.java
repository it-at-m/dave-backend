package de.muenchen.dave.migrationespg.domain.mapper;

import de.muenchen.dave.domain.dtos.OpenZaehlungDTO;
import de.muenchen.dave.domain.dtos.OptionsLaengsverkehrDTO;
import de.muenchen.dave.domain.dtos.OptionsQuerungsverkehrDTO;
import de.muenchen.dave.domain.dtos.OptionsVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungVisumDTO;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlungSuggestDTO;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.LaengsverkehrEntity;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.QuerungsverkehrEntity;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.VerkehrsbeziehungEntity;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.migrationespg.services.IndexServiceUtils;
import de.muenchen.dave.migrationespg.util.SuchwortUtil;
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

    abstract BearbeiteZaehlungDTO bean2BearbeiteDto(ZaehlungEntity bean);

    public abstract LadeZaehlungVisumDTO bean2ladeVisumDto(ZaehlungEntity bean);

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

        if (Zaehlart.FJS.name().equals(dto.getZaehlart())) {
            final var laengsverkehr = dto.getLaengsverkehr();
            if (CollectionUtils.isNotEmpty(laengsverkehr)) {
                bean.setLaengsverkehre(new ArrayList<>());
                laengsverkehr.forEach(bearbeiteLaengsverkehrs -> bean.getLaengsverkehre()
                        .add(bewegungsbeziehungMapper.dto2Bean(bearbeiteLaengsverkehrs)));
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

    @AfterMapping
    public void toBearbeiteZaehlungDTO(@MappingTarget BearbeiteZaehlungDTO dto, ZaehlungEntity bean) {

        if (Zaehlart.FJS.name().equals(dto.getZaehlart())) {
            final var laengsverkehr = bean.getLaengsverkehre();
            if (CollectionUtils.isNotEmpty(laengsverkehr)) {
                dto.setLaengsverkehr(new ArrayList<>());
                laengsverkehr.forEach(beanLaengsverkehrs -> dto.getLaengsverkehr()
                        .add(bewegungsbeziehungMapper.bean2Dto(beanLaengsverkehrs)));
            }
        } else if (Zaehlart.QU.name().equals(dto.getZaehlart())) {
            final var querungsverkehr = bean.getQuerungsverkehre();
            if (CollectionUtils.isNotEmpty(querungsverkehr)) {
                dto.setQuerungsverkehr(new ArrayList<>());
                querungsverkehr.forEach(beanQuerungsverkehr -> dto.getQuerungsverkehr()
                        .add(bewegungsbeziehungMapper.bean2Dto(beanQuerungsverkehr)));
            }
        } else {
            final var verkehrsbeziehungen = bean.getVerkehrsbeziehungen();
            if (CollectionUtils.isNotEmpty(verkehrsbeziehungen)) {
                dto.setVerkehrsbeziehungen(new ArrayList<>());
                verkehrsbeziehungen.forEach(beanVerkehrsbeziehung -> dto.getVerkehrsbeziehungen()
                        .add(bewegungsbeziehungMapper.bean2Dto(beanVerkehrsbeziehung)));
            }
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
