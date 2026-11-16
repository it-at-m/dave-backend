package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.StadtbezirksviertelEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadtbezirksviertelRepository extends JpaRepository<StadtbezirksviertelEntity, UUID> {

    List<StadtbezirksviertelEntity> findAllByStadtbezirk_Stadt(String stadt);

}
