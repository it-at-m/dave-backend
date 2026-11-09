package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.StadtbezirkViertelEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadtbezirkViertelRepository extends JpaRepository<StadtbezirkViertelEntity, UUID> {

    List<StadtbezirkViertelEntity> findAllByStadtbezirk_Stadt(String stadt);

}
