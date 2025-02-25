package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.UnauffaelligerTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnauffaelligeTageRepository extends JpaRepository<UnauffaelligerTag, UUID> {

    List<UnauffaelligerTag> findByMstId(final Integer mstId);

    Optional<UnauffaelligerTag> findTopByOrderByKalendertagDatumDesc();
}
