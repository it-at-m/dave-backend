package de.muenchen.dave.domain.mapper;

import de.muenchen.dave.domain.dtos.OpenZaehlungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteFahrbeziehungDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteZaehlungDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungVisumDTO;
import de.muenchen.dave.domain.dtos.suche.SucheZaehlungSuggestDTO;
import de.muenchen.dave.domain.elasticsearch.Fahrbeziehung;
import de.muenchen.dave.domain.elasticsearch.Knotenarm;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Zaehlart;
import de.muenchen.dave.services.IndexServiceUtils;
import de.muenchen.dave.util.DaveConstants;
import de.muenchen.dave.util.SuchwortUtil;
import de.muenchen.dave.util.ZaehldatenProcessingUtil;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ZaehlungMapper {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DaveConstants.DATE_FORMAT);

    Zaehlung bearbeiteDto2bean(BearbeiteZaehlungDTO dto);

    BearbeiteZaehlungDTO bean2BearbeiteDto(Zaehlung bean);

    LadeZaehlungVisumDTO bean2ladeVisumDto(Zaehlung bean);

    de.muenchen.dave.domain.analytics.Zaehlung elastic2analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlung analytics,
            Zaehlung elastic);

    @BeforeMapping
    default void beforeElastic2Analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlung analytics) {
        // Ensure all list fields are mutable ArrayLists to avoid UnsupportedOperationException
        // when MapStruct tries to clear them during mapping
        if (analytics.getSuchwoerter() == null) {
            analytics.setSuchwoerter(new ArrayList<>());
        } else if (!(analytics.getSuchwoerter() instanceof ArrayList)) {
            analytics.setSuchwoerter(new ArrayList<>(analytics.getSuchwoerter()));
        }
        
        if (analytics.getCustomSuchwoerter() == null) {
            analytics.setCustomSuchwoerter(new ArrayList<>());
        } else if (!(analytics.getCustomSuchwoerter() instanceof ArrayList)) {
            analytics.setCustomSuchwoerter(new ArrayList<>(analytics.getCustomSuchwoerter()));
        }
        
        if (analytics.getGeographie() == null) {
            analytics.setGeographie(new ArrayList<>());
        } else if (!(analytics.getGeographie() instanceof ArrayList)) {
            analytics.setGeographie(new ArrayList<>(analytics.getGeographie()));
        }
    }

    @AfterMapping
    default void afterElastic2Analytics(@MappingTarget de.muenchen.dave.domain.analytics.Zaehlung analytics, 
            Zaehlung elastic) {
        // Initialize collection if null
        if (analytics.getKnotenarme() == null) {
            analytics.setKnotenarme(new ArrayList<>());
        }
        
        if (elastic.getKnotenarme() == null || elastic.getKnotenarme().isEmpty()) {
            analytics.getKnotenarme().clear();
            return;
        }
        
        // Create a map of existing knotenarme by ID for quick lookup
        Map<UUID, de.muenchen.dave.domain.analytics.Knotenarm> existingKnotenarmeMap = new HashMap<>();
        for (de.muenchen.dave.domain.analytics.Knotenarm k : analytics.getKnotenarme()) {
            if (k.getId() != null) {
                existingKnotenarmeMap.put(k.getId(), k);
            }
        }
        
        // Process incoming knotenarme
        List<de.muenchen.dave.domain.analytics.Knotenarm> updatedKnotenarme = new ArrayList<>();
        for (Knotenarm elasticKnotenarm : elastic.getKnotenarme()) {
            de.muenchen.dave.domain.analytics.Knotenarm analyticsKnotenarm;
            
            if (elasticKnotenarm.getId() != null && !elasticKnotenarm.getId().isBlank()) {
                UUID knotenarmId = UUID.fromString(elasticKnotenarm.getId());
                // Update existing knotenarm
                analyticsKnotenarm = existingKnotenarmeMap.get(knotenarmId);
                if (analyticsKnotenarm == null) {
                    analyticsKnotenarm = new de.muenchen.dave.domain.analytics.Knotenarm();
                }
            } else {
                // Create new knotenarm
                analyticsKnotenarm = new de.muenchen.dave.domain.analytics.Knotenarm();
            }
            
            // Map properties from elastic to analytics
            if (elasticKnotenarm.getId() != null && !elasticKnotenarm.getId().isBlank()) {
                analyticsKnotenarm.setId(UUID.fromString(elasticKnotenarm.getId()));
                analyticsKnotenarm.setVersion(elasticKnotenarm.getVersion());
            }
            analyticsKnotenarm.setNummer(elasticKnotenarm.getNummer());
            analyticsKnotenarm.setStrassenname(elasticKnotenarm.getStrassenname());
            analyticsKnotenarm.setFilename(elasticKnotenarm.getFilename());
        
            // Set bidirectional relationship
            analyticsKnotenarm.setZaehlung(analytics);
            updatedKnotenarme.add(analyticsKnotenarm);
        }
        
        // Clear and replace the collection content (preserves Hibernate wrapper)
        analytics.getKnotenarme().clear();
        analytics.getKnotenarme().addAll(updatedKnotenarme);
    }

    Iterable<de.muenchen.dave.domain.analytics.Zaehlung> elasticlist2analyticslist(Iterable<? extends Zaehlung> elastic);

    Iterable<Zaehlung> analyticslist2elasticlist(Iterable<? extends de.muenchen.dave.domain.analytics.Zaehlung> elastic);

    Zaehlung analytics2elastic(de.muenchen.dave.domain.analytics.Zaehlung analytics);

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
