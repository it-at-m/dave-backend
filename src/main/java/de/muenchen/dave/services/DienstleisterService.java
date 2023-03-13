/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.dave.services;

import de.muenchen.dave.domain.Dienstleister;
import de.muenchen.dave.domain.dtos.DienstleisterDTO;
import de.muenchen.dave.domain.mapper.DienstleisterMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.DienstleisterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
public class DienstleisterService {

    private final DienstleisterRepository dienstleisterRepository;
    private final DienstleisterMapper dienstleisterMapper;

    private final IndexService indexService;

    public DienstleisterService(final DienstleisterRepository dienstleisterRepository,
                                final DienstleisterMapper dienstleisterMapper,
                                @Lazy final IndexService indexService) {
        this.dienstleisterRepository = dienstleisterRepository;
        this.dienstleisterMapper = dienstleisterMapper;
        this.indexService = indexService;
    }

    /**
     * Service-Methode zum Speichern eines Dienstleisters
     * @param dienstleisterDTO neue oder geaenderte Daten eines Dienstleisters
     * @return gespeicherten Dienstleister als DTO
     */
    public DienstleisterDTO saveOrUpdateDienstleister(final DienstleisterDTO dienstleisterDTO) {
        if(dienstleisterDTO.getId() != null) {
            final Optional<Dienstleister> byId = this.dienstleisterRepository.findById(dienstleisterDTO.getId());
            if(byId.isPresent()) {
                return this.updateDienstleister(byId.get(), dienstleisterDTO);
            }
        }
        return this.saveDienstleister(this.dienstleisterMapper.dto2bean(dienstleisterDTO));
    }

    private DienstleisterDTO updateDienstleister(final Dienstleister toUpdate, final DienstleisterDTO newData) {
        toUpdate.setName(newData.getName());
        toUpdate.setKennung(newData.getKennung());
        toUpdate.setEmailAddresses(newData.getEmailAddresses());
        toUpdate.setActive(newData.isActive());
        return this.saveDienstleister(toUpdate);
    }

    private DienstleisterDTO saveDienstleister(final Dienstleister toSave) {
        return this.dienstleisterMapper.bean2Dto(this.dienstleisterRepository.saveAndFlush(toSave));
    }

    public List<String> getDienstleisterEmailAddressByKennung(final String dienstleisterkennung) {
        final Optional<Dienstleister> dienstleister = this.dienstleisterRepository.findByKennung(dienstleisterkennung);
        if(dienstleister.isPresent()) {
            return dienstleister.get().getEmailAddresses();
        } else {
            return Collections.emptyList();
        }
    }

    public DienstleisterDTO loadDienstleisterByKennung(final String dienstleisterkennung) throws DataNotFoundException {
        final Optional<Dienstleister> dienstleister = this.dienstleisterRepository.findByKennung(dienstleisterkennung);
        if(dienstleister.isPresent()) {
            return this.dienstleisterMapper.bean2Dto(dienstleister.get());
        } else {
            throw new DataNotFoundException(String.format("Der Dienstleister mit Kennung %s existiert nicht", dienstleisterkennung));
        }
    }

    public List<DienstleisterDTO> loadAllDienstleister() {
        return this.addErasableTag(this.dienstleisterRepository.findAll());
    }

    public List<DienstleisterDTO> loadAllActiveDienstleister() {
        return this.addErasableTag(this.dienstleisterRepository.findAllByActiveIsTrue());
    }

    public void deleteDienstleister(final UUID id) throws BrokenInfrastructureException {
        final Optional<Dienstleister> optional = this.dienstleisterRepository.findById(id);
        if(optional.isPresent()) {
            final Dienstleister dienstleister = optional.get();
            if(!this.indexService.existsActiveZaehlungWithDienstleisterkennung(dienstleister.getKennung())) {
                this.dienstleisterRepository.deleteById(id);
            }
        }
    }

    private List<DienstleisterDTO> addErasableTag(final List<Dienstleister> dienstleister) {
        final List<DienstleisterDTO> dienstleisterDTOS = this.dienstleisterMapper.beanList2DtoList(dienstleister);
        dienstleisterDTOS.forEach(dienstleisterDTO -> {
            try {
                dienstleisterDTO.setErasable(!this.indexService.existsActiveZaehlungWithDienstleisterkennung(dienstleisterDTO.getKennung()));
            } catch (final BrokenInfrastructureException e) {
                log.info("Zugriff auf IndexService war nicht möglich.");
                dienstleisterDTO.setErasable(false);
            }
        });
        return dienstleisterDTOS;
    }

}
