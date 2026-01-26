package de.muenchen.dave.util;

import de.muenchen.dave.domain.dtos.bearbeiten.BearbeiteVerkehrsbeziehungDTO;
import de.muenchen.dave.domain.dtos.laden.FahrbeziehungenDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehlungDTO;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class FahrbeziehungUtil {

    public static FahrbeziehungenDTO determinePossibleVerkehrsbeziehung(final LadeZaehlungDTO ladeZaehlung) {
        final FahrbeziehungenDTO optionsFahrbeziehungen;
        if (ObjectUtils.isNotEmpty(ladeZaehlung.getKreisverkehr()) && ladeZaehlung.getKreisverkehr()) {
            optionsFahrbeziehungen = determinePossibleVerkehrsbeziehungenKreisverkehr(ladeZaehlung);
        } else {
            optionsFahrbeziehungen = determinePossibleVerkehrsbeziehungKreuzung(ladeZaehlung);
        }
        return optionsFahrbeziehungen;
    }

    private static FahrbeziehungenDTO determinePossibleVerkehrsbeziehungenKreisverkehr(final LadeZaehlungDTO ladeZaehlung) {
        final FahrbeziehungenDTO optionsFahrbeziehungen = new FahrbeziehungenDTO();
        optionsFahrbeziehungen.setVonKnotenarme(
                ladeZaehlung.getVerkehrsbeziehungen().stream()
                        .filter(BearbeiteVerkehrsbeziehungDTO::getHinein)
                        .map(BearbeiteVerkehrsbeziehungDTO::getKnotenarm)
                        .collect(Collectors.toCollection(TreeSet::new)));
        optionsFahrbeziehungen.setNachKnotenarme(new HashMap<>());
        final TreeSet<Integer> possibleNachKnotenarme = ladeZaehlung.getVerkehrsbeziehungen().stream()
                .filter(BearbeiteVerkehrsbeziehungDTO::getHeraus)
                .map(BearbeiteVerkehrsbeziehungDTO::getKnotenarm)
                .collect(Collectors.toCollection(TreeSet::new));
        optionsFahrbeziehungen.getVonKnotenarme().forEach(vonKnotenarm -> {
            optionsFahrbeziehungen.getNachKnotenarme().put(
                    vonKnotenarm,
                    possibleNachKnotenarme);
        });
        return optionsFahrbeziehungen;
    }

    private static FahrbeziehungenDTO determinePossibleVerkehrsbeziehungKreuzung(final LadeZaehlungDTO ladeZaehlung) {
        final FahrbeziehungenDTO optionsFahrbeziehungen = new FahrbeziehungenDTO();
        optionsFahrbeziehungen.setVonKnotenarme(new TreeSet<>());
        optionsFahrbeziehungen.setNachKnotenarme(new HashMap<>());
        ladeZaehlung.getVerkehrsbeziehungen().stream()
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
                                possibleNachKnotenarme);
                    }
                });
        return optionsFahrbeziehungen;
    }

}
