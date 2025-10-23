package de.muenchen.dave.configuration;

import de.muenchen.dave.services.pdfgenerator.ImageUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Config for report settings.
 */
@Configuration
@Slf4j
public class ReportConfiguration {

    @Value("${dave.reports.logo-icon}")
    private Resource logoIcon;
    @Value("${dave.reports.logo-subtitle}")
    @Getter
    private String logoSubtitle;

    @Getter
    private String logoIconDataSource;

    @PostConstruct
    public void init() {
        try {
            logoIconDataSource = ImageUtil.getImageDatasource(logoIcon.getContentAsByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Das Logo Icon konnte nicht gelesen werden.", e);
        }
    }

}
