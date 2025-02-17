package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.UnauffaelligeTage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnauffaelligeTageRepository extends JpaRepository<UnauffaelligeTage, UUID> {
}
