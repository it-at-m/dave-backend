package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.relational.zaehlstellen.ZaehlstelleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZaehlstellenIndexRepository extends JpaRepository<ZaehlstelleEntity, UUID> {

    Optional<ZaehlstelleEntity> findByZaehlungen_Id(UUID zaehlungId);

    /**
     * Sucht Zählstellen anhand des Volltext-Suchfelds {@code suchwoerter_neu}.
     *
     * <p>Der Suchtext wird mit der PostgreSQL-Konfiguration {@code simple} in eine Volltextsuchanfrage
     * umgewandelt. Die Abfrage unterstützt dabei eine websuche-ähnliche Syntax, zum Beispiel Phrasen in
     * Anführungszeichen, {@code OR} und {@code -wort}. Der Operator {@code @@} liefert nur Zählstellen,
     * deren Suchfeld zur Anfrage passt. Die Treffer werden absteigend nach ihrer mit {@code ts_rank}
     * berechneten Relevanz sortiert. {@link Pageable} begrenzt die Ergebnismenge und ermöglicht die
     * Seitennavigation.
     *
     * <p>Da die Abfrage nativ ausgeführt wird, muss das Datenbankschema über den PostgreSQL-
     * {@code search_path} verfügbar sein, z.B.:
     * spring.datasource.hikari.connection-init-sql: SET search_path TO ${db.schema}
     *
     * @param query Volltextsuchanfrage
     * @param pageable Seitengröße und Offset der Ergebnismenge
     * @return passende Zählstellen, nach Relevanz absteigend sortiert
     */
    @Query(
            value = "SELECT * FROM zaehlstelle " +
                    "WHERE suchwoerter_neu @@ websearch_to_tsquery('simple', :query) " +
                    "ORDER BY ts_rank(suchwoerter_neu, websearch_to_tsquery('simple', :query)) DESC",
            nativeQuery = true
    )
    List<ZaehlstelleEntity> findAllBySuchwoerterNeu(@Param("query") String query, Pageable pageable);
}
