package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.UnauffaelligeTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UnauffaelligeTageRepository extends JpaRepository<UnauffaelligeTag, UUID> {

    Optional<UnauffaelligeTag> findTopByOrderByDatumDesc();
}
