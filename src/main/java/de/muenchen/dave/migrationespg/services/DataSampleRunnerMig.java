package de.muenchen.dave.migrationespg.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.dave.migrationespg.domain.relational.zaehlstellen.ZaehlstelleEntity;
import de.muenchen.dave.migrationespg.repositories.relationaldb.ZaehlstellenIndexRepository;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sample")
@RequiredArgsConstructor
public class DataSampleRunnerMig implements CommandLineRunner {

    private final ZaehlstellenIndexRepository zaehlstelleIndex;

    @Override
    public void run(String... args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        InputStream stream = this.getClass().getResourceAsStream("/data/sample-data.json");
        ZaehlstelleEntity zaehlstelle = objectMapper.readValue(stream, ZaehlstelleEntity.class);
        zaehlstelleIndex.save(zaehlstelle);
    }
}
