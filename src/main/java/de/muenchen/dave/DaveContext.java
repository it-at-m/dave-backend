package de.muenchen.dave;

import de.muenchen.dave.domain.PkwEinheit;
import de.muenchen.dave.relational.repository.PkwEinheitRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaveContext {

    @Bean
    public PkwEinheit createSingletonPkwEinheit(PkwEinheitRepository pkwEinheitRepository) {
        return pkwEinheitRepository.findAll().getFirst();  // There is only one
    }
}