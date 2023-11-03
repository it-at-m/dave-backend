package de.muenchen.dave.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient webClient(@Value("${spring.codec.max-in-memory-size}") final int maxInMemorySizeBytes) {
        return WebClient
                .builder()
                .codecs(codecs -> {
                    codecs.defaultCodecs().maxInMemorySize(maxInMemorySizeBytes);
                    codecs.defaultCodecs().enableLoggingRequestDetails(false);
                })
                .build();
    }
}
