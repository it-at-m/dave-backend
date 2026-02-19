package de.muenchen.dave.services;

import de.muenchen.dave.domain.CityDistrictEntity;
import de.muenchen.dave.repositories.relationaldb.CityDistrictRepository;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class CityDistrictService {

    private final CityDistrictRepository cityDistrictRepository;

    public List<CityDistrictEntity> findAll() {
        return cityDistrictRepository.findAll();
    }

    public List<CityDistrictEntity> findByCity(String city) {
        return cityDistrictRepository.findByCity(city);
    }
}
