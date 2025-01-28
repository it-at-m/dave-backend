/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.services;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.dtos.laden.ZeitauswahlDTO;
import de.muenchen.dave.domain.enums.Zaehldauer;
import de.muenchen.dave.domain.enums.Zeitblock;
import de.muenchen.dave.repositories.relationaldb.ZeitintervallRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Slf4j
@Service
public class ZeitauswahlService {

    private final ZeitintervallRepository zeitintervallRepository;

    public ZeitauswahlService(final ZeitintervallRepository zeitintervallRepository) {
        this.zeitintervallRepository = zeitintervallRepository;
    }

    public ZeitauswahlDTO determinePossibleZeitauswahl(final String zaehldauer, final String zaehlungId) {
        final ZeitauswahlDTO optionsZeitauswahl;
        final Zaehldauer zd = Zaehldauer.valueOf(zaehldauer);

        switch (zd) {
        case DAUER_2_X_4_STUNDEN:
            optionsZeitauswahl = getZeitauswahlFor2x4h();
            break;
        case DAUER_13_STUNDEN:
            optionsZeitauswahl = getZeitauswahlFor13h();
            break;
        case DAUER_16_STUNDEN:
            optionsZeitauswahl = getZeitauswahlFor16h(zaehlungId);
            break;
        case SONSTIGE:
            optionsZeitauswahl = getZeitauswahlForSonstige(zaehlungId);
            break;
        default:
            optionsZeitauswahl = getZeitauswahlFor24h();
            break;
        }
        return optionsZeitauswahl;
    }

    /**
     * Kurzzeiterhebung (6 bis 10 Uhr; 15 bis 19 Uhr)
     *
     * @return
     */
    private ZeitauswahlDTO getZeitauswahlFor2x4h() {
        final ZeitauswahlDTO zeitauswahlDTO = new ZeitauswahlDTO();
        final Set<Zeitblock> blocks = new TreeSet<>();
        blocks.add(Zeitblock.ZB_06_10);
        blocks.add(Zeitblock.ZB_15_19);

        final Set<Zeitblock> hours = new TreeSet<>();
        blocks.forEach(block -> hours.addAll(getAllHoursOfBlock(block)));

        zeitauswahlDTO.setBlocks(blocks);
        zeitauswahlDTO.setHours(hours);
        return zeitauswahlDTO;
    }

    /**
     * Kurzzeiterhebung (6 bis 19Uhr)
     *
     * @return
     */
    private ZeitauswahlDTO getZeitauswahlFor13h() {
        final ZeitauswahlDTO zeitauswahlDTO = new ZeitauswahlDTO();
        final Set<Zeitblock> blocks = new TreeSet<>();
        blocks.add(Zeitblock.ZB_06_10);
        blocks.add(Zeitblock.ZB_10_15);
        blocks.add(Zeitblock.ZB_15_19);
        blocks.add(Zeitblock.ZB_06_19);

        final Set<Zeitblock> hours = new TreeSet<>();
        blocks.forEach(block -> hours.addAll(getAllHoursOfBlock(block)));

        zeitauswahlDTO.setBlocks(blocks);
        zeitauswahlDTO.setHours(hours);
        return zeitauswahlDTO;
    }

    /**
     * @return
     */
    private ZeitauswahlDTO getZeitauswahlFor16h(final String id) {
        final ZeitauswahlDTO zeitauswahlDTO = getZeitauswahlDtoByZeitintervalle(id);
        zeitauswahlDTO.getBlocks().add(Zeitblock.ZB_06_22);
        return zeitauswahlDTO;
    }

    /**
     * Ganztagserhebung
     *
     * @return
     */
    private ZeitauswahlDTO getZeitauswahlFor24h() {
        final ZeitauswahlDTO zeitauswahlDTO = new ZeitauswahlDTO();
        final Set<Zeitblock> blocks = new TreeSet<>();
        blocks.add(Zeitblock.ZB_00_06);
        blocks.add(Zeitblock.ZB_06_10);
        blocks.add(Zeitblock.ZB_10_15);
        blocks.add(Zeitblock.ZB_15_19);
        blocks.add(Zeitblock.ZB_19_24);

        final Set<Zeitblock> hours = new TreeSet<>();
        blocks.forEach(block -> hours.addAll(getAllHoursOfBlock(block)));

        zeitauswahlDTO.setBlocks(blocks);
        zeitauswahlDTO.setHours(hours);
        return zeitauswahlDTO;
    }

    private ZeitauswahlDTO getZeitauswahlForSonstige(final String id) {
        return getZeitauswahlDtoByZeitintervalle(id);
    }

    private Set<Zeitblock> getAllHoursOfBlock(final Zeitblock block) {
        final Set<Zeitblock> hours = new TreeSet<>();
        switch (block) {
        case ZB_00_06:
            hours.addAll(getHoursOf00Bis06());
            break;
        case ZB_06_10:
            hours.addAll(getHoursOf06Bis10());
            break;
        case ZB_10_15:
            hours.addAll(getHoursOf10Bis15());
            break;
        case ZB_15_19:
            hours.addAll(getHoursOf15Bis19());
            break;
        case ZB_19_24:
            hours.addAll(getHoursOf19Bis24());
            break;
        default:
            hours.addAll(getHoursOf00Bis24());
            break;
        }
        return hours;
    }

    private Set<Zeitblock> getHoursOf00Bis06() {
        final Set<Zeitblock> hours = new TreeSet<>();
        hours.add(Zeitblock.ZB_00_01);
        hours.add(Zeitblock.ZB_01_02);
        hours.add(Zeitblock.ZB_02_03);
        hours.add(Zeitblock.ZB_03_04);
        hours.add(Zeitblock.ZB_04_05);
        hours.add(Zeitblock.ZB_05_06);
        return hours;
    }

    private Set<Zeitblock> getHoursOf06Bis10() {
        final Set<Zeitblock> hours = new TreeSet<>();
        hours.add(Zeitblock.ZB_06_07);
        hours.add(Zeitblock.ZB_07_08);
        hours.add(Zeitblock.ZB_08_09);
        hours.add(Zeitblock.ZB_09_10);
        return hours;
    }

    private Set<Zeitblock> getHoursOf10Bis15() {
        final Set<Zeitblock> hours = new TreeSet<>();
        hours.add(Zeitblock.ZB_10_11);
        hours.add(Zeitblock.ZB_11_12);
        hours.add(Zeitblock.ZB_12_13);
        hours.add(Zeitblock.ZB_13_14);
        hours.add(Zeitblock.ZB_14_15);
        return hours;
    }

    private Set<Zeitblock> getHoursOf15Bis19() {
        final Set<Zeitblock> hours = new TreeSet<>();
        hours.add(Zeitblock.ZB_15_16);
        hours.add(Zeitblock.ZB_16_17);
        hours.add(Zeitblock.ZB_17_18);
        hours.add(Zeitblock.ZB_18_19);
        return hours;
    }

    private Set<Zeitblock> getHoursOf19Bis24() {
        final Set<Zeitblock> hours = new TreeSet<>();
        hours.add(Zeitblock.ZB_19_20);
        hours.add(Zeitblock.ZB_20_21);
        hours.add(Zeitblock.ZB_21_22);
        hours.add(Zeitblock.ZB_22_23);
        hours.add(Zeitblock.ZB_23_24);
        return hours;
    }

    private Set<Zeitblock> getHoursOf00Bis24() {
        final Set<Zeitblock> hours = new TreeSet<>();
        hours.addAll(getHoursOf00Bis06());
        hours.addAll(getHoursOf06Bis10());
        hours.addAll(getHoursOf10Bis15());
        hours.addAll(getHoursOf15Bis19());
        hours.addAll(getHoursOf19Bis24());
        return hours;
    }

    private ZeitauswahlDTO getZeitauswahlDtoByZeitintervalle(final String id) {
        final ZeitauswahlDTO zeitauswahlDTO = new ZeitauswahlDTO();
        final Set<Zeitblock> hours = new TreeSet<>();
        final Set<Zeitblock> blocks = new TreeSet<>();
        final List<Zeitintervall> zeitintervalle = zeitintervallRepository.findByZaehlungId(UUID.fromString(id), Sort.by(Sort.Direction.ASC, "startUhrzeit"));
        final Map<Integer, Set<LocalDateTime>> hoursMap = new HashMap<>();
        for (int index = 0; index < 24; index++) {
            hoursMap.put(index, new HashSet<>());
        }
        zeitintervalle.forEach(intervall -> {
            hoursMap.get(intervall.getStartUhrzeit().getHour()).add(intervall.getStartUhrzeit());
        });

        final Map<Zeitblock, Set<Zeitblock>> blocksMap = new HashMap<>();
        blocksMap.put(Zeitblock.ZB_00_06, new HashSet<>());
        blocksMap.put(Zeitblock.ZB_06_10, new HashSet<>());
        blocksMap.put(Zeitblock.ZB_10_15, new HashSet<>());
        blocksMap.put(Zeitblock.ZB_15_19, new HashSet<>());
        blocksMap.put(Zeitblock.ZB_19_24, new HashSet<>());

        hoursMap.forEach((key, value) -> {
            switch (key) {
            case 0:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_00_01);
                    blocksMap.get(Zeitblock.ZB_00_06).add(Zeitblock.ZB_00_01);
                }
                break;
            case 1:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_01_02);
                    blocksMap.get(Zeitblock.ZB_00_06).add(Zeitblock.ZB_01_02);
                }
                break;
            case 2:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_02_03);
                    blocksMap.get(Zeitblock.ZB_00_06).add(Zeitblock.ZB_02_03);
                }
                break;
            case 3:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_03_04);
                    blocksMap.get(Zeitblock.ZB_00_06).add(Zeitblock.ZB_03_04);
                }
                break;
            case 4:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_04_05);
                    blocksMap.get(Zeitblock.ZB_00_06).add(Zeitblock.ZB_04_05);
                }
                break;
            case 5:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_05_06);
                    blocksMap.get(Zeitblock.ZB_00_06).add(Zeitblock.ZB_05_06);
                }
                break;
            case 6:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_06_07);
                    blocksMap.get(Zeitblock.ZB_06_10).add(Zeitblock.ZB_06_07);
                }
                break;
            case 7:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_07_08);
                    blocksMap.get(Zeitblock.ZB_06_10).add(Zeitblock.ZB_07_08);
                }
                break;
            case 8:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_08_09);
                    blocksMap.get(Zeitblock.ZB_06_10).add(Zeitblock.ZB_08_09);
                }
                break;
            case 9:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_09_10);
                    blocksMap.get(Zeitblock.ZB_06_10).add(Zeitblock.ZB_09_10);
                }
                break;
            case 10:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_10_11);
                    blocksMap.get(Zeitblock.ZB_10_15).add(Zeitblock.ZB_10_11);
                }
                break;
            case 11:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_11_12);
                    blocksMap.get(Zeitblock.ZB_10_15).add(Zeitblock.ZB_11_12);
                }
                break;
            case 12:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_12_13);
                    blocksMap.get(Zeitblock.ZB_10_15).add(Zeitblock.ZB_12_13);
                }
                break;
            case 13:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_13_14);
                    blocksMap.get(Zeitblock.ZB_10_15).add(Zeitblock.ZB_13_14);
                }
                break;
            case 14:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_14_15);
                    blocksMap.get(Zeitblock.ZB_10_15).add(Zeitblock.ZB_14_15);
                }
                break;
            case 15:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_15_16);
                    blocksMap.get(Zeitblock.ZB_15_19).add(Zeitblock.ZB_15_16);
                }
                break;
            case 16:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_16_17);
                    blocksMap.get(Zeitblock.ZB_15_19).add(Zeitblock.ZB_16_17);
                }
                break;
            case 17:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_17_18);
                    blocksMap.get(Zeitblock.ZB_15_19).add(Zeitblock.ZB_17_18);
                }
                break;
            case 18:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_18_19);
                    blocksMap.get(Zeitblock.ZB_15_19).add(Zeitblock.ZB_18_19);
                }
                break;
            case 19:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_19_20);
                    blocksMap.get(Zeitblock.ZB_19_24).add(Zeitblock.ZB_19_20);
                }
                break;
            case 20:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_20_21);
                    blocksMap.get(Zeitblock.ZB_19_24).add(Zeitblock.ZB_20_21);
                }
                break;
            case 21:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_21_22);
                    blocksMap.get(Zeitblock.ZB_19_24).add(Zeitblock.ZB_21_22);
                }
                break;
            case 22:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_22_23);
                    blocksMap.get(Zeitblock.ZB_19_24).add(Zeitblock.ZB_22_23);
                }
                break;
            case 23:
                if (value.size() == 4) {
                    hours.add(Zeitblock.ZB_23_24);
                    blocksMap.get(Zeitblock.ZB_19_24).add(Zeitblock.ZB_23_24);
                }
                break;
            }
        });

        blocksMap.forEach((key, value) -> {
            switch (key) {
            case ZB_00_06:
                if (value.size() == 6) {
                    blocks.add(Zeitblock.ZB_00_06);
                }
                break;
            case ZB_06_10:
                if (value.size() == 4) {
                    blocks.add(Zeitblock.ZB_06_10);
                }
                break;
            case ZB_10_15:
                if (value.size() == 5) {
                    blocks.add(Zeitblock.ZB_10_15);
                }
                break;
            case ZB_15_19:
                if (value.size() == 4) {
                    blocks.add(Zeitblock.ZB_15_19);
                }
                break;
            case ZB_19_24:
                if (value.size() == 5) {
                    blocks.add(Zeitblock.ZB_19_24);
                }
                break;
            }
        });

        zeitauswahlDTO.setHours(hours);
        zeitauswahlDTO.setBlocks(blocks);

        return zeitauswahlDTO;
    }

}
