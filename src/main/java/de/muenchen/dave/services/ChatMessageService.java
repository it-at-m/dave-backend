/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.dave.services;

import de.muenchen.dave.domain.ChatMessage;
import de.muenchen.dave.domain.dtos.ChatMessageDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.UpdateStatusDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Participant;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.domain.mapper.ChatMessageMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.repositories.relationaldb.ChatMessageRepository;
import de.muenchen.dave.services.email.EmailSendService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageService {

    public static final String MESSAGE_TYPE = "text";
    public static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    private static final boolean SEND_EMAIL_IF_UPDATE_EXTERNAL_ZAEHLUNG = false;
    private static final boolean SEND_EMAIL_IF_UPDATE_INTERNAL_ZAEHLUNG = false;
    private static final List<String> UPDATE_FROM_EXTERNAL_FOR_STATUS = Arrays.asList(Status.ACCOMPLISHED.name(), Status.COUNTING.name());
    private static final List<String> SEND_EMAIL_TO_EXTERNAL_FOR_STATUS = Arrays.asList(Status.INSTRUCTED.name(), Status.COUNTING.name(),
            Status.CORRECTION.name());

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final IndexService indexService;
    private final EmailSendService emailSendService;

    public ChatMessageService(final ChatMessageRepository chatMessageRepository,
            final ChatMessageMapper chatMessageMapper,
            final IndexService indexService,
            final EmailSendService emailSendService) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatMessageMapper = chatMessageMapper;
        this.indexService = indexService;
        this.emailSendService = emailSendService;
    }

    /**
     * Die Methode speichert eine Chat Nachricht ab. Ist die Nachricht vom Dienstleister oder liegt die
     * Zählung
     * beim Dienstleister, dann wird auch eine E-Mail versendet.
     *
     * @param chatMessageDTO Das {@link ChatMessageDTO} zum Speichern.
     * @return Das gespeicherte {@link ChatMessageDTO}.
     * @throws BrokenInfrastructureException Bei Verbindungsfehlern
     * @throws DataNotFoundException Wenn Daten nicht geladen werden konnten
     */
    public ChatMessageDTO saveChatMessage(final ChatMessageDTO chatMessageDTO) throws BrokenInfrastructureException, DataNotFoundException {
        ChatMessage chatMessage = chatMessageMapper.dto2bean(chatMessageDTO);
        chatMessage = chatMessageRepository.saveAndFlush(chatMessage);

        final Zaehlung zaehlung = indexService.getZaehlung(chatMessage.getZaehlungId().toString());
        if (chatMessage.getParticipantId() == Participant.DIENSTLEISTER.getParticipantId() ||
                SEND_EMAIL_TO_EXTERNAL_FOR_STATUS.contains(zaehlung.getStatus())) {
            emailSendService.sendEmail(chatMessage);
        }

        final Participant participant = (chatMessageDTO.getParticipantId() == Participant.DIENSTLEISTER.getParticipantId()) ? Participant.MOBILITAETSREFERAT
                : Participant.DIENSTLEISTER;
        indexService.setUnreadMessagesInZaehlungForParticipant(chatMessageDTO.getZaehlungId(), participant, true);
        return chatMessageMapper.bean2Dto(chatMessage);
    }

    /**
     * Die Methode lädt alle ChatMessages zu einer ZaehlungID.
     *
     * @param zaehlungID Die ID der Zählung zum Laden der ChatMessage.
     * @return Alle geladenen {@link ChatMessageDTO}s.
     */
    public List<ChatMessageDTO> loadChatMessages(final UUID zaehlungID) {
        final List<ChatMessage> messages = chatMessageRepository.findAllByZaehlungIdOrderByTimestampAsc(zaehlungID);
        return chatMessageMapper.beanList2DtoList(messages);
    }

    public void saveUpdateMessageForZaehlungStatus(final String zaehlungId, final UpdateStatusDTO updateStatusDTO) throws BrokenInfrastructureException {
        final ChatMessage message = new ChatMessage();
        message.setZaehlungId(UUID.fromString(zaehlungId));
        boolean sendEmail = false;
        if (Status.CREATED.name().equalsIgnoreCase(updateStatusDTO.getStatus())) {
            message.setContent("Die Zählung wurde angelegt.");
        } else if (Status.INSTRUCTED.name().equalsIgnoreCase(updateStatusDTO.getStatus())) {
            message.setContent("Die Zählung wurde beauftragt.");
            sendEmail = true;
        } else if (Status.COUNTING.name().equalsIgnoreCase(updateStatusDTO.getStatus())) {
            message.setContent("Die Zählung wird durchgeführt.");
        } else if (Status.ACCOMPLISHED.name().equalsIgnoreCase(updateStatusDTO.getStatus())) {
            message.setContent("Die Zählung wurde abgeschlossen.");
            sendEmail = true;
        } else if (Status.CORRECTION.name().equalsIgnoreCase(updateStatusDTO.getStatus())) {
            message.setContent("Die Zählung soll korrigiert werden.");
            sendEmail = true;
        } else if (Status.ACTIVE.name().equalsIgnoreCase(updateStatusDTO.getStatus())) {
            message.setContent("Die Zählung wurde freigegeben.");
        } else if (Status.INACTIVE.name().equalsIgnoreCase(updateStatusDTO.getStatus())) {
            message.setContent("Die Zählung wurde deaktiviert.");
        } else {
            // Nichts tun, wenn kein passender Status dabei
            return;
        }

        Participant participant;
        if (UPDATE_FROM_EXTERNAL_FOR_STATUS.contains(updateStatusDTO.getStatus())) {
            participant = Participant.DIENSTLEISTER;
        } else {
            participant = Participant.MOBILITAETSREFERAT;
        }
        message.setParticipantId(participant.getParticipantId());
        saveUpdateMessage(message, zaehlungId, participant, sendEmail);
    }

    public void saveUpdateMessageForZaehlungExternal(final String zaehlungId) throws BrokenInfrastructureException {
        final ChatMessage message = new ChatMessage();
        message.setZaehlungId(UUID.fromString(zaehlungId));
        message.setParticipantId(Participant.DIENSTLEISTER.getParticipantId());
        message.setContent("Die Zählung wurde aktualisiert.");
        saveUpdateMessage(message, zaehlungId, Participant.DIENSTLEISTER, SEND_EMAIL_IF_UPDATE_EXTERNAL_ZAEHLUNG);
    }

    public void saveUpdateMessageForZaehlungInternal(final String zaehlungId) throws BrokenInfrastructureException {
        final ChatMessage message = new ChatMessage();
        message.setZaehlungId(UUID.fromString(zaehlungId));
        message.setParticipantId(Participant.MOBILITAETSREFERAT.getParticipantId());
        message.setContent("Die Zählung wurde aktualisiert.");
        saveUpdateMessage(message, zaehlungId, Participant.MOBILITAETSREFERAT, SEND_EMAIL_IF_UPDATE_INTERNAL_ZAEHLUNG);
    }

    public void saveUpdateMessage(final ChatMessage message, final String zaehlungId, final Participant callingParticipant,
            final boolean sendEmail) throws BrokenInfrastructureException {
        message.setTimestamp(LocalDateTime.now(ChatMessageService.ZONE));
        message.setUploaded(true);
        message.setViewed(false);
        message.setType(MESSAGE_TYPE);

        final Participant participant = callingParticipant.equals(Participant.DIENSTLEISTER) ? Participant.MOBILITAETSREFERAT : Participant.DIENSTLEISTER;
        indexService.setUnreadMessagesInZaehlungForParticipant(zaehlungId, participant, true);
        chatMessageRepository.saveAndFlush(message);

        if (sendEmail) {
            emailSendService.sendEmail(message);
        }
    }

    /**
     * In dieser Methode werden alle ChatMessages zur übergebenen Zählung zum übergebenen Participant
     * auf viewed = true gesetzt.
     *
     * @param zaehlungId Zählungs-ID der Zählung
     * @param callingParticipantId Participant dessen Messages auf viewed gestellt werden sollen.
     * @return Liste mit allen Chatnachrichten zu einer Zählung
     * @throws BrokenInfrastructureException Bei Verbindungsfehlern
     */
    public List<ChatMessage> updateUnreadMessages(final String zaehlungId, final Integer callingParticipantId) throws BrokenInfrastructureException {
        final Participant participant = (callingParticipantId == Participant.DIENSTLEISTER.getParticipantId()) ? Participant.DIENSTLEISTER
                : Participant.MOBILITAETSREFERAT;
        indexService.setUnreadMessagesInZaehlungForParticipant(zaehlungId, participant, false);
        final List<ChatMessage> messages = chatMessageRepository.findAllByZaehlungIdOrderByTimestampAsc(UUID.fromString(zaehlungId));
        messages.stream()
                .filter(chatMessage -> !chatMessage.getParticipantId().equals(callingParticipantId))
                .forEach(chatMessage -> chatMessage.setViewed(true));
        return chatMessageRepository.saveAll(messages);
    }
}
