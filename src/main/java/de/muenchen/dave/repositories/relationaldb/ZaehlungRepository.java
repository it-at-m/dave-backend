package de.muenchen.dave.repositories.relationaldb;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import de.muenchen.dave.domain.analytics.Zaehlung;

public interface ZaehlungRepository extends JpaRepository<Zaehlung, UUID> { //NOSONAR
}
