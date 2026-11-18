package de.muenchen.dave.relational.repository;

import de.muenchen.dave.domain.enums.Status;
import de.muenchen.dave.relational.zaehlstellen.ZaehlstelleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZaehlstellenIndexRepository extends JpaRepository<ZaehlstelleEntity, UUID> {

    Optional<ZaehlstelleEntity> findByNummer(String nummer);

    List<ZaehlstelleEntity> findAllByZaehlungenJahr(String jahr);

    Optional<ZaehlstelleEntity> findByZaehlungenId(UUID id);

    List<ZaehlstelleEntity> findAllByNummerStartsWithAndStadtbezirkNummer(String nummer, Integer stadtbezirksnummer);

    List<ZaehlstelleEntity> findAllByZaehlungenStatus(Status status);

    List<ZaehlstelleEntity> findAllByZaehlungenUnreadMessagesMobilitaetsreferatTrue();

    List<ZaehlstelleEntity> findAllByZaehlungenUnreadMessagesDienstleisterTrue();

    // Native Queries benötigen den Schema-Platzhalter für hibernate.default_schema.
    @Query(
            value = """
                    SELECT z.*
                    FROM {h-schema}zaehlstelle z
                    WHERE z.nummer ILIKE '%' || :query || '%'
                       OR z.stadtbezirk ILIKE '%' || :query || '%'
                       OR array_to_string(z.suchwoerter, ' ') ILIKE '%' || :query || '%'
                       OR array_to_string(z.custom_suchwoerter, ' ') ILIKE '%' || :query || '%'
                       OR EXISTS (
                            SELECT 1
                            FROM {h-schema}zaehlung za
                            WHERE za.zaehlstelle_id = z.id
                              AND (array_to_string(za.suchwoerter, ' ') ILIKE '%' || :query || '%'
                                OR za.kreuzungsname ILIKE '%' || :query || '%'
                                OR za.zaehlsituation ILIKE '%' || :query || '%'
                                OR za.zaehlsituation_erweitert ILIKE '%' || :query || '%')
                       )
                    """,
            countQuery = """
                    SELECT count(*)
                    FROM {h-schema}zaehlstelle z
                    WHERE z.nummer ILIKE '%' || :query || '%'
                       OR z.stadtbezirk ILIKE '%' || :query || '%'
                       OR array_to_string(z.suchwoerter, ' ') ILIKE '%' || :query || '%'
                       OR array_to_string(z.custom_suchwoerter, ' ') ILIKE '%' || :query || '%'
                       OR EXISTS (
                            SELECT 1
                            FROM {h-schema}zaehlung za
                            WHERE za.zaehlstelle_id = z.id
                              AND (array_to_string(za.suchwoerter, ' ') ILIKE '%' || :query || '%'
                                OR za.kreuzungsname ILIKE '%' || :query || '%'
                                OR za.zaehlsituation ILIKE '%' || :query || '%'
                                OR za.zaehlsituation_erweitert ILIKE '%' || :query || '%')
                       )
                    """,
            nativeQuery = true
    )
    Page<ZaehlstelleEntity> suggestSearch(@Param("query") String query, Pageable pageable);

    // Unterstützt die von den Services übergebenen Ausdrücke "zaehlungen.status:... OR ...".
    @Query(
            value = """
                    SELECT z.*
                    FROM {h-schema}zaehlstelle z
                    WHERE EXISTS (
                            SELECT 1
                            FROM {h-schema}zaehlung za
                            WHERE za.zaehlstelle_id = z.id
                              AND za.status = ANY (
                                  regexp_split_to_array(replace(:query, 'zaehlungen.status:', ''), '\\s+OR\\s+')
                              )
                       )
                    """,
            countQuery = """
                    SELECT count(*)
                    FROM {h-schema}zaehlstelle z
                    WHERE EXISTS (
                            SELECT 1
                            FROM {h-schema}zaehlung za
                            WHERE za.zaehlstelle_id = z.id
                              AND za.status = ANY (
                                  regexp_split_to_array(replace(:query, 'zaehlungen.status:', ''), '\\s+OR\\s+')
                              )
                       )
                    """,
            nativeQuery = true
    )
    Page<ZaehlstelleEntity> findAllByStatus(@Param("query") String query, Pageable pageable);

}
