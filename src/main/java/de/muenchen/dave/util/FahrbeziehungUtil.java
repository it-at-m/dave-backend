/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.util;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteFahrbeziehungDTO;
import de.muenchen.dave.domain.dtos.laden.FahrbeziehungenDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class FahrbeziehungUtil {

    public static FahrbeziehungenDTO determinePossibleFahrbeziehungen(final LadeZaehlungDTO ladeZaehlung) {
        final FahrbeziehungenDTO optionsFahrbeziehungen;
        if (ObjectUtils.isNotEmpty(ladeZaehlung.getKreisverkehr()) && ladeZaehlung.getKreisverkehr()) {
            optionsFahrbeziehungen = determinePossibleFahrbeziehungenKreisverkehr(ladeZaehlung);
        } else {
            optionsFahrbeziehungen = determinePossibleFahrbeziehungenKreuzung(ladeZaehlung);
        }
        return optionsFahrbeziehungen;
    }

    private static FahrbeziehungenDTO determinePossibleFahrbeziehungenKreisverkehr(final LadeZaehlungDTO ladeZaehlung) {
        final FahrbeziehungenDTO optionsFahrbeziehungen = new FahrbeziehungenDTO();
        optionsFahrbeziehungen.setVonKnotenarme(
                ladeZaehlung.getFahrbeziehungen().stream()
                        .filter(BearbeiteFahrbeziehungDTO::getHinein)
                        .map(BearbeiteFahrbeziehungDTO::getKnotenarm)
                        .collect(Collectors.toCollection(TreeSet::new))
        );
        optionsFahrbeziehungen.setNachKnotenarme(new HashMap<>());
        final TreeSet<Integer> possibleNachKnotenarme = ladeZaehlung.getFahrbeziehungen().stream()
                .filter(BearbeiteFahrbeziehungDTO::getHeraus)
                .map(BearbeiteFahrbeziehungDTO::getKnotenarm)
                .collect(Collectors.toCollection(TreeSet::new));
        optionsFahrbeziehungen.getVonKnotenarme().forEach(vonKnotenarm -> {
            optionsFahrbeziehungen.getNachKnotenarme().put(
                    vonKnotenarm,
                    possibleNachKnotenarme
            );
        });
        return optionsFahrbeziehungen;
    }

    private static FahrbeziehungenDTO determinePossibleFahrbeziehungenKreuzung(final LadeZaehlungDTO ladeZaehlung) {
        final FahrbeziehungenDTO optionsFahrbeziehungen = new FahrbeziehungenDTO();
        optionsFahrbeziehungen.setVonKnotenarme(new TreeSet<>());
        optionsFahrbeziehungen.setNachKnotenarme(new HashMap<>());
        ladeZaehlung.getFahrbeziehungen().stream()
                .forEach(bearbeiteFahrbeziehung -> {
                    optionsFahrbeziehungen.getVonKnotenarme().add(bearbeiteFahrbeziehung.getVon());
                    if (optionsFahrbeziehungen.getNachKnotenarme().containsKey(bearbeiteFahrbeziehung.getVon())) {
                        optionsFahrbeziehungen.getNachKnotenarme()
                                .get(bearbeiteFahrbeziehung.getVon())
                                .add(bearbeiteFahrbeziehung.getNach());
                    } else {
                        final TreeSet<Integer> possibleNachKnotenarme = new TreeSet<>();
                        possibleNachKnotenarme.add(bearbeiteFahrbeziehung.getNach());
                        optionsFahrbeziehungen.getNachKnotenarme().put(
                                bearbeiteFahrbeziehung.getVon(),
                                possibleNachKnotenarme
                        );
                    }
                });
        return optionsFahrbeziehungen;
    }

}
