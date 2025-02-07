/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.dave.services;

import de.muenchen.dave.repositories.relationaldb.KalendertagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class KalendertagService {

    private final KalendertagRepository kalendertagRepository;

}
