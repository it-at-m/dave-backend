package de.muenchen.dave.services.email;

import de.muenchen.dave.domain.EmailAddress;
import de.muenchen.dave.domain.dtos.EmailAddressDTO;
import de.muenchen.dave.domain.enums.Participant;
import de.muenchen.dave.domain.mapper.EmailAddressMapper;
import de.muenchen.dave.repositories.relationaldb.EmailAddressRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class EmailAddressService {

    private final EmailAddressRepository emailAddressRepository;
    private final EmailAddressMapper emailAddressMapper;

    public EmailAddressService(final EmailAddressRepository emailAddressRepository, final EmailAddressMapper emailAddressMapper) {
        this.emailAddressRepository = emailAddressRepository;
        this.emailAddressMapper = emailAddressMapper;
    }

    /**
     * Lädt eine Email-Adresse zu einer Participant ID. Falls sie noch nicht existiert, wird eine neues
     * EmailAddressDTO mit leerem Wert für die Email-Adresse zurückgegeben.
     *
     * @param participantId Die ID des Participants
     * @return geladenes EmailAddressDTO
     */
    public EmailAddressDTO loadEmailAddressByParticipantId(final int participantId) {
        final Optional<EmailAddress> optionalEmailAddress = this.emailAddressRepository.findByParticipantId(participantId);
        if (optionalEmailAddress.isPresent()) {
            return this.emailAddressMapper.bean2Dto(optionalEmailAddress.get());
        } else {
            final EmailAddress emptyEmailAddress = new EmailAddress();
            emptyEmailAddress.setEmailAddress("");
            emptyEmailAddress.setParticipantId(participantId);
            return this.emailAddressMapper.bean2Dto(emptyEmailAddress);
        }
    }

    /**
     * Erneuert den String Email-Adresse anhand der UUID des übergebenen EmailAddressDTOs.
     *
     * @param emailAddressDTO Das zu erneuernde EmailAddressDTO
     * @return das erneuerte EmailAddressDTO
     */
    public EmailAddressDTO saveOrUpdateEmailAddress(final EmailAddressDTO emailAddressDTO) {
        if(emailAddressDTO.getId() != null) {
            final Optional<EmailAddress> byId = this.emailAddressRepository.findById(emailAddressDTO.getId());
            if(byId.isPresent()) {
                return this.updateEmailAddress(byId.get(), emailAddressDTO);
            }
        }
        return this.saveEmailAddress(this.emailAddressMapper.dto2bean(emailAddressDTO));
    }

    private EmailAddressDTO updateEmailAddress(final EmailAddress toUpdate, final EmailAddressDTO newData) {
        toUpdate.setEmailAddress(newData.getEmailAddress());
        return this.saveEmailAddress(toUpdate);
    }

    private EmailAddressDTO saveEmailAddress(final EmailAddress toSave) {
        return this.emailAddressMapper.bean2Dto(this.emailAddressRepository.saveAndFlush(toSave));
    }

    /**
     * Lädt die Email-Adressen. Falls sie noch nicht existiert, wird eine neues
     * EmailAddressDTO mit leerem Wert für die Email-Adresse zurückgegeben.
     *
     * @return geladenes EmailAddressDTO
     */
    public List<EmailAddressDTO> loadEmailAddresses() {
        final List<EmailAddress> emailAddresses = this.emailAddressRepository.findAll();
        if (CollectionUtils.isNotEmpty(emailAddresses)) {
            return this.emailAddressMapper.bean2Dto(emailAddresses);
        } else {
            final EmailAddress emptyEmailAddress = new EmailAddress();
            emptyEmailAddress.setEmailAddress("");
            emptyEmailAddress.setParticipantId(Participant.MOBILITAETSREFERAT.getParticipantId());
            return this.emailAddressMapper.bean2Dto(new ArrayList<>(emailAddresses));
        }
    }

    public void deleteEmailAddress(final UUID id) {
        this.emailAddressRepository.deleteById(id);
    }
}
