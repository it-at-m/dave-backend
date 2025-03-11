package de.muenchen.dave.domain.model;

import de.muenchen.dave.domain.enums.MessstelleStatus;
import lombok.Data;

@Data
public class MessstelleChangeMessage {

    private String technicalIdMst;

    private String mstId;

    private MessstelleStatus statusAlt;

    private MessstelleStatus statusNeu;

}
