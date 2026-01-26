package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZeitintervallRepository extends JpaRepository<Zeitintervall, UUID> { //NOSONAR

    @Override
    Optional<Zeitintervall> findById(final UUID id);

    @Override
    <S extends Zeitintervall> S save(final S theEntity);

    @Override
    <S extends Zeitintervall> List<S> saveAll(final Iterable<S> entities);

    @Override
    void deleteById(final UUID id);

    @Override
    void delete(final Zeitintervall entity);

    @Override
    void deleteAll(final Iterable<? extends Zeitintervall> entities);

    @Override
    void deleteAll();

    Long deleteAllByZaehlungId(final UUID zaehlungId);

    void deleteByBewegungsbeziehungIdIn(final List<UUID> fahrbeziehungIds);

    boolean existsByZaehlungId(final UUID zaehlungId);

    List<Zeitintervall> findAll(final Sort sort);

    List<Zeitintervall> findByZaehlungId(final UUID zaehlungId, final Sort sort);

    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndTypeOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final TypeZeitintervall type);

    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Integer von,
            final Integer nach,
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr,
            final Set<TypeZeitintervall> types);

    Zeitintervall findByZaehlungIdAndTypeAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungFahrbewegungKreisverkehrIsNull(
            final UUID zaehlungId,
            final TypeZeitintervall type,
            final Integer fahrbeziehungVon,
            final Integer fahrbeziehungNach,
            final LocalDateTime startuhrzeit,
            final LocalDateTime endeuhrzeit);

    /*
     * Extrahiert den Zeitintervall entsprechend des SortingIndex über alle vorhandenen Fahrbeziehungen.
     */
    Optional<Zeitintervall> findByZaehlungIdAndTypeAndVerkehrsbeziehungVonNullAndVerkehrsbeziehungNachNullAndSortingIndex(
            final UUID zaehlungId,
            final TypeZeitintervall type,
            final Integer sortingIndex);

    /*
     * Die Methode wird verwendet, um die Zeitintervalle, beginnend bei der Startuhrzeit und endend
     * bei der Endeuhrzeit, entsprechend des angegebenen {@link TypeZeitintervall} zu extrahieren.
     * Es werden nur konkrete Fahrbeziehungen einer KREUZUNG (von und nach nicht NULL) berücksichtigt.
     */
    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndVerkehrsbeziehungNachNotNullAndTypeOrderBySortingIndexAsc(
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
    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr,
            final TypeZeitintervall type);

}
