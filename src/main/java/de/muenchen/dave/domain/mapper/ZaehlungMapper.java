package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.OpenZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteFahrbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungVisumDTO;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlungSuggestDTO;
import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.services.IndexServiceUtils;
import de.muenchen.dave.util.DaveConstants;
import de.muenchen.dave.util.SuchwortUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ZaehlungMapper {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DaveConstants.DATE_FORMAT);

    Zaehlung bearbeiteDto2bean(BearbeiteZaehlungDTO dto);

    BearbeiteZaehlungDTO bean2BearbeiteDto(Zaehlung bean);

    LadeZaehlungVisumDTO bean2ladeVisumDto(Zaehlung bean);

    @AfterMapping
    default void toZaehlung(@MappingTarget Zaehlung bean, BearbeiteZaehlungDTO dto) {
        if (ObjectUtils.isNotEmpty(dto.getDatum())) {
            bean.setJahr(String.valueOf(dto.getDatum().getYear()));
            bean.setMonat(ZaehldatenProcessingUtil.getMonatTextuell(dto.getDatum()));
            bean.setJahreszeit(IndexServiceUtils.jahreszeitenDetector(dto.getDatum()));
            bean.setTagesTyp(IndexServiceUtils.getTagesTyp(dto.getDatum()));
        }

        if (dto.getLat() > 0 && dto.getLng() > 0) {
            bean.setPunkt(new GeoPoint(dto.getLat(), dto.getLng()));
        }

        if (dto.getPunkt() != null && dto.getPunkt().getLat() > 0 && dto.getPunkt().getLon() > 0) {
            bean.setPunkt(dto.getPunkt());
        }

        final List<BearbeiteFahrbeziehungDTO> fahrbeziehungenDTO = dto.getFahrbeziehungen();
        if (CollectionUtils.isNotEmpty(fahrbeziehungenDTO)) {
            bean.setFahrbeziehungen(new ArrayList<>());
            fahrbeziehungenDTO.forEach(fahr -> bean.getFahrbeziehungen().add(new FahrbeziehungMapperImpl().bearbeiteFahrbeziehungDto2bean(fahr)));
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
    default void toBearbeiteZaehlungDTO(@MappingTarget BearbeiteZaehlungDTO dto, Zaehlung bean) {

        final List<Fahrbeziehung> fahrbeziehungenBean = bean.getFahrbeziehungen();
        if (CollectionUtils.isNotEmpty(fahrbeziehungenBean)) {
            dto.setFahrbeziehungen(new ArrayList<>());
            fahrbeziehungenBean.forEach(fahr -> dto.getFahrbeziehungen().add(new FahrbeziehungMapperImpl().bean2bearbeiteFahrbeziehunDto(fahr)));
        }
    }

    LadeZaehlungDTO bean2LadeDto(Zaehlung bean);

    @AfterMapping
    default void toLadeZaehlungDTO(@MappingTarget LadeZaehlungDTO dto, Zaehlung bean) {
        dto.setLat(bean.getPunkt().getLat());
        dto.setLng(bean.getPunkt().getLon());
    }

    @AfterMapping
    default void toZaehlung(@MappingTarget Zaehlung bean, LadeZaehlungDTO dto) {
        bean.setJahr(String.valueOf(dto.getDatum().getYear()));
        bean.setMonat(ZaehldatenProcessingUtil.getMonatTextuell(dto.getDatum()));
        bean.setJahreszeit(IndexServiceUtils.jahreszeitenDetector(dto.getDatum()));

        bean.setPunkt(new GeoPoint(dto.getLat(), dto.getLng()));

        final List<BearbeiteFahrbeziehungDTO> fahrbeziehungenDTO = dto.getFahrbeziehungen();
        if (CollectionUtils.isNotEmpty(fahrbeziehungenDTO)) {
            bean.setFahrbeziehungen(new ArrayList<>());
            fahrbeziehungenDTO.forEach(fahr -> bean.getFahrbeziehungen().add(new FahrbeziehungMapperImpl().bearbeiteFahrbeziehungDto2bean(fahr)));
        }
    }

    SucheZaehlungSuggestDTO bean2SucheZaehlungSuggestDto(Zaehlung bean);

    @AfterMapping
    default void toSucheZaehlungSuggestDTO(@MappingTarget SucheZaehlungSuggestDTO dto, Zaehlung bean) {
        dto.setText(bean.getDatum().format(DATE_TIME_FORMATTER) + StringUtils.SPACE + bean.getProjektName());
    }

    OpenZaehlungDTO bean2OpenZaehlungDto(Zaehlung bean);
}
