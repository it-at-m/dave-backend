package de.muenchen.dave.services.pdfgenerator;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Config for report settings.
 */
@Service
@Slf4j
public class ReportLogoService {

    @Value("${dave.reports.logo-icon:#{null}}")
    private Resource logoIcon;
    @Value("${dave.reports.logo-subtitle:}")
    @Getter
    private String logoSubtitle;

    @Getter
    private String logoIconDataSource;

    @PostConstruct
    public void init() {
        if (logoIcon != null) {
            try {
                logoIconDataSource = ImageUtil.getImageDatasource(logoIcon.getContentAsByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Das Logo Icon konnte nicht gelesen werden.", e);
            }
        }
    }

}
