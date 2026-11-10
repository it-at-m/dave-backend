package de.muenchen.dave.migrationespg.repositories.relationaldb;

import de.muenchen.dave.migrationespg.domain.relational.messtellen.MessstelleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessstellenIndexRepository extends JpaRepository<MessstelleEntity, UUID> {

    List<MessstelleEntity> findAllBySichtbarDatenportalIsTrue();
    Optional<MessstelleEntity> findByMstId(String mstId);


    @Query(
            value = """
        SELECT ms.*
        FROM {h-schema}messstelle ms
        WHERE array_to_string(ms.suchwoerter, ' ') ILIKE '%' || :query || '%'
        """,
            countQuery = """
        SELECT count(*)
        FROM {h-schema}messstelle ms
        WHERE array_to_string(ms.suchwoerter, ' ') ILIKE '%' || :query || '%'
        """,
            nativeQuery = true
    )
    Page<MessstelleEntity> suggestSearch(@Param("query") String query, Pageable pageable);

}
