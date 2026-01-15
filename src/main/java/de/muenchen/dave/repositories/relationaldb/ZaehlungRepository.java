package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.analytics.Zaehlung;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZaehlungRepository extends JpaRepository<Zaehlung, UUID> { //NOSONAR
}
