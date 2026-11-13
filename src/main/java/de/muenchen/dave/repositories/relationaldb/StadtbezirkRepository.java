package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.StadtbezirkEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StadtbezirkRepository extends JpaRepository<StadtbezirkEntity, UUID> {

    List<StadtbezirkEntity> findByStadt(String stadt);

    StadtbezirkEntity findByStadtAndNummer(String stadt, Integer nummer);

}
