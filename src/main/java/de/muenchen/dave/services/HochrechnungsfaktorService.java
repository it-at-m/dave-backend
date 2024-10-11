/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.dave.services;

import de.muenchen.dave.domain.Hochrechnungsfaktor;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.domain.mapper.HochrechnungsfaktorMapper;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.HochrechnungsfaktorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HochrechnungsfaktorService {

    private final String SORTING_ATTRIBUTE = "defaultFaktor";

    private final HochrechnungsfaktorRepository hochrechnungsfaktorRepository;

    private final HochrechnungsfaktorMapper hochrechnungsfaktorMapper;

    public HochrechnungsfaktorService(final HochrechnungsfaktorRepository hochrechnungsfaktorRepository,
            final HochrechnungsfaktorMapper hochrechnungsfaktorMapper) {
        this.hochrechnungsfaktorRepository = hochrechnungsfaktorRepository;
        this.hochrechnungsfaktorMapper = hochrechnungsfaktorMapper;
    }

    /**
     * Setzte die fachlichen Attribute des im Parameter gegebenen DTOs in der im zweiten Parameter übergebene Entität.
     *
     * @param hofaDto    DTO
     * @param hofaEntity Bean
     * @return die im zweiten Parameter übergebene Entität mit den fachlichen Daten der DTO.
     */
    public static Hochrechnungsfaktor setDtoDataToEntity(final HochrechnungsfaktorDTO hofaDto,
            final Hochrechnungsfaktor hofaEntity) {
        hofaEntity.setMatrix(hofaDto.getMatrix());
        hofaEntity.setKfz(hofaDto.getKfz());
        hofaEntity.setSv(hofaDto.getSv());
        hofaEntity.setGv(hofaDto.getGv());
        hofaEntity.setActive(hofaDto.isActive());
        hofaEntity.setDefaultFaktor(hofaDto.isDefaultFaktor());
        return hofaEntity;
    }

    /**
     * Diese Methode speichert/aktualisiert einen {@link Hochrechnungsfaktor} in der Relationalen Datenbank.
     * <p>
     * Die Aktualisierung findet statt, sobald im DTO eine Id vorhanden ist. Ansonsten wird ein neues Objekt angelegt.
     *
     * @param hochrechnungsfaktorDTO hochrechnungsfaktorDTO
     * @return die gespeicherte {@link Hochrechnungsfaktor} als {@link HochrechnungsfaktorDTO}.
     * @throws DataNotFoundException Wenn keine Daten gelesen werden konnten
     */
    public HochrechnungsfaktorDTO saveHochrechnungsfaktor(final HochrechnungsfaktorDTO hochrechnungsfaktorDTO) throws DataNotFoundException {
        Hochrechnungsfaktor hochrechnungsfaktor;
        if (ObjectUtils.isEmpty(hochrechnungsfaktorDTO.getId())) {
            hochrechnungsfaktor = hochrechnungsfaktorMapper.dto2bean(hochrechnungsfaktorDTO);
        } else {
            hochrechnungsfaktor = hochrechnungsfaktorRepository.findById(hochrechnungsfaktorDTO.getId())
                    .orElseThrow(() -> new DataNotFoundException("Id existiert nicht."));
            hochrechnungsfaktor = setDtoDataToEntity(hochrechnungsfaktorDTO, hochrechnungsfaktor);
        }
        hochrechnungsfaktor = hochrechnungsfaktorRepository.saveAndFlush(hochrechnungsfaktor);
        return hochrechnungsfaktorMapper.bean2Dto(hochrechnungsfaktor);
    }

    /**
     * Diese Methode extrahiert die gespeicherten {@link Hochrechnungsfaktor}en und gibt diese zurück.
     *
     * @return die gespeicherten {@link Hochrechnungsfaktor} als {@link HochrechnungsfaktorDTO}.
     * @throws DataNotFoundException Wenn keine Daten gelesen werden konnten
     */
    public List<HochrechnungsfaktorDTO> getHochrechnungsfaktoren() throws DataNotFoundException {
        return hochrechnungsfaktorRepository.findAll(Sort.by(SORTING_ATTRIBUTE).descending())
                .stream()
                .map(hochrechnungsfaktorMapper::bean2Dto)
                .collect(Collectors.toList());
    }

    public void deleteHochrechnungsfaktor(final UUID id) {
        hochrechnungsfaktorRepository.deleteById(id);
    }

}
