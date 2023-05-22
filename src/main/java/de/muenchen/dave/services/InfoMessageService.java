/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.dave.services;

import de.muenchen.dave.domain.InfoMessage;
import de.muenchen.dave.domain.dtos.InfoMessageDTO;
import de.muenchen.dave.domain.mapper.InfoMessageMapper;
import de.muenchen.dave.repositories.relationaldb.InfoMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class InfoMessageService {

    private static final int MAX_NUMBER_OF_INACTIVE_MESSAGES = 10;

    private final InfoMessageRepository infoMessageRepository;

    private final InfoMessageMapper infoMessageMapper;

    public InfoMessageService(final InfoMessageRepository infoMessageRepository,
            final InfoMessageMapper infoMessageMapper) {
        this.infoMessageRepository = infoMessageRepository;
        this.infoMessageMapper = infoMessageMapper;
    }

    /**
     * Die Methode speichert eine InfoMessage ab und gibt alle vorhandene
     * {@link InfoMessageDTO}s als Liste zurück, beginnend mit der
     * neusten aktiven InfoMessage.
     *
     * @param infoMessageDTO Das {@link InfoMessageDTO} zum Speichern.
     * @return die {@link InfoMessageDTO}s
     */
    public List<InfoMessageDTO> saveInfoMessage(final InfoMessageDTO infoMessageDTO) {
        final InfoMessage infoMessage;
        if (ObjectUtils.isEmpty(infoMessageDTO.getId())) {
            setAllInfoMessagesInactiveAndDeleteInactiveExceptAllowedInfoMessages();
            infoMessage = new InfoMessage();
        } else {
            infoMessage = infoMessageRepository.findById(infoMessageDTO.getId())
                    .orElseGet(() -> {
                        setAllInfoMessagesInactiveAndDeleteInactiveExceptAllowedInfoMessages();
                        return new InfoMessage();
                    });
        }
        infoMessage.setContent(infoMessageDTO.getContent());
        infoMessage.setGueltigVon(infoMessageDTO.getGueltigVon());
        infoMessage.setGueltigBis(infoMessageDTO.getGueltigBis());
        infoMessage.setAktiv(true);
        infoMessageRepository.saveAndFlush(infoMessage);
        return loadAllInfoMessages();
    }

    public InfoMessageDTO loadActiveInfoMessage() {
        final var infoMessage = infoMessageRepository.findTopByAktivIsTrueOrderByCreatedTimeDesc()
                .orElseGet(this::getEmptyInfoMessage);
        return infoMessageMapper.bean2Dto(infoMessage);

    }

    public List<InfoMessageDTO> loadAllInfoMessages() {
        final var infoMessages = infoMessageRepository.findAllByOrderByCreatedTimeDesc();
        final var isInfoMessageEmpty = infoMessages.stream()
                .noneMatch(InfoMessage::getAktiv);
        if (isInfoMessageEmpty) {
            infoMessages.add(0, getEmptyInfoMessage());
        }
        return infoMessages.stream()
                .map(infoMessageMapper::bean2Dto)
                .collect(Collectors.toList());
    }

    public void setAllInfoMessagesInactiveAndDeleteInactiveExceptAllowedInfoMessages() {
        infoMessageRepository.findAllByAktivIsTrue()
                .forEach(infoMessage -> {
                    infoMessage.setAktiv(false);
                    infoMessageRepository.save(infoMessage);
                });
        deleteInactiveInfoMessagesExceptAllowedInfoMessages();
    }

    public void deleteInactiveInfoMessagesExceptAllowedInfoMessages() {
        final var infoMessages = infoMessageRepository.findAllByOrderByCreatedTimeDesc();
        IntStream.range(0, infoMessages.size())
                .filter(index -> index > MAX_NUMBER_OF_INACTIVE_MESSAGES - 1)
                .forEach(index -> infoMessageRepository.delete(infoMessages.get(index)));
    }

    private InfoMessage getEmptyInfoMessage() {
        var infoMessage = new InfoMessage();
        infoMessage.setAktiv(true);
        return infoMessage;
    }

}
