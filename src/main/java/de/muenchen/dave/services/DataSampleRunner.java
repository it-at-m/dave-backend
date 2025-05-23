package de.muenchen.dave.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.dave.domain.elasticsearch.Zaehlstelle;
import de.muenchen.dave.repositories.elasticsearch.ZaehlstelleIndex;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sample")
@RequiredArgsConstructor
public class DataSampleRunner implements CommandLineRunner {

    private final ZaehlstelleIndex zaehlstelleIndex;

    @Override
    public void run(String... args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        InputStream stream = this.getClass().getResourceAsStream("/data/sample-data.json");
        Zaehlstelle zaehlstelle = objectMapper.readValue(stream, Zaehlstelle.class);
        zaehlstelleIndex.save(zaehlstelle);
    }
}
