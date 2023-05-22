/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ZeitintervallRepository extends JpaRepository<Zeitintervall, UUID> { //NOSONAR

    @Override
    Optional<Zeitintervall> findById(UUID id);

    @Override
    <S extends Zeitintervall> S save(S theEntity);

    @Override
    <S extends Zeitintervall> List<S> saveAll(Iterable<S> entities);

    @Override
    void deleteById(UUID id);

    @Override
    void delete(Zeitintervall entity);

    @Override
    void deleteAll(Iterable<? extends Zeitintervall> entities);

    @Override
    void deleteAll();

    Long deleteAllByZaehlungId(UUID zaehlungId);

    Long deleteAllByFahrbeziehungId(UUID fahrbeziehungId);

    boolean existsByZaehlungId(UUID zaehlungId);

    boolean existsByFahrbeziehungId(UUID fahrbeziehungId);

    List<Zeitintervall> findAll(final Sort sort);

    List<Zeitintervall> findByZaehlungId(final UUID zaehlungId, final Sort sort);

    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndFahrbeziehungVonNotNullAndTypeOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final TypeZeitintervall type);

    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndFahrbeziehungVonAndFahrbeziehungNachAndFahrbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Integer von,
            final Integer nach,
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr,
            final Set<TypeZeitintervall> types);

    Zeitintervall findByZaehlungIdAndTypeAndFahrbeziehungVonAndFahrbeziehungNachAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndFahrbeziehungFahrbewegungKreisverkehrIsNull(
            final UUID zaehlungId,
            final TypeZeitintervall type,
            final Integer fahrbeziehungVon,
            final Integer fahrbeziehungNach,
            final LocalDateTime startuhrzeit,
            final LocalDateTime endeuhrzeit);

    /*
     * Extrahiert den Zeitintervall entsprechend des SortingIndex über alle vorhandenen Fahrbeziehungen.
     */
    Optional<Zeitintervall> findByZaehlungIdAndTypeAndFahrbeziehungVonNullAndFahrbeziehungNachNullAndSortingIndex(
            final UUID zaehlungId,
            final TypeZeitintervall type,
            final Integer sortingIndex);

    /*
     * Die Methode wird verwendet, um die Zeitintervalle, beginnend bei der Startuhrzeit und endend
     * bei der Endeuhrzeit, entsprechend des angegebenen {@link TypeZeitintervall} zu extrahieren.
     * Es werden nur konkrete Fahrbeziehungen einer KREUZUNG (von und nach nicht NULL) berücksichtigt.
     */
    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndFahrbeziehungVonNotNullAndFahrbeziehungNachNotNullAndTypeOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final TypeZeitintervall type);

    /*
     * Die Methode wird verwendet, um die Zeitintervalle, beginnend bei der Startuhrzeit und endend
     * bei der Endeuhrzeit, entsprechend des angegebenen {@link TypeZeitintervall} zu extrahieren.
     * Es werden nur Fahrbeziehungen für einen KREISVERKEHR (von und nach nicht NULL)
     * entsprechend der Ausprägung {@link FahrbewegungKreisverkehr} berücksichtigt.
     */
    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndFahrbeziehungVonNotNullAndFahrbeziehungFahrbewegungKreisverkehrAndTypeOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr,
            final TypeZeitintervall type);

}
