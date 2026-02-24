package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.Zeitintervall;
import de.muenchen.dave.domain.enums.Bewegungsrichtung;
import de.muenchen.dave.domain.enums.FahrbewegungKreisverkehr;
import de.muenchen.dave.domain.enums.Himmelsrichtung;
import de.muenchen.dave.domain.enums.TypeZeitintervall;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZeitintervallRepository extends JpaRepository<Zeitintervall, UUID> {

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

    void deleteByBewegungsbeziehungIdIn(final List<UUID> bewegungsbeziehungIds);

    boolean existsByZaehlungId(final UUID zaehlungId);

    List<Zeitintervall> findAll(final Sort sort);

    List<Zeitintervall> findByZaehlungId(final UUID zaehlungId, final Sort sort);

    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndVerkehrsbeziehungNachNotNullAndTypeOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final TypeZeitintervall type);

    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonNotNullAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr,
            final TypeZeitintervall type);

    // Verkehrsbeziehung
    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndTypeInAndVerkehrsbeziehungStrassenseiteOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Integer von,
            final Integer nach,
            final Set<TypeZeitintervall> types,
            final Himmelsrichtung strassenseite);

    // Laengsverkehr
    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndLaengsverkehrKnotenarmAndLaengsverkehrRichtungAndLaengsverkehrStrassenseiteAndTypeInOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Integer knotenarm,
            final Bewegungsrichtung richtung,
            final Himmelsrichtung strassenseite,
            final Set<TypeZeitintervall> types);

    // Querungsverkehr
    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndQuerungsverkehrKnotenarmAndQuerungsverkehrRichtungAndTypeInOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Integer knotenarm,
            final Himmelsrichtung richtung,
            final Set<TypeZeitintervall> types);

    // Verkehrsbeziehung von X nach Y
    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungNachAndTypeInOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Integer von,
            final Integer nach,
            final Set<TypeZeitintervall> types);

    // Verkehrsbeziehung von X nach ALLE und für Kreisverkehr an bestimmten Knotenarm
    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungVonAndVerkehrsbeziehungFahrbewegungKreisverkehrAndTypeInOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Integer von,
            final FahrbewegungKreisverkehr fahrbewegungKreisverkehr,
            final Set<TypeZeitintervall> types);

    // Verkehrsbeziehung von ALLE nach Y
    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndVerkehrsbeziehungNachAndTypeInOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Integer nach,
            final Set<TypeZeitintervall> types);

    // Verkehrsbeziehung von ALLE nach ALLE und für kompletten Kreisverkehr
    List<Zeitintervall> findByZaehlungIdAndStartUhrzeitGreaterThanEqualAndEndeUhrzeitLessThanEqualAndTypeInOrderBySortingIndexAsc(
            final UUID zaehlungId,
            final LocalDateTime startUhrzeit,
            final LocalDateTime endeUhrzeit,
            final Set<TypeZeitintervall> types);
}
