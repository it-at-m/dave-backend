/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services.persist;

import de.muenchen.dave.domain.Hochrechnung;
import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.HochrechnungsfaktorDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.BackendIdDTO;
import de.muenchen.dave.domain.dtos.bearbeiten.UpdateStatusDTO;
import de.muenchen.dave.domain.dtos.laden.LadeZaehldatumDTO;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.domain.elasticsearch.Zaehlung;
import de.muenchen.dave.domain.enums.Fahrzeug;
import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.domain.mapper.ZeitintervallMapper;
import de.muenchen.dave.exceptions.BrokenInfrastructureException;
import de.muenchen.dave.exceptions.DataNotFoundException;
import de.muenchen.dave.exceptions.PlausibilityException;
import de.muenchen.dave.services.IndexService;
import de.muenchen.dave.util.dataimport.ZeitintervallBaseUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public abstract class ZaehlungPersistierungsService {

    protected final IndexService indexService;

    protected final ZeitintervallPersistierungsService zeitintervallPersistierungsService;

    protected final ZeitintervallMapper zeitintervallMapper;

    public ZaehlungPersistierungsService(final IndexService indexService,
            final ZeitintervallPersistierungsService zeitintervallPersistierungsService,
            final ZeitintervallMapper zeitintervallMapper) {
        this.indexService = indexService;
        this.zeitintervallPersistierungsService = zeitintervallPersistierungsService;
        this.zeitintervallMapper = zeitintervallMapper;
    }

    /**
     * Methode zum Aktualiseren des Status einer Zählung
     *
     * @param updateStatusDto enthält die Id und den neuen Status
     * @return Id der aktualiserten Zaehlung
     * @throws BrokenInfrastructureException Beim Speichern der Zaehlstelle
     * @throws DataNotFoundException Beim Laden der Zaehlstelle
     * @throws PlausibilityException Beim Pruefen der Daten
     */
    public BackendIdDTO updateStatus(final UpdateStatusDTO updateStatusDto) throws BrokenInfrastructureException, DataNotFoundException, PlausibilityException {
        final Zaehlstelle zaehlstelleByZaehlungId = this.indexService.getZaehlstelleByZaehlungId(updateStatusDto.getZaehlungId());

        for (final Zaehlung zaehlung : zaehlstelleByZaehlungId.getZaehlungen()) {
            if (zaehlung.getId().equalsIgnoreCase(updateStatusDto.getZaehlungId())) {
                zaehlung.setStatus(updateStatusDto.getStatus());
                // WIrd eine Zaehlung beauftragt, so wird die dienstleiserkennung gespeichert
                if (zaehlung.getStatus().equalsIgnoreCase(Status.INSTRUCTED.name())) {
                    zaehlung.setDienstleisterkennung(updateStatusDto.getDienstleisterkennung());
                } else if (zaehlung.getStatus().equalsIgnoreCase(Status.ACCOMPLISHED.name())) {
                    // Wird eine Zaehlung abgeschlossen, dann die Zeitintervalle auslesen und alle Werte berechnen
                    this.zeitintervallPersistierungsService.checkZeitintervalleIfPlausible(zaehlung, this.getNumberOfNecessaryZeitintervalle(zaehlung));
                } else if (zaehlung.getStatus().equalsIgnoreCase(Status.CORRECTION.name())) {
                    // Wird eine Zaehlung korrigert, dann
                    //      alle Zeitintervalle zu den Fahrbeziehungen löschen, die keine FahrbeziehungId enthalten
                    this.zeitintervallPersistierungsService.deleteZeitintervalleForCorrection(zaehlung.getId());
                    // Alle Filenames aus den Knotenarmen löschen
                    zaehlung.getKnotenarme().forEach(arm -> arm.setFilename(""));
                }
                break;
            }
        }
        this.indexService.erneuereZaehlstelle(zaehlstelleByZaehlungId);

        final BackendIdDTO backendIdDto = new BackendIdDTO();

        // Rückgabe der ZaehlungsId
        backendIdDto.setId(updateStatusDto.getZaehlungId());
        return backendIdDto;
    }

    public int getNumberOfNecessaryZeitintervalle(final Zaehlung zaehlung) {
        return Zaehldauer.valueOf(zaehlung.getZaehldauer()).getAnzahlZeitintervalle() * zaehlung.getFahrbeziehungen().size();
    }

    /**
     * Diese Methode erstellt die {@link Hochrechnung} für den {@link Zeitintervall}.
     *
     * @param zeitintervall für dem die Hochrechnung erstellt werden soll.
     * @param hochrechnungsfaktorDto zur Ermittlung der hochgerechneten Werte.
     * @param zaehldauer Zaehldauer als String
     * @return die {@link Hochrechnung}.
     */
    public Hochrechnung createHochrechnung(final Zeitintervall zeitintervall,
            final HochrechnungsfaktorDTO hochrechnungsfaktorDto,
            final String zaehldauer) {
        final LadeZaehldatumDTO ladeZaehldatumDTO = new LadeZaehldatumDTO();
        ladeZaehldatumDTO.setPkw(zeitintervall.getPkw());
        ladeZaehldatumDTO.setLkw(zeitintervall.getLkw());
        ladeZaehldatumDTO.setLastzuege(zeitintervall.getLastzuege());
        ladeZaehldatumDTO.setBusse(zeitintervall.getBusse());
        ladeZaehldatumDTO.setKraftraeder(zeitintervall.getKraftraeder());
        ladeZaehldatumDTO.setFahrradfahrer(zeitintervall.getFahrradfahrer());
        ladeZaehldatumDTO.setFussgaenger(zeitintervall.getFussgaenger());

        final Hochrechnung hochrechnung = new Hochrechnung();
        if (StringUtils.equalsAny(zaehldauer, Zaehldauer.DAUER_16_STUNDEN.toString(), Zaehldauer.DAUER_13_STUNDEN.toString())
                && (ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, Zeitblock.ZB_10_15)
                        || ZeitintervallBaseUtil.isZeitintervallWithinZeitblock(zeitintervall, Zeitblock.ZB_19_24))) {
            hochrechnung.setFaktorKfz(BigDecimal.ZERO);
            hochrechnung.setFaktorSv(BigDecimal.ZERO);
            hochrechnung.setFaktorGv(BigDecimal.ZERO);
        } else {
            hochrechnung.setFaktorKfz(BigDecimal.valueOf(hochrechnungsfaktorDto.getKfz()));
            hochrechnung.setFaktorSv(BigDecimal.valueOf(hochrechnungsfaktorDto.getSv()));
            hochrechnung.setFaktorGv(BigDecimal.valueOf(hochrechnungsfaktorDto.getGv()));
        }
        hochrechnung.setHochrechnungKfz(ladeZaehldatumDTO.getKfz().multiply(hochrechnung.getFaktorKfz()));
        hochrechnung.setHochrechnungGv(ladeZaehldatumDTO.getGueterverkehr().multiply(hochrechnung.getFaktorGv()));
        hochrechnung.setHochrechnungSv(ladeZaehldatumDTO.getSchwerverkehr().multiply(hochrechnung.getFaktorSv()));
        return hochrechnung;
    }

    /**
     * Diese Methode ermittelt aus den {@link Zeitintervall}en, die darin repräsentierten
     * Fahrzeugklassen und Fahrzeugkategorien.
     *
     * @param zeitintervalle zur Ermittlung der Fahrzeugklassen und Fahrzeugkategorien.
     * @return die Fahrzeugklassen und Fahrzeugkategorien abhängig von den {@link Zeitintervall}en.
     */
    public List<Fahrzeug> getFahrzeugKategorienAndFahrzeugklassen(final List<Zeitintervall> zeitintervalle) {
        final Set<Fahrzeug> fahrzeugkategorien = new HashSet<>();
        zeitintervalle.forEach(zeitintervall -> {
            boolean pkweinheiten = false;
            if (ObjectUtils.isNotEmpty(zeitintervall.getPkw())) {
                fahrzeugkategorien.add(Fahrzeug.PKW);
                pkweinheiten = true;
            }
            if (ObjectUtils.isNotEmpty(zeitintervall.getLkw())) {
                fahrzeugkategorien.add(Fahrzeug.LKW);
                pkweinheiten = true;
            }
            if (ObjectUtils.isNotEmpty(zeitintervall.getLastzuege())) {
                fahrzeugkategorien.add(Fahrzeug.LZ);
                pkweinheiten = true;
            }
            if (ObjectUtils.isNotEmpty(zeitintervall.getBusse())) {
                fahrzeugkategorien.add(Fahrzeug.BUS);
                pkweinheiten = true;
            }
            if (ObjectUtils.isNotEmpty(zeitintervall.getKraftraeder())) {
                fahrzeugkategorien.add(Fahrzeug.KRAD);
                pkweinheiten = true;
            }
            if (ObjectUtils.isNotEmpty(zeitintervall.getFahrradfahrer())) {
                fahrzeugkategorien.add(Fahrzeug.RAD);
                pkweinheiten = true;
            }
            if (ObjectUtils.isNotEmpty(zeitintervall.getFussgaenger())) {
                fahrzeugkategorien.add(Fahrzeug.FUSS);
            }
            if (fahrzeugkategorien.contains(Fahrzeug.LKW) || fahrzeugkategorien.contains(Fahrzeug.LZ)) {
                fahrzeugkategorien.add(Fahrzeug.GV);
            }
            if (fahrzeugkategorien.contains(Fahrzeug.GV) || fahrzeugkategorien.contains(Fahrzeug.BUS)) {
                fahrzeugkategorien.add(Fahrzeug.SV);
            }
            if (fahrzeugkategorien.contains(Fahrzeug.SV) || fahrzeugkategorien.contains(Fahrzeug.PKW)) {
                fahrzeugkategorien.add(Fahrzeug.KFZ);
            }
            if (fahrzeugkategorien.contains(Fahrzeug.SV) && fahrzeugkategorien.contains(Fahrzeug.KFZ)) {
                fahrzeugkategorien.add(Fahrzeug.SV_P);
            }
            if (fahrzeugkategorien.contains(Fahrzeug.GV) && fahrzeugkategorien.contains(Fahrzeug.KFZ)) {
                fahrzeugkategorien.add(Fahrzeug.GV_P);
            }
            if (pkweinheiten) {
                fahrzeugkategorien.add(Fahrzeug.PKW_EINHEIT);
            }
        });
        return new ArrayList<>(fahrzeugkategorien);
    }

    public void updateStatusIfDateIsInThePast(final UpdateStatusDTO updateStatusDto, final Status newStatus)
            throws BrokenInfrastructureException, DataNotFoundException {
        final Zaehlung zaehlung = this.indexService.getZaehlung(updateStatusDto.getZaehlungId());
        // Wenn Das Datum der Zählung <= LocalDate.now() ist, dann Status ändern
        if (zaehlung.getDatum().isBefore(LocalDate.now()) || zaehlung.getDatum().isEqual(LocalDate.now())) {
            this.indexService.updateStatusOfZaehlung(updateStatusDto.getZaehlungId(), newStatus);
        }
    }
}
