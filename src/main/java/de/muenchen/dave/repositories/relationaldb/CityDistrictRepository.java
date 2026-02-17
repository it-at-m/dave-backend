package de.muenchen.dave.repositories.relationaldb;

import de.muenchen.dave.domain.CityDistrictEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityDistrictRepository extends JpaRepository<CityDistrictEntity, UUID> {

    CityDistrictEntity findByName(String name);

    List<CityDistrictEntity> findByCity(String city);

    CityDistrictEntity findByNumber(Integer number);

    CityDistrictEntity findByNumberAndCity(Integer number, String city);

}
