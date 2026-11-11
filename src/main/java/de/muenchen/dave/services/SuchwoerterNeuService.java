package de.muenchen.dave.services;

import de.muenchen.dave.domain.relational.messtellen.MessstelleEntity;
import de.muenchen.dave.domain.relational.zaehlstellen.KnotenarmEntity;
import de.muenchen.dave.domain.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.domain.relational.zaehlstellen.ZaehlungEntity;
import de.muenchen.dave.repositories.relationaldb.ZaehlstellenIndexRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SuchwoerterNeuService {

    private final ZaehlstellenIndexRepository zaehlstellenIndexRepository;

    /**
     * Bereitet den Suchvektor für eine Zählstelle-Entity vor. Der aufrufende Persistierungsservice
     * speichert anschließend die Entity einschließlich dieses Felds.
     *
     * @param zaehlstelle zu aktualisierende Entity
     */
    public void reassignSuchwoerterNeu(final ZaehlstelleEntity zaehlstelle) {
        if (zaehlstelle == null) {
            return;
        }

        final List<String> suchwoerter = new ArrayList<>();
        addSuchwort(suchwoerter, zaehlstelle.getNummer());
        addSuchwort(suchwoerter, zaehlstelle.getKommentar());
        CollectionUtils.emptyIfNull(zaehlstelle.getSuchwoerter()).forEach(suchwort -> addSuchwort(suchwoerter, suchwort));
        CollectionUtils.emptyIfNull(zaehlstelle.getZaehlungen()).stream()
                .filter(Objects::nonNull)
                .forEach(zaehlung -> addSuchwoerterFromZaehlung(suchwoerter, zaehlung));

        zaehlstelle.setSuchwoerterNeu(String.join(" ", suchwoerter));
    }

    /**
     * Aktualisiert den Suchvektor der Zählstelle nach einer Änderung an einer Zählung und speichert
     * die Zählstelle anschließend. Die Zählung muss bereits gespeichert sein beziehungsweise sich
     * im selben Persistence Context befinden.
     *
     * @param zaehlung aktualisierte Zählung
     */
    @Transactional
    public void reassignSuchwoerterNeu(final ZaehlungEntity zaehlung) {
        if (zaehlung == null || zaehlung.getId() == null) {
            return;
        }

        zaehlstellenIndexRepository.findByZaehlungen_Id(zaehlung.getId()).ifPresent(zaehlstelle -> {
            reassignSuchwoerterNeu(zaehlstelle);
            zaehlstellenIndexRepository.save(zaehlstelle);
        });
    }

    /**
     * Bereitet den Suchvektor für eine Messstelle-Entity vor. Der aufrufende Persistierungsservice
     * speichert anschließend die Entity einschließlich dieses Felds.
     *
     * @param messstelle zu aktualisierende Entity
     */
    public void reassignSuchwoerterNeu(final MessstelleEntity messstelle) {
        if (messstelle == null) {
            return;
        }

        final List<String> suchwoerter = new ArrayList<>();
        addSuchwort(suchwoerter, messstelle.getMstId());
        addSuchwort(suchwoerter, messstelle.getName());

        messstelle.setSuchwoerterNeu(String.join(" ", suchwoerter));
    }

    private static void addSuchwoerterFromZaehlung(final List<String> suchwoerter, final ZaehlungEntity zaehlung) {
        addSuchwort(suchwoerter, zaehlung.getProjektName());
        addSuchwort(suchwoerter, zaehlung.getProjektNummer());
        addSuchwort(suchwoerter, zaehlung.getKreuzungsname());
        addSuchwort(suchwoerter, zaehlung.getKommentar());
        addSuchwort(suchwoerter, zaehlung.getZaehlsituation());
        addSuchwort(suchwoerter, zaehlung.getZaehlsituationErweitert());

        CollectionUtils.emptyIfNull(zaehlung.getKnotenarme()).stream()
                .filter(Objects::nonNull)
                .map(KnotenarmEntity::getStrassenname)
                .forEach(strassenname -> addSuchwort(suchwoerter, strassenname));
    }

    private static void addSuchwort(final List<String> suchwoerter, final String suchwort) {
        if (StringUtils.isNotBlank(suchwort)) {
            suchwoerter.add(suchwort);
        }
    }
}
